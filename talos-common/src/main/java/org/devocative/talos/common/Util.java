package org.devocative.talos.common;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.function.Supplier;

public class Util {
	public static Result exec(List<String> cmdLine) {
		try {
			final ProcessBuilder processBuilder = new ProcessBuilder(cmdLine);

			final Process process = processBuilder.start();

			final StringBuilder output = new StringBuilder();

			final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			String line;
			while ((line = reader.readLine()) != null) {
				output
					.append(line)
					.append("\n");
			}

			final int exitCode = process.waitFor();

			return new Result(exitCode, output.toString());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T nvl(T t1, T t2) {
		return t1 != null ? t1 : t2;
	}

	public static <T> T nvl(T t1, Supplier<T> t2) {
		return t1 != null ? t1 : t2.get();
	}
}
