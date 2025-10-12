
package com.dudumusic.audio;

import com.dudumusic.core.PlayerConfig;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MusicManager {
    private static final Map<Long, MusicManager> managers = new ConcurrentHashMap<>();

    private final AudioPlayer player;
    private final TrackScheduler scheduler;
    private final AudioHandler audioHandler;
    private TextChannel textChannel;

    public MusicManager(AudioPlayerManager playerManager) {
        this.player = playerManager.createPlayer();
        this.scheduler = new TrackScheduler(player);
        this.player.addListener(scheduler);
        this.audioHandler = new AudioHandler(player);
    }

    public static MusicManager getManager(long guildId) {
        return managers.computeIfAbsent(guildId, id -> {
            MusicManager manager = new MusicManager(PlayerConfig.getInstance());
            manager.getScheduler().setGuildId(guildId);
            return manager;
        });
    }

    public static void removeManager(long guildId) {
        MusicManager manager = managers.remove(guildId);
        if (manager != null) {
            manager.player.destroy();
        }
    }

    public AudioPlayer getPlayer() {
        return player;
    }

    public TrackScheduler getScheduler() {
        return scheduler;
    }

    public AudioHandler getAudioHandler() {
        return audioHandler;
    }

    public TextChannel getTextChannel() {
        return textChannel;
    }

    public void setTextChannel(TextChannel textChannel) {
        this.textChannel = textChannel;
        this.scheduler.setTextChannel(textChannel);
    }
}
