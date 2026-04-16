FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append:ast-irot = " \
    file://mctp_ipc.cfg \
"
