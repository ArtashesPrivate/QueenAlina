package nl.alina.tovertuin;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.*;
import android.os.SystemClock;
import android.view.*;
import java.io.IOException;

/** Native animated sprite scene. No browser, game server, or network dependency. */
public final class CharacterView extends View {
    private static Bitmap friends, extras, extraActions;
    private static final Rect[][] frames = new Rect[4][5];
    private static final Rect[] extraFrames = new Rect[6];
    private static final Rect[][] extraActionFrames = new Rect[3][6];
    private final Paint paint = new Paint(3);
    private final RectF box = new RectF();
    private int pet;
    private boolean showAlina = true, scene = true;
    private String action = "idle";
    private long actionUntil;
    private boolean speaking;
    private float startedX;

    public CharacterView(Context context) { this(context,0); }

    public CharacterView(Context context, int pet) {
        super(context); this.pet=pet; load(context);
        setContentDescription("Alina met haar " + GameRules.LABELS[pet].toLowerCase(java.util.Locale.forLanguageTag("nl-NL")));
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
    }
    public CharacterView thumbnail() { showAlina=false; scene=false; return this; }
    public void setPet(int p) { pet=p; invalidate(); }
    public void setSpeaking(boolean s) { speaking=s; invalidate(); }
    public void act(String a) { action=a; actionUntil=SystemClock.uptimeMillis()+4200; invalidate(); }

