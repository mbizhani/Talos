package org.devocative.talos.command;

import org.devocative.talos.Context;
import org.devocative.talos.common.Tabular;
import org.devocative.talos.xml.XVm;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Command(name = CList.LIST_CMD, description = "Shows list of VMs in Talos config")
public class CList extends CAbstract {
	public static final String LIST_CMD = "ls";
	public static final String LIST_OPT_NAME_SHORT = "-n";

	@Option(names = {"-U", "--update"}, description = "Update config file based on existing VMs")
	private boolean update = false;

	@Option(names = {LIST_OPT_NAME_SHORT, "--name"}, description = "Show only names in a row")
	private boolean onlyName = false;

	// ------------------------------

	public CList(Context context) {
		super(context);
	}

	// ------------------------------

	@Override
	public void run() {
		if (onlyName) {
			System.out.println(context.getVmList().stream()
				.map(XVm::getFullName)
				.collect(Collectors.joining(" "))
			);
		} else {
			final List<String> removedList = new ArrayList<>();
			final Collection<XVm> vms = context.getVmList();
			final Tabular tabular = new Tabular("NAME", "ADDRESS", "VMX FILE");
			for (XVm vm : vms) {
				if (!update || new File(vm.getVmxAddr()).exists()) {
					tabular.addRow(vm.getFullName(), vm.getAddress(), vm.getVmxAddr());
				} else {
					removedList.add(vm.getFullName());
				}
			}
			tabular.print();

			if (!removedList.isEmpty()) {
				for (String name : removedList) {
					context.removeConfig(name);
				}

				context.flush();

				System.err.printf("Removed VMs Config: %s\n", removedList);
			}
		}
	}
}
