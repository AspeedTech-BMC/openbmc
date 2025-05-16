MACHINE_FEATURES:append = " ast-ssp"
MACHINEOVERRIDES .= ":ast-ssp"

# AST2600
# Do not load the "aspeed-ssp" module on boot.
KERNEL_MODULE_PROBECONF:append:aspeed-g6 = " aspeed-ssp"
module_conf_aspeed-ssp = "blacklist aspeed-ssp"

# AST2700
UBOOT_FIT_CONF_USER_LOADABLES:append:aspeed-g7 = ' ,\"sspfw\"'