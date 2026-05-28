package com.surffountain.browser.managers;

import android.content.Context;
import android.webkit.WebView;
import com.surffountain.browser.models.Tab;
import java.util.ArrayList;
import java.util.List;

public class TabManager {
    private static TabManager instance;
    private List<Tab> tabs = new ArrayList<>();
    private List<WebView> webViews = new ArrayList<>();
    private int currentIndex = 0;
    private int nextId = 0;
    private Context context;

    private TabManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized TabManager getInstance(Context context) {
        if (instance == null) instance = new TabManager(context);
        return instance;
    }

    public Tab addTab(String url, boolean incognito) {
        Tab tab = new Tab(nextId++, url, incognito);
        tabs.add(tab);
        WebView wv = new WebView(context);
        webViews.add(wv);
        currentIndex = tabs.size() - 1;
        return tab;
    }

    public Tab addTab(String url) { return addTab(url, false); }

    public void closeTab(int index) {
        if (index >= 0 && index < tabs.size()) {
            webViews.get(index).destroy();
            webViews.remove(index);
            tabs.remove(index);
            if (currentIndex >= tabs.size()) currentIndex = tabs.size() - 1;
        }
    }

    public Tab getCurrentTab() {
        if (tabs.isEmpty()) return null;
        return tabs.get(currentIndex);
    }

    public WebView getCurrentWebView() {
        if (webViews.isEmpty()) return null;
        return webViews.get(currentIndex);
    }

    public WebView getWebView(int index) {
        if (index >= 0 && index < webViews.size()) return webViews.get(index);
        return null;
    }

    public List<Tab> getTabs() { return tabs; }
    public int getCurrentIndex() { return currentIndex; }
    public void setCurrentIndex(int index) { currentIndex = index; }
    public int getTabCount() { return tabs.size(); }

    public Tab duplicateTab(int index) {
        if (index >= 0 && index < tabs.size()) {
            Tab original = tabs.get(index);
            return addTab(original.getUrl(), original.isIncognito());
        }
        return null;
    }
}
