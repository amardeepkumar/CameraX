package com.camerax

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity(), LifecycleOwner {

    private lateinit var imageCapture: ImageCapture
    private val CODE = 10
    private val PERMISSION = arrayOf(Manifest.permission.CAMERA)
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var previewView: PreviewView
    private lateinit var outputDirectory: File
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        outputDirectory = getOutputDirectory(this)

        checkAllPermission()
    }

    private fun checkAllPermission() {
        if (allPermissionGranted()) {
            bindCameraUseCases()
        } else {
            requestPermissions(PERMISSION, CODE)
        }
    }

    private fun allPermissionGranted() =
        PERMISSION.all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == CODE) {
            if (allPermissionGranted()) {
                bindCameraUseCases()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun bindCameraUseCases() {
        previewView = findViewById<PreviewView>(R.id.previewView)

        cameraButton.setOnClickListener {

            val photoFile = createFile(outputDirectory, FILENAME, PHOTO_EXTENSION)

            val outputOption = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            imageCapture.takePicture(outputOption, Executors.newSingleThreadExecutor(), object: ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = outputFileResults.savedUri ?: Uri.fromFile(photoFile)
                    Log.d(TAG, "Photo capture succeeded: $savedUri")
                }
            })
        }

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview: Preview = Preview.Builder()
            .build()


        imageCapture = ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        var cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        cameraProvider.unbindAll()
        var camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)


        preview.setSurfaceProvider(previewView.createSurfaceProvider(camera.cameraInfo))
    }


    companion object {

        private val TAG = CameraActivity::class.java.canonicalName
        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_EXTENSION = ".jpg"

        /** Helper function used to create a timestamped file */
        private fun createFile(baseFolder: File, format: String, extension: String) =
            File(baseFolder, SimpleDateFormat(format, Locale.US)
                .format(System.currentTimeMillis()) + extension)

        private fun getOutputDirectory(context: Context): File {
            val mediaDir = context.applicationContext.externalMediaDirs.firstOrNull()?.let { it.apply { mkdirs() } }
            return if (mediaDir != null && mediaDir.exists()) mediaDir else context.applicationContext.filesDir
        }
    }
}