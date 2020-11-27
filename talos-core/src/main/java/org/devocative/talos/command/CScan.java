package org.devocative.talos.command;

import org.devocative.talos.Context;
import org.devocative.talos.xml.XVm;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Command(name = "scan", description = "Scans a directory to find .vmx files")
public class CScan extends CAbstract {

	@Parameters(index = "0", paramLabel = "DIR", description = "Base directory to start scanning recursively")
	private File dir;

	private int foundVmxFile = 0;
	private int foundNewVmxFile = 0;

	// ------------------------------

	public CScan(Context context) {
		super(context);
	}

	// ------------------------------

	@Override
	public void run() {
		try {
			final File file = dir.getCanonicalFile();
			if (!file.exists()) {
				error("'%s' not exists", file.getAbsolutePath());
			}

			if (!file.isDirectory()) {
				error("'%s' must be a directory", file.getAbsolutePath());
			}

			printVerbose("Start scanning from directory '%s'", file.getAbsolutePath());

			Files
				.walk(file.toPath())
				.filter(path -> path.toString().toLowerCase().endsWith(".vmx"))
				.forEach(path -> {
					foundVmxFile++;

					final File vmxFile = path.toFile();
					final boolean isNew = context.addLocalVm(
						new XVm()
							.setName(vmxFile.getName().substring(0, vmxFile.getName().length() - 4))
							.setVmxAddr(vmxFile.getAbsolutePath())
					);

					if (isNew) {
						foundNewVmxFile++;
						printVerbose("Found NEW: '%s'", vmxFile.getAbsolutePath());
					} else {
						printVerbose("Found old: '%s'", vmxFile.getAbsolutePath());
					}
				});

			if (foundVmxFile == 0) {
				printVerbose("No VMX File Found!");
			} else {
				printVerbose("Total [%s] found vmx, [%s] added now", foundVmxFile, foundNewVmxFile);
				if (foundNewVmxFile > 0) {
					context.flush();
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
