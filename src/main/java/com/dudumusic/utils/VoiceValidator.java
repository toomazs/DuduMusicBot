package com.dudumusic.utils;

import com.dudumusic.core.Translation;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VoiceValidator {
    private static final Logger logger = LoggerFactory.getLogger(VoiceValidator.class);

    public static boolean isUserInVoiceChannel(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        long guildId = event.getGuild().getIdLong();

        if (member == null || !member.getVoiceState().inAudioChannel()) {
            event.replyEmbeds(
                EmbedFactory.error(
                    Translation.t(guildId, "voice_not_in_channel_title"),
                    Translation.t(guildId, "voice_user_must_be_in_channel")
                )
            ).setEphemeral(true).queue();
            logger.warn("Usuário {} tentou usar comando sem estar em canal de voz no servidor {}",
                member != null ? member.getUser().getName() : "desconhecido", guildId);
            return false;
        }

        return true;
    }

    public static boolean isUserInSameChannel(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        Member bot = event.getGuild().getSelfMember();
        long guildId = event.getGuild().getIdLong();

        if (!isUserInVoiceChannel(event)) {
            return false;
        }

        if (!bot.getVoiceState().inAudioChannel()) {
            return true;
        }

        VoiceChannel userChannel = (VoiceChannel) member.getVoiceState().getChannel();
        VoiceChannel botChannel = (VoiceChannel) bot.getVoiceState().getChannel();

        if (!userChannel.equals(botChannel)) {
            event.replyEmbeds(
                EmbedFactory.error(
                    Translation.t(guildId, "voice_not_same_channel_title"),
                    Translation.t(guildId, "voice_not_same_channel_desc")
                )
            ).setEphemeral(true).queue();
            logger.warn("Usuário {} tentou usar comando em canal diferente do bot no servidor {}",
                member.getUser().getName(), guildId);
            return false;
        }

        return true;
    }

    public static boolean validate(SlashCommandInteractionEvent event, boolean requireBotInChannel) {
        Member bot = event.getGuild().getSelfMember();
        long guildId = event.getGuild().getIdLong();

        if (!isUserInSameChannel(event)) {
            return false;
        }

        if (requireBotInChannel && !bot.getVoiceState().inAudioChannel()) {
            event.replyEmbeds(
                EmbedFactory.error(
                    Translation.t(guildId, "voice_bot_not_in_channel_title"),
                    Translation.t(guildId, "voice_bot_not_in_channel_desc")
                )
            ).setEphemeral(true).queue();
            logger.warn("Comando requer bot em canal mas bot não está conectado no servidor {}", guildId);
            return false;
        }

        return true;
    }

    public static boolean isUserInVoiceChannelDeferred(InteractionHook hook, Member member, long guildId) {
        if (member == null || !member.getVoiceState().inAudioChannel()) {
            hook.editOriginalEmbeds(
                EmbedFactory.error(
                    Translation.t(guildId, "voice_not_in_channel_title"),
                    Translation.t(guildId, "voice_user_must_be_in_channel")
                )
            ).queue();
            logger.warn("Usuário {} tentou usar comando sem estar em canal de voz no servidor {}",
                member != null ? member.getUser().getName() : "desconhecido", guildId);
            return false;
        }
        return true;
    }

    public static boolean isUserInSameChannelDeferred(InteractionHook hook, Member member, Member bot, long guildId) {
        if (!isUserInVoiceChannelDeferred(hook, member, guildId)) {
            return false;
        }

        if (!bot.getVoiceState().inAudioChannel()) {
            return true;
        }

        VoiceChannel userChannel = (VoiceChannel) member.getVoiceState().getChannel();
        VoiceChannel botChannel = (VoiceChannel) bot.getVoiceState().getChannel();

        if (!userChannel.equals(botChannel)) {
            hook.editOriginalEmbeds(
                EmbedFactory.error(
                    Translation.t(guildId, "voice_not_same_channel_title"),
                    Translation.t(guildId, "voice_not_same_channel_desc")
                )
            ).queue();
            logger.warn("Usuário {} tentou usar comando em canal diferente do bot no servidor {}",
                member.getUser().getName(), guildId);
            return false;
        }

        return true;
    }

    public static boolean validateDeferred(InteractionHook hook, Member member, Member bot, long guildId, boolean requireBotInChannel) {
        if (!isUserInSameChannelDeferred(hook, member, bot, guildId)) {
            return false;
        }

        if (requireBotInChannel && !bot.getVoiceState().inAudioChannel()) {
            hook.editOriginalEmbeds(
                EmbedFactory.error(
                    Translation.t(guildId, "voice_bot_not_in_channel_title"),
                    Translation.t(guildId, "voice_bot_not_in_channel_desc")
                )
            ).queue();
            logger.warn("Comando requer bot em canal mas bot não está conectado no servidor {}", guildId);
            return false;
        }

        return true;
    }
}
