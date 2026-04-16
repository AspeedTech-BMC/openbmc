# AST2700 mcu-runtime bundle build recovery image

RECOVERY_DUMMY_IMAGE = "recovery_dummy_0_bytes.bin"
SOURCE_IMAGES = "zephyr-aspeed-bootmcu.bin"
A1_ZEPHYR_IMAGE = "zephyr-aspeed-bootmcu-a1.bin"
OUT_A1_ZEPHYR_IMAGE = "ast-zephyr-mcu-runtime-a1.bin"

do_deploy () {
    bbnote "Running AST2700 mcu-runtime bundle build recovery image"

    rm -rf ${SOURCE_IMAGE_DIR}
    rm -rf ${OUTPUT_IMAGE_DIR}
    install -d ${SOURCE_IMAGE_DIR}
    install -d ${OUTPUT_IMAGE_DIR}

    # Generate dummy image
    dd if=/dev/zero bs=1 count=4 > ${OUTPUT_IMAGE_DIR}/${RECOVERY_DUMMY_IMAGE}

    # Generate A1 recovery image
    install -m 0644 ${DEPLOY_DIR_IMAGE}/${OUT_A1_ZEPHYR_IMAGE} \
        ${OUTPUT_IMAGE_DIR}/recovery_${A1_ZEPHYR_IMAGE}

    # Generate A2 recovery image
    for source_image in ${SOURCE_IMAGES}; do
        install -m 0644 ${DEPLOY_DIR_IMAGE}/${source_image} ${SOURCE_IMAGE_DIR}
    done
    for source_image in ${SOURCE_IMAGES}; do
        output_image="recovery_${source_image}"
        python3 ${STAGING_BINDIR_NATIVE}/gen_uart_booting_image.py \
            ${SOURCE_IMAGE_DIR}/${source_image} \
            ${OUTPUT_IMAGE_DIR}/${output_image}
    done

    # Deploy all generated UART recovery images
    install -d ${DEPLOYDIR}
    install -m 644 ${OUTPUT_IMAGE_DIR}/* ${DEPLOYDIR}/.
}

do_deploy[depends] += " \
    zephyr-aspeed-bootmcu-a1:do_deploy \
    "
