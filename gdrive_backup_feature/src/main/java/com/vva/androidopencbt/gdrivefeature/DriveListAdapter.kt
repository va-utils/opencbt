package com.vva.androidopencbt.gdrivefeature

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.api.services.drive.model.File

class DriveListAdapter(val clickListener: OnClickListener): ListAdapter<File, DriveListAdapter.DriveViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DriveViewHolder {
        return DriveViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false))
    }

    override fun onBindViewHolder(holder: DriveViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class DriveViewHolder(val view: View): RecyclerView.ViewHolder(view) {
        private val icon: ImageView = view.findViewById(R.id.icon)
        private val name: TextView = view.findViewById(R.id.file_name_tv)
        private val size: TextView = view.findViewById(R.id.file_size_tv)
        private val date: TextView = view.findViewById(R.id.file_date_tv)

        fun bind(item: File, listener: OnClickListener) {
            view.setOnClickListener {
                listener.onClick(item)
            }
//            icon.setImageResource(R.drawable.ic_baseline_select_all_24)
            name.text = item.name
            size.text = item.size.toString()
//            date.text = item.createdTime.toStringRfc3339()
        }
    }
}

class DiffCallback: DiffUtil.ItemCallback<File>() {
    override fun areItemsTheSame(oldItem: File, newItem: File): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: File, newItem: File): Boolean {
        return oldItem == newItem
    }
}

class OnClickListener(val listener: (file: File) -> Unit) {
    fun onClick(file: File) = listener(file)
}