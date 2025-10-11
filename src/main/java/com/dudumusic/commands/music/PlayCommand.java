package com.dudumusic.commands.music;

import com.dudumusic.audio.MusicManager;
import com.dudumusic.commands.Command;
import com.dudumusic.core.PlayerConfig;
import com.dudumusic.utils.EmbedFactory;
import com.dudumusic.utils.MusicLinkConverter;
import com.dudumusic.utils.SourceDetector;
import com.dudumusic.utils.SpotifyApiClient;
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
        return "Toca músicas via URL ou via query de busca";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "query", "URL ou query de busca", true)
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

        String customArtworkUrl = null;
        long guildId = event.getGuild().getIdLong();
        MusicManager musicManager = MusicManager.getManager(guildId);

        if (event.getChannel() instanceof net.dv8tion.jda.api.entities.channel.concrete.TextChannel) {
            musicManager.setTextChannel((net.dv8tion.jda.api.entities.channel.concrete.TextChannel) event.getChannel());
        }

        AudioManager audioManager = event.getGuild().getAudioManager();
        if (!audioManager.isConnected()) {
            VoiceChannel voiceChannel = (VoiceChannel) voiceState.getChannel();
            audioManager.openAudioConnection(voiceChannel);
            audioManager.setSendingHandler(musicManager.getAudioHandler());
            logger.info("Conectado ao canal de voz: {}", voiceChannel.getName());
        }
        if (sourceType == SourceDetector.SourceType.DEEZER_PLAYLIST || sourceType == SourceDetector.SourceType.APPLE_MUSIC_PLAYLIST) {
            var playlistResult = MusicLinkConverter.convertPlaylistToYouTubeSearches(query, sourceType == SourceDetector.SourceType.DEEZER_PLAYLIST ? SourceDetector.SourceType.DEEZER : SourceDetector.SourceType.APPLE_MUSIC);
            if (playlistResult != null && !playlistResult.searchQueries.isEmpty()) {
                int limit = Math.min(playlistResult.searchQueries.size(), 200);
                final int[] added = {0};
                final MusicManager mgr = musicManager;

                java.util.concurrent.atomic.AtomicInteger idx = new java.util.concurrent.atomic.AtomicInteger(0);
                java.util.concurrent.atomic.AtomicInteger inFlight = new java.util.concurrent.atomic.AtomicInteger(0);
                java.util.concurrent.atomic.AtomicReference<java.util.function.Consumer<Void>> loaderRef = new java.util.concurrent.atomic.AtomicReference<>();

                Runnable checkFinish = () -> {
                    if (idx.get() >= limit && inFlight.get() == 0) {
                        var embedBuilder = EmbedFactory.withRequester(event.getUser())
                                .setTitle("Playlist adicionada")
                                .setDescription(String.format("**%s**", playlistResult.playlistName != null ? playlistResult.playlistName : "Playlist"))
                                .addField("Músicas adicionadas", String.valueOf(added[0]), true);

                        if (playlistResult.artworkUrl != null) {
                            embedBuilder.setThumbnail(playlistResult.artworkUrl);
                        }

                        event.getHook().editOriginalEmbeds(embedBuilder.build()).queue();
                    }
                };

                var initialEmbed = EmbedFactory.withRequester(event.getUser())
                        .setTitle("Adicionando playlist...")
                        .setDescription(String.format("**%s**", playlistResult.playlistName != null ? playlistResult.playlistName : "Playlist"))
                        .addField("Músicas totais", String.valueOf(playlistResult.searchQueries.size()), true);
                if (playlistResult.artworkUrl != null) initialEmbed.setThumbnail(playlistResult.artworkUrl);
                event.getHook().editOriginalEmbeds(initialEmbed.build()).queue();

                loaderRef.set((v) -> {
                    int i = idx.getAndIncrement();
                    if (i >= limit) {
                        checkFinish.run();
                        return;
                    }
                    String toLoad = playlistResult.searchQueries.get(i);
                    inFlight.incrementAndGet();

                    PlayerConfig.getInstance().loadItemOrdered(mgr, toLoad, new com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler() {
                        @Override
                        public void trackLoaded(AudioTrack track) {
                            mgr.getScheduler().queue(track);
                            added[0]++;
                            inFlight.decrementAndGet();
                            loaderRef.get().accept(null);
                            checkFinish.run();
                        }

                        @Override
                        public void playlistLoaded(AudioPlaylist playlist) {
                            if (!playlist.getTracks().isEmpty()) {
                                mgr.getScheduler().queue(playlist.getTracks().get(0));
                                added[0]++;
                            }
                            inFlight.decrementAndGet();
                            loaderRef.get().accept(null);
                            checkFinish.run();
                        }

                        @Override
                        public void noMatches() {
                            inFlight.decrementAndGet();
                            loaderRef.get().accept(null);
                            checkFinish.run();
                        }

                        @Override
                        public void loadFailed(com.sedmelluq.discord.lavaplayer.tools.FriendlyException exception) {
                            logger.warn("Falha ao carregar item da playlist convertida: {}", toLoad, exception);
                            inFlight.decrementAndGet();
                            loaderRef.get().accept(null);
                            checkFinish.run();
                        }
                    });
                });

                loaderRef.get().accept(null);
                return;
            } else {
                event.getHook().editOriginalEmbeds(
                        EmbedFactory.error("Não foi possível processar playlist", "Não consegui extrair músicas dessa playlist")
                ).queue();
                return;
            }
        } else if (MusicLinkConverter.needsConversion(sourceType)) {
            String convertedQuery = MusicLinkConverter.convertToYouTubeSearch(query, sourceType);
            if (convertedQuery != null) {
                logger.info("Link {} convertido para busca no YouTube: {}", sourceType.getDisplayName(), convertedQuery);
                query = convertedQuery;
                customArtworkUrl = MusicLinkConverter.getArtworkUrl(convertedQuery);
            } else {
                logger.warn("Não foi possível converter link {}, usando como busca de texto", sourceType.getDisplayName());
                query = SourceDetector.toYoutubeSearch(query);
            }
        } else if (sourceType == SourceDetector.SourceType.SEARCH) {
            query = SourceDetector.toYoutubeSearch(query);
        }

        final String trackUrl = query;
        final String finalArtworkUrl = customArtworkUrl;
        final SourceDetector.SourceType finalSourceType = sourceType;
        final String originalQuery = event.getOption("query").getAsString();
        loadWithRetry(event, musicManager, trackUrl, 0, finalArtworkUrl, finalSourceType, originalQuery);
    }

    private void loadWithRetry(SlashCommandInteractionEvent event, MusicManager musicManager, String trackUrl, int attemptCount, String customArtworkUrl, SourceDetector.SourceType sourceType, String originalQuery) {
        final int MAX_RETRIES = 3;
        final long RETRY_DELAY_MS = 1500;

        PlayerConfig.getInstance().loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                musicManager.getScheduler().queue(track);

                var info = track.getInfo();
                String artworkUrl = customArtworkUrl != null ? customArtworkUrl : info.artworkUrl;

                if (artworkUrl == null) {
                    if (info.uri != null && info.uri.contains("youtube")) {
                        artworkUrl = SourceDetector.getYouTubeThumbnail(info.uri);
                    }
                    if (artworkUrl == null && info.identifier != null) {
                        String videoId = info.identifier;
                        if (videoId.startsWith("youtube:")) {
                            videoId = videoId.substring(8);
                        }
                        if (videoId.length() == 11) {
                            artworkUrl = "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg";
                            logger.info("Usando thumbnail do YouTube para: {} (ID: {})", info.title, videoId);
                        }
                    }
                }

                if (customArtworkUrl != null && info.identifier != null) {
                    MusicLinkConverter.cacheTrackArtwork(info.identifier, customArtworkUrl);
                    logger.info("Artwork customizada cacheada para track: {}", info.identifier);
                }

                var embed = EmbedFactory.withRequester(event.getUser())
                        .setTitle("Adicionado à fila")
                        .setDescription(String.format("**[%s](%s)**", info.title, info.uri))
                        .addField("Canal", info.author, true)
                        .addField("Duração", TimeFormat.format(track.getDuration()), true)
                        .addField("Posição na fila", String.valueOf(musicManager.getScheduler().getQueue().size() + 1), true)
                        .setThumbnail(artworkUrl)
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

                boolean isSearchResult = playlist.isSearchResult() ||
                        (playlist.getName() != null && playlist.getName().startsWith("Search results for:"));

                if (isSearchResult) {
                    AudioTrack firstTrack = tracks.get(0);
                    musicManager.getScheduler().queue(firstTrack);

                    var info = firstTrack.getInfo();
                    String artworkUrl = customArtworkUrl != null ? customArtworkUrl : info.artworkUrl;

                    if (artworkUrl == null) {
                        logger.info("Tentando extrair thumbnail - URI: {}, Identifier: {}", info.uri, info.identifier);
                        if (info.uri != null && info.uri.contains("youtube")) {
                            artworkUrl = SourceDetector.getYouTubeThumbnail(info.uri);
                            if (artworkUrl != null) {
                                logger.info("Thumbnail extraída do URI: {}", artworkUrl);
                            }
                        }
                        if (artworkUrl == null && info.identifier != null) {
                            String videoId = info.identifier;
                            if (videoId.startsWith("youtube:")) {
                                videoId = videoId.substring(8);
                            }
                            if (videoId.length() == 11) {
                                artworkUrl = "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg";
                                logger.info("Usando thumbnail do YouTube para: {} (ID: {})", info.title, videoId);
                            } else {
                                logger.warn("Video ID inválido: {} (tamanho: {})", videoId, videoId.length());
                            }
                        }
                    }

                    if (customArtworkUrl != null && info.identifier != null) {
                        MusicLinkConverter.cacheTrackArtwork(info.identifier, customArtworkUrl);
                        logger.info("Artwork customizada cacheada para track: {}", info.identifier);
                    }

                    var embed = EmbedFactory.withRequester(event.getUser())
                            .setTitle("Adicionado à fila")
                            .setDescription(String.format("**[%s](%s)**", info.title, info.uri))
                            .addField("Canal", info.author, true)
                            .addField("Duração", TimeFormat.format(firstTrack.getDuration()), true)
                            .addField("Posição na fila", String.valueOf(musicManager.getScheduler().getQueue().size() + 1), true)
                            .setThumbnail(artworkUrl)
                            .build();

                    event.getHook().editOriginalEmbeds(embed).queue();
                    logger.info("Música carregada (do resultado da busca): {}", info.title);
                } else {
                    int count = 0;
                    for (AudioTrack track : tracks) {
                        if (musicManager.getScheduler().queue(track)) {
                            count++;
                        }
                    }

                    String playlistArtwork = null;

                    if (sourceType == SourceDetector.SourceType.SPOTIFY && originalQuery != null) {
                        playlistArtwork = SpotifyApiClient.getPlaylistArtwork(originalQuery);
                        if (playlistArtwork != null) {
                            logger.info("Usando artwork oficial da playlist Spotify");
                        }
                    }

                    if (playlistArtwork == null && !tracks.isEmpty()) {
                        playlistArtwork = tracks.get(0).getInfo().artworkUrl;
                        logger.info("Usando artwork da primeira música como fallback");
                    }

                    var embedBuilder = EmbedFactory.withRequester(event.getUser())
                            .setTitle("Playlist adicionada")
                            .setDescription(String.format("**%s**", playlist.getName()))
                            .addField("Músicas adicionadas", String.valueOf(count), true)
                            .addField("Duração total", TimeFormat.formatVerbose(
                                    tracks.stream().mapToLong(AudioTrack::getDuration).sum()
                            ), true);

                    if (playlistArtwork != null) {
                        embedBuilder.setThumbnail(playlistArtwork);
                    }

                    event.getHook().editOriginalEmbeds(embedBuilder.build()).queue();
                    logger.info("Playlist carregada: {} ({} músicas)", playlist.getName(), count);
                }
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
                boolean isRetryable = exception.getMessage() != null &&
                        (exception.getMessage().contains("timed out") ||
                         exception.getMessage().contains("SocketTimeout") ||
                         exception.getCause() instanceof java.net.SocketTimeoutException);

                if (isRetryable && attemptCount < MAX_RETRIES) {
                    logger.warn("Tentativa {} de {} falhou para: {}. Tentando novamente em {}ms...",
                            attemptCount + 1, MAX_RETRIES, trackUrl, RETRY_DELAY_MS);

                    new Thread(() -> {
                        try {
                            Thread.sleep(RETRY_DELAY_MS);
                            loadWithRetry(event, musicManager, trackUrl, attemptCount + 1, customArtworkUrl, sourceType, originalQuery);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            logger.error("Thread retry interompida", e);
                        }
                    }).start();
                } else {
                    if (attemptCount > 0) {
                        logger.error("Falha ao carregar música após {} tentativas: {}", attemptCount + 1, trackUrl, exception);
                        event.getHook().editOriginalEmbeds(
                                EmbedFactory.error("Falha ao carregar",
                                        "Falha ao carregar música após " + (attemptCount + 1) + " tentativas: " + exception.getMessage())
                        ).queue();
                    } else {
                        event.getHook().editOriginalEmbeds(
                                EmbedFactory.error("Falha ao carregar", "Falha ao carregar música: " + exception.getMessage())
                        ).queue();
                        logger.error("Falha ao carregar música: {}", trackUrl, exception);
                    }
                }
            }
        });
    }
}
