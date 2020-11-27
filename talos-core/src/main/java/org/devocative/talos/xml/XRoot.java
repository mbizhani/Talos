package org.devocative.talos.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@XStreamAlias("config")
public class XRoot {
	private String cloneBaseDir;

	private List<XVmInfo> local;

	// ------------------------------

	public void addVm(XVmInfo vm) {
		if (local == null) {
			local = new ArrayList<>();
		}
		local.add(vm);
	}
}
