package com.dudumusic.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MusicLinkConverter {
    private static final Logger logger = LoggerFactory.getLogger(MusicLinkConverter.class);

    private static final Pattern APPLE_MUSIC_PATTERN = Pattern.compile("music\\.apple\\.com/([^/]+)/song/([^/]+)/(\\d+)");

    public static class ConversionResult {
        public final String searchQuery;
        public final String artworkUrl;

        public ConversionResult(String searchQuery, String artworkUrl) {
            this.searchQuery = searchQuery;
            this.artworkUrl = artworkUrl;
        }
    }

    private static final java.util.Map<String, String> artworkCache = new java.util.concurrent.ConcurrentHashMap<>();

    private static final java.util.Map<String, String> trackArtworkCache = new java.util.concurrent.ConcurrentHashMap<>();

    public static String convertToYouTubeSearch(String url, SourceDetector.SourceType sourceType) {
        ConversionResult result = convertToYouTubeSearchWithArtwork(url, sourceType);
        return result != null ? result.searchQuery : null;
    }

    public static void cacheTrackArtwork(String trackIdentifier, String artworkUrl) {
        if (trackIdentifier != null && artworkUrl != null) {
            trackArtworkCache.put(trackIdentifier, artworkUrl);
        }
    }

    public static String getTrackArtwork(String trackIdentifier) {
        return trackArtworkCache.get(trackIdentifier);
    }

    public static ConversionResult convertToYouTubeSearchWithArtwork(String url, SourceDetector.SourceType sourceType) {
        try {
            switch (sourceType) {
                case DEEZER:
                    return convertDeezerToSearchWithArt(url);
                case APPLE_MUSIC:
                    return convertAppleMusicToSearchWithArt(url);
                default:
                    return null;
            }
        } catch (Exception e) {
            logger.error("Erro ao converter URL para busca no YouTube: {}", url, e);
            return null;
        }
    }

    public static String getArtworkUrl(String searchQuery) {
        return artworkCache.get(searchQuery);
    }

    private static ConversionResult convertDeezerToSearchWithArt(String url) {
        try {
            DeezerMetadata metadata = fetchDeezerMetadataFull(url);
            if (metadata != null && metadata.title != null && !metadata.title.isEmpty()) {
                String searchQuery = metadata.artist != null
                    ? "ytsearch:" + metadata.artist + " " + metadata.title
                    : "ytsearch:" + metadata.title;

                logger.info("Informações extraídas do Deezer: {} (artwork: {})",
                    metadata.artist != null ? metadata.artist + " - " + metadata.title : metadata.title,
                    metadata.artworkUrl != null ? "sim" : "não");

                if (metadata.artworkUrl != null) {
                    artworkCache.put(searchQuery, metadata.artworkUrl);
                }
                return new ConversionResult(searchQuery, metadata.artworkUrl);
            }
        } catch (Exception e) {
            logger.error("Erro ao buscar metadados do Deezer: {}", url, e);
        }

        return null;
    }

    private static class DeezerMetadata {
        String title;
        String artist;
        String artworkUrl;

        DeezerMetadata(String title, String artist, String artworkUrl) {
            this.title = title;
            this.artist = artist;
            this.artworkUrl = artworkUrl;
        }
    }

    private static DeezerMetadata fetchDeezerMetadataFull(String urlString) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setInstanceFollowRedirects(true);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
                );

                String line;
                StringBuilder content = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                    if (content.length() > 200000) break;
                }
                reader.close();

                String html = content.toString();

                String title = null;
                String artist = null;
                String artworkUrl = null;

                Pattern dzrPattern = Pattern.compile("\"SNG_TITLE\"\\s*:\\s*\"([^\"]+)\"");
                Matcher matcher = dzrPattern.matcher(html);
                if (matcher.find()) {
                    title = matcher.group(1);
                }

                Pattern artistPattern = Pattern.compile("\"ART_NAME\"\\s*:\\s*\"([^\"]+)\"");
                matcher = artistPattern.matcher(html);
                if (matcher.find()) {
                    artist = matcher.group(1);
                }

                Pattern artworkPattern = Pattern.compile("\"ALB_PICTURE\"\\s*:\\s*\"([^\"]+)\"");
                matcher = artworkPattern.matcher(html);
                if (matcher.find()) {
                    String albumPicture = matcher.group(1);
                    artworkUrl = "https://e-cdns-images.dzcdn.net/images/cover/" + albumPicture + "/1000x1000-000000-80-0-0.jpg";
                }

                if (title == null) {
                    title = extractMetaTag(html, "og:title");
                    if (title != null) {
                        title = title.replaceAll(" - Deezer$", "").trim();
                    }
                }

                if (artworkUrl == null) {
                    artworkUrl = extractMetaTag(html, "og:image");
                }

                if (title != null && !title.isEmpty()) {
                    return new DeezerMetadata(title, artist, artworkUrl);
                }
            }
        } catch (Exception e) {
            logger.error("Erro ao fazer fetch do Deezer: {}", urlString, e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return null;
    }


    private static String extractMetaTag(String html, String property) {
        Pattern pattern = Pattern.compile(
            "<meta[^>]+(?:property|name)=['\"]" + Pattern.quote(property) + "['\"][^>]+content=['\"]([^'\"]+)['\"]",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        pattern = Pattern.compile(
            "<meta[^>]+content=['\"]([^'\"]+)['\"][^>]+(?:property|name)=['\"]" + Pattern.quote(property) + "['\"]",
            Pattern.CASE_INSENSITIVE
        );
        matcher = pattern.matcher(html);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }

    private static ConversionResult convertAppleMusicToSearchWithArt(String url) {
        try {
            AppleMusicMetadata metadata = fetchAppleMusicMetadata(url);

            if (metadata != null && metadata.songName != null) {
                String searchQuery = metadata.artist != null
                    ? "ytsearch:" + metadata.artist + " " + metadata.songName
                    : "ytsearch:" + metadata.songName;

                logger.info("Convertendo link Apple Music para busca no YouTube: {} (artwork: {})",
                    metadata.artist != null ? metadata.artist + " - " + metadata.songName : metadata.songName,
                    metadata.artworkUrl != null ? "sim" : "não");

                if (metadata.artworkUrl != null) {
                    artworkCache.put(searchQuery, metadata.artworkUrl);
                }
                return new ConversionResult(searchQuery, metadata.artworkUrl);
            }
        } catch (Exception e) {
            logger.error("Erro ao processar URL Apple Music: {}", url, e);
        }

        return null;
    }

    private static class AppleMusicMetadata {
        String songName;
        String artist;
        String artworkUrl;

        AppleMusicMetadata(String songName, String artist, String artworkUrl) {
            this.songName = songName;
            this.artist = artist;
            this.artworkUrl = artworkUrl;
        }
    }

    private static AppleMusicMetadata fetchAppleMusicMetadata(String urlString) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setInstanceFollowRedirects(true);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
                );

                String line;
                StringBuilder content = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                    if (content.length() > 300000) break;
                }
                reader.close();

                String html = content.toString();

                String songTitle = null;
                String artist = null;
                String artworkUrl = null;

                Pattern jsonLdPattern = Pattern.compile("<script type=\"application/ld\\+json\">\\s*([\\s\\S]*?)\\s*</script>", Pattern.CASE_INSENSITIVE);
                Matcher jsonMatcher = jsonLdPattern.matcher(html);

                while (jsonMatcher.find()) {
                    String jsonLd = jsonMatcher.group(1);

                    if (jsonLd.contains("\"@type\"") && (jsonLd.contains("MusicRecording") || jsonLd.contains("MusicComposition"))) {
                        Pattern namePattern = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");
                        Matcher nameMatcher = namePattern.matcher(jsonLd);
                        if (nameMatcher.find()) {
                            songTitle = nameMatcher.group(1);
                        }

                        Pattern multiArtistPattern = Pattern.compile("\"byArtist\"\\s*:\\s*\\[([^\\]]+)\\]");
                        Matcher multiMatcher = multiArtistPattern.matcher(jsonLd);
                        if (multiMatcher.find()) {
                            String artistsJson = multiMatcher.group(1);
                            Pattern artistNamePattern = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");
                            Matcher artistNameMatcher = artistNamePattern.matcher(artistsJson);
                            StringBuilder artists = new StringBuilder();
                            while (artistNameMatcher.find()) {
                                if (artists.length() > 0) artists.append(" & ");
                                artists.append(artistNameMatcher.group(1));
                            }
                            if (artists.length() > 0) {
                                artist = artists.toString();
                            }
                        }

                        Pattern imagePattern = Pattern.compile("\"image\"\\s*:\\s*\"([^\"]+)\"");
                        Matcher imageMatcher = imagePattern.matcher(jsonLd);
                        if (imageMatcher.find()) {
                            artworkUrl = imageMatcher.group(1);
                            artworkUrl = artworkUrl.replaceAll("/(\\d+)x\\d+.*", "/1200x1200bb.jpg");
                        }

                        break;
                    }
                }

                if (songTitle == null) {
                    String ogTitle = extractMetaTag(html, "og:title");
                    if (ogTitle != null) {
                        ogTitle = ogTitle.replaceAll("&amp;", "&");
                        ogTitle = ogTitle.replaceAll("&nbsp;", " ");

                        Pattern titleArtistPattern = Pattern.compile("^(.+?)\\s+(?:by|de)\\s+(.+?)\\s+(?:on|no)\\s+Apple", Pattern.CASE_INSENSITIVE);
                        Matcher titleArtistMatcher = titleArtistPattern.matcher(ogTitle);
                        if (titleArtistMatcher.find()) {
                            songTitle = titleArtistMatcher.group(1).trim();
                            artist = titleArtistMatcher.group(2).trim();
                        } else {
                            songTitle = ogTitle.replaceAll(" - Single$", "").trim();
                            songTitle = songTitle.replaceAll(" (?:by|de) .+? (?:on|no) Apple.?Music", "").trim();
                        }
                    }
                }

                if (artworkUrl == null) {
                    artworkUrl = extractMetaTag(html, "og:image");
                    if (artworkUrl != null) {
                        artworkUrl = artworkUrl.replaceAll("/(\\d+)x\\d+.*", "/1200x1200bb.jpg");
                    }
                }

                if (songTitle != null && !songTitle.isEmpty()) {
                    return new AppleMusicMetadata(songTitle, artist, artworkUrl);
                }
            }
        } catch (Exception e) {
            logger.error("Erro ao buscar metadados do Apple Music: {}", urlString, e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return null;
    }

    public static boolean needsConversion(SourceDetector.SourceType sourceType) {
        return sourceType == SourceDetector.SourceType.DEEZER ||
               sourceType == SourceDetector.SourceType.APPLE_MUSIC;
    }

    public static class PlaylistResult {
        public final String playlistName;
        public final String artworkUrl;
        public final java.util.List<String> searchQueries;

        public PlaylistResult(String playlistName, String artworkUrl, java.util.List<String> searchQueries) {
            this.playlistName = playlistName;
            this.artworkUrl = artworkUrl;
            this.searchQueries = searchQueries;
        }
    }

    public static PlaylistResult convertPlaylistToYouTubeSearches(String url, SourceDetector.SourceType sourceType) {
        try {
            if (sourceType == SourceDetector.SourceType.DEEZER) {
                return convertDeezerPlaylist(url);
            } else if (sourceType == SourceDetector.SourceType.APPLE_MUSIC) {
                return convertAppleMusicPlaylist(url);
            }
        } catch (Exception e) {
            logger.error("Erro ao converter playlist para buscas no YouTube: {}", url, e);
        }
        return null;
    }

    private static PlaylistResult convertDeezerPlaylist(String url) {
        HttpURLConnection connection = null;
        try {
            Pattern idPattern = Pattern.compile("playlist/(\\d+)");
            java.util.regex.Matcher idMatcher = idPattern.matcher(url);
            String playlistId = null;
            if (idMatcher.find()) playlistId = idMatcher.group(1);

            if (playlistId != null) {
                String apiUrl = "https://api.deezer.com/playlist/" + playlistId;
                URL api = new URL(apiUrl);
                HttpURLConnection apiConn = (HttpURLConnection) api.openConnection();
                apiConn.setRequestMethod("GET");
                apiConn.setRequestProperty("User-Agent", "Mozilla/5.0");
                apiConn.setConnectTimeout(10000);
                apiConn.setReadTimeout(10000);

                int code = apiConn.getResponseCode();
                if (code == HttpURLConnection.HTTP_OK) {
                    BufferedReader rd = new BufferedReader(new InputStreamReader(apiConn.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder sb = new StringBuilder();
                    String l;
                    while ((l = rd.readLine()) != null) {
                        sb.append(l).append('\n');
                    }
                    rd.close();
                    try {
                        JSONObject root = new JSONObject(sb.toString());
                        String playlistName = root.optString("title", null);
                        String artworkUrl = root.optString("picture_xl", null);

                        java.util.List<String> queries = new java.util.ArrayList<>();
                        if (root.has("tracks")) {
                            JSONObject tracksObj = root.optJSONObject("tracks");
                            if (tracksObj != null && tracksObj.has("data")) {
                                JSONArray arr = tracksObj.optJSONArray("data");
                                if (arr != null) {
                                    for (int i = 0; i < arr.length(); i++) {
                                        JSONObject t = arr.optJSONObject(i);
                                        if (t == null) continue;
                                        String title = t.optString("title", null);
                                        String artist = null;
                                        JSONObject artistObj = t.optJSONObject("artist");
                                        if (artistObj != null) artist = artistObj.optString("name", null);
                                        if (title != null) {
                                            title = Jsoup.parse(title).text();
                                            if (artist != null) artist = Jsoup.parse(artist).text();
                                            queries.add("ytsearch:" + (artist != null ? artist + " " + title : title));
                                        }
                                    }
                                }
                            }
                        }

                        if (!queries.isEmpty()) {
                            logger.info("Playlist Deezer extraída via API: {} ({} músicas)", playlistName, queries.size());
                            return new PlaylistResult(playlistName, artworkUrl, queries);
                        }
                    } catch (Exception je) {
                        logger.debug("Deezer API parse falhou, fallback para HTML", je);
                    }
                }
            }

            URL urlObj = new URL(url);
            connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setInstanceFollowRedirects(true);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
                );

                String line;
                StringBuilder content = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                    if (content.length() > 500000) break;
                }
                reader.close();

                String html = content.toString();

                String playlistName = extractMetaTag(html, "og:title");
                if (playlistName != null) {
                    playlistName = playlistName.replaceAll(" - Deezer$", "").trim();
                    try { playlistName = java.net.URLDecoder.decode(playlistName, StandardCharsets.UTF_8.name()); } catch (Exception ignore) {}
                    playlistName = Jsoup.parse(playlistName).text();
                }

                String artworkUrl = extractMetaTag(html, "og:image");

                java.util.List<String> searchQueries = new java.util.ArrayList<>();
                Pattern trackPattern = Pattern.compile("\"SNG_TITLE\"\\s*:\\s*\"([^\"]+)\"[\\s\\S]*?\"ART_NAME\"\\s*:\\s*\"([^\"]+)\"", Pattern.DOTALL);
                Matcher matcher = trackPattern.matcher(html);

                while (matcher.find()) {
                    String title = matcher.group(1);
                    String artist = matcher.group(2);
                    try { title = Jsoup.parse(title).text(); artist = Jsoup.parse(artist).text(); } catch (Exception ignore) {}
                    searchQueries.add("ytsearch:" + artist + " " + title);
                }

                if (!searchQueries.isEmpty()) {
                    logger.info("Playlist Deezer extraída: {} ({} músicas)", playlistName, searchQueries.size());
                    return new PlaylistResult(playlistName, artworkUrl, searchQueries);
                }
            }
        } catch (Exception e) {
            logger.error("Erro ao buscar playlist do Deezer: {}", url, e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    private static PlaylistResult convertAppleMusicPlaylist(String url) {
        HttpURLConnection connection = null;
        try {
            URL urlObj = new URL(url);
            connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setInstanceFollowRedirects(true);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
                );

                String line;
                StringBuilder content = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                    if (content.length() > 500000) break;
                }
                reader.close();

                String html = content.toString();

                String playlistName = null;
                String artworkUrl = null;
                java.util.List<String> searchQueries = new java.util.ArrayList<>();

                Pattern jsonLdPattern = Pattern.compile("<script type=\"application/ld\\+json\">\\s*([\\s\\S]*?)\\s*</script>", Pattern.CASE_INSENSITIVE);
                Matcher jsonMatcher = jsonLdPattern.matcher(html);

                while (jsonMatcher.find()) {
                    String jsonLd = jsonMatcher.group(1).trim();

                    if (!jsonLd.contains("MusicPlaylist")) continue;

                    try {
                        Object parsed = null;
                        jsonLd = jsonLd.replaceAll("^<!--\\s*", "").replaceAll("\\s*-->", "");
                        if (jsonLd.startsWith("{")) {
                            JSONObject obj = new JSONObject(jsonLd);
                            parsed = obj;
                        } else if (jsonLd.startsWith("[")) {
                            JSONArray arr = new JSONArray(jsonLd);
                            if (arr.length() > 0) parsed = arr.get(0);
                        }

                        if (parsed instanceof JSONObject) {
                            JSONObject obj = (JSONObject) parsed;

                            if (obj.has("@type") && (obj.optString("@type").contains("MusicPlaylist") || obj.optString("@type").contains("Playlist"))) {
                                if (obj.has("name")) {
                                    playlistName = obj.optString("name");
                                    playlistName = Jsoup.parse(playlistName).text();
                                }

                                if (obj.has("image")) {
                                    artworkUrl = obj.optString("image");
                                    artworkUrl = artworkUrl.replaceAll("/(\\d+)x\\d+.*", "/1200x1200bb.jpg");
                                }

                                if (obj.has("track")) {
                                    Object trackObj = obj.get("track");
                                    if (trackObj instanceof JSONArray) {
                                        JSONArray tracks = (JSONArray) trackObj;
                                        for (int t = 0; t < tracks.length(); t++) {
                                            try {
                                                Object entry = tracks.get(t);
                                                if (entry instanceof JSONObject) {
                                                    JSONObject trackJson = (JSONObject) entry;
                                                    String songName = trackJson.optString("name", null);
                                                    String artist = null;
                                                    if (trackJson.has("byArtist")) {
                                                        Object byArtist = trackJson.get("byArtist");
                                                        if (byArtist instanceof JSONObject) {
                                                            artist = ((JSONObject) byArtist).optString("name", null);
                                                        } else if (byArtist instanceof JSONArray) {
                                                            JSONArray ba = (JSONArray) byArtist;
                                                            StringBuilder sb = new StringBuilder();
                                                            for (int bi = 0; bi < ba.length(); bi++) {
                                                                Object baEntry = ba.get(bi);
                                                                if (baEntry instanceof JSONObject) {
                                                                    if (sb.length() > 0) sb.append(" & ");
                                                                    sb.append(((JSONObject) baEntry).optString("name", ""));
                                                                }
                                                            }
                                                            if (sb.length() > 0) artist = sb.toString();
                                                        }
                                                    }

                                                    if (songName != null) {
                                                        songName = Jsoup.parse(songName).text();
                                                        if (artist != null) artist = Jsoup.parse(artist).text();
                                                        searchQueries.add("ytsearch:" + (artist != null ? artist + " " + songName : songName));
                                                    }
                                                }
                                            } catch (Exception ignore) {
                                            }
                                        }
                                    }
                                }

                                break;
                            }
                        }
                    } catch (JSONException je) {
                        logger.debug("Falha ao parsear JSON-LD da Apple Music, tentando próxima ocorrência", je);
                    }
                }

                if (!searchQueries.isEmpty()) {
                    logger.info("Playlist Apple Music extraída: {} ({} músicas)", playlistName, searchQueries.size());
                    return new PlaylistResult(playlistName, artworkUrl, searchQueries);
                }
                try {
                    org.jsoup.nodes.Document doc = Jsoup.parse(html);
                    if (playlistName == null) {
                        String title = null;
                        org.jsoup.nodes.Element h1 = doc.selectFirst("h1.t-page-title, h1.product-hero__title");
                        if (h1 != null) title = h1.text();
                        if (title == null) title = extractMetaTag(html, "og:title");
                        if (title != null) playlistName = Jsoup.parse(title).text();
                    }

                    if (artworkUrl == null) {
                        org.jsoup.nodes.Element img = doc.selectFirst("meta[property=og:image], img.product-hero__artwork, img.playlist-cover-art");
                        if (img != null) {
                            String src = img.hasAttr("content") ? img.attr("content") : img.attr("src");
                            if (src != null) artworkUrl = src.replaceAll("/(\\d+)x\\d+.*", "/1200x1200bb.jpg");
                        }
                    }

                    // Apple Music often has track lists in li elements with data-test attributes
                    java.util.List<org.jsoup.nodes.Element> items = doc.select("li[data-test=track], li.track, div.songs-list-row");
                    if (items != null && !items.isEmpty()) {
                        for (org.jsoup.nodes.Element it : items) {
                            try {
                                String t = it.selectFirst(".songs-list-row__song-name, .track-name, .song-title, .songs-list-row__song-title") != null ? it.selectFirst(".songs-list-row__song-name, .track-name, .song-title, .songs-list-row__song-title").text() : null;
                                String a = it.selectFirst(".songs-list-row__by-line, .artist-name, .songs-list-row__artist-name") != null ? it.selectFirst(".songs-list-row__by-line, .artist-name, .songs-list-row__artist-name").text() : null;
                                if (t == null) continue;
                                t = Jsoup.parse(t).text();
                                if (a != null) a = Jsoup.parse(a).text();
                                searchQueries.add("ytsearch:" + (a != null ? a + " " + t : t));
                            } catch (Exception ignore) {}
                        }
                    }

                    if (!searchQueries.isEmpty()) {
                        logger.info("Playlist Apple Music extraída via DOM: {} ({} músicas)", playlistName, searchQueries.size());
                        return new PlaylistResult(playlistName, artworkUrl, searchQueries);
                    }
                } catch (Exception e) {
                    logger.debug("Fallback DOM parsing falhou para Apple Music", e);
                }
            }
        } catch (Exception e) {
            logger.error("Erro ao buscar playlist do Apple Music: {}", url, e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }
}
