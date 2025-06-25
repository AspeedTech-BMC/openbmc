# Secure Boot Settings for u-boot verified boot
require ast2600-secure-cot.inc

# Secure Boot Settings for OTP and SOCSEC
# The following variables can be overridden in local.conf
SOCSEC_SIGN_ENABLE ?= "1"
SOCSEC_SIGN_SOC ?= "2600"
SOCSEC_SIGN_HELPER ?= "${STAGING_DATADIR_NATIVE}/aspeed-secure-config/signing_helper.sh"
OTP_SOCSEC_KEY_DIR ?= "${STAGING_DATADIR_NATIVE}/aspeed-secure-config/keys"
OTPTOOL_KEY_DIR ?= "${OTP_SOCSEC_KEY_DIR}"
OTPTOOL_USER_DIR ?= "${STAGING_DATADIR_NATIVE}/aspeed-secure-config/ast2600/data"
OTPTOOL_SOC ?= "2600"

#
# By default, algorithm is "Mode 2 RSA4096_SHA512"
#

# Using "--no_last_bit" if users still need to add or update OEM DSS
# public keys. Please add "ecc_region": false in OTP CONFIG to disable
# ECC. Once users have done all OTP programming, then enable region ECC.
#
#OTPTOOL_EXTRA_OPTS ?= "--no_last_bit"

# RSA2048_SHA256
#OTPTOOL_CONFIGS ?= "${STAGING_DATADIR_NATIVE}/aspeed-secure-config/ast2600/otp/evbA3_RSA2048_SHA256.json"
#SOCSEC_SIGN_EXTRA_OPTS ?= " \
#    --stack_intersects_verification_region=false \
#    --rsa_key_order=big \
#    "
#SOCSEC_SIGN_ALGO ?= "RSA2048_SHA256"
#SOCSEC_SIGN_KEY ?= "${OTP_SOCSEC_KEY_DIR}/test_oem_dss_private_key_2048_1.pem"

# RSA2048_SHA256_O1
#OTPTOOL_CONFIGS ?= "${STAGING_DATADIR_NATIVE}/aspeed-secure-config/ast2600/otp/evbA3_RSA2048_SHA256_o1.json"
#SOCSEC_SIGN_ALGO ?= "AES_RSA2048_SHA256"
#SOCSEC_SIGN_KEY ?= "${OTP_SOCSEC_KEY_DIR}/test_oem_dss_private_key_2048_1.pem"
#SOCSEC_SIGN_EXTRA_OPTS ?= " \
#    --stack_intersects_verification_region=false \
#    --rsa_key_order=big \
#    --key_in_otp \
#    --aes_key ${OTP_SOCSEC_KEY_DIR}/test_aes_key.bin \
#    "

# RSA2048_SHA256_O2_PUB
#OTPTOOL_CONFIGS ?= "${STAGING_DATADIR_NATIVE}/aspeed-secure-config/ast2600/otp/evbA3_RSA2048_SHA256_o2_pub.json"
#SOCSEC_SIGN_ALGO ?= "AES_RSA2048_SHA256"
#SOCSEC_SIGN_KEY ?= "${OTP_SOCSEC_KEY_DIR}/test_oem_dss_private_key_2048_1.pem"
#SOCSEC_SIGN_EXTRA_OPTS ?= " \
#    --stack_intersects_verification_region=false \
#    --rsa_key_order=big \
#    --aes_key ${OTP_SOCSEC_KEY_DIR}/test_aes_key.bin \
#    --rsa_aes ${OTP_SOCSEC_KEY_DIR}/test_soc_private_key_2048.pem \
#    "

# RSA3072_SHA384
#OTPTOOL_CONFIGS ?= "${STAGING_DATADIR_NATIVE}/aspeed-secure-config/ast2600/otp/evbA3_RSA3072_SHA384.json"
#SOCSEC_SIGN_EXTRA_OPTS ?= " \
#    --stack_intersects_verification_region=false \
#    --rsa_key_order=big \
#    "
#SOCSEC_SIGN_ALGO ?= "RSA3072_SHA384"
#SOCSEC_SIGN_KEY ?= "${OTP_SOCSEC_KEY_DIR}/test_oem_dss_private_key_3072_1.pem"

