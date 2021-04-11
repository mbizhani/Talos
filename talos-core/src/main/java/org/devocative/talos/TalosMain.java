package org.devocative.talos;


import org.devocative.talos.command.*;
import org.devocative.talos.command.server.CSFetch;
import org.devocative.talos.command.server.CSList;
import org.devocative.talos.command.server.CSSCan;
import org.devocative.talos.command.server.CServer;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.File;

public class TalosMain {

	public static void main(String[] args) {
		final String homeDir = System.getProperty("user.home");
		final File file = new File(homeDir + "/.talos/config.xml");
		file.getParentFile().mkdirs();
		final Context context = new Context(file);

		final CommandLine line = new CommandLine(new TalosCommand());
		line.addSubcommand(new CClone(context))
			.addSubcommand(new CCopy(context))
			.addSubcommand(new CList(context))
			.addSubcommand(new CPs(context))
			.addSubcommand(new CScan(context))
			.addSubcommand(new CSsh(context))
			.addSubcommand(new CExec(context))
			.addSubcommand(new CStart(context))
			.addSubcommand(new CStop(context))
			.addSubcommand(new CRemove(context))
			.addSubcommand(new CCompletion(line))
			.addSubcommand(new CommandLine(new CSnapshot())
				.addSubcommand(new CSnapshot.Create(context))
				.addSubcommand(new CSnapshot.List(context))
				.addSubcommand(new CSnapshot.Remove(context))
				.addSubcommand(new CSnapshot.Revert(context)))
			.addSubcommand(new CommandLine(new CServer())
				.addSubcommand(new CSList(context))
				.addSubcommand(new CSFetch(context))
				.addSubcommand(new CSSCan(context))
			).execute(args);
	}

	@Command(name = "\b")
	private static class TalosCommand {
	}
}
