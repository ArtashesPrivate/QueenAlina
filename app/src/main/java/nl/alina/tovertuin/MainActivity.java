package nl.alina.tovertuin;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.Locale;

/** Queen Alina, same games and tabs as Spelen met LOU, with a harder level ladder on top. */
public class MainActivity extends Activity {
    private static final int CREAM = 0xfffff9f2, PLUM = 0xff49334f, PURPLE = 0xff795297, MUTED = 0xff735d73;
    private static final String[] TABS = {"Alina", "Keuken", "Bouwen", "Spelletjes", "Leren", "Sprookjes", "Sinterklaas"};
    private static final String[] HELLO = {
        "Hoi! Ik ben Alina. Wat gaan we samen doen?",
        "Kom, we gaan koken voor Alina!",
        "Kom, we gaan bouwen met Alina!",
        "Kies een spelletje!",
        "Rekenen en letters leren!",
        "Er was eens... een meisje dat Alina heette!",
        "Sinterklaas is in het land!"
    };
    private static final String[][] GAMES = {
        {"Alina", "Zorg voor Alina", "Eten, slapen, wassen", "care", "#2fae63"},
        {"Alina", "Voer Alina", "Tel de hapjes", "feed", "#f2b705"},
        {"Alina", "Alina zegt", "Doe Alina na", "simon", "#8c5cff"},
        {"Alina", "Verstoppertje", "Waar is Alina?", "hide", "#14a89b"},
        {"Alina", "Alina-puzzel", "Zet de stukjes", "puzzle", "#ff7ccf"},
        {"Alina", "Alina aankleden", "Met je sterren", "dress", "#ff6b5b"},
        {"Alina", "Fotoalbum", "Foto van Alina", "album", "#2f8cff"},
        {"Alina", "Alina's kamer", "Richt in met sterren", "room", "#c7824a"},
        {"Alina", "Feestje", "Kaarsjes en cadeautjes", "party", "#e0559f"},
        {"Alina", "Naar bed", "Welterusten Alina", "bed", "#5b74c2"},
        {"Keuken", "Pizza voor Alina", "Rollen, smeren, bakken", "pizza", "#ff6b5b"},
        {"Keuken", "Boterham voor Alina", "Smeren en beleggen", "bread", "#f2b705"},
        {"Keuken", "Koekjes bakken", "Kneden, uitsteken, bakken", "cookies", "#c7824a"},
        {"Keuken", "Taart voor Alina", "Bakken en versieren", "cake", "#ff7ccf"},
        {"Keuken", "Smoothie maken", "Fruit in de blender", "smoothie", "#8c5cff"},
        {"Keuken", "IJsjes maken", "Bolletjes en toppings", "ice", "#2f8cff"},
        {"Bouwen", "Torenbouwer", "Hoe hoog wordt hij?", "tower", "#ff9330"},
        {"Bouwen", "Vormen bouwen", "Kies de vorm", "shapes", "#2f8cff"},
        {"Bouwen", "Vrij bouwen", "Bouw wat je wilt", "draw", "#2fae63"},
        {"Bouwen", "Muur bouwen", "Maak het patroon af", "wall", "#d7263d"},
        {"Spelletjes", "Memory", "Zoek de paren", "memory", "#8c5cff"},
        {"Spelletjes", "Tellen", "1 tot 50", "count", "#2f8cff"},
        {"Spelletjes", "Kleuren", "Welke kleur?", "color", "#ff6b5b"},
        {"Spelletjes", "Ballonnen", "Knal ze!", "pop", "#3ccf7a"},
        {"Spelletjes", "Puzzelen", "Zet de stukjes", "jigsaw", "#f2b705"},
        {"Spelletjes", "Alina springt", "Rennen en springen", "runner", "#14a89b"},
        {"Spelletjes", "Alina's avontuur", "8 werelden", "adventure", "#2fae63"},
        {"Spelletjes", "Alina racet", "10 circuits", "race", "#ff6b5b"},
        {"Spelletjes", "Muziek maken", "Trommels en tonen", "music", "#ff9330"},
        {"Spelletjes", "Piano", "Speel na", "piano", "#1d2a4d"},
        {"Spelletjes", "Kleurplaten", "Kleur de tekening", "coloring", "#ff7ccf"},
        {"Spelletjes", "Vrij kleuren", "Teken wat je wilt", "draw", "#8c5cff"},
        {"Leren", "Plussommen", "Tot 10000", "plus", "#2f8cff"},
        {"Leren", "Minsommen", "Tot 10000", "minus", "#ff6b5b"},
        {"Leren", "Keersommen", "Ook 11 en 12", "times", "#8c5cff"},
        {"Leren", "Deelsommen", "Eerlijk verdelen", "divide", "#14a89b"},
        {"Leren", "Letters schrijven", "Volg de stippen", "trace", "#8c5cff"},
        {"Leren", "Welke letter?", "a van aap", "letter", "#2fae63"},
        {"Leren", "Klokkijken", "Tot op de minuut", "clock", "#14a89b"},
        {"Leren", "Geld tellen", "Tot 100 euro", "money", "#f2b705"},
        {"Sprookjes", "Mandje voor oma", "Veilig het pad", "basket", "#ff4b4b"},
        {"Sprookjes", "De drie beren", "Klein of groot?", "bears", "#f2b705"},
        {"Sprookjes", "Sprookjes-memory", "Zoek de paren", "fairymem", "#2f8cff"},
        {"Sprookjes", "De drie biggetjes", "Welk huis blijft staan", "piggies", "#ff9ccf"},
        {"Sprookjes", "Waar is de wolf?", "Zoek de wolf", "wolf", "#5b74c2"},
        {"Sprookjes", "Snoephuisje", "Pak de snoepjes", "candy", "#ff7ccf"},
        {"Sinterklaas", "Pepernoten tellen", "Tot 50", "pepper", "#c7824a"},
        {"Sinterklaas", "Schoen zetten", "Waar is het cadeau?", "shoe", "#2f8cff"},
        {"Sinterklaas", "Cadeautjes", "Welke kleur?", "gifts", "#3ccf7a"},
        {"Sinterklaas", "De stoomboot", "Vang de pepernoten", "boat", "#d7263d"},
    };

