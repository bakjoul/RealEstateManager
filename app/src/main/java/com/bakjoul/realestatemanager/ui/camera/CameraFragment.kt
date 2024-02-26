package com.bakjoul.realestatemanager.ui.camera

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.FragmentCameraBinding
import com.bakjoul.realestatemanager.ui.utils.IdGenerator
import com.bakjoul.realestatemanager.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class CameraFragment : Fragment(R.layout.fragment_camera) {

    private companion object {
        private const val TAG = "CameraFragment"
    }

    private val binding by viewBinding { FragmentCameraBinding.bind(it) }
    private val viewModel by viewModels<CameraViewModel>()

    private val filenameFormatter by lazy {
        DateTimeFormatter.ofPattern(getString(R.string.photo_filename_format))
    }

    private var imageCapture: ImageCapture? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startCamera()

        binding.cameraShutterButton.setOnClickListener { takePhoto() }
        binding.cameraCloseButton.setOnClickListener { viewModel.onCloseButtonClicked() }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener(
            {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                val preview = Preview.Builder().build().apply {
                    setSurfaceProvider(binding.cameraViewFinder.surfaceProvider)
                }

                imageCapture = ImageCapture.Builder().build()

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), getString(R.string.camera_error_starting), Toast.LENGTH_LONG).show()
                }
            },
            Dispatchers.Main.asExecutor()
        )
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val fileName = filenameFormatter.format(LocalDateTime.now())
        val fileNameSuffix = "_${IdGenerator.generateShortUuid()}.jpg"
        val formattedFileName = "IMG_${fileName}${fileNameSuffix}"
        val cacheFile = File(requireContext().cacheDir, formattedFileName)

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(cacheFile)
            .build()

        imageCapture.takePicture(
            outputOptions,
            Dispatchers.Main.asExecutor(),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    val message = "Error taking photo: ${exception.message}"
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                    Log.e(TAG, message, exception)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    viewModel.onImageSaved(output.savedUri?.path, requireActivity().intent.getLongExtra("propertyId", -1))
                }
            })
    }
}
