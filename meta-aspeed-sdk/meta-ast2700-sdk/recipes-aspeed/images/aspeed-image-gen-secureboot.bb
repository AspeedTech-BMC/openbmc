DESCRIPTION = "Generate aspeed customize secure boot images for AST2700. \
It is used for testing. Users should not use these generated images for production."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${ASPEEDSDKBASE}/LICENSE;md5=a3740bd0a194cd6dcafdc482a200a56f"
PACKAGE_ARCH = "${MACHINE_ARCH}"

PR = "r0"

DEPENDS = " \
    aspeed-image-tools-native \
    socsec-native \
    aspeed-secure-config-native \
    fmc-imgtool-native \
    u-boot-tools-native \
    dtc-native \
    xz-native \
    e2fsprogs-native \
    parted-native \
    cptra-imgtool-native \
    virtual/bootloader \
    "

do_patch[noexec] = "1"
do_configure[noexec] = "1"
do_compile[noexec] = "1"
do_install[noexec] = "1"

inherit python3native deploy

ASPEED_CUSTOMIZE_GEN_SECURE_IMAGE_ENABLE ?= "0"
ASPEED_CUSTOMIZE_GEN_SECURE_IMAGE ?= "\
    ecdsa384 \
    ecdsa384-lms \
    "

DISTROOVERRIDES .= ":flash-${FLASH_SIZE}"
MCU_RUNTIME_IMAGE = "zephyr-aspeed-bootmcu.bin"
UBOOT_IMAGE_NAME = "u-boot.bin"
KERNEL_FITIMAGE_NAME = "fitImage-${INITRAMFS_IMAGE}-${MACHINE}-${MACHINE}"
KERNEL_FITIMAGE_ITS_NAME = "fitImage-its-${INITRAMFS_IMAGE}-${MACHINE}-${MACHINE}"
ASPEED_BOOT_EMMC_UFS = "${@bb.utils.contains_any('MACHINE_FEATURES', ['ast-mmc', 'ast-ufs'], 'yes', 'no', d)}"
ASPEED_BOOT_UFS = "${@bb.utils.contains('MACHINE_FEATURES', 'ast-ufs', 'yes', 'no', d)}"
ASPEED_IROT = "${@bb.utils.contains('MACHINE_FEATURES', 'ast-irot', 'yes', 'no', d)}"

IMAGE_BASE_NAME = "obmc-phosphor-image"
INITRAMFS_IMAGE_NAME = "${INITRAMFS_IMAGE}-${MACHINE}.${INITRAMFS_FSTYPES}"

# MMC or UFS
MMC_UBOOT_OFFSET = "0"
WIC_IMAGE_NAME = "${IMAGE_BASE_NAME}-${MACHINE}.wic.xz"
USER_DATA_IMAGE_NAME = "${IMAGE_BASE_NAME}-${MACHINE}.bin"
USER_DATA_BOOTPART_IMAGE_NAME = "boot-image.ext4"

# Keys and Configs
UBOOT_SIGN_KEYDIR = "${STAGING_DATADIR_NATIVE}/aspeed-secure-config/ast2700/keys"
SOCSEC_SIGN_HELPER = "${STAGING_DATADIR_NATIVE}/aspeed-secure-config/signing_helper.sh"
OTPTOOL_KEY_DIR = "${STAGING_DATADIR_NATIVE}/aspeed-secure-config/ast2700/keys"
OTPTOOL_CONFIGS_DIR = "${STAGING_DATADIR_NATIVE}/aspeed-secure-config/ast2700/otp"
OTPTOOL_SOC = "2700"
FMC_KEY_DIR = "${OTPTOOL_KEY_DIR}"

# Caliptra manifest
CALIPTRA_MANIFEST_CONFIG_DIR = "${STAGING_DATADIR_NATIVE}/aspeed-secure-config/ast2700/caliptra"
CALIPTRA_MANIFEST_KEY_DIR = "${STAGING_DATADIR_NATIVE}/aspeed-secure-config/ast2700/keys"
CALIPTRA_MANIFEST_FLASH_IMAGE = "ast2700-manifest-flash.bin"
CALIPTRA_MANIFEST_RECOVERY_IMAGE = "ast2700-soc-manifest.bin"
CALIPTRA_MANIFEST_BINARY = "${CALIPTRA_MANIFEST_FLASH_IMAGE}"

install_unsigned_image() {
    install -d ${S}/${GEN_IMAGE_MODE}
    install -d ${S}/${GEN_IMAGE_MODE}/arch
    install -d ${S}/${GEN_IMAGE_MODE}/arch/arm64
    install -d ${S}/${GEN_IMAGE_MODE}/arch/arm64/boot
    install -d ${S}/${GEN_IMAGE_MODE}/arch/arm64/boot/dts
    install -d ${S}/${GEN_IMAGE_MODE}/arch/arm64/boot/dts/aspeed
    install -d ${DEPLOYDIR}
    install -d ${DEPLOYDIR}/${GEN_IMAGE_MODE}

    # u-boot unsigned image and dtb
    install -m 0644 ${STAGING_DIR_HOST}/sysroot-only/u-boot* ${S}/${GEN_IMAGE_MODE}

    # kernel unsigned image, dtb and its
    install -m 0644 ${DEPLOY_DIR_IMAGE}/${KERNEL_FITIMAGE_ITS_NAME} ${S}/${GEN_IMAGE_MODE}
    install -m 0644 ${DEPLOY_DIR_IMAGE}/fitImage-linux.bin-${MACHINE} ${S}/${GEN_IMAGE_MODE}
    install -m 0644 ${DEPLOY_DIR_IMAGE}/fitImage-linux.bin-${MACHINE} ${S}/${GEN_IMAGE_MODE}/linux.bin
    for kernel_dtb in ${KERNEL_DEVICETREE}; do
        kernel_dtb_basename=$(basename ${kernel_dtb})
        install -m 0644 ${DEPLOY_DIR_IMAGE}/${kernel_dtb_basename} ${S}/${GEN_IMAGE_MODE}
        install -m 0644 ${DEPLOY_DIR_IMAGE}/${kernel_dtb_basename} ${S}/${GEN_IMAGE_MODE}/arch/arm64/boot/dts/aspeed
    done
}

