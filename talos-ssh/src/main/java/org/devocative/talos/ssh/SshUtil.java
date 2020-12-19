package org.devocative.talos.ssh;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.KeyPair;
import com.jcraft.jsch.Session;
import com.jediterm.ssh.jsch.JSchShellTtyConnector;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.AbstractTerminalFrame;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;

public class SshUtil {
	private static final JSch J_SCH = new JSch();

	private static int GEN_KEY_TYPE = KeyPair.RSA;
	private static int GEN_KEY_SIZE = 4096;

	// ------------------------------

	public static void setGenKeyType(int genKeyType) {
		GEN_KEY_TYPE = genKeyType;
	}

	public static void setGenKeySize(int genKeySize) {
		GEN_KEY_SIZE = genKeySize;
	}

	// ------------------------------

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

	public static void generateKeyAndInstall(String keyName, SshInfo info, boolean regenerate) {
		try {
			final File keyFile = new File(System.getProperty("user.home") + "/.ssh/" + keyName);

			if (regenerate || !keyFile.exists()) {
				final KeyPair keyPair = KeyPair.genKeyPair(J_SCH, GEN_KEY_TYPE, GEN_KEY_SIZE);
				keyPair.writePrivateKey(keyFile.getAbsolutePath());
				keyPair.writePublicKey(keyFile.getAbsolutePath() + ".pub", "Created by talos.sh: " + keyName);
				keyPair.dispose();

				Files.setPosixFilePermissions(keyFile.toPath(), PosixFilePermissions.fromString("rw-------"));
			}

			exec(info, "mkdir -p ~/.ssh && chmod 700 ~/.ssh && cat >> ~/.ssh/authorized_keys",
				new FileInputStream(keyFile.getAbsolutePath() + ".pub"));

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void exec(SshInfo info, String cmd, InputStream stdin) {
		try {
			final Session session = J_SCH.getSession(info.getUser(), info.getAddress(), 22);
			session.setPassword(info.getPass());
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect();

			final ChannelExec ch = (ChannelExec) session.openChannel("exec");
			ch.setCommand(cmd);
			ch.setPty(true);

			ch.setInputStream(stdin);
			ch.setOutputStream(System.out);
			ch.setErrStream(System.err);

			final InputStream in = ch.getInputStream();
			ch.connect();

			System.out.print(IOUtils.toString(in, Charset.defaultCharset()));

			ch.disconnect();
			session.disconnect();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
