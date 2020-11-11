package com.kirkbushman.auth.models.bearers

import com.kirkbushman.auth.AppAuth
import com.kirkbushman.auth.managers.StorageManager
import com.kirkbushman.auth.models.Token

class AppTokenBearer(
    storManager: StorageManager,
    private val appAuth: AppAuth
) : TokenBearer(
    storManager = storManager
) {

    override fun renewToken(token: Token): Token? {

        return appAuth.renewToken(token)
    }

    override fun revokeToken(token: Token): Boolean {

        return appAuth.revokeToken(token)
    }
}
