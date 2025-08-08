UBOOT_FIT_SSP_ITS = '\
       sspfw {\n\
            description = \"SSP Firmware\";\n\
            data = /incbin/(\"${UBOOT_FIT_SSP_IMAGE}\");\n\
            type = \"firmware\";\n\
            arch = \"${UBOOT_FIT_SSP_ARCH}\";\n\
            os = \"${UBOOT_FIT_SSP_OS}\";\n\
            load = <${UBOOT_FIT_SSP_LOADADDRESS}>;\n\
            entry = <${UBOOT_FIT_SSP_ENTRYPOINT}>;\n\
            compression = \"none\";\n\
        };\n\
'

UBOOT_FIT_SSP_SECURE_ITS = '\
       sspfw {\n\
            description = \"SSP Firmware\";\n\
            data = /incbin/(\"${UBOOT_FIT_SSP_IMAGE}\");\n\
            type = \"firmware\";\n\
            arch = \"${UBOOT_FIT_SSP_ARCH}\";\n\
            os = \"${UBOOT_FIT_SSP_OS}\";\n\
            load = <${UBOOT_FIT_SSP_LOADADDRESS}>;\n\
            entry = <${UBOOT_FIT_SSP_ENTRYPOINT}>;\n\
            compression = \"none\";\n\
            signature {\n\
                algo = \"${UBOOT_FIT_HASH_ALG},${UBOOT_FIT_SIGN_ALG}\";\n\
                key-name-hint = \"${SPL_SIGN_KEYNAME}\";\n\
            };\n\
        };\n\
'

UBOOT_FIT_TSP_ITS = '\
       tspfw {\n\
            description = \"TSP Firmware\";\n\
            data = /incbin/(\"${UBOOT_FIT_TSP_IMAGE}\");\n\
            type = \"firmware\";\n\
            arch = \"${UBOOT_FIT_TSP_ARCH}\";\n\
            os = \"${UBOOT_FIT_TSP_OS}\";\n\
            load = <${UBOOT_FIT_TSP_LOADADDRESS}>;\n\
            entry = <${UBOOT_FIT_TSP_ENTRYPOINT}>;\n\
            compression = \"none\";\n\
        };\n\
'

UBOOT_FIT_TSP_SECURE_ITS = '\
        tspfw {\n\
            description = \"TSP Firmware\";\n\
            data = /incbin/(\"${UBOOT_FIT_TSP_IMAGE}\");\n\
            type = \"firmware\";\n\
            arch = \"${UBOOT_FIT_TSP_ARCH}\";\n\
            os = \"${UBOOT_FIT_TSP_OS}\";\n\
            load = <${UBOOT_FIT_TSP_LOADADDRESS}>;\n\
            entry = <${UBOOT_FIT_TSP_ENTRYPOINT}>;\n\
            compression = \"none\";\n\
            signature {\n\
                algo = \"${UBOOT_FIT_HASH_ALG},${UBOOT_FIT_SIGN_ALG}\";\n\
                key-name-hint = \"${SPL_SIGN_KEYNAME}\";\n\
            };\n\
        };\n\
'

UBOOT_FIT_IBEXFW_ITS = '\
       ibexfw {\n\
            description = \"Ibex Firmware\";\n\
            data = /incbin/(\"${UBOOT_FIT_IBEXFW_IMAGE}\");\n\
            type = \"firmware\";\n\
            arch = \"${UBOOT_FIT_IBEXFW_ARCH}\";\n\
            os = \"${UBOOT_FIT_IBEXFW_OS}\";\n\
            load = <${UBOOT_FIT_IBEXFW_LOADADDRESS}>;\n\
            entry = <${UBOOT_FIT_IBEXFW_ENTRYPOINT}>;\n\
            compression = \"none\";\n\
        };\n\
'

UBOOT_FIT_IBEXFW_SECURE_ITS = '\
       ibexfw {\n\
            description = \"Ibex Firmware\";\n\
            data = /incbin/(\"${UBOOT_FIT_IBEXFW_IMAGE}\");\n\
            type = \"firmware\";\n\
            arch = \"${UBOOT_FIT_IBEXFW_ARCH}\";\n\
            os = \"${UBOOT_FIT_IBEXFW_OS}\";\n\
            load = <${UBOOT_FIT_IBEXFW_LOADADDRESS}>;\n\
            entry = <${UBOOT_FIT_IBEXFW_ENTRYPOINT}>;\n\
            compression = \"none\";\n\
            signature {\n\
                algo = \"${UBOOT_FIT_HASH_ALG},${UBOOT_FIT_SIGN_ALG}\";\n\
                key-name-hint = \"${SPL_SIGN_KEYNAME}\";\n\
            };\n\
        };\n\
'

MACHINEOVERRIDES .= "${@':ast-bl-fit-sign' if "${SPL_SIGN_ENABLE}" == "1" else ''}"

ASPEED_UBOOT_FIT_USER_SETTINGS:aspeed-g7:append:ast-ssp = " ${UBOOT_FIT_SSP_ITS}"
ASPEED_UBOOT_FIT_USER_SETTINGS:append:ast-tsp = " ${UBOOT_FIT_TSP_ITS}"
ASPEED_UBOOT_FIT_USER_SETTINGS:append:ast-ibexfw = " ${UBOOT_FIT_IBEXFW_ITS}"

ASPEED_UBOOT_FIT_USER_SECURE_SETTINGS:aspeed-g7:append:ast-ssp = " ${UBOOT_FIT_SSP_SECURE_ITS}"
ASPEED_UBOOT_FIT_USER_SECURE_SETTINGS:append:ast-tsp = " ${UBOOT_FIT_TSP_SECURE_ITS}"
ASPEED_UBOOT_FIT_USER_SECURE_SETTINGS:append:ast-ibexfw = " ${UBOOT_FIT_IBEXFW_SECURE_ITS}"

UBOOT_FIT_USER_SETTINGS = "${ASPEED_UBOOT_FIT_USER_SETTINGS}"
UBOOT_FIT_USER_SETTINGS:ast-bl-fit-sign = "${ASPEED_UBOOT_FIT_USER_SECURE_SETTINGS}"

do_compile[depends] += " \
    ${@bb.utils.contains('MACHINE_FEATURES', 'ast-ssp', 'virtual/ssp:do_deploy', '', d)} \
    ${@bb.utils.contains('MACHINE_FEATURES', 'ast-tsp', 'virtual/tsp:do_deploy', '', d)} \
    ${@bb.utils.contains('MACHINE_FEATURES', 'ast-ibexfw', 'virtual/ibexfw:do_deploy', '', d)} \
    "
