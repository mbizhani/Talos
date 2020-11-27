package org.devocative.talos.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
public class Result {
	private final int exitCode;
	private final String output;

	public String assertSuccess() {
		if (exitCode != 0) {
			System.err.printf("Exit(%s): %s\n", exitCode, output);
			System.exit(exitCode);
		}

		return output;
	}

	public boolean isSuccessful() {
		return exitCode == 0;
	}
}
