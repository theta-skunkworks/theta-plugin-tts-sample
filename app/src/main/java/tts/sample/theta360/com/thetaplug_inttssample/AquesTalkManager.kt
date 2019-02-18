/*
 * Copyright 2019 Ricoh Company, Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tts.sample.theta360.com.thetaplug_inttssample

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import aqkanji2koe.AqKanji2Koe
import aquestalk.AquesTalk
import java.io.File

class AquesTalkManager(
    private val ctx: Context
) {
    private var mediaPlayer: MediaPlayer? = null

    @Synchronized
    fun initialize() {
        copyAsset("aqdic.bin")
    }

    @Synchronized
    fun speech(text: String) {
        val koe = kanji2Koe(text)
        val wavFile = synthesize(koe)
        try {
            play(wavFile)
        } finally {
            wavFile.delete()
        }
    }

    private fun copyAsset(filename: String) {
        if (!ctx.assets.list("")!!.contains(filename)) {
            throw AssertionError("missing $filename in assets.")
        }

        val dstFile = File("${ctx.filesDir.absolutePath}/$filename")
        if (dstFile.exists()) {
            return
        }

        dstFile.outputStream().use { dst ->
            val src = ctx.assets.open(filename)
            src.copyTo(dst)
        }
    }

    private fun kanji2Koe(kanji: String): String {
        return AqKanji2Koe.convert(ctx.filesDir.toString(), kanji)
    }

    private fun synthesize(koe: String): File {
        val aquestalk = AquesTalk(AquesTalk.PRESET.F1).apply {
            spd = 90
        }

        val wav = aquestalk.synthe(koe)

        if (wav.size == 1) { // error is occurred if result size is 1.
            val errorCode = wav[0] // first byte is error code if error is occurred.
            throw RuntimeException("failed to synthesize voice : $errorCode")
        }

        val outputFile = File.createTempFile("koe", ".wav", ctx.cacheDir)

        outputFile.outputStream().use {
            it.write(wav)
        }

        return outputFile
    }

    private fun play(file: File) {
        setVolumeMax()

        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .setLegacyStreamType(AudioManager.STREAM_ALARM)
            .build()

        resetMediaPlayer()
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(attributes)
            setDataSource(file.path)
            setOnCompletionListener { resetMediaPlayer() }
            prepare()
            start()
        }
    }

    @Synchronized
    private fun resetMediaPlayer() {
        mediaPlayer?.run {
            stop()
            reset()
            release()
        }
        mediaPlayer = null
    }

    private fun setVolumeMax() {
        val am = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVol = am.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        am.setStreamVolume(AudioManager.STREAM_ALARM, maxVol, 0)
    }
}
