package org.devocative.talos.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.devocative.talos.vmware.VMRun;

import java.io.File;
import java.util.Objects;

import static org.devocative.talos.common.Util.nvl;

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

	// ---------------

	@XStreamOmitField
	private String serverName;

	// ------------------------------

	public XUser getSshSafely(String username, String password, boolean assign) {
		final XUser user = ssh == null ? new XUser() : ssh;
		user.setUser(nvl(username, user.getUser()));
		user.setPass(nvl(password, user.getPass()));

		if (assign) {
			this.ssh = user;
		}

		return user;
	}

	public XUser getGuestSafely(String username, String password, boolean assign) {
		final XUser user = guest == null ? new XUser() : guest;
		user.setUser(nvl(username, user.getUser()));
		user.setPass(nvl(password, user.getPass()));

		if (assign) {
			this.guest = user;
		}

		return user;
	}

	public String getFullName() {
		return serverName == null ? getName() : String.format("%s.%s", serverName, name);
	}

	public boolean isLocal() {
		return serverName == null;
	}

	public String getAddressSafely() {
		return nvl(getAddress(), () -> isLocal() ? VMRun
			.getIpOf(new File(getVmxAddr()))
			.assertSuccess()
			.trim() : null);
	}

	// ---------------

	@Override
	public String toString() {
		return getName();
	}

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
