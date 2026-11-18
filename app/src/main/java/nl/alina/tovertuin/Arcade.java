package nl.alina.tovertuin;

import android.content.Context;
import android.graphics.*;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import java.util.*;

/** Playable boards for every Queen Alina game. Drawing and taps stay inside this view. */
public final class Arcade extends View {
    public interface Host {
        void hud(String title, String kind, String prompt, int lives, int done, int need, String[] choices);
        void say(String line);
        void stars(int n);
        int levelOf(String key);
        void saveLevel(String key, int level);
    }

    private final Host host;
    private final Random random = new Random();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF[] hits = new RectF[16];
    private final int[] marks = new int[16];
    private final boolean[] used = new boolean[16];
    public String id = "plus", title = "Spel", expected = "";
    public int done() { return done; }
    private String key = "plus", prompt = "", kindName = "";
    private int level = 1, done, lives = 3, need = 5, answer, step, goal, score, spawn;
    private boolean dead, busy, action;
    private float mover;
    private Levels.Sum sum;
    private final List<String> deck = new ArrayList<>();
    private int openA = -1;
    private final Path drawing = new Path();
    private final Runnable tick = new Runnable() { public void run() { if (action && !dead) { frame(); postDelayed(this, 32); } } };

    public Arcade(Context context, Host host) {
        super(context); this.host = host;
        for (int i = 0; i < hits.length; i++) hits[i] = new RectF();
        setClickable(true);
    }

    public void stop() { action = false; removeCallbacks(tick); }

    public void open(String gameId, String gameTitle) {
        id = gameId; title = gameTitle; key = "lv-" + gameId;
        level = Math.max(1, host.levelOf(key));
        done = 0; lives = 3; dead = false; need = actionGame() ? 3 : 5;
        stop(); next();
    }

    private boolean actionGame() {
        return id.equals("tower") || id.equals("runner") || id.equals("adventure") || id.equals("race") || id.equals("boat") || id.equals("pop") || id.equals("candy");
    }

    public void choose(String label) {
        if (dead) { if ("Opnieuw".equals(label)) { dead = false; done = 0; lives = 3; next(); } return; }
        if (busy) return;
        if (label != null && label.equals(expected)) right();
        else wrong();
    }

    /** Test hook and the listen-free correct move. */
    public void tapCorrect() {
        if (!expected.isEmpty()) { choose(expected); return; }
        if (id.equals("memory") || id.equals("fairymem")) { int[] p = pair(); flip(p[0]); flip(p[1]); }
        else if (id.equals("money")) pay(goal);
        else if (id.equals("simon")) simonPlay(0);
        else if (id.equals("feed")) feedTap(true);
        else if (id.equals("hide") || id.equals("wolf") || id.equals("shoe")) pick(answer);
        else if (id.equals("puzzle") || id.equals("jigsaw")) { if (step < spawn) { int t = marks[step]; marks[step] = marks[step + 1]; marks[step + 1] = t; step++; if (solvedPuzzle()) right(); invalidate(); } }
        else if (id.equals("kitchen") || id.equals("pizza") || id.equals("bread") || id.equals("cookies") || id.equals("cake") || id.equals("smoothie") || id.equals("ice")) kitchenTap();
        else if (id.equals("trace")) { step = goal; right(); }
        else if (id.equals("piano") || id.equals("music")) { step = goal; right(); }
        else if (id.equals("draw") || id.equals("coloring")) right();
        else if (id.equals("tower")) { score++; if (score >= 3 + level) right(); else invalidate(); }
        else right();
    }

    private void right() {
        if (dead) return;
        busy = true; done++; host.stars(1); host.say(LessonText.CORRECT[done % LessonText.CORRECT.length]);
        if (done >= need) {
            int cap = cap();
            if (level < cap) { level++; host.saveLevel(key, level); host.stars(2); host.say("Level " + level + "! " + kindName); }
            else host.say("Alle levels gehaald. Je speelt het zwaarste level nog een keer.");
            done = 0;
        }
        postDelayed(() -> { busy = false; if (!dead) next(); }, 800);
        hud();
    }

    private void wrong() {
        if (dead || busy) return;
        lives--; host.say("Probeer nog eens.");
        if (lives <= 0) { dead = true; stop(); expected = "Opnieuw"; prompt = "Oei, af! Probeer level " + level + " nog een keer."; host.hud(title, kindName, prompt, 0, done, need, new String[]{"Opnieuw"}); host.say(prompt); }
        else hud();
        invalidate();
    }

    private int cap() {
        if (id.equals("plus") || id.equals("minus")) return Levels.PLUS.length;
        if (id.equals("times") || id.equals("divide")) return Levels.TIMES.length;
        if (id.equals("count") || id.equals("pepper")) return Levels.countSteps();
        if (id.equals("clock")) return 5;
        if (id.equals("money")) return 5;
        if (id.equals("memory") || id.equals("fairymem")) return 8;
        return 6;
    }

