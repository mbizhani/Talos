package org.devocative.talos.command;

import java.util.ArrayList;
import java.util.Collections;

class VMListCompletion extends ArrayList<String> {

	public VMListCompletion() {
		super(Collections.singletonList(
			String.format("$(%s %s %s)", CCompletion.TALOS_CMD, CList.LIST_CMD, CList.LIST_OPT_NAME_SHORT)
		));
	}

}
