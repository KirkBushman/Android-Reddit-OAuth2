package com.kirkbushman.auth.managers

import com.kirkbushman.auth.models.Token

class NoOpStorageManager : StorageManager {

    override fun isAuthed(): Boolean {
        return false
    }

    override fun hasToken(): Boolean {
        return false
    }

    override fun getToken(): Token? {
        return null
    }

    override fun saveToken(token: Token) {}
    override fun deleteToken() {}
    override fun clearAll() {}
}
