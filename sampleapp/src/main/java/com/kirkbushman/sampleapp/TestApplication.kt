package com.kirkbushman.sampleapp

import android.app.Application
import com.kirkbushman.auth.managers.RedditAuthManager
import com.kirkbushman.auth.managers.SharedPrefsStorageManager
import com.kirkbushman.auth.models.TokenBearer
import org.xmlpull.v1.XmlPullParser

class TestApplication : Application() {

    companion object {
        lateinit var instance: TestApplication
    }

    val authClient by lazy {

        val creds = loadCredsFromFile()

        RedditAuthManager.Builder()
            .setClientId(creds.clientId)
            .setRedirectUrl(creds.redirectUrl)
            .setScopes(creds.scopes.toTypedArray())
            .setStorageManager(SharedPrefsStorageManager(this))
            .build()
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

    private fun loadCredsFromFile(): Credentials {
        val xpp = resources.getXml(R.xml.credentials)

        var clientId = ""
        var redirectUrl = ""
        val scopes = ArrayList<String>()

        while (xpp.eventType != XmlPullParser.END_DOCUMENT) {

            when (xpp.eventType) {

                XmlPullParser.START_TAG -> {

                    when (xpp.name) {
                        "clientId" -> clientId = xpp.nextText()
                        "redirectUrl" -> redirectUrl = xpp.nextText()
                        "scope" -> scopes.add(xpp.nextText())
                    }
                }
            }

            xpp.next()
        }

        return Credentials(clientId, redirectUrl, scopes)
    }
}
