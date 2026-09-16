/*
 * Copyright (c) 2022 ASPEED Technology Inc.
 *
 * SPDX-License-Identifier: MIT
 */

#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include <getopt.h>
#include <errno.h>
#include <fcntl.h>
#include <unistd.h>
#include "provision.h"
#include "checkpoint.h"
#include "i2c_utils.h"
#include "utils.h"
#include "arguments.h"
#include "config.h"
#include "status.h"
#include "info.h"
#include "spdm.h"

static const char short_options[] = "hvb:a:c:p:uk:w:r:dsiS:l:ED:t:n:";
static const struct option
	long_options[] = {
	{ "help", no_argument, NULL, 'h' },
	{ "version", no_argument, NULL, 'v' },
	{ "bus", required_argument, NULL, 'b' },
	{ "address", required_argument, NULL, 'a' },
	{ "pfrtoolconf", required_argument, NULL, 'c' },
	{ "provision", required_argument, NULL, 'p' },
	{ "unprovision", no_argument, NULL, 'u' },
	{ "checkpoint", required_argument, NULL, 'k' },
	{ "write_reg", required_argument, NULL, 'w' },
	{ "read_reg", required_argument, NULL, 'r' },
	{ "debug", no_argument, NULL, 'd' },
	{ "status", no_argument, NULL, 's' },
	{ "info", no_argument, NULL, 'i' },
	{ "spdm", required_argument, NULL, 'S' },
	{ "lms", required_argument, NULL, 'l' },
	{ "secure", no_argument, NULL, 'E' },
	{ "dest_id", required_argument, NULL, 'D' },
	{ "test_case", required_argument, NULL, 't' },
	{ "mctp_network", required_argument, NULL, 'n' },
	{ 0, 0, 0, 0 }
};

#ifdef SUPPORT_SECURE_CONNECTION
	char secure_connect_status[32] = "this is supported";
#else
	char secure_connect_status[32] = "this is not supported";
#endif

static void usage(FILE *fp, int argc, char **argv)
{
	fprintf(fp,
		"Usage: %s [options]\n\n"
		"Options:\n"
		" -h | --help           Print this message\n"
		" -v | --version        show version\n"
		" -b | --bus            bus number\n"
		" -a | --address        slave address\n"
		" -c | --pfrtoolconf    aspeed pfr tool config\n"
		"                       [default : /usr/share/pfrconfig/aspeed-pfr-tool.conf]\n"
		" -p | --provision      provision\n"
		" -u | --unprovision    unprovision\n"
		" -k | --checkpoint     checkpoint\n"
		" -w | --write_reg      write register\n"
		" -r | --read_reg       read register\n"
		" -d | --debug          debug mode\n"
		" -s | --status         show rot status\n"
		" -i | --info           show bmc/pch version info\n"
		" -S | --spdm           enable or disable SPDM attestation\n"
		" -l | --lms            use LMS sign scheme\n"
		" -E | --secure         use secure connection (%s)\n"
		" -D | --dest_id        MCTP destination endpoint id [default : 0x%x]\n"
		" -t | --test_case      specify test case for secure connection\n"
		" -n | --mctp_network   specify MCTP network index\n"
		"example:\n"
		"--provision /usr/share/pfrconfig/rk_pub.pem\n"
		"--provision show\n"
		"--provision lock\n"
		"--checkpoint start\n"
		"--checkpoint pause\n"
		"--checkpoint resume\n"
		"--checkpoint complete\n"
		"--write_reg <rf_addr> <data> (byte mode)\n"
		"--write_reg <rf_addr> <data1> <data2>... (block mode)\n"
		"--read_reg <rf_addr> (byte mode)\n"
		"--read_reg <rf_addr> <length> (block mode)\n"
		"--status\n"
		"--info\n"
		"--spdm enable\n"
		"--spdm disable\n"
		"--lms 256\n"
		"--lms 384\n"
		"",
		argv[0], secure_connect_status, DEFAULT_MCTP_DST_SKT);
}

void printVersion(void)
{
	printf("ASPEED PFR tool version: %s\n", ASPEED_PFR_TOOL_VERSION);
}

void printArguments(ARGUMENTS args)
{
	printf("I2C_BUS = %d\n", args.i2c_bus);
	printf("ROT_ADDRESS = 0x%02x\n", args.rot_addr);
	printf("BMC_ACTIVE_PFM_OFFSET = 0x%08x\n", args.bmc_active_pfm_offset);
	printf("BMC_STAGING_OFFSET = 0x%08x\n", args.bmc_staging_offset);
	printf("BMC_RECOVERY_OFFSET = 0x%08x\n", args.bmc_recovery_offset);
	printf("PCH_ACTIVE_PFM_OFFSET = 0x%08x\n", args.pch_active_pfm_offset);
	printf("PCH_STAGING_OFFSET = 0x%08x\n", args.pch_staging_offset);
	printf("PCH_RECOVERY_OFFSET = 0x%08x\n", args.pch_recovery_offset);
	printf("AFM_STAGING_OFFSET = 0x%08x\n", args.afm_staging_offset);
	printf("Tx Msg\n");
	printRawData(args.tx_msg, args.tx_msg_len);
}

