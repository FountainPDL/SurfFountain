package com.surffountain.browser.managers;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import com.surffountain.browser.models.Bookmark;
import java.util.List;

@Dao
public interface BookmarkDao {
    @Insert
    void insert(Bookmark bookmark);
    @Delete
    void delete(Bookmark bookmark);
    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    List<Bookmark> getAll();
    @Query("SELECT * FROM bookmarks WHERE url = :url LIMIT 1")
    Bookmark findByUrl(String url);
}
