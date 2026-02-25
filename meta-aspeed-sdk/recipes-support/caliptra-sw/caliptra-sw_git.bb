SUMMARY = "Caliptra firmware and software"
HOMEPAGE = "https://github.com/chipsalliance/caliptra-sw"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"


BRANCH = "aspeed-rt-1.2.0"
SRC_URI = "gitsm://github.com/AspeedTech-BMC/caliptra-sw;protocol=https;branch=${BRANCH};"

# Tag for v01.02.04
SRCREV = "40f4dce10e5eda01efc5b3f5e0b03d63886bed62"

PV = "1.0+git"
S = "${WORKDIR}/git"

CARGO_SRC_DIR = "auth-manifest/app/"

inherit cargo

# Using cargo to download packages
CARGO_DISABLE_BITBAKE_VENDORING = "1"

# Enable network for the compile task allowing cargo to download dependencies
do_compile[network] = "1"

do_compile() {
    cd ${S}
    # Build caliptra-auth-manifest-app
    cargo build --release --manifest-path=auth-manifest/app/Cargo.toml
}

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${B}/target/release/caliptra-auth-manifest-app ${D}${bindir}
}

BBCLASSEXTEND = "native nativesdk"
