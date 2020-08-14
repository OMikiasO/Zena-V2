package com.mike.zenaplusplus.models;

import java.util.ArrayList;
import java.util.List;

public class FeedModel {
    private int status;
    private List<FeedElementModel> objectList = new ArrayList<>();

    public FeedModel() {
    }

    public List<FeedElementModel> getObjectList() {
        return objectList;
    }

    public void setObjectList(List<FeedElementModel> objectList) {
        this.objectList = objectList;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
