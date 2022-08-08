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

@Command(name = "exec", description = "Execute Command/Script")
public class CExec extends CAbstract {

	@Parameters(index = "0", arity = "0", paramLabel = "VM_NAME", description = "Name of VM",
		completionCandidates = ParamCompletion.VMListCompletion.class)
	private String name;

	@Parameters(index = "1..*", paramLabel = "CMD/SCRIPT_PARAMS", description = "Command/Params")
	private List<String> commandOrParams;

	@Option(names = {"-s", "--script"}, paramLabel = "script", description = "Name of Installed Script",
		completionCandidates = ScriptListCompletion.class)
	private String execScript;

	@Option(names = {"-u", "--username"}, paramLabel = "username", description = "VM Login Username")
	private String username;

	@Option(names = {"-p", "--password"}, paramLabel = "password", description = "VM Login Password")
	private String password;

	@Option(names = {"-P", "--persist"}, description = "Store Username and Password in Talos Config")
	private boolean persist = false;

	@Option(names = {"-l"}, description = "List of Installed Script(s)")
	private boolean listScrips = false;

	@Option(names = {"--install-script"}, paramLabel = "script_file", description = "Install Script")
	private File scriptFile;

	// It is handled in the script part of talos.sh
	@Option(names = {"--edit-script"}, paramLabel = "script", description = "Edit Script",
		completionCandidates = ScriptListCompletion.class)
	private String editScript;

	@Option(names = {"--init"}, description = "Init Scripts")
	private boolean init = false;

	// ---------------

	private static final String SCRIPT_HOME_DIR = System.getProperty("user.home") + "/.talos/scripts/";

	// ------------------------------

	public CExec(Context context) {
		super(context);
	}

	// ------------------------------

	@Override
	public void run() {
		/*if (listScrips) {
			printList();
			return;
		}*/

		if (scriptFile != null) {
			installFile();
			return;
		}

		if (init) {
			installOfClasspath();
			return;
		}

		if (name == null) {
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
		if (execScript != null) {
			cmd = loadScript();
		} else {
			cmd = String.join(" ", commandOrParams);
		}

		if (cmd == null) {
			error("No Command!");
		}

		printVerbose("Exec[%s]: <<\n%s\n>>", name, cmd);
		// TIP: https://www.cyberciti.biz/faq/how-to-run-multiple-commands-in-sudo-under-linux-or-unix/
		SshUtil.exec(info, String.format("echo '%s' | sudo -S -p '' -- bash -c '%s'", info.getPass(), cmd));
	}

	// ------------------------------

	private String loadScript() {
		try {
			String content = null;

			final File scriptFile = new File(SCRIPT_HOME_DIR + execScript);
			if (scriptFile.exists()) {
				printVerbose("Load Script[%s]: file:/%s", execScript, scriptFile.getAbsolutePath());
				content = IOUtils.toString(new FileInputStream(scriptFile), Charset.defaultCharset());
			} else {
				final InputStream asStream = getClass().getResourceAsStream("/scripts/" + execScript);
				if (asStream != null) {
					printVerbose("Load Script[%s]: classpath:/scripts/%s", execScript, execScript);
					content = IOUtils.toString(asStream, Charset.defaultCharset());
				} else {
					error("Script Not Found: %s", execScript);
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

			final Map<String, Boolean> vars = findVarsInScript(content);

			for (Map.Entry<String, Boolean> entry : vars.entrySet()) {
				final String var = entry.getKey();
				final boolean required = entry.getValue();
				if (!params.containsKey(var)) {
					ask(var + (required ? " (*): " : ": "), s -> {
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

			return content;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private final static Pattern pattern = Pattern.compile("\\{\\{(.+?)}}");

	private Map<String, Boolean> findVarsInScript(String content) {
		final Map<String, Boolean> result = new LinkedHashMap<>();
		final Matcher matcher = pattern.matcher(content);
		while (matcher.find()) {
			final String param = matcher.group(1);
			if (param.endsWith("*")) {
				result.put(param.substring(0, param.length() - 1), true);
			} else {
				result.put(param, false);
			}
		}
		return result;
	}

	private void installFile() {
		try {
			Files.createDirectories(Paths.get(SCRIPT_HOME_DIR));

			final var dest = new File(SCRIPT_HOME_DIR + scriptFile.getName());
			IOUtils.copy(new FileInputStream(scriptFile), new FileOutputStream(dest));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void installOfClasspath() {
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

	/*private void printList() {
		try {
			final var scriptDir = Paths.get(SCRIPT_HOME_DIR);
			if (Files.exists(scriptDir)) {
				System.out.println(
					Files.list(scriptDir)
						.map(path -> path.getFileName().toString())
						.collect(Collectors.joining(" "))
				);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}*/

	// ------------------------------

	static class ScriptListCompletion extends ArrayList<String> {

		public ScriptListCompletion() {
			super(Collections.singletonList(
				String.format("$(ls %s)", SCRIPT_HOME_DIR)
			));
		}

	}
}
