package nl.alina.tovertuin;

import java.util.Random;

/** Checks the level ladder, including the steps that go past the original games. */
public final class LevelsTest {
    private static int checks;
    private static void check(boolean condition, String message) { checks++; if (!condition) throw new AssertionError(message); }
    public static void main(String[] args) {
        Random random = new Random(1);
        for (int i = 0; i < 30; i++) {
            Levels.Sum easy = Levels.makeSum("+", 1, random);
            check(easy.answer <= 5 && easy.answer >= 2, "First plus level stays within 5");
            Levels.Sum big = Levels.makeSum("+", 14, random);
            check(big.a >= 1000 && big.answer > 1000, "Harder plus level uses numbers above 1000");
            Levels.Sum huge = Levels.makeSum("+", 16, random);
            check(huge.four && huge.answer == huge.a + huge.b + huge.c + huge.d, "Four-number sums add every part");
            Levels.Sum times = Levels.makeSum("×", 9, random);
            check(times.b == 11 || times.b == 12, "Harder times level uses the 11 and 12 tables");
        }
        check(Levels.countMax(1) == 5 && Levels.countMax(4) == 20 && Levels.countMax(6) == 50, "Counting grows to 50");
        check(Levels.memoryPairs(1) == 3 && Levels.memoryPairs(8) == 10, "Memory grows past eight pairs");
        check(Levels.clockMinutes(1).length == 1 && Levels.clockMinutes(5).length == 60, "Clock reaches every minute");
        check(Levels.moneyMaxPrice(3) == 20 && Levels.moneyMaxPrice(5) == 100, "Money grows to 100 euro");
        check(Levels.harderStart("+") == 14 && Levels.harderStart("×") == 9, "Harder steps start after the original ladder");
        check(Levels.sumKind("+", 14).equals("Tot 5000"), "Level name tells Alina it is harder");
        System.out.println("PASS: " + checks + " level checks");
    }
}
