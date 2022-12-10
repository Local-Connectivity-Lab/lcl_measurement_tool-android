package com.lcl.lclmeasurementtool

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lcl.lclmeasurementtool.databinding.ConnectivityDataCardTemplateBinding
import com.lcl.lclmeasurementtool.model.datamodel.ConnectivityReportModel

class ConnectivityFragment(private val onItemClicked: (ConnectivityReportModel) -> Unit):
ListAdapter<ConnectivityReportModel, ConnectivityFragment.ConnectivityReportViewHolder>(DiffCallback){
    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<ConnectivityReportModel>() {
            override fun areItemsTheSame(
                oldItem: ConnectivityReportModel,
                newItem: ConnectivityReportModel
            ): Boolean {
                return oldItem.timestamp == newItem.timestamp
            }

            override fun areContentsTheSame(
                oldItem: ConnectivityReportModel,
                newItem: ConnectivityReportModel
            ): Boolean {
                return oldItem == newItem
            }

        }
    }


    class ConnectivityReportViewHolder(private var binding: ConnectivityDataCardTemplateBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(connectivity: ConnectivityReportModel) {
            binding.connectivityUpload.text = connectivity.uploadSpeed.toString()
            binding.connectivityDownload.text = connectivity.downloadSpeed.toString()
            binding.connectivityDate.text = connectivity.timestamp
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ConnectivityReportViewHolder {
        val viewHolder = ConnectivityReportViewHolder(
            ConnectivityDataCardTemplateBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.adapterPosition
            onItemClicked(getItem(position))
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: ConnectivityReportViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}