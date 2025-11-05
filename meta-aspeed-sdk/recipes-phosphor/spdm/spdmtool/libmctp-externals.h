/*
 * SPDX-FileCopyrightText: Copyright (c)  NVIDIA CORPORATION & AFFILIATES. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#ifndef _LIBMCTP_EXTERNALS_H
#define _LIBMCTP_EXTERNALS_H

#ifdef __cplusplus
extern "C" {
#endif

/**
 * @brief Defines a set of MCTP message tag values that the applications can use
 * to set distinct tags to their request messages. MCTP 1.0 specification only
 * allows for 3 bit tags, so we are limited to values from 0-7.
 */
typedef enum {
	MCTP_TAG_PLDM = 0,
	MCTP_TAG_SPDM = 1,
	MCTP_TAG_VDM = 2,
	MCTP_TAG_NSM = 3,
	MCTP_TAG_NSM_ASYNC = 4,
	MCTP_TAG_NVME = 5,
	MCTP_TAG_NCSI = 6
} mctp_tag_t;

/**
 * @brief Convenience defines for MCTP tag and tag owner masks. Clients can use
 * these against a uint8 to set/check for TO bit and to extract/insert MCTP tags
 * into the first byte of the message sent to/received from the MCTP demux
 * daemon's unix socket.
 */
#define LIBMCTP_TAG_OWNER_MASK 0x08
#define LIBMCTP_TAG_MASK       0x07

#ifdef __cplusplus
}
#endif

#endif /* _LIBMCTP_EXTERNALS_H */
