package com.kirkbushman.auth.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BasicInfo(val clientId: String, val redirectUrl: String) : Parcelable