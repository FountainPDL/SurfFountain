package com.surffountain.browser.managers;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

public class AdBlockManager {
    private static AdBlockManager instance;
    private boolean enabled = true;
    private Set<String> blockedDomains = new HashSet<>();
    private SharedPreferences prefs;

    private static final String[] DEFAULT_BLOCKED = {
        "doubleclick.net", "googlesyndication.com", "adservice.google.com",
        "pagead2.googlesyndication.com", "ads.facebook.com", "adnxs.com",
        "adsystem.com", "amazon-adsystem.com", "moatads.com", "scorecardresearch.com",
        "quantserve.com", "outbrain.com", "taboola.com", "revcontent.com",
        "zedo.com", "advertising.com", "ads.twitter.com", "pixel.facebook.com",
        "analytics.google.com", "google-analytics.com", "googletagmanager.com",
        "hotjar.com", "mixpanel.com", "segment.com", "amplitude.com",
        "track.click", "tracker.", "telemetry.", "metrics.", "beacon.",
        "bat.bing.com", "c.amazon-adsystem.com", "cdn.ampproject.org",
        "static.ads-twitter.com", "ads.linkedin.com", "px.ads.linkedin.com"
    };

    private AdBlockManager(Context context) {
        prefs = context.getSharedPreferences("adblock", Context.MODE_PRIVATE);
        enabled = prefs.getBoolean("enabled", true);
        for (String d : DEFAULT_BLOCKED) blockedDomains.add(d);
    }

    public static synchronized AdBlockManager getInstance(Context context) {
        if (instance == null) instance = new AdBlockManager(context);
        return instance;
    }

    public boolean isEnabled() { return enabled; }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        prefs.edit().putBoolean("enabled", enabled).apply();
    }

    public boolean shouldBlock(String url) {
        if (!enabled || url == null) return false;
        String lower = url.toLowerCase();
        for (String domain : blockedDomains) {
            if (lower.contains(domain)) return true;
        }
        return false;
    }

    public boolean shouldUpgradeToHttps(String url) {
        return url != null && url.startsWith("http://") && !url.startsWith("http://localhost");
    }

    public String upgradeToHttps(String url) {
        if (shouldUpgradeToHttps(url)) return url.replace("http://", "https://");
        return url;
    }

    public void addCustomBlock(String domain) { blockedDomains.add(domain.toLowerCase()); }
    public Set<String> getBlockedDomains() { return blockedDomains; }
}
