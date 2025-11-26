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

if mctp link|grep mctpi3c0 > /dev/null;then
	mctp address add 0x9d dev mctpi3c0
	mctp link set mctpi3c0 net 3 up mtu 68
fi

if mctp link|grep mctpi3c1 > /dev/null;then
	mctp address add 0x1d dev mctpi3c1
	mctp link set mctpi3c1 net 4 up mtu 68
fi

touch /tmp/.mctp_init_done
