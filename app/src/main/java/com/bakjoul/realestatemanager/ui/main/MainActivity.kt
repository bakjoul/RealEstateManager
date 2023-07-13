package com.bakjoul.realestatemanager.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.MenuProvider
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.ActivityMainBinding
import com.bakjoul.realestatemanager.ui.EmptyFragment
import com.bakjoul.realestatemanager.ui.details.DetailsActivity
import com.bakjoul.realestatemanager.ui.details.DetailsFragment
import com.bakjoul.realestatemanager.ui.dispatcher.DispatcherActivity
import com.bakjoul.realestatemanager.ui.list.PropertyListFragment
import com.bakjoul.realestatemanager.ui.photos.PhotosFragment
import com.bakjoul.realestatemanager.ui.settings.SettingsActivity
import com.bakjoul.realestatemanager.ui.settings.SettingsFragment
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
        setNavigationView()

        val containerDetailsId = binding.mainFrameLayoutContainerDetails?.id
        // Empty fragment if in tablet mode
        if (containerDetailsId != null &&  supportFragmentManager.findFragmentById(containerDetailsId) == null) {
            supportFragmentManager.beginTransaction()
                .replace(containerDetailsId, DetailsFragment())
                .commitNow()
        }

        if (savedInstanceState == null) {
            // List of properties
            supportFragmentManager.beginTransaction()
                .replace(binding.mainFrameLayoutContainerList.id, PropertyListFragment())
                .commitNow()
        }

        viewModel.mainViewActionLiveData.observeEvent(this) {
            when (it) {
                MainViewAction.NavigateToDetails -> startActivity(Intent(this, DetailsActivity::class.java))

                MainViewAction.DisplayPhotosDialog -> {
                    Log.d("test", "main: dialog emit")
                    val existingDialog = supportFragmentManager.findFragmentByTag("PhotosDialogFragment") as? PhotosFragment
                    if (existingDialog == null) {
                        val dialog = PhotosFragment()
                        dialog.show(supportFragmentManager, "PhotosDialogFragment")
                    }
                }
            }
        }
    }

    private fun setToolbar() {
        val toolbar = binding.mainToolbar
        toolbar.setTitle(R.string.app_name)
        setSupportActionBar(toolbar)
    }

    private fun setNavigationView() {
        val containerSettingsId = binding.mainFrameLayoutContainerSettings?.id
        binding.mainNavigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.main_drawer_logout -> {
                    viewModel.logOut()
                    startActivity(Intent(this, DispatcherActivity::class.java))
                    finish()
                }

                R.id.main_drawer_settings -> {
                    if (resources.getBoolean(R.bool.isTablet) && containerSettingsId != null) {
                        binding.mainDrawerLayout.closeDrawer(GravityCompat.END)
                        supportFragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.slide_in_right, 0)
                            .replace(containerSettingsId, SettingsFragment())
                            .commitNow()
                    } else {
                        binding.mainDrawerLayout.closeDrawer(GravityCompat.END)
                        startActivity(Intent(this, SettingsActivity::class.java))
                    }
                }
            }
            true
        }
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
