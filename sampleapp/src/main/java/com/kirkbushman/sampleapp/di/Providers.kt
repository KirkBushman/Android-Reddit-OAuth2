package com.kirkbushman.sampleapp.di

import android.content.Context
import com.kirkbushman.auth.AppAuth
import com.kirkbushman.auth.RedditAuth
import com.kirkbushman.auth.ScriptAuth
import com.kirkbushman.auth.UserlessAuth
import com.kirkbushman.auth.managers.SharedPrefsStorageManager
import com.kirkbushman.auth.utils.Utils.buildDefaultRetrofit
import com.kirkbushman.sampleapp.module.TestCredentials
import com.kirkbushman.sampleapp.utils.Utils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object Providers {

    @Provides
    @Singleton
    fun provideTestCreds(@ApplicationContext context: Context): TestCredentials {

        return Utils.loadCredsFromFile(context)
    }

    @Provides
    @Singleton
    fun provideSharedPrefs(@ApplicationContext context: Context): SharedPrefsStorageManager {

        return SharedPrefsStorageManager(context)
    }

    @Provides
    @Singleton
    fun provideDefaultRetrofit(): Retrofit {

        return buildDefaultRetrofit("https://github.com", true)
    }

    @Provides
    @Singleton
    fun provideAppAuth(

        creds: TestCredentials,
        retrofit: Retrofit,
        prefs: SharedPrefsStorageManager
    ): AppAuth {

        return RedditAuth.Builder()
            .setRetrofit(retrofit)
            .setApplicationCredentials(
                clientId = creds.clientId,
                redirectUrl = creds.redirectUrl
            )
            .setScopes(creds.scopes.toTypedArray())
            .setStorageManager(prefs)
            .setLogging(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideUserlessAuth(

        creds: TestCredentials,
        retrofit: Retrofit,
        prefs: SharedPrefsStorageManager
    ): UserlessAuth {

        return RedditAuth.Builder()
            .setRetrofit(retrofit)
            .setUserlessCredentials(
                clientId = creds.clientId
            )
            .setStorageManager(prefs)
            .setLogging(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideScriptAuth(

        creds: TestCredentials,
        retrofit: Retrofit,
        prefs: SharedPrefsStorageManager
    ): ScriptAuth {

        return RedditAuth.Builder()
            .setRetrofit(retrofit)
            .setScriptAuthCredentials(
                clientId = creds.scriptClientId,
                clientSecret = creds.scriptClientSecret,
                username = creds.username,
                password = creds.password
            )
            .setStorageManager(prefs)
            .setLogging(true)
            .build()
    }
}
