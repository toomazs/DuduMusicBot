package com.dudumusic.listeners;

import com.dudumusic.audio.MusicManager;
import com.dudumusic.commands.info.QueueCommand;
import com.dudumusic.core.Translation;
import com.dudumusic.utils.EmbedFactory;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

public class ButtonListener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ButtonListener.class);
    private static final int TRACKS_PER_PAGE = 10;

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String buttonId = event.getComponentId();

        if (buttonId.startsWith("restore_queue:")) {
            handleRestoreQueue(event);
            return;
        }

        if (!buttonId.startsWith("queue:")) {
            return;
        }

        String[] parts = buttonId.split(":");
        if (parts.length < 2) {
            return;
        }

        String action = parts[1];

        try {
            switch (action) {
                case "first" -> handleFirstPage(event);
                case "prev" -> handlePreviousPage(event, parts);
                case "next" -> handleNextPage(event, parts);
                case "last" -> handleLastPage(event);
                case "clear" -> handleClearQueue(event);
                default -> logger.warn("Ação de botão desconhecida: {}", action);
            }
        } catch (Exception e) {
            long guildId = event.getGuild() != null ? event.getGuild().getIdLong() : 0;
            logger.error("Erro ao processar interação de botão: {}", buttonId, e);
            event.reply(Translation.t(guildId, "button_error"))
                    .setEphemeral(true)
                    .queue();
        }
    }

    private void handlePreviousPage(ButtonInteractionEvent event, String[] parts) {
        if (parts.length < 3) {
            return;
        }

        int currentPage = Integer.parseInt(parts[2]);
        int newPage = currentPage - 1;

        if (newPage < 0) {
            long guildId = event.getGuild().getIdLong();
            event.reply(Translation.t(guildId, "button_first_page"))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        updateQueuePage(event, newPage);
    }

    private void handleNextPage(ButtonInteractionEvent event, String[] parts) {
        if (parts.length < 3) {
            return;
        }

        int currentPage = Integer.parseInt(parts[2]);
        int newPage = currentPage + 1;

        long guildId = event.getGuild().getIdLong();
        MusicManager musicManager = MusicManager.getManager(guildId);
        BlockingQueue<?> queue = musicManager.getScheduler().getQueue();

        int totalPages = (int) Math.ceil((double) queue.size() / TRACKS_PER_PAGE);

        if (newPage >= totalPages) {
            event.reply(Translation.t(guildId, "button_last_page"))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        updateQueuePage(event, newPage);
    }

    private void handleFirstPage(ButtonInteractionEvent event) {
        updateQueuePage(event, 0);
    }

    private void handleLastPage(ButtonInteractionEvent event) {
        long guildId = event.getGuild().getIdLong();
        MusicManager musicManager = MusicManager.getManager(guildId);
        BlockingQueue<?> queue = musicManager.getScheduler().getQueue();

        int totalPages = (int) Math.ceil((double) queue.size() / TRACKS_PER_PAGE);
        int lastPage = Math.max(0, totalPages - 1);

        updateQueuePage(event, lastPage);
    }

    private void updateQueuePage(ButtonInteractionEvent event, int page) {
        long guildId = event.getGuild().getIdLong();
        MusicManager musicManager = MusicManager.getManager(guildId);

        var currentTrack = musicManager.getPlayer().getPlayingTrack();
        var queue = musicManager.getScheduler().getQueue();

        MessageEmbed embed = QueueCommand.createQueueEmbed(currentTrack, queue, page, guildId);

        int totalPages = (int) Math.ceil((double) queue.size() / TRACKS_PER_PAGE);

        Button firstButton = Button.secondary("queue:first", "|<<")
                .withDisabled(page == 0);
        Button prevButton = Button.secondary("queue:prev:" + page, "<<")
                .withDisabled(page == 0);
        Button nextButton = Button.secondary("queue:next:" + page, ">>")
                .withDisabled(page >= totalPages - 1);
        Button lastButton = Button.secondary("queue:last", ">>|")
                .withDisabled(page >= totalPages - 1);
        Button clearButton = Button.danger("queue:clear", Translation.t(guildId, "queue_btn_clear"));

        event.editMessageEmbeds(embed)
                .setActionRow(firstButton, prevButton, nextButton, lastButton, clearButton)
                .queue();

        logger.info("Página da fila atualizada para {} no servidor: {}", page, guildId);
    }

    private void handleClearQueue(ButtonInteractionEvent event) {
        long guildId = event.getGuild().getIdLong();
        MusicManager musicManager = MusicManager.getManager(guildId);

        if (musicManager.getScheduler().getQueue().isEmpty()) {
            event.reply(Translation.t(guildId, "clear_empty_desc"))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        int size = musicManager.getScheduler().getQueue().size();
        musicManager.getScheduler().clearQueue();

        event.editMessageEmbeds(
                EmbedFactory.success(
                        Translation.t(guildId, "clear_title"),
                        Translation.t(guildId, "clear_desc", size)
                )
        ).setComponents().queue();

        logger.info("Fila limpa via botão no servidor: {} ({} músicas)", guildId, size);
    }

    private void handleRestoreQueue(ButtonInteractionEvent event) {
        String buttonId = event.getComponentId();
        String[] parts = buttonId.split(":");

        if (parts.length < 2) {
            return;
        }

        long guildId = event.getGuild().getIdLong();
        Member member = event.getMember();

        if (member == null || !member.getVoiceState().inAudioChannel()) {
            event.replyEmbeds(
                EmbedFactory.error(
                    Translation.t(guildId, "voice_not_in_channel_title"),
                    Translation.t(guildId, "voice_user_must_be_in_channel")
                )
            ).setEphemeral(true).queue();
            logger.warn("Usuário {} tentou restaurar fila sem estar em canal de voz no servidor {}",
                member != null ? member.getUser().getName() : "desconhecido", guildId);
            return;
        }

        MusicManager musicManager = MusicManager.getManager(guildId);

        if (musicManager == null || musicManager.getScheduler() == null) {
            event.reply(Translation.t(guildId, "previousqueue_no_manager_desc"))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        try {
            int snapshotIndex = Integer.parseInt(parts[1]);
            com.dudumusic.audio.QueueHistory history = com.dudumusic.audio.QueueHistory.getInstance(guildId);
            com.dudumusic.audio.QueueHistory.QueueSnapshot snapshot = history.getSnapshot(snapshotIndex);

            if (snapshot == null) {
                event.reply(Translation.t(guildId, "previousqueue_invalid_selection"))
                        .setEphemeral(true)
                        .queue();
                return;
            }

            int restored = musicManager.getScheduler().restoreQueue(snapshot);

            AudioManager audioManager = event.getGuild().getAudioManager();
            if (!audioManager.isConnected()) {
                VoiceChannel userVoiceChannel = (VoiceChannel) member.getVoiceState().getChannel();
                audioManager.openAudioConnection(userVoiceChannel);
                audioManager.setSendingHandler(musicManager.getAudioHandler());
                logger.info("Bot conectado ao canal de voz '{}' para restaurar fila", userVoiceChannel.getName());
            }

            if (event.getChannel() instanceof TextChannel) {
                musicManager.getScheduler().setTextChannel((TextChannel) event.getChannel());
            }

            if (musicManager.getPlayer().getPlayingTrack() == null && !musicManager.getScheduler().getQueue().isEmpty()) {
                musicManager.getScheduler().nextTrack();
                logger.info("Iniciando reprodução da fila restaurada no servidor: {}", guildId);
            }

            event.editMessageEmbeds(
                    EmbedFactory.success(
                            Translation.t(guildId, "previousqueue_restored_title"),
                            Translation.t(guildId, "previousqueue_restored_desc", restored, snapshot.getDescription())
                    )
            ).setComponents().queue();

            logger.info("Fila restaurada no servidor: {} ({} músicas de '{}')", guildId, restored, snapshot.getDescription());
        } catch (NumberFormatException e) {
            logger.error("Índice de snapshot inválido: {}", parts[1], e);
            event.reply(Translation.t(guildId, "button_error"))
                    .setEphemeral(true)
                    .queue();
        }
    }
}
