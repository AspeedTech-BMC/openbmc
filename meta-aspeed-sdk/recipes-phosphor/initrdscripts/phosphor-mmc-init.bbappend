FILESEXTRAPATHS:prepend:ast-ufs := "${THISDIR}/files:"

# Use ufs-init.sh for UFS init script.
SRC_URI:append:ast-ufs = " file://ufs-init.sh \
                         "

do_install:append:ast-ufs () {
    install -m 0755 ${UNPACKDIR}/ufs-init.sh ${D}/init
}
