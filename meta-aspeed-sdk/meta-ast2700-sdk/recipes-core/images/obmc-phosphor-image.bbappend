do_populate_lic_deploy[depends] += " \
    ${@bb.utils.contains('MACHINE_FEATURES', 'ast-secure', 'aspeed-image-gen-secureboot:do_deploy', '', d)} \
    "
