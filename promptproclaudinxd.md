# DISCORD MUSIC BOT - PROMPT ENTERPRISE PARA CLAUDE CODE

## 🎯 OBJETIVO

Bot de música profissional para Discord com streaming em tempo real. Zero downloads locais. Arquitetura enterprise, código limpo, pronto para comunidades com centenas de usuários simultâneos.

---

## 📚 STACK COMPLETO

**Linguagem:** Java 21 (LTS)
**Build:** Maven 3.9+

**Dependências obrigatórias:**

- `net.dv8tion:JDA:5.2.1` - Discord API
- `dev.arbjerg:lavaplayer:2.2.2` - Audio streaming engine
- `dev.lavalink.youtube:youtube-plugin:1.9.1` - YouTube source
- `com.github.topi314.lavasrc:lavasrc:4.3.0` - Spotify/Apple/Deezer
- `ch.qos.logback:logback-classic:1.5.12` - Logging
- `io.github.cdimascio:dotenv-java:3.0.2` - Environment config

**Repositórios:**

- `https://jitpack.io`
- `https://maven.lavalink.dev/releases`

**Plugin Maven:** maven-shade-plugin para criar JAR executável com todas dependências.

---

## 🏗️ ARQUITETURA ENTERPRISE

### Camadas da Aplicação

**1. CORE LAYER** (inicialização e configuração)

- `Main.java` - Entry point, inicializa bot
- `BotConfig.java` - Carrega variáveis de ambiente
- `PlayerConfig.java` - Configura Lavaplayer e sources

**2. AUDIO LAYER** (gerenciamento de áudio)

- `MusicManager.java` - Gerencia player e scheduler por guild
- `TrackScheduler.java` - Fila e controle de reprodução
- `AudioHandler.java` - Envia pacotes Opus ao Discord (renomeado de AudioPlayerSendHandler)

**3. COMMAND LAYER** (lógica de comandos)

- Interface `Command.java` - Contrato para comandos
- `PlayCommand.java` - Tocar música/playlist
- `StopCommand.java` - Parar e limpar tudo
- `SkipCommand.java` - Pular música atual
- `QueueCommand.java` - Listar fila com paginação
- `NowPlayingCommand.java` - Informações da música atual
- `ShuffleCommand.java` - Embaralhar fila
- `PauseCommand.java` - Pausar reprodução
- `ResumeCommand.java` - Retomar reprodução
- `ClearCommand.java` - Limpar fila inteira
- `VolumeCommand.java` - Ajustar volume (0-100)
- `SeekCommand.java` - Avançar/voltar na música
- `LoopCommand.java` - Loop (música/fila/off)

**4. LISTENER LAYER** (event handlers)

- `CommandListener.java` - Processa slash commands
- `ButtonListener.java` - Processa cliques em botões (paginação)
- `VoiceListener.java` - Monitora eventos de voz (detecção de canal vazio)

**5. UTIL LAYER** (utilitários)

- `TimeFormat.java` - Formata durações (3:45, 1:23:45)
- `EmbedBuilder.java` - Cria embeds padronizados
- `ProgressBar.java` - Gera barra ASCII [████████--] 80%
- `SourceDetector.java` - Identifica tipo de URL (Spotify/YouTube/etc)

---

## 🎵 FONTES DE MÚSICA SUPORTADAS

**STREAMING DIRETO (sem conversão):**

- YouTube (via youtube-plugin)
- SoundCloud (via Lavaplayer)
- Bandcamp (via Lavaplayer)
- Vimeo (via Lavaplayer)
- Twitch streams (via Lavaplayer)
- HTTP/HTTPS direto (MP3, M4A, OGG, FLAC, WAV)

**VIA CONVERSÃO (metadados → busca YouTube):**

- Spotify (tracks, albums, playlists)
- Apple Music (tracks, albums, playlists)
- Deezer (tracks, albums, playlists)

**BUSCA POR TEXTO:**

- YouTube search (ytsearch:query)
- SoundCloud search (scsearch:query)

---

## 📁 ESTRUTURA DE PACOTES

```
src/main/java/com/musicbot/
├── Main.java
│
├── core/
│   ├── BotConfig.java
│   └── PlayerConfig.java
│
├── audio/
│   ├── MusicManager.java
│   ├── TrackScheduler.java
│   └── AudioHandler.java
│
├── commands/
│   ├── Command.java (interface)
│   ├── music/
│   │   ├── PlayCommand.java
│   │   ├── StopCommand.java
│   │   ├── SkipCommand.java
│   │   ├── PauseCommand.java
│   │   ├── ResumeCommand.java
│   │   ├── VolumeCommand.java
│   │   ├── SeekCommand.java
│   │   └── LoopCommand.java
│   └── info/
│       ├── QueueCommand.java
│       └── NowPlayingCommand.java
│
├── listeners/
│   ├── CommandListener.java
│   ├── ButtonListener.java
│   └── VoiceListener.java
│
└── utils/
    ├── TimeFormat.java
    ├── EmbedFactory.java
    ├── ProgressBar.java
    └── SourceDetector.java

src/main/resources/
├── logback.xml
└── .env.example
```

