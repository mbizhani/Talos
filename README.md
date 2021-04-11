# Talos
A CLI application to utilize working with VMWare Workstation and all your VMs.

## Introduction
When I started to learn cloud technologies in my home lab, I needed a virtualization env, and I chose VMware
Workstation. However, when you want to clone, start, stop, and other tasks with all the VMs, it becomes cumbersome.

The VMware Workstation has a command line tool called `vmrun`, which can help you to work with VMs. The `Talos` project
is a wrapper
around [`vmrun`](https://docs.vmware.com/en/VMware-Fusion/12/com.vmware.fusion.using.doc/GUID-24F54E24-EFB0-4E94-8A07-2AD791F0E497.html)
with extra utilities such as `ssh`, and it eases working with all your VMs.

**NOTE: Talos has been only tested on Linux.**

## Prepare Your Template VM
- Install a Linux VM
- In your VM:
  - Install `open-vm-tools` or `vmware-tools`
  - Find your main NIC's name using `ip a` command. It may be something like `ens*`.
  - Due to this [link](https://docs.vmware.com/en/VMware-Tools/10.2.0/com.vmware.vsphere.vmwaretools.doc/GUID-ECCF9D01-3666-40CE-B9FD-7EE0738AB5D9.html),
  modify `/etc/vmware-tools/tools.conf` and append following section to enable your physical NIC as default one.
    ```
    [guestinfo]
    primary-nics=ens*
    ```
  - [optional] Install Docker

Now your template VM is ready.

## Installation

For both compilation and execution `OpenJDK`, `OracleJDK` or any valid JDK is required.

Apache Maven is also required for building from the source. You can clone the repo and execute `mvn clean package` in
the root directory. Then `talos.sh` is created in both
`target` and `$HOME/.local/bin/` directory. `$HOME/.local/bin/` can be appended to the `PATH`.

## Talos Commands

`Talos` command creates its config file as `$HOME/.talos/config.xml`.

Note: for most of the commands, `-V` enables verbose.

- `talos.sh scan <DIR>` - scans the `<DIR>` recursively and adds the VMs(`vmx` files) in the config.
- `talos.sh ls [-U]` - lists all added VMs in the config
  - `-U` deletes non-existed VMs from the config.
- `talos.sh start VM [VM...]` - starts VM(s).
- `talos.sh stop VM [VM...]` - stops VM(s).
- `talos.sh clone VM NEW_VM` - creates a full clone.
- `talos.sh ps` - lists running VMs and shows their IP address (needs `open-vm-tools` or `vmware-tools` installed in VMs).
- `talos.sh ssh VM [-u USER] [-p PASSWORD] [-P]` - creates an SSH session to your VM
  - `-P` persists the username and password in the config in plain text.
- `talos.sh ssh VM1 VM2 [VM...]` - creates multi SSH shells with synchronized input to all sessions.
- `talos.sh cp SRC DEST [-u USER] [-p PASSWORD] [-P]` - copies a file from `SRC` to `DEST`
  - `SRC` or `DEST` is a simple `FILE` on the host or `VM:FILE` on the guest.
  - `-P` persists the username and password in the config in plain text.