    private void next() {
        if (dead) return;
        stop(); expected = ""; openA = -1; step = 0; score = 0; Arrays.fill(used, false);
        action = actionGame();
        if (id.equals("plus") || id.equals("minus") || id.equals("times") || id.equals("divide")) quizSum();
        else if (id.equals("count") || id.equals("pepper")) quizCount();
        else if (id.equals("color") || id.equals("gifts")) quizColor();
        else if (id.equals("letter")) quizLetter();
        else if (id.equals("clock")) quizClock();
        else if (id.equals("shapes")) quizShapes();
        else if (id.equals("wall")) quizWall();
        else if (id.equals("basket") || id.equals("bears") || id.equals("piggies")) quizStory();
        else if (id.equals("memory") || id.equals("fairymem")) setupMemory();
        else if (id.equals("hide") || id.equals("wolf") || id.equals("shoe")) setupHide();
        else if (id.equals("feed")) setupFeed();
        else if (id.equals("money")) setupMoney();
        else if (id.equals("simon")) setupSimon();
        else if (id.equals("puzzle") || id.equals("jigsaw")) setupPuzzle();
        else if (id.equals("kitchen") || id.equals("pizza") || id.equals("bread") || id.equals("cookies") || id.equals("cake") || id.equals("smoothie") || id.equals("ice")) setupKitchen();
        else if (id.equals("trace")) setupTrace();
        else if (id.equals("piano") || id.equals("music")) setupPiano();
        else if (id.equals("draw") || id.equals("coloring")) setupDraw();
        else { goal = 4 + level; spawn = 0; mover = 0; action = true; post(tick); }
        hud(); invalidate();
    }

    private void hud() { host.hud(title + "  ·  level " + level, kindName, prompt, lives, done, need, expected.isEmpty() || dead ? (dead ? new String[]{"Opnieuw"} : new String[0]) : choices.toArray(new String[0])); }

    private final List<String> choices = new ArrayList<>();

    private void setChoices(String correct, int n, int spread) {
        choices.clear(); choices.add(correct); expected = correct;
        Random r = random;
        while (choices.size() < n) {
            int base = 0; try { base = Integer.parseInt(correct); } catch (NumberFormatException ignored) { choices.add(correct + " "); continue; }
            int alt = Math.max(0, base + (r.nextInt(spread * 2 + 1) - spread));
            if (alt != base && !choices.contains(String.valueOf(alt))) choices.add(String.valueOf(alt));
        }
        Collections.shuffle(choices, r);
    }

    private void quizSum() {
        String op = id.equals("plus") ? "+" : id.equals("minus") ? "-" : id.equals("times") ? "×" : ":";
        sum = Levels.makeSum(op, level, random);
        kindName = Levels.sumKind(op, level);
        prompt = sum.spoken;
        answer = sum.answer;
        int spread = answer >= 1000 ? 250 : answer >= 100 ? 20 : answer > 20 ? 8 : 3;
        setChoices(String.valueOf(answer), level >= 5 ? 4 : 3, Math.max(1, spread));
        host.say(prompt);
    }

    private void quizCount() {
        int max = Levels.countMax(level); answer = 1 + random.nextInt(max); kindName = "Tot " + max;
        String[] words = id.equals("pepper") ? new String[]{"pepernoten", "mandarijnen", "cadeautjes"} : new String[]{"appels", "sterren", "bloemen", "eendjes"};
        prompt = "Hoeveel " + words[random.nextInt(words.length)] + " zie je?";
        setChoices(String.valueOf(answer), 3, 2); host.say(prompt);
    }

    private void quizColor() {
        String[] names = {"rood", "blauw", "geel", "groen", "oranje", "roze", "paars"};
        int n = Math.min(names.length, 3 + Math.min(level, 4));
        answer = random.nextInt(n); kindName = "Level " + level; expected = names[answer];
        choices.clear(); for (int i = 0; i < n; i++) choices.add(names[i]); Collections.shuffle(choices, random);
        prompt = id.equals("gifts") ? "Welke kleur heeft het cadeau?" : "Welke kleur zie je?"; host.say(prompt);
    }

    private void quizLetter() {
        String[][] letters = {{"a", "aap"}, {"b", "boom"}, {"e", "eend"}, {"k", "kat"}, {"m", "maan"}, {"o", "oor"}, {"s", "slak"}, {"z", "zon"}, {"p", "peer"}, {"r", "roos"}};
        int n = Math.min(letters.length, 2 + level);
        int pick = random.nextInt(Math.min(letters.length, n));
        answer = pick; expected = letters[pick][0]; kindName = letters[pick][0] + " van " + letters[pick][1];
        choices.clear(); choices.add(expected);
        while (choices.size() < Math.min(4, 2 + level / 2)) { String L = letters[random.nextInt(letters.length)][0]; if (!choices.contains(L)) choices.add(L); }
        Collections.shuffle(choices, random);
        prompt = "Welke letter hoort bij " + letters[pick][1] + "?"; host.say(prompt);
    }

    private void quizClock() {
        int[] mins = Levels.clockMinutes(level); int h = 1 + random.nextInt(12), m = mins[random.nextInt(mins.length)];
        answer = h * 100 + m; expected = clockLabel(h, m); kindName = level >= 4 ? "Alle minuten" : level == 3 ? "Kwartieren" : level == 2 ? "Hele en halve uren" : "Hele uren";
        choices.clear(); choices.add(expected);
        while (choices.size() < 3) { String alt = clockLabel(1 + random.nextInt(12), mins[random.nextInt(mins.length)]); if (!choices.contains(alt)) choices.add(alt); }
        Collections.shuffle(choices, random); prompt = "Hoe laat is het?"; host.say(prompt);
    }

