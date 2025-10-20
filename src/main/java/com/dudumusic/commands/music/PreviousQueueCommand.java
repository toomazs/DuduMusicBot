package com.dudumusic.commands.music;

import com.dudumusic.audio.MusicManager;
import com.dudumusic.audio.QueueHistory;
import com.dudumusic.core.Translation;
import com.dudumusic.utils.EmbedFactory;
import com.dudumusic.utils.TimeFormat;
import com.dudumusic.utils.VoiceValidator;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class PreviousQueueCommand implements com.dudumusic.commands.Command {

    @Override
    public String getName() {
        return "previousqueue";
    }

    @Override
    public String getDescription() {
        return Translation.t(0L, "cmd_previousqueue_desc");
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        long gid = event.getGuild().getIdLong();

        if (!VoiceValidator.validate(event, false)) {
            return;
        }

        MusicManager mgr = MusicManager.getManager(gid);
        if (mgr == null || mgr.getScheduler() == null) {
            event.replyEmbeds(
                    EmbedFactory.error(
                            Translation.t(gid, "previousqueue_no_manager_title"),
                            Translation.t(gid, "previousqueue_no_manager_desc")
                    )
            ).queue();
            return;
        }

        QueueHistory history = QueueHistory.getInstance(gid);
        if (!history.hasHistory()) {
            event.replyEmbeds(
                    EmbedFactory.error(
                            Translation.t(gid, "previousqueue_no_history_title"),
                            Translation.t(gid, "previousqueue_no_history_desc")
                    )
            ).queue();
            return;
        }

        List<QueueHistory.QueueSnapshot> snapshots = history.getHistory();

        StringBuilder description = new StringBuilder();
        description.append(Translation.t(gid, "previousqueue_select_prompt")).append("\n\n");

        List<Button> buttons = new ArrayList<>();

        for (int i = 0; i < snapshots.size(); i++) {
            QueueHistory.QueueSnapshot snapshot = snapshots.get(i);

            Duration timeSince = Duration.between(snapshot.getTimestamp(), Instant.now());
            String timeAgo = formatTimeAgo(timeSince, gid);

            description.append(String.format("**%d.** %s\n", i + 1, snapshot.getDescription()));
            description.append(String.format("   %s - %s\n",
                    Translation.t(gid, "previousqueue_tracks", snapshot.getSize()),
                    timeAgo
            ));

            var tracks = snapshot.getTracks();
            if (!tracks.isEmpty()) {
                description.append("   ");

                java.util.Set<String> uniqueArtists = new java.util.LinkedHashSet<>();
                int previewCount = Math.min(3, tracks.size());
                for (int j = 0; j < previewCount; j++) {
                    var info = tracks.get(j).getInfo();
                    if (info.author != null && !info.author.trim().isEmpty()) {
                        uniqueArtists.add(info.author);
                    }
                }

                int artistCount = 0;
                for (String artist : uniqueArtists) {
                    description.append(artist);
                    if (artistCount < uniqueArtists.size() - 1) {
                        description.append(", ");
                    }
                    artistCount++;
                }

                if (tracks.size() > 3) {
                    description.append(String.format(" +%d", tracks.size() - 3));
                }
                description.append("\n");
            }
            description.append("\n");

            buttons.add(Button.primary("restore_queue:" + i, String.valueOf(i + 1)));
        }

        var embed = EmbedFactory.musicBuilder()
                .setTitle(Translation.t(gid, "previousqueue_title"))
                .setDescription(description.toString())
                .build();

        event.replyEmbeds(embed)
                .addActionRow(buttons)
                .queue();
    }

    private String formatTimeAgo(Duration duration, long gid) {
        long seconds = duration.getSeconds();

        if (seconds < 60) {
            return Translation.t(gid, "time_seconds_ago", seconds);
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            return Translation.t(gid, "time_minutes_ago", minutes);
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            return Translation.t(gid, "time_hours_ago", hours);
        } else {
            long days = seconds / 86400;
            return Translation.t(gid, "time_days_ago", days);
        }
    }
}
