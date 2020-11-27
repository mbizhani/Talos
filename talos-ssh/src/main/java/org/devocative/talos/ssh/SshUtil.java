package org.devocative.talos.ssh;

import com.jediterm.ssh.jsch.JSchShellTtyConnector;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.AbstractTerminalFrame;

import java.util.List;

public class SshUtil {

	public static void singleSsh(SshInfo info) {
		new AbstractTerminalFrame() {
			@Override
			public TtyConnector createTtyConnector() {
				return new JSchShellTtyConnector(info.getAddress(), 22, info.getUser(), info.getPass(), info.getName());
			}
		};
	}

	public static void multiSsh(List<SshInfo> sshInfoList) {
		new MultiSshPanel(sshInfoList);
	}
}
