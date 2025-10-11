package com.dudumusic.utils;

import java.util.regex.Pattern;

public class SourceDetector {

    private static final Pattern SPOTIFY_PATTERN = Pattern.compile("(https?://)?(open\\.)?spotify\\.com/.+");
    private static final Pattern YOUTUBE_PATTERN = Pattern.compile("(https?://)?(www\\.)?(youtube\\.com|youtu\\.be)/.+");
    private static final Pattern SOUNDCLOUD_PATTERN = Pattern.compile("(https?://)?(www\\.)?soundcloud\\.com/.+");
    private static final Pattern BANDCAMP_PATTERN = Pattern.compile("(https?://)?([a-zA-Z0-9-]+\\.)?bandcamp\\.com/.+");
    private static final Pattern TWITCH_PATTERN = Pattern.compile("(https?://)?(www\\.)?twitch\\.tv/.+");
    private static final Pattern APPLE_MUSIC_PATTERN = Pattern.compile("(https?://)?music\\.apple\\.com/.+");
    private static final Pattern DEEZER_PATTERN = Pattern.compile("(https?://)?(www\\.)?deezer\\.com/.+");
    private static final Pattern HTTP_PATTERN = Pattern.compile("https?://.+\\.(mp3|m4a|ogg|flac|wav)");

    public enum SourceType {
        SPOTIFY("Spotify"),
        YOUTUBE("YouTube"),
        SOUNDCLOUD("SoundCloud"),
        BANDCAMP("Bandcamp"),
        TWITCH("Twitch"),
        APPLE_MUSIC("Apple Music"),
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
        if (BANDCAMP_PATTERN.matcher(input).matches()) {
            return SourceType.BANDCAMP;
        }
        if (TWITCH_PATTERN.matcher(input).matches()) {
            return SourceType.TWITCH;
        }
        if (APPLE_MUSIC_PATTERN.matcher(input).matches()) {
            return SourceType.APPLE_MUSIC;
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
}
