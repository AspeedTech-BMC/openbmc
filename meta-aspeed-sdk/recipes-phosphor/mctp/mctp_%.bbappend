FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

do_install:append() {
   install -m 755 ${WORKDIR}/build/mctp-req ${D}${bindir}
   install -m 755 ${WORKDIR}/build/mctp-echo ${D}${bindir}
   install -m 755 ${WORKDIR}/build/mctp-bench ${D}${bindir}
}
