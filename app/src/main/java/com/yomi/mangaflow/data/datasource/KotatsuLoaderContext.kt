package com.yomi.mangaflow.data.datasource

import android.content.Context
import android.graphics.Bitmap
import coil3.ImageLoader
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import okhttp3.Response
import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.model.MangaSourceConfig
import org.koitharu.kotatsu.parsers.network.OkHttpNetworkClient
import org.koitharu.kotatsu.parsers.util.MimeType

class KotatsuLoaderContext(
    private val appContext: Context,
    private val okHttpClient: OkHttpClient,
    private val imageLoader: ImageLoader,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : MangaLoaderContext {

    override val httpClient: OkHttpClient = okHttpClient
    override val networkClient = OkHttpNetworkClient(okHttpClient)
    override val resources = AndroidResources(appContext)
    override val cookieJar: CookieJar = okHttpClient.cookieJar

    override suspend fun getCachedMangaPage(key: String): ByteArray? = null
    override suspend fun storeMangaPage(key: String, data: ByteArray, mime: MimeType): Boolean = false
    override suspend fun evaluateJs(script: String): String? = null
    override fun getConfig(source: MangaSource): MangaSourceConfig = MangaSourceConfig.Default
    override fun getDefaultUserAgent(): String = "Mozilla/5.0 (Linux; Android 13)"
    override fun redrawImageResponse(response: Response, redraw: (Bitmap) -> Bitmap): Response = response
    override fun createBitmap(width: Int, height: Int): Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    private class AndroidResources(private val context: Context) : MangaLoaderContext.Resources {
        override fun getString(id: Int, vararg args: Any): String = context.getString(id, *args)
        override fun getColor(id: Int): Int = context.getColor(id)
    }
}
