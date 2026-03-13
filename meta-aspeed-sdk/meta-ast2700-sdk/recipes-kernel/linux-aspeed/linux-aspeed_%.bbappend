FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append:ast2700-default-aspeed-irot = " \
    file://mctp_ipc.cfg \
"
