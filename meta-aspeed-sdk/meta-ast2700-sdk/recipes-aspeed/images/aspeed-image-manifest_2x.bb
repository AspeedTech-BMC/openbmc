DESCRIPTION = "Generate ASPEED Caliptra Manifest image"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${ASPEEDSDKBASE}/LICENSE;md5=a3740bd0a194cd6dcafdc482a200a56f"
PACKAGE_ARCH = "${MACHINE_ARCH}"

S = "${UNPACKDIR}"

do_patch[noexec] = "1"
do_configure[noexec] = "1"
do_install[noexec] = "1"

inherit deploy

DEPENDS += "cptra-imgtool-native aspeed-secure-config-native"

CALIPTRA_MANIFEST_AUTH_FLASH_EXTRA_COMMAND ?= ""
CALIPTRA_MANIFEST_AUTH_MAN_ENABLE ?= "1"
CALIPTRA_MANIFEST_FLASH_IMAGE ?= "ast2705-manifest-flash.bin"
CALIPTRA_MANIFEST_SOC_IMAGE ?= "ast2705-soc-manifest.bin"

# Using cptra-imgtool to create manifest image.
create_cptra_manifest_image() {
    export RUST_LOG="debug"

    local caliptra_manifest_key_dir=""
    local caliptra_manifest_auth_flash_extra_command="${CALIPTRA_MANIFEST_AUTH_FLASH_EXTRA_COMMAND}"

    if [ -n "${CALIPTRA_MANIFEST_KEY_DIR}" ]; then
        caliptra_manifest_key_dir="--key-dir ${CALIPTRA_MANIFEST_KEY_DIR}/"
    fi

    echo "caliptra_manifest_key_dir=${caliptra_manifest_key_dir}"
    echo "caliptra_manifest_auth_flash_extra_command=${caliptra_manifest_auth_flash_extra_command}"

    # Build the Caliptra Flash Image (including the Caliptra SoC manifest).
    cptra-imgtool \
        create-auth-flash-2x \
        --cfg ${CALIPTRA_MANIFEST_CONFIG_DIR}/${CALIPTRA_MANIFEST_CONFIG} \
        ${caliptra_manifest_key_dir} \
        --prebuilt-dir ${DEPLOY_DIR_IMAGE}/ \
        ${caliptra_manifest_auth_flash_extra_command} \
        --flash ${B}/${CALIPTRA_MANIFEST_FLASH_IMAGE} \
        --pqc-key-type 1

    if [ "${CALIPTRA_MANIFEST_AUTH_MAN_ENABLE}" = "1" ]; then
        # Build only the Caliptra SoC Manifest.
        cptra-imgtool \
            create-auth-man-2x \
            --cfg ${CALIPTRA_MANIFEST_CONFIG_DIR}/${CALIPTRA_MANIFEST_CONFIG} \
            ${caliptra_manifest_key_dir} \
            --prebuilt-dir ${DEPLOY_DIR_IMAGE}/ \
            --man ${B}/${CALIPTRA_MANIFEST_SOC_IMAGE} \
            --pqc-key-type 1
    fi
}

do_compile() {
    create_cptra_manifest_image
}

do_compile[depends] += " \
    optee-os:do_deploy \
    trusted-firmware-a:do_deploy \
    virtual/bootloader:do_deploy \
    bmc-pb:do_deploy \
    aspeed-manifest-config:do_deploy \
    ${@bb.utils.contains('MACHINE_FEATURES', 'ast-ssp', 'virtual/ssp:do_deploy', '', d)} \
    ${@bb.utils.contains('MACHINE_FEATURES', 'ast-tsp', 'virtual/tsp:do_deploy', '', d)} \
    "
do_compile[nostamp] = "1"

do_deploy() {
    install -d ${DEPLOYDIR}
    install -m 644 ${B}/${CALIPTRA_MANIFEST_FLASH_IMAGE} ${DEPLOYDIR}
    if [ "${CALIPTRA_MANIFEST_AUTH_MAN_ENABLE}" = "1" ]; then
        install -m 644 ${B}/${CALIPTRA_MANIFEST_SOC_IMAGE} ${DEPLOYDIR}
    fi
}
do_deploy[nostamp] = "1"

addtask deploy before do_build after do_compile
