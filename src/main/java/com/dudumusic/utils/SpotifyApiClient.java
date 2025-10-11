package com.dudumusic.utils;

import com.dudumusic.core.BotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpotifyApiClient {
    private static final Logger logger = LoggerFactory.getLogger(SpotifyApiClient.class);
    private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";
    private static final String API_BASE_URL = "https://api.spotify.com/v1";

    private static String accessToken = null;
    private static long tokenExpiresAt = 0;

    private static final Map<String, String> playlistArtworkCache = new ConcurrentHashMap<>();

    public static class PlaylistInfo {
        public final String name;
        public final String artworkUrl;

        public PlaylistInfo(String name, String artworkUrl) {
            this.name = name;
            this.artworkUrl = artworkUrl;
        }
    }

    public static String getPlaylistArtwork(String playlistUrl) {
        String playlistId = extractPlaylistId(playlistUrl);
        if (playlistId == null) {
            logger.warn("Não foi possivel pegar ID pelo extrato da URL: {}", playlistUrl);
            return null;
        }

        if (playlistArtworkCache.containsKey(playlistId)) {
            logger.info("Usando artwork do cache para playlist: {}", playlistId);
            return playlistArtworkCache.get(playlistId);
        }

        PlaylistInfo info = fetchPlaylistInfo(playlistId);
        if (info != null && info.artworkUrl != null) {
            playlistArtworkCache.put(playlistId, info.artworkUrl);
            logger.info("Artwork da playlist {} obtida e cacheada: {}", playlistId, info.artworkUrl);
            return info.artworkUrl;
        }

        return null;
    }

    private static String extractPlaylistId(String url) {
        Pattern pattern = Pattern.compile("spotify\\.com/playlist/([a-zA-Z0-9]+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private static PlaylistInfo fetchPlaylistInfo(String playlistId) {
        try {
            if (!ensureValidToken()) {
                logger.error("Falha ao pegar token de acesso Spotify");
                return null;
            }

            String apiUrl = API_BASE_URL + "/playlists/" + playlistId + "?fields=name,images";
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
                );

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                String json = response.toString();

                String name = extractJsonField(json, "name");
                String artworkUrl = extractFirstImage(json);

                if (name != null && artworkUrl != null) {
                    logger.info("Informação da playlist obtida: {} (artwork: sim)", name);
                    return new PlaylistInfo(name, artworkUrl);
                }
            } else {
                logger.warn("Spotify API retornou status code: {} para a playlist: {}", responseCode, playlistId);
            }

            connection.disconnect();
        } catch (Exception e) {
            logger.error("Erro pegando informação da playlist do Spotify: {}", playlistId, e);
        }

        return null;
    }

    private static synchronized boolean ensureValidToken() {
        long currentTime = System.currentTimeMillis();
        if (accessToken != null && tokenExpiresAt > currentTime + 300000) {
            return true;
        }

        logger.info("Renovando token de acesso do Spotify...");

        try {
            String clientId = BotConfig.getSpotifyClientId();
            String clientSecret = BotConfig.getSpotifyClientSecret();

            if (clientId == null || clientSecret == null) {
                logger.error("Credenciais do Spotify não configuradas");
                return false;
            }

            String auth = clientId + ":" + clientSecret;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

            URL url = new URL(TOKEN_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Basic " + encodedAuth);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            String body = "grant_type=client_credentials";
            OutputStream os = connection.getOutputStream();
            os.write(body.getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
                );

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                String json = response.toString();

                accessToken = extractJsonField(json, "access_token");
                String expiresInStr = extractJsonField(json, "expires_in");

                if (accessToken != null && expiresInStr != null) {
                    int expiresIn = Integer.parseInt(expiresInStr);
                    tokenExpiresAt = currentTime + (expiresIn * 1000L);
                    logger.info("Token do Spotify renovado com sucesso (expira em {} segundos)", expiresIn);
                    return true;
                }
            } else {
                logger.error("Falha ao pegar token Spotify, status code: {}", responseCode);
            }

            connection.disconnect();
        } catch (Exception e) {
            logger.error("Falha ao pegar token Spotify", e);
        }

        return false;
    }

    private static String extractJsonField(String json, String fieldName) {
        Pattern pattern = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }

        pattern = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*([0-9]+)");
        matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    private static String extractFirstImage(String json) {
        Pattern pattern = Pattern.compile("\"images\"\\s*:\\s*\\[\\s*\\{[^}]*\"url\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
