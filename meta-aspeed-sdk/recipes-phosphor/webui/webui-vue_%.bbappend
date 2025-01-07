FILESEXTRAPATHS:append := ":${THISDIR}/${PN}"

SRC_URI:append = " file://0001-Use-aspeed-s-novnc-fork.patch "
SRC_URI:append = " file://0002-revert-limit-to-on-chunk.patch "
