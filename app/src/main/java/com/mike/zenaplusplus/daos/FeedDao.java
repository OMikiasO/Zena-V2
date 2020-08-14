package com.mike.zenaplusplus.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.mike.zenaplusplus.models.FeedElementModel;

import java.util.List;

@Dao
public interface FeedDao {

    @Insert
    void insert(FeedElementModel feedElementModel);

    @Insert
    void insertAll(List<FeedElementModel> feedElementModels);

    @Update
    void update(FeedElementModel feedElementModel);

    @Delete
    void delete(FeedElementModel feedElementModel);

    @Query("DELETE FROM main_feed_table")
    void deleteAll();

    @Query("DELETE FROM main_feed_table WHERE feedId = 'mainFeed'")
    void deleteAllMainFeed();

    @Query("SELECT * FROM main_feed_table WHERE feedId = 'mainFeed'")
    LiveData<List<FeedElementModel>> getMainFeedElements();

    @Query("SELECT * FROM main_feed_table WHERE feedId = 'savedNewsFeed'")
    LiveData<List<FeedElementModel>> getSavedFeedElements();

}
