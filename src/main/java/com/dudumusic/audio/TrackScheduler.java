package com.dudumusic.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class TrackScheduler extends AudioEventAdapter {
    private static final Logger logger = LoggerFactory.getLogger(TrackScheduler.class);
    private static final int MAX_QUEUE_SIZE = 100;

    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue;
    private LoopMode loopMode;
    private AudioTrack lastTrack;

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

    public boolean queue(AudioTrack track) {
        if (queue.size() >= MAX_QUEUE_SIZE) {
            logger.warn("Fila cheia, rejeitando música: {}", track.getInfo().title);
            return false;
        }

        if (player.getPlayingTrack() == null) {
            player.playTrack(track);
            logger.info("Tocando agora: {}", track.getInfo().title);
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
        int size = queue.size();
        queue.clear();
        logger.info("Fila limpa ({} músicas removidas)", size);
    }

    public BlockingQueue<AudioTrack> getQueue() {
        return queue;
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
}
