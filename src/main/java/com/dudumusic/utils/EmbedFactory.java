package com.dudumusic.utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

import java.awt.Color;
import java.time.Instant;

public class EmbedFactory {

    private static final Color COLOR_SUCCESS = new Color(87, 242, 135);
    private static final Color COLOR_ERROR = new Color(237, 66, 69);
    private static final Color COLOR_INFO = new Color(88, 101, 242);
    private static final Color COLOR_WARNING = new Color(254, 231, 92);
    private static final Color COLOR_MUSIC = new Color(114, 137, 218);

    public static MessageEmbed success(String title, String description) {
        return new EmbedBuilder()
                .setColor(COLOR_SUCCESS)
                .setTitle(title)
                .setDescription(description)
                .setTimestamp(Instant.now())
                .build();
    }

    public static MessageEmbed error(String title, String description) {
        return new EmbedBuilder()
                .setColor(COLOR_ERROR)
                .setTitle(title)
                .setDescription(description)
                .setTimestamp(Instant.now())
                .build();
    }

    public static MessageEmbed info(String title, String description) {
        return new EmbedBuilder()
                .setColor(COLOR_INFO)
                .setTitle(title)
                .setDescription(description)
                .setTimestamp(Instant.now())
                .build();
    }

    public static MessageEmbed warning(String title, String description) {
        return new EmbedBuilder()
                .setColor(COLOR_WARNING)
                .setTitle(title)
                .setDescription(description)
                .setTimestamp(Instant.now())
                .build();
    }

    public static EmbedBuilder musicBuilder() {
        return new EmbedBuilder()
                .setColor(COLOR_MUSIC)
                .setTimestamp(Instant.now());
    }

    public static EmbedBuilder withRequester(User user, long guildId) {
        String footerText = com.dudumusic.core.Translation.t(guildId, "embed_requested_by", user.getName());
        return musicBuilder()
                .setFooter(footerText, user.getAvatarUrl());
    }
}
