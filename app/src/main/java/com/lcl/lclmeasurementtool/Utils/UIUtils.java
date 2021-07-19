package com.lcl.lclmeasurementtool.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public class UIUtils {

    public static void showDialog(Context context, int titleID, final int messageID,
                                  final int positiveMessageID,
                                  DialogInterface.OnClickListener positiveListener,
                                  final int negativeMessageID,
                                  DialogInterface.OnClickListener negativeListener) {
        new AlertDialog.Builder(context)
                .setTitle(titleID)
                .setMessage(messageID)
                .setPositiveButton(positiveMessageID, positiveListener)
                .setNegativeButton(negativeMessageID, negativeListener)
                .show();
    }

    public static void showSnackbar(View view, final String textString, final String actionStringId,
                                    View.OnClickListener listener) {
        Snackbar.make(view,
                textString,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(actionStringId, listener).show();
    }
}
