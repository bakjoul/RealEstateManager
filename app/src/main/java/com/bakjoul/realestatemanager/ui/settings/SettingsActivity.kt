package com.bakjoul.realestatemanager.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.ActivitySettingsBinding
import com.bakjoul.realestatemanager.ui.utils.viewBinding

class SettingsActivity : AppCompatActivity() {

    private val binding by viewBinding { ActivitySettingsBinding.inflate(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setToolbar()
    }


    private fun setToolbar() {
        val toolbar = binding.settingsToolbar
        toolbar.setTitle(R.string.settings)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }
}
