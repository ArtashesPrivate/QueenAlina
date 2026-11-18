# Queen Alina: Leer & Speel

Uitgever: **KingApps.com**. Bron: https://github.com/ArtashesPrivate/QueenAlina (openbaar; de eigenaar heeft dit zo gekozen).

Queen Alina is **Spelen en Leren met LOU** als Android-app, met Queen Alina als hoofdpersonage, lessen per schoolgroep en moeilijkere levels. De eigenaar heeft LOU zelf gemaakt. Het spel is de originele LOU-code met aanpassingen; LOU's tekeningen van de andere figuren en LOU's ingesproken stem zitten er nog in. Alles zit in de app en werkt zonder internet.

## Downloaden en installeren

Stabiele downloadlink voor de nieuwste geslaagde build:

**https://github.com/ArtashesPrivate/QueenAlina/releases/latest/download/Queen-Alina.apk**

- Geen GitHub-aanmelding nodig: de repository is openbaar.
- Tik op de link op een Android-telefoon of -tablet (Android 8.0 of nieuwer), en open het gedownloade bestand.
- Android vraagt om toestemming om apps uit deze bron te installeren. Dat is een beveiligingsstap van Android, geen automatische installatie.
- Updates installeer je over de bestaande app heen, omdat elke build met dezelfde sleutel wordt ondertekend. Sterren uit de vorige versie van Queen Alina worden één keer overgenomen.

## Spelen

De app opent meteen in het menu, zonder code. Alleen **⚙️ Voor ouders** vraagt de code **2021**. Kies een tabblad (Alina, Keuken, Bouwen, Spelletjes, Leren, Sprookjes, Sinterklaas) en een spel. Titel: **Spelen en Leren met Queen Alina**.

### Queen Alina

LOU de luiaard is vervangen door Queen Alina: een lief meisje met bruine krullen, blauwe ogen, een bloem in het haar en een lila jurk. Ze is getekend als SVG in losse delen (hoofd, ogen, mond, armen, krullen, jurk), die apart bewegen: ademen, hoofd kantelen, knipperen, rondkijken, krullen en jurk zwaaien, praten met haar mond, juichen bij een goed antwoord en nee schudden bij een fout. In de spellen die op een canvas tekenen (springen, avontuur, racen, puzzel, stempels) wisselt ze steeds tussen houdingen, zodat ze daar ook beweegt. Kleren en spullen uit de winkel passen nog op dezelfde plek.

### De naam van het kind

Overal waar LOU stond, in tekst, knoppen, voorgelezen zinnen en tekst op het canvas, staat de naam van het profiel dat speelt. Zonder profiel is dat **Alina**.

## Leren per groep

Bij **Leren** kies je groep 3 t/m 8 (de bovenste groep van de basisschool). Elke groep heeft eigen lessen naar de kerndoelen en tussendoelen van het basisonderwijs (SLO), en sommen, klok en geld beginnen op het niveau van die groep. Brugklasstof (machten, wortels, vergelijkingen) zit er niet in; groep 9 bestaat niet meer:

| Groep | Lessen |
| --- | --- |
| 3 | plussen en minnen, woorden lezen, letters schrijven, welke letter, klokkijken, geld |
| 4 | + tafels en delen, spelling, begrijpend lezen |
| 5 | + breuken, meten (lengte, gewicht, inhoud) |
| 6 | + kommagetallen, werkwoorden (d/t) |
| 7 | + procenten, negatieve getallen, Engels |
| 8 | dezelfde lessen als groep 7, maar sommen en breuken beginnen op een hoger niveau |

Elke les heeft levels; je hebt drie hartjes en gaat na vijf goede antwoorden een level hoger. `tools/check-lessons.js` controleert ruim 22.000 vragen: het goede antwoord staat tussen de keuzes, geen dubbele keuzes, kommagetallen met een komma.

## Telefoons

Elk spel past op één scherm, staand en liggend, zonder scrollen. Rasters zoals memory kiezen zelf het aantal kolommen waarbij de kaartjes het grootst zijn; plaatjes blijven binnen hun tegel.

## Moeilijkere levels

LOU houdt per spel een level bij. Queen Alina gaat verder waar LOU stopt:

| Spel | LOU stopt bij | Queen Alina gaat door met |
| --- | --- | --- |
| Plus- en minsommen | Grote sommen tot 1000 | Tot 5000, tot 10.000, wat mist er? tot 1000 |
| Keer- en deelsommen | Grote keersommen | Tafel van 11 en 12, sommen tot 240 |
| Tellen | tot 20 | tot 25 en tot 30 |
| Memory | 8 paren | 10 paren |
| Puzzelen | 5×5 stukjes | 6×5 en 6×6 |
| Klokkijken | kwartieren | per 5 minuten, en op de minuut |
| Geld tellen | tot 20 euro | tot 50 euro (briefje van 20), tot 100 euro (briefje van 50) |

