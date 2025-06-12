DESCRIPTION = "Generate aspeed customize secure boot images for AST2600. \
It is used for testing. Users should not use these generated images for production."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${ASPEEDSDKBASE}/LICENSE;md5=a3740bd0a194cd6dcafdc482a200a56f"
PACKAGE_ARCH = "${MACHINE_ARCH}"

PR = "r0"

DEPENDS = " \
    aspeed-image-tools-native \
    socsec-native \
    aspeed-secure-config-native \
    u-boot-tools-native \
    dtc-native \
    xz-native \
    e2fsprogs-native \
    parted-native \
    virtual/kernel \
    virtual/bootloader \
    "

do_patch[noexec] = "1"
do_configure[noexec] = "1"
do_compile[noexec] = "1"
do_install[noexec] = "1"

inherit python3native deploy

ASPEED_CUSTOMIZE_GEN_SECURE_IMAGE_ENABLE ?= "0"
ASPEED_CUSTOMIZE_GEN_SECURE_IMAGE ?= "\
    rsa2048-sha256 \
    rsa2048-sha256-o1 \
    rsa2048-sha256-o2-pub \
    rsa3072-sha384 \
    rsa3072-sha384-o1 \
    rsa3072-sha384-o2-pub \
    rsa4096-sha512 \
    rsa4096-sha512-o1 \
    rsa4096-sha512-o2-pub \
    "

DISTROOVERRIDES .= ":flash-${FLASH_SIZE}"
KERNEL_FITIMAGE_NAME = "fitImage-${INITRAMFS_IMAGE}-${MACHINE}-${MACHINE}"
KERNEL_FITIMAGE_ITS_NAME = "fitImage-its-${INITRAMFS_IMAGE}-${MACHINE}-${MACHINE}"
UBOOT_FITIMAGE_NAME = "u-boot.bin"
UBOOT_FITIMAGE_ITS_NAME = "u-boot.its"
SPL_IMAGE_NAME = "u-boot-spl.bin"
ASPEED_SECURE_BOOT = "${@bb.utils.contains('MACHINE_FEATURES', 'ast-secure', 'yes', 'no', d)}"
ASPEED_BOOT_EMMC = "${@bb.utils.contains('MACHINE_FEATURES', 'ast-mmc', 'yes', 'no', d)}"
IMAGE_BASE_NAME = "obmc-phosphor-image"
INITRAMFS_IMAGE_NAME = "${INITRAMFS_IMAGE}-${MACHINE}.${INITRAMFS_FSTYPES}"

# EMMC
MMC_UBOOT_SPL_SIZE = "64"
MMC_UBOOT_OFFSET = "0"
WIC_IMAGE_NAME = "${IMAGE_BASE_NAME}-${MACHINE}.wic.xz"
USER_DATA_IMAGE_NAME = "${IMAGE_BASE_NAME}-${MACHINE}.bin"
USER_DATA_BOOTPART_IMAGE_NAME = "boot-image.ext4"

