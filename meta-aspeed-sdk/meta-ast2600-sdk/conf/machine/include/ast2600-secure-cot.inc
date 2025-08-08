# Secure Boot Settings for u-boot verified boot
#
# By default, algorithm is "sha512,rsa4096"
#
# The following variables can be overridden in local.conf
#

SPL_SIGN_ENABLE ?= "1"
SPL_SIGN_KEYDIR ?= "${STAGING_DATADIR_NATIVE}/aspeed-secure-config/keys"

UBOOT_SIGN_ENABLE ?= "1"
UBOOT_SIGN_KEYDIR ?= "${STAGING_DATADIR_NATIVE}/aspeed-secure-config/keys"


# Algorithm "sha512,rsa4096"
SPL_SIGN_KEYNAME ?= "test_bl2_4096"
UBOOT_SIGN_KEYNAME ?= "test_bl3_4096"

# Kernel / Bootloader fitImage Hash Algo
FIT_HASH_ALG ?= "sha512"
UBOOT_FIT_HASH_ALG ?= "sha512"

# Kernel / Bootloader fitImage Signature Algo
FIT_SIGN_ALG ?= "rsa4096"
UBOOT_FIT_SIGN_ALG ?= "rsa4096"

# Size of private keys in number of bits
FIT_SIGN_NUMBITS ?= "4096"
UBOOT_FIT_SIGN_NUMBITS ?= "4096"


# Algorithm "sha384,rsa3072"
#SPL_SIGN_KEYNAME ?= "test_bl2_3072"
#UBOOT_SIGN_KEYNAME ?= "test_bl3_3072"

# Kernel / Bootloader fitImage Hash Algo
#FIT_HASH_ALG ?= "sha384"
#UBOOT_FIT_HASH_ALG ?= "sha384"

# Kernel / Bootloader fitImage Signature Algo
#FIT_SIGN_ALG ?= "rsa3072"
#UBOOT_FIT_SIGN_ALG ?= "rsa3072"

# Size of private keys in number of bits
#FIT_SIGN_NUMBITS ?= "3072"
#UBOOT_FIT_SIGN_NUMBITS ?= "3072"


# Algorithm "sha256,rsa2048"
#SPL_SIGN_KEYNAME ?= "test_bl2_2048"
#UBOOT_SIGN_KEYNAME ?= "test_bl3_2048"

# Kernel / Bootloader fitImage Hash Algo
#FIT_HASH_ALG ?= "sha256"
#UBOOT_FIT_HASH_ALG ?= "sha256"

# Kernel / Bootloader fitImage Signature Algo
#FIT_SIGN_ALG ?= "rsa2048"
#UBOOT_FIT_SIGN_ALG ?= "rsa2048"

# Size of private keys in number of bits
#FIT_SIGN_NUMBITS ?= "2048"
#UBOOT_FIT_SIGN_NUMBITS ?= "2048"
