SUMMARY = "BootMCU execute Zephyr OS"
DESCRIPTION = "BootMCU is designated to load the first, verified image for the main processor"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${ASPEEDSDKBASE}/LICENSE;md5=a3740bd0a194cd6dcafdc482a200a56f"
PACKAGE_ARCH = "${MACHINE_ARCH}"

PR = "r0"

PROVIDES += "virtual/bootmcu"
DEPENDS += "fmc-imgtool-native"

BOOTMCU_FIRMWARE ?= "ast2700-zephyr.bin"

S = "${WORKDIR}/sources"
UNPACKDIR = "${S}"
SRC_URI = "file://${BOOTMCU_FIRMWARE};subdir=${S}"

inherit python3native deploy

# export CRYPTOGRAPHY_OPENSSL_NO_LEGACY variable to fix the following errors.
# OpenSSL 3.0 legacy provider failed to load
# https://github.com/pyca/cryptography/issues/10598
do_compile() {
    export CRYPTOGRAPHY_OPENSSL_NO_LEGACY=1

    cd ${STAGING_LIBDIR_NATIVE}/${PYTHON_DIR}/fmc-imgtool
    python3 main.py \
        --verbose \
        --version 2 \
        --input ${S}/${BOOTMCU_FIRMWARE} \
        --output ${S}/fmc-${BOOTMCU_FIRMWARE}
    cd -
}

do_deploy () {
    install -d ${DEPLOYDIR}
    install -m 644 ${S}/${BOOTMCU_FIRMWARE} ${DEPLOYDIR}/.
    install -m 644 ${S}/fmc-${BOOTMCU_FIRMWARE} ${DEPLOYDIR}/.
    cd ${DEPLOYDIR}
    ln -sf fmc-${BOOTMCU_FIRMWARE} bootmcu-fmc.bin
    cd -
}

addtask deploy before do_build after do_compile

ALLOW_EMPTY:${PN} = "1"
