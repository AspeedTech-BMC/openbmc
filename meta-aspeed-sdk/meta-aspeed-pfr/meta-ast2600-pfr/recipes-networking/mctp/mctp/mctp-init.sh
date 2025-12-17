#!/bin/sh

if [ -f /tmp/.mctp_init_done ];then
	exit 0
fi

# For BMC side spdm attestation emulation
mctp link set mctpi2c4 up mtu 68
mctp addr add 0x0a dev mctpi2c4
mctp route add 0x0b via mctpi2c4
mctp neigh add 0x0b dev mctpi2c4 lladdr 0x38

# For PCH side spdm attestation emulation
mctp link set mctpi2c13 net 2 up mtu 68
mctp addr add 0x0a dev mctpi2c13
mctp route add 0x0b via mctpi2c13
mctp neigh add 0x0b dev mctpi2c13 lladdr 0x70

touch /tmp/.mctp_init_done
