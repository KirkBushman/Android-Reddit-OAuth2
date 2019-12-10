package com.kirkbushman.auth

import com.kirkbushman.auth.errors.AccessDeniedException
import com.kirkbushman.auth.errors.InvalidRequestException
import com.kirkbushman.auth.errors.InvalidScopesException
import com.kirkbushman.auth.errors.OAuth2Exception
import com.kirkbushman.auth.errors.UnsupportedResponseTypeException
import com.kirkbushman.auth.http.RedditService
import com.kirkbushman.auth.managers.StorageManager
import com.kirkbushman.auth.models.ApplicationCredentials
import com.kirkbushman.auth.models.AuthType
import com.kirkbushman.auth.models.RefreshToken
import com.kirkbushman.auth.models.Scope
import com.kirkbushman.auth.models.ScopesEnvelope
import com.kirkbushman.auth.models.ScriptCredentials
import com.kirkbushman.auth.models.Token
import com.kirkbushman.auth.models.TokenBearer
import com.kirkbushman.auth.models.base.Credentials
import com.kirkbushman.auth.utils.Utils.addParamsToUrl
import com.kirkbushman.auth.utils.Utils.generateRandomString
import com.kirkbushman.auth.utils.Utils.getRetrofit
import com.kirkbushman.auth.utils.toHeaderString
import com.kirkbushman.auth.utils.toSeparatedString
import retrofit2.Call

/**
 * Class that is needed to interact with reddit authentication using a webView in
 * an installed application or using id and secret from a script
 */
