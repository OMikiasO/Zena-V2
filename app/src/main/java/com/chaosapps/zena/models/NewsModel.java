package com.chaosapps.zena.models;

public class NewsModel implements Comparable<NewsModel> {

    public static final int BIG_NEWS_ITEM = 1;
    public static final int SMALL_NEWS_ITEM = 2;

    public NewsModel() {
    }

    private String title;
    private String thumbnailLink;
    private String source;
    private String id;
    private String category;
    private long postedTime;
    private int number = -1;
    private boolean showSourceText;
    private String audio;
    private String link;
    private int newsType = BIG_NEWS_ITEM;
    private double relevanceScore;
    private Double rankingScore = 0d;

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

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
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

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
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

    public int getNewsType() {
        return newsType;
    }

    public void setNewsType(int newsType) {
        this.newsType = newsType;
    }

    public Double getRankingScore() {
        return rankingScore;
    }

    public void setRankingScore(Double rankingScore) {
        this.rankingScore = rankingScore;
    }

    @Override
    public int compareTo(NewsModel newsModel) {
        return newsModel.getRankingScore().compareTo(this.getRankingScore());
    }
}
