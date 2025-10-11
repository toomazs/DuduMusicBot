package com.dudumusic.commands.info;

import com.dudumusic.audio.MusicManager;
import com.dudumusic.commands.Command;
import com.dudumusic.utils.EmbedFactory;
import com.dudumusic.utils.TimeFormat;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class QueueCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(QueueCommand.class);
    private static final int TRACKS_PER_PAGE = 10;

    @Override
    public String getName() {
        return "queue";
    }

    @Override
    public String getDescription() {
        return "Mostrar a fila de musicas";
    }

    @Override
    public List<OptionData> getOptions() {
        return Collections.emptyList();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        long guildId = event.getGuild().getIdLong();
        MusicManager musicManager = MusicManager.getManager(guildId);

        AudioTrack currentTrack = musicManager.getPlayer().getPlayingTrack();
        BlockingQueue<AudioTrack> queue = musicManager.getScheduler().getQueue();

        if (currentTrack == null && queue.isEmpty()) {
            event.replyEmbeds(
                    EmbedFactory.info("Fila vazia", "Nao ha musicas na fila")
            ).queue();
            return;
        }

        MessageEmbed embed = createQueueEmbed(currentTrack, queue, 0);

        int totalPages = (int) Math.ceil((double) queue.size() / TRACKS_PER_PAGE);

        if (totalPages > 1) {
            event.replyEmbeds(embed)
                    .addActionRow(
                            Button.secondary("queue:prev:0", "Anterior").asDisabled(),
                            Button.secondary("queue:next:1", "Proximo"),
                            Button.danger("queue:clear", "Limpar fila")
                    )
                    .queue();
        } else {
            event.replyEmbeds(embed)
                    .addActionRow(
                            Button.danger("queue:clear", "Limpar fila")
                    )
                    .queue();
        }

        logger.info("Fila exibida para o servidor: {} (pagina 0/{} paginas)", guildId, totalPages);
    }

    public static MessageEmbed createQueueEmbed(AudioTrack currentTrack, BlockingQueue<AudioTrack> queue, int page) {
        EmbedBuilder builder = EmbedFactory.musicBuilder()
                .setTitle("Fila de musicas");

        if (currentTrack != null) {
            var info = currentTrack.getInfo();
            builder.addField("Tocando agora",
                    String.format("**[%s](%s)**\n%s - `%s`",
                            info.title,
                            info.uri,
                            info.author,
                            TimeFormat.format(currentTrack.getDuration())),
                    false);
        }

        // Show queue
        if (!queue.isEmpty()) {
            List<AudioTrack> tracks = new ArrayList<>(queue);
            int totalPages = (int) Math.ceil((double) tracks.size() / TRACKS_PER_PAGE);

            int start = page * TRACKS_PER_PAGE;
            int end = Math.min(start + TRACKS_PER_PAGE, tracks.size());

            StringBuilder queueText = new StringBuilder();
            for (int i = start; i < end; i++) {
                AudioTrack track = tracks.get(i);
                queueText.append(String.format("`%d.` **%s** - `%s`\n",
                        i + 1,
                        track.getInfo().title,
                        TimeFormat.format(track.getDuration())));
            }

            builder.addField("Proximas musicas", queueText.toString(), false);

            long totalDuration = tracks.stream()
                    .mapToLong(AudioTrack::getDuration)
                    .sum();

            builder.setFooter(String.format("Pagina %d/%d - %d musicas - Total: %s",
                    page + 1,
                    totalPages,
                    tracks.size(),
                    TimeFormat.formatVerbose(totalDuration)));
        } else {
            builder.setDescription("Nenhuma musica na fila");
        }

        return builder.build();
    }
}
