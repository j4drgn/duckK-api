package com.duckchat.api.service;

import com.duckchat.api.config.OpenAIConfig;
import com.duckchat.api.dto.YouTubeRecommendation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class YouTubeDataApiService {

    private final OpenAIConfig openAIConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    // simple in-memory cache: query -> (timestamp, results)
    private final Map<String, CacheEntry> cache = new LinkedHashMap<>();
    private static final long CACHE_TTL_MS = TimeUnit.MINUTES.toMillis(30);

    private static class CacheEntry {
        long ts;
        List<YouTubeRecommendation> results;
        CacheEntry(long ts, List<YouTubeRecommendation> results) { this.ts = ts; this.results = results; }
    }

    public List<YouTubeRecommendation> searchByQuery(String query, int maxResults) {
        List<YouTubeRecommendation> out = new ArrayList<>();
        // check cache
        CacheEntry ce = cache.get(query);
        if (ce != null && (System.currentTimeMillis() - ce.ts) < CACHE_TTL_MS) {
            return ce.results;
        }
        try {
            String apiKey = openAIConfig.getYoutubeApiKey();
            if (apiKey == null || apiKey.isEmpty()) return out;
            URI uri = UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/youtube/v3/search")
                    .queryParam("part", "snippet")
                    .queryParam("maxResults", maxResults)
                    .queryParam("q", query)
                    .queryParam("type", "video")
                    .queryParam("key", apiKey)
                    .build().toUri();
            // retry with exponential backoff
            String body = null;
            int attempts = 0;
            int maxAttempts = 3;
            while (attempts < maxAttempts) {
                try {
                    ResponseEntity<String> resp = restTemplate.getForEntity(uri, String.class);
                    body = resp.getBody();
                    break;
                } catch (Exception ex) {
                    attempts++;
                    long backoff = (long) Math.pow(2, attempts) * 500L;
                    try { Thread.sleep(backoff); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }
            if (body == null) return out;
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            var root = om.readTree(body);
            if (root.has("items")) {
                for (var item : root.get("items")) {
                    var id = item.get("id").get("videoId").asText();
                    var snip = item.get("snippet");
                    var title = snip.get("title").asText();
                    var desc = snip.get("description").asText();
                    YouTubeRecommendation rec = new YouTubeRecommendation();
                    rec.setTitle(title);
                    rec.setDescription(desc);
                    rec.setVideoId(id);
                    rec.setQuery(query);
                    out.add(rec);
                }
            }
            // cache
            cache.put(query, new CacheEntry(System.currentTimeMillis(), out));
        } catch (Exception e) {
            // swallow
        }
        return out;
    }
}
