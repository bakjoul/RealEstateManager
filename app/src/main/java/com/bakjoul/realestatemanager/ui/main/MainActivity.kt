package com.bakjoul.realestatemanager.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.ActivityMainBinding
import com.bakjoul.realestatemanager.ui.add.AddPropertyFragment
import com.bakjoul.realestatemanager.ui.details.DetailsFragment
import com.bakjoul.realestatemanager.ui.dispatcher.DispatcherActivity
import com.bakjoul.realestatemanager.ui.drafts.DraftsFragment
import com.bakjoul.realestatemanager.ui.list.PropertyListFragment
import com.bakjoul.realestatemanager.ui.loan_simulator.LoanSimulatorFragment
import com.bakjoul.realestatemanager.ui.photos.PhotosFragment
import com.bakjoul.realestatemanager.ui.settings.SettingsFragment
import com.bakjoul.realestatemanager.ui.utils.Event.Companion.observeEvent
import com.bakjoul.realestatemanager.ui.utils.viewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private companion object {
        private const val PROPERTY_LIST_TAG = "PropertyListFragment"
        private const val DETAILS_TABLET_TAG = "DetailsFragmentTablet"
        private const val DETAILS_PORTRAIT_TAG = "DetailsFragmentPortrait"
        private const val PHOTOS_DIALOG_TAG = "PhotosDialogFragment"
        private const val DRAFTS_DIALOG_TAG = "DraftsDialogFragment"
        private const val ADD_PROPERTY_DIALOG_TAG = "AddPropertyDialogFragment"
        private const val SETTINGS_DIALOG_TAG = "SettingsDialogFragment"
        private const val LOAN_SIMULATOR_DIALOG_TAG = "LoanSimulatorDialogFragment"
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

        val containerMainId = binding.mainFrameLayoutContainer.id
        val containerDetailsId = binding.mainFrameLayoutContainerDetails?.id

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(containerMainId, PropertyListFragment(), PROPERTY_LIST_TAG)
                .commitNow()
        }

        viewModel.mainViewActionLiveData.observeEvent(this) {
            Log.d("test", "main activity observed event: $it")
            when (it) {
                MainViewAction.ShowDetailsTablet -> {
                    hideDetailsPortrait()
                    showDetailsTablet(containerDetailsId)
                }

                MainViewAction.CloseDetailsTablet -> {
                    val existingFragment = supportFragmentManager.findFragmentByTag(DETAILS_TABLET_TAG)
                    if (existingFragment != null) {
                        supportFragmentManager.commit {
                            setCustomAnimations(0, R.anim.slide_out_left)
                            remove(existingFragment)
                        }
                        handleOnBackPressed()
                    }
                }

                MainViewAction.ShowDetailsPortrait -> {
                    val existingFragment = supportFragmentManager.findFragmentByTag(DETAILS_PORTRAIT_TAG)
                    if (existingFragment == null) {
                        supportFragmentManager.commit {
                            setCustomAnimations(R.anim.slide_in_right, 0)
                            add(containerMainId, DetailsFragment(), DETAILS_PORTRAIT_TAG)
                            addToBackStack(DETAILS_PORTRAIT_TAG)
                        }
                    } else {
                        supportFragmentManager.commit {
                            setCustomAnimations(R.anim.slide_in_right, 0)
                            show(existingFragment)
                        }
                    }
                }

                MainViewAction.CloseDetailsPortrait -> {
                    val detailsPortraitFragment = supportFragmentManager.findFragmentByTag(DETAILS_PORTRAIT_TAG)
                    if (detailsPortraitFragment != null) {
                        supportFragmentManager.popBackStack(DETAILS_PORTRAIT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        supportFragmentManager.commitNow {
                            setCustomAnimations(0, R.anim.slide_out_right)
                            remove(detailsPortraitFragment)
                        }
                        handleOnBackPressed()
                    }

                    val detailsTabletFragment = supportFragmentManager.findFragmentByTag(DETAILS_TABLET_TAG)
                    if (detailsTabletFragment != null) {
                        supportFragmentManager.commit { remove(detailsTabletFragment) }
                    }
                }

                MainViewAction.ShowDetailsPortraitIfNeeded -> showDetailsPortraitIfNeeded(containerMainId)

                MainViewAction.HideDetailsPortrait -> hideDetailsPortrait()

                is MainViewAction.ShowClipboardToastAndDetailsTabletIfNeeded -> {
                    if (it.showToast) {
                        Toast.makeText(this, it.message.toCharSequence(this), Toast.LENGTH_SHORT).show()
                        viewModel.onClipboardToastShown()
                    }
                    hideDetailsPortrait()
                    showDetailsTablet(containerDetailsId)
                }

                is MainViewAction.ShowClipboardToastAndDetailsPortraitIfNeeded -> {
                    if (it.showToast) {
                        Toast.makeText(this, it.message.toCharSequence(this), Toast.LENGTH_SHORT).show()
                        viewModel.onClipboardToastShown()
                    }
                    showDetailsPortraitIfNeeded(containerMainId)
                }

                MainViewAction.ShowPhotosDialog -> {
                    showDetailsPortraitIfNeeded(containerMainId)
                    showPhotosDialog()
                }

                MainViewAction.ShowPhotosDialogAndHideDetailsPortrait -> {
                    val detailsPortraitFragment = hideDetailsPortrait()
                    showDetailsTabletIfNeeded(containerDetailsId, detailsPortraitFragment)
                    showPhotosDialog()
                }

                MainViewAction.ShowPropertyDraftDialog -> {
                    MaterialAlertDialogBuilder(this)
                        .setTitle(getString(R.string.draft_dialog_title))
                        .setMessage(getString(R.string.draft_dialog_message))
                        .setNeutralButton(getString(R.string.cancel)) { _, _ -> }
                        .setNegativeButton(getString(R.string.draft_dialog_negative)) { _, _ ->
                            viewModel.onAddNewPropertyClicked()
                        }
                        .setPositiveButton(getString(R.string.draft_dialog_positive)) { _, _ ->
                            viewModel.onContinueEditingDraftClicked()
                        }
                        .show()
                }

                MainViewAction.ShowDraftListDialog -> {
                    val existingFragment = supportFragmentManager.findFragmentByTag(DRAFTS_DIALOG_TAG)
                    if (existingFragment == null) {
                        DraftsFragment().show(supportFragmentManager, DRAFTS_DIALOG_TAG)
                    }
                }

                is MainViewAction.ShowAddPropertyDialog -> {
                    showDetailsPortraitIfNeeded(containerMainId)

                    val existingFragment = supportFragmentManager.findFragmentByTag(ADD_PROPERTY_DIALOG_TAG)
                    if (existingFragment == null) {
                        AddPropertyFragment.newInstance(it.draftId, it.isNewDraft).show(supportFragmentManager, ADD_PROPERTY_DIALOG_TAG)
                    }
                }

                MainViewAction.ReturnToDispatcher -> {
                    startActivity(Intent(this, DispatcherActivity::class.java))
                    finish()
                }

                MainViewAction.ShowSettings -> {
                    binding.mainDrawerLayout.closeDrawer(GravityCompat.END)
                    showSettings()
                }

                MainViewAction.ShowSettingsAndHideDetailsPortrait -> {
                    binding.mainDrawerLayout.closeDrawer(GravityCompat.END)

                    val detailsPortraitFragment = hideDetailsPortrait()
                    showDetailsTabletIfNeeded(containerDetailsId, detailsPortraitFragment)
                    showSettings()
                }

                MainViewAction.ShowLoanSimulatorDialog -> {
                    val existingFragment = supportFragmentManager.findFragmentByTag(LOAN_SIMULATOR_DIALOG_TAG)
                    if (existingFragment == null) {
                        LoanSimulatorFragment().show(supportFragmentManager, LOAN_SIMULATOR_DIALOG_TAG)
                    }
                }
            }
        }
    }

    private fun showDetailsTablet(containerDetailsId: Int?) {
        val existingDetailsTabletFragment =
            supportFragmentManager.findFragmentByTag(DETAILS_TABLET_TAG)
        if (containerDetailsId != null && existingDetailsTabletFragment == null) {
            supportFragmentManager.commit {
                setCustomAnimations(R.anim.slide_in_left, 0)
                replace(containerDetailsId, DetailsFragment(), DETAILS_TABLET_TAG)
            }
        }
    }

    private fun hideDetailsPortrait(): Fragment? {
        val detailsPortraitFragment = supportFragmentManager.findFragmentByTag(DETAILS_PORTRAIT_TAG)
        if (detailsPortraitFragment != null) {
            supportFragmentManager.commit { hide(detailsPortraitFragment) }
        }
        return detailsPortraitFragment
    }

    private fun showDetailsTabletIfNeeded(containerDetailsId: Int?, detailsPortraitFragment: Fragment?) {
        val detailsTabletFragment = supportFragmentManager.findFragmentByTag(DETAILS_TABLET_TAG)
        if (containerDetailsId != null && detailsPortraitFragment != null && detailsTabletFragment == null) {
            supportFragmentManager.commit {
                setCustomAnimations(R.anim.slide_in_left, 0)
                replace(containerDetailsId, DetailsFragment(), DETAILS_TABLET_TAG)
            }
        }
    }

    private fun showDetailsPortraitIfNeeded(containerMainId: Int) {
        val detailsPortraitFragment = supportFragmentManager.findFragmentByTag(DETAILS_PORTRAIT_TAG)
        val detailsTabletFragment = supportFragmentManager.findFragmentByTag(DETAILS_TABLET_TAG)

        if (detailsPortraitFragment == null && detailsTabletFragment != null && !detailsTabletFragment.isVisible) {
            supportFragmentManager.commit {
                add(containerMainId, DetailsFragment(), DETAILS_PORTRAIT_TAG)
                addToBackStack(DETAILS_PORTRAIT_TAG)
            }
        } else if (detailsPortraitFragment != null && detailsPortraitFragment.isHidden && detailsTabletFragment != null && !detailsTabletFragment.isVisible) {
            supportFragmentManager.commit { show(detailsPortraitFragment) }
        }
    }

    private fun showPhotosDialog() {
        val existingFragment = supportFragmentManager.findFragmentByTag(PHOTOS_DIALOG_TAG)
        if (existingFragment == null) {
            PhotosFragment().show(supportFragmentManager, PHOTOS_DIALOG_TAG)
        }
    }

    private fun showSettings() {
        val existingFragment = supportFragmentManager.findFragmentByTag(SETTINGS_DIALOG_TAG)
        if (existingFragment == null) {
            SettingsFragment().show(supportFragmentManager, SETTINGS_DIALOG_TAG)
        }
    }

    private fun setToolbar() {
        val toolbar = binding.mainToolbar
        toolbar.setTitle(R.string.app_name)
        setSupportActionBar(toolbar)
    }

    private fun setMenu() {
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.main_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean = when (menuItem.itemId) {
                R.id.main_menu_button -> {
                    binding.mainDrawerLayout.openDrawer(GravityCompat.END)
                    true
                }
                R.id.main_loan_simulator_button -> {
                    viewModel.onLoanSimulatorClicked()
                    true
                }

                else -> false
            }
        })
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

    override fun onResume() {
        super.onResume()

        viewModel.onResume(resources.getBoolean(R.bool.isTablet))
    }
}
