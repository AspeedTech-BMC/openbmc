DESCRIPTION = "Generate aspeed customize secure boot images for AST2700. \
It is used for testing. Users should not use these generated images for production."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${ASPEEDSDKBASE}/LICENSE;md5=a3740bd0a194cd6dcafdc482a200a56f"
PACKAGE_ARCH = "${MACHINE_ARCH}"

PR = "r0"

DEPENDS = " \
    aspeed-image-tools-native \
    socsec-native \
    aspeed-secure-config-native \
    fmc-imgtool-native \
    fmc-images \
    u-boot-tools-native \
    dtc-native \
    xz-native \
    e2fsprogs-native \
    parted-native \
    virtual/kernel \
    virtual/bootloader \
    virtual/bootmcu \
    "

do_patch[noexec] = "1"
do_configure[noexec] = "1"
do_compile[noexec] = "1"
do_install[noexec] = "1"

inherit python3native deploy

ASPEED_CUSTOMIZE_GEN_SECURE_IMAGE_ENABLE ?= "0"
ASPEED_CUSTOMIZE_GEN_SECURE_IMAGE ?= "\
    ecdsa384-sha384 \
    ecdsa384-sha384-lms \
    "

DISTROOVERRIDES .= ":flash-${FLASH_SIZE}"
KERNEL_FITIMAGE_NAME = "fitImage-${INITRAMFS_IMAGE}-${MACHINE}-${MACHINE}"
KERNEL_FITIMAGE_ITS_NAME = "fitImage-its-${INITRAMFS_IMAGE}-${MACHINE}-${MACHINE}"
UBOOT_FITIMAGE_NAME = "u-boot.bin"
UBOOT_FITIMAGE_ITS_NAME = "u-boot.its"
SPL_IMAGE_NAME = "u-boot-spl.bin"
ASPEED_SECURE_BOOT = "${@bb.utils.contains('MACHINE_FEATURES', 'ast-secure', 'yes', 'no', d)}"
ASPEED_BOOT_EMMC_UFS = "${@bb.utils.contains_any('MACHINE_FEATURES', ['ast-mmc', 'ast-ufs'], 'yes', 'no', d)}"
ASPEED_BOOT_UFS = "${@bb.utils.contains_any('MACHINE_FEATURES', 'ast-ufs', 'yes', 'no', d)}"

IMAGE_BASE_NAME = "obmc-phosphor-image"
INITRAMFS_IMAGE_NAME = "${INITRAMFS_IMAGE}-${MACHINE}.${INITRAMFS_FSTYPES}"

# MMC or UFS
MMC_UBOOT_OFFSET = "0"
WIC_IMAGE_NAME = "${IMAGE_BASE_NAME}-${MACHINE}.wic.xz"
USER_DATA_IMAGE_NAME = "${IMAGE_BASE_NAME}-${MACHINE}.bin"
USER_DATA_BOOTPART_IMAGE_NAME = "boot-image.ext4"

install_unsigned_image() {
    install -d ${S}/${GEN_IMAGE_MODE}
    install -d ${S}/${GEN_IMAGE_MODE}/arch
    install -d ${S}/${GEN_IMAGE_MODE}/arch/arm64
    install -d ${S}/${GEN_IMAGE_MODE}/arch/arm64/boot
    install -d ${S}/${GEN_IMAGE_MODE}/arch/arm64/boot/dts
    install -d ${S}/${GEN_IMAGE_MODE}/arch/arm64/boot/dts/aspeed

    # caliptra
    install -m 0644 ${DEPLOY_DIR_IMAGE}/${CALIPTRA_FW_BINARY} ${S}/${GEN_IMAGE_MODE}

    # u-boot unsigned image, dtb and its
    install -m 0644 ${DEPLOY_DIR_IMAGE}/${UBOOT_FITIMAGE_ITS_NAME} ${S}/${GEN_IMAGE_MODE}
    install -m 0644 ${STAGING_DIR_HOST}/sysroot-only/u-boot* ${S}/${GEN_IMAGE_MODE}

    # kernel unsigned image, dtb and its
    install -m 0644 ${DEPLOY_DIR_IMAGE}/${KERNEL_FITIMAGE_ITS_NAME} ${S}/${GEN_IMAGE_MODE}
    install -m 0644 ${DEPLOY_DIR_IMAGE}/fitImage-linux.bin-${MACHINE} ${S}/${GEN_IMAGE_MODE}
    install -m 0644 ${DEPLOY_DIR_IMAGE}/fitImage-linux.bin-${MACHINE} ${S}/${GEN_IMAGE_MODE}/linux.bin
    for kernel_dtb in ${KERNEL_DEVICETREE}; do
        kernel_dtb_basename=$(basename ${kernel_dtb})
        install -m 0644 ${DEPLOY_DIR_IMAGE}/${kernel_dtb_basename} ${S}/${GEN_IMAGE_MODE}
        install -m 0644 ${DEPLOY_DIR_IMAGE}/${kernel_dtb_basename} ${S}/${GEN_IMAGE_MODE}/arch/arm64/boot/dts/aspeed
    done
}

