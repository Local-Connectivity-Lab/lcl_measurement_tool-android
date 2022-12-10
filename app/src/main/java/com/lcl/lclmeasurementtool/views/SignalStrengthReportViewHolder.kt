package com.lcl.lclmeasurementtool.views

import com.lcl.lclmeasurementtool.databinding.SignalDataCardTemplateBinding
import com.lcl.lclmeasurementtool.model.datamodel.SignalStrengthReportModel

class SignalStrengthReportViewHolder(private var binding: SignalDataCardTemplateBinding):
    AbstractViewHolder<SignalStrengthReportModel>(binding) {
    override fun bind(data: SignalStrengthReportModel) {
        binding.`val`.text = data.dbm.toString()
        binding.date.text = data.timestamp
    }
}