FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append = " file://aspeed-pfr-tool-ast2700.conf;subdir=${S}"

do_install:append() {
    install -m 0644 ${S}/aspeed-pfr-tool-ast2700.conf ${D}/${datadir}/pfrconfig/aspeed-pfr-tool.conf
}
