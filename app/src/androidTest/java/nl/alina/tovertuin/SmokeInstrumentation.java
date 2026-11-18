package nl.alina.tovertuin;

import android.app.*;
import android.content.*;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.*;
import android.widget.*;
import java.util.*;

/** Native device regression runner without third-party test dependencies. */
public final class SmokeInstrumentation extends Instrumentation {
    private Activity activity;
    private int passed;
    private int purchaseStars;
    @Override public void onCreate(Bundle args) { super.onCreate(args); start(); }
    private void check(boolean ok,String msg) { if(!ok)throw new AssertionError(msg);passed++;Bundle b=new Bundle();b.putString("stream","PASS: "+msg+"\n");sendStatus(0,b); }
    private ArrayList<TextView> labels() {
        ArrayList<TextView> list=new ArrayList<>();
        collect(activity.findViewById(android.R.id.content),list);return list;
    }
    private void collect(View v,ArrayList<TextView> out) {
        if(v instanceof TextView)out.add((TextView)v);
        if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++)collect(((ViewGroup)v).getChildAt(i),out);
    }
    private boolean has(String text) { for(TextView v:labels())if(v.getText().toString().equals(text))return true;return false; }
    private boolean hasStarting(String prefix) { for(TextView v:labels())if(v.getText().toString().startsWith(prefix))return true;return false; }
    private void click(String text) {
        for(TextView v:labels())if(v instanceof Button && v.getText().toString().equals(text)) {v.performClick();return;}
        throw new AssertionError("Missing native button: "+text);
    }
    private void startFeed() { click("Leren");click("Spelen"); }
    private PlayStage board() { return ((MainActivity)activity).play; }
    private void onMain(Runnable action) {
        final Throwable[] failure={null};
        super.runOnMainSync(()->{try{action.run();}catch(Throwable t){failure[0]=t;}});
        if(failure[0]!=null)throw new AssertionError("Native UI check failed",failure[0]);
    }
    /** Polls the UI from the test thread so automatic transitions on the main thread can run. */
    private boolean waitUntil(String prefix,long timeoutMs) {
        long end=SystemClock.uptimeMillis()+timeoutMs;
        while(SystemClock.uptimeMillis()<end) {
            final boolean[] found={false};
            onMain(()->found[0]=hasStarting(prefix));
            if(found[0])return true;
            SystemClock.sleep(100);
        }
        return false;
    }
    private void playAt(int index) { int n=0;for(TextView v:labels())if(v instanceof Button && v.getText().toString().equals("Spelen")) {if(n++==index){v.performClick();return;}}throw new AssertionError("Missing game "+index); }
    private GameRules state() { return new GameStore(getTargetContext()).load(); }

    private void feedRound(boolean wrongFirst) {
        onMain(this::startFeed);
        for(int q=0;q<5;q++) {
            final int number=q+1;
            onMain(()-> {
                PlayStage board=board();
                check(board!=null && board.target()>=2,"Feed board "+number+" is on screen");
                String expected=LessonText.feedPrompt(state().names[state().activePet], board.target(), board.food());
                check(has(expected),"Spoken and shown prompt are the same sentence: "+expected);
                check(hasStarting("Opdracht "+number+" van 5"),"Question counter matches question "+number);
                check(board.getContentDescription()!=null && board.getContentDescription().toString().contains(board.food()),"Board names the food Alina must tap");
            });
            if(wrongFirst&&q==0) {
                onMain(()-> {
                    check(board().tapDistractor(),"A wrong snack can be tapped");
                    boolean encouraged=false;
                    for(String line:LessonText.WRONG_FEED)encouraged|=has(line);
                    check(encouraged,"Wrong snack shows an encouraging hint");
                });
                SystemClock.sleep(2600);
                onMain(()->check(hasStarting("Opdracht 1 van 5"),"Wrong snack does not advance automatically"));
            }
            onMain(()-> { PlayStage board=board(); while(board.given()<board.target()) check(board.tapNextFood(),"Tapping the right snack counts"); });
            if(q<4) check(waitUntil("Opdracht "+(q+2)+" van 5",12000),"Correct snacks advance automatically to question "+(q+2));
        }
        onMain(()->check(has("+5 sterren"),"Round completion displays reward"));
    }

    @Override public void onStart() {
        Bundle result=new Bundle();
        try {
            Intent intent=new Intent(getTargetContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity=startActivitySync(intent);
            onMain(()-> {
                check(has("Hoi Alina!"),"Native Dutch home launches");
                check(has("Queen Alina"),"Header shows the new app name");
            });
            onMain(()->check(state().petCount()>=4,"Starter animals available"));
            int initial=state().stars, rounds=state().completedRounds;
            feedRound(true);
            onMain(()->check(state().stars==initial+5,"Round saves exactly five stars"));
            for(int i=0;i<3;i++)feedRound(false);
            onMain(()->check(state().stars==initial+20,"Four real UI rounds earn twenty stars"));
            onMain(()-> {
                click("Winkel");check(has("De dierenwinkel"),"Animal shop opens");
                click("Welkom!");
            });
            // Android Dialog is a separate native window. Confirm through its real button.
            onMain(()-> {
                MainActivity a=(MainActivity)activity;
                check(a.purchaseDialog!=null&&a.purchaseDialog.isShowing(),"Purchase requires confirmation");
                purchaseStars=state().stars;
                a.purchaseDialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
            });
            // AlertDialog dispatches its click listener through the main message queue.
            onMain(()-> {
                GameRules s=state();
                check(s.owned[4]&&s.activePet==4&&s.stars==purchaseStars-20,"Purchase saves animal, exact price and selection");
                int hunger=s.needs[4][0],rest=s.needs[4][2];
                click("Eten geven");check(state().needs[4][0]>hunger,"Feeding restores the selected pet");
                click("Slapen");check(state().needs[4][2]>rest,"Sleep restores rest");
                click("Leren");playAt(1);
                check(has(LessonText.memoryPrompt()),"Memory game shows the tap instruction");
                int[] pair=board().openPair();
                board().tapCard(pair[0]); board().tapCard(pair[1]);
                check(board().matchedCount()==2,"A matching pair stays open");
                click("Thuis");click("Leren");playAt(2);
                check(has(LessonText.hidePrompt(state().names[state().activePet])),"Hide and seek shows where to look");
                int wrong=board().hidingSpot()==0?1:0;
                board().tapBush(wrong);
                check(hasStarting("Opdracht 1 van 5"),"Wrong bush does not advance");
                click("Thuis");activity.finish();
            });
            activity=startActivitySync(intent);
            onMain(()->{check(has("Hoi Alina!")&&state().owned[4]&&state().activePet==4,"Activity restart restores pet and progress");});
            result.putString("stream","PASS: "+passed+" native activity checks. Learning, automatic advance, purchase, care and restart passed.\n");
            finish(Activity.RESULT_OK,result);
        } catch(Throwable t) {
            result.putString("stream","FAIL: "+android.util.Log.getStackTraceString(t));finish(Activity.RESULT_CANCELED,result);
        }
    }
}
