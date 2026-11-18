package nl.alina.tovertuin;

import android.content.Context;
import android.graphics.*;
import android.view.MotionEvent;
import android.view.View;
import java.util.Random;

/**
 * A playable board, drawn and tapped. Three games:
 * feed (tap the right snacks), memory (flip matching cards), hide (find the ear).
 * Original drawings for Queen Alina. Not a copy of another app's art.
 */
public final class PlayStage extends View {
    public interface Listener {
        void solved();
        void mistake(String hint);
    }

    private static final int SLOTS = 8;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int kind; // 0 feed, 1 memory, 2 hide
    private final int target;
    private final String food;
    private final Listener listener;
    private final String[] slotKind = new String[SLOTS];
    private final boolean[] taken = new boolean[SLOTS];
    private final int[] cards = new int[4];
    private final boolean[] open = new boolean[4];
    private final boolean[] matched = new boolean[4];
    private final RectF[] boxes = new RectF[SLOTS];
    private int given, firstCard = -1, hideIndex;
    private boolean solved, busy;
    private String status = "";

    public PlayStage(Context context, int kind, int target, String food, int seed, Listener listener) {
        super(context);
        this.kind = kind;
        this.target = target;
        this.food = food == null ? "aardbei" : food;
        this.listener = listener;
        for (int i = 0; i < SLOTS; i++) boxes[i] = new RectF();
        Random random = new Random(seed);
        if (kind == 0) dealFood(random);
        else if (kind == 1) dealCards(random);
        else hideIndex = random.nextInt(3);
        setClickable(true);
        refreshDescription();
    }

    private void dealFood(Random random) {
        for (int i = 0; i < SLOTS; i++) slotKind[i] = i < target ? food : (random.nextBoolean() ? "blad" : "bloem");
        for (int i = SLOTS - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            String swap = slotKind[i]; slotKind[i] = slotKind[j]; slotKind[j] = swap;
        }
    }

    private void dealCards(Random random) {
        int[] deck = {0, 0, 1, 1};
        for (int i = 3; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int swap = deck[i]; deck[i] = deck[j]; deck[j] = swap;
        }
        System.arraycopy(deck, 0, cards, 0, 4);
    }

    public int given() { return given; }
    public int target() { return target; }
    public String food() { return food; }
    public int hidingSpot() { return hideIndex; }
    public int matchedCount() { int n = 0; for (boolean m : matched) if (m) n++; return n; }

    /** Indices of one still-closed matching pair. For the device test. */
    public int[] openPair() {
        for (int i = 0; i < 4; i++) for (int j = i + 1; j < 4; j++) {
            if (cards[i] == cards[j] && !matched[i] && !matched[j]) return new int[]{i, j};
        }
        return new int[]{0, 1};
    }

    public boolean tapNextFood() {
        for (int i = 0; i < SLOTS; i++) if (!taken[i] && food.equals(slotKind[i])) { takeFood(i); return true; }
        return false;
    }

    public boolean tapDistractor() {
        for (int i = 0; i < SLOTS; i++) if (!taken[i] && !food.equals(slotKind[i])) { missFood(); return true; }
        return false;
    }

    public void tapCard(int index) { flip(index); }

    public void tapBush(int index) { pickBush(index); }

    private void takeFood(int index) {
        if (solved || taken[index]) return;
        taken[index] = true; given++;
        status = given + " van " + target;
        refreshDescription(); invalidate();
        if (given >= target) finish();
    }

    private void missFood() {
        if (solved || listener == null) return;
        listener.mistake(LessonText.WRONG_FEED[given % LessonText.WRONG_FEED.length]);
    }

    private void flip(int index) {
        if (solved || busy || index < 0 || index > 3 || open[index] || matched[index]) return;
        open[index] = true; invalidate();
        if (firstCard < 0) { firstCard = index; return; }
        int previous = firstCard; firstCard = -1;
        if (cards[previous] == cards[index]) {
            matched[previous] = matched[index] = true; invalidate();
            if (matchedCount() == 4) finish();
        } else {
            busy = true;
            postDelayed(() -> {
                open[previous] = open[index] = false; busy = false; invalidate();
                if (listener != null) listener.mistake(LessonText.WRONG_MEMORY[0]);
            }, 700);
        }
    }