    private GameStore store;
    private GameRules game;
    private TextToSpeech tts;
    private boolean voiceReady;
    private LinearLayout root, choices;
    private TextView promptView;
    Arcade arcade;
    private String tab = "Alina";
    private int photos, candles;
    private final boolean[] outfit = new boolean[4];
    private final boolean[] furniture = new boolean[4];

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        store = new GameStore(this); game = store.load();
        if (state != null) tab = state.getString("tab", tab);
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS && tts.setLanguage(Locale.forLanguageTag("nl-NL")) >= TextToSpeech.LANG_AVAILABLE) voiceReady = true;
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                public void onStart(String id) {}
                public void onDone(String id) {}
                public void onError(String id) { voiceReady = false; }
            });
        });
        if (prefs().getBoolean("unlocked", false)) hub(); else lock(this::hub);
    }

    @Override protected void onSaveInstanceState(Bundle out) { super.onSaveInstanceState(out); out.putString("tab", tab); }
    @Override protected void onPause() { if (arcade != null) arcade.stop(); store.save(game); super.onPause(); }
    @Override protected void onDestroy() { if (tts != null) { tts.stop(); tts.shutdown(); } super.onDestroy(); }

    private android.content.SharedPreferences prefs() { return getSharedPreferences("alina_progress_v1", MODE_PRIVATE); }
    private int dp(float n) { return (int) (n * getResources().getDisplayMetrics().density + .5f); }
    private GradientDrawable bg(int color) { GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(18)); return d; }
    private TextView text(String value, int size, int color, boolean bold) {
        TextView v = new TextView(this); v.setText(value); v.setTextSize(size); v.setTextColor(color);
        v.setTypeface(Typeface.create("sans-serif-rounded", bold ? Typeface.BOLD : Typeface.NORMAL)); return v;
    }
    private Button button(String label, int color, Runnable action) {
        Button b = new Button(this); b.setText(label); b.setAllCaps(false); b.setTextColor(Color.WHITE); b.setTextSize(16);
        b.setTypeface(Typeface.create("sans-serif-rounded", Typeface.BOLD)); b.setBackground(bg(color));
        b.setMinHeight(dp(52)); b.setStateListAnimator(null); b.setOnClickListener(v -> action.run()); return b;
    }
    private void shell() {
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(CREAM);
        root.setOnApplyWindowInsetsListener((v, insets) -> {
            if (Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets bars = insets.getInsets(android.view.WindowInsets.Type.systemBars() | android.view.WindowInsets.Type.displayCutout());
                root.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            }
            return insets;
        });
        setContentView(root); root.requestApplyInsets();
    }
    private void say(String line) { if (game.sound && voiceReady && tts != null) tts.speak(line, TextToSpeech.QUEUE_FLUSH, null, "q" + System.currentTimeMillis()); }

    private void lock(Runnable ok) {
        shell();
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.addView(text("Queen Alina", 32, PLUM, true));
        TextView sub = text("Spelen en leren. Vul de code in. Dat hoeft maar één keer.", 16, MUTED, false);
        sub.setGravity(Gravity.CENTER); sub.setPadding(dp(24), dp(8), dp(24), dp(12)); root.addView(sub);
        TextView dots = text("○ ○ ○ ○", 28, PURPLE, true); dots.setGravity(Gravity.CENTER); root.addView(dots);
        TextView msg = text("", 16, 0xffd7263d, false); msg.setGravity(Gravity.CENTER); root.addView(msg);
        final StringBuilder entry = new StringBuilder();
        LinearLayout pad = new LinearLayout(this); pad.setOrientation(LinearLayout.VERTICAL); pad.setGravity(Gravity.CENTER);
        String[] rows = {"123", "456", "789", "C0⌫"};
        for (String row : rows) {
            LinearLayout line = new LinearLayout(this); line.setGravity(Gravity.CENTER);
            for (char ch : row.toCharArray()) {
                String key = String.valueOf(ch);
                Button b = button(key.equals("C") ? "wis" : key.equals("⌫") ? "terug" : key, PURPLE, () -> {
                    if (key.equals("C")) entry.setLength(0); else if (key.equals("⌫")) { if (entry.length() > 0) entry.setLength(entry.length() - 1); }
                    else if (entry.length() < 4) entry.append(key);
                    StringBuilder shown = new StringBuilder();
                    for (int i = 0; i < 4; i++) shown.append(i < entry.length() ? "● " : "○ ");
                    dots.setText(shown.toString().trim());
                    if (entry.length() == 4) {
                        if (entry.toString().equals("2021")) { prefs().edit().putBoolean("unlocked", true).apply(); say("Welkom Alina!"); ok.run(); }
                        else { msg.setText("Die code klopt niet. Probeer het nog eens."); entry.setLength(0); dots.setText("○ ○ ○ ○"); }
                    }
                });
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(78), dp(64)); p.setMargins(dp(6), dp(6), dp(6), dp(6)); line.addView(b, p);
            }
            pad.addView(line);
        }
        root.addView(pad);
    }

    private void hub() {
        if (arcade != null) arcade.stop();
        shell();
        LinearLayout bar = new LinearLayout(this); bar.setGravity(Gravity.CENTER_VERTICAL); bar.setPadding(dp(16), dp(10), dp(12), dp(6));
        TextView title = text("Queen Alina", 24, PLUM, true); bar.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        TextView stars = text("★ " + game.stars, 18, PLUM, true); stars.setBackground(bg(0xffffe7ab)); stars.setPadding(dp(12), dp(8), dp(12), dp(8));
        stars.setContentDescription(game.stars + " sterren"); bar.addView(stars);
        Button parents = button("Ouders", PURPLE, () -> lock(this::parentPanel)); parents.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(-2, dp(48)); pp.leftMargin = dp(8); bar.addView(parents, pp);
        root.addView(bar);
        HorizontalScrollView tabs = new HorizontalScrollView(this); tabs.setHorizontalScrollBarEnabled(false);
        LinearLayout tabRow = new LinearLayout(this); tabRow.setPadding(dp(12), 0, dp(12), dp(6));
        for (int i = 0; i < TABS.length; i++) {
            final String name = TABS[i];
            Button b = button(name, name.equals(tab) ? PURPLE : 0xffeee1f2, () -> { tab = name; hub(); });
            if (!name.equals(tab)) b.setTextColor(PLUM);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-2, dp(48)); p.rightMargin = dp(8); tabRow.addView(b, p);
        }
        tabs.addView(tabRow); root.addView(tabs);
        TextView hello = text(HELLO[indexOf(tab)], 18, PLUM, true); hello.setPadding(dp(18), dp(8), dp(18), dp(8)); root.addView(hello);
        ScrollView scroll = new ScrollView(this);
        LinearLayout grid = new LinearLayout(this); grid.setOrientation(LinearLayout.VERTICAL); grid.setPadding(dp(12), 0, dp(12), dp(16));
        LinearLayout row = null; int col = 0;
        for (String[] g : GAMES) if (g[0].equals(tab)) {
            if (col % 2 == 0) { row = new LinearLayout(this); grid.addView(row, new LinearLayout.LayoutParams(-1, -2)); }
            Button tile = button(g[1] + "\n" + g[2], Color.parseColor(g[4]), () -> open(g[3], g[1]));
            tile.setTextSize(15); tile.setMinHeight(dp(108));
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, -2, 1); p.setMargins(dp(6), dp(6), dp(6), dp(6));
            row.addView(tile, p); col++;
        }
        if (col % 2 == 1) { View filler = new View(this); row.addView(filler, new LinearLayout.LayoutParams(0, 1, 1)); }
        scroll.addView(grid); root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
    }

    private int indexOf(String name) { for (int i = 0; i < TABS.length; i++) if (TABS[i].equals(name)) return i; return 0; }

    private void open(String id, String title) {
        if (id.equals("care")) { care(); return; }
        if (id.equals("dress")) { shop(); return; }
        if (id.equals("album")) { album(); return; }
        if (id.equals("room")) { room(); return; }
        if (id.equals("party")) { party(); return; }
        if (id.equals("bed")) { bed(); return; }
        shell();
        LinearLayout top = new LinearLayout(this); top.setGravity(Gravity.CENTER_VERTICAL); top.setPadding(dp(8), dp(8), dp(8), 0);
        top.addView(button("Terug", PURPLE, () -> { if (arcade != null) arcade.stop(); hub(); }));
        promptView = text("", 18, PLUM, true); promptView.setPadding(dp(12), 0, dp(8), 0);
        top.addView(promptView, new LinearLayout.LayoutParams(0, -2, 1)); root.addView(top);
        arcade = new Arcade(this, new Arcade.Host() {
            public void hud(String heading, String kind, String prompt, int lives, int done, int need, String[] options) {
                runOnUiThread(() -> showHud(heading, kind, prompt, lives, done, need, options));
            }
            public void say(String line) { MainActivity.this.say(line); }
            public void stars(int n) { game.stars += n; store.save(game); }
            public int levelOf(String key) { return prefs().getInt(key, 1); }
            public void saveLevel(String key, int level) { prefs().edit().putInt(key, level).apply(); }
        });
        root.addView(arcade, new LinearLayout.LayoutParams(-1, 0, 1));
        choices = new LinearLayout(this); choices.setOrientation(LinearLayout.VERTICAL); choices.setPadding(dp(10), dp(4), dp(10), dp(8));
        root.addView(choices);
        arcade.open(id, title);
    }

    private void showHud(String heading, String kind, String prompt, int lives, int done, int need, String[] options) {
        if (promptView == null) return;
        String hearts = lives >= 3 ? "❤❤❤" : lives == 2 ? "❤❤" : lives == 1 ? "❤" : "";
        promptView.setText(heading + "\n" + kind + "   " + done + "/" + need + "  " + hearts + "\n" + prompt);
        choices.removeAllViews();
        if (options == null) return;
        LinearLayout line = new LinearLayout(this);
        for (int i = 0; i < options.length; i++) {
            final String label = options[i];
            Button b = button(label, 0xffeee1f2, () -> arcade.choose(label));
            b.setTextColor(PLUM);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, -2, 1); p.setMargins(dp(4), dp(4), dp(4), dp(4));
            line.addView(b, p);
            if (i % 2 == 1 || i == options.length - 1) { choices.addView(line); line = new LinearLayout(this); }
        }
    }

    private void page(String title, String body, String action, Runnable go) {
        shell();
        root.setPadding(root.getPaddingLeft() + dp(16), root.getPaddingTop(), root.getPaddingRight() + dp(16), dp(12));
        root.addView(button("Terug", PURPLE, this::hub));
        root.addView(text(title, 28, PLUM, true));
        TextView t = text(body, 18, PLUM, false); t.setPadding(0, dp(12), 0, dp(12)); root.addView(t);
        if (action != null) root.addView(button(action, PURPLE, go));
    }

    private void care() {
        page("Zorg voor Alina", "Buikje " + game.needs[game.activePet][0] + "   verzorging " + game.needs[game.activePet][1] + "   rust " + game.needs[game.activePet][2] + ".\nEten, wassen en slapen zijn gratis. Alina wordt niet ziek als je een dag niet speelt.", null, null);
        root.addView(button("Eten geven", 0xff2fae63, () -> { game.care(0); store.save(game); say("Mmm, dank je wel!"); care(); }));
        root.addView(button("Wassen", 0xff2f8cff, () -> { game.care(1); store.save(game); say("Helemaal fris!"); care(); }));
        root.addView(button("Slapen", 0xff5b74c2, () -> { game.care(3); store.save(game); say("Welterusten."); care(); }));
        root.addView(button("Spelen", 0xfff2b705, () -> { game.care(2); store.save(game); say("Hoera, samen spelen!"); care(); }));
    }

    private void shop() {
        String[] names = {"Bloem in het haar", "Kroon", "Sjaal", "Bril"}; int[] price = {5, 15, 10, 20};
        page("Alina aankleden", "Je hebt " + game.stars + " sterren. Kleding blijft van jou.", null, null);
        for (int i = 0; i < names.length; i++) {
            final int n = i;
            String label = outfit[n] ? names[n] + "  ·  aan" : names[n] + "  ·  ★ " + price[n];
            root.addView(button(label, outfit[n] ? 0xff2fae63 : PURPLE, () -> {
                if (outfit[n]) return;
                if (game.stars < price[n]) { say("Nog even sparen."); return; }
                game.stars -= price[n]; outfit[n] = true; store.save(game); say("Mooi!"); shop();
            }));
        }
    }

    private void album() { page("Fotoalbum", "Je hebt " + photos + " foto's van Alina. Een foto kost niets.", "Maak een foto", () -> { photos++; say("Klik! Wat een lieve foto."); album(); }); }
    private void room() {
        String[] names = {"Bank", "Lamp", "Plant", "Kussen"}; int[] price = {8, 6, 4, 5};
        page("Alina's kamer", "Zet meubels neer met je sterren. Je hebt er " + game.stars + ".", null, null);
        for (int i = 0; i < names.length; i++) {
            final int n = i;
            root.addView(button(furniture[n] ? names[n] + " staat er" : names[n] + "  ·  ★ " + price[n], furniture[n] ? 0xff2fae63 : 0xffc7824a, () -> {
                if (furniture[n]) return;
                if (game.stars < price[n]) { say("Nog even sparen."); return; }
                game.stars -= price[n]; furniture[n] = true; store.save(game); say("Staat mooi!"); room();
            }));
        }
    }
    private void party() {
        page("Feestje", "Kaarsjes aangestoken: " + candles + " van 5. Daarna mag je blazen.", candles >= 5 ? "Blaas uit" : "Steek een kaarsje aan", () -> {
            if (candles >= 5) { candles = 0; game.stars += 2; store.save(game); say("Gefeliciteerd!"); }
            else { candles++; say("Een kaarsje brandt."); }
            party();
        });
    }
    private void bed() { page("Naar bed", "Stop Alina in. Ze slaapt en is morgen weer vrolijk.", "Dekentje erover", () -> { game.care(3); store.save(game); say("Welterusten Alina."); }); }

    private void parentPanel() {
        page("Voor ouders", "De zware levels gaan verder dan de gewone ladder: sommen tot 10.000, tafels van 11 en 12, tellen tot 50, de klok tot op de minuut en betalen tot 100 euro. Drie hartjes, vijf goede antwoorden is een level omhoog.", null, null);
        root.addView(button(game.sound ? "Geluid aan" : "Geluid uit", PURPLE, () -> { game.sound = !game.sound; store.save(game); parentPanel(); }));
        root.addView(button("Zet de zware levels aan", 0xffd7263d, () -> {
            android.content.SharedPreferences.Editor e = prefs().edit();
            e.putInt("lv-plus", 14).putInt("lv-minus", 14).putInt("lv-times", 9).putInt("lv-divide", 9);
            e.putInt("lv-count", 5).putInt("lv-pepper", 5).putInt("lv-clock", 4).putInt("lv-money", 4);
            e.putInt("lv-memory", 8).putInt("lv-fairymem", 8).putInt("lv-hide", 6).apply();
            say("De zware levels staan aan."); parentPanel();
        }));
        root.addView(button("Alle levels terug naar 1", PURPLE, () -> {
            android.content.SharedPreferences.Editor e = prefs().edit();
            for (String[] g : GAMES) e.putInt("lv-" + g[3], 1);
            e.apply(); parentPanel();
        }));
        root.addView(button("Opnieuw om de code vragen", PURPLE, () -> { prefs().edit().putBoolean("unlocked", false).apply(); lock(this::hub); }));
    }
}
