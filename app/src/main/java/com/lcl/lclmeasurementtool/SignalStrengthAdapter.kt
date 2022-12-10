package com.lcl.lclmeasurementtool

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lcl.lclmeasurementtool.databinding.SignalDataCardTemplateBinding
import com.lcl.lclmeasurementtool.model.datamodel.SignalStrengthReportModel


// TODO: combine signal strength and connectivity to the same adapter interface
class SignalStrengthAdapter(private val onItemClicked: (SignalStrengthReportModel) -> Unit):
    ListAdapter<SignalStrengthReportModel, SignalStrengthAdapter.SignalStrengthReportViewHolder>(DiffCallback){

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<SignalStrengthReportModel>() {
            override fun areItemsTheSame(
                oldItem: SignalStrengthReportModel,
                newItem: SignalStrengthReportModel
            ): Boolean {
                return oldItem.timestamp == newItem.timestamp
            }

            override fun areContentsTheSame(
                oldItem: SignalStrengthReportModel,
                newItem: SignalStrengthReportModel
            ): Boolean {
                return oldItem == newItem
            }

        }
    }


    class SignalStrengthReportViewHolder(private var binding: SignalDataCardTemplateBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(signalStrength: SignalStrengthReportModel) {
            binding.`val`.text = signalStrength.dbm.toString()
            binding.date.text = signalStrength.timestamp
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SignalStrengthReportViewHolder {
        val viewHolder = SignalStrengthReportViewHolder(
            SignalDataCardTemplateBinding.inflate(
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

    override fun onBindViewHolder(holder: SignalStrengthReportViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}