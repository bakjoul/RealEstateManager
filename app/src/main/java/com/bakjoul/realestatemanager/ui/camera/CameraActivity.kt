package com.bakjoul.realestatemanager.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.ActivityCameraBinding
import com.bakjoul.realestatemanager.ui.utils.viewBinding

class CameraActivity : AppCompatActivity() {

    private val binding by viewBinding { ActivityCameraBinding.inflate(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            finish()
        } else {
            startCamera()
        }
    }

    private fun startCamera() {
        val processCameraProvider = ProcessCameraProvider.getInstance(this)
        processCameraProvider.addListener({
            try {
                val cameraProvider = processCameraProvider.get()
                val previewUseCase = Preview.Builder().build().also { it.setSurfaceProvider(binding.cameraPreviewView.surfaceProvider) }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, previewUseCase)
            } catch (e: Exception) {
                Toast.makeText(this, getString(R.string.camera_error_starting), Toast.LENGTH_LONG).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }
}