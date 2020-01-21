package com.kirkbushman.auth.utils

import android.util.Base64
import com.kirkbushman.auth.models.Scope
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object Utils {

    private val STRING_CHARACTERS = ('0'..'9').plus('a'..'z').toTypedArray()

    fun addParamsToUrl(url: String, array: Array<String>): String {
        return url.plus(array.joinToString(separator = "&", prefix = "?"))
    }

    fun generateRandomString(): String {
        return (1..32).map { STRING_CHARACTERS.random() }.joinToString("")
    }

    fun buildRetrofit(baseUrl: String, logging: Boolean): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create())
            .client(getOkHttpClient(logging))
            .build()
    }

    private fun getOkHttpClient(logging: Boolean): OkHttpClient {
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
