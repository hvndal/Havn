package com.havn.app.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.SoundPool
import android.os.Build
import com.havn.app.data.prefs.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Hävn Sound System — Pure Synthesized Warm Audio
 * Zero external asset dependencies. Generates authentic Muji/Ceramic soundscapes in memory.
 */
@Singleton
class SoundManager @Inject constructor(
    private val userPreferences: UserPreferences,
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    /**
     * Play subtle ceramic lid click (80ms warm snap)
     */
    fun playCeramicClick() {
        scope.launch {
            if (!isSoundEnabled()) return@launch
            synthesizeAndPlay(
                sampleRate = 44100,
                durationMs = 80,
                generator = { t ->
                    val env = Math.exp(-t * 85.0)
                    val tone = Math.sin(2.0 * Math.PI * 1350.0 * t) * 0.45
                    val noise = (Math.random() * 2.0 - 1.0) * 0.55
                    (tone + noise) * env * 0.4
                }
            )
        }
    }

    /**
     * Play warm Muji 432Hz bell chime (1.8s smooth harmonic decay)
     */
    fun playSoftChime() {
        scope.launch {
            if (!isSoundEnabled()) return@launch
            synthesizeAndPlay(
                sampleRate = 44100,
                durationMs = 1800,
                generator = { t ->
                    val env = Math.exp(-t * 2.2)
                    val f1 = Math.sin(2.0 * Math.PI * 432.0 * t) * 0.55
                    val f2 = Math.sin(2.0 * Math.PI * 648.0 * t) * 0.30 // perfect fifth
                    val f3 = Math.sin(2.0 * Math.PI * 864.0 * t) * 0.15 // octave
                    (f1 + f2 + f3) * env * 0.35
                }
            )
        }
    }

    /**
     * Play soft tap thud (100ms warm low pitch bend)
     */
    fun playSoftTap() {
        scope.launch {
            if (!isSoundEnabled()) return@launch
            synthesizeAndPlay(
                sampleRate = 44100,
                durationMs = 110,
                generator = { t ->
                    val env = Math.exp(-t * 38.0)
                    val freq = Math.max(70.0, 260.0 * (1.0 - t * 5.0))
                    Math.sin(2.0 * Math.PI * freq * t) * env * 0.5
                }
            )
        }
    }

    /**
     * Interface sounds are governed by their own preference.
     *
     * This previously read `reminderSound`, which conflated two unrelated
     * things: choosing a silent *notification* also silenced every in-app tap
     * and chime, and there was no way to keep notification sound while working
     * quietly in the app (or the reverse).
     */
    private suspend fun isSoundEnabled(): Boolean = userPreferences.interfaceSound.first()

    private fun synthesizeAndPlay(
        sampleRate: Int,
        durationMs: Int,
        generator: (Double) -> Double,
    ) {
        try {
            val numSamples = sampleRate * durationMs / 1000
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val sample = generator(t)
                val clamped = Math.max(-1.0, Math.min(1.0, sample))
                buffer[i] = (clamped * 32767.0).toInt().toShort()
            }

            val bufferSize = numSamples * 2
            val audioTrack = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                AudioTrack(
                    android.media.AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize,
                    AudioTrack.MODE_STATIC
                )
            }

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()

            // Auto release after playback
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (e: Exception) {}
            }, durationMs.toLong() + 100)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
