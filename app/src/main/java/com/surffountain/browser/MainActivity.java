package com.surffountain.browser;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.surffountain.browser.activities.BookmarksActivity;
import com.surffountain.browser.activities.DownloadsActivity;
import com.surffountain.browser.activities.HistoryActivity;
import com.surffountain.browser.activities.PDLAIActivity;
import com.surffountain.browser.activities.SettingsActivity;
import com.surffountain.browser.managers.AdBlockManager;
import com.surffountain.browser.managers.BrowserDatabase;
import com.surffountain.browser.managers.TabManager;
import com.surffountain.browser.models.Bookmark;
import com.surffountain.browser.models.DownloadItem;
import com.surffountain.browser.models.HistoryItem;
import com.surffountain.browser.models.Tab;
import java.io.ByteArrayInputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private EditText urlBar;
    private ProgressBar progressBar;
    private FloatingActionButton aiFab;
    private TextView tabCountView;
    private LinearLayout toolbar;
    private FrameLayout webContainer;

    private TabManager tabManager;
    private AdBlockManager adBlockManager;
    private BrowserDatabase db;
    private SharedPreferences prefs;

    private static final int FILE_CHOOSER_REQUEST = 1;
    private static final int PERMISSION_REQUEST = 2;
    private ValueCallback<Uri[]> filePathCallback;

    private boolean isIncognito = false;
    private boolean isDesktopMode = false;
    private boolean isReaderMode = false;
    private boolean isFullscreen = false;
    private String currentSearchEngine = "https://www.google.com/search?q=";

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = getSharedPreferences("surf_prefs", MODE_PRIVATE);
        applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db           = BrowserDatabase.getInstance(this);
        tabManager   = TabManager.getInstance(this);
        adBlockManager = AdBlockManager.getInstance(this);

        webView      = findViewById(R.id.webview);
        urlBar       = findViewById(R.id.url_bar);
        progressBar  = findViewById(R.id.progress_bar);
        aiFab        = findViewById(R.id.ai_fab);
        tabCountView = findViewById(R.id.tab_count);
        toolbar      = findViewById(R.id.toolbar_layout);
        webContainer = findViewById(R.id.web_container);

        ImageButton backBtn    = findViewById(R.id.btn_back);
        ImageButton forwardBtn = findViewById(R.id.btn_forward);
        ImageButton reloadBtn  = findViewById(R.id.btn_reload);
        ImageButton tabsBtn    = findViewById(R.id.btn_tabs);
        ImageButton menuBtn    = findViewById(R.id.btn_menu);
        ImageButton shieldBtn  = findViewById(R.id.btn_shield);

        setupWebView();
        setupUrlBar();

        backBtn.setOnClickListener(v -> {
            if (webView.canGoBack()) webView.goBack();
        });
        forwardBtn.setOnClickListener(v -> {
            if (webView.canGoForward()) webView.goForward();
        });
        reloadBtn.setOnClickListener(v -> {
            if (progressBar.getVisibility() == View.VISIBLE) webView.stopLoading();
            else webView.reload();
        });
        tabsBtn.setOnClickListener(v -> showTabsMenu());
        menuBtn.setOnClickListener(v -> showMainMenu());
        shieldBtn.setOnClickListener(v -> showShieldMenu());
        aiFab.setOnClickListener(v -> openPDLAI());

        tabManager.addTab(getStartUrl(), false);
        handleIntent(getIntent());
        requestPermissions();
    }

    private void applyTheme() {
        int mode = prefs == null ? 0 : prefs.getInt("theme_mode", 0);
        if (mode == 1)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else if (mode == 2)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    private String getStartUrl() {
        return prefs.getString("homepage", "https://www.google.com");
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setSupportZoom(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        s.setSupportMultipleWindows(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);

        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            int nightPref = prefs.getInt("theme_mode", 0);
            if (nightPref == 1)
                WebSettingsCompat.setForceDark(s, WebSettingsCompat.FORCE_DARK_ON);
            else
                WebSettingsCompat.setForceDark(s, WebSettingsCompat.FORCE_DARK_AUTO);
        }

        webView.setWebViewClient(new SurfWebViewClient());
        webView.setWebChromeClient(new SurfWebChromeClient());
        webView.addJavascriptInterface(new JsBridge(), "SurfFountain");

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, !isIncognito);

        webView.setDownloadListener((url, ua, contentDisposition, mimeType, contentLength) ->
            downloadFile(url, ua, contentDisposition, mimeType));

        injectExtensionScripts();
    }

    private void injectExtensionScripts() {
        String js = "(function(){" +
            "var style=document.createElement('style');" +
            "style.innerHTML='.ad,.ads,.advertisement,.banner-ad," +
            "[id*=\"ad-\"],[class*=\"ad-\"]{display:none!important}';" +
            "if(document.head) document.head.appendChild(style);" +
            "})();";
        webView.evaluateJavascript(js, null);
    }

    private void setupUrlBar() {
        urlBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO ||
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                loadUrl(urlBar.getText().toString());
                hideKeyboard();
                return true;
            }
            return false;
        });
        urlBar.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) urlBar.selectAll();
        });
    }

    public void loadUrl(String input) {
        String url = input.trim();
        if (url.isEmpty()) return;
        if (adBlockManager.shouldUpgradeToHttps(url))
            url = adBlockManager.upgradeToHttps(url);
        if (!url.startsWith("http://") && !url.startsWith("https://")
                && !url.startsWith("about:") && !url.startsWith("file:")) {
            if (url.contains(".") && !url.contains(" ")) {
                url = "https://" + url;
            } else {
                url = currentSearchEngine + Uri.encode(url);
            }
        }
        webView.loadUrl(url);
        urlBar.setText(url);
        Tab t = tabManager.getCurrentTab();
        if (t != null) t.setUrl(url);
    }

    private void downloadFile(String url, String ua,
                               String contentDisposition, String mimeType) {
        try {
            String fileName = URLUtil.guessFileName(url, contentDisposition, mimeType);
            DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url));
            req.setMimeType(mimeType);
            req.addRequestHeader("User-Agent", ua);
            req.addRequestHeader("Cookie", CookieManager.getInstance().getCookie(url));
            req.setDescription("Surf Fountain download");
            req.setTitle(fileName);
            req.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            req.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS, fileName);
            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            if (dm != null) dm.enqueue(req);
            db.downloadDao().insert(new DownloadItem(fileName, url, mimeType));
            Toast.makeText(this, "Downloading: " + fileName, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Download failed: " + e.getMessage(),
                Toast.LENGTH_SHORT).show();
        }
    }

    private void showMainMenu() {
        BottomSheetDialog sheet = new BottomSheetDialog(this, R.style.BottomSheetTheme);
        View view = getLayoutInflater().inflate(R.layout.menu_main, null);
        sheet.setContentView(view);

        view.findViewById(R.id.menu_new_tab).setOnClickListener(v -> {
            openNewTab(false); sheet.dismiss(); });
        view.findViewById(R.id.menu_new_incognito).setOnClickListener(v -> {
            openNewTab(true); sheet.dismiss(); });
        view.findViewById(R.id.menu_bookmarks).setOnClickListener(v -> {
            startActivity(new Intent(this, BookmarksActivity.class)); sheet.dismiss(); });
        view.findViewById(R.id.menu_history).setOnClickListener(v -> {
            startActivity(new Intent(this, HistoryActivity.class)); sheet.dismiss(); });
        view.findViewById(R.id.menu_downloads).setOnClickListener(v -> {
            startActivity(new Intent(this, DownloadsActivity.class)); sheet.dismiss(); });
        view.findViewById(R.id.menu_bookmark_page).setOnClickListener(v -> {
            bookmarkCurrentPage(); sheet.dismiss(); });
        view.findViewById(R.id.menu_find).setOnClickListener(v -> {
            showFindInPage(); sheet.dismiss(); });
        view.findViewById(R.id.menu_desktop).setOnClickListener(v -> {
            toggleDesktopMode(); sheet.dismiss(); });
        view.findViewById(R.id.menu_reader).setOnClickListener(v -> {
            toggleReaderMode(); sheet.dismiss(); });
        view.findViewById(R.id.menu_share).setOnClickListener(v -> {
            shareCurrentPage(); sheet.dismiss(); });
        view.findViewById(R.id.menu_print).setOnClickListener(v -> {
            printCurrentPage(); sheet.dismiss(); });
        view.findViewById(R.id.menu_qr).setOnClickListener(v -> {
            shareQR(); sheet.dismiss(); });
        view.findViewById(R.id.menu_iframe).setOnClickListener(v -> {
            extractIframeUrls(); sheet.dismiss(); });
        view.findViewById(R.id.menu_copy_url).setOnClickListener(v -> {
            copyCurrentUrl(); sheet.dismiss(); });
        view.findViewById(R.id.menu_settings).setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class)); sheet.dismiss(); });

        sheet.show();
    }

    private void showShieldMenu() {
        String[] items = {
            adBlockManager.isEnabled() ? "✅ Ad Blocking ON" : "❌ Ad Blocking OFF",
            "🔒 HTTPS Upgrade",
            "🍪 Clear Cookies",
            "🗑 Clear Cache",
            "🔗 Extract iFrame / Video URLs",
            "📋 Copy Page URL"
        };
        new AlertDialog.Builder(this)
            .setTitle("🛡 Surf Shield")
            .setItems(items, (d, which) -> {
                switch (which) {
                    case 0:
                        adBlockManager.setEnabled(!adBlockManager.isEnabled());
                        webView.reload();
                        Toast.makeText(this,
                            "Ad blocking " + (adBlockManager.isEnabled() ? "ON" : "OFF"),
                            Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        CookieManager.getInstance().removeAllCookies(null);
                        Toast.makeText(this, "Cookies cleared", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        webView.clearCache(true);
                        Toast.makeText(this, "Cache cleared", Toast.LENGTH_SHORT).show();
                        break;
                    case 4:
                        extractIframeUrls();
                        break;
                    case 5:
                        copyCurrentUrl();
                        break;
                }
            }).show();
    }

    private void showTabsMenu() {
        List<Tab> tabs = tabManager.getTabs();
        String[] titles = new String[tabs.size() + 2];
        for (int i = 0; i < tabs.size(); i++) {
            Tab t = tabs.get(i);
            String prefix = t.isPinned() ? "📌 " : t.isIncognito() ? "🕵 " : "🌐 ";
            String title = t.getTitle();
            if (title == null || title.isEmpty()) title = t.getUrl();
            if (title.length() > 40) title = title.substring(0, 40) + "...";
            titles[i] = prefix + title;
        }
        titles[tabs.size()]     = "＋ New Tab";
        titles[tabs.size() + 1] = "🕵 New Incognito Tab";

        new AlertDialog.Builder(this)
            .setTitle("Tabs  (" + tabs.size() + ")")
            .setItems(titles, (d, which) -> {
                if (which < tabs.size()) {
                    tabManager.setCurrentIndex(which);
                    switchToTab(which);
                } else if (which == tabs.size()) {
                    openNewTab(false);
                } else {
                    openNewTab(true);
                }
            })
            .setNeutralButton("Close Current", (d, w) -> closeCurrentTab())
            .show();
    }

    private void openNewTab(boolean incognito) {
        isIncognito = incognito;
        tabManager.addTab("https://www.google.com", incognito);
        updateTabCount();
        if (incognito) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, false);
            webView.clearCache(true);
            Toast.makeText(this, "🕵 Incognito tab", Toast.LENGTH_SHORT).show();
        }
        loadUrl("https://www.google.com");
    }

    private void switchToTab(int index) {
        Tab tab = tabManager.getTabs().get(index);
        urlBar.setText(tab.getUrl());
        loadUrl(tab.getUrl());
        updateTabCount();
    }

    private void closeCurrentTab() {
        int idx = tabManager.getCurrentIndex();
        tabManager.closeTab(idx);
        if (tabManager.getTabCount() == 0)
            tabManager.addTab("https://www.google.com");
        Tab t = tabManager.getCurrentTab();
        if (t != null) loadUrl(t.getUrl());
        updateTabCount();
    }

    private void updateTabCount() {
        tabCountView.setText(String.valueOf(tabManager.getTabCount()));
    }

    private void showTabsMenu() {
        List<Tab> tabs = tabManager.getTabs();
        String[] titles = new String[tabs.size() + 2];
        for (int i = 0; i < tabs.size(); i++) {
            Tab t = tabs.get(i);
            String prefix = t.isPinned() ? "📌 " : t.isIncognito() ? "🕵 " : "🌐 ";
            String title = t.getTitle();
            if (title == null || title.isEmpty()) title = t.getUrl();
            if (title.length() > 40) title = title.substring(0, 40) + "...";
            titles[i] = prefix + title;
        }
        titles[tabs.size()]     = "＋ New Tab";
        titles[tabs.size() + 1] = "🕵 New Incognito Tab";

        new AlertDialog.Builder(this)
            .setTitle("Tabs  (" + tabs.size() + ")")
            .setItems(titles, (d, which) -> {
                if (which < tabs.size()) {
                    tabManager.setCurrentIndex(which);
                    switchToTab(which);
                } else if (which == tabs.size()) {
                    openNewTab(false);
                } else {
                    openNewTab(true);
                }
            })
            .setNeutralButton("Close Current", (d, w) -> closeCurrentTab())
            .show();
    }

    private void openNewTab(boolean incognito) {
        isIncognito = incognito;
        tabManager.addTab("https://www.google.com", incognito);
        updateTabCount();
        if (incognito) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, false);
            webView.clearCache(true);
            Toast.makeText(this, "🕵 Incognito tab", Toast.LENGTH_SHORT).show();
        }
        loadUrl("https://www.google.com");
    }

    private void switchToTab(int index) {
        Tab tab = tabManager.getTabs().get(index);
        urlBar.setText(tab.getUrl());
        loadUrl(tab.getUrl());
        updateTabCount();
    }

    private void closeCurrentTab() {
        int idx = tabManager.getCurrentIndex();
        tabManager.closeTab(idx);
        if (tabManager.getTabCount() == 0)
            tabManager.addTab("https://www.google.com");
        Tab t = tabManager.getCurrentTab();
        if (t != null) loadUrl(t.getUrl());
        updateTabCount();
    }

    private void updateTabCount() {
        tabCountView.setText(String.valueOf(tabManager.getTabCount()));
    }

    private void bookmarkCurrentPage() {
        String url   = webView.getUrl();
        String title = webView.getTitle();
        if (url == null) return;
        if (db.bookmarkDao().findByUrl(url) != null) {
            Toast.makeText(this, "Already bookmarked", Toast.LENGTH_SHORT).show();
            return;
        }
        db.bookmarkDao().insert(new Bookmark(title != null ? title : url, url));
        Toast.makeText(this, "Bookmarked!", Toast.LENGTH_SHORT).show();
    }

    private void showFindInPage() {
        View view = getLayoutInflater().inflate(R.layout.dialog_find, null);
        EditText input = view.findViewById(R.id.find_input);
        new AlertDialog.Builder(this)
            .setTitle("Find in Page")
            .setView(view)
            .setPositiveButton("Find", (d, w) -> {
                String q = input.getText().toString();
                if (!q.isEmpty()) webView.findAllAsync(q);
            })
            .setNeutralButton("Next", (d, w) -> webView.findNext(true))
            .setNegativeButton("Close", (d, w) -> webView.clearMatches())
            .show();
    }

    private void toggleDesktopMode() {
        WebSettings s = webView.getSettings();
        isDesktopMode = !isDesktopMode;
        if (isDesktopMode) {
            s.setUserAgentString(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/120.0.0.0 Safari/537.36");
            s.setUseWideViewPort(true);
            s.setLoadWithOverviewMode(true);
        } else {
            s.setUserAgentString(WebSettings.getDefaultUserAgent(this));
        }
        webView.reload();
        Toast.makeText(this,
            isDesktopMode ? "🖥 Desktop mode ON" : "📱 Mobile mode",
            Toast.LENGTH_SHORT).show();
    }

    private void toggleReaderMode() {
        isReaderMode = !isReaderMode;
        if (isReaderMode) {
            String js = "(function(){" +
                "document.querySelectorAll(" +
                "'nav,header,footer,aside,.ad,.sidebar,iframe,script,style')" +
                ".forEach(function(e){e.remove();});" +
                "document.body.style.cssText=" +
                "'max-width:720px;margin:auto;padding:24px;" +
                "font-size:18px;line-height:1.8;" +
                "background:#faf9f7;color:#1a1a1a;font-family:Georgia,serif';" +
                "})();";
            webView.evaluateJavascript(js, null);
            Toast.makeText(this, "📖 Reader mode ON", Toast.LENGTH_SHORT).show();
        } else {
            webView.reload();
            Toast.makeText(this, "Reader mode OFF", Toast.LENGTH_SHORT).show();
        }
    }

    private void extractIframeUrls() {
        String js = "(function(){" +
            "var els=document.querySelectorAll('iframe,frame,video,source,embed,object');" +
            "var urls=[];" +
            "els.forEach(function(e){" +
            "  var src=e.src||e.data||e.getAttribute('data-src')||'';" +
            "  if(src) urls.push(e.tagName+': '+src);" +
            "});" +
            "return urls.length>0?urls.join('\\n'):'NONE';" +
            "})();";
        webView.evaluateJavascript(js, value -> {
            String result = value != null
                ? value.replaceAll("^\"|\"$", "").replace("\\n", "\n")
                : "NONE";
            if ("NONE".equals(result) || result.isEmpty()) {
                Toast.makeText(this, "No embeds found", Toast.LENGTH_SHORT).show();
                return;
            }
            final String finalResult = result;
            new AlertDialog.Builder(this)
                .setTitle("🔗 Extracted URLs")
                .setMessage(finalResult)
                .setPositiveButton("Share", (d, w) -> {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_TEXT, finalResult);
                    startActivity(Intent.createChooser(i, "Share URLs"));
                })
                .setNeutralButton("Copy", (d, w) -> {
                    ClipboardManager cm =
                        (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    cm.setPrimaryClip(ClipData.newPlainText("URLs", finalResult));
                    Toast.makeText(this, "Copied!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Close", null)
                .show();
        });
    }

    private void shareCurrentPage() {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, webView.getUrl());
        i.putExtra(Intent.EXTRA_SUBJECT, webView.getTitle());
        startActivity(Intent.createChooser(i, "Share via"));
    }

    private void printCurrentPage() {
        PrintManager pm = (PrintManager) getSystemService(PRINT_SERVICE);
        if (pm != null) {
            PrintDocumentAdapter adapter =
                webView.createPrintDocumentAdapter("Surf Fountain");
            pm.print("Surf Fountain", adapter,
                new PrintAttributes.Builder().build());
        }
    }

    private void shareQR() {
        String url = webView.getUrl();
        if (url == null) return;
        startActivity(new Intent(Intent.ACTION_VIEW,
            Uri.parse("https://api.qrserver.com/v1/create-qr-code/?size=300x300&data="
                + Uri.encode(url))));
    }

    private void copyCurrentUrl() {
        String url = webView.getUrl();
        if (url == null) return;
        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("URL", url));
        Toast.makeText(this, "URL copied!", Toast.LENGTH_SHORT).show();
    }

    private void openPDLAI() {
        Intent i = new Intent(this, PDLAIActivity.class);
        i.putExtra("page_url",   webView.getUrl());
        i.putExtra("page_title", webView.getTitle());
        startActivity(i);
    }

    private void hideKeyboard() {
        InputMethodManager imm =
            (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(urlBar.getWindowToken(), 0);
    }

    private void handleIntent(Intent intent) {
        if (intent != null
                && Intent.ACTION_VIEW.equals(intent.getAction())
                && intent.getData() != null) {
            loadUrl(intent.getData().toString());
        } else {
            loadUrl(getStartUrl());
        }
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                PERMISSION_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CHOOSER_REQUEST) {
            if (filePathCallback == null) return;
            filePathCallback.onReceiveValue(
                WebChromeClient.FileChooserParams.parseResult(resultCode, data));
            filePathCallback = null;
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) webView.goBack();
        else if (tabManager.getTabCount() > 1) closeCurrentTab();
        else super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
        if (isIncognito) {
            webView.clearCache(true);
            CookieManager.getInstance().removeAllCookies(null);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
        updateTabCount();
    }

    @Override
    protected void onDestroy() {
        webView.destroy();
        super.onDestroy();
    }

    // ── WebViewClient ────────────────────────────────────────────────────────
    private class SurfWebViewClient extends WebViewClient {

        @Override
        public WebResourceResponse shouldInterceptRequest(
                WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
            if (adBlockManager.shouldBlock(url)) {
                return new WebResourceResponse("text/plain", "utf-8",
                    new ByteArrayInputStream("".getBytes()));
            }
            return super.shouldInterceptRequest(view, request);
        }

        @Override
        public boolean shouldOverrideUrlLoading(
                WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
            String upgraded = adBlockManager.upgradeToHttps(url);
            if (!upgraded.equals(url)) {
                view.loadUrl(upgraded);
                return true;
            }
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            progressBar.setVisibility(View.VISIBLE);
            urlBar.setText(url);
            Tab t = tabManager.getCurrentTab();
            if (t != null) t.setUrl(url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            progressBar.setVisibility(View.GONE);
            String title = view.getTitle();
            Tab t = tabManager.getCurrentTab();
            if (t != null) {
                t.setTitle(title != null ? title : url);
                t.setUrl(url);
            }
            if (!isIncognito) {
                db.historyDao().insert(
                    new HistoryItem(title != null ? title : url, url));
            }
            urlBar.setText(url);
            injectExtensionScripts();
        }
    }

    // ── WebChromeClient ──────────────────────────────────────────────────────
    private class SurfWebChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            progressBar.setProgress(newProgress);
            if (newProgress == 100)
                progressBar.setVisibility(View.GONE);
            else
                progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            Tab t = tabManager.getCurrentTab();
            if (t != null) t.setTitle(title);
        }

        @Override
        public boolean onShowFileChooser(WebView webView,
                ValueCallback<Uri[]> filePathCallback,
                FileChooserParams fileChooserParams) {
            MainActivity.this.filePathCallback = filePathCallback;
            try {
                startActivityForResult(
                    fileChooserParams.createIntent(), FILE_CHOOSER_REQUEST);
            } catch (Exception e) {
                filePathCallback.onReceiveValue(null);
                return false;
            }
            return true;
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            isFullscreen = true;
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            webContainer.addView(view);
        }

        @Override
        public void onHideCustomView() {
            isFullscreen = false;
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            webContainer.removeAllViews();
        }
    }

    // ── JS Bridge (Extensions API) ───────────────────────────────────────────
    public class JsBridge {

        @android.webkit.JavascriptInterface
        public void openAI(String context) {
            runOnUiThread(() -> openPDLAI());
        }

        @android.webkit.JavascriptInterface
        public void shareUrl(String url) {
            runOnUiThread(() -> {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, url);
                startActivity(Intent.createChooser(i, "Share"));
            });
        }

        @android.webkit.JavascriptInterface
        public void downloadUrl(String url) {
            runOnUiThread(() -> downloadFile(url, "", null, "*/*"));
        }

        @android.webkit.JavascriptInterface
        public void showToast(String msg) {
            runOnUiThread(() ->
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show());
        }

        @android.webkit.JavascriptInterface
        public void loadPage(String url) {
            runOnUiThread(() -> loadUrl(url));
        }

        @android.webkit.JavascriptInterface
        public String getBrowserName() { return "Surf Fountain"; }

        @android.webkit.JavascriptInterface
        public String getAIName() { return "PDL AI"; }

        @android.webkit.JavascriptInterface
        public boolean isAdBlockEnabled() { return adBlockManager.isEnabled(); }

        @android.webkit.JavascriptInterface
        public String getCurrentUrl() {
            return webView.getUrl() != null ? webView.getUrl() : "";
        }

        @android.webkit.JavascriptInterface
        public int getTabCount() { return tabManager.getTabCount(); }
    }

} // end MainActivity