@Suppress("unused")
class RedditAuth private constructor(

    private val credentials: Credentials,

    private val scopes: String,

    private val storManager: StorageManager,

    logging: Boolean
) {

    companion object {
        private const val BASE_URL = "https://www.reddit.com"
        private const val BASE_AUTH_URL = "$BASE_URL/api/v1/authorize.compact"

        private val codeRegex = Regex("(?<=(code=))([a-zA-Z0-9]|-|_)+(?=(&|\\s|))")
        private val stateRegex = Regex("(?<=(state=))([a-zA-Z0-9]|-|_)+(?=(&|\\s|))")
        private val errorRegex = Regex("(?<=(error=))([a-zA-Z0-9]|-|_)+(?=(&|\\s|))")

        private var instance: RedditAuth? = null
        fun instance(): RedditAuth? {
            return instance
        }

        fun getScopes(logging: Boolean = false): ScopesEnvelope? {
            val retrofit = getRetrofit(logging)
            val api = retrofit.create(RedditService::class.java)

            val req = api.getScopes()
            val res = req.execute()

            if (res.isSuccessful) {
                return res.body()
            } else {
                throw Exception(res.errorBody().toString())
            }
        }
    }

    private val retrofit by lazy { getRetrofit(logging) }
    private val api: RedditService by lazy { retrofit.create(RedditService::class.java) }

    private val authType: AuthType = when (credentials) {
        is ApplicationCredentials -> AuthType.INSTALLED_APP
        is ScriptCredentials -> AuthType.SCRIPT

        else -> AuthType.NONE
    }

    private var state: String? = null

    // Installed App Authentication
    constructor(

        /**
         * Contains
         *
         * clientId:
         * Given by creating an app on reddit console, at https://www.reddit.com/prefs/apps,
         * Click the "create an app" button
         *
         * redirectUrl:
         * Url the browser shall be redirected on the API response,
         * It must match the one inserted on console (https://www.reddit.com/prefs/apps).
         */
        credentials: ApplicationCredentials,

        /**
         * This is a random string that is given as a url param at beginning of the auth request,
         * we must check it's the same on future responses.
         */
        state: String,

        /**
         * Permissions the client will have, should be as tight as possible.
         * Can be retrieved at https://www.reddit.com/api/v1/scopes
         */
        scopes: String,

        /**
         * Interface instance that is used to persist token to memory.
         * Can be extended using the method you prefer, this lib provides a working
         * example with SharedPreferences
         */
        storManager: StorageManager,

        logging: Boolean = false

    ) : this (credentials, scopes, storManager, logging) {

        this.state = state
    }

    // Script App Authentication
    constructor(

        /**
         * Contains
         *
         * username, password:
         * Login info of the account.
         *
         * clientId:
         * Given by creating an app on reddit console, at https://www.reddit.com/prefs/apps,
         * Click the "create an app" button
         *
         * clientSecret:
         * Given by creating an app on reddit console, at https://www.reddit.com/prefs/apps,
         * Click the "create an app" button
         *
         */
        credentials: ScriptCredentials,

        /**
         * Permissions the client will have, should be as tight as possible.
         * Can be retrieved at https://www.reddit.com/api/v1/scopes
         */
        scopes: String,

        /**
         * Interface instance that is used to persist token to memory.
         * Can be extended using the method you prefer, this lib provides a working
         * example with SharedPreferences
         */
        storManager: StorageManager,

        logging: Boolean = false

    ) : this (credentials as Credentials, scopes, storManager, logging)

    fun getAuthType(): AuthType {
        return authType
    }

    init {
        instance = this
    }

    fun provideAuthorizeUrl(): String {

        if (authType == AuthType.INSTALLED_APP &&
            credentials is ApplicationCredentials) {

            val params = arrayOf(
                "client_id=${credentials.clientId}",
                "response_type=code",
                "state=$state",
                "redirect_uri=${credentials.redirectUrl}",
                "duration=permanent",
                "scope=$scopes"
            )

            return addParamsToUrl(BASE_AUTH_URL, params)
        }

        return ""
    }

    fun isRedirectedUrl(url: String?): Boolean {

        if (authType == AuthType.INSTALLED_APP &&
            credentials is ApplicationCredentials) {

            if (url == null) {
                throw IllegalStateException("Provided url is null!")
            }

            if (credentials.redirectUrl.isEmpty()) {
                throw IllegalStateException("Redirect Url was not provided or invalid!")
            }

            return url.startsWith(credentials.redirectUrl)
        }

        return false
    }

    fun getTokenBearer(): TokenBearer? {

        if (authType == AuthType.SCRIPT &&
            credentials is ScriptCredentials) {

            try {

                val req = api.getAccessToken(
                    header = "${credentials.clientId}:${credentials.clientSecret}".toHeaderString(),
                    grantType = "password",
                    username = credentials.username,
                    password = credentials.password
                )

                val res = req.execute()

                return if (res.isSuccessful) {
                    val token = res.body() as Token
                    TokenBearer(storManager, token, credentials)
                } else {
                    null
                }
            } catch (ex: Exception) {
                ex.printStackTrace()

                return null
            }
        }

        return null
    }

    fun getTokenBearer(url: String?): TokenBearer? {

        if (authType == AuthType.INSTALLED_APP && credentials is ApplicationCredentials) {

            if (url == null) {
                throw IllegalStateException("Provided url is null!")
            }

            if (url.contains("error")) {
                val errorStr = errorRegex.find(url)?.value ?: ""

                if (errorStr == "access_denied") {
                    throw AccessDeniedException("User chose not to grant your app permissions!")
                }

                if (errorStr == "unsupported_response_type") {
                    throw UnsupportedResponseTypeException("Invalid response_type parameter in initial Authorization!")
                }

                if (errorStr == "invalid_scope") {
                    throw InvalidScopesException("Invalid scope parameter in initial Authorization!")
                }

                if (errorStr == "invalid_request") {
                    throw InvalidRequestException("There was an issue with the request sent to /api/v1/authorize!")
                }

                throw OAuth2Exception("The Reddit API returned the error: $errorStr")
            }

            if (!url.contains("state")) {
                throw OAuth2Exception("State param is missing from response url!")
            }

            val stateParam = stateRegex.find(url)?.value ?: ""
            if (stateParam == "") {
                throw IllegalStateException("Could not retrieve state param value!")
            }
            if (stateParam != state) {
                throw IllegalStateException("Saved State and retrieved one are different!")
            }

            val authCode = codeRegex.find(url)?.value ?: ""
            if (authCode == "") {
                throw IllegalStateException("Could not retrieve code param value!")
            }

            return requestToken(authCode)
        }

        return null
    }

    private fun requestToken(authCode: String): TokenBearer? {

        if (authType == AuthType.INSTALLED_APP &&
            credentials is ApplicationCredentials) {

            try {

                val req = api.getAccessToken(
                    header = "${credentials.clientId}:".toHeaderString(),
                    grantType = "authorization_code",
                    code = authCode,
                    redirectUrl = credentials.redirectUrl
                )

                val res = req.execute()

                return if (res.isSuccessful) {
                    val token = res.body() as Token
                    TokenBearer(storManager, token, credentials)
                } else {
                    null
                }
            } catch (ex: Exception) {
                ex.printStackTrace()

                return null
            }
        }

        return null
    }

    fun getRenewTokenRequest(token: Token): Call<RefreshToken>? {

        if (authType == AuthType.INSTALLED_APP) {

            return api.renewToken(
                header = "${credentials.clientId}:".toHeaderString(),

                refreshToken = token.refreshToken!!
            )
        }

        return null
    }

    fun getRevokeTokenRequest(token: Token): Call<Any>? {

        if (authType == AuthType.INSTALLED_APP) {

            return api.revoke(
                header = "${credentials.clientId}:".toHeaderString(),

                token = token.refreshToken!!,
                tokenTypeHint = "refresh_token"
            )
        }

        if (authType == AuthType.SCRIPT) {

            return api.revoke(
                header = "${(credentials as ScriptCredentials).clientId}:${credentials.clientSecret}".toHeaderString(),

                token = token.accessToken,
                tokenTypeHint = "access_token"
            )
        }

        return null
    }

    fun hasSavedBearer(): Boolean {
        return storManager.isAuthed() && storManager.hasToken()
    }

    fun getSavedBearer(): TokenBearer {
        return TokenBearer(storManager, storManager.getToken(), credentials)
    }

    class Builder {

        private var credentials: Credentials? = null

        private var scopes = ""
        private var storManager: StorageManager? = null

        private var logging: Boolean = false

        fun setCredentials(credentials: ApplicationCredentials): Builder {
            this.credentials = credentials
            return this
        }

        fun setCredentials(clientId: String, redirectUrl: String): Builder {
            this.credentials = ApplicationCredentials(clientId, redirectUrl)
            return this
        }

        fun setCredentials(credentials: ScriptCredentials): Builder {
            this.credentials = credentials
            return this
        }

        fun setCredentials(username: String, password: String, clientId: String, clientSecret: String): Builder {
            this.credentials = ScriptCredentials(username, password, clientId, clientSecret)
            return this
        }

        fun setScopes(scopes: Array<Scope>): Builder {
            this.scopes = scopes.toSeparatedString()
            return this
        }

        fun setScopes(scopes: List<Scope>): Builder {
            this.scopes = scopes.toSeparatedString()
            return this
        }

        fun setScopes(scopes: Array<String>): Builder {
            this.scopes = scopes.joinToString(separator = " ")
            return this
        }

        fun setStorageManager(storManager: StorageManager): Builder {
            this.storManager = storManager
            return this
        }

        fun setLogging(logging: Boolean): Builder {
            this.logging = logging
            return this
        }

        fun build(): RedditAuth {

            if (credentials != null &&
                credentials is ApplicationCredentials) {

                val state = generateRandomString()

                return RedditAuth(
                    credentials = credentials as ApplicationCredentials,
                    state = state,
                    scopes = scopes,
                    storManager = storManager
                        ?: throw IllegalArgumentException("StorageManager must not be null!"),
                    logging = logging
                )
            }

            if (credentials != null &&
                credentials is ScriptCredentials) {

                return RedditAuth(
                    credentials = credentials as ScriptCredentials,
                    scopes = scopes,
                    storManager = storManager
                        ?: throw IllegalArgumentException("StorageManager must not be null!"),
                    logging = logging
                )
            }

            throw IllegalArgumentException("Missing arguments!")
        }
    }
}
