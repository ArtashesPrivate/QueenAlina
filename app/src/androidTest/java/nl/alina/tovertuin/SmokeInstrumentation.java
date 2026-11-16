package nl.alina.tovertuin;

import android.app.*;
import android.content.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.util.regex.*;

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
    private void click(String text) {
        for(TextView v:labels())if(v instanceof Button && v.getText().toString().equals(text)) {v.performClick();return;}
        throw new AssertionError("Missing native button: "+text);
    }
    private void startMath() { click("Leren");click("Spelen"); }
    private void onMain(Runnable action) {
        final Throwable[] failure={null};
        super.runOnMainSync(()->{try{action.run();}catch(Throwable t){failure[0]=t;}});
        if(failure[0]!=null)throw new AssertionError("Native UI check failed",failure[0]);
    }
    private void playAt(int index) { int n=0;for(TextView v:labels())if(v instanceof Button && v.getText().toString().equals("Spelen")) {if(n++==index){v.performClick();return;}}throw new AssertionError("Missing game "+index); }
    private GameRules state() { return new GameStore(getTargetContext()).load(); }
    private void mathRound(boolean wrongFirst) {
        startMath();
        Pattern pattern=Pattern.compile("(\\d+) ([+−]) (\\d+) = \\?");
        for(int q=0;q<5;q++) {
            int answer=-1;
            for(TextView v:labels()) {Matcher m=pattern.matcher(v.getText());if(m.matches())answer=m.group(2).equals("+")?Integer.parseInt(m.group(1))+Integer.parseInt(m.group(3)):Integer.parseInt(m.group(1))-Integer.parseInt(m.group(3));}
            check(answer>=0,"Math question "+(q+1)+" renders");
            if(wrongFirst&&q==0) {
                for(TextView v:labels())if(v instanceof Button && v.getText().toString().matches("\\d+") && !v.getText().toString().equals(String.valueOf(answer))) {v.performClick();break;}
                check(has("Bijna! Kijk nog eens rustig. Je mag opnieuw kiezen."),"Wrong answer offers retry");
            }
            click(String.valueOf(answer));if(q<4)click("Volgende opdracht");
        }
        check(has("+5 sterren"),"Round completion displays reward");
    }
    @Override public void onStart() {
        Bundle result=new Bundle();
        try {
            Intent intent=new Intent(getTargetContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity=startActivitySync(intent);
            onMain(()-> {
                check(has("Hoi Alina!"),"Native Dutch home launches");
                int initial=state().stars, rounds=state().completedRounds;
                check(state().petCount()>=4,"Starter animals available");
                mathRound(true);check(state().stars==initial+5,"Round saves exactly five stars");
                for(int i=0;i<3;i++)mathRound(false);
                check(state().stars==initial+20,"Four real UI rounds earn twenty stars");
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
                click("Leren");playAt(1);check(has("Welk woord hoort bij dit dier?"),"Dutch word game renders");
                click("Thuis");click("Leren");playAt(2);check(has("Welke vorm komt hierna?"),"Pattern game renders");
                click("Thuis");activity.finish();
            });
            activity=startActivitySync(intent);
            onMain(()->{check(has("Hoi Alina!")&&state().owned[4]&&state().activePet==4,"Activity restart restores pet and progress");});
            result.putString("stream","PASS: "+passed+" native activity checks. Learning, purchase, care and restart passed.\n");
            finish(Activity.RESULT_OK,result);
        } catch(Throwable t) {
            result.putString("stream","FAIL: "+android.util.Log.getStackTraceString(t));finish(Activity.RESULT_CANCELED,result);
        }
    }
}
