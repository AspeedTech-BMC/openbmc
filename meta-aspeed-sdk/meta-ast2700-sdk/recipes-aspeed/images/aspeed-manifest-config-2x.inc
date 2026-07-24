# Overridable settings for the Caliptra 2.x manifest configuration TOML.
# Set any of these in machine.conf / local.conf to change a value without
# adding a new TOML file.

# Signing algorithm enables.
# ECC is enabled by default.
CPTRA_TOML_ECC_ENABLE ?= "1"
# LMS is enabled by default.
CPTRA_TOML_LMS_ENABLE ?= "1"
# MLDSA is enabled by default.
CPTRA_TOML_MLDSA_ENABLE ?= "1"

# [manifest_config]
CPTRA_TOML_SECURITY_VERSION ?= "0"
CPTRA_TOML_PADDING_ALIGN_SIZE ?= "256"
CPTRA_TOML_PADDING_OUT_FOLDER ?= "out/padding_output"

# Vendor firmware keys.
CPTRA_TOML_VND_FW_ECC_PUB ?= "vnd-fw-ecc-pub-key-1.pem"
CPTRA_TOML_VND_FW_ECC_PRIV ?= "vnd-fw-ecc-priv-key-1.pem"
CPTRA_TOML_VND_FW_LMS_PUB ?= "vnd-fw-lms-pub-key-0.pem"
CPTRA_TOML_VND_FW_LMS_PRIV ?= "vnd-fw-lms-priv-key-0.pem"
CPTRA_TOML_VND_FW_MLDSA_PUB ?= "vnd-fw-mldsa-pub-key-0.bin"
CPTRA_TOML_VND_FW_MLDSA_PRIV ?= "vnd-fw-mldsa-priv-key-0.bin"

# Vendor manifest keys.
CPTRA_TOML_VND_MAN_ECC_PUB ?= "vnd-man-ecc-pub-key.pem"
CPTRA_TOML_VND_MAN_ECC_PRIV ?= "vnd-man-ecc-priv-key.pem"
CPTRA_TOML_VND_MAN_LMS_PUB ?= "vnd-man-lms-pub-key.pem"
CPTRA_TOML_VND_MAN_LMS_PRIV ?= "vnd-man-lms-priv-key.pem"
CPTRA_TOML_VND_MAN_MLDSA_PUB ?= "vnd-man-mldsa-pub-key.bin"
CPTRA_TOML_VND_MAN_MLDSA_PRIV ?= "vnd-man-mldsa-priv-key.bin"

# Owner firmware keys.
CPTRA_TOML_OWN_FW_ECC_PUB ?= "own-fw-ecc-pub-key.pem"
CPTRA_TOML_OWN_FW_ECC_PRIV ?= "own-fw-ecc-priv-key.pem"
CPTRA_TOML_OWN_FW_LMS_PUB ?= "own-fw-lms-pub-key.pem"
CPTRA_TOML_OWN_FW_LMS_PRIV ?= "own-fw-lms-priv-key.pem"
CPTRA_TOML_OWN_FW_MLDSA_PUB ?= "own-fw-mldsa-pub-key.bin"
CPTRA_TOML_OWN_FW_MLDSA_PRIV ?= "own-fw-mldsa-priv-key.bin"

# Owner manifest keys.
CPTRA_TOML_OWN_MAN_ECC_PUB ?= "own-man-ecc-pub-key.pem"
CPTRA_TOML_OWN_MAN_ECC_PRIV ?= "own-man-ecc-priv-key.pem"
CPTRA_TOML_OWN_MAN_LMS_PUB ?= "own-man-lms-pub-key.pem"
CPTRA_TOML_OWN_MAN_LMS_PRIV ?= "own-man-lms-priv-key.pem"
CPTRA_TOML_OWN_MAN_MLDSA_PUB ?= "own-man-mldsa-pub-key.bin"
CPTRA_TOML_OWN_MAN_MLDSA_PRIV ?= "own-man-mldsa-priv-key.bin"

# [image_runtime_list]
CPTRA_TOML_CALIPTRA_RUNTIME ?= "caliptra-fw.bin"
CPTRA_TOML_MCU_RUNTIME ?= "ssmcu-runtime.bin"

# Per-image settings. Each image groups its file, load stage, load address
# and staging address together.
CPTRA_TOML_IMG_MCU ?= "ssmcu-runtime.bin"
CPTRA_TOML_LOAD_STAGE_MCU ?= "0"
CPTRA_TOML_IMG_LOAD_ADDR_MCU ?= "0x0000_0000_0000_0000"
CPTRA_TOML_IMG_STAGING_ADDR_MCU ?= "0x0000_0000_0000_0000"

