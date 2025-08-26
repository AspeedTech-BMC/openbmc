# Provide support for generating the ASPEED BootMCU-RT + SSP-iRoT FIT image.
# A new helper function (ssp_irot_fitimage) is introduced for this purpose,
# but it deliberately reuses the same variable conventions defined by
# uboot-sign.bbclass. This ensures compatibility with existing scripts and
# avoids the need to maintain a separate set of variables for iRoT builds.
require recipes-bsp/u-boot/aspeed-coprocessor.inc

DESCRIPTION = "Generate ASPEED IROT image"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${ASPEEDSDKBASE}/LICENSE;md5=a3740bd0a194cd6dcafdc482a200a56f"
PACKAGE_ARCH = "${MACHINE_ARCH}"

PR = "r0"

DEPENDS = "u-boot-tools-native"

do_patch[noexec] = "1"
do_configure[noexec] = "1"
do_install[noexec] = "1"

inherit deploy

SSP_IROT_ITS = "bl-fit-irot.its"
SSP_IROT_FIT = "bl-fit-irot.bin"

# Create a ITS file for the ASPEED BootMCU-RT and SSP-iRoT FIT image.
ssp_irot_fitimage() {
    conf_loadables="\"ibexfw\""
    rm -f ${SSP_IROT_ITS} ${SSP_IROT_FIT}

    # First we create the ITS script
    cat << EOF >> ${SSP_IROT_ITS}
/dts-v1/;

/ {
    description = "BootMCU-RT and SSP-iRoT FIT";
    #address-cells = <1>;

    images {
EOF
    if [ -n "${UBOOT_FIT_USER_SETTINGS}" ] ; then
        printf "%b" "${UBOOT_FIT_USER_SETTINGS}" >> ${SSP_IROT_ITS}
    fi

    if [ -n "${UBOOT_FIT_CONF_USER_LOADABLES}" ] ; then
        conf_loadables="${conf_loadables}${UBOOT_FIT_CONF_USER_LOADABLES}"
    fi

    cat << EOF >> ${SSP_IROT_ITS}
    };

    configurations {
        default = "conf";
        conf {
            description = "BootMCU-RT and SSP-iRoT FIT";
            loadables = ${conf_loadables};
        };
    };
};
EOF

    #
    # Assemble the BootMCU-RT and SSP-iRoT image
    #
    uboot-mkimage -f ${SSP_IROT_ITS} ${SSP_IROT_FIT}
}

do_compile() {
    ssp_irot_fitimage
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

    # SSP FIT
    append_image(os.path.join(d.getVar('B', True),
                 '%s' % d.getVar('SSP_IROT_FIT',True)),
                  int(d.getVar('IROT_OFFSET_SSP_FIT', True)),
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
    virtual/bootloader:do_deploy \
    ${@bb.utils.contains('MACHINE_FEATURES', 'ast-ssp', 'virtual/ssp:do_deploy', '', d)} \
    ${@bb.utils.contains('MACHINE_FEATURES', 'ast-tsp', 'virtual/tsp:do_deploy', '', d)} \
    ${@bb.utils.contains('MACHINE_FEATURES', 'ast-ibexfw', 'virtual/ibexfw:do_deploy', '', d)} \
    "

addtask deploy before do_build after do_compile
