package com.bakjoul.realestatemanager.ui.details

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.FragmentDetailsBinding
import com.bakjoul.realestatemanager.ui.utils.DensityUtil
import com.bakjoul.realestatemanager.ui.utils.viewBinding
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs

@AndroidEntryPoint
class DetailsFragment : Fragment(R.layout.fragment_details) {

    private companion object {
        private const val FAB_MENU_ANIMATION_DURATION = 400L
    }

    private val binding by viewBinding { FragmentDetailsBinding.bind(it) }
    private val viewModel by viewModels<DetailsViewModel>()

    // region fab menu animations
    private val fabMenuTranslationX by lazy { DensityUtil.dip2px(requireContext(), 64f).toFloat() }
    private val fabMenuInterpolator by lazy { OvershootInterpolator() }
    private val fabMenuMainAnimator by lazy {
        var isClosing = false
        ObjectAnimator.ofPropertyValuesHolder(
            binding.detailsFabMenu,
            PropertyValuesHolder.ofFloat("rotation", 0f, -180f),
            PropertyValuesHolder.ofFloat("alpha", 1f, 0f, 1f)
        ).apply {
            duration = FAB_MENU_ANIMATION_DURATION
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                val rotation = animation.animatedValue as Float
                val shouldClose = rotation <= -90
                if (shouldClose != isClosing) {
                    isClosing = shouldClose
                    if (isClosing) {
                        binding.detailsFabMenu.setImageResource(R.drawable.baseline_close_24_white)
                    } else {
                        binding.detailsFabMenu.setImageResource(R.drawable.baseline_more_vert_24)
                    }
                }
            }
        }
    }
    private val fabEditAnimator by lazy {
        ObjectAnimator.ofPropertyValuesHolder(
            binding.detailsFabEdit,
            PropertyValuesHolder.ofFloat("translationX", 0f),
            PropertyValuesHolder.ofFloat("alpha", 1f)
        ).apply {
            duration = FAB_MENU_ANIMATION_DURATION
            interpolator = fabMenuInterpolator
        }
    }
    private val fabDeleteAnimator by lazy {
        ObjectAnimator.ofPropertyValuesHolder(
            binding.detailsFabDelete,
            PropertyValuesHolder.ofFloat("translationX", 0f),
            PropertyValuesHolder.ofFloat("alpha", 1f)
        ).apply {
            duration = FAB_MENU_ANIMATION_DURATION
            interpolator = fabMenuInterpolator
        }
    }
    // For devices with API < 26
    private val fabMenuMainAnimatorReverse by lazy {
        ObjectAnimator.ofPropertyValuesHolder(
            binding.detailsFabMenu,
            PropertyValuesHolder.ofFloat("rotation", -180f, 0f),
            PropertyValuesHolder.ofFloat("alpha", 1f, 0f, 1f)
        ).apply {
            duration = FAB_MENU_ANIMATION_DURATION
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                val rotation = animation.animatedValue as Float
                if (rotation == -90f) {
                    binding.detailsFabMenu.setImageResource(R.drawable.baseline_more_vert_24)
                }
            }
        }
    }
    private val fabEditAnimatorReverse by lazy {
        ObjectAnimator.ofPropertyValuesHolder(
            binding.detailsFabEdit,
            PropertyValuesHolder.ofFloat("translationX", fabMenuTranslationX),
            PropertyValuesHolder.ofFloat("alpha", 0f)
        ).apply {
            duration = FAB_MENU_ANIMATION_DURATION
            interpolator = fabMenuInterpolator
        }
    }
    private val fabDeleteAnimatorReverse by lazy {
        ObjectAnimator.ofPropertyValuesHolder(
            binding.detailsFabDelete,
            PropertyValuesHolder.ofFloat("translationX", fabMenuTranslationX),
            PropertyValuesHolder.ofFloat("alpha", 0f)
        ).apply {
            duration = FAB_MENU_ANIMATION_DURATION
            interpolator = fabMenuInterpolator
        }
    }
    private var isFabMenuOpen = false
    // endregion

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.detailsToolbar.setPadding(0, 0, 0, 0)  // Removes toolbar padding on tablets
        if (!resources.getBoolean(R.bool.isTablet)) {
            setToolbarInfoAnimation()
        }
        handleOnBackPressed()
        setFabMenu()

        binding.detailsFabBack?.setOnClickListener { viewModel.onBackButtonClicked() }
        binding.detailsFabEdit.setOnClickListener {
            viewModel.onEditButtonClicked()
            closeFabMenu()
        }
        binding.detailsFabDelete.setOnClickListener {
            viewModel.onDeleteButtonClicked()
            closeFabMenu()
        }

        // Medias RecyclerView
        val divider = DividerItemDecoration(requireContext(), DividerItemDecoration.HORIZONTAL)
        divider.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.photos_divider_details)!!)
        binding.detailsPhotoListView.addItemDecoration(divider)

        viewModel.viewStateLiveData.observe(viewLifecycleOwner) { details ->
            Glide.with(binding.detailsToolbarPhoto)
                .load(details.featuredPhotoUrl)
                .placeholder(R.drawable.photo_placeholder)
                .into(binding.detailsToolbarPhoto)
            binding.detailsToolbarType.text = details.type.toCharSequence(requireContext())
            binding.detailsToolbarPrice.text = details.price
            binding.detailsToolbarPrice.paintFlags = if (details.isSold) binding.detailsToolbarPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG else 0
            binding.detailsToolbarSold.visibility = if (details.isSold) View.VISIBLE else View.GONE
            binding.detailsToolbarCity.text = details.city
            binding.detailsToolbarSurface.text = details.surface.toCharSequence(requireContext())
            binding.detailsToolbarSaleStatus.text = details.saleStatus.toCharSequence(requireContext())
            binding.detailsDescriptionText.text = details.description
            binding.detailsItemSurface.setText(details.surface.toCharSequence(requireContext()))
            binding.detailsItemRooms.setText(details.rooms)
            binding.detailsItemBedrooms.setText(details.bedrooms)
            binding.detailsItemBathrooms.setText(details.bathrooms)
            setTooltip(details.poiSchool, binding.detailsPoiSchool, getString(R.string.property_poi_school))
            setTooltip(details.poiStore, binding.detailsPoiStore, getString(R.string.property_poi_store))
            setTooltip(details.poiPark, binding.detailsPoiPark, getString(R.string.property_poi_park))
            setTooltip(details.poiRestaurant, binding.detailsPoiRestaurant, getString(R.string.property_poi_restaurant))
            setTooltip(details.poiHospital, binding.detailsPoiHospital, getString(R.string.property_poi_hospital))
            setTooltip(details.poiBus, binding.detailsPoiBus, getString(R.string.property_poi_bus))
            setTooltip(details.poiSubway, binding.detailsPoiSubway, getString(R.string.property_poi_subway))
            setTooltip(details.poiTramway, binding.detailsPoiTramway, getString(R.string.property_poi_tramway))
            setTooltip(details.poiTrain, binding.detailsPoiTrain, getString(R.string.property_poi_train))
            setTooltip(details.poiAirport, binding.detailsPoiAirport, getString(R.string.property_poi_airport))
            binding.detailsItemLocation.setText(details.location)

            if (details.medias.isNotEmpty()) {
                binding.detailsPhotoListView.bind(details.medias)
            } else {
                binding.detailsPhotoListView.visibility = View.GONE
                binding.detailsNoPhotosTextView.visibility = View.VISIBLE
            }

            binding.detailsItemLocation.setOnClickListener {
                val clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("address", details.clipboardAddress)
                clipboardManager.setPrimaryClip(clipData)
                viewModel.onLocationClicked()
            }
            Glide.with(binding.detailsStaticMap).load(details.staticMapUrl).into(binding.detailsStaticMap)
            binding.detailsStaticMap.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${details.mapsAddress}"))
                intent.setPackage("com.google.android.apps.maps")
                startActivity(intent)
            }
        }
    }

    private fun setToolbarInfoAnimation() {
        val toolbarInfo = binding.detailsToolbarTypePriceStatusContainer
        val xMargin = DensityUtil.dip2px(requireContext(), 34f)
        val startAnimationY = DensityUtil.dip2px(requireContext(), 56f)
        val layoutParams = toolbarInfo.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.marginStart = 0

        var previousMargin = 0 // Avoids unnecessary updates

        binding.detailsAppbar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->

            val startOfAnimation = appBarLayout.totalScrollRange - startAnimationY
            val endOfAnimation = appBarLayout.totalScrollRange

            val animatedMargin = if (abs(verticalOffset) > startOfAnimation) {
                xMargin * (abs(verticalOffset) - startOfAnimation) / (endOfAnimation - startOfAnimation)
            } else {
                0
            }

            // Checks if the new margin is different from the previous one
            if (previousMargin != animatedMargin) {
                layoutParams.marginStart = animatedMargin
                toolbarInfo.layoutParams = layoutParams

                // Updates previous margin
                previousMargin = animatedMargin
            }
        }
    }

    private fun handleOnBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val drawer: DrawerLayout = requireActivity().findViewById(R.id.main_DrawerLayout)
                if (drawer.isDrawerOpen(GravityCompat.END)) {
                    drawer.closeDrawer(GravityCompat.END)
                } else {
                    viewModel.onBackButtonClicked()
                }
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setFabMenu() {
        binding.detailsFabEdit.translationX = fabMenuTranslationX
        binding.detailsFabDelete.translationX = fabMenuTranslationX
        binding.detailsFabEdit.setAlpha(0f)
        binding.detailsFabDelete.setAlpha(0f)

        binding.detailsFabMenu.setOnClickListener {
            if (isFabMenuOpen) {
                closeFabMenu()
            } else {
                openFabMenu()
            }
        }

        binding.detailsTouchInterceptorView.setOnTouchListener { _, event ->
            if (isFabMenuOpen && event.action == MotionEvent.ACTION_DOWN) {
                if (!isTouchInsideView(binding.detailsFabMenuLinearLayout, event)) {
                    closeFabMenu()
                }
            }
            false
        }

    }

    private fun openFabMenu() {
        isFabMenuOpen = !isFabMenuOpen

        AnimatorSet().apply {
            playTogether(fabMenuMainAnimator, fabEditAnimator, fabDeleteAnimator)
            start()
        }
    }


    @SuppressLint("Recycle")
    private fun closeFabMenu() {
        isFabMenuOpen = !isFabMenuOpen

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AnimatorSet().apply {
                playTogether(fabMenuMainAnimator, fabEditAnimator, fabDeleteAnimator)
                reverse()
            }
        } else {
            AnimatorSet().apply {
                playTogether(fabMenuMainAnimatorReverse, fabEditAnimatorReverse, fabDeleteAnimatorReverse)
                start()
            }
        }
    }

    private fun isTouchInsideView(view: View, event: MotionEvent): Boolean {
        val touchX = event.rawX.toInt()
        val touchY = event.rawY.toInt()

        val viewLocation = IntArray(2)
        view.getLocationOnScreen(viewLocation)
        val viewX = viewLocation[0]
        val viewYY = viewLocation[1]

        return touchX >= viewX && touchX <= viewX + view.width &&
                touchY >= viewYY && touchY <= viewYY + view.height
    }

    private fun setTooltip(isNearby: Boolean, poi: View, text: String) {
        if (isNearby) {
            poi.visibility = View.VISIBLE
            val tooltip = createPopupWindow(poi.context, text)
            poi.setOnClickListener {
                tooltip.showAsDropDown(poi, DensityUtil.dip2px(poi.context, 12f), 0)
            }
        } else {
            poi.visibility = View.GONE
        }
    }

    @SuppressLint("InflateParams")
    private fun createPopupWindow(context: Context, text: String): PopupWindow {
        val tooltipView = LayoutInflater.from(context).inflate(R.layout.fragment_list_tooltip, null)
        val tooltipTextView = tooltipView.findViewById<TextView>(R.id.tooltip_text)
        tooltipTextView.text = text

        return PopupWindow(context).apply {
            contentView = tooltipView
            width = ViewGroup.LayoutParams.WRAP_CONTENT
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            isOutsideTouchable = true
        }
    }
}
