package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `official ElevenLabs URL constant is HTTPS`() {
        assertEquals("https://elevenlabs.io/", ElevenLabsWebPolicy.OFFICIAL_URL)
    }

    @Test
    fun `safe URL policy rejects non-HTTPS schemes`() {
        assertFalse(ElevenLabsWebPolicy.isSafeHttpsUrl("http://elevenlabs.io"))
        assertFalse(ElevenLabsWebPolicy.isSafeHttpsUrl("javascript:alert(1)"))
        assertFalse(ElevenLabsWebPolicy.isSafeHttpsUrl("file:///etc/passwd"))
        assertFalse(ElevenLabsWebPolicy.isSafeHttpsUrl(""))
        assertFalse(ElevenLabsWebPolicy.isSafeHttpsUrl(null))
    }
}
