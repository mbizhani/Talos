package org.devocative.talos;

import com.thoughtworks.xstream.XStream;
import org.devocative.talos.xml.XRoot;
import org.devocative.talos.xml.XServer;
import org.devocative.talos.xml.XVm;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

public class Context {
	private final XStream xStream;
	private final File file;
	private final XRoot root;

	private final Map<String, XVm> vmMap = new TreeMap<>();
	private final Map<String, XServer> serverMap = new HashMap<>();

	// ------------------------------

	Context(File configFile) {
		this.file = configFile;

		xStream = new XStream();
		xStream.processAnnotations(XRoot.class);
		xStream.allowTypesByWildcard(new String[]{"org.devocative.talos.xml.**"});

		try {
			if (configFile.exists()) {
				root = (XRoot) xStream.fromXML(new FileReader(configFile));
			} else {
				root = new XRoot();
				root.setCloneBaseDir(System.getProperty("user.home") + File.separator + "VMware");
				xStream.toXML(root, new FileWriter(configFile));
			}

			update();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// ------------------------------

	public boolean addLocalVm(XVm vm) {
		vm.setName(vm.getName().trim().toLowerCase());

		if (!vmMap.containsKey(vm.getName())) {
			root.addLocalVm(vm);
			return true;
		}
		return false;
	}

	public boolean addServer(XServer server) {
		server.setName(server.getName().trim().toLowerCase());

		if (!serverMap.containsKey(server.getName())) {
			root.addServer(server);
			return true;
		}
		return false;
	}

	public Collection<XVm> getVmList() {
		return vmMap.values();
	}

	public Collection<XServer> getServerList() {
		return serverMap.values();
	}

	public File getVmx(String vmName) {
		vmName = assertVMNameAndReturn(vmName);

		return new File(vmMap.get(vmName).getVmxAddr());
	}

	public XVm getVm(String vmName) {
		vmName = assertVMNameAndReturn(vmName);

		return vmMap.get(vmName);
	}

	public Optional<XServer> getServer(String name, String address) {
		if (serverMap.containsKey(name)) {
			final XServer server = serverMap.get(name);

			if (address != null && !server.getAddress().equals(address)) {
				System.err.printf("Name(%s) already exists with an address(%s)\n", name, server.getAddress());
				System.exit(1);
			}

			return Optional.of(server);
		} else if (address != null) {
			final Optional<XServer> first = serverMap.values()
				.stream()
				.filter(server -> server.getAddress().equals(address))
				.findFirst();

			if (first.isPresent()) {
				final XServer server = first.get();
				System.err.printf("Address(%s) already exists with a name(%s)\n",
					server.getAddress(), server.getName());
				System.exit(1);
			}
		}

		return Optional.empty();
	}

	public void flush() {
		try {
			xStream.toXML(root, new FileWriter(file));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		update();
	}

	public Optional<String> findNameByVMX(String vmx) {
		return vmMap.values()
			.stream()
			.filter(vmInfo -> vmx.equals(vmInfo.getVmxAddr()))
			.map(XVm::getName)
			.findFirst();
	}

	public String getCloneBaseDir() {
		return root.getCloneBaseDir();
	}

	public XVm removeConfig(String vmName) {
		vmName = assertVMNameAndReturn(vmName);

		final XVm removed = vmMap.remove(vmName);
		if (removed.isLocal()) {
			root.getLocal().remove(removed);
		} else {
			serverMap
				.get(removed.getServerName())
				.getVms()
				.remove(removed);
		}
		return removed;
	}

	// ------------------------------

	private void update() {
		if (root.getLocal() != null) {
			for (XVm vmInfo : root.getLocal()) {
				vmMap.put(vmInfo.getName(), vmInfo);
			}
		}

		if (root.getServers() != null) {
			for (XServer server : root.getServers()) {
				serverMap.put(server.getName(), server);

				if (server.getVms() != null) {
					for (XVm vm : server.getVms()) {
						vm.setServerName(server.getName());
						vmMap.put(vm.getFullName(), vm);
					}
				}
			}
		}
	}

	private String assertVMNameAndReturn(String vmName) {
		vmName = vmName.trim().toLowerCase();

		if (!vmMap.containsKey(vmName)) {
			System.err.printf("Invalid VM Name [%s] (run 'ls' for list of valid VMs)\n", vmName);
			System.exit(1);
		}

		return vmName;
	}

}
