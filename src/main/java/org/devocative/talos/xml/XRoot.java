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

	private List<XVmInfo> vms;

	// ------------------------------

	public void addVm(XVmInfo vm) {
		if (vms == null) {
			vms = new ArrayList<>();
		}
		vms.add(vm);
	}
}
