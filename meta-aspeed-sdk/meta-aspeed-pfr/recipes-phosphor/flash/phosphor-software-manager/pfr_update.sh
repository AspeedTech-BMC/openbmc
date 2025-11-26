#!/bin/bash

findmtd() {
	m=$(grep -xl "$1" /sys/class/mtd/*/name)
	m=${m%/name}
	m=${m##*/}
	echo "$m"
}


updateProgress()
{
    busctl set-property xyz.openbmc_project.Software.BMC.Updater \
           /xyz/openbmc_project/software/"${IMAGE_VERSIONID}" \
           xyz.openbmc_project.Software.ActivationProgress Progress y "${1}"
}


pfr_update() {
	EXTENDED_VERSION=$1
	IMAGE=$2
	MTD_NAME=$3
	CMD=$4
	UPDATE_IMAGE=$IMAGE_PATH/$IMAGE
	MTD=$(findmtd "$MTD_NAME")
	if [ -z "$MTD" ]; then
		echo "ExtendedVersion: ${EXTENDED_VERSION} partition not found."
		exit 1
	fi

	# If unprovisioned, stop firmware update.
	result="0x$(aspeed-pfr-tool -r 0x0a)"
	# Bit[1]: Command Done, Bitp[5]: UFM provisioned
	result=$(( result & 0x22 ))
	if [[ $result -ne 0x22 ]]; then
		echo "ExtendedVersion: ${EXTENDED_VERSION} is unprovisioned."
		exit 1
	fi

	# Check if the previous copy image operation is running.
	result=$(ps)
	result=$(echo "$result" | grep flashcp | grep "$IMAGE")
	if [ -n "$result" ]; then
		echo "ExtendedVersion: ${EXTENDED_VERSION} flashcp $MTD is runing."
		exit 1
	fi

	updateProgress 30
	echo "Start ${EXTENDED_VERSION} copy image to ${MTD}."
	if ! flashcp -v "$UPDATE_IMAGE" /dev/"$MTD"; then
		echo "ExtendedVersion: ${EXTENDED_VERSION} flash fail."
		exit 1
	fi

	updateProgress 60
	echo "Finish copy image. Send command to PFR to start firmware update"
	sleep 1;
	if ! $CMD ; then
		echo "ExtendedVersion: ${EXTENDED_VERSION} aspeed-pfr-tool fail."
		exit 1
	fi
}

IMAGE_VERSIONID="$1"
IMAGE_PATH=/tmp/images/"$1"
if [ ! -d "$IMAGE_PATH" ]; then
	echo "The folder $IMAGE_PATH does not exist"
	exit 1
fi

MANIFEST_PATH="${IMAGE_PATH}/MANIFEST"
if [ ! -f "$MANIFEST_PATH" ]; then
	echo "The MANIFEST file $MANIFEST_PATH does not exist"
	exit 1
fi

EXTENDED_VERSION=$(awk -F '=' '/ExtendedVersion/ {print $2}' "${MANIFEST_PATH}" | tr -d '"')

# If the ExtendedVersion is empty, set default to bios firmware update
if [ -z "$EXTENDED_VERSION" ]; then
	EXTENDED_VERSION="bios"
fi

# Execute Firmware update based on the ExtendedVersion
case ${EXTENDED_VERSION} in
	"bios")
		IMAGE=bios_signed_cap.bin
		MTD_NAME=img-stg
		CMD="aspeed-pfr-tool -E -w 0x13 0x01"
		pfr_update "$EXTENDED_VERSION" "$IMAGE" "$MTD_NAME" "$CMD"
		;;

	"pfrbmc")
		IMAGE=bmc_signed_cap.bin
		MTD_NAME=img-stg
		CMD="aspeed-pfr-tool -E -w 0x13 0x08"
		pfr_update "$EXTENDED_VERSION" "$IMAGE" "$MTD_NAME" "$CMD"
		;;

	"pfr")
		IMAGE=zephyr_signed.bin
		MTD_NAME=pfr-stg
		CMD="aspeed-pfr-tool -E -w 0x13 0x04"
		pfr_update "$EXTENDED_VERSION" "$IMAGE" "$MTD_NAME" "$CMD"
		;;

	*)
		echo "Invalid ExtendedVersion: ${EXTENDED_VERSION}. Please check MANIFEST file!"
		exit 1
		;;
esac
