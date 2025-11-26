SUMMARY = "Generate CPTRA authorization flash image with cptra_imgtool"
HOMEPAGE = "https://github.com/AspeedTech-BMC/cptra_imgtool"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

BRANCH = "master"
SRC_URI = "git://github.com/AspeedTech-BMC/cptra_imgtool;protocol=https;branch=${BRANCH};"

# Tag for v00.01.01
SRCREV = "e300bf362330bb3bb1f12bc2d155dfb7a14ccf14"

PV = "1.0+git"
S = "${WORKDIR}/git"

DEPENDS += "caliptra-sw caliptra-mcu-sw"
RDEPENDS:${PN} += "caliptra-sw caliptra-mcu-sw"

inherit cargo

# Using cargo to download packages
CARGO_DISABLE_BITBAKE_VENDORING = "1"

# Enable network for the compile task allowing cargo to download dependencies
do_compile[network] = "1"

do_compile() {
    cd ${S}
    # Build cptra_imgtool
    cargo build -p cptra-imgtool --release
    cd -
}

do_install() {
    install -d ${D}${bindir}

    install -m 0755 ${B}/target/release/cptra-imgtool ${D}${bindir}
}

BBCLASSEXTEND = "native nativesdk"