make_otp_image() {
    otptool_config="${OTPTOOL_CONFIGS_DIR}/${OTPTOOL_JSON}"
    otptool_config_slug="$(basename ${otptool_config} .json)"
    otptool_config_outdir="${S}/${GEN_IMAGE_MODE}/${otptool_config_slug}"
    local otptool_user_folder=""

    if [ -n "${OTPTOOL_USER_DIR}" ]; then
        otptool_user_folder="--user_data_folder ${OTPTOOL_USER_DIR}"
    fi

    echo "otptool_config=${otptool_config}"
    echo "otptool_user_folder=${otptool_user_folder}"
    echo "otptool_key_dir=${OTPTOOL_KEY_DIR}"
    echo "otptool_extra_opts=${OTPTOOL_EXTRA_OPTS}"

    mkdir -p "${otptool_config_outdir}"
    otptool make_otp_image \
        --key_folder ${OTPTOOL_KEY_DIR} \
        --output_folder "${otptool_config_outdir}" \
        ${otptool_user_folder} \
        ${otptool_config} \
        ${OTPTOOL_EXTRA_OPTS}

    if [ $? -ne 0 ]; then
        bbfatal "Generated OTP image failed."
    fi

    otptool print --soc ${OTPTOOL_SOC} "${otptool_config_outdir}"/otp-all.image

    if [ $? -ne 0 ]; then
        bbfatal "Printed OTP image failed."
    fi
}

# export CRYPTOGRAPHY_OPENSSL_NO_LEGACY variable to fix the following errors.
# OpenSSL 3.0 legacy provider failed to load
# https://github.com/pyca/cryptography/issues/10598
make_fmc_image_and_sign() {
    export CRYPTOGRAPHY_OPENSSL_NO_LEGACY=1

    local ecc_key=""
    local ecc_key_index=""
    local lms_key=""
    local lms_key_index=""
    local sign_args=""

    if [ -f "${FMC_KEY_DIR}/${ROT_ECC_KEY_NAME}" ]; then
        ecc_key="--ecc-key ${FMC_KEY_DIR}/${ROT_ECC_KEY_NAME}"
    fi

    if [ -n "${ROT_ECC_KEY_INDEX}" ]; then
        ecc_key_index="--ecc-key-index ${ROT_ECC_KEY_INDEX}"
    fi

    if [ -f "${FMC_KEY_DIR}/${ROT_LMS_KEY_NAME}" ]; then
        lms_key="--lms-key ${FMC_KEY_DIR}/${ROT_LMS_KEY_NAME}"
    fi

    if [ -n "${ROT_LMS_KEY_INDEX}" ]; then
        lms_key_index="--lms-key-index ${ROT_LMS_KEY_INDEX}"
    fi

    sign_args="${ecc_key} ${ecc_key_index} ${lms_key} ${lms_key_index}"
    echo "sign_args=${sign_args}"

    fmc-imgtool \
        --verbose \
        --version 2 \
        --input ${DEPLOY_DIR_IMAGE}/${MCU_RUNTIME_IMAGE} \
        --output ${S}/${GEN_IMAGE_MODE}/${BOOTMCU_FW_BINARY} \
        --prebuilt-dir ${DEPLOY_DIR_IMAGE}/ \
        ${sign_args}
}

make_kernel_fitimage_and_sign() {
    cd ${S}/${GEN_IMAGE_MODE}

    # Assemble the kernel image
    uboot-mkimage -f ${KERNEL_FITIMAGE_ITS_NAME} ${KERNEL_FITIMAGE_NAME}
    # Sign the Kernel FIT image and add public key to U-Boot dtb
    uboot-mkimage -F -k ${UBOOT_SIGN_KEYDIR} -K "u-boot.dtb" -r ${KERNEL_FITIMAGE_NAME}
    # Verify kernel fitImage
    uboot-fit_check_sign -f ${KERNEL_FITIMAGE_NAME} -k u-boot.dtb
    if [ $? -ne 0 ]; then
        bbfatal "Verified kernel fitImage failed."
    fi

    # concat u-boot-nodtb and u-boot-dtb
    cat u-boot-nodtb.bin u-boot.dtb > ${UBOOT_IMAGE_NAME}

    rm -rf ${S}/${GEN_IMAGE_MODE}/arch
    rm -f ${S}/${GEN_IMAGE_MODE}/linux.bin

    cd ${S}
}

make_caliptra_manifest_image_and_sign() {
    export RUST_LOG="debug"

    local caliptra_manifest_key_dir=""

    if [ -n "${CALIPTRA_MANIFEST_KEY_DIR}" ]; then
        caliptra_manifest_key_dir="--key-dir ${CALIPTRA_MANIFEST_KEY_DIR}/"
    fi

    echo "caliptra_manifest_key_dir=${caliptra_manifest_key_dir}"

    # Run cptra-imgtool to generate manifest flash image.
    cptra-imgtool \
        create-auth-flash \
        --cfg ${CALIPTRA_MANIFEST_CONFIG_DIR}/${CALIPTRA_MANIFEST_CONFIG} \
        ${caliptra_manifest_key_dir} \
        --prebuilt-dir ${DEPLOY_DIR_IMAGE}/ \
        --flash ${S}/${GEN_IMAGE_MODE}/${CALIPTRA_MANIFEST_FLASH_IMAGE}

    # Run cptra-imgtool to generate manifest image for recovery.
    cptra-imgtool \
        create-auth-man \
        --cfg ${CALIPTRA_MANIFEST_CONFIG_DIR}/${CALIPTRA_MANIFEST_CONFIG} \
        ${caliptra_manifest_key_dir} \
        --prebuilt-dir ${DEPLOY_DIR_IMAGE}/ \
        --man ${S}/${GEN_IMAGE_MODE}/${CALIPTRA_MANIFEST_RECOVERY_IMAGE}

    rm -f ${S}/${GEN_IMAGE_MODE}/caliptra-manifest.toml
    rm -f ${S}/${GEN_IMAGE_MODE}/default_project-auth-manifest.bin
    rm -f ${S}/${GEN_IMAGE_MODE}/svn_sig.bin
}

