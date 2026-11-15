package com.zora.zorsic

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture

private val AppBackground = Color(0xFF101114)
private val CardBackground = Color(0xFF1B1D22)
private val Muted = Color(0xFF9699A2)

class MainActivity : ComponentActivity() {
    private val songs = mutableStateListOf<Music>()
    private var controller by mutableStateOf<MediaController?>(null)
    private var selectedSong by mutableStateOf<Music?>(null)
    private var playing by mutableStateOf(false)
    private var controllerFuture: ListenableFuture<MediaController>? = null

    private val permissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) scanMusic()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connectPlayback()
        requestAudioPermission()
        setContent {
            ZorsicApp(
                songs = songs,
                selectedSong = selectedSong,
                playing = playing,
                onPlay = ::playSong,
                onToggle = ::togglePlayback,
                onRefresh = ::requestAudioPermission,
                onNext = ::nextSong,
                onPrevious = ::previousSong
            )
        }
    }

    private fun requestAudioPermission() {
        val permission = if (Build.VERSION.SDK_INT >= 33) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            scanMusic()
        } else {
            permissionRequest.launch(permission)
        }
    }

    private fun scanMusic() {
        songs.clear()
        songs.addAll(MusicRepository.getMusic(this))
    }

    private fun connectPlayback() {
        val token = SessionToken(this, ComponentName(this, MusicService::class.java))
        controllerFuture = MediaController.Builder(this, token).buildAsync()
        controllerFuture?.addListener({
            try {
                controller = controllerFuture?.get()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun playSong(song: Music) {
        val mediaController = controller ?: return
        selectedSong = song
        mediaController.setMediaItem(
            MediaItem.Builder()
                .setUri(song.uri)
                .setMediaId(song.id.toString())
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .build()
                )
                .build()
        )
        mediaController.prepare()
        mediaController.play()
        playing = true
    }

    private fun togglePlayback() {
        val mediaController = controller ?: return
        if (mediaController.isPlaying) {
            mediaController.pause()
            playing = false
        } else {
            mediaController.play()
            playing = true
        }
    }

    private fun nextSong() {
        val index = songs.indexOfFirst { it.id == selectedSong?.id }
        if (index >= 0 && index < songs.lastIndex) playSong(songs[index + 1])
    }

    private fun previousSong() {
        val index = songs.indexOfFirst { it.id == selectedSong?.id }
        if (index > 0) playSong(songs[index - 1])
    }

    override fun onDestroy() {
        controllerFuture?.let { MediaController.releaseFuture(it) }
        super.onDestroy()
    }
}

@Composable
private fun ZorsicApp(
    songs: List<Music>,
    selectedSong: Music?,
    playing: Boolean,
    onPlay: (Music) -> Unit,
    onToggle: () -> Unit,
    onRefresh: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    var tab by remember { mutableIntStateOf(0) }
    var query by remember { mutableStateOf("") }
    val filtered = songs.filter {
        it.title.contains(query, true) || it.artist.contains(query, true)
    }

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Color.White,
        background = AppBackground,
        surface = CardBackground
    )) {
        Scaffold(
            containerColor = AppBackground,
            bottomBar = {
                NavigationBar(containerColor = CardBackground) {
                    NavigationBarItem(
                        selected = tab == 0, onClick = { tab = 0 },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = tab == 1, onClick = { tab = 1 },
                        icon = { Icon(Icons.Default.Search, contentDescription = null) },
                        label = { Text("Search") }
                    )
                    NavigationBarItem(
                        selected = tab == 2, onClick = { tab = 2 },
                        icon = { Icon(Icons.Default.LibraryMusic, contentDescription = null) },
                        label = { Text("Library") }
                    )
                }
            }
        ) { insets ->
            Column(
                Modifier.fillMaxSize().padding(insets).padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(24.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("ZORSIC", fontSize = 27.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                        Text("YOUR MUSIC SPACE", fontSize = 10.sp, color = Muted, letterSpacing = 2.sp)
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Scan music")
                    }
                }
                Spacer(Modifier.height(28.dp))
                Text(
                    when (tab) { 1 -> "Search"; 2 -> "Your Library"; else -> "Discover your music" },
                    fontSize = 23.sp, fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text("Your local music, all in one place.", color = Muted, fontSize = 13.sp)
                Spacer(Modifier.height(18.dp))
                OutlinedTextField(
                    value = query, onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search your songs...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true, shape = RoundedCornerShape(16.dp)
                )
                Spacer(Modifier.height(20.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("YOUR TRACKS", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                    Text("${songs.size} songs", color = Muted, fontSize = 12.sp)
                }
                Spacer(Modifier.height(12.dp))
                if (filtered.isEmpty()) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.MusicOff, contentDescription = null, tint = Muted, modifier = Modifier.size(42.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(if (songs.isEmpty()) "No music found" else "No matching songs", color = Muted)
                            TextButton(onClick = onRefresh) { Text("Scan again") }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 12.dp)
                    ) {
                        items(filtered, key = { it.id }) { song ->
                            SongRow(song = song, onClick = { onPlay(song) })
                        }
                    }
                }
                if (selectedSong != null) {
                    MiniPlayer(
                        song = selectedSong, playing = playing,
                        onToggle = onToggle, onNext = onNext, onPrevious = onPrevious
                    )
                }
            }
        }
    }
}

@Composable
private fun SongRow(song: Music, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(CardBackground, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick).padding(13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(48.dp).background(Color(0xFF30323A), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.MusicNote, contentDescription = null)
        }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(song.title, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(song.artist, color = Muted, fontSize = 12.sp, maxLines = 1)
        }
        Icon(Icons.Default.PlayCircleOutline, contentDescription = "Play")
    }
}

@Composable
private fun MiniPlayer(
    song: Music,
    playing: Boolean,
    onToggle: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 10.dp)
            .background(Color(0xFF292B31), RoundedCornerShape(16.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Album, contentDescription = null, modifier = Modifier.size(34.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(song.title, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(song.artist, fontSize = 11.sp, color = Muted, maxLines = 1)
        }
        IconButton(onClick = onPrevious) { Icon(Icons.Default.SkipPrevious, contentDescription = "Previous") }
        IconButton(onClick = onToggle) {
            Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = "Play or pause")
        }
        IconButton(onClick = onNext) { Icon(Icons.Default.SkipNext, contentDescription = "Next") }
    }
}
