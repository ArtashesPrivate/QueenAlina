# Verification

## 2026-11-18 — speelbare spellen

Na het bekijken van Spelen met LOU (hapjes aantikken, memory, verstoppertje) zijn de tekstkeuzes vervangen door drie tikspellen: Hapjes tellen, Memory en Verstoppertje. Schermen: `phone-feed-game-small.png`, `phone-memory-small.png`. Op de Android 16-emulator: 190 controles geslaagd, inclusief fout hapje zonder straf, juiste hapjes met automatische volgende opdracht, een memory-paar en een fout struikje.

## 2026-11-18 — Queen Alina: Leer & Speel (versie 0.2.x)

Ondertekening en update-pad

- De oude installatie 0.1.0 (`Alinas-Tovertuin-0.1.0.apk`) is ondertekend met de Android-debugsleutel, SHA-256 `bc885dda…`. Die sleutel is nu als GitHub-secret opgeslagen en wordt gebruikt voor elke build.
- Test: 0.1.0 geïnstalleerd, voortgang in de app gezet (37 sterren, aangepaste naam, twee dieren), daarna de nieuwe release-APK met `adb install -r` eroverheen. Resultaat: installatie geslaagd, applicationId gelijk, versionCode 42 > 1, voortgang behouden (`phone-update-from-0.1.0.png`: "Queen Alina", ★ 37, Mila actief).
- Downloadlink `/releases/latest/download/Queen-Alina.apk`: anoniem bereikbaar (HTTP 200, `application/vnd.android.package-archive`, 6,9 MB). De repository staat sinds 16 nov 2026 op openbaar; de eigenaar heeft dat zo gehouden.
- Niet getest: een echte telefoon, en het installeren vanaf de link op een telefoon.

Automatische controles (lokaal en in CI-opzet)

- `tools/GameRulesTest.java`: 26 controles (economie, aankopen, verzorging, voortgang).
- `tools/LessonTextTest.java`: 2255 controles (Nederlandse zinnen voor optellen en aftrekken, uitkomst niet voorgelezen, reacties zonder herhaling achter elkaar, lengte en unieke zinnen).
- `assembleRelease`, `assembleDebugAndroidTest`, `lintDebug`: geslaagd.
- Native instrumentatietest op Android 16-emulator (Google Play-image): **95 controles geslaagd**. Dekt: vijf sommen per rondje met automatische overgang na een goed antwoord, foutantwoord zonder straf en zonder overgang, voorgelezen zin gelijk aan de zichtbare zin, vijf sterren per rondje, aankoop met bevestiging, verzorging, woord- en patroonspel, herstart met behoud van voortgang.

Schermen

- `phone-learn-menu.png` en `phone-math-question.png` (emulator, 1080×2400). Het spelscherm toont de som, de zin die voorgelezen wordt, en de keuzes. Het luisterblok en de feedback vallen op deze hoogte onder de vouw: schermen zonder scroll zijn **nog niet** afgerond.

Niet getest

- Nederlandse stemuitspraak is niet beoordeeld op gehoor en er is geen audio-controle uitgevoerd. De tests controleren de zinnen die worden voorgelezen en de tijdsvangnetten, niet de klank.
- Gearticuleerde animatie: nog niet gewijzigd.
- Fysieke telefoons en tablets.

## Eerdere verificatie — 2026-11-16 (Alina’s Tovertuin 0.1.0)

Historische schermafbeeldingen `phone-home-verified.png`, `phone-learning-verified.png` en `phone-shop-verified.png` tonen de oude naam.

- Android APK en native testAPK gebouwd met AGP 8.13.0 / Gradle 8.14.3 / JDK 21 / compile SDK 36.
- Android lint: geen fouten.
- Pure Java-regels: 26 controles geslaagd.
- Native Android-activiteit: 37 controles geslaagd op een Android 16-emulator (`native-tests.txt`).

`tools/smoke-device.py` is de volledige touch/accessibility-test voor een stabiele emulator of fysiek testtoestel.

## Iconen

- `icon-preview.png`: het icoon in groot, middel en klein formaat. Gemaakt door `tools/make-icon.py` uit het goedgekeurde spritesheet.
