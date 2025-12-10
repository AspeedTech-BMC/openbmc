# Secure Boot Settings for u-boot verified boot
require ast2700-secure-cot.inc

# Secure Boot Settings for OTP and CALIPTRA
# The following variables can be overridden in local.conf
OTPTOOL_KEY_DIR ?= "${STAGING_DATADIR_NATIVE}/aspeed-secure-config/ast2700/keys"
OTPTOOL_USER_DIR ?= ""
OTPTOOL_SOC ?= "2700"
CALIPTRA_MANIFEST_KEY_DIR ?= "${STAGING_DATADIR_NATIVE}/aspeed-secure-config/ast2700/keys"

#
# By default, algorithm is "ecdsa384-lms"
#

# Using "--no_last_bit" if users still need to add or update OEM DSS
# public keys. Please add "ecc_region": false in OTP CONFIG to disable
# ECC. Once users have done all OTP programming, then enable region ECC.
#
#OTPTOOL_EXTRA_OPTS ?= "--no_last_bit"

# ecdsa384
#OTPTOOL_CONFIGS ?= "${STAGING_DATADIR_NATIVE}/aspeed-secure-config/ast2700/otp/2700A2_ECDSA384.json"
#CALIPTRA_MANIFEST_CONFIG ?= "ast2700-default-ecc-manifest.toml"
#CALIPTRA_MANIFEST_CONFIG:ast-irot ?= "ast2700-irot-ecc-manifest.toml"

# ecdsa384-lms
OTPTOOL_CONFIGS ?= "${STAGING_DATADIR_NATIVE}/aspeed-secure-config/ast2700/otp/2700A2_ECDSA384_LMS.json"
CALIPTRA_MANIFEST_CONFIG ?= "ast2700-default-ecc-lms-manifest.toml"
CALIPTRA_MANIFEST_CONFIG:ast-irot ?= "ast2700-irot-ecc-lms-manifest.toml"

