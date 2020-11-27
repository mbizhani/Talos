package org.devocative.talos.command;

import org.devocative.talos.Context;
import org.devocative.talos.common.Paraller;
import org.devocative.talos.common.Result;
import org.devocative.talos.vmware.VMCommand;
import org.devocative.talos.vmware.VMRun;
import picocli.CommandLine.Command;

import java.io.File;

@Command(name = "ps", description = "Shows running VM(s)")
public class CPs extends CAbstract {
	public CPs(Context context) {
		super(context);
	}

	// ------------------------------

	@Override
	public void run() {
		final String list = VMRun
			.of(VMCommand.list)
			.call()
			.getOutput();
		final String[] lines = list.split("\n");

		System.out.printf("%-10s %-15s %s\n", "NAME", "IP", "VMX ADDRESS");

		final Paraller<String> paraller = new Paraller<>(3);

		for (int i = 1; i < lines.length; i++) {
			final String vmxAddr = lines[i];
			paraller.addTask(vmxAddr, () -> {
				final Result ipResult = VMRun.getIpOf(new File(vmxAddr));
				return ipResult.isSuccessful() ? ipResult.getOutput().trim() : "";
			});
		}

		paraller.execute((vmxAddr, opt) -> {
			final String name = context.findNameByVMX(vmxAddr).orElse("- N/A -");
			System.out.printf("%-10s %-15s %s\n", name, opt.orElse("").trim(), vmxAddr);
		});
	}
}
