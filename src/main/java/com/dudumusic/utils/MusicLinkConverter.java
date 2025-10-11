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
                }

                String artworkUrl = extractMetaTag(html, "og:image");

                java.util.List<String> searchQueries = new java.util.ArrayList<>();
                Pattern trackPattern = Pattern.compile("\"SNG_TITLE\"\\s*:\\s*\"([^\"]+)\"[^}]*\"ART_NAME\"\\s*:\\s*\"([^\"]+)\"");
                Matcher matcher = trackPattern.matcher(html);

                while (matcher.find()) {
                    String title = matcher.group(1);
                    String artist = matcher.group(2);
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
                    String jsonLd = jsonMatcher.group(1);

                    if (jsonLd.contains("\"@type\"") && jsonLd.contains("MusicPlaylist")) {
                        Pattern namePattern = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");
                        Matcher nameMatcher = namePattern.matcher(jsonLd);
                        if (nameMatcher.find()) {
                            playlistName = nameMatcher.group(1);
                        }

                        Pattern imagePattern = Pattern.compile("\"image\"\\s*:\\s*\"([^\"]+)\"");
                        Matcher imageMatcher = imagePattern.matcher(jsonLd);
                        if (imageMatcher.find()) {
                            artworkUrl = imageMatcher.group(1);
                            artworkUrl = artworkUrl.replaceAll("/(\\d+)x\\d+.*", "/1200x1200bb.jpg");
                        }

                        Pattern trackArrayPattern = Pattern.compile("\"track\"\\s*:\\s*\\[([^\\]]+)\\]");
                        Matcher trackArrayMatcher = trackArrayPattern.matcher(jsonLd);
                        if (trackArrayMatcher.find()) {
                            String tracksJson = trackArrayMatcher.group(1);

                            Pattern trackInfoPattern = Pattern.compile("\\{[^}]*\"name\"\\s*:\\s*\"([^\"]+)\"[^}]*\"byArtist\"[^}]*\"name\"\\s*:\\s*\"([^\"]+)\"");
                            Matcher trackMatcher = trackInfoPattern.matcher(tracksJson);

                            while (trackMatcher.find()) {
                                String songName = trackMatcher.group(1);
                                String artist = trackMatcher.group(2);
                                searchQueries.add("ytsearch:" + artist + " " + songName);
                            }
                        }

                        break;
                    }
                }

                if (!searchQueries.isEmpty()) {
                    logger.info("Playlist Apple Music extraída: {} ({} músicas)", playlistName, searchQueries.size());
                    return new PlaylistResult(playlistName, artworkUrl, searchQueries);
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
