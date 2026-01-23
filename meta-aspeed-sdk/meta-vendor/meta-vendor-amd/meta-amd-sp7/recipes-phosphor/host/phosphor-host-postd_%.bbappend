FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SNOOP_DEVICE = "aspeed-lpc-pcc0"
POST_CODE_BYTES = "8"

# AMD DCSCM is using AST2700 single node. Remove meta-ast2700-sdk lpcsnoop1.service
SRC_URI:remove = " file://lpcsnoop1.service"
SYSTEMD_SERVICE:${PN}:remove = " lpcsnoop1.service"