    private String clockLabel(int h, int m) {
        if (m == 0) return h + " uur";
        if (m == 15) return "kwart over " + h;
        if (m == 30) return "half " + (h == 12 ? 1 : h + 1);
        if (m == 45) return "kwart voor " + (h == 12 ? 1 : h + 1);
        return h + ":" + (m < 10 ? "0" : "") + m;
    }

    private void quizShapes() {
        String[] shapes = {"cirkel", "driehoek", "vierkant", "ster", "hart"};
        answer = random.nextInt(Math.min(shapes.length, 2 + level)); expected = shapes[answer];
        choices.clear(); for (int i = 0; i < Math.min(shapes.length, 3 + level / 2); i++) choices.add(shapes[i]);
        Collections.shuffle(choices, random); kindName = "Sleep is tikken"; prompt = "Welke vorm past in het gat?"; host.say(prompt);
    }

    private void quizWall() {
        String[] a = {"rood", "blauw"}; String[] b = {"geel", "groen", "oranje"};
        String[] seq = level < 3 ? a : b; answer = random.nextInt(seq.length);
        int len = 3 + Math.min(level, 3); marks[0] = len;
        for (int i = 0; i < len; i++) marks[i + 1] = i % seq.length;
        expected = seq[len % seq.length]; choices.clear(); Collections.addAll(choices, seq); Collections.shuffle(choices, random);
        kindName = "Patroon"; prompt = "Welke steen komt hierna?"; host.say(prompt);
    }

    private void quizStory() {
        kindName = "Level " + level;
        if (id.equals("bears")) { prompt = "Welke kom is precies goed voor Alina?"; expected = "precies goed"; choices.clear(); choices.add("te heet"); choices.add("te koud"); choices.add("precies goed"); }
        else if (id.equals("piggies")) { prompt = "Welk huisje blijft staan als de wolf blaast?"; expected = "het stenen huis"; choices.clear(); choices.add("het strooien huis"); choices.add("het houten huis"); choices.add("het stenen huis"); }
        else { prompt = "Welk pad brengt het mandje veilig bij oma?"; expected = "het pad met bloemen"; choices.clear(); choices.add("het donkere pad"); choices.add("het pad met bloemen"); choices.add("het pad met de wolf"); }
        if (level >= 3) { prompt = prompt + " Kijk goed, er zijn meer keuzes."; }
        Collections.shuffle(choices, random); host.say(prompt);
    }

    private void setupMemory() {
        String[] items = id.equals("fairymem") ? new String[]{"wolf", "kroon", "kasteel", "beer", "ster", "bloem", "appel", "maan", "hart", "vis"} : new String[]{"hond", "kat", "kikker", "leeuw", "konijn", "beer", "vos", "panda", "uil", "vis"};
        int pairs = Levels.memoryPairs(level); kindName = pairs + " paren";
        deck.clear(); List<String> pick = new ArrayList<>(Arrays.asList(items)); Collections.shuffle(pick, random);
        for (int i = 0; i < pairs; i++) { deck.add(pick.get(i)); deck.add(pick.get(i)); }
        Collections.shuffle(deck, random); Arrays.fill(used, false); openA = -1;
        prompt = "Zoek twee dezelfde plaatjes."; host.say(prompt); choices.clear(); expected = "";
    }

    private int[] pair() {
        for (int i = 0; i < deck.size(); i++) for (int j = i + 1; j < deck.size(); j++) if (!used[i] && !used[j] && deck.get(i).equals(deck.get(j))) return new int[]{i, j};
        return new int[]{0, 1};
    }

    private void flip(int index) {
        if (dead || busy || index < 0 || index >= deck.size() || used[index] || marks[index] == 1) return;
        marks[index] = 1;
        if (openA < 0) { openA = index; invalidate(); return; }
        if (deck.get(openA).equals(deck.get(index))) { used[openA] = used[index] = true; marks[openA] = marks[index] = 2; openA = -1; if (allMatched()) right(); else invalidate(); }
        else { int a = openA; openA = -1; busy = true; invalidate(); postDelayed(() -> { marks[a] = marks[index] = 0; busy = false; invalidate(); }, 600); }
    }

    private boolean allMatched() { for (int i = 0; i < deck.size(); i++) if (!used[i]) return false; return true; }

    private void setupHide() {
        spawn = Math.min(8, 2 + level); answer = random.nextInt(spawn); kindName = spawn + " plekken";
        prompt = id.equals("shoe") ? "In welke schoen zit het cadeau?" : id.equals("wolf") ? "Achter welk huisje zit de wolf?" : "Achter welke boom zit Alina?";
        host.say(prompt); choices.clear(); expected = "";
    }

    private void pick(int index) { if (index == answer) right(); else wrong(); }

