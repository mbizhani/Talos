package org.devocative.talos.command;

import org.apache.commons.io.FileUtils;
import org.devocative.talos.Context;
import org.devocative.talos.xml.XVmInfo;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Command(name = "rm", description = "Removes VM(s) just from config, or entirely all its content (-E)")
public class CRemove extends CAbstract {

	@Parameters(arity = "1", paramLabel = "VM_NAME(s)", description = "Name(s) of VM (use 'ls' command)",
		completionCandidates = VMListCompletion.class)
	private List<String> names;

	@Option(names = {"-E", "--entirely"}, description = "Removes entirely all VM content")
	private boolean entirely = false;

	// ------------------------------

	public CRemove(Context context) {
		super(context);
	}

	// ------------------------------

	@Override
	public void run() {
		if (entirely) {
			ask(String.format("Are you sure to remove [%s] VM%s ENTIRELY? It is NOT REVERSIBLE!!!", names.size(), names.size() > 1 ? "s" : ""),
				Arrays.asList("yes", "no"), "no", s -> {
					if ("yes".equals(s)) {
						remove();
					} else {
						error("Aborted");
					}
				});
		} else {
			ask(String.format("Are you sure to remove [%s] VM%s?", names.size(), names.size() > 1 ? "s" : ""),
				Arrays.asList("y", "n"), "n", s -> {
					if ("y".equals(s)) {
						remove();
					} else {
						error("Aborted");
					}
				});
		}
	}

	// ------------------------------

	private void remove() {
		for (String name : names) {
			final XVmInfo vmInfo = context.removeConfig(name);
			printVerbose("[%s]: removed from config.", name);

			if (entirely) {
				final File vmxFile = new File(vmInfo.getVmxAddr());

				if (vmxFile.exists()) {
					final File vmDir = vmxFile.getParentFile();
					final String vmDirName = vmDir.getAbsolutePath();

					if (vmDir.isDirectory() && vmDir.getName().equalsIgnoreCase(name)) {
						rmDir(vmDir);
						printVerbose("[%s]: [%s] removed entirely!", name, vmDirName);
					} else {
						error("Error removing [%s]: mismatched parent dir [%s]", name, vmDirName);
					}
				}
			}

			context.flush();
		}
	}

	private void rmDir(File dir) {
		try {
			FileUtils.deleteDirectory(dir);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}