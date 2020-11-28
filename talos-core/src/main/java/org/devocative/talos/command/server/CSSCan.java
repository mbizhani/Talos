package org.devocative.talos.command.server;

import org.devocative.talos.Context;
import org.devocative.talos.vmware.esx.VmInfo;
import org.devocative.talos.xml.XServer;
import org.devocative.talos.xml.XVm;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;

import static com.vmware.vim25.VirtualMachinePowerState.POWERED_ON;

@Command(name = "scan", description = "Fetches VM list and store in config")
public class CSSCan extends CSAbstractRemote {

	@Option(names = {"-A", "--all"}, description = "Get all VMs, both on and off")
	private boolean all = false;

	// ------------------------------

	public CSSCan(Context context) {
		super(context);
	}

	// ------------------------------

	@Override
	protected void run(XServer server, List<VmInfo> allVMs) {
		printVerbose("ESX[%s]: (%s) number of VM(s)", server.getName(), allVMs.size());

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
	}
}
