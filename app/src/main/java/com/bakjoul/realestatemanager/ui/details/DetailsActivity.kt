package com.bakjoul.realestatemanager.ui.details

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.ActivityDetailsBinding
import com.bakjoul.realestatemanager.ui.photos.PhotosFragment
import com.bakjoul.realestatemanager.ui.utils.Event.Companion.observeEvent
import com.bakjoul.realestatemanager.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailsActivity : AppCompatActivity() {

    private val binding by viewBinding { ActivityDetailsBinding.inflate(it) }
    private val viewModel by viewModels<DetailsViewModel>()

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

        viewModel.detailsViewActionLiveData.observeEvent(this) {
            when (it) {
                DetailsViewAction.DisplayPhotosDialog -> {
                    Log.d("test", "details activity: dialog emit")
                    val existingDialog = supportFragmentManager.findFragmentByTag("PhotosDialogFragment") as? PhotosFragment
                    if (existingDialog == null) {
                        val dialog = PhotosFragment()
                        dialog.show(supportFragmentManager, "PhotosDialogFragment")
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.onResume(resources.getBoolean(R.bool.isTablet))
    }
}