# RSA3072_SHA384_O1
#OTPTOOL_CONFIGS ?= "${STAGING_DATADIR_NATIVE}/aspeed-secure-config/ast2600/otp/evbA3_RSA3072_SHA384_o1.json"
#SOCSEC_SIGN_ALGO ?= "AES_RSA3072_SHA384"
#SOCSEC_SIGN_KEY ?= "${OTP_SOCSEC_KEY_DIR}/test_oem_dss_private_key_3072_1.pem"
#SOCSEC_SIGN_EXTRA_OPTS ?= " \
#    --stack_intersects_verification_region=false \
#    --rsa_key_order=big \
#    --key_in_otp \
#    --aes_key ${OTP_SOCSEC_KEY_DIR}/test_aes_key.bin \
#    "

# RSA3072_SHA384_O2_PUB
#OTPTOOL_CONFIGS ?= "${STAGING_DATADIR_NATIVE}/aspeed-secure-config/ast2600/otp/evbA3_RSA3072_SHA384_o2_pub.json"
#SOCSEC_SIGN_ALGO ?= "AES_RSA3072_SHA384"
#SOCSEC_SIGN_KEY ?= "${OTP_SOCSEC_KEY_DIR}/test_oem_dss_private_key_3072_1.pem"
#SOCSEC_SIGN_EXTRA_OPTS ?= " \
#    --stack_intersects_verification_region=false \
#    --rsa_key_order=big \
#    --aes_key ${OTP_SOCSEC_KEY_DIR}/test_aes_key.bin \
#    --rsa_aes ${OTP_SOCSEC_KEY_DIR}/test_soc_private_key_3072.pem \
#    "

# RSA4096_SHA512
OTPTOOL_CONFIGS ?= "${STAGING_DATADIR_NATIVE}/aspeed-secure-config/ast2600/otp/evbA3_RSA4096_SHA512.json"
SOCSEC_SIGN_EXTRA_OPTS ?= " \
    --stack_intersects_verification_region=false \
    --rsa_key_order=big \
    "
SOCSEC_SIGN_ALGO ?= "RSA4096_SHA512"
SOCSEC_SIGN_KEY ?= "${OTP_SOCSEC_KEY_DIR}/test_oem_dss_private_key_4096_1.pem"

# RSA4096_SHA512_O1
#OTPTOOL_CONFIGS ?= "${STAGING_DATADIR_NATIVE}/aspeed-secure-config/ast2600/otp/evbA3_RSA4096_SHA512_o1.json"
#SOCSEC_SIGN_ALGO ?= "AES_RSA4096_SHA512"
#SOCSEC_SIGN_KEY ?= "${OTP_SOCSEC_KEY_DIR}/test_oem_dss_private_key_4096_1.pem"
#SOCSEC_SIGN_EXTRA_OPTS ?= " \
#    --stack_intersects_verification_region=false \
#    --rsa_key_order=big \
#    --key_in_otp \
#    --aes_key ${OTP_SOCSEC_KEY_DIR}/test_aes_key.bin \
#    "

# RSA4096_SHA512_O2_PUB
#OTPTOOL_CONFIGS ?= "${STAGING_DATADIR_NATIVE}/aspeed-secure-config/ast2600/otp/evbA3_RSA4096_SHA512_o2_pub.json"
#SOCSEC_SIGN_ALGO ?= "AES_RSA4096_SHA512"
#SOCSEC_SIGN_KEY ?= "${OTP_SOCSEC_KEY_DIR}/test_oem_dss_private_key_4096_1.pem"
#SOCSEC_SIGN_EXTRA_OPTS ?= " \
#    --stack_intersects_verification_region=false \
#    --rsa_key_order=big \
#    --aes_key ${OTP_SOCSEC_KEY_DIR}/test_aes_key.bin \
#    --rsa_aes ${OTP_SOCSEC_KEY_DIR}/test_soc_private_key_4096.pem \
#    "

