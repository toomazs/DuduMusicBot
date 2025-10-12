package com.dudumusic.commands.info;

import com.dudumusic.core.LanguageManager;
import com.dudumusic.core.Translation;
import com.dudumusic.utils.EmbedFactory;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public class LanguageCommand implements com.dudumusic.commands.Command {

    @Override
    public String getName() {
        return "language";
    }

    @Override
    public String getDescription() {
        return Translation.t(0L, "cmd_language_desc");
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "lang", "ptbr, en, es", true)
                        .addChoice("Português (BR)", "ptbr")
                        .addChoice("English", "en")
                        .addChoice("Español", "es")
        );
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String l = event.getOption("lang").getAsString().toLowerCase();
        long gid = event.getGuild().getIdLong();

        if (!l.equals("ptbr") && !l.equals("en") && !l.equals("es")) {
            event.replyEmbeds(
                    EmbedFactory.error(
                            Translation.t(gid, "language_invalid"),
                            ""
                    )
            ).setEphemeral(true).queue();
            return;
        }

        LanguageManager.setLanguage(gid, l);

        String languageName = switch (l) {
            case "ptbr" -> "Português (BR)";
            case "en" -> "English";
            case "es" -> "Español";
            default -> l;
        };

        event.replyEmbeds(
                EmbedFactory.success(
                        Translation.t(gid, "language_set", languageName),
                        ""
                )
        ).queue();
    }
}