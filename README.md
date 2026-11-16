# Alina’s Tovertuin — native Android first playable

Publisher account: **KingApps.com**. Proposed store name: **Queen Alina: Leer & Speel** (see `BRANDING.md`). Source repository: https://github.com/ArtashesPrivate/QueenAlina.

An actual native Android app written in Java using Android Views and Canvas. No WebView, website, JavaScript runtime, remote game server, account, or network permission.

## Install and play

Open `Alinas-Tovertuin-0.1.0.apk` on an Android phone or tablet running Android 8.0 or newer. Allow installation from the app that opened the file when Android asks. This is a locally signed debug build for personal testing, not a Play Store release. Keep the original signing key if you want future updates to retain this installation's progress.

Alina starts with Bobby the dog, Mila the cat, Dino, and Pluis the rabbit. Tap **Leren**, choose an activity, and complete five questions to earn five stars. Incorrect answers can be retried. In **Winkel**, use stars to welcome a hamster (20), hedgehog (30), turtle (40), pony (60), unicorn (80), or fox (100). Purchases ask for confirmation and cannot charge twice for an owned animal. Choose a friend under **Dieren**, then feed, brush, play, or rest under **Thuis**. Care costs no stars. Progress is automatically saved on the device.

## Learning and speech

- **Rekenbakkerij:** addition and subtraction within 10; parents can switch to 20.
- **Dierenwoorden:** match whole written Dutch animal names to images. This is introductory vocabulary/reading practice, not a complete reading curriculum or phoneme tutor.
- **Toverketting:** complete alternating shape-and-colour patterns.
- Dutch interface regardless of the phone's language. Spoken prompts and scripted pet dialogue use an installed **offline Dutch Android text-to-speech voice**. There is no microphone or free-form conversation. Install Dutch voice data under the phone's text-to-speech settings if unavailable, then reopen the game. The parent screen links to these settings. Text and gameplay remain available without a voice.
- Native pose animation: all characters have neutral, walking, talking, and sleeping frames. Feeding adds movement and a bowl; grooming adds a brush. These are simple frame animations, not fully rigged 3D models.

**Ouders** on the home screen uses a simple arithmetic gate. It provides sound, maths difficulty, and progress information. The gate is a child-friendly separation, not a security mechanism. The world displays a level that rises with learning rounds; full avatar growth, clothing, decorative purchases, advanced school content, and richer animation packs are future extensions.

## Build

Open this directory in Android Studio. Use its bundled JDK (17 or newer), install Android SDK platform 36, and let Android Studio create `local.properties` with your SDK location.

Windows:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:assembleDebug :app:lintDebug
.\tools\test-rules.ps1
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`.

Build dependencies: Android Gradle Plugin 8.13.0 and Gradle 8.14.3. No application libraries or third-party SDKs. A first build on another machine downloads the build tools; gameplay itself needs no internet.

## Project structure

- `MainActivity.java`: native screens, learning rounds, speech, and navigation.
- `CharacterView.java`: frame-based character rendering and care effects.
- `GameRules.java`: pure economy/progression/care rules.
- `GameStore.java`: versioned local progress storage.
- `assets/friends.png`, `assets/extras.png`, `assets/extra-actions.png`: AI-generated game sprite atlases based on the approved designs.
- `art/approved-animation-sheet.png`: the approved visual reference.
- `tools/GameRulesTest.java`: regression checks for rewards, purchase bounds, duplicate claims, pet selection, and care limits.
- `verification/`: build/test evidence and device screenshots when available.

Progress is local. Uninstalling or clearing Android app data removes it. No destructive reset control is exposed to Alina.

## GitHub builds and publishing

`.github/workflows/android.yml` builds the APK, compiles native device tests, runs Android lint and the game-rule checks for pushes and pull requests. Download `Alinas-Tovertuin-Android` from the Actions run. Pushing a version tag such as `v0.1.0` creates a private GitHub prerelease with the APK after a successful build. This distributes an Android test build; it does not deploy a website or publish to Google Play.

Before long-term update distribution, configure a stable dedicated signing key. Default debug keys on fresh GitHub runners can differ between builds; Android will reject an update signed by another key. Never commit signing keys or passwords. A production Play release additionally needs its final application ID, upload signing key, signed AAB, store listing and Play Console setup.

To use a build directory outside a cloud-synced folder: `./gradlew -PalinaBuildDir=/absolute/local/path :app:assembleDebug`.
