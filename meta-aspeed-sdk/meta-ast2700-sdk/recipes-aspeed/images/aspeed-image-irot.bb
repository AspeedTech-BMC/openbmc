DESCRIPTION = "Generate AST2700 irot image for PLDM Firmware Update"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${ASPEEDSDKBASE}/LICENSE;md5=a3740bd0a194cd6dcafdc482a200a56f"
PACKAGE_ARCH = "${MACHINE_ARCH}"

PR = "r0"

SRC_URI = " \
    file://img.py \
    file://metadata-irot.json \
    "

S = "${UNPACKDIR}"

DEPENDS = " \
    python3-bitarray-native \
    "

do_patch[noexec] = "1"
do_configure[noexec] = "1"
do_install[noexec] = "1"

inherit python3native deploy

IROT_PKG_IMAGE ?= "irot.pkg"
IROT_METADATA_FILE ?= "metadata-irot.json"

do_compile() {
    install -d ${B}
    ln -sf ${CALIPTRA_MANIFEST_FLASH_IMAGE} ${DEPLOY_DIR_IMAGE}/image-bmc

    ${PYTHON} ${UNPACKDIR}/img.py \
        ${B}/${IROT_PKG_IMAGE} \
        ${UNPACKDIR}/${IROT_METADATA_FILE} \
        ${DEPLOY_DIR_IMAGE}/${CALIPTRA_MANIFEST_FLASH_IMAGE}
}

do_compile[depends] += " \
    aspeed-image-manifest:do_deploy \
    "

do_deploy() {
    install -d ${DEPLOYDIR}
    install -m 0644 ${B}/${IROT_PKG_IMAGE} ${DEPLOYDIR}/
}

addtask deploy before do_build after do_compile
