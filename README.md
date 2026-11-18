# Queen Alina: Leer & Speel

Uitgever: **KingApps.com**. Bron: https://github.com/ArtashesPrivate/QueenAlina (openbaar; de eigenaar heeft dit zo gekozen).

Een echte Android-app in Java met Android Views en Canvas. Geen WebView, website, JavaScript-runtime of externe spelserver, geen account en geen netwerkrechten.

## Downloaden en installeren

Stabiele downloadlink voor de nieuwste geslaagde build:

**https://github.com/ArtashesPrivate/QueenAlina/releases/latest/download/Queen-Alina.apk**

- Geen GitHub-aanmelding nodig: de repository is openbaar, dus de link werkt ook zonder account.
- Tik op de link op een Android-telefoon of -tablet (Android 8.0 of nieuwer), en open het gedownloade bestand.
- Android vraagt om toestemming om apps uit deze bron te installeren. Dat is een beveiligingsstap van Android, geen automatische installatie.
- Updates installeer je over de bestaande app heen. Sterren, dieren, namen en voortgang blijven behouden, omdat elke build met dezelfde sleutel wordt ondertekend.

## Spelen

Alina begint met Bobby de hond, Mila de kat, Dino en Pluis het konijn. Kies **Leren**, kies een spel en beantwoord vijf opdrachten voor vijf sterren.

- Na een **goed** antwoord reageert het spel vrolijk en gaat de volgende opdracht vanzelf verder. Er is geen knop "Volgende opdracht".
- Na een **fout** antwoord krijg je concrete hulp en mag je opnieuw kiezen. Je verliest geen sterren en het spel gaat niet vanzelf verder.
- Iedere som wordt voorgelezen, precies zoals hij op het scherm staat (bijvoorbeeld "Drie plus twee. Hoeveel is dat samen?"). De uitkomst wordt niet voorgelezen. Met de knop **Luister naar de opdracht** hoor je de opdracht opnieuw.
- Reacties wisselen af, bijvoorbeeld "Jaaa, dat klopt!", "Hoera! Goed gerekend!" of "Wauw, dat heb je slim gedaan!". Er zijn aparte reacties voor een afgerond rondje.

In **Winkel** kun je met sterren nieuwe dieren verwelkomen: hamster (20), egel (30), schildpad (40), pony (60), eenhoorn (80) en vos (100). Een aankoop vraagt bevestiging en wordt nooit dubbel afgeschreven. Kies een vriendje onder **Dieren**. Verzorging onder **Thuis** kost geen sterren.

## Leren

- **Rekenbakkerij:** optellen en aftrekken tot 10; ouders kunnen op 20 zetten.
- **Dierenwoorden:** kies het juiste woord bij een dier. Dit is introductie-woordenschat, geen volledig leesprogramma.
- **Toverketting:** patronen met afwisselende vormen afmaken.

## Stem

- Voorlezen gebruikt een **offline Nederlandse tekst-naar-spraak-stem op het apparaat**. Installeer die via Android-instellingen voor tekst-naar-spraak; het spel heeft een knop daarvoor bij **Ouders**.
- Zonder stem blijven alle teksten en spelletjes werken; de opdracht staat dan alleen geschreven.
- Er is geen microfoon en geen vrij gesprek met een AI.
- Stemmen per dier zijn nog **niet** aparte opnamen; alle dieren gebruiken dezelfde Android-stem. Dat staat als open punt in de overdracht.

## Bouwen

Open de map in Android Studio (JDK 17 of nieuwer, Android SDK platform 36).

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:assembleDebug :app:lintDebug
.\tools\test-rules.ps1
```

Lokale builds gebruiken de standaard debug-sleutel. De release-build in CI gebruikt de vaste sleutel uit GitHub-secrets.

## Automatische releases (GitHub Actions)

`.github/workflows/android.yml` doet bij elke push en pull request:

1. Speltesten en Nederlandse teksttests (`tools/GameRulesTest.java`, `tools/LessonTextTest.java`).
2. Bouwen van de ondertekende release-APK, compileren van de apparaattests en Android lint.
3. Controle dat de APK met de vaste sleutel is ondertekend (`bc885dda…`) en dat de `versionCode` hoger is dan eerder.

Alleen na een **geslaagde** build op `main` wordt `Queen-Alina.apk` bijgewerkt in de release **android-latest**, die als "latest" gemarkeerd is. Versienummer: `0.2.<run-nummer>`, `versionCode` = `100 + run-nummer`.

Vereiste secrets (nooit in de code):

- `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`

De sleutel is de Android-debugsleutel waarmee de eerdere 0.1.0-installatie is gemaakt. Die is nodig zodat updates over de bestaande app heen werken. Voor een Play Store-release is een eigen uploadsleutel nodig; die moet dan apart worden beheerd.

## Projectstructuur

- `MainActivity.java`: schermen, leerrondjes, voorlezen en navigatie.
- `LessonText.java`: Nederlandse zinnen voor sommen en de reacties (zonder Android-afhankelijkheid, dus testbaar).
- `CharacterView.java`: frame-gebaseerd personage en verzorgingseffecten.
- `GameRules.java`: pure regels voor sterren, aankopen, voortgang en verzorging.
- `GameStore.java`: lokale voortgang met versie.
- `res/mipmap-anydpi-v26/`: adaptive launcher-icoon met Alina (afgeleid van het goedgekeurde personage).
- `assets/friends.png`, `extras.png`, `extra-actions.png`: sprite-atlassen van het goedgekeurde ontwerp.
- `art/queen-alina-icon-512.png`: winkel- en presentatie-icoon. Gemaakt met `tools/make-icon.py`.
- `verification/`: bewijs van builds, tests en schermen op een emulator.

## Beperkingen (eerlijk)

- Personages bewegen nog met getekende frames en eenvoudige effecten, **geen** gearticuleerd skelet of 3D-rig.
- Hoofdschermen passen op een telefoon van 1080×2400 niet overal volledig zonder scrollen; dat is nog open werk.
- Alle dieren gebruiken één Android-stem; er zijn nog geen aparte stemopnamen per dier.
- Getest op een Android 16-emulator (Google Play-image), niet op een fysieke telefoon of tablet.
