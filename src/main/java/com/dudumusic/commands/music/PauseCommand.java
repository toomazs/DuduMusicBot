package com.dudumusic.commands.music;

import com.dudumusic.audio.MusicManager;
import com.dudumusic.commands.Command;
import com.dudumusic.core.Translation;
import com.dudumusic.utils.EmbedFactory;
import com.dudumusic.utils.VoiceValidator;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class PauseCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(PauseCommand.class);

    @Override
    public String getName() {
        return "pause";
    }

    @Override
    public String getDescription() {
        return Translation.t(0L, "cmd_pause_desc");
    }

    @Override
    public List<OptionData> getOptions() {
        return Collections.emptyList();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        long guildId = event.getGuild().getIdLong();

        if (!VoiceValidator.validate(event, true)) {
            return;
        }

        MusicManager musicManager = MusicManager.getManager(guildId);

        if (musicManager.getPlayer().getPlayingTrack() == null) {
            event.replyEmbeds(
                    EmbedFactory.error(
                            Translation.t(guildId, "pause_nothing_playing"),
                            Translation.t(guildId, "pause_no_track")
                    )
            ).queue();
            return;
        }

        if (musicManager.getPlayer().isPaused()) {
            event.replyEmbeds(
                    EmbedFactory.warning(
                            Translation.t(guildId, "pause_already_paused_title"),
                            Translation.t(guildId, "pause_already_paused_desc")
                    )
            ).queue();
            return;
        }

        musicManager.getPlayer().setPaused(true);

        event.replyEmbeds(
                EmbedFactory.success(
                        Translation.t(guildId, "pause_title"),
                        Translation.t(guildId, "pause_desc")
                )
        ).queue();

        logger.info("Reprodução pausada no servidor: {}", guildId);
    }
}
