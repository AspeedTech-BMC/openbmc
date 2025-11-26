FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI:append:ast-mmc = " file://u-boot-env.txt"
SRC_URI:append:ast-ufs = " file://u-boot-env-ufs.txt"

# save unsigned binaries
do_compile:append() {
    install -d ${B}/unsigned-bin

    if [ -f ${B}/u-boot-nodtb.bin ]; then
        install -m 0644 ${B}/u-boot-nodtb.bin ${B}/unsigned-bin
    fi

    if [ -f ${B}/u-boot.dtb ]; then
        install -m 0644 ${B}/u-boot.dtb ${B}/unsigned-bin
    fi
}

# install unsigned binaries to SYSROOT_DIRS and allow recipes which depend on u-boot to use its
# installed artifacts from RECIPE_SYSROOT instead of DEPLOY_DIR_IMAGE
do_install:append() {
    install -d ${D}/sysroot-only
    install -m 0644 ${B}/unsigned-bin/* ${D}/sysroot-only
}

