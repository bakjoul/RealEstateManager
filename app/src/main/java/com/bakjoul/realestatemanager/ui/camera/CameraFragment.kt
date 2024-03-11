package com.bakjoul.realestatemanager.ui.camera

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.OrientationEventListener
import android.view.ScaleGestureDetector
import android.view.Surface
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.animation.doOnEnd
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.FragmentCameraBinding
import com.bakjoul.realestatemanager.ui.utils.DensityUtil
import com.bakjoul.realestatemanager.ui.utils.IdGenerator
import com.bakjoul.realestatemanager.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@AndroidEntryPoint
class CameraFragment : Fragment(R.layout.fragment_camera) {

    private companion object {
        private const val TAG = "CameraFragment"
        private const val NO_FLASH = -1
    }

    private val binding by viewBinding { FragmentCameraBinding.bind(it) }
    private val viewModel by viewModels<CameraViewModel>()

    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var preview: Preview? = null
    private var displayId: Int = -1
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var imageCapture: ImageCapture? = null

    // Handles captured image orientation
    private val displayManager by lazy {
        requireContext().getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    }
    private val displayListener by lazy {
        object : DisplayManager.DisplayListener {
            override fun onDisplayAdded(id: Int) = Unit
            override fun onDisplayRemoved(id: Int) = Unit
            override fun onDisplayChanged(id: Int) {
                if (id == displayId) {
                    imageCapture?.targetRotation = binding.cameraPreviewView.display.rotation
                }
            }
        }
    }

    // Handles buttons orientation on device rotation
    private var previousOrientation: Int = 0
    private val orientationEventListener by lazy {
        object : OrientationEventListener(requireContext()) {
            override fun onOrientationChanged(orientation: Int) {
                val deviceOrientation = when (orientation) {
                    in 45..134 -> Surface.ROTATION_270
                    in 135..224 -> Surface.ROTATION_180
                    in 225..314 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }
                imageCapture?.targetRotation = deviceOrientation

                if (deviceOrientation != previousOrientation) {
                    val rotation = calculateRotation(deviceOrientation)
                    startButtonsRotationAnimation(
                        listOf(
                            binding.cameraFlashToggleButton,
                            binding.cameraCloseButton,
                            binding.cameraSwitchLensButton
                        ),
                        rotation
                    )

                    if (deviceOrientation != Surface.ROTATION_180) {
                        previousOrientation = deviceOrientation
                    }
                }
            }
        }
    }
    private var buttonsAnimatorSet: AnimatorSet? = null

