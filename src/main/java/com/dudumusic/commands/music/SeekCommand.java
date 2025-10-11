package com.dudumusic.commands.music;

import com.dudumusic.audio.MusicManager;
import com.dudumusic.commands.Command;
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
        return "Pula para uma posição específica na música (em segundos)";
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
                    EmbedFactory.error("Nada tocando", "Não há nenhuma música tocando no momento")
            ).queue();
            return;
        }

        if (!track.isSeekable()) {
            event.replyEmbeds(
                    EmbedFactory.error("Não é possível pular", "Esta música não permite pular posições (ex: Transmissão Ao Vivo)")
            ).queue();
            return;
        }

        int seconds = event.getOption("seconds").getAsInt();
        long positionMs = seconds * 1000L;

        if (positionMs > track.getDuration()) {
            event.replyEmbeds(
                    EmbedFactory.error("Posição inválida",
                            String.format("Posição excede a duração da música (%s)",
                                    TimeFormat.format(track.getDuration())))
            ).queue();
            return;
        }

        track.setPosition(positionMs);

        event.replyEmbeds(
                EmbedFactory.success("Posição alterada",
                        String.format("Pulou para **%s**", TimeFormat.format(positionMs)))
        ).queue();

        logger.info("Posição alterada para {} no servidor: {}", TimeFormat.format(positionMs), guildId);
    }
}
