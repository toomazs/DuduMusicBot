package com.dudumusic.audio;

import com.dudumusic.core.Translation;
import com.dudumusic.utils.EmbedFactory;
import com.dudumusic.utils.SourceDetector;
import com.dudumusic.utils.TimeFormat;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class TrackScheduler extends AudioEventAdapter {
    private static final Logger logger = LoggerFactory.getLogger(TrackScheduler.class);
    private static final int MAX_QUEUE_SIZE = 1000;

    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue;
    private LoopMode loopMode;
    private AudioTrack lastTrack;
    private TextChannel textChannel;
    private long guildId;

    public enum LoopMode {
        OFF,
        TRACK,
        QUEUE
    }

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
        this.loopMode = LoopMode.OFF;
    }

    public void setGuildId(long guildId) {
        this.guildId = guildId;
    }

    public boolean queue(AudioTrack track) {
        if (queue.size() >= MAX_QUEUE_SIZE) {
            logger.warn("Fila cheia, rejeitando música: {}", track.getInfo().title);
            return false;
        }

        if (player.getPlayingTrack() == null) {
            player.playTrack(track);
            logger.info("Tocando agora: {}", track.getInfo().title);

            sendNowPlayingMessage(track);

            return true;
        } else {
            boolean added = queue.offer(track);
            if (added) {
                logger.info("Música adicionada à fila: {} (posição: {})", track.getInfo().title, queue.size());
            }
            return added;
        }
    }

    public void nextTrack() {
        AudioTrack next = queue.poll();
        if (next != null) {
            player.playTrack(next);
            logger.info("Pulou para próxima música: {}", next.getInfo().title);

            sendNowPlayingMessage(next);
        } else {
            player.stopTrack();
            logger.info("Fila vazia, parando reprodução");
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        this.lastTrack = track;

        if (!endReason.mayStartNext) {
            return;
        }

        if (loopMode == LoopMode.TRACK) {
            player.playTrack(track.makeClone());
            logger.info("Repetindo música: {}", track.getInfo().title);
            return;
        }

        AudioTrack nextTrack = queue.poll();

        if (nextTrack != null) {
            player.playTrack(nextTrack);
            logger.info("Reproduzindo automaticamente próxima música: {}", nextTrack.getInfo().title);

            sendNowPlayingMessage(nextTrack);

            if (loopMode == LoopMode.QUEUE) {
                queue.offer(track.makeClone());
            }
        } else {
            logger.info("Fila vazia, aguardando próximo comando");
        }
    }

    public void shuffle() {
        List<AudioTrack> tracks = new ArrayList<>(queue);
        Collections.shuffle(tracks);
        queue.clear();
        queue.addAll(tracks);
        logger.info("Fila embaralhada ({} músicas)", tracks.size());
    }

    public void clearQueue() {
        clearQueue(true, "Fila limpa com /clear");
    }

    public void clearQueue(boolean saveToHistory, String historyDescription) {
        if (saveToHistory && !queue.isEmpty()) {
            List<AudioTrack> currentQueue = new ArrayList<>(queue);
            QueueHistory.getInstance(guildId).saveQueue(currentQueue, historyDescription);
        }
        int size = queue.size();
        queue.clear();
        logger.info("Fila limpa ({} músicas removidas)", size);
    }

    public BlockingQueue<AudioTrack> getQueue() {
        return queue;
    }

    public AudioTrack jumpTo(int position) {
        if (position <= 0) return null;

        synchronized (queue) {
            if (queue.isEmpty() || position > queue.size()) {
                return null;
            }

            java.util.List<AudioTrack> list = new java.util.ArrayList<>(queue);
            AudioTrack target = list.get(position - 1);

            boolean removed = queue.remove(target);
            if (!removed) {
                java.util.Iterator<AudioTrack> it = queue.iterator();
                while (it.hasNext()) {
                    AudioTrack t = it.next();
                    if (t.getInfo().title.equals(target.getInfo().title) && t.getDuration() == target.getDuration()) {
                        it.remove();
                        break;
                    }
                }
            }

            player.playTrack(target.makeClone());
            logger.info("Jumped to track: {}", target.getInfo().title);
            sendNowPlayingMessage(target);
            return target;
        }
    }

    public AudioTrack switchTracks(int fromPosition, int toPosition) {
        if (fromPosition <= 0 || toPosition <= 0) return null;
        if (fromPosition == toPosition) return null;

        synchronized (queue) {
            if (queue.isEmpty() || fromPosition > queue.size() || toPosition > queue.size()) {
                return null;
            }

            java.util.List<AudioTrack> list = new java.util.ArrayList<>(queue);
            AudioTrack trackToMove = list.get(fromPosition - 1);

            list.remove(fromPosition - 1);
            list.add(toPosition - 1, trackToMove);

            queue.clear();
            queue.addAll(list);

            logger.info("Switched track '{}' from position {} to {}", trackToMove.getInfo().title, fromPosition, toPosition);
            return trackToMove;
        }
    }

    public AudioTrack removeTrack(int position) {
        if (position <= 0) return null;

        synchronized (queue) {
            if (queue.isEmpty() || position > queue.size()) {
                return null;
            }

            java.util.List<AudioTrack> list = new java.util.ArrayList<>(queue);
            AudioTrack trackToRemove = list.get(position - 1);

            list.remove(position - 1);

            queue.clear();
            queue.addAll(list);

            logger.info("Removed track '{}' from position {}", trackToRemove.getInfo().title, position);
            return trackToRemove;
        }
    }

    public LoopMode getLoopMode() {
        return loopMode;
    }

    public void setLoopMode(LoopMode mode) {
        this.loopMode = mode;
        logger.info("Modo de repetição definido para: {}", mode);
    }

    public AudioTrack getLastTrack() {
        return lastTrack;
    }

    public AudioTrack rewind() {
        if (lastTrack == null) {
            logger.warn("Não há música anterior para retornar");
            return null;
        }

        AudioTrack currentTrack = player.getPlayingTrack();

        if (currentTrack != null) {
            List<AudioTrack> tracks = new ArrayList<>(queue);
            tracks.add(0, currentTrack.makeClone());
            queue.clear();
            queue.addAll(tracks);
            logger.info("Música atual '{}' retornou para o início da fila", currentTrack.getInfo().title);
        }

        AudioTrack trackToPlay = lastTrack.makeClone();
        player.playTrack(trackToPlay);
        logger.info("Voltando para a música anterior: {}", trackToPlay.getInfo().title);

        sendNowPlayingMessage(trackToPlay);

        return trackToPlay;
    }

    public void setTextChannel(TextChannel textChannel) {
        this.textChannel = textChannel;
    }

    public TextChannel getTextChannel() {
        return textChannel;
    }

    public int restoreQueue(QueueHistory.QueueSnapshot snapshot) {
        if (snapshot == null) {
            logger.warn("Tentativa de restaurar snapshot nulo");
            return 0;
        }

        List<AudioTrack> tracks = snapshot.getTracks();
        if (tracks.isEmpty()) {
            logger.warn("Snapshot não contém músicas");
            return 0;
        }

        if (!queue.isEmpty()) {
            List<AudioTrack> currentQueue = new ArrayList<>(queue);
            QueueHistory.getInstance(guildId).saveQueue(currentQueue, "Fila substituída por fila anterior");
        }

        queue.clear();
        int added = 0;
        for (AudioTrack track : tracks) {
            if (queue.offer(track)) {
                added++;
            }
        }

        logger.info("Fila restaurada: {} músicas do histórico", added);
        return added;
    }

    private void sendNowPlayingMessage(AudioTrack track) {
        if (textChannel == null) {
            return;
        }

        try {
            var info = track.getInfo();
            String artworkUrl = null;

            if (info.identifier != null) {
                artworkUrl = com.dudumusic.utils.MusicLinkConverter.getTrackArtwork(info.identifier);
                if (artworkUrl != null) {
                    logger.info("Usando artwork customizada cacheada para: {}", info.title);
                }
            }

            if (artworkUrl == null) {
                artworkUrl = info.artworkUrl;
            }

            if (artworkUrl == null && info.uri != null && info.uri.contains("youtube")) {
                artworkUrl = SourceDetector.getYouTubeThumbnail(info.uri);
            }

            if (artworkUrl == null && info.identifier != null) {
                String videoId = info.identifier;
                if (videoId.startsWith("youtube:")) {
                    videoId = videoId.substring(8);
                }
                if (videoId.length() == 11) {
                    artworkUrl = "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg";
                }
            }

            String displayTitle = getDisplayTitle(info.title);

            var embed = EmbedFactory.nowPlayingBuilder()
                    .setTitle(Translation.t(guildId, "track_now_playing"))
                    .setDescription(String.format("**[%s](%s)**", displayTitle, info.uri))
                    .addField(Translation.t(guildId, "track_artist"), info.author, true)
                    .addField(Translation.t(guildId, "track_duration"), TimeFormat.format(track.getDuration()), true)
                    .setThumbnail(artworkUrl)
                    .build();

            textChannel.sendMessageEmbeds(embed).queue(
                success -> logger.info("Mensagem 'Tocando agora' enviada para: {}", info.title),
                error -> logger.warn("Falha ao enviar mensagem 'Tocando agora': {}", error.getMessage())
            );
        } catch (Exception e) {
            logger.error("Erro ao enviar mensagem 'Tocando agora'", e);
        }
    }

    private String getDisplayTitle(String title) {
        if (title == null || title.trim().isEmpty() || title.matches("^\\s*$")) {
            return Translation.t(guildId, "track_untitled");
        }
        return title;
    }
}
