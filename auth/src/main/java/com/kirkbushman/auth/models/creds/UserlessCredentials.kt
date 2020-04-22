package com.kirkbushman.auth.models.creds

import android.os.Parcelable
import com.kirkbushman.auth.models.base.Credentials
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserlessCredentials(override val clientId: String, val deviceId: String? = null) : Parcelable, Credentials
