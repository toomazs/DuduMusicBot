package com.dudumusic.commands.music;

import com.dudumusic.audio.MusicManager;
import com.dudumusic.commands.Command;
import com.dudumusic.core.Translation;
import com.dudumusic.utils.EmbedFactory;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class SkipCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(SkipCommand.class);

    @Override
    public String getName() {
        return "skip";
    }

    @Override
    public String getDescription() {
        return Translation.t(0L, "cmd_skip_desc");
    }

    @Override
    public List<OptionData> getOptions() {
        return Collections.emptyList();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        long guildId = event.getGuild().getIdLong();
        MusicManager musicManager = MusicManager.getManager(guildId);

        if (musicManager.getPlayer().getPlayingTrack() == null) {
            event.replyEmbeds(
                    EmbedFactory.error(
                            Translation.t(guildId, "skip_nothing_playing"),
                            Translation.t(guildId, "skip_no_track")
                    )
            ).queue();
            return;
        }

        String skippedTitle = musicManager.getPlayer().getPlayingTrack().getInfo().title;
        musicManager.getScheduler().nextTrack();

        event.replyEmbeds(
                EmbedFactory.success(
                        Translation.t(guildId, "skip_title"),
                        Translation.t(guildId, "skip_desc", skippedTitle)
                )
        ).queue();

        logger.info("Música pulada: {}", skippedTitle);
    }
}
