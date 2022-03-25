package com.lcl.lclmeasurementtool;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.lcl.lclmeasurementtool.Database.Entity.Connectivity;
import com.lcl.lclmeasurementtool.Database.Entity.ConnectivityViewModel;
import com.lcl.lclmeasurementtool.Database.Entity.SignalStrength;
import com.lcl.lclmeasurementtool.databinding.ConnectivityDataFragmentBinding;
import com.lcl.lclmeasurementtool.databinding.SignalDataFragmentBinding;

import java.util.Formatter;
import java.util.List;
import java.util.Locale;

/**
 * A fragment support displaying connectivity measurement data
 */
public class ConnectivityDataFragment extends Fragment {

    private ConnectivityDataFragmentBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ConnectivityDataFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ConnectivityViewModel mConnectivityViewModel = new ViewModelProvider(requireActivity()).get(ConnectivityViewModel.class);
        mConnectivityViewModel.getAll().observe(getViewLifecycleOwner(), connectivities -> {
            connectivities.stream().distinct().forEach(c -> {
                binding.dataListLinearLayout.addView(setupRow(c));
            });
        });
    }

    /**
     * Set up cardview for each given connectivity measurement
     * @param c given signal strength
     * @return a cardview instance for the row
     */
    private CardView setupRow(Connectivity c) {
        CardView row = (CardView) getLayoutInflater().inflate(R.layout.connectivity_data_card_template, null);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = 15;
        layoutParams.rightMargin = 15;
        layoutParams.bottomMargin = 20;
        row.setLayoutParams(layoutParams);

        TextView tvDate = row.findViewById(R.id.connectivity_date);
        tvDate.setText(c.getTimestamp());
        TextView tvUpload = row.findViewById(R.id.connectivity_upload);
        tvUpload.setText(new Formatter().format("%.2f", c.getUpload()).toString());

        TextView tvDownload = row.findViewById(R.id.connectivity_download);
        tvDownload.setText(new Formatter().format("%.2f", c.getDownload()).toString());

        TextView tvPing = row.findViewById(R.id.connectivity_ping);
        tvPing.setText(new Formatter().format("%.2f", c.getPing()).toString());
        return row;
    }
}
