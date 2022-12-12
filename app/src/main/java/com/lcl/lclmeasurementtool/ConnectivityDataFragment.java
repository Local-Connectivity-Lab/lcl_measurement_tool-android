//package com.lcl.lclmeasurementtool;
//
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.cardview.widget.CardView;
//import androidx.core.content.ContextCompat;
//import androidx.fragment.app.Fragment;
//import androidx.lifecycle.ViewModelProvider;
//
//import com.kongzue.dialogx.dialogs.BottomDialog;
//import com.kongzue.dialogx.util.TextInfo;
//import com.lcl.lclmeasurementtool.database.Entity.Connectivity;
//import com.lcl.lclmeasurementtool.database.Entity.ConnectivityViewModel;
//import com.lcl.lclmeasurementtool.databinding.ConnectivityDataFragmentBinding;
//
//import java.util.Formatter;
//
///**
// * A fragment support displaying connectivity measurement data
// */
//public class ConnectivityDataFragment extends Fragment {
//
//    private ConnectivityDataFragmentBinding binding;
//
//    private static final String mbps = " mbps";
//    private static final String ms = " ms";
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        binding = ConnectivityDataFragmentBinding.inflate(inflater, container, false);
//        return binding.getRoot();
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        ConnectivityViewModel mConnectivityViewModel = new ViewModelProvider(requireActivity()).get(ConnectivityViewModel.class);
////        Connectivity[] cc = new Connectivity[] {
////                new Connectivity("2022-05-20T06:19:33.912082Z", 1, 2, 3, new LatLng(0,0)),
////                new Connectivity("2022-05-20T06:19:33.912", 1, 2, 3, new LatLng(0,0)),
////                new Connectivity("2022-05-20T06:19:33.", 1, 2, 3, new LatLng(0,0)),
////                new Connectivity("2022-05-20T06:19:33", 1, 2, 3, new LatLng(0,0))
////        };
////
////        for (Connectivity c:cc) {
////            binding.dataListLinearLayout.addView(setupRow(c));
////        }
//
//        mConnectivityViewModel.getAll().observe(getViewLifecycleOwner(), connectivities -> {
//            connectivities.stream().distinct().forEach(c -> {
//                binding.dataListLinearLayout.addView(setupRow(c));
//            });
//        });
//    }
//
//    /**
//     * Set up cardview for each given connectivity measurement
//     * @param c given signal strength
//     * @return a cardview instance for the row
//     */
//    private CardView setupRow(Connectivity c) {
//        CardView row = (CardView) getLayoutInflater().inflate(R.layout.connectivity_data_card_template, null);
//        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        layoutParams.leftMargin = 20;
//        layoutParams.rightMargin = 20;
//        layoutParams.bottomMargin = 40;
//        row.setLayoutParams(layoutParams);
//
//        TextView tvDate = row.findViewById(R.id.connectivity_date);
//        tvDate.setText(c.getTimestamp());
//        TextView tvUpload = row.findViewById(R.id.connectivity_upload);
//        String uploadSpeedString = formatSpeedData(c.getUpload()) + mbps;
//        tvUpload.setText(uploadSpeedString);
//
//        TextView tvDownload = row.findViewById(R.id.connectivity_download);
//        String downloadSpeedString = formatSpeedData(c.getDownload()) + mbps;
//        tvDownload.setText(downloadSpeedString);
//
//        TextInfo textInfo = new TextInfo();
//        int fontColor = ContextCompat.getColor(binding.getRoot().getContext(), R.color.white);
//        int backgroundColor = ContextCompat.getColor(binding.getRoot().getContext(), R.color.dark_gray);
//        textInfo.setFontColor(fontColor);
//        textInfo.setBold(true);
//        String sb = "Ping: " +
//                c.getPing() + ms +
//                "\n" +
//                "Upload Speed: " +
//                c.getUpload() + mbps +
//                "\n" +
//                "Download Speed: " +
//                c.getDownload() + mbps +
//                "\n" +
//                "Time: " +
//                c.getTimestamp() +
//                "\n" + c.getLocation();
//
//        row.setOnClickListener(v -> BottomDialog.show("Speed Measurement Data Collected on " + c.getTimestamp(), sb)
//                .setBackgroundColor(backgroundColor)
//                .setTitleTextInfo(textInfo)
//                .setMessageTextInfo(textInfo));
//
//        return row;
//    }
//
//    private String formatSpeedData(double speed) {
//        return new Formatter().format("%.2f", speed).toString();
//    }
//}
