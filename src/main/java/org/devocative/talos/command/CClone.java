package org.devocative.talos.command;

import org.devocative.talos.Context;
import org.devocative.talos.vmware.Result;
import org.devocative.talos.vmware.VMCommand;
import org.devocative.talos.vmware.VMRun;
import org.devocative.talos.xml.XUser;
import org.devocative.talos.xml.XVmInfo;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.List;

@Command(name = "clone")
public class CClone extends CAbstract {

	@Parameters(index = "0", paramLabel = "VM_NAME", description = "Name of VM (use 'ls' command)")
	private String name;

	@Parameters(arity = "1", index = "1..*", paramLabel = "NEW_VM_NAME(s)", description = "Name(s) for New VM")
	private List<String> newNames;

	// ------------------------------

	public CClone(Context context) {
		super(context);
	}

	// ------------------------------

	@Override
	public void run() {
		final File cloneBaseDir = new File(context.getCloneBaseDir());
		printVerbose("Checking clone base dir: [%s]", cloneBaseDir.getAbsolutePath());

		if (!cloneBaseDir.exists()) {
			cloneBaseDir.mkdirs();
			printVerbose("Clone base dir created");
		}

		for (String newName : newNames) {
			final String newVMX = cloneBaseDir.getAbsolutePath() + File.separator + newName + File.separator + newName + ".vmx";

			printVerbose("Cloning new VM: vmx=[%s]", newVMX);

			final XVmInfo srcVmInfo = context.getVmInfo(name);

			final Result rs = VMRun
				.of(VMCommand.clone)
				.vmxFile(new File(srcVmInfo.getVmxAddr()))
				.options(newVMX, "full", "-cloneName=" + newName)
				.call();

			if (rs.isSuccessful()) {
				printVerbose("Clone done successfully!");

				context.addVmInfo(new XVmInfo()
					.setName(newName)
					.setVmxAddr(newVMX)
					.setSsh(new XUser(srcVmInfo.getSsh()))
					.setGuest(new XUser(srcVmInfo.getGuest())));

				context.flush();
			} else {
				error(rs.getOutput());
			}
		}
	}
}
