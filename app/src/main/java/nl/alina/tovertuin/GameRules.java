package nl.alina.tovertuin;

import java.util.*;

/** Pure game rules; independent of Android so the reward economy can be tested. */
public final class GameRules {
    public static final String[] IDS = {"hond","kat","dino","konijn","hamster","egel","schildpad","pony","eenhoorn","vos"};
    public static final String[] NAMES = {"Bobby","Mila","Dino","Pluis","Pip","Prik","Tess","Roos","Luna","Fien"};
    public static final String[] LABELS = {"Hond","Kat","Dino","Konijn","Hamster","Egel","Schildpad","Pony","Eenhoorn","Vos"};
    public static final int[] PRICES = {0,0,0,0,20,30,40,60,80,100};
    public static final int ROUND_SIZE = 5;
    public static final int ROUND_REWARD = 5;
    public int stars;
    public int completedRounds;
    public int activePet;
    public int maxNumber = 10;
    public boolean sound = true;
    public final boolean[] owned = {true,true,true,true,false,false,false,false,false,false};
    public final int[][] needs = new int[10][3];
    public final String[] names = NAMES.clone();

    public GameRules() { for (int[] n : needs) Arrays.fill(n,70); }

    public boolean buy(int pet) {
        if (pet < 0 || pet >= owned.length || owned[pet] || stars < PRICES[pet]) return false;
        stars -= PRICES[pet]; owned[pet] = true; activePet = pet; return true;
    }

    public boolean select(int pet) {
        if (pet < 0 || pet >= owned.length || !owned[pet]) return false;
        activePet = pet; return true;
    }

    public void care(int kind) {
        if (kind < 0 || kind > 3) return;
        int[] n = needs[activePet];
        if (kind == 0) n[0] = Math.min(100, n[0]+25);
        if (kind == 1) n[1] = Math.min(100, n[1]+25);
        if (kind == 2) { n[1] = Math.min(100,n[1]+15); n[2] = Math.max(20,n[2]-8); }
        if (kind == 3) n[2] = Math.min(100,n[2]+30);
    }

    public void tick() {
        // Only active play changes needs. No absence penalty and no sick/dead pets.
        int[] n = needs[activePet];
        n[0] = Math.max(20,n[0]-2); n[1] = Math.max(20,n[1]-1); n[2] = Math.max(20,n[2]-1);
    }

    public int petCount() { int n=0; for(boolean b:owned) if(b)n++; return n; }
    public int level() { return 1 + completedRounds / 3; }

    public static final class Round {
        public int solved;
        private boolean awarded;
        public boolean answer(boolean correct) {
            if (!correct || solved >= ROUND_SIZE) return false;
            solved++; return true;
        }
        public boolean claim(GameRules game) {
            if (solved != ROUND_SIZE || awarded) return false;
            game.stars += ROUND_REWARD; game.completedRounds++; awarded = true; return true;
        }
    }
}
