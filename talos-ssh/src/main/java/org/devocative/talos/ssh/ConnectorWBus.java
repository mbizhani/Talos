package org.devocative.talos.ssh;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

class ConnectorWBus {
	private static final Consumer<byte[]> EMPTY = bytes -> {
	};

	private final Map<String, SimpleJSchConnector> connectors = new LinkedHashMap<>();
	private String master;

	// ------------------------------

	public ConnectorWBus(List<SshInfo> infos) {
		for (SshInfo info : infos) {
			SimpleJSchConnector connector = new SimpleJSchConnector(info.getAddress(), 22, info.getUser(), info.getPass(), info.getName());
			connectors.put(info.getName(), connector);
		}
	}

	// ------------------------------

	public SimpleJSchConnector getConnector(String name) {
		return connectors.get(name);
	}

	public void setMaster(String name) {
		if (master != null) {
			if (master.equals(name)) {
				return;
			} else {
				connectors
					.get(master)
					.setWriter(EMPTY);
			}
		}

		master = name;

		final List<SimpleJSchConnector> slaves = connectors
			.entrySet()
			.stream()
			.filter(entry -> !entry.getKey().equals(name))
			.map(Map.Entry::getValue)
			.collect(Collectors.toList());

		final SimpleJSchConnector master = connectors.get(name);
		master.setWriter(bytes -> slaves.forEach(connector -> {
			try {
				connector.write(bytes);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}));
	}

	public void removeMaster() {
		if (master != null) {
			connectors.get(master).setWriter(EMPTY);
			master = null;
		}
	}
}
