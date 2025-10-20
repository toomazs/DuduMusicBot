package com.dudumusic.commands.music;

import com.dudumusic.audio.MusicManager;
import com.dudumusic.commands.Command;
import com.dudumusic.core.PlayerConfig;
import com.dudumusic.core.Translation;
import com.dudumusic.utils.EmbedFactory;
import com.dudumusic.utils.MusicLinkConverter;
import com.dudumusic.utils.SourceDetector;
import com.dudumusic.utils.SpotifyApiClient;
import com.dudumusic.utils.TimeFormat;
import com.dudumusic.utils.VoiceValidator;
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
        return Translation.t(0L, "cmd_play_desc");
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

        long guildId = event.getGuild().getIdLong();
        Member member = event.getMember();
        Member bot = event.getGuild().getSelfMember();

        if (!VoiceValidator.validateDeferred(event.getHook(), member, bot, guildId, false)) {
            return;
        }

        String query = event.getOption("query").getAsString();

        SourceDetector.SourceType sourceType = SourceDetector.detect(query);
        logger.info("Requisição de reprodução - Fonte: {}, Query: {}", sourceType.getDisplayName(), query);

        String customArtworkUrl = null;
        MusicManager musicManager = MusicManager.getManager(guildId);

        if (event.getChannel() instanceof net.dv8tion.jda.api.entities.channel.concrete.TextChannel) {
            musicManager.setTextChannel((net.dv8tion.jda.api.entities.channel.concrete.TextChannel) event.getChannel());
        }

        AudioManager audioManager = event.getGuild().getAudioManager();
        if (!audioManager.isConnected()) {
            VoiceChannel voiceChannel = (VoiceChannel) member.getVoiceState().getChannel();
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
                        var embedBuilder = EmbedFactory.withRequester(event.getUser(), guildId)
                                .setTitle(Translation.t(guildId, "play_playlist_added"))
                                .setDescription(String.format("**%s**", playlistResult.playlistName != null ? playlistResult.playlistName : "Playlist"))
                                .addField(Translation.t(guildId, "play_playlist_songs_added"), String.valueOf(added[0]), true);

                        if (playlistResult.artworkUrl != null) {
                            embedBuilder.setThumbnail(playlistResult.artworkUrl);
                        }

                        event.getHook().editOriginalEmbeds(embedBuilder.build()).queue();
                    }
                };

                var initialEmbed = EmbedFactory.withRequester(event.getUser(), guildId)
                        .setTitle(Translation.t(guildId, "play_playlist_adding"))
                        .setDescription(String.format("**%s**", playlistResult.playlistName != null ? playlistResult.playlistName : "Playlist"))
                        .addField(Translation.t(guildId, "play_playlist_songs_total"), String.valueOf(playlistResult.searchQueries.size()), true);
                if (playlistResult.artworkUrl != null) initialEmbed.setThumbnail(playlistResult.artworkUrl);
                event.getHook().editOriginalEmbeds(initialEmbed.build()).queue();

                final String playlistArtwork = playlistResult.artworkUrl;

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
                            if (!isInvalidTrack(track)) {
                                mgr.getScheduler().queue(track);
                                added[0]++;
                                if (playlistArtwork != null && track.getInfo().identifier != null) {
                                    MusicLinkConverter.cacheTrackArtwork(track.getInfo().identifier, playlistArtwork);
                                }
                            } else {
                                logger.warn("Track inválido pulado de playlist convertida: {} - {}",
                                        track.getInfo().title, track.getInfo().author);
                            }
                            inFlight.decrementAndGet();
                            loaderRef.get().accept(null);
                            checkFinish.run();
                        }

                        @Override
                        public void playlistLoaded(AudioPlaylist playlist) {
                            if (!playlist.getTracks().isEmpty()) {
                                AudioTrack track = playlist.getTracks().get(0);
                                if (!isInvalidTrack(track)) {
                                    mgr.getScheduler().queue(track);
                                    added[0]++;
                                    if (playlistArtwork != null && track.getInfo().identifier != null) {
                                        MusicLinkConverter.cacheTrackArtwork(track.getInfo().identifier, playlistArtwork);
                                    }
                                } else {
                                    logger.warn("Track inválido pulado de playlist convertida (search result): {} - {}",
                                            track.getInfo().title, track.getInfo().author);
                                }
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
                        EmbedFactory.error(
                                Translation.t(guildId, "play_playlist_convert_fail_title"),
                                Translation.t(guildId, "play_playlist_convert_fail_desc")
                        )
                ).queue();
                return;
            }
        } else if (sourceType == SourceDetector.SourceType.APPLE_MUSIC) {
            event.getHook().editOriginalEmbeds(
                    EmbedFactory.error(
                            Translation.t(guildId, "play_apple_music_unsupported_title"),
                            Translation.t(guildId, "play_apple_music_unsupported_desc")
                    )
            ).queue();
            logger.warn("Link Apple Music não suportado: {}", query);
            return;
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
        final int MAX_RETRIES = 5;
        final long BASE_RETRY_DELAY_MS = 1500;
        final long retryDelay = BASE_RETRY_DELAY_MS * (attemptCount + 1);

        if (attemptCount > 0) {
            logger.info("Tentativa {} de {} para carregar: {}", attemptCount + 1, MAX_RETRIES + 1, trackUrl);
        }

        PlayerConfig.getInstance().loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                if (isInvalidTrack(track)) {
                    logger.warn("Track inválido detectado e pulado: {} - {} (duração: {})",
                            track.getInfo().title, track.getInfo().author, track.getDuration());
                    long gid = event.getGuild().getIdLong();
                    event.getHook().editOriginalEmbeds(
                            EmbedFactory.error(
                                    Translation.t(gid, "play_invalid_track_title"),
                                    Translation.t(gid, "play_invalid_track_desc")
                            )
                    ).queue();
                    return;
                }

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

                long gid = event.getGuild().getIdLong();
                String displayTitle = getDisplayTitle(info.title, gid);

                boolean wasPlaying = musicManager.getPlayer().getPlayingTrack() != null;
                musicManager.getScheduler().queue(track);

                var embedBuilder = EmbedFactory.withRequester(event.getUser(), gid)
                        .setTitle(Translation.t(gid, "play_added_to_queue"))
                        .setDescription(String.format("**[%s](%s)**", displayTitle, info.uri))
                        .addField(Translation.t(gid, "play_channel"), info.author, true)
                        .addField(Translation.t(gid, "play_duration"), TimeFormat.format(track.getDuration()), true);

                if (wasPlaying) {
                    int queuePosition = musicManager.getScheduler().getQueue().size();
                    embedBuilder.addField(Translation.t(gid, "play_position"), String.valueOf(queuePosition), true);
                }

                embedBuilder.setThumbnail(artworkUrl);

                event.getHook().editOriginalEmbeds(embedBuilder.build()).queue();
                logger.info("Música carregada: {}", info.title);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                List<AudioTrack> tracks = playlist.getTracks();

                if (tracks.isEmpty()) {
                    long gid = event.getGuild().getIdLong();
                    event.getHook().editOriginalEmbeds(
                            EmbedFactory.error(
                                    Translation.t(gid, "play_playlist_empty_title"),
                                    Translation.t(gid, "play_playlist_empty_desc")
                            )
                    ).queue();
                    return;
                }

                boolean isSearchResult = playlist.isSearchResult() ||
                        (playlist.getName() != null && playlist.getName().startsWith("Search results for:"));

                if (isSearchResult) {
                    AudioTrack firstTrack = tracks.get(0);

                    if (isInvalidTrack(firstTrack)) {
                        logger.warn("Track inválido detectado em search result e pulado: {} - {}",
                                firstTrack.getInfo().title, firstTrack.getInfo().author);
                        long gid = event.getGuild().getIdLong();
                        event.getHook().editOriginalEmbeds(
                                EmbedFactory.error(
                                        Translation.t(gid, "play_invalid_track_title"),
                                        Translation.t(gid, "play_invalid_track_desc")
                                )
                        ).queue();
                        return;
                    }

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

                    long gid = event.getGuild().getIdLong();
                    String displayTitle = getDisplayTitle(info.title, gid);

                    boolean wasPlaying = musicManager.getPlayer().getPlayingTrack() != null;
                    musicManager.getScheduler().queue(firstTrack);

                    var embedBuilder = EmbedFactory.withRequester(event.getUser(), gid)
                            .setTitle(Translation.t(gid, "play_added_to_queue"))
                            .setDescription(String.format("**[%s](%s)**", displayTitle, info.uri))
                            .addField(Translation.t(gid, "play_channel"), info.author, true)
                            .addField(Translation.t(gid, "play_duration"), TimeFormat.format(firstTrack.getDuration()), true);

                    if (wasPlaying) {
                        int queuePosition = musicManager.getScheduler().getQueue().size();
                        embedBuilder.addField(Translation.t(gid, "play_position"), String.valueOf(queuePosition), true);
                    }

                    embedBuilder.setThumbnail(artworkUrl);

                    event.getHook().editOriginalEmbeds(embedBuilder.build()).queue();
                    logger.info("Música carregada (do resultado da busca): {}", info.title);
                } else {
                    int count = 0;
                    int skipped = 0;
                    for (AudioTrack track : tracks) {
                        if (isInvalidTrack(track)) {
                            logger.warn("Track inválido pulado da playlist: {} - {} (duração: {})",
                                    track.getInfo().title, track.getInfo().author, track.getDuration());
                            skipped++;
                            continue;
                        }
                        if (musicManager.getScheduler().queue(track)) {
                            count++;
                        }
                    }
                    if (skipped > 0) {
                        logger.info("Playlist carregada com {} tracks válidos, {} inválidos pulados", count, skipped);
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

                    long gid = event.getGuild().getIdLong();
                    var embedBuilder = EmbedFactory.withRequester(event.getUser(), gid)
                            .setTitle(Translation.t(gid, "play_playlist_added"))
                            .setDescription(String.format("**%s**", playlist.getName()))
                            .addField(Translation.t(gid, "play_playlist_songs_added"), String.valueOf(count), true)
                            .addField(Translation.t(gid, "play_playlist_total_duration"), TimeFormat.formatVerbose(
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
                long gid = event.getGuild().getIdLong();
                event.getHook().editOriginalEmbeds(
                        EmbedFactory.error(
                                Translation.t(gid, "play_no_matches_title"),
                                Translation.t(gid, "play_no_matches_desc")
                        )
                ).queue();
                logger.warn("Nenhuma correspondência encontrada para: {}", trackUrl);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                boolean isRetryable = isRetryableException(exception);

                if (isRetryable && attemptCount < MAX_RETRIES) {
                    logger.warn("Tentativa {} de {} falhou para: {}. Tentando novamente em {}ms... Motivo: {}",
                            attemptCount + 1, MAX_RETRIES + 1, trackUrl, retryDelay,
                            exception.getMessage() != null ? exception.getMessage() : "Unknown");

                    new Thread(() -> {
                        try {
                            Thread.sleep(retryDelay);
                            loadWithRetry(event, musicManager, trackUrl, attemptCount + 1, customArtworkUrl, sourceType, originalQuery);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            logger.error("Thread retry interrompida", e);
                        }
                    }).start();
                } else {
                    long gid = event.getGuild().getIdLong();
                    if (attemptCount > 0) {
                        logger.error("Falha ao carregar música após {} tentativas: {}", attemptCount + 1, trackUrl, exception);
                        event.getHook().editOriginalEmbeds(
                                EmbedFactory.error(
                                        Translation.t(gid, "play_load_failed_title"),
                                        Translation.t(gid, "play_load_failed_retry", attemptCount + 1, exception.getMessage())
                                )
                        ).queue();
                    } else {
                        event.getHook().editOriginalEmbeds(
                                EmbedFactory.error(
                                        Translation.t(gid, "play_load_failed_title"),
                                        Translation.t(gid, "play_load_failed_desc", exception.getMessage())
                                )
                        ).queue();
                        logger.error("Falha ao carregar música: {}", trackUrl, exception);
                    }
                }
            }
        });
    }

    private boolean isRetryableException(FriendlyException exception) {
        if (exception == null) return false;

        String message = exception.getMessage();
        if (message != null) {
            message = message.toLowerCase();
            if (message.contains("timed out") ||
                message.contains("timeout") ||
                message.contains("read timed out") ||
                message.contains("something went wrong when looking up") ||
                message.contains("connection reset") ||
                message.contains("connection refused") ||
                message.contains("temporarily unavailable")) {
                return true;
            }
        }

        Throwable cause = exception.getCause();
        while (cause != null) {
            if (cause instanceof java.net.SocketTimeoutException ||
                cause instanceof java.net.ConnectException ||
                cause instanceof java.net.SocketException ||
                cause instanceof java.io.IOException) {
                return true;
            }
            cause = cause.getCause();
        }

        return false;
    }

    private String getDisplayTitle(String title, long guildId) {
        if (title == null || title.trim().isEmpty() || title.matches("^\\s*$")) {
            return Translation.t(guildId, "track_untitled");
        }
        return title;
    }

    private boolean isInvalidTrack(AudioTrack track) {
        if (track == null) return true;

        var info = track.getInfo();

        boolean hasNoTitle = info.title == null || info.title.trim().isEmpty();
        boolean isUnknownArtist = info.author != null && info.author.equalsIgnoreCase("Unknown");
        boolean hasZeroDuration = track.getDuration() == 0;

        return (hasNoTitle || hasZeroDuration) && isUnknownArtist;
    }
}
