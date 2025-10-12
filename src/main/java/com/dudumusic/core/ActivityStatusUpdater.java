package com.dudumusic.core;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ActivityStatusUpdater {
    private static final Logger logger = LoggerFactory.getLogger(ActivityStatusUpdater.class);
    private static final List<String> LANGUAGES = List.of("ptbr", "en", "es");
    private static final long UPDATE_INTERVAL_SECONDS = 30;

    private final JDA jda;
    private final ScheduledExecutorService scheduler;
    private int currentLanguageIndex = 0;

    public ActivityStatusUpdater(JDA jda) {
        this.jda = jda;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        updateStatus();

        scheduler.scheduleAtFixedRate(
                this::updateStatus,
                UPDATE_INTERVAL_SECONDS,
                UPDATE_INTERVAL_SECONDS,
                TimeUnit.SECONDS
        );

        logger.info("Activity status updater iniciado (cicla a cada {} segundos)", UPDATE_INTERVAL_SECONDS);
    }

    public void stop() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("Activity status updater parado");
    }

    private void updateStatus() {
        try {
            String lang = LANGUAGES.get(currentLanguageIndex);
            String translatedStatus = getTranslation(lang, "activity_status");

            jda.getPresence().setActivity(Activity.customStatus(translatedStatus));

            currentLanguageIndex = (currentLanguageIndex + 1) % LANGUAGES.size();

            logger.debug("Status atualizado para: {} ({})", translatedStatus, lang);
        } catch (Exception e) {
            logger.error("Erro ao atualizar status de atividade", e);
        }
    }

    private String getTranslation(String lang, String key) {
        long tempGuildId = switch (lang) {
            case "en" -> Long.MAX_VALUE - 1;
            case "es" -> Long.MAX_VALUE - 2;
            default -> 0;
        };

        LanguageManager.setLanguage(tempGuildId, lang);
        String result = Translation.t(tempGuildId, key);

        return result;
    }
}
