package com.dudumusic.listeners;

import com.dudumusic.audio.MusicManager;
import com.dudumusic.audio.QueueHistory;
import com.dudumusic.core.Translation;
import com.dudumusic.utils.EmbedFactory;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class VoiceListener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(VoiceListener.class);
    private static final long DISCONNECT_DELAY_SECONDS = 120;
    private static final Map<Long, ScheduledFuture<?>> disconnectTasks = new ConcurrentHashMap<>();
    private static final Map<Long, Boolean> autoDisconnecting = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        Guild guild = event.getGuild();
        Member bot = guild.getSelfMember();
        Member member = event.getMember();
        long guildId = guild.getIdLong();

        if (member.equals(bot)) {
            if (event.getChannelLeft() != null && event.getChannelJoined() == null) {
                logger.info("Bot foi desconectado do canal de voz '{}' no servidor '{}'",
                        event.getChannelLeft().getName(), guild.getName());

                cancelDisconnectTask(guildId);

                if (autoDisconnecting.remove(guildId) != null) {
                    logger.info("Desconexão automática detectada, ignorando evento de desconexão manual");
                    return;
                }

                MusicManager musicManager = MusicManager.getManager(guildId);

                if (musicManager != null) {
                    TextChannel textChannel = musicManager.getScheduler().getTextChannel();

                    musicManager.getPlayer().stopTrack();
                    musicManager.getScheduler().clearQueue(true, "Bot foi desconectado do canal");

                    if (textChannel != null) {
                        textChannel.sendMessageEmbeds(
                            EmbedFactory.info(
                                Translation.t(guildId, "voice_disconnected_title"),
                                Translation.t(guildId, "voice_disconnected_desc")
                            )
                        ).queue(
                            success -> logger.info("Mensagem de desconexão enviada"),
                            error -> logger.warn("Falha ao enviar mensagem de desconexão: {}", error.getMessage())
                        );
                    }
                }

                guild.getAudioManager().closeAudioConnection();
                logger.info("Audio limpo após desconexão");
                return;
            }
        }

        if (!bot.getVoiceState().inAudioChannel()) {
            return;
        }

        VoiceChannel botChannel = (VoiceChannel) bot.getVoiceState().getChannel();

        List<Member> members = botChannel.getMembers().stream()
                .filter(m -> !m.getUser().isBot())
                .toList();

        if (members.isEmpty()) {
            if (!disconnectTasks.containsKey(guildId)) {
                logger.info("Bot está sozinho no canal de voz '{}' no servidor '{}', aguardando {} segundos antes de desconectar",
                        botChannel.getName(), guild.getName(), DISCONNECT_DELAY_SECONDS);

                ScheduledFuture<?> task = scheduler.schedule(() -> {
                    if (!bot.getVoiceState().inAudioChannel()) {
                        disconnectTasks.remove(guildId);
                        return;
                    }

                    VoiceChannel currentChannel = (VoiceChannel) bot.getVoiceState().getChannel();
                    List<Member> currentMembers = currentChannel.getMembers().stream()
                            .filter(m -> !m.getUser().isBot())
                            .toList();

                    if (currentMembers.isEmpty()) {
                        logger.info("Bot ainda está sozinho após {} segundos, desconectando do servidor '{}'",
                                DISCONNECT_DELAY_SECONDS, guild.getName());

                        MusicManager musicManager = MusicManager.getManager(guildId);

                        if (musicManager != null) {
                            TextChannel textChannel = musicManager.getScheduler().getTextChannel();

                            musicManager.getPlayer().stopTrack();
                            musicManager.getScheduler().clearQueue(true, "Bot saiu do canal (todos os membros saíram)");

                            if (textChannel != null) {
                                textChannel.sendMessageEmbeds(
                                    EmbedFactory.info(
                                        Translation.t(guildId, "voice_alone_title"),
                                        Translation.t(guildId, "voice_alone_desc", DISCONNECT_DELAY_SECONDS / 60)
                                    )
                                ).queue(
                                    success -> logger.info("Mensagem de inatividade enviada"),
                                    error -> logger.warn("Falha ao enviar mensagem de inatividade: {}", error.getMessage())
                                );
                            }
                        }

                        autoDisconnecting.put(guildId, true);
                        guild.getAudioManager().closeAudioConnection();
                        logger.info("Desconectado do canal de voz por inatividade");
                    } else {
                        logger.info("Membros retornaram ao canal, cancelando desconexão automática");
                    }

                    disconnectTasks.remove(guildId);
                }, DISCONNECT_DELAY_SECONDS, TimeUnit.SECONDS);

                disconnectTasks.put(guildId, task);
            }
        } else {
            cancelDisconnectTask(guildId);
        }
    }

    private void cancelDisconnectTask(long guildId) {
        ScheduledFuture<?> task = disconnectTasks.remove(guildId);
        if (task != null && !task.isDone()) {
            task.cancel(false);
            logger.info("Tarefa de desconexão automática cancelada para servidor {}", guildId);
        }
    }
}
