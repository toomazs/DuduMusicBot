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

public class ResumeCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(ResumeCommand.class);

    @Override
    public String getName() {
        return "resume";
    }

    @Override
    public String getDescription() {
        return Translation.t(0L, "cmd_resume_desc");
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
                            Translation.t(guildId, "resume_nothing_playing"),
                            Translation.t(guildId, "resume_no_track")
                    )
            ).queue();
            return;
        }

        if (!musicManager.getPlayer().isPaused()) {
            event.replyEmbeds(
                    EmbedFactory.warning(
                            Translation.t(guildId, "resume_not_paused_title"),
                            Translation.t(guildId, "resume_not_paused_desc")
                    )
            ).queue();
            return;
        }

        musicManager.getPlayer().setPaused(false);

        event.replyEmbeds(
                EmbedFactory.success(
                        Translation.t(guildId, "resume_title"),
                        Translation.t(guildId, "resume_desc")
                )
        ).queue();

        logger.info("Reprodução retomada no servidor: {}", guildId);
    }
}
