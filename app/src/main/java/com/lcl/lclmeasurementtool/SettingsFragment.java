package com.lcl.lclmeasurementtool;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.kongzue.dialogx.dialogs.MessageDialog;
import com.kongzue.dialogx.dialogs.TipDialog;
import com.kongzue.dialogx.dialogs.WaitDialog;
import com.lcl.lclmeasurementtool.Database.Entity.AbstractViewModel;
import com.lcl.lclmeasurementtool.Database.Entity.Connectivity;
import com.lcl.lclmeasurementtool.Database.Entity.ConnectivityViewModel;
import com.lcl.lclmeasurementtool.Database.Entity.EntityEnum;
import com.lcl.lclmeasurementtool.Database.Entity.SignalStrength;
import com.lcl.lclmeasurementtool.Database.Entity.SignalViewModel;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String TAG = "SETTINGS";
    private static final String TOU = "https://seattlecommunitynetwork.org/";
    private static final String PRIVACY = "https://seattlecommunitynetwork.org/";
    private static final String EMAIL = "help@seattlecommunitynetwork.org";
    private static final String MIME_TYPE = "text/csv";

    private List<String[]> connectivityList;
    private List<String[]> signalStrengthList;

    private static final String CSV = ".csv";
    private Activity activity;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        Context context = this.getContext();

        activity = this.getActivity();
        if (context == null || activity == null) {
            forceQuit();
            return;
        }

        Preference signalCSV = findPreference("export_signal");
        if (signalCSV == null) {
            forceQuit();
            return;
        }

        signalCSV.setOnPreferenceClickListener(preference -> {
            generateCSV(EntityEnum.SIGNALSTRENGTH);
            return true;
        });

        Preference measurementCSV = findPreference("export_measurement");
        if (measurementCSV == null) {
            forceQuit();
            return;
        }
        measurementCSV.setOnPreferenceClickListener(preference -> {
            generateCSV(EntityEnum.CONNECTIVITY);
            return true;
        });


        Preference logout = findPreference("logout");
        if (logout != null) {
            logout.setOnPreferenceClickListener(preference -> {
                SharedPreferences preferences = activity.getPreferences(MODE_PRIVATE);
                preferences.edit().clear().apply();
                TipDialog.show("Logging out ...", WaitDialog.TYPE.SUCCESS, 1000);
                // TODO show login screen
                Intent out = new Intent(activity, MainActivity.class);
                activity.startActivity(out);
                activity.finish();
                return true;
            });
        }

        Preference tou = findPreference("tou");
        Preference privacy = findPreference("privacy");
        if (tou != null && privacy != null) {
            Intent touIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(TOU));
            Intent privacyIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY));
            tou.setIntent(touIntent);
            privacy.setIntent(privacyIntent);
        } else {
            forceQuit();
        }

        Preference feedback = findPreference("feedback");
        if (feedback != null) {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + EMAIL));
            intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Feedback");
            intent.putExtra(android.content.Intent.EXTRA_TEXT, "Let us know what you think!");
            feedback.setIntent(intent);
        } else {
            forceQuit();
        }

        CheckBoxPreference showData = findPreference("show_data");
        if (showData != null) {
            if (this.getActivity() == null) {
                forceQuit();
                return;
            }
            SharedPreferences preferences = activity.getPreferences(MODE_PRIVATE);
            showData.setChecked(preferences.getBoolean("showData", false));
            showData.setOnPreferenceClickListener(preference -> {
                Log.d(TAG, "show data is set to " + showData.isChecked());
                preferences.edit().putBoolean("showData", showData.isChecked()).apply();
                return true;
            });
        }
    }

    private void generateCSV(EntityEnum entity) {
        System.out.println(entity);
        String baseDir = activity.getFilesDir().getAbsolutePath();
        String fileName = entity.getFileName();
        String filePath = baseDir + File.separator + fileName + CSV;
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(filePath), CSVFormat.EXCEL)) {
            printer.printRecords((Object) entity.getHeader());
            switch (entity) {
                case CONNECTIVITY:
                    printer.printRecords(connectivityList);
                    break;
                case SIGNALSTRENGTH:
                    printer.printRecords(signalStrengthList);
                    break;
            }
        } catch (IOException ex) {
            forceQuit();
            return;
        }

        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        Uri fileUri = FileProvider.getUriForFile(
                this.activity.getApplicationContext(),
                "com.lcl.lclmeasurementtool.fileprovider",
                new File(filePath));
        this.activity.grantUriPermission(activity.getPackageName(), fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        sharingIntent.setType(MIME_TYPE);
        sharingIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        sharingIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent chooser = Intent.createChooser(sharingIntent, "Share using");
        chooser.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        List<ResolveInfo> resInfoList1 = this.activity.getPackageManager().queryIntentActivities(sharingIntent, PackageManager.MATCH_DEFAULT_ONLY);
        List<ResolveInfo> resInfoList2 = this.activity.getPackageManager().queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY);
        resolvePermissions(resInfoList1, fileUri);
        resolvePermissions(resInfoList2, fileUri);
        this.activity.startActivity(chooser);
    }

    private void initData() {
        AbstractViewModel<Connectivity> connectivityViewModel = new ViewModelProvider(requireActivity()).get(ConnectivityViewModel.class);
        connectivityViewModel.getAll().observe(this, connectivities -> connectivityList = connectivities.stream().map(Connectivity::toCSV).collect(Collectors.toList()));

        AbstractViewModel<SignalStrength> signalStrengthViewModel  = new ViewModelProvider(requireActivity()).get(SignalViewModel.class);
        signalStrengthViewModel.getAll().observe(this, signalStrengths -> signalStrengthList = signalStrengths.stream().map(SignalStrength::toCSV).collect(Collectors.toList()));
    }

    private void resolvePermissions(List<ResolveInfo> resInfo, Uri fileUri) {
        for (ResolveInfo resolveInfo : resInfo) {
            String packageName = resolveInfo.activityInfo.packageName;
            this.activity.grantUriPermission(packageName, fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
    }

    private void forceQuit() {
        MessageDialog.show(R.string.error, R.string.setting_fatal_error_message, android.R.string.ok).setOkButtonClickListener((baseDialog, v) -> {
            baseDialog.getActivity().finish();
            return true;
        });
    }
}
