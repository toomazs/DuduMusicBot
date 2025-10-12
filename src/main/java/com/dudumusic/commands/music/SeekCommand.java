package com.dudumusic.commands.music;

import com.dudumusic.audio.MusicManager;
import com.dudumusic.commands.Command;
import com.dudumusic.core.Translation;
import com.dudumusic.utils.EmbedFactory;
import com.dudumusic.utils.TimeFormat;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SeekCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(SeekCommand.class);

    @Override
    public String getName() {
        return "seek";
    }

    @Override
    public String getDescription() {
        return Translation.t(0L, "cmd_seek_desc");
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.INTEGER, "seconds", "Tempo em segundos", true)
                        .setMinValue(0)
        );
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        long guildId = event.getGuild().getIdLong();
        MusicManager musicManager = MusicManager.getManager(guildId);

        var track = musicManager.getPlayer().getPlayingTrack();

        if (track == null) {
            event.replyEmbeds(
                    EmbedFactory.error(
                            Translation.t(guildId, "seek_nothing_playing"),
                            Translation.t(guildId, "seek_no_track")
                    )
            ).queue();
            return;
        }

        if (!track.isSeekable()) {
            event.replyEmbeds(
                    EmbedFactory.error(
                            Translation.t(guildId, "seek_not_seekable_title"),
                            Translation.t(guildId, "seek_not_seekable_desc")
                    )
            ).queue();
            return;
        }

        int seconds = event.getOption("seconds").getAsInt();
        long positionMs = seconds * 1000L;

        if (positionMs > track.getDuration()) {
            event.replyEmbeds(
                    EmbedFactory.error(
                            Translation.t(guildId, "seek_invalid_title"),
                            Translation.t(guildId, "seek_invalid_desc", TimeFormat.format(track.getDuration()))
                    )
            ).queue();
            return;
        }

        track.setPosition(positionMs);

        event.replyEmbeds(
                EmbedFactory.success(
                        Translation.t(guildId, "seek_title"),
                        Translation.t(guildId, "seek_desc", TimeFormat.format(positionMs))
                )
        ).queue();

        logger.info("Posição alterada para {} no servidor: {}", TimeFormat.format(positionMs), guildId);
    }
}
