package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `app_name matches Latif VPN Control`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Latif VPN Control", appName)
    }

    @Test
    fun `embedded WebView allowlist permits official ElevenLabs, Nord Account, and auth hosts only`() {
        assertTrue(OfficialAccessPolicy.shouldEmbedInWebView("https://elevenlabs.io/"))
        assertTrue(OfficialAccessPolicy.shouldEmbedInWebView("https://app.elevenlabs.io/speech-synthesis"))
        assertTrue(OfficialAccessPolicy.shouldEmbedInWebView("https://my.nordaccount.com/"))
        assertTrue(OfficialAccessPolicy.shouldEmbedInWebView("https://accounts.google.com/o/oauth2/v2/auth"))
        assertFalse(OfficialAccessPolicy.shouldEmbedInWebView("https://evil.example.com/phish"))
        assertFalse(OfficialAccessPolicy.shouldEmbedInWebView("http://elevenlabs.io/"))
    }
}
