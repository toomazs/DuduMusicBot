package com.dudumusic.core;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BotConfig {
    private static final Logger logger = LoggerFactory.getLogger(BotConfig.class);
    private static Dotenv dotenv;

    private static String token;
    private static String spotifyClientId;
    private static String spotifyClientSecret;

    public static void load() {
        logger.info("Carregando configuração");

        try {
            dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();

            token = getEnv("TOKEN");
            spotifyClientId = getEnv("SPOTIFY_CLIENT_ID");
            spotifyClientSecret = getEnv("SPOTIFY_CLIENT_SECRET");

            if (token == null || token.isEmpty()) {
                throw new IllegalStateException("TOKEN é obrigatório no .env");
            }

            if (spotifyClientId == null || spotifyClientId.isEmpty()) {
                throw new IllegalStateException("SPOTIFY_CLIENT_ID é obrigatório no .env");
            }

            if (spotifyClientSecret == null || spotifyClientSecret.isEmpty()) {
                throw new IllegalStateException("SPOTIFY_CLIENT_SECRET é obrigatório no .env");
            }

            logger.info("Configuração carregada com sucesso");

        } catch (Exception e) {
            logger.error("Falha ao carregar configurações: {}", e.getMessage());
            throw e;
        }
    }

    private static String getEnv(String key) {
        String value = dotenv != null ? dotenv.get(key) : null;
        if (value == null) {
            value = System.getenv(key);
        }
        return value;
    }

    public static String getToken() {
        return token;
    }

    public static String getSpotifyClientId() {
        return spotifyClientId;
    }

    public static String getSpotifyClientSecret() {
        return spotifyClientSecret;
    }
}
