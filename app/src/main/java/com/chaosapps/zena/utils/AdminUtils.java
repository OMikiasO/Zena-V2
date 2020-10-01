package com.chaosapps.zena.utils;

import android.app.Activity;
import android.util.Log;

import com.chaosapps.zena.models.NewsModel;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.HashMap;
import java.util.Map;

public class AdminUtils {
    private static final String TAG = "AdminUtils";
    private static AdminUtils INSTANCE;

    public static AdminUtils getInstance(){
        if(INSTANCE==null) INSTANCE = new AdminUtils();
        return INSTANCE;
    }

    public void sendNotification(Activity activity, NewsModel newsModel) {
        // Create the arguments to the callable function.
        try{
        final long time = System.currentTimeMillis();
        Map<String, Object> data = new HashMap<>();
        data.put("id", newsModel.getId());
        data.put("notificationType", "newsNotification");
        data.put("source", newsModel.getSource());
        data.put("text", newsModel.getTitle());
        data.put("imgUrl", newsModel.getThumbnailLink());
        Dialogs.getInstate().showLoadingDialog(activity, "Sending notification");
        FirebaseFunctions.getInstance("europe-west1")
                .getHttpsCallable("notification")
                .call(data)
                .continueWith(task -> {
                    Dialogs.getInstate().hideLoadingDialog();
                    if (task.isSuccessful()) {
                        Utils.getInstance().makeToast(activity, "Notification sent");
                        return "Notification sent";
                    } else {
                        Utils.getInstance().makeToast(activity, "Notification could not be sent");
                        Log.e(TAG,task.getException().getMessage());
                        return "err";
                    }
                });} catch (Exception e) {
            Utils.getInstance().recordException(e);
        }
    }
}
