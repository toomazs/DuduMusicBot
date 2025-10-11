package com.dudumusic;

import com.dudumusic.core.BotConfig;
import com.dudumusic.core.PlayerConfig;
import com.dudumusic.commands.Command;
import com.dudumusic.commands.music.*;
import com.dudumusic.commands.info.*;
import com.dudumusic.listeners.CommandListener;
import com.dudumusic.listeners.ButtonListener;
import com.dudumusic.listeners.VoiceListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final List<Command> commands = new ArrayList<>();

    public static void main(String[] args) {
        try {

            logger.info("Iniciando bot...");

            logger.info("1 - Carregando configurações");
            BotConfig.load();

            logger.info("2 - Configurando audio-player");
            PlayerConfig.setup();

            logger.info("3 - Buildando JDA");
            JDA jda = JDABuilder.createDefault(BotConfig.getToken())
                    .enableIntents(
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.GUILD_VOICE_STATES
                    )
                    .setActivity(Activity.customStatus("feito por @tomazdudux"))
                    .addEventListeners(
                            new CommandListener(),
                            new ButtonListener(),
                            new VoiceListener()
                    )
                    .build()
                    .awaitReady();

            logger.info("JDA pronto!");

            logger.info("4 - Registrando comandos slash");
            registerCommands(jda);

        } catch (Exception e) {
            logger.error("Erro ao inicializar o bot", e);
            System.exit(1);
        }
    }

    private static void registerCommands(JDA jda) {
        commands.add(new PlayCommand());
        commands.add(new StopCommand());
        commands.add(new SkipCommand());
        commands.add(new PauseCommand());
        commands.add(new ResumeCommand());
        commands.add(new VolumeCommand());
        commands.add(new LoopCommand());
        commands.add(new ShuffleCommand());
        commands.add(new SeekCommand());
        commands.add(new ClearCommand());

        commands.add(new QueueCommand());
        commands.add(new NowPlayingCommand());

        for (Command command : commands) {
            jda.upsertCommand(command.getName(), command.getDescription())
                    .addOptions(command.getOptions())
                    .queue(
                            success -> logger.info("Comando registrado: /{}", command.getName()),
                            error -> logger.error("Falha ao registrar comando: /{}: {}", command.getName(), error.getMessage())
                    );

        }
    }

    public static List<Command> getCommands() {
        return commands;
    }
}