void parseConfigElements(ARGUMENTS *args)
{
	size_t len = 0;
	char *line = NULL;
	int read;
	FILE *fp;

	fp = fopen(args->pfr_tool_conf, "r");
	if (fp == NULL) {
		printf("Open %s failed\n", args->pfr_tool_conf);
		exit(EXIT_FAILURE);
	}

	while ((read = getline(&line, &len, fp)) != -1) {
		if (strncmp(line, "I2C_BUS", strlen("I2C_BUS")) == 0)
			args->i2c_bus = strtoul(&line[strlen("I2C_BUS") + 1], 0, 0);
		else if (strncmp(line, "ROT_ADDRESS", strlen("ROT_ADDRESS")) == 0)
			args->rot_addr = strtoul(&line[strlen("ROT_ADDRESS") + 1], 0, 0);
		else if (strncmp(line, "BMC_ACTIVE_PFM_OFFSET", strlen("BMC_ACTIVE_PFM_OFFSET")) == 0)
			args->bmc_active_pfm_offset = strtoul(&line[strlen("BMC_ACTIVE_PFM_OFFSET") + 1], 0, 0);
		else if (strncmp(line, "BMC_STAGING_OFFSET", strlen("BMC_STAGING_OFFSET")) == 0)
			args->bmc_staging_offset = strtoul(&line[strlen("BMC_STAGING_OFFSET") + 1], 0, 0);
		else if (strncmp(line, "BMC_RECOVERY_OFFSET", strlen("BMC_RECOVERY_OFFSET")) == 0)
			args->bmc_recovery_offset = strtoul(&line[strlen("BMC_RECOVERY_OFFSET") + 1], 0, 0);
		else if (strncmp(line, "PCH_ACTIVE_PFM_OFFSET", strlen("PCH_ACTIVE_PFM_OFFSET")) == 0)
			args->pch_active_pfm_offset = strtoul(&line[strlen("PCH_ACTIVE_PFM_OFFSET") + 1], 0, 0);
		else if (strncmp(line, "PCH_STAGING_OFFSET", strlen("PCH_STAGING_OFFSET")) == 0)
			args->pch_staging_offset = strtoul(&line[strlen("PCH_STAGING_OFFSET") + 1], 0, 0);
		else if (strncmp(line, "PCH_RECOVERY_OFFSET", strlen("PCH_RECOVERY_OFFSET")) == 0)
			args->pch_recovery_offset = strtoul(&line[strlen("PCH_RECOVERY_OFFSET") + 1], 0, 0);
		else if (strncmp(line, "AFM_STAGING_OFFSET", strlen("AFM_STAGING_OFFSET")) == 0)
			args->afm_staging_offset = strtoul(&line[strlen("AFM_STAGING_OFFSET") + 1], 0, 0);

	}
}

