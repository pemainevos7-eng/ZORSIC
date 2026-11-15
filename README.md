# ZORSIC

Native Android music player foundation built with Kotlin, Jetpack Compose, and AndroidX Media3.

## Features in this starter
- Scan audio files available to the app through Android MediaStore.
- Search local tracks by title or artist.
- Play and pause local audio.
- Previous and next track controls.
- MediaSessionService foundation for background playback and system media controls.
- GitHub Actions workflow to build a debug APK.

## Build APK on GitHub
1. Upload the contents of this project to the root of your `ZORSIC` repository.
2. Open the repository's **Actions** tab.
3. Select **ZORSIC Android APK**.
4. Choose **Run workflow** (or push a commit to `main`).
5. Open the completed workflow run and download `ZORSIC-debug-apk` under **Artifacts**.

## Important
This is an initial local-music-player foundation, not a feature-complete clone of another app. Online catalogues, lyrics providers, playlists, album artwork UI, and a full now-playing screen still need separate implementation. Use music sources that authorize playback and downloads.
