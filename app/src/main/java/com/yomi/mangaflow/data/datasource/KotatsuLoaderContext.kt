package com.yomi.mangaflow.data.datasource

import android.content.Context
import coil3.ImageLoader
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.network.OkHttpNetworkClient
import org.koitharu.kotatsu.parsers.util.MimeType

class KotatsuLoaderContext(
    private val appContext: Context,
    private val okHttpClient: OkHttpClient,
    private val imageLoader: ImageLoader,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : MangaLoaderContext {

    override val networkClient = OkHttpNetworkClient(okHttpClient)
    override val resources = AndroidResources(appContext)
    override val cookieJar = okHttpClient.cookieJar
    override val imageLoader = imageLoader

    override suspend fun getCachedMangaPage(key: String): ByteArray? = null
    override suspend fun storeMangaPage(key: String, data: ByteArray, mime: MimeType): Boolean = false

    private class AndroidResources(private val context: Context) : MangaLoaderContext.Resources {
        override fun getString(id: Int, vararg args: Any): String = context.getString(id, *args)
        override fun getColor(id: Int): Int = context.getColor(id)
    }
}
