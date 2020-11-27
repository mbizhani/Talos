package org.devocative.talos.vmware;

import org.devocative.talos.common.ExecUtil;
import org.devocative.talos.common.Result;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

public class VMRun implements Callable<Result> {
	private final VMCommand vmCmd;
	private File vmxFile;
	private String guestUser;
	private String guestPass;
	private String[] options;

	// ------------------------------

	private VMRun(VMCommand vmCmd) {
		this.vmCmd = vmCmd;
	}

	public static VMRun of(VMCommand vmCmd) {
		return new VMRun(vmCmd);
	}

	public static Result getIpOf(File vmxFile) {
		return VMRun
			.of(VMCommand.getGuestIPAddress)
			.vmxFile(vmxFile)
			.options("-wait")
			.call();
	}

	// ------------------------------

	@Override
	public Result call() {
		final List<String> cmdLine = new ArrayList<>();
		cmdLine.add("vmrun");
		if (guestUser != null) {
			cmdLine.add("-gu");
			cmdLine.add(guestUser);
		}
		if (guestPass != null) {
			cmdLine.add("-gp");
			cmdLine.add(guestPass);
		}
		cmdLine.add(vmCmd.name());
		if (vmxFile != null) {
			if (!vmxFile.exists()) {
				throw new RuntimeException("VMX Not Found: " + vmxFile.getAbsolutePath());
			}
			cmdLine.add(vmxFile.getAbsolutePath());
		}

		if (options != null) {
			cmdLine.addAll(Arrays.asList(options));
		}

		return ExecUtil.exec(cmdLine);
	}

	public VMRun vmxFile(File vmxFile) {
		this.vmxFile = vmxFile;
		return this;
	}

	public VMRun guestUser(String guestUser) {
		this.guestUser = guestUser;
		return this;
	}

	public VMRun guestPass(String guestPass) {
		this.guestPass = guestPass;
		return this;
	}

	public VMRun options(String... options) {
		this.options = options;
		return this;
	}
}
