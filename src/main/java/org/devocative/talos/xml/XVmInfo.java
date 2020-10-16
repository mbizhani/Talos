package org.devocative.talos.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@XStreamAlias("vm")
public class XVmInfo {
	@XStreamAsAttribute
	private String name;

	@XStreamAsAttribute
	private String vmxAddr;

	private XUser ssh = new XUser();

	private XUser guest = new XUser();

	// ------------------------------

	public XUser getSshSafely() {
		return ssh == null ? new XUser() : ssh;
	}

	public XUser getGuestSafely() {
		return guest == null ? new XUser() : guest;
	}
}
