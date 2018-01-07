
package net.notalkingonlyquiet.bot.audio;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import net.notalkingonlyquiet.bot.config.Config;
import net.notalkingonlyquiet.bot.config.Google;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author arawson
 */
@Component
public class YouTubeSearcher {
    private final YouTube youtube;
    private final String apiKey;
    private final String baseUrl = "https://www.youtube.com/watch?v=";

    @Autowired
    public YouTubeSearcher(Config config) {
        final Google gconfig = config.google;
        youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest request) throws IOException {
            }
        }).setApplicationName(gconfig.appName).build();
        
        apiKey = gconfig.apiToken;
    }
    
    public URL performSearch(String terms) throws MalformedURLException, IOException {
        String result = null;
        YouTube.Search.List search = youtube.search().list("id,snippet");search.setKey(apiKey);
        search.setQ(terms);
        search.setType("video");
        search.setFields("items(id/kind,id/videoId)");
        search.setMaxResults(1L);
        
        SearchListResponse searchResponse = search.execute();
        List<SearchResult> items = searchResponse.getItems();
        if (items != null) {
            result = baseUrl + items.get(0).getId().getVideoId();
        }
        
        return new URL(result);
    }
}
