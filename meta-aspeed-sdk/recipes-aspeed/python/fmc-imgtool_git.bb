SUMMARY = "ASPEED FMC image tool is to pack the SoC FMC image with the header \
for runtime FW load."
HOMEPAGE = "https://github.com/AspeedTech-BMC/fmc_imgtool"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=522c9891674011c4a2390d828772384d"

BRANCH = "master"
SRC_URI = "git://github.com/AspeedTech-BMC/fmc_imgtool.git;protocol=https;branch=${BRANCH}"

# Tag for v00.01.02+
SRCREV = "66445bbc3538c7c8940870be9698311081aa93fc"

PV = "1.0+git"

S = "${WORKDIR}/git"

inherit python3native deploy

DEPENDS += "python3-cryptography"
DEPENDS += "python3-pyhsslms"

RDEPENDS:${PN} += "python3-cryptography"
RDEPENDS:${PN} += "python3-pyhsslms"

do_install() {
    install -d -m 0755 ${D}${libdir}/${PYTHON_DIR}
    install -d -m 0755 ${D}${libdir}/${PYTHON_DIR}/${BPN}
    install -d -m 0755 ${D}${libdir}/${PYTHON_DIR}/${BPN}/prebuilt
    install -d -m 0755 ${D}${libdir}/${PYTHON_DIR}/${BPN}/keys

    install -m 0644 ${S}/*.py ${D}${libdir}/${PYTHON_DIR}/${BPN}
    install -m 0644 ${S}/prebuilt/* ${D}${libdir}/${PYTHON_DIR}/${BPN}/prebuilt
    install -m 0644 ${S}/keys/* ${D}${libdir}/${PYTHON_DIR}/${BPN}/keys
}

do_deploy() {
    :
}

do_deploy:class-native() {
    install -m 0644 ${S}/prebuilt/* ${DEPLOYDIR}/.
}

addtask deploy before do_build after do_compile

BBCLASSEXTEND = "native nativesdk"

FILES:${PN} += "${libdir}/*"

