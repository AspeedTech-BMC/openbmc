#!/bin/bash

if [ -f /tmp/.mctp_init_done ];then
	exit 0
fi

cpu_rev_id=$(devmem 0x12c02000 32)

# For BMC side spdm attestation emulation
# AST2700-A0 DCSCM BMC i2c10 -> AST1060 i2c5
# AST2700-A1 DCSCM BMC i2c10 -> AST1060 i2c0
mctp link set mctpi2c10 up mtu 68
mctp addr add 0x0a dev mctpi2c10
mctp route add 0x0b via mctpi2c10
mctp neigh add 0x0b dev mctpi2c10 lladdr 0x38

# For PCH side spdm attestation emulation
# Using AST1060 to emulate PCH side spdm attestation
if [[ $(( (cpu_rev_id >> 24) & 0xFF )) -eq 0x06 && \
      $(( (cpu_rev_id >> 16) & 0xFF )) -eq 0x00 ]]; then
    # AST2700-A0 DCSCM
    # BMC i2c15 -> AST1060 i2c2
    mctp link set mctpi2c15 net 2 up mtu 68
    mctp addr add 0x0a dev mctpi2c15
    mctp route add 0x0b via mctpi2c15
    mctp neigh add 0x0b dev mctpi2c15 lladdr 0x70
else
    # AST2700-A1 DCSCM
    # BMC i2c8 -> AST1060 i2c5
    mctp link set mctpi2c8 net 2 up mtu 68
    mctp addr add 0x0a dev mctpi2c8
    mctp route add 0x0b via mctpi2c8
    mctp neigh add 0x0b dev mctpi2c8 lladdr 0x70
fi

touch /tmp/.mctp_init_done
