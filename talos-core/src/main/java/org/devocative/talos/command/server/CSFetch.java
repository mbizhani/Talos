package org.devocative.talos.command.server;

import org.devocative.talos.Context;
import org.devocative.talos.common.Tabular;
import org.devocative.talos.vmware.esx.VmInfo;
import org.devocative.talos.xml.XServer;
import picocli.CommandLine.Command;

import java.util.List;

@Command(name = "fetch", description = "Fetches VM list from server and prints")
public class CSFetch extends CSAbstractRemote {

	public CSFetch(Context context) {
		super(context);
	}

	// ------------------------------

	@Override
	protected void run(XServer server, List<VmInfo> allVMs) {
		System.out.printf("[%s - %s]\n", server.getName(), server.getAddress());

		final Tabular tabular = new Tabular("NAME", "POWER", "VM-TOOLS", "IP", "HOSTNAME", "OS");
		allVMs.forEach(vmInfo -> tabular.addRow(
			vmInfo.getName(),
			vmInfo.getRuntimeInfo().getPowerState().toString(),
			vmInfo.getGuestInfo().getToolsRunningStatus(),
			vmInfo.getGuestInfo().getIpAddress(),
			vmInfo.getGuestInfo().getHostName(),
			vmInfo.getGuestInfo().getGuestFullName())
		);
		tabular.print();
	}
}
