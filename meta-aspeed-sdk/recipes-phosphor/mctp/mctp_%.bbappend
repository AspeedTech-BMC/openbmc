FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRCREV = "e35f4a5b212e86e6546e8982b46946c2d8af6041"

#ToDo
SRC_URI:append = " file://0001-mctp-req-Add-data-argument-in-usage.patch"
#SRC_URI:append = " file://0002-mctpd-pfr-Support-intel-pfr-DAA-flow.patch"
#The new SRCREV has included this patch file.
SRC_URI:remove = " file://0001-mctp-bench-Adjust-headers.patch"

do_install:append() {
   install -m 755 ${WORKDIR}/build/mctp-req ${D}${bindir}
   install -m 755 ${WORKDIR}/build/mctp-echo ${D}${bindir}
   install -m 755 ${WORKDIR}/build/mctp-bench ${D}${bindir}
}
