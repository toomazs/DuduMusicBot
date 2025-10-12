package com.dudumusic.commands.info;

import com.dudumusic.Main;
import com.dudumusic.core.Translation;
import com.dudumusic.utils.EmbedFactory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public class HelpCommand implements com.dudumusic.commands.Command {
    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return Translation.t(0L, "cmd_help_desc");
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        long gid = event.getGuild().getIdLong();

        StringBuilder sb = new StringBuilder();
        for (var c : Main.getCommands()) {
            String translatedDesc = Translation.t(gid, c.getDescriptionKey());
            sb.append("`/").append(c.getName()).append("` - ").append(translatedDesc).append("\n");
        }

        EmbedBuilder embed = EmbedFactory.musicBuilder()
                .setTitle(Translation.t(gid, "help_title"))
                .setDescription(sb.toString())
                .setFooter(Translation.t(gid, "embed_requested_by", event.getUser().getName()),
                          event.getUser().getAvatarUrl());

        event.replyEmbeds(embed.build()).queue();
    }
}
