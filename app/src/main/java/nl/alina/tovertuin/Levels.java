package nl.alina.tovertuin;

import java.util.Random;

/**
 * Level ladders for Queen Alina. The first steps follow the same climb as the LOU games
 * (sums through 1000, tables, counting to 20, whole/half/quarter hours).
 * The steps after that are the harder levels asked for on top.
 */
public final class Levels {
    public static final String[] PLUS = {
        "Tot 5 met plaatjes", "Tot 10 met plaatjes", "Tot 10", "Tot 20", "Tientallen", "Over de 10",
        "Wat mist er?", "Wat mist er? Tot 20", "Drie getallen", "Tot 100", "Honderdtallen", "Tot 1000",
        "Grote sommen tot 1000", "Tot 5000", "Tot 10000", "Vier getallen"
    };
    public static final String[] TIMES = {
        "Tafel van 1, 2 en 3 met plaatjes", "Tafel van 5 en 10", "Tafel van 3 en 4", "Tafel van 6 en 7",
        "Tafel van 8 en 9", "Alle tafels", "Wat mist er?", "Grote keersommen",
        "Tafel van 11 en 12", "Keersommen tot 100"
    };

    private Levels() {}

    public static String sumKind(String op, int level) {
        String[] table = (op.equals("×") || op.equals(":")) ? TIMES : PLUS;
        int i = Math.min(Math.max(1, level), table.length) - 1;
        return table[i];
    }

    public static int sumStepCount(String op) {
        return (op.equals("×") || op.equals(":")) ? TIMES.length : PLUS.length;
    }

    /** First level that is harder than the original ladder. */
    public static int harderStart(String op) {
        if (op.equals("×") || op.equals(":")) return 9;
        return 14;
    }

    public static int memoryPairs(int level) {
        return Math.min(10, Math.max(1, level) + 2);
    }

    public static int countMax(int level) {
        int[] caps = {5, 10, 15, 20, 30, 50};
        return caps[Math.min(caps.length, Math.max(1, level)) - 1];
    }

    public static int countSteps() { return 6; }

    /** Minutes the clock may show at this level. Level 4 and 5 are the harder steps. */
    public static int[] clockMinutes(int level) {
        if (level <= 1) return new int[]{0};
        if (level == 2) return new int[]{0, 30};
        if (level == 3) return new int[]{0, 30, 15, 45};
        if (level == 4) return new int[]{0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55};
        int[] all = new int[60];
        for (int i = 0; i < 60; i++) all[i] = i;
        return all;
    }

    public static int moneyMaxPrice(int level) {
        if (level <= 1) return 5;
        if (level == 2) return 10;
        if (level == 3) return 20;
        if (level == 4) return 50;
        return 100;
    }

    public static int[] moneyCoins(int level) {
        if (level <= 1) return new int[]{1, 2};
        if (level == 2) return new int[]{1, 2, 5};
        if (level == 3) return new int[]{1, 2, 5, 10};
        if (level == 4) return new int[]{1, 2, 5, 10, 20};
        return new int[]{1, 2, 5, 10, 20, 50};
    }

    public static final class Sum {
        public final int a, b, c, d, answer, shown;
        public final boolean miss, pictures, four;
        public final String sign, spoken;
        Sum(int a, int b, int c, int d, int answer, int shown, boolean miss, boolean pictures, boolean four, String sign, String spoken) {
            this.a = a; this.b = b; this.c = c; this.d = d; this.answer = answer; this.shown = shown;
            this.miss = miss; this.pictures = pictures; this.four = four; this.sign = sign; this.spoken = spoken;
        }
    }

