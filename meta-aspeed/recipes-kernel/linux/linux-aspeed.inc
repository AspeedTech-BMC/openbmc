DESCRIPTION = "Linux kernel for Aspeed"
SECTION = "kernel"
LICENSE = "GPLv2"

PROVIDES += "virtual/kernel"

KCONFIG_MODE="--alldefconfig"

### OpenBMC
#KSRC ?= "git://github.com/openbmc/linux;protocol=git;branch=${KBRANCH}"
### ASPEED SDK
KSRC ?= "git://github.com/AspeedTech-BMC/linux;protocol=git;branch=${KBRANCH}"

SRC_URI = "${KSRC}"
SRC_URI += " file://defconfig"

LINUX_VERSION_EXTENSION ?= "-${SRCREV}"

PV = "${LINUX_VERSION}+git${SRCPV}"

inherit kernel
require recipes-kernel/linux/linux-yocto.inc

# From 5.6+ the COPYING file changed
### OpenBMC
#LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"
### ASPEED SDK
LIC_FILES_CHKSUM = "file://COPYING;md5=bbea815ee2795b2f4230826c0c6b8814"
