package com.example.taller3.interfaces

import android.net.Uri

interface ImageDownloadCallback {
    fun onImageDownloadComplete(uris: List<Uri>)
}