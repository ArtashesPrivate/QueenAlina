# Verification

## 2026-11-18 — Queen Alina als personage, groepen, alles op één scherm

Wat is veranderd: geen code bij het openen (alleen Voor ouders vraagt 2021), Queen Alina als bewegend SVG-personage in plaats van LOU, overal de naam van het kind (standaard Alina), groep 3 t/m 9 bij Leren met eigen lessen, en elk spel op één telefoonscherm.

- `tools/test-rules.ps1`: 26 sterrencontroles, 60.015 levelcontroles, 21.607 lesvragen, `index.html` gelijk aan `web-src/`.
- `tools/fit-audit.js` op de Android 16-emulator (Pixel 6, 412×915 CSS-pixels): elk tabblad en elk spel geopend, staand en liggend, ook met alle groeiende spellen op level 9 en sommen op level 14, en het tabblad Leren voor groep 3, 6 en 9. Resultaat: niets scrollt, niets valt buiten beeld. Eén fout gevonden en opgelost: bij sommen tot 10.000 duwden vier antwoordknoppen de rij 11 pixels buiten beeld.
- Gevonden en opgelost: de tekst "Tik om te springen!" op het canvas was breder dan een staand scherm, en canvastekst ("Houd je vinger naast LOU") kreeg de naam niet. Canvastekst krijgt nu de naam en krimpt als hij niet past.
- Animatie op het canvas: twee schermafbeeldingen van "Emma springt" 0,3 s na elkaar verschillen in hoofdstand, oogrichting en krullen; hoed en bril blijven op hun plek.
- Naam: op de emulator met profiel "Emma" heten alle tegels "Zorg voor Emma", "Emma springt" enz.; zonder profiel "Zorg voor Alina".
- Apparaattest na een schone installatie: **23 controles geslaagd** (menu zonder code, Alina in de balk, geen LOU op het scherm, hoofd en ogen geanimeerd, groep 9 toont brugklaslessen, stem, stemfragmenten, lettertype, 37 overgenomen sterren, som op level 14, ster, terug-knop, geld op level 5 past, Voor ouders vraagt de code en 2021 opent het).
- Opgelost: het laden van de Nederlandse stem gebeurde op de hoofdthread van Android. Dat gebeurt nu op de achtergrond, zodat het spel bij de eerste start niet even vastloopt.
- Schermen: `alina-menu.png` (Leren met groepkeuze), `alina-springt.png` (Alina in een canvasspel), en van de vorige ronde `alina-leren-groep.png`, `alina-breuken.png`, `alina-begrijpend-lezen.png`, `alina-zorg.png`, `alina-memory-staand.png`, `alina-memory-liggend.png`.
- Niet getest: een fysieke telefoon of tablet; de uitspraak van de apparaatstem op gehoor.

## 2026-11-18 — precieze kopie van Spelen met LOU

Op verzoek van de eigenaar (die LOU zelf heeft gemaakt) is LOU's eigen spelcode offline in de app gezet, met de naam Queen Alina en moeilijkere levels. De vorige, zelfgetekende versie is vervangen.

- `tools/check-levels.js`: 60.015 controles op de gebundelde code (sommen op elk level kloppen, levels 14–16 en 9–10, getallen boven 1000 uitspreken, klok op elke minuut).
- Apparaattest op de Android 16-emulator, na een schone installatie, staand én liggend: 17 controles geslaagd. Titel en codescherm Queen Alina, code 2021 opent het menu, 7 tabbladen, titel loopt niet over de knoppen, 37 sterren uit de vorige versie overgenomen, Android-stem gekoppeld, LOU's stemfragmenten laden en worden tot geluid gedecodeerd, lettertype Baloo 2 laadt offline, plussom op level 14 tussen 1000 en 5000, goed antwoord geeft een ster, Android-terug gaat naar het menu, geld op level 5 (prijs 20–100 euro) met zes munten en briefjes die op het scherm passen.
- Scherm: `web-menu-small.png` (tab Leren, liggend).
- Update: de release-APK van build 9 (0.2.9, versionCode 109) is met `adb install -r` over een bestaande installatie gezet; code, 38 sterren en het gekozen tabblad bleven bewaard.

## 2026-11-18 — dezelfde wereld als Spelen met LOU (vervangen)

Alle tabbladen en spellen staan in de app, onder de naam Queen Alina. Code 2021. Levels lopen door tot voorbij de oorspronkelijke ladder (sommen tot 10.000, tafels 11 en 12, tellen tot 50, klok per minuut, geld tot 100). `LevelsTest` 126 controles. Op de emulator: code, hub, plussom en level geslaagd. Scherm: `phone-hub-small.png`.

Avontuur, race en de keuken zijn speelbaar, maar eenvoudiger dan de volledige werelden in LOU. Ze gebruiken eigen tekeningen, niet de plaatjes van LOU.

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
