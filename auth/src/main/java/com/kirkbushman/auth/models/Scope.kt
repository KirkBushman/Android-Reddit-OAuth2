package com.kirkbushman.auth.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Scope(

    @Json(name = "id")
    val id: String,
    @Json(name = "name")
    val name: String,

    @Json(name = "description")
    val description: String
)
