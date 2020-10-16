package org.devocative.talos.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@XStreamAlias("user")
public class XUser {
	@XStreamAsAttribute
	private String user;

	@XStreamAsAttribute
	private String pass;

	// ------------------------------

	public XUser(XUser user) {
		if (user != null) {
			this.user = user.getUser();
			this.pass = user.getPass();
		}
	}
}
