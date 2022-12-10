package com.lcl.lclmeasurementtool

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.lcl.lclmeasurementtool.databinding.SignalDataCardTemplateBinding
import com.lcl.lclmeasurementtool.databinding.SignalDataFragmentBinding
import com.lcl.lclmeasurementtool.model.viewmodel.SignalStrengthViewModel
import com.lcl.lclmeasurementtool.model.viewmodel.SignalStrengthViewModelFactory

class SignalStrengthFragment: Fragment() {

    private var _binding: SignalDataFragmentBinding? = null

    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView

    private val viewModel: SignalStrengthViewModel by activityViewModels {
        SignalStrengthViewModelFactory(
            (activity?.application as SignalStrengthApplication).database.signalStrengthDao()
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


}