package com.kirkbushman.lib.managers

import com.kirkbushman.lib.models.Token

/**
 * Interface for persisting auth data on device,
 * can be extended with your preferred method
 */
interface StorageManager {

    fun isAuthed(): Boolean

    fun hasToken(): Boolean
    fun getToken(): Token

    fun saveToken(token: Token)
    fun deleteToken()

    fun clearAll()
}