package com.bakjoul.realestatemanager.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.ActivityMainBinding
import com.bakjoul.realestatemanager.ui.details.DetailsActivity
import com.bakjoul.realestatemanager.ui.details.DetailsFragment
import com.bakjoul.realestatemanager.ui.list.PropertyListFragment
import com.bakjoul.realestatemanager.ui.utils.Event.Companion.observeEvent
import com.bakjoul.realestatemanager.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding by viewBinding { ActivityMainBinding.inflate(it) }
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
        viewModel.mainViewActionLiveData.observeEvent(this) {
            when (it) {
                MainViewAction.NavigateToDetails -> startActivity(Intent(this, DetailsActivity::class.java))
                MainViewAction.DisplayDetailsFragment -> {
                    if (containerDetailsId != null && supportFragmentManager.findFragmentById(containerDetailsId) == null) {
                        supportFragmentManager.beginTransaction()
                            .replace(containerDetailsId, DetailsFragment())
                            .commitNow()
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
