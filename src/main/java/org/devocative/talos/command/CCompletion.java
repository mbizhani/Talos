package org.devocative.talos.command;

import lombok.RequiredArgsConstructor;
import picocli.AutoComplete;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@RequiredArgsConstructor
@Command(name = "completion")
public class CCompletion implements Runnable {
	private final CommandLine commandLine;

	@Override
	public void run() {
		System.out.println(AutoComplete.bash("talos.sh", commandLine));
	}
}
