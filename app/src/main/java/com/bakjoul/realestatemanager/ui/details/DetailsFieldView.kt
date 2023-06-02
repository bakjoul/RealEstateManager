package com.bakjoul.realestatemanager.ui.details

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bakjoul.realestatemanager.R

class DetailsFieldView : ConstraintLayout {

    private lateinit var imageView: ImageView
    private lateinit var tvLabel: TextView
    private lateinit var tvText: TextView
    private var imageResId = 0
    private var label = ""

    constructor(context: Context) : super(context) {
        initDetailsField(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        getStuffFromXml(context, attrs)
        initDetailsField(context)
    }

    fun setText(text: String) {
        tvText.text = text
    }

    private fun initDetailsField(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.fragment_details_field, this, true)

        imageView = findViewById(R.id.details_field_icon)
        tvLabel = findViewById(R.id.details_field_label)
        tvText = findViewById(R.id.details_field_text)

        if (imageResId != 0) {
            imageView.setImageResource(imageResId)
        }
        if (label.isNotEmpty()) {
            tvLabel.text = label
        }
    }

    private fun getStuffFromXml(context: Context, attrs: AttributeSet?) {
        val data = context.obtainStyledAttributes(attrs, R.styleable.DetailsFieldView)

        imageResId = data.getResourceId(R.styleable.DetailsFieldView_imageSrc, 0)
        label = data.getString(R.styleable.DetailsFieldView_label).toString()

        data.recycle()
    }
}
