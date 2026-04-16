KBRANCH = "aspeed-master-v5.15"
LINUX_VERSION ?= "5.15"

# Tag for v00.05.20
SRCREV = "5f97d495ae1a9518277a9f1d3016047567a50f45"

LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

require linux-aspeed.inc

DEPENDS += "lzop-native"

SRC_URI:append = " file://ipmi_ssif.cfg "
SRC_URI:append = " file://mtd_test.cfg "
SRC_URI:append = " file://crpyto_manager.cfg "
SRC_URI:append:spi-nor-ecc = " file://jffs2_writebuffer.cfg "
