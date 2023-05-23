package com.bakjoul.realestatemanager.ui.main

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    private var isTablet: Boolean = false

    fun onResume(isTablet: Boolean) {
        this.isTablet = isTablet
    }
}
