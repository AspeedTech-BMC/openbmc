FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI:append = " file://fw_env_ast2700_nor.config"
SRC_URI:append:ast-mmc = " file://fw_env_ast2700_mmc.config"
SRC_URI:append:ast-ufs = " file://fw_env_ast2700_ufs.config"
SRC_URI:append:ast-irot = " file://boot-ram.cfg"

ENV_CONFIG_FILE = "fw_env_ast2700_nor.config"
ENV_CONFIG_FILE:ast-mmc = "fw_env_ast2700_mmc.config"
ENV_CONFIG_FILE:ast-ufs = "fw_env_ast2700_ufs.config"