make_otp_image() {
    otptool_config="$(dirname ${OTPTOOL_CONFIGS})/${OTPTOOL_JSON}"
    otptool_config_slug="$(basename ${otptool_config} .json)"
    otptool_config_outdir="${S}/${GEN_IMAGE_MODE}/${otptool_config_slug}"
    local otptool_user_folder=""

    if [ -n "${OTPTOOL_USER_DIR}" ]; then
        otptool_user_folder="--user_data_folder ${OTPTOOL_USER_DIR}"
    fi

    echo "otptool_config=${otptool_config}"
    echo "otptool_user_folder=${otptool_user_folder}"
    echo "otptool_key_dir=${OTPTOOL_KEY_DIR}"
    echo "otptool_extra_opts=${OTPTOOL_EXTRA_OPTS}"

    mkdir -p "${otptool_config_outdir}"
    otptool make_otp_image \
        --key_folder ${OTPTOOL_KEY_DIR} \
        --output_folder "${otptool_config_outdir}" \
        ${otptool_user_folder} \
        ${otptool_config} \
        ${OTPTOOL_EXTRA_OPTS}

    if [ $? -ne 0 ]; then
        bbfatal "Generated OTP image failed."
    fi

    otptool print --soc ${OTPTOOL_SOC} "${otptool_config_outdir}"/otp-all.image

    if [ $? -ne 0 ]; then
        bbfatal "Printed OTP image failed."
    fi
}

# export CRYPTOGRAPHY_OPENSSL_NO_LEGACY variable to fix the following errors.
# OpenSSL 3.0 legacy provider failed to load
# https://github.com/pyca/cryptography/issues/10598
fmc_sign_spl_and_verify() {
    export CRYPTOGRAPHY_OPENSSL_NO_LEGACY=1

    local ecc_key=""
    local ecc_key_index=""
    local lms_key=""
    local lms_key_index=""
    local sign_args=""

    if [ -f "${FMC_KEY_DIR}/${ROT_ECC_KEY_NAME}" ]; then
        ecc_key="--ecc-key ${FMC_KEY_DIR}/${ROT_ECC_KEY_NAME}"
    fi

    if [ -n "${ROT_ECC_KEY_INDEX}" ]; then
        ecc_key_index="--ecc-key-index ${ROT_ECC_KEY_INDEX}"
    fi

    if [ -f "${FMC_KEY_DIR}/${ROT_LMS_KEY_NAME}" ]; then
        lms_key="--lms-key ${FMC_KEY_DIR}/${ROT_LMS_KEY_NAME}"
    fi

    if [ -n "${ROT_LMS_KEY_INDEX}" ]; then
        lms_key_index="--lms-key-index ${ROT_LMS_KEY_INDEX}"
    fi

    if [ "${FMC_SIGN_ENABLE}" = "1" ]; then
        sign_args="${ecc_key} ${ecc_key_index} ${lms_key} ${lms_key_index}"
    fi

    echo "sign_args=${sign_args}"

    fmc-imgtool \
        --verbose \
        --version 2 \
        --input ${S}/${GEN_IMAGE_MODE}/u-boot-spl.bin \
        --output ${S}/${GEN_IMAGE_MODE}/${BOOTMCU_FW_BINARY} \
        --prebuilt-dir ${DEPLOY_DIR_IMAGE}/fmc-images/ \
        ${sign_args}

    # TODO: The FMC tool does not support verification yet.
    # To reduce the risk of unexpected run-time errors, verification should be added.
    echo "!!! WARNING: FMC verification is not supported yet."
}

