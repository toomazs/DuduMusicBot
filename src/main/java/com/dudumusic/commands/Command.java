package com.dudumusic.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public interface Command {

    String getName();

    String getDescription();

    default String getDescriptionKey() {
        return "cmd_" + getName() + "_desc";
    }

    List<OptionData> getOptions();

    void execute(SlashCommandInteractionEvent event);
}
