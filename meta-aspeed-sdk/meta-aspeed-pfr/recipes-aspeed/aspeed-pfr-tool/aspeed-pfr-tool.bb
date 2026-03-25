LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit pkgconfig meson

S = "${UNPACKDIR}"

SRC_URI = " file://include/provision.h \
            file://include/checkpoint.h \
            file://include/i2c_utils.h \
            file://include/status.h \
            file://include/info.h \
            file://include/spdm.h \
            file://include/mailbox_enums.h \
            file://include/arguments.h \
            file://include/config.h \
            file://include/utils.h \
            file://provision.c \
            file://checkpoint.c \
            file://i2c_utils.c \
            file://status.c \
            file://info.c \
            file://spdm.c \
            file://main.c \
            file://utils.c \
            file://meson.build \
            file://meson_options.txt \
            file://aspeed-pfr-tool.conf.in \
            file://aspeed-pfr-tool-egs.conf \
          "

DEPENDS = "openssl i2c-tools"
RDEPENDS:${PN} = "openssl i2c-tools"

do_install:append() {
    install -d ${D}/${datadir}/pfrconfig
    install -m 0644 ${S}/aspeed-pfr-tool-egs.conf ${D}/${datadir}/pfrconfig/
}

FILES:${PN}:append = " ${datadir}/pfrconfig"

PACKAGECONFIG ??= ""

PACKAGECONFIG:append:intel-pfr = " attestation pfr-5-0-secure-conn"

PACKAGECONFIG[attestation] = "-Dattestation=enabled, -Dattestation=disabled,, spdm-emu"
PACKAGECONFIG[pfr-5-0-secure-conn] = "-Dsecure_connection=enabled, -Dsecure_connection=disabled, spdm-emu"
PACKAGECONFIG[pfr-5-0-secure-test-case] = "-Dsecure_test_case=enabled, -Dsecure_test_case=disabled, spdm-emu"

