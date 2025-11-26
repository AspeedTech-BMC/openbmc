#include <ctype.h>
#include <fcntl.h>
#include <getopt.h>
#include <poll.h>
#include <pthread.h>
#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <linux/types.h>
#include <sys/socket.h>
#include <linux/mctp.h>
#include <netinet/in.h>
#include <errno.h>
#include <string.h>

#define MCTP_BTU 68
#define MCTP_PAYLOAD_SIZE 64

#define SELF_EID 0xA
#define ROT_EID  0xB
#define CPU0_EID 0x1D

#define BMC_I3C_SLAVE_ADDR       0x08

#define MCTP_CONTROL_MSG         0
#define MCTP_VENDOR_MSG          0x7E
#define MCTP_VENDOR_MSG_OP       0xA

#define MCTP_CONTROL_SET_EID          0x1
#define MCTP_CONTROL_DISCOVERY_NOTIFY 0xD

#define INTEL_VENDOR_ID               0x8680
#define MCTP_VENDOR_DOE_REGISTRATION  0x4


#pragma pack(1)

struct i3c_mctp_xfer {
	uint32_t *data;
	uint16_t len;
	uint8_t rnw;
	uint8_t pad[5];
};

struct mctp_i3c_header {
	uint8_t hdr_ver:4;
	uint8_t rsvd:4;
	uint8_t dest_eid;
	uint8_t src_eid;
	uint8_t msg_tag:3;
	uint8_t to:1;
	uint8_t pkt_seq:2;
	uint8_t eom:1;
	uint8_t som:1;
};

struct mctp_i3c_control_msg {
	uint8_t msg_type;
	uint8_t inst_id:5;
	uint8_t rsvd:1;
	uint8_t dbit:1;
	uint8_t rq:1;
	uint8_t command;
};

struct mctp_i3c_set_eid {
	struct mctp_i3c_header mctp_header;
	struct mctp_i3c_control_msg control_header;
	uint8_t op:2;
	uint8_t rsvd:6;
	uint8_t eid;
	uint8_t pec;
};

struct mctp_i3c_doe_msg {
	uint8_t msg_type;
	uint16_t vendor_id;
	uint8_t inst_id:5;
	uint8_t rsvd:1;
	uint8_t dbit:1;
	uint8_t rq:1;
	uint8_t op_code;
	uint32_t seq_num;
	uint8_t doe_cmd;
	uint8_t status;
	uint8_t cpld_reg_addr;
	uint8_t len;
};

struct mctp_i3c_doe_registration {
	struct mctp_i3c_header mctp_header;
	struct mctp_i3c_doe_msg doe_header;
	uint8_t eid;
	uint8_t pec;
};

struct i3c_mctp_packet_data {
	struct mctp_i3c_header header;
	uint8_t payload[MCTP_PAYLOAD_SIZE];
};

struct i3c_mctp_packet {
	struct i3c_mctp_packet_data data;
	uint32_t size;
};
#pragma pack()

const char *sopts = "d:hs";
static const struct option lopts[] = {
	{"device",	required_argument,	NULL,	'd' },
	{"socket",	no_argument,		NULL,	's' },
	{"help",	no_argument,		NULL,	'h' },
	{0, 0, 0, 0}
};

enum mctp_mode {
	MODE_I3C = 0,
	MODE_SOCKET = 1,
};

static enum mctp_mode g_mode = MODE_I3C;
static int fd = -1;
char *dev = NULL;

static void print_usage(const char *name)
{
	fprintf(stderr, "usage: %s options...\n", name);
	fprintf(stderr, "  options:\n");
	fprintf(stderr, "    -d --device       <dev>          device to use.\n");
	fprintf(stderr, "    -s --socket                      use socket interface.\n");
	fprintf(stderr, "    -h --help                        Output usage message and exit.\n");
}

uint8_t crc8 (uint8_t crc, const uint8_t *data, uint8_t len)
{
	int i, j;

	if (data == NULL)
		return crc;

	for (i = 0; i < len; ++i) {
		crc ^= data[i];

		for (j = 0; j < 8; ++j) {
			if ((crc & 0x80) != 0)
				crc = (uint8_t) ((crc << 1) ^ 0x07);
			else
				crc <<= 1;
		}
	}

	return crc;
}

