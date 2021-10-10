package com.lcl.lclmeasurementtool;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.lcl.lclmeasurementtool.Database.Entity.Connectivity;
import com.lcl.lclmeasurementtool.Database.Entity.ConnectivityViewModel;
import com.lcl.lclmeasurementtool.Database.Entity.SignalStrength;
import com.lcl.lclmeasurementtool.databinding.ConnectivityDataFragmentBinding;
import com.lcl.lclmeasurementtool.databinding.SignalDataFragmentBinding;

import java.util.List;

public class ConnectivityDataFragment extends Fragment {

    private ConnectivityDataFragmentBinding binding;
    private Context context;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ConnectivityDataFragmentBinding.inflate(inflater, container, false);
        this.context = getContext();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ConnectivityViewModel mConnectivityViewModel = new ViewModelProvider(requireActivity()).get(ConnectivityViewModel.class);
        mConnectivityViewModel.getAllConnectivityResults().observe(getViewLifecycleOwner(), connectivities -> {
            connectivities.forEach(c -> {

            });
        });
    }

//    private TableRow setupTableRow(Connectivity c) {
//        TableRow r = new TableRow(context);
////        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams();
////        layoutParams.setMargins(5,5,5,5);
////        r.setLayoutParams(layoutParams);
//
//        String[] d = new String[]{s.getTimestamp(), String.valueOf(s.getSignalStrength()), String.valueOf(s.getLevel())};
//        TextView[] tvs = new TextView[d.length];
//        for (int i = 0; i < d.length; i++) {
//            tvs[i] = setupTextView(d[i]);
//        }
//
//        for (TextView tv : tvs) {
//            r.addView(tv);
//        }
//
//        return r;
//    }

    private TextView setupTextView(String s) {
        TextView tv = new TextView(context);

        tv.setText(s);
        tv.setTypeface(Typeface.MONOSPACE);
        tv.setTextSize(15);
        tv.setTextColor(ContextCompat.getColor(context, R.color.white));
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        return tv;
    }
}
