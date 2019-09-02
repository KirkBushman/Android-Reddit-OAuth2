package com.kirkbushman.auth.managers

import com.kirkbushman.auth.errors.AccessDeniedException
import com.kirkbushman.auth.errors.InvalidRequestException
import com.kirkbushman.auth.errors.InvalidScopesException
import com.kirkbushman.auth.errors.OAuth2Exception
import com.kirkbushman.auth.errors.UnsupportedResponseTypeException
import com.kirkbushman.auth.http.RedditService
import com.kirkbushman.auth.models.BasicInfo
import com.kirkbushman.auth.models.Scope
import com.kirkbushman.auth.models.ScopesEnvelope
import com.kirkbushman.auth.models.Token
import com.kirkbushman.auth.models.TokenBearer
import com.kirkbushman.auth.utils.Utils.addParamsToUrl
import com.kirkbushman.auth.utils.Utils.generateRandomString
import com.kirkbushman.auth.utils.toHeaderString
import com.kirkbushman.auth.utils.toSeparatedString
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Class that is needed to interact with reddit authentication using a webView
 */
class RedditAuthManager(

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
    private val basicInfo: BasicInfo,

    /**
     * This is a random string that is given as a url param at beginning of the auth request,
     * we must check it's the same on future responses.
     */
    private val state: String,

    /**
     * Permissions the client will have, should be as tight as possible.
     * Can be retrieved at https://www.reddit.com/api/v1/scopes
     */
    private val scopes: String,

    /**
     * Interface instance that is used to persist token to memory.
     * Can be extended using the method you prefer, this lib provides a working
     * example with SharedPreferences
     */
    private val storManager: StorageManager

) {

    companion object {
        private const val BASE_URL = "https://www.reddit.com"
        private const val BASE_AUTH_URL = "$BASE_URL/api/v1/authorize.compact"

        private val codeRegex = Regex("(?<=(code=))([a-zA-Z0-9]|-|_)+(?=(&|\\s|))")
        private val stateRegex = Regex("(?<=(state=))([a-zA-Z0-9]|-|_)+(?=(&|\\s|))")
        private val errorRegex = Regex("(?<=(error=))([a-zA-Z0-9]|-|_)+(?=(&|\\s|))")

        private val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        val api: RedditService = retrofit.create(RedditService::class.java)

        fun getScopes(): ScopesEnvelope? {
            val req = api.getScopes()
            val res = req.execute()

            if (res.isSuccessful) {
                return res.body()
            } else {
                throw Exception(res.errorBody().toString())
            }
        }
    }

    constructor(clientId: String, redirectUrl: String, state: String, scopes: String, storManager: StorageManager) :
            this(BasicInfo(clientId, redirectUrl), state, scopes, storManager)

    fun provideAuthorizeUrl(): String {

        val params = arrayOf(
            "client_id=${basicInfo.clientId}",
            "response_type=code",
            "state=$state",
            "redirect_uri=${basicInfo.redirectUrl}",
            "duration=permanent",
            "scope=$scopes"
        )

        return addParamsToUrl(BASE_AUTH_URL, params)
    }

    fun isRedirectedUrl(url: String?): Boolean {

        if (url == null) {
            throw IllegalStateException("Provided url is null!")
        }

        if (basicInfo.redirectUrl.isEmpty()) {
            throw IllegalStateException("Redirect Url was not provided or invalid!")
        }

        return url.startsWith(basicInfo.redirectUrl)
    }

    fun getTokenBearer(url: String?): TokenBearer? {

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

    private fun requestToken(authCode: String): TokenBearer? {

        try {

            val req = api.getAccessToken(
                header = "${basicInfo.clientId}:".toHeaderString(),
                code = authCode,
                redirectUrl = basicInfo.redirectUrl
            )

            val res = req.execute()

            return if (res.isSuccessful) {
                val token = res.body() as Token
                TokenBearer(storManager, token, basicInfo)
            } else {
                null
            }
        } catch (ex: Exception) {
            ex.printStackTrace()

            return null
        }
    }

    fun hasSavedBearer(): Boolean {
        return storManager.isAuthed() && storManager.hasToken()
    }

    fun getSavedBearer(): TokenBearer {
        return TokenBearer(storManager, storManager.getToken(), basicInfo)
    }

    class Builder {

        private var clientId = ""
        private var redirectUrl = ""
        private var scopes = ""

        private var basicInfo: BasicInfo? = null
        private var storManager: StorageManager? = null

        fun setBasicInfo(basicInfo: BasicInfo): Builder {
            this.basicInfo = basicInfo
            return this
        }

        fun setClientId(clientId: String): Builder {
            this.clientId = clientId
            return this
        }

        fun setRedirectUrl(redirectUrl: String): Builder {
            this.redirectUrl = redirectUrl
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

        fun build(): RedditAuthManager {
            val state = generateRandomString()

            if (basicInfo != null) {
                return RedditAuthManager(
                    basicInfo = basicInfo!!,
                    state = state,
                    scopes = scopes,
                    storManager = storManager ?: throw IllegalArgumentException("StorageManager must not be null!")
                )
            }

            return RedditAuthManager(
                clientId = clientId,
                redirectUrl = redirectUrl,
                state = state,
                scopes = scopes,
                storManager = storManager ?: throw IllegalArgumentException("StorageManager must not be null!")
            )
        }
    }
}
