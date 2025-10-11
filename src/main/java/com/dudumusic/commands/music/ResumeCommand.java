package com.dudumusic.commands.music;

import com.dudumusic.audio.MusicManager;
import com.dudumusic.commands.Command;
import com.dudumusic.utils.EmbedFactory;
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
        return "Despausa a música atual";
    }

    @Override
    public List<OptionData> getOptions() {
        return Collections.emptyList();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        long guildId = event.getGuild().getIdLong();
        MusicManager musicManager = MusicManager.getManager(guildId);

        if (musicManager.getPlayer().getPlayingTrack() == null) {
            event.replyEmbeds(
                    EmbedFactory.error("Nada tocando", "Não há nenhuma música tocando no momento")
            ).queue();
            return;
        }

        if (!musicManager.getPlayer().isPaused()) {
            event.replyEmbeds(
                    EmbedFactory.warning("Não pausado", "A reprodução não está pausada")
            ).queue();
            return;
        }

        musicManager.getPlayer().setPaused(false);

        event.replyEmbeds(
                EmbedFactory.success("Retomado", "Reprodução retomada")
        ).queue();

        logger.info("Reprodução retomada no servidor: {}", guildId);
    }
}