make_uboot_kernel_fitimage_and_sign() {
    cd ${S}/${GEN_IMAGE_MODE}

    # Assemble the kernel image
    uboot-mkimage -f ${KERNEL_FITIMAGE_ITS_NAME} ${KERNEL_FITIMAGE_NAME}
    # Sign the Kernel FIT image and add public key to U-Boot dtb
    uboot-mkimage -F -k ${UBOOT_SIGN_KEYDIR} -K "u-boot.dtb" -r ${KERNEL_FITIMAGE_NAME}
    # Verify kernel fitImage
    uboot-fit_check_sign -f ${KERNEL_FITIMAGE_NAME} -k u-boot.dtb
    if [ $? -ne 0 ]; then
        bbfatal "Verified kernel fitImage failed."
    fi

    # Assemble the bootloader image
    uboot-mkimage -f ${UBOOT_FITIMAGE_ITS_NAME} ${UBOOT_FITIMAGE_NAME}
    # Sign the Bootloader FIT image and add public key to SPL dtb
    uboot-mkimage -F -k ${SPL_SIGN_KEYDIR} -K "u-boot-spl.dtb" -r ${UBOOT_FITIMAGE_NAME}
    # Verify bootloader fitImage
    uboot-fit_check_sign -f ${UBOOT_FITIMAGE_NAME} -k u-boot-spl.dtb
    if [ $? -ne 0 ]; then
        bbfatal "Verified bootloader fitImage failed."
    fi

    # concat spl dtb
    cat u-boot-spl-nodtb.bin u-boot-spl.dtb > ${SPL_IMAGE_NAME}

    rm -rf ${S}/${GEN_IMAGE_MODE}/arch
    rm -f ${S}/${GEN_IMAGE_MODE}/linux.bin

    cd ${S}
}

make_boot_partition_ext4() {
    # Generate a compressed ext4 filesystem with the fitImage file in it to be
    # flashed to the user data area at boot partition of the eMMC

    cd ${S}/${GEN_IMAGE_MODE}
    install -d boot-image
    install -m 0644 ${KERNEL_FITIMAGE_NAME} boot-image/fitImage

    mkfs.ext4 -F -i 4096 -d boot-image ${USER_DATA_BOOTPART_IMAGE_NAME}
    # Error codes 0-3 indicate successfull operation of fsck
    fsck.ext4 -pvfD ${USER_DATA_BOOTPART_IMAGE_NAME} || [ $? -le 3 ]
    cd ${S}
}

