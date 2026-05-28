package com.surffountain.browser.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "downloads")
public class DownloadItem {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String fileName;
    public String url;
    public String mimeType;
    public long size;
    public long downloadedAt;
    public String status;

    public DownloadItem(String fileName, String url, String mimeType) {
        this.fileName = fileName;
        this.url = url;
        this.mimeType = mimeType;
        this.size = 0L;
        this.downloadedAt = System.currentTimeMillis();
        this.status = "completed";
    }
}
