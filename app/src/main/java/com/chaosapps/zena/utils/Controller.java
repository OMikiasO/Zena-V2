package com.chaosapps.zena.utils;

import androidx.lifecycle.MutableLiveData;

public class Controller {
    private static Controller INSTANCE;

    public static synchronized Controller getInstance(){
        if(INSTANCE==null){
            INSTANCE  = new Controller();
        }
        return INSTANCE;
    }

    public MutableLiveData<Integer> navController = new MutableLiveData<>(1);
    public MutableLiveData<Boolean> detailsFragment = new MutableLiveData<>(false);
    public MutableLiveData<Boolean> searchFragment = new MutableLiveData<>(false);
    public MutableLiveData<Boolean> sourcesFragment = new MutableLiveData<>(false);
    public MutableLiveData<Boolean> singleSourceFragment = new MutableLiveData<>(false);
    public MutableLiveData<Boolean> savedNewsFragment = new MutableLiveData<>(false);
    public MutableLiveData<Integer> headlinesClicked = new MutableLiveData<>(0);
    public MutableLiveData<Boolean> noInternet = new MutableLiveData<>(false);

    public MutableLiveData<Boolean> showCategories = new MutableLiveData<>(false);


}
