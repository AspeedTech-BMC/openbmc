SUMMARY = "libmctp:intel"
DESCRIPTION = "Implementation of MCTP(DMTF DSP0236)"

SRC_URI = "git://github.com/Intel-BMC/libmctp.git;protocol=https;branch=master"
SRCREV = "21dc38e911a27af2e914f834b2e2b775f7dad520"

PV = "1.0+git"

LICENSE = "Apache-2.0 | GPL-2.0-or-later"
LIC_FILES_CHKSUM = "file://LICENSE;md5=0d30807bb7a4f16d36e96b78f9ed8fae"

inherit cmake

DEPENDS += "i2c-tools"
