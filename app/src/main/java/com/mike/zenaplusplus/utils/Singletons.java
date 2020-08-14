package com.mike.zenaplusplus.utils;

import com.google.gson.Gson;

public class Singletons {
    private static Singletons INSTANCE;

    private Singletons() {
    }

    public static synchronized Singletons getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Singletons();
        }
        return INSTANCE;
    }

    private static Gson gson;
    public static Gson gson(){
        if(gson == null){
            gson=new Gson();
        }
        return gson;
    }

}
