package com.vva.androidopencbt.recordslist

import android.view.View
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.vva.androidopencbt.R
import com.vva.androidopencbt.databinding.ListDateHeaderBinding
import com.vva.androidopencbt.databinding.ListItemBinding
import com.vva.androidopencbt.db.DbRecord
import com.vva.androidopencbt.getDateTimeString

abstract class AbstractRecordViewHolder(private val binding: ViewDataBinding): RecyclerView.ViewHolder(binding.root) {
    companion object {
        const val DATE_HEADER_TYPE = 1
        const val RECORD_ITEM_TYPE = 2
    }
}

class RecordsDateHeaderViewHolder(private val binding: ListDateHeaderBinding): AbstractRecordViewHolder(binding) {
    private val header = binding.tvDateHeader

    fun bind(date: String) {
        header.text = date
    }
}

class RecordsBindingViewHolder(private val binding: ListItemBinding): AbstractRecordViewHolder(binding) {
    fun bind(args: Args) {
        binding.apply {
            val rec = args.record.record
            this.record = args.record.record
            cardView.setOnClickListener {
                args.onClickListener.onClick(it, rec, position)
            }
            cardView.setOnLongClickListener {
                args.onLongListener.onClick(it, rec, position)
            }
            dateTextView.text = rec.datetime.getDateTimeString()

            itemLl.showDividers = if (args.dividers) {
                LinearLayout.SHOW_DIVIDER_BEGINNING or LinearLayout.SHOW_DIVIDER_MIDDLE
            } else {
                LinearLayout.SHOW_DIVIDER_NONE
            }

            if (args.selectionSet.contains(rec)) {
                cardView.background.setTint(ResourcesCompat.getColor(root.resources, R.color.list_selection_color, null))
            } else {
                cardView.background.setTintList(null)
            }

            if (args.indication) {
                root.setPadding(0, 1, 0, 0)
                cardView.shapeAppearanceModel = cardView.shapeAppearanceModel.toBuilder()
                    .setTopLeftCornerSize(1F * root.resources.getDimension(R.dimen.reply_small_component_corner_radius))
                    .build()
                root.setBackgroundResource(when (rec.intensity) {
                    0 -> R.color.intensity_zero
                    in 1..30 -> R.color.intensity_low
                    in 31..60 -> R.color.intensity_mid
                    in 61..90 -> R.color.intensity_mid_high
                    else -> R.color.intensity_high
                })
            } else {
                root.setPadding(0, 0, 0, 0)
            }

//            intensityTextView.text = root.resources.getString(R.string.adapter_intensity, record.intensity)
        }
    }

    data class Args(val record: DataItem.Record, val onClickListener: RecordListener,
                    val onLongListener: RecordLongListener, val quotes: Boolean,
                    val indication: Boolean, val dividers : Boolean, val position: Int, val selectionSet: Set<DbRecord>)

    fun interface ClickListener {
        fun onClick(view: View, dbRecord: DbRecord)
    }

    fun interface LongClickListener {
        fun onLongClick()
    }
}