package com.vva.androidopencbt.recordslist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.vva.androidopencbt.R
import com.vva.androidopencbt.db.DbRecord
import com.vva.androidopencbt.getDateHeaderString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecordsAdapter(private val listener: RecordListener, private val longListener: RecordLongListener): ListAdapter<DataItem, AbstractRecordViewHolder>(DiffContCallback()){
    var quotes = false
    var dividers = true
    var intensityIndication = false

    private val adapterScope = CoroutineScope(Dispatchers.Default)
    private val selectionSet = HashSet<DbRecord>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbstractRecordViewHolder {
        return when (viewType) {
            AbstractRecordViewHolder.DATE_HEADER_TYPE -> {
                RecordsDateHeaderViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.list_date_header, parent, false))
            }
            AbstractRecordViewHolder.RECORD_ITEM_TYPE -> {
                RecordsBindingViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.list_item, parent, false))
            }
            else -> {
                throw IllegalArgumentException("Unknown view type $viewType")
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.Header -> {
                AbstractRecordViewHolder.DATE_HEADER_TYPE
            }
            is DataItem.Record -> {
                AbstractRecordViewHolder.RECORD_ITEM_TYPE
            }
            else -> {
                throw IllegalArgumentException("Unknown viewType")
            }
        }
    }

    fun setList(list: List<DbRecord>?) {
        if (list == null)
            super.submitList(list)

        val newList = mutableListOf<DataItem>()
        adapterScope.launch {
            if (list?.isNotEmpty() == true) {
                var currentDate = list[0].datetime
                newList.add(DataItem.Header(currentDate.getDateHeaderString()))
                list.forEach {
                    if (it.datetime.year != currentDate.year
                        || it.datetime.monthOfYear != currentDate.monthOfYear
                        || it.datetime.dayOfMonth != currentDate.dayOfMonth) {
                            currentDate = it.datetime
                            newList.add(DataItem.Header(currentDate.getDateHeaderString()))
                    }
                    newList.add(DataItem.Record(it))
                }
            }
            withContext(Dispatchers.Main) {
                super.submitList(newList)
            }
        }
    }

    override fun onBindViewHolder(holder: AbstractRecordViewHolder, position: Int) {
        when (val viewType = getItemViewType(position)) {
            AbstractRecordViewHolder.DATE_HEADER_TYPE -> {
                (holder as RecordsDateHeaderViewHolder).bind((getItem(position) as DataItem.Header).date)
            }
            AbstractRecordViewHolder.RECORD_ITEM_TYPE -> {
                val args = RecordsBindingViewHolder.Args(getItem(position) as DataItem.Record, listener, longListener, quotes, intensityIndication, dividers, position, selectionSet)
                (holder as RecordsBindingViewHolder).bind(args)
            }
            else -> {
                throw IllegalArgumentException("Unknown view type $viewType")
            }
        }
    }

    fun setItemSelection(record: DbRecord) {
        if (selectionSet.contains(record)) {
            selectionSet.remove(record)
        } else {
            selectionSet.add(record)
        }
        notifyDataSetChanged()
    }

    fun selectAll() {
        currentList.forEach {
            if (it is DataItem.Record) {
                selectionSet.add(it.record)
            }
        }
        notifyDataSetChanged()
    }

    fun deselectAll() {
        selectionSet.clear()
        notifyDataSetChanged()
    }

    fun getList(): List<DbRecord> {
        return selectionSet.toList()
    }

    class DiffCallback: DiffUtil.ItemCallback<DbRecord>() {
        override fun areItemsTheSame(oldItem: DbRecord, newItem: DbRecord): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DbRecord, newItem: DbRecord): Boolean {
            return oldItem == newItem
        }
    }

    class DiffContCallback: DiffUtil.ItemCallback<DataItem>() {
        override fun areItemsTheSame(
            oldItem: DataItem,
            newItem: DataItem
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: DataItem,
            newItem: DataItem
        ): Boolean {
            return oldItem == newItem
        }
    }
}

sealed class DataItem {
    abstract val id: Long

    data class Record(val record: DbRecord): DataItem() {
        override val id = record.id
    }
    data class Header(val date: String): DataItem() {
        override val id: Long = date.hashCode().toLong()
    }
}

class RecordListener(val clickListener: (view: View, record: DbRecord, position: Int) -> Unit) {
    fun onClick(view: View, record: DbRecord, position: Int) = clickListener(view, record, position)
}

class RecordLongListener(val clickListener: (view: View, record: DbRecord, position: Int) -> Boolean) {
    fun onClick(view: View, record: DbRecord, position: Int) = clickListener(view, record, position)
}