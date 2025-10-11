package com.dudumusic.commands.music;

import com.dudumusic.audio.MusicManager;
import com.dudumusic.audio.TrackScheduler;
import com.dudumusic.commands.Command;
import com.dudumusic.utils.EmbedFactory;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class LoopCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(LoopCommand.class);

    @Override
    public String getName() {
        return "loop";
    }

    @Override
    public String getDescription() {
        return "Definir modo de repeticao";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "mode", "Modo de repeticao", true)
                        .addChoice("Desligado", "off")
                        .addChoice("Musica", "track")
                        .addChoice("Fila", "queue")
        );
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        long guildId = event.getGuild().getIdLong();
        MusicManager musicManager = MusicManager.getManager(guildId);

        String modeStr = event.getOption("mode").getAsString();
        TrackScheduler.LoopMode mode = switch (modeStr.toLowerCase()) {
            case "track" -> TrackScheduler.LoopMode.TRACK;
            case "queue" -> TrackScheduler.LoopMode.QUEUE;
            default -> TrackScheduler.LoopMode.OFF;
        };

        musicManager.getScheduler().setLoopMode(mode);

        String modeText = switch (mode) {
            case TRACK -> "Musica";
            case QUEUE -> "Fila";
            case OFF -> "Desligado";
        };

        String description = switch (mode) {
            case TRACK -> "A musica atual sera repetida";
            case QUEUE -> "A fila sera repetida";
            case OFF -> "Repeticao desativada";
        };

        event.replyEmbeds(
                EmbedFactory.success("Modo de repeticao: " + modeText, description)
        ).queue();

        logger.info("Modo de repeticao definido para {} no servidor: {}", mode, guildId);
    }
}
