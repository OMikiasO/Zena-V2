package com.mike.zenaplusplus.models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "main_feed_table")
public class FeedElementModel {
    @Ignore
    public static final int BIG_NEWS_ITEM = 1;
    public static final int SMALL_NEWS_ITEM = 2;
    public static final int HEADER_ITEM = 3;
    public static final int BIG_DIVIDER_ITEM = 4;

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int itemType;
    private String itemJson;
    private double rankingScore;
    private String feedId;

    public FeedElementModel(int itemType, String itemJson) {
        this.itemType = itemType;
        this.itemJson = itemJson;
    }

    @Ignore
    public FeedElementModel() {
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getItemType() {
        return itemType;
    }

    public String getItemJson() {
        return itemJson;
    }

    public double getRankingScore() {
        return rankingScore;
    }

    public void setRankingScore(double rankingScore) {
        this.rankingScore = rankingScore;
    }

    public String getFeedId() {
        return feedId;
    }

    public void setFeedId(String feedId) {
        this.feedId = feedId;
    }
}

