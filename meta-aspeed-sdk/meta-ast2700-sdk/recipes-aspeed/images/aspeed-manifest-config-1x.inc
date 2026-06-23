# Overridable filenames. Set any of these in machine.conf / local.conf to
# change a name without adding a new TOML file.
# ECC is enabled by default because it is required by Caliptra firmware.
CPTRA_TOML_ECC_ENABLE ?= "1"
# LMS is disabled by default.
CPTRA_TOML_LMS_ENABLE ?= "0"

CPTRA_TOML_VND_FW_ECC_PUB ?= "vnd-fw-ecc-pubk.pem"
CPTRA_TOML_VND_FW_ECC_PRIV ?= "vnd-fw-ecc-prvk.pem"
CPTRA_TOML_VND_FW_LMS_PUB ?= "vnd-fw-lms-pubk.pem"
CPTRA_TOML_VND_FW_LMS_PRIV ?= "vnd-fw-lms-prvk.pem"

CPTRA_TOML_VND_MAN_ECC_PUB ?= "vnd-man-ecc-pubk.pem"
CPTRA_TOML_VND_MAN_ECC_PRIV ?= "vnd-man-ecc-prvk.pem"
CPTRA_TOML_VND_MAN_LMS_PUB ?= "vnd-man-lms-pubk.pem"
CPTRA_TOML_VND_MAN_LMS_PRIV ?= "vnd-man-lms-prvk.pem"

CPTRA_TOML_OWN_FW_ECC_PUB ?= "own-fw-ecc-pubk.pem"
CPTRA_TOML_OWN_FW_ECC_PRIV ?= "own-fw-ecc-prvk.pem"
CPTRA_TOML_OWN_FW_LMS_PUB ?= "own-fw-lms-pubk.pem"
CPTRA_TOML_OWN_FW_LMS_PRIV ?= "own-fw-lms-prvk.pem"

CPTRA_TOML_OWN_MAN_ECC_PUB ?= "own-man-ecc-pubk.pem"
CPTRA_TOML_OWN_MAN_ECC_PRIV ?= "own-man-ecc-prvk.pem"
CPTRA_TOML_OWN_MAN_LMS_PUB ?= "own-man-lms-pubk.pem"
CPTRA_TOML_OWN_MAN_LMS_PRIV ?= "own-man-lms-prvk.pem"

CPTRA_TOML_CALIPTRA_RUNTIME ?= "${CALIPTRA_FW_BINARY}"
CPTRA_TOML_MCU_RUNTIME ?= "zephyr-aspeed-bootmcu.bin"
CPTRA_TOML_IMG_MCU ?= "zephyr-aspeed-bootmcu.bin"

CPTRA_TOML_IMG_DDR4_IMEM ?= "ddr4_pmu_train_imem.bin"
CPTRA_TOML_IMG_DDR4_DMEM ?= "ddr4_pmu_train_dmem.bin"
CPTRA_TOML_IMG_DDR4_2D_IMEM ?= "ddr4_2d_pmu_train_imem.bin"
CPTRA_TOML_IMG_DDR4_2D_DMEM ?= "ddr4_2d_pmu_train_dmem.bin"
CPTRA_TOML_IMG_DDR5_IMEM ?= "ddr5_pmu_train_imem.bin"
CPTRA_TOML_IMG_DDR5_DMEM ?= "ddr5_pmu_train_dmem.bin"
CPTRA_TOML_IMG_DP ?= "dp_fw.bin"
CPTRA_TOML_IMG_UEFI ?= "uefi_ast2700.bin"

CPTRA_TOML_IMG_ATF ?= "trusted-firmware-a/bl31.bin"
CPTRA_TOML_LOAD_STAGE_ATF ?= "1"
CPTRA_TOML_IMG_TEE ?= "optee/tee-raw.bin"
CPTRA_TOML_LOAD_STAGE_TEE ?= "1"
CPTRA_TOML_IMG_UBOOT ?= "u-boot.bin"
CPTRA_TOML_LOAD_STAGE_UBOOT ?= "1"
CPTRA_TOML_IMG_KERNEL ?= ""
CPTRA_TOML_LOAD_STAGE_KERNEL ?= "0"

CPTRA_TOML_IMG_SSP ?= ""
CPTRA_TOML_LOAD_STAGE_SSP ?= "0"
CPTRA_TOML_IMG_SSP:ast-ssp ?= "${@os.path.basename(d.getVar('UBOOT_FIT_SSP_IMAGE'))}"
CPTRA_TOML_LOAD_STAGE_SSP:ast-ssp ?= "1"

