SUMMARY = "BootMCU firmware and execute SPL"
DESCRIPTION = "BootMCU is designated to load the first, verified image for the main processor"
PACKAGE_ARCH = "${MACHINE_ARCH}"

PROVIDES += "virtual/bootmcu"
DEPENDS = "ast2700-riscv-linux-gnu-native kern-tools-native swig-native ${PYTHON_PN}-setuptools-native fmc-imgtool-native fmc-images"
DEPENDS += "${@bb.utils.contains('MACHINE_FEATURES', 'ast-secure', 'aspeed-secure-config-native u-boot-tools-native', '', d)}"

inherit deploy cml1 python3native otptool

RISCV_PREBUILT_TOOLCHAIN = "${STAGING_DIR_NATIVE}${datadir}/ast2700-riscv-linux-gnu/bin/riscv32-unknown-linux-gnu-"

EXTRA_OEMAKE = 'CROSS_COMPILE=${RISCV_PREBUILT_TOOLCHAIN} CC="${RISCV_PREBUILT_TOOLCHAIN}gcc" V=1'
EXTRA_OEMAKE += 'HOSTCC="${BUILD_CC} ${BUILD_CFLAGS} ${BUILD_LDFLAGS}"'

PACKAGECONFIG ??= "openssl"
# u-boot will compile its own tools during the build, with specific
# configurations (aka when CONFIG_FIT_SIGNATURE is enabled) openssl is needed as
# a host build dependency.
PACKAGECONFIG[openssl] = ",,openssl-native"

# Allow setting an additional version string that will be picked up by the
# u-boot build system and appended to the u-boot version.  If the .scmversion
# file already exists it will not be overwritten.
UBOOT_LOCALVERSION ?= ""

# mkimage command
UBOOT_MKIMAGE_SIGN ?= "uboot-mkimage"

# uboot-fit_check_sign command
UBOOT_FIT_CHECK_SIGN ?= "uboot-fit_check_sign"

# Options for the device tree compiler passed to mkimage '-D' feature:
SPL_MKIMAGE_DTCOPTS ?= ""

# Arguments passed to mkimage for signing
SPL_MKIMAGE_SIGN_ARGS ?= ""

# returns all the elements from the src uri that are .cfg files
def find_cfgs(d):
    sources=src_patches(d, True)
    sources_list=[]
    for s in sources:
        if s.endswith('.cfg'):
            sources_list.append(s)

    return sources_list


do_configure () {
    oe_runmake -C ${S} O=${B} ${BOOTMCU_MACHINE}
    merge_config.sh -m .config ${@" ".join(find_cfgs(d))}
    cml1_do_configure
}

do_compile() {
    unset LDFLAGS
    unset CFLAGS
    unset CPPFLAGS

    if [ ! -e ${B}/.scmversion -a ! -e ${S}/.scmversion ]
    then
        echo ${UBOOT_LOCALVERSION} > ${B}/.scmversion
        echo ${UBOOT_LOCALVERSION} > ${S}/.scmversion
    fi

    oe_runmake -C ${S} O=${B} all
}

do_install () {
    install -d ${D}/boot
    install -m 644 ${B}/spl/u-boot-spl.bin ${D}/boot/u-boot-spl.bin
}

FILES:${PN} = "/boot ${sysconfdir} ${datadir}"

