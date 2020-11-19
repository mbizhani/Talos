# Talos
A CLI application to utilize working with VMWare Workstation and all your VMs.

## Introduction
When I wanted to learn about cloud technologies in my home lab, I needed a virtualization env. 
So I chose VMware Workstation. However, when you want to clone, start, stop, and other tasks with all the VMs,
it becomes cumbersome. 

The VMware Workstation has a command line tool called `vmrun`, which can help working with VMs.
The `Talos` project is a wrapper around `vmrun` with extra utilities such as `ssh`, 
and it eases working with all your VMs.

**NOTE: Talos has been only tested on Linux.**

## Prepare Your Template VM
- Install a Linux VM
- Install `open-vm-tools` or `vmware-tools` in the VM
- Find your main NIC's name using `ip a` command. It may be something like `ens33`.
- Due to this [link](https://docs.vmware.com/en/VMware-Tools/10.2.0/com.vmware.vsphere.vmwaretools.doc/GUID-ECCF9D01-3666-40CE-B9FD-7EE0738AB5D9.html),
modify `/etc/vmware-tools/tools.conf` and append following section to enable your physical NIC as default one.
    ```
    [guestinfo]
    primary-nics=ens*
    ```
- [optional] Install Docker

Now your template VM is ready.

## Installation
For both compilation from source and execution `OpenJDK`, `OracleJDK` or any valid JDK is required. 

Apache Maven is also required for building from source. 
You can clone the repo and execute `mvn clean package` in the root directory. Then `talos.sh` is created in `target` directory. 
An instance of `talos.sh` is also copied in `$HOME/.local/bin/` directory. The `$HOME/.local/bin/` can be appended to the `PATH`.

## Talos Commands
`Talos` command creates its config file in `$HOME/.talos-config.xml`.

- `talos.sh scan <DIR>` - scans the `<DIR>` recursively and adds the VMs(`vmx` files) in the config.
- `talos.sh ls [-U]` - lists all added VMs in the config
  - `-U` deletes non-existed VMs from config.
- `talos.sh start VM [VM...]` - starts VM(s).
- `talos.sh stop VM [VM...]` - stops VM(s).
- `talos.sh clone VM NEW_VM` - creates a full clone.
- `talos.sh ps` - lists running VMs and shows their IP address (needs `open-vm-tools` or `vmware-tools` installed in VMs).
- `talos.sh ssh VM [-u USER] [-p PASSWORD] [-P]` - creates a ssh session to your VM
  - `-P` persists username and password in config in plain text.
- `talos.sh ssh VM1 VM2 [VM...]` - creates a multi ssh shell with synchronized input to all sessions.
- `talos.sh cp SRC DEST [-u USER] [-p PASSWORD] [-P]` - copies a file.
  - `SRC` or `DEST` is a simple `FILE` on the host or `VM:FILE` on the guest.
  - `-P` persists username and password in config in plain text.
