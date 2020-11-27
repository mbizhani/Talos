package org.devocative.talos.command;

import lombok.RequiredArgsConstructor;
import picocli.AutoComplete;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@RequiredArgsConstructor
@Command(name = "completion",
	description = "Generates bash completion script (add entry 'source <(talos.sh completion)' in your '~/.bashrc')")
public class CCompletion implements Runnable {
	static final String TALOS_CMD = "talos.sh";

	private final CommandLine commandLine;

	// ------------------------------

	@Override
	public void run() {
		System.out.println(AutoComplete.bash(TALOS_CMD, commandLine));
	}
}
