package com.vva.androidopencbt.recordslist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.vva.androidopencbt.R
import com.vva.androidopencbt.db.DbRecord

class RecordsAdapter constructor(private val listener: RecordListener, private val longListener: RecordLongListener): ListAdapter<DbRecord, RecordsAdapter.RecordsViewHolder>(DiffCallback()){
    var quotes = false
    var dividers = true
    var intensityIndication = false

    private var _selectedItems = HashMap<DbRecord, Boolean>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordsViewHolder {
        return RecordsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false))
    }

    override fun onBindViewHolder(holder: RecordsViewHolder, position: Int) {
        holder.bind(getItem(position), listener, longListener, quotes, intensityIndication, dividers, position, _selectedItems)
    }

    class RecordsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        private val situationTextView: TextView = itemView.findViewById(R.id.situationTextView)
        private val thoughtTextView: TextView = itemView.findViewById(R.id.thoughtTextView)
        private val disputTextView: TextView = itemView.findViewById(R.id.rationalTextView)
        private val emotionTextView: TextView = itemView.findViewById(R.id.emotionTextView)
        private val feelingsTextView: TextView = itemView.findViewById(R.id.feelingsTextView)
        private val actionsTextView: TextView = itemView.findViewById(R.id.actionsTextView)
        private val distortionTextView: TextView = itemView.findViewById(R.id.distortionTextView)
        private val intensityTextView: TextView = itemView.findViewById(R.id.intensityTextView)
        private val cardView: MaterialCardView = itemView.findViewById(R.id.card_view)
        private val ll : LinearLayout  = itemView.findViewById(R.id.item_ll)
        private val res = itemView.resources

        fun bind(record: DbRecord, onClickListener: RecordListener, onLongListener: RecordLongListener,
                 quotes: Boolean, indication: Boolean, dividers : Boolean, position: Int, selection: HashMap<DbRecord, Boolean>) {
            cardView.setOnClickListener {
                onClickListener.onClick(it, record, position)
            }

            cardView.setOnLongClickListener {
                onLongListener.onClick(it, record, position)
            }

            cardView.transitionName = "${res.getString(R.string.record_card_view_detail_transition_name)}(${record.id})"

            if (dividers)
                ll.showDividers = LinearLayout.SHOW_DIVIDER_BEGINNING or LinearLayout.SHOW_DIVIDER_MIDDLE
            else
                ll.showDividers = LinearLayout.SHOW_DIVIDER_NONE

            if (selection[record] == true) {
                cardView.background.setTint(ResourcesCompat.getColor(res, R.color.list_selection_color, null))
            } else {
                cardView.background.setTintList(null)
            }

            dateTextView.text = record.datetime.getDateTimeString()
            record.apply {
                distortions.let {
                    if (it == 0x0) {
                        distortionTextView.visibility = View.GONE
                        return@let
                    }
                    distortionTextView.visibility = View.VISIBLE
                    val builder = StringBuilder()
                    if (it.and(DbRecord.ALL_OR_NOTHING) != 0) builder.append(res.getString(R.string.dist_all_or_nothing)).append(", ")
                    if (it.and(DbRecord.OVERGENERALIZING) != 0) builder.append(res.getString(R.string.dist_overgeneralizing)).append(", ")
                    if (it.and(DbRecord.FILTERING) != 0) builder.append(res.getString(R.string.dist_filtering)).append(", ")
                    if (it.and(DbRecord.DISQUAL_POSITIVE) != 0) builder.append(res.getString(R.string.dist_disqual_positive)).append(", ")
                    if (it.and(DbRecord.JUMP_CONCLUSION) != 0) builder.append(res.getString(R.string.dist_jump_conclusion)).append(", ")
                    if (it.and(DbRecord.MAGN_AND_MIN) != 0) builder.append(res.getString(R.string.dist_magn_and_min)).append(", ")
                    if (it.and(DbRecord.EMOTIONAL_REASONING) != 0) builder.append(res.getString(R.string.dist_emotional_reasoning)).append(", ")
                    if (it.and(DbRecord.MUST_STATEMENTS) != 0) builder.append(res.getString(R.string.dist_must_statement)).append(", ")
                    if (it.and(DbRecord.LABELING) != 0) builder.append(res.getString(R.string.dist_labeling)).append(", ")
                    if (it.and(DbRecord.PERSONALIZATION) != 0) builder.append(res.getString(R.string.dist_personalistion)).append(", ")

                    distortionTextView.text = builder.substring(0, builder.length - 2).toString()
                }

                if (situation.isEmpty()) {
                    situationTextView.visibility = View.GONE
                } else {
                    situationTextView.visibility = View.VISIBLE
                    situationTextView.text = res.getString(R.string.adapter_situation, situation)
                }

                if (intensity == 0) {
                    intensityTextView.visibility = View.GONE
                } else {
                    intensityTextView.visibility = View.VISIBLE
                    intensityTextView.text = res.getString(R.string.adapter_intensity, intensity)
                }
                if (indication) {
                    itemView.setPadding(0, 1, 0, 0)
                    cardView.apply {
                        shapeAppearanceModel = shapeAppearanceModel.toBuilder()
                                .setTopLeftCornerSize(1F * itemView.resources.getDimension(R.dimen.reply_small_component_corner_radius))
                                .build()
                    }
                    itemView.setBackgroundResource(when (intensity) {
                        0 -> R.color.intensity_zero
                        in 1..30 -> R.color.intensity_low
                        in 31..60 -> R.color.intensity_mid
                        in 61..90 -> R.color.intensity_mid_high
                        else -> R.color.intensity_high
                    })
                } else {
                    itemView.setPadding(0, 0, 0, 0)
                }

                when  {
                    thoughts.isEmpty() -> {
                        thoughtTextView.visibility = View.GONE
                    }
                    quotes -> {
                        thoughtTextView.text = res.getString(R.string.adapter_thought, "\"$thoughts\"")
                    }
                    else -> {
                        thoughtTextView.text = res.getString(R.string.adapter_thought, thoughts)
                    }

                }

                if (emotions.isEmpty()) {
                    emotionTextView.visibility = View.GONE
                } else {
                    emotionTextView.visibility = View.VISIBLE
                    emotionTextView.text = res.getString(R.string.adapter_emotions, emotions)
                }

                if (feelings.isEmpty()) {
                    feelingsTextView.visibility = View.GONE
                } else {
                    feelingsTextView.visibility = View.VISIBLE
                    feelingsTextView.text = res.getString(R.string.adapter_feelsing, feelings)
                }

                if (actions.isEmpty()) {
                    actionsTextView.visibility = View.GONE
                } else {
                    actionsTextView.visibility = View.VISIBLE
                    actionsTextView.text = res.getString(R.string.adapter_actions, actions)
                }

                if (rational.isEmpty()) {
                    disputTextView.visibility = View.GONE
                } else {
                    disputTextView.visibility = View.VISIBLE
                    disputTextView.text = res.getString(R.string.adapter_disput, rational)
                }
            }
        }
    }

    fun submitSelectionArray(map: HashMap<DbRecord, Boolean>) {
        _selectedItems = map
        notifyDataSetChanged()
    }

    fun getList(): List<DbRecord> {
        return currentList
    }

    class DiffCallback: DiffUtil.ItemCallback<DbRecord>() {
        override fun areItemsTheSame(oldItem: DbRecord, newItem: DbRecord): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DbRecord, newItem: DbRecord): Boolean {
            return oldItem == newItem
        }
    }
}

class RecordListener constructor(val clickListener: (view: View, record: DbRecord, position: Int) -> Unit) {
    fun onClick(view: View, record: DbRecord, position: Int) = clickListener(view, record, position)
}

class RecordLongListener constructor(val clickListener: (view: View, record: DbRecord, position: Int) -> Boolean) {
    fun onClick(view: View, record: DbRecord, position: Int) = clickListener(view, record, position)
}