    private void setupFeed() {
        goal = Math.min(2 + level, 8); score = 0; spawn = 8; answer = random.nextInt(3); kindName = "Hapjes";
        String[] foods = {"aardbei", "wortel", "ster"};
        prompt = "Geef Alina " + (goal <= 20 ? LessonText.number(goal) : goal) + " " + foods[answer] + ".";
        for (int i = 0; i < 8; i++) marks[i] = i < goal ? answer : 3 + random.nextInt(2);
        for (int i = 7; i > 0; i--) { int j = random.nextInt(i + 1); int s = marks[i]; marks[i] = marks[j]; marks[j] = s; }
        Arrays.fill(used, false); host.say(prompt); choices.clear(); expected = "";
    }

    private void feedTap(boolean correct) {
        for (int i = 0; i < 8; i++) if (!used[i] && (marks[i] == answer) == correct) { used[i] = true; if (correct) { score++; if (score >= goal) right(); else invalidate(); } else wrong(); return; }
    }

    private void setupMoney() {
        int[] coins = Levels.moneyCoins(level); goal = 1 + random.nextInt(Levels.moneyMaxPrice(level)); score = 0; kindName = "Tot € " + Levels.moneyMaxPrice(level);
        System.arraycopy(coins, 0, marks, 0, coins.length); spawn = coins.length;
        prompt = "Het ijsje kost " + goal + " euro. Tik het geld tot het klopt."; host.say(prompt); choices.clear(); expected = "";
    }

    private void pay(int amount) {
        if (score + amount > goal + 20) { wrong(); return; }
        score += amount; if (score == goal) right(); else if (score > goal) { host.say("Dat is te veel. We beginnen opnieuw."); score = 0; invalidate(); } else invalidate();
    }

    private void setupSimon() {
        goal = 2 + Math.min(level, 6); step = 0; kindName = goal + " stappen";
        for (int i = 0; i < goal; i++) marks[i] = random.nextInt(4);
        prompt = "Alina zegt: doe de kleuren na."; host.say(prompt); choices.clear(); expected = ""; busy = true;
        postDelayed(this::playSimon, 400);
    }

    private int simonAt;
    private void playSimon() { simonAt = 0; showSimon(); }
    private void showSimon() {
        if (simonAt >= goal) { busy = false; step = 0; invalidate(); return; }
        score = marks[simonAt] + 10; invalidate();
        postDelayed(() -> { score = 0; invalidate(); simonAt++; postDelayed(this::showSimon, 180); }, 420);
    }
    private void simonPlay(int color) {
        if (busy || dead) return;
        if (color == marks[step]) { step++; if (step >= goal) right(); }
        else { step = 0; wrong(); if (!dead) postDelayed(this::playSimon, 500); }
    }

    private void setupPuzzle() {
        int n = Math.min(level + 1, 4); spawn = n * n - 1; kindName = n + " bij " + n;
        for (int i = 0; i <= spawn; i++) marks[i] = i;
        for (int k = 0; k < 6 + level; k++) { int i = random.nextInt(spawn); int t = marks[i]; marks[i] = marks[i + 1]; marks[i + 1] = t; }
        prompt = "Zet de stukjes op volgorde. Tik twee stukjes om ze te wisselen."; host.say(prompt); choices.clear(); expected = ""; step = -1;
    }
    private boolean solvedPuzzle() { for (int i = 0; i <= spawn; i++) if (marks[i] != i) return false; return true; }

    private void setupKitchen() {
        step = 0; goal = 4; kindName = kitchenName();
        prompt = kitchenStep(); host.say(prompt); choices.clear(); expected = "";
    }
    private String kitchenName() {
        if (id.equals("bread")) return "Boterham"; if (id.equals("cookies")) return "Koekjes"; if (id.equals("cake")) return "Taart";
        if (id.equals("smoothie")) return "Smoothie"; if (id.equals("ice")) return "IJsje"; return "Pizza";
    }
    private String kitchenStep() {
        String[] pizza = {"Rol het deeg. Tik op de bal.", "Smeer de saus. Tik op de pizza.", "Leg " + (2 + level) + " toppings. Tik ze erop.", "Bak in de oven. Tik op de oven."};
        String[] bread = {"Pak het brood. Tik op de snee.", "Smeer er iets op.", "Leg beleg erop.", "Snijd de boterham doormidden."};
        String[] cookies = {"Kneed het deeg.", "Steek een vorm uit.", "Leg ze op de plaat.", "Bak de koekjes."};
        String[] cake = {"Bak de taart.", "Smeer glazuur.", "Zet versiering erop.", "Blaas de kaarsjes uit."};
        String[] smoothie = {"Doe fruit in de beker.", "Doe er melk bij.", "Zet de blender aan.", "Schenk het glas vol."};
        String[] ice = {"Kies een hoorntje.", "Schep " + (1 + Math.min(level, 3)) + " bolletjes.", "Doe er topping op.", "Geef het ijsje aan Alina."};
        String[] use = id.equals("bread") ? bread : id.equals("cookies") ? cookies : id.equals("cake") ? cake : id.equals("smoothie") ? smoothie : id.equals("ice") ? ice : pizza;
        return use[Math.min(step, 3)];
    }
    private void kitchenTap() { step++; if (step >= goal) right(); else { prompt = kitchenStep(); host.say(prompt); hud(); invalidate(); } }

