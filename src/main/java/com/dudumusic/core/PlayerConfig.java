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


    logger.info("Configurando player de audio..");

    instance = new DefaultAudioPlayerManager();

    YoutubeAudioSourceManager youtubeManager = new YoutubeAudioSourceManager();
    instance.registerSourceManager(youtubeManager);
    logger.info("Fonte YouTube registrada");

    SpotifySourceManager spotifyManager = new SpotifySourceManager(
            null,
            BotConfig.getSpotifyClientId(),
            BotConfig.getSpotifyClientSecret(),
            "BR",
            instance
    );
    instance.registerSourceManager(spotifyManager);
        logger.info("Fonte Spotify registrada");

        AudioSourceManagers.registerRemoteSources(instance);
        logger.info("Fontes remotas registradas");

        logger.info("Player de audio configurado com sucesso");
        return instance;

    }
    public static AudioPlayerManager getInstance() {
        return instance;
    }
}