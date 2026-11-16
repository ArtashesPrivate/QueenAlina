# Alina’s Tovertuin

## Platform
android

## Users and purpose
Alina, a Dutch-speaking girl in groep 3, learns through short activities, earns stars, and cares for collectible animal friends. Her parent installs the first playable APK.

## Confirmed requirements
- Actual native Android application, no website or WebView.
- Dutch interface, learning activities and speech.
- Approved style 2: plush-like girl avatar and animals.
- Moving/talking/eating/sleeping characters, pet care, persistent stars and shop with more animals.
- Dog, cat, dinosaur and rabbit, plus unlockable animals.

## Implementation decisions
Native Java Android Views with a custom animated illustration view; local storage; device Dutch text-to-speech. Inferred first-version defaults: four starter pets, zero stars, five-question rounds earning five stars, short sessions and no real-money purchases. Exact school reading method is not known. Reading activities practise whole animal words; phoneme teaching is not simulated with general-purpose speech.

## Boundaries
Single local profile. No accounts, ads, analytics, network permission, microphone, or open-ended AI chat. Native device voice availability is checked. Additional grade content and clothing are later work.
