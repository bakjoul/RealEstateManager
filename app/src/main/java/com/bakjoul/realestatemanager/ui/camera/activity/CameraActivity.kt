package com.bakjoul.realestatemanager.ui.camera.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bakjoul.realestatemanager.databinding.ActivityCameraBinding
import com.bakjoul.realestatemanager.ui.camera.CameraFragment
import com.bakjoul.realestatemanager.ui.camera.photo_preview.PhotoPreviewFragment
import com.bakjoul.realestatemanager.ui.utils.Event.Companion.observeEvent
import com.bakjoul.realestatemanager.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CameraActivity : AppCompatActivity() {

    private val binding by viewBinding { ActivityCameraBinding.inflate(it) }
    private val viewModel by viewModels<CameraActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            finish()
            return
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.cameraFrameLayoutContainer.id, CameraFragment())
                .commitNow()
        }

        viewModel.viewActionLiveData.observeEvent(this) {
            when (it) {
                CameraActivityViewAction.ShowPhotoPreview -> {
                    supportFragmentManager.beginTransaction()
                        .replace(binding.cameraFrameLayoutContainer.id, PhotoPreviewFragment())
                        .addToBackStack(null)
                        .commit()
                }
                CameraActivityViewAction.ClosePhotoPreview -> supportFragmentManager.popBackStack()
                CameraActivityViewAction.CloseCamera -> finish()
            }
        }
    }
}
