KBRANCH = "aspeed-master-v5.15"
LINUX_VERSION ?= "5.15"

# Tag for v00.05.22
SRCREV = "f8c9f311fc3b1752815580df1ce7080b592cd0dd"

LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

require linux-aspeed.inc

DEPENDS += "lzop-native"

SRC_URI:append = " file://ipmi_ssif.cfg "
SRC_URI:append = " file://mtd_test.cfg "
SRC_URI:append = " file://crpyto_manager.cfg "
SRC_URI:append:spi-nor-ecc = " file://jffs2_writebuffer.cfg "

# scripts/gcc-plugins fails to build against gcc 16's plugin headers
SRC_URI:append = " file://disable_gcc_plugins.cfg "
