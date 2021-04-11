package org.devocative.talos.command;

import org.devocative.talos.Context;
import org.devocative.talos.vmware.VMCommand;
import org.devocative.talos.vmware.VMRun;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;

@Command(name = "snapshot", description = "Manage Snapshot(s) of a VM")
public class CSnapshot {

	private static abstract class ASnapshot extends CAbstract {
		@Parameters(index = "0", paramLabel = "VM_NAME", description = "Name of VM",
			completionCandidates = VMListCompletion.class)
		protected String vmName;

		protected ASnapshot(Context context) {
			super(context);
		}
	}

	// ------------------------------

	@Command(name = "create", description = "Create a Snapshot")
	public static class Create extends ASnapshot {
		@Parameters(arity = "0", index = "1", paramLabel = "NAME", description = "Snapshot Name")
		private String snapshotName;

		public Create(Context context) {
			super(context);
		}

		@Override
		public void run() {
			final File vmx = context.getVmx(vmName);
			VMRun
				.of(VMCommand.snapshot)
				.vmxFile(vmx)
				.options(snapshotName)
				.call()
				.assertSuccess();
		}
	}

	@Command(name = "ls", description = "List Snapshot(s)")
	public static class List extends ASnapshot {

		public List(Context context) {
			super(context);
		}

		@Override
		public void run() {
			final File vmx = context.getVmx(vmName);

			final String output = VMRun
				.of(VMCommand.listSnapshots)
				.vmxFile(vmx)
				.call()
				.getOutput();
			System.out.println(output);
		}
	}

	@Command(name = "rm", description = "Remove Snapshot(s)")
	public static class Remove extends ASnapshot {
		@Parameters(arity = "1", index = "1..*", paramLabel = "NAME(s)", description = "Snapshot Name(s)")
		private java.util.List<String> snapshotNames;

		public Remove(Context context) {
			super(context);
		}

		@Override
		public void run() {
			final File vmx = context.getVmx(vmName);

			for (String snapshotName : snapshotNames) {
				VMRun
					.of(VMCommand.deleteSnapshot)
					.vmxFile(vmx)
					.options(snapshotName)
					.call()
					.assertSuccess();
			}
		}
	}

	@Command(name = "revert", description = "Revert to a Snapshot")
	public static class Revert extends ASnapshot {
		@Parameters(arity = "0", index = "1", paramLabel = "NAME", description = "Snapshot Name")
		private String snapshotName;

		public Revert(Context context) {
			super(context);
		}

		@Override
		public void run() {
			final File vmx = context.getVmx(vmName);
			VMRun
				.of(VMCommand.revertToSnapshot)
				.vmxFile(vmx)
				.options(snapshotName)
				.call()
				.assertSuccess();
		}
	}
}
