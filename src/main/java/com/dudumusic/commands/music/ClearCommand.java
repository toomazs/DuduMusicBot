package com.dudumusic.commands.music;

import com.dudumusic.audio.MusicManager;
import com.dudumusic.commands.Command;
import com.dudumusic.utils.EmbedFactory;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class ClearCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(ClearCommand.class);

    @Override
    public String getName() {
        return "clear";
    }

    @Override
    public String getDescription() {
        return "Limpa a fila de músicas";
    }

    @Override
    public List<OptionData> getOptions() {
        return Collections.emptyList();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        long guildId = event.getGuild().getIdLong();
        MusicManager musicManager = MusicManager.getManager(guildId);

        if (musicManager.getScheduler().getQueue().isEmpty()) {
            event.replyEmbeds(
                    EmbedFactory.error("Fila vazia", "A fila já está vazia")
            ).queue();
            return;
        }

        int size = musicManager.getScheduler().getQueue().size();
        musicManager.getScheduler().clearQueue();

        event.replyEmbeds(
                EmbedFactory.success("Fila limpa", "Removeu " + size + " músicas da fila")
        ).queue();

        logger.info("Fila limpa no servidor: {} ({} músicas removidas)", guildId, size);
    }
}
