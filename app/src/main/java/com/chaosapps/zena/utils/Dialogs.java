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
        if(INSTANCE==null){
            INSTANCE = new Dialogs();
        }
        return INSTANCE;
    }

//    public void feedSettingsWillBeLost(final Activity activity) {
//        final androidx.appcompat.app.AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
//        alertDialog.setCancelable(false);
//
//        alertDialog.setTitle("Your selection wont be saved");
//        alertDialog.setMessage(message);
//
//
//        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Okay", (dialog, which) -> dialog.dismiss());
//        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Exit", (dialog, which) -> activity.finish());
//
//        alertDialog.show();
//        alertDialog.getWindow().setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.fill_white_rounded_corner));
//    }

    private AlertDialog loadingAlertDialog;

    public void showLoadingDialog(final Activity activity, String title) {
        loadingAlertDialog = new AlertDialog.Builder(activity).create();
        final View view = activity.getLayoutInflater().inflate(R.layout.dialog_loading, null);
        loadingAlertDialog.setCancelable(false);

        final TextView loadingDialogTextView = view.findViewById(R.id.loadingDialogTextView);
        loadingDialogTextView.setText(title);

        loadingAlertDialog.setView(view);
        loadingAlertDialog.show();
        loadingAlertDialog.getWindow().setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.fill_white_rounded_corner));
    }

    public void hideLoadingDialog() {
        try {
            loadingAlertDialog.dismiss();
        } catch (Exception e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
    }

}
