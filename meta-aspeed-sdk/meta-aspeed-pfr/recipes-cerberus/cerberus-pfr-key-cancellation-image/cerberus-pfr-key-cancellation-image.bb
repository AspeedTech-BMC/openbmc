SUMMARY = "Cerberus PFR key cancellation image"
DESCRIPTION = "Cerberus PFR key cancellation creation"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit python3native setuptools3

DEPENDS:append = " ${PYTHON_PN}-pycryptodome-native \
                   cerberus-pfr-signing-utility-native \
                 "

SRC_URI = " file://key_management_tools;subdir=${S} "

do_patch[noexec] = "1"
do_configure[noexec] = "1"
do_compile[noexec] = "1"

PFR_KEY_MANAGEMENT_TOOLS_DIR = "${STAGING_DIR_NATIVE}${datadir}/cerberus/key_management_tools"

do_install() {
    rm -f ${PFR_KEY_MANAGEMENT_TOOLS_DIR}/*.bin

    # install config and xml
    install -m 0644 ${S}/key_management_tools/*.* ${PFR_KEY_MANAGEMENT_TOOLS_DIR}

    cd ${PFR_KEY_MANAGEMENT_TOOLS_DIR}
    python3 key_management_tool.py cancel_key_manifest0_keys.config
    python3 key_management_tool.py cancel_key_manifest1_keys.config
    cd ${S}

    install -d ${D}${datadir}/cerberus
    install -m 0644 ${PFR_KEY_MANAGEMENT_TOOLS_DIR}/*image.bin ${D}${datadir}/cerberus/.
}

FILES:${PN}:append = " ${datadir}/cerberus/* "
