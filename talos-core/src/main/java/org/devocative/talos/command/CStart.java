package org.devocative.talos.command;

import org.devocative.talos.Context;
import org.devocative.talos.common.Paraller;
import org.devocative.talos.common.Result;
import org.devocative.talos.vmware.VMCommand;
import org.devocative.talos.vmware.VMRun;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.List;

@Command(name = "start", description = "Starts VM(s)")
public class CStart extends CAbstract {

	@Parameters(arity = "1", paramLabel = "VM_NAME(s)", description = "Name(s) of VM (use 'ls' command)",
		completionCandidates = ParamCompletion.VMListCompletion.class)
	private List<String> names;

	@CommandLine.Option(arity = "1", names = {"--from-snapshot"}, paramLabel = "SNAPSHOT_NAME", description = "Revert to snapshot before start")
	private String snapshotName;

	// ------------------------------

	public CStart(Context context) {
		super(context);
	}

	// ------------------------------

	@Override
	public void run() {
		final Paraller<Result> paraller = new Paraller<>(0);

		for (String name : names) {
			final File vmx = context.getVmx(name);
			printVerbose("Starting VM: name=[%s] vmx=[%s]", name, vmx.getAbsolutePath());

			paraller.addTask(name, () -> {
				if (snapshotName != null) {
					printVerbose("Revert Snapshot: vm=[%s] snapshot=[%s]", name, snapshotName);
					VMRun
						.of(VMCommand.revertToSnapshot)
						.vmxFile(vmx)
						.options(snapshotName)
						.call()
						.assertSuccess();
				}

				return VMRun
					.of(VMCommand.start)
					.vmxFile(vmx)
					.options("nogui")
					.call();
			});
		}

		paraller.execute((name, opt) -> {
			final Result result = opt.orElseGet(() -> new Result(-1, "CANCELED"));

			if (result.isSuccessful()) {
				printVerbose("[%s] started successfully", name);
			} else {
				error("Error[%s]: [%s]", name, result.getOutput());
			}
		});
	}
}
