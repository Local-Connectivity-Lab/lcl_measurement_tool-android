package com.lcl.lclmeasurementtool;

import android.content.Context;
import android.content.res.Resources;
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
import androidx.lifecycle.ViewModelProvider;

import com.lcl.lclmeasurementtool.Database.Entity.EntityEnum;
import com.lcl.lclmeasurementtool.Database.Entity.SignalStrength;
import com.lcl.lclmeasurementtool.Database.Entity.SignalViewModel;
import com.lcl.lclmeasurementtool.Utils.TimeUtils;
import com.lcl.lclmeasurementtool.databinding.SignalDataFragmentBinding;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class SignalDataFragment extends Fragment {

    private SignalDataFragmentBinding binding;
    public EntityEnum type;
    private Context context;
    private SignalViewModel mSignalViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = SignalDataFragmentBinding.inflate(inflater, container, false);
        this.context = getContext();
        mSignalViewModel = new ViewModelProvider(requireActivity()).get(SignalViewModel.class);


        List<SignalStrength> data = mSignalViewModel.getAllConnectivityResults().getValue();
        if (data != null) {
            for (SignalStrength s : data) {
                binding.dataList.addView( setupTableRow(s) );
            }
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private TableRow setupTableRow(SignalStrength s) {
        TableRow r = new TableRow(context);

        String[] d = new String[]{s.getTimestamp(), String.valueOf(s.getSignalStrength()), String.valueOf(s.getLevel())};
        TextView[] tvs = new TextView[d.length];
        for (int i = 0; i < d.length; i++) {
            tvs[i] = setupTextView(d[i]);
        }

        for (TextView tv : tvs) {
            r.addView(tv);
        }

        return r;
    }

    private TextView setupTextView(String s) {
        TextView tv = new TextView(context);
        tv.setText(s);
        tv.setTypeface(Typeface.MONOSPACE);
        tv.setTextSize(16);
        tv.setTextColor(ContextCompat.getColor(context, R.color.white));
        return tv;
    }
}
