DESCRIPTION = "Generate the AST2700 Caliptra 1.x manifest configuration TOML."
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"
PACKAGE_ARCH = "${MACHINE_ARCH}"

require aspeed-manifest-config-1x.inc

do_patch[noexec] = "1"
do_configure[noexec] = "1"
do_install[noexec] = "1"

inherit deploy

CPTRA_TOML_FILENAME ??= "aspeed-manifest.toml"

do_compile() {
    cptra_generate_manifest_config "${B}/${CPTRA_TOML_FILENAME}"
}

do_deploy() {
    install -d "${DEPLOYDIR}"
    install -m 0644 "${B}/${CPTRA_TOML_FILENAME}" "${DEPLOYDIR}/"
}

addtask deploy before do_build after do_compile
