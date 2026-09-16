
/*
 * Copyright (c) 2023 ASPEED Technology Inc.
 *
 * SPDX-License-Identifier: MIT
 */

#pragma once
#include "arguments.h"
#include "i2c_utils.h"
#include <stdbool.h>
#include <string.h>
#ifdef SUPPORT_SECURE_CONNECTION
#include "library/spdm_lib_config.h"
#include "internal/libspdm_common_lib.h"
#include "library/spdm_requester_lib.h"
#include "library/spdm_transport_none_lib.h"
#include "library/spdm_transport_mctp_lib.h"
#include "industry_standard/mctp.h"
#include "command.h"
#include "spdm_emu.h"
#endif

#define DEFAULT_MCTP_DST_SKT 0x7b
#define DEFAULT_MCTP_NETWORK 0x4
#define VENDOR_DEFINED_MSG_TYPE 0x7e
#define INTEL_MESSAGE_OPCODE 0xa
#define PCIE_VENDOR_ID htons(0x8086)
#define MCTP_IC_FLAG 0x80

#define HEART_BEAT_PERIOD 10 // the unit of heart beat is second

#pragma pack(1)
typedef struct {
	uint8_t msg_type:7;
	uint8_t ic:1;
	uint16_t pcie_vendor_id;
	uint8_t instance_id:5;
	uint8_t rvsd:1;
	uint8_t dbit:1;
	uint8_t rq:1;
	uint8_t msg_opcode;
} vendor_def_msg_hdr;

typedef struct {
	vendor_def_msg_hdr msg_hdr;
	uint32_t seq_num;
	uint8_t command;
	uint8_t status;
	uint8_t address;
	uint8_t length;
} intel_pfr_doe_header;

typedef struct {
	intel_pfr_doe_header hdr;
	uint8_t data[64];
} vendor_def_msg;

typedef enum {
	CMD_UNUSED,
	CMD_WRITE_DATA,
	CMD_READ_DATA
} opcode;

#pragma pack(4)
struct spdm_mctp_ctx {
	struct mctp *mctp;
	uint16_t len;
	void *rx_buf;
	void *prot;
	void *priv_binding;
};

#ifdef SUPPORT_SECURE_CONNECTION
extern int m_socket;
extern int m_socket_org;
extern int m_mctp_secure;
extern void *m_spdm_context;
extern void *m_scratch_buffer;
extern struct sockaddr_mctp my_address;
extern struct sockaddr_mctp my_address_secure;
extern bool isSessionCreated;
extern uint32_t debug_level;
extern libspdm_return_t do_authentication_via_spdm(void);
#endif

void *spdm_client_init(void);
void setSPDMFunction(ARGUMENTS args, int enabled);
int setupSecureConnect(ARGUMENTS *args);
int close_secure_session(ARGUMENTS *args);
int build_vendor_msg(vendor_def_msg *msg, opcode op,
				size_t size, uint8_t *value);
int process_secure_msg(vendor_def_msg *msg, size_t data_size,
				uint8_t *rsp, size_t *rsp_size);
void test_case_handler(ARGUMENTS *args, int test_case_id, bool close_session);