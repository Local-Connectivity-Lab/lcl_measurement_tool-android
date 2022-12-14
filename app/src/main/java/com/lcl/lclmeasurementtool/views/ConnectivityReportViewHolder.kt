package com.lcl.lclmeasurementtool.views

import com.lcl.lclmeasurementtool.databinding.ConnectivityDataCardTemplateBinding
import com.lcl.lclmeasurementtool.model.datamodel.ConnectivityReportModel

class ConnectivityReportViewHolder(private var binding: ConnectivityDataCardTemplateBinding):
    AbstractViewHolder<ConnectivityReportModel>(binding) {
    override fun bind(data: ConnectivityReportModel) {
        binding.connectivityUpload.text = data.uploadSpeed.toString()
        binding.connectivityDownload.text = data.downloadSpeed.toString()
        binding.connectivityDate.text = data.timestamp
    }
}