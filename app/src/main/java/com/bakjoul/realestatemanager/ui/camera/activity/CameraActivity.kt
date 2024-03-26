package com.bakjoul.realestatemanager.ui.camera.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bakjoul.realestatemanager.databinding.ActivityCameraBinding
import com.bakjoul.realestatemanager.ui.camera.CameraFragment
import com.bakjoul.realestatemanager.ui.photo_preview.PhotoPreviewFragment
import com.bakjoul.realestatemanager.ui.utils.Event.Companion.observeEvent
import com.bakjoul.realestatemanager.ui.utils.hideKeyboard
import com.bakjoul.realestatemanager.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CameraActivity : AppCompatActivity() {

    companion object {
        const val PHOTO_PREVIEW_DIALOG_TAG = "PhotoPreviewFragment"

        fun navigate(
            context: Context,
            propertyId: Long,
            isExistingProperty: Boolean
        ) : Intent {
            return Intent(context, CameraActivity::class.java).apply {
                putExtra("propertyId", propertyId)
                putExtra("isExistingProperty", isExistingProperty)
            }
        }
    }

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

        viewModel.viewActionLiveData.observeEvent(this) { viewAction ->
            when (viewAction) {
                is CameraActivityViewAction.ShowPhotoPreview -> {
                    val photoPreviewFragment = PhotoPreviewFragment().apply {
                        arguments = Bundle().apply {
                            putLong("propertyId", viewAction.propertyId)
                            putBoolean("isExistingProperty", viewAction.isExistingProperty)
                        }
                    }

                    supportFragmentManager.beginTransaction()
                        .add(binding.cameraFrameLayoutContainer.id, photoPreviewFragment, PHOTO_PREVIEW_DIALOG_TAG)
                        .addToBackStack(null)
                        .commit()
                }
                CameraActivityViewAction.ClosePhotoPreview -> {
                    supportFragmentManager.findFragmentByTag(PHOTO_PREVIEW_DIALOG_TAG)?.let {
                        supportFragmentManager
                            .beginTransaction()
                            .remove(it)
                            .commit()
                    }
                }
                CameraActivityViewAction.CloseCamera -> finish()
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            hideKeyboard()
            currentFocus!!.clearFocus()
        }
        return super.dispatchTouchEvent(ev)
    }
}