deploy_static_image_helper() {
    otptool_config_slug="$(basename ${OTPTOOL_JSON} .json)"

    install -d ${DEPLOYDIR}
    install -d ${DEPLOYDIR}/${GEN_IMAGE_MODE}

    install -m 0644 ${DEPLOY_DIR_IMAGE}/image-rofs ${DEPLOYDIR}/${GEN_IMAGE_MODE}
    install -m 0644 ${DEPLOY_DIR_IMAGE}/image-rwfs ${DEPLOYDIR}/${GEN_IMAGE_MODE}
    install -m 0644 ${DEPLOY_DIR_IMAGE}/${INITRAMFS_IMAGE_NAME} ${DEPLOYDIR}/${GEN_IMAGE_MODE}
    install -m 0644 ${S}/${GEN_IMAGE_MODE}/*.* ${DEPLOYDIR}/${GEN_IMAGE_MODE}
    install -m 0644 ${S}/${GEN_IMAGE_MODE}/fitImage* ${DEPLOYDIR}/${GEN_IMAGE_MODE}
    install -m 0644 ${S}/${GEN_IMAGE_MODE}/${otptool_config_slug}/otp-all.image ${DEPLOYDIR}/${GEN_IMAGE_MODE}/${otptool_config_slug}-otp-all.image
    install -m 0644 ${DEPLOYDIR}/${GEN_IMAGE_MODE}/${KERNEL_FITIMAGE_NAME} ${DEPLOYDIR}/${GEN_IMAGE_MODE}/image-kernel

    # u-boot-env
    if [ -f ${DEPLOY_DIR_IMAGE}/u-boot-env.bin ]; then
        install -m 0644 ${DEPLOY_DIR_IMAGE}/u-boot-env.bin ${DEPLOYDIR}/${GEN_IMAGE_MODE}
    fi

    # trusted-firmware-a
    install -m 0644 ${DEPLOY_DIR_IMAGE}/bl31.* ${DEPLOYDIR}/${GEN_IMAGE_MODE}

    # optee-os
    if [ -f ${DEPLOY_DIR_IMAGE}/optee/tee-raw.bin ]; then
        cp --no-preserve=ownership -rf ${DEPLOY_DIR_IMAGE}/optee ${DEPLOYDIR}/${GEN_IMAGE_MODE}
    fi

    # co-processors
    install -m 0644 ${DEPLOY_DIR_IMAGE}/zephyr-aspeed-*.* ${DEPLOYDIR}/${GEN_IMAGE_MODE}
}

deploy_mmc_image_helper() {
    otptool_config_slug="$(basename ${OTPTOOL_JSON} .json)"

    install -d ${DEPLOYDIR}
    install -d ${DEPLOYDIR}/${GEN_IMAGE_MODE}

    install -m 0644 ${DEPLOY_DIR_IMAGE}/${IMAGE_BASE_NAME}-${MACHINE}.ext4 ${DEPLOYDIR}/${GEN_IMAGE_MODE}
    install -m 0644 ${DEPLOY_DIR_IMAGE}/${IMAGE_BASE_NAME}-${MACHINE}.rwfs.ext4 ${DEPLOYDIR}/${GEN_IMAGE_MODE}
    install -m 0644 ${DEPLOY_DIR_IMAGE}/${INITRAMFS_IMAGE_NAME} ${DEPLOYDIR}/${GEN_IMAGE_MODE}
    install -m 0644 ${S}/${GEN_IMAGE_MODE}/*.* ${DEPLOYDIR}/${GEN_IMAGE_MODE}
    install -m 0644 ${S}/${GEN_IMAGE_MODE}/fitImage* ${DEPLOYDIR}/${GEN_IMAGE_MODE}
    install -m 0644 ${S}/${GEN_IMAGE_MODE}/${otptool_config_slug}/otp-all.image ${DEPLOYDIR}/${GEN_IMAGE_MODE}/${otptool_config_slug}-otp-all.image

    # u-boot-env
    if [ -f ${DEPLOY_DIR_IMAGE}/u-boot-env.bin ]; then
        install -m 0644 ${DEPLOY_DIR_IMAGE}/u-boot-env.bin ${DEPLOYDIR}/${GEN_IMAGE_MODE}
    fi

    # trusted-firmware-a
    install -m 0644 ${DEPLOY_DIR_IMAGE}/bl31.* ${DEPLOYDIR}/${GEN_IMAGE_MODE}

    # optee-os
    if [ -f ${DEPLOY_DIR_IMAGE}/optee/tee-raw.bin ]; then
        cp --no-preserve=ownership -rf ${DEPLOY_DIR_IMAGE}/optee ${DEPLOYDIR}/${GEN_IMAGE_MODE}
    fi

    # co-processors
    install -m 0644 ${DEPLOY_DIR_IMAGE}/zephyr-aspeed-*.* ${DEPLOYDIR}/${GEN_IMAGE_MODE}

    # decompress wic image for user data area boot partition update
    xz -cd ${DEPLOY_DIR_IMAGE}/${WIC_IMAGE_NAME} > ${S}/${GEN_IMAGE_MODE}/${USER_DATA_IMAGE_NAME}
}


def make_empty_image_zeros(img, size_kb):
    size = int(size_kb) * 1024
    with open(img, "wb+") as fp:
        fp.seek(0)
        fp.write(b'\x00'*size)


def make_empty_image(img, size_kb):
    size = int(size_kb) * 1024
    with open(img, "wb+") as fp:
        fp.seek(0)
        fp.write(b'\xFF'*size)


def update_its_file(file_path, oldstr, newstr):
    with open(file_path, 'r') as fp:
        file_contents = fp.read()
    new_contents = file_contents.replace(oldstr, newstr)
    with open(file_path, 'w') as fp:
        fp.write(new_contents)


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


def deploy_static_image(d):
    bb.build.exec_func("deploy_static_image_helper", d)
    gen_img = d.getVar('GEN_IMAGE_MODE', True)

    # image-bmc
    nor_img = os.path.join(d.getVar('DEPLOYDIR', True), gen_img, "image-bmc")
    make_empty_image(nor_img, d.getVar('FLASH_SIZE', True))

    uboot_offset = int(d.getVar('FLASH_UBOOT_OFFSET', True))
    caliptra_end_offset = uboot_offset + int(d.getVar('FLASH_CALIPTRA_SIZE', True))
    append_image(os.path.join(d.getVar('DEPLOYDIR', True), gen_img, d.getVar('CALIPTRA_FW_BINARY', True)),
                 nor_img,
                 uboot_offset,
                 caliptra_end_offset)

    uboot_offset = caliptra_end_offset
    bootmcu_end_offset = uboot_offset + int(d.getVar('FLASH_BMCU_SIZE', True))
    append_image(os.path.join(d.getVar('DEPLOYDIR', True), gen_img, d.getVar('BOOTMCU_FW_BINARY', True)),
                 nor_img,
                 uboot_offset,
                 bootmcu_end_offset)

    uboot_offset = bootmcu_end_offset
    append_image(os.path.join(d.getVar('DEPLOYDIR', True), gen_img, d.getVar('UBOOT_FITIMAGE_NAME', True)),
                 nor_img,
                 uboot_offset,
                 int(d.getVar('FLASH_UBOOT_ENV_OFFSET', True)))

    append_image(os.path.join(d.getVar('DEPLOYDIR', True), gen_img, "image-kernel"),
                 nor_img,
                 int(d.getVar('FLASH_KERNEL_OFFSET', True)),
                 int(d.getVar('FLASH_ROFS_OFFSET', True)))

    append_image(os.path.join(d.getVar('DEPLOYDIR', True), gen_img, "image-rofs"),
                 nor_img,
                 int(d.getVar('FLASH_ROFS_OFFSET', True)),
                 int(d.getVar('FLASH_RWFS_OFFSET', True)))

    append_image(os.path.join(d.getVar('DEPLOYDIR', True), gen_img, "image-rwfs"),
                 nor_img,
                 int(d.getVar('FLASH_RWFS_OFFSET', True)),
                 int(d.getVar('FLASH_SIZE', True)))

    # image-u-boot
    uboot_img = os.path.join(d.getVar('DEPLOYDIR', True), gen_img, "image-u-boot")
    make_empty_image(uboot_img, d.getVar('FLASH_UBOOT_ENV_OFFSET', True))

    uboot_offset = int(d.getVar('FLASH_UBOOT_OFFSET', True))
    caliptra_end_offset = uboot_offset + int(d.getVar('FLASH_CALIPTRA_SIZE', True))
    append_image(os.path.join(d.getVar('DEPLOYDIR', True), gen_img, d.getVar('CALIPTRA_FW_BINARY', True)),
                 uboot_img,
                 uboot_offset,
                 caliptra_end_offset)

    uboot_offset = caliptra_end_offset
    bootmcu_end_offset = uboot_offset + int(d.getVar('FLASH_BMCU_SIZE', True))
    append_image(os.path.join(d.getVar('DEPLOYDIR', True), gen_img, d.getVar('BOOTMCU_FW_BINARY', True)),
                 uboot_img,
                 uboot_offset,
                 bootmcu_end_offset)

    uboot_offset = bootmcu_end_offset
    append_image(os.path.join(d.getVar('DEPLOYDIR', True), gen_img, d.getVar('UBOOT_FITIMAGE_NAME', True)),
                 uboot_img,
                 uboot_offset,
                 int(d.getVar('FLASH_UBOOT_ENV_OFFSET', True)))


def deploy_mmc_image(d):
    import subprocess

    gen_img = d.getVar('GEN_IMAGE_MODE', True)
    user_data_image = os.path.join(d.getVar('S', True), gen_img, d.getVar('USER_DATA_IMAGE_NAME', True))
    user_data_bootpart_image = os.path.join(d.getVar('S', True), gen_img, d.getVar('USER_DATA_BOOTPART_IMAGE_NAME', True))
    make_empty_image_zeros(user_data_bootpart_image, d.getVar('MMC_BOOT_PARTITION_SIZE', True))
    bb.build.exec_func("make_boot_partition_ext4", d)
    bb.build.exec_func("deploy_mmc_image_helper", d)

    # get partition offset from user data area image
    # eMMC sector size is 512 bytes
    # UFS sector size is 4096 bytes
    aspeed_boot_ufs = d.getVar('ASPEED_BOOT_UFS', True)
    if aspeed_boot_ufs == "yes":
        sector_size = 4096
    else:
        sector_size = 512

    print("sector_size=%d" % (sector_size))

    # boot-a
    cmd = "PARTED_SECTOR_SIZE=%d parted -s %s unit B print | grep 'boot-a'" % (sector_size, user_data_image)
    print("Get boot-a partition information...")
    print(cmd)
    boot_a_out = subprocess.check_output(cmd, shell=True, text=True)
    print(boot_a_out)
    boot_a_offset_kb = int(boot_a_out.split()[1].rstrip("B")) // 1024
    print("boot_a_offset_kb=%d" % (boot_a_offset_kb))

    # boot-b
    cmd = "PARTED_SECTOR_SIZE=%d parted -s %s unit B print | grep 'boot-b'" % (sector_size, user_data_image)
    print("Get boot-b partition information...")
    print(cmd)
    boot_b_out = subprocess.check_output(cmd, shell=True, text=True)
    print(boot_b_out)
    boot_b_offset_kb = int(boot_b_out.split()[1].rstrip("B")) // 1024
    print("boot_b_offset_kb=%d" % (boot_b_offset_kb))

    # rofs-a
    cmd = "PARTED_SECTOR_SIZE=%d parted -s %s unit B print | grep 'rofs-a'" % (sector_size, user_data_image)
    print("Get rofs-a partition information...")
    print(cmd)
    rofs_a_out = subprocess.check_output(cmd, shell=True, text=True)
    print(rofs_a_out)
    rofs_a_offset_kb = int(rofs_a_out.split()[1].rstrip("B")) // 1024
    print("rofs_a_offset_kb=%d" % (rofs_a_offset_kb))

    # update boot partition in user data area image
    append_image(user_data_bootpart_image, user_data_image, boot_a_offset_kb, boot_b_offset_kb)
    append_image(user_data_bootpart_image, user_data_image, boot_b_offset_kb, rofs_a_offset_kb)

    # compress user data image and deploy
    deploy_wic_image = os.path.join(d.getVar('DEPLOYDIR', True), gen_img, d.getVar('WIC_IMAGE_NAME', True))
    cmd = "xz -f -k -c -9 {} --check=crc32 {} > {}".format(d.getVar('XZ_DEFAULTS', True),
                                                           user_data_image,
                                                           deploy_wic_image)
    print(cmd)
    subprocess.check_call(cmd, shell=True)

    # image-u-boot for Boot Area Partition 1 and 2
    mmc_boot_img = os.path.join(d.getVar('DEPLOYDIR', True), gen_img, "image-u-boot")
    make_empty_image(mmc_boot_img, d.getVar('MMC_UBOOT_SIZE', True))

    uboot_offset = int(d.getVar('MMC_UBOOT_OFFSET', True))
    caliptra_end_offset = uboot_offset + int(d.getVar('FLASH_CALIPTRA_SIZE', True))
    append_image(os.path.join(d.getVar('DEPLOYDIR', True), gen_img, d.getVar('CALIPTRA_FW_BINARY', True)),
                 mmc_boot_img,
                 uboot_offset,
                 caliptra_end_offset)

    uboot_offset = caliptra_end_offset
    bootmcu_end_offset = uboot_offset + int(d.getVar('FLASH_BMCU_SIZE', True))
    append_image(os.path.join(d.getVar('DEPLOYDIR', True), gen_img, d.getVar('BOOTMCU_FW_BINARY', True)),
                 mmc_boot_img,
                 uboot_offset,
                 bootmcu_end_offset)

    uboot_offset = bootmcu_end_offset
    append_image(os.path.join(d.getVar('DEPLOYDIR', True), gen_img, d.getVar('UBOOT_FITIMAGE_NAME', True)),
                 mmc_boot_img,
                 uboot_offset,
                 int(d.getVar('MMC_UBOOT_SIZE', True)))


def verify_uboot_kernel_image_status(d):
    aspeed_secure_boot = d.getVar('ASPEED_SECURE_BOOT', True)
    if aspeed_secure_boot != "yes":
        bb.fatal("Only support secure boot enable")

    bootmcu_fw_binary = d.getVar('BOOTMCU_FW_BINARY', True)
    if not bootmcu_fw_binary:
        bb.fatal("Only support BootMCU SPL")

    kernel_imagetype = d.getVar('KERNEL_IMAGETYPE', True)
    if "fitImage" not in kernel_imagetype:
        bb.fatal("Only support Kernel FIT image")

    uboot_fitimage_enable = d.getVar('UBOOT_FITIMAGE_ENABLE', True)
    if uboot_fitimage_enable != "1":
        bb.fatal("Only support Bootloader FIT image")

    spl_sign_enable = d.getVar('SPL_SIGN_ENABLE', True)
    if spl_sign_enable != "1":
        bb.fatal("Only support SPL sign enable")

    uboot_sign_enable = d.getVar('UBOOT_SIGN_ENABLE', True)
    if uboot_sign_enable != "1":
        bb.fatal("Only support U-Boot sign enable")

    fmc_sign_enable = d.getVar('FMC_SIGN_ENABLE', True)
    if fmc_sign_enable != "1":
        bb.fatal("Only support FMC sign enable")


python do_deploy() {
    secure_image_list = [
        {
            "mode": "ecdsa384-sha384",
            "otptool_json": "2700A1_ECDSA384.json",
            "rot_ecc_key_name" : "test_oem_dss_private_key_ecdsa384_1.pem",
            "rot_ecc_key_index" : "1",
            "rot_lms_key_name" : "",
            "rot_lms_key_index" : "",
            "cot_uboot_algo": "ecdsa384",
            "cot_uboot_hash": "sha384",
            "cot_kernel_algo": "ecdsa384",
            "cot_kernel_hash": "sha384",
            "cot_spl_sign_key_name": "test_bl2_ecdsa_secp384r1",
            "cot_uboot_sign_key_name": "test_bl3_ecdsa_secp384r1"
        },
        {
            "mode": "ecdsa384-sha384-lms",
            "otptool_json": "2700A1_ECDSA384_LMS.json",
            "rot_ecc_key_name" : "test_oem_dss_private_key_ecdsa384_1.pem",
            "rot_ecc_key_index" : "1",
            "rot_lms_key_name" : "test_oem_dss_lms_key_1.prv",
            "rot_lms_key_index" : "1",
            "cot_uboot_algo": "ecdsa384",
            "cot_uboot_hash": "sha384",
            "cot_kernel_algo": "ecdsa384",
            "cot_kernel_hash": "sha384",
            "cot_spl_sign_key_name": "test_bl2_ecdsa_secp384r1",
            "cot_uboot_sign_key_name": "test_bl3_ecdsa_secp384r1"
        }
    ]

    gen_secure_image_enable = d.getVar('ASPEED_CUSTOMIZE_GEN_SECURE_IMAGE_ENABLE', True)
    if gen_secure_image_enable != "1":
        print("Disable gen secure image. Do nothing.")
        return

    verify_uboot_kernel_image_status(d)

    uboot_default_algo = d.getVar('UBOOT_FIT_SIGN_ALG', True)
    uboot_default_hash = d.getVar('UBOOT_FIT_HASH_ALG', True)
    kernel_default_algo = d.getVar('FIT_SIGN_ALG', True)
    kernel_default_hash = d.getVar('FIT_HASH_ALG', True)
    spl_default_sign_key_name = d.getVar('SPL_SIGN_KEYNAME', True)
    uboot_default_sign_key_name = d.getVar('UBOOT_SIGN_KEYNAME', True)
    gen_secure_image = d.getVar('ASPEED_CUSTOMIZE_GEN_SECURE_IMAGE', True)
    aspeed_boot_emmc_ufs = d.getVar('ASPEED_BOOT_EMMC_UFS', True)

    for gen_img in gen_secure_image.split():
        for sec_img in secure_image_list:
            if gen_img == sec_img["mode"]:
                break
        else:
          bb.fatal("%s mode not support" % gen_img)

        print("Start %s image..." % gen_img)
        d.setVar('GEN_IMAGE_MODE', gen_img)
        d.setVar('OTPTOOL_JSON', sec_img["otptool_json"])
        d.setVar('ROT_ECC_KEY_NAME', sec_img["rot_ecc_key_name"])
        d.setVar('ROT_ECC_KEY_INDEX', sec_img["rot_ecc_key_index"])
        d.setVar('ROT_LMS_KEY_NAME', sec_img["rot_lms_key_name"])
        d.setVar('ROT_LMS_KEY_INDEX', sec_img["rot_lms_key_index"])

        bb.build.exec_func("install_unsigned_image", d)
        kernel_its = os.path.join(d.getVar('S', True), gen_img, d.getVar('KERNEL_FITIMAGE_ITS_NAME', True))
        print("Update kernel its file", kernel_its)
        update_its_file(kernel_its, kernel_default_hash, sec_img["cot_kernel_hash"])
        update_its_file(kernel_its, kernel_default_algo, sec_img["cot_kernel_algo"])
        update_its_file(kernel_its, uboot_default_sign_key_name, sec_img["cot_uboot_sign_key_name"])
        uboot_its = os.path.join(d.getVar('S', True), gen_img, d.getVar('UBOOT_FITIMAGE_ITS_NAME', True))
        print("Update uboot its file", uboot_its)
        update_its_file(uboot_its, uboot_default_hash, sec_img["cot_uboot_hash"])
        update_its_file(uboot_its, uboot_default_algo, sec_img["cot_uboot_algo"])
        update_its_file(uboot_its, spl_default_sign_key_name, sec_img["cot_spl_sign_key_name"])

        print("Make bootloader, kernel fitimage and sign")
        bb.build.exec_func("make_uboot_kernel_fitimage_and_sign", d)
        print("Make otp image")
        bb.build.exec_func("make_otp_image", d)
        print("FMC sign spl and verify")
        bb.build.exec_func("fmc_sign_spl_and_verify", d)

        if aspeed_boot_emmc_ufs == "yes":
            print("Deploy mmc image...")
            deploy_mmc_image(d)
        else:
            print("Deploy static image...")
            deploy_static_image(d)

        print("Started %s image" % gen_img)
}

do_deploy[depends] += " \
    obmc-phosphor-image:do_image_complete \
    "

addtask deploy before do_build after do_compile

python do_cleanall:prepend() {
    import subprocess
    gen_secure_image = [
        "ecdsa384-sha384",
        "ecdsa384-sha384-lms"
    ]

    for gen_img in gen_secure_image:
        path = os.path.join(d.getVar('DEPLOY_DIR_IMAGE', True), gen_img)
        if os.path.exists(path):
            cmd = "rm -rf %s" % (path)
            print(cmd)
            subprocess.check_call(cmd, shell=True)
}

