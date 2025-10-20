package com.dudumusic.commands.music;

import com.dudumusic.audio.MusicManager;
import com.dudumusic.commands.Command;
import com.dudumusic.core.Translation;
import com.dudumusic.utils.EmbedFactory;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import com.dudumusic.utils.VoiceValidator;
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
        return Translation.t(0L, "cmd_shuffle_desc");
    }

    @Override
    public List<OptionData> getOptions() {
        return Collections.emptyList();
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) {
        long guildId = event.getGuild().getIdLong();

        if (!VoiceValidator.validate(event, true)) {
            return;
        }

        MusicManager musicManager = MusicManager.getManager(guildId);

        if (musicManager.getScheduler().getQueue().isEmpty()) {
            event.replyEmbeds(
                    EmbedFactory.error(
                            Translation.t(guildId, "shuffle_empty_title"),
                            Translation.t(guildId, "shuffle_empty_desc")
                    )
            ).queue();
            return;
        }

        int size = musicManager.getScheduler().getQueue().size();
        musicManager.getScheduler().shuffle();

        event.replyEmbeds(
                EmbedFactory.success(
                        Translation.t(guildId, "shuffle_title"),
                        Translation.t(guildId, "shuffle_desc", size)
                )
        ).queue();

        logger.info("Fila embaralhada no servidor: {} ({} músicas)", guildId, size);
    }
}
