#!/bin/bash
# Wait for RS485 udev symlinks before rackmond starts.

timeout=30
expected=7

for _ in $(seq 1 "$timeout"); do
    missing=0
    for i in $(seq 1 "$expected"); do
        if [ ! -e "/dev/ttyRS485-$i" ]; then
            missing=$((missing + 1))
        fi
    done
    if [ "$missing" -eq 0 ]; then
        exit 0
    fi
    sleep 1
done

echo "Timeout waiting for /dev/ttyRS485-*"
exit 1
