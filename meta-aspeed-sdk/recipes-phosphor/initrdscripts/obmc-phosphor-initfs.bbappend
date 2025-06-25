FILESEXTRAPATHS:prepend:ast2700-abr := "${THISDIR}/files:"


# Update obmc-init.sh and obmc-update.sh for AST2700 single flash ABR.
SRC_URI:append:ast2700-abr = " file://obmc-init-ast2700-ABR.sh \
                               file://obmc-update-ast2700-ABR.sh \
                             "

do_install:append:ast2700-abr() {
    install -m 0755 ${UNPACKDIR}/obmc-init-ast2700-ABR.sh ${D}/init
    install -m 0755 ${UNPACKDIR}/obmc-update-ast2700-ABR.sh ${D}/update
}
