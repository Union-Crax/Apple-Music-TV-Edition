# Apple Music — TV Edition

A dedicated Android TV client for Apple Music, reimagined for the big screen with a
native 10‑foot UI and full D‑pad navigation — now wired up to **real Apple Music
catalogue data and real audio playback**.

## What it does

- **Connects to Apple Music's catalogue** through Apple's public **iTunes Search API**
  (`itunes.apple.com`) — real songs, real artists, real high‑resolution album art, and
  real playable **30‑second preview streams**. No paid developer account, API key, or
  token is required, so the app works out of the box.
- **Plays real audio** using **Media3 ExoPlayer**. Play / pause, next / previous,
  seek, shuffle, and repeat are all backed by a single process‑wide player, so playback
  stays in sync across every screen.
- **10‑foot UI built with Compose for TV**: a focusable top nav bar (Home / Search),
  a featured hero banner, multiple content shelves, a real search experience, a
  full‑screen Now Playing view with synchronized lyrics and an up‑next queue, and a
  persistent mini‑player docked at the bottom.
- **Graceful offline fallback**: if the network is unavailable, the UI degrades to a
  bundled catalogue so shelves still render and navigation still works.

## Screens

| Screen | Highlights |
| --- | --- |
| **Home** | Hero banner + shelves (Top Charts, New Releases, Pop, Hip‑Hop & R&B, Chill, Throwbacks) and a "Made For You" playlist row. Selecting anything starts a queue and opens Now Playing. |
| **Search** | Debounced catalogue search with a results grid, plus one‑tap suggestion chips so it's fully usable with only a D‑pad remote. |
| **Now Playing** | Blurred‑art backdrop, live progress bar, full transport (shuffle · previous · play/pause · next · repeat), buffering indicator, synchronized lyrics, and an Up Next queue. |
| **Mini‑player** | Always‑visible playback bar with live progress and quick controls; opens Now Playing when selected. |

## Architecture

```
com.apple.music.tv
├── AppleMusicTvApp          # Application → initialises ServiceLocator
├── di/ServiceLocator        # Process-wide repository + player (no DI framework)
├── data
│   ├── model/               # Track, Playlist, LyricLine
│   ├── remote/              # Retrofit AppleMusicApi + DTOs + NetworkModule
│   ├── repository/          # MusicRepository + AppleMusicRepository (with fallback)
│   └── MockData             # Bundled offline catalogue + lyrics
├── playback/PlayerController # Media3 ExoPlayer wrapper exposing StateFlows
├── viewmodel/               # Dashboard / Search / NowPlaying view models
├── navigation/              # NavHost + destinations + tabs
└── ui/                      # theme, components, screens
```

Data flows one way: `AppleMusicApi → AppleMusicRepository → ViewModel (StateFlow) → Compose`.
Playback commands flow the other way into the shared `PlayerController`, whose state
flows back out to every observing screen.

## Building & running

```bash
./gradlew assembleDebug        # build the APK
./gradlew test                 # run JVM unit tests
./gradlew installDebug         # install on a connected Android TV / emulator
```

Requires the Android SDK (compileSdk 34) and JDK 17+. Launch it on an Android TV
emulator or device; the app registers on the leanback home screen.

## Notes & limitations

- Playback uses the **30‑second previews** Apple returns publicly. Full‑length,
  DRM‑protected playback requires Apple's proprietary MusicKit SDK and an active
  subscription, which Apple does not currently ship for Android TV.
- Upgrading to the full **Apple Music API** (`api.music.apple.com`) for personal
  library, editorial playlists, and lyrics is a drop‑in change: implement
  `MusicRepository` against it with a MusicKit **developer token**. The rest of the app
  (player, UI, view models) is agnostic to the data source.
- Synchronized lyrics are provided for a sample track; the API does not expose lyrics,
  so other tracks show the Up Next queue instead.
