# ZORSIC

Native Android music player built with Kotlin, Jetpack Compose, and AndroidX Media3.

## Included
- Scan local audio through Android MediaStore.
- Search tracks by title or artist.
- Play and pause local tracks.
- Previous and next controls.
- Background playback foundation using MediaSessionService.
- GitHub Actions workflow for a debug APK.

## Build APK
1. Push this project to the `main` branch of your GitHub repository.
2. Open **Actions**.
3. Select **ZORSIC Android APK**.
4. Choose **Run workflow** or push a commit.
5. When the workflow succeeds, download **ZORSIC-debug-apk** from Artifacts.

This is an initial local music player foundation. Online music catalogues, lyrics providers, playlists, album artwork, and a full now-playing screen are not included yet.
