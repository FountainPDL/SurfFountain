package com.surffountain.browser.managers;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.surffountain.browser.models.Bookmark;
import com.surffountain.browser.models.HistoryItem;
import com.surffountain.browser.models.DownloadItem;

@Database(entities = {Bookmark.class, HistoryItem.class, DownloadItem.class}, version = 1, exportSchema = false)
public abstract class BrowserDatabase extends RoomDatabase {
    private static BrowserDatabase instance;

    public abstract BookmarkDao bookmarkDao();
    public abstract HistoryDao historyDao();
    public abstract DownloadDao downloadDao();

    public static synchronized BrowserDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    BrowserDatabase.class, "surf_fountain_db")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
