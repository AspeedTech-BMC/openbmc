SUMMARY = "ASPEED BMC Prebuilt Binaries for AST27xx bring-up"
HOMEPAGE = "https://github.com/AspeedTech-BMC/bmc-pb"
PACKAGE_ARCH = "${MACHINE_ARCH}"

require bmc-pb.inc

PREBUILT_DIR ?= "ast2700a2"
PREBUILT_DIR:ast2700-a1 ?= "ast2700a1"
CALIPTRA_FW_BINARY ?= "caliptra-fw.bin"
BOOTMCU_ROM_BINARY ?= ""

do_patch[noexec] = "1"
do_configure[noexec] = "1"
do_compile[noexec] = "1"
do_install[noexec] = "1"

inherit deploy

do_deploy () {
  install -d ${DEPLOYDIR}

  install -m 644 ${S}/${PREBUILT_DIR}/${CALIPTRA_FW_BINARY} ${DEPLOYDIR}
  install -m 644 ${S}/${PREBUILT_DIR}/ddr4_*.bin ${DEPLOYDIR}
  install -m 644 ${S}/${PREBUILT_DIR}/ddr5_*.bin ${DEPLOYDIR}
  install -m 644 ${S}/${PREBUILT_DIR}/dp_*.bin ${DEPLOYDIR}
  install -m 644 ${S}/${PREBUILT_DIR}/uefi_*.bin ${DEPLOYDIR}
  if [ -n "${BOOTMCU_ROM_BINARY}" ]; then
    install -m 644 ${S}/${PREBUILT_DIR}/${BOOTMCU_ROM_BINARY} ${DEPLOYDIR}
  fi
}

addtask deploy before do_build after do_compile
