package org.devocative.talos.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Accessors(chain = true)
@XStreamAlias("config")
public class XRoot {
	private String cloneBaseDir;

	private Set<XVm> local;
	private Set<XServer> servers;

	// ------------------------------

	public void addLocalVm(XVm vm) {
		if (local == null) {
			local = new HashSet<>();
		}
		local.add(vm);
	}

	public void addServer(XServer server) {
		if (servers == null) {
			servers = new HashSet<>();
		}
		servers.add(server);
	}
}