CPTRA_TOML_IMG_DDR4_IMEM ?= "ddr4_pmu_train_imem.bin"
CPTRA_TOML_LOAD_STAGE_DDR4_IMEM ?= "0"
CPTRA_TOML_IMG_LOAD_ADDR_DDR4_IMEM ?= "0x0000_0000_34BA_0000"
CPTRA_TOML_IMG_STAGING_ADDR_DDR4_IMEM ?= "0x0000_0000_0000_0000"

CPTRA_TOML_IMG_DDR4_DMEM ?= "ddr4_pmu_train_dmem.bin"
CPTRA_TOML_LOAD_STAGE_DDR4_DMEM ?= "0"
CPTRA_TOML_IMG_LOAD_ADDR_DDR4_DMEM ?= "0x0000_0000_34BA_0000"
CPTRA_TOML_IMG_STAGING_ADDR_DDR4_DMEM ?= "0x0000_0000_0000_0000"

CPTRA_TOML_IMG_DDR4_2D_IMEM ?= "ddr4_2d_pmu_train_imem.bin"
CPTRA_TOML_LOAD_STAGE_DDR4_2D_IMEM ?= "0"
CPTRA_TOML_IMG_LOAD_ADDR_DDR4_2D_IMEM ?= "0x0000_0000_34BA_0000"
CPTRA_TOML_IMG_STAGING_ADDR_DDR4_2D_IMEM ?= "0x0000_0000_0000_0000"

CPTRA_TOML_IMG_DDR4_2D_DMEM ?= "ddr4_2d_pmu_train_dmem.bin"
CPTRA_TOML_LOAD_STAGE_DDR4_2D_DMEM ?= "0"
CPTRA_TOML_IMG_LOAD_ADDR_DDR4_2D_DMEM ?= "0x0000_0000_34BA_0000"
CPTRA_TOML_IMG_STAGING_ADDR_DDR4_2D_DMEM ?= "0x0000_0000_0000_0000"

CPTRA_TOML_IMG_DDR5_IMEM ?= "ddr5_pmu_train_imem.bin"
CPTRA_TOML_LOAD_STAGE_DDR5_IMEM ?= "0"
CPTRA_TOML_IMG_LOAD_ADDR_DDR5_IMEM ?= "0x0000_0000_34BA_0000"
CPTRA_TOML_IMG_STAGING_ADDR_DDR5_IMEM ?= "0x0000_0000_0000_0000"

CPTRA_TOML_IMG_DDR5_DMEM ?= "ddr5_pmu_train_dmem.bin"
CPTRA_TOML_LOAD_STAGE_DDR5_DMEM ?= "0"
CPTRA_TOML_IMG_LOAD_ADDR_DDR5_DMEM ?= "0x0000_0000_34BA_0000"
CPTRA_TOML_IMG_STAGING_ADDR_DDR5_DMEM ?= "0x0000_0000_0000_0000"

CPTRA_TOML_IMG_DP ?= "dp_fw.bin"
CPTRA_TOML_LOAD_STAGE_DP ?= "0"
CPTRA_TOML_IMG_LOAD_ADDR_DP ?= "0x0000_0000_34BA_0000"
CPTRA_TOML_IMG_STAGING_ADDR_DP ?= "0x0000_0000_0000_0000"

CPTRA_TOML_IMG_UEFI ?= "uefi_ast2700.bin"
CPTRA_TOML_LOAD_STAGE_UEFI ?= "0"
CPTRA_TOML_IMG_LOAD_ADDR_UEFI ?= "0x0000_0000_34BA_0000"
CPTRA_TOML_IMG_STAGING_ADDR_UEFI ?= "0x0000_0000_0000_0000"

CPTRA_TOML_IMG_ATF ?= "trusted-firmware-a/bl31.bin"
CPTRA_TOML_LOAD_STAGE_ATF ?= "1"
CPTRA_TOML_IMG_LOAD_ADDR_ATF ?= "0x0000_0000_f000_0000"
CPTRA_TOML_IMG_STAGING_ADDR_ATF ?= "0x0000_0000_0000_0000"

CPTRA_TOML_IMG_TEE ?= "optee/tee-raw.bin"
CPTRA_TOML_LOAD_STAGE_TEE ?= "1"
CPTRA_TOML_IMG_LOAD_ADDR_TEE ?= "0x0000_0000_f008_0000"
CPTRA_TOML_IMG_STAGING_ADDR_TEE ?= "0x0000_0000_0000_0000"