CPTRA_TOML_IMG_TSP ?= ""
CPTRA_TOML_LOAD_STAGE_TSP ?= "0"
CPTRA_TOML_IMG_TSP:ast-tsp ?= "${@os.path.basename(d.getVar('UBOOT_FIT_TSP_IMAGE'))}"
CPTRA_TOML_LOAD_STAGE_TSP:ast-tsp ?= "1"

CPTRA_TOML_ZERO_CALIPTRA_FILE_END ?= ""

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
    local kernel_metadata=""
    local zero_caliptra_file_end=""

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

    if [ -n "${CPTRA_TOML_IMG_KERNEL}" ]; then
        kernel_metadata="[[image_metadata_list]]
file = \"${CPTRA_TOML_IMG_KERNEL}\"
source = 1
fw_id = 15
ignore_auth_check = false
load_stage = ${CPTRA_TOML_LOAD_STAGE_KERNEL}"
    fi

    if [ -n "${CPTRA_TOML_ZERO_CALIPTRA_FILE_END}" ]; then
        zero_caliptra_file_end="zero_caliptra_file_end = ${CPTRA_TOML_ZERO_CALIPTRA_FILE_END}"
    fi

    cat << EOF > ${out}
# Licensed under the Apache-2.0 license

[manifest_config]
version = 1
flags = 0
security_version = 1

[vendor_fw_key_config]
${vendor_fw_ecc_keys}
${vendor_fw_lms_keys}

[vendor_man_key_config]
${vendor_man_ecc_keys}
${vendor_man_lms_keys}

[owner_fw_key_config]
${owner_fw_ecc_keys}
${owner_fw_lms_keys}

[owner_man_key_config]
${owner_man_ecc_keys}
${owner_man_lms_keys}

[image_runtime_list]
caliptra_file = "${CPTRA_TOML_CALIPTRA_RUNTIME}"
mcu_file = "${CPTRA_TOML_MCU_RUNTIME}"
${zero_caliptra_file_end}

[[image_metadata_list]]
file = "${CPTRA_TOML_IMG_MCU}"
source = 1
fw_id = 1
ignore_auth_check = false
load_stage = 0

[[image_metadata_list]]
file = "${CPTRA_TOML_IMG_DDR4_IMEM}"
source = 1
fw_id = 2
ignore_auth_check = false
load_stage = 0

[[image_metadata_list]]
file = "${CPTRA_TOML_IMG_DDR4_DMEM}"
source = 1
fw_id = 3
ignore_auth_check = false
load_stage = 0

[[image_metadata_list]]
file = "${CPTRA_TOML_IMG_DDR4_2D_IMEM}"
source = 1
fw_id = 4
ignore_auth_check = false
load_stage = 0

[[image_metadata_list]]
file = "${CPTRA_TOML_IMG_DDR4_2D_DMEM}"
source = 1
fw_id = 5
ignore_auth_check = false
load_stage = 0

[[image_metadata_list]]
file = "${CPTRA_TOML_IMG_DDR5_IMEM}"
source = 1
fw_id = 6
ignore_auth_check = false
load_stage = 0

[[image_metadata_list]]
file = "${CPTRA_TOML_IMG_DDR5_DMEM}"
source = 1
fw_id = 7
ignore_auth_check = false
load_stage = 0

[[image_metadata_list]]
file = "${CPTRA_TOML_IMG_DP}"
source = 1
fw_id = 8
ignore_auth_check = false
load_stage = 0

[[image_metadata_list]]
file = "${CPTRA_TOML_IMG_UEFI}"
source = 1
fw_id = 9
ignore_auth_check = false
load_stage = 0

[[image_metadata_list]]
file = "${CPTRA_TOML_IMG_ATF}"
source = 1
fw_id = 10
ignore_auth_check = false
load_stage = ${CPTRA_TOML_LOAD_STAGE_ATF}

[[image_metadata_list]]
file = "${CPTRA_TOML_IMG_TEE}"
source = 1
fw_id = 11
ignore_auth_check = false
load_stage = ${CPTRA_TOML_LOAD_STAGE_TEE}

[[image_metadata_list]]
file = "${CPTRA_TOML_IMG_UBOOT}"
source = 1
fw_id = 12
ignore_auth_check = false
load_stage = ${CPTRA_TOML_LOAD_STAGE_UBOOT}

[[image_metadata_list]]
file = "${CPTRA_TOML_IMG_SSP}"
source = 1
fw_id = 13
ignore_auth_check = false
load_stage = ${CPTRA_TOML_LOAD_STAGE_SSP}

[[image_metadata_list]]
file = "${CPTRA_TOML_IMG_TSP}"
source = 1
fw_id = 14
ignore_auth_check = false
load_stage = ${CPTRA_TOML_LOAD_STAGE_TSP}

${kernel_metadata}
EOF
}
