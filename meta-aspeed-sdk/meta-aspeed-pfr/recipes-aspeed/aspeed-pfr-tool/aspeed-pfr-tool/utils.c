/*
 * Copyright (c) 2024 ASPEED Technology Inc.
 *
 * SPDX-License-Identifier: MIT
 */

#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <errno.h>
#include <i2c/smbus.h>
#include <linux/i2c-dev.h>
#include <sys/ioctl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include "arguments.h"
#include "spdm.h"

void WriteByteData(ARGUMENTS args, uint8_t offset, uint8_t value)
{
	size_t data_size;
	uint8_t tmpbuf[8];

	if (args.secure_mode) {
		vendor_def_msg msg;

		build_vendor_msg(&msg, CMD_WRITE_DATA, sizeof(uint8_t), &offset);
		msg.data[0] = value;
		msg.hdr.length = 1;
		data_size = sizeof(tmpbuf);
		if (process_secure_msg(&msg, sizeof(uint8_t), tmpbuf, &data_size)) {
			printf("Failed to process secure msg\n");
			return;
		}
	} else {
		i2cWriteByteData(args, offset, value);
	}
}

void WriteBlockData(ARGUMENTS args, uint8_t offset, uint8_t length, uint8_t *value)
{
	size_t data_size;

	if (args.secure_mode) {
		vendor_def_msg msg;

		build_vendor_msg(&msg, CMD_WRITE_DATA, sizeof(uint8_t), &offset);
		memcpy(msg.data, value, length);
		msg.hdr.length = length;
		data_size = length;
		if (process_secure_msg(&msg, length, (uint8_t *)&value, &data_size)) {
			printf("Failed to process secure msg\n");
			return;
		}
	} else {
		i2cWriteBlockData(args, offset, length, value);
	}
}

uint8_t ReadByteData(ARGUMENTS args, uint8_t offset)
{
	uint8_t tmpbuf[8];
	size_t data_size;

	if (args.secure_mode) {
		vendor_def_msg msg;

		build_vendor_msg(&msg, CMD_READ_DATA, sizeof(uint8_t), &offset);
		data_size = sizeof(tmpbuf);
		if (process_secure_msg(&msg, sizeof(uint8_t), tmpbuf, &data_size)) {
			printf("Failed to process secure msg\n");
			return -1;
		}
		return (uint8_t)tmpbuf[0];
	} else {
		return i2cReadByteData(args, offset);
	}
}

int ReadBlockData(ARGUMENTS args, uint8_t offset, uint8_t length, uint8_t *value)
{
	int read_length;
	size_t data_size;
	uint8_t tmpbuf[8];
	uint8_t *ptr;

	if (args.secure_mode) {
		vendor_def_msg msg;

		if (length > I2C_SMBUS_BLOCK_MAX) {
			printf("Request length (%d) > max block size (%d)," \
				" to adjust the request length\n", length, I2C_SMBUS_BLOCK_MAX);
			length = I2C_SMBUS_BLOCK_MAX;
		}

		build_vendor_msg(&msg, CMD_READ_DATA, sizeof(uint8_t), &offset);
		msg.hdr.length = length;
		if (length < sizeof(tmpbuf)) {
			ptr = tmpbuf;
			data_size = sizeof(tmpbuf);
		} else {
			ptr = value;
			data_size = length;
		}
		if (process_secure_msg(&msg, sizeof(uint8_t), (uint8_t *)ptr, &data_size)) {
			printf("Failed to process secure msg\n");
			return -1;
		}
		read_length = data_size;
		if (length < sizeof(tmpbuf)) {
			if (read_length >= length) {
				memcpy(value, tmpbuf, length);
				read_length = length;
			} else {
				printf("Failed to read %d bytes data from %x\n", length, offset);
				return -1;
			}
		}
		return read_length;
	} else {
		return i2cReadBlockData(args, offset, length, value);
	}
}
