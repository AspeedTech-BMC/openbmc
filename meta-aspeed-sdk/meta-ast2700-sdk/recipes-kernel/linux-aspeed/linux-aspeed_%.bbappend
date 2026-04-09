FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append:ast2700-irot = " \
    file://mctp_ipc.cfg \
"
