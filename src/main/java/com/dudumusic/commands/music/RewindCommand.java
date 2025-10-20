package com.dudumusic.commands.music;

import com.dudumusic.audio.MusicManager;
import com.dudumusic.core.Translation;
import com.dudumusic.utils.EmbedFactory;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import com.dudumusic.utils.VoiceValidator;

import java.util.List;

public class RewindCommand implements com.dudumusic.commands.Command {

    @Override
    public String getName() {
        return "rewind";
    }

    @Override
    public String getDescription() {
        return Translation.t(0L, "cmd_rewind_desc");
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        long gid = event.getGuild().getIdLong();

        if (!VoiceValidator.validate(event, true)) {
            return;
        }

        MusicManager mgr = MusicManager.getManager(gid);
        if (mgr == null || mgr.getScheduler() == null) {
            event.replyEmbeds(
                    EmbedFactory.error(
                            Translation.t(gid, "rewind_no_manager_title"),
                            Translation.t(gid, "rewind_no_manager_desc")
                    )
            ).queue();
            return;
        }

        var scheduler = mgr.getScheduler();
        var result = scheduler.rewind();

        if (result == null) {
            event.replyEmbeds(
                    EmbedFactory.error(
                            Translation.t(gid, "rewind_no_previous_title"),
                            Translation.t(gid, "rewind_no_previous_desc")
                    )
            ).queue();
            return;
        }

        event.replyEmbeds(
                EmbedFactory.success(
                        Translation.t(gid, "rewind_title"),
                        Translation.t(gid, "rewind_desc", result.getInfo().title)
                )
        ).queue();
    }
}
