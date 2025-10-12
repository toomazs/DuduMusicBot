package com.dudumusic.core;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.github.topi314.lavasrc.spotify.SpotifySourceManager;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerConfig {
    private static final Logger logger = LoggerFactory.getLogger(PlayerConfig.class);
    private static AudioPlayerManager instance;

    public static AudioPlayerManager setup() {
        if (instance != null) {
            return instance;
        }

        logger.info("Configurando player de áudio..");

    instance = new DefaultAudioPlayerManager();

    // yt com OAuth2
    YoutubeAudioSourceManager youtubeManager = new YoutubeAudioSourceManager();

    // Configurar OAuth2 se o refresh token estiver disponível
    String refreshToken = BotConfig.getYoutubeRefreshToken();
    if (refreshToken != null && !refreshToken.isEmpty()) {
        try {
            youtubeManager.useOauth2(refreshToken, false);
            logger.info("YouTube OAuth2 configurado com sucesso");
        } catch (Exception e) {
            logger.error("Erro ao configurar YouTube OAuth2: {}", e.getMessage());
            logger.warn("Continuando sem OAuth2 - pode haver limitações de reprodução");
        }
    } else {
        logger.warn("YouTube OAuth2 não configurado - recomendado adicionar YOUTUBE_REFRESH_TOKEN no .env");
    }

    instance.registerSourceManager(youtubeManager);
    logger.info("Fonte YouTube registrada");

    // popotify
    SpotifySourceManager spotifyManager = new SpotifySourceManager(
            null,
            BotConfig.getSpotifyClientId(),
            BotConfig.getSpotifyClientSecret(),
            "BR",
            instance
    );
        instance.registerSourceManager(spotifyManager);
        logger.info("Fonte Spotify registrada");

        // audios remotos (soundcloud, http, etc)
        AudioSourceManagers.registerRemoteSources(instance);
        logger.info("Fontes remotas registradas");

        logger.info("Player de áudio configurado com sucesso");
        return instance;

    }
    public static AudioPlayerManager getInstance() {
        return instance;
    }
}