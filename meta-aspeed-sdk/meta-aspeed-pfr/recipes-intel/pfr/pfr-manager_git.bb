SUMMARY = "PFR Manager Service"
DESCRIPTION = "Daemon to handle all PFR functionalities"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=7becf906c8f8d03c237bad13bc3dac53"
inherit cmake systemd pkgconfig

SRC_URI = "git://github.com/openbmc/pfr-manager;branch=master;protocol=https \
           file://0001-fix-pfr-manager-crash.patch \
           file://0002-fix-no-postcodeIface.patch \
           file://0003-fix-no-update-UfmProvisioned-property.patch \
           file://0004-Fix-build-issues-with-boost-1.87.patch \
           file://0005-Fix-build-issue-updatde-to-C-23.patch \
           "

PV = "0.1+git"
SRCREV = "1695ee330bc54df35f537b4026bf1e2426ce9d28"

S = "${WORKDIR}/git"

SYSTEMD_SERVICE:${PN} = "xyz.openbmc_project.PFR.Manager.service"

DEPENDS += " \
    sdbusplus \
    phosphor-logging \
    boost \
    i2c-tools \
    libgpiod \
    "
