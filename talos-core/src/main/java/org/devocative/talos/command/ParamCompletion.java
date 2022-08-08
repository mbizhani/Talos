package org.devocative.talos.command;

import java.util.ArrayList;
import java.util.Collections;

public class ParamCompletion {

	public static class VMListCompletion extends ArrayList<String> {
		public VMListCompletion() {
			super(Collections.singletonList(
				String.format("$(%s %s %s)", CCompletion.TALOS_CMD, CList.LIST_CMD, CList.LIST_OPT_NAME_SHORT)
			));
		}
	}

	public static class SnapshotListCompletion extends ArrayList<String> {
		public SnapshotListCompletion() {
			super(Collections.singletonList(
				String.format("$([ ${#COMP_WORDS[@]} -ge 5 ] && %s snapshot ls \"${COMP_WORDS[3]}\")", CCompletion.TALOS_CMD)
			));
		}
	}

}
