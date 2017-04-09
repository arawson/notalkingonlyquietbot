
package net.notalkingonlyquiet.bot.fun;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.notalkingonlyquiet.bot.serialization.GsonUtil;
import sx.blah.discord.handle.obj.IGuild;

/**
 *
 * @author arawson
 */
public class MemeMap {
    
    private Map<String, Map<Type, ArrayList<String>>> guildMemes = new HashMap<>();
    
    public Map<Type, ArrayList<String>> getMemes(IGuild guild) {
        Map<Type, ArrayList<String>> get = guildMemes.get(guild.getID());
        if (get == null) {
            get = new HashMap<>();
            guildMemes.put(guild.getID(), get);
            for (Type value : Type.values()) {
                get.put(value, new ArrayList<>());
            }
        }
        return get;
    }
    
    public static MemeMap getMemeMap(String memeFile) throws FileNotFoundException, IOException {
        MemeMap memes;
        File f = new File(memeFile);
        
        if (f.exists()) {
            try (Reader memeInput = new FileReader(f)) {
                Gson gson = GsonUtil.getGson();
                memes = gson.fromJson(memeInput, MemeMap.class);
            }
        } else {
            memes = new MemeMap();
        }
        
        return memes;
    }
    
    public static void saveMemeMap(String memeFile, MemeMap memeMap) throws IOException {
        try (Writer memeOutput = new FileWriter(memeFile)) {
            Gson gson = GsonUtil.getGson();
            gson.toJson(memeMap, memeOutput);
            memeOutput.flush();
        }
    }

    public void putMeme(IGuild guild, Type type, String link) {
        Map<Type, ArrayList<String>> memes = getMemes(guild);
        
        if (memes.containsKey(type)) {
            memes.get(type).add(link);
        } else {
            ArrayList<String> list = new ArrayList<>();
            list.add(link);
            memes.put(type, list);
        }
    }
    
    public enum Type {
        PLAYABLE,
        IMAGE;
        
        public static Type get(String t) {
            Type type = null;
            switch (t) {
                case "play":
                    type = PLAYABLE;
                    break;
                case "image":
                    type = IMAGE;
                    break;
            }
            return type;
        }

        public static Type random() {
            return values()[(int)(Math.random() * values().length)];
        }
    }
}
