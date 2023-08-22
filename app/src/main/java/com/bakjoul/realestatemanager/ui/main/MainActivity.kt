package com.bakjoul.realestatemanager.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.ActivityMainBinding
import com.bakjoul.realestatemanager.ui.add.AddPropertyActivity
import com.bakjoul.realestatemanager.ui.add.AddPropertyFragment
import com.bakjoul.realestatemanager.ui.details.DetailsFragment
import com.bakjoul.realestatemanager.ui.details.activity.DetailsActivity
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

    private companion object {
        private const val PHOTOS_DIALOG_TAG = "PhotosDialogFragment"
        private const val ADD_PROPERTY_DIALOG_TAG = "AddPropertyDialogFragment"
    }

    private val binding by viewBinding { ActivityMainBinding.inflate(it) }
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setToolbar()
        setNavigationView()
        handleOnBackPressed()

        val containerDetailsId = binding.mainFrameLayoutContainerDetails?.id
        // Empty fragment if in tablet mode
        if (containerDetailsId != null &&  supportFragmentManager.findFragmentById(containerDetailsId) == null) {
            supportFragmentManager.beginTransaction()
                .replace(containerDetailsId, DetailsFragment())
                .commitNow()
        }

        val containerMainId = binding.mainFrameLayoutContainer.id
        if (savedInstanceState == null) {
            // List of properties
            supportFragmentManager.beginTransaction()
                .replace(containerMainId, PropertyListFragment())
                .commitNow()
        }

        viewModel.mainViewActionLiveData.observeEvent(this) {
            when (it) {
                MainViewAction.ShowDetails -> startActivity(Intent(this, DetailsActivity::class.java))

                MainViewAction.ShowPhotosDialog -> {
                    val existingFragment = supportFragmentManager.findFragmentByTag(PHOTOS_DIALOG_TAG) as? PhotosFragment
                    if (existingFragment == null) {
                        PhotosFragment().show(supportFragmentManager, PHOTOS_DIALOG_TAG)
                    }
                }

                MainViewAction.ShowAddPropertyActivity -> {
                    startActivity(Intent(this, AddPropertyActivity::class.java))
                }

                MainViewAction.ShowAddPropertyDialog -> {
                    val existingFragment = supportFragmentManager.findFragmentByTag(ADD_PROPERTY_DIALOG_TAG) as? AddPropertyFragment
                    if (existingFragment == null) {
                        AddPropertyFragment().show(supportFragmentManager, ADD_PROPERTY_DIALOG_TAG)
                    }
                }

                else -> {}
            }
        }
    }

    private fun closeAddPropertyFragment(fragmentManager: FragmentManager, addPropertyFragment: Fragment) {
        fragmentManager.beginTransaction()
            .setCustomAnimations(0, R.anim.slide_out_bottom)
            .remove(addPropertyFragment)
            .commit()
        fragmentManager.popBackStack()
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

    private fun handleOnBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fragmentManager = supportFragmentManager
                val existingFragment = fragmentManager.findFragmentByTag(ADD_PROPERTY_DIALOG_TAG)

                if (existingFragment != null) {
                    closeAddPropertyFragment(fragmentManager, existingFragment)
                } else {
                    onBackPressedDispatcher.onBackPressed()
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
