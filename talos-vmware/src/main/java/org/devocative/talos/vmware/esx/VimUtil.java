package org.devocative.talos.vmware.esx;

import com.vmware.vim25.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VimUtil {

	public static List<VmInfo> getAllVMs(String server, String username, String password) {
		SslUtil.trustAllHttpsCertificates();

		final VimAuthenticationHelper authHelper = new VimAuthenticationHelper();
		authHelper.loginByUsernameAndPassword(server, username, password);

		try {
			final List<VmInfo> allVMs = getAllVMs(authHelper.getVimPort(), authHelper.getServiceContent());
			authHelper.logout();

			return allVMs;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// ------------------------------

	private static List<VmInfo> getAllVMs(VimPortType vimPortType, ServiceContent serviceContent) throws Exception {
		final ManagedObjectReference propCollectorRef = serviceContent.getPropertyCollector();
		final ManagedObjectReference rootFolderRef = serviceContent.getRootFolder();

		final TraversalSpec tSpec = getVMTraversalSpec();

		final ObjectSpec objectSpec = new ObjectSpec();
		objectSpec.setObj(rootFolderRef);
		objectSpec.setSkip(Boolean.TRUE);
		objectSpec.getSelectSet().add(tSpec);

		final PropertySpec propertySpec = new PropertySpec();
		propertySpec.setAll(Boolean.FALSE);
		propertySpec.getPathSet().addAll(Arrays.asList("name", "guest", "runtime"));
		propertySpec.setType("VirtualMachine");

		final PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
		propertyFilterSpec.getPropSet().add(propertySpec);
		propertyFilterSpec.getObjectSet().add(objectSpec);

		final List<PropertyFilterSpec> listPfs = new ArrayList<>(1);
		listPfs.add(propertyFilterSpec);

		final List<ObjectContent> listObjCont = retrievePropertiesAllObjects(vimPortType, propCollectorRef, listPfs);

		final List<VmInfo> result = new ArrayList<>();
		for (ObjectContent oc : listObjCont) {
			String vmName = null;
			GuestInfo guestInfo = null;
			VirtualMachineRuntimeInfo runtimeInfo = null;

			final List<DynamicProperty> dps = oc.getPropSet();
			for (DynamicProperty dp : dps) {
				if (dp.getName().equals("name")) {
					vmName = (String) dp.getVal();
				} else if (dp.getName().equals("guest")) {
					guestInfo = (GuestInfo) dp.getVal();
				} else if (dp.getName().equals("runtime")) {
					runtimeInfo = (VirtualMachineRuntimeInfo) dp.getVal();
				}
			}

			result.add(new VmInfo(vmName, guestInfo, runtimeInfo, oc.getObj()));
		}
		return result;
	}

	private static TraversalSpec getVMTraversalSpec() {
		final TraversalSpec vAppToVM = new TraversalSpec();
		vAppToVM.setName("vAppToVM");
		vAppToVM.setType("VirtualApp");
		vAppToVM.setPath("vm");

		final TraversalSpec vAppToVApp = new TraversalSpec();
		vAppToVApp.setName("vAppToVApp");
		vAppToVApp.setType("VirtualApp");
		vAppToVApp.setPath("resourcePool");

		final SelectionSpec vAppRecursion = new SelectionSpec();
		vAppRecursion.setName("vAppToVApp");

		final SelectionSpec vmInVApp = new SelectionSpec();
		vmInVApp.setName("vAppToVM");

		final List<SelectionSpec> vAppToVMSS = new ArrayList<>();
		vAppToVMSS.add(vAppRecursion);
		vAppToVMSS.add(vmInVApp);
		vAppToVApp.getSelectSet().addAll(vAppToVMSS);

		final SelectionSpec sSpec = new SelectionSpec();
		sSpec.setName("VisitFolders");

		final TraversalSpec dataCenterToVMFolder = new TraversalSpec();
		dataCenterToVMFolder.setName("DataCenterToVMFolder");
		dataCenterToVMFolder.setType("Datacenter");
		dataCenterToVMFolder.setPath("vmFolder");
		dataCenterToVMFolder.setSkip(false);
		dataCenterToVMFolder.getSelectSet().add(sSpec);

		final TraversalSpec traversalSpec = new TraversalSpec();
		traversalSpec.setName("VisitFolders");
		traversalSpec.setType("Folder");
		traversalSpec.setPath("childEntity");
		traversalSpec.setSkip(false);
		List<SelectionSpec> sSpecArr = new ArrayList<SelectionSpec>();
		sSpecArr.add(sSpec);
		sSpecArr.add(dataCenterToVMFolder);
		sSpecArr.add(vAppToVM);
		sSpecArr.add(vAppToVApp);
		traversalSpec.getSelectSet().addAll(sSpecArr);
		return traversalSpec;
	}

	private static List<ObjectContent> retrievePropertiesAllObjects(
		VimPortType vimPort, ManagedObjectReference propCollectorRef,
		List<PropertyFilterSpec> listpfs)
		throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
		RetrieveOptions propObjectRetrieveOpts = new RetrieveOptions();
		List<ObjectContent> listobjcontent = new ArrayList<ObjectContent>();

		RetrieveResult rslts = vimPort.retrievePropertiesEx(propCollectorRef,
			listpfs, propObjectRetrieveOpts);
		if (rslts != null && rslts.getObjects() != null
			&& !rslts.getObjects().isEmpty()) {
			listobjcontent.addAll(rslts.getObjects());
		}
		String token = null;
		if (rslts != null && rslts.getToken() != null) {
			token = rslts.getToken();
		}
		while (token != null && !token.isEmpty()) {
			rslts = vimPort.continueRetrievePropertiesEx(propCollectorRef,
				token);
			token = null;
			if (rslts != null) {
				token = rslts.getToken();
				if (rslts.getObjects() != null
					&& !rslts.getObjects().isEmpty()) {
					listobjcontent.addAll(rslts.getObjects());
				}
			}
		}

		return listobjcontent;
	}
}
