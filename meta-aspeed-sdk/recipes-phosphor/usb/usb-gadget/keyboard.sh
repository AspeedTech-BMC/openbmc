#!/bin/sh

GADGET_BASE=/sys/kernel/config/usb_gadget
CONFIG_FILE="/etc/usb-gadget.conf"
if [ -f "$CONFIG_FILE" ]; then
    . "$CONFIG_FILE"
    echo "USB_VHUB: $USB_VHUB" >&2
else
    USB_VHUB=1e6a0000.usb-vhub
    echo "Config file $CONFIG_FILE not found, using default USB_VHUB: $USB_VHUB" >&2
fi
NAME=key_usb

which_dev()
{
    in_use="$(cat $GADGET_BASE/*/UDC)"
    cd /sys/class/udc || exit
    for dev in $USB_VHUB*; do
        case "$in_use" in
            *"$dev"*) ;;
            *) echo "$dev"; return 0;;
        esac
    done
    return 1
}


usb_keyboard_create()
{
    if [ -d $GADGET_BASE/$NAME ]; then
        echo "device $NAME already exists" >&2
        return 1
    fi
    mkdir $GADGET_BASE/$NAME
    cd $GADGET_BASE/$NAME

    echo 0x1d6b > idVendor  # Linux Foundation
    echo 0x0105 > idProduct # FunctionFS Gadget
    mkdir strings/0x409
    local serial="OBMCKEY001"
    echo $serial > strings/0x409/serialnumber
    echo OpenBMC > strings/0x409/manufacturer
    echo "OpenBMC Keyboard" > strings/0x409/product

    mkdir configs/c.1
    mkdir functions/hid.$NAME
    mkdir configs/c.1/strings/0x409

    echo 1 > functions/hid.$NAME/protocol	# 1: keyboard
    echo 8 > functions/hid.$NAME/report_length
    echo 1 > functions/hid.$NAME/subclass

    echo -ne '\x05\x01\x09\x06\xa1\x01\x05\x07\x19\xe0\x29\xe7\x15\x00\x25\x01\x75\x01\x95\x08\x81\x02\x95\x01\x75\x08\x81\x03\x95\x05\x75\x01\x05\x08\x19\x01\x29\x05\x91\x02\x95\x01\x75\x03\x91\x03\x95\x06\x75\x08\x15\x00\x25\x65\x05\x07\x19\x00\x29\x65\x81\x00\xc0' > functions/hid.$NAME/report_desc

    echo "Config-1" > configs/c.1/strings/0x409/configuration
    echo 0xe0 > configs/c.1/bmAttributes
    echo 120 > configs/c.1/MaxPower
    ln -s functions/hid.$NAME configs/c.1

    echo $(which_dev) > UDC
}

if test "$1" = stop; then
    rm -f $GADGET_BASE/$NAME/configs/c.1/hid.$NAME
    rmdir $GADGET_BASE/$NAME/configs/c.1/strings/0x409
    rmdir $GADGET_BASE/$NAME/configs/c.1
    rmdir $GADGET_BASE/$NAME/functions/hid.$NAME
    rmdir $GADGET_BASE/$NAME/strings/0x409
    rmdir $GADGET_BASE/$NAME
else
    usb_keyboard_create
fi
