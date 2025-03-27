SUMMARY = "Ibex firmware"
DESCRIPTION = "Ibex firmware for AST2700."
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${ASPEEDSDKBASE}/LICENSE;md5=a3740bd0a194cd6dcafdc482a200a56f"
PACKAGE_ARCH = "${MACHINE_ARCH}"

PROVIDES += "virtual/ibexfw"
PR = "r0"

IBEX_FIRMWARE ?= "ast2700-ibex-fw.bin"

SRC_URI = "file://${IBEX_FIRMWARE};subdir=${S}"

do_patch[noexec] = "1"
do_configure[noexec] = "1"
do_compile[noexec] = "1"

inherit deploy

do_deploy () {
    install -d ${DEPLOYDIR}
    install -m 644 ${S}/${IBEX_FIRMWARE} ${DEPLOYDIR}/.
}

addtask deploy before do_build after do_compile

ALLOW_EMPTY:${PN} = "1"

