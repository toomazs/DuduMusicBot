package com.dudumusic.listeners;

import com.dudumusic.audio.MusicManager;
import com.dudumusic.commands.info.QueueCommand;
import com.dudumusic.core.Translation;
import com.dudumusic.utils.EmbedFactory;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
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
                case "prev" -> handlePreviousPage(event, parts);
                case "next" -> handleNextPage(event, parts);
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

    private void updateQueuePage(ButtonInteractionEvent event, int page) {
        long guildId = event.getGuild().getIdLong();
        MusicManager musicManager = MusicManager.getManager(guildId);

        var currentTrack = musicManager.getPlayer().getPlayingTrack();
        var queue = musicManager.getScheduler().getQueue();

        MessageEmbed embed = QueueCommand.createQueueEmbed(currentTrack, queue, page, guildId);

        int totalPages = (int) Math.ceil((double) queue.size() / TRACKS_PER_PAGE);

        Button prevButton = Button.secondary("queue:prev:" + page, "<<")
                .withDisabled(page == 0);
        Button nextButton = Button.secondary("queue:next:" + page, ">>")
                .withDisabled(page >= totalPages - 1);
        Button clearButton = Button.danger("queue:clear", Translation.t(guildId, "queue_btn_clear"));

        event.editMessageEmbeds(embed)
                .setActionRow(prevButton, nextButton, clearButton)
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
}
