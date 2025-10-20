package com.dudumusic.commands.music;

import com.dudumusic.audio.MusicManager;
import com.dudumusic.core.Translation;
import com.dudumusic.utils.EmbedFactory;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import com.dudumusic.utils.VoiceValidator;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public class EraseCommand implements com.dudumusic.commands.Command {

    @Override
    public String getName() {
        return "erase";
    }

    @Override
    public String getDescription() {
        return Translation.t(0L, "cmd_erase_desc");
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(new OptionData(OptionType.INTEGER, "position", "Posição da música na fila para remover", true));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        long gid = event.getGuild().getIdLong();

        if (!VoiceValidator.validate(event, true)) {
            return;
        }

        int pos = (int) event.getOption("position").getAsLong();
        MusicManager mgr = MusicManager.getManager(gid);
        if (mgr == null || mgr.getScheduler() == null) {
            event.replyEmbeds(
                    EmbedFactory.error(
                            Translation.t(gid, "erase_no_manager_title"),
                            Translation.t(gid, "erase_no_manager_desc")
                    )
            ).queue();
            return;
        }

        if (pos <= 0) {
            event.replyEmbeds(
                    EmbedFactory.error(
                            Translation.t(gid, "erase_invalid_title"),
                            Translation.t(gid, "erase_invalid_desc")
                    )
            ).queue();
            return;
        }

        var scheduler = mgr.getScheduler();
        var result = scheduler.removeTrack(pos);
        if (result == null) {
            event.replyEmbeds(
                    EmbedFactory.error(
                            Translation.t(gid, "erase_invalid_title"),
                            Translation.t(gid, "erase_not_found")
                    )
            ).queue();
            return;
        }

        event.replyEmbeds(
                EmbedFactory.success(
                        Translation.t(gid, "erase_title"),
                        Translation.t(gid, "erase_desc", result.getInfo().title, pos)
                )
        ).queue();
    }
}
