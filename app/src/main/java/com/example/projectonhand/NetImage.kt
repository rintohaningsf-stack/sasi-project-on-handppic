package com.example.projectonhand

import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

private val imageCache = LruCache<String, ImageBitmap>(80)

/** Loader gambar URL sederhana (tanpa library): cache memori + fallback drawable lokal. */
@Composable fun NetImage(url: String?, modifier: Modifier, fallback: Int? = null, contentScale: ContentScale = ContentScale.Crop) {
    var bmp by remember(url) { mutableStateOf(url?.let { imageCache.get(it) }) }
    LaunchedEffect(url) {
        if (url != null && bmp == null) {
            val loaded = withContext(Dispatchers.IO) {
                runCatching {
                    val o = BitmapFactory.Options().apply { inSampleSize = 2 }
                    URL(url).openStream().use { BitmapFactory.decodeStream(it, null, o)?.asImageBitmap() }
                }.getOrNull()
            }
            if (loaded != null) { imageCache.put(url, loaded); bmp = loaded }
        }
    }
    val b = bmp
    when {
        b != null -> Image(b, null, modifier, contentScale = contentScale)
        fallback != null -> Image(painterResource(fallback), null, modifier, contentScale = contentScale)
        else -> Box(modifier)
    }
}