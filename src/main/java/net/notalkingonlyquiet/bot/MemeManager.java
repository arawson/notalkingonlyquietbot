
package net.notalkingonlyquiet.bot;

import java.io.File;
import java.io.IOException;
import net.notalkingonlyquiet.bot.config.Memes;
import net.notalkingonlyquiet.bot.fun.MemeMap;

/**
 *
 * @author arawson
 */
public class MemeManager {
    private final MemeMap memeMap;
    private final String memeFile;

    public MemeManager(Memes memes) throws IOException {
        memeMap = MemeMap.getMemeMap(memes.memeFile);
        memeFile = memes.memeFile;
    }
    
    void deinit() {
        save();
    }

    MemeMap getMemeMap() {
        return memeMap;
    }

    void save() {
        try {
            MemeMap.saveMemeMap(memeFile, memeMap);
        } catch (IOException ex) {
            LogUtil.logError("Error saving meme file...");
            LogUtil.logError(ex.getLocalizedMessage());
        }
    }
}
