SUMMARY = "Cerberus PFR provision image"
DESCRIPTION = "Cerberus PFR provision image creation"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit python3native setuptools3

DEPENDS:append = " ${PYTHON_PN}-pycryptodome-native \
                   cerberus-pfr-signing-utility-native \
                 "

SRC_URI = " file://provision_tools;subdir=${S} "
SRC_URI += " file://key_management_tools;subdir=${S} "

do_patch[noexec] = "1"
do_configure[noexec] = "1"
do_compile[noexec] = "1"

PFR_PROVISION_TOOLS_DIR = "${STAGING_DIR_NATIVE}${datadir}/cerberus/provision_tools"
PFR_KEY_MANAGEMENT_TOOLS_DIR = "${STAGING_DIR_NATIVE}${datadir}/cerberus/key_management_tools"

# Provision image size is 4KB
# offset    0 - 2047 : provisioning information(Root key, BMC/PCH firmware offset, ... etc.)
# offset 2048 - 4095 : key manifest image
PROVISION_IMAGE_SIZE = "4"

do_install() {
    rm -f ${PFR_PROVISION_TOOLS_DIR}/*.bin

    # install config and xml
    install -m 0644 ${S}/key_management_tools/*.* ${PFR_KEY_MANAGEMENT_TOOLS_DIR}

    cd ${PFR_KEY_MANAGEMENT_TOOLS_DIR}
    python3 key_management_tool.py key_manifest0_image.config

    # install json and ini
    install -m 0644 ${S}/provision_tools/*.* ${PFR_PROVISION_TOOLS_DIR}/.

    cd ${PFR_PROVISION_TOOLS_DIR}
    python3 provisioning_image_generator.py provisioning_image_generator_rootkey.ini

    dd if=/dev/zero bs=1k count=${PROVISION_IMAGE_SIZE} | tr '\000' '\377' > \
        ${PFR_PROVISION_TOOLS_DIR}/final_provision.bin
    dd bs=1 conv=notrunc seek=0 \
        if=${PFR_PROVISION_TOOLS_DIR}/provision_rootkey.bin \
        of=${PFR_PROVISION_TOOLS_DIR}/final_provision.bin
    dd bs=1 conv=notrunc seek=2048 \
        if=${PFR_KEY_MANAGEMENT_TOOLS_DIR}/key_manifest0_image.bin \
        of=${PFR_PROVISION_TOOLS_DIR}/final_provision.bin

    cd ${S}

    install -d ${D}${datadir}/cerberus
    install -m 0644 ${PFR_PROVISION_TOOLS_DIR}/final_provision.bin ${D}${datadir}/cerberus/provision_rootkey.bin
}

FILES:${PN}:append = " ${datadir}/cerberus/* "
