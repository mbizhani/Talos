package org.devocative.talos.vmware.esx;

import com.vmware.vim25.GuestInfo;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.VirtualMachineRuntimeInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class VmInfo {
	private final String name;
	private final GuestInfo guestInfo;
	private final VirtualMachineRuntimeInfo runtimeInfo;
	private final ManagedObjectReference mor;
}
