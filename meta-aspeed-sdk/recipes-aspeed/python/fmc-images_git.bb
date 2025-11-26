SUMMARY = "ASPEED FMC images provide the necessary prebuilt image for AST2700 bring-up"
HOMEPAGE = "https://github.com/AspeedTech-BMC/fmc_imgtool"
PACKAGE_ARCH = "${MACHINE_ARCH}"

require fmc-imgtool.inc

do_patch[noexec] = "1"
do_configure[noexec] = "1"
do_compile[noexec] = "1"
do_install[noexec] = "1"

inherit deploy

do_deploy () {
  install -d ${DEPLOYDIR}

  install -m 644 ${S}/prebuilt/${CALIPTRA_FW_BINARY} ${DEPLOYDIR}
  install -m 644 ${S}/prebuilt/ddr4_*.bin ${DEPLOYDIR}
  install -m 644 ${S}/prebuilt/ddr5_*.bin ${DEPLOYDIR}
  install -m 644 ${S}/prebuilt/dp_*.bin ${DEPLOYDIR}
  install -m 644 ${S}/prebuilt/uefi_*.bin ${DEPLOYDIR}
}

addtask deploy before do_build after do_compile

