package com.kirkbushman.auth.managers

import com.kirkbushman.auth.models.enums.AuthType
import com.kirkbushman.auth.models.Token

class NoOpStorageManager : StorageManager {

    override fun isAuthed(): Boolean {
        return false
    }

    override fun authType(): AuthType {
        return AuthType.NONE
    }

    override fun hasToken(): Boolean {
        return false
    }

    override fun getToken(): Token? {
        return null
    }

    override fun saveToken(token: Token, authType: AuthType) = Unit
    override fun clearAll() = Unit
}
