do_populate_lic_deploy[depends] += " \
    ${@oe.utils.conditional('ASPEED_CUSTOMIZE_GEN_SECURE_IMAGE_ENABLE', '1', 'aspeed-image-gen-secureboot:do_deploy', '', d)} \
    "
