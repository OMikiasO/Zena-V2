package com.mike.zenaplusplus.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserModel {
    private String userId;
    private List<String> selectedCategories = new ArrayList<>();
    private Map<String, Integer> topicsData = new HashMap<>();
    private List<String> followingSources = new ArrayList<>();

    public UserModel(){}

    public List<String> getSelectedCategories() {
        return selectedCategories;
    }

    public void setSelectedCategories(List<String> selectedCategories) {
        this.selectedCategories = selectedCategories;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Map<String, Integer> getTopicsData() {
        return topicsData;
    }

    public void setTopicsData(Map<String, Integer> topicsData) {
        this.topicsData = topicsData;
    }

    public List<String> getFollowingSources() {
        return followingSources;
    }

    public void setFollowingSources(List<String> followingSources) {
        this.followingSources = followingSources;
    }
}
