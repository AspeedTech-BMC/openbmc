#!/bin/sh
# CPU emulation for PFR-4.0:
#  ┌────────────────────┐                 ┌─────────────────────┐
#  │     AST2700        │                 │       AST1060       │
#  │                    │                 │                     │
#  │           i3c4     │                 │                     │
#  │  i3c-mctp-target-0 │      I3C        │i3c2                 │
#  │           EID=0x1D ├─────────────────┤EID=0x0B             │
#  │                    │                 │                     │
#  └────────────────────┘                 └─────────────────────┘
#
# CPU emulation for PFR-5.0:
#  ┌────────────────────┐                 ┌─────────────────────┐
#  │     AST2700        │                 │       AST1060       │
#  │                    │                 │                     │
#  │                    │                 │                     │
#  │           mctpi3c4 │      I3C        │i3c2                 │
#  │           EID=0x1D ├─────────────────┤EID=0x08             │
#  │                    │                 │                     │
#  └────────────────────┘                 └─────────────────────┘

SetupEndpoint()
{
	busctl call au.com.codeconstruct.MCTP1 \
	/au/com/codeconstruct/mctp1/interfaces/mctpi3c4 \
	au.com.codeconstruct.MCTP.BusOwner1 SetupEndpoint \
	ay 6 0x07 0xec 0xa0 0x03 0x20 0x00
}

GetPlatformState()
{
	result="0x$(aspeed-pfr-tool -r 0x0a)"
	result=$(( result & 0x22 ))
	if [[ $result -eq 0x22 ]]; then
		# update PlatformState property
		busctl get-property xyz.openbmc_project.PFR.Manager \
		/xyz/openbmc_project/pfr xyz.openbmc_project.State.Boot.Platform \
		Data > /dev/null
		busctl get-property xyz.openbmc_project.PFR.Manager \
		/xyz/openbmc_project/pfr xyz.openbmc_project.State.Boot.Platform \
		PlatformState|cut -b 4-|awk -F '"' '{print $1}'
	else
		if systemctl status xyz.openbmc_project.PFR.Manager| \
			grep "code=exited, status=0/SUCCESS" > /dev/null;then
			echo "T0 boot complete"
		fi
	fi
}

if [ -f /tmp/.mctp_i3c_done ];then
	exit 0
fi

if mctp link|grep mctpi3c4 > /dev/null;then
	echo "Running PFR-5.0 MCTP over I3C Master"
	mctp address add 0x1d dev mctpi3c4
	mctp link set mctpi3c4 net 4 up mtu 68
	STATE=$(GetPlatformState)
	while true;do
		if [ "$STATE" = "T0 BMC booted" ] || [ "$STATE" = "T0 boot complete" ];then
			SetupEndpoint
			break
		fi

		sleep 2
		STATE=$(GetPlatformState)
	done
	/usr/bin/pfr-mctpd -s &
elif [ -r /dev/i3c-mctp-target-0 ];then
	echo "Running PFR-4.0 MCTP over I3C Target"
	/usr/bin/pfr-mctpd -d /dev/i3c-mctp-target-0
else
	echo "No I3C MCTP device found"
	exit 1
fi
systemctl start i3c-attestation-emu.service
touch /tmp/.mctp_i3c_done
