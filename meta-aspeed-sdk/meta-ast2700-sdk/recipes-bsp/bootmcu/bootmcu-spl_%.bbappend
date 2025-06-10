FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

# save unsigned binaries
do_compile:append:ast-secure() {
    install -d ${B}/unsigned-bin
    install -m 0644 ${B}/spl/u-boot-spl-nodtb.bin ${B}/unsigned-bin
    install -m 0644 ${B}/spl/u-boot-spl.dtb ${B}/unsigned-bin
}

# install unsigned binaries to SYSROOT_DIRS and allow recipes which depend on u-boot to use its
# installed artifacts from RECIPE_SYSROOT instead of DEPLOY_DIR_IMAGE
do_install:append:ast-secure() {
    install -d ${D}/sysroot-only
    install -m 0644 ${B}/unsigned-bin/* ${D}/sysroot-only
}

