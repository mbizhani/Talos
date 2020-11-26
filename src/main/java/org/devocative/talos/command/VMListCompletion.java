package org.devocative.talos.command;

import java.util.ArrayList;
import java.util.Collections;

class VMListCompletion extends ArrayList<String> {
	public VMListCompletion() {
		super(Collections.singletonList("$(talos.sh ls -n)"));
	}
}
