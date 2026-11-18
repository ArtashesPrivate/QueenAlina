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

Open de app en vul **2021** in. Daarna kies je een tabblad en een spel. Een goed antwoord levert een ster op en telt mee voor het level. Drie fouten en het level begint opnieuw, zonder dat je sterren kwijtraakt. Sterren gebruik je om Alina aan te kleden en haar kamer in te richten.

## Leren

De indeling volgt Spelen met LOU, met de naam **Queen Alina**. De code om te beginnen is **2021**.

Tabbladen: Alina, Keuken, Bouwen, Spelletjes, Leren, Sprookjes en Sinterklaas. Daarin zitten dezelfde soorten spellen: zorgen, voeren, nazeggen, verstoppertje, puzzel, aankleden, album, kamer, feest, bed, pizza, boterham, koekjes, taart, smoothie, ijs, toren, vormen, vrij bouwen, muur, memory, tellen, kleuren, ballonnen, springen, avontuur, race, muziek, piano, kleurplaat, tekenen, plus, min, keer, delen, letters, klok, geld, sprookjes en Sinterklaas.

Elk spel heeft levels en drie hartjes. Vijf goede antwoorden (drie bij een actiespel) is een level omhoog. Een level blijft bewaard. Bij **Ouders** (opnieuw code 2021) kun je de zware levels aanzetten of alles terugzetten naar level 1.

De zware levels gaan verder dan de gewone ladder: plussommen en minsommen tot 10.000 en met vier getallen, tafels van 11 en 12, tellen tot 50, de klok tot op de minuut, betalen tot 100 euro, en memory met tien paren.

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

- Avontuur, race en de keuken zijn speelbaar, maar eenvoudiger dan de volledige werelden en tekenstappen van Spelen met LOU. De tekeningen zijn van ons, niet de plaatjes van LOU.
- Het menu scrolt, omdat er veel spellen op staan. Een spel zelf past op één scherm.
- Er is één Nederlandse Android-stem, geen aparte stem per dier.
- Getest op een Android 16-emulator, niet op een fysieke telefoon.
