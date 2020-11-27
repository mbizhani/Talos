package org.devocative.talos.command;

import org.devocative.talos.Context;
import org.devocative.talos.ssh.SshInfo;
import org.devocative.talos.ssh.SshUtil;
import org.devocative.talos.vmware.VMRun;
import org.devocative.talos.xml.XUser;
import org.devocative.talos.xml.XVmInfo;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Command(name = "ssh", description = "Creates SSH console(s) to VM(s)")
public class CSsh extends CAbstract {

	@Parameters(arity = "1", paramLabel = "VM_NAME(s)", description = "Name of VM(s) (Two or more names shows multi ssh)",
		completionCandidates = VMListCompletion.class)
	private List<String> names;

	@Option(names = {"-u", "--username"}, description = "VM login username")
	private String username;

	@Option(names = {"-p", "--password"}, description = "VM login password")
	private String password;

	@Option(names = {"-P", "--persist"}, description = "Stores username and password in Talos config")
	private boolean persist = false;

	// ------------------------------

	public CSsh(Context context) {
		super(context);
	}

	// ------------------------------

	@Override
	public void run() {
		final List<SshInfo> sshInfoList = new ArrayList<>();
		for (String name : names) {
			final XVmInfo vmInfo = context.getVmInfo(name);

			printVerbose("Getting IP for VM: name=[%s] vmx=[%s]", vmInfo.getName(), vmInfo.getVmxAddr());

			final String hostname = VMRun
				.getIpOf(new File(vmInfo.getVmxAddr()))
				.assertSuccess()
				.trim();
			final XUser ssh = vmInfo.getSshSafely();
			final String user = username != null ? username : ssh.getUser();
			final String pass = username != null ? password : ssh.getPass();

			if (persist && user != null) {
				printVerbose("Persist username and password of SSH for VM name=[%s]", name);
				vmInfo.setSsh(new XUser(user, pass));
				context.flush();
			}

			printVerbose("Start SSH Connection: Name=[%s] Guest=[%s] User=[%s] Pass=[%s]", name, hostname, user, pass);

			sshInfoList.add(new SshInfo(hostname, user, pass, name));
		}

		if (sshInfoList.size() == 1) {
			final SshInfo info = sshInfoList.get(0);
			SshUtil.singleSsh(info);
		} else {
			SshUtil.multiSsh(sshInfoList);
		}
	}
}