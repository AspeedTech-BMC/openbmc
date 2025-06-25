FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

do_install:append() {
    for atfbin in ${TFA_INSTALL_TARGET}; do
        if [ -f ${BUILD_DIR}/$atfbin/$atfbin.map ]; then
            echo "Install $atfbin.map"
            install -m 0644 ${BUILD_DIR}/$atfbin/$atfbin.map \
                ${D}/firmware/$atfbin-${TFA_PLATFORM}.map
            ln -sf $atfbin-${TFA_PLATFORM}.map ${D}/firmware/$atfbin.map
        fi
        if [ -f ${BUILD_DIR}/$atfbin/$atfbin.dump ]; then
            echo "Install $atfbin.dump"
            install -m 0644 ${BUILD_DIR}/$atfbin/$atfbin.dump \
                ${D}/firmware/$atfbin-${TFA_PLATFORM}.dump
            ln -sf $atfbin-${TFA_PLATFORM}.dump ${D}/firmware/$atfbin.dump
        fi
    done
}
