package com.lcl.lclmeasurementtool

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.lcl.lclmeasurementtool.databinding.SignalDataFragmentBinding
import com.lcl.lclmeasurementtool.model.viewmodels.SignalStrengthViewModel

class SignalStrengthFragment: Fragment() {

    private var _binding: SignalDataFragmentBinding? = null

    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView

    private val viewModel: SignalStrengthViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }

        ViewModelProvider(this, SignalStrengthViewModel.Factory(activity.application))[SignalStrengthViewModel::class.java]

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


}