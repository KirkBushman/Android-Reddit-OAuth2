package com.kirkbushman.sampleapp.local.tests

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.kirkbushman.auth.AppAuth
import com.kirkbushman.auth.http.RedditAuthClient
import com.kirkbushman.auth.managers.StorageManager
import com.kirkbushman.auth.models.Token
import com.kirkbushman.auth.models.creds.ApplicationCredentials
import com.kirkbushman.auth.models.enums.AuthType
import okhttp3.ResponseBody
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.Silent::class)
class AppAuthTests {

    companion object {

        private const val accessToken = "access_token"
        private const val refreshToken = "refresh_token"
        private const val tokenType = "test_type"
        private const val expiresInSecs = 121212
        private const val createdTime = 12323423L
        private const val scopes = "scopes"

        private val testToken = Token(
            accessToken = accessToken,
            refreshToken = refreshToken,
            tokenType = tokenType,
            expiresInSecs = expiresInSecs,
            createdTime = createdTime,
            scopes = scopes
        )

        private val testNullToken = Token(
            accessToken = accessToken,
            refreshToken = null,
            tokenType = tokenType,
            expiresInSecs = expiresInSecs,
            createdTime = createdTime,
            scopes = scopes
        )
    }

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    lateinit var auth: AppAuth

    @Mock
    lateinit var client: RedditAuthClient
    @Mock
    lateinit var credentials: ApplicationCredentials
    @Mock
    lateinit var storageManager: StorageManager

    @Before
    fun onPre() {
        auth = AppAuth(
            client,
            credentials,
            storageManager,
            scopes
        )
    }

    @Test
    fun authTest_checkType() {

        assertEquals(AuthType.INSTALLED_APP, auth.getAuthType())
    }

    @Test
    fun authTest_renewToken() {

        Mockito.`when`(credentials.clientId).thenReturn("")

        Mockito
            .`when`(
                client.renewRefreshToken("", testToken.refreshToken!!)
            )
            .thenReturn(testToken)

        val newToken = auth.renewToken(testToken)

        assertEquals(testToken, newToken)

        assertEquals(accessToken, newToken!!.accessToken)
        assertEquals(refreshToken, newToken.refreshToken)
        assertEquals(tokenType, newToken.tokenType)
        assertEquals(expiresInSecs, newToken.expiresInSecs)
        assertEquals(createdTime, newToken.createdTime)
        assertEquals(scopes, newToken.scopes)
    }

    @Test
    fun authTest_renewTokenNull() {

        Mockito.`when`(credentials.clientId).thenReturn("")

        Mockito
            .`when`(
                client.renewRefreshToken("", testToken.refreshToken!!)
            )
            .thenReturn(null)

        val newToken = auth.renewToken(testToken)

        assertEquals(null, newToken)
    }

    @Test
    fun authTest_renewTokenMissingRefresh() {

        var exception: Exception? = null

        try {
            auth.renewToken(testNullToken)
        } catch (ex: Exception) {
            ex.printStackTrace()
            exception = ex
        } finally {
            assertNotNull(exception)
        }
    }

    @Test
    fun authTest_revokeTokenError() {

        Mockito.`when`(credentials.clientId).thenReturn("")

        Mockito
            .`when`(
                client.revokeRefreshToken("", testToken.refreshToken!!)
            )
            .thenThrow(IllegalStateException("test exception"))

        assertEquals(false, auth.revokeToken(testToken))
    }

    @Test
    fun authTest_revokeTokenNull() {

        Mockito.`when`(credentials.clientId).thenReturn("")

        Mockito
            .`when`(
                client.revokeRefreshToken("", testToken.refreshToken!!)
            )
            .thenReturn(null)

        assertEquals(false, auth.revokeToken(testToken))
    }

    @Test
    fun authTest_revokeTokenRight() {

        Mockito.`when`(credentials.clientId).thenReturn("")

        val response = Mockito.mock(ResponseBody::class.java)

        Mockito
            .`when`(
                client.revokeRefreshToken("", testToken.refreshToken!!)
            )
            .thenReturn(response)

        assertEquals(true, auth.revokeToken(testToken))
    }

    @Test
    fun authTest_revokeTokenMissingRefresh() {

        var exception: Exception? = null

        try {
            auth.revokeToken(testNullToken)
        } catch (ex: Exception) {
            ex.printStackTrace()
            exception = ex
        } finally {
            assertNotNull(exception)
        }
    }

    @Test
    fun authTest_hasSavedBearerWrongType() {

        Mockito.`when`(storageManager.isAuthed()).thenReturn(true)
        Mockito.`when`(storageManager.hasToken()).thenReturn(true)
        Mockito.`when`(storageManager.authType()).thenReturn(AuthType.USERLESS)

        Assert.assertFalse(auth.hasSavedBearer())
    }

    @Test
    fun authTest_hasSavedBearerEmpty() {

        Mockito.`when`(storageManager.isAuthed()).thenReturn(false)
        Mockito.`when`(storageManager.hasToken()).thenReturn(false)
        Mockito.`when`(storageManager.authType()).thenReturn(AuthType.INSTALLED_APP)

        Assert.assertFalse(auth.hasSavedBearer())
    }

    @Test
    fun authTest_hasSavedBearerRight() {

        Mockito.`when`(storageManager.isAuthed()).thenReturn(true)
        Mockito.`when`(storageManager.hasToken()).thenReturn(true)
        Mockito.`when`(storageManager.authType()).thenReturn(AuthType.INSTALLED_APP)

        Assert.assertTrue(auth.hasSavedBearer())
    }
}
