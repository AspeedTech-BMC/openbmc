# AST2700 A0 DCSCM BIOS_POST_CODE_LED at SGPIOS.
# AST2700 A1 DCSCM BIOS_POST_CODE_LED at LTPI GPIO.
# We added a set-post-code-led to transfer BIOS post code values to BMC_GPO[16:23].
IMAGE_INSTALL:append= " set-post-code-led"

# AST2700 A0 doesn't support SGPIO Slave interrupt.
# If the SGPIOS input pin is configured for x86-power-control PowerOk,
# the power status will not update.
# We use phosphor-inventory-manager to create DBus,
# and use power-status-sync.sh to poll SGPIOS status to update DBus.
IMAGE_INSTALL:append:ast2700-a0 = " power-status-sync"
