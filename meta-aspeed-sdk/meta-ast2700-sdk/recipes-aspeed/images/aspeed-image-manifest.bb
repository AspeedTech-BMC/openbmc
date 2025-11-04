DESCRIPTION = "Generate ASPEED Caliptra Manifest image"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${ASPEEDSDKBASE}/LICENSE;md5=a3740bd0a194cd6dcafdc482a200a56f"
PACKAGE_ARCH = "${MACHINE_ARCH}"

PR = "r0"

do_patch[noexec] = "1"
do_configure[noexec] = "1"
do_install[noexec] = "1"

inherit deploy

DEPENDS += "cptra-imgtool-native caliptra-sw-native caliptra-mcu-sw-native"

CPTRA_IMGTOOL_CFG ?= "ast2700a1-default"
CPTRA_PREBUILD_IMAGE_DIR ?= "prebuilt/ast2700a1-default"
CPTRA_FLASH_IMAGE ?= "ast2700-manifest-flash.bin"
CPTRA_NON_FLASH_IMAGE ?= "ast2700-soc-manifest.bin"
ASPEED_IROT = "${@bb.utils.contains('MACHINE_FEATURES', 'ast-irot', 'yes', 'no', d)}"

# Using cptra-imgtool to create manifest image.
create_cptra_manifest_image() {
    export RUST_LOG="debug"
    echo "Running cptra-imgtool..."

    cd ${STAGING_DATADIR_NATIVE}/cptra-imgtool

    mkdir -p out
    mkdir -p ${CPTRA_PREBUILD_IMAGE_DIR}

    # Copy fmc-images prebuilt image into cptra-imgtool prebuilt folder
    echo "CPTRA_PREBUILD_IMAGE_DIR=${CPTRA_PREBUILD_IMAGE_DIR}"
    install -m 644 ${DEPLOY_DIR_IMAGE}/fmc-images/* ${CPTRA_PREBUILD_IMAGE_DIR}/.

    # Overwrite AFT image into cptra-imgtool prebuilt folder
    if [ -f "${UBOOT_FIT_ARM_TRUSTED_FIRMWARE_IMAGE}" ]; then
        echo "Overwrite ${UBOOT_FIT_ARM_TRUSTED_FIRMWARE_IMAGE} into ${CPTRA_PREBUILD_IMAGE_DIR}/atf.bin"
        install -m 0644 ${UBOOT_FIT_ARM_TRUSTED_FIRMWARE_IMAGE} ${CPTRA_PREBUILD_IMAGE_DIR}/atf.bin
    fi

    # Overwrite OPTEE image into cptra-imgtool prebuilt folder
    if [ -f "${UBOOT_FIT_TEE_IMAGE}" ]; then
        echo "Overwrite ${UBOOT_FIT_TEE_IMAGE} into ${CPTRA_PREBUILD_IMAGE_DIR}/optee.bin"
        install -m 0644 ${UBOOT_FIT_TEE_IMAGE} ${CPTRA_PREBUILD_IMAGE_DIR}/optee.bin
    fi

    # Overwrite U-Boot raw image into cptra-imgtool prebuilt folder
    echo "Overwrite ${DEPLOY_DIR_IMAGE}/u-boot.bin into ${CPTRA_PREBUILD_IMAGE_DIR}/u-boot.bin"
    install -m 0644 ${DEPLOY_DIR_IMAGE}/u-boot.bin ${CPTRA_PREBUILD_IMAGE_DIR}/u-boot.bin

    # Overwrite SSP image into cptra-imgtool prebuilt folder
    if [ -f "${SSP_IMAGE}" ]; then
        echo "Overwrite ${SSP_IMAGE} into ${CPTRA_PREBUILD_IMAGE_DIR}/ssp.bin"
        install -m 0644 ${SSP_IMAGE} ${CPTRA_PREBUILD_IMAGE_DIR}/ssp.bin
    fi

    # Overwrite TSP image into cptra-imgtool prebuilt folder
    if [ -f "${TSP_IMAGE}" ]; then
        echo "Overwrite ${TSP_IMAGE} into ${CPTRA_PREBUILD_IMAGE_DIR}/tsp.bin"
        install -m 0644 ${TSP_IMAGE} ${CPTRA_PREBUILD_IMAGE_DIR}/tsp.bin
    fi

    # Run cptra-imgtool to generate manifest flash image.
    ./cptra-imgtool create-auth-flash --cfg ${CPTRA_IMGTOOL_CFG} --flash ${CPTRA_FLASH_IMAGE}

    # Run cptra-imgtool to generate manifest image for recovery.
    ./cptra-imgtool create-auth-man --cfg ${CPTRA_IMGTOOL_CFG} --man ${CPTRA_NON_FLASH_IMAGE}

    cd -

    # Install manifest image
    install -m 644 ${STAGING_DATADIR_NATIVE}/cptra-imgtool/${CPTRA_FLASH_IMAGE} ${B}/.
    install -m 644 ${STAGING_DATADIR_NATIVE}/cptra-imgtool/${CPTRA_NON_FLASH_IMAGE} ${B}/.
}

do_compile() {
    create_cptra_manifest_image
}

do_compile[depends] += " \
    optee-os:do_deploy \
    trusted-firmware-a:do_deploy \
    virtual/bootloader:do_deploy \
    fmc-images:do_deploy \
    ${@bb.utils.contains('MACHINE_FEATURES', 'ast-ssp', 'virtual/ssp:do_deploy', '', d)} \
    ${@bb.utils.contains('MACHINE_FEATURES', 'ast-tsp', 'virtual/tsp:do_deploy', '', d)} \
    "

do_deploy_image() {
    install -d ${DEPLOYDIR}
    install -m 644 ${B}/* ${DEPLOYDIR}/.
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

    # MANIFEST
    append_image(os.path.join(d.getVar('B', True), d.getVar('CPTRA_FLASH_IMAGE', True)),
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

    cmd = "rm -f {}".format(os.path.join(d.getVar('B', True), d.getVar('CPTRA_FLASH_IMAGE', True)))
    print(cmd)
    subprocess.check_call(cmd, shell=True)

    cmd = "mv {} {}".format(irot_boot_img,
                            os.path.join(d.getVar('B', True), d.getVar('CPTRA_FLASH_IMAGE', True)))
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

