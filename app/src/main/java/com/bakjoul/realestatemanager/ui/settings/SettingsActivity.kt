package com.bakjoul.realestatemanager.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.ActivitySettingsBinding
import com.bakjoul.realestatemanager.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private val binding by viewBinding { ActivitySettingsBinding.inflate(it) }

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
    }
}
