package com.dudumusic.commands.music;

import com.dudumusic.audio.MusicManager;
import com.dudumusic.core.Translation;
import com.dudumusic.utils.EmbedFactory;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public class JumpCommand implements com.dudumusic.commands.Command {

    @Override
    public String getName() {
        return "jump";
    }

    @Override
    public String getDescription() {
        return Translation.t(0L, "cmd_jump_desc");
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(new OptionData(OptionType.INTEGER, "position", "Posição na fila (1 = primeira da fila)", true));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        int pos = (int) event.getOption("position").getAsLong();
        long gid = event.getGuild().getIdLong();

        MusicManager mgr = MusicManager.getManager(gid);
        if (mgr == null || mgr.getScheduler() == null) {
            event.replyEmbeds(
                    EmbedFactory.error(
                            Translation.t(gid, "jump_invalid"),
                            Translation.t(gid, "jump_no_queue")
                    )
            ).queue();
            return;
        }

        if (pos <= 0) {
            event.replyEmbeds(
                    EmbedFactory.error(
                            Translation.t(gid, "jump_invalid"),
                            Translation.t(gid, "jump_no_queue")
                    )
            ).queue();
            return;
        }

        var scheduler = mgr.getScheduler();
        var result = scheduler.jumpTo(pos);
        if (result == null) {
            event.replyEmbeds(
                    EmbedFactory.error(
                            Translation.t(gid, "jump_invalid"),
                            Translation.t(gid, "jump_no_queue")
                    )
            ).queue();
            return;
        }

        event.replyEmbeds(
                EmbedFactory.success(
                        Translation.t(gid, "skip_title"),
                        Translation.t(gid, "jump_done", result.getInfo().title)
                )
        ).queue();
    }
}
