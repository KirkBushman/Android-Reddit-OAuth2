package com.kirkbushman.auth.builders

import com.kirkbushman.auth.http.RedditAuthClient
import com.kirkbushman.auth.managers.StorageManager
import retrofit2.Retrofit

abstract class BaseAuthBuilder {

    protected var storManager: StorageManager? = null
    protected var retrofit: Retrofit? = null
    protected var client: RedditAuthClient? = null
    protected var logging: Boolean = false

    open fun setStorageManager(storManager: StorageManager?): BaseAuthBuilder {
        this.storManager = storManager
        return this
    }

    open fun setRetrofit(retrofit: Retrofit?): BaseAuthBuilder {
        this.retrofit = retrofit
        return this
    }

    open fun setClient(client: RedditAuthClient?): BaseAuthBuilder {
        this.client = client
        return this
    }

    open fun setLogging(logging: Boolean): BaseAuthBuilder {
        this.logging = logging
        return this
    }
}
