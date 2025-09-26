# Secure Boot Settings for u-boot verified boot
require ast2700-secure-cot.inc

# Secure Boot Settings for OTP and FMC
# The following variables can be overridden in local.conf
FMC_SIGN_ENABLE ?= "1"
OTPTOOL_KEY_DIR ?= "${DEPLOY_DIR_IMAGE}/keys"
OTPTOOL_USER_DIR ?= ""
OTPTOOL_SOC ?= "2700"
FMC_KEY_DIR ?= "${DEPLOY_DIR_IMAGE}/keys"

#
# By default, algorithm is "ECDSA384_LMS"
#

# Using "--no_last_bit" if users still need to add or update OEM DSS
# public keys. Please add "ecc_region": false in OTP CONFIG to disable
# ECC. Once users have done all OTP programming, then enable region ECC.
#
#OTPTOOL_EXTRA_OPTS ?= "--no_last_bit"

# ECDSA384
#OTPTOOL_CONFIGS ?= "${STAGING_DATADIR_NATIVE}/aspeed-secure-config/ast2700/otp/2700A1_ECDSA384.json"
#FMC_ECC_KEY ?= "${FMC_KEY_DIR}/test_oem_dss_private_key_ecdsa384_1.pem"
#FMC_ECC_KEY_INDEX ?= "1"

# ECDSA384_LMS
OTPTOOL_CONFIGS ?= "${STAGING_DATADIR_NATIVE}/aspeed-secure-config/ast2700/otp/2700A1_ECDSA384_LMS.json"
FMC_ECC_KEY ?= "${FMC_KEY_DIR}/test_oem_dss_private_key_ecdsa384_1.pem"
FMC_ECC_KEY_INDEX ?= "1"
FMC_LMS_KEY ?= "${FMC_KEY_DIR}/test_oem_dss_lms_key_1.prv"
FMC_LMS_KEY_INDEX ?= "1"
