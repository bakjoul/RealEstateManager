package com.bakjoul.realestatemanager.ui.settings.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.ActivitySettingsBinding
import com.bakjoul.realestatemanager.ui.settings.SettingsFragment
import com.bakjoul.realestatemanager.ui.utils.Event.Companion.observeEvent
import com.bakjoul.realestatemanager.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private val binding by viewBinding { ActivitySettingsBinding.inflate(it) }
    private val viewModel by viewModels<SettingsActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (resources.getBoolean(R.bool.isTablet)) {
            finish()
        }

        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.settingsFrameLayoutContainer.id, SettingsFragment())
                .commitNow()
        }

        viewModel.viewActionLiveData.observeEvent(this) {
            when (it) {
                SettingsActivityViewAction.CloseSettings -> finish()
            }
        }
    }
}
