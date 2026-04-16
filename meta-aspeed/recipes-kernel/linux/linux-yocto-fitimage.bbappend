FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}:"

SRC_URI += " \
    file://rsa_oem_fitimage_key.key;sha256sum=eeb4ff2ebbfbd97b6254fe6dbaeea41067e54c65176c233ec7b2ab2decf1ddcd \
    file://rsa_oem_fitimage_key.crt;sha256sum=45f5a55497cce8040999bf9f3214d471ac7b83ab7acef41c4425a34662e8372e \
"