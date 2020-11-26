package org.devocative.talos.command;

import com.jediterm.ssh.jsch.JSchShellTtyConnector;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.AbstractTerminalFrame;
import org.devocative.talos.Context;
import org.devocative.talos.ssh.MultiSshPanel;
import org.devocative.talos.ssh.SshInfo;
import org.devocative.talos.vmware.VMRun;
import org.devocative.talos.xml.XUser;
import org.devocative.talos.xml.XVmInfo;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Command(name = "ssh")
public class CSsh extends CAbstract {

	@Parameters(arity = "1", paramLabel = "VM_NAME(s)", description = "Name of VM (use 'ls' command)",
		completionCandidates = VMListCompletion.class)
	private List<String> names;

	@Option(names = {"-u", "--username"})
	private String username;

	@Option(names = {"-p", "--password"})
	private String password;

	@Option(names = {"-P", "--persist"})
	private boolean persist = false;

	/*@Option(names = "--no-gui", negatable = true)
	private boolean gui = true;*/

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
			new AbstractTerminalFrame() {
				@Override
				public TtyConnector createTtyConnector() {
					return new JSchShellTtyConnector(info.getHostname(), 22, info.getUser(), info.getPass(), info.getName());
				}
			};
		} else {
			new MultiSshPanel(sshInfoList);
		}
	}
}