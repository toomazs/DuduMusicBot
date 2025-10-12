package com.dudumusic.commands.music;

import com.dudumusic.audio.MusicManager;
import com.dudumusic.audio.TrackScheduler;
import com.dudumusic.commands.Command;
import com.dudumusic.core.Translation;
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
        return "Define modo de repetição";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "mode", "Modo de repetição", true)
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
            case TRACK -> Translation.t(guildId, "loop_mode_track");
            case QUEUE -> Translation.t(guildId, "loop_mode_queue");
            case OFF -> Translation.t(guildId, "loop_mode_off");
        };

        String description = switch (mode) {
            case TRACK -> Translation.t(guildId, "loop_desc_track");
            case QUEUE -> Translation.t(guildId, "loop_desc_queue");
            case OFF -> Translation.t(guildId, "loop_desc_off");
        };

        event.replyEmbeds(
                EmbedFactory.success(
                        Translation.t(guildId, "loop_title", modeText),
                        description
                )
        ).queue();

        logger.info("Modo de repetição definido para {} no servidor: {}", mode, guildId);
    }
}
