package com.dudumusic.commands.music;

import com.dudumusic.audio.MusicManager;
import com.dudumusic.commands.Command;
import com.dudumusic.utils.EmbedFactory;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class VolumeCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(VolumeCommand.class);

    @Override
    public String getName() {
        return "volume";
    }

    @Override
    public String getDescription() {
        return "Ajusta o volume de reprodução (0-150)";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.INTEGER, "level", "Nivel de volume (0-150)", true)
                        .setMinValue(0)
                        .setMaxValue(150)
        );
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        long guildId = event.getGuild().getIdLong();
        MusicManager musicManager = MusicManager.getManager(guildId);

        int volume = event.getOption("level").getAsInt();
        musicManager.getPlayer().setVolume(volume);

        String volumeBar = createVolumeBar(volume);

        event.replyEmbeds(
                EmbedFactory.success("Volume ajustado",
                        String.format("Volume definido para **%d%%**\n%s", volume, volumeBar))
        ).queue();

        logger.info("Volume definido para {} no servidor: {}", volume, guildId);
    }

    private String createVolumeBar(int volume) {
        int bars = volume * 20 / 150;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            if (i < bars) {
                sb.append("▰");
            } else {
                sb.append("▱");
            }
        }
        return sb.toString();
    }
}
