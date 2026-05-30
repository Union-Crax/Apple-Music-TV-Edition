package com.apple.music.tv.audio

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import com.apple.music.tv.data.model.Mood
import kotlin.concurrent.thread
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.tanh

/**
 * A tiny real-time software synthesiser that turns a [Mood] into actual sound.
 *
 * Because the app can't legally stream Apple Music, "playback" is genuine audio
 * generated on the device: each mood maps to a chord progression that is
 * arpeggiated, padded and (optionally) given a beat, then run through a feedback
 * delay and a gentle low-pass for space. Pressing Play really does play music —
 * with zero network access and no bundled media files.
 *
 * All DSP runs on a dedicated background [thread]; the public API is safe to call
 * from the main thread. The thread parks itself when paused so idle CPU is ~0.
 */
class ToneAudioEngine {

    // ── Public state ────────────────────────────────────────────────────────────
    @Volatile private var running = false
    @Volatile private var playing = false
    @Volatile private var spec: MoodSpec = SPECS.getValue(Mood.CHILL)
    @Volatile private var targetVolume = 0.7

    private var audioTrack: AudioTrack? = null
    private var renderThread: Thread? = null
    private val gate = Object()

    // ── Lifecycle ────────────────────────────────────────────────────────────────

    /** Begin (or resume) playback of the current mood. */
    fun play() {
        if (running) {
            playing = true
            synchronized(gate) { gate.notifyAll() }
            return
        }
        running = true
        playing = true
        renderThread = thread(name = "ToneAudioEngine", isDaemon = true) { renderLoop() }
    }

    /** Pause playback. The synth fades to silence and the thread parks. */
    fun pause() {
        playing = false
    }

    /** Switch the harmonic/timbral character. Takes effect on the next bar. */
    fun setMood(mood: Mood) {
        spec = SPECS.getValue(mood)
    }

    /** Set output level (0f‒1f). Smoothed to avoid zipper noise. */
    fun setVolume(volume: Float) {
        targetVolume = (volume.coerceIn(0f, 1f).toDouble()) * 0.85
    }

    /** Tear everything down. Call from [androidx.lifecycle.ViewModel.onCleared]. */
    fun release() {
        running = false
        playing = false
        synchronized(gate) { gate.notifyAll() }
        renderThread?.join(500)
        renderThread = null
        audioTrack?.let {
            runCatching { it.stop() }
            runCatching { it.release() }
        }
        audioTrack = null
    }

    // ── Render loop ────────────────────────────────────────────────────────────────

    @Suppress("DEPRECATION")
    private fun renderLoop() {
        val minBuf = AudioTrack.getMinBufferSize(SR, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT)
        val bufBytes = maxOf(minBuf, FRAMES * 2 * 2 * 2)
        val track = AudioTrack(
            AudioManager.STREAM_MUSIC,
            SR,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufBytes,
            AudioTrack.MODE_STREAM,
        )
        audioTrack = track
        track.play()

        val out = ShortArray(FRAMES * 2)
        try {
            while (running) {
                // Park the thread while paused and already silent → ~0 idle CPU.
                if (!playing && curVol <= 0.0008) {
                    synchronized(gate) { if (!playing && running) gate.wait(250) }
                    if (!running) break
                    continue
                }
                renderChunk(out)
                var written = 0
                while (written < out.size && running) {
                    val r = track.write(out, written, out.size - written)
                    if (r <= 0) break
                    written += r
                }
            }
        } catch (_: Throwable) {
            // Never let an audio glitch crash the app.
        }
    }

    // ── DSP ─────────────────────────────────────────────────────────────────────

    private val voices = ArrayList<Voice>(48)
    private var sampleClock = 0L
    private var step = 0
    private var nextStepSample = 0L
    private var chord = intArrayOf(0, 4, 7, 11)
    private var curVol = 0.0

    // Stereo feedback delay
    private val delayLen = (SR * 0.33).toInt()
    private val delayL = DoubleArray(delayLen)
    private val delayR = DoubleArray(delayLen)
    private var delayIdx = 0

    // One-pole low-pass (per channel) for the master "tone"
    private var lpL = 0.0
    private var lpR = 0.0

