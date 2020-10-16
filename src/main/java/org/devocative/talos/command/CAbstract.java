package org.devocative.talos.command;

import lombok.RequiredArgsConstructor;
import org.devocative.talos.Context;
import picocli.CommandLine.Option;

@RequiredArgsConstructor
abstract class CAbstract implements Runnable {
	protected final Context context;

	@Option(names = "-V", description = "Verbose")
	private boolean verbose = false;

	// ------------------------------

	protected void printVerbose(String str, Object... args) {
		if (verbose) {
			if (args == null) {
				System.out.println(str);
			} else {
				System.out.printf(str + "\n", args);
			}
		}
	}
}
