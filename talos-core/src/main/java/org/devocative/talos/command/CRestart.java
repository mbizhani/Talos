package org.devocative.talos.command;

import org.devocative.talos.Context;
import org.devocative.talos.common.Paraller;
import org.devocative.talos.common.Result;
import org.devocative.talos.vmware.VMCommand;
import org.devocative.talos.vmware.VMRun;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.List;

@Command(name = "restart", description = "Restart VM(s)")
public class CRestart extends CAbstract {

	@Parameters(arity = "1", paramLabel = "VM_NAME(s)", description = "Name(s) of VM (use 'ls' command)",
		completionCandidates = ParamCompletion.VMListCompletion.class)
	private List<String> names;

	@Option(names = {"-f", "--force"}, description = "Power-off the VM")
	private boolean force = false;

	// ------------------------------

	public CRestart(Context context) {
		super(context);
	}

	// ------------------------------

	@Override
	public void run() {
		final Paraller<Result> paraller = new Paraller<>(0);

		for (String name : names) {
			final File vmx = context.getVmx(name);

			printVerbose("Restarting VM: name=[%s] vmx=[%s]\n", name, vmx.getAbsolutePath());

			paraller.addTask(name, () -> VMRun
				.of(VMCommand.reset)
				.vmxFile(vmx)
				.options(force ? "hard" : "soft")
				.call()
			);

			paraller.execute((s, opt) -> {
				final Result result = opt.orElseGet(() -> new Result(-1, "CANCELED"));

				if (result.isSuccessful()) {
					printVerbose("[%s] restarted successfully", name);
				} else {
					error(result.getOutput());
				}
			});
		}
	}
}
