FILESEXTRAPATHS:append := ":${THISDIR}/${PN}"

SRC_URI:append = " file://0001-Use-aspeed-s-novnc-fork.patch"
SRC_URI:append = " file://0002-revert-limit-to-on-chunk.patch"
SRC_URI:append = " file://Use-the-createWebHashHistory-method-to-avoid-404-err.patch"