    private void pickBush(int index) {
        if (solved || index < 0 || index > 2) return;
        if (index == hideIndex) finish();
        else if (listener != null) listener.mistake(LessonText.WRONG_HIDE[index % LessonText.WRONG_HIDE.length]);
    }

    private void finish() {
        if (solved) return;
        solved = true; refreshDescription(); invalidate();
        if (listener != null) listener.solved();
    }

    private void refreshDescription() {
        if (kind == 0) setContentDescription("Hapjes: " + given + " van " + target + " " + food);
        else if (kind == 1) setContentDescription("Memory, " + matchedCount() + " kaartjes gevonden");
        else setContentDescription("Drie struikjes. Eentje heeft een oortje.");
    }

    private void layout(float w, float h) {
        if (kind == 1) {
            float size = Math.min(w * 0.42f, h * 0.42f);
            float gap = size * 0.12f;
            float x0 = (w - size * 2 - gap) / 2f, y0 = (h - size * 2 - gap) / 2f;
            for (int i = 0; i < 4; i++) {
                float x = x0 + (i % 2) * (size + gap), y = y0 + (i / 2) * (size + gap);
                boxes[i].set(x, y, x + size, y + size);
            }
        } else if (kind == 2) {
            float bw = w * 0.26f, bh = h * 0.55f, y = h * 0.28f;
            for (int i = 0; i < 3; i++) {
                float x = w * (0.08f + i * 0.31f);
                boxes[i].set(x, y, x + bw, y + bh);
            }
        } else {
            int cols = 4;
            float cellW = w / cols, cellH = h * 0.78f / 2f;
            for (int i = 0; i < SLOTS; i++) {
                float cx = cellW * (i % cols + 0.5f), cy = h * 0.06f + cellH * (i / cols + 0.5f);
                float r = Math.min(cellW, cellH) * 0.42f;
                boxes[i].set(cx - r, cy - r, cx + r, cy + r);
            }
        }
    }

    @Override protected void onDraw(Canvas canvas) {
        float w = getWidth(), h = getHeight();
        layout(w, h);
        paint.setColor(0xfffff3ea);
        canvas.drawRoundRect(0, 0, w, h, 28, 28, paint);
        if (kind == 0) drawFood(canvas);
        else if (kind == 1) drawMemory(canvas);
        else drawHide(canvas);
    }

    private void drawFood(Canvas canvas) {
        for (int i = 0; i < SLOTS; i++) {
            if (taken[i]) continue;
            RectF b = boxes[i];
            float cx = b.centerX(), cy = b.centerY(), r = b.width() / 2f;
            drawSnack(canvas, slotKind[i], cx, cy, r);
        }
        paint.setColor(0xff49334f);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(boxes[0].height() * 0.55f);
        paint.setTypeface(Typeface.create("sans-serif-rounded", Typeface.BOLD));
        canvas.drawText(given + " / " + target, getWidth() / 2f, getHeight() * 0.94f, paint);
    }

