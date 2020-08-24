package com.chaosapps.zena.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewsDetailsModel {

    public NewsDetailsModel() {
    }

    private String title;
    private String thumbnailLink;
    private String source;
    private String id;
    private String category;
    private long postedTime;
    private boolean showSourceText;
    private List<String> body = new ArrayList<>();
    private String audio;
    private Map<String, Integer> topics = new HashMap<>();
    private String link;

    private double relevanceScore;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnailLink() {
        return thumbnailLink;
    }

    public void setThumbnailLink(String thumbnailLink) {
        this.thumbnailLink = thumbnailLink;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getPostedTime() {
        return postedTime;
    }

    public void setPostedTime(long postedTime) {
        this.postedTime = postedTime;
    }

    public boolean isShowSourceText() {
        return showSourceText;
    }

    public void setShowSourceText(boolean showSourceText) {
        this.showSourceText = showSourceText;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getBody() {
        return body;
    }

    public void setBody(List<String> body) {
        this.body = body;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public Map<String, Integer> getTopics() {
        return topics;
    }

    public void setTopics(Map<String, Integer> topics) {
        this.topics = topics;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public double getRelevanceScore() {
        return relevanceScore;
    }

    public void setRelevanceScore(double relevanceScore) {
        this.relevanceScore = relevanceScore;
    }
}
