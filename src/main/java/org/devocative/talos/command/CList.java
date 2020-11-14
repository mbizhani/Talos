package org.devocative.talos.command;

import org.devocative.talos.Context;
import org.devocative.talos.xml.XVmInfo;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Command(name = "ls")
public class CList extends CAbstract {
	@Option(names = {"-U", "--update"})
	private boolean update = false;

	// ------------------------------

	public CList(Context context) {
		super(context);
	}

	// ------------------------------

	@Override
	public void run() {
		final List<String> removedList = new ArrayList<>();
		final Collection<XVmInfo> vms = context.getVmList();
		System.out.printf("%-10s %s\n", "NAME", "VMX ADDRESS");
		for (XVmInfo vm : vms) {
			if (!update || new File(vm.getVmxAddr()).exists()) {
				System.out.printf("%-10s %s\n", vm.getName(), vm.getVmxAddr());
			} else {
				removedList.add(vm.getName());
			}
		}

		if (!removedList.isEmpty()) {
			for (String name : removedList) {
				context.removeConfig(name);
			}

			context.flush();

			System.err.printf("Removed VMs Config: %s\n", removedList);
		}
	}
}
