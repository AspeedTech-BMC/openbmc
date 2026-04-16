SUMMARY = "The xdma-test application performs DMA operations between the BMC and the host."
HOMEPAGE = "https://github.com/eddiejames/xdma-test"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b234ee4d69f5fce4486a80fdaf4a4263"

INSANE_SKIP:${PN} = "ldflags"

SRC_URI = "git://github.com/eddiejames/xdma-test.git;protocol=https;branch=master"

PV = "1.0+git"
SRCREV = "caab5a2f47364e95e6da96526a6af6787dc2198c"

do_compile() {
    oe_runmake
}

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${B}/xdma-test ${D}${bindir}/xdma-test
}

FILES:${PN} = "/usr/bin/xdma-test"

