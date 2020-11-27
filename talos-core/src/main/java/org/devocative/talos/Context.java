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
		xStream.allowTypesByRegExp(new String[]{".*"});

		try {
			if (configFile.exists()) {
				root = (XRoot) xStream.fromXML(new FileReader(configFile));
			} else {
				root = new XRoot();
				root.setCloneBaseDir(System.getProperty("user.home") + File.separator + "VMWare");
				xStream.toXML(root, new FileWriter(configFile));
				System.out.printf("A new config file created: '%s'", configFile.getAbsolutePath());
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

	public File getVmx(String vmName) {
		vmName = assertVMNameAndReturn(vmName);

		return new File(vmMap.get(vmName).getVmxAddr());
	}

	public XVm getVm(String vmName) {
		vmName = assertVMNameAndReturn(vmName);

		return vmMap.get(vmName);
	}

	public Optional<XServer> getServer(String name) {
		return Optional.ofNullable(serverMap.get(name));
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
		root.getLocal().remove(removed);
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
				if (server.getVms() != null) {

					serverMap.put(server.getName(), server);

					for (XVm vm : server.getVms()) {
						vm.setServerName(server.getName());
						final String key = String.format("%s.%s", server.getName(), vm.getName());
						vmMap.put(key, vm);
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
