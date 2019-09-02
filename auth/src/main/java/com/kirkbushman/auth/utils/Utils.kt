package com.kirkbushman.auth.utils

import android.util.Base64
import com.kirkbushman.auth.models.Scope

object Utils {

    private val STRING_CHARACTERS = ('0'..'9').plus('a'..'z').toTypedArray()

    fun addParamsToUrl(url: String, array: Array<String>): String {
        return url.plus(array.joinToString(separator = "&", prefix = "?"))
    }

    fun generateRandomString(): String {
        return (1..32).map { STRING_CHARACTERS.random() }.joinToString("")
    }
}

fun Array<Scope>.toSeparatedString(): String {
    return joinToString(separator = " ")
}

fun List<Scope>.toSeparatedString(): String {
    return joinToString(separator = " ")
}

fun String.toHeaderString(): HashMap<String, String> {
    return hashMapOf("Authorization" to "Basic ".plus(Base64.encodeToString(this.toByteArray(), Base64.NO_WRAP)))
}
