package com.example.poke_android

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import com.example.poke_android.data.*
import kotlinx.coroutines.flow.MutableSharedFlow

class PokeApplication : Application(), ImageLoaderFactory {
    lateinit var session: SessionStore
        private set

    lateinit var repository: Repository
        private set

    val unauthorized = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this).components { add(SvgDecoder.Factory()) }.build()

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
