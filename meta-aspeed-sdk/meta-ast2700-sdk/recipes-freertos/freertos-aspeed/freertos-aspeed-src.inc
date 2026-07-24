LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE-apache-2.0.txt;md5=635040556244c38145c5162ad045836d"

# ARM prebuilt toolchain, provided by
# meta-arm/meta-arm-toolchain/recipes-devtools/external-arm-toolchain
DEPENDS = "gcc-arm-none-eabi-native"

BRANCH = "main"
SRC_URI = "gitsm://github.com/AspeedTech-BMC/freertos-project;branch=${BRANCH};protocol=https"

# Tag for v00.01.08
SRCREV = "a0c61a61531e211ae747ab01ddce4402dd472e0e"
PV = "1.0+git"
B = "${WORKDIR}/build"

inherit deploy

ARM_PREBUILT_CROSS_COMPILE = "arm-none-eabi-"

EXTRA_OEMAKE = 'CROSS_COMPILE=${ARM_PREBUILT_CROSS_COMPILE} CC="${ARM_PREBUILT_CROSS_COMPILE}gcc"'
EXTRA_OEMAKE += 'HOSTCC="${BUILD_CC} ${BUILD_CFLAGS} ${BUILD_LDFLAGS}"'

do_configure () {
    oe_runmake -C ${S} O=${B} clean
    oe_runmake -C ${S} O=${B} dist-clean
    oe_runmake -C ${S} O=${B} ${FREERTOS_MACHINE}
}

do_compile(){
    oe_runmake -C ${S} O=${B}
}

do_deploy() {
    install -d ${DEPLOYDIR}
    install -m 644 ${B}/bin/ast27xx.elf ${DEPLOYDIR}/freertos-aspeed-${IMG_SUFFIX}.elf
    install -m 644 ${B}/bin/ast27xx.lst ${DEPLOYDIR}/freertos-aspeed-${IMG_SUFFIX}.lst
    install -m 644 ${B}/bin/ast2700-${IMG_SUFFIX}.bin ${DEPLOYDIR}/freertos-aspeed-${IMG_SUFFIX}.bin
}

addtask deploy before do_build after do_compile

INSANE_SKIP:${PN} += "arch"

INHIBIT_SYSROOT_STRIP = "1"
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"

ALLOW_EMPTY:${PN} = "1"

