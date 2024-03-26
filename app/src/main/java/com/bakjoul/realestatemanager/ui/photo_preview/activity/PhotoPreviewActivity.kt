package com.bakjoul.realestatemanager.ui.photo_preview.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bakjoul.realestatemanager.databinding.ActivityPhotoPreviewBinding
import com.bakjoul.realestatemanager.ui.photo_preview.PhotoPreviewFragment
import com.bakjoul.realestatemanager.ui.utils.Event.Companion.observeEvent
import com.bakjoul.realestatemanager.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PhotoPreviewActivity : AppCompatActivity() {

    companion object {
        fun navigate(
            context: Context,
            propertyId: Long,
            isExistingProperty: Boolean
        ): Intent {
            return Intent(context, PhotoPreviewActivity::class.java).apply {
                putExtra("propertyId", propertyId)
                putExtra("isExistingProperty", isExistingProperty)
            }
        }
    }

    private val binding by viewBinding { ActivityPhotoPreviewBinding.inflate(it) }
    private val viewModel by viewModels<PhotoPreviewActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            val propertyId = intent.getLongExtra("propertyId", -1)
            if (propertyId != -1L) {
                val photoPreviewFragment = PhotoPreviewFragment()
                val args = Bundle().apply {
                    putLong("propertyId", propertyId)
                    putBoolean("isExistingProperty", intent.getBooleanExtra("isExistingProperty", false))
                }
                photoPreviewFragment.arguments = args

                supportFragmentManager.beginTransaction()
                    .replace(binding.photoPreviewFrameLayoutContainer.id, photoPreviewFragment)
                    .commitNow()
            } else {
                finish()
            }
        }

        viewModel.viewActionLiveData.observeEvent(this) {
            when (it) {
                is PhotoPreviewActivityViewAction.ClosePhotoPreview -> finish()
            }
        }
    }
}
