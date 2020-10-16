package org.devocative.talos.vmware;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Result {
	private final String output;
	private final int exitCode;

	public String assertSuccess() {
		if (exitCode != 0) {
			System.err.println(output);
			System.exit(exitCode);
		}

		return output;
	}

	public boolean isSuccessful() {
		return exitCode == 0;
	}
}
