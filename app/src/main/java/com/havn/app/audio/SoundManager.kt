package com.havn.app.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.havn.app.R
import com.havn.app.data.prefs.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interface sounds: a small felt-marimba set bundled in res/raw.
 *
 * Loaded once into a SoundPool, so a tap plays with no latency and no
 * per-play allocation (the previous version synthesised a fresh AudioTrack
 * every time). Played on the sonification stream, so they follow the
 * phone's touch-sound volume rather than media volume.
 */
@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext context: Context,
    private val userPreferences: UserPreferences,
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val pool = SoundPool.Builder()
        .setMaxStreams(3)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val lid = pool.load(context, R.raw.sfx_lid, 1)
    private val take = pool.load(context, R.raw.sfx_take, 1)
    private val tap = pool.load(context, R.raw.sfx_tap, 1)

    /** A compartment lid closing. */
    fun playCeramicClick() = play(lid, 0.5f)

    /** A dose logged. */
    fun playSoftChime() = play(take, 0.6f)

    /** Selection, undo. */
    fun playSoftTap() = play(tap, 0.4f)

    private fun play(id: Int, volume: Float) {
        scope.launch {
            if (userPreferences.interfaceSound.first()) {
                pool.play(id, volume, volume, 1, 0, 1f)
            }
        }
    }
}
