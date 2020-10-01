package com.chaosapps.zena.utils;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.chaosapps.zena.R;

import java.util.Objects;

public class Dialogs {
    private static Dialogs INSTANCE;
    private static final String TAG = "Dialogs";

    public static synchronized Dialogs getInstate(){
        if (INSTANCE == null) INSTANCE = new Dialogs();
        return INSTANCE;
    }

    private AlertDialog loadingAlertDialog;

    public void showLoadingDialog(final Activity activity, String title) {
        try {
            loadingAlertDialog = new AlertDialog.Builder(activity).create();
            final View view = activity.getLayoutInflater().inflate(R.layout.dialog_loading, null);
            loadingAlertDialog.setCancelable(false);

            final TextView loadingDialogTextView = view.findViewById(R.id.loadingDialogTextView);
            loadingDialogTextView.setText(title);

            loadingAlertDialog.setView(view);
            loadingAlertDialog.show();
            loadingAlertDialog.getWindow().setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.fill_white_rounded_corner));
        } catch (Exception e) {
            Utils.getInstance().recordException(e);
        }
    }

    public void hideLoadingDialog() {
        try {
            loadingAlertDialog.dismiss();
            loadingAlertDialog = null;
        } catch (Exception e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
    }

}
