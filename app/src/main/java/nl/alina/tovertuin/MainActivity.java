package nl.alina.tovertuin;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.speech.tts.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    private static final int CREAM=0xfffff9f2, PLUM=0xff49334f, PURPLE=0xff795297,
        LILAC=0xffeee1f2, PEACH=0xfffae8df, GREEN=0xffe4edda, MUTED=0xff735d73;
    /** Pause after a correct answer so the reaction and animation are visible before the next question. */
    private static final long MIN_REACTION_MS=1600L;
    /** Safety net when speech is unavailable or does not report completion. */
    private static final long SPEECH_FALLBACK_MS=5000L, SILENT_FALLBACK_MS=2800L;
    private static final String ADVANCE_ID="advance", PROMPT_ID="prompt";
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final Random random=new Random();
    private GameStore store;
    private GameRules game;
    private LinearLayout shell, body, header, navigation;
    private ScrollView scroll;
    private TextView starsLabel, bubble, feedback;
    private CharacterView stage, reactionPet;
    private TextToSpeech tts;
    private boolean voiceReady, voiceChecked, activityActive, answerLocked;
    private String screen="Thuis", gameType="", lastSpeech="", feedbackText="";
    private GameRules.Round round;
    private Item current;
    PlayStage play;
    AlertDialog purchaseDialog;
    private int pendingParentAnswer, lastCorrect=-1, lastWrong=-1;
    private boolean advancePending;
    private long advanceReadyAt;
    private final Runnable careClock=new Runnable() {
        @Override public void run() { if(activityActive) { game.tick(); store.save(game); handler.postDelayed(this,90000); } }
    };
    private final Runnable advanceTask=this::advanceToNextQuestion;

    /** One question, generated once. Text on screen, pictures and the spoken prompt all come from this object. */
    private static final class Item {
        String type, spoken, sum="", food="aardbei";
        int amount, targetPet, pattern, value, seed;
        boolean answered;
        final ArrayList<String> choices=new ArrayList<>();
        int correct;
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        store=new GameStore(this); game=store.load();
        if(state!=null) {
            screen=state.getString("screen","Thuis"); gameType=state.getString("type","");
            if(screen.equals("Spel")) {
                int solved=state.getInt("solved",0);
                if(solved>=5) { screen="Leren"; gameType=""; }
                else { round=new GameRules.Round(); round.solved=Math.max(0,solved); }
            }
        }
        tts=new TextToSpeech(this,status -> {
            voiceChecked=true;
            if(status==TextToSpeech.SUCCESS) {
                Locale dutch=Locale.forLanguageTag("nl-NL");
                int available=tts.setLanguage(dutch);
                if(available>=TextToSpeech.LANG_AVAILABLE) {
                    Set<Voice> voices=tts.getVoices();
                    if(voices!=null)for(Voice v:voices) {
                        if(v.getLocale().getLanguage().equals("nl") && !v.isNetworkConnectionRequired()) {
                            tts.setVoice(v); voiceReady=true; break;
                        }
                    }
                    tts.setSpeechRate(.84f); tts.setPitch(1.12f);
                }
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override public void onStart(String id) { runOnUiThread(() -> { if(stage!=null)stage.setSpeaking(true); }); }
                    @Override public void onDone(String id) { runOnUiThread(() -> { if(stage!=null)stage.setSpeaking(false); if(id!=null&&id.startsWith(ADVANCE_ID))onReactionSpoken(); }); }
                    @Override public void onError(String id) { runOnUiThread(() -> { voiceReady=false; if(stage!=null)stage.setSpeaking(false); }); }
                });
            }
            runOnUiThread(() -> { if(screen.equals("Thuis"))render(); });
        });
        if(Build.VERSION.SDK_INT>=33) getOnBackInvokedDispatcher().registerOnBackInvokedCallback(0,this::goBack);
        render();
    }
    @Override protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out); out.putString("screen",screen); out.putString("type",gameType);
        if(round!=null)out.putInt("solved",round.solved);
    }
    @Override protected void onResume() {
        super.onResume(); activityActive=true; handler.removeCallbacks(careClock); handler.postDelayed(careClock,90000);
        // Resume an interrupted transition exactly once; the question itself is not skipped.
        if(advancePending) { handler.removeCallbacks(advanceTask); handler.postDelayed(advanceTask,1200); }
    }
    @Override public void onConfigurationChanged(android.content.res.Configuration c) { super.onConfigurationChanged(c); render(); }
    @Override protected void onPause() {
        activityActive=false; handler.removeCallbacks(careClock); handler.removeCallbacks(advanceTask);
        store.save(game); if(tts!=null)tts.stop(); super.onPause();
    }
    @Override protected void onDestroy() { handler.removeCallbacksAndMessages(null); if(tts!=null) { tts.stop(); tts.shutdown(); } super.onDestroy(); }
    // API 33+ uses the OnBackInvokedDispatcher registered in onCreate; older phones use this fallback.
    @android.annotation.SuppressLint("GestureBackNavigation")
    @Override public void onBackPressed() { goBack(); }
    private void goBack() {
        if(screen.equals("Spel") && round!=null && round.solved>0 && round.solved<5) {
            new AlertDialog.Builder(this).setTitle("Even stoppen?").setMessage("Je sterren blijven bewaard. Dit rondje kun je later opnieuw spelen.")
                .setNegativeButton("Verder spelen",null).setPositiveButton("Naar huis",(d,w)->navigate("Thuis")).show();
        } else if(screen.equals("Thuis"))finish(); else navigate("Thuis");
    }
    private int dp(float n) { return (int)(n*getResources().getDisplayMetrics().density+.5f); }
    private GradientDrawable bg(int color,int radius) { GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d; }
    private LinearLayout column() { LinearLayout v=new LinearLayout(this); v.setOrientation(LinearLayout.VERTICAL); return v; }
    private LinearLayout row() { LinearLayout v=new LinearLayout(this); v.setOrientation(LinearLayout.HORIZONTAL); v.setGravity(Gravity.CENTER_VERTICAL); return v; }
    private TextView text(String value,int size,int color,boolean bold) {
        TextView v=new TextView(this); v.setText(value); v.setTextSize(size); v.setTextColor(color);
        v.setFontFeatureSettings("kern"); v.setIncludeFontPadding(false);
        v.setTypeface(android.graphics.Typeface.create("sans-serif-rounded",bold?Typeface.BOLD:Typeface.NORMAL));
        v.setLineSpacing(dp(3),1); return v;
    }
    private void gap(LinearLayout parent,int h) { View v=new View(this); parent.addView(v,new LinearLayout.LayoutParams(1,dp(h))); }
    private Button button(String label,boolean primary,Runnable action) {
        Button b=new Button(this); b.setText(label); b.setTextSize(16); b.setAllCaps(false);
        b.setTypeface(android.graphics.Typeface.create("sans-serif-rounded",Typeface.BOLD));
        b.setTextColor(primary?Color.WHITE:PLUM); b.setBackground(bg(primary?PURPLE:LILAC,16));
        b.setMinHeight(dp(54)); b.setMinimumHeight(dp(54)); b.setMinWidth(0); b.setMinimumWidth(0);
        b.setPadding(dp(12),dp(10),dp(12),dp(10)); b.setStateListAnimator(null);
        b.setOnClickListener(v->action.run()); return b;
    }
    private void addButton(String label,boolean primary,Runnable r) {
        Button b=button(label,primary,r); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.topMargin=dp(10); body.addView(b,p);
    }
    private void title(String title,String subtitle) {
        body.addView(text(title,30,PLUM,true)); gap(body,8); body.addView(text(subtitle,16,MUTED,false)); gap(body,20);
    }
    private void navigate(String dest) {
        cancelAdvance();
        if(tts!=null)tts.stop(); if(stage!=null)stage.setSpeaking(false);
        screen=dest; if(!dest.equals("Spel")) { round=null; gameType=""; } store.save(game); render();
    }
    private void cancelAdvance() {
        advancePending=false; handler.removeCallbacks(advanceTask); current=null; feedbackText="";
    }
    private void render() {
        stage=null; bubble=null; reactionPet=null; play=null;
        shell=column(); shell.setBackgroundColor(CREAM);
        shell.setOnApplyWindowInsetsListener((v,insets)-> {
            if(Build.VERSION.SDK_INT>=30) {
                android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());
                shell.setPadding(bars.left,bars.top,bars.right,bars.bottom);
            } else shell.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());
            return insets;
        });
        setContentView(shell); shell.requestApplyInsets();
        header=row(); header.setPadding(dp(20),dp(12),dp(16),dp(12));
        TextView brand=text("Queen Alina",21,PLUM,true);
        header.addView(brand,new LinearLayout.LayoutParams(0,-2,1));
        starsLabel=text("★ "+game.stars,18,PLUM,true); starsLabel.setBackground(bg(0xffffe7ab,18)); starsLabel.setPadding(dp(14),dp(12),dp(14),dp(12));
        starsLabel.setContentDescription(game.stars+" sterren"); header.addView(starsLabel);
        shell.addView(header);
        scroll=new ScrollView(this); scroll.setFillViewport(true); scroll.setClipToPadding(false);
        LinearLayout wrap=column(); wrap.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL);
        body=column(); body.setPadding(dp(20),dp(12),dp(20),dp(24));
        int width=Math.min(getResources().getDisplayMetrics().widthPixels,dp(680));
        wrap.addView(body,new LinearLayout.LayoutParams(width,-2)); scroll.addView(wrap);
        shell.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        if(screen.equals("Thuis"))home();
        else if(screen.equals("Leren"))learn();
        else if(screen.equals("Dieren"))pets();
        else if(screen.equals("Winkel"))shop();
        else if(screen.equals("Spel"))question();
        else if(screen.equals("Ouders"))parents();
        navigation=row(); navigation.setPadding(dp(10),dp(8),dp(10),dp(10)); navigation.setBackgroundColor(0xfff3e9f3);
        for(String tab:new String[]{"Thuis","Leren","Dieren","Winkel"}) {
            Button b=button(tab,screen.equals(tab)||screen.equals("Spel")&&tab.equals("Leren"),()-> {
                if(screen.equals("Spel")&&round!=null&&round.solved>0&&round.solved<5) {
                    new AlertDialog.Builder(this).setTitle("Dit rondje stoppen?").setMessage("Je verzamelde sterren blijven bewaard.")
                        .setNegativeButton("Verder spelen",null).setPositiveButton("Stoppen",(d,w)->navigate(tab)).show();
                } else navigate(tab);
            });
            b.setTextSize(14); b.setPadding(dp(3),dp(12),dp(3),dp(12));
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1); p.setMargins(dp(3),0,dp(3),0); navigation.addView(b,p);
        }
        shell.addView(navigation);
    }
    private void home() {
        title("Hoi Alina!","Wat gaan we vandaag samen ontdekken?");
        stage=new CharacterView(this,game.activePet); body.addView(stage,new LinearLayout.LayoutParams(-1,dp(245)));
        stage.setOnClickListener(v->say(game.names[game.activePet]+" vindt het fijn om met jou te spelen!"));
        gap(body,12); bubble=text("Hoi! Ik ben "+game.names[game.activePet]+". Zullen we samen leren?",18,PLUM,true); body.addView(bubble);
        addButton("Samen leren  ·  +5 sterren",true,()->navigate("Leren"));
        gap(body,22); body.addView(text("Zorg voor "+game.names[game.activePet],21,PLUM,true)); gap(body,12);
        needs(); careButtons();
        gap(body,20);
        body.addView(text("Samen gegroeid · niveau "+game.level(),18,PLUM,true)); gap(body,6);
        body.addView(text(game.completedRounds+" leerrondjes klaar · "+game.petCount()+" dierenvriendjes",15,MUTED,false));
        if(voiceChecked&&!voiceReady) {
            gap(body,14); body.addView(text("Voorlezen is nog niet beschikbaar. Een ouder kan de Nederlandse stem instellen bij Ouders.",14,MUTED,false));
        }
        addButton("Ouders",false,this::parentGate);
    }
    private void needs() {
        LinearLayout r=row(); String[] names={"Buikje","Verzorging","Rust"};
        for(int i=0;i<3;i++) {
            LinearLayout c=column(); c.setPadding(dp(5),0,dp(5),0); c.addView(text(names[i],14,MUTED,false)); gap(c,7);
            ProgressBar p=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal); p.setMax(100); p.setProgress(game.needs[game.activePet][i]);
            p.setProgressTintList(android.content.res.ColorStateList.valueOf(PURPLE));
            p.setContentDescription(names[i]+": "+game.needs[game.activePet][i]+" van 100"); c.addView(p,new LinearLayout.LayoutParams(-1,dp(10)));
            r.addView(c,new LinearLayout.LayoutParams(0,-2,1));
        }
        body.addView(r); gap(body,10);
    }
    private void careButtons() {
        String[] labels={"Eten geven","Borstelen","Spelen","Slapen"};
        for(int r=0;r<2;r++) {
            LinearLayout line=row();
            for(int col=0;col<2;col++) { final int kind=r*2+col;
                Button b=button(labels[kind],false,()->care(kind));
                LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1); p.setMargins(dp(col==0?0:5),dp(5),dp(col==0?5:0),dp(5)); line.addView(b,p);
            }
            body.addView(line);
        }
    }
    private void care(int kind) {
        game.care(kind); store.save(game); render();
        String name=game.names[game.activePet];
        String[] lines={"Mmm, dank je wel Alina! Mijn buikje is weer vol.","Wat is mijn vacht weer mooi. Dank je wel!","Hoera! Samen spelen is fijn.","Welterusten, "+name+". Slaap maar lekker."};
        if(stage!=null) { stage.act(new String[]{"feed","brush","play","sleep"}[kind]); }
        if(kind==3) { if(bubble!=null)bubble.setText(lines[kind]); } else say(lines[kind]);
    }
    private void learn() {
        title("Samen leren","Kies een spel. Vijf opdrachten = vijf sterren!");
        learningOption("Hapjes tellen","Tik zelf de hapjes die je vriendje vraagt.","feed",0,PEACH);
        learningOption("Memory","Draai kaartjes om en zoek twee dezelfde.","memory",1,LILAC);
        learningOption("Verstoppertje","Zoek je vriendje achter het struikje.","hide",2,GREEN);
        gap(body,18); body.addView(text("Neem je tijd. Je mag altijd opnieuw proberen.",16,MUTED,false));
    }
    private void learningOption(String name,String desc,String type,int symbol,int color) {
        LinearLayout panel=column(); panel.setPadding(dp(18),dp(18),dp(18),dp(18)); panel.setBackground(bg(color,20));
        LinearLayout top=row(); top.addView(new Symbols(this,symbol,0),new LinearLayout.LayoutParams(dp(58),dp(58)));
        LinearLayout words=column(); words.setPadding(dp(14),0,0,0); words.addView(text(name,21,PLUM,true)); gap(words,6); words.addView(text(desc,16,MUTED,false));
        top.addView(words,new LinearLayout.LayoutParams(0,-2,1)); panel.addView(top); gap(panel,14);
        panel.addView(button("Spelen",true,()->startRound(type))); body.addView(panel); gap(body,14);
    }
    private void startRound(String type) {
        gameType=type; round=new GameRules.Round(); current=null; feedbackText=""; cancelAdvance(); screen="Spel";
        render(); speakPrompt();
    }

    // ---- Question flow: generate once, display, speak, answer, reward, then advance automatically ----

    private Item newItem() {
        Item item=new Item(); item.type=gameType; item.seed=random.nextInt(); item.correct=0;
        String name=game.names[game.activePet];
        if(gameType.equals("memory")) item.spoken=LessonText.memoryPrompt();
        else if(gameType.equals("hide")) item.spoken=LessonText.hidePrompt(name);
        else {
            gameType="feed"; item.type="feed";
            String[] foods={"aardbei","wortel","botje"};
            item.food=foods[random.nextInt(foods.length)];
            int max=Math.max(2, Math.min(5, game.maxNumber));
            item.value=2+random.nextInt(max-1);
            item.spoken=LessonText.feedPrompt(name, item.value, item.food);
        }
        return item;
    }

    private void question() {
        if(round==null) { round=new GameRules.Round(); gameType="feed"; }
        if(round.solved>=5) { finished(); return; }
        if(current==null||!current.type.equals(gameType)) current=newItem();
        answerLocked=current.answered;
        String name=gameType.equals("memory")?"Memory":gameType.equals("hide")?"Verstoppertje":"Hapjes tellen";
        int shown=round.solved+(current.answered?0:1);
        title(name,"Opdracht "+shown+" van 5 · tik op het speelveld");
        ProgressBar progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal); progress.setMax(5); progress.setProgress(round.solved);
        progress.setProgressTintList(android.content.res.ColorStateList.valueOf(PURPLE)); body.addView(progress,new LinearLayout.LayoutParams(-1,dp(8))); gap(body,12);
        TextView q=text(current.spoken,22,PLUM,true); q.setGravity(Gravity.CENTER); body.addView(q); gap(body,10);
        LinearLayout arena=row();
        reactionPet=new CharacterView(this,game.activePet).thumbnail();
        arena.addView(reactionPet,new LinearLayout.LayoutParams(dp(108),dp(200)));
        int boardKind=gameType.equals("memory")?1:gameType.equals("hide")?2:0;
        play=new PlayStage(this,boardKind,current.value,current.food,current.seed,new PlayStage.Listener() {
            @Override public void solved() { answer(0); }
            @Override public void mistake(String hint) {
                feedbackText=hint; if(feedback!=null)feedback.setText(hint); say(hint);
            }
        });
        arena.addView(play,new LinearLayout.LayoutParams(0,dp(230),1));
        body.addView(arena);
        feedback=text(feedbackText,18,PLUM,true); feedback.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE); gap(body,8); body.addView(feedback);
        addButton("Luister naar de opdracht",false,this::speakPrompt);
    }

    /** Speaks the same sentence that is shown on screen for the current question. */
    private void speakPrompt() {
        if(current!=null) say(current.spoken,PROMPT_ID+"-"+SystemClock.uptimeMillis());
    }

    private void answer(int index) {
        if(answerLocked || round==null || current==null)return;
        if(index!=current.correct) {
            String[] pool=gameType.equals("memory")?LessonText.WRONG_MEMORY:gameType.equals("hide")?LessonText.WRONG_HIDE:LessonText.WRONG_FEED;
            lastWrong=LessonText.pick(random,pool.length,lastWrong);
            feedbackText=pool[lastWrong];
            if(feedback!=null)feedback.setText(feedbackText);
            say(feedbackText);
            return;
        }
        answerLocked=true; current.answered=true; round.answer(true);
        if(round.solved==5) {
            round.claim(game); store.save(game); feedbackText=""; render();
            say(LessonText.ROUND_DONE[random.nextInt(LessonText.ROUND_DONE.length)]);
            return;
        }
        lastCorrect=LessonText.pick(random,LessonText.CORRECT.length,lastCorrect);
        String line=LessonText.CORRECT[lastCorrect];
        feedbackText=line+"\nNog "+(5-round.solved)+" te gaan.";
        if(feedback!=null)feedback.setText(feedbackText);
        if(reactionPet!=null)reactionPet.act("play");
        boolean spoken=game.sound&&voiceReady&&tts!=null;
        advancePending=true; advanceReadyAt=SystemClock.uptimeMillis()+MIN_REACTION_MS;
        handler.removeCallbacks(advanceTask);
        handler.postDelayed(advanceTask,spoken?SPEECH_FALLBACK_MS:SILENT_FALLBACK_MS);
        say(line,ADVANCE_ID+"-"+SystemClock.uptimeMillis());
    }

    /** Called when the reaction has been spoken. Waits for the minimum animation time, then advances. */
    private void onReactionSpoken() {
        if(!advancePending)return;
        long wait=Math.max(0,advanceReadyAt-SystemClock.uptimeMillis());
        handler.removeCallbacks(advanceTask);
        handler.postDelayed(advanceTask,wait);
    }

    private void advanceToNextQuestion() {
        if(!advancePending)return;
        advancePending=false;
        if(!screen.equals("Spel")||round==null||round.solved>=5)return;
        current=null; feedbackText=""; render(); speakPrompt();
    }

    private void finished() {
        title("Hoera, Alina!","Jij en je dierenvriendje hebben samen geleerd.");
        stage=new CharacterView(this,game.activePet); body.addView(stage,new LinearLayout.LayoutParams(-1,dp(240))); stage.act("play");
        TextView reward=text("+5 sterren",36,PURPLE,true); reward.setGravity(Gravity.CENTER); gap(body,16); body.addView(reward);
        gap(body,10); body.addView(text("Je hebt nu "+game.stars+" sterren. Spaar voor een nieuw vriendje!",18,PLUM,false));
        addButton("Naar de dierenwinkel",true,()->navigate("Winkel"));
        addButton("Nog een spelletje",false,()->navigate("Leren"));
        addButton("Naar mijn vriendje",false,()->navigate("Thuis"));
    }
    private void pets() {
        title("Mijn dierenvriendjes",game.petCount()+" vriendjes om voor te zorgen. Kies wie bij je komt!");
        for(int i=0;i<game.owned.length;i++)if(game.owned[i]) {
            final int pet=i; LinearLayout line=row();
            line.setPadding(dp(8),dp(10),dp(12),dp(10)); line.setBackground(bg(i==game.activePet?LILAC:0xfffff0e5,18));
            line.addView(new CharacterView(this,i).thumbnail(),new LinearLayout.LayoutParams(dp(100),dp(105)));
            LinearLayout details=column(); details.addView(text(game.names[i],21,PLUM,true)); gap(details,5); details.addView(text(GameRules.LABELS[i],15,MUTED,false));
            gap(details,8); details.addView(button(i==game.activePet?"Bij mij":"Kies "+game.names[i],i==game.activePet,()-> {
                game.select(pet); store.save(game); navigate("Thuis"); say("Hoi Alina! Ik ben "+game.names[pet]+". Wat gaan we doen?");
            }));
            line.addView(details,new LinearLayout.LayoutParams(0,-2,1)); body.addView(line); gap(body,12);
        }
        addButton("Nieuwe vriendjes ontdekken",true,()->navigate("Winkel"));
        addButton("Geef je vriendje een naam",false,this::renamePet);
    }
    private void renamePet() {
        EditText input=new EditText(this); input.setSingleLine(true); input.setText(game.names[game.activePet]);
        input.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(18)});
        input.setPadding(dp(24),dp(16),dp(24),dp(16));
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Hoe heet je vriendje?").setView(input)
            .setNegativeButton("Terug",null).setPositiveButton("Opslaan",null).create();
        dialog.setOnShowListener(d->dialog.getButton(-1).setOnClickListener(v->{
            String name=input.getText().toString().trim(); if(name.isEmpty()) { input.setError("Kies een naam"); return; }
            game.names[game.activePet]=name; store.save(game); dialog.dismiss(); render();
        })); dialog.show();
    }
    private void shop() {
        title("De dierenwinkel","Nieuwe vriendjes koop je met verdiende sterren.");
        body.addView(text("Je hebt "+game.stars+" sterren",19,PLUM,true)); gap(body,16);
        for(int i=4;i<10;i+=2) {
            LinearLayout line=new LinearLayout(this); line.setOrientation(LinearLayout.HORIZONTAL); line.setBaselineAligned(false);
            for(int j=i;j<i+2;j++) {
                final int pet=j; LinearLayout tile=column(); tile.setPadding(dp(12),dp(14),dp(12),dp(14)); tile.setBackground(bg(j%2==0?PEACH:LILAC,20));
                tile.addView(new CharacterView(this,j).thumbnail(),new LinearLayout.LayoutParams(-1,dp(122)));
                TextView name=text(GameRules.LABELS[j],19,PLUM,true); name.setGravity(Gravity.CENTER); gap(tile,8);tile.addView(name);
                TextView cost=text(game.owned[j]?"Woont al bij jou":"★ "+GameRules.PRICES[j],16,MUTED,false); cost.setGravity(Gravity.CENTER);gap(tile,6);tile.addView(cost);gap(tile,12);
                boolean canBuy=game.stars>=GameRules.PRICES[j];
                Button b=button(game.owned[j]?"Kies":canBuy?"Welkom!":"Nog "+(GameRules.PRICES[j]-game.stars)+" sterren",canBuy&&!game.owned[j],()-> {
                    if(game.owned[pet]) { game.select(pet);navigate("Thuis");return; }
                    if(game.stars<GameRules.PRICES[pet]) { shopPrompt(pet);return; }
                    purchaseDialog=new AlertDialog.Builder(this).setTitle("Een nieuw vriendje!").setMessage("Wil je de "+GameRules.LABELS[pet].toLowerCase(Locale.forLanguageTag("nl-NL"))+" verwelkomen voor "+GameRules.PRICES[pet]+" sterren?")
                        .setNegativeButton("Nog even sparen",null).setPositiveButton("Ja, welkom!",(d,w)-> {
                            if(game.buy(pet)) {
                                store.save(game); navigate("Thuis");
                                say("Welkom "+game.names[pet]+"! "+LessonText.NEW_FRIEND[random.nextInt(LessonText.NEW_FRIEND.length)]);
                            }
                        }).create();
                    purchaseDialog.show();
                }); b.setTextSize(14);tile.addView(b);
                LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-1,1); p.setMargins(j==i?0:dp(6),0,j==i?dp(6):0,0);line.addView(tile,p);
            }
            body.addView(line,new LinearLayout.LayoutParams(-1,-2)); gap(body,14);
        }
        addButton("Sterren verdienen",true,()->navigate("Leren"));
        gap(body,12); body.addView(text("Alle sterren verdien je door te leren. Er wordt geen echt geld gebruikt.",14,MUTED,false));
    }
    private void shopPrompt(int pet) {
        new AlertDialog.Builder(this).setTitle("Nog even sparen")
            .setMessage("Voor de "+GameRules.LABELS[pet].toLowerCase(Locale.forLanguageTag("nl-NL"))+" heb je nog "+(GameRules.PRICES[pet]-game.stars)+" sterren nodig. Met een leerrondje verdien je er vijf.")
            .setNegativeButton("Terug",null).setPositiveButton("Samen leren",(d,w)->navigate("Leren")).show();
    }
    private void parentGate() {
        pendingParentAnswer=14+random.nextInt(10); int b=7+random.nextInt(8);
        EditText input=new EditText(this); input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER); input.setPadding(dp(24),dp(16),dp(24),dp(16));
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Voor ouders").setMessage("Hoeveel is "+(pendingParentAnswer-b)+" + "+b+"?").setView(input)
            .setNegativeButton("Terug",null).setPositiveButton("Verder",null).create();
        dialog.setOnShowListener(d->dialog.getButton(-1).setOnClickListener(v->{
            if(input.getText().toString().equals(String.valueOf(pendingParentAnswer))) {dialog.dismiss();navigate("Ouders");}
            else input.setError("Probeer het nog eens");
        }));dialog.show();
    }
    private void parents() {
        title("Voor ouders","Een eerste speelbare versie, speciaal voor Alina.");
        body.addView(text("Rekenniveau",21,PLUM,true)); gap(body,10);
        for(int max:new int[]{10,20})addButton("Tellen tot "+max+(game.maxNumber==max?" · gekozen":""),game.maxNumber==max,()->{game.maxNumber=max;store.save(game);render();});
        gap(body,24);body.addView(text("Nederlandse stem",21,PLUM,true));gap(body,10);
        body.addView(text(voiceReady?"Een Nederlandse stem is beschikbaar.":"Er is nog geen bruikbare offline Nederlandse stem. Installeer Nederlandse spraakgegevens via de steminstellingen van Android.",16,MUTED,false));
        addButton(game.sound?"Geluid staat aan":"Geluid staat uit",game.sound,()->{game.sound=!game.sound;store.save(game);render();});
        addButton("Voorbeeld beluisteren",false,()->say("Hoi Alina! Zullen we samen leren en voor de dieren zorgen?"));
        addButton("Steminstellingen openen",false,()-> {
            try {startActivity(new Intent("com.android.settings.TTS_SETTINGS"));}
            catch(ActivityNotFoundException e) {new AlertDialog.Builder(this).setMessage("Open Android-instellingen en zoek naar Tekst-naar-spraak.").setPositiveButton("Goed",null).show();}
        });
        gap(body,24);body.addView(text("Voortgang",21,PLUM,true));gap(body,10);
        body.addView(text(game.completedRounds+" afgeronde leerrondjes\n"+game.stars+" sterren beschikbaar\n"+game.petCount()+" dierenvriendjes",17,PLUM,false));
        gap(body,20);body.addView(text("De spellen zijn hapjes aantikken, memory en verstoppertje. Een fout tik kost geen sterren.\n\nAlles blijft op dit apparaat. Geen advertenties, accounts, microfoon of aankopen met echt geld. De dieren worden niet ziek wanneer Alina niet speelt.",15,MUTED,false));
        addButton("Terug naar Alina",true,()->navigate("Thuis"));
    }
    private void say(String line) { say(line,"line-"+SystemClock.uptimeMillis()); }
    private void say(String line,String utteranceId) {
        lastSpeech=line; if(bubble!=null)bubble.setText(line);
        if(game.sound&&voiceReady&&tts!=null)tts.speak(line,TextToSpeech.QUEUE_FLUSH,null,utteranceId);
        else if(feedback!=null && screen.equals("Ouders"))feedback.setText(line);
    }

    /** Real counting/pattern geometry, drawn natively; not text emoji or web graphics. */
    private static final class Symbols extends View {
        private final Paint p=new Paint(3); private final int kind,value;
        private final int[] colors={0xff9c6297,0xffb27c24,0xff447a68};
        Symbols(Context c,int kind,int value){super(c);this.kind=kind;this.value=value;}
        private void shape(Canvas c,int type,float x,float y,float r) {
            p.setColor(colors[type%3]);
            if(type==0)c.drawCircle(x,y,r,p);
            else if(type==2)c.drawRoundRect(x-r,y-r,x+r,y+r,r*.2f,r*.2f,p);
            else {Path path=new Path();for(int i=0;i<10;i++){double a=-Math.PI/2+i*Math.PI/5;float rad=i%2==0?r:r*.46f;float px=x+(float)Math.cos(a)*rad,py=y+(float)Math.sin(a)*rad;if(i==0)path.moveTo(px,py);else path.lineTo(px,py);}path.close();c.drawPath(path,p);}
        }
        @Override protected void onDraw(Canvas c) {
            float w=getWidth(),h=getHeight();
            if(kind==3) {for(int i=0;i<value;i++){int cols=10;float step=w/cols;shape(c,1,step*(i%cols+.5f),h*(i<10?.29f:.74f),Math.min(step*.32f,h*.16f));}}
            else if(kind==4) {int a=value/3,b=value%3;for(int i=0;i<4;i++)shape(c,i%2==0?a:b,w*(i+.5f)/5,h*.5f,Math.min(w/14,h*.2f));p.setColor(0xff49334f);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(h*.38f);p.setTypeface(Typeface.DEFAULT_BOLD);c.drawText("?",w*.9f,h*.63f,p);}
            else if(kind==5)shape(c,value,w/2,h/2,Math.min(w,h)*.28f);
            else if(kind==0) {shape(c,1,w*.32f,h*.35f,w*.2f);shape(c,1,w*.68f,h*.65f,w*.2f);}
            else if(kind==1) {p.setColor(0xff795297);p.setTextSize(h*.7f);p.setTypeface(Typeface.DEFAULT_BOLD);c.drawText("Aa",0,h*.8f,p);}
            else {shape(c,0,w*.2f,h*.5f,w*.13f);shape(c,1,w*.5f,h*.5f,w*.13f);shape(c,2,w*.8f,h*.5f,w*.13f);}
        }
    }
}