Je komt er vanzelf door te spelen. Bij **⚙️ Voor ouders** (code 2021) zet de knop **🚀 Moeilijke levels** ze meteen aan.

## Stem

LOU's eigen ingesproken zinnen (1559 fragmenten) zitten in de app. Zinnen waarvoor geen opname is, zoals grote getallen boven de 1000, spreekt de Nederlandse stem van het apparaat uit. Geen microfoon, geen gesprek met een AI.

## Code van de aanpassingen

De grote aanpassingen staan in `web-src/`: `alina.js`/`alina.css` (het personage, de naam, de animatie), `leren.js`/`leren.css` (groepen en lessen) en `fit.js`/`fit.css` (passen op de telefoon). Ze staan tussen `/* QA:BEGIN … */` en `/* QA:END … */` in `index.html`. Na een wijziging in `web-src/`:

```powershell
python tools/build-web.py
```

## LOU bijwerken

```powershell
python tools/import-lou.py
```

Dit haalt de nieuwste versie van https://spelenmetlou.web.app/ op, met stem en lettertypen. Het voegt die samen met Queen Alina via een drieweg-samenvoeging (`git merge-file`), met `web-src/lou-original.html` als basis: wat LOU veranderde en wat wij veranderden blijft allebei. Botsen ze, dan stopt het script en staan de botsingen in `index.html.merge`.

## Bouwen

Open de map in Android Studio (JDK 17 of nieuwer, Android SDK platform 36).

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:assembleDebug :app:lintDebug
.\tools\test-rules.ps1
```

`tools/test-rules.ps1` controleert de sterren, de levels (`tools/check-levels.js`), de lessen (`tools/check-lessons.js`) en of `index.html` gelijk is aan `web-src/`. De apparaattest (`SmokeInstrumentation`, 23 controles) speelt op een emulator: menu zonder code, Alina en haar animatie, geen LOU op het scherm, groep kiezen, stem, lettertype, overgenomen sterren, een som op level 14, geld op level 5 en de oudercode.

`tools/dev-audit.ps1 [-Landscape] [-Level 9]` bouwt een debug-APK, zet hem op de emulator en opent elk tabblad en elk spel om te controleren dat niets scrollt of buiten beeld valt.

## Automatische releases (GitHub Actions)

`.github/workflows/android.yml` doet bij elke push en pull request:

1. Sterren-, level- en lestests, en controle dat `index.html` gelijk is aan `web-src/`.
2. Bouwen van de ondertekende release-APK, compileren van de apparaattests en Android lint.
3. Controle dat de APK met de vaste sleutel is ondertekend (`bc885dda…`) en dat de `versionCode` hoger is dan eerder.

Alleen na een **geslaagde** build op `main` wordt `Queen-Alina.apk` bijgewerkt in de release **android-latest**, die als "latest" gemarkeerd is. Versienummer: `0.2.<run-nummer>`, `versionCode` = `100 + run-nummer`.

Vereiste secrets (nooit in de code): `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`.

## Projectstructuur

- `app/src/main/assets/web/`: het spel (LOU's `index.html` met aanpassingen, stemfragmenten, lettertypen) en `qa-bridge.js`, dat de Android-stem koppelt.
- `web-src/`: de Queen Alina-aanpassingen, en `lou-original.html` als basis voor het samenvoegen.
- `MainActivity.java`: toont het spel schermvullend vanuit de app, zonder internet, en regelt terug-knop, pauze en spraak.
- `GameRules.java`, `GameStore.java`: de opgeslagen sterren van de vorige versie, om over te nemen.
- `res/mipmap-anydpi-v26/`: launcher-icoon met Alina.
- `tools/build-web.py`: `web-src/` in het spel zetten. `tools/import-lou.py`: nieuwe LOU samenvoegen. `tools/check-levels.js`, `tools/check-lessons.js`: levels en lessen controleren. `tools/fit-audit.js`: alles op één scherm.
- `verification/`: bewijs van tests en schermen.

## Beperkingen

- Technisch is dit LOU's webcode in een Android-app (offline), geen in Java herschreven spel. Daar is bewust voor gekozen, omdat het precies hetzelfde moet zijn.
- Kleren, kamer en levels uit de vorige Queen Alina-versie gaan niet mee; alleen de sterren.
- Getest op een Android 16-emulator (Pixel 6-formaat, staand en liggend), niet op een fysieke telefoon of tablet.
- LOU's ingesproken stem zegt geen kindernamen. Zinnen met de naam spreekt de Nederlandse stem van het apparaat uit, dus de stem wisselt soms.
- De andere figuren (vriendjes, dieren) en de achtergronden zijn nog LOU's tekeningen. Puzzelplaatjes met Alina zijn stilstaand.
- De lessen volgen de gangbare leerlijnen per groep, maar zijn niet vergeleken met een bepaalde lesmethode van school.