int main(int argc, char *argv[])
{
	uint8_t rot_address_flag = 0;
	uint8_t unprovision_flag = 0;
	uint8_t checkpoint_flag = 0;
	uint8_t read_reg_value = 0;
	uint8_t provision_flag = 0;
	uint8_t write_reg_flag = 0;
	uint8_t read_reg_flag = 0;
	uint8_t bus_flag = 0;
	uint8_t status_flag = 0;
	uint8_t info_flag = 0;
	int8_t spdm_flag = -1;
	ARGUMENTS args = {0};
	uint8_t rot_addr;
	char option = 0;
	uint8_t bus;
	int i;
	int j;
	uint8_t test_case_flag = 0;

	args.pfr_tool_conf = "/usr/share/pfrconfig/aspeed-pfr-tool.conf";
	args.debug_flag = 0;

	if (!argv[1]) {
		usage(stdout, argc, argv);
		exit(EXIT_FAILURE);
	}

	while ((option = getopt_long(argc, argv, short_options, long_options, NULL)) != (char) -1) {
		switch (option) {
		case 'h':
			usage(stdout, argc, argv);
			exit(EXIT_SUCCESS);
			break;
		case 'v':
			printVersion();
			exit(EXIT_SUCCESS);
			break;
		case 'b':
			bus = strtoul(optarg, 0, 0);
			bus_flag = 1;
			break;
		case 'a':
			rot_addr = strtoul(optarg, 0, 0);
			rot_address_flag = 1;
			break;
		case 'c':
			args.pfr_tool_conf = optarg;
			break;
		case 'p':
			args.provision_cmd = optarg;
			provision_flag = 1;
			break;
		case 'u':
			unprovision_flag = 1;
			break;
		case 'k':
			args.checkpoint_cmd = optarg;
			checkpoint_flag = 1;
			break;
		case 'd':
			args.debug_flag = 1;
			break;
		case 'r':
			args.tx_msg[0] = strtoul(optarg, 0, 0);
			args.tx_msg_len += 1;
			read_reg_flag = 1;
			break;
		case 'w':
			args.tx_msg[0] = strtoul(optarg, 0, 0);
			args.tx_msg_len += 1;
			write_reg_flag = 1;
			break;
		case 's':
			status_flag = 1;
			break;
		case 'i':
			info_flag = 1;
			break;
		case 'S':
			if (strcmp(optarg, "enable") == 0)
				spdm_flag = 1;
			else if (strcmp(optarg, "disable") == 0)
				spdm_flag = 0;
			break;
		case 'l':
			args.lms_mode = strtoul(optarg, 0, 10);
			if ((args.lms_mode != 256) && (args.lms_mode != 384)) {
				usage(stdout, argc, argv);
				exit(EXIT_FAILURE);
			}
			break;
		case 'E':
			args.secure_mode = 1;
			break;
		case 'D':
			args.mctp_dst = strtoul(optarg, 0, 0);
			break;
		case 't':
			test_case_flag = strtoul(optarg, 0, 0);
			break;
		case 'n':
			args.mctp_network = strtoul(optarg, 0, 0);
			break;
		default:
			usage(stdout, argc, argv);
			exit(EXIT_FAILURE);
			break;
		}
	}

	parseConfigElements(&args);

	if (optind < argc) {
		args.tx_msg_len += argc - optind;
		for (j = 1, i = optind; i < argc; i++, j++)
			args.tx_msg[j] = strtoul(argv[i], 0, 0);
	}

	if (bus_flag)
		args.i2c_bus = bus;

	if (rot_address_flag)
		args.rot_addr = rot_addr;

	if (args.debug_flag)
		printArguments(args);

#ifdef SUPPORT_SECURE_CONNECTION
	if (args.secure_mode) {
		int ret = access("/sys/class/net/mctpi3c4", F_OK);

		if (ret == 0) {
			if (args.mctp_dst == 0)
				args.mctp_dst = DEFAULT_MCTP_DST_SKT;

			if (args.mctp_network == 0)
				args.mctp_network = DEFAULT_MCTP_NETWORK;

			if (setupSecureConnect(&args)) {
				fprintf(stderr, "Failed to setup secure connection\n");
				// If secure connection can't be used, use SMBus instead
				args.i2c_fd = i2cOpenDev(args.i2c_bus, args.rot_addr);
				args.secure_mode = 0;
			}
		} else {
			fprintf(stderr, "Secure connection isn't supported in this mode\n");
			args.i2c_fd = i2cOpenDev(args.i2c_bus, args.rot_addr);
			args.secure_mode = 0;
		}
	} else {
		args.i2c_fd = i2cOpenDev(args.i2c_bus, args.rot_addr);
	}
#else
	if (args.secure_mode) {
		fprintf(stderr, "Secure connection feature is not enabled\n");
		args.i2c_fd = i2cOpenDev(args.i2c_bus, args.rot_addr);
		args.secure_mode = 0;
	} else {
		args.i2c_fd = i2cOpenDev(args.i2c_bus, args.rot_addr);
	}
#endif

#ifdef SECURE_TEST_CASE
	if (test_case_flag) {
		if (!args.secure_mode) {
			printf("Test case should be executed in secure mode (-E)\n");
			return -1;
		}
		if (test_case_flag == 0xff) {
			/* to run all test cases */
			for (i = 0; i < test_case_flag; i++)
				test_case_handler(&args, i, (i == test_case_flag - 1)?true:false);
		} else
			test_case_handler(&args, test_case_flag, true);
		return 0;
	}
#else
	if (test_case_flag)
		printf("Secure test case is not enabled\n");
#endif

	if (read_reg_flag) {
		if (args.tx_msg_len > 2)
			printf("Invalid read register command\n");
		else {
			if (args.tx_msg_len == 1) {
				read_reg_value = ReadByteData(args, args.tx_msg[0]);
				printf("%02x\n", read_reg_value);
			} else {
				if (args.tx_msg[1] < 1) {
					printf("Invalid length %02x\n", args.tx_msg[1]);
				} else {
					args.rx_msg_len = ReadBlockData(args, args.tx_msg[0],
								args.tx_msg[1], args.rx_msg);
					if (args.rx_msg_len > 0)
						printRawData(args.rx_msg, args.rx_msg_len);
				}
			}
		}
	}

	if (write_reg_flag) {
		if (args.tx_msg_len < 2)
			printf("Invalid write register command\n");
		else
			if (args.tx_msg_len > 2)
				WriteBlockData(args, args.tx_msg[0], args.tx_msg_len - 1,
						&args.tx_msg[1]);
			else
				WriteByteData(args, args.tx_msg[0], args.tx_msg[1]);
	}

	if (provision_flag)
		provision(args);

	if (unprovision_flag)
		unprovision(args);

	if (checkpoint_flag)
		checkpoint(args);

	if (status_flag)
		show_status(args);

	if (info_flag)
		show_info(args);

	if (spdm_flag != -1)
		setSPDMFunction(args, spdm_flag);

	if (args.secure_mode)
		close_secure_session(&args);

	if (args.i2c_fd >= 0)
		close(args.i2c_fd);

	return 0;
}
