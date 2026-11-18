package nl.alina.tovertuin;

import java.util.Random;

/**
 * Dutch spoken prompts and varied reactions. Pure Java, independent of Android, so the
 * wording and variety rules run in the CI test step.
 *
 * Spoken prompts never contain the answer. Text on screen and speech always come from the same question object.
 */
public final class LessonText {
    private static final String[] NUMBERS = {
        "nul","een","twee","drie","vier","vijf","zes","zeven","acht","negen","tien",
        "elf","twaalf","dertien","veertien","vijftien","zestien","zeventien","achttien","negentien","twintig"
    };

    /** Short, warm reactions after a correct answer. Spoken, then the next question follows. */
    public static final String[] CORRECT = {
        "Jaaa, dat klopt!",
        "Hoera! Goed gerekend!",
        "Wauw, dat heb je slim gedaan!",
        "Dat ging goed! High five!",
        "Gelukt! We kunnen het samen!",
        "Precies goed, Alina!",
        "Super, dat was het juiste antwoord!",
        "Knap gedaan, lieverd!",
        "Dat zit goed!",
        "Mooi zo, wat ben jij slim!",
        "Ja, goed zo!",
        "Dat was raak!",
        "Jouw vriendje is heel blij met jou!",
        "Goed gedaan, ga zo door!"
    };

    /** Encouragement after a wrong answer. Never a penalty; the question stays until Alina answers correctly. */
    public static final String[] WRONG_GENERIC = {
        "Goed geprobeerd! Probeer het nog eens.",
        "Geen zorg, we proberen het samen opnieuw.",
        "Oeps, bijna! Kijk nog eens rustig.",
        "Bijna! Kies gerust een ander antwoord."
    };

    public static final String[] WRONG_MATH = {
        "Bijna! Tel de sterren nog eens rustig mee.",
        "Goed geprobeerd! Tel de sterren nog eens.",
        "Geen zorg, tel samen met mij opnieuw."
    };

    public static final String[] WRONG_WORD = {
        "Bijna! Kijk goed naar het dier en kies opnieuw.",
        "Geen zorg, probeer een ander woord.",
        "Goed geprobeerd! Let op de eerste letter."
    };

    public static final String[] WRONG_PATTERN = {
        "Bijna! Kijk welke vorm steeds terugkomt.",
        "Goed geprobeerd! Volg de vormen in de ketting.",
        "Geen zorg, kijk nog eens naar de volgorde."
    };

    public static final String[] WRONG_FEED = {
        "Dat hapje is het niet. Tik op het juiste eten.",
        "Bijna! Kijk welk eten je vriendje wil.",
        "Geen zorg, probeer een ander hapje."
    };

    public static final String[] WRONG_MEMORY = {
        "Die twee zijn niet hetzelfde. Kijk nog eens.",
        "Bijna! Zoek het plaatje dat erop lijkt.",
        "Geen zorg, draai nog twee kaartjes om."
    };

    public static final String[] WRONG_HIDE = {
        "Hier zit je vriendje niet. Kijk naar het oortje.",
        "Bijna! Zoek het struikje met een oortje.",
        "Geen zorg, probeer een ander struikje."
    };

    /** Celebration after a full five-question round. Special, but calm. */
    public static final String[] ROUND_DONE = {
        "Hoera Alina! Je hebt vijf sterren verdiend!",
        "Wat een mooi rondje! Vijf sterren voor jou!",
        "Klaar! Jij bent een superleerling, Alina!"
    };

    /** Line after "Welkom [naam]!" when a new animal friend joins. */
    public static final String[] NEW_FRIEND = {
        "Wat leuk, je hebt een nieuw dierenvriendje!",
        "Hoera, een nieuw vriendje is bij jou gekomen!",
        "Fijn dat je er bent. Samen gaan we spelen!"
    };

    private LessonText() {}

    /** Dutch number word for 0–20. */
    public static String number(int n) {
        if (n < 0 || n >= NUMBERS.length) throw new IllegalArgumentException("Number outside 0-20: " + n);
        return NUMBERS[n];
    }

    /** "Drie plus twee. Hoeveel is dat samen?" or "Zeven min drie. Hoeveel blijven er over?" — never includes the result. */
    public static String mathPrompt(int a, int b, boolean subtract) {
        if (subtract) return capitalize(number(a)) + " min " + number(b) + ". Hoeveel blijven er over?";
        return capitalize(number(a)) + " plus " + number(b) + ". Hoeveel is dat samen?";
    }

    public static String wordPrompt() { return "Welk woord hoort bij dit dier? Kies het juiste woord."; }

    public static String patternPrompt() { return "Welke vorm komt hierna? Kijk goed naar de ketting."; }

    /** "Geef Mila vier aardbeien." The number is the instruction, because Alina has to tap that many. */
    public static String feedPrompt(String name, int count, String food) {
        return "Geef " + name + " " + number(count) + " " + foodWord(food, count) + ".";
    }

    public static String memoryPrompt() { return "Zoek twee dezelfde plaatjes. Tik een kaartje open."; }

    public static String hidePrompt(String name) {
        return "Waar verstopt " + name + " zich? Tik op het struikje met het oortje.";
    }

    public static String foodWord(String food, int count) {
        boolean one = count == 1;
        if ("aardbei".equals(food)) return one ? "aardbei" : "aardbeien";
        if ("wortel".equals(food)) return one ? "wortel" : "wortels";
        if ("botje".equals(food)) return one ? "botje" : "botjes";
        throw new IllegalArgumentException("Onbekend hapje: " + food);
    }

    /** Picks an index in [0, count) that differs from {@code avoid}, so the same line is not repeated back to back. */
    public static int pick(Random random, int count, int avoid) {
        if (count < 1) throw new IllegalArgumentException("Empty pool");
        if (count == 1) return 0;
        int index;
        do { index = random.nextInt(count); } while (index == avoid);
        return index;
    }

    static String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
