SUMMARY = "Pre-built toolchain for the Ibex RISC-V core in AST2700 SoCs."
DESCRIPTION = "Pre-built toolchain for the Ibex RISC-V core in AST2700 SoCs.\
Built with --with-arch=rv32gc --with-abi=ilp32"
LICENSE = "GPL-3.0-with-GCC-exception & GPL-3.0-only"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

SRC_URI = "https://github.com/AspeedTech-BMC/riscv-gnu-toolchain/releases/download/v${PV}/${BPN}.tar.gz;subdir=${S}"

SRC_URI[md5sum] = "61a85f30dcf2d9b6a3ced3065d7dd090"
SRC_URI[sha256sum] = "05dec6cc815372f1f82284d2f829858c5361916e902cb0988fdfc0eaf7e38680"

do_patch[noexec] = "1"
do_configure[noexec] = "1"
do_compile[noexec] = "1"

inherit native

do_install() {
    install -d -m 0755 ${D}${datadir}
    cp --no-preserve=ownership -rf ${S}/${BPN} ${D}${datadir}/
}

INHIBIT_DEFAULT_DEPS = "1"

INSANE_SKIP:${PN} = "already-stripped libdir staticdev file-rdeps arch dev-so ldflags"

INHIBIT_SYSROOT_STRIP = "1"
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
