SUMMARY = "measures the performance of cpu and mcu"
DESCRIPTION = "CoreMarkR is an industry-standard benchmark \
that measures the performance of central processing units \
(CPU) and embedded microcrontrollers (MCU)"

HOMEPAGE = "https://github.com/eembc/coremark"
SECTION = "benchmark"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE.md;md5=0a18b17ae63deaa8a595035f668aebe1"

SRCREV = "d5fad6bd094899101a4e5fd53af7298160ced6ab"

SRC_URI = "\
    git://github.com/eembc/coremark.git;branch=main;protocol=https \
    file://0001-add-LDFLAGS-in-OUTCMD-to-fix-QA-issues.patch \
"

do_compile() {
    rm -rf ${S}/output
    install -d ${S}/output
    oe_runmake clean

    oe_runmake PORT_DIR=linux XCFLAGS="-DMULTITHREAD=1 -DUSE_FORK -pthread" link
    install -m 0755 coremark.exe ${S}/output/coremark_1thread
    oe_runmake clean
    oe_runmake PORT_DIR=linux XCFLAGS="-DMULTITHREAD=2 -DUSE_FORK -pthread" link
    install -m 0755 coremark.exe ${S}/output/coremark_2thread
    oe_runmake clean
    oe_runmake PORT_DIR=linux XCFLAGS="-DMULTITHREAD=4 -DUSE_FORK -pthread" link
    install -m 0755 coremark.exe ${S}/output/coremark_4thread
}

do_install() {
    install -d ${D}/${bindir}
    install -m 0755 ${S}/output/* ${D}/${bindir}
}

