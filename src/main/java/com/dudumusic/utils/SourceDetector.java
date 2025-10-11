package com.dudumusic.utils;

import java.util.regex.Pattern;

public class SourceDetector {

    private static final Pattern SPOTIFY_PATTERN = Pattern.compile("(https?://)?(open\\.)?spotify\\.com/.+");
    private static final Pattern YOUTUBE_PATTERN = Pattern.compile("(https?://)?(www\\.)?(youtube\\.com|youtu\\.be)/.+");
    private static final Pattern SOUNDCLOUD_PATTERN = Pattern.compile("(https?://)?(www\\.)?soundcloud\\.com/.+");
    private static final Pattern APPLE_MUSIC_PATTERN = Pattern.compile("(https?://)?music\\.apple\\.com/.+");
    private static final Pattern APPLE_MUSIC_PLAYLIST_PATTERN = Pattern.compile("(https?://)?music\\.apple\\.com/.+/playlist/.+");
    private static final Pattern DEEZER_PATTERN = Pattern.compile("(https?://)?((www\\.|link\\.)?deezer\\.com)/.+");
    private static final Pattern DEEZER_PLAYLIST_PATTERN = Pattern.compile("(https?://)?((www\\.|link\\.)?deezer\\.com)/.+/playlist/.+");
    private static final Pattern HTTP_PATTERN = Pattern.compile("https?://.+\\.(mp3|m4a|ogg|flac|wav)");

    public enum SourceType {
        SPOTIFY("Spotify"),
        YOUTUBE("YouTube"),
        SOUNDCLOUD("SoundCloud"),
        APPLE_MUSIC_PLAYLIST("Apple Music Playlist"),
        APPLE_MUSIC("Apple Music"),
        DEEZER_PLAYLIST("Deezer Playlist"),
        DEEZER("Deezer"),
        HTTP("Direct URL"),
        SEARCH("Search Query"),
        UNKNOWN("Unknown");

        private final String displayName;

        SourceType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public static SourceType detect(String input) {
        if (input == null || input.isEmpty()) {
            return SourceType.UNKNOWN;
        }

        input = input.trim();

        if (SPOTIFY_PATTERN.matcher(input).matches()) {
            return SourceType.SPOTIFY;
        }
        if (YOUTUBE_PATTERN.matcher(input).matches()) {
            return SourceType.YOUTUBE;
        }
        if (SOUNDCLOUD_PATTERN.matcher(input).matches()) {
            return SourceType.SOUNDCLOUD;
        }
        if (APPLE_MUSIC_PLAYLIST_PATTERN.matcher(input).matches()) {
            return SourceType.APPLE_MUSIC_PLAYLIST;
        }
        if (APPLE_MUSIC_PATTERN.matcher(input).matches()) {
            return SourceType.APPLE_MUSIC;
        }
        if (DEEZER_PLAYLIST_PATTERN.matcher(input).matches()) {
            return SourceType.DEEZER_PLAYLIST;
        }
        if (DEEZER_PATTERN.matcher(input).matches()) {
            return SourceType.DEEZER;
        }
        if (HTTP_PATTERN.matcher(input).matches()) {
            return SourceType.HTTP;
        }

        if (!input.startsWith("http")) {
            return SourceType.SEARCH;
        }

        return SourceType.UNKNOWN;
    }

    public static String toYoutubeSearch(String query) {
        return "ytsearch:" + query;
    }

    public static String toSoundCloudSearch(String query) {
        return "scsearch:" + query;
    }

    public static String getYouTubeThumbnail(String url) {
        if (url == null) return null;

        String videoId = null;

        Pattern watchPattern = Pattern.compile("(?:youtube\\.com/watch\\?v=)([a-zA-Z0-9_-]{11})");
        java.util.regex.Matcher matcher = watchPattern.matcher(url);
        if (matcher.find()) {
            videoId = matcher.group(1);
        }

        if (videoId == null) {
            Pattern shortPattern = Pattern.compile("(?:youtu\\.be/)([a-zA-Z0-9_-]{11})");
            matcher = shortPattern.matcher(url);
            if (matcher.find()) {
                videoId = matcher.group(1);
            }
        }

        if (videoId == null) {
            Pattern embedPattern = Pattern.compile("(?:youtube\\.com/embed/)([a-zA-Z0-9_-]{11})");
            matcher = embedPattern.matcher(url);
            if (matcher.find()) {
                videoId = matcher.group(1);
            }
        }

        if (videoId != null) {
            return "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg";
        }

        return null;
    }
}
