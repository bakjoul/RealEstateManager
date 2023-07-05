package com.bakjoul.realestatemanager.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.ActivityMainBinding
import com.bakjoul.realestatemanager.ui.EmptyFragment
import com.bakjoul.realestatemanager.ui.details.DetailsActivity
import com.bakjoul.realestatemanager.ui.details.DetailsFragment
import com.bakjoul.realestatemanager.ui.list.PropertyListFragment
import com.bakjoul.realestatemanager.ui.settings.SettingsActivity
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

        setToolbar()
        setMenu()

        val containerDetailsId = binding.mainFrameLayoutContainerDetails?.id
        if (savedInstanceState == null) {
            // List of properties
            supportFragmentManager.beginTransaction()
                .replace(binding.mainFrameLayoutContainerList.id, PropertyListFragment())
                .commitNow()

            // Empty fragment if in tablet mode
            if (containerDetailsId != null) {
                supportFragmentManager.beginTransaction()
                    .replace(containerDetailsId, EmptyFragment())
                    .commitNow()
            }
        }

        viewModel.mainViewActionLiveData.observeEvent(this) {
            when (it) {
                MainViewAction.NavigateToDetails -> startActivity(Intent(this, DetailsActivity::class.java))

                MainViewAction.DisplayDetailsFragment -> {
                    if (containerDetailsId != null) {
                        val existingFragment = supportFragmentManager.findFragmentById(containerDetailsId)
                        if (existingFragment == null || existingFragment is EmptyFragment) {
                            supportFragmentManager.beginTransaction()
                                .replace(containerDetailsId, DetailsFragment())
                                .commitNow()
                        }
                    }
                }

                MainViewAction.DisplayEmptyFragment -> {
                    if (containerDetailsId != null) {
                        val existingFragment = supportFragmentManager.findFragmentById(containerDetailsId)
                        if (existingFragment == null || existingFragment is DetailsFragment) {
                            supportFragmentManager.beginTransaction()
                                .replace(containerDetailsId, EmptyFragment())
                                .commitNow()
                        }
                    }
                }
            }
        }
    }

    private fun setToolbar() {
        val toolbar = binding.mainToolbar
        toolbar?.setTitle(R.string.app_name)
        setSupportActionBar(toolbar)
    }

    private fun setMenu() {
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.actions, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean = when (menuItem.itemId) {
                R.id.menu_settings -> {
                    startActivity(Intent(applicationContext, SettingsActivity::class.java))
                    true
                }

                else -> false
            }
        })
    }

    override fun onResume() {
        super.onResume()

        viewModel.onResume(resources.getBoolean(R.bool.isTablet))
    }
}
