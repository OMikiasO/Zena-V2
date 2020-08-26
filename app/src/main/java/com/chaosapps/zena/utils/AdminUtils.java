package com.chaosapps.zena.utils;

import android.app.Activity;

import com.chaosapps.zena.models.NewsDetailsModel;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.HashMap;
import java.util.Map;

public class AdminUtils {
    private static AdminUtils INSTANCE;

    public static AdminUtils getInstance(){
        if(INSTANCE==null) INSTANCE = new AdminUtils();
        return INSTANCE;
    }

    public void sendNotification(Activity activity, NewsDetailsModel newsDetailsModel) {
        // Create the arguments to the callable function.

        final long time = System.currentTimeMillis();
        Map<String, Object> data = new HashMap<>();
        data.put("id", newsDetailsModel.getId());
        data.put("notificationType", "newsNotification");
        data.put("source", newsDetailsModel.getSource());
        data.put("title", newsDetailsModel.getTitle());
        data.put("imgUrl", newsDetailsModel.getThumbnailLink());
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
                        return "err";
                    }
                });
    }
}
