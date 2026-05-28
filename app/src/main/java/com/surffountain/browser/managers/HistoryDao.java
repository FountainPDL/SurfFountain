package com.surffountain.browser.managers;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.surffountain.browser.models.HistoryItem;
import java.util.List;

@Dao
public interface HistoryDao {
    @Insert
    void insert(HistoryItem item);
    @Query("SELECT * FROM history ORDER BY visitedAt DESC LIMIT 500")
    List<HistoryItem> getAll();
    @Query("SELECT * FROM history WHERE title LIKE '%' || :q || '%' OR url LIKE '%' || :q || '%' ORDER BY visitedAt DESC")
    List<HistoryItem> search(String q);
    @Query("DELETE FROM history")
    void clearAll();
}
