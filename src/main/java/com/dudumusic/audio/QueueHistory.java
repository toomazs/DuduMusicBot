package com.dudumusic.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class QueueHistory {
    private static final Logger logger = LoggerFactory.getLogger(QueueHistory.class);
    private static final int MAX_HISTORY_SIZE = 10;
    private static final ConcurrentHashMap<Long, QueueHistory> instances = new ConcurrentHashMap<>();

    private final LinkedList<QueueSnapshot> history;

    public static class QueueSnapshot {
        private final List<AudioTrack> tracks;
        private final Instant timestamp;
        private final String description;

        public QueueSnapshot(List<AudioTrack> tracks, String description) {
            this.tracks = new ArrayList<>();
            for (AudioTrack track : tracks) {
                this.tracks.add(track.makeClone());
            }
            this.timestamp = Instant.now();
            this.description = description;
        }

        public List<AudioTrack> getTracks() {
            List<AudioTrack> clones = new ArrayList<>();
            for (AudioTrack track : tracks) {
                clones.add(track.makeClone());
            }
            return clones;
        }

        public Instant getTimestamp() {
            return timestamp;
        }

        public String getDescription() {
            return description;
        }

        public int getSize() {
            return tracks.size();
        }
    }

    private QueueHistory() {
        this.history = new LinkedList<>();
    }

    public static QueueHistory getInstance(long guildId) {
        return instances.computeIfAbsent(guildId, k -> new QueueHistory());
    }

    public void saveQueue(List<AudioTrack> tracks, String description) {
        if (tracks == null || tracks.isEmpty()) {
            logger.debug("Não salvando fila vazia no histórico");
            return;
        }

        synchronized (history) {
            QueueSnapshot snapshot = new QueueSnapshot(tracks, description);
            history.addFirst(snapshot);

            if (history.size() > MAX_HISTORY_SIZE) {
                history.removeLast();
            }

            logger.info("Fila salva no histórico: {} músicas - {}", tracks.size(), description);
        }
    }

    public List<QueueSnapshot> getHistory() {
        synchronized (history) {
            return new ArrayList<>(history);
        }
    }

    public QueueSnapshot getSnapshot(int index) {
        synchronized (history) {
            if (index >= 0 && index < history.size()) {
                return history.get(index);
            }
            return null;
        }
    }

    public boolean hasHistory() {
        synchronized (history) {
            return !history.isEmpty();
        }
    }

    public void clear() {
        synchronized (history) {
            history.clear();
            logger.info("Histórico de filas limpo");
        }
    }
}
