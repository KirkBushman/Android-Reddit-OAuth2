package com.kirkbushman.auth.utils

import android.util.Base64
import com.kirkbushman.auth.http.RedditAuthClient
import com.kirkbushman.auth.http.RedditAuthService
import com.kirkbushman.auth.models.Scope
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*
import kotlin.collections.HashMap

object Utils {

    const val BASE_URL = "https://www.reddit.com"

    private val STRING_CHARACTERS = ('0'..'9').plus('a'..'z').toTypedArray()

    fun addParamsToUrl(url: String, array: Array<String>): String {
        return url.plus(array.joinToString(separator = "&", prefix = "?"))
    }

    fun generateRandomString(): String {
        return (1..32).map { STRING_CHARACTERS.random() }.joinToString("")
    }

    fun getDeviceUUID(): String {
        return UUID.randomUUID().toString()
    }

    fun buildDefaultClient(retrofit: Retrofit?, logging: Boolean): RedditAuthClient {

        val retrofitVal =
            if (retrofit != null) {
                retrofit
            } else {

                val httpClient = buildOkHttpClient(logging)
                buildRetrofit(BASE_URL, httpClient)
            }

        val api = buildApi(retrofitVal)
        return buildClient(api)
    }

    fun buildClient(api: RedditAuthService): RedditAuthClient {

        return RedditAuthClient(api)
    }

    fun buildApi(retrofit: Retrofit): RedditAuthService {

        return retrofit.create(RedditAuthService::class.java)
    }

    fun buildDefaultRetrofit(baseUrl: String, logging: Boolean): Retrofit {

        val httpClient = buildOkHttpClient(logging)
        return buildRetrofit(baseUrl, httpClient)
    }

    fun buildRetrofit(baseUrl: String, client: OkHttpClient): Retrofit {

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create())
            .client(client)
            .build()
    }

    private fun buildOkHttpClient(logging: Boolean): OkHttpClient {
        return if (logging) {

            val logger = HttpLoggingInterceptor()
            logger.level = HttpLoggingInterceptor.Level.BODY

            OkHttpClient
                .Builder()
                .addInterceptor(logger)
                .build()
        } else {

            OkHttpClient
                .Builder()
                .build()
        }
    }
}

fun Array<Scope>.toSeparatedString(): String {
    return joinToString(separator = " ")
}

fun List<Scope>.toSeparatedString(): String {
    return joinToString(separator = " ")
}

fun String.toHeaderString(): HashMap<String, String> {
    return hashMapOf("Authorization" to "Basic ".plus(Base64.encodeToString(this.toByteArray(), Base64.NO_WRAP)))
}
