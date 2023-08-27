package com.bakjoul.realestatemanager.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.MenuProvider
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.ActivityMainBinding
import com.bakjoul.realestatemanager.ui.add.AddPropertyFragment
import com.bakjoul.realestatemanager.ui.details.DetailsFragment
import com.bakjoul.realestatemanager.ui.details.activity.DetailsActivity
import com.bakjoul.realestatemanager.ui.dispatcher.DispatcherActivity
import com.bakjoul.realestatemanager.ui.list.PropertyListFragment
import com.bakjoul.realestatemanager.ui.photos.PhotosFragment
import com.bakjoul.realestatemanager.ui.settings.SettingsFragment
import com.bakjoul.realestatemanager.ui.settings.activity.SettingsActivity
import com.bakjoul.realestatemanager.ui.utils.Event.Companion.observeEvent
import com.bakjoul.realestatemanager.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private companion object {
        private const val PHOTOS_DIALOG_TAG = "PhotosDialogFragment"
        private const val DETAILS_TABLET_TAG = "DetailsFragmentTablet"
        private const val DETAILS_PORTRAIT_TAG = "DetailsFragmentPortrait"
        private const val ADD_PROPERTY_DIALOG_TAG = "AddPropertyDialogFragment"
        private const val SETTINGS_TAG = "SettingsFragment"
    }

    private val binding by viewBinding { ActivityMainBinding.inflate(it) }
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setToolbar()
        setMenu()
        setNavigationView()
        handleOnBackPressed()

        val containerDetailsId = binding.mainFrameLayoutContainerDetails?.id
        // Empty fragment if in tablet mode
        /*if (containerDetailsId != null &&  supportFragmentManager.findFragmentById(containerDetailsId) == null) {
            supportFragmentManager.beginTransaction()
                .replace(containerDetailsId, DetailsFragment())
                .commitNow()
        }*/

        val containerMainId = binding.mainFrameLayoutContainer.id
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(containerMainId, PropertyListFragment())
                .commitNow()
        }

        viewModel.mainViewActionLiveData.observeEvent(this) {
            Log.d("test", "main activity observed event: $it")
            when (it) {
                MainViewAction.ShowDetailsTablet -> {
                    val existingFragment = supportFragmentManager.findFragmentByTag(DETAILS_TABLET_TAG)
                    if (containerDetailsId != null && existingFragment == null) {
                        supportFragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.slide_in_right, 0)
                            .replace(containerDetailsId, DetailsFragment(), DETAILS_TABLET_TAG)
                            .commitNow()
                    }
                }

                MainViewAction.CloseDetailsTablet -> {
                    val existingFragment = supportFragmentManager.findFragmentByTag(DETAILS_TABLET_TAG)
                    if (existingFragment != null) {
                        supportFragmentManager.beginTransaction()
                            .remove(existingFragment)
                            .commitNow()
                    }
                }

                MainViewAction.ShowDetailsPortrait -> {
                    val addPropertyFragment = supportFragmentManager.findFragmentByTag(DETAILS_PORTRAIT_TAG)
                    if (addPropertyFragment == null) {
                        startActivity(Intent(this, DetailsActivity::class.java))
                    }
                }

                MainViewAction.ShowPhotosDialog -> {
                    val existingFragment = supportFragmentManager.findFragmentByTag(PHOTOS_DIALOG_TAG) as? PhotosFragment
                    if (existingFragment == null) {
                        PhotosFragment().show(supportFragmentManager, PHOTOS_DIALOG_TAG)
                    }
                }

                MainViewAction.ShowAddPropertyDialog -> {
                    val existingFragment = supportFragmentManager.findFragmentByTag(ADD_PROPERTY_DIALOG_TAG) as? AddPropertyFragment
                    if (existingFragment == null) {
                        AddPropertyFragment().show(supportFragmentManager, ADD_PROPERTY_DIALOG_TAG)
                    }
                }

                MainViewAction.ReturnToDispatcher -> {
                    startActivity(Intent(this, DispatcherActivity::class.java))
                    finish()
                }

                MainViewAction.ShowSettingsTablet -> {
                    binding.mainDrawerLayout.closeDrawer(GravityCompat.END)
                    val existingFragment = supportFragmentManager.findFragmentByTag(SETTINGS_TAG)
                    val containerSettingsId = binding.mainFrameLayoutContainerSettings?.id
                    if (containerSettingsId != null && existingFragment == null) {
                        supportFragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.slide_in_right, 0)
                            .replace(containerSettingsId, SettingsFragment(), SETTINGS_TAG)
                            .commitNow()
                    }
                }

                MainViewAction.CloseSettingsTablet -> {
                    val existingFragment = supportFragmentManager.findFragmentByTag(SETTINGS_TAG)
                    if (existingFragment != null) {
                        supportFragmentManager.beginTransaction()
                            .setCustomAnimations(0, R.anim.slide_out_right)
                            .remove(existingFragment)
                            .commitNow()
                    }
                }

                MainViewAction.ShowSettings -> {
                    binding.mainDrawerLayout.closeDrawer(GravityCompat.END)
                    startActivity(Intent(this, SettingsActivity::class.java))
                }

                MainViewAction.CloseSettings -> {}
            }
        }
    }

    private fun setToolbar() {
        val toolbar = binding.mainToolbar
        toolbar.setTitle(R.string.app_name)
        setSupportActionBar(toolbar)
    }

    private fun setNavigationView() {
        binding.mainNavigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.main_drawer_logout -> viewModel.onLogOut()
                R.id.main_drawer_settings -> viewModel.onSettingsClicked()
            }
            true
        }
    }

    private fun handleOnBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fragmentManager = supportFragmentManager

                if (binding.mainDrawerLayout.isDrawerOpen(GravityCompat.END)) {
                    binding.mainDrawerLayout.closeDrawer(GravityCompat.END)
                } else {
                    if (fragmentManager.backStackEntryCount > 0) {
                        fragmentManager.popBackStack()
                    } else {
                        finish()
                    }
                }
            }
        })
    }

    private fun setMenu() {
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.actions, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean = when (menuItem.itemId) {
                R.id.actions_menu -> {
                    binding.mainDrawerLayout.openDrawer(GravityCompat.END)
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
