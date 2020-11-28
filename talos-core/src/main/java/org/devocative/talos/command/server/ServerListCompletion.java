package org.devocative.talos.command.server;

import org.devocative.talos.command.CCompletion;

import java.util.ArrayList;
import java.util.Collections;

class ServerListCompletion extends ArrayList<String> {

	public ServerListCompletion() {
		super(Collections.singletonList(
			String.format("$(%s server ls -n)", CCompletion.TALOS_CMD)
		));
	}

}
