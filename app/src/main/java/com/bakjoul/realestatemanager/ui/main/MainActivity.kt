package com.bakjoul.realestatemanager.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bakjoul.realestatemanager.databinding.MainActivityBinding
import com.bakjoul.realestatemanager.ui.details.DetailsActivity
import com.bakjoul.realestatemanager.ui.details.DetailsFragment
import com.bakjoul.realestatemanager.ui.list.PropertyListFragment
import com.bakjoul.realestatemanager.ui.utils.Event.Companion.observeEvent
import com.bakjoul.realestatemanager.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding by viewBinding { MainActivityBinding.inflate(it) }
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.mainFrameLayoutContainerList.id, PropertyListFragment())
                .commitNow()
        }

        val containerDetailsId = binding.mainFrameLayoutContainerDetails?.id
        viewModel.getCurrentPropertyIdLiveData().observe(this) { propertyId ->
            if (propertyId != null && containerDetailsId != null && supportFragmentManager.findFragmentById(containerDetailsId) == null) {
                supportFragmentManager.beginTransaction()
                    .replace(containerDetailsId, DetailsFragment())
                    .commitNow()
            }
        }

        viewModel.mainViewActionLiveData.observeEvent(this) {
            when (it) {
                MainViewAction.NavigateToDetails -> startActivity(Intent(this, DetailsActivity::class.java))
            }
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.onResume()
    }
}
