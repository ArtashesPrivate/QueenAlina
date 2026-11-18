# Queen Alina: Leer & Speel

Uitgever: **KingApps.com**. Bron: https://github.com/ArtashesPrivate/QueenAlina (openbaar; de eigenaar heeft dit zo gekozen).

Queen Alina is **Spelen en Leren met LOU** als Android-app, met de naam Queen Alina en moeilijkere levels. De eigenaar heeft LOU zelf gemaakt. Het spel zelf is de originele LOU-code, met de luiaard LOU, alle tekeningen en LOU's ingesproken stem. Alles zit in de app en werkt zonder internet.

## Downloaden en installeren

Stabiele downloadlink voor de nieuwste geslaagde build:

**https://github.com/ArtashesPrivate/QueenAlina/releases/latest/download/Queen-Alina.apk**

- Geen GitHub-aanmelding nodig: de repository is openbaar.
- Tik op de link op een Android-telefoon of -tablet (Android 8.0 of nieuwer), en open het gedownloade bestand.
- Android vraagt om toestemming om apps uit deze bron te installeren. Dat is een beveiligingsstap van Android, geen automatische installatie.
- Updates installeer je over de bestaande app heen, omdat elke build met dezelfde sleutel wordt ondertekend. Sterren uit de vorige versie van Queen Alina worden één keer overgenomen.

## Spelen

Precies zoals Spelen met LOU: vul één keer **2021** in, kies een tabblad (LOU, Keuken, Bouwen, Spelletjes, Leren, Sprookjes, Sinterklaas) en een spel. Alleen de titel is anders: **Spelen en Leren met Queen Alina**.

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

## LOU bijwerken

```powershell
python tools/import-lou.py
```

Dit haalt de nieuwste versie van https://spelenmetlou.web.app/ op, met stem en lettertypen, en zet de naam en de moeilijkere levels erin. Past een aanpassing niet meer omdat LOU is veranderd, dan stopt het script met een melding.

## Bouwen

Open de map in Android Studio (JDK 17 of nieuwer, Android SDK platform 36).

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:assembleDebug :app:lintDebug
.\tools\test-rules.ps1
```

`tools/test-rules.ps1` controleert de sterren en de moeilijkere levels (`tools/check-levels.js`, met Node). De apparaattest (`SmokeInstrumentation`) speelt op een emulator: code, menu, stem, lettertype, overgenomen sterren, een som op level 14 en geld op level 5.

## Automatische releases (GitHub Actions)

`.github/workflows/android.yml` doet bij elke push en pull request:

1. Sterren- en leveltests.
2. Bouwen van de ondertekende release-APK, compileren van de apparaattests en Android lint.
3. Controle dat de APK met de vaste sleutel is ondertekend (`bc885dda…`) en dat de `versionCode` hoger is dan eerder.

Alleen na een **geslaagde** build op `main` wordt `Queen-Alina.apk` bijgewerkt in de release **android-latest**, die als "latest" gemarkeerd is. Versienummer: `0.2.<run-nummer>`, `versionCode` = `100 + run-nummer`.

Vereiste secrets (nooit in de code): `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`.

## Projectstructuur

- `app/src/main/assets/web/`: het spel (LOU's `index.html` met aanpassingen, stemfragmenten, lettertypen) en `qa-bridge.js`, dat de Android-stem koppelt.
- `MainActivity.java`: toont het spel schermvullend vanuit de app, zonder internet, en regelt terug-knop, pauze en spraak.
- `GameRules.java`, `GameStore.java`: de opgeslagen sterren van de vorige versie, om over te nemen.
- `res/mipmap-anydpi-v26/`: launcher-icoon met Alina.
- `tools/import-lou.py`: LOU ophalen en aanpassen. `tools/check-levels.js`: levels controleren.
- `verification/`: bewijs van tests en schermen.

## Beperkingen

- Technisch is dit LOU's webcode in een Android-app (offline), geen in Java herschreven spel. Daar is bewust voor gekozen, omdat het precies hetzelfde moet zijn.
- Kleren, kamer en levels uit de vorige Queen Alina-versie gaan niet mee; alleen de sterren.
- Getest op een Android 16-emulator, niet op een fysieke telefoon.
