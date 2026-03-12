inherit skeleton-rev

HOMEPAGE = "http://github.com/openbmc/skeleton"

SRC_URI += "${SKELETON_URI}"
S = "${UNPACKDIR}/${PN}-1.0+git/${SKELETON_DIR}"
