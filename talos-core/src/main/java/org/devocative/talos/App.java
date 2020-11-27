package org.devocative.talos;


import org.devocative.talos.command.*;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.File;

public class App {

	public static void main(String[] args) {
		final String homeDir = System.getProperty("user.home");
		final File file = new File(homeDir + File.separator + ".talos-config.xml");
		final Context context = new Context(file);

		final CommandLine line = new CommandLine(new TalosCommand());
		line.addSubcommand(new CClone(context))
			.addSubcommand(new CCopy(context))
			.addSubcommand(new CList(context))
			.addSubcommand(new CPs(context))
			.addSubcommand(new CScan(context))
			.addSubcommand(new CSsh(context))
			.addSubcommand(new CStart(context))
			.addSubcommand(new CStop(context))
			.addSubcommand(new CCompletion(line))
			.execute(args);
	}

	@Command(name = "\b")
	private static class TalosCommand {
	}
}