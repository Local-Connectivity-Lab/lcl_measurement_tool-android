package com.lcl.lclmeasurementtool;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.kongzue.dialogx.DialogX;
import com.kongzue.dialogx.dialogs.MessageDialog;
import com.kongzue.dialogx.dialogs.TipDialog;
import com.kongzue.dialogx.dialogs.WaitDialog;
import com.kongzue.dialogx.interfaces.OnDialogButtonClickListener;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String TOU = "https://seattlecommunitynetwork.org/";
    private static final String PRIVACY = "https://seattlecommunitynetwork.org/";
    private static final String EMAIL = "help@seattlecommunitynetwork.org";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
//        if (view != null && getContext() != null) {
//            view.setBackgroundColor(ContextCompat.getColor(this.getContext(), R.color.black));
//        }
        return view;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        Preference logout = findPreference("logout");
        if (logout != null) {
            logout.setOnPreferenceClickListener(preference -> {
                Activity activity = this.getActivity();
                if (activity != null) {
                    SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
                    preferences.edit().clear().apply();
                    TipDialog.show("Logging out ...", WaitDialog.TYPE.SUCCESS, 1000);
                    // TODO show login screen
                    Intent out = new Intent(activity, MainActivity.class);
                    activity.startActivity(out);
                    activity.finish();
                    return true;
                } else {
                    forceQuit();
                    return false;
                }
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
    }

    private void forceQuit() {
        MessageDialog.show(R.string.error, R.string.setting_fatal_error_message, android.R.string.ok).setOkButtonClickListener((baseDialog, v) -> {
            baseDialog.getActivity().finish();
            return true;
        });
    }
}