    private fun renderChunk(out: ShortArray) {
        val s = spec
        val stepLen = SR * 60.0 / s.tempo / 4.0   // sixteenth-note length in samples
        val lpA = 1.0 - exp(-2.0 * Math.PI * s.cutoff / SR)
        val volTarget = if (playing) targetVolume else 0.0

        var i = 0
        while (i < FRAMES) {
            // Sequencer
            if (sampleClock >= nextStepSample) {
                onStep(step, s)
                step++
                nextStepSample += stepLen.toLong().coerceAtLeast(1)
            }

            var send = 0.0
            var left = 0.0
            var right = 0.0

            var v = 0
            while (v < voices.size) {
                val voice = voices[v]
                val m = voice.render(sampleClock)
                left += m * voice.panL
                right += m * voice.panR
                send += m * voice.send
                v++
            }

            // Feedback delay (ping-pong)
            val dl = delayL[delayIdx]
            val dr = delayR[delayIdx]
            left += dl * 0.6
            right += dr * 0.6
            delayL[delayIdx] = send * 0.5 + dr * 0.34
            delayR[delayIdx] = send * 0.5 + dl * 0.34
            delayIdx++
            if (delayIdx >= delayLen) delayIdx = 0

            // Master tone (low-pass) + soft clip + smoothed volume
            lpL += lpA * (left - lpL)
            lpR += lpA * (right - lpR)
            curVol += (volTarget - curVol) * 0.0009

            val oL = tanh(lpL * 1.1) * curVol
            val oR = tanh(lpR * 1.1) * curVol
            out[i * 2] = (oL * 32767.0).toInt().coerceIn(-32768, 32767).toShort()
            out[i * 2 + 1] = (oR * 32767.0).toInt().coerceIn(-32768, 32767).toShort()

            sampleClock++
            i++
        }

        // Reap finished voices once per chunk.
        if (voices.isNotEmpty()) voices.removeAll { !it.alive }
    }

    private fun onStep(step: Int, s: MoodSpec) {
        val bar = step / 16
        val s16 = step % 16
        if (s16 == 0) {
            chord = s.prog[bar % s.prog.size]
            // Pad chord (slow, lush, sent to delay) + sub-bass on the downbeat.
            for (iv in chord) {
                voices.add(
                    Voice(
                        kind = OSC, wave = s.padWave, freq = midi(s.base + iv),
                        startN = sampleClock, durN = (SR * (60.0 / s.tempo) * 3.6).toLong(),
                        attackN = SR * 0.6, gain = if (s.padWave == SAW) 0.030 else 0.046,
                        panL = 0.85, panR = 0.85, detune = 1.004, send = 0.6,
                    ),
                )
            }
            voices.add(
                Voice(
                    kind = OSC, wave = SINE, freq = midi(s.base - 12),
                    startN = sampleClock, durN = (SR * 1.6).toLong(),
                    attackN = SR * 0.01, gain = 0.13, panL = 1.0, panR = 1.0, detune = 0.0, send = 0.05,
                ),
            )
        }
        if (step % s.arpEvery == 0) {
            val idx = step / s.arpEvery
            val note = chord[idx % chord.size] + if ((idx / chord.size) % 2 == 1) 12 else 0
            val pan = if (idx % 2 == 0) 0.55 else 1.0
            voices.add(
                Voice(
                    kind = OSC, wave = s.arpWave, freq = midi(s.base + note + 12),
                    startN = sampleClock, durN = (SR * 0.55).toLong(),
                    attackN = SR * 0.004, gain = 0.06,
                    panL = pan, panR = 1.55 - pan, detune = 0.0, send = 0.5,
                ),
            )
        }
        if (s.beat) {
            if (s16 == 0 || s16 == 8) addKick()
            if (s16 % 4 == 2) addHat()
        }
    }

    private fun addKick() {
        voices.add(
            Voice(
                kind = KICK, wave = SINE, freq = 0.0, startN = sampleClock,
                durN = (SR * 0.22).toLong(), attackN = 0.0, gain = 0.17,
                panL = 1.0, panR = 1.0, detune = 0.0, send = 0.0,
            ),
        )
    }

    private fun addHat() {
        voices.add(
            Voice(
                kind = NOISE, wave = 0, freq = 0.0, startN = sampleClock,
                durN = (SR * 0.05).toLong(), attackN = 0.0, gain = 0.05,
                panL = 0.8, panR = 1.2, detune = 0.0, send = 0.1,
            ),
        )
    }

    // ── A single synth voice ──────────────────────────────────────────────────────

