package com.kirkbushman.auth

import com.kirkbushman.auth.builders.BaseSavedBuilder
import com.kirkbushman.auth.http.RedditAuthClient
import com.kirkbushman.auth.managers.StorageManager
import com.kirkbushman.auth.models.creds.ApplicationCredentials
import com.kirkbushman.auth.models.enums.AuthType
import com.kirkbushman.auth.models.Scope
import com.kirkbushman.auth.models.Token
import com.kirkbushman.auth.models.creds.ScriptCredentials
import com.kirkbushman.auth.models.bearers.TokenBearer
import com.kirkbushman.auth.models.creds.UserlessCredentials
import com.kirkbushman.auth.models.base.Credentials
import com.kirkbushman.auth.utils.Utils
import com.kirkbushman.auth.utils.toSeparatedString
import retrofit2.Retrofit

/**
 * Class that is needed to interact with reddit authentication using a webView in
 * an installed application or using id and secret from a script
 */
@Suppress("unused")
abstract class RedditAuth constructor(

    private val client: RedditAuthClient,
    private val credentials: Credentials,
    private val storManager: StorageManager,
    private val authType: AuthType,
) {

    fun getAuthType(): AuthType {
        return authType
    }

    abstract fun renewToken(token: Token): Token?
    abstract fun revokeToken(token: Token): Boolean

    fun hasSavedBearer(): Boolean {

        return storManager.isAuthed() &&
            storManager.hasToken() &&
            storManager.authType() == getAuthType()
    }

    abstract fun retrieveSavedBearer(): TokenBearer?

    protected abstract fun fetchToken(): Token?

    protected fun saveToken(token: Token) {

        storManager.saveToken(token, authType)
    }

    open class Builder {

        protected var storManager: StorageManager? = null
        protected var retrofit: Retrofit? = null
        protected var client: RedditAuthClient? = null
        protected var logging: Boolean = false

        open fun setStorageManager(storManager: StorageManager?): Builder {
            this.storManager = storManager
            return this
        }

        open fun setRetrofit(retrofit: Retrofit?): Builder {
            this.retrofit = retrofit
            return this
        }

        open fun setClient(client: RedditAuthClient?): Builder {
            this.client = client
            return this
        }

        open fun setLogging(logging: Boolean): Builder {
            this.logging = logging
            return this
        }

        fun setApplicationCredentials(credentials: ApplicationCredentials): AppAuthBuilder {
            val builder = AppAuthBuilder(credentials)
            builder.setStorageManager(storManager)
            builder.setLogging(logging)
            return builder
        }

        fun setApplicationCredentials(clientId: String, redirectUrl: String): AppAuthBuilder {
            val builder = AppAuthBuilder(
                ApplicationCredentials(
                    clientId = clientId,
                    redirectUrl = redirectUrl
                )
            )

            builder.setStorageManager(storManager)
            builder.setLogging(logging)
            return builder
        }

        fun setUserlessCredentials(credentials: UserlessCredentials): UserlessAuthBuilder {
            val builder = UserlessAuthBuilder(credentials)
            builder.setStorageManager(storManager)
            builder.setLogging(logging)
            return builder
        }

        fun setUserlessCredentials(clientId: String, deviceId: String? = null): UserlessAuthBuilder {
            val builder = UserlessAuthBuilder(
                UserlessCredentials(
                    clientId = clientId,
                    deviceId = deviceId ?: Utils.getDeviceUUID()
                )
            )
            builder.setStorageManager(storManager)
            builder.setLogging(logging)
            return builder
        }

        fun setScriptAuthCredentials(credentials: ScriptCredentials): ScriptAuthBuilder {
            val builder = ScriptAuthBuilder(credentials)
            builder.setStorageManager(storManager)
            builder.setLogging(logging)
            return builder
        }

        fun setScriptAuthCredentials(
            username: String,
            password: String,
            clientId: String,
            clientSecret: String
        ): ScriptAuthBuilder {

            val builder = ScriptAuthBuilder(
                ScriptCredentials(
                    username = username,
                    password = password,
                    clientId = clientId,
                    clientSecret = clientSecret
                )
            )

            builder.setStorageManager(storManager)
            builder.setLogging(logging)
            return builder
        }
    }

    class AppAuthBuilder(private var credentials: ApplicationCredentials) : Builder() {

        private var scopes: String = ""

        fun setScopes(scopes: Array<Scope>): AppAuthBuilder {
            this.scopes = scopes.toSeparatedString()
            return this
        }

        fun setScopes(scopes: String): AppAuthBuilder {
            this.scopes = scopes
            return this
        }

        fun setScopes(scopes: List<Scope>): AppAuthBuilder {
            this.scopes = scopes.toSeparatedString()
            return this
        }

        fun setScopes(scopes: Array<String>): AppAuthBuilder {
            this.scopes = scopes.joinToString(separator = " ")
            return this
        }

        override fun setStorageManager(storManager: StorageManager?): AppAuthBuilder {
            return super.setStorageManager(storManager) as AppAuthBuilder
        }

        override fun setRetrofit(retrofit: Retrofit?): AppAuthBuilder {
            return super.setRetrofit(retrofit) as AppAuthBuilder
        }

        override fun setClient(client: RedditAuthClient?): AppAuthBuilder {
            return super.setClient(client) as AppAuthBuilder
        }

        override fun setLogging(logging: Boolean): AppAuthBuilder {
            return super.setLogging(logging) as AppAuthBuilder
        }

        fun build(): AppAuth {

            return AppAuth(
                client = client ?: Utils.buildDefaultClient(retrofit, logging),
                credentials = credentials,
                scopes = scopes,
                storManager = storManager
                    ?: throw IllegalArgumentException("StorageManager must not be null!")
            )
        }
    }

    class UserlessAuthBuilder(private var credentials: UserlessCredentials) : Builder() {

        override fun setStorageManager(storManager: StorageManager?): UserlessAuthBuilder {
            return super.setStorageManager(storManager) as UserlessAuthBuilder
        }

        override fun setRetrofit(retrofit: Retrofit?): UserlessAuthBuilder {
            return super.setRetrofit(retrofit) as UserlessAuthBuilder
        }

        override fun setClient(client: RedditAuthClient?): UserlessAuthBuilder {
            return super.setClient(client) as UserlessAuthBuilder
        }

        override fun setLogging(logging: Boolean): UserlessAuthBuilder {
            return super.setLogging(logging) as UserlessAuthBuilder
        }

        fun build(): UserlessAuth {

            return UserlessAuth(
                client = client ?: Utils.buildDefaultClient(retrofit, logging),
                credentials = credentials,
                storManager = storManager
                    ?: throw IllegalArgumentException("StorageManager must not be null!"),
            )
        }
    }

    class ScriptAuthBuilder(private var credentials: ScriptCredentials) : Builder() {

        override fun setStorageManager(storManager: StorageManager?): ScriptAuthBuilder {
            return super.setStorageManager(storManager) as ScriptAuthBuilder
        }

        override fun setRetrofit(retrofit: Retrofit?): ScriptAuthBuilder {
            return super.setRetrofit(retrofit) as ScriptAuthBuilder
        }

        override fun setClient(client: RedditAuthClient?): ScriptAuthBuilder {
            return super.setClient(client) as ScriptAuthBuilder
        }

        override fun setLogging(logging: Boolean): ScriptAuthBuilder {
            return super.setLogging(logging) as ScriptAuthBuilder
        }

        fun build(): ScriptAuth {

            return ScriptAuth(
                client = client ?: Utils.buildDefaultClient(retrofit, logging),
                credentials = credentials,
                storManager = storManager
                    ?: throw IllegalArgumentException("StorageManager must not be null!"),
            )
        }
    }

    class Saved(private val storManager: StorageManager) : BaseSavedBuilder() {

        override fun retrieve(
            provideCredentials: (type: AuthType) -> Credentials?,
            onFound: (auth: RedditAuth?, bearer: TokenBearer?) -> Unit,
            onMiss: () -> Unit
        ) {

            if (storManager.isAuthed() && storManager.hasToken()) {

                val credentials = provideCredentials(storManager.authType())
                val auth = when (storManager.authType()) {
                    AuthType.INSTALLED_APP ->
                        AppAuth(
                            client = client ?: Utils.buildDefaultClient(retrofit, logging),
                            storManager = storManager,
                            credentials = credentials as? ApplicationCredentials
                                ?: throw IllegalArgumentException(
                                    "Credentials provided must be ApplicationCredentials!"
                                ),
                            scopes = scopes
                        )

                    AuthType.USERLESS ->
                        UserlessAuth(
                            client = client ?: Utils.buildDefaultClient(retrofit, logging),
                            storManager = storManager,
                            credentials = credentials as? UserlessCredentials
                                ?: throw IllegalArgumentException(
                                    "Credentials provided must be UserlessCredentials!"
                                )
                        )

                    AuthType.SCRIPT ->
                        ScriptAuth(
                            client = client ?: Utils.buildDefaultClient(retrofit, logging),
                            storManager = storManager,
                            credentials = credentials as? ScriptCredentials
                                ?: throw IllegalArgumentException(
                                    "Credentials provided must be ScriptCredentials!"
                                )
                        )

                    else -> null
                }

                val bearer = if (auth?.hasSavedBearer() == true) {
                    auth.retrieveSavedBearer()
                } else {
                    null
                }

                onFound(auth, bearer)
            } else {
                onMiss()
            }
        }
    }
}
