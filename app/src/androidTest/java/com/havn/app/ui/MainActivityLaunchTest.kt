package com.havn.app.ui

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Cold-start smoke test. versionCode 20 crashed in setContent with
 * "CompositionLocal LocalLifecycleOwner not present"; this fails if the
 * composition root ever throws again.
 */
@RunWith(AndroidJUnit4::class)
class MainActivityLaunchTest {

    @Test
    fun coldStart_reachesResumed() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.moveToState(Lifecycle.State.RESUMED)
            assertEquals(Lifecycle.State.RESUMED, scenario.state)
        }
    }
}
