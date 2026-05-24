package com.yomi.mangaflow

import android.app.Application
import coil3.ImageLoader
import com.yomi.mangaflow.data.datasource.KotatsuDataSource
import com.yomi.mangaflow.data.datasource.KotatsuLoaderContext
import com.yomi.mangaflow.data.local.SettingsDataStore
import com.yomi.mangaflow.data.repository.MangaRepository
import com.yomi.mangaflow.ui.viewmodel.MainViewModel
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class YomiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@YomiApplication)
            modules(appModule)
        }
    }
}

val appModule = module {
    single { SettingsDataStore(get()) }
    single { OkHttpClient.Builder().build() }
    single {
        ImageLoader.Builder(get())
            .build()
    }
    single { KotatsuLoaderContext(get(), get(), get()) }
    single { KotatsuDataSource(get()) }
    single { MangaRepository(get(), get()) }
    factory { MainViewModel(get(), get()) }
}
