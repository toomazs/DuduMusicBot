package com.dudumusic.commands.music;

import com.dudumusic.audio.MusicManager;
import com.dudumusic.commands.Command;
import com.dudumusic.core.Translation;
import com.dudumusic.utils.EmbedFactory;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class StopCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(StopCommand.class);

    @Override
    public String getName() {
        return "stop";
    }

    @Override
    public String getDescription() {
        return Translation.t(0L, "cmd_stop_desc");
    }

    @Override
    public List<OptionData> getOptions() {
        return Collections.emptyList();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        long guildId = event.getGuild().getIdLong();
        MusicManager musicManager = MusicManager.getManager(guildId);

        musicManager.getPlayer().stopTrack();
        musicManager.getScheduler().clearQueue();

        event.getGuild().getAudioManager().closeAudioConnection();

        event.replyEmbeds(
                EmbedFactory.success(
                        Translation.t(guildId, "stop_title"),
                        Translation.t(guildId, "stop_desc")
                )
        ).queue();

        logger.info("Reprodução parada no servidor: {}", guildId);
    }
}
