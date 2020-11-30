package com.kirkbushman.auth.models.creds

import android.os.Parcelable
import com.kirkbushman.auth.models.base.Credentials
import kotlinx.parcelize.Parcelize

@Parcelize
data class ApplicationCredentials(override val clientId: String, val redirectUrl: String) : Parcelable, Credentials
