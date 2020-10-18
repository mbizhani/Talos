package org.devocative.talos.command;

import org.devocative.talos.Context;
import org.devocative.talos.xml.XVmInfo;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

@Command(name = "scan")
public class CScan extends CAbstract {
	private final FileFilter filter = file -> file.isDirectory() || (file.isFile() && file.getName().endsWith(".vmx"));

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

			System.out.printf("Start scanning from directory '%s' ...\n", file.getAbsolutePath());
			processDir(file);

			if (foundVmxFile == 0) {
				System.out.println("No VMX File Found!");
			} else {
				System.out.printf("[%s] VMX Found, [%s] Newly Added\n", foundVmxFile, foundNewVmxFile);
				if (foundNewVmxFile > 0) {
					context.flush();
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void processDir(File folder) {
		printVerbose("Scanning directory '%s' ...", folder.getAbsolutePath());

		final File[] subItems = folder.listFiles(filter);
		for (File subItem : subItems) {
			if (subItem.isDirectory()) {
				processDir(subItem);
			} else {
				foundVmxFile++;
				final boolean isNew = context.addVmInfo(
					new XVmInfo()
						.setName(subItem.getName().substring(0, subItem.getName().length() - 4))
						.setVmxAddr(subItem.getAbsolutePath())
				);

				if (isNew) {
					foundNewVmxFile++;
					System.out.printf("Found New: '%s'\n", subItem.getAbsolutePath());
				} else {
					System.out.printf("Found Already Added: '%s'\n", subItem.getAbsolutePath());
				}
			}
		}
	}
}
