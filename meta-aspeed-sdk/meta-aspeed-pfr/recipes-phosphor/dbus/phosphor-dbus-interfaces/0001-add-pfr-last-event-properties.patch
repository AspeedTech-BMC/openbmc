From 9dadeee9dee5a87df3ab31595b819f246335690f Mon Sep 17 00:00:00 2001
From: VinceChang6637 <vince_chang@aspeedtech.com>
Date: Tue, 9 Jul 2024 13:14:14 +0800
Subject: [PATCH] add pfr last event properties

Add PFR LastEvents for pfr-manager.

Upstream-Status: Pending

Signed-off-by: VinceChang6637 <vince_chang@aspeedtech.com>
---
 .../PFR/LastEvents/meson.build                | 40 +++++++++++++++++++
 gen/xyz/openbmc_project/PFR/meson.build       | 25 ++++++++++++
 .../PFR/LastEvents.interface.yaml             | 25 ++++++++++++
 3 files changed, 90 insertions(+)
 create mode 100644 gen/xyz/openbmc_project/PFR/LastEvents/meson.build
 create mode 100644 yaml/xyz/openbmc_project/PFR/LastEvents.interface.yaml

diff --git a/gen/xyz/openbmc_project/PFR/LastEvents/meson.build b/gen/xyz/openbmc_project/PFR/LastEvents/meson.build
new file mode 100644
index 000000000..2739796ec
--- /dev/null
+++ b/gen/xyz/openbmc_project/PFR/LastEvents/meson.build
@@ -0,0 +1,40 @@
+# Generated file; do not modify.
+
+sdbusplus_current_path = 'xyz/openbmc_project/PFR/LastEvents'
+
+generated_sources += custom_target(
+    'xyz/openbmc_project/PFR/LastEvents__cpp'.underscorify(),
+    input: [
+        '../../../../../yaml/xyz/openbmc_project/PFR/LastEvents.interface.yaml',
+    ],
+    output: [
+        'common.hpp',
+        'server.hpp',
+        'server.cpp',
+        'aserver.hpp',
+        'client.hpp',
+    ],
+    depend_files: sdbusplusplus_depfiles,
+    command: [
+        sdbuspp_gen_meson_prog,
+        '--command',
+        'cpp',
+        '--output',
+        meson.current_build_dir(),
+        '--tool',
+        sdbusplusplus_prog,
+        '--directory',
+        meson.current_source_dir() / '../../../../../yaml',
+        'xyz/openbmc_project/PFR/LastEvents',
+    ],
+    install: should_generate_cpp,
+    install_dir: [
+        get_option('includedir') / sdbusplus_current_path,
+        get_option('includedir') / sdbusplus_current_path,
+        false,
+        get_option('includedir') / sdbusplus_current_path,
+        get_option('includedir') / sdbusplus_current_path,
+    ],
+    build_by_default: should_generate_cpp,
+)
+
diff --git a/gen/xyz/openbmc_project/PFR/meson.build b/gen/xyz/openbmc_project/PFR/meson.build
index add0dc83a..cf94db801 100644
--- a/gen/xyz/openbmc_project/PFR/meson.build
+++ b/gen/xyz/openbmc_project/PFR/meson.build
@@ -1,5 +1,6 @@
 # Generated file; do not modify.
 subdir('Attributes')
+subdir('LastEvents')
 
 sdbusplus_current_path = 'xyz/openbmc_project/PFR'
 
@@ -27,3 +28,27 @@ generated_markdown += custom_target(
     build_by_default: should_generate_markdown,
 )
 
+generated_markdown += custom_target(
+    'xyz/openbmc_project/PFR/LastEvents__markdown'.underscorify(),
+    input: [
+        '../../../../yaml/xyz/openbmc_project/PFR/LastEvents.interface.yaml',
+    ],
+    output: ['LastEvents.md'],
+    depend_files: sdbusplusplus_depfiles,
+    command: [
+        sdbuspp_gen_meson_prog,
+        '--command',
+        'markdown',
+        '--output',
+        meson.current_build_dir(),
+        '--tool',
+        sdbusplusplus_prog,
+        '--directory',
+        meson.current_source_dir() / '../../../../yaml',
+        'xyz/openbmc_project/PFR/LastEvents',
+    ],
+    install: should_generate_markdown,
+    install_dir: [inst_markdown_dir / sdbusplus_current_path],
+    build_by_default: should_generate_markdown,
+)
+
diff --git a/yaml/xyz/openbmc_project/PFR/LastEvents.interface.yaml b/yaml/xyz/openbmc_project/PFR/LastEvents.interface.yaml
new file mode 100644
index 000000000..11db8713e
--- /dev/null
+++ b/yaml/xyz/openbmc_project/PFR/LastEvents.interface.yaml
@@ -0,0 +1,25 @@
+description: >
+    Provides the last event parameters of PFR.
+
+properties:
+    - name: lastMajorErr
+      type: byte
+      description: >
+          Value indicating error code observed on the system.
+
+    - name: lastMinorErr
+      type: byte
+      description: >
+          Value indicating minor error code observed on the system.
+
+    - name: lastPanicCount
+      type: byte
+      description: >
+          Number of Panic events (T0 to T-1 transitions) since last AC-on. Read-only for
+          external agents.
+
+    - name: lastRecoveryCount 
+      type: byte
+      description: >
+          Numbers of FW Recovery actions since last AC-on, read-only for external
+          agents.
-- 
2.43.0

