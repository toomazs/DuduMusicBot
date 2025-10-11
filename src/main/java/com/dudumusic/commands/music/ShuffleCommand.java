package com.dudumusic.commands.music;

import com.dudumusic.audio.MusicManager;
import com.dudumusic.commands.Command;
import com.dudumusic.utils.EmbedFactory;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class ShuffleCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(ShuffleCommand.class);

    @Override
    public String getName() {
        return "shuffle";
    }

    @Override
    public String getDescription() {
        return "Shuffle the queue";
    }

    @Override
    public List<OptionData> getOptions() {
        return Collections.emptyList();
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) {
        long guildId = event.getGuild().getIdLong();
        MusicManager musicManager = MusicManager.getManager(guildId);

        if (musicManager.getScheduler().getQueue().isEmpty()) {
            event.replyEmbeds(
                    EmbedFactory.error("Fila vazia", "A fila está vazia")
            ).queue();
            return;
        }

        int size = musicManager.getScheduler().getQueue().size();
        musicManager.getScheduler().shuffle();

        event.replyEmbeds(
                EmbedFactory.success("Embaralhado", "Embaralhou " + size + " músicas")
        ).queue();

        logger.info("Fila embaralhada no servidor: {} ({} músicas)", guildId, size);
    }
}
