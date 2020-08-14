package com.mike.zenaplusplus.utils;

import androidx.lifecycle.MutableLiveData;

public class Controller {
    private static Controller INSTANCE;

    public static synchronized Controller getInstance(){
        if(INSTANCE==null){
            INSTANCE  = new Controller();
        }
        return INSTANCE;
    }

    public MutableLiveData<Boolean> detailsFragment = new MutableLiveData<>(false);
    public MutableLiveData<Boolean> searchFragment = new MutableLiveData<>(false);
    public MutableLiveData<Boolean> sourcesFragment = new MutableLiveData<>(false);
    public MutableLiveData<Boolean> savedNewsFragment = new MutableLiveData<>(false);

}
