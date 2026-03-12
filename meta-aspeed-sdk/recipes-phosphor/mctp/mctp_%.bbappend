FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

do_install:append() {
   install -m 755 ${B}/mctp-req ${D}${bindir}
   install -m 755 ${B}/mctp-echo ${D}${bindir}
   install -m 755 ${B}/mctp-bench ${D}${bindir}
}
