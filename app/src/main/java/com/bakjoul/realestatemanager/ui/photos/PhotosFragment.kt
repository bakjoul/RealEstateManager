package com.bakjoul.realestatemanager.ui.photos

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.viewpager2.widget.ViewPager2
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.FragmentPhotosBinding
import com.bakjoul.realestatemanager.domain.property.model.PhotoEntity
import com.bakjoul.realestatemanager.ui.utils.viewBinding

class PhotosFragment(
    private val photos: List<PhotoEntity>,
    private val currentPhotoId: Long,
    private val dialogDismissListener: OnDialogDismissListener
) : DialogFragment(R.layout.fragment_photos) {

    companion object {
        fun newInstance(
            photos: List<PhotoEntity>,
            currentPhotoId: Long,
            dialogDismissListener: OnDialogDismissListener
        ) = PhotosFragment(photos, currentPhotoId, dialogDismissListener)
    }

    private val binding by viewBinding { FragmentPhotosBinding.bind(it) }
    private var selectedPhotoId = currentPhotoId

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val adapter = PhotosPagerAdapter(photos)
        binding.photosViewPager.adapter = adapter
        binding.photosViewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        val currentPhotoIndex = photos.indexOfFirst { it.id == currentPhotoId }
        binding.photosViewPager.setCurrentItem(currentPhotoIndex, false)

        binding.photosDotsIndicator.attachTo(binding.photosViewPager)

        binding.photosViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                selectedPhotoId = photos[position].id
            }
        })
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        dialogDismissListener.onDialogDismissed(selectedPhotoId)
    }
}
