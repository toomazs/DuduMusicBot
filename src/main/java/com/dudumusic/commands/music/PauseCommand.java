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

public class PauseCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(PauseCommand.class);

    @Override
    public String getName() {
        return "pause";
    }

    @Override
    public String getDescription() {
        return "Pausa a música atual";
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

        if (musicManager.getPlayer().isPaused()) {
            event.replyEmbeds(
                    EmbedFactory.warning("Já pausado", "A reprodução já está pausada")
            ).queue();
            return;
        }

        musicManager.getPlayer().setPaused(true);

        event.replyEmbeds(
                EmbedFactory.success("Pausado", "Reprodução pausada")
        ).queue();

        logger.info("Reprodução pausada no servidor: {}", guildId);
    }
}
