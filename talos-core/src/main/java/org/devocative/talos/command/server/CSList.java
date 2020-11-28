package org.devocative.talos.command.server;

import org.devocative.talos.Context;
import org.devocative.talos.command.CAbstract;
import org.devocative.talos.common.Tabular;
import org.devocative.talos.xml.XServer;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.stream.Collectors;

@Command(name = "ls")
public class CSList extends CAbstract {

	@Option(names = {"-n", "--name"}, description = "Show only names in a row")
	private boolean onlyName = false;

	// ------------------------------

	public CSList(Context context) {
		super(context);
	}

	// ------------------------------

	@Override
	public void run() {
		if (onlyName) {
			System.out.println(context.getServerList().stream().map(XServer::getName).collect(Collectors.joining(" ")));
		} else {
			final Tabular tabular = new Tabular("NAME", "ADDRESS");
			context.getServerList().forEach(server -> tabular.addRow(server.getName(), server.getAddress()));
			tabular.print();
		}
	}
}