    private void drawSnack(Canvas canvas, String kindName, float cx, float cy, float r) {
        if ("aardbei".equals(kindName)) {
            paint.setColor(0xffe23d4a); canvas.drawCircle(cx, cy + r * 0.08f, r * 0.72f, paint);
            paint.setColor(0xff3e8f45);
            canvas.drawOval(cx - r * 0.28f, cy - r * 0.85f, cx + r * 0.05f, cy - r * 0.25f, paint);
            canvas.drawOval(cx - r * 0.05f, cy - r * 0.9f, cx + r * 0.32f, cy - r * 0.28f, paint);
            paint.setColor(0xffffe08a);
            canvas.drawCircle(cx - r * 0.22f, cy, r * 0.08f, paint);
            canvas.drawCircle(cx + r * 0.16f, cy + r * 0.22f, r * 0.08f, paint);
            canvas.drawCircle(cx + r * 0.02f, cy - r * 0.18f, r * 0.08f, paint);
        } else if ("wortel".equals(kindName)) {
            paint.setColor(0xfff08a2a);
            Path path = new Path();
            path.moveTo(cx, cy + r * 0.85f); path.lineTo(cx - r * 0.38f, cy - r * 0.15f); path.lineTo(cx + r * 0.38f, cy - r * 0.15f); path.close();
            canvas.drawPath(path, paint);
            paint.setColor(0xff3e8f45);
            canvas.drawOval(cx - r * 0.45f, cy - r * 0.7f, cx + r * 0.1f, cy - r * 0.05f, paint);
        } else if ("botje".equals(kindName)) {
            paint.setColor(0xfff4e2c4);
            canvas.drawRoundRect(cx - r * 0.7f, cy - r * 0.22f, cx + r * 0.7f, cy + r * 0.22f, r, r, paint);
            canvas.drawCircle(cx - r * 0.55f, cy - r * 0.28f, r * 0.28f, paint);
            canvas.drawCircle(cx - r * 0.55f, cy + r * 0.28f, r * 0.28f, paint);
            canvas.drawCircle(cx + r * 0.55f, cy - r * 0.28f, r * 0.28f, paint);
            canvas.drawCircle(cx + r * 0.55f, cy + r * 0.28f, r * 0.28f, paint);
        } else if ("blad".equals(kindName)) {
            paint.setColor(0xff7dba6a);
            canvas.drawOval(cx - r * 0.7f, cy - r * 0.4f, cx + r * 0.7f, cy + r * 0.4f, paint);
            paint.setColor(0xff3e8f45); paint.setStrokeWidth(3); paint.setStyle(Paint.Style.STROKE);
            canvas.drawLine(cx - r * 0.4f, cy, cx + r * 0.45f, cy, paint); paint.setStyle(Paint.Style.FILL);
        } else {
            paint.setColor(0xffe86aa2);
            for (int p = 0; p < 5; p++) {
                double a = -Math.PI / 2 + p * Math.PI * 2 / 5;
                canvas.drawCircle(cx + (float) Math.cos(a) * r * 0.38f, cy + (float) Math.sin(a) * r * 0.38f, r * 0.32f, paint);
            }
            paint.setColor(0xffffd35a); canvas.drawCircle(cx, cy, r * 0.28f, paint);
        }
    }

    private void drawMemory(Canvas canvas) {
        for (int i = 0; i < 4; i++) {
            RectF b = boxes[i];
            paint.setColor(matched[i] || open[i] ? 0xfffff9f2 : 0xff795297);
            canvas.drawRoundRect(b, 18, 18, paint);
            if (matched[i] || open[i]) {
                drawSnack(canvas, cards[i] == 0 ? "aardbei" : "wortel", b.centerX(), b.centerY(), b.width() * 0.38f);
            } else {
                paint.setColor(Color.WHITE); paint.setTextAlign(Paint.Align.CENTER);
                paint.setTextSize(b.height() * 0.45f); paint.setTypeface(Typeface.DEFAULT_BOLD);
                canvas.drawText("?", b.centerX(), b.centerY() + b.height() * 0.15f, paint);
            }
        }
    }

    private void drawHide(Canvas canvas) {
        paint.setColor(0xffd9ead3); canvas.drawOval(0, getHeight() * 0.72f, getWidth(), getHeight() * 1.05f, paint);
        for (int i = 0; i < 3; i++) {
            RectF b = boxes[i];
            if (i == hideIndex) {
                paint.setColor(0xfff3c1b0);
                canvas.drawOval(b.centerX() - b.width() * 0.12f, b.top - b.height() * 0.08f, b.centerX() + b.width() * 0.16f, b.top + b.height() * 0.22f, paint);
            }
            paint.setColor(0xff3f8f55);
            canvas.drawOval(b.left, b.top + b.height() * 0.15f, b.right, b.bottom, paint);
            paint.setColor(0xff2f6b40);
            canvas.drawOval(b.left + b.width() * 0.12f, b.top, b.right - b.width() * 0.08f, b.centerY() + b.height() * 0.1f, paint);
        }
    }

    private int hit(float x, float y, int count) {
        for (int i = 0; i < count; i++) if (boxes[i].contains(x, y)) return i;
        return -1;
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP) return event.getAction() == MotionEvent.ACTION_DOWN;
        int index = hit(event.getX(), event.getY(), kind == 1 ? 4 : kind == 2 ? 3 : SLOTS);
        if (index < 0) return true;
        if (kind == 0) { if (!taken[index] && food.equals(slotKind[index])) takeFood(index); else if (!taken[index]) missFood(); }
        else if (kind == 1) flip(index);
        else pickBush(index);
        performClick();
        return true;
    }

    @Override public boolean performClick() { super.performClick(); return true; }
}
