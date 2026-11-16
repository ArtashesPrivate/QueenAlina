package nl.alina.tovertuin;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.*;

final class GameStore {
    private final SharedPreferences prefs;
    GameStore(Context context) { prefs=context.getSharedPreferences("alina_progress_v1",Context.MODE_PRIVATE); }
    GameRules load() {
        GameRules g=new GameRules();
        try {
            JSONObject o=new JSONObject(prefs.getString("state","{}"));
            g.stars=Math.max(0,o.optInt("stars",0));
            g.completedRounds=Math.max(0,o.optInt("rounds",0));
            g.maxNumber=o.optInt("max",10)==20?20:10;
            g.sound=o.optBoolean("sound",true);
            JSONArray pets=o.optJSONArray("pets");
            for(int i=0;pets!=null && i<Math.min(pets.length(),g.owned.length);i++) {
                JSONObject p=pets.optJSONObject(i); if(p==null)continue;
                g.owned[i]=i<4 || p.optBoolean("owned",false);
                String name=p.optString("name",g.names[i]).trim();
                if(!name.isEmpty())g.names[i]=name.substring(0,Math.min(18,name.length()));
                for(int j=0;j<3;j++) g.needs[i][j]=Math.max(20,Math.min(100,p.optInt("n"+j,70)));
            }
            g.select(o.optInt("active",0));
        } catch(JSONException ignored) { /* Keep safe defaults for malformed local data. */ }
        return g;
    }
    void save(GameRules g) {
        try {
            JSONObject o=new JSONObject().put("version",1).put("stars",g.stars).put("rounds",g.completedRounds)
                .put("max",g.maxNumber).put("sound",g.sound).put("active",g.activePet);
            JSONArray pets=new JSONArray();
            for(int i=0;i<g.owned.length;i++)pets.put(new JSONObject().put("owned",g.owned[i]).put("name",g.names[i])
                .put("n0",g.needs[i][0]).put("n1",g.needs[i][1]).put("n2",g.needs[i][2]));
            o.put("pets",pets); prefs.edit().putString("state",o.toString()).apply();
        } catch(JSONException e) { throw new IllegalStateException("Voortgang opslaan is mislukt",e); }
    }
}
