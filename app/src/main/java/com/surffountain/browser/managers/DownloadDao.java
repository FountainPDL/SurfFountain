package com.surffountain.browser.managers;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import com.surffountain.browser.models.DownloadItem;
import java.util.List;

@Dao
public interface DownloadDao {
    @Insert
    void insert(DownloadItem item);
    @Delete
    void delete(DownloadItem item);
    @Query("SELECT * FROM downloads ORDER BY downloadedAt DESC")
    List<DownloadItem> getAll();
}
