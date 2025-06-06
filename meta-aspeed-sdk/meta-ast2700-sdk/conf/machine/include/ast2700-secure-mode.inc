# Secure Boot Settings for u-boot verified boot
require ast2700-secure-cot.inc

# Secure Boot Settings for OTP and FMC
# The following variables can be overridden in local.conf
FMC_SIGN_ENABLE ?= "1"
FMC_KEY_DIR ?= "${STAGING_LIBDIR_NATIVE}/${PYTHON_DIR}/fmc-imgtool/keys"

#
# By default, algorithm is "ECDSA384"
#

# ECDSA384
FMC_ECC_KEY ?= "${FMC_KEY_DIR}/test_oem_dss_private_key_ecdsa384_1.pem"
FMC_ECC_KEY_INDEX ?= "1"

# ECDSA384_LMS
#FMC_ECC_KEY ?= "${FMC_KEY_DIR}/test_oem_dss_private_key_ecdsa384_1.pem"
#FMC_ECC_KEY_INDEX ?= "1"
#FMC_LMS_KEY ?= "${FMC_KEY_DIR}/test_oem_dss_lms_key_1.prv"
#FMC_LMS_KEY_INDEX ?= "1"
