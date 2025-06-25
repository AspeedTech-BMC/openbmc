require u-boot-common-aspeed-sdk_${PV}.inc

UBOOT_MAKE_TARGET ?= "DEVICE_TREE=${UBOOT_DEVICETREE}"

require recipes-bsp/u-boot/u-boot-aspeed.inc

PROVIDES += "u-boot"
DEPENDS += "bc-native dtc-native"
DEPENDS += "${@bb.utils.contains('MACHINE_FEATURES', 'ast-secure', 'aspeed-secure-config-native', '', d)}"

UBOOT_ENV_SIZE:ast-mmc = "0x20000"
UBOOT_ENV:ast-mmc = "u-boot-env"
UBOOT_ENV_SUFFIX:ast-mmc = "bin"
UBOOT_ENV_TXT:ast-mmc = "u-boot-env.txt"

do_compile:append() {
    if [ -n "${UBOOT_ENV}" ]
    then
        # Generate default environment image
        # add -r parameter if wants redundant environment image
        ${B}/tools/mkenvimage -s ${UBOOT_ENV_SIZE} -o ${B}/${UBOOT_ENV_BINARY} ${UNPACKDIR}/${UBOOT_ENV_TXT}
    fi
}
