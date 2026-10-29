# Follow-up Static Analysis — 8 Ball Pool 56.32.2 MOD APK

**Status:** Static triage only; no code was executed. No APK or extracted proprietary binaries are included in this repository.

## Sample identity

- Outer APK: `8 Ball Pool_56.32.2.apk`
- Outer APK SHA-256: `cc125cbec4ccc9b23da3645d001221a50f0ed4ee3cf313edb979229c13b94f8a`
- Outer archive entries: 5,800
- Embedded archive: `assets/SignatureKiller/origin.apk`
- Embedded archive size: 265,304,207 bytes (about 253 MiB)
- Embedded archive entries: 5,793
- Outer manifest size: 85,368 bytes
- Embedded manifest size: 85,008 bytes

## Findings

### 1. The MOD APK contains an embedded original/base APK

The outer package includes a full APK at `assets/SignatureKiller/origin.apk`. This is an important structural finding: the submitted file is not simply a normal APK with a few extra assets. The embedded archive and outer archive have different manifest sizes and different DEX inventories. This can be consistent with a repackaging/modification workflow, but the exact build process cannot be established from archive metadata alone.

### 2. Outer APK signing identity is different in appearance from a typical official publisher identity

The outer APK includes `META-INF/CRM-RELE.RSA`. Parsing its X.509 certificate yielded:

- Subject / issuer: `C=GB, CN=Hacker World`
- Certificate validity: 2026-10-20 through 2051-10-14
- Public key: RSA 2048-bit
- Signature algorithm: SHA-512 with RSA
- Certificate SHA-256 fingerprint: `A2:42:C3:1D:0D:DD:57:1F:07:D8:2D:7A:03:8D:D4:BE:C2:E1:40:69:B9:3B:58:BF:07:1C:E7:0F:C3:9E:60:54`

The certificate subject is self-asserted metadata, not proof of a verified identity. This inspection extracted and parsed the certificate; it did **not** complete Android's full APK signature verification using `apksigner`, which is not installed in the analysis environment. The embedded archive uses a separate signature entry, `META-INF/ANDROID.RSA`; its certificate was not fully compared in this pass.

### 3. Signature-hooking component is present

The outer APK includes `lib/arm64-v8a/libSignatureKiller.so`. Its printable strings include:

- `Java_bin_mt_signature_KillerApplication_hookApkPath`
- `xhook_register`, `xhook_refresh`
- references to `openat`, `open64`, `openat64`
- ELF symbol-table / hooking diagnostics

These strings support the conclusion that the library includes native hooking functionality associated with APK-path handling. They do not, by themselves, prove which checks are changed at runtime or establish malicious intent. The library name and observed strings warrant caution.

### 4. Other third-party components

The outer package contains libraries named:

- `libHwMods.so`
- `libHackerWorldRuntime.so`
- `libHackerWorldProtection.so`

The names indicate third-party additions or branding. Determining their exact behavior requires decompilation and controlled dynamic analysis.

### 5. Native library / ABI inventory

The archive contains libraries under `arm64-v8a`, `armeabi-v7a`, and `x86_64` locations. Several small libraries also appear under an asset subdirectory with ABI-specific copies. Presence of these directories alone does not prove that all libraries are valid, loadable, or correctly packaged.

## Installation failure: current conclusion

The Android dialog in the user's screenshot says only **“App not installed.”** It is not specific enough to identify the exact failure.

Given the archive contents, a signature conflict with an already-installed official version is a plausible cause: Android normally rejects updating an installed app when the signer does not match. However, this is not proven without the package-installer error. Other possibilities include a package/manifest issue, APK integrity problem, incompatible SDK/ABI, or insufficient storage.

### How to collect the decisive error safely

If Android Debug Bridge is available, connect the phone and capture the install error while attempting installation. Avoid sharing personal device logs publicly without reviewing them first.

```bash
adb logcat -c
adb install "8 Ball Pool_56.32.2.apk"
adb logcat -d | grep -iE 'PackageManager|PackageInstaller|INSTALL_FAILED|INSTALL_PARSE_FAILED'
```

If the original game is installed, do not uninstall it just to test this APK unless the game account is linked and progress recovery is confirmed.

## Recommended follow-up work

1. On a machine with Android SDK Build Tools, run `apksigner verify --verbose --print-certs` on both the outer APK and embedded `origin.apk` to compare signer fingerprints and determine whether signatures validate.
2. Decode both manifests with `apkanalyzer manifest print` or a reputable APK analysis tool, then compare package name, version code/name, SDK levels, permissions, exported components, and application flags.
3. Decompile DEX files for a high-level inventory of added packages/classes and review network access, dynamic loading, accessibility, overlay, and device identifier usage.
4. Inspect native libraries with ELF tools and disassembly in an isolated lab.
5. Use a disposable emulator with no personal accounts for dynamic analysis. Do not connect a modified client to a live game account.

## Safety note

This report is not a malware verdict and not a guarantee that the file is safe. The sample is a third-party modified build with a bundled APK and native hooking-related code. Do not install it on a primary phone or enter credentials into it unless its provenance and behavior have been independently verified. This repository intentionally contains findings only—not the APK, a repackaged build, or instructions for bypassing game integrity checks.
