package com.dudumusic.listeners;

import com.dudumusic.Main;
import com.dudumusic.commands.Command;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandListener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(CommandListener.class);

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String commandName = event.getName();

        for (Command command : Main.getCommands()) {
            if (command.getName().equals(commandName)) {
                try {
                    logger.info("Executando comando: /{} pelo usuário: {} no servidor: {}",
                            commandName,
                            event.getUser().getAsTag(),
                            event.getGuild().getName());

                    command.execute(event);

                } catch (Exception e) {
                    logger.error("Erro ao executar comando: /{}", commandName, e);

                    try {
                        if (!event.isAcknowledged()) {
                            event.reply("Ocorreu um erro ao executar o comando: " + e.getMessage())
                                    .setEphemeral(true)
                                    .queue();
                        } else {
                            event.getHook().editOriginal("Ocorreu um erro: " + e.getMessage()).queue();
                        }
                    } catch (Exception ignored) {
                    }
                }
                return;
            }
        }

        logger.warn("Comando desconhecido: /{}", commandName);
        event.reply("Comando desconhecido!")
                .setEphemeral(true)
                .queue();
    }
}
