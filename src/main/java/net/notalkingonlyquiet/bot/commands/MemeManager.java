
package net.notalkingonlyquiet.bot.commands;

import com.google.inject.Inject;
import java.io.IOException;
import net.notalkingonlyquiet.bot.LogUtil;
import net.notalkingonlyquiet.bot.fun.MemeMap;

/**
 *
 * @author arawson
 */
//TODO: refactor backing storage for this
public class MemeManager {
    private final MemeMap memeMap;
    private final String memeFile;

	@Inject
    public MemeManager() throws IOException {
//        memeMap = MemeMap.getMemeMap(memes.memeFile);
//        memeFile = memes.memeFile;
		memeFile = null;
		memeMap = null;
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
