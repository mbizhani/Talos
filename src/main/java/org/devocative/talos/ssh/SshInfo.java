package org.devocative.talos.ssh;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SshInfo {
	private final String hostname;
	private final String user;
	private final String pass;
	private final String name;
}
