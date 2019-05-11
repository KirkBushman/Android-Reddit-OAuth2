package com.kirkbushman.lib.errors

class InvalidRequestException(errorStr: String) : OAuth2Exception(errorStr)