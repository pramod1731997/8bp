# 8 Ball Pool 56.32.2 APK — Initial Static Triage

> **Scope:** Metadata and archive-level inspection only. This is not a full decompilation, dynamic analysis, malware verdict, or confirmation of why Android refused installation. The APK is a third-party modified build; do not install it on a device containing sensitive accounts.

## Sample

- File name: `8 Ball Pool_56.32.2.apk`
- Size: approximately 449 MiB (file size reported by the local filesystem: 449M)
- SHA-256: `cc125cbec4ccc9b23da3645d001221a50f0ed4ee3cf313edb979229c13b94f8a`
- Container: ZIP/APK archive
- Archive entries: 5,800
- Android manifest entry: present (85,368 bytes; binary Android XML, not decoded in this pass)
- DEX files: 20, including `classes.dex` through `classes20.dex` and `assets/audience_network.dex`
- Signature-related entries: `META-INF/CRM-RELE.RSA`, `META-INF/CRM-RELE.SF`, and `META-INF/MANIFEST.MF` are present. Their presence alone does **not** establish that Android considers the APK signature valid or that it matches the official game's signer.

## Notable native libraries

The following paths were observed in the archive:

- `lib/arm64-v8a/libHwMods.so` (1,780,688 bytes)
- `lib/arm64-v8a/libSignatureKiller.so` (21,184 bytes)
- `lib/arm64-v8a/libHackerWorldRuntime.so` (804,136 bytes)
- `lib/arm64-v8a/libHackerWorldProtection.so` (359,456 bytes)

The names suggest third-party modification/protection components, but names alone are not proof of behavior or maliciousness. A small string sample from `libSignatureKiller.so` includes a JNI entry named `Java_bin_mt_signature_KillerApplication_hookApkPath`, references to `xhook_register` / `xhook_refresh`, and ELF hooking-related diagnostics. This is a reason to examine the component carefully, not a definitive malware finding.

## Why installation may fail

The screenshot supplied with the sample showed Android's generic **“App not installed”** dialog. The precise cause cannot be inferred from that dialog alone. Plausible causes include:

1. A signing-certificate mismatch with an already-installed official build.
2. A malformed, repackaged, or internally inconsistent APK.
3. Device/OS compatibility or package configuration problems.
4. Insufficient storage or a package installer failure.
5. A package split or ABI issue, depending on how the file was assembled.

The archive contains both ARM64 and ARM32 native library paths, as well as some x86_64 entries. This does not by itself prove that ABI compatibility is correct.

## Recommended next analysis

1. Decode `AndroidManifest.xml` with a reputable Android analysis tool and record package name, version code/name, min/target SDK, declared permissions, exported components, and debuggable flags.
2. Verify the APK signature with Android Build Tools (`apksigner verify --verbose --print-certs`) and compare the signer fingerprint against a known-good official APK obtained from a trusted source.
3. Inventory all DEX and native libraries; inspect strings/imports and identify network endpoints and dynamic code loading, distinguishing indicators from confirmed behavior.
4. Run static and dynamic analysis only in an isolated emulator/sandbox with no personal accounts or sensitive data.
5. For the install error, collect the exact Package Installer / `adb logcat` failure (for example, `INSTALL_PARSE_FAILED`, `INSTALL_FAILED_UPDATE_INCOMPATIBLE`, or `INSTALL_FAILED_NO_MATCHING_ABIS`) before choosing a fix.

## Safety and distribution

This repository report intentionally does not include the APK, a repackaged build, proprietary game source, or instructions to bypass game integrity checks or server-side protections. Preserve the original sample privately and use its SHA-256 to identify it. Do not treat this preliminary triage as a clean bill of health.

## Commands for a local lab

```bash
sha256sum "8 Ball Pool_56.32.2.apk"
apksigner verify --verbose --print-certs "8 Ball Pool_56.32.2.apk"
apkanalyzer manifest print "8 Ball Pool_56.32.2.apk"
```

Record tool versions and outputs with any follow-up findings so the analysis can be reproduced.
