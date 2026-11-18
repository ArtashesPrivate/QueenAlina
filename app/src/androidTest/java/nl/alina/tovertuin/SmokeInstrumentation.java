package nl.alina.tovertuin;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;

/** Runs the offline game in the real WebView: menu without code, Alina, groep, voice clips, old stars, a level-14 sum and the parent code. Expects a fresh install. */
public final class SmokeInstrumentation extends Instrumentation {
    private MainActivity activity;
    private int passed;

    @Override public void onCreate(Bundle args) { super.onCreate(args); start(); }

    private void check(boolean ok, String msg) {
        if (!ok) throw new AssertionError(msg);
        passed++;
        Bundle b = new Bundle(); b.putString("stream", "PASS: " + msg + "\n"); sendStatus(0, b);
    }

    private String js(String code) throws Exception {
        final String[] out = {null};
        CountDownLatch done = new CountDownLatch(1);
        runOnMainSync(() -> activity.web.evaluateJavascript("(function(){try{return String(" + code + ");}catch(e){return 'ERR '+e;}})()", v -> { out[0] = v; done.countDown(); }));
        if (!done.await(10, TimeUnit.SECONDS)) throw new AssertionError("No answer from the page for " + code);
        return out[0] == null || out[0].equals("null") ? "" : new JSONArray("[" + out[0] + "]").getString(0);
    }

    private String waitFor(String code, String expected, long ms) throws Exception {
        long end = SystemClock.uptimeMillis() + ms;
        String v = "";
        while (SystemClock.uptimeMillis() < end) {
            v = js(code);
            if (expected == null ? !v.isEmpty() && !v.equals("false") : v.equals(expected)) return v;
            SystemClock.sleep(250);
        }
        throw new AssertionError("Waited for " + code + " = " + expected + ", got " + v);
    }

