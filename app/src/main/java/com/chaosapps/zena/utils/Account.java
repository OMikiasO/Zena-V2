package com.chaosapps.zena.utils;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Source;
import com.chaosapps.zena.models.UserModel;
import com.chaosapps.zena.repository.NewsRepo;

public class Account implements Parcelable {
    private static Account INSTANCE;
    private static final String TAG = "Account";

    public MutableLiveData<UserModel> user = new MutableLiveData<>(new UserModel());

    private Account(Parcel in) {
    }

    public static final Creator<Account> CREATOR = new Creator<Account>() {
        @Override
        public Account createFromParcel(Parcel in) {
            return new Account(in);
        }

        @Override
        public Account[] newArray(int size) {
            return new Account[size];
        }
    };

    private Account() {

    }

    public static synchronized Account getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Account();
            INSTANCE.init();
        }
        return INSTANCE;
    }

    private void init() {
        topicAdder();
        viewNumberUpdater();
    }

    public void signIn() {
        FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String userId = task.getResult().getUser().getUid();
                Log.e(getClass().getSimpleName(), "User id -> " + userId);
                user.getValue().setUserId(userId);
                createUserDataIfNeeded(userId);
                CacheUtils.getInstance().userSharedPref.edit().putString("userId", userId).apply();
            } else {
                Log.e(getClass().getSimpleName(), task.getException().getMessage());
            }
        });
    }

    public void syncUserData(Context context) {
        final String userId = CacheUtils.getInstance().userSharedPref.getString("userId", null);
        Log.e(TAG, "userId" + userId);
        if (userId == null) {
            return;
        }
        FirebaseFirestore.getInstance().collection("Users").document(userId).get(Source.CACHE).addOnCompleteListener(taskA -> {
            if (taskA.isSuccessful()) {
                user.setValue(taskA.getResult().toObject(UserModel.class));
                NewsRepo.getInstance().fetchNewsForMainFeedFromCache(context);
                NewsRepo.getInstance().fetchNewsForMainFeed(context,false);
            } else {
                FirebaseFirestore.getInstance().collection("Users").document(userId).get().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        user.setValue(task1.getResult().toObject(UserModel.class));
                        NewsRepo.getInstance().fetchNewsForMainFeedFromCache(context);
                        NewsRepo.getInstance().fetchNewsForMainFeed(context,false);
                    } else {
                        Log.e(TAG, task1.getException().getMessage());
                    }
                });
            }
        });
    }

    public Task<Void> updateUserFirebaseData() {
        return FirebaseFirestore.getInstance().collection("Users").document(user.getValue().getUserId()).set(user.getValue(), SetOptions.merge()).addOnSuccessListener(aVoid -> Log.e(TAG, "updateUserFireStoreData - Done")).addOnFailureListener(e -> Log.e(TAG, e.getMessage()));
    }

    private void createUserDataIfNeeded(String userId) {
        FirebaseFirestore.getInstance().document("Users/" + userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (!task.getResult().exists()) {
                    UserModel userModel = new UserModel();
                    userModel.setUserId(userId);
                    FirebaseFirestore.getInstance().document("Users/" + userId).set(userModel).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) Log.e(TAG, "User data created");
                    });
                } else {
                    Log.e(TAG, "User data exists");
                }
            } else {
                Log.e(TAG, task.getException().getMessage());
            }
        });
    }

    private void topicAdder() {
        NewsRepo.getInstance().selectedNewsModel.observeForever(newsDetailsModel -> {
            UserModel userModel = user.getValue();
            if (newsDetailsModel.getBody().isEmpty() || userModel.getUserId() == null) return;
            for (String topic : newsDetailsModel.getTopics().keySet()) {
                if(newsDetailsModel.getTopics().get(topic)<0.8) continue;
                if (userModel.getTopicsData().get(topic) == null) {
                    userModel.getTopicsData().put(topic, 1);
                } else {
                    int topicFrequency = userModel.getTopicsData().get(topic);
                    userModel.getTopicsData().put(topic, topicFrequency + 1);
                }
            }
            user.setValue(userModel);
            updateUserFirebaseData();
        });
    }

    private void viewNumberUpdater(){
        NewsRepo.getInstance().selectedNewsModel.observeForever(newsDetailsModel -> {
            UserModel userModel = user.getValue();
            if (newsDetailsModel.getBody().isEmpty() || userModel.getUserId() == null) return;
            FirebaseFirestore.getInstance().document("News/"+newsDetailsModel.getId()).update("views", FieldValue.increment(1));
        });
    }

    public void followSource(String source){
        UserModel userModel = user.getValue();
        userModel.getFollowingSources().add(source);
        user.setValue(userModel);
        updateUserFirebaseData();
    }

    public void unFollowSource(String source){
        UserModel userModel = user.getValue();
        userModel.getFollowingSources().remove(source);
        user.setValue(userModel);
        updateUserFirebaseData();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
