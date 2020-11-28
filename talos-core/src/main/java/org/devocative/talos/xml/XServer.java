package org.devocative.talos.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@Accessors(chain = true)
@XStreamAlias("server")
@NoArgsConstructor
public class XServer {
	@XStreamAsAttribute
	private String name;

	@XStreamAsAttribute
	private String address;

	@XStreamAsAttribute
	private String username;

	@XStreamAsAttribute
	private String password;

	private Set<XVm> vms;

	// ------------------------------

	public XServer(String name, String address) {
		this.name = name;
		this.address = address;
	}

	// ------------------------------

	public boolean addVm(XVm vm) {
		vm.setName(vm.getName().trim().toLowerCase());

		if (vms == null) {
			vms = new HashSet<>();
		}

		return vms.add(vm);
	}

	// ---------------

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof XServer)) return false;
		XServer xServer = (XServer) o;
		return Objects.equals(getAddress(), xServer.getAddress());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getAddress());
	}
}
