SUMMARY = "Caliptra MCU firmware and software"
HOMEPAGE = "https://github.com/chipsalliance/caliptra-mcu-sw"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

BRANCH = "aspeed-dev-ast2700a2"
SRC_URI = "gitsm://gerrit.aspeed.com:29418/caliptra-mcu-sw;protocol=ssh;branch=${BRANCH};"
SRCREV = "${AUTOREV}"

PV = "1.0+git"
S = "${WORKDIR}/git"

inherit cargo

# Using cargo to download packages
CARGO_DISABLE_BITBAKE_VENDORING = "1"

# Enable network for the compile task allowing cargo to download dependencies
do_compile[network] = "1"

do_compile() {
    cd ${S}

    # Build xtask
    cargo build -p xtask --release
}

do_install() {
    install -d ${D}${datadir}
    install -d -m 0755 ${D}${datadir}/cptra-imgtool

    install -m 0755 ${B}/target/release/xtask ${D}${datadir}/cptra-imgtool/
}

BBCLASSEXTEND = "native nativesdk"
