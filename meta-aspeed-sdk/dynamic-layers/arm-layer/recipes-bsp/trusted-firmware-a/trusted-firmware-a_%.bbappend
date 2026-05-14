FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

FILES:${PN} += " \
    ${FIRMWARE_DIR}/*.map \
    ${FIRMWARE_DIR}/*.dump \
"

do_install:append() {
    for atfbin in ${TFA_INSTALL_TARGET}; do
        if [ -f ${BUILD_DIR}/$atfbin/$atfbin.map ]; then
            echo "Install $atfbin.map"
            install -m 0644 ${BUILD_DIR}/$atfbin/$atfbin.map \
                ${D}${FIRMWARE_DIR}/$atfbin-${TFA_PLATFORM}.map
            ln -sf $atfbin-${TFA_PLATFORM}.map ${D}${FIRMWARE_DIR}/$atfbin.map
        fi
        if [ -f ${BUILD_DIR}/$atfbin/$atfbin.dump ]; then
            echo "Install $atfbin.dump"
            install -m 0644 ${BUILD_DIR}/$atfbin/$atfbin.dump \
                ${D}${FIRMWARE_DIR}/$atfbin-${TFA_PLATFORM}.dump
            ln -sf $atfbin-${TFA_PLATFORM}.dump ${D}${FIRMWARE_DIR}/$atfbin.dump
        fi
    done
}
