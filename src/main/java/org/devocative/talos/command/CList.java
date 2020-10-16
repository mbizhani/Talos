package org.devocative.talos.command;

import org.devocative.talos.Context;
import org.devocative.talos.xml.XVmInfo;
import picocli.CommandLine.Command;

import java.util.Collection;

@Command(name = "ls")
public class CList extends CAbstract {
	public CList(Context context) {
		super(context);
	}

	@Override
	public void run() {
		final Collection<XVmInfo> vms = context.getVmList();
		System.out.printf("%-10s %s\n", "NAME", "VMX ADDRESS");
		for (XVmInfo vm : vms) {
			System.out.printf("%-10s %s\n", vm.getName(), vm.getVmxAddr());
		}
	}
}
