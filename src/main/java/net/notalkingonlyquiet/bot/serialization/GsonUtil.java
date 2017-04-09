
package net.notalkingonlyquiet.bot.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 *
 * @author arawson
 */
public class GsonUtil {
    private static final ThreadLocal<Gson> threadSerializer = new ThreadLocal<Gson>() {
        @Override
        protected Gson initialValue() {
            return new GsonBuilder()
                    .setPrettyPrinting()
                    .create();
        }
    };
    
    public static Gson getGson() {
        return threadSerializer.get();
    }
}