    private class Voice(
        val kind: Int,
        val wave: Int,
        var freq: Double,
        val startN: Long,
        val durN: Long,
        val attackN: Double,
        val gain: Double,
        val panL: Double,
        val panR: Double,
        val detune: Double,
        val send: Double,
    ) {
        private var phase = 0.0
        private var phase2 = 0.0
        private var prevNoise = 0.0
        var alive = true

        fun render(n: Long): Double {
            val t = (n - startN).toDouble()
            if (t < 0) return 0.0
            if (t >= durN) { alive = false; return 0.0 }

            val env: Double = when (kind) {
                KICK -> exp(-t / (SR * 0.16))
                NOISE -> exp(-t / (SR * 0.018))
                else -> {
                    val atk = if (attackN > 0) (t / attackN).coerceAtMost(1.0) else 1.0
                    val p = t / durN
                    atk * exp(-3.5 * p)
                }
            }

            val raw: Double = when (kind) {
                KICK -> {
                    val f = 42.0 + 120.0 * exp(-t / (SR * 0.022))
                    phase += f / SR
                    sine(phase)
                }
                NOISE -> {
                    val x = Math.random() * 2.0 - 1.0
                    val hp = x - prevNoise
                    prevNoise = x
                    hp
                }
                else -> {
                    var sig = osc(wave, phase)
                    phase += freq / SR
                    if (detune > 0.0) {
                        sig = (sig + osc(wave, phase2)) * 0.5
                        phase2 += freq * detune / SR
                    }
                    sig
                }
            }
            return raw * env * gain
        }
    }

    // ── Mood definitions ───────────────────────────────────────────────────────────

    private class MoodSpec(
        val tempo: Double,
        val base: Int,
        val padWave: Int,
        val arpWave: Int,
        val arpEvery: Int,
        val beat: Boolean,
        val cutoff: Double,
        val prog: Array<IntArray>,
    )

    companion object {
        private const val SR = 44_100
        private const val FRAMES = 1024

        private const val OSC = 0
        private const val KICK = 1
        private const val NOISE = 2

        private const val SINE = 0
        private const val TRI = 1
        private const val SAW = 2

        private const val TABLE = 2048
        private const val MASK = TABLE - 1
        private val SINE_TABLE = DoubleArray(TABLE) { kotlin.math.sin(2.0 * Math.PI * it / TABLE) }

        private fun midi(m: Int): Double = 440.0 * Math.pow(2.0, (m - 69) / 12.0)

        private fun sine(phase: Double): Double {
            val frac = phase - floor(phase)
            return SINE_TABLE[(frac * TABLE).toInt() and MASK]
        }

        private fun osc(wave: Int, phase: Double): Double {
            val p = phase - floor(phase)
            return when (wave) {
                TRI -> 4.0 * kotlin.math.abs(p - 0.5) - 1.0
                SAW -> 2.0 * p - 1.0
                else -> SINE_TABLE[(p * TABLE).toInt() and MASK]
            }
        }

        private val SPECS: Map<Mood, MoodSpec> = mapOf(
            Mood.CHILL to MoodSpec(68.0, 60, SINE, SINE, 4, false, 2600.0, arrayOf(
                intArrayOf(0, 4, 7, 11), intArrayOf(5, 9, 12, 16), intArrayOf(7, 11, 14, 17), intArrayOf(2, 5, 9, 12))),
            Mood.DREAMY to MoodSpec(76.0, 60, TRI, SINE, 2, false, 2400.0, arrayOf(
                intArrayOf(0, 3, 7, 10), intArrayOf(8, 12, 15, 19), intArrayOf(5, 8, 12, 15), intArrayOf(7, 10, 14, 17))),
            Mood.BRIGHT to MoodSpec(112.0, 62, TRI, TRI, 2, true, 4200.0, arrayOf(
                intArrayOf(0, 4, 7), intArrayOf(7, 11, 14), intArrayOf(9, 12, 16), intArrayOf(5, 9, 12))),
            Mood.WARM to MoodSpec(86.0, 57, SINE, TRI, 2, false, 3000.0, arrayOf(
                intArrayOf(0, 4, 7, 11), intArrayOf(2, 5, 9, 12), intArrayOf(4, 7, 11, 14), intArrayOf(5, 9, 12, 16))),
            Mood.NIGHT to MoodSpec(98.0, 55, SAW, TRI, 2, true, 1300.0, arrayOf(
                intArrayOf(0, 3, 7), intArrayOf(10, 14, 17), intArrayOf(8, 12, 15), intArrayOf(7, 10, 14))),
            Mood.FOCUS to MoodSpec(80.0, 59, SINE, SINE, 4, false, 2200.0, arrayOf(
                intArrayOf(0, 5, 7), intArrayOf(2, 7, 9), intArrayOf(0, 4, 7), intArrayOf(5, 9, 12))),
            Mood.SUNRISE to MoodSpec(104.0, 60, TRI, TRI, 2, true, 4000.0, arrayOf(
                intArrayOf(0, 4, 7, 9), intArrayOf(5, 9, 12, 14), intArrayOf(7, 11, 14, 16), intArrayOf(9, 12, 16, 19))),
        )
    }
}