    @Override public void onStart() {
        Bundle result = new Bundle();
        try {
            Context target = getTargetContext();
            GameRules old = new GameRules(); old.stars = 37;
            new GameStore(target).save(old);
            activity = (MainActivity) startActivitySync(new Intent(target, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

            waitFor("!!document.querySelector('.tile')", "true", 20000);
            check(js("document.title").equals("Queen Alina"), "Page title is Queen Alina");
            check(js("!document.querySelector('.key')").equals("true"), "The app opens straight into the menu, no code");
            check(js("document.querySelector('.bar h1').textContent").contains("Queen Alina"), "The menu is Queen Alina");
            check(js("document.querySelectorAll('.tab').length").equals("7"), "All seven tabs are there");
            check(js("document.querySelector('.bar .h1-lou').getBoundingClientRect().right <= document.getElementById('pal').getBoundingClientRect().left + 1").equals("true"),
                "The title does not run into the buttons");
            check(js("localStorage.getItem('speeltuin-stars')").equals("37"), "Stars from the earlier app carried over");

            check(js("!!document.querySelector('#pal svg.alina')").equals("true"), "Alina is the character in the top bar");
            check(js("!/\\bLOU\\b|\\bLou\\b/.test(document.body.innerText) && /Alina/.test(document.body.innerText)").equals("true"),
                "No LOU on screen; without a profile the child is called Alina");
            js("document.querySelector('.tab[data-t=\"lou\"]').click()");
            waitFor("[].some.call(document.querySelectorAll('.tile .name'),function(n){return n.textContent==='Zorg voor Alina';})", "true", 4000);
            js("[].filter.call(document.querySelectorAll('.tile'),function(t){return t.querySelector('.name').textContent==='Zorg voor Alina';})[0].click()");
            waitFor("!!document.querySelector('#cl svg.alina')", "true", 4000);
            check(js("document.querySelector('#cl svg.alina .al-head').getAnimations().length > 0 && document.querySelector('#cl svg.alina .al-eyes').getAnimations().length > 0").equals("true"),
                "Alina moves: head and eyes are animated");
            runOnMainSync(() -> activity.goBack());
            waitFor("!!document.querySelector('.tile')", "true", 4000);

            js("document.querySelector('.tab[data-t=\"leer\"]').click()");
            waitFor("document.querySelectorAll('.groep').length", "6", 4000);
            js("document.querySelector('.groep[data-g=\"8\"]').click()");
            waitFor("[].some.call(document.querySelectorAll('.tile .name'),function(n){return n.textContent==='Negatieve getallen';})", "true", 4000);
            check(js("localStorage.getItem('speeltuin-groep')").equals("8") && js("!!document.querySelector('.groep[data-g=\"9\"]')").equals("false"),
                "Groep 3 to 8 can be chosen (no groep 9); groep 8 shows negatieve getallen");
            js("document.querySelector('.groep[data-g=\"3\"]').click()");
            waitFor("localStorage.getItem('speeltuin-groep')", "3", 4000);

            check(js("typeof speechSynthesis.speak==='function' && !!window.QAHost").equals("true"), "Device voice is connected to Android");
            js("fetch('voice/voice.json').then(function(r){return r.json();}).then(function(j){window.__qaVoice=Object.keys(j).length;})");
            check(Integer.parseInt(waitFor("window.__qaVoice||''", null, 8000)) > 1000, "Recorded voice lines load offline");
            js("fetch('voice/voice.json').then(function(r){return r.json();}).then(function(j){return fetch('voice/'+j[Object.keys(j)[0]]);})"
                + ".then(function(r){return r.arrayBuffer();}).then(function(a){return new Promise(function(ok,no){new AudioContext().decodeAudioData(a,ok,no);});})"
                + ".then(function(b){window.__qaClip=b.duration>0.1?'yes':'short';},function(e){window.__qaClip='no '+e;})");
            check(waitFor("window.__qaClip||''", null, 8000).equals("yes"), "A recorded voice line decodes to sound");
            js("document.fonts.ready.then(function(){window.__qaFont=document.fonts.check('800 20px \"Baloo 2\"');})");
            check(waitFor("window.__qaFont===undefined?'':window.__qaFont", null, 8000).equals("true"), "Baloo 2 font loads offline");

            js("localStorage.setItem('speeltuin-lv-sum-+','14')");
            js("document.querySelector('.tab[data-t=\"leer\"]').click()");
            waitFor("[].some.call(document.querySelectorAll('.tile .name'),function(n){return n.textContent==='Plussommen';})", "true", 4000);
            js("[].filter.call(document.querySelectorAll('.tile'),function(t){return t.querySelector('.name').textContent==='Plussommen';})[0].click()");
            waitFor("!!document.querySelector('.sum-kind')", "true", 4000);
            check(js("document.querySelector('.sum-kind').textContent").equals("Tot 5000"), "Harder level 14: sums to 5000");
            int a = Integer.parseInt(js("document.querySelector('.eq').textContent.match(/\\d+/g)[0]"));
            int b = Integer.parseInt(js("document.querySelector('.eq').textContent.match(/\\d+/g)[1]"));
            check(a >= 1000 && a + b <= 5000, "The sum is between 1000 and 5000: " + a + " + " + b);
            check(js("!!document.querySelector('.ans[data-n=\"" + (a + b) + "\"]')").equals("true"), "The right answer is one of the buttons");
            js("document.querySelector('.ans[data-n=\"" + (a + b) + "\"]').click()");
            waitFor("document.querySelectorAll('#lvProg i.on').length", "1", 3000);
            check(js("localStorage.getItem('speeltuin-stars')").equals("38"), "A right answer gives a star");

            runOnMainSync(() -> activity.goBack());
            waitFor("!!document.querySelector('.tile')", "true", 4000);
            check(true, "Android back returns to the menu");

            js("localStorage.setItem('speeltuin-lv-money','5')");
            js("[].filter.call(document.querySelectorAll('.tile'),function(t){return t.querySelector('.name').textContent==='Geld tellen';})[0].click()");
            waitFor("document.querySelectorAll('.mo-btn').length", "6", 4000);
            int price = Integer.parseInt(js("document.querySelector('.mo-tag').textContent.replace(/\\D/g,'')"));
            check(price >= 20 && price <= 100, "Harder money level 5: price " + price + " euro");
            check(js("[].every.call(document.querySelectorAll('.mo-btn'),function(b){var r=b.getBoundingClientRect();return r.bottom<=innerHeight+1&&r.right<=innerWidth+1&&r.width>30;})").equals("true"),
                "All six coins and notes fit on the screen");

            runOnMainSync(() -> activity.goBack());
            waitFor("!!document.querySelector('.tile')", "true", 4000);
            js("document.getElementById('parentBtn').click()");
            waitFor("!!document.querySelector('.key')", "true", 4000);
            check(js("!document.getElementById('pReset')").equals("true"), "Voor ouders asks for the code first");
            js("['2','0','2','1'].forEach(function(k){document.querySelector('.key[data-k=\"'+k+'\"]').click();})");
            waitFor("!!document.getElementById('pReset')", "true", 4000);
            check(true, "Code 2021 opens Voor ouders");

            result.putString("stream", "PASS: " + passed + " checks in the offline game.\n");
            finish(Activity.RESULT_OK, result);
        } catch (Throwable t) {
            result.putString("stream", "FAIL: " + android.util.Log.getStackTraceString(t));
            finish(Activity.RESULT_CANCELED, result);
        }
    }
}
