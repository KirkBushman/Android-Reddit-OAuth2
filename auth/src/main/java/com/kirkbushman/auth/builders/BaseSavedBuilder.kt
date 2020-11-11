package com.kirkbushman.auth.builders

import com.kirkbushman.auth.RedditAuth
import com.kirkbushman.auth.http.RedditAuthClient
import com.kirkbushman.auth.models.Scope
import com.kirkbushman.auth.models.base.Credentials
import com.kirkbushman.auth.models.bearers.TokenBearer
import com.kirkbushman.auth.models.enums.AuthType
import com.kirkbushman.auth.utils.toSeparatedString
import retrofit2.Retrofit

abstract class BaseSavedBuilder {

    protected var scopes: String = ""

    protected var retrofit: Retrofit? = null
    protected var client: RedditAuthClient? = null
    protected var logging: Boolean = false

    open fun setScopes(scopes: Array<Scope>): BaseSavedBuilder {
        this.scopes = scopes.toSeparatedString()
        return this
    }

    open fun setScopes(scopes: String): BaseSavedBuilder {
        this.scopes = scopes
        return this
    }

    open fun setScopes(scopes: List<Scope>): BaseSavedBuilder {
        this.scopes = scopes.toSeparatedString()
        return this
    }

    open fun setScopes(scopes: Array<String>): BaseSavedBuilder {
        this.scopes = scopes.joinToString(separator = " ")
        return this
    }

    open fun setRetrofit(retrofit: Retrofit?): BaseSavedBuilder {
        this.retrofit = retrofit
        return this
    }

    open fun setClient(client: RedditAuthClient?): BaseSavedBuilder {
        this.client = client
        return this
    }

    open fun setLogging(logging: Boolean): BaseSavedBuilder {
        this.logging = logging
        return this
    }

    abstract fun retrieve(
        provideCredentials: (type: AuthType) -> Credentials?,
        onFound: (auth: RedditAuth?, bearer: TokenBearer?) -> Unit,
        onMiss: () -> Unit
    )
}