# Add the public key into the SPL device tree (u-boot-spl.dtb) by signing
# a dummy FIT image and injecting the key into the /signature node of the DTB.
#
# This function only runs when:
#   SPL_SIGN_ENABLE = "1" --> Signing is enabled
#   SPL_DTB_BINARY = ""   --> Explicitly indicates that the SPL DTB should not be
#   generated or signed within the U-Boot build recipe itself.
#
# This is typically used in a post-processing step outside the U-Boot recipe,
# allowing the public key to be managed independently.
do_add_pubkey_to_spl_dtb() {
    # Proceed only when SPL signing is enabled, and public key needs to be added to the SPL DTB
    if [ "${SPL_SIGN_ENABLE}" != "1" ] || [ -n "${SPL_DTB_BINARY}" ]; then
        echo "Skipping SPL DTB public key injection"
        return 0
    fi

    # Sign dummy image in order to add the signing keys to SPL dtb
    ${UBOOT_MKIMAGE_SIGN} \
        ${@'-D "${SPL_MKIMAGE_DTCOPTS}"' if len('${SPL_MKIMAGE_DTCOPTS}') else ''} \
        -f auto \
        -k "${SPL_SIGN_KEYDIR}" \
        -o "${UBOOT_FIT_HASH_ALG},${UBOOT_FIT_SIGN_ALG}" \
        -g "${SPL_SIGN_KEYNAME}" \
        -K "${B}/spl/u-boot-spl.dtb" \
        -d /dev/null \
        -r ${B}/unused.itb \
        ${SPL_MKIMAGE_SIGN_ARGS}

    # Verify the dummy fitImage signature against SPL dtb
    # augmented using public key material.
    ${UBOOT_FIT_CHECK_SIGN} \
        -k "${B}/spl/u-boot-spl.dtb" \
        -f ${B}/unused.itb

    # Reconstruct final SPL binary by concatenating nodtb part and signed DTB
    cat ${B}/spl/u-boot-spl-nodtb.bin ${B}/spl/u-boot-spl.dtb > ${B}/spl/u-boot-spl.bin
}

addtask add_pubkey_to_spl_dtb before do_deploy do_create_fmc_image after do_compile

# Use fmc-imgtool to create fmc image since A1
# export CRYPTOGRAPHY_OPENSSL_NO_LEGACY variable to fix the following errors.
# OpenSSL 3.0 legacy provider failed to load
# https://github.com/pyca/cryptography/issues/10598
do_create_fmc_image() {
    export CRYPTOGRAPHY_OPENSSL_NO_LEGACY=1

    local ecc_key=""
    local ecc_key_index=""
    local lms_key=""
    local lms_key_index=""
    local sign_args=""

    if [ -f "${FMC_ECC_KEY}" ]; then
        ecc_key="--ecc-key ${FMC_ECC_KEY}"
    fi

    if [ -n "${FMC_ECC_KEY_INDEX}" ]; then
        ecc_key_index="--ecc-key-index ${FMC_ECC_KEY_INDEX}"
    fi

    if [ -f "${FMC_LMS_KEY}" ]; then
        lms_key="--lms-key ${FMC_LMS_KEY}"
    fi

    if [ -n "${FMC_LMS_KEY_INDEX}" ]; then
        lms_key_index="--lms-key-index ${FMC_LMS_KEY_INDEX}"
    fi

    if [ "${FMC_SIGN_ENABLE}" = "1" ]; then
        sign_args="${ecc_key} ${ecc_key_index} ${lms_key} ${lms_key_index}"
    fi

    echo "sign_args=${sign_args}"

    fmc-imgtool \
        --verbose \
        --version 2 \
        --input ${B}/spl/u-boot-spl.bin \
        --output ${B}/${BOOTMCU_FW_BINARY} \
        --prebuilt-dir ${STAGING_DATADIR}/fmc-images/prebuilt/ \
        ${sign_args}
}

addtask create_fmc_image before do_deploy after do_compile

do_deploy() {
    sub_ver=$(cat ${B}/.subversion)
    install -d ${DEPLOYDIR}

    # spl
    install -m 644 ${B}/spl/u-boot-spl.dtb ${DEPLOYDIR}
    install -m 644 ${B}/spl/u-boot-spl-nodtb.bin ${DEPLOYDIR}
    install -m 644 ${B}/spl/u-boot-spl.bin ${DEPLOYDIR}

    # TODO: The FMC tool does not support verification yet.
    # To reduce the risk of unexpected run-time errors, verification should be added.

    # spl fmc since AST2700 A1
    install -m 644 ${BOOTMCU_FW_BINARY} ${DEPLOYDIR}
    install -m 644 ${BOOTMCU_FW_BINARY} ${DEPLOYDIR}/${BOOTMCU_FW_BINARY}-${sub_ver}
}

addtask deploy before do_build after do_compile

ALLOW_EMPTY:${PN} = "1"
