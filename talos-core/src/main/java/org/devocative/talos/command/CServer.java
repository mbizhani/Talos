package org.devocative.talos.command;

import org.devocative.talos.Context;
import org.devocative.talos.vmware.esx.VimUtil;
import org.devocative.talos.vmware.esx.VmInfo;
import org.devocative.talos.xml.XServer;
import org.devocative.talos.xml.XVm;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;

import static com.vmware.vim25.VirtualMachinePowerState.POWERED_ON;
import static org.devocative.talos.common.Util.nvl;

@Command(name = "server", description = "Scans ESX server for VMs")
public class CServer extends CAbstract {

	@Parameters(index = "0", paramLabel = "NAME", description = "Server address (IP or hostname)")
	private String name;

	@Option(names = {"-a", "--address"}, description = "Server address (IP or hostname)")
	private String address;

	@Option(names = {"-u", "--username"}, description = "Server login username")
	private String username;

	@Option(names = {"-p", "--password"}, description = "Server login password")
	private String password;

	@Option(names = {"-P", "--persist"}, description = "Stores username and password in Talos config")
	private boolean persist = false;

	@Option(names = {"-A", "--all"}, description = "Get all VMs, both on and off")
	private boolean all = false;

	// ------------------------------

	public CServer(Context context) {
		super(context);
	}

	// ------------------------------

	@Override
	public void run() {
		final XServer server = context
			.getServer(name)
			.orElseGet(() -> {
				printVerbose("Adding new server: %s", name);
				return new XServer(name, address);
			});

		final List<VmInfo> allVMs = VimUtil.getAllVMs(
			server.getAddress(),
			nvl(username, server.getUsername()),
			nvl(password, server.getPassword()));

		printVerbose("ESX[%s]: (%s) number of VM(s)", name, allVMs.size());

		for (VmInfo vmInfo : allVMs) {
			String addition;

			if (all || vmInfo.getRuntimeInfo().getPowerState() == POWERED_ON) {
				final XVm vm = new XVm()
					.setName(vmInfo.getName())
					.setAddress(vmInfo.getGuestInfo().getIpAddress());

				addition = server.addVm(vm) ? "+" : ".";
			} else {
				addition = "-";
			}

			printVerbose("\t(%s)[%s]: Power=[%s] Tools=[%s] IP=[%s] Hostname=[%s]",
				addition,
				vmInfo.getName(),
				vmInfo.getRuntimeInfo().getPowerState(),
				vmInfo.getGuestInfo().getToolsRunningStatus(),
				vmInfo.getGuestInfo().getIpAddress(),
				vmInfo.getGuestInfo().getHostName());
		}

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
