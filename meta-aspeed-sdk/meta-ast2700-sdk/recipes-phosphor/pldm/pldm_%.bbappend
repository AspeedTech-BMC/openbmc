FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append:ast2700-irot = " file://host_eid "

do_install:append:ast2700-irot() {
    install -D -m 0644 ${UNPACKDIR}/host_eid ${D}/usr/share/pldm
}
