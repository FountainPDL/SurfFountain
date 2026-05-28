package com.surffountain.browser.models;

public class Tab {
    private int id;
    private String title;
    private String url;
    private boolean isIncognito;
    private boolean isPinned;
    private boolean isMuted;
    private long createdAt;

    public Tab(int id, String url, boolean isIncognito) {
        this.id = id;
        this.url = url;
        this.title = url;
        this.isIncognito = isIncognito;
        this.createdAt = System.currentTimeMillis();
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public boolean isIncognito() { return isIncognito; }
    public boolean isPinned() { return isPinned; }
    public void setPinned(boolean pinned) { isPinned = pinned; }
    public boolean isMuted() { return isMuted; }
    public void setMuted(boolean muted) { isMuted = muted; }
    public long getCreatedAt() { return createdAt; }
}
