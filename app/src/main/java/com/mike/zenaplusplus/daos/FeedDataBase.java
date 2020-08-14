package com.mike.zenaplusplus.daos;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.mike.zenaplusplus.models.FeedElementModel;

@Database(entities = {FeedElementModel.class}, version = 1)
public abstract class FeedDataBase extends RoomDatabase {

    private static FeedDataBase instance;

    public abstract FeedDao feedDao();

    public static synchronized FeedDataBase getInstance(Context context){
        if(instance==null){
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    FeedDataBase.class, "feed_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
