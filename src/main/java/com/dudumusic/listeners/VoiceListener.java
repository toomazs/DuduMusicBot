package com.dudumusic.listeners;

import com.dudumusic.audio.MusicManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class VoiceListener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(VoiceListener.class);

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        Guild guild = event.getGuild();
        Member bot = guild.getSelfMember();

        if (!bot.getVoiceState().inAudioChannel()) {
            return;
        }

        VoiceChannel botChannel = (VoiceChannel) bot.getVoiceState().getChannel();

        List<Member> members = botChannel.getMembers().stream()
                .filter(m -> !m.getUser().isBot())
                .toList();

        if (members.isEmpty()) {
            logger.info("Bot esta sozinho no canal de voz '{}' no servidor '{}', parando reproducao",
                    botChannel.getName(), guild.getName());

            MusicManager musicManager = MusicManager.getManager(guild.getIdLong());
            musicManager.getPlayer().stopTrack();
            musicManager.getScheduler().clearQueue();

            guild.getAudioManager().closeAudioConnection();

            logger.info("Desconectado do canal de voz por inatividade");
        }
    }
}
