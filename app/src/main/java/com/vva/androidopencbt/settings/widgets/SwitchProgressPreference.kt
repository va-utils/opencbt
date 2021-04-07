package com.vva.androidopencbt.settings.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.appcompat.widget.SwitchCompat
import androidx.preference.CheckBoxPreference
import androidx.preference.PreferenceViewHolder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.vva.androidopencbt.R

@Suppress("unused")
class SwitchProgressPreference @JvmOverloads constructor(
        context: Context,
        attributeSet: AttributeSet,
        defStyleAttr: Int = R.attr.switchPreferenceCompatStyle
): CheckBoxPreference(context, attributeSet, defStyleAttr) {
    init {
        widgetLayoutResource = R.layout.setting_widget_switch_progress
    }

    private lateinit var sw: SwitchCompat
    private lateinit var pg: ProgressBar
    private lateinit var ib: ImageButton

    private var listener: View.OnClickListener? = null

    var isInProgress: Boolean = false
        set(value) {
            if (value) {
                setInProgress()
            } else {
                setNotInProgress()
            }

            field = value
        }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        sw = holder.itemView.findViewById(android.R.id.checkbox)
        pg = holder.itemView.findViewById(R.id.progress) as ProgressBar
        ib = holder.itemView.findViewById(R.id.cancel_btn)

        ib.setOnClickListener(listener)
    }

    fun setOnCancelClickListener(listener: View.OnClickListener) {
        this.listener = listener
    }

    private fun setInProgress() {
        sw.visibility = View.GONE
        pg.visibility = View.VISIBLE
        ib.visibility = View.VISIBLE

        notifyChanged()
    }

    private fun setNotInProgress() {
        sw.visibility = View.VISIBLE
        pg.visibility = View.GONE
        ib.visibility = View.GONE

        notifyChanged()
    }

    fun setProgress(max: Int, progress: Int, indeterminate: Boolean) {
        if (pg.isIndeterminate != indeterminate) {
            pg.visibility = View.GONE
            pg.isIndeterminate = indeterminate
            pg.visibility = View.VISIBLE
        }

        if (!indeterminate) {
            pg.max = max
            pg.progress = progress
        }

        notifyChanged()
    }
}