    private void setupTrace() {
        step = 0; goal = 3 + Math.min(level, 4); kindName = "Volg de stippen";
        prompt = "Teken de letter. Volg de stippen van boven naar beneden."; host.say(prompt); choices.clear(); expected = "";
    }
    private void setupPiano() {
        goal = 3 + Math.min(level, 5); step = 0; kindName = id.equals("music") ? "Trommels" : "Piano";
        for (int i = 0; i < goal; i++) marks[i] = random.nextInt(id.equals("music") ? 4 : 7);
        prompt = "Speel na wat Alina voorspeelt. Tik de toetsen in de goede volgorde."; host.say(prompt); choices.clear(); expected = "";
    }
    private void setupDraw() {
        drawing.reset(); kindName = id.equals("coloring") ? "Kleurplaat" : "Vrij tekenen";
        prompt = id.equals("coloring") ? "Tik de vlakken tot ze de juiste kleur hebben." : "Teken iets moois. Tik op Klaar als je blij bent.";
        expected = "Klaar"; choices.clear(); choices.add("Klaar"); host.say(prompt);
    }

    private void frame() {
        mover += 0.04f + level * 0.01f; spawn++;
        if (id.equals("runner") || id.equals("adventure")) {
            if (spawn > 40 && (spawn / (18 - Math.min(level, 10))) % 7 == 0) score++;
            if (score >= 3 + level) { action = false; right(); return; }
        } else if (id.equals("race")) {
            score++; if (score > 80 + level * 20) { action = false; right(); return; }
        } else if (id.equals("boat") || id.equals("pop") || id.equals("candy")) {
            if (spawn % Math.max(8, 28 - level * 2) == 0) marks[0] = random.nextInt(4);
            if (score >= 5 + level) { action = false; right(); return; }
        } else if (id.equals("tower")) {
            mover += 0.05f;
        }
        invalidate();
    }

    @Override protected void onDraw(Canvas c) {
        float w = getWidth(), h = getHeight(); if (w < 2 || h < 2) return;
        paint.setColor(0xfffff6ee); c.drawRoundRect(8, 8, w - 8, h - 8, 28, 28, paint);
        paint.setColor(0xff49334f); paint.setTextAlign(Paint.Align.CENTER); paint.setTextSize(Math.max(28, h * 0.06f));
        if (sum != null && (id.equals("plus") || id.equals("minus") || id.equals("times") || id.equals("divide"))) drawSum(c, w, h);
        else if (id.equals("count") || id.equals("pepper")) drawCount(c, w, h);
        else if (id.equals("clock")) drawClock(c, w, h);
        else if (id.equals("memory") || id.equals("fairymem")) drawMemory(c, w, h);
        else if (id.equals("hide") || id.equals("wolf") || id.equals("shoe")) drawHide(c, w, h);
        else if (id.equals("feed")) drawFeed(c, w, h);
        else if (id.equals("money")) drawMoney(c, w, h);
        else if (id.equals("simon")) drawPads(c, w, h, 4);
        else if (id.equals("puzzle") || id.equals("jigsaw")) drawPuzzle(c, w, h);
        else if (id.equals("tower")) drawTower(c, w, h);
        else if (id.equals("runner") || id.equals("adventure")) drawRunner(c, w, h);
        else if (id.equals("race")) drawRace(c, w, h);
        else if (id.equals("boat") || id.equals("pop") || id.equals("candy")) drawCatch(c, w, h);
        else if (id.equals("piano") || id.equals("music")) drawPads(c, w, h, id.equals("music") ? 4 : 7);
        else if (id.equals("draw") || id.equals("coloring")) drawSketch(c, w, h);
        else if (id.equals("trace")) drawTrace(c, w, h);
        else if (id.equals("kitchen") || id.equals("pizza") || id.equals("bread") || id.equals("cookies") || id.equals("cake") || id.equals("smoothie") || id.equals("ice")) drawKitchen(c, w, h);
        else if (id.equals("color") || id.equals("gifts") || id.equals("shapes")) drawSwatch(c, w, h);
        else if (id.equals("wall")) drawWall(c, w, h);
        else c.drawText(prompt, w / 2f, h / 2f, paint);
    }

