package com.kirkbushman.sampleapp

import android.app.Application
import com.kirkbushman.auth.RedditAuth
import com.kirkbushman.auth.managers.SharedPrefsStorageManager
import com.kirkbushman.auth.models.AuthType
import com.kirkbushman.auth.models.TokenBearer
import org.xmlpull.v1.XmlPullParser

class TestApplication : Application() {

    companion object {
        lateinit var instance: TestApplication
    }

    var authClient: RedditAuth? = null

    fun loadClient(authType: AuthType) {

        val creds = loadCredsFromFile()

        authClient = when (authType) {
            AuthType.INSTALLED_APP -> {

                RedditAuth.Builder()
                    .setCredentials(creds.clientId, creds.redirectUrl)
                    .setScopes(creds.scopes.toTypedArray())
                    .setStorageManager(SharedPrefsStorageManager(this))
                    .build()
            }

            AuthType.SCRIPT -> {

                RedditAuth.Builder()
                    .setCredentials(creds.clientId, creds.redirectUrl)
                    .setScopes(creds.scopes.toTypedArray())
                    .setStorageManager(SharedPrefsStorageManager(this))
                    .build()
            }

            else -> null
        }
    }

    private var bearer: TokenBearer? = null
    fun getBearer(): TokenBearer? {
        return bearer
    }
    fun setBearer(bearer: TokenBearer) {
        this.bearer = bearer
    }

    init {
        instance = this
    }

    private fun loadCredsFromFile(): TestCredentials {
        val xpp = resources.getXml(R.xml.credentials)

        var clientId = ""
        var redirectUrl = ""

        var scriptClientId = ""
        var scriptClientSecret = ""
        var username = ""
        var password = ""

        val scopes = ArrayList<String>()

        while (xpp.eventType != XmlPullParser.END_DOCUMENT) {

            when (xpp.eventType) {

                XmlPullParser.START_TAG -> {

                    when (xpp.name) {
                        "clientId" -> clientId = xpp.nextText()
                        "redirectUrl" -> redirectUrl = xpp.nextText()
                        "scope" -> scopes.add(xpp.nextText())
                        "scriptClientId" -> scriptClientId = xpp.nextText()
                        "scriptClientSecret" -> scriptClientSecret = xpp.nextText()
                        "username" -> username = xpp.nextText()
                        "password" -> password = xpp.nextText()
                    }
                }
            }

            xpp.next()
        }

        return TestCredentials(
            clientId,
            redirectUrl,

            scriptClientId,
            scriptClientSecret,
            username,
            password,

            scopes
        )
    }
}
