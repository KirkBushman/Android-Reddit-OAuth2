package com.kirkbushman.auth.models

enum class AuthType {

    INSTALLED_APP,
    USERLESS,
    SCRIPT,

    // this state should never be used, give error
    NONE
}
