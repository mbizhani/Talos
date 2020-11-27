package org.devocative.talos.command;

import lombok.RequiredArgsConstructor;
import org.devocative.talos.Context;
import picocli.CommandLine.Option;

@RequiredArgsConstructor
abstract class CAbstract implements Runnable {
	protected final Context context;

	@Option(names = {"-V", "--verbose"}, description = "Verbose")
	private boolean verbose = false;

	// ------------------------------

	void printVerbose(String str, Object... args) {
		if (verbose) {
			if (args == null) {
				System.out.println(str);
			} else {
				System.out.printf(str + "\n", args);
			}
		}
	}

	void error(String str, Object... args) {
		error(1, str, args);
	}

	void error(Integer exitCode, String str, Object... args) {
		if (args == null) {
			System.err.println(str);
		} else {
			System.err.printf(str + "\n", args);
		}

		System.exit(exitCode == null ? 1 : exitCode);
	}
}
