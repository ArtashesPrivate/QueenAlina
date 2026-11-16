# Verification — 2026-11-16

- Android APK and native test APK build successfully with AGP 8.13.0 / Gradle 8.14.3 / JDK 21 / compile SDK 36.
- Android lint: no errors. Remaining warnings concern Dutch-only inline interface strings.
- Pure Java rules: 26 checks passed (reward bounds, duplicate claims/purchases, ownership, progression, care bounds).
- Native Android activity tests: **37 checks passed** on an Android 16 emulator. They exercised four actual learning rounds, wrong-answer retries, exact star rewards, the native purchase confirmation, buying the hamster, feeding, sleeping, the word and pattern screens, and reopening the activity with saved progress.
- APK signature verified using APK Signature Scheme v2; minimum Android API 26.

The Windows-hosted Android emulator repeatedly displayed system-app ANR dialogs during early accessibility/screenshot automation. These were emulator System UI / Pixel Launcher / Digital Wellbeing failures, not application exceptions. The native activity runner bypassed the blocked accessibility bridge and completed all 37 app checks. After stopping the stalled emulator background apps, the home, learning menu and shop were captured and visually checked. Dutch speech playback, animation smoothness, and physical-device touch testing are not yet signed off. Dutch speech depends on an installed offline Dutch voice.

`native-tests.txt` contains the successful native runner output. `tools/smoke-device.py` is the full touch/accessibility test for a stable emulator or physical test device. Run that script on a clean dedicated profile without previously bought animals; it earns the first hamster through four rounds and intentionally changes that test profile.
