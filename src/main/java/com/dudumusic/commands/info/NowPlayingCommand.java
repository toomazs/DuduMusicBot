package com.dudumusic.commands.info;

import com.dudumusic.audio.MusicManager;
import com.dudumusic.commands.Command;
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
        return "Mostra a música que está tocando";
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
                    EmbedFactory.error("Nada tocando", "Não há nenhuma música tocando no momento")
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

        EmbedBuilder builder = EmbedFactory.withRequester(event.getUser())
                .setTitle("Tocando agora")
                .setDescription(String.format("**[%s](%s)**", info.title, info.uri))
                .addField("Artista", info.author, true)
                .addField("Duração", TimeFormat.format(duration), true)
                .addField("Volume", musicManager.getPlayer().getVolume() + "%", true);

        if (track.getInfo().isStream) {
            builder.addField("Progresso", "TRANSMISSAO AO VIVO", false);
        } else {
            builder.addField("Progresso",
                    progressBar + "\n" + timeDisplay,
                    false);
        }

        if (info.artworkUrl != null) {
            builder.setThumbnail(info.artworkUrl);
        }

        var loopMode = musicManager.getScheduler().getLoopMode();
        String loopText = switch (loopMode) {
            case TRACK -> "Musica";
            case QUEUE -> "Fila";
            case OFF -> "Desligado";
        };
        builder.addField("Modo de repetição", loopText, true);

        if (musicManager.getPlayer().isPaused()) {
            builder.addField("Status", "Pausado", true);
        } else {
            builder.addField("Status", "Tocando", true);
        }

        int queueSize = musicManager.getScheduler().getQueue().size();
        builder.addField("Músicas na fila", String.valueOf(queueSize), true);

        event.replyEmbeds(builder.build()).queue();

        logger.info("Tocando agora exibido para o servidor: {} - Musica: {}", guildId, info.title);
    }
}
