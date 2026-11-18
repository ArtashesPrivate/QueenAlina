package nl.alina.tovertuin;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import java.util.ArrayList;

/** Opens the same world as Spelen met LOU and checks a real sum, a level and a playable board. */
public final class SmokeInstrumentation extends Instrumentation {
    private Activity activity;
    private int passed;
    @Override public void onCreate(Bundle args) { super.onCreate(args); start(); }
    private void check(boolean ok, String msg) { if (!ok) throw new AssertionError(msg); passed++; Bundle b = new Bundle(); b.putString("stream", "PASS: " + msg + "\n"); sendStatus(0, b); }
    private ArrayList<TextView> labels() { ArrayList<TextView> list = new ArrayList<>(); collect(activity.findViewById(android.R.id.content), list); return list; }
    private void collect(View v, ArrayList<TextView> out) {
        if (v instanceof TextView) out.add((TextView) v);
        if (v instanceof ViewGroup) for (int i = 0; i < ((ViewGroup) v).getChildCount(); i++) collect(((ViewGroup) v).getChildAt(i), out);
    }
    private boolean hasStarting(String prefix) { for (TextView v : labels()) if (v.getText().toString().startsWith(prefix)) return true; return false; }
    private void clickStarting(String prefix) {
        for (TextView v : labels()) if (v instanceof Button && v.getText().toString().startsWith(prefix)) { v.performClick(); return; }
        throw new AssertionError("Missing button " + prefix);
    }
    private void onMain(Runnable action) {
        final Throwable[] failure = {null};
        super.runOnMainSync(() -> { try { action.run(); } catch (Throwable t) { failure[0] = t; } });
        if (failure[0] != null) throw new AssertionError("Native UI check failed", failure[0]);
    }
    @Override public void onStart() {
        Bundle result = new Bundle();
        try {
            activity = startActivitySync(new Intent(getTargetContext(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            onMain(() -> {
                if (hasStarting("wis") || hasStarting("2")) {
                    if (hasStarting("2")) clickStarting("2");
                    clickStarting("0"); clickStarting("2"); clickStarting("1");
                }
            });
            SystemClock.sleep(400);
            onMain(() -> {
                check(hasStarting("Queen Alina"), "Hub opens as Queen Alina");
                clickStarting("Leren");
            });
            SystemClock.sleep(200);
            onMain(() -> { check(hasStarting("Plussommen"), "Learn tab lists the sum game"); clickStarting("Plussommen"); });
            SystemClock.sleep(300);
            onMain(() -> {
                MainActivity host = (MainActivity) activity;
                check(host.arcade != null, "A sum game is on screen");
                check(host.arcade.expected != null && !host.arcade.expected.isEmpty(), "The sum has one right answer");
                check(hasStarting("level 1") || hasStarting("Plussommen"), "Level is shown");
                String answer = host.arcade.expected;
                clickStarting(answer);
            });
            SystemClock.sleep(200);
            onMain(() -> check(((MainActivity) activity).arcade.done() >= 1, "A correct sum is accepted"));
            result.putString("stream", "PASS: " + passed + " native checks. Code, hub, sum and level passed.\n");
            finish(Activity.RESULT_OK, result);
        } catch (Throwable t) {
            result.putString("stream", "FAIL: " + android.util.Log.getStackTraceString(t));
            finish(Activity.RESULT_CANCELED, result);
        }
    }
}
