# Android-Reddit-OAuth2

This is a OAuth2 authentication client for the Reddit API built for Android.

### How to use it.

1) First of all create the manager object that is responsible for the initial interaction with the browser and the retrieval of the token.
The clientId and redirectUrl should match what you wrote on the reddit api console, for more info refer to this: https://github.com/reddit-archive/reddit/wiki/oauth2

```
val manager = RedditAuthManager.Builder()
            .setClientId(creds.clientId)
            .setRedirectUrl(creds.redirectUrl)
            .setScopes(creds.scopes.toTypedArray())
            .setStorageManager(SharedPrefsStorageManager(this))
            .build()
```

2) You need to make your user input his username and password, and this is done with a webView.
The manager object we built in step 1 will provide the url: ```browser.loadUrl(authClient.provideAuthorizeUrl())```

3) We'll be watching for any redirects, so implement a webViewClient like so:

```
browser.webViewClient = object : WebViewClient() {
  override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
  
    if (authClient.isRedirectedUrl(url)) {
      browser.stopLoading()
      
        // We will retrieve the bearer on the background thread.
        doAsync(doWork = {
        
          // This is the tokenBearer object, that will manage your token. Done.
          val bearer = authClient.getTokenBearer(url)
        })
     }
  }
}
```

### License
This project is licensed under the MIT License
