package org.devocative.talos.command;

import org.devocative.talos.Context;
import org.devocative.talos.vmware.Result;
import org.devocative.talos.vmware.VMRun;
import org.devocative.talos.xml.XUser;
import org.devocative.talos.xml.XVmInfo;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;

import static org.devocative.talos.vmware.VMCommand.*;

@Command(name = "cp")
public class CCopy extends CAbstract {

	@Parameters(paramLabel = "SRC", description = "Source File (NAME:FILE for guest)")
	private String src;

	@Parameters(paramLabel = "DEST", description = "Destination Directory (NAME:DIR for guest)")
	private String dest;

	@Option(names = {"-u", "--username"})
	private String username;

	@Option(names = {"-p", "--password"})
	private String password;

	@Option(names = {"-P", "--persist"})
	private boolean persist = false;

	// ------------------------------

	public CCopy(Context context) {
		super(context);
	}

	// ------------------------------

	@Override
	public void run() {
		if (src.contains(":") && dest.contains(":")) {
			error("Source/Destination must address a VM in format of 'NAME:'");
		}

		if (src.contains(":")) {
			final String[] split = src.split("[:]");
			if (split.length == 2) {
				final String name = split[0];
				final String fileOnGuest = split[1];
				copyFileFromGuestToHost(name, dest, fileOnGuest);
			} else {
				error("Invalid src");
			}
		} else if (dest.contains(":")) {
			final String[] split = dest.split("[:]");
			if (split.length == 2) {
				final String name = split[0];
				final String fileOnGuest = split[1];
				copyFileFromHostToGuest(name, src, fileOnGuest);
			} else {
				error("Invalid dest");
			}
		} else {
			error("Invalid src and dest");
		}
	}

	private void copyFileFromHostToGuest(String name, String fileOnHost, String dirOnGuest) {
		printVerbose("Copy-FromHostToGuest: name=[%s] host=[%s] > guest=[%s]", name, fileOnHost, dirOnGuest);

		final File fHost = new File(fileOnHost);
		if (fHost.exists() && fHost.isFile()) {

			final File vmx = context.getVmx(name);
			final XUser user = getUser(name);

			printVerbose("Creating directory on guest: '%s' ...", dirOnGuest);
			final Result dirRs = VMRun
				.of(createDirectoryInGuest)
				.vmxFile(vmx)
				.guestUser(user.getUser())
				.guestPass(user.getPass())
				.options(dirOnGuest)
				.call();
			printVerbose("Created directory on guest: '%s' - rs = %s", dirOnGuest, dirRs);

			final String fileOnGuest = dirOnGuest + "/" + fHost.getName();

			VMRun
				.of(copyFileFromHostToGuest)
				.vmxFile(vmx)
				.guestUser(user.getUser())
				.guestPass(user.getPass())
				.options(fHost.getAbsolutePath(), fileOnGuest)
				.call()
				.assertSuccess();

		} else {
			error("Invalid file on host: [%s]", fileOnHost);
		}
	}

	private void copyFileFromGuestToHost(String name, String dirOnHost, String fileOnGuest) {
		printVerbose("Copy-FromGuestToHost: name=[%s] guest=[%s] > host=[%s]", name, fileOnGuest, dirOnHost);

		final File dHost = new File(dirOnHost);
		if (dHost.exists() && dHost.isFile()) {
			error("Invalid directory on host: [%s]", dirOnHost);
		}

		dHost.mkdirs();

		final String[] fileOnGuestParts = fileOnGuest.split("[/\\\\]");
		final String fileNameOnGuest = fileOnGuestParts[fileOnGuestParts.length - 1];
		printVerbose("Filename on guest: [%s]", fileNameOnGuest);

		final String fileOnHost = dHost.getAbsolutePath() + File.separator + fileNameOnGuest;

		final File vmx = context.getVmx(name);
		final XUser user = getUser(name);

		VMRun
			.of(copyFileFromGuestToHost)
			.vmxFile(vmx)
			.guestUser(user.getUser())
			.guestPass(user.getPass())
			.options(fileOnGuest, fileOnHost)
			.call()
			.assertSuccess();
	}

	private XUser getUser(String name) {
		final XVmInfo vmInfo = context.getVmInfo(name);
		final XUser guest = vmInfo.getGuestSafely();
		final String user = username != null ? username : guest.getUser();
		final String pass = username != null ? password : guest.getPass();

		final XUser result = new XUser(user, pass);
		if (persist) {
			printVerbose("Persist username and password of guest for VM name=[%s]", name);
			vmInfo.setGuest(result);
			context.flush();
		}

		return result;
	}
}