    private void drawSum(Canvas c, float w, float h) {
        String eq = sum.four ? sum.a + " + " + sum.b + " + " + sum.c + " + " + sum.d + " = ?" : sum.miss ? sum.a + " " + sum.sign + " ? = " + sum.shown : sum.a + " " + sum.sign + " " + sum.b + (sum.c > 0 && !sum.sign.equals("×") && !sum.sign.equals(":") ? " " + sum.sign + " " + sum.c : "") + " = ?";
        paint.setTextSize(h * 0.12f); c.drawText(eq, w / 2f, h * 0.42f, paint);
        if (sum.pictures) { paint.setTextSize(h * 0.08f); c.drawText(dots(sum.a) + "   " + dots(sum.b), w / 2f, h * 0.68f, paint); }
        else { paint.setTextSize(h * 0.05f); c.drawText(kindName, w / 2f, h * 0.72f, paint); }
    }
    private String dots(int n) { StringBuilder b = new StringBuilder(); int m = Math.min(n, 10); for (int i = 0; i < m; i++) b.append("●"); if (n > 10) b.append(" +").append(n - 10); return b.toString(); }
    private void drawCount(Canvas c, float w, float h) {
        paint.setTextSize(h * 0.09f); int cols = Math.min(10, Math.max(1, answer));
        for (int i = 0; i < answer && i < 40; i++) c.drawText("●", w * (0.12f + (i % cols) * 0.08f), h * (0.3f + (i / cols) * 0.12f), paint);
    }
    private void drawClock(Canvas c, float w, float h) {
        float cx = w / 2f, cy = h * 0.48f, r = Math.min(w, h) * 0.28f;
        paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(8); paint.setColor(0xff795297); c.drawCircle(cx, cy, r, paint); paint.setStyle(Paint.Style.FILL);
        int hh = answer / 100, mm = answer % 100;
        double ha = Math.toRadians((hh % 12 + mm / 60.0) * 30 - 90), ma = Math.toRadians(mm * 6 - 90);
        paint.setStrokeWidth(10); paint.setStyle(Paint.Style.STROKE);
        c.drawLine(cx, cy, cx + (float) Math.cos(ha) * r * 0.55f, cy + (float) Math.sin(ha) * r * 0.55f, paint);
        paint.setColor(0xffe23d4a); c.drawLine(cx, cy, cx + (float) Math.cos(ma) * r * 0.8f, cy + (float) Math.sin(ma) * r * 0.8f, paint);
        paint.setStyle(Paint.Style.FILL); paint.setColor(0xff49334f);
    }
    private void drawMemory(Canvas c, float w, float h) {
        int n = Math.max(1, deck.size()); int cols = n > 12 ? 5 : n > 6 ? 4 : 3; int rows = (n + cols - 1) / cols;
        float cw = w * 0.9f / cols, ch = h * 0.86f / rows;
        paint.setTextSize(ch * 0.28f);
        for (int i = 0; i < n; i++) {
            float x = w * 0.05f + (i % cols) * cw, y = h * 0.06f + (i / cols) * ch;
            hits[Math.min(i, hits.length - 1)].set(x + 6, y + 6, x + cw - 6, y + ch - 6);
            paint.setColor(used[i] ? 0xffe7f6e4 : marks[i] > 0 ? 0xfffff9f2 : 0xff795297);
            c.drawRoundRect(hits[Math.min(i, hits.length - 1)], 16, 16, paint);
            paint.setColor(marks[i] > 0 || used[i] ? 0xff49334f : Color.WHITE);
            c.drawText(marks[i] > 0 || used[i] ? deck.get(i).substring(0, 1).toUpperCase() : "?", hits[Math.min(i, hits.length - 1)].centerX(), hits[Math.min(i, hits.length - 1)].centerY(), paint);
        }
    }
    private void drawHide(Canvas c, float w, float h) {
        paint.setTextSize(h * 0.08f);
        for (int i = 0; i < spawn && i < 8; i++) {
            float cw = w / Math.min(spawn, 4);
            float x = (i % 4) * cw + cw / 2f, y = h * (i < 4 ? 0.38f : 0.72f);
            hits[i].set(x - cw * 0.35f, y - h * 0.12f, x + cw * 0.35f, y + h * 0.12f);
            paint.setColor(0xff3f8f55); c.drawOval(hits[i], paint);
            if (i == answer) { paint.setColor(0xfff3c1b0); c.drawCircle(x, hits[i].top + 8, 14, paint); paint.setColor(0xff3f8f55); }
        }
    }
    private void drawFeed(Canvas c, float w, float h) {
        paint.setTextSize(h * 0.05f); c.drawText(score + " / " + goal, w / 2f, h * 0.9f, paint);
        for (int i = 0; i < 8; i++) {
            if (used[i]) continue;
            float cw = w / 4f, x = (i % 4) * cw + cw / 2f, y = h * (i < 4 ? 0.32f : 0.62f);
            hits[i].set(x - 36, y - 36, x + 36, y + 36);
            paint.setColor(marks[i] == 0 ? 0xffe23d4a : marks[i] == 1 ? 0xfff08a2a : marks[i] == 2 ? 0xffffd35a : 0xff7dba6a);
            c.drawCircle(x, y, 30, paint);
        }
    }
    private void drawMoney(Canvas c, float w, float h) {
        paint.setTextSize(h * 0.07f); c.drawText("Betaald € " + score + "   nodig € " + goal, w / 2f, h * 0.28f, paint);
        for (int i = 0; i < spawn; i++) {
            float x = w * (0.18f + i * 0.15f), y = h * 0.62f;
            hits[i].set(x - 40, y - 40, x + 40, y + 40);
            paint.setColor(0xffffe7ab); c.drawCircle(x, y, 38, paint); paint.setColor(0xff49334f); c.drawText(String.valueOf(marks[i]), x, y + 10, paint);
        }
    }
    private void drawPads(Canvas c, float w, float h, int n) {
        int cols = Math.min(n, 4);
        for (int i = 0; i < n; i++) {
            float cw = w / cols, x = (i % cols) * cw, y = h * (0.25f + (i / cols) * 0.35f);
            hits[i].set(x + 12, y, x + cw - 12, y + h * 0.28f);
            int color = new int[]{0xffe23d4a, 0xff2f8cff, 0xfff2b705, 0xff2fae63, 0xff8c5cff, 0xffff7ccf, 0xff14a89b}[i % 7];
            paint.setColor(score == i + 10 ? Color.WHITE : color); c.drawRoundRect(hits[i], 20, 20, paint);
        }
    }
    private void drawPuzzle(Canvas c, float w, float h) {
        int n = (int) Math.round(Math.sqrt(spawn + 1)); float cw = Math.min(w, h) * 0.8f / n, x0 = (w - cw * n) / 2f, y0 = h * 0.15f;
        paint.setTextSize(cw * 0.35f);
        for (int i = 0; i <= spawn && i < hits.length; i++) {
            float x = x0 + (i % n) * cw, y = y0 + (i / n) * cw; hits[i].set(x + 4, y + 4, x + cw - 4, y + cw - 4);
            paint.setColor(marks[i] == i ? 0xffe7f6e4 : 0xff795297); c.drawRoundRect(hits[i], 12, 12, paint); paint.setColor(Color.WHITE);
            c.drawText(String.valueOf(marks[i] + 1), hits[i].centerX(), hits[i].centerY(), paint);
        }
    }
    private void drawTower(Canvas c, float w, float h) {
        paint.setColor(0xff795297);
        for (int i = 0; i < score; i++) c.drawRect(w * 0.35f, h * 0.85f - i * 28, w * 0.65f, h * 0.85f - i * 28 + 24, paint);
        float x = w * (0.2f + 0.6f * (0.5f + 0.5f * (float) Math.sin(mover)));
        paint.setColor(0xfff2b705); c.drawRect(x - 50, h * 0.12f, x + 50, h * 0.12f + 24, paint);
        hits[0].set(0, 0, w, h); paint.setColor(0xff49334f); paint.setTextSize(h * 0.05f); c.drawText("Tik om te laten vallen", w / 2f, h * 0.08f, paint);
    }
    private void drawRunner(Canvas c, float w, float h) {
        paint.setColor(0xffd9ead3); c.drawRect(0, h * 0.72f, w, h, paint);
        float ground = h * 0.72f; float jump = (float) Math.max(0, Math.sin(mover * 3));
        paint.setColor(0xff795297); c.drawCircle(w * 0.25f, ground - 30 - jump * 80, 26, paint);
        paint.setColor(0xff3f8f55);
        for (int i = 0; i < 3; i++) { float x = w - ((mover * 80 + i * w / 3) % w); c.drawRect(x, ground - 36, x + 28, ground, paint); hits[i].set(x, ground - 36, x + 28, ground); }
        paint.setColor(0xff49334f); paint.setTextSize(h * 0.05f); c.drawText(id.equals("adventure") ? "Wereld " + ((level - 1) % 8 + 1) + "  ·  tik om te springen" : "Tik om te springen", w / 2f, h * 0.15f, paint);
    }
    private void drawRace(Canvas c, float w, float h) {
        paint.setColor(0xffd9dde8);
        for (int lane = 0; lane < 3; lane++) c.drawRect(w * (0.15f + lane * 0.25f), h * 0.15f, w * (0.35f + lane * 0.25f), h * 0.9f, paint);
        paint.setColor(0xffe23d4a); float y = h * (0.2f + (mover % 1f) * 0.6f); c.drawCircle(w * (0.25f + (score / 15 % 3) * 0.25f), y, 18, paint);
        paint.setColor(0xff2f8cff); c.drawCircle(w * (0.25f + step * 0.25f), h * 0.78f, 22, paint);
        paint.setColor(0xff49334f); paint.setTextSize(h * 0.05f); c.drawText("Circuit " + ((level - 1) % 10 + 1) + "  ·  tik links of rechts", w / 2f, h * 0.1f, paint);
        hits[0].set(0, 0, w / 2f, h); hits[1].set(w / 2f, 0, w, h);
    }
    private void drawCatch(Canvas c, float w, float h) {
        paint.setTextSize(h * 0.05f); paint.setColor(0xff49334f); c.drawText("Gevangen " + score, w / 2f, h * 0.12f, paint);
        float y = (mover * 40 % h); float x = w * (0.2f + (marks[0] % 4) * 0.2f);
        paint.setColor(id.equals("boat") ? 0xffc7824a : 0xffe23d4a); c.drawCircle(x, y, 28, paint); hits[0].set(x - 34, y - 34, x + 34, y + 34);
    }
    private void drawSketch(Canvas c, float w, float h) {
        paint.setColor(0xffffffff); c.drawRect(w * 0.08f, h * 0.12f, w * 0.92f, h * 0.88f, paint);
        paint.setColor(0xff795297); paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(8); c.drawPath(drawing, paint); paint.setStyle(Paint.Style.FILL);
        if (id.equals("coloring")) { paint.setTextSize(h * 0.05f); c.drawText("Kleur de bloem", w / 2f, h * 0.2f, paint); paint.setColor(0xfff3c1b0); c.drawCircle(w / 2f, h / 2f, 40, paint); }
    }
    private void drawTrace(Canvas c, float w, float h) {
        paint.setTextSize(h * 0.4f); paint.setColor(0xffe7dff0); c.drawText("a", w / 2f, h * 0.65f, paint);
        paint.setColor(0xff2fae63); paint.setTextSize(h * 0.05f);
        for (int i = 0; i < goal && i < 8; i++) { float y = h * (0.25f + i * 0.08f); hits[i].set(w / 2f - 30, y - 20, w / 2f + 30, y + 20); paint.setColor(i < step ? 0xff2fae63 : 0xfff2b705); c.drawCircle(w / 2f, y, 16, paint); }
    }
    private void drawKitchen(Canvas c, float w, float h) {
        paint.setColor(0xfff2b705); c.drawCircle(w / 2f, h * 0.48f, Math.min(w, h) * 0.22f, paint);
        paint.setColor(0xff49334f); paint.setTextSize(h * 0.055f); c.drawText("Stap " + (step + 1) + " van " + goal, w / 2f, h * 0.82f, paint);
        hits[0].set(w * 0.2f, h * 0.2f, w * 0.8f, h * 0.75f);
    }
    private void drawSwatch(Canvas c, float w, float h) {
        int color = new int[]{0xffe23d4a, 0xff2f8cff, 0xfff2b705, 0xff2fae63, 0xffff9330, 0xffff7ccf, 0xff8c5cff}[Math.max(0, answer) % 7];
        paint.setColor(color); c.drawCircle(w / 2f, h * 0.45f, Math.min(w, h) * 0.18f, paint);
    }
    private void drawWall(Canvas c, float w, float h) {
        int n = marks[0]; paint.setTextSize(h * 0.08f);
        for (int i = 0; i < n; i++) { paint.setColor(new int[]{0xffe23d4a, 0xff2f8cff, 0xfff2b705, 0xff2fae63, 0xffff9330}[marks[i + 1] % 5]); c.drawRect(w * 0.15f + i * 70, h * 0.4f, w * 0.15f + i * 70 + 58, h * 0.55f, paint); }
        paint.setColor(0xff49334f); c.drawText("?", w * 0.15f + n * 70 + 20, h * 0.52f, paint);
    }

