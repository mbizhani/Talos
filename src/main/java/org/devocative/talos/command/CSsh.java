package org.devocative.talos.command;

import com.jediterm.ssh.jsch.JSchShellTtyConnector;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.AbstractTerminalFrame;
import org.devocative.talos.Context;
import org.devocative.talos.vmware.VMCommand;
import org.devocative.talos.vmware.VMRun;
import org.devocative.talos.xml.XUser;
import org.devocative.talos.xml.XVmInfo;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;

@Command(name = "ssh")
public class CSsh extends CAbstract {

	@Parameters(paramLabel = "VM_NAME", description = "Name of VM (use 'ls' command)")
	private String name;

	@Option(names = {"-u", "--username"})
	private String username;

	@Option(names = {"-p", "--password"})
	private String password;

	@Option(names = {"-P", "--persist"})
	private boolean persist = false;

	@Option(names = "--no-gui", negatable = true)
	private boolean gui = true;

	// ------------------------------

	public CSsh(Context context) {
		super(context);
	}

	// ------------------------------

	@Override
	public void run() {
		final XVmInfo vmInfo = context.getVmInfo(name);

		printVerbose("Getting IP for VM: name=[%s] vmx=[%s]", vmInfo.getName(), vmInfo.getVmxAddr());

		final String hostname = VMRun
			.of(VMCommand.getGuestIPAddress)
			.vmxFile(new File(vmInfo.getVmxAddr()))
			.call()
			.assertSuccess()
			.trim();
		final XUser ssh = vmInfo.getSshSafely();
		final String user = username != null ? username : ssh.getUser();
		final String pass = username != null ? password : ssh.getPass();

		if (persist && user != null) {
			printVerbose("Persist username and password for VM name=[%s]", name);
			vmInfo.setSsh(new XUser(user, pass));
			context.flush();
		}

		printVerbose("Start SSH Connection: Guest=[%s] User=[%s] Pass=[%s]", hostname, user, pass);

		if (gui) {
			new AbstractTerminalFrame() {
				@Override
				public TtyConnector createTtyConnector() {
					return new JSchShellTtyConnector(hostname, 22, user, pass, name);
				}
			};
		} else {
			//TODO: simple ssh shell
			System.out.printf("sshpass -p %s ssh %s@%s\n", pass, user, hostname);
		}
	}
}