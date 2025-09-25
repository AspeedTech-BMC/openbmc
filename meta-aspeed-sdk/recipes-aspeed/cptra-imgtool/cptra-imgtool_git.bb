SUMMARY = "Generate CPTRA authorization flash image with cptra_imgtool"
HOMEPAGE = "https://github.com/AspeedTech-BMC/cptra_imgtool"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

BRANCH = "master"
SRC_URI = "git://github.com/AspeedTech-BMC/cptra_imgtool;protocol=https;branch=${BRANCH};"

# Tag for v00.01.00
SRCREV = "dcf9a5426126f24336ffc719630cfba276abe042"

PV = "1.0+git"
S = "${WORKDIR}/git"

inherit cargo

# Using cargo to download packages
CARGO_DISABLE_BITBAKE_VENDORING = "1"

# Enable network for the compile task allowing cargo to download dependencies
do_compile[network] = "1"

do_compile() {
    cd ${S}
    # Build cptra_imgtool
    cargo build -p cptra-imgtool --release
}

do_install() {
    install -d ${D}${datadir}
    install -d -m 0755 ${D}${datadir}/${BPN}
    install -d -m 0755 ${D}${datadir}/${BPN}/config
    install -d -m 0755 ${D}${datadir}/${BPN}/prebuilt
    install -d -m 0755 ${D}${datadir}/${BPN}/key

    install -m 0755 ${B}/target/release/cptra-imgtool ${D}${datadir}/${BPN}/
    cp --no-preserve=ownership -fr ${S}/config/* ${D}${datadir}/${BPN}/config
    cp --no-preserve=ownership -fr ${S}/prebuilt/* ${D}${datadir}/${BPN}/prebuilt/
    cp --no-preserve=ownership -fr ${S}/key/* ${D}${datadir}/${BPN}/key/
}

BBCLASSEXTEND = "native nativesdk"
