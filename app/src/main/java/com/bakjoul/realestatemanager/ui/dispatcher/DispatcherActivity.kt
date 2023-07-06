package com.bakjoul.realestatemanager.ui.dispatcher

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bakjoul.realestatemanager.ui.auth.AuthActivity
import com.bakjoul.realestatemanager.ui.main.MainActivity
import com.bakjoul.realestatemanager.ui.utils.Event.Companion.observeEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DispatcherActivity : AppCompatActivity() {

    private val viewModel by viewModels<DispatcherViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.dispatcherViewActionLiveData.observeEvent(this) {
            when (it) {
                DispatcherViewAction.NavigateToMainScreen -> {
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                    finish()
                }
                DispatcherViewAction.NavigateToAuthScreen -> {
                    startActivity(Intent(applicationContext, AuthActivity::class.java))
                    finish()
                }
            }
        }
    }
}
