DESCRIPTION = "Linux kernel for Aspeed"
SECTION = "kernel"
LICENSE = "GPLv2"

PROVIDES += "virtual/kernel"

KCONFIG_MODE="--alldefconfig"

#FILESEXTRAPATHS_prepend_pilot4 := "${THISDIR}/pilot4:"

KSRC ?= "git://github.com/AspeedTech-BMC/linux;protocol=git;branch=${KBRANCH}"
#KSRC ?= "git:///home/vnigade/pilot4/linux;protocol=file;nocheckout=1;branch=${KBRANCH}"
SRC_URI = "${KSRC}"
SRC_URI += " file://defconfig"


#FILESEXTRAPATHS_prepend_ast2400 := "${THISDIR}/ast2400:"
#FILESEXTRAPATHS_prepend_ast2500 := "${THISDIR}/ast2500:"
FILESEXTRAPATHS_prepend_pilot4 := "${THISDIR}/pilot4:"


LINUX_VERSION_EXTENSION ?= "-${SRCREV}"

PV = "${LINUX_VERSION}+git${SRCPV}"

inherit kernel
require recipes-kernel/linux/linux-yocto.inc

# From 4.16+ the COPYING file changed
LIC_FILES_CHKSUM = "file://COPYING;md5=bbea815ee2795b2f4230826c0c6b8814"
