package com.dudumusic.listeners;

import com.dudumusic.audio.MusicManager;
import com.dudumusic.commands.info.QueueCommand;
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
                default -> logger.warn("Acao de botao desconhecida: {}", action);
            }
        } catch (Exception e) {
            logger.error("Erro ao processar interacao de botao: {}", buttonId, e);
            event.reply("Ocorreu um erro ao processar sua solicitacao")
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
            event.reply("Ja esta na primeira pagina!")
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
            event.reply("Ja esta na ultima pagina!")
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

        MessageEmbed embed = QueueCommand.createQueueEmbed(currentTrack, queue, page);

        int totalPages = (int) Math.ceil((double) queue.size() / TRACKS_PER_PAGE);

        Button prevButton = Button.secondary("queue:prev:" + page, "Anterior")
                .withDisabled(page == 0);
        Button nextButton = Button.secondary("queue:next:" + page, "Proximo")
                .withDisabled(page >= totalPages - 1);
        Button clearButton = Button.danger("queue:clear", "Limpar fila");

        // Update message
        event.editMessageEmbeds(embed)
                .setActionRow(prevButton, nextButton, clearButton)
                .queue();

        logger.info("Pagina da fila atualizada para {} no servidor: {}", page, guildId);
    }

    private void handleClearQueue(ButtonInteractionEvent event) {
        long guildId = event.getGuild().getIdLong();
        MusicManager musicManager = MusicManager.getManager(guildId);

        if (musicManager.getScheduler().getQueue().isEmpty()) {
            event.reply("A fila ja esta vazia!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        int size = musicManager.getScheduler().getQueue().size();
        musicManager.getScheduler().clearQueue();

        event.editMessageEmbeds(
                EmbedFactory.success("Fila limpa", "Removidas " + size + " musicas da fila")
        ).setComponents().queue();

        logger.info("Fila limpa via botao no servidor: {} ({} musicas)", guildId, size);
    }
}
