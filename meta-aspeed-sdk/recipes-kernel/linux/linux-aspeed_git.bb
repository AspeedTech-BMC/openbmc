KBRANCH = "aspeed-master-v6.6"
LINUX_VERSION ?= "6.6.93"

# Tag for v00.06.07+
SRCREV = "0b1181b8d94062208f23e67fa672b10bac4e7411"

LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

require linux-aspeed.inc

DEPENDS += "lzop-native"
DEPENDS += "${@bb.utils.contains('MACHINE_FEATURES', 'ast-secure', 'aspeed-secure-config-native', '', d)}"

SRC_URI:append = " file://ipmi_ssif.cfg "
SRC_URI:append = " file://mtd_test.cfg "
SRC_URI:append = " file://crpyto_manager.cfg "
SRC_URI:append:spi-nor-ecc = " file://jffs2_writebuffer.cfg "
SRC_URI:append:aspeed-g5 = " file://aspeed_g5_disable.cfg "