CPTRA_TOML_IMG_UBOOT ?= "u-boot.bin"
CPTRA_TOML_LOAD_STAGE_UBOOT ?= "1"
CPTRA_TOML_IMG_LOAD_ADDR_UBOOT ?= "0x0000_0000_c000_0000"
CPTRA_TOML_IMG_STAGING_ADDR_UBOOT ?= "0x0000_0000_0000_0000"

CPTRA_TOML_IMG_SSP ?= "zephyr-aspeed-ssp.bin"
CPTRA_TOML_LOAD_STAGE_SSP ?= "0"
CPTRA_TOML_IMG_LOAD_ADDR_SSP ?= "0x0000_0000_0000_0000"
CPTRA_TOML_IMG_STAGING_ADDR_SSP ?= "0x0000_0000_0000_0000"

CPTRA_TOML_IMG_TSP ?= "zephyr-aspeed-tsp.bin"
CPTRA_TOML_LOAD_STAGE_TSP ?= "0"
CPTRA_TOML_IMG_LOAD_ADDR_TSP ?= "0x0000_0000_0000_0000"
CPTRA_TOML_IMG_STAGING_ADDR_TSP ?= "0x0000_0000_0000_0000"

cptra_generate_manifest_config() {
    local out="$1"
    local vendor_fw_ecc_keys=""
    local vendor_man_ecc_keys=""
    local owner_fw_ecc_keys=""
    local owner_man_ecc_keys=""
    local vendor_fw_lms_keys=""
    local vendor_man_lms_keys=""
    local owner_fw_lms_keys=""
    local owner_man_lms_keys=""
    local vendor_fw_mldsa_keys=""
    local vendor_man_mldsa_keys=""
    local owner_fw_mldsa_keys=""
    local owner_man_mldsa_keys=""

    if [ "${CPTRA_TOML_ECC_ENABLE}" = "1" ]; then
        vendor_fw_ecc_keys="ecc_pub_key = \"${CPTRA_TOML_VND_FW_ECC_PUB}\"
ecc_priv_key = \"${CPTRA_TOML_VND_FW_ECC_PRIV}\""

        vendor_man_ecc_keys="ecc_pub_key = \"${CPTRA_TOML_VND_MAN_ECC_PUB}\"
ecc_priv_key = \"${CPTRA_TOML_VND_MAN_ECC_PRIV}\""

        owner_fw_ecc_keys="ecc_pub_key = \"${CPTRA_TOML_OWN_FW_ECC_PUB}\"
ecc_priv_key = \"${CPTRA_TOML_OWN_FW_ECC_PRIV}\""

        owner_man_ecc_keys="ecc_pub_key = \"${CPTRA_TOML_OWN_MAN_ECC_PUB}\"
ecc_priv_key = \"${CPTRA_TOML_OWN_MAN_ECC_PRIV}\""
    fi

    if [ "${CPTRA_TOML_LMS_ENABLE}" = "1" ]; then
        vendor_fw_lms_keys="lms_pub_key = \"${CPTRA_TOML_VND_FW_LMS_PUB}\"
lms_priv_key = \"${CPTRA_TOML_VND_FW_LMS_PRIV}\""

        vendor_man_lms_keys="lms_pub_key = \"${CPTRA_TOML_VND_MAN_LMS_PUB}\"
lms_priv_key = \"${CPTRA_TOML_VND_MAN_LMS_PRIV}\""

        owner_fw_lms_keys="lms_pub_key = \"${CPTRA_TOML_OWN_FW_LMS_PUB}\"
lms_priv_key = \"${CPTRA_TOML_OWN_FW_LMS_PRIV}\""

        owner_man_lms_keys="lms_pub_key = \"${CPTRA_TOML_OWN_MAN_LMS_PUB}\"
lms_priv_key = \"${CPTRA_TOML_OWN_MAN_LMS_PRIV}\""
    fi

    if [ "${CPTRA_TOML_MLDSA_ENABLE}" = "1" ]; then
        vendor_fw_mldsa_keys="mldsa_pub_key = \"${CPTRA_TOML_VND_FW_MLDSA_PUB}\"
mldsa_priv_key = \"${CPTRA_TOML_VND_FW_MLDSA_PRIV}\""

        vendor_man_mldsa_keys="mldsa_pub_key = \"${CPTRA_TOML_VND_MAN_MLDSA_PUB}\"
mldsa_priv_key = \"${CPTRA_TOML_VND_MAN_MLDSA_PRIV}\""

        owner_fw_mldsa_keys="mldsa_pub_key = \"${CPTRA_TOML_OWN_FW_MLDSA_PUB}\"
mldsa_priv_key = \"${CPTRA_TOML_OWN_FW_MLDSA_PRIV}\""

        owner_man_mldsa_keys="mldsa_pub_key = \"${CPTRA_TOML_OWN_MAN_MLDSA_PUB}\"
mldsa_priv_key = \"${CPTRA_TOML_OWN_MAN_MLDSA_PRIV}\""
    fi

    cat << EOF > ${out}
# Licensed under the Apache-2.0 license

[manifest_config]
version = 1
flags = 0
security_version = ${CPTRA_TOML_SECURITY_VERSION}
padding_align_size = ${CPTRA_TOML_PADDING_ALIGN_SIZE}
padding_out_folder = "${CPTRA_TOML_PADDING_OUT_FOLDER}"

[vendor_fw_key_config]
${vendor_fw_ecc_keys}
${vendor_fw_lms_keys}
${vendor_fw_mldsa_keys}

[vendor_man_key_config]
${vendor_man_ecc_keys}
${vendor_man_lms_keys}
${vendor_man_mldsa_keys}

[owner_fw_key_config]
${owner_fw_ecc_keys}
${owner_fw_lms_keys}
${owner_fw_mldsa_keys}

[owner_man_key_config]
${owner_man_ecc_keys}
${owner_man_lms_keys}
${owner_man_mldsa_keys}

[image_runtime_list]
caliptra_file = "${CPTRA_TOML_CALIPTRA_RUNTIME}"
mcu_file = "${CPTRA_TOML_MCU_RUNTIME}"

[[image_metadata_list]]
file = "${CPTRA_TOML_IMG_MCU}"
source = 1
fw_id = 0x0002
ignore_auth_check = false
load_stage = ${CPTRA_TOML_LOAD_STAGE_MCU}
exec_bit = 0
component_id = 0
image_load_address = ${CPTRA_TOML_IMG_LOAD_ADDR_MCU}
image_staging_address = ${CPTRA_TOML_IMG_STAGING_ADDR_MCU}
classification = 0

[[image_metadata_list]]
file = "${CPTRA_TOML_IMG_DDR4_IMEM}"
source = 1
fw_id = 0x1000
ignore_auth_check = false
load_stage = ${CPTRA_TOML_LOAD_STAGE_DDR4_IMEM}
exec_bit = 0
component_id = 0
image_load_address = ${CPTRA_TOML_IMG_LOAD_ADDR_DDR4_IMEM}
image_staging_address = ${CPTRA_TOML_IMG_STAGING_ADDR_DDR4_IMEM}
classification = 0

[[image_metadata_list]]
file = "${CPTRA_TOML_IMG_DDR4_DMEM}"
source = 1
fw_id = 0x1001
ignore_auth_check = false
load_stage = ${CPTRA_TOML_LOAD_STAGE_DDR4_DMEM}
exec_bit = 0
component_id = 0
image_load_address = ${CPTRA_TOML_IMG_LOAD_ADDR_DDR4_DMEM}
image_staging_address = ${CPTRA_TOML_IMG_STAGING_ADDR_DDR4_DMEM}
classification = 0

[[image_metadata_list]]
file = "${CPTRA_TOML_IMG_DDR4_2D_IMEM}"
source = 1
fw_id = 0x1002
ignore_auth_check = false
load_stage = ${CPTRA_TOML_LOAD_STAGE_DDR4_2D_IMEM}
exec_bit = 0
component_id = 0
image_load_address = ${CPTRA_TOML_IMG_LOAD_ADDR_DDR4_2D_IMEM}
image_staging_address = ${CPTRA_TOML_IMG_STAGING_ADDR_DDR4_2D_IMEM}
classification = 0

[[image_metadata_list]]
file = "${CPTRA_TOML_IMG_DDR4_2D_DMEM}"
source = 1
fw_id = 0x1003
ignore_auth_check = false
load_stage = ${CPTRA_TOML_LOAD_STAGE_DDR4_2D_DMEM}
exec_bit = 0
component_id = 0
image_load_address = ${CPTRA_TOML_IMG_LOAD_ADDR_DDR4_2D_DMEM}
image_staging_address = ${CPTRA_TOML_IMG_STAGING_ADDR_DDR4_2D_DMEM}
classification = 0

[[image_metadata_list]]
file = "${CPTRA_TOML_IMG_DDR5_IMEM}"
source = 1
fw_id = 0x1004
ignore_auth_check = false
load_stage = ${CPTRA_TOML_LOAD_STAGE_DDR5_IMEM}
exec_bit = 0
component_id = 0
image_load_address = ${CPTRA_TOML_IMG_LOAD_ADDR_DDR5_IMEM}
image_staging_address = ${CPTRA_TOML_IMG_STAGING_ADDR_DDR5_IMEM}
classification = 0

[[image_metadata_list]]
file = "${CPTRA_TOML_IMG_DDR5_DMEM}"
source = 1
fw_id = 0x1005
ignore_auth_check = false
load_stage = ${CPTRA_TOML_LOAD_STAGE_DDR5_DMEM}
exec_bit = 0
component_id = 0
image_load_address = ${CPTRA_TOML_IMG_LOAD_ADDR_DDR5_DMEM}
image_staging_address = ${CPTRA_TOML_IMG_STAGING_ADDR_DDR5_DMEM}
classification = 0

[[image_metadata_list]]
file = "${CPTRA_TOML_IMG_DP}"
source = 1
fw_id = 0x1006
ignore_auth_check = false
load_stage = ${CPTRA_TOML_LOAD_STAGE_DP}
exec_bit = 0
component_id = 0
image_load_address = ${CPTRA_TOML_IMG_LOAD_ADDR_DP}
image_staging_address = ${CPTRA_TOML_IMG_STAGING_ADDR_DP}
classification = 0

[[image_metadata_list]]
file = "${CPTRA_TOML_IMG_UEFI}"
source = 1
fw_id = 0x1007
ignore_auth_check = false
load_stage = ${CPTRA_TOML_LOAD_STAGE_UEFI}
exec_bit = 0
component_id = 0
image_load_address = ${CPTRA_TOML_IMG_LOAD_ADDR_UEFI}
image_staging_address = ${CPTRA_TOML_IMG_STAGING_ADDR_UEFI}
classification = 0

[[image_metadata_list]]
file = "${CPTRA_TOML_IMG_ATF}"
source = 1
fw_id = 0x1008
ignore_auth_check = false
load_stage = ${CPTRA_TOML_LOAD_STAGE_ATF}
exec_bit = 0
component_id = 0
image_load_address = ${CPTRA_TOML_IMG_LOAD_ADDR_ATF}
image_staging_address = ${CPTRA_TOML_IMG_STAGING_ADDR_ATF}
classification = 0

[[image_metadata_list]]
file = "${CPTRA_TOML_IMG_TEE}"
source = 1
fw_id = 0x1009
ignore_auth_check = false
load_stage = ${CPTRA_TOML_LOAD_STAGE_TEE}
exec_bit = 0
component_id = 0
image_load_address = ${CPTRA_TOML_IMG_LOAD_ADDR_TEE}
image_staging_address = ${CPTRA_TOML_IMG_STAGING_ADDR_TEE}
classification = 0

[[image_metadata_list]]
file = "${CPTRA_TOML_IMG_UBOOT}"
source = 1
fw_id = 0x100a
ignore_auth_check = false
load_stage = ${CPTRA_TOML_LOAD_STAGE_UBOOT}
exec_bit = 0
component_id = 0
image_load_address = ${CPTRA_TOML_IMG_LOAD_ADDR_UBOOT}
image_staging_address = ${CPTRA_TOML_IMG_STAGING_ADDR_UBOOT}
classification = 0

[[image_metadata_list]]
file = "${CPTRA_TOML_IMG_SSP}"
source = 1
fw_id = 0x100b
ignore_auth_check = false
load_stage = ${CPTRA_TOML_LOAD_STAGE_SSP}
exec_bit = 0
component_id = 0
image_load_address = ${CPTRA_TOML_IMG_LOAD_ADDR_SSP}
image_staging_address = ${CPTRA_TOML_IMG_STAGING_ADDR_SSP}
classification = 0

[[image_metadata_list]]
file = "${CPTRA_TOML_IMG_TSP}"
source = 1
fw_id = 0x100c
ignore_auth_check = false
load_stage = ${CPTRA_TOML_LOAD_STAGE_TSP}
exec_bit = 0
component_id = 0
image_load_address = ${CPTRA_TOML_IMG_LOAD_ADDR_TSP}
image_staging_address = ${CPTRA_TOML_IMG_STAGING_ADDR_TSP}
classification = 0
EOF
}
