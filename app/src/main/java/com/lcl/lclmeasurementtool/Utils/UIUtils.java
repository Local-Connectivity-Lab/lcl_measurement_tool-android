package com.lcl.lclmeasurementtool.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public class UIUtils {

    /**
     * Create and show a system Alert Dialog with given content.
     *
     * @param context               the current context of the application
     * @param titleID               the ID of the title string in string.xml
     * @param messageID             the ID of the message string in string.xml
     * @param positiveMessageID     the ID of the string of the positive action
     * @param positiveListener      the listener when the positive action is triggered
     * @param negativeMessageID     the ID of the string of the negative action; -1 if no negative messages
     * @param negativeListener      the listener when the negative action is triggered; null if no negative action listener
     */
    public static void showDialog(Context context, int titleID, final int messageID,
                                  final int positiveMessageID,
                                  DialogInterface.OnClickListener positiveListener,
                                  final int negativeMessageID,
                                  DialogInterface.OnClickListener negativeListener) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context)
                .setTitle(titleID)
                .setMessage(messageID)
                .setPositiveButton(positiveMessageID, positiveListener);
        if (negativeMessageID != -1) {
            alert.setNegativeButton(negativeMessageID, negativeListener);
        }

        alert.show();
    }

    /**
     * Create and show a system Snackbar with given content
     *
     * @param view               the view on which the snack bar will be present
     * @param textString         the string of the text message
     * @param actionStringId     the ID of the name of the action
     * @param listener           the listener when the action is triggered
     */
    public static void showSnackbar(View view, final String textString, final String actionStringId,
                                    View.OnClickListener listener) {
        Snackbar.make(view,
                textString,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(actionStringId, listener).show();
    }
}