int process_mctp_header( struct i3c_mctp_packet_data *mctp_msg, uint16_t len)
{
	struct mctp_i3c_header *header = &mctp_msg->header;

	if (len < (sizeof(struct mctp_i3c_header) + sizeof(struct mctp_i3c_control_msg))) {
		printf("Invalid message length\n");
		return -1;
	}

	if ((header->hdr_ver != 1) || (header->rsvd != 0)) {
		printf("Invalid hdr version or rsvd bit\n");
		return -1;
	}

	if (!(header->som == 1 && header->eom == 1)) {
		printf("Splitted mctp message is not supported\n");
		return -1;
	}

	return 0;
}

bool is_mctp_control_message_valid(struct mctp_i3c_control_msg *msg, uint16_t len)
{
	if (msg->rq != 1 || msg->rsvd != 0) {
		return false;
	}

	return true;
}

bool is_mctp_vendor_message_valid(struct mctp_i3c_doe_msg *msg, uint16_t len)
{
	if (msg->rq != 1 || msg->rsvd != 0) {
		printf("Invalid header\n");
		return false;
	}

	if (msg->vendor_id != INTEL_VENDOR_ID) {
		printf("msg->vendor_id : %x\n", msg->vendor_id);
		return false;
	}

	if (msg->op_code != MCTP_VENDOR_MSG_OP) {
		printf("msg->op_code : %x", msg->op_code);
		return false;
	}
	return true;
}

void send_mctp_set_eid(struct i3c_mctp_packet_data *mctp_msg, uint16_t len)
{
	uint8_t pec, i3c_addr = (BMC_I3C_SLAVE_ADDR << 1) | 0x01;
	struct mctp_i3c_set_eid *msg = (struct mctp_i3c_set_eid *)mctp_msg;

	msg->mctp_header.dest_eid = 0;
	msg->mctp_header.src_eid = SELF_EID;
	msg->mctp_header.msg_tag = 0;
	msg->mctp_header.to = 1;
	msg->mctp_header.pkt_seq = 0;

	msg->control_header.msg_type = MCTP_CONTROL_MSG;
	msg->control_header.inst_id = 0;
	msg->control_header.rsvd = 0;
	msg->control_header.dbit = 0;
	msg->control_header.rq = 1;
	msg->control_header.command = MCTP_CONTROL_SET_EID;
	msg->op = 0;
	msg->rsvd = 0;
	msg->eid = ROT_EID;

	len = sizeof(struct mctp_i3c_set_eid) - 1;
	pec = crc8(0, &i3c_addr, 1);
	pec = crc8(pec, (uint8_t *)msg, len);
	msg->pec = pec;
	len++;

	if (write(fd, msg, len) < 0) {
		printf("Failed to send mctp set eid\n");
	}
}

int send_doe_registration_res(struct i3c_mctp_packet_data *mctp_msg, uint16_t len)
{
	uint8_t pec, i3c_addr = (BMC_I3C_SLAVE_ADDR << 1) | 0x01;
	struct mctp_i3c_doe_registration *msg = (struct mctp_i3c_doe_registration *)mctp_msg;
	msg->mctp_header.dest_eid = ROT_EID;
	msg->mctp_header.src_eid = CPU0_EID;
	msg->mctp_header.to = 0;
	msg->doe_header.status = 0;

	len = sizeof(struct mctp_i3c_doe_registration) - 1;
	pec = crc8(0, &i3c_addr, 1);
	pec = crc8(pec, (uint8_t *)msg, len);
	msg->pec = pec;
	len++;

	if (write(fd, msg, len) < 0) {
		printf("Failed to send doe registration response\n");
		return -1;
	}

	return 0;
}

void process_mctp_control_message( struct i3c_mctp_packet_data *mctp_msg, uint16_t len)
{
	struct mctp_i3c_control_msg *msg = (struct mctp_i3c_control_msg *)mctp_msg->payload;

	if (!is_mctp_control_message_valid(msg, len))
		return;

	switch (msg->command) {
		case MCTP_CONTROL_DISCOVERY_NOTIFY:
			// Send set eid message
			printf("Received discovery notify\n");
			send_mctp_set_eid(mctp_msg, len);
			break;
		default:
			printf("Drop mctp control message command : %x\n", msg->command);
			break;
	}
}

