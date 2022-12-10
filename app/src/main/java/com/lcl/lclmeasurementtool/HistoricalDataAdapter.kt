package com.lcl.lclmeasurementtool

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.lcl.lclmeasurementtool.views.AbstractViewHolder

open class HistoricalDataAdapter<DataModel, ViewHolder : AbstractViewHolder<DataModel>>(
    private val createCustomViewHolder: (parent: ViewGroup, viewType: Int) -> ViewHolder,
    diffCallback: DiffUtil.ItemCallback<DataModel>):
    ListAdapter<DataModel, ViewHolder>(diffCallback){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return createCustomViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}