---

## ⚙️ PROCESSO DE INICIALIZAÇÃO (Main.java)

**ORDEM EXATA:**

1. **Carregar Configuração**

   ```
   BotConfig.load() → lê .env
   Valida TOKEN, SPOTIFY_CLIENT_ID, SPOTIFY_CLIENT_SECRET
   Se faltar alguma, lançar exceção com mensagem clara
   ```

2. **Configurar Audio Player**

   ```
   PlayerConfig.setup() cria AudioPlayerManager
   Registra YoutubeAudioSourceManager
   Registra SpotifySourceManager (com clientId/secret, país BR)
   Registra AudioSourceManagers.registerRemoteSources()
   Configura buffer: setFrameBufferDuration(5000)
   Retorna instância singleton
   ```

3. **Construir JDA**

   ```
   JDABuilder.createDefault(token)
   .enableIntents(GUILD_VOICE_STATES, GUILD_MESSAGES)
   .setActivity(Activity.listening("/play"))
   .addEventListeners(CommandListener, ButtonListener, VoiceListener)
   .build()
   .awaitReady()
   ```

4. **Registrar Comandos Slash**

   ```
   Para cada Command implementação:
   jda.upsertCommand(name, description)
      .addOptions(...)
      .queue()
   ```

5. **Log de Sucesso**
   ```
   Logger: "Bot iniciado com sucesso!"
   Logger: "Conectado como: {username}"
   Logger: "Guilds: {count}"
   Logger: "Comandos registrados: {count}"
   ```

---

## 🎼 FLUXO DE EXECUÇÃO DETALHADO

### COMANDO /play

**INPUT:** URL do Spotify ou busca "Artista - Música"

**PROCESSO:**

1. **Validação Inicial**

   ```
   event.deferReply() → Discord sabe que vai demorar
   Verificar se usuário está em VoiceChannel
   Se não: reply "Entre em um canal de voz primeiro!"
   ```

2. **Obter/Criar MusicManager**

   ```
   guildId = event.getGuild().getIdLong()
   musicManager = musicManagers.computeIfAbsent(guildId,
       id -> new MusicManager(playerManager))
   ```

3. **Conectar ao Canal de Voz**

   ```
   Se bot não conectado:
       voiceChannel = event.getMember().getVoiceState().getChannel()
       guild.getAudioManager().openAudioConnection(voiceChannel)
       guild.getAudioManager().setSendingHandler(musicManager.getAudioHandler())
   ```

4. **Carregar Música**

   ```
   playerManager.loadItemOrdered(musicManager, trackUrl, resultHandler)

   ResultHandler processa 4 casos:

   A) trackLoaded(track):
      - Adicionar à fila: scheduler.queue(track)
      - Embed: thumbnail, título, artista, duração
      - Se primeira da fila, começar a tocar

   B) playlistLoaded(playlist):
      - Iterar tracks: tracks.forEach(scheduler::queue)
      - Embed: "Adicionadas {count} músicas"
      - Começar primeira

   C) noMatches():
      - Embed erro: "Música não encontrada"

   D) loadFailed(exception):
      - Embed erro: "Falha ao carregar: {message}"
      - Logger.error com stack trace
   ```

5. **TrackScheduler.queue(track)**
   ```
   Se player.getPlayingTrack() == null:
       player.playTrack(track)
       Enviar embed "Tocando agora"
   Senão:
       queue.offer(track)
       Enviar embed "Adicionado à fila: posição {queue.size()}"
   ```

### COMANDO /queue

**PAGINAÇÃO COM BOTÕES:**

1. **Calcular Páginas**

   ```
   tracksPerPage = 10
   totalPages = Math.ceil(queue.size() / tracksPerPage)
   currentPage = 0
   ```

2. **Criar Embed**

   ```
   Para cada track na página atual:
   {posição}. {título} - {artista} [{duração}]

   Footer: "Página {current}/{total} • {queue.size()} músicas"
   ```

3. **Adicionar Botões**

   ```
   Se totalPages > 1:
       Button "◀️ Anterior" (disabled se currentPage == 0)
       Button "▶️ Próximo" (disabled se currentPage == totalPages-1)
       Button "🗑️ Limpar Fila"

   Cada botão tem customId: "queue:{action}:{page}"
   ```

4. **ButtonListener Processa Cliques**
   ```
   Ao clicar "Próximo":
       Incrementar page
       Recriar embed com nova página
       event.editMessage(newEmbed, newButtons)
   ```

### COMANDO /nowplaying

**EMBED DETALHADO:**

1. **Obter Informações**

   ```
   track = player.getPlayingTrack()
   position = track.getPosition()
   duration = track.getDuration()
   info = track.getInfo()
   ```

2. **Criar Embed**

   ```
   Título: "🎵 Tocando Agora"
   Thumbnail: artwork URL

   Campo "Música": {info.title}
   Campo "Artista": {info.author}
   Campo "Duração": {TimeFormat.format(duration)}

   Barra de Progresso:
   ProgressBar.create(position, duration, 20)
   → [████████████--------] 60%

   Campo "Tempo": {current} / {total}

   Footer: "Requisitado por {user}"
   ```

