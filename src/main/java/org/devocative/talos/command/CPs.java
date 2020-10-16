package org.devocative.talos.command;

import org.devocative.talos.Context;
import org.devocative.talos.vmware.Result;
import org.devocative.talos.vmware.VMCommand;
import org.devocative.talos.vmware.VMRun;
import picocli.CommandLine.Command;

import java.io.File;

@Command(name = "ps")
public class CPs extends CAbstract {
	public CPs(Context context) {
		super(context);
	}

	@Override
	public void run() {
		final String list = VMRun
			.of(VMCommand.list)
			.call()
			.getOutput();
		final String[] lines = list.split("\n");

		System.out.printf("%-10s %-15s %s\n", "NAME", "IP", "VMX ADDRESS");

		for (int i = 1; i < lines.length; i++) {
			String vmxAddr = lines[i];
			final String name = context.findNameByVMX(vmxAddr).orElse("- N/A -");
			final Result ipResult = VMRun
				.of(VMCommand.getGuestIPAddress)
				.vmxFile(new File(vmxAddr))
				.call();
			final String ip = ipResult.isSuccessful() ? ipResult.getOutput() : "";
			System.out.printf("%-10s %-15s %s\n", name, ip.trim(), vmxAddr);
		}
	}
}
