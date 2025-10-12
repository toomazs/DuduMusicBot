package com.dudumusic.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LanguageManager {
    private static final Map<Long, String> guildLanguage = new ConcurrentHashMap<>();

    public static void setLanguage(long guildId, String lang) {
        if (lang == null) return;
        guildLanguage.put(guildId, lang);
    }

    public static String getLanguage(long guildId) {
        return guildLanguage.getOrDefault(guildId, "ptbr");
    }
}
