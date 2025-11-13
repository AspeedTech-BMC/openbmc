# Secure Boot Settings for u-boot verified boot
#
# By default, algorithm is "sha384,ecdsa384"
#
# The following variables can be overridden in local.conf
#

UBOOT_SIGN_ENABLE ?= "1"
UBOOT_SIGN_KEYDIR ?= "${STAGING_DATADIR_NATIVE}/aspeed-secure-config/ast2700/keys"

# Algorithm "sha384,ecdsa384"
FIT_PAD_ALG ?= ""
UBOOT_SIGN_KEYNAME ?= "test_bl3_ecdsa_secp384r1"

# Kernel fitImage Hash Algo
FIT_HASH_ALG ?= "sha384"

# Kernel / Bootloader fitImage Signature Algo
FIT_SIGN_ALG ?= "ecdsa384"

# Size of private keys in number of bits
FIT_SIGN_NUMBITS ?= "384"

