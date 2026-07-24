#!/bin/bash
# CPU emulation for PFR-4.0:
#  ┌────────────────────┐                 ┌─────────────────────┐
#  │     AST2700        │                 │       AST1060       │
#  │                    │                 │                     │
#  │           i3c4     │                 │                     │
#  │  i3c-mctp-target-0 │      I3C        │i3c2                 │
#  │           EID=0x1D ├─────────────────┤EID=0x7B             │
#  │                    │                 │                     │
#  └────────────────────┘                 └─────────────────────┘
#
# CPU emulation for PFR-5.0:
#  ┌────────────────────┐                 ┌─────────────────────┐
#  │     AST2700        │                 │       AST1060       │
#  │                    │                 │                     │
#  │                    │                 │                     │
#  │           mctpi3c4 │      I3C        │i3c2                 │
#  │           EID=0x1D ├─────────────────┤EID=0x7B             │
#  │                    │                 │                     │
#  └────────────────────┘                 └─────────────────────┘
#
# MCTP Bridge Mode for PFR-5.0:
# ┌────────────┐       ┌────────────────────┐       ┌───────────┐
# │     CPU    │       │      AST2700       │       │  AST1060  │
# │            │       │    MCTP Bridge     │       │           │
# │            │       │                    │       │           │
# │     I3C_MNG│  I3C  │mctpi3c5   mctpi3c4 │  I3C  │i3c2       │
# │BHS EID=0x1D├───────┤EID=0x7E   EID=0x7A ├───────┤EID=0x7B   │
# │OKS EID=0x09│       │net=4               │       │net=4      │
# └────────────┘       └────────────────────┘       └───────────┘

# shellcheck source=/dev/null
source /usr/bin/intel-gpio-lib.sh

CPU_EID=0x1d
CPU_I3C_DEVICE="5-20a012900ef"
CPU_I3C_LLADDR="0x02:0a:01:29:00:ef"

# Determine PFR MCTP I3C mode from board SKU ID:
#   0   -> Emulation environment -> CPU_EMULATION
#   37  -> BHS AvenueCity platform   -> BRIDGE_MODE
#   *   -> OKS JohnsonCity platform  -> BRIDGE_MODE
board_id=$(read_id)
echo "Board ID=$board_id"

case $board_id in
	0)
		# Emulation environment
		PFR_MCTP_I3C_MODE="CPU_EMULATION"
		;;
	37)
		# BHS AvenueCity platform
		PFR_MCTP_I3C_MODE="BRIDGE_MODE"
		;;
	*)
		# OKS JohnsonCity platform
		PFR_MCTP_I3C_MODE="BRIDGE_MODE"
		CPU_EID=0x09
		CPU_I3C_DEVICE="5-20a0168000f"
		CPU_I3C_LLADDR="0x02:0a:01:68:00:0f"
		;;
esac

if ! mctp link|grep mctpi3c4 > /dev/null;then
	echo "mctpi3c4 not found, falling back to CPU_EMULATION mode"
	PFR_MCTP_I3C_MODE="CPU_EMULATION"
fi

echo "PFR_MCTP_I3C_MODE=$PFR_MCTP_I3C_MODE CPU_EID=$CPU_EID"

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
	if [ "$result" -eq $((0x22)) ]; then
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

WaitForPlatformReady()
{
	STATE=$(GetPlatformState)
	while true; do
		if [ "$STATE" = "T0 BMC booted" ] || [ "$STATE" = "T0 boot complete" ]; then
			SetupEndpoint
			break
		fi

		sleep 2
		STATE=$(GetPlatformState)
	done
}

SetupCpuI3cDevice()
{
	for retry_count in $(seq 1 10); do
		if [ -e /sys/bus/i3c/devices/$CPU_I3C_DEVICE ]; then
			echo "CPU I3C found after $retry_count attempts"
			break
		else
			echo "CPU I3C not found, rescanning I3C MNG bus (attempt $retry_count)"
			echo 1 > /sys/bus/i3c/devices/14c25000.i3c5/rescan
			sleep 10
		fi
	done
	if [ ! -e /sys/bus/i3c/devices/$CPU_I3C_DEVICE ]; then
		echo "Warning: CPU I3C not found after 10 attempts"
	fi

	if mctp link|grep mctpi3c5 > /dev/null;then
		echo "Setup MCTP bridge over I3C to CPU"
		# Delete existing MCTP configurations if they exist
		mctp neigh del $CPU_EID dev mctpi3c5 2>/dev/null || true
		mctp route del $CPU_EID via mctpi3c5 2>/dev/null || true
		mctp addr del 0x7e dev mctpi3c5 2>/dev/null || true

		# Re-add MCTP configurations
		mctp link set mctpi3c5 net 4 up mtu 68
		mctp addr add 0x7e dev mctpi3c5
		mctp route add $CPU_EID via mctpi3c5
		mctp neigh add $CPU_EID dev mctpi3c5 lladdr $CPU_I3C_LLADDR
	fi
}

MonitorPltrstn()
{
	# Open the pltrstn device for reading
	exec 3< "/dev/aspeed-espi-pltrstn0"

	while true; do
		# read 1 byte (blocking)
		v="$(dd bs=1 count=1 <&3 2>/dev/null | tr -d '\0')"

		[ -n "$v" ] || continue

		if [ "$v" = "1" ]; then
			echo "MonitorPltrstn rescan i3c"
			echo 1 > "/sys/bus/i3c/devices/14c25000.i3c5/rescan"
			# After rescan, setup CPU I3C device again
			SetupCpuI3cDevice
		fi
	done

	# Close the file descriptor
	exec 3<&-;
}

StartCpuEmulationMode()
{
	if mctp link|grep mctpi3c4 > /dev/null;then
		echo "Running CPU Emulation for PFR-5.0 MCTP over I3C Master"
		mctp link set mctpi3c4 net 4 up mtu 68
		mctp addr add $CPU_EID dev mctpi3c4
		WaitForPlatformReady
		/usr/bin/pfr-mctpd -s &
	elif [ -r /dev/i3c-mctp-target-0 ];then
		echo "Running CPU Emulation for PFR-4.0 MCTP over I3C Target"
		/usr/bin/pfr-mctpd -d /dev/i3c-mctp-target-0
	else
		echo "No I3C MCTP device found"
		exit 1
	fi
	systemctl start i3c-attestation-emu.service
}

StartMCTPBridgeMode()
{
	if mctp link|grep mctpi3c4 > /dev/null;then
		echo "Setup MCTP bridge over I3C to PFR"
		mctp link set mctpi3c4 net 4 up mtu 68
		mctp addr add 0x7a dev mctpi3c4
		WaitForPlatformReady
	fi

	if [ ! -e /dev/aspeed-espi-pltrstn0 ]; then
		# This is a workaround to ensure the CPU I3C device is available.
		echo "Waiting for CPU I3C device to be ready..."
		sleep 90
		SetupCpuI3cDevice
	else
		echo "Using aspeed-espi-pltrstn0 to ensure CPU I3C device is ready"
		SetupCpuI3cDevice
		MonitorPltrstn
	fi

	#ls /sys/bus/i3c/devices/
	#mctp-client net 4 eid 0x1d type control data 80 05
}

if [ "$PFR_MCTP_I3C_MODE" = "CPU_EMULATION" ]; then
	if [ -f /tmp/.mctp_i3c_done ];then
		echo "CPU emulation mode already started"
		exit 0
	fi
	StartCpuEmulationMode
	touch /tmp/.mctp_i3c_done
else
	echo "Running MCTP I3C Bridge Mode"
	StartMCTPBridgeMode
fi
