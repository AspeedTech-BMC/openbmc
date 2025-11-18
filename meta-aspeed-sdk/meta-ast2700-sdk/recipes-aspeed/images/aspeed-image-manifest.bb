DESCRIPTION = "Generate ASPEED Caliptra Manifest image"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${ASPEEDSDKBASE}/LICENSE;md5=a3740bd0a194cd6dcafdc482a200a56f"
PACKAGE_ARCH = "${MACHINE_ARCH}"

PR = "r0"

do_patch[noexec] = "1"
do_configure[noexec] = "1"
do_install[noexec] = "1"

inherit deploy

DEPENDS += "cptra-imgtool-native aspeed-secure-config-native"

CALIPTRA_MANIFEST_FLASH_IMAGE ?= "ast2700-manifest-flash.bin"
CALIPTRA_MANIFEST_RECOVERY_IMAGE ?= "ast2700-soc-manifest.bin"
ASPEED_IROT = "${@bb.utils.contains('MACHINE_FEATURES', 'ast-irot', 'yes', 'no', d)}"

# Using cptra-imgtool to create manifest image.
create_cptra_manifest_image() {
    export RUST_LOG="debug"

    local caliptra_manifest_key_dir=""

    if [ -n "${CALIPTRA_MANIFEST_KEY_DIR}" ]; then
        caliptra_manifest_key_dir="--key-dir ${CALIPTRA_MANIFEST_KEY_DIR}/"
    fi

    echo "caliptra_manifest_key_dir=${caliptra_manifest_key_dir}"

    # Run cptra-imgtool to generate manifest flash image.
    cptra-imgtool \
        create-auth-flash \
        --cfg ${CALIPTRA_MANIFEST_CONFIG_DIR}/${CALIPTRA_MANIFEST_CONFIG} \
        ${caliptra_manifest_key_dir} \
        --prebuilt-dir ${DEPLOY_DIR_IMAGE}/ \
        --flash ${B}/${CALIPTRA_MANIFEST_FLASH_IMAGE}

    # Run cptra-imgtool to generate manifest image for recovery.
    cptra-imgtool \
        create-auth-man \
        --cfg ${CALIPTRA_MANIFEST_CONFIG_DIR}/${CALIPTRA_MANIFEST_CONFIG} \
        ${caliptra_manifest_key_dir} \
        --prebuilt-dir ${DEPLOY_DIR_IMAGE}/ \
        --man ${B}/${CALIPTRA_MANIFEST_RECOVERY_IMAGE}
}

do_compile() {
    create_cptra_manifest_image
}

do_compile[depends] += " \
    optee-os:do_deploy \
    trusted-firmware-a:do_deploy \
    virtual/bootloader:do_deploy \
    virtual/bootmcu:do_deploy \
    fmc-images:do_deploy \
    ${@bb.utils.contains('MACHINE_FEATURES', 'ast-ssp', 'virtual/ssp:do_deploy', '', d)} \
    ${@bb.utils.contains('MACHINE_FEATURES', 'ast-tsp', 'virtual/tsp:do_deploy', '', d)} \
    "

do_deploy_image() {
    install -d ${DEPLOYDIR}
    install -m 644 ${B}/${CALIPTRA_MANIFEST_FLASH_IMAGE} ${DEPLOYDIR}
    install -m 644 ${B}/${CALIPTRA_MANIFEST_RECOVERY_IMAGE} ${DEPLOYDIR}
}


def make_empty_image(img, size_kb):
    size = int(size_kb) * 1024
    with open(img, "wb+") as fp:
        fp.seek(0)
        fp.write(b'\xFF'*size)


def append_image(inimg, outimg, start_kb, finish_kb):
    import subprocess
    imgsize = os.path.getsize(inimg)
    maxsize = (finish_kb - start_kb) * 1024
    print(flush=True)
    bb.debug(1, 'Considering file size=' + str(imgsize) + ' name=' + inimg)
    bb.debug(1, 'Spanning start=' + str(start_kb) + 'K end=' + str(finish_kb) + 'K')
    bb.debug(1, 'Compare needed=' + str(imgsize) + ' available=' + str(maxsize) + ' margin=' + str(maxsize - imgsize))
    if imgsize > maxsize:
        bb.fatal("Image '%s' is too large!" % inimg)

    cmd = "dd bs=1k conv=notrunc seek=%d if=%s of=%s" % (start_kb, inimg, outimg)
    print(cmd)
    subprocess.check_call(cmd, shell=True)


def create_irot_image(d):
    import subprocess

    irot_boot_img = os.path.join(d.getVar('B', True), 'irot_boot_img')
    make_empty_image(irot_boot_img, d.getVar('IROT_IMAGE_SIZE', True))

    # Caliptra manifest
    append_image(os.path.join(d.getVar('B', True), d.getVar('CALIPTRA_MANIFEST_FLASH_IMAGE', True)),
                 irot_boot_img,
                 int(d.getVar('IROT_OFFSET_MANIFEST', True)),
                 int(d.getVar('IROT_OFFSET_ATF', True)))
    # ATF
    append_image(d.getVar('UBOOT_FIT_ARM_TRUSTED_FIRMWARE_IMAGE', True),
                 irot_boot_img,
                 int(d.getVar('IROT_OFFSET_ATF', True)),
                 int(d.getVar('IROT_OFFSET_UBOOT', True)))
    # U-Boot raw image
    append_image(os.path.join(d.getVar('DEPLOY_DIR_IMAGE', True), 'u-boot.bin'),
                 irot_boot_img,
                 int(d.getVar('IROT_OFFSET_UBOOT', True)),
                 int(d.getVar('IROT_OFFSET_TEE', True)))
    # TEE
    append_image(d.getVar('UBOOT_FIT_TEE_IMAGE', True),
                 irot_boot_img,
                 int(d.getVar('IROT_OFFSET_TEE', True)),
                 int(d.getVar('IROT_IMAGE_SIZE', True)))

    cmd = "rm -f {}".format(os.path.join(d.getVar('B', True), d.getVar('CALIPTRA_MANIFEST_FLASH_IMAGE', True)))
    print(cmd)
    subprocess.check_call(cmd, shell=True)

    cmd = "mv {} {}".format(irot_boot_img,
                            os.path.join(d.getVar('B', True), d.getVar('CALIPTRA_MANIFEST_FLASH_IMAGE', True)))
    print(cmd)
    subprocess.check_call(cmd, shell=True)


python do_deploy() {
    aspeed_irot = d.getVar('ASPEED_IROT', True)
    if aspeed_irot == "yes":
        print("Create_irot_image...")
        create_irot_image(d)

    bb.build.exec_func("do_deploy_image", d)
}

addtask deploy before do_build after do_compile