install_unsigned_image() {
    install -d ${S}/${GEN_IMAGE_MODE}
    install -d ${S}/${GEN_IMAGE_MODE}/arch
    install -d ${S}/${GEN_IMAGE_MODE}/arch/arm
    install -d ${S}/${GEN_IMAGE_MODE}/arch/arm/boot
    install -d ${S}/${GEN_IMAGE_MODE}/arch/arm/boot/dts
    install -d ${S}/${GEN_IMAGE_MODE}/arch/arm/boot/dts/aspeed

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
        install -m 0644 ${DEPLOY_DIR_IMAGE}/${kernel_dtb_basename} ${S}/${GEN_IMAGE_MODE}/arch/arm/boot/dts
        install -m 0644 ${DEPLOY_DIR_IMAGE}/${kernel_dtb_basename} ${S}/${GEN_IMAGE_MODE}/arch/arm/boot/dts/aspeed
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
socsec_sign_spl_and_verify() {
    export CRYPTOGRAPHY_OPENSSL_NO_LEGACY=1
    socsec_sign_key_dir="$(dirname ${SOCSEC_SIGN_KEY})"
    socsec_sign_key="${socsec_sign_key_dir}/${ROT_SIGN_KEY_NAME}"
    signing_extra_default_opts="--stack_intersects_verification_region=false --rsa_key_order=big"
    signing_extra_rsa_aes_key_opts=""
    signing_extra_aes_key_opts=""
    signing_helper_args=""
    signing_extra_opts=""

    if [ -n "${SOCSEC_SIGN_HELPER}" ]; then
        signing_helper_args="--signing_helper ${SOCSEC_SIGN_HELPER}"
    fi

    if [ -n "${ROT_AES_KEY_NAME}" -a -n "${ROT_RSA_AES_KEY_NAME}" ]; then
        signing_extra_aes_key_opts="--aes_key ${socsec_sign_key_dir}/${ROT_AES_KEY_NAME}"
        signing_extra_rsa_aes_key_opts="--rsa_aes ${socsec_sign_key_dir}/${ROT_RSA_AES_KEY_NAME}"
    elif [ -n "${ROT_AES_KEY_NAME}" ]; then
        signing_extra_aes_key_opts="--key_in_otp --aes_key ${socsec_sign_key_dir}/${ROT_AES_KEY_NAME}"
    fi

    signing_extra_opts="${signing_extra_default_opts} ${signing_extra_aes_key_opts} ${signing_extra_rsa_aes_key_opts}"

    echo "rot_sign_algo=${ROT_SIGN_ALGO}"
    echo "socsec_sign_key=${socsec_sign_key}"
    echo "signing_helper_args=${signing_helper_args}"
    echo "signing_extra_opts=${signing_extra_opts}"

    socsec make_secure_bl1_image \
        --soc ${SOCSEC_SIGN_SOC}  \
        --algorithm ${ROT_SIGN_ALGO} \
        --rsa_sign_key ${socsec_sign_key} \
        --bl1_image ${S}/${GEN_IMAGE_MODE}/${SPL_IMAGE_NAME} \
        $signing_helper_args \
        $signing_extra_opts \
        --output ${S}/${GEN_IMAGE_MODE}/${SPL_IMAGE_NAME}.staged

    # install unsigned image
    install -m 0644 ${S}/${GEN_IMAGE_MODE}/${SPL_IMAGE_NAME} ${S}/${GEN_IMAGE_MODE}/${SPL_IMAGE_NAME}.unsigned
    mv ${S}/${GEN_IMAGE_MODE}/${SPL_IMAGE_NAME}.staged ${S}/${GEN_IMAGE_MODE}/${SPL_IMAGE_NAME}

    # verify spl and otp
    echo "verify otp and spl"
    socsec verify \
        --sec_image ${S}/${GEN_IMAGE_MODE}/${SPL_IMAGE_NAME} \
        --otp_image ${S}/${GEN_IMAGE_MODE}/"$(basename ${OTPTOOL_JSON} .json)"/otp-all.image

    if [ $? -ne 0 ]; then
        bbfatal "Verified OTP image failed."
    fi
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

make_recovery_image() {
    python3 ${STAGING_BINDIR_NATIVE}/gen_uart_booting_image.py ${S}/${GEN_IMAGE_MODE}/${SPL_IMAGE_NAME} ${S}/${GEN_IMAGE_MODE}/recovery_${SPL_IMAGE_NAME}
}

make_emmc_unsigned_rot_image() {
    if [ -f ${S}/${GEN_IMAGE_MODE}/${SPL_IMAGE_NAME}.unsigned ]; then
        python3 ${STAGING_BINDIR_NATIVE}/gen_emmc_boot_image.py ${S}/${GEN_IMAGE_MODE}/${SPL_IMAGE_NAME}.unsigned ${S}/${GEN_IMAGE_MODE}/emmc_${SPL_IMAGE_NAME}.unsigned
    fi
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

    # optee-os
    if [ -f ${DEPLOY_DIR_IMAGE}/optee/tee-raw.bin ]; then
        cp --no-preserve=ownership -rf ${DEPLOY_DIR_IMAGE}/optee ${DEPLOYDIR}/${GEN_IMAGE_MODE}
    fi
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

    # optee-os
    if [ -f ${DEPLOY_DIR_IMAGE}/optee/tee-raw.bin ]; then
        cp --no-preserve=ownership -rf ${DEPLOY_DIR_IMAGE}/optee ${DEPLOYDIR}/${GEN_IMAGE_MODE}
    fi

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
    uboot_spl_end_offset = uboot_offset + int(d.getVar('FLASH_UBOOT_SPL_SIZE', True))
    append_image(os.path.join(d.getVar('DEPLOYDIR', True), gen_img, d.getVar('SPL_IMAGE_NAME', True)),
                 nor_img,
                 uboot_offset,
                 uboot_spl_end_offset)

    uboot_offset = uboot_spl_end_offset
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
    uboot_spl_end_offset = uboot_offset + int(d.getVar('FLASH_UBOOT_SPL_SIZE', True))
    append_image(os.path.join(d.getVar('DEPLOYDIR', True), gen_img, d.getVar('SPL_IMAGE_NAME', True)),
                 uboot_img,
                 uboot_offset,
                 uboot_spl_end_offset)

    uboot_offset = uboot_spl_end_offset
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

    # emmc_image-boot for Boot Area Partition 1 and 2
    emmc_boot_img = os.path.join(d.getVar('DEPLOYDIR', True), gen_img, "emmc_image-u-boot")
    make_empty_image(emmc_boot_img, d.getVar('MMC_UBOOT_SIZE', True))

    uboot_offset = int(d.getVar('MMC_UBOOT_OFFSET', True))
    uboot_spl_end_offset = uboot_offset + int(d.getVar('MMC_UBOOT_SPL_SIZE', True))
    append_image(os.path.join(d.getVar('DEPLOYDIR', True), gen_img, d.getVar('SPL_IMAGE_NAME', True)),
                 emmc_boot_img,
                 uboot_offset,
                 uboot_spl_end_offset)

    uboot_offset = uboot_spl_end_offset
    append_image(os.path.join(d.getVar('DEPLOYDIR', True), gen_img, d.getVar('UBOOT_FITIMAGE_NAME', True)),
                 emmc_boot_img,
                 uboot_offset,
                 int(d.getVar('MMC_UBOOT_SIZE', True)))


def verify_uboot_kernel_image_status(d):
    aspeed_secure_boot = d.getVar('ASPEED_SECURE_BOOT', True)
    if aspeed_secure_boot != "yes":
        bb.fatal("Only support secure boot enable")

    spl_binary = d.getVar('SPL_BINARY', True)
    if not spl_binary:
        bb.fatal("Only support SPL")

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

    socsec_sign_enable = d.getVar('SOCSEC_SIGN_ENABLE', True)
    if socsec_sign_enable != "1":
        bb.fatal("Only support SOCSEC sign enable")


python do_deploy() {
    secure_image_list = [
        {
            "mode": "rsa2048-sha256",
            "otptool_json": "evbA3_RSA2048_SHA256.json",
            "rot_sign_algo" : "RSA2048_SHA256",
            "rot_sign_key_name" : "test_oem_dss_private_key_2048_1.pem",
            "rot_aes_key_name" : "",
            "rot_rsa_aes_key_name" : "",
            "cot_uboot_algo": "rsa2048",
            "cot_uboot_hash": "sha256",
            "cot_kernel_algo": "rsa2048",
            "cot_kernel_hash": "sha256",
            "cot_spl_sign_key_name": "test_bl2_2048",
            "cot_uboot_sign_key_name": "test_bl3_2048"
        },
        {
            "mode": "rsa2048-sha256-o1",
            "otptool_json": "evbA3_RSA2048_SHA256_o1.json",
            "rot_sign_algo" : "AES_RSA2048_SHA256",
            "rot_sign_key_name" : "test_oem_dss_private_key_2048_1.pem",
            "rot_aes_key_name" : "test_aes_key.bin",
            "rot_rsa_aes_key_name" : "",
            "cot_uboot_algo": "rsa2048",
            "cot_uboot_hash": "sha256",
            "cot_kernel_algo": "rsa2048",
            "cot_kernel_hash": "sha256",
            "cot_spl_sign_key_name": "test_bl2_2048",
            "cot_uboot_sign_key_name": "test_bl3_2048"
        },
        {
            "mode": "rsa2048-sha256-o2-pub",
            "otptool_json": "evbA3_RSA2048_SHA256_o2_pub.json",
            "rot_sign_algo" : "AES_RSA2048_SHA256",
            "rot_sign_key_name" : "test_oem_dss_private_key_2048_1.pem",
            "rot_aes_key_name" : "test_aes_key.bin",
            "rot_rsa_aes_key_name" : "test_soc_private_key_2048.pem",
            "cot_uboot_algo": "rsa2048",
            "cot_uboot_hash": "sha256",
            "cot_kernel_algo": "rsa2048",
            "cot_kernel_hash": "sha256",
            "cot_spl_sign_key_name": "test_bl2_2048",
            "cot_uboot_sign_key_name": "test_bl3_2048"
        },
        {
            "mode": "rsa3072-sha384",
            "otptool_json": "evbA3_RSA3072_SHA384.json",
            "rot_sign_algo" : "RSA3072_SHA384",
            "rot_sign_key_name" : "test_oem_dss_private_key_3072_1.pem",
            "rot_aes_key_name" : "",
            "rot_rsa_aes_key_name" : "",
            "cot_uboot_algo": "rsa3072",
            "cot_uboot_hash": "sha384",
            "cot_kernel_algo": "rsa3072",
            "cot_kernel_hash": "sha384",
            "cot_spl_sign_key_name": "test_bl2_3072",
            "cot_uboot_sign_key_name": "test_bl3_3072"
        },
        {
            "mode": "rsa3072-sha384-o1",
            "otptool_json": "evbA3_RSA3072_SHA384_o1.json",
            "rot_sign_algo" : "AES_RSA3072_SHA384",
            "rot_sign_key_name" : "test_oem_dss_private_key_3072_1.pem",
            "rot_aes_key_name" : "test_aes_key.bin",
            "rot_rsa_aes_key_name" : "",
            "cot_uboot_algo": "rsa3072",
            "cot_uboot_hash": "sha384",
            "cot_kernel_algo": "rsa3072",
            "cot_kernel_hash": "sha384",
            "cot_spl_sign_key_name": "test_bl2_3072",
            "cot_uboot_sign_key_name": "test_bl3_3072"
        },
        {
            "mode": "rsa3072-sha384-o2-pub",
            "otptool_json": "evbA3_RSA3072_SHA384_o2_pub.json",
            "rot_sign_algo" : "AES_RSA3072_SHA384",
            "rot_sign_key_name" : "test_oem_dss_private_key_3072_1.pem",
            "rot_aes_key_name" : "test_aes_key.bin",
            "rot_rsa_aes_key_name" : "test_soc_private_key_3072.pem",
            "cot_uboot_algo": "rsa3072",
            "cot_uboot_hash": "sha384",
            "cot_kernel_algo": "rsa3072",
            "cot_kernel_hash": "sha384",
            "cot_spl_sign_key_name": "test_bl2_3072",
            "cot_uboot_sign_key_name": "test_bl3_3072"
        },
        {
            "mode": "rsa4096-sha512",
            "otptool_json": "evbA3_RSA4096_SHA512.json",
            "rot_sign_algo" : "RSA4096_SHA512",
            "rot_sign_key_name" : "test_oem_dss_private_key_4096_1.pem",
            "rot_aes_key_name" : "",
            "rot_rsa_aes_key_name" : "",
            "cot_uboot_algo": "rsa4096",
            "cot_uboot_hash": "sha512",
            "cot_kernel_algo": "rsa4096",
            "cot_kernel_hash": "sha512",
            "cot_spl_sign_key_name": "test_bl2_4096",
            "cot_uboot_sign_key_name": "test_bl3_4096"
        },
        {
            "mode": "rsa4096-sha512-o1",
            "otptool_json": "evbA3_RSA4096_SHA512_o1.json",
            "rot_sign_algo" : "AES_RSA4096_SHA512",
            "rot_sign_key_name" : "test_oem_dss_private_key_4096_1.pem",
            "rot_aes_key_name" : "test_aes_key.bin",
            "rot_rsa_aes_key_name" : "",
            "cot_uboot_algo": "rsa4096",
            "cot_uboot_hash": "sha512",
            "cot_kernel_algo": "rsa4096",
            "cot_kernel_hash": "sha512",
            "cot_spl_sign_key_name": "test_bl2_4096",
            "cot_uboot_sign_key_name": "test_bl3_4096"
        },
        {
            "mode": "rsa4096-sha512-o2-pub",
            "otptool_json": "evbA3_RSA4096_SHA512_o2_pub.json",
            "rot_sign_algo" : "AES_RSA4096_SHA512",
            "rot_sign_key_name" : "test_oem_dss_private_key_4096_1.pem",
            "rot_aes_key_name" : "test_aes_key.bin",
            "rot_rsa_aes_key_name" : "test_soc_private_key_4096.pem",
            "cot_uboot_algo": "rsa4096",
            "cot_uboot_hash": "sha512",
            "cot_kernel_algo": "rsa4096",
            "cot_kernel_hash": "sha512",
            "cot_spl_sign_key_name": "test_bl2_4096",
            "cot_uboot_sign_key_name": "test_bl3_4096"
        }
    ]


    verify_uboot_kernel_image_status(d)
    gen_secure_image_enable = d.getVar('ASPEED_CUSTOMIZE_GEN_SECURE_IMAGE_ENABLE', True)
    if gen_secure_image_enable != "1":
        print("Disable gen secure image. Do nothing.")
        return

    uboot_default_algo = d.getVar('UBOOT_FIT_SIGN_ALG', True)
    uboot_default_hash = d.getVar('UBOOT_FIT_HASH_ALG', True)
    kernel_default_algo = d.getVar('FIT_SIGN_ALG', True)
    kernel_default_hash = d.getVar('FIT_HASH_ALG', True)
    spl_default_sign_key_name = d.getVar('SPL_SIGN_KEYNAME', True)
    uboot_default_sign_key_name = d.getVar('UBOOT_SIGN_KEYNAME', True)
    gen_secure_image = d.getVar('ASPEED_CUSTOMIZE_GEN_SECURE_IMAGE', True)
    aspeed_boot_emmc = d.getVar('ASPEED_BOOT_EMMC', True)

    for gen_img in gen_secure_image.split():
        for sec_img in secure_image_list:
            if gen_img == sec_img["mode"]:
                break
        else:
          bb.fatal("%s mode not support" % gen_img)

        print("Start %s image..." % gen_img)
        d.setVar('GEN_IMAGE_MODE', gen_img)
        d.setVar('OTPTOOL_JSON', sec_img["otptool_json"])
        d.setVar('ROT_SIGN_ALGO', sec_img["rot_sign_algo"])
        d.setVar('ROT_SIGN_KEY_NAME', sec_img["rot_sign_key_name"])
        d.setVar('ROT_AES_KEY_NAME', sec_img["rot_aes_key_name"])
        d.setVar('ROT_RSA_AES_KEY_NAME', sec_img["rot_rsa_aes_key_name"])

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
        print("SOCSEC sign spl and verify")
        bb.build.exec_func("socsec_sign_spl_and_verify", d)
        print("Make recovery image")
        bb.build.exec_func("make_recovery_image", d)

        if aspeed_boot_emmc == "yes":
            print("Make emmc unsigned rot image")
            bb.build.exec_func("make_emmc_unsigned_rot_image", d)
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
        "rsa2048-sha256",
        "rsa2048-sha256-o1",
        "rsa2048-sha256-o2-pub",
        "rsa3072-sha384",
        "rsa3072-sha384-o1",
        "rsa3072-sha384-o2-pub",
        "rsa4096-sha512",
        "rsa4096-sha512-o1",
        "rsa4096-sha512-o2-pub"
    ]

    for gen_img in gen_secure_image:
        path = os.path.join(d.getVar('DEPLOY_DIR_IMAGE', True), gen_img)
        if os.path.exists(path):
            cmd = "rm -rf %s" % (path)
            print(cmd)
            subprocess.check_call(cmd, shell=True)
}

