SUMMARY = "The Caliptra firmware"
DESCRIPTION = "The Caliptra prebuild firmware for AST2700."
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${ASPEEDSDKBASE}/LICENSE;md5=a3740bd0a194cd6dcafdc482a200a56f"
PACKAGE_ARCH = "${MACHINE_ARCH}"

PR = "r0"

CALIPTRA_FIRMWARE ?= "ast2700-caliptra-fw.bin"

S = "${WORKDIR}/sources"
UNPACKDIR = "${S}"
SRC_URI = "file://${CALIPTRA_FIRMWARE};subdir=${S}"

do_patch[noexec] = "1"
do_configure[noexec] = "1"
do_compile[noexec] = "1"

inherit deploy

do_deploy () {
    install -d ${DEPLOYDIR}
    install -m 644 ${S}/${CALIPTRA_FIRMWARE} ${DEPLOYDIR}/.
}

addtask deploy before do_build after do_compile

ALLOW_EMPTY:${PN} = "1"

