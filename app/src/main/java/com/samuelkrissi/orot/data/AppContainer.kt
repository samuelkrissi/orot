package com.samuelkrissi.orot.data

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.samuelkrissi.orot.catalog.Books
import com.samuelkrissi.orot.data.api.YhbApi
import com.samuelkrissi.orot.data.prefs.ReaderPrefs
import com.samuelkrissi.orot.data.repo.OrotRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val okHttp = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "BeOrotReader/1.0 (Android; educational reader)")
                .header("Accept", "application/json")
                .build()
            chain.proceed(request)
        }
        .build()

    private val api: YhbApi = Retrofit.Builder()
        .baseUrl(Books.API_BASE)
        .client(okHttp)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(YhbApi::class.java)

    val prefs = ReaderPrefs(context.applicationContext)
    val repository = OrotRepository(
        api = api,
        cacheDir = context.applicationContext.cacheDir,
        json = json,
    )
}
