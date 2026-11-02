package com.freeturn.app.domain.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdaterVersionTest {

    @Test
    fun debugSuffixDoesNotMakeOlderReleaseNewer() {
        assertFalse(AppUpdater.isNewer("5.0.2", "5.0.3-debug"))
        assertFalse(AppUpdater.isNewer("5.0.3", "5.0.3-debug"))
    }

    @Test
    fun newerReleaseIsOffered() {
        assertTrue(AppUpdater.isNewer("5.0.4", "5.0.3-debug"))
        assertTrue(AppUpdater.isNewer("5.1.0", "5.0.3"))
    }
}
