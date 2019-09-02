package com.kirkbushman.auth.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ScopesEnvelope(

    @Json(name = "creddits")
    val creddits: Scope,
    @Json(name = "modcontributors")
    val modcontributors: Scope,
    @Json(name = "modmail")
    val modmail: Scope,
    @Json(name = "modconfig")
    val modconfig: Scope,
    @Json(name = "subscribe")
    val subscribe: Scope,
    @Json(name = "structuredstyles")
    val structuredstyles: Scope,
    @Json(name = "vote")
    val vote: Scope,
    @Json(name = "wikiedit")
    val wikiedit: Scope,
    @Json(name = "mysubreddits")
    val mysubreddits: Scope,
    @Json(name = "submit")
    val submit: Scope,
    @Json(name = "modlog")
    val modlog: Scope,
    @Json(name = "modposts")
    val modposts: Scope,
    @Json(name = "modflair")
    val modflair: Scope,
    @Json(name = "save")
    val save: Scope,
    @Json(name = "modothers")
    val modothers: Scope,
    @Json(name = "read")
    val read: Scope,
    @Json(name = "privatemessages")
    val privatemessages: Scope,
    @Json(name = "report")
    val report: Scope,
    @Json(name = "identity")
    val identity: Scope,
    @Json(name = "livemanage")
    val livemanage: Scope,
    @Json(name = "account")
    val account: Scope,
    @Json(name = "modtraffic")
    val modtraffic: Scope,
    @Json(name = "wikiread")
    val wikiread: Scope,
    @Json(name = "edit")
    val edit: Scope,
    @Json(name = "modwiki")
    val modwiki: Scope,
    @Json(name = "modself")
    val modself: Scope,
    @Json(name = "history")
    val history: Scope,
    @Json(name = "flair")
    val flair: Scope

) {

    fun toScopesArray(): Array<Scope> {
        return arrayOf(
            creddits,
            modcontributors,
            modmail,
            modconfig,
            subscribe,
            structuredstyles,
            vote,
            wikiedit,
            mysubreddits,
            submit,
            modlog,
            modposts,
            modflair,
            save,
            modothers,
            read,
            privatemessages,
            report,
            identity,
            livemanage,
            account,
            modtraffic,
            wikiread,
            edit,
            modwiki,
            modself,
            history,
            flair
        )
    }

    fun noMods(): Array<Scope> {
        return arrayOf(
            creddits,
            subscribe,
            structuredstyles,
            vote,
            wikiedit,
            mysubreddits,
            submit,
            save,
            read,
            privatemessages,
            report,
            identity,
            livemanage,
            account,
            wikiread,
            edit,
            history,
            flair
        )
    }

    fun toSeparatedString(): String {
        return toScopesArray().joinToString(separator = " ")
    }
}
