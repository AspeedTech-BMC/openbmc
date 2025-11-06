#!/bin/bash

if [ -f /tmp/.mctp_init_done ];then
	exit 0
fi

# For BMC side spdm attestation emulation
# AST2700-A1 DCSCM BMC i2c10 -> AST1060 i2c0
mctp link set mctpi2c10 up mtu 68
mctp addr add 0x0a dev mctpi2c10
mctp route add 0x0b via mctpi2c10
mctp neigh add 0x0b dev mctpi2c10 lladdr 0x38

# For PCH side spdm attestation emulation
# Using AST1060 to emulate PCH side spdm attestation
# AST2700-A1 DCSCM
# BMC i2c8 -> AST1060 i2c5
mctp link set mctpi2c8 net 2 up mtu 68
mctp addr add 0x0a dev mctpi2c8
mctp route add 0x0b via mctpi2c8
mctp neigh add 0x0b dev mctpi2c8 lladdr 0x70

if mctp link|grep mctpi3c4 > /dev/null;then
	mctp address add 0x1d dev mctpi3c4
	mctp link set mctpi3c4 net 4 up mtu 68
fi

touch /tmp/.mctp_init_done