    private static void load(Context c) {
        if(friends!=null)return;
        try {
            friends=BitmapFactory.decodeStream(c.getAssets().open("friends.png"));
            extras=BitmapFactory.decodeStream(c.getAssets().open("extras.png"));
            extraActions=BitmapFactory.decodeStream(c.getAssets().open("extra-actions.png"));
            for(int r=0;r<4;r++)for(int col=0;col<5;col++)frames[r][col]=bounds(friends,col,r,5,4);
            for(int i=0;i<6;i++)extraFrames[i]=bounds(extras,i%3,i/3,3,2);
            for(int r=0;r<3;r++)for(int i=0;i<6;i++)extraActionFrames[r][i]=bounds(extraActions,i,r,6,3);
        } catch(IOException e) { throw new IllegalStateException("De dierenvriendjes ontbreken",e); }
    }
    private static Rect bounds(Bitmap b,int col,int row,int cols,int rows) {
        int l=col*b.getWidth()/cols, t=row*b.getHeight()/rows;
        int right=(col+1)*b.getWidth()/cols, bottom=(row+1)*b.getHeight()/rows;
        int minX=right,minY=bottom,maxX=l,maxY=t;
        int stride=right-l;
        int[] pixels=new int[stride*(bottom-t)];
        b.getPixels(pixels,0,stride,l,t,stride,bottom-t);
        for(int y=t;y<bottom;y++)for(int x=l;x<right;x++) {
            if(Color.alpha(pixels[(y-t)*stride+x-l])>110) { minX=Math.min(x,minX); maxX=Math.max(x,maxX); minY=Math.min(y,minY); maxY=Math.max(y,maxY); }
        }
        return minX<maxX?new Rect(minX,minY,maxX+1,maxY+1):new Rect(l,t,right,bottom);
    }
    private void sprite(Canvas c, Bitmap b, Rect src, float x,float base,float width,float height,float angle) {
        float s=Math.min(width/src.width(),height/src.height());
        float w=src.width()*s,h=src.height()*s;
        box.set(x-w/2,base-h,x+w/2,base);
        c.save(); c.rotate(angle,x,base-h*.45f); c.drawBitmap(b,src,box,paint); c.restore();
    }
    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        float w=getWidth(),h=getHeight(),t=SystemClock.uptimeMillis()/1000f;
        boolean motion=ValueAnimator.areAnimatorsEnabled() && scene;
        String a=SystemClock.uptimeMillis()<actionUntil?action:"idle";
        boolean sleeping=a.equals("sleep"), walking=a.equals("play"), eating=a.equals("feed");
        boolean talking=speaking || eating;
        float bounce=motion && !sleeping?(float)Math.sin(t*(walking?10:2.4))*(walking?7:2):0;
        if(scene) {
            paint.setColor(Color.rgb(251,235,224)); c.drawRoundRect(0,0,w,h,32,32,paint);
            paint.setColor(Color.rgb(230,235,208)); c.drawOval(-w*.12f,h*.65f,w*1.12f,h*1.38f,paint);
            paint.setColor(Color.rgb(212,221,189)); c.drawOval(w*.1f,h*.86f,w*.9f,h*.98f,paint);
        }
        int row=sleeping?3:talking?((int)(t*5)%2==0?2:0):walking?((int)(t*5)%2==0?1:0):0;
        if(!motion && !sleeping && !talking)row=0;
        float base=h*(scene?.9f:.97f), px=showAlina?w*.68f:w*.5f;
        if(showAlina) sprite(c,friends,frames[speaking&&((int)(t*5)%2==0)?2:walking?1:0][0],w*.27f,base+bounce,w*.43f,h*.86f,0);
        if(walking && motion)px+=(float)Math.sin(t*2)*w*.055f;
        if(pet<4) sprite(c,friends,frames[row][pet+1],px,base-bounce,w*(showAlina?.48f:.9f),h*(scene?.7f:.94f),walking&&motion?(float)Math.sin(t*5)*3:0);
        else {
            int frame=sleeping?2:talking&&((int)(t*5)%2==0)?1:walking&&((int)(t*5)%2==0)?0:-1;
            sprite(c,frame>=0?extraActions:extras,frame>=0?extraActionFrames[frame][pet-4]:extraFrames[pet-4],px,base-bounce,w*(showAlina?.49f:.92f),h*(scene?.7f:.94f),motion&&walking?(float)Math.sin(t*7)*4:0);
        }
        if(eating && scene) {
            float x=px+w*.08f,y=base+2;
            paint.setColor(Color.rgb(177,135,195)); c.drawRoundRect(x-w*.12f,y-h*.08f,x+w*.12f,y,10,10,paint);
            paint.setColor(Color.rgb(244,216,170)); c.drawOval(x-w*.12f,y-h*.105f,x+w*.12f,y-h*.04f,paint);
            boolean plantEater=pet==2||pet==3||pet==6||pet==7||pet==8;
            paint.setColor(plantEater?Color.rgb(98,146,67):Color.rgb(143,105,65));
            for(int i=0;i<6;i++) {
                float foodX=x-w*.08f+i*w*.03f;
                if(plantEater)c.drawOval(foodX-3,y-h*.09f,foodX+5,y-h*.04f,paint);
                else c.drawCircle(foodX,y-h*.07f,4,paint);
            }
        }
        if(a.equals("brush") && scene) {
            float x=px+w*.13f, y=h*.4f+(motion?(float)Math.sin(t*7)*12:0);
            paint.setColor(Color.rgb(128,80,156)); c.drawRoundRect(x,y,x+25,y+45,8,8,paint);
            paint.setColor(Color.rgb(242,200,147)); c.drawRoundRect(x-5,y-5,x+30,y+15,8,8,paint);
        }
        if(sleeping && scene) {
            paint.setColor(Color.rgb(115,78,141)); paint.setTypeface(Typeface.DEFAULT_BOLD); paint.setTextSize(h*.07f);
            c.drawText("z z z",px-w*.06f,h*.24f,paint);
        }
        if(scene && isShown() && (motion || speaking || SystemClock.uptimeMillis()<actionUntil)) postInvalidateDelayed(50);
    }
    @Override public boolean onTouchEvent(MotionEvent e) {
        if(!scene)return false;
        if(e.getAction()==MotionEvent.ACTION_DOWN) { startedX=e.getX(); return true; }
        if(e.getAction()==MotionEvent.ACTION_UP) { act(Math.abs(e.getX()-startedX)>30?"brush":"play"); performClick(); return true; }
        return true;
    }
    @Override public boolean performClick() { super.performClick(); return true; }
}
