package org.devocative.talos.ssh;

import com.jediterm.ssh.jsch.JSchShellTtyConnector;

import java.io.IOException;
import java.util.function.Consumer;

class SimpleJSchConnector extends JSchShellTtyConnector {
	private Consumer<byte[]> writer = bytes -> {
	};

	public SimpleJSchConnector(String host, int port, String user, String password, String alias) {
		super(host, port, user, password, alias);
	}

	public void setWriter(Consumer<byte[]> writer) {
		this.writer = writer;
	}

	@Override
	public void write(byte[] bytes) throws IOException {
		if (myOutputStream != null) {
			writer.accept(bytes);
			myOutputStream.write(bytes);
			myOutputStream.flush();
		}
	}
}
