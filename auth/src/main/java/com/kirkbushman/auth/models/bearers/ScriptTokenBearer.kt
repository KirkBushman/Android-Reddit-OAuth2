package com.kirkbushman.auth.models.bearers

import com.kirkbushman.auth.ScriptAuth
import com.kirkbushman.auth.managers.StorageManager
import com.kirkbushman.auth.models.Token

class ScriptTokenBearer(
    storManager: StorageManager,
    private val scriptAuth: ScriptAuth
) : TokenBearer(
    storManager = storManager
) {

    override fun renewToken(token: Token): Token? {

        return scriptAuth.renewToken(token)
    }

    override fun revokeToken(token: Token): Boolean {

        return scriptAuth.revokeToken(token)
    }
}
