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

import android.os.Bundle
import android.view.KeyEvent
import com.theta360.pluginlibrary.activity.PluginActivity
import com.theta360.pluginlibrary.callback.KeyCallback
import com.theta360.pluginlibrary.receiver.KeyReceiver
import org.theta4j.webapi.Options.EXPOSURE_COMPENSATION
import org.theta4j.webapi.Options.EXPOSURE_COMPENSATION_SUPPORT
import org.theta4j.webapi.Theta
import java.math.BigDecimal
import java.util.concurrent.Executors

class MainActivity : PluginActivity() {
    private val executor = Executors.newSingleThreadExecutor()

    private val theta = Theta.createForPlugin()

    private var aquesTalk: AquesTalkManager? = null

    private val keyCallback = object : KeyCallback {
        override fun onKeyDown(keyCode: Int, event: KeyEvent) {
            // ignore
        }

        override fun onKeyLongPress(keyCode: Int, event: KeyEvent) {
            if (keyCode == KeyReceiver.KEYCODE_CAMERA) {
                executor.submit { currentExposureCompensation() }
            }
        }

        override fun onKeyUp(keyCode: Int, event: KeyEvent) {
            if (keyCode == KeyReceiver.KEYCODE_CAMERA && !event.isCanceled) {
                executor.submit { theta.takePicture() }
            } else if (keyCode == KeyReceiver.KEYCODE_WLAN_ON_OFF) {
                executor.submit { incrementExposureCompensation() }
            } else if (keyCode == KeyReceiver.KEYCODE_MEDIA_RECORD && !event.isTracking) {
                executor.submit { decrementExposureCompensation() }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setAutoClose(true);

        aquesTalk = AquesTalkManager(applicationContext)

        executor.submit {
            aquesTalk!!.initialize()
        }
    }

    override fun onResume() {
        super.onResume()
        setKeyCallback(keyCallback)
    }

    override fun onPause() {
        super.onPause()
        setKeyCallback(null)
    }

    private fun currentExposureCompensation() {
        val bias = theta.getOption(EXPOSURE_COMPENSATION)
        val koe = "露出補正 ${decimalToKoe(bias.value)}"
        aquesTalk!!.speech(koe)
    }

    private fun incrementExposureCompensation() {
        val optionSet = theta.getOptions(EXPOSURE_COMPENSATION, EXPOSURE_COMPENSATION_SUPPORT)
        val bias = optionSet.get(EXPOSURE_COMPENSATION)!!
        val support = optionSet.get(EXPOSURE_COMPENSATION_SUPPORT)!!

        val index = support.indexOf(bias) + 1
        if (support.size - 1 < index) {
            aquesTalk!!.speech("最大値です")
            return
        }

        val newBias = support.get(index)
        theta.setOption(EXPOSURE_COMPENSATION, newBias)

        val koe = decimalToKoe(newBias.value)
        aquesTalk!!.speech(koe)
    }

    private fun decrementExposureCompensation() {
        val optionSet = theta.getOptions(EXPOSURE_COMPENSATION, EXPOSURE_COMPENSATION_SUPPORT)
        val bias = optionSet.get(EXPOSURE_COMPENSATION)!!
        val support = optionSet.get(EXPOSURE_COMPENSATION_SUPPORT)!!

        val index = support.indexOf(bias) - 1
        if (index < 0) {
            aquesTalk!!.speech("最小値です")
            return
        }

        val newBias = support.get(index)
        theta.setOption(EXPOSURE_COMPENSATION, newBias)

        val koe = decimalToKoe(newBias.value)
        aquesTalk!!.speech(koe)
    }

    private fun decimalToKoe(value: BigDecimal): String {
        return when (value.signum()) {
            0 -> "0"
            1 -> "プラス${value.toPlainString()}"
            -1 -> "マイナス${value.toPlainString()}"
            else -> throw AssertionError()
        }
    }
}
