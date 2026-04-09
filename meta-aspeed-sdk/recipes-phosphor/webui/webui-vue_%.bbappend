FILESEXTRAPATHS:append := ":${THISDIR}/${PN}"

SRC_URI:append = " file://0001-Use-aspeed-s-novnc-fork.patch"
SRC_URI:append = " file://0002-fix-webpack-runtimeId-error.patch"

# Roll back to older webui-vue revision to fix WebUI SOL issue.
SRCREV = "e1420032fef78a490fb2be7b407c4a6bd82b1b02"
