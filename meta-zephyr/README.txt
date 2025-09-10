Building Zephyr Images via bitbake recipes
==========================================

More detailed and up-to-date information can be found here:

https://wiki.yoctoproject.org/wiki/TipsAndTricks/BuildingZephyrImages

Prerequisites:
==============

This layer depends on:
    Yocto distro (master)
        git://git.yoctoproject.org/poky
    Python layer (meta-openembedded/meta-python)
        git://git.openembedded.org/meta-openembedded

Add "meta-openembedded/meta-oe" to BBLAYERS
Add "meta-openembedded/meta-python" to BBLAYERS
Add "meta-zephyr-core" and "meta-zephyr-bsp" to BBLAYERS

Building and Running Zephyr Samples
===================================

You can build Zephyr samples. There are several sample recipes.

To use the Yocto toolchain, modify local conf by adding:
    DISTRO = "zephyr"

To use the Zephyr pre-built toolchain, modify local conf by adding:
    ZEPHYR_TOOLCHAIN_VARIANT = "zephyr"

For example, to build the Zephyr "philosophers" sample:
    
    $ MACHINE=qemu-x86 bitbake zephyr-philosophers
    
You can then run the created "philosophers" image in qemu:

    $ runqemu qemu-x86
    
The same sample, for ARM image:

    $ MACHINE=qemu-cortex-m3 bitbake zephyr-philosophers
    $ runqemu qemu-cortex-m3
    
The same sample, for Nios2 image:

    $ MACHINE=qemu-nios2 bitbake zephyr-philosophers
    $ runqemu qemu-nios2

Flashing
=================================

You can flash Zephyr samples to boards. Currently, the following MACHINEs
are supported:
 * DFU:
  - arduino-101-sss
  - arduino-101
  - arduino-101-ble
 * pyocd:
  - 96b-nitrogen

To flash the example you built with command e.g.

    $ MACHINE=96b-nitrogen bitbake zephyr-philosophers

call similar command with explicit flash_usb command:

    $ MACHINE=96b-nitrogen bitbake zephyr-philosophers -c flash_usb

dfu-util and/or pyocd need to be installed in your system. If you observe
permission errors or the flashing process seem to hang, follow those instructions:
https://github.com/pyocd/pyOCD/tree/master/udev

By default, pyocd tries to flash all the attached probes. This behaviour can be
customised by defining the PYOCD_FLASH_IDS variable as a space-separated list
of IDs. Once that is set, the tool will only try to program these IDs. You can
query for the IDs by running `pyocd list` on your host while having the probes
attached. Besides setting this variable through the build's configuration or
metadata, you can also inject its value from command line with something like:

    $ PYOCD_FLASH_IDS='<ID1> <ID2> <ID3>' BB_ENV_EXTRAWHITE="$BB_ENV_EXTRAWHITE PYOCD_FLASH_IDS" bitbake <TARGET> -c flash_usb

Building and Running Zephyr Tests
=================================
Presently only toolchains for ARM, x86, IAMCU and Nios2 are supported.
(For ARM we use CortexM3 toolchain)

To run Zephyr Test using Yocto Image Tests, ensure following in local.conf:

    INHERIT += "testimage"

You can build and test an individual existing Zephyr test.
This is done by appending the actual test name to the "zephyr-kernel-test",
for example:

    $ MACHINE=qemu-x86 bitbake zephyr-kernel-test-sleep
    $ MACHINE=qemu-x86 bitbake zephyr-kernel-test-sleep -c testimage

You can also build and run all Zephyr existing tests (as listed in the file
zephyr-kernel-test.inc). For example:

    $ MACHINE=qemu-x86 bitbake zephyr-kernel-test-all
    $ MACHINE=qemu-x86 bitbake zephyr-kernel-test-all -c testimage
or 
    $ MACHINE=qemu-cortex-m3 bitbake zephyr-kernel-test-all
    $ MACHINE=qemu-cortex-m3 bitbake zephyr-kernel-test-all -c testimage
or 
    $ MACHINE=qemu-nios2 bitbake zephyr-kernel-test-all
    $ MACHINE=qemu-nios2 bitbake zephyr-kernel-test-all -c testimage
        

Generating OE Machines based on Zephyr board definitions
========================================================
We currently have a recipe called generate-zephry-machines which will go through
and attempt to create an OE machine conf file for every board in Zephyr.

This is run via:

MACHINE=qemu-x86 bitbake generate-zephyr-machines

The output is then put in the normal deploy dir. This recipe is really only
useful for maintainers. There is currently no way to use the Zephyr board 
definition in a single step build. So if you wish to regenerate those machines,
you will need to run the above, copy the conf files from the deploy dir to the
machine conf directory and then run your build. This shouldn't need to happen 
often.

Generating new Zephyr recipe versions
=====================================
The script meta-zephyr-core/scripts/generate-version.py is used to generate
Yocto configuration for a Zephyr version from the West configuration in the
Zephyr repository. It requires the west and jinja2 Python packages to be
installed on the host. Run it as follows:

    $ ./meta-zephyr-core/scripts/generate-version.py x.x.x

where x.x.x is the Zephyr version.

The patch files added to SRC_URI in the generated file should be validated and
modified if required.

The new version should be committed and submitted to the mailing list as
described in "Contributing".

Contributing
============

Patches for meta-zephyr should be sent to the yocto-patches@lists.yoctoproject.org
mailing list.  See https://lists.yoctoproject.org/g/yocto-patches for subscription
details and the list archive.  Please add [meta-zephyr] to the subject so
the patches are identifable.

Git can be configured to send mails appropriately when using git send-email:

$ git config --local sendemail.to yocto-patches@lists.yoctoproject.org
$ git config --local format.subjectPrefix meta-zephyr][PATCH
