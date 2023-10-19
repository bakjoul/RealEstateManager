package com.bakjoul.realestatemanager.designsystem.molecule.photo_list

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView

class PhotoListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RecyclerView(context, attrs, defStyleAttr) {

    private val adapter = PhotoListAdapter()

    init {
        setAdapter(adapter)
    }

    fun bind(items: List<PhotoListItemViewState>) {
        adapter.submitList(items)
    }
}
