package com.example.poke_android

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.example.poke_android.data.*
import kotlinx.coroutines.flow.MutableSharedFlow

class PokeApplication : Application(), ImageLoaderFactory {
    lateinit var session: SessionStore
        private set

    lateinit var repository: Repository
        private set

    val unauthorized = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .memoryCache { MemoryCache.Builder(this).maxSizePercent(0.25).build() }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("pokedex_images"))
                    .maxSizeBytes(64L * 1024L * 1024L)
                    .build()
            }
            .components { add(SvgDecoder.Factory()) }
            .crossfade(false)
            .build()

    override fun onCreate() {
        super.onCreate()
        session = SessionStore(this)
        repository =
            Repository(
                createApi(
                    BuildConfig.API_URL,
                    { session.token },
                    { unauthorized.tryEmit(Unit) },
                    BuildConfig.DEBUG,
                )
            )
    }
}
