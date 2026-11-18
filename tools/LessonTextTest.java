package nl.alina.tovertuin;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/** Checks Dutch spoken prompts and reaction variety; run with tools/test-rules.ps1 or CI. */
public final class LessonTextTest {
    private static int checks;
    private static void check(boolean condition, String message) {
        checks++; if (!condition) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        check(LessonText.mathPrompt(3, 2, false).equals("Drie plus twee. Hoeveel is dat samen?"), "Addition is read in full Dutch");
        check(LessonText.mathPrompt(7, 3, true).equals("Zeven min drie. Hoeveel blijven er over?"), "Subtraction is read in full Dutch");
        check(LessonText.number(0).equals("nul") && LessonText.number(20).equals("twintig"), "Number words cover 0-20");
        check(LessonText.number(11).equals("elf") && LessonText.number(12).equals("twaalf"), "Irregular number words elf and twaalf");
        try { LessonText.number(21); throw new AssertionError("Out of range numbers must fail"); } catch (IllegalArgumentException expected) { checks++; }

        // The spoken prompt must not reveal the answer, compared word by word.
        for (int a = 2; a <= 9; a++) for (int b = 1; b <= 9; b++) {
            int sum = a + b;
            Set<String> words = new HashSet<>(java.util.Arrays.asList(LessonText.mathPrompt(a, b, false).toLowerCase().split("[^a-z]+")));
            check(sum == a || !words.contains(LessonText.number(sum)) || LessonText.number(sum).equals(LessonText.number(a)) || LessonText.number(sum).equals(LessonText.number(b)),
                "Sum answer must not be spoken before answering: " + a + "+" + b);
            if (a > b) {
                Set<String> minus = new HashSet<>(java.util.Arrays.asList(LessonText.mathPrompt(a, b, true).toLowerCase().split("[^a-z]+")));
                int diff = a - b;
                check(!minus.contains(LessonText.number(diff)) || LessonText.number(diff).equals(LessonText.number(a)) || LessonText.number(diff).equals(LessonText.number(b)),
                    "Difference must not be spoken before answering: " + a + "-" + b);
            }
        }

        // Every pool has unique, child-friendly lines, and no English filler.
        assertPool("CORRECT", LessonText.CORRECT);
        assertPool("WRONG_GENERIC", LessonText.WRONG_GENERIC);
        assertPool("WRONG_MATH", LessonText.WRONG_MATH);
        assertPool("WRONG_WORD", LessonText.WRONG_WORD);
        assertPool("WRONG_PATTERN", LessonText.WRONG_PATTERN);
        assertPool("WRONG_FEED", LessonText.WRONG_FEED);
        assertPool("WRONG_MEMORY", LessonText.WRONG_MEMORY);
        assertPool("WRONG_HIDE", LessonText.WRONG_HIDE);
        assertPool("ROUND_DONE", LessonText.ROUND_DONE);
        check(LessonText.feedPrompt("Mila", 4, "aardbei").equals("Geef Mila vier aardbeien."), "Feed instruction names the count and the food");
        check(LessonText.feedPrompt("Bobby", 1, "botje").equals("Geef Bobby een botje."), "One item uses the singular");
        check(LessonText.memoryPrompt().startsWith("Zoek twee dezelfde"), "Memory tells Alina to tap cards");
        check(LessonText.hidePrompt("Pluis").contains("oortje"), "Hide and seek points at the visible ear");
        assertPool("NEW_FRIEND", LessonText.NEW_FRIEND);
        check(LessonText.CORRECT.length >= 12, "Enough correct-answer variety");

        // Variety: consecutive picks never repeat, and every line is used over time.
        Random random = new Random(42);
        int avoid = -1; Set<Integer> used = new HashSet<>();
        for (int i = 0; i < 2000; i++) {
            int next = LessonText.pick(random, LessonText.CORRECT.length, avoid);
            check(next != avoid, "Reaction must not repeat back to back");
            used.add(next); avoid = next;
        }
        check(used.size() == LessonText.CORRECT.length, "Every correct reaction is used");
        check(LessonText.pick(random, 1, 0) == 0, "Single-line pool is allowed");

        System.out.println("PASS: " + checks + " Dutch speech and reaction checks");
    }

    private static void assertPool(String name, String[] lines) {
        Set<String> unique = new HashSet<>();
        for (String line : lines) {
            check(line != null && !line.isBlank(), name + " has no empty line");
            check(line.length() <= 60, name + " line too long for speech: " + line);
            check(!line.toLowerCase().contains("volgende"), name + " must not mention a next button");
            check(!line.contains("High five") || name.equals("CORRECT"), name + " English filler only as a reward");
            unique.add(line);
        }
        check(unique.size() == lines.length, name + " has duplicate lines");
    }
}
