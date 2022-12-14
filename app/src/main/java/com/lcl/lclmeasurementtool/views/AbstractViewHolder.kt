package com.lcl.lclmeasurementtool.views

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class AbstractViewHolder<T>(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
    open fun bind(data: T){}
}