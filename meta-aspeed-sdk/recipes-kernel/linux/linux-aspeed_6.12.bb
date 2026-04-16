KBRANCH = "aspeed-master-v6.12"
LINUX_VERSION ?= "6.12"

# Tag for v00.07.03
SRCREV = "3c16a1fa761f2edbd2a2de6b8b31ab9c27f4e333"
LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

require linux-aspeed.inc

DEPENDS += "lzop-native"

SRC_URI:append = " file://ipmi_ssif.cfg "
SRC_URI:append = " file://mtd_test.cfg "
SRC_URI:append = " file://crpyto_manager.cfg "
SRC_URI:append:spi-nor-ecc = " file://jffs2_writebuffer.cfg "
