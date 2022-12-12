package com.lcl.lclmeasurementtool

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.lcl.lclmeasurementtool.databinding.SignalDataFragmentBinding
import com.lcl.lclmeasurementtool.model.repository.PreferencesRepository
import com.lcl.lclmeasurementtool.model.repository.dataStore
import com.lcl.lclmeasurementtool.model.viewmodels.SignalStrengthViewModel

class SignalStrengthFragment: Fragment() {

    private var _binding: SignalDataFragmentBinding? = null

    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView

//    private lateinit var preferences: PreferencesRepository

    private val viewModel: SignalStrengthViewModel by viewModels { SignalStrengthViewModel.Factory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        preferences = PreferencesRepository(requireContext().dataStore)
    }


}