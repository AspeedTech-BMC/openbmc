LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE-apache-2.0.txt;md5=635040556244c38145c5162ad045836d"

# ARM prebuilt toolchain, provided by
# meta-arm/meta-arm-toolchain/recipes-devtools/external-arm-toolchain
DEPENDS = "gcc-arm-none-eabi-native"

BRANCH = "main"
SRC_URI = "gitsm://github.com/AspeedTech-BMC/freertos-project;branch=${BRANCH};protocol=https"

# Tag for v00.01.04
SRCREV = "efe8161609d6e90acfb8b54ddb773706aedeb1f5"
PV = "1.0+git"

S = "${WORKDIR}/git"

inherit deploy

ARM_PREBUILT_CROSS_COMPILE = "arm-none-eabi-"

PARALLEL_MAKE = ""

EXTRA_OEMAKE = 'CROSS_COMPILE=${ARM_PREBUILT_CROSS_COMPILE} CC="${ARM_PREBUILT_CROSS_COMPILE}gcc"'
EXTRA_OEMAKE += 'HOSTCC="${BUILD_CC} ${BUILD_CFLAGS} ${BUILD_LDFLAGS}"'

do_configure () {
    oe_runmake -C ${S} cleanconfig
    oe_runmake -C ${S} ${FREERTOS_MACHINE}
}

do_compile(){
    oe_runmake -C ${S} clean
    oe_runmake -C ${S} dist-clean
    oe_runmake -C ${S} image
}

do_deploy() {
    install -d ${DEPLOYDIR}
    install -m 644 ${S}/bin/ast27xx.elf ${DEPLOYDIR}/freertos-aspeed-${IMG_SUFFIX}.elf
    install -m 644 ${S}/bin/ast27xx.lst ${DEPLOYDIR}/freertos-aspeed-${IMG_SUFFIX}.lst
    install -m 644 ${S}/bin/ast2700-${IMG_SUFFIX}.bin ${DEPLOYDIR}/freertos-aspeed-${IMG_SUFFIX}.bin
}

addtask deploy before do_build after do_compile

INSANE_SKIP:${PN} += "arch"

INHIBIT_SYSROOT_STRIP = "1"
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"

ALLOW_EMPTY:${PN} = "1"

