# The following variables can be overridden in local.conf
# Enable aspeed customize secure boot image generation
ASPEED_CUSTOMIZE_GEN_SECURE_IMAGE_ENABLE ?= "1"

# Generate aspeed customize secure boot image
ASPEED_CUSTOMIZE_GEN_SECURE_IMAGE ?= "\
    ecdsa384 \
    ecdsa384-lms \
    "
