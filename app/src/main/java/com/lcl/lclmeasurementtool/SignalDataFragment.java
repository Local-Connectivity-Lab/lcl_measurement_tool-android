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

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.card.MaterialCardView;
import com.kongzue.dialogx.dialogs.BottomDialog;
import com.kongzue.dialogx.interfaces.OnBindView;
import com.kongzue.dialogx.util.TextInfo;
import com.lcl.lclmeasurementtool.Database.Entity.EntityEnum;
import com.lcl.lclmeasurementtool.Database.Entity.SignalStrength;
import com.lcl.lclmeasurementtool.Database.Entity.SignalViewModel;
import com.lcl.lclmeasurementtool.databinding.SignalDataFragmentBinding;

import java.util.Arrays;

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
//        SignalStrength[] ss = new SignalStrength[] {
//                new SignalStrength("2022-05-20T06:19:33.912082Z", 65, 1, new LatLng(0,0)),
//                new SignalStrength("2022-05-20T06:19:33.912082", 65, 1, new LatLng(0,0)),
//                new SignalStrength("2022-05-20T06:19:33.9120", 65, 1, new LatLng(0,0)),
//                new SignalStrength("2022-05-20T06:19:33", 65, 1, new LatLng(0,0)),
//        };
//        for (SignalStrength s : ss) {
//            binding.dataListLinearLayout.addView(setupRow(s));
//        }
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
        layoutParams.leftMargin = 20;
        layoutParams.rightMargin = 20;
        layoutParams.bottomMargin = 40;
        row.setLayoutParams(layoutParams);
        TextView tvVal = row.findViewById(R.id.val);
        tvVal.setText(String.valueOf(s.getSignalStrength()));
        TextView tvDate = row.findViewById(R.id.date);
        tvDate.setText(s.getTimestamp());
        TextInfo textInfo = new TextInfo();
        int fontColor = ContextCompat.getColor(binding.getRoot().getContext(), R.color.white);
        int backgroundColor = ContextCompat.getColor(binding.getRoot().getContext(), R.color.dark_gray);
        textInfo.setFontColor(fontColor);
        textInfo.setBold(true);
        StringBuilder sb = new StringBuilder();
        sb.append("Signal Strength: ")
                .append(s.getSignalStrength()).append(" dBm")
                .append("\n")
                .append("Level: ")
                .append(s.getLevel())
                .append("\n")
                .append("Time: ")
                .append(s.getTimestamp())
                .append("\n").append(s.getLocation());
        row.setOnClickListener(v -> {
            BottomDialog.show("Signal Strength Data Collected on " + s.getTimestamp(), sb.toString())
                    .setBackgroundColor(backgroundColor)
                    .setTitleTextInfo(textInfo)
                    .setMessageTextInfo(textInfo);
        });
        return row;
    }
}
