package org.devocative.talos.command;

import org.devocative.talos.Context;
import org.devocative.talos.vmware.Result;
import org.devocative.talos.vmware.VMCommand;
import org.devocative.talos.vmware.VMRun;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.List;

@Command(name = "start")
public class CStart extends CAbstract {

	@Parameters(arity = "1", paramLabel = "VM_NAME(s)", description = "Name(s) of VM (use 'ls' command)")
	private List<String> names;

	// ------------------------------

	public CStart(Context context) {
		super(context);
	}

	// ------------------------------

	@Override
	public void run() {
		for (String name : names) {
			final File vmx = context.getVmx(name);

			printVerbose("Starting VM: name=[%s] vmx=[%s]", name, vmx.getAbsolutePath());

			final Result rs = VMRun
				.of(VMCommand.start)
				.vmxFile(vmx)
				//.options("nogui")
				.call();

			if (rs.isSuccessful()) {
				printVerbose("VM Started Successfully");
			} else {
				System.err.println("Error: " + rs.getOutput());
			}
		}
	}
}