    // Flash mode
    private var flashMode: Int = ImageCapture.FLASH_MODE_OFF
    private val fadeOut by lazy {
        ObjectAnimator.ofFloat(binding.cameraFlashToggleButton, "alpha", 1f, 0f).apply { duration = 200 }
    }
    private val fadeIn by lazy {
        ObjectAnimator.ofFloat(binding.cameraFlashToggleButton, "alpha", 0f, 1f).apply { duration = 200 }
    }
    private val onFlashModeChangeListener by lazy {
        object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                when (flashMode) {
                    ImageCapture.FLASH_MODE_OFF -> binding.cameraFlashToggleButton.setImageResource(R.drawable.baseline_flash_off_24)
                    ImageCapture.FLASH_MODE_ON -> binding.cameraFlashToggleButton.setImageResource(R.drawable.baseline_flash_on_24)
                    ImageCapture.FLASH_MODE_AUTO -> binding.cameraFlashToggleButton.setImageResource(R.drawable.baseline_flash_auto_24)
                }
            }
        }
    }

    // Shutter animation
    private val shutterAnimator: ValueAnimator by lazy {
        ValueAnimator.ofInt(
            DensityUtil.dip2px(requireContext(), 60f),
            DensityUtil.dip2px(requireContext(), 54f),
            DensityUtil.dip2px(requireContext(), 60f)
        ).apply {
            addUpdateListener {
                val animatedValue = it.animatedValue as Int
                binding.cameraShutterButton.layoutParams.width = animatedValue
                binding.cameraShutterButton.layoutParams.height = animatedValue
                binding.cameraShutterButton.requestLayout()
            }
            duration = 700
            interpolator = OvershootInterpolator()
        }
    }

    // Focus ring
    private val focusRingSizeAnimator: ValueAnimator by lazy {
        ValueAnimator.ofInt(
            DensityUtil.dip2px(requireContext(), 112f),
            DensityUtil.dip2px(requireContext(), 64f),
            DensityUtil.dip2px(requireContext(), 72f)
        ).apply {
            addUpdateListener {
                val animatedValue = it.animatedValue as Int
                binding.cameraFocusRing.layoutParams.width = animatedValue
                binding.cameraFocusRing.layoutParams.height = animatedValue
                binding.cameraFocusRing.requestLayout()
            }
            duration = 450
            interpolator = AccelerateDecelerateInterpolator()
        }
    }
    private val focusRingAlphaAnimation by lazy {
        ObjectAnimator.ofFloat(binding.cameraFocusRing, View.ALPHA, 1f, 0f).apply {
            duration = 200
            startDelay = 3000
        }
    }
    private var focusRingAnimatorSet: AnimatorSet? = null

    private val scaleGestureListener by lazy {
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val currentZoomRatio = camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 0f
                val zoomSpeed = 0.02f
                val zoomRatioDelta = if ( detector.scaleFactor > 1.0f) {    // Zoom
                    currentZoomRatio * zoomSpeed
                } else {    // Dézoom
                    abs(currentZoomRatio) * zoomSpeed * -1
                }
                val newZoomRatio = (currentZoomRatio + zoomRatioDelta).coerceIn(1f, 10f)

                Log.d("test", "onScale: $newZoomRatio")

                camera?.cameraControl?.setZoomRatio(newZoomRatio)

                return true
            }
        }
    }
    private val scaleGestureDetector by lazy {
        ScaleGestureDetector(requireContext(), scaleGestureListener)
    }

    private var touchCount = 0
    private val touchSequence = mutableListOf<Int>()
    private var lastUpTime = 0L
    private val tapThreshold = 250

    private val filenameFormatter by lazy {
        DateTimeFormatter.ofPattern(getString(R.string.photo_filename_format))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cameraShutterButton.setOnClickListener {
            shutterAnimator.cancel()
            shutterAnimator.start()
            takePhoto()
        }
        binding.cameraFlashToggleButton.setOnClickListener { toggleFlash() }
        binding.cameraCloseButton.setOnClickListener { viewModel.onCloseButtonClicked() }
        binding.cameraSwitchLensButton.setOnClickListener { switchLens() }

        displayManager.registerDisplayListener(displayListener, null)
        binding.cameraPreviewView.post {
            displayId = binding.cameraPreviewView.display.displayId
            setCamera()
            setTapToFocusAndPinchToZoom()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        displayManager.unregisterDisplayListener(displayListener)
        orientationEventListener.disable()
    }

    private fun hasBackCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
    }

    private fun hasFrontCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false
    }

    private fun hasFlashMode(): Boolean {
        return requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    }

    private fun setCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener(
            {
                // CameraProvider
                cameraProvider = cameraProviderFuture.get()

                // Selects lens depending on available cameras
                lensFacing = when {
                    hasBackCamera() -> CameraSelector.LENS_FACING_BACK
                    hasFrontCamera() -> CameraSelector.LENS_FACING_FRONT
                    else -> throw IllegalStateException("Back and front camera are unavailable")
                }
                if (lensFacing == CameraSelector.LENS_FACING_BACK && !hasFrontCamera()) {
                    binding.cameraSwitchLensButton.visibility = View.GONE
                }

                flashMode = when {
                    hasFlashMode() -> ImageCapture.FLASH_MODE_OFF
                    else -> NO_FLASH
                }

                bindCameraUseCases()
                orientationEventListener.enable()
            },
            Dispatchers.Main.asExecutor()
        )
    }

    @SuppressLint("RestrictedApi")
    private fun bindCameraUseCases() {
        val rotation = binding.cameraPreviewView.display.rotation

        // CameraProvider
        val camProvider = cameraProvider ?: throw IllegalStateException("Camera initialization failed")

        // CameraSelector
        val cameraSelector = CameraSelector.Builder().apply {
            requireLensFacing(lensFacing)
        }.build()

        // Preview
        preview = Preview.Builder().apply {
            setTargetRotation(rotation)
        }.build()

        imageCapture = ImageCapture.Builder().apply {
            setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            setTargetRotation(rotation)
            if (flashMode != NO_FLASH) {
                setFlashMode(flashMode)
            } else {
                binding.cameraFlashToggleButton.visibility = View.GONE
            }
        }.build()

        try {
            camProvider.unbindAll()
            camera = camProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            preview?.setSurfaceProvider(binding.cameraPreviewView.surfaceProvider)
        } catch (e: Exception) {
            Log.e(TAG, "Use cases binding failed", e)
            Toast.makeText(requireContext(), getString(R.string.camera_init_error), Toast.LENGTH_LONG).show()
        }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        // File name and path
        val fileName = filenameFormatter.format(LocalDateTime.now())
        val fileNameSuffix = "_${IdGenerator.generateShortUuid()}.jpg"
        val formattedFileName = "IMG_${fileName}${fileNameSuffix}"
        val cacheFile = File(requireContext().cacheDir, formattedFileName)

        // Image capture metadata
        val metadata = ImageCapture.Metadata().apply {
            isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
        }

        // Output options
        val outputOptions = ImageCapture.OutputFileOptions.Builder(cacheFile).apply {
            setMetadata(metadata)
        }.build()

        // Image capture listener
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
                    viewModel.onImageSaved(
                        output.savedUri?.path,
                        requireActivity().intent.getLongExtra("propertyId", -1)
                    )
                }
            })
    }

    private fun switchLens() {
        lensFacing = when (lensFacing) {
            CameraSelector.LENS_FACING_BACK -> CameraSelector.LENS_FACING_FRONT
            else -> CameraSelector.LENS_FACING_BACK
        }
        bindCameraUseCases()
    }

    private fun toggleFlash() {
        AnimatorSet().apply {
            fadeOut.addListener(onFlashModeChangeListener)
            play(fadeOut).before(fadeIn)
            start()
        }

        when (flashMode) {
            ImageCapture.FLASH_MODE_OFF -> {
                imageCapture?.flashMode = ImageCapture.FLASH_MODE_ON
                flashMode = ImageCapture.FLASH_MODE_ON
            }
            ImageCapture.FLASH_MODE_ON -> {
                imageCapture?.flashMode = ImageCapture.FLASH_MODE_AUTO
                flashMode = ImageCapture.FLASH_MODE_AUTO
            }
            ImageCapture.FLASH_MODE_AUTO -> {
                imageCapture?.flashMode = ImageCapture.FLASH_MODE_OFF
                flashMode = ImageCapture.FLASH_MODE_OFF
            }
        }
    }

    private fun calculateRotation(deviceOrientation: Int) : Float {
        if (deviceOrientation == Surface.ROTATION_180) {
            return -1f
        }

        return when (deviceOrientation) {
            Surface.ROTATION_0 -> when (previousOrientation) {
                Surface.ROTATION_90, Surface.ROTATION_270 -> 0f
                else -> -1f
            }

            Surface.ROTATION_90 -> when (previousOrientation) {
                Surface.ROTATION_0 -> 90f
                else -> -1f
            }

            Surface.ROTATION_270 -> when (previousOrientation) {
                Surface.ROTATION_0 -> -90f
                else -> -1f
            }

            else -> -1f
        }
    }

    private fun startButtonsRotationAnimation(views: List<View>, rotation: Float) {
        if (rotation == -1f) return

        val animators = mutableListOf<ValueAnimator>()
        for (view in views) {
            animators.add(ValueAnimator.ofFloat(view.rotation, rotation).apply {
                addUpdateListener {
                    view.rotation = it.animatedValue as Float
                }
                duration = 400
            })
        }

        buttonsAnimatorSet?.cancel()
        buttonsAnimatorSet = AnimatorSet().apply {
            playTogether(animators as Collection<Animator>?)
            startDelay = 100
            start()
        }
    }

    private fun animateFocusRing(x: Float, y: Float) {
        focusRingAnimatorSet?.cancel()

        val width = binding.cameraFocusRing.width
        val height = binding.cameraFocusRing.height
        binding.cameraFocusRing.x = x - width / 2
        binding.cameraFocusRing.y = y - height / 2

        binding.cameraFocusRing.visibility = View.VISIBLE
        binding.cameraFocusRing.alpha = 1f

        focusRingAnimatorSet = AnimatorSet().apply {
            playSequentially(focusRingSizeAnimator, focusRingAlphaAnimation)
            doOnEnd { binding.cameraFocusRing.alpha = 0f }
            start()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTapToFocusAndPinchToZoom() {
        binding.cameraPreviewView.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)

            when (event.actionMasked ) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    touchCount++
                    touchSequence.add(touchCount)
                    true
                }
                MotionEvent.ACTION_POINTER_UP -> {
                    touchCount--
                    true
                }
                MotionEvent.ACTION_UP -> {
                    touchCount--

                    val currentTime = System.currentTimeMillis()
                    if ((touchSequence.size > 1 && !touchSequence.contains(2)) ||
                        (touchSequence.size == 1 && currentTime - lastUpTime >= tapThreshold)
                    ) {
                        val location = IntArray(2)
                        binding.cameraPreviewView.getLocationInWindow(location)
                        val offsetX = location[0]
                        val offsetY = location[1]
                        val adjustedX = event.rawX - offsetX
                        val adjustedY = event.rawY - offsetY + binding.cameraPreviewView.marginTop

                        animateFocusRing(adjustedX, adjustedY)

                        val factory = binding.cameraPreviewView.meteringPointFactory
                        val point = factory.createPoint(adjustedX, adjustedY)
                        val action = FocusMeteringAction.Builder(point).build()
                        camera?.cameraControl?.startFocusAndMetering(action)

                        lastUpTime = currentTime
                        touchSequence.clear()
                        true
                    } else {
                        lastUpTime = 0
                        touchSequence.clear()
                        false
                    }

                }
                else -> false
            }
        }
    }
}
