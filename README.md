# Android-Reddit-OAuth2

This is a OAuth2 authentication client for the Reddit API built for Android.



### How to install.

```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    implementation 'com.github.KirkBushman:Android-Reddit-OAuth2:Tag'
}
```


### How to use it.


### Installed App Flow

1) Register your application at: https://www.reddit.com/prefs/apps

2) Create the manager object that is responsible for the initial interaction with the browser and the retrieval of the token.
The clientId and redirectUrl should match what you wrote on the reddit api console, for more info refer to this: https://github.com/reddit-archive/reddit/wiki/oauth2

```
val manager = RedditAuth.Builder()
            .setCredentials(creds.clientId, creds.redirectUrl)
            .setScopes(creds.scopes.toTypedArray())
            .setStorageManager(SharedPrefsStorageManager(this))
            .build()
```

2) You need to make your user input his username and password, and this is done with a webView.
The manager object we built in step 1 will provide the url: ```browser.loadUrl(authClient.provideAuthorizeUrl())```

3) We'll be watching for any redirects, so implement a webViewClient like this:

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

4) The token bearer is what will help keep the token updated and make requests, Enjoy.


### Script Flow

1) Register your script at: https://www.reddit.com/prefs/apps, make sure that your account is a `developer` for that script.

2) Create the auth manager object like so:

```
val manager = RedditAuth.Builder()
                .setCredentials(creds.username, creds.password, creds.scriptClientId, creds.scriptClientSecret)
                .setScopes(creds.scopes.toTypedArray())
                .setStorageManager(SharedPrefsStorageManager(this))
                .build()
```
(Notice that for this kind of flow, you will provide username and password for your account and also both the clientId and the clientSecret).

3) Simply use this code, to get your token bearer:

```
bearer = authClient?.getTokenBearer()
```

4) Enjoy.


### Be a good citizen of the web!
When you are done using any token, rember to revoke them, and not let them around!


### License
This project is licensed under the MIT License
