KBRANCH = "aspeed-master-v6.6"
LINUX_VERSION ?= "6.6"

# Tag for v00.06.12
SRCREV = "9cb22efd57a7f0c5cdae4c34562d4f3dba70d160"

LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

require linux-aspeed.inc

DEPENDS += "lzop-native"

SRC_URI:append = " file://ipmi_ssif.cfg "
SRC_URI:append = " file://mtd_test.cfg "
SRC_URI:append = " file://crpyto_manager.cfg "
SRC_URI:append:spi-nor-ecc = " file://jffs2_writebuffer.cfg "
