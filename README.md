# Android-Reddit-OAuth2

This is a OAuth2 authentication client for the Reddit API built for Android.\
This library is used as a base for this API wrapper:
https://github.com/KirkBushman/ARAW


### How to install.

```groovy
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

```kotlin
val authClient = RedditAuth.Builder()
            // specify the credentials you can find on your reddit app console
            .setApplicationCredentials(creds.clientId, creds.redirectUrl)
            // the api endpoints scopes this client will need
            .setScopes(arrayOf("Input", "scopes", "here"))
            // to manage tokens info in memory
            .setStorageManager(SharedPrefsStorageManager(this))
            // if you set this flag to 'true' it will add to the OkHttp Client a listener to log the
            // Request and Response object, to make it easy to debug.
            .setLogging(true)
            .build()
```

2) You need to make your user input his username and password, and this is done with a webView.
The manager object we built in step 1 will provide the url: ```browser.loadUrl(authClient.provideAuthorizeUrl())```

3) We'll be watching for any redirects, so implement a webViewClient like this:

```kotlin
val browser = WebView(context)

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

4) The token bearer is what will help you keep the token updated and make requests, Enjoy.



### Userless Flow
##### For navigating without a user context.

1) Register your application at: https://www.reddit.com/prefs/apps

2) Create the auth manager object like so:

```kotlin
val authClient = RedditAuth.Builder()
                    // specify the credentials you can find on your reddit app console,
                    // in this case only the client id is provided.
                    .setUserlessCredentials(creds.clientId)
                    // the api endpoints scopes this client will need
                    .setScopes(creds.scopes.toTypedArray())
                    // to manage tokens info in memory
                    .setStorageManager(SharedPrefsStorageManager(this))
                    // if you set this flag to 'true' it will add to the OkHttp Client a listener to log the
                    // Request and Response object, to make it easy to debug.
                    .setLogging(true)
                    .build()
```

3) Simply use this code, to get your token bearer:

```kotlin
bearer = authClient?.getTokenBearer()
```

4) Enjoy.



### Script Flow
##### Do not use this in your app, do not store the client secret in the Apk or where people can see it.
##### This method is useful for testing purpuses, like a junit testing class for you app.

1) Register your script at: https://www.reddit.com/prefs/apps, make sure that your account is a `developer` for that script.

2) Create the auth manager object like so:

```kotlin
val authClient = RedditAuth.Builder()
                // specify the credentials you can find on your reddit app console
                // for script / web apps you will be given a client id and a client secret
                .setCredentials(creds.username, creds.password, creds.scriptClientId, creds.scriptClientSecret)
                // the api endpoints scopes this client will need
                .setScopes(creds.scopes.toTypedArray())
                // to manage tokens info in memory
                .setStorageManager(SharedPrefsStorageManager(this))
                // if you set this flag to 'true' it will add to the OkHttp Client a listener to log the
                // Request and Response object, to make it easy to debug.
                .setLogging(true)
                .build()
```

3) Simply use this code, to get your token bearer:

```kotlin
bearer = authClient?.getTokenBearer()
```

4) Enjoy.



### Be a good citizen of the web!
When you are done using any token, rember to revoke them, and not let them around!


### License
This project is licensed under the MIT License
