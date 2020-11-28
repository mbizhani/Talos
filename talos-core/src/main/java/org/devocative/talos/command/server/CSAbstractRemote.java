package org.devocative.talos.command.server;

import org.devocative.talos.Context;
import org.devocative.talos.command.CAbstract;
import org.devocative.talos.vmware.esx.VimUtil;
import org.devocative.talos.vmware.esx.VmInfo;
import org.devocative.talos.xml.XServer;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;

import static org.devocative.talos.common.Util.nvl;

abstract class CSAbstractRemote extends CAbstract {
	@Parameters(index = "0", paramLabel = "NAME", description = "Server Name", completionCandidates = ServerListCompletion.class)
	private String name;

	@Option(names = {"-a", "--address"}, description = "Server address (IP or hostname)")
	private String address;

	@Option(names = {"-u", "--username"}, description = "Server login username")
	private String username;

	@Option(names = {"-p", "--password"}, description = "Server login password")
	private String password;

	@Option(names = {"-P", "--persist"}, description = "Stores username and password in Talos config")
	private boolean persist = false;

	// ------------------------------

	CSAbstractRemote(Context context) {
		super(context);
	}

	// ------------------------------

	protected abstract void run(XServer server, List<VmInfo> allVMs);

	// ------------------------------

	@Override
	public void run() {
		final XServer server = context
			.getServer(name, address)
			.orElseGet(() -> {
				printVerbose("Adding new server: %s", name);
				return new XServer(name, address);
			});

		final String user = nvl(username, server.getUsername());
		final String pass = nvl(password, server.getPassword());

		if (server.getAddress() == null) {
			error("New server: address required (set '-a' or '--address')");
		} else if (user == null || pass == null) {
			error("Invalid server login, set username and password switches");
		}

		final List<VmInfo> allVMs = VimUtil.getAllVMs(server.getAddress(), user, pass);

		run(server, allVMs);

		if (persist) {
			if (username != null) {
				server.setUsername(username);
			}

			if (password != null) {
				server.setPassword(password);
			}
		}

		context.addServer(server);
		context.flush();
	}
}
