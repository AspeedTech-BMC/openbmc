FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

# This is a workarond for AST2700 A1 VFF DCSCM.
# Reduce eth1 speed to solve network issues.
SRC_URI:append:ast2700-vff = " file://90-vff-eth1-network.rules"

do_install:append:ast2700-vff() {
    install -d ${D}/${nonarch_base_libdir}/udev/rules.d
    install -m 0644 ${UNPACKDIR}/90-vff-eth1-network.rules ${D}/${nonarch_base_libdir}/udev/rules.d
}