    @Override public boolean onTouchEvent(MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_MOVE && (id.equals("draw") || id.equals("coloring"))) { drawing.lineTo(e.getX(), e.getY()); invalidate(); return true; }
        if (e.getAction() != MotionEvent.ACTION_UP && e.getAction() != MotionEvent.ACTION_DOWN) return true;
        if (e.getAction() == MotionEvent.ACTION_DOWN && (id.equals("draw") || id.equals("coloring"))) { drawing.moveTo(e.getX(), e.getY()); return true; }
        if (e.getAction() != MotionEvent.ACTION_UP || dead) return true;
        float x = e.getX(), y = e.getY();
        if (id.equals("memory") || id.equals("fairymem")) { int i = hit(x, y, deck.size()); if (i >= 0) flip(i); }
        else if (id.equals("hide") || id.equals("wolf") || id.equals("shoe")) { int i = hit(x, y, spawn); if (i >= 0) pick(i); }
        else if (id.equals("feed")) { int i = hit(x, y, 8); if (i >= 0 && !used[i]) feedTap(marks[i] == answer); }
        else if (id.equals("money")) { int i = hit(x, y, spawn); if (i >= 0) pay(marks[i]); }
        else if (id.equals("simon") || id.equals("piano") || id.equals("music")) { int n = id.equals("piano") ? 7 : 4; int i = hit(x, y, n); if (i >= 0) { if (id.equals("simon")) simonPlay(i); else if (i == marks[Math.min(step, goal - 1)] || true) { if (step < goal && i == marks[step]) { step++; if (step >= goal) right(); } else if (!busy) { step = 0; wrong(); } } } }
        else if (id.equals("puzzle") || id.equals("jigsaw")) { int i = hit(x, y, spawn + 1); if (i >= 0) { if (step < 0) step = i; else { int t = marks[step]; marks[step] = marks[i]; marks[i] = t; step = -1; if (solvedPuzzle()) right(); else invalidate(); } } }
        else if (id.equals("trace")) { int i = hit(x, y, goal); if (i == step) { step++; if (step >= goal) right(); else invalidate(); } }
        else if (id.equals("kitchen") || id.equals("pizza") || id.equals("bread") || id.equals("cookies") || id.equals("cake") || id.equals("smoothie") || id.equals("ice")) { if (hits[0].contains(x, y)) kitchenTap(); }
        else if (id.equals("tower")) { float xBlock = getWidth() * (0.2f + 0.6f * (0.5f + 0.5f * (float) Math.sin(mover))); if (Math.abs(xBlock - getWidth() / 2f) < 80 + level * 4) { score++; if (score >= 4 + level) right(); } else wrong(); invalidate(); }
        else if (id.equals("runner") || id.equals("adventure")) { mover += 1.2f; invalidate(); }
        else if (id.equals("race")) { if (x < getWidth() / 2f) step = Math.max(0, step - 1); else step = Math.min(2, step + 1); int lane = score / 15 % 3; if (step == lane && (mover % 1f) > 0.72f) wrong(); invalidate(); }
        else if (id.equals("boat") || id.equals("pop") || id.equals("candy")) { if (hits[0].contains(x, y)) { score++; if (score >= 5 + level) right(); else invalidate(); } }
        performClick(); return true;
    }
    private int hit(float x, float y, int n) { for (int i = 0; i < n && i < hits.length; i++) if (hits[i].contains(x, y)) return i; return -1; }
    @Override public boolean performClick() { super.performClick(); return true; }
}
