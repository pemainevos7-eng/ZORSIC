package com.zora.zorsic

import android.net.Uri

data class Music(
    val id: Long,
    val title: String,
    val artist: String,
    val uri: Uri,
    val duration: Long
)
