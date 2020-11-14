package org.devocative.talos.ssh;

import com.jediterm.ssh.jsch.JSchShellTtyConnector;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.AbstractTerminalFrame;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TabbedSshPanel extends AbstractTerminalFrame {
	private final SshInfo info;

	@Override
	public TtyConnector createTtyConnector() {
		return new JSchShellTtyConnector(info.getHostname(), 22, info.getUser(), info.getPass(), info.getName());
	}
}
