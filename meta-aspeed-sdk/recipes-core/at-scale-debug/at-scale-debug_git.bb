SUMMARY = "At Scale Debug Service"
DESCRIPTION = "At Scale Debug Service exposes remote JTAG target debug capabilities"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=8929d33c051277ca2294fe0f5b062f38"

inherit cmake pkgconfig useradd obmc-phosphor-systemd
DEPENDS = "sdbusplus openssl libpam libgpiod safec linux-libc-headers"

SRC_URI = "git://github.com/Intel-BMC/asd;protocol=https;branch=master"
# 1.5.1
SRCREV = "5f6d69696bd1114c38041faad120b3fb6f661b78"

USERADD_PACKAGES = "${PN}"

# add a special user asdbg
USERADD_PARAM:${PN} = "-u 9999 asdbg"

S = "${WORKDIR}/git"

SYSTEMD_SERVICE:${PN} += "com.intel.AtScaleDebug.service"
SYSTEMD_AUTO_ENABLE:${PN} = "disable"

# Specify any options you want to pass to cmake using EXTRA_OECMAKE:
EXTRA_OECMAKE = "-DBUILD_UT=OFF"

# Copying the depricated header from kernel as a temporary fix to resolve build breaks.
# It should be removed later after fixing the header dependency in this repository.
SRC_URI:append = " file://uapi "

do_configure:prepend() {
    cp -r ${UNPACKDIR}/uapi ${S}/.
}

CFLAGS:append = " -I ${S}"
