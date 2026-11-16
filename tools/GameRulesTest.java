package nl.alina.tovertuin;

import java.util.Arrays;

/** Economy, progress, and care regression checks; run with tools/test-rules.ps1. */
public final class GameRulesTest {
    private static int checks;
    private static void check(boolean condition,String message) {
        checks++; if(!condition)throw new AssertionError(message);
    }
    public static void main(String[] args) {
        GameRules g=new GameRules();
        check(g.stars==0 && g.petCount()==4,"Four starter pets, no unearned currency");
        check(!g.buy(4) && !g.owned[4],"Cannot buy without stars");
        check(!g.buy(-1) && !g.buy(10),"Invalid animal IDs rejected");
        check(!g.select(4),"Cannot select an unowned pet");
        GameRules.Round round=new GameRules.Round();
        check(!round.answer(false) && round.solved==0,"Wrong answers never advance");
        check(!round.claim(g) && g.stars==0,"Incomplete rounds cannot pay");
        for(int i=0;i<5;i++)check(round.answer(true),"Correct answer advances");
        check(!round.answer(true),"Completed round cannot overrun");
        check(round.claim(g) && g.stars==5 && g.completedRounds==1,"Exactly five stars per round");
        check(!round.claim(g) && g.stars==5,"Cannot claim reward twice");
        for(int r=0;r<3;r++) {
            GameRules.Round next=new GameRules.Round(); for(int i=0;i<5;i++)next.answer(true); next.claim(g);
        }
        check(g.stars==20,"Four real learning rounds earn first animal");
        check(g.buy(4) && g.stars==0 && g.activePet==4 && g.petCount()==5,"Purchase spends exact price and selects new animal");
        check(!g.buy(4) && g.stars==0,"Duplicate purchases rejected");
        check(g.level()==2,"Learning level grows with completed rounds");
        int[] other=g.needs[0].clone();
        for(int i=0;i<200;i++)g.tick();
        check(Arrays.equals(other,g.needs[0]),"Only selected animal changes during active play");
        for(int n:g.needs[4])check(n>=20,"Needs have a gentle floor");
        for(int i=0;i<100;i++)for(int k=0;k<4;k++)g.care(k);
        for(int n:g.needs[4])check(n<=100 && n>=20,"Care stays within bounds");
        g.stars=100;check(g.buy(9)&&g.stars==0,"Most expensive pet can be bought");
        System.out.println("PASS: "+checks+" economy, progress and pet-care checks");
    }
}