void process_mctp_vendor_message( struct i3c_mctp_packet_data *mctp_msg, uint16_t len)
{
	struct mctp_i3c_doe_msg *msg = (struct mctp_i3c_doe_msg *)mctp_msg->payload;
	int ret;

	if (!is_mctp_vendor_message_valid(msg, len)) {
		return;
	}

	switch (msg->doe_cmd) {
		case MCTP_VENDOR_DOE_REGISTRATION:
			printf("Received DOE eid registration\n");
			ret = send_doe_registration_res(mctp_msg, len);
			if (ret == 0) {
				printf("EID registration flow completed");
				exit(0);
			}
			break;
		default:
			printf("Drop doe command : %x\n", msg->doe_cmd);
			break;
	}
}

void *mctp_i3c_state_handler(void *arg)
{
	int ret;
	uint8_t buf[MCTP_BTU];
	uint16_t length;
	struct i3c_mctp_packet_data *mctp_msg;
	struct pollfd fds[1];

	fds[0].fd = fd;
	fds[0].events = POLLIN | POLLOUT | POLLPRI | POLLERR;

	while(1) {
		if (poll(fds, 1 , -1) <= 0) {
			continue;
		}
		lseek(fd, 0, SEEK_SET);
		ret = read(fd, buf, sizeof(buf));
		if (ret > 0) {
			printf("I3C Buffer: ");
			for (uint16_t i = 0; i < ret; ++i)
				printf("%02x ", *(buf + i));
			printf("\n");
			length = ret;
			mctp_msg = (struct i3c_mctp_packet_data *)buf;
			ret = process_mctp_header(mctp_msg, length);
			if (ret == 0) {
				if (mctp_msg->payload[0] == MCTP_CONTROL_MSG) {
					process_mctp_control_message(mctp_msg, length);
				} else if (mctp_msg->payload[0] == MCTP_VENDOR_MSG) {
					process_mctp_vendor_message(mctp_msg, length);
				} else {
					// do nothing
					printf("Drop unsupported message\n");
				}
			} else {
				printf("Failed to process mctp header\n");
			}
		} else if (ret < 0) {
			perror("Error :");
		}
	}

	pthread_exit(NULL);
}

#define MCTP_VNDR_HDR_MSG_TYPE 0x7E
struct mctp_vendor_intel_msg_hdr {
	uint16_t vendor_id;
	uint8_t rq_dgram_inst;
	uint8_t msg_op_code;
} __attribute__((__packed__));

struct mctp_vendor_intel_doe {
	struct mctp_vendor_intel_msg_hdr vendor_hdr;
	uint32_t seq_num;
	uint8_t command_code;
	uint8_t status;
	uint8_t address;
	uint8_t length;
} __attribute__((__packed__));

static int init_mctp_socket(void)
{
	int sd = socket(AF_MCTP, SOCK_DGRAM, 0);
	if (sd < 0) {
		perror("socket(AF_MCTP)");
		return -1;
	}

	struct sockaddr_mctp addr = {0};
	memset(&addr, 0, sizeof(addr));
	addr.smctp_family  = AF_MCTP;
	addr.smctp_network = MCTP_NET_ANY;
	addr.smctp_addr.s_addr = MCTP_ADDR_ANY;
	addr.smctp_type    = MCTP_VNDR_HDR_MSG_TYPE;
	addr.smctp_tag     = MCTP_TAG_OWNER;

	if (bind(sd, (struct sockaddr*)&addr, sizeof(addr)) < 0) {
		perror("bind(AF_MCTP)");
		close(sd);
		return -1;
	}

	int val = 1;
	if (setsockopt(sd, SOL_MCTP, MCTP_OPT_ADDR_EXT, &val, sizeof(val)) < 0) {
		perror("setsockopt(MCTP_OPT_ADDR_EXT)");
	}

	return sd;
}

