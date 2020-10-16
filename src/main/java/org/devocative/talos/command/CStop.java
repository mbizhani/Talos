package org.devocative.talos.command;

import org.devocative.talos.Context;
import org.devocative.talos.vmware.Result;
import org.devocative.talos.vmware.VMCommand;
import org.devocative.talos.vmware.VMRun;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.List;

@Command(name = "stop")
public class CStop extends CAbstract {

	@Parameters(arity = "1", paramLabel = "VM_NAME(s)", description = "Name(s) of VM (use 'ls' command)")
	private List<String> names;

	// ------------------------------

	public CStop(Context context) {
		super(context);
	}

	// ------------------------------

	@Override
	public void run() {
		for (String name : names) {
			final File vmx = context.getVmx(name);

			System.out.printf("Stopping VM: name=[%s] vmx=[%s]\n", name, vmx.getAbsolutePath());

			final Result rs = VMRun
				.of(VMCommand.stop)
				.vmxFile(vmx)
				.call();

			if (rs.isSuccessful()) {
				System.out.println("VM Stopped Successfully");
			} else {
				System.err.println(rs.getOutput());
			}
		}
	}
}
