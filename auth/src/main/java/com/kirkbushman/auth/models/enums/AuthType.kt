package com.kirkbushman.auth.models.enums

enum class AuthType {

    INSTALLED_APP,
    USERLESS,
    SCRIPT,

    // this state should never be used, give error
    NONE
}
