#!/bin/sh

SetupEndpoint()
{
	busctl call au.com.codeconstruct.MCTP1 \
	/au/com/codeconstruct/mctp1/interfaces/mctpi3c0 \
	au.com.codeconstruct.MCTP.BusOwner1 SetupEndpoint \
	ay 6 0x07 0xec 0xa0 0x03 0x00 0x00

	busctl call au.com.codeconstruct.MCTP1 \
	/au/com/codeconstruct/mctp1/interfaces/mctpi3c1 \
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

if [ -r /dev/i3c-mctp-target-0 ];then

/usr/bin/pfr-mctpd -d /dev/i3c-mctp-target-0

else

STATE=$(GetPlatformState)
while true;do
if [ "$STATE" = "T0 BMC booted" ] || [ "$STATE" = "T0 boot complete" ];then
	SetupEndpoint
	break
fi
sleep 2
STATE=$(GetPlatformState)
done

fi
systemctl start i3c-attestation-emu.service
touch /tmp/.mctp_i3c_done
