package com.kirkbushman.auth.managers

import com.kirkbushman.auth.models.enums.AuthType
import com.kirkbushman.auth.models.Token

/**
 * Interface for persisting auth data on device,
 * can be extended with your preferred method
 */
interface StorageManager {

    fun isAuthed(): Boolean

    fun authType(): AuthType

    fun hasToken(): Boolean
    fun getToken(): Token?

    fun saveToken(token: Token, authType: AuthType)

    fun clearAll()
}
