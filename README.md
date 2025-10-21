# Dudu Music Bot

A Discord music bot with multi-platform support and advanced queue management features.

## Overview

Dudu Music Bot is built with Java 21 and supports playback from YouTube, Spotify, Deezer, and SoundCloud. Features include queue history tracking, multi-language support, and automatic retry mechanisms for reliable playback.

## Features

- Multi-platform audio playback (YouTube, Spotify, Deezer, SoundCloud)
- Advanced queue management with shuffle, loop modes, and position jumping
- Queue history system with up to 10 restorable snapshots per guild
- Interactive paginated queue display with navigation controls
- Automatic retry logic with exponential backoff for failed requests
- Dynamic artwork fetching with multiple fallback sources
- Multi-language support (Portuguese, English, Spanish)
- Auto-disconnect after 2 minutes of inactivity with queue preservation

## Platform Support

| Platform | Tracks | Playlists | Implementation |
|----------|--------|-----------|----------------|
| YouTube | ✓ | ✓ | Direct playback via LavaPlayer |
| Spotify | ✓ | ✓ | LavaSource integration |
| Deezer | ✓ | ✓ | Metadata extraction → YouTube search |
| SoundCloud | ✓ | ✓ | LavaSource integration |
| Direct URLs | ✓ | ✗ | Supports .mp3, .m4a, .ogg, .flac, .wav |
| Apple Music | ✗ | ✗ | Not supported due to DRM restrictions |

## Commands

### Playback Control
| Command | Description |
|---------|-------------|
| `/play <url\|query>` | Add track or playlist to queue |
| `/pause` | Pause current playback |
| `/resume` | Resume playback |
| `/skip` | Skip to next track |
| `/stop` | Stop playback and clear queue |
| `/rewind` | Play previous track |

### Queue Management
| Command | Description |
|---------|-------------|
| `/queue` | Display current queue (paginated) |
| `/clear` | Remove all tracks from queue |
| `/shuffle` | Randomize queue order |
| `/jump <position>` | Jump to specific track position |
| `/switch <from> <to>` | Move track between positions |
| `/erase <position>` | Remove track at position |
| `/previousqueue` | Restore previous queue snapshot |

### Advanced Playback
| Command | Description |
|---------|-------------|
| `/loop <mode>` | Set loop mode (off/track/queue) |
| `/seek <time>` | Seek to timestamp (HH:MM:SS) |
| `/volume <level>` | Adjust volume (0-150%) |

### Information
| Command | Description |
|---------|-------------|
| `/nowplaying` | Show current track with progress bar |
| `/help` | Display all available commands |
| `/language <lang>` | Set guild language (ptbr/en/es) |

## Technical Stack

- **Java** 21
- **JDA** 5.6.0 - Discord API wrapper
- **LavaPlayer** 2.2.4 - Audio player engine
- **LavaSource** 4.8.1 - Multi-platform source support
- **Maven** - Build automation with Shade plugin

## Architecture

```
src/main/java/com/dudumusic/
├── Main.java                    # Application entry point
├── core/                        # Core configuration
│   ├── BotConfig.java          # Environment configuration
│   ├── PlayerConfig.java       # Audio player setup
│   ├── LanguageManager.java    # Per-guild language preferences
│   ├── Translation.java        # Localization strings
│   └── ActivityStatusUpdater.java
├── commands/                    # Command implementations
│   ├── music/                  # Playback commands
│   └── info/                   # Information commands
├── listeners/                   # Event handlers
│   ├── CommandListener.java   # Slash command processor
│   ├── ButtonListener.java    # Interactive button handler
│   └── VoiceListener.java     # Voice state tracking
├── audio/                       # Audio processing
│   ├── MusicManager.java       # Per-guild audio manager
│   ├── TrackScheduler.java    # Queue and playback control
│   ├── AudioHandler.java      # Audio stream provider
│   └── QueueHistory.java      # Queue snapshot storage
└── utils/                       # Utility classes
    ├── SourceDetector.java     # Platform detection
    ├── MusicLinkConverter.java # URL conversion logic
    ├── SpotifyApiClient.java  # Spotify API integration
    ├── EmbedFactory.java      # Discord embed builder
    ├── ProgressBar.java       # Progress visualization
    └── VoiceValidator.java    # Voice state validation
```

## Implementation Details

### Retry Mechanism
Implements exponential backoff retry strategy with up to 5 attempts for transient failures (network timeouts, temporary API unavailability). Base delay of 1.5 seconds between retries.

### Queue History System
Automatically captures queue snapshots when:
- Queue is manually cleared via `/clear`
- Bot disconnects from voice channel
- Maintains rolling history of 10 snapshots per guild

### Artwork Resolution
Multi-tier fallback strategy for track artwork:
1. Custom artwork from platform conversion
2. Track metadata artwork URL
3. YouTube thumbnail extraction (maxresdefault → hqdefault → default)
4. Spotify API playlist artwork

### Playlist Loading
- Concurrent batch loading with up to 200 tracks per playlist
- Real-time progress updates via Discord embeds
- Automatic filtering of invalid tracks (zero duration, missing metadata)
- Retry logic per individual track

### Voice Channel Management
Auto-disconnect timer activates when bot is alone in voice channel:
- 2-minute grace period before disconnect
- Automatic queue snapshot preservation
- Timer cancellation if users rejoin

## Limitations

- Apple Music tracks not supported (encryption/DRM)
- Deezer tracks converted to YouTube searches (may not be exact matches)
- Maximum queue size: 1,000 tracks
- Queue history limit: 10 snapshots per guild
- Playlist loading cap: 200 tracks per playlist

## Contributing

Contributions are welcome. Please submit pull requests with clear descriptions of changes.
- PS: git clone the project and configure your .env file with your Discord bot token and the Spotify client ID and client secret.

## Acknowledgments

Built with:
- [JDA](https://github.com/discord-jda/JDA) - Java Discord API
- [LavaPlayer](https://github.com/sedmelluq/lavaplayer) - Audio player library
- [LavaSource](https://github.com/topi314/LavaSource) - Multi-platform source support
