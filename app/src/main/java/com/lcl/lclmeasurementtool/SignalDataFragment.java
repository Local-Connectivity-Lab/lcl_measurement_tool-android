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
import androidx.lifecycle.ViewModelProvider;

import com.lcl.lclmeasurementtool.Database.Entity.EntityEnum;
import com.lcl.lclmeasurementtool.Database.Entity.SignalStrength;
import com.lcl.lclmeasurementtool.Database.Entity.SignalViewModel;
import com.lcl.lclmeasurementtool.databinding.SignalDataFragmentBinding;

/**
 * A fragment support displaying signal data
 */
public class SignalDataFragment extends Fragment {

    private SignalDataFragmentBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = SignalDataFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SignalViewModel mSignalViewModel = new ViewModelProvider(requireActivity()).get(SignalViewModel.class);
        mSignalViewModel.getAll().observe(getViewLifecycleOwner(),
                signalStrengths -> signalStrengths.stream().distinct().forEach(s -> binding.dataListLinearLayout.addView(setupRow(s))));
    }

    /**
     * Set up cardview for each given signal strength
     * @param s given signal strength
     * @return a cardview instance for the row
     */
    private CardView setupRow(SignalStrength s) {
        CardView row = (CardView) getLayoutInflater().inflate(R.layout.signal_data_card_template, null);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = 15;
        layoutParams.rightMargin = 15;
        layoutParams.bottomMargin = 20;
        row.setLayoutParams(layoutParams);
        TextView tvVal = row.findViewById(R.id.val);
        tvVal.setText(String.valueOf(s.getSignalStrength()));
        TextView tvCode = row.findViewById(R.id.code);
        tvCode.setText(String.valueOf(s.getLevel()));
        TextView tvDate = row.findViewById(R.id.date);
        tvDate.setText(s.getTimestamp());
        return row;
    }
}
