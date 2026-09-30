SUMMARY = "NVIDIA LSTP (Low-Speed Transport Protocol) kernel module"
DESCRIPTION = "Out-of-tree Linux USB driver for the NVIDIA Low-Speed Transport \
Protocol (LSTP) device, exposing firmware-configured I2C, SPI, GPIO, UART, \
IPMI, and MMIO channels over a single USB interface."
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://LICENSE;md5=bb9a1a2dd48e5f3bd19c6c1930d05f6a"

SRC_URI = "git://github.com/NVIDIA/lstp_module.git;protocol=https;branch=main \
           file://lstp.service \
          "
SRCREV = "a91dee07c285a22139f17533049b2924c1fbf045"

PV = "1.0+git"

DEPENDS = "virtual/kernel"

inherit module systemd

# module.bbclass/kernel-module-split.bbclass only register the kernel-module-*
# package at do_package time; unlike kernel.bbclass, they don't declare
# PACKAGES_DYNAMIC, so nothing lets other recipes' RDEPENDS/RRECOMMENDS on
# kernel-module-lstp find this recipe before it has ever been built.
PACKAGES_DYNAMIC += "^kernel-module-.*"

SYSTEMD_SERVICE:${PN} = "lstp.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

RDEPENDS:${PN} += "kmod"
FILES:${PN} += "${systemd_system_unitdir}/lstp.service"

# The upstream Makefile uses KDIR rather than module.bbclass's KERNEL_SRC
# interface and stages sources into its own build directory. Invoke Kbuild
# directly so the module is built and installed using Yocto's configured
# target kernel and toolchain.
module_do_compile() {
	unset CFLAGS CPPFLAGS CXXFLAGS LDFLAGS
	oe_runmake -C ${STAGING_KERNEL_DIR} M=${S} \
		   KERNEL_VERSION=${KERNEL_VERSION} \
		   CC="${KERNEL_CC}" LD="${KERNEL_LD}" \
		   AR="${KERNEL_AR}" OBJCOPY="${KERNEL_OBJCOPY}" \
		   STRIP="${KERNEL_STRIP}" \
		   O=${STAGING_KERNEL_BUILDDIR} \
		   KBUILD_EXTRA_SYMBOLS="${KBUILD_EXTRA_SYMBOLS}" \
		   modules
}

module_do_install() {
	unset CFLAGS CPPFLAGS CXXFLAGS LDFLAGS
	oe_runmake -C ${STAGING_KERNEL_DIR} M=${S} \
		   DEPMOD=echo MODLIB="${D}${nonarch_base_libdir}/modules/${KERNEL_VERSION}" \
		   INSTALL_FW_PATH="${D}${nonarch_base_libdir}/firmware" \
		   CC="${KERNEL_CC}" LD="${KERNEL_LD}" OBJCOPY="${KERNEL_OBJCOPY}" \
		   STRIP="${KERNEL_STRIP}" \
		   O=${STAGING_KERNEL_BUILDDIR} \
		   KBUILD_EXTRA_SYMBOLS="${KBUILD_EXTRA_SYMBOLS}" \
		   modules_install

	install -d ${D}${systemd_system_unitdir}
	install -m 0644 ${UNPACKDIR}/lstp.service \
		${D}${systemd_system_unitdir}/lstp.service
}
