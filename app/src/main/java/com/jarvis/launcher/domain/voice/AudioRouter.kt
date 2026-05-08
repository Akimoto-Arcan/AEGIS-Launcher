package com.jarvis.launcher.domain.voice

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioRouter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun isBluetoothHeadsetConnected(): Boolean {
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        return devices.any {
            it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
            it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
            it.type == AudioDeviceInfo.TYPE_BLE_HEADSET
        }
    }

    fun startBluetoothSco() {
        if (isBluetoothHeadsetConnected()) {
            try {
                audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
                audioManager.isBluetoothScoOn = true
                audioManager.startBluetoothSco()
            } catch (_: Exception) {}
        }
    }

    fun stopBluetoothSco() {
        try {
            audioManager.isBluetoothScoOn = false
            audioManager.stopBluetoothSco()
            audioManager.mode = AudioManager.MODE_NORMAL
        } catch (_: Exception) {}
    }
}