### AUTO-PLAY PRÓXIMA MÚSICA

**TrackScheduler.onTrackEnd(event):**

```
Se reason.mayStartNext:
    nextTrack = queue.poll()

    Se nextTrack != null:
        player.playTrack(nextTrack)
        Enviar embed "Tocando agora: {nextTrack}"
    Senão:
        Logger.info("Fila vazia, aguardando...")
        Player fica idle (não desconecta)
```

---

## 🎨 PADRÕES DE DESIGN

**Singleton:** PlayerConfig (uma instância do AudioPlayerManager)
**Factory:** EmbedFactory cria embeds padronizados
**Strategy:** Command interface permite adicionar comandos facilmente
**Observer:** Listeners observam eventos do JDA
**Repository:** Map<GuildId, MusicManager> armazena managers

---

## 🔒 TRATAMENTO DE ERROS

**Erros de API (YouTube rate limit):**

```
Catch FriendlyException
Se severity == COMMON:
    Log warning, informar usuário, continuar
Se severity == SUSPICIOUS ou FAULT:
    Log error com stack trace, informar usuário
```

**Erros de Conexão:**

```
Try-catch em guild.getAudioManager().openAudioConnection()
Se falhar: "Não consegui entrar no canal de voz"
```

**Timeout de Loading:**

```
PlayerManager tem timeout de 30s
Se exceder: loadFailed será chamado automaticamente
```

**Validações:**

```
Sempre verificar:
- Usuário em VoiceChannel
- Bot tem permissão VOICE_CONNECT e VOICE_SPEAK
- URL é válida (regex ou try-catch)
```

---

## 🚀 PERFORMANCE

**Otimizações obrigatórias:**

1. **Pool de Threads:** Lavaplayer gerencia automaticamente
2. **Buffer:** setFrameBufferDuration(5000) = 5s de buffer
3. **Garbage Collection:** Limpar MusicManager quando bot sair do canal
4. **Memory:** Um AudioPlayer por guild (não por canal)
5. **Network:** Usar HTTP/2 quando possível (Lavaplayer faz automaticamente)

**Limites:**

- Fila máxima: 100 músicas por guild
- Volume: 0-150 (100 = normal)
- Seek: apenas em tracks não-stream

---

## 📊 LOGGING

**Configurar logback.xml:**

```
Pattern: %d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n

Níveis:
- INFO: comandos executados, músicas tocadas
- WARN: rate limits, tentativas falhadas
- ERROR: exceções, erros críticos
- DEBUG: desabilitado em produção

Appenders:
- Console (stdout)
- Arquivo rolante (music-bot.log, max 10MB, 7 dias)
```

---

## 🌐 DEPLOY

**Variáveis de Ambiente (.env):**

```
TOKEN=seu_bot_token_aqui
SPOTIFY_CLIENT_ID=seu_client_id
SPOTIFY_CLIENT_SECRET=seu_client_secret
```

**Comando para rodar:**

```bash
mvn clean package
java -jar target/music-bot-1.0-SNAPSHOT-shaded.jar
```

**Compatível com:** Railway, Heroku, Render, VPS, Docker

**Recursos necessários:**

- RAM: 512MB mínimo, 1GB recomendado
- CPU: 1 vCPU
- Disco: 100MB (apenas código, sem cache)
- Rede: 1Mbps por 10 usuários simultâneos

---

## ❌ NÃO FAZER

- ❌ Baixar arquivos no disco
- ❌ Criar cache local
- ❌ Usar ProcessBuilder para chamar youtube-dl/spotdl
- ❌ Fazer bot sair do canal automaticamente
- ❌ Criar banco de dados
- ❌ Sistema de playlists salvas
- ❌ Comandos de moderação/admin
- ❌ Múltiplos prefixes
- ❌ Comandos de texto (apenas slash)
- ❌ Votação para pular (só owner pode pular)

---

## ✅ CHECKLIST FINAL

Antes de entregar, verificar:

- [ ] Bot conecta e registra comandos
- [ ] /play funciona com YouTube URL
- [ ] /play funciona com Spotify URL
- [ ] /play funciona com busca por texto
- [ ] Playlist inteira é adicionada na ordem correta
- [ ] Fila funciona (múltiplas músicas)
- [ ] Auto-play próxima música
- [ ] /queue tem paginação funcionando
- [ ] /nowplaying mostra barra de progresso
- [ ] /skip pula corretamente
- [ ] /pause e /resume funcionam
- [ ] /shuffle embaralha a fila
- [ ] /clear limpa tudo
- [ ] Embeds estão bonitos e informativos
- [ ] Erros são tratados graciosamente
- [ ] Logs estão claros e úteis
- [ ] README.md com instruções completas
- [ ] .env.example incluído
- [ ] JAR executável funciona
- [ ] Código está limpo e comentado

---

## 📝 NOTAS FINAIS

Este bot é **production-ready** para comunidades. Arquitetura escalável, código limpo seguindo padrões Java, tratamento robusto de erros. O usuário final não deve perceber complexidade técnica - apenas um bot que funciona perfeitamente.
