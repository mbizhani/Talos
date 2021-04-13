package org.devocative.talos.vmware;

public enum VMCommand {
	clone,
	getGuestIPAddress,
	list,
	start,
	stop,
	reset,

	createDirectoryInGuest,
	fileExistsInGuest,
	deleteFileInGuest,
	copyFileFromHostToGuest,
	copyFileFromGuestToHost,

	listSnapshots,
	snapshot,
	deleteSnapshot,
	revertToSnapshot
}
