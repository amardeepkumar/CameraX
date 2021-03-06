package com.camerax

import android.app.Application
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraX
import androidx.camera.core.CameraXConfig

class MyCameraXApplication: Application(), CameraXConfig.Provider  {
    override fun getCameraXConfig(): CameraXConfig {

        return Camera2Config.defaultConfig()
    }
}