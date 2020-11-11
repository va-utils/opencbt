package com.vva.androidopencbt.recordslist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vva.androidopencbt.R
import com.vva.androidopencbt.db.DbRecord
import com.vva.androidopencbt.getDateTimeString

class RecordsAdapter(private val listener: RecordListener, private val scrollListener: ScrollListener): ListAdapter<DbRecord, RecordsAdapter.RecordsViewHolder>(DiffCallback()){
    private var order: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordsViewHolder {
        return RecordsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false))
    }

    override fun onBindViewHolder(holder: RecordsViewHolder, position: Int) {
        holder.bind(getItem(position), listener)
    }

    override fun onCurrentListChanged(previousList: MutableList<DbRecord>, currentList: MutableList<DbRecord>) {
        super.onCurrentListChanged(previousList, currentList)
        if (currentList.size > previousList.size && previousList.isNotEmpty()) {
            scrollListener.scroll(order)
        }
    }

    fun updateList(list: List<DbRecord>, order: Int) {
        submitList(list)
        this.order = order
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

        private val res = itemView.resources

        fun bind(record: DbRecord, onClickListener: RecordListener) {
            itemView.setOnClickListener {
                onClickListener.onClick(record)
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

                if (thoughts.isEmpty()) {
                    thoughtTextView.visibility = View.GONE
                } else {
                    thoughtTextView.text = res.getString(R.string.adapter_thought, thoughts)
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



    class DiffCallback: DiffUtil.ItemCallback<DbRecord>() {
        override fun areItemsTheSame(oldItem: DbRecord, newItem: DbRecord): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DbRecord, newItem: DbRecord): Boolean {
            return oldItem == newItem
        }
    }
}

class RecordListener(val clickListener: (record: DbRecord) -> Unit) {
    fun onClick(record: DbRecord) = clickListener(record)
}

class ScrollListener(val scrollListener: (orderBy: Int) -> Unit) {
    fun scroll(orderBy: Int) = scrollListener(orderBy)
}