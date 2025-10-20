package com.dudumusic.core;

import java.util.Map;
import java.util.HashMap;

// sim. isso aqui obviamente foi feito por IA. eu nao tenho paciencia p traduzir cada coisa xD

public class Translation {
    private static final Map<String, Map<String, String>> messages = new HashMap<>();

    static {
        // ========== PORTUGUÊS BR ==========
        Map<String, String> pt = new HashMap<>();

        // Language Command
        pt.put("language_set", "Idioma definido para: %s");
        pt.put("language_invalid", "Idioma inválido. Opções: ptbr, en, es");

        // Jump Command
        pt.put("jump_invalid", "Posição inválida na fila");
        pt.put("jump_no_queue", "Não há músicas suficientes na fila");
        pt.put("jump_done", "Pulando para: %s");

        // Switch Command
        pt.put("switch_no_manager_title", "Erro");
        pt.put("switch_no_manager_desc", "Não há nenhuma música na fila");
        pt.put("switch_invalid_title", "Posição inválida");
        pt.put("switch_invalid_desc", "As posições devem ser maiores que zero");
        pt.put("switch_invalid_queue", "Uma ou ambas as posições estão fora do alcance da fila");
        pt.put("switch_same_position_title", "Mesma posição");
        pt.put("switch_same_position_desc", "A música já está nessa posição");
        pt.put("switch_title", "Posição alterada");
        pt.put("switch_desc", "**%s** movida da posição %d para %d");

        // Erase Command
        pt.put("erase_no_manager_title", "Erro");
        pt.put("erase_no_manager_desc", "Não há nenhuma música na fila");
        pt.put("erase_invalid_title", "Posição inválida");
        pt.put("erase_invalid_desc", "A posição deve ser maior que zero");
        pt.put("erase_not_found", "Não há música nessa posição da fila");
        pt.put("erase_title", "Música removida");
        pt.put("erase_desc", "**%s** removida da posição %d");

        // Rewind Command
        pt.put("rewind_no_manager_title", "Erro");
        pt.put("rewind_no_manager_desc", "Não há nenhuma música tocando");
        pt.put("rewind_no_previous_title", "Sem música anterior");
        pt.put("rewind_no_previous_desc", "Não há música anterior para retornar");
        pt.put("rewind_title", "Voltando");
        pt.put("rewind_desc", "Voltando para: **%s**");

        // PreviousQueue Command
        pt.put("previousqueue_no_manager_title", "Erro");
        pt.put("previousqueue_no_manager_desc", "Não há player ativo");
        pt.put("previousqueue_no_history_title", "Sem histórico");
        pt.put("previousqueue_no_history_desc", "Não há filas anteriores salvas");
        pt.put("previousqueue_title", "Filas Anteriores");
        pt.put("previousqueue_select_prompt", "Selecione uma fila anterior para restaurar:");
        pt.put("previousqueue_tracks", "%d músicas");
        pt.put("previousqueue_invalid_selection", "Seleção inválida");
        pt.put("previousqueue_restored_title", "Fila restaurada");
        pt.put("previousqueue_restored_desc", "**%d músicas** restauradas de: %s");
        pt.put("time_seconds_ago", "%d segundos atrás");
        pt.put("time_minutes_ago", "%d minutos atrás");
        pt.put("time_hours_ago", "%d horas atrás");
        pt.put("time_days_ago", "%d dias atrás");

        // Help Command
        pt.put("help_title", "Comandos disponíveis");
        pt.put("help_desc", "Lista de comandos:");

        // Skip Command
        pt.put("skip_nothing_playing", "Nada tocando");
        pt.put("skip_no_track", "Não há nenhuma música tocando no momento");
        pt.put("skip_title", "Pulado");
        pt.put("skip_desc", "Pulou: **%s**");

        // Stop Command
        pt.put("stop_title", "Parado");
        pt.put("stop_desc", "Reprodução parada e fila limpa");

        // Pause Command
        pt.put("pause_nothing_playing", "Nada tocando");
        pt.put("pause_no_track", "Não há nenhuma música tocando no momento");
        pt.put("pause_already_paused_title", "Já pausado");
        pt.put("pause_already_paused_desc", "A reprodução já está pausada");
        pt.put("pause_title", "Pausado");
        pt.put("pause_desc", "Reprodução pausada");

        // Resume Command
        pt.put("resume_nothing_playing", "Nada tocando");
        pt.put("resume_no_track", "Não há nenhuma música tocando no momento");
        pt.put("resume_not_paused_title", "Não pausado");
        pt.put("resume_not_paused_desc", "A reprodução não está pausada");
        pt.put("resume_title", "Retomado");
        pt.put("resume_desc", "Reprodução retomada");

        // Volume Command
        pt.put("volume_title", "Volume ajustado");
        pt.put("volume_desc", "Volume definido para **%d%%**\n%s");

        // Loop Command
        pt.put("loop_mode_off", "Desligado");
        pt.put("loop_mode_track", "Musica");
        pt.put("loop_mode_queue", "Fila");
        pt.put("loop_desc_off", "Repetição desativada");
        pt.put("loop_desc_track", "A música atual será repetida");
        pt.put("loop_desc_queue", "A fila será repetida");
        pt.put("loop_title", "Modo de repetição: %s");

        // Shuffle Command
        pt.put("shuffle_empty_title", "Fila vazia");
        pt.put("shuffle_empty_desc", "A fila está vazia");
        pt.put("shuffle_title", "Embaralhado");
        pt.put("shuffle_desc", "Embaralhou %d músicas");

        // Clear Command
        pt.put("clear_empty_title", "Fila vazia");
        pt.put("clear_empty_desc", "A fila já está vazia");
        pt.put("clear_title", "Fila limpa");
        pt.put("clear_desc", "Removeu %d músicas da fila");

        // Seek Command
        pt.put("seek_nothing_playing", "Nada tocando");
        pt.put("seek_no_track", "Não há nenhuma música tocando no momento");
        pt.put("seek_not_seekable_title", "Não é possível pular");
        pt.put("seek_not_seekable_desc", "Esta música não permite pular posições (ex: Transmissão Ao Vivo)");
        pt.put("seek_invalid_title", "Posição inválida");
        pt.put("seek_invalid_desc", "Posição excede a duração da música (%s)");
        pt.put("seek_title", "Posição alterada");
        pt.put("seek_desc", "Pulou para **%s**");

        // Queue Command
        pt.put("queue_empty_title", "Fila vazia");
        pt.put("queue_empty_desc", "Não há músicas na fila");
        pt.put("queue_title", "Fila de Músicas");
        pt.put("queue_now_playing", "Tocando agora");
        pt.put("queue_up_next", "Próximas músicas");
        pt.put("queue_no_songs", "Nenhuma música na fila");
        pt.put("queue_footer", "Pagina %d/%d - %d músicas - Total: %s");
        pt.put("queue_btn_clear", "Limpar fila");

        // NowPlaying Command
        pt.put("nowplaying_nothing_title", "Nada tocando");
        pt.put("nowplaying_nothing_desc", "Não há nenhuma música tocando no momento");
        pt.put("nowplaying_title", "Tocando agora");
        pt.put("nowplaying_artist", "Artista");
        pt.put("nowplaying_duration", "Duração");
        pt.put("nowplaying_volume", "Volume");
        pt.put("nowplaying_progress", "Progresso");
        pt.put("nowplaying_live", "TRANSMISSAO AO VIVO");
        pt.put("nowplaying_loop_mode", "Modo de repetição");
        pt.put("nowplaying_status", "Status");
        pt.put("nowplaying_status_paused", "Pausado");
        pt.put("nowplaying_status_playing", "Tocando");
        pt.put("nowplaying_queue_size", "Músicas na fila");

        // PlayCommand
        pt.put("play_error_member", "Erro");
        pt.put("play_error_member_desc", "Não foi possível encontrar informações do membro");
        pt.put("play_not_in_voice_title", "Não está em um canal de voz");
        pt.put("play_not_in_voice_desc", "Você precisa estar em um canal de voz para tocar música!");
        pt.put("play_added_to_queue", "Adicionado à fila");
        pt.put("play_channel", "Canal");
        pt.put("play_duration", "Duração");
        pt.put("play_position", "Posição na fila");
        pt.put("play_playlist_empty_title", "Playlist vazia");
        pt.put("play_playlist_empty_desc", "Esta playlist não tem músicas");
        pt.put("play_playlist_added", "Playlist adicionada");
        pt.put("play_playlist_adding", "Adicionando playlist...");
        pt.put("play_playlist_songs_added", "Músicas adicionadas");
        pt.put("play_playlist_songs_total", "Músicas totais");
        pt.put("play_playlist_total_duration", "Duração total");
        pt.put("play_no_matches_title", "Nenhum resultado");
        pt.put("play_no_matches_desc", "Não foi possível encontrar nenhuma música com sua busca");
        pt.put("play_load_failed_title", "Falha ao carregar");
        pt.put("play_load_failed_desc", "Falha ao carregar música: %s");
        pt.put("play_load_failed_retry", "Falha ao carregar música após %d tentativas: %s");
        pt.put("play_playlist_convert_fail_title", "Playlists do Apple Music não suportadas");
        pt.put("play_playlist_convert_fail_desc", "Playlists do Apple Music ainda não são suportadas. Use Spotify, YouTube ou Deezer, ou cole músicas individuais do Apple Music!");
        pt.put("play_apple_music_unsupported_title", "Apple Music não suportado");
        pt.put("play_apple_music_unsupported_desc", "Infelizmente, músicas do Apple Music não são mais suportadas porque a Apple encripta o site, impossibilitando a extração de informações.\n\nUse **Spotify**, **YouTube** ou **Deezer** como alternativa!");
        pt.put("play_invalid_track_title", "Música inválida");
        pt.put("play_invalid_track_desc", "Esta música não está disponível ou foi removida do Spotify");

        // TrackScheduler
        pt.put("track_now_playing", "Tocando agora");
        pt.put("track_artist", "Artista/Canal");
        pt.put("track_duration", "Duração");
        pt.put("track_untitled", "Música sem nome");

        // EmbedFactory
        pt.put("embed_requested_by", "Requisitado por %s");

        // Voice State
        pt.put("voice_not_in_channel_title", "Não está em um canal de voz");
        pt.put("voice_not_in_channel_desc", "Você precisa estar em um canal de voz!");
        pt.put("voice_user_must_be_in_channel", "Você precisa estar em um canal de voz para usar este comando!");
        pt.put("voice_not_same_channel_title", "Canal de voz diferente");
        pt.put("voice_not_same_channel_desc", "Você precisa estar no mesmo canal de voz que eu!");
        pt.put("voice_bot_not_in_channel_title", "Bot não está em canal");
        pt.put("voice_bot_not_in_channel_desc", "Não estou tocando nada no momento!");
        pt.put("voice_disconnected_title", "Desconectado");
        pt.put("voice_disconnected_desc", "Fui desconectado do canal de voz.\n\nA fila foi salva e pode ser restaurada com /previousqueue");
        pt.put("voice_alone_title", "Desconectado por inatividade");
        pt.put("voice_alone_desc", "Todos saíram da call, então me desconectei após %d minutos de espera.\n\nA fila foi salva e pode ser restaurada com /previousqueue");

        // ButtonListener
        pt.put("button_error", "Ocorreu um erro ao processar sua solicitação. Tente novamente");
        pt.put("button_first_page", "Já está na primeira página!");
        pt.put("button_last_page", "Já está na última página!");

        // Command Descriptions
        pt.put("cmd_play_desc", "Toca uma música ou playlist pelo nome ou URL");
        pt.put("cmd_stop_desc", "Para a música e limpa a fila");
        pt.put("cmd_skip_desc", "Pula a música atual");
        pt.put("cmd_pause_desc", "Pausa a música atual");
        pt.put("cmd_resume_desc", "Retoma a música pausada");
        pt.put("cmd_volume_desc", "Ajusta o volume da reprodução");
        pt.put("cmd_loop_desc", "Define o modo de repetição (desligado, música, fila)");
        pt.put("cmd_shuffle_desc", "Embaralha a fila de músicas");
        pt.put("cmd_seek_desc", "Pula para uma posição específica na música");
        pt.put("cmd_clear_desc", "Limpa toda a fila de músicas");
        pt.put("cmd_queue_desc", "Mostra a fila de músicas");
        pt.put("cmd_nowplaying_desc", "Mostra a música que está tocando agora");
        pt.put("cmd_language_desc", "Define o idioma do bot para este servidor");
        pt.put("cmd_jump_desc", "Pula para uma música específica na fila");
        pt.put("cmd_switch_desc", "Troca a posição de uma música na fila");
        pt.put("cmd_erase_desc", "Remove uma música específica da fila");
        pt.put("cmd_rewind_desc", "Volta para a música anterior");
        pt.put("cmd_previousqueue_desc", "Restaura uma fila anterior");
        pt.put("cmd_help_desc", "Mostra os comandos disponíveis");

        // Activity Status
        pt.put("activity_status", "feito por @tomazdudux");

        // ========== ENGLISH ==========
        Map<String, String> en = new HashMap<>();

        // Language Command
        en.put("language_set", "Language set to: %s");
        en.put("language_invalid", "Invalid language. Options: ptbr, en, es");

        // Jump Command
        en.put("jump_invalid", "Invalid position in queue");
        en.put("jump_no_queue", "There are not enough tracks in the queue");
        en.put("jump_done", "Jumping to: %s");

        // Switch Command
        en.put("switch_no_manager_title", "Error");
        en.put("switch_no_manager_desc", "There are no tracks in the queue");
        en.put("switch_invalid_title", "Invalid position");
        en.put("switch_invalid_desc", "Positions must be greater than zero");
        en.put("switch_invalid_queue", "One or both positions are out of queue range");
        en.put("switch_same_position_title", "Same position");
        en.put("switch_same_position_desc", "The track is already in that position");
        en.put("switch_title", "Position changed");
        en.put("switch_desc", "**%s** moved from position %d to %d");

        // Erase Command
        en.put("erase_no_manager_title", "Error");
        en.put("erase_no_manager_desc", "There are no tracks in the queue");
        en.put("erase_invalid_title", "Invalid position");
        en.put("erase_invalid_desc", "Position must be greater than zero");
        en.put("erase_not_found", "No track found at that queue position");
        en.put("erase_title", "Track removed");
        en.put("erase_desc", "**%s** removed from position %d");

        // Rewind Command
        en.put("rewind_no_manager_title", "Error");
        en.put("rewind_no_manager_desc", "There is no track playing");
        en.put("rewind_no_previous_title", "No previous track");
        en.put("rewind_no_previous_desc", "There is no previous track to return to");
        en.put("rewind_title", "Rewinding");
        en.put("rewind_desc", "Returning to: **%s**");

        // PreviousQueue Command
        en.put("previousqueue_no_manager_title", "Error");
        en.put("previousqueue_no_manager_desc", "There is no active player");
        en.put("previousqueue_no_history_title", "No history");
        en.put("previousqueue_no_history_desc", "There are no previous queues saved");
        en.put("previousqueue_title", "Previous Queues");
        en.put("previousqueue_select_prompt", "Select a previous queue to restore:");
        en.put("previousqueue_tracks", "%d tracks");
        en.put("previousqueue_invalid_selection", "Invalid selection");
        en.put("previousqueue_restored_title", "Queue restored");
        en.put("previousqueue_restored_desc", "**%d tracks** restored from: %s");
        en.put("time_seconds_ago", "%d seconds ago");
        en.put("time_minutes_ago", "%d minutes ago");
        en.put("time_hours_ago", "%d hours ago");
        en.put("time_days_ago", "%d days ago");

        // Help Command
        en.put("help_title", "Available commands");
        en.put("help_desc", "List of commands:");

        // Skip Command
        en.put("skip_nothing_playing", "Nothing playing");
        en.put("skip_no_track", "There is no track currently playing");
        en.put("skip_title", "Skipped");
        en.put("skip_desc", "Skipped: **%s**");

        // Stop Command
        en.put("stop_title", "Stopped");
        en.put("stop_desc", "Playback stopped and queue cleared");

        // Pause Command
        en.put("pause_nothing_playing", "Nothing playing");
        en.put("pause_no_track", "There is no track currently playing");
        en.put("pause_already_paused_title", "Already paused");
        en.put("pause_already_paused_desc", "Playback is already paused");
        en.put("pause_title", "Paused");
        en.put("pause_desc", "Playback paused");

        // Resume Command
        en.put("resume_nothing_playing", "Nothing playing");
        en.put("resume_no_track", "There is no track currently playing");
        en.put("resume_not_paused_title", "Not paused");
        en.put("resume_not_paused_desc", "Playback is not paused");
        en.put("resume_title", "Resumed");
        en.put("resume_desc", "Playback resumed");

        // Volume Command
        en.put("volume_title", "Volume adjusted");
        en.put("volume_desc", "Volume set to **%d%%**\n%s");

        // Loop Command
        en.put("loop_mode_off", "Off");
        en.put("loop_mode_track", "Track");
        en.put("loop_mode_queue", "Queue");
        en.put("loop_desc_off", "Loop disabled");
        en.put("loop_desc_track", "Current track will repeat");
        en.put("loop_desc_queue", "Queue will repeat");
        en.put("loop_title", "Loop mode: %s");

        // Shuffle Command
        en.put("shuffle_empty_title", "Empty queue");
        en.put("shuffle_empty_desc", "The queue is empty");
        en.put("shuffle_title", "Shuffled");
        en.put("shuffle_desc", "Shuffled %d tracks");

        // Clear Command
        en.put("clear_empty_title", "Empty queue");
        en.put("clear_empty_desc", "The queue is already empty");
        en.put("clear_title", "Queue cleared");
        en.put("clear_desc", "Removed %d tracks from the queue");

        // Seek Command
        en.put("seek_nothing_playing", "Nothing playing");
        en.put("seek_no_track", "There is no track currently playing");
        en.put("seek_not_seekable_title", "Cannot seek");
        en.put("seek_not_seekable_desc", "This track doesn't support seeking (e.g. Live Stream)");
        en.put("seek_invalid_title", "Invalid position");
        en.put("seek_invalid_desc", "Position exceeds track duration (%s)");
        en.put("seek_title", "Position changed");
        en.put("seek_desc", "Jumped to **%s**");

        // Queue Command
        en.put("queue_empty_title", "Empty queue");
        en.put("queue_empty_desc", "There are no tracks in the queue");
        en.put("queue_title", "Music Queue");
        en.put("queue_now_playing", "Now playing");
        en.put("queue_up_next", "Up next");
        en.put("queue_no_songs", "No tracks in queue");
        en.put("queue_footer", "Page %d/%d - %d tracks - Total: %s");
        en.put("queue_btn_clear", "Clear queue");

        // NowPlaying Command
        en.put("nowplaying_nothing_title", "Nothing playing");
        en.put("nowplaying_nothing_desc", "There is no track currently playing");
        en.put("nowplaying_title", "Now playing");
        en.put("nowplaying_artist", "Artist");
        en.put("nowplaying_duration", "Duration");
        en.put("nowplaying_volume", "Volume");
        en.put("nowplaying_progress", "Progress");
        en.put("nowplaying_live", "LIVE STREAM");
        en.put("nowplaying_loop_mode", "Loop mode");
        en.put("nowplaying_status", "Status");
        en.put("nowplaying_status_paused", "Paused");
        en.put("nowplaying_status_playing", "Playing");
        en.put("nowplaying_queue_size", "Tracks in queue");

        // PlayCommand
        en.put("play_error_member", "Error");
        en.put("play_error_member_desc", "Could not find member information");
        en.put("play_not_in_voice_title", "Not in a voice channel");
        en.put("play_not_in_voice_desc", "You need to be in a voice channel to play music!");
        en.put("play_added_to_queue", "Added to queue");
        en.put("play_channel", "Channel");
        en.put("play_duration", "Duration");
        en.put("play_position", "Queue position");
        en.put("play_playlist_empty_title", "Empty playlist");
        en.put("play_playlist_empty_desc", "This playlist has no tracks");
        en.put("play_playlist_added", "Playlist added");
        en.put("play_playlist_adding", "Adding playlist...");
        en.put("play_playlist_songs_added", "Tracks added");
        en.put("play_playlist_songs_total", "Total tracks");
        en.put("play_playlist_total_duration", "Total duration");
        en.put("play_no_matches_title", "No results");
        en.put("play_no_matches_desc", "Could not find any tracks matching your search");
        en.put("play_load_failed_title", "Failed to load");
        en.put("play_load_failed_desc", "Failed to load track: %s");
        en.put("play_load_failed_retry", "Failed to load track after %d attempts: %s");
        en.put("play_playlist_convert_fail_title", "Apple Music Playlists Not Supported");
        en.put("play_playlist_convert_fail_desc", "Apple Music playlists are not yet supported. Use Spotify, YouTube, or Deezer, or paste individual songs from Apple Music!");
        en.put("play_apple_music_unsupported_title", "Apple Music Not Supported");
        en.put("play_apple_music_unsupported_desc", "Unfortunately, Apple Music tracks are no longer supported because Apple encrypts their website, making it impossible to extract information.\n\nUse **Spotify**, **YouTube**, or **Deezer** as an alternative!");
        en.put("play_invalid_track_title", "Invalid track");
        en.put("play_invalid_track_desc", "This track is unavailable or has been removed from Spotify");

        // TrackScheduler
        en.put("track_now_playing", "Now playing");
        en.put("track_artist", "Artist/Channel");
        en.put("track_duration", "Duration");
        en.put("track_untitled", "Untitled track");

        // EmbedFactory
        en.put("embed_requested_by", "Requested by %s");

        // Voice State
        en.put("voice_not_in_channel_title", "Not in a voice channel");
        en.put("voice_not_in_channel_desc", "You need to be in a voice channel!");
        en.put("voice_user_must_be_in_channel", "You need to be in a voice channel to use this command!");
        en.put("voice_not_same_channel_title", "Different voice channel");
        en.put("voice_not_same_channel_desc", "You need to be in the same voice channel as me!");
        en.put("voice_bot_not_in_channel_title", "Bot not in channel");
        en.put("voice_bot_not_in_channel_desc", "I'm not playing anything right now!");
        en.put("voice_disconnected_title", "Disconnected");
        en.put("voice_disconnected_desc", "I was disconnected from the voice channel.\n\nThe queue has been saved and can be restored with /previousqueue");
        en.put("voice_alone_title", "Disconnected due to inactivity");
        en.put("voice_alone_desc", "Everyone left the call, so I disconnected after %d minutes of waiting.\n\nThe queue has been saved and can be restored with /previousqueue");

        // ButtonListener
        en.put("button_error", "An error occurred while processing your request. Try again");
        en.put("button_first_page", "Already on the first page!");
        en.put("button_last_page", "Already on the last page!");

        // Command Descriptions
        en.put("cmd_play_desc", "Plays a song or playlist by name or URL");
        en.put("cmd_stop_desc", "Stops the music and clears the queue");
        en.put("cmd_skip_desc", "Skips the current track");
        en.put("cmd_pause_desc", "Pauses the current track");
        en.put("cmd_resume_desc", "Resumes the paused track");
        en.put("cmd_volume_desc", "Adjusts the playback volume");
        en.put("cmd_loop_desc", "Sets the loop mode (off, track, queue)");
        en.put("cmd_shuffle_desc", "Shuffles the music queue");
        en.put("cmd_seek_desc", "Jumps to a specific position in the track");
        en.put("cmd_clear_desc", "Clears the entire music queue");
        en.put("cmd_queue_desc", "Shows the music queue");
        en.put("cmd_nowplaying_desc", "Shows the currently playing track");
        en.put("cmd_language_desc", "Sets the bot language for this server");
        en.put("cmd_jump_desc", "Jumps to a specific track in the queue");
        en.put("cmd_switch_desc", "Switches the position of a track in the queue");
        en.put("cmd_erase_desc", "Removes a specific track from the queue");
        en.put("cmd_rewind_desc", "Returns to the previous track");
        en.put("cmd_previousqueue_desc", "Restores a previous queue");
        en.put("cmd_help_desc", "Shows available commands");

        // Activity Status
        en.put("activity_status", "made by @tomazdudux");

        // ========== ESPAÑOL ==========
        Map<String, String> es = new HashMap<>();

        // Language Command
        es.put("language_set", "Idioma establecido a: %s");
        es.put("language_invalid", "Idioma inválido. Opciones: ptbr, en, es");

        // Jump Command
        es.put("jump_invalid", "Posición inválida en la cola");
        es.put("jump_no_queue", "No hay suficientes pistas en la cola");
        es.put("jump_done", "Saltando a: %s");

        // Switch Command
        es.put("switch_no_manager_title", "Error");
        es.put("switch_no_manager_desc", "No hay pistas en la cola");
        es.put("switch_invalid_title", "Posición inválida");
        es.put("switch_invalid_desc", "Las posiciones deben ser mayores que cero");
        es.put("switch_invalid_queue", "Una o ambas posiciones están fuera del rango de la cola");
        es.put("switch_same_position_title", "Misma posición");
        es.put("switch_same_position_desc", "La pista ya está en esa posición");
        es.put("switch_title", "Posición cambiada");
        es.put("switch_desc", "**%s** movida de la posición %d a %d");

        // Erase Command
        es.put("erase_no_manager_title", "Error");
        es.put("erase_no_manager_desc", "No hay pistas en la cola");
        es.put("erase_invalid_title", "Posición inválida");
        es.put("erase_invalid_desc", "La posición debe ser mayor que cero");
        es.put("erase_not_found", "No se encontró pista en esa posición de la cola");
        es.put("erase_title", "Pista eliminada");
        es.put("erase_desc", "**%s** eliminada de la posición %d");

        // Rewind Command
        es.put("rewind_no_manager_title", "Error");
        es.put("rewind_no_manager_desc", "No hay ninguna pista reproduciéndose");
        es.put("rewind_no_previous_title", "Sin pista anterior");
        es.put("rewind_no_previous_desc", "No hay pista anterior para volver");
        es.put("rewind_title", "Retrocediendo");
        es.put("rewind_desc", "Volviendo a: **%s**");

        // PreviousQueue Command
        es.put("previousqueue_no_manager_title", "Error");
        es.put("previousqueue_no_manager_desc", "No hay reproductor activo");
        es.put("previousqueue_no_history_title", "Sin historial");
        es.put("previousqueue_no_history_desc", "No hay colas anteriores guardadas");
        es.put("previousqueue_title", "Colas Anteriores");
        es.put("previousqueue_select_prompt", "Selecciona una cola anterior para restaurar:");
        es.put("previousqueue_tracks", "%d pistas");
        es.put("previousqueue_invalid_selection", "Selección inválida");
        es.put("previousqueue_restored_title", "Cola restaurada");
        es.put("previousqueue_restored_desc", "**%d pistas** restauradas de: %s");
        es.put("time_seconds_ago", "hace %d segundos");
        es.put("time_minutes_ago", "hace %d minutos");
        es.put("time_hours_ago", "hace %d horas");
        es.put("time_days_ago", "hace %d días");

        // Help Command
        es.put("help_title", "Comandos disponibles");
        es.put("help_desc", "Lista de comandos:");

        // Skip Command
        es.put("skip_nothing_playing", "Nada reproduciéndose");
        es.put("skip_no_track", "No hay ninguna pista reproduciéndose en este momento");
        es.put("skip_title", "Saltado");
        es.put("skip_desc", "Saltó: **%s**");

        // Stop Command
        es.put("stop_title", "Detenido");
        es.put("stop_desc", "Reproducción detenida y cola limpiada");

        // Pause Command
        es.put("pause_nothing_playing", "Nada reproduciéndose");
        es.put("pause_no_track", "No hay ninguna pista reproduciéndose en este momento");
        es.put("pause_already_paused_title", "Ya pausado");
        es.put("pause_already_paused_desc", "La reproducción ya está pausada");
        es.put("pause_title", "Pausado");
        es.put("pause_desc", "Reproducción pausada");

        // Resume Command
        es.put("resume_nothing_playing", "Nada reproduciéndose");
        es.put("resume_no_track", "No hay ninguna pista reproduciéndose en este momento");
        es.put("resume_not_paused_title", "No pausado");
        es.put("resume_not_paused_desc", "La reproducción no está pausada");
        es.put("resume_title", "Reanudado");
        es.put("resume_desc", "Reproducción reanudada");

        // Volume Command
        es.put("volume_title", "Volumen ajustado");
        es.put("volume_desc", "Volumen establecido a **%d%%**\n%s");

        // Loop Command
        es.put("loop_mode_off", "Desactivado");
        es.put("loop_mode_track", "Pista");
        es.put("loop_mode_queue", "Cola");
        es.put("loop_desc_off", "Repetición desactivada");
        es.put("loop_desc_track", "La pista actual se repetirá");
        es.put("loop_desc_queue", "La cola se repetirá");
        es.put("loop_title", "Modo de repetición: %s");

        // Shuffle Command
        es.put("shuffle_empty_title", "Cola vacía");
        es.put("shuffle_empty_desc", "La cola está vacía");
        es.put("shuffle_title", "Mezclado");
        es.put("shuffle_desc", "Mezcló %d pistas");

        // Clear Command
        es.put("clear_empty_title", "Cola vacía");
        es.put("clear_empty_desc", "La cola ya está vacía");
        es.put("clear_title", "Cola limpiada");
        es.put("clear_desc", "Eliminó %d pistas de la cola");

        // Seek Command
        es.put("seek_nothing_playing", "Nada reproduciéndose");
        es.put("seek_no_track", "No hay ninguna pista reproduciéndose en este momento");
        es.put("seek_not_seekable_title", "No se puede buscar");
        es.put("seek_not_seekable_desc", "Esta pista no permite buscar posiciones (ej: Transmisión en Vivo)");
        es.put("seek_invalid_title", "Posición inválida");
        es.put("seek_invalid_desc", "La posición excede la duración de la pista (%s)");
        es.put("seek_title", "Posición cambiada");
        es.put("seek_desc", "Saltó a **%s**");

        // Queue Command
        es.put("queue_empty_title", "Cola vacía");
        es.put("queue_empty_desc", "No hay pistas en la cola");
        es.put("queue_title", "Cola de Música");
        es.put("queue_now_playing", "Reproduciendo ahora");
        es.put("queue_up_next", "Próximas pistas");
        es.put("queue_no_songs", "No hay pistas en la cola");
        es.put("queue_footer", "Página %d/%d - %d pistas - Total: %s");
        es.put("queue_btn_clear", "Limpiar cola");

        // NowPlaying Command
        es.put("nowplaying_nothing_title", "Nada reproduciéndose");
        es.put("nowplaying_nothing_desc", "No hay ninguna pista reproduciéndose en este momento");
        es.put("nowplaying_title", "Reproduciendo ahora");
        es.put("nowplaying_artist", "Artista");
        es.put("nowplaying_duration", "Duración");
        es.put("nowplaying_volume", "Volumen");
        es.put("nowplaying_progress", "Progreso");
        es.put("nowplaying_live", "TRANSMISIÓN EN VIVO");
        es.put("nowplaying_loop_mode", "Modo de repetición");
        es.put("nowplaying_status", "Estado");
        es.put("nowplaying_status_paused", "Pausado");
        es.put("nowplaying_status_playing", "Reproduciendo");
        es.put("nowplaying_queue_size", "Pistas en cola");

        // PlayCommand
        es.put("play_error_member", "Error");
        es.put("play_error_member_desc", "No se pudo encontrar información del miembro");
        es.put("play_not_in_voice_title", "No está en un canal de voz");
        es.put("play_not_in_voice_desc", "¡Necesitas estar en un canal de voz para reproducir música!");
        es.put("play_added_to_queue", "Añadido a la cola");
        es.put("play_channel", "Canal");
        es.put("play_duration", "Duración");
        es.put("play_position", "Posición en cola");
        es.put("play_playlist_empty_title", "Playlist vacía");
        es.put("play_playlist_empty_desc", "Esta playlist no tiene pistas");
        es.put("play_playlist_added", "Playlist añadida");
        es.put("play_playlist_adding", "Añadiendo playlist...");
        es.put("play_playlist_songs_added", "Pistas añadidas");
        es.put("play_playlist_songs_total", "Pistas totales");
        es.put("play_playlist_total_duration", "Duración total");
        es.put("play_no_matches_title", "Sin resultados");
        es.put("play_no_matches_desc", "No se pudo encontrar ninguna pista con tu búsqueda");
        es.put("play_load_failed_title", "Falló al cargar");
        es.put("play_load_failed_desc", "Falló al cargar pista: %s");
        es.put("play_load_failed_retry", "Falló al cargar pista después de %d intentos: %s");
        es.put("play_playlist_convert_fail_title", "Playlists de Apple Music no soportadas");
        es.put("play_playlist_convert_fail_desc", "Las playlists de Apple Music aún no son soportadas. ¡Usa Spotify, YouTube o Deezer, o pega canciones individuales de Apple Music!");
        es.put("play_apple_music_unsupported_title", "Apple Music no soportado");
        es.put("play_apple_music_unsupported_desc", "Desafortunadamente, las pistas de Apple Music ya no son soportadas porque Apple encripta su sitio web, haciendo imposible extraer información.\n\n¡Use **Spotify**, **YouTube** o **Deezer** como alternativa!");
        es.put("play_invalid_track_title", "Pista inválida");
        es.put("play_invalid_track_desc", "Esta pista no está disponible o ha sido eliminada de Spotify");

        // TrackScheduler
        es.put("track_now_playing", "Reproduciendo ahora");
        es.put("track_artist", "Artista/Canal");
        es.put("track_duration", "Duración");
        es.put("track_untitled", "Pista sin nombre");

        // EmbedFactory
        es.put("embed_requested_by", "Solicitado por %s");

        // Voice State
        es.put("voice_not_in_channel_title", "No está en un canal de voz");
        es.put("voice_not_in_channel_desc", "¡Necesitas estar en un canal de voz!");
        es.put("voice_user_must_be_in_channel", "¡Necesitas estar en un canal de voz para usar este comando!");
        es.put("voice_not_same_channel_title", "Canal de voz diferente");
        es.put("voice_not_same_channel_desc", "¡Necesitas estar en el mismo canal de voz que yo!");
        es.put("voice_bot_not_in_channel_title", "Bot no está en canal");
        es.put("voice_bot_not_in_channel_desc", "¡No estoy reproduciendo nada en este momento!");
        es.put("voice_disconnected_title", "Desconectado");
        es.put("voice_disconnected_desc", "Fui desconectado del canal de voz.\n\nLa cola ha sido guardada y puede ser restaurada con /previousqueue");
        es.put("voice_alone_title", "Desconectado por inactividad");
        es.put("voice_alone_desc", "Todos salieron de la llamada, así que me desconecté después de %d minutos de espera.\n\nLa cola ha sido guardada y puede ser restaurada con /previousqueue");

        // ButtonListener
        es.put("button_error", "Ocurrió un error al procesar su solicitud. Inténtelo de nuevo");
        es.put("button_first_page", "¡Ya está en la primera página!");
        es.put("button_last_page", "¡Ya está en la última página!");

        // Command Descriptions
        es.put("cmd_play_desc", "Reproduce una canción o playlist por nombre o URL");
        es.put("cmd_stop_desc", "Detiene la música y limpia la cola");
        es.put("cmd_skip_desc", "Salta la pista actual");
        es.put("cmd_pause_desc", "Pausa la pista actual");
        es.put("cmd_resume_desc", "Reanuda la pista pausada");
        es.put("cmd_volume_desc", "Ajusta el volumen de reproducción");
        es.put("cmd_loop_desc", "Establece el modo de repetición (desactivado, pista, cola)");
        es.put("cmd_shuffle_desc", "Mezcla la cola de música");
        es.put("cmd_seek_desc", "Salta a una posición específica en la pista");
        es.put("cmd_clear_desc", "Limpia toda la cola de música");
        es.put("cmd_queue_desc", "Muestra la cola de música");
        es.put("cmd_nowplaying_desc", "Muestra la pista que se está reproduciendo ahora");
        es.put("cmd_language_desc", "Establece el idioma del bot para este servidor");
        es.put("cmd_jump_desc", "Salta a una pista específica en la cola");
        es.put("cmd_switch_desc", "Cambia la posición de una pista en la cola");
        es.put("cmd_erase_desc", "Elimina una pista específica de la cola");
        es.put("cmd_rewind_desc", "Vuelve a la pista anterior");
        es.put("cmd_previousqueue_desc", "Restaura una cola anterior");
        es.put("cmd_help_desc", "Muestra los comandos disponibles");

        // Activity Status
        es.put("activity_status", "hecho por @tomazdudux");

        messages.put("ptbr", pt);
        messages.put("en", en);
        messages.put("es", es);
    }

    public static String t(long guildId, String key, Object... args) {
        String lang = LanguageManager.getLanguage(guildId);
        Map<String, String> map = messages.getOrDefault(lang, messages.get("ptbr"));
        String template = map.getOrDefault(key, key);
        return String.format(template, args);
    }
}