    public static Sum makeSum(String op, int level, Random random) {
        int L = Math.max(1, level);
        int a, b, c = 0, d = 0;
        boolean miss = false, pics, four = false;
        if (op.equals("×") || op.equals(":")) {
            int step = Math.min(L, TIMES.length);
            int[][] tables = {{1, 2, 3}, {5, 10}, {3, 4}, {6, 7}, {8, 9}, {2, 3, 4, 5, 6, 7, 8, 9, 10}, {2, 3, 4, 5, 6, 7, 8, 9, 10}, {11, 12, 15, 20, 25}, {11, 12}, {2, 3, 4, 5, 6, 7, 8, 9}};
            int[] table = tables[step - 1];
            int t = table[random.nextInt(table.length)];
            int k = step == 10 ? 2 + random.nextInt(12) : step == 8 ? 2 + random.nextInt(8) : step <= 1 ? 1 + random.nextInt(5) : 1 + random.nextInt(10);
            miss = step == 7;
            pics = step == 1 && t * k <= 20;
            int product = k * t;
            if (op.equals("×")) {
                int ans = miss ? t : product;
                return new Sum(k, t, 0, 0, ans, product, miss, pics, false, "×", speak(k, "keer", t, product, miss));
            }
            int ans = miss ? t : k;
            return new Sum(product, t, 0, 0, ans, k, miss, pics, false, ":", speak(product, "gedeeld door", t, k, miss));
        }
        int step = Math.min(L, PLUS.length);
        pics = step <= 2;
        if (op.equals("+")) {
            if (step == 1) { a = 1 + random.nextInt(4); b = 1 + random.nextInt(5 - a); }
            else if (step <= 3) { a = 1 + random.nextInt(9); b = 1 + random.nextInt(10 - a); }
            else if (step == 4) { a = 11 + random.nextInt(8); b = 1 + random.nextInt(Math.max(1, 9 - a % 10)); }
            else if (step == 5) { a = (1 + random.nextInt(8)) * 10; b = (1 + random.nextInt(Math.max(1, 9 - a / 10))) * 10; }
            else if (step == 6) { a = 5 + random.nextInt(5); b = Math.max(1, 11 - a) + random.nextInt(Math.max(1, a - 1)); if (a + b > 18) b = 18 - a; }
            else if (step == 7) { a = 1 + random.nextInt(8); b = 1 + random.nextInt(10 - a); miss = true; }
            else if (step == 8) { a = 3 + random.nextInt(13); b = 1 + random.nextInt(Math.max(1, 20 - a)); miss = true; }
            else if (step == 9) { a = 1 + random.nextInt(7); b = 1 + random.nextInt(7); c = 1 + random.nextInt(Math.max(1, 20 - a - b)); }
            else if (step == 10) { a = 11 + random.nextInt(69); b = 2 + random.nextInt(Math.max(1, 99 - a)); }
            else if (step == 11) { a = (1 + random.nextInt(8)) * 100; b = (1 + random.nextInt(Math.max(1, 9 - a / 100))) * 100; }
            else if (step == 12) { a = 101 + random.nextInt(750); b = 5 + random.nextInt(Math.max(1, Math.min(99, 999 - a))); }
            else if (step == 13) { a = 100 + random.nextInt(750); b = 100 + random.nextInt(Math.max(1, 999 - a)); }
            else if (step == 14) { a = 1000 + random.nextInt(3000); b = 50 + random.nextInt(Math.max(1, 4999 - a)); }
            else if (step == 15) { a = 1000 + random.nextInt(7000); b = 100 + random.nextInt(Math.max(1, 9999 - a)); }
            else { a = 20 + random.nextInt(80); b = 20 + random.nextInt(80); c = 20 + random.nextInt(80); d = 20 + random.nextInt(80); four = true; pics = false; }
            int total = a + b + c + d;
            int ans = miss ? b : total;
            String said = speak(a, "plus", b, total, miss);
            if (four) said = "Hoeveel is " + a + " plus " + b + " plus " + c + " plus " + d + "?";
            return new Sum(a, b, c, d, ans, total, miss, pics, four, "+", said);
        }
        if (step == 1) { a = 2 + random.nextInt(4); b = 1 + random.nextInt(a - 1); }
        else if (step <= 3) { a = 3 + random.nextInt(8); b = 1 + random.nextInt(a - 1); }
        else if (step == 4) { a = 13 + random.nextInt(7); b = 1 + random.nextInt(Math.max(1, a % 10)); }
        else if (step == 5) { a = (3 + random.nextInt(7)) * 10; b = (1 + random.nextInt(Math.max(1, a / 10 - 1))) * 10; }
        else if (step == 6) { a = 12 + random.nextInt(6); b = Math.max(2, a - 9) + random.nextInt(Math.max(1, 9 - Math.max(1, a - 9))); if (b >= a) b = a - 1; }
        else if (step == 7) { a = 4 + random.nextInt(7); b = 1 + random.nextInt(a - 1); miss = true; }
        else if (step == 8) { a = 11 + random.nextInt(10); b = 2 + random.nextInt(a - 2); miss = true; }
        else if (step == 9) { a = 12 + random.nextInt(9); b = 1 + random.nextInt(5); c = 1 + random.nextInt(Math.max(1, a - b - 1)); }
        else if (step == 10) { a = 25 + random.nextInt(75); b = 2 + random.nextInt(Math.max(1, a - 5)); }
        else if (step == 11) { a = (2 + random.nextInt(8)) * 100; b = (1 + random.nextInt(a / 100 - 1)) * 100; }
        else if (step == 12) { a = 150 + random.nextInt(850); b = 5 + random.nextInt(99); }
        else if (step == 13) { a = 400 + random.nextInt(600); b = 100 + random.nextInt(Math.max(1, a - 150)); }
        else if (step == 14) { a = 2000 + random.nextInt(3000); b = 50 + random.nextInt(1500); }
        else if (step == 15) { a = 4000 + random.nextInt(6000); b = 100 + random.nextInt(3000); }
        else { a = 80 + random.nextInt(40); b = 5 + random.nextInt(20); c = 5 + random.nextInt(20); four = true; pics = false; }
        int total = a - b - (four || step == 9 ? c : 0);
        if (total < 0) total = 0;
        int ans = miss ? b : total;
        String said = four ? "Hoeveel is " + a + " min " + b + " min " + c + "?" : speak(a, "min", b, total, miss);
        return new Sum(a, b, c, 0, ans, a, miss, pics, four, "−", said);
    }

    private static String speak(int a, String word, int b, int result, boolean miss) {
        String left = a <= 20 ? LessonText.number(a) : String.valueOf(a);
        String right = b <= 20 ? LessonText.number(b) : String.valueOf(b);
        if (miss) return LessonText.capitalize(left) + " " + word + " hoeveel is " + (result <= 20 ? LessonText.number(result) : result) + "?";
        return "Hoeveel is " + left + " " + word + " " + right + "?";
    }
}
