DESCRIPTION = "Generate ASPEED Caliptra-1.2 (iROT) image"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${ASPEEDSDKBASE}/LICENSE;md5=a3740bd0a194cd6dcafdc482a200a56f"
PACKAGE_ARCH = "${MACHINE_ARCH}"

PR = "r0"

do_patch[noexec] = "1"
do_configure[noexec] = "1"
do_install[noexec] = "1"

inherit deploy

DEPENDS += "cptra-imgtool-native caliptra-sw-native caliptra-mcu-sw-native"

CPTRA_IMGTOOL_PRJ ?= "ast2700a1-irot"
PREBUILD_IMAGE_DIR = "prebuilt/${CPTRA_IMGTOOL_PRJ}"
CPTRA_IMGTOOL_IMAGE = "man-irot.bin"

# Using cptra-imgtool to create manifest image.
create_cptra_manifest_image() {
    export RUST_LOG="debug"
    bbnote "Running cptra-imgtool"

    cd ${STAGING_DATADIR_NATIVE}/cptra-imgtool
    if [ ! -d out ]; then
        install -d out
    fi

    # Copy SSP image into cptra-imgtool prebuilt folder
    install -m 0644 ${SSP_IMAGE} ${PREBUILD_IMAGE_DIR}/.

    # Update cptra-imgtool manifest.toml
    sed -i 's/file = "ssp\.bin"/file = "'$(basename ${SSP_IMAGE})'"/g' config/${CPTRA_IMGTOOL_PRJ}-manifest.toml

    # Run cptra-imgtool to generate manifest image.
    ./cptra-imgtool create-auth-flash --prj ${CPTRA_IMGTOOL_PRJ} --flash ${CPTRA_IMGTOOL_IMAGE}

    # Copy manifest image
    install -d ${DEPLOYDIR}
    install -m 644 ${STAGING_DATADIR_NATIVE}/cptra-imgtool/${CPTRA_IMGTOOL_IMAGE} ${B}/.
}

do_compile() {
    create_cptra_manifest_image
}

do_mk_empty_image() {
    # Assemble the flash image
    dd if=/dev/zero bs=1k count=${IROT_IMAGE_SIZE} | \
        tr '\000' '\377' > ${B}/${IROT_IMAGE}
}

do_deploy_image() {
    install -d ${DEPLOYDIR}
    install -m 644 ${B}/* ${DEPLOYDIR}/.
}

def append_image(imgpath, start_kb, finish_kb, nor_image):
    import subprocess

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

python do_deploy() {
    import subprocess

    bb.build.exec_func("do_mk_empty_image", d)
    nor_image = os.path.join(d.getVar('B', True), d.getVar('IROT_IMAGE', True))

    # MANIFEST
    append_image(os.path.join(d.getVar('B', True),
                 '%s' % d.getVar('CPTRA_IMGTOOL_IMAGE',True)),
                  int(d.getVar('IROT_OFFSET_MANIFEST', True)),
                  int(d.getVar('IROT_OFFSET_ATF', True)),
                  nor_image)
    # ATF
    append_image(d.getVar('UBOOT_FIT_ARM_TRUSTED_FIRMWARE_IMAGE', True),
                 int(d.getVar('IROT_OFFSET_ATF', True)),
                 int(d.getVar('IROT_OFFSET_UBOOT', True)),
                 nor_image)
    # U-Boot raw image
    append_image(os.path.join(d.getVar('DEPLOY_DIR_IMAGE', True), 'u-boot.bin'),
                 int(d.getVar('IROT_OFFSET_UBOOT', True)),
                 int(d.getVar('IROT_OFFSET_TEE', True)),
                 nor_image)
    # TEE
    append_image(d.getVar('UBOOT_FIT_TEE_IMAGE', True),
                 int(d.getVar('IROT_OFFSET_TEE', True)),
                 int(d.getVar('IROT_IMAGE_SIZE', True)),
                 nor_image)

    bb.build.exec_func("do_deploy_image", d)
}

do_deploy[depends] += " \
    optee-os:do_deploy \
    trusted-firmware-a:do_deploy \
    virtual/bootmcu:do_deploy \
    virtual/bootloader:do_deploy \
    ${@bb.utils.contains('MACHINE_FEATURES', 'ast-ssp', 'virtual/ssp:do_deploy', '', d)} \
    ${@bb.utils.contains('MACHINE_FEATURES', 'ast-tsp', 'virtual/tsp:do_deploy', '', d)} \
    "

addtask deploy before do_build after do_compile