make_recovery_image() {
    install -m 644 ${DEPLOY_DIR_IMAGE}/recovery_${CALIPTRA_FW_BINARY} ${S}/${GEN_IMAGE_MODE}/
    python3 ${STAGING_BINDIR_NATIVE}/recovery_spl_extraction.py -i ${S}/${GEN_IMAGE_MODE}/${BOOTMCU_FW_BINARY}
}

make_boot_partition_ext4() {
    # Generate a compressed ext4 filesystem with the fitImage file in it to be
    # flashed to the user data area at boot partition of the eMMC

    block_size_command=""
    if [ "${ASPEED_BOOT_UFS}" = "yes" ]; then
        block_size_command="-b 4096"
    fi

    echo "block_size_command=${block_size_command}"

    cd ${S}/${GEN_IMAGE_MODE}
    install -d boot-image
    install -m 0644 ${KERNEL_FITIMAGE_NAME} boot-image/fitImage

    mkfs.ext4 -F ${block_size_command} -i 4096 -d boot-image ${USER_DATA_BOOTPART_IMAGE_NAME}
    # Error codes 0-3 indicate successfull operation of fsck
    fsck.ext4 -pvfD ${USER_DATA_BOOTPART_IMAGE_NAME} || [ $? -le 3 ]
    cd ${S}
}

