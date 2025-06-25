SUMMARY = "Three partition MTD image with u-boot, kernel and bootmcu"
HOMEPAGE = "https://github.com/openbmc/meta-aspeed"
LICENSE = "MIT"
PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit deploy
inherit ${@bb.utils.contains_any('MACHINE_FEATURES', ['ast-mmc', 'ast-ufs'], 'image', '', d)}

UBOOT_SUFFIX ?= "bin"

ASPEED_IMAGE_UBOOT_SPL_IMAGE ?= "u-boot-spl"
ASPEED_IMAGE_UBOOT_IMAGE ?= "u-boot"

ASPEED_IMAGE_SIZE_KB = "32768"
ASPEED_IMAGE_SIZE_KB:aspeed-g5 ?= "24576"

ASPEED_IMAGE_KERNEL_IMAGE ?= "fitImage-${INITRAMFS_IMAGE}-${MACHINE}-${MACHINE}"
ASPEED_IMAGE_NAME ?= "all.bin"
ASPEED_BOOT_EMMC_UFS ?= "${@bb.utils.contains_any('MACHINE_FEATURES', ['ast-mmc', 'ast-ufs'], 'yes', 'no', d)}"

#IMAGE_FSTYPES += "${@bb.utils.contains_any('MACHINE_FEATURES', ['ast-mmc', 'ast-ufs'], 'wic.xz mmc-ext4-tar', 'no', d)}"
#IMAGE_FEATURES += "${@bb.utils.contains_any('MACHINE_FEATURES', ['ast-mmc', 'ast-ufs'], 'read-only-rootfs-delayed-postinsts', 'no', d)}"

# Flash characteristics in KB unless otherwise noted
DISTROOVERRIDES .= ":flash-${FLASH_SIZE}"

do_mk_empty_image() {
    # Assemble the flash image
    dd if=/dev/zero bs=1k count=${ASPEED_IMAGE_SIZE_KB} | \
        tr '\000' '\377' > ${B}/aspeed-sdk.bin
}

python do_deploy() {
    import subprocess

    if d.getVar('ASPEED_BOOT_EMMC_UFS', True) == "yes":
        bb.fatal("MMC UFS mode should not run this task")

    initramfs_image = d.getVar('INITRAMFS_IMAGE', True)
    if initramfs_image != "aspeed-image-initramfs":
         bb.fatal('Not support ' + str(initramfs_image) + ' INITRAMFS_IMAGE')

    bootmcu_fw_binary = d.getVar('BOOTMCU_FW_BINARY', True)
    spl_binary = d.getVar('SPL_BINARY', True)

    if bootmcu_fw_binary and spl_binary:
        bb.fatal('SPL_BINARY and BOOTMCU_FW_BINARY should not be set at the same time')

    bb.build.exec_func("do_mk_empty_image", d)
    nor_image = os.path.join(d.getVar('B', True), "aspeed-sdk.bin")


    def _append_image(imgpath, start_kb, finish_kb):
        imgsize = os.path.getsize(imgpath)
        maxsize = (finish_kb - start_kb) * 1024
        bb.debug(1, 'Considering file size=' + str(imgsize) + ' name=' + imgpath)
        bb.debug(1, 'Spanning start=' + str(start_kb) + 'K end=' + str(finish_kb) + 'K')
        bb.debug(1, 'Compare needed=' + str(imgsize) + ' available=' + str(maxsize) + ' margin=' + str(maxsize - imgsize))
        if imgsize > maxsize:
            bb.fatal("Image '%s' is too large!" % imgpath)

        subprocess.check_call(['dd', 'bs=1k', 'conv=notrunc',
                                      'seek=%d' % start_kb,
                                      'if=%s' % imgpath,
                                      'of=%s' % nor_image])


    uboot_offset = int(d.getVar('FLASH_UBOOT_OFFSET', True))

    # Caliptra
    caliptra_fw_binary = d.getVar('CALIPTRA_FW_BINARY', True)
    if caliptra_fw_binary:
        caliptra_fw_finish_kb = int(d.getVar('FLASH_CALIPTRA_SIZE', True))
        _append_image(os.path.join(d.getVar('DEPLOY_DIR_IMAGE', True),
                                   '%s' % (d.getVar('CALIPTRA_FW_BINARY', True))),
                      uboot_offset,
                      caliptra_fw_finish_kb)
        uboot_offset += caliptra_fw_finish_kb

    # bootmcu
    if bootmcu_fw_binary:
        bootmcu_fw_finish_kb = int(d.getVar('FLASH_BMCU_SIZE', True))
        _append_image(os.path.join(d.getVar('DEPLOY_DIR_IMAGE', True),
                                   '%s' % (d.getVar('BOOTMCU_FW_BINARY', True))),
                      uboot_offset,
                      bootmcu_fw_finish_kb)
        uboot_offset += bootmcu_fw_finish_kb

    # spl
    if spl_binary:
        spl_finish_kb = int(d.getVar('FLASH_UBOOT_SPL_SIZE', True))
        _append_image(os.path.join(d.getVar('DEPLOY_DIR_IMAGE', True),
                                   '%s.%s' % (
                                   d.getVar('ASPEED_IMAGE_UBOOT_SPL_IMAGE', True),
                                   d.getVar('UBOOT_SUFFIX', True))),
                      uboot_offset,
                      spl_finish_kb)
        uboot_offset += spl_finish_kb

    # u-boot
    uboot_size_kb = int(d.getVar('FLASH_UBOOT_ENV_OFFSET', True)) - uboot_offset
    uboot_finish_kb = uboot_offset + uboot_size_kb
    _append_image(os.path.join(d.getVar('DEPLOY_DIR_IMAGE', True),
                  '%s.%s' % (
                  d.getVar('ASPEED_IMAGE_UBOOT_IMAGE', True),
                  d.getVar('UBOOT_SUFFIX', True))),
                  uboot_offset,
                  uboot_finish_kb)

    # kernel
    _append_image(os.path.join(d.getVar('DEPLOY_DIR_IMAGE', True),
                  '%s' %
                  d.getVar('ASPEED_IMAGE_KERNEL_IMAGE',True)),
                  int(d.getVar('FLASH_KERNEL_OFFSET', True)),
                  int(d.getVar('ASPEED_IMAGE_SIZE_KB', True)))

    dest_image = os.path.join(d.getVar('DEPLOYDIR', True), d.getVar('ASPEED_IMAGE_NAME', True))
    subprocess.check_call(['install',
                           '-m644',
                           '-D',
                           '%s' % nor_image,
                           '%s' % dest_image])
}

do_deploy[depends] = " \
    virtual/kernel:do_deploy \
    virtual/bootloader:do_deploy \
    ${@bb.utils.contains('MACHINE_FEATURES', 'ast-bootmcu', 'virtual/bootmcu:do_deploy', '', d)} \
    "
do_fetch[noexec] = "1"
do_unpack[noexec] = "1"
do_patch[noexec] = "1"
do_configure[noexec] = "1"
do_compile[noexec] = "1"
do_install[noexec] = "1"
deltask do_populate_sysroot
do_package[noexec] = "1"
deltask do_package_qa
do_packagedata[noexec] = "1"
deltask do_package_write_ipk
deltask do_package_write_deb
deltask do_package_write_rpm
addtask deploy before do_build after do_compile
