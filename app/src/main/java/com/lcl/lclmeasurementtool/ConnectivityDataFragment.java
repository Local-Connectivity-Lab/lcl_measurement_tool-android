package com.lcl.lclmeasurementtool;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.lcl.lclmeasurementtool.Database.Entity.ConnectivityViewModel;
import com.lcl.lclmeasurementtool.databinding.ConnectivityDataFragmentBinding;
import com.lcl.lclmeasurementtool.databinding.SignalDataFragmentBinding;

public class ConnectivityDataFragment extends Fragment {

    private ConnectivityDataFragmentBinding binding;
    private Context context;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ConnectivityDataFragmentBinding.inflate(inflater, container, false);
        this.context = getContext();
//        mConnectivityViewModel = new ViewModelProvider(requireActivity()).get(ConnectivityViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
