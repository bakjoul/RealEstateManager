package com.bakjoul.realestatemanager.ui.add

import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.bakjoul.realestatemanager.databinding.ActivityAddPropertyBinding
import com.bakjoul.realestatemanager.ui.utils.hideKeyboard
import com.bakjoul.realestatemanager.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddPropertyActivity : AppCompatActivity() {

    private val binding by viewBinding { ActivityAddPropertyBinding.inflate(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.addPropertyFrameLayoutContainer.id, AddPropertyFragment())
                .commitNow()
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            hideKeyboard()
            currentFocus!!.clearFocus()
        }
        return super.dispatchTouchEvent(ev)
    }
}
