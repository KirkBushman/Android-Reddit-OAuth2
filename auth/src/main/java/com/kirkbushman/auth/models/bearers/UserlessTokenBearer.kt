package com.kirkbushman.auth.models.bearers

import com.kirkbushman.auth.UserlessAuth
import com.kirkbushman.auth.managers.StorageManager
import com.kirkbushman.auth.models.Token

class UserlessTokenBearer(
    storManager: StorageManager,
    private val userlessAuth: UserlessAuth
) : TokenBearer(
    storManager = storManager
) {

    override fun renewToken(token: Token): Token? {

        return userlessAuth.renewToken(token)
    }

    override fun revokeToken(token: Token): Boolean {

        return userlessAuth.revokeToken(token)
    }
}
