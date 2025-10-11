package com.dudumusic.commands.music;

import com.dudumusic.audio.MusicManager;
import com.dudumusic.commands.Command;
import com.dudumusic.core.PlayerConfig;
import com.dudumusic.utils.EmbedFactory;
import com.dudumusic.utils.SourceDetector;
import com.dudumusic.utils.TimeFormat;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.managers.AudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PlayCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(PlayCommand.class);

    @Override
    public String getName() {
        return "play";
    }

    @Override
    public String getDescription() {
        return "Play music from URL or search query";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "query", "URL or search query", true)
        );
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        Member member = event.getMember();
        if (member == null) {
            event.getHook().editOriginalEmbeds(
                    EmbedFactory.error("Erro", "Não foi possível encontrar informações do membro")
            ).queue();
            return;
        }

        GuildVoiceState voiceState = member.getVoiceState();
        if (voiceState == null || !voiceState.inAudioChannel()) {
            event.getHook().editOriginalEmbeds(
                    EmbedFactory.error("Não está em um canal de voz", "Você precisa estar em um canal de voz para tocar música!")
            ).queue();
            return;
        }

        String query = event.getOption("query").getAsString();

        SourceDetector.SourceType sourceType = SourceDetector.detect(query);
        logger.info("Requisição de reprodução - Fonte: {}, Query: {}", sourceType.getDisplayName(), query);

        if (sourceType == SourceDetector.SourceType.SEARCH) {
            query = SourceDetector.toYoutubeSearch(query);
        }

        long guildId = event.getGuild().getIdLong();
        MusicManager musicManager = MusicManager.getManager(guildId);

        AudioManager audioManager = event.getGuild().getAudioManager();
        if (!audioManager.isConnected()) {
            VoiceChannel voiceChannel = (VoiceChannel) voiceState.getChannel();
            audioManager.openAudioConnection(voiceChannel);
            audioManager.setSendingHandler(musicManager.getAudioHandler());
            logger.info("Conectado ao canal de voz: {}", voiceChannel.getName());
        }

        final String trackUrl = query;
        PlayerConfig.getInstance().loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                musicManager.getScheduler().queue(track);

                var info = track.getInfo();
                var embed = EmbedFactory.withRequester(event.getUser())
                        .setTitle("Adicionado à fila")
                        .setDescription(String.format("**[%s](%s)**", info.title, info.uri))
                        .addField("Canal", info.author, true)
                        .addField("Duração", TimeFormat.format(track.getDuration()), true)
                        .addField("Posição na fila", String.valueOf(musicManager.getScheduler().getQueue().size() + 1), true)
                        .setThumbnail(info.artworkUrl)
                        .build();

                event.getHook().editOriginalEmbeds(embed).queue();
                logger.info("Música carregada: {}", info.title);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                List<AudioTrack> tracks = playlist.getTracks();

                if (tracks.isEmpty()) {
                    event.getHook().editOriginalEmbeds(
                            EmbedFactory.error("Playlist vazia", "Esta playlist não tem músicas")
                    ).queue();
                    return;
                }

                int count = 0;
                for (AudioTrack track : tracks) {
                    if (musicManager.getScheduler().queue(track)) {
                        count++;
                    }
                }

                var embed = EmbedFactory.withRequester(event.getUser())
                        .setTitle("Playlist adicionada")
                        .setDescription(String.format("**%s**", playlist.getName()))
                        .addField("Músicas adicionadas", String.valueOf(count), true)
                        .addField("Duração total", TimeFormat.formatVerbose(
                                tracks.stream().mapToLong(AudioTrack::getDuration).sum()
                        ), true)
                        .build();

                event.getHook().editOriginalEmbeds(embed).queue();
                logger.info("Playlist carregada: {} ({} músicas)", playlist.getName(), count);
            }

            @Override
            public void noMatches() {
                event.getHook().editOriginalEmbeds(
                        EmbedFactory.error("Nenhum resultado", "Não foi possível encontrar nenhuma música com sua busca")
                ).queue();
                logger.warn("Nenhuma correspondência encontrada para: {}", trackUrl);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                event.getHook().editOriginalEmbeds(
                        EmbedFactory.error("Falha ao carregar", "Falha ao carregar música: " + exception.getMessage())
                ).queue();
                logger.error("Falha ao carregar música: {}", trackUrl, exception);
            }
        });
    }
}
