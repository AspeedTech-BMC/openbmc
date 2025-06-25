EXTRA_OECONF = " --without-encryption \
                 --without-random-device \
                 --disable-init-scripts \
                 --with-dont-check-for-root \
                 --enable-rawdumps \
               "

PACKAGES =+ " \
    ${PN}-ipmi-raw \
    ${PN}-other \
    "

FILES:${PN}-ipmi-raw = " ${sbindir}/ipmi-raw "
FILES:${PN}-other = " ${sbindir}/* "
