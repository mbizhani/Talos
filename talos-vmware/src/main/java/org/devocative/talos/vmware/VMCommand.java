package org.devocative.talos.vmware;

public enum VMCommand {
	clone,
	getGuestIPAddress,
	list,
	start,
	stop,

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
