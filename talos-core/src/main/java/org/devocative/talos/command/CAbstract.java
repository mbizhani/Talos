package org.devocative.talos.command;

import lombok.RequiredArgsConstructor;
import org.devocative.talos.Context;
import picocli.CommandLine.Option;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class CAbstract implements Runnable {
	protected final Context context;

	@Option(names = {"-V", "--verbose"}, description = "Verbose")
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

	protected void error(String str, Object... args) {
		error(1, str, args);
	}

	protected void error(Integer exitCode, String str, Object... args) {
		if (args == null) {
			System.err.println(str);
		} else {
			System.err.printf(str + "\n", args);
		}

		System.exit(exitCode == null ? 1 : exitCode);
	}

	void ask(String question, Consumer<String> action) {
		ask(question, Collections.emptyList(), null, action);
	}

	void ask(String question, List<String> options, String defaultOption, Consumer<String> action) {
		if (!options.isEmpty()) {
			if (!options.contains(defaultOption)) {
				throw new RuntimeException(String.format("Invalid default [%s]: options=%s", defaultOption, options));
			}

			final StringBuilder builder = new StringBuilder();
			builder
				.append(question)
				.append(" (")
				.append(options
					.stream()
					.map(String::toLowerCase)
					.collect(Collectors.joining("/")))
				.append(")[")
				.append(defaultOption.toUpperCase())
				.append("] ");

			System.out.print(builder.toString());

			final String opt = readLine();
			action.accept(opt == null || opt.isEmpty() || !options.contains(opt) ? defaultOption : opt);
		} else {
			action.accept(readLine());
		}
	}

	// ------------------------------

	private String readLine() {
		try {
			final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			return reader.readLine();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
