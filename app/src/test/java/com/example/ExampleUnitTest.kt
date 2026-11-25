package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `NordVPN servers include Ireland and Sweden`() {
        val countries = NordVpnServer.entries.map { it.countryName }
        assertEquals(listOf("Ireland", "Sweden"), countries)
    }

    @Test
    fun `official URLs use HTTPS and safe policy rejects non-HTTPS`() {
        assertEquals("https://elevenlabs.io/", OfficialAccessPolicy.ELEVENLABS_URL)
        assertEquals("https://my.nordaccount.com/", OfficialAccessPolicy.NORDVPN_ACCOUNT_URL)
        assertFalse(OfficialAccessPolicy.isSafeHttpsUrl("http://elevenlabs.io/"))
        assertFalse(OfficialAccessPolicy.isSafeHttpsUrl("javascript:alert(1)"))
        assertFalse(OfficialAccessPolicy.isSafeHttpsUrl("file:///etc/passwd"))
        assertFalse(OfficialAccessPolicy.isSafeHttpsUrl(null))
    }
}
