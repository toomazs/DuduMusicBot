package com.dudumusic.commands.music;

import com.dudumusic.audio.MusicManager;
import com.dudumusic.commands.Command;
import com.dudumusic.core.Translation;
import com.dudumusic.utils.EmbedFactory;
import com.dudumusic.utils.ProgressBar;
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
        return Translation.t(0L, "cmd_volume_desc");
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

        String volumeBar = ProgressBar.createVolumeBar(volume);

        event.replyEmbeds(
                EmbedFactory.success(
                        Translation.t(guildId, "volume_title"),
                        Translation.t(guildId, "volume_desc", volume, volumeBar)
                )
        ).queue();

        logger.info("Volume definido para {} no servidor: {}", volume, guildId);
    }
}
