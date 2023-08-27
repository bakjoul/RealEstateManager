package com.bakjoul.realestatemanager.ui.details.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.ActivityDetailsBinding
import com.bakjoul.realestatemanager.ui.details.DetailsFragment
import com.bakjoul.realestatemanager.ui.photos.PhotosFragment
import com.bakjoul.realestatemanager.ui.utils.Event.Companion.observeEvent
import com.bakjoul.realestatemanager.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailsActivity : AppCompatActivity() {

    private companion object {
        private const val PHOTOS_DIALOG_TAG = "PhotosDialogFragment"
    }

    private val binding by viewBinding { ActivityDetailsBinding.inflate(it) }
    private val viewModel by viewModels<DetailsActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (resources.getBoolean(R.bool.isTablet)) {
            finish()
        }

        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.detailsFrameLayoutContainer.id, DetailsFragment())
                .commitNow()
        }

        viewModel.detailsActivityViewActionLiveData.observeEvent(this) {
            Log.d("test", "details activity observed event: $it")
            when (it) {
                DetailsActivityViewAction.ShowPhotosDialog -> {
                    val existingDialog = supportFragmentManager.findFragmentByTag(PHOTOS_DIALOG_TAG) as? PhotosFragment
                    if (existingDialog == null) {
                        val dialog = PhotosFragment()
                        dialog.show(supportFragmentManager, PHOTOS_DIALOG_TAG)
                    }
                }

                DetailsActivityViewAction.CloseActivity -> {
                    finish()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.onResume(resources.getBoolean(R.bool.isTablet))
    }
}
