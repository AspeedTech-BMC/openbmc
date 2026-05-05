# meta-zephyr

This layer enables building Zephyr using Yocto Project.

## Dependencies

This layer depends on:

    URI: https://git.openembedded.org/bitbake
    branch: master

    URI: https://git.openembedded.org/openembedded-core
    layers: meta
    branch: master

    URI: https://git.openembedded.org/meta-openembedded
    layers: meta-oe, meta-python
    branch: master

## Building Zephyr Images via bitbake recipes

### Quick Build

Ensure your build host meets the
[Yocto Project system requirements](https://docs.yoctoproject.org/ref-manual/system-requirements.html)
and follow the
[Quick Build setup guide](https://docs.yoctoproject.org/brief-yoctoprojectqs/index.html#yocto-project-quick-build)
to setup the host enviroment.

Clone the following repos:

- bitbake
- meta-zephyr

```console
git clone https://git.openembedded.org/bitbake
git clone https://git.yoctoproject.org/meta-zephyr
```

Initialize default build configuration with bitbake-setup:

```console
./bitbake/bin/bitbake-setup init --non-interactive \
  ./meta-zephyr/zephyr-master.conf.json \
  zephyr-default distro/zephyr machine/qemu-x86
```

Alternatively, run bitbake-setup with interactive mode to choose for
different configuration:

```console
./bitbake/bin/bitbake-setup init ./meta-zephyr/zephyr-master.conf.json
```

Initialize build environment

```console
source ./bitbake-builds/zephyr-master/build/init-build-env
```

build the Zephyr "helloworld" sample:

```console
bitbake zephyr-helloworld
```

Then, you can run the created "helloworld" image in QEMU:

```console
runqemu nographic
```

To exit QEMU when running in nographic mode, press `Ctrl+A x`.

### Building and Running other Zephyr Samples

You can build other Zephyr samples. There are several sample recipes
[available here](https://git.yoctoproject.org/meta-zephyr/tree/meta-zephyr-core/recipes-kernel/zephyr-kernel).

For example, to build the
[Zephyr "philosophers" sample](https://git.yoctoproject.org/meta-zephyr/tree/meta-zephyr-core/recipes-kernel/zephyr-kernel/zephyr-philosophers.bb):

```console
bitbake zephyr-philosophers
```

You can then run the created "philosophers" image in QEMU.

```console
runqemu nographic
```

The same sample can be built for other machines/boards, for example ARM Cortex-M3:

```console
bitbake-config-build enable-fragment machine/qemu-cortex-m3
bitbake zephyr-philosophers
runqemu nographic
```

Alternatively, you can use the MACHINE variable to define the target machine,
you will need to disable the machine fragment to prevent conflict:

```console
bitbake-config-build disable-fragment machine/qemu-x86
MACHINE=qemu-cortex-m3 bitbake zephyr-philosophers
runqemu qemu-cortex-m3 nographic
```

The default configuration (with `zephyr` DISTRO) uses the Yocto Project toolchain
to compile Zephyr applications. To use the Zephyr pre-built toolchain instead,
modify `local.conf` by adding:

```
ZEPHYR_TOOLCHAIN_VARIANT = "zephyr"
```

Other Tips and Tricks for building zephyr image can be found
[here](https://wiki.yoctoproject.org/wiki/TipsAndTricks/BuildingZephyrImages).

### Supported Zephyr Machines

To see the list of supported Zephyr machines, type the following command:

```
bitbake-layers show-machines | grep zephyr
```

### Flashing

You can flash Zephyr samples to boards. Currently, the following MACHINEs
are supported:
 * DFU:
   * arduino-101-sss
   * arduino-101
   * arduino-101-ble
 * pyocd:
   * 96b-nitrogen

To flash the example you built with command e.g.
```
    $ MACHINE=96b-nitrogen bitbake zephyr-philosophers
```

call similar command with explicit flash_usb command:
```
    $ MACHINE=96b-nitrogen bitbake zephyr-philosophers -c flash_usb
```

dfu-util and/or pyocd need to be installed in your system. If you observe
permission errors or the flashing process seem to hang, follow those instructions:
https://github.com/pyocd/pyOCD/tree/master/udev

By default, pyocd tries to flash all the attached probes. This behaviour can be
customised by defining the PYOCD_FLASH_IDS variable as a space-separated list
of IDs. Once that is set, the tool will only try to program these IDs. You can
query for the IDs by running `pyocd list` on your host while having the probes
attached. Besides setting this variable through the build's configuration or
metadata, you can also inject its value from command line with something like:
```
    $ PYOCD_FLASH_IDS='<ID1> <ID2> <ID3>' BB_ENV_EXTRAWHITE="$BB_ENV_EXTRAWHITE PYOCD_FLASH_IDS" bitbake <TARGET> -c flash_usb
```

## Building and Running Zephyr Tests

Presently only toolchains for ARM, x86 and IAMCU are supported.
(For ARM we use CortexM3 toolchain)

To run Zephyr Test using Yocto Image Tests, ensure following in local.conf:
```
    IMAGE_CLASSES += "testimage"
```

You can build and test an individual existing Zephyr test.
This is done by appending the actual test name to the "zephyr-kernel-test",
for example:
```
    $ MACHINE=qemu-x86 bitbake zephyr-kernel-test-sleep
    $ MACHINE=qemu-x86 bitbake zephyr-kernel-test-sleep -c testimage
```

You can also build and run all Zephyr existing tests (as listed in the file
zephyr-kernel-test.inc). For example:
```
    $ MACHINE=qemu-x86 bitbake zephyr-kernel-test-all
    $ MACHINE=qemu-x86 bitbake zephyr-kernel-test-all -c testimage
or 
    $ MACHINE=qemu-cortex-m3 bitbake zephyr-kernel-test-all
    $ MACHINE=qemu-cortex-m3 bitbake zephyr-kernel-test-all -c testimage
```

## Generating OE Machines based on Zephyr board definitions

We currently have a recipe called generate-zephry-machines which will go through
and attempt to create an OE machine conf file for every board in Zephyr.

This is run via:
```
MACHINE=qemu-x86 bitbake generate-zephyr-machines
```

The output is then put in the normal deploy dir. This recipe is really only
useful for maintainers. There is currently no way to use the Zephyr board 
definition in a single step build. So if you wish to regenerate those machines,
you will need to run the above, copy the conf files from the deploy dir to the
machine conf directory and then run your build. This shouldn't need to happen 
often.

## Generating new Zephyr recipe versions

The script meta-zephyr-core/scripts/generate-version.py is used to generate
Yocto configuration for a Zephyr version from the West configuration in the
Zephyr repository. It requires the west and jinja2 Python packages to be
installed on the host. Run it as follows:
```
    $ ./meta-zephyr-core/scripts/generate-version.py -v x.x.x
```

where x.x.x is the Zephyr version.

The patch files added to SRC_URI in the generated file should be validated and
modified if required.

The new version should be committed and submitted to the mailing list as
described in "Maintainers, Mailing list, Patches".

## Maintainers, Mailing list, Patches

Please send any patches for this layer to the yocto-patches mailinglists
with ['meta-zephyr'] in the subject:

	yocto-patches@lists.yoctoproject.org

When sending patches, please make sure the email subject line includes
`[meta-zephyr][<BRANCH_NAME>][PATCH]` and cc'ing the maintainers.

For more details follow the Yocto Project community patch submission guidelines,
as described in:

https://docs.yoctoproject.org/dev/contributor-guide/submit-changes.html#

`git send-email --to yocto-patches@lists.yoctoproject.org *.patch`

> **Note:** When creating patches, please use below format. To follow best practice,
> if you have more than one patch use `--cover-letter` option while generating the
> patches. Edit the 0000-cover-letter.patch and change the title and top of the
> body as appropriate.

**Syntax:**
`git format-patch -s --subject-prefix="meta-zephyr][<BRANCH_NAME>][PATCH" -1`

**Example:**
`git format-patch -s --subject-prefix="meta-zephyr][scarthgap][PATCH" -1`

**Maintainers:**

    Lee Chee Yang <chee.yang.lee@intel.com>
    Sandeep Gundlupet Raju <sandeep.gundlupet-raju@amd.com>