deploy_static_image_helper() {
    otptool_config_slug="$(basename ${OTPTOOL_JSON} .json)"

    install -m 0644 ${DEPLOY_DIR_IMAGE}/image-rofs ${DEPLOYDIR}/${GEN_IMAGE_MODE}
    install -m 0644 ${DEPLOY_DIR_IMAGE}/image-rwfs ${DEPLOYDIR}/${GEN_IMAGE_MODE}
    install -m 0644 ${DEPLOY_DIR_IMAGE}/${INITRAMFS_IMAGE_NAME} ${DEPLOYDIR}/${GEN_IMAGE_MODE}
    install -m 0644 ${S}/${GEN_IMAGE_MODE}/*.* ${DEPLOYDIR}/${GEN_IMAGE_MODE}
    install -m 0644 ${S}/${GEN_IMAGE_MODE}/fitImage* ${DEPLOYDIR}/${GEN_IMAGE_MODE}
    install -m 0644 ${S}/${GEN_IMAGE_MODE}/${otptool_config_slug}/otp-all.image ${DEPLOYDIR}/${GEN_IMAGE_MODE}/${otptool_config_slug}-otp-all.image
    install -m 0644 ${DEPLOYDIR}/${GEN_IMAGE_MODE}/${KERNEL_FITIMAGE_NAME} ${DEPLOYDIR}/${GEN_IMAGE_MODE}/image-kernel

    # u-boot-env
    if [ -f ${DEPLOY_DIR_IMAGE}/u-boot-env.bin ]; then
        install -m 0644 ${DEPLOY_DIR_IMAGE}/u-boot-env.bin ${DEPLOYDIR}/${GEN_IMAGE_MODE}
    fi

    # trusted-firmware-a
    install -m 0644 ${DEPLOY_DIR_IMAGE}/bl31.* ${DEPLOYDIR}/${GEN_IMAGE_MODE}

    # optee-os
    if [ -f ${DEPLOY_DIR_IMAGE}/optee/tee-raw.bin ]; then
        cp --no-preserve=ownership -rf ${DEPLOY_DIR_IMAGE}/optee ${DEPLOYDIR}/${GEN_IMAGE_MODE}
    fi

    # mcu run-time
    install -m 0644 ${DEPLOY_DIR_IMAGE}/zephyr-* ${DEPLOYDIR}/${GEN_IMAGE_MODE}

    # caliptra
    install -m 0644 ${DEPLOY_DIR_IMAGE}/${CALIPTRA_FW_BINARY} ${DEPLOYDIR}/${GEN_IMAGE_MODE}

    # ssp
    if [ -f "${SSP_IMAGE}" ]; then
        install -m 0644 ${SSP_IMAGE} ${DEPLOYDIR}/${GEN_IMAGE_MODE}
    fi

    # tsp
    if [ -f "${TSP_IMAGE}" ]; then
        install -m 0644 ${TSP_IMAGE} ${DEPLOYDIR}/${GEN_IMAGE_MODE}
    fi
}

deploy_mmc_image_helper() {
    otptool_config_slug="$(basename ${OTPTOOL_JSON} .json)"

    install -m 0644 ${DEPLOY_DIR_IMAGE}/${IMAGE_BASE_NAME}-${MACHINE}.ext4 ${DEPLOYDIR}/${GEN_IMAGE_MODE}
    install -m 0644 ${DEPLOY_DIR_IMAGE}/${IMAGE_BASE_NAME}-${MACHINE}.rwfs.ext4 ${DEPLOYDIR}/${GEN_IMAGE_MODE}
    install -m 0644 ${DEPLOY_DIR_IMAGE}/${INITRAMFS_IMAGE_NAME} ${DEPLOYDIR}/${GEN_IMAGE_MODE}
    install -m 0644 ${S}/${GEN_IMAGE_MODE}/*.* ${DEPLOYDIR}/${GEN_IMAGE_MODE}
    install -m 0644 ${S}/${GEN_IMAGE_MODE}/fitImage* ${DEPLOYDIR}/${GEN_IMAGE_MODE}
    install -m 0644 ${S}/${GEN_IMAGE_MODE}/${otptool_config_slug}/otp-all.image ${DEPLOYDIR}/${GEN_IMAGE_MODE}/${otptool_config_slug}-otp-all.image

    # u-boot-env
    if [ -f ${DEPLOY_DIR_IMAGE}/u-boot-env.bin ]; then
        install -m 0644 ${DEPLOY_DIR_IMAGE}/u-boot-env.bin ${DEPLOYDIR}/${GEN_IMAGE_MODE}
    fi

    # trusted-firmware-a
    install -m 0644 ${DEPLOY_DIR_IMAGE}/bl31.* ${DEPLOYDIR}/${GEN_IMAGE_MODE}

    # optee-os
    if [ -f ${DEPLOY_DIR_IMAGE}/optee/tee-raw.bin ]; then
        cp --no-preserve=ownership -rf ${DEPLOY_DIR_IMAGE}/optee ${DEPLOYDIR}/${GEN_IMAGE_MODE}
    fi

    # mcu run-time
    install -m 0644 ${DEPLOY_DIR_IMAGE}/zephyr-* ${DEPLOYDIR}/${GEN_IMAGE_MODE}

    # caliptra
    install -m 0644 ${DEPLOY_DIR_IMAGE}/${CALIPTRA_FW_BINARY} ${DEPLOYDIR}/${GEN_IMAGE_MODE}

    # ssp
    if [ -f "${SSP_IMAGE}" ]; then
        install -m 0644 ${SSP_IMAGE} ${DEPLOYDIR}/${GEN_IMAGE_MODE}
    fi

    # tsp
    if [ -f "${TSP_IMAGE}" ]; then
        install -m 0644 ${TSP_IMAGE} ${DEPLOYDIR}/${GEN_IMAGE_MODE}
    fi

    # decompress wic image for user data area boot partition update
    xz -cd ${DEPLOY_DIR_IMAGE}/${WIC_IMAGE_NAME} > ${S}/${GEN_IMAGE_MODE}/${USER_DATA_IMAGE_NAME}
}


def make_empty_image_zeros(img, size_kb):
    size = int(size_kb) * 1024
    with open(img, "wb+") as fp:
        fp.seek(0)
        fp.write(b'\x00'*size)


def make_empty_image(img, size_kb):
    size = int(size_kb) * 1024
    with open(img, "wb+") as fp:
        fp.seek(0)
        fp.write(b'\xFF'*size)


def update_its_file(file_path, oldstr, newstr):
    with open(file_path, 'r') as fp:
        file_contents = fp.read()
    new_contents = file_contents.replace(oldstr, newstr)
    with open(file_path, 'w') as fp:
        fp.write(new_contents)


def add_or_update_signature_nodes(its_path, target, algo, key_hint, padding=None):
    """
    Add or update signature blocks in either 'images' or
    'configurations' sections of a FIT ITS file.

    Behavior:
    - target == "images":
        Add a simple signature with algo and key-name-hint.
    - target == "configurations":
        Add a full signature with algo, key-name-hint, optional padding,
        and sign-images parsed from kernel/fdt/ramdisk/loadables.
    - Existing signatures are updated, not duplicated.
    - Nodes starting with "hash" are skipped.
    """
    from pathlib import Path
    import sys
    import re

    path = Path(its_path)
    lines = path.read_text().splitlines()
    out_lines = []
    stack = []
    inside_signature = False

    # ----- Helper: return the current node in stack -----
    def current_node():
        """Return the most recent node from the parsing stack."""
        for s in reversed(stack):
            if s["type"] == "node":
                return s
        return None

    # ----- Helper: check if inside a given section -----
    def in_section(name):
        """Return True if currently inside the given section."""
        return any(
            s["type"] == "section" and s["name"] == name for s in stack
        )

    # ----- Helper: normalize image names for sign-images -----
    def normalize_image_name(name):
        """
        Clean image names:
        - strip SoC/board suffixes (e.g. -aspeed-...),
        - strip trailing -<digits>,
        - strip common extensions (.dtb, .bin, .img).
        """
        name = re.sub(r"-aspeed.*", "", name)
        name = re.sub(r"-[0-9]+$", "", name)
        name = re.sub(r"\.dtb$|\.bin$|\.img$", "", name)
        return name

    # ----- Helper: extract image refs from a configuration node -----
    def extract_images_from_conf(node_lines):
        """Parse kernel/fdt/ramdisk/loadables and normalize names."""
        content = "\n".join(node_lines)
        names = set()
        for field in ["loadables", "kernel", "fdt", "ramdisk"]:
            matches = re.findall(rf'{field}\s*=\s*"([^"]+)"', content)
            for m in matches:
                for n in re.split(r"\s*,\s*", m):
                    n = n.strip()
                    if not n:
                        continue
                    names.add(normalize_image_name(n))
        # Force canonical keys if present in any form
        canon = set()
        for n in names:
            if n.startswith("kernel"):
                canon.add("kernel")
            elif n.startswith("fdt"):
                canon.add("fdt")
            elif n.startswith("ramdisk"):
                canon.add("ramdisk")
            else:
                canon.add(n)
        # Prefer the common trio ordering when present
        ordered = []
        for k in ["kernel", "fdt", "ramdisk"]:
            if k in canon:
                ordered.append(k)
        for x in sorted(canon):
            if x not in ("kernel", "fdt", "ramdisk"):
                ordered.append(x)
        return ordered

    # ----- Parse ITS file line by line -----
    for line in lines:
        stripped = line.strip()

        # --- Section start: images { ... } or configurations { ... } ---
        if stripped.startswith("images") and "{" in stripped:
            stack.append({"type": "section", "name": "images"})
            out_lines.append(line)
            continue
        if stripped.startswith("configurations") and "{" in stripped:
            stack.append({"type": "section", "name": "configurations"})
            out_lines.append(line)
            continue

        # --- Node start (e.g. uboot { / conf {) ---
        if (
            "{" in stripped
            and not stripped.startswith("{")
            and not stripped.startswith("signature")
        ):
            name = stripped.split("{", 1)[0].strip()
            parent = stack[-1] if stack else None
            parent_is_section = parent and parent["type"] == "section"
            stack.append({
                "type": "node",
                "name": name,
                "has_signature": False,
                "lines": [],
                "parent_is_section": bool(parent_is_section),
                "parent_section": parent["name"]
                if parent_is_section else None
            })
            out_lines.append(line)
            continue

        # --- Entering a signature block ---
        if stripped.startswith("signature") and "{" in stripped:
            node = current_node()
            valid_top = (
                node and node.get("parent_is_section")
                and not node["name"].lower().startswith("hash")
            )
            if valid_top:
                node["has_signature"] = True
                inside_signature = True
                stack.append({"type": "signature"})
                out_lines.append(line)
                continue
            # Ignore nested signatures (e.g. inside hash nodes)
            out_lines.append(line)
            continue

        # --- Inside signature block: update fields ---
        if inside_signature:
            node = current_node()
            valid_top = (
                node and node.get("parent_is_section")
                and not node["name"].lower().startswith("hash")
            )
            if not valid_top:
                inside_signature = False
                out_lines.append(line)
                continue

            if stripped.startswith("algo"):
                indent = line[:line.find("a")]
                line = f'{indent}algo = "{algo}";'
            elif stripped.startswith("key-name-hint"):
                indent = line[:line.find("k")]
                line = f'{indent}key-name-hint = "{key_hint}";'
            elif stripped.startswith("padding") and padding:
                indent = line[:line.find("p")]
                line = f'{indent}padding = "{padding}";'
            elif stripped.startswith("padding") and not padding:
                # Skip padding line if user did not request it
                continue

            if "}" in stripped:
                if stack and stack[-1]["type"] == "signature":
                    stack.pop()
                inside_signature = False

            out_lines.append(line)
            continue

        # --- Closing brace ("}") ---
        if stripped.startswith("}"):
            if stack:
                top = stack[-1]
                if top["type"] == "node":
                    node = top
                    node_lines = node.get("lines", [])
                    in_images = in_section("images")
                    in_configs = in_section("configurations")

                    direct_child = bool(node.get("parent_is_section"))
                    is_hash_like = node["name"].lower().startswith("hash")

                    if (
                        direct_child and not is_hash_like
                        and not node["has_signature"]
                    ):
                        if target == "images" and in_images:
                            indent = " " * 12
                            out_lines.extend([
                                f"{indent}signature {{",
                                f"{indent}    algo = \"{algo}\";",
                                f"{indent}    key-name-hint = "
                                f"\"{key_hint}\";",
                                f"{indent}}};"
                            ])
                        elif target == "configurations" and in_configs:
                            names = extract_images_from_conf(node_lines)
                            joined = (
                                ", ".join(f"\"{n}\"" for n in names)
                                if names else ""
                            )
                            indent = " " * 24
                            out_lines.append(f"{indent}signature-1 {{")
                            out_lines.append(
                                f"{indent}    algo = \"{algo}\";"
                            )
                            out_lines.append(
                                f"{indent}    key-name-hint = "
                                f"\"{key_hint}\";"
                            )
                            if padding:
                                out_lines.append(
                                    f"{indent}    padding = "
                                    f"\"{padding}\";"
                                )
                            if joined:
                                out_lines.append(
                                    f"{indent}    sign-images = {joined};"
                                )
                            out_lines.append(f"{indent}}};")

                    # Pop the node from stack
                    stack.pop()
                elif top["type"] in ("section", "signature"):
                    stack.pop()
                inside_signature = False

            out_lines.append(line)
            continue

        # --- Default: copy line and record node content ---
        out_lines.append(line)
        node = current_node()
        if node:
            node["lines"].append(line)

    # Overwrite the original file
    path.write_text("\n".join(out_lines))


def update_hash_algo(its_path, new_algo):
    """
    Safely update the 'algo' value inside all 'hash-*' nodes
    in an ITS file (line-by-line, no regex, in-place update).

    Args:
        its_path (str | Path): Path to the .its file.
        new_algo (str): The new algorithm name to replace, e.g. "sha512".

    Behavior:
        - Reads the ITS file line-by-line.
        - Detects when entering and leaving a 'hash-*' block.
        - Replaces any line starting with 'algo =' inside that block.
        - Writes the result directly back to the same file.

    Example:
        update_hash_algo("kernel.its", "sha512")
    """
    from pathlib import Path

    path = Path(its_path)
    lines = path.read_text().splitlines()
    out_lines = []

    inside_hash_block = False
    modified_count = 0

    for line in lines:
        stripped = line.strip()

        # Detect entering a hash-* node
        if "{" in stripped and stripped.startswith("hash-"):
            inside_hash_block = True
            out_lines.append(line)
            continue

        # Detect leaving a hash-* node
        if stripped.startswith("}"):
            if inside_hash_block:
                inside_hash_block = False
            out_lines.append(line)
            continue

        # Replace algo line only inside hash-* node
        if inside_hash_block and stripped.startswith("algo"):
            indent = line[:line.find("a")]
            line = f'{indent}algo = "{new_algo}";'
            modified_count += 1

        out_lines.append(line)

    # Overwrite the original file
    path.write_text("\n".join(out_lines))


def append_image(inimg, outimg, start_kb, finish_kb):
    import subprocess
    imgsize = os.path.getsize(inimg)
    maxsize = (finish_kb - start_kb) * 1024
    print(flush=True)
    bb.debug(1, 'Considering file size=' + str(imgsize) + ' name=' + inimg)
    bb.debug(1, 'Spanning start=' + str(start_kb) + 'K end=' + str(finish_kb) + 'K')
    bb.debug(1, 'Compare needed=' + str(imgsize) + ' available=' + str(maxsize) + ' margin=' + str(maxsize - imgsize))
    if imgsize > maxsize:
        bb.fatal("Image '%s' is too large!" % inimg)

    cmd = "dd bs=1k conv=notrunc seek=%d if=%s of=%s" % (start_kb, inimg, outimg)
    print(cmd)
    subprocess.check_call(cmd, shell=True)


def deploy_static_image(d):
    bb.build.exec_func("deploy_static_image_helper", d)
    gen_img = d.getVar('GEN_IMAGE_MODE', True)

    # image-bmc
    nor_img = os.path.join(d.getVar('DEPLOYDIR', True), gen_img, "image-bmc")
    make_empty_image(nor_img, d.getVar('FLASH_SIZE', True))

    uboot_offset = int(d.getVar('FLASH_UBOOT_OFFSET', True))
    caliptra_end_offset = uboot_offset + int(d.getVar('FLASH_CALIPTRA_SIZE', True))
    append_image(os.path.join(d.getVar('DEPLOYDIR', True), gen_img, d.getVar('CALIPTRA_FW_BINARY', True)),
                 nor_img,
                 uboot_offset,
                 caliptra_end_offset)

    uboot_offset = caliptra_end_offset
    bootmcu_end_offset = uboot_offset + int(d.getVar('FLASH_BMCU_SIZE', True))
    append_image(os.path.join(d.getVar('DEPLOYDIR', True), gen_img, d.getVar('BOOTMCU_FW_BINARY', True)),
                 nor_img,
                 uboot_offset,
                 bootmcu_end_offset)

    uboot_offset = bootmcu_end_offset
    append_image(os.path.join(d.getVar('DEPLOYDIR', True), gen_img, d.getVar('CALIPTRA_MANIFEST_BINARY', True)),
                 nor_img,
                 uboot_offset,
                 int(d.getVar('FLASH_UBOOT_ENV_OFFSET', True)))

    append_image(os.path.join(d.getVar('DEPLOYDIR', True), gen_img, "image-kernel"),
                 nor_img,
                 int(d.getVar('FLASH_KERNEL_OFFSET', True)),
                 int(d.getVar('FLASH_ROFS_OFFSET', True)))

    append_image(os.path.join(d.getVar('DEPLOYDIR', True), gen_img, "image-rofs"),
                 nor_img,
                 int(d.getVar('FLASH_ROFS_OFFSET', True)),
                 int(d.getVar('FLASH_RWFS_OFFSET', True)))

    append_image(os.path.join(d.getVar('DEPLOYDIR', True), gen_img, "image-rwfs"),
                 nor_img,
                 int(d.getVar('FLASH_RWFS_OFFSET', True)),
                 int(d.getVar('FLASH_SIZE', True)))

    # image-u-boot
    uboot_img = os.path.join(d.getVar('DEPLOYDIR', True), gen_img, "image-u-boot")
    make_empty_image(uboot_img, d.getVar('FLASH_UBOOT_ENV_OFFSET', True))

    uboot_offset = int(d.getVar('FLASH_UBOOT_OFFSET', True))
    caliptra_end_offset = uboot_offset + int(d.getVar('FLASH_CALIPTRA_SIZE', True))
    append_image(os.path.join(d.getVar('DEPLOYDIR', True), gen_img, d.getVar('CALIPTRA_FW_BINARY', True)),
                 uboot_img,
                 uboot_offset,
                 caliptra_end_offset)

    uboot_offset = caliptra_end_offset
    bootmcu_end_offset = uboot_offset + int(d.getVar('FLASH_BMCU_SIZE', True))
    append_image(os.path.join(d.getVar('DEPLOYDIR', True), gen_img, d.getVar('BOOTMCU_FW_BINARY', True)),
                 uboot_img,
                 uboot_offset,
                 bootmcu_end_offset)

    uboot_offset = bootmcu_end_offset
    append_image(os.path.join(d.getVar('DEPLOYDIR', True), gen_img, d.getVar('CALIPTRA_MANIFEST_BINARY', True)),
                 uboot_img,
                 uboot_offset,
                 int(d.getVar('FLASH_UBOOT_ENV_OFFSET', True)))


def deploy_mmc_image(d):
    import subprocess

    gen_img = d.getVar('GEN_IMAGE_MODE', True)
    user_data_image = os.path.join(d.getVar('S', True), gen_img, d.getVar('USER_DATA_IMAGE_NAME', True))
    user_data_bootpart_image = os.path.join(d.getVar('S', True), gen_img, d.getVar('USER_DATA_BOOTPART_IMAGE_NAME', True))
    make_empty_image_zeros(user_data_bootpart_image, d.getVar('MMC_BOOT_PARTITION_SIZE', True))
    bb.build.exec_func("make_boot_partition_ext4", d)
    bb.build.exec_func("deploy_mmc_image_helper", d)

    # get partition offset from user data area image
    # eMMC sector size is 512 bytes
    # UFS sector size is 4096 bytes
    aspeed_boot_ufs = d.getVar('ASPEED_BOOT_UFS', True)
    if aspeed_boot_ufs == "yes":
        sector_size = 4096
    else:
        sector_size = 512

    print("sector_size=%d" % (sector_size))

    # boot-a
    cmd = "PARTED_SECTOR_SIZE=%d parted -s %s unit B print | grep 'boot-a'" % (sector_size, user_data_image)
    print("Get boot-a partition information...")
    print(cmd)
    boot_a_out = subprocess.check_output(cmd, shell=True, text=True)
    print(boot_a_out)
    boot_a_offset_kb = int(boot_a_out.split()[1].rstrip("B")) // 1024
    print("boot_a_offset_kb=%d" % (boot_a_offset_kb))

    # boot-b
    cmd = "PARTED_SECTOR_SIZE=%d parted -s %s unit B print | grep 'boot-b'" % (sector_size, user_data_image)
    print("Get boot-b partition information...")
    print(cmd)
    boot_b_out = subprocess.check_output(cmd, shell=True, text=True)
    print(boot_b_out)
    boot_b_offset_kb = int(boot_b_out.split()[1].rstrip("B")) // 1024
    print("boot_b_offset_kb=%d" % (boot_b_offset_kb))

    # rofs-a
    cmd = "PARTED_SECTOR_SIZE=%d parted -s %s unit B print | grep 'rofs-a'" % (sector_size, user_data_image)
    print("Get rofs-a partition information...")
    print(cmd)
    rofs_a_out = subprocess.check_output(cmd, shell=True, text=True)
    print(rofs_a_out)
    rofs_a_offset_kb = int(rofs_a_out.split()[1].rstrip("B")) // 1024
    print("rofs_a_offset_kb=%d" % (rofs_a_offset_kb))

    # update boot partition in user data area image
    append_image(user_data_bootpart_image, user_data_image, boot_a_offset_kb, boot_b_offset_kb)
    append_image(user_data_bootpart_image, user_data_image, boot_b_offset_kb, rofs_a_offset_kb)

    # compress user data image and deploy
    deploy_wic_image = os.path.join(d.getVar('DEPLOYDIR', True), gen_img, d.getVar('WIC_IMAGE_NAME', True))
    cmd = "xz -f -k -c -9 {} --check=crc32 {} > {}".format(d.getVar('XZ_DEFAULTS', True),
                                                           user_data_image,
                                                           deploy_wic_image)
    print(cmd)
    subprocess.check_call(cmd, shell=True)

    # image-u-boot for Boot Area Partition 1 and 2
    mmc_boot_img = os.path.join(d.getVar('DEPLOYDIR', True), gen_img, "image-u-boot")
    make_empty_image(mmc_boot_img, d.getVar('MMC_UBOOT_SIZE', True))

    uboot_offset = int(d.getVar('MMC_UBOOT_OFFSET', True))
    caliptra_end_offset = uboot_offset + int(d.getVar('FLASH_CALIPTRA_SIZE', True))
    append_image(os.path.join(d.getVar('DEPLOYDIR', True), gen_img, d.getVar('CALIPTRA_FW_BINARY', True)),
                 mmc_boot_img,
                 uboot_offset,
                 caliptra_end_offset)

    uboot_offset = caliptra_end_offset
    bootmcu_end_offset = uboot_offset + int(d.getVar('FLASH_BMCU_SIZE', True))
    append_image(os.path.join(d.getVar('DEPLOYDIR', True), gen_img, d.getVar('BOOTMCU_FW_BINARY', True)),
                 mmc_boot_img,
                 uboot_offset,
                 bootmcu_end_offset)

    uboot_offset = bootmcu_end_offset
    append_image(os.path.join(d.getVar('DEPLOYDIR', True), gen_img, d.getVar('CALIPTRA_MANIFEST_BINARY', True)),
                 mmc_boot_img,
                 uboot_offset,
                 int(d.getVar('MMC_UBOOT_SIZE', True)))


def create_irot_image(d):
    import subprocess

    gen_img = d.getVar('GEN_IMAGE_MODE', True)
    irot_boot_img = os.path.join(d.getVar('S', True), gen_img, 'irot_boot_img')
    make_empty_image(irot_boot_img, d.getVar('IROT_IMAGE_SIZE', True))

    # Caliptra manifest
    append_image(os.path.join(d.getVar('S', True), gen_img, d.getVar('CALIPTRA_MANIFEST_FLASH_IMAGE', True)),
                 irot_boot_img,
                 int(d.getVar('IROT_OFFSET_MANIFEST', True)),
                 int(d.getVar('IROT_OFFSET_ATF', True)))
    # ATF
    append_image(d.getVar('UBOOT_FIT_ARM_TRUSTED_FIRMWARE_IMAGE', True),
                 irot_boot_img,
                 int(d.getVar('IROT_OFFSET_ATF', True)),
                 int(d.getVar('IROT_OFFSET_UBOOT', True)))
    # U-Boot raw image
    append_image(os.path.join(d.getVar('S', True), gen_img, d.getVar('UBOOT_IMAGE_NAME', True)),
                 irot_boot_img,
                 int(d.getVar('IROT_OFFSET_UBOOT', True)),
                 int(d.getVar('IROT_OFFSET_TEE', True)))
    # TEE
    append_image(d.getVar('UBOOT_FIT_TEE_IMAGE', True),
                 irot_boot_img,
                 int(d.getVar('IROT_OFFSET_TEE', True)),
                 int(d.getVar('IROT_IMAGE_SIZE', True)))

    cmd = "rm -f {}".format(os.path.join(d.getVar('S', True), gen_img, d.getVar('CALIPTRA_MANIFEST_FLASH_IMAGE', True)))
    print(cmd)
    subprocess.check_call(cmd, shell=True)

    cmd = "mv {} {}".format(irot_boot_img,
                            os.path.join(d.getVar('S', True), gen_img, d.getVar('CALIPTRA_MANIFEST_FLASH_IMAGE', True)))
    print(cmd)
    subprocess.check_call(cmd, shell=True)


def verify_uboot_kernel_image_status(d):
    uboot_fitimage_enable = d.getVar('UBOOT_FITIMAGE_ENABLE', True)
    if uboot_fitimage_enable == "1":
        bb.fatal("Not support Bootloader FIT image")
    kernel_imagetype = d.getVar('KERNEL_IMAGETYPE', True)
    if "fitImage" not in kernel_imagetype:
        bb.fatal("Only support Kernel FIT image")


python do_deploy() {
    secure_image_list = [
        {
            "mode": "ecdsa384",
            "otptool_json": "2700A1_ECDSA384.json",
            "rot_ecc_key_name" : "test_oem_dss_private_key_ecdsa384_1.pem",
            "rot_ecc_key_index" : "1",
            "rot_lms_key_name" : "",
            "rot_lms_key_index" : "",
            "cot_kernel_algo": "ecdsa384",
            "cot_kernel_hash": "sha384",
            "cot_uboot_sign_key_name": "test_bl3_ecdsa_secp384r1",
            "caliptra_manifest_config": "ast2700a1-default-ecc-manifest.toml",
            "caliptra_manifest_config_irot": "ast2700a1-irot-ecc-manifest.toml"
        },
        {
            "mode": "ecdsa384-lms",
            "otptool_json": "2700A1_ECDSA384_LMS.json",
            "rot_ecc_key_name" : "test_oem_dss_private_key_ecdsa384_1.pem",
            "rot_ecc_key_index" : "1",
            "rot_lms_key_name" : "test_oem_dss_lms_key_1.prv",
            "rot_lms_key_index" : "1",
            "cot_kernel_algo": "ecdsa384",
            "cot_kernel_hash": "sha384",
            "cot_uboot_sign_key_name": "test_bl3_ecdsa_secp384r1",
            "caliptra_manifest_config": "ast2700a1-default-ecc-lms-manifest.toml",
            "caliptra_manifest_config_irot": "ast2700a1-irot-ecc-lms-manifest.toml"
        }
    ]

    gen_secure_image_enable = d.getVar('ASPEED_CUSTOMIZE_GEN_SECURE_IMAGE_ENABLE', True)
    if gen_secure_image_enable != "1":
        print("Disable gen secure image. Do nothing.")
        return

    verify_uboot_kernel_image_status(d)
    gen_secure_image = d.getVar('ASPEED_CUSTOMIZE_GEN_SECURE_IMAGE', True)
    aspeed_boot_emmc_ufs = d.getVar('ASPEED_BOOT_EMMC_UFS', True)
    aspeed_irot = d.getVar('ASPEED_IROT', True)

    for gen_img in gen_secure_image.split():
        for sec_img in secure_image_list:
            if gen_img == sec_img["mode"]:
                break
        else:
          bb.fatal("%s mode not support" % gen_img)

        print("Start %s image..." % gen_img)
        d.setVar('GEN_IMAGE_MODE', gen_img)
        d.setVar('OTPTOOL_JSON', sec_img["otptool_json"])
        d.setVar('ROT_ECC_KEY_NAME', sec_img["rot_ecc_key_name"])
        d.setVar('ROT_ECC_KEY_INDEX', sec_img["rot_ecc_key_index"])
        d.setVar('ROT_LMS_KEY_NAME', sec_img["rot_lms_key_name"])
        d.setVar('ROT_LMS_KEY_INDEX', sec_img["rot_lms_key_index"])
        if aspeed_irot == "yes":
            d.setVar('CALIPTRA_MANIFEST_CONFIG', sec_img["caliptra_manifest_config_irot"])
        else:
            d.setVar('CALIPTRA_MANIFEST_CONFIG', sec_img["caliptra_manifest_config"])

        bb.build.exec_func("install_unsigned_image", d)
        kernel_its = os.path.join(d.getVar('S', True), gen_img, d.getVar('KERNEL_FITIMAGE_ITS_NAME', True))
        print("Update kernel its file", kernel_its)
        algo = sec_img["cot_kernel_hash"] + "," + sec_img["cot_kernel_algo"]
        update_hash_algo(kernel_its, sec_img["cot_kernel_hash"])
        add_or_update_signature_nodes(kernel_its, "configurations", algo, sec_img["cot_uboot_sign_key_name"])

        print("Make kernel fitimage and sign")
        bb.build.exec_func("make_kernel_fitimage_and_sign", d)
        print("Make caliptra manifest image and sign")
        bb.build.exec_func("make_caliptra_manifest_image_and_sign", d)
        print("Make otp image")
        bb.build.exec_func("make_otp_image", d)
        print("Make FMC image and sign")
        bb.build.exec_func("make_fmc_image_and_sign", d)
        print("Make recovery image")
        bb.build.exec_func("make_recovery_image", d)

        if aspeed_irot == "yes":
            print("Create_irot_image...")
            create_irot_image(d)

        if aspeed_boot_emmc_ufs == "yes":
            print("Deploy mmc image...")
            deploy_mmc_image(d)
        else:
            print("Deploy static image...")
            deploy_static_image(d)

        print("Started %s image" % gen_img)
}

addtask deploy before do_build after do_compile

do_deploy[depends] += " \
    virtual/kernel:do_deploy \
    virtual/bootloader:do_deploy \
    virtual/bootmcu:do_deploy \
    fmc-images:do_deploy \
    obmc-phosphor-image:do_image_complete \
    "

python do_cleanall:prepend() {
    import subprocess
    gen_secure_image = [
        "ecdsa384",
        "ecdsa384-lms"
    ]

    for gen_img in gen_secure_image:
        path = os.path.join(d.getVar('DEPLOY_DIR_IMAGE', True), gen_img)
        if os.path.exists(path):
            cmd = "rm -rf %s" % (path)
            print(cmd)
            subprocess.check_call(cmd, shell=True)
}

