package com.kirkbushman.auth.models

import android.os.Parcelable
import com.kirkbushman.auth.models.base.Credentials
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ApplicationCredentials(override val clientId: String, val redirectUrl: String) : Parcelable, Credentials
