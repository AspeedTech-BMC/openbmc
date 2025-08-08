SUMMARY = "PFR Manager Service"
DESCRIPTION = "Daemon to handle all PFR functionalities"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=7becf906c8f8d03c237bad13bc3dac53"
inherit cmake systemd pkgconfig

SRC_URI = "git://github.com/openbmc/pfr-manager;branch=master;protocol=https \
           file://0001-fix-pfr-manager-crash.patch \
           file://0002-fix-no-postcodeIface.patch \
           "

PV = "0.1+git"
SRCREV = "eedbae776db69b033de23629310692aa3b780b41"

S = "${WORKDIR}/git"

SYSTEMD_SERVICE:${PN} = "xyz.openbmc_project.PFR.Manager.service"

DEPENDS += " \
    sdbusplus \
    phosphor-logging \
    boost \
    i2c-tools \
    libgpiod \
    "
