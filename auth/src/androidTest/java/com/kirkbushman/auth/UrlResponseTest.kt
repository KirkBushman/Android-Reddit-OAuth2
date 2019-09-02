package com.kirkbushman.lib

import android.content.Context
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlButton
import com.gargoylesoftware.htmlunit.html.HtmlInput
import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.kirkbushman.lib.managers.RedditAuthManager
import com.kirkbushman.lib.managers.SharedPrefsStorageManager
import org.junit.Test
import org.mockito.Mockito
import org.xmlpull.v1.XmlPullParser

class UrlResponseTest {

    private val authClient by lazy {

        val context = Mockito.mock(Context::class.java)
        val creds = loadCredsFromFile(context)

        RedditAuthManager.Builder()
            .setClientId(creds.clientId)
            .setRedirectUrl(creds.redirectUrl)
            .setScopes(creds.scopes.toTypedArray())
            .setStorageManager(SharedPrefsStorageManager(context))
            .build()
    }

    private val client by lazy {
        val client = WebClient()
        val options = client.options
        options.isCssEnabled = false
        options.isDownloadImages = false
        options.isRedirectEnabled = true
        options.isJavaScriptEnabled = false
        options.timeout = 6000

        client
    }

    @Test
    fun testFullAuth() {

        val url = authClient.provideAuthorizeUrl()

        System.out.println("url: $url")

        try {
            val page = client.getPage<HtmlPage>(url)

            System.out.println("page forms size ${page.forms.size}")
            System.out.println("attr ${page.forms.first().actionAttribute}")

            assert(page.baseURL.path.contains("/login"))
            assert(page.forms.isNotEmpty())
            assert(page.forms.any { it.actionAttribute.contains("/login.compact") })

            val form = page.forms.find { it.actionAttribute.contains("/login.compact") }!!

            val username = form.getInputByName<HtmlInput>("user")
            val password = form.getInputByName<HtmlInput>("passwd")
            val dest = form.getInputByName<HtmlInput>("dest")

            assert(username != null)
            assert(password != null)
            assert(dest != null)

            val submitButton = page.getFirstByXPath<HtmlButton>("//button[@type='submit']")

            assert(submitButton != null)
        } catch (ex: Exception) {
            System.out.println(ex.printStackTrace())
            assert(false)
        }
    }

    private fun loadCredsFromFile(context: Context?): Credentials {

        System.out.println("context is null: ${context == null}")
        System.out.println("resources is null: ${context!!.resources == null}")

        val xpp = context.resources.getXml(R.xml.credentials)

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
