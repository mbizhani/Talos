package org.devocative.talos;

import com.thoughtworks.xstream.XStream;
import org.devocative.talos.xml.XRoot;
import org.devocative.talos.xml.XVmInfo;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class Context {
	private final XStream xStream;
	private final File file;
	private final XRoot root;

	private final Map<String, XVmInfo> vmMap = new LinkedHashMap<>();

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

	public boolean addVmInfo(XVmInfo vmInfo) {
		vmInfo.setName(vmInfo.getName().trim().toLowerCase());

		if (!vmMap.containsKey(vmInfo.getName())) {
			root.addVm(vmInfo);
			return true;
		}
		return false;
	}

	public Collection<XVmInfo> getVmList() {
		return vmMap.values();
	}

	public File getVmx(String vmName) {
		vmName = vmName.trim().toLowerCase();

		if (!vmMap.containsKey(vmName.trim().toLowerCase())) {
			System.err.printf("Invalid VM Name [%s] (run 'ls' for list of valid VMs)\n", vmName);
			System.exit(1);
		}

		return new File(vmMap.get(vmName).getVmxAddr());
	}

	public XVmInfo getVmInfo(String vmName) {
		vmName = vmName.trim().toLowerCase();

		if (!vmMap.containsKey(vmName)) {
			System.err.printf("Invalid VM Name [%s] (run 'ls' for list of valid VMs)\n", vmName);
			System.exit(1);
		}

		return vmMap.get(vmName);
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
			.filter(vmInfo -> vmInfo.getVmxAddr().equals(vmx))
			.map(XVmInfo::getName)
			.findFirst();
	}

	public String getCloneBaseDir() {
		return root.getCloneBaseDir();
	}

	// ------------------------------

	private void update() {
		if (root.getVms() != null) {
			for (XVmInfo vmInfo : root.getVms()) {
				vmMap.put(vmInfo.getName(), vmInfo);
			}
		}
	}
}
