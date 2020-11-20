package com.kirkbushman.sampleapp.local.tests

import com.kirkbushman.auth.RedditAuth
import com.kirkbushman.auth.managers.NoOpStorageManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class BuildersTests {

    @Test
    fun userlessBuilderTest_yesStorManager() {

        var exception: Exception? = null

        try {

            RedditAuth.Builder()
                .setUserlessCredentials("")
                .setStorageManager(NoOpStorageManager())
                .setLogging(true)
                .build()
        } catch (ex: Exception) {
            ex.printStackTrace()
            exception = ex
        } finally {
            assertNull("Assert that an error is thrown when storeManager is not set", exception)
        }
    }

    @Test
    fun userlessBuilderTest_noStorManager() {

        var exception: Exception? = null

        try {

            RedditAuth.Builder()
                .setUserlessCredentials("")
                .setLogging(true)
                .build()
        } catch (ex: Exception) {
            ex.printStackTrace()
            exception = ex
        } finally {
            assertNotNull("Assert that an error is thrown when storeManager is not set", exception)
            assertEquals(
                "Assert the error is IllegalStateException",
                IllegalArgumentException::class.java,
                exception?.javaClass
            )
        }
    }
}
