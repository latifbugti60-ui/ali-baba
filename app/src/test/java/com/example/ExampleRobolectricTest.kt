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
    fun `app_name matches Latif AI Studio`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Latif AI Studio", appName)
    }

    @Test
    fun `embedded WebView allowlist permits official ElevenLabs and auth hosts only`() {
        assertTrue(ElevenLabsWebPolicy.shouldEmbedInWebView("https://elevenlabs.io/"))
        assertTrue(ElevenLabsWebPolicy.shouldEmbedInWebView("https://app.elevenlabs.io/speech-synthesis"))
        assertTrue(ElevenLabsWebPolicy.shouldEmbedInWebView("https://accounts.google.com/o/oauth2/v2/auth"))
        assertFalse(ElevenLabsWebPolicy.shouldEmbedInWebView("https://evil.example.com/phish"))
        assertFalse(ElevenLabsWebPolicy.shouldEmbedInWebView("http://elevenlabs.io/"))
    }
}
