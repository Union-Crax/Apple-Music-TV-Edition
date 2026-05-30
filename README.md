# Apple Music — TV Edition

A dedicated Android TV music client, reimagined for the big screen with a native
10-foot UI, full D-pad navigation, and an Apple Music–inspired design language.

It is fully self-contained: there is no Apple Music backend to connect to, so the
app **generates everything on device** — gradient cover art and *real audio*.
Pressing Play actually produces music.

## What it does

- **Apple-style shell** — persistent left sidebar (Listen Now · Browse · Radio ·
  Library · Search), shelf-based browsing with a featured hero, an always-on
  Now Playing bar, and an immersive full-screen player.
- **It actually plays** — a tiny built-in software synthesiser
  ([`ToneAudioEngine`](app/src/main/java/com/apple/music/tv/audio/ToneAudioEngine.kt))
  turns each track's *mood* into a live chord progression with arpeggios, pads,
  bass, an optional beat, and a stereo feedback delay. No network, no media files.
- **Generated artwork** — every album/playlist/station gets a deterministic
  gradient cover with soft abstract texture
  ([`Artwork`](app/src/main/java/com/apple/music/tv/ui/components/Artwork.kt)),
  so the UI looks designed and loads instantly offline.
- **Full transport** — play/pause, next/previous, shuffle, repeat (off/all/one),
  a D-pad scrubber (◀/▶ to seek), volume, a live queue, and time-synced lyrics.
- **TV-first input** — D-pad focus everywhere with clear focus treatments,
  plus hardware media keys (play/pause/next/previous) routed to the player.

## Architecture

```
data/
  model/        Track, Playlist, LyricLine, Mood   (pure Kotlin, JVM-testable)
  MockData      in-memory catalogue + search
audio/
  ToneAudioEngine   real-time PCM synth on AudioTrack
playback/
  PlayerViewModel   single source of truth: queue, position clock, transport
ui/
  AppRoot           sidebar shell + full-screen overlay
  components/        Artwork, MediaCard, NowPlayingBar, LyricsPanel, Focusable
  screens/          BrowseScreen, SearchScreen, NowPlayingScreen
  theme/            Apple-black color scheme + 10-foot typography
```

Playback state is hoisted into an activity-scoped `PlayerViewModel`, so audio keeps
playing seamlessly as you move between browsing and the full player.

## Build & run

```bash
# Unit tests (pure JVM — no device needed)
./gradlew testDebugUnitTest

# Build the APK
./gradlew assembleDebug

# Install on a connected Android TV / emulator
./gradlew installDebug
```

Requires the Android SDK (compileSdk 34) and JDK 17+. `minSdk` is 21.

## Notes

This is a UI/UX showcase. It deliberately does **not** stream Apple Music — the
"songs" are mood pieces rendered by the on-device synth, and titles/artists are
sample metadata used purely to demonstrate the interface.