static int mctp_sock_eid_registration(int sd, const struct sockaddr_mctp_ext *addr,
		const uint8_t *buf, const size_t buf_size)
{
	struct mctp_vendor_intel_doe *req = NULL;
	struct mctp_vendor_intel_doe *resp = NULL;
	uint8_t resp_buf[sizeof(*resp) + MAX_ADDR_LEN];
	uint8_t *req_data_ptr;
	uint8_t *res_data_ptr;
	size_t resp_len;

	if (buf_size > sizeof(resp_buf)) {
		return -ENOMSG;
	}
	req = (void*)buf;
	resp = (void*)resp_buf;
	memset(resp, 0x0, sizeof(*resp));
	resp->command_code = req->command_code;
	resp->vendor_hdr.vendor_id = req->vendor_hdr.vendor_id;
	resp->vendor_hdr.rq_dgram_inst = 0;
	resp->vendor_hdr.msg_op_code = req->vendor_hdr.msg_op_code;
	resp->length = req->length;
	req_data_ptr = (uint8_t *)req + sizeof(struct mctp_vendor_intel_doe);
	res_data_ptr = (uint8_t *)resp + sizeof(struct mctp_vendor_intel_doe);
	memcpy(res_data_ptr, req_data_ptr, resp->length);
	resp_len = sizeof(*resp) + resp->length;

	struct sockaddr_mctp resp_addr = {0};
	memcpy(&resp_addr, addr, sizeof(resp_addr));
	resp_addr.smctp_tag &= ~MCTP_TAG_OWNER;

	return sendto(sd, resp, resp_len, 0,
			(const struct sockaddr *)&resp_addr, sizeof(resp_addr));
}

void *mctp_sock_state_handler(void *arg)
{
	uint8_t buf[MCTP_BTU];

	while (1) {
		struct sockaddr_mctp_ext addr;
		struct mctp_vendor_intel_doe *vi_msg = NULL;
		socklen_t alen = sizeof(addr);

		int ret = recvfrom(fd, buf, sizeof(buf), 0,
				(struct sockaddr*)&addr, &alen);

		if (ret < 0) {
			perror("socket recvfrom");
			continue;
		}

		printf("SOCK Buffer: ");
		for (int i = 0; i < ret; i++)
			printf("%02x ", buf[i]);
		printf("\n");
		vi_msg = (struct mctp_vendor_intel_doe *)buf;
		switch (vi_msg->command_code) {
			case MCTP_VENDOR_DOE_REGISTRATION:
				printf("Received DOE eid registration\n");
				mctp_sock_eid_registration(fd, &addr, buf, ret);
				break;
			default:
				printf("Drop doe command : %x\n", vi_msg->command_code);
				continue;
		}
	}

	return NULL;
}

int main(int argc, char *argv[])
{
	pthread_t pthread_mctp;
	int opt;

	if (!argv[1]) {
		print_usage(argv[0]);
		exit(EXIT_FAILURE);
	}
	while ((opt = getopt_long(argc, argv, sopts, lopts, NULL)) != EOF) {
		switch(opt) {
			case 'd':
				dev = optarg;
				g_mode = MODE_I3C;
				break;
			case 's':
				g_mode = MODE_SOCKET;
				break;
			default:
				print_usage(argv[0]);
				exit(EXIT_FAILURE);
		}
	}

	if (g_mode == MODE_I3C) {
		fd = open(dev, O_RDWR);

		if (fd < 0) {
			printf("Failed to open i3c dev\n");
			print_usage(argv[0]);
			exit(EXIT_FAILURE);
		}

		pthread_create(&pthread_mctp, NULL, mctp_i3c_state_handler, NULL);
	} else if (g_mode == MODE_SOCKET) {
		fd = init_mctp_socket();
		if (fd < 0) {
			printf("Failed to open mctp socket\n");
			print_usage(argv[0]);
			exit(EXIT_FAILURE);
		}
		pthread_create(&pthread_mctp, NULL, mctp_sock_state_handler, NULL);
	} else {
		printf("Invalid mode\n");
		exit(EXIT_FAILURE);
	}

	pthread_join(pthread_mctp, NULL);

	if (fd >= 0) {
		close(fd);
	}

	return 0;
}
