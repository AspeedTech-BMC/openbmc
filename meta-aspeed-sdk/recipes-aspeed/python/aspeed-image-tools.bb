SUMMARY = "Aspeed image tools"
HOMEPAGE = "https://github.com/AspeedTech-BMC/"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${UNPACKDIR}/LICENSE-2.0.txt;md5=3b83ef96387f14655fc854ddc3c6bd57"

S = "${WORKDIR}/sources"
UNPACKDIR = "${S}"

SRC_URI = "file://gen_emmc_boot_image.py \
           file://gen_uart_booting_image.py \
           file://recovery_spl_extraction.py \
           file://LICENSE-2.0.txt \
          "

inherit python3native

RDEPENDS:${PN} += "python3-core"

BBCLASSEXTEND = "native nativesdk"

do_install() {
    install -d ${D}/${bindir}
    install -m 0755 ${UNPACKDIR}/gen_emmc_boot_image.py ${D}/${bindir}
    install -m 0755 ${UNPACKDIR}/gen_uart_booting_image.py ${D}/${bindir}
    install -m 0755 ${UNPACKDIR}/recovery_spl_extraction.py ${D}/${bindir}
}

FILES:${PN} += "${bindir}"
