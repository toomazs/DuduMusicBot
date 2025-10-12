package com.dudumusic.commands.info;

import com.dudumusic.audio.MusicManager;
import com.dudumusic.commands.Command;
import com.dudumusic.core.Translation;
import com.dudumusic.utils.EmbedFactory;
import com.dudumusic.utils.ProgressBar;
import com.dudumusic.utils.TimeFormat;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class NowPlayingCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(NowPlayingCommand.class);

    @Override
    public String getName() {
        return "nowplaying";
    }

    @Override
    public String getDescription() {
        return Translation.t(0L, "cmd_nowplaying_desc");
    }

    @Override
    public List<OptionData> getOptions() {
        return Collections.emptyList();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        long guildId = event.getGuild().getIdLong();
        MusicManager musicManager = MusicManager.getManager(guildId);

        AudioTrack track = musicManager.getPlayer().getPlayingTrack();

        if (track == null) {
            event.replyEmbeds(
                    EmbedFactory.error(
                            Translation.t(guildId, "nowplaying_nothing_title"),
                            Translation.t(guildId, "nowplaying_nothing_desc")
                    )
            ).queue();
            return;
        }

        AudioTrackInfo info = track.getInfo();
        long position = track.getPosition();
        long duration = track.getDuration();

        String progressBar = ProgressBar.create(position, duration, 20);
        String timeDisplay = String.format("%s / %s",
                TimeFormat.format(position),
                TimeFormat.format(duration));

        EmbedBuilder builder = EmbedFactory.withRequester(event.getUser(), guildId)
                .setTitle(Translation.t(guildId, "nowplaying_title"))
                .setDescription(String.format("**[%s](%s)**", info.title, info.uri))
                .addField(Translation.t(guildId, "nowplaying_artist"), info.author, true)
                .addField(Translation.t(guildId, "nowplaying_duration"), TimeFormat.format(duration), true)
                .addField(Translation.t(guildId, "nowplaying_volume"), musicManager.getPlayer().getVolume() + "%", true);

        if (track.getInfo().isStream) {
            builder.addField(Translation.t(guildId, "nowplaying_progress"),
                    Translation.t(guildId, "nowplaying_live"),
                    false);
        } else {
            builder.addField(Translation.t(guildId, "nowplaying_progress"),
                    progressBar + "\n" + timeDisplay,
                    false);
        }

        if (info.artworkUrl != null) {
            builder.setThumbnail(info.artworkUrl);
        }

        var loopMode = musicManager.getScheduler().getLoopMode();
        String loopText = switch (loopMode) {
            case TRACK -> Translation.t(guildId, "loop_mode_track");
            case QUEUE -> Translation.t(guildId, "loop_mode_queue");
            case OFF -> Translation.t(guildId, "loop_mode_off");
        };
        builder.addField(Translation.t(guildId, "nowplaying_loop_mode"), loopText, true);

        if (musicManager.getPlayer().isPaused()) {
            builder.addField(Translation.t(guildId, "nowplaying_status"),
                    Translation.t(guildId, "nowplaying_status_paused"),
                    true);
        } else {
            builder.addField(Translation.t(guildId, "nowplaying_status"),
                    Translation.t(guildId, "nowplaying_status_playing"),
                    true);
        }

        int queueSize = musicManager.getScheduler().getQueue().size();
        builder.addField(Translation.t(guildId, "nowplaying_queue_size"), String.valueOf(queueSize), true);

        event.replyEmbeds(builder.build()).queue();

        logger.info("Tocando agora exibido para o servidor: {} - Musica: {}", guildId, info.title);
    }
}
