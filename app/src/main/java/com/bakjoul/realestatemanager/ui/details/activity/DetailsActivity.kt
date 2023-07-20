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
            when (it) {
                DetailsActivityViewAction.DisplayPhotosDialog -> {
                    Log.d("test", "details activity: show photos dialog")
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
