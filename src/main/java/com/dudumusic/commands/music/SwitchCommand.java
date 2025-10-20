package com.dudumusic.commands.music;

import com.dudumusic.audio.MusicManager;
import com.dudumusic.core.Translation;
import com.dudumusic.utils.EmbedFactory;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import com.dudumusic.utils.VoiceValidator;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public class SwitchCommand implements com.dudumusic.commands.Command {

    @Override
    public String getName() {
        return "switch";
    }

    @Override
    public String getDescription() {
        return Translation.t(0L, "cmd_switch_desc");
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.INTEGER, "from", "Posição atual da música na fila", true),
                new OptionData(OptionType.INTEGER, "to", "Nova posição desejada na fila", true)
        );
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        long gid = event.getGuild().getIdLong();

        if (!VoiceValidator.validate(event, true)) {
            return;
        }

        int fromPos = (int) event.getOption("from").getAsLong();
        int toPos = (int) event.getOption("to").getAsLong();
        MusicManager mgr = MusicManager.getManager(gid);
        if (mgr == null || mgr.getScheduler() == null) {
            event.replyEmbeds(
                    EmbedFactory.error(
                            Translation.t(gid, "switch_no_manager_title"),
                            Translation.t(gid, "switch_no_manager_desc")
                    )
            ).queue();
            return;
        }

        if (fromPos <= 0 || toPos <= 0) {
            event.replyEmbeds(
                    EmbedFactory.error(
                            Translation.t(gid, "switch_invalid_title"),
                            Translation.t(gid, "switch_invalid_desc")
                    )
            ).queue();
            return;
        }

        if (fromPos == toPos) {
            event.replyEmbeds(
                    EmbedFactory.error(
                            Translation.t(gid, "switch_same_position_title"),
                            Translation.t(gid, "switch_same_position_desc")
                    )
            ).queue();
            return;
        }

        var scheduler = mgr.getScheduler();
        var result = scheduler.switchTracks(fromPos, toPos);
        if (result == null) {
            event.replyEmbeds(
                    EmbedFactory.error(
                            Translation.t(gid, "switch_invalid_title"),
                            Translation.t(gid, "switch_invalid_queue")
                    )
            ).queue();
            return;
        }

        event.replyEmbeds(
                EmbedFactory.success(
                        Translation.t(gid, "switch_title"),
                        Translation.t(gid, "switch_desc", result.getInfo().title, fromPos, toPos)
                )
        ).queue();
    }
}
