package org.devocative.talos.command;

import com.google.common.base.Strings;
import org.apache.commons.io.IOUtils;
import org.devocative.talos.Context;
import org.devocative.talos.ssh.SshInfo;
import org.devocative.talos.ssh.SshUtil;
import org.devocative.talos.xml.XUser;
import org.devocative.talos.xml.XVm;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Command(name = "exec", description = "Execute command")
public class CExec extends CAbstract {

	@Parameters(index = "0", arity = "0", paramLabel = "VM_NAME", description = "Name of VM",
		completionCandidates = VMListCompletion.class)
	private String name;

	@Parameters(index = "1..*", paramLabel = "CMD", description = "Command/Params")
	private List<String> commandOrParams;

	@Option(names = {"-S", "--script"}, description = "Name of installed script",
		completionCandidates = ScriptListCompletion.class)
	private String script;

	@Option(names = {"-s", "--stdin"}, description = "Standard Input for Command")
	private String stdin;

	@Option(names = {"-u", "--username"}, description = "VM login username")
	private String username;

	@Option(names = {"-p", "--password"}, description = "VM login password")
	private String password;

	@Option(names = {"-P", "--persist"}, description = "Stores username and password in Talos config")
	private boolean persist = false;

	@Option(names = {"-L", "--list-scripts"}, description = "Name of installed scripts")
	private boolean listScrips = false;

	@Option(names = {"-I", "--install-scripts"}, description = "Install scripts")
	private boolean installScrips = false;

	// ---------------

	private static final String SCRIPT_HOME_DIR = System.getProperty("user.home") + "/.talos/scripts/";

	// ------------------------------

	public CExec(Context context) {
		super(context);
	}

	// ------------------------------

	@Override
	public void run() {
		if (listScrips) {
			printList();
			return;
		}

		if (installScrips) {
			install();
			return;
		}

		final XVm vm = context.getVm(name);
		final String address = vm.getAddressSafely();
		final XUser ssh = vm.getSshSafely(username, password, persist);
		if (persist) {
			printVerbose("Persist username and password of SSH for VM name=[%s]", name);
			context.flush();
		}
		final SshInfo info = new SshInfo(address, ssh.getUser(), ssh.getPass(), name);

		final String cmd;
		if (script != null) {
			cmd = loadScript();
		} else {
			cmd = String.join(" ", commandOrParams);
		}

		if (cmd == null) {
			error("No Command!");
		}

		printVerbose("Exec[%s]: <<\n%s\n>>", name, cmd);
		String stdIn = (stdin != null ? stdin : "") + "\n";
		SshUtil.exec(info, cmd, new ByteArrayInputStream(stdIn.getBytes()));
	}

	// ------------------------------

	private String loadScript() {
		try {
			String content = null;

			final File scriptFile = new File(SCRIPT_HOME_DIR + script);
			if (scriptFile.exists()) {
				printVerbose("Load Script[%s]: file:/%s", script, scriptFile.getAbsolutePath());
				content = IOUtils.toString(new FileInputStream(scriptFile), Charset.defaultCharset());
			} else {
				final InputStream asStream = getClass().getResourceAsStream("/scripts/" + script);
				if (asStream != null) {
					printVerbose("Load Script[%s]: classpath:/scripts/%s", script, script);
					content = IOUtils.toString(asStream, Charset.defaultCharset());
				} else {
					error("Script Not Found: %s", script);
				}
			}

			final Map<String, String> params = new HashMap<>();
			if (commandOrParams != null) {
				for (String param : commandOrParams) {
					final int idx = param.indexOf('=');
					if (idx < 0) {
						error("Invalid param for script: %s", param);
					}
					final String key = param.substring(0, idx);
					final String value = param.substring(idx + 1);
					params.put(key, value);
				}
			}

			if (content != null) {
				final Map<String, Boolean> vars = findVarsInScript(content);

				for (Map.Entry<String, Boolean> entry : vars.entrySet()) {
					final String var = entry.getKey();
					final boolean required = entry.getValue();
					if (!params.containsKey(var)) {
						ask(var + ": ", s -> {
							if (Strings.isNullOrEmpty(s) && required) {
								error("Required Param: %s", var);
							}
							params.put(var, s != null ? s : "");
						});
					}
				}

				for (Map.Entry<String, String> entry : params.entrySet()) {
					content = content.replace("{{" + entry.getKey() + "}}", entry.getValue());
					content = content.replace("{{" + entry.getKey() + "*}}", entry.getValue());
				}
			}

			return content;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private final static Pattern pattern = Pattern.compile("\\{\\{(\\w+?\\*?)}}");

	private Map<String, Boolean> findVarsInScript(String content) {
		final Map<String, Boolean> result = new HashMap<>();
		final Matcher matcher = pattern.matcher(content);
		while (matcher.find()) {
			final String param = matcher.group(1);
			if (param.endsWith("*")) {
				result.put(param.substring(0, param.length() - 1), true);
			} else if (!result.containsKey(param)) {
				result.put(param, false);
			}
		}
		return result;
	}

	private void install() {
		try {
			Files.createDirectories(Paths.get(SCRIPT_HOME_DIR));

			final URI uri = getClass().getResource("/scripts/").toURI();
			final Path scripts;
			if (uri.getScheme().equals("jar")) {
				FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
				scripts = fileSystem.getPath("/scripts/");
			} else {
				scripts = Paths.get(uri);
			}
			Files.list(scripts).forEach(path -> {
				try {
					final String fileName = path.getFileName().toString();
					final String dest = SCRIPT_HOME_DIR + fileName;
					printVerbose("Installing: [%s] -> [%s]", fileName, dest);
					final InputStream asStream = getClass().getResourceAsStream("/scripts/" + fileName);
					IOUtils.copy(asStream, new FileOutputStream(dest));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void printList() {
		try {
			System.out.println(
				Files.list(Paths.get(SCRIPT_HOME_DIR))
					.map(path -> path.getFileName().toString())
					.collect(Collectors.joining(" "))
			);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// ------------------------------

	static class ScriptListCompletion extends ArrayList<String> {

		public ScriptListCompletion() {
			super(Collections.singletonList(
				String.format("$(%s %s %s)", CCompletion.TALOS_CMD, "exec", "-L")
			));
		}

	}
}
