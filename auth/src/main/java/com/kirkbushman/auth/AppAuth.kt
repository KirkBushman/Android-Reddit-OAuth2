package com.kirkbushman.auth

import com.kirkbushman.auth.errors.AccessDeniedException
import com.kirkbushman.auth.errors.InvalidRequestException
import com.kirkbushman.auth.errors.InvalidScopesException
import com.kirkbushman.auth.errors.OAuth2Exception
import com.kirkbushman.auth.errors.RefreshTokenMissingException
import com.kirkbushman.auth.errors.UnsupportedResponseTypeException
import com.kirkbushman.auth.http.RedditAuthClient
import com.kirkbushman.auth.managers.StorageManager
import com.kirkbushman.auth.models.Token
import com.kirkbushman.auth.models.bearers.AppTokenBearer
import com.kirkbushman.auth.models.enums.AuthType
import com.kirkbushman.auth.models.bearers.TokenBearer
import com.kirkbushman.auth.models.creds.ApplicationCredentials
import com.kirkbushman.auth.utils.Utils

// Installed App Authentication
class AppAuth(

    private val client: RedditAuthClient,

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
    private val credentials: ApplicationCredentials,

    /**
     * Interface instance that is used to persist token to memory.
     * Can be extended using the method you prefer, this lib provides a working
     * example with SharedPreferences
     */
    private val storManager: StorageManager,

    /**
     * Permissions the client will have, should be as tight as possible.
     * Can be retrieved at https://www.reddit.com/api/v1/scopes
     */
    private val scopes: String

) : RedditAuth(
    client = client,
    credentials = credentials,
    storManager = storManager,
    authType = AuthType.INSTALLED_APP
) {

    companion object {

        private const val BASE_AUTH_URL = "${Utils.BASE_URL}/api/v1/authorize.compact"

        private val codeRegex = Regex("(?<=(code=))([a-zA-Z0-9]|-|_)+(?=(&|\\s|))")
        private val stateRegex = Regex("(?<=(state=))([a-zA-Z0-9]|-|_)+(?=(&|\\s|))")
        private val errorRegex = Regex("(?<=(error=))([a-zA-Z0-9]|-|_)+(?=(&|\\s|))")
    }

    private val state: String by lazy { Utils.generateRandomString() }

    private var authCode: String? = null

    override fun renewToken(token: Token): Token? {

        if (token.refreshToken == null) {

            throw RefreshTokenMissingException()
        }

        return client.renewRefreshToken(
            clientId = credentials.clientId,
            refreshToken = token.refreshToken
        )
    }

    override fun revokeToken(token: Token): Boolean {

        if (token.refreshToken == null) {

            throw RefreshTokenMissingException()
        }

        return try {

            val res = client.revokeRefreshToken(
                clientId = credentials.clientId,
                refreshToken = token.refreshToken
            )

            res != null
        } catch (ex: Exception) {
            ex.printStackTrace()

            false
        }
    }

    override fun fetchToken(): Token? {

        return client.accessToken(
            clientId = credentials.clientId,
            redirectUrl = credentials.redirectUrl,
            authCode = authCode ?: ""
        )
    }

    fun provideAuthorizeUrl(): String {

        val params = arrayOf(
            "client_id=${credentials.clientId}",
            "response_type=code",
            "state=$state",
            "redirect_uri=${credentials.redirectUrl}",
            "duration=permanent",
            "scope=$scopes"
        )

        return Utils.addParamsToUrl(BASE_AUTH_URL, params)
    }

    fun isRedirectedUrl(url: String?): Boolean {

        if (url == null) {
            throw IllegalStateException("Provided url is null!")
        }

        if (credentials.redirectUrl.isEmpty()) {
            throw IllegalStateException("Redirect Url was not provided or invalid!")
        }

        return url.startsWith(credentials.redirectUrl)
    }

    fun authenticate(url: String?): TokenBearer? {

        if (url == null) {
            throw IllegalStateException("Provided url is null!")
        }

        if (url.contains("error")) {

            val errorStr = errorRegex.find(url)?.value ?: ""
            throwOnErrorStr(errorStr)
        }

        if (!url.contains("state")) {
            throw OAuth2Exception("State param is missing from response url!")
        }

        val stateStr = stateRegex.find(url)?.value ?: ""
        throwOnStateStr(stateStr)

        authCode = codeRegex.find(url)?.value ?: ""
        if (authCode == "") {
            throw IllegalStateException("Could not retrieve code param value!")
        }

        try {

            val token = fetchToken()

            return if (token != null) {

                saveToken(token)

                AppTokenBearer(
                    storManager = storManager,
                    this
                )
            } else {
                null
            }
        } catch (ex: Exception) {
            ex.printStackTrace()

            return null
        }
    }

    override fun retrieveSavedBearer(): TokenBearer? {

        if (!hasSavedBearer())
            return null

        return AppTokenBearer(
            storManager = storManager,
            appAuth = this
        )
    }

    private fun throwOnErrorStr(errorStr: String) {

        throw when (errorStr) {
            "access_denied" ->
                AccessDeniedException("User chose not to grant your app permissions!")
            "unsupported_response_type" ->
                UnsupportedResponseTypeException("Invalid response_type parameter in initial Authorization!")
            "invalid_scope" ->
                InvalidScopesException("Invalid scope parameter in initial Authorization!")
            "invalid_request" ->
                InvalidRequestException("There was an issue with the request sent to /api/v1/authorize!")

            else -> OAuth2Exception("The Reddit API returned the error: $errorStr")
        }
    }

    private fun throwOnStateStr(stateStr: String) {

        when {
            stateStr == "" ->
                throw IllegalStateException("Could not retrieve state param value!")
            stateStr != state ->
                throw IllegalStateException("Saved State and retrieved one are different!")
        }
    }
}
