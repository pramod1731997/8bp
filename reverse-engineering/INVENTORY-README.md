# APK Inventory

This directory contains inventory metadata for the user-provided 8 Ball Pool 56.32.2 MOD APK. It does not contain the proprietary APK binaries or decompiled game source.

## Inventory scope

- Outer archive: 5,800 entries
- Embedded archive: `assets/SignatureKiller/origin.apk`
- DEX files: 21 entries (including `assets/audience_network.dex`)
- Native shared libraries: 38 entries
- Outer archive SHA-256: `cc125cbec4ccc9b23da3645d001221a50f0ed4ee3cf313edb979229c13b94f8a`

## Local inventory files

The accompanying CSV inventories should be generated from the original sample in the analysis environment:

- `8bp_apk_inventory.csv`: path, uncompressed/compressed size, compression type, CRC32, extension and top-level directory for every outer APK entry.
- `8bp_embedded_origin_inventory.csv`: equivalent inventory for the embedded APK.
- `8bp_apk_inventory_README.md`: grouped counts and notable DEX/native entries.

## Important limitation

These are archive-entry inventories, not a decompiled class/package namespace listing. A DEX-aware tool such as JADX or Android SDK `apkanalyzer dex packages` is needed for class-level package names. The generic Android APK Analyzer supports listing files, printing manifests, enumerating DEX files, and listing DEX package/class trees. See the official [APK Analyzer documentation](https://developer.android.com/tools/apkanalyzer).

The inventory is not a malware verdict. Do not install an untrusted modified APK on a primary device.
