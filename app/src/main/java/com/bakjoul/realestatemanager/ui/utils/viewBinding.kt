@file:Suppress("unused")

package com.bakjoul.realestatemanager.ui.utils

import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/** Activity binding delegate, may be used since onCreate up to onDestroy (inclusive)
 *
 * Example usage:
 * ```
 * class MyActivity : AppCompatActivity() {
 *     private val binding by viewBinding { MyActivityBinding.inflate(it) }
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *
 *         setContentView(binding.root)
 *         binding.myTextView.text = "Hello world!"
 *     }
 * }
 * ```
 * */
inline fun <T : ViewBinding> AppCompatActivity.viewBinding(
    crossinline factory: (LayoutInflater) -> T,
) = lazy(LazyThreadSafetyMode.NONE) {
    factory(layoutInflater)
}

/** Fragment binding delegate, may be used since onViewCreated up to onDestroyView (inclusive)
 *
 * Example usage:
 * ```
 * class MyFragment : Fragment(R.layout.my_fragment) {
 *     private val binding by viewBinding { MyFragmentBinding.bind(it) }
 *
 *     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
 *         super.onViewCreated(view, savedInstanceState)
 *
 *         binding.myTextView.text = "Hello world!"
 *     }
 * }
 * ```
 * */
inline fun <T : ViewBinding> Fragment.viewBinding(
    crossinline factory: (View) -> T,
): ReadOnlyProperty<Fragment, T> = object : ReadOnlyProperty<Fragment, T>, DefaultLifecycleObserver {

    private var binding: T? = null

    private val viewLifecycleOwnerLiveDataObserver = Observer<LifecycleOwner?> {
        it?.lifecycle?.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                binding = null
            }
        })
    }

    init {
        lifecycle.addObserver(this)
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        val capturedBinding = binding
        if (capturedBinding != null) {
            return capturedBinding
        }

        if (!viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            throw IllegalStateException("Should not attempt to get bindings when Fragment views are destroyed.")
        }

        return factory(thisRef.requireView()).also { this.binding = it }
    }

    override fun onCreate(owner: LifecycleOwner) {
        viewLifecycleOwnerLiveData.observeForever(viewLifecycleOwnerLiveDataObserver)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        viewLifecycleOwnerLiveData.removeObserver(viewLifecycleOwnerLiveDataObserver)
    }
}
