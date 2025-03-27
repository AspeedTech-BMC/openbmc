# Create a ITS file for the ssp
uboot_fitimage_ssp() {
	cat << EOF >> ${UBOOT_ITS}
        sspfw {
            description = "SSP Firmware";
            data = /incbin/("${UBOOT_FIT_SSP_IMAGE}");
            type = "firmware";
            arch = "${UBOOT_FIT_SSP_ARCH}";
            os = "${UBOOT_FIT_SSP_OS}";
            load = <${UBOOT_FIT_SSP_LOADADDRESS}>;
            entry = <${UBOOT_FIT_SSP_ENTRYPOINT}>;
            compression = "none";
EOF
	if [ "${SPL_SIGN_ENABLE}" = "1" ] ; then
		cat << EOF >> ${UBOOT_ITS}
            signature {
                algo = "${UBOOT_FIT_HASH_ALG},${UBOOT_FIT_SIGN_ALG}";
                key-name-hint = "${SPL_SIGN_KEYNAME}";
            };
EOF
	fi

	cat << EOF >> ${UBOOT_ITS}
        };
EOF
}

# Create a ITS file for the tsp
uboot_fitimage_tsp() {
	cat << EOF >> ${UBOOT_ITS}
        tspfw {
            description = "TSP Firmware";
            data = /incbin/("${UBOOT_FIT_TSP_IMAGE}");
            type = "firmware";
            arch = "${UBOOT_FIT_TSP_ARCH}";
            os = "${UBOOT_FIT_TSP_OS}";
            load = <${UBOOT_FIT_TSP_LOADADDRESS}>;
            entry = <${UBOOT_FIT_TSP_ENTRYPOINT}>;
            compression = "none";
EOF
	if [ "${SPL_SIGN_ENABLE}" = "1" ] ; then
		cat << EOF >> ${UBOOT_ITS}
            signature {
                algo = "${UBOOT_FIT_HASH_ALG},${UBOOT_FIT_SIGN_ALG}";
                key-name-hint = "${SPL_SIGN_KEYNAME}";
            };
EOF
	fi

	cat << EOF >> ${UBOOT_ITS}
        };
EOF
}

# Create a ITS file for the ibexfw
uboot_fitimage_ibexfw() {
	cat << EOF >> ${UBOOT_ITS}
        ibexfw {
            description = "Ibex Firmware";
            data = /incbin/("${UBOOT_FIT_IBEXFW_IMAGE}");
            type = "firmware";
            arch = "${UBOOT_FIT_IBEXFW_ARCH}";
            os = "${UBOOT_FIT_IBEXFW_OS}";
            load = <${UBOOT_FIT_IBEXFW_LOADADDRESS}>;
            entry = <${UBOOT_FIT_IBEXFW_ENTRYPOINT}>;
            compression = "none";
EOF
	if [ "${SPL_SIGN_ENABLE}" = "1" ] ; then
		cat << EOF >> ${UBOOT_ITS}
            signature {
                algo = "${UBOOT_FIT_HASH_ALG},${UBOOT_FIT_SIGN_ALG}";
                key-name-hint = "${SPL_SIGN_KEYNAME}";
            };
EOF
	fi

	cat << EOF >> ${UBOOT_ITS}
        };
EOF
}

uboot_fitimage_user_image() {
	if [ "${UBOOT_FIT_SSP}" = "1" ] ; then
		uboot_fitimage_ssp
	fi
	if [ "${UBOOT_FIT_TSP}" = "1" ] ; then
		uboot_fitimage_tsp
	fi
	if [ "${UBOOT_FIT_IBEXFW}" = "1" ] ; then
		uboot_fitimage_ibexfw
	fi
}

do_compile[depends] += " \
    ${@bb.utils.contains('MACHINE_FEATURES', 'ast-ssp', 'virtual/ssp:do_deploy', '', d)} \
    ${@bb.utils.contains('MACHINE_FEATURES', 'ast-tsp', 'virtual/tsp:do_deploy', '', d)} \
    ${@bb.utils.contains('MACHINE_FEATURES', 'ast-ibexfw', 'virtual/ibexfw:do_deploy', '', d)} \
    "
