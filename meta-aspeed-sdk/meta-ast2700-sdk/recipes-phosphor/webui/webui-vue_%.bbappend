FILESEXTRAPATHS:append := ":${THISDIR}/${PN}"

SRC_URI:append = " file://0003-webui-vue-support-ast2750-dual-nodes.patch"

SRC_URI:append:ast-irot = " file://0004-webui-vue-poll-TaskMonitor-to-completion-for-multipa.patch"
