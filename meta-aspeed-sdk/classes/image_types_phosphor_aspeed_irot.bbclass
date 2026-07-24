# Run AST27x0 iROT-specific image artifacts after do_image_complete.
do_populate_lic_deploy[depends] += " \
    aspeed-image-manifest:do_deploy \
    aspeed-image-irot:do_deploy \
    "
