SUMMARY = "The Secondary Service Processor (SSP) firmware"
DESCRIPTION = "The Secondary Service Processor (SSP) is an ARM Cortex-M3 r2p1 processor \
for AST2600 and ARM Cortex-M4F r0p1 processor for AST2700 which are aimed at \
monitoring/controlling the peripherals to offload the primary processor (PSP) \
of Cortex-A7 for AST2600 and Cortex-A35 for AST2700."
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${ASPEEDSDKBASE}/LICENSE;md5=a3740bd0a194cd6dcafdc482a200a56f"
PACKAGE_ARCH = "${MACHINE_ARCH}"

PROVIDES += "virtual/ssp"
RPROVIDES:${PN} += "virtual-ssp"
PR = "r0"

SSP_FIRMWARE ?= "ast2700-ssp.bin"
SSP_FIRMWARE:aspeed-g6 ?= "ast2600_ssp.bin"

SSP_FIRMWARE_ELF ?= "ast2700-ssp.elf"
SSP_FIRMWARE_ELF:aspeed-g6 ?= ""

SSP_FIRMWARE_EXTRA ?= "${@'file://${SSP_FIRMWARE_ELF};subdir=${S}' if len('${SSP_FIRMWARE_ELF}') else ''}"

SRC_URI = "file://${SSP_FIRMWARE};subdir=${S} \
           ${SSP_FIRMWARE_EXTRA} \
          "

do_patch[noexec] = "1"
do_configure[noexec] = "1"
do_compile[noexec] = "1"

inherit deploy

do_install () {
    if [ "${SOC_FAMILY}" = "aspeed-g6" ]; then
        install -d ${D}/${base_libdir}/firmware
        install -m 644 ${S}/${SSP_FIRMWARE} ${D}/${base_libdir}/firmware
    fi
}

do_deploy () {
    if [ "${SOC_FAMILY}" = "aspeed-g7" ]; then
        install -d ${DEPLOYDIR}
        install -m 644 ${S}/${SSP_FIRMWARE} ${DEPLOYDIR}/.
        if [ -f ${S}/${SSP_FIRMWARE_ELF} ]; then
            install -m 644 ${S}/${SSP_FIRMWARE_ELF} ${DEPLOYDIR}/.
        fi
    fi
}

addtask deploy before do_build after do_compile

FILES:${PN} += "${base_libdir}/firmware/*"
ALLOW_EMPTY:${PN} = "1"

