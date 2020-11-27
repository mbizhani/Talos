package org.devocative.talos.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Objects;

@Getter
@Setter
@Accessors(chain = true)
@XStreamAlias("vm")
public class XVm {
	@XStreamAsAttribute
	private String name;

	@XStreamAsAttribute
	private String vmxAddr;

	@XStreamAsAttribute
	private String address;

	private XUser ssh = new XUser();

	private XUser guest = new XUser();

	// ------------------------------

	public XUser getSshSafely() {
		return ssh == null ? new XUser() : ssh;
	}

	public XUser getGuestSafely() {
		return guest == null ? new XUser() : guest;
	}

	// ---------------

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof XVm)) return false;
		XVm info = (XVm) o;
		return Objects.equals(getName(), info.getName());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getName());
	}
}
