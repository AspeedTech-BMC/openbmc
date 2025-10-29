DESCRIPTION = "Generate ASPEED Caliptra Manifest image"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${ASPEEDSDKBASE}/LICENSE;md5=a3740bd0a194cd6dcafdc482a200a56f"
PACKAGE_ARCH = "${MACHINE_ARCH}"

PR = "r0"

do_patch[noexec] = "1"
do_configure[noexec] = "1"
do_compile[noexec] = "1"
do_install[noexec] = "1"

inherit deploy

DEPENDS += "cptra-imgtool-native caliptra-sw-native caliptra-mcu-sw-native fmc-images"

CPTRA_IMGTOOL_CFG ?= "ast2700a1-default"
CPTRA_IMGTOOL_TOML = "config/${CPTRA_IMGTOOL_PRJ}-manifest.toml"
CPTRA_PREBUILD_IMAGE_DIR ?= "prebuilt/ast2700a1-default"
CPTRA_FLASH_IMAGE ?= "ast2700-manifest-flash.bin"
CPTRA_NON_FLASH_IMAGE ?= "ast2700-soc-manifest.bin"

# Using cptra-imgtool to create manifest image.
do_deploy() {
    export RUST_LOG="debug"
    bbnote "Running cptra-imgtool"

    cd ${STAGING_DATADIR_NATIVE}/cptra-imgtool

    mkdir -p out
    mkdir -p ${CPTRA_PREBUILD_IMAGE_DIR}

    # Copy fmc-images prebuilt image into cptra-imgtool prebuilt folder
    install -m 644 ${DEPLOY_DIR_IMAGE}/fmc-images/* ${CPTRA_PREBUILD_IMAGE_DIR}/.

    # Overwrite AFT image into cptra-imgtool prebuilt folder
    if [ -n "${UBOOT_FIT_ARM_TRUSTED_FIRMWARE_IMAGE}" ]; then
        install -m 0644 ${UBOOT_FIT_ARM_TRUSTED_FIRMWARE_IMAGE} ${CPTRA_PREBUILD_IMAGE_DIR}/atf.bin
    fi

    # Overwrite OPTEE image into cptra-imgtool prebuilt folder
    if [ -n "${UBOOT_FIT_TEE_IMAGE}" ]; then
        install -m 0644 ${UBOOT_FIT_TEE_IMAGE} ${CPTRA_PREBUILD_IMAGE_DIR}/optee.bin
    fi

    # Overwrite U-Boot raw image into cptra-imgtool prebuilt folder
    install -m 0644 ${DEPLOY_DIR_IMAGE}/u-boot.bin ${CPTRA_PREBUILD_IMAGE_DIR}/u-boot.bin

    # Overwrite SSP image into cptra-imgtool prebuilt folder
    if [ -n "${SSP_IMAGE}" ]; then
        install -m 0644 ${SSP_IMAGE} ${CPTRA_PREBUILD_IMAGE_DIR}/ssp.bin
    fi

    # Overwrite TSP image into cptra-imgtool prebuilt folder
    if [ -n "${TSP_IMAGE}" ]; then
        install -m 0644 ${TSP_IMAGE} ${CPTRA_PREBUILD_IMAGE_DIR}/tsp.bin
    fi

    # Run cptra-imgtool to generate manifest flash image.
    ./cptra-imgtool create-auth-flash --cfg ${CPTRA_IMGTOOL_CFG} --flash ${CPTRA_FLASH_IMAGE}

    # Run cptra-imgtool to generate manifest image for recovery.
    bbnote "Running cptra-imgtool for recovery"
    ./cptra-imgtool create-auth-man --cfg ${CPTRA_IMGTOOL_CFG} --man ${CPTRA_NON_FLASH_IMAGE}

    cd -

    # deploy manifest image
    install -d ${DEPLOYDIR}
    install -m 644 ${STAGING_DATADIR_NATIVE}/cptra-imgtool/${CPTRA_FLASH_IMAGE} ${DEPLOYDIR}
    install -m 644 ${STAGING_DATADIR_NATIVE}/cptra-imgtool/${CPTRA_NON_FLASH_IMAGE} ${DEPLOYDIR}
}

do_deploy[depends] += " \
    optee-os:do_deploy \
    trusted-firmware-a:do_deploy \
    virtual/bootloader:do_deploy \
    ${@bb.utils.contains('MACHINE_FEATURES', 'ast-ssp', 'virtual/ssp:do_deploy', '', d)} \
    ${@bb.utils.contains('MACHINE_FEATURES', 'ast-tsp', 'virtual/tsp:do_deploy', '', d)} \
    "

addtask deploy before do_build after do_compile

