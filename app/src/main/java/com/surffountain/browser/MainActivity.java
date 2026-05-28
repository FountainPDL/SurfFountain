package com.surffountain.browser;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private EditText urlBar;
    private ProgressBar progressBar;
    private FloatingActionButton aiFab;
    private static final int FILE_CHOOSER_REQUEST = 1;
    private ValueCallback<Uri[]> filePathCallback;

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        urlBar = findViewById(R.id.url_bar);
        progressBar = findViewById(R.id.progress_bar);
        aiFab = findViewById(R.id.ai_fab);

        ImageButton backBtn = findViewById(R.id.btn_back);
        ImageButton forwardBtn = findViewById(R.id.btn_forward);
        ImageButton reloadBtn = findViewById(R.id.btn_reload);
        ImageButton menuBtn = findViewById(R.id.btn_menu);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);

        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            WebSettingsCompat.setForceDark(settings, WebSettingsCompat.FORCE_DARK_AUTO);
        }

        webView.setWebViewClient(new SurfClient());
        webView.setWebChromeClient(new SurfChromeClient());
        webView.addJavascriptInterface(new JsBridge(), "PDLAI");
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        urlBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                loadUrl(urlBar.getText().toString());
                return true;
            }
            return false;
        });

        backBtn.setOnClickListener(v -> { if (webView.canGoBack()) webView.goBack(); });
        forwardBtn.setOnClickListener(v -> { if (webView.canGoForward()) webView.goForward(); });
        reloadBtn.setOnClickListener(v -> webView.reload());
        menuBtn.setOnClickListener(v -> showMenu());

        aiFab.setOnClickListener(v -> {
            webView.evaluateJavascript("window.PDLAI.prompt('PDL AI active')", null);
            Toast.makeText(this, "PDL AI activated", Toast.LENGTH_SHORT).show();
        });

        webView.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> {
            String fileName = URLUtil.guessFileName(url, contentDisposition, mimeType);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setMimeType(mimeType);
            request.addRequestHeader("User-Agent", userAgent);
            request.setDescription("Downloading...");
            request.setTitle(fileName);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            if (dm != null) dm.enqueue(request);
            Toast.makeText(this, "Downloading: " + fileName, Toast.LENGTH_SHORT).show();
        });

        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null) {
            loadUrl(intent.getData().toString());
        } else {
            loadUrl("https://www.google.com");
        }
    }

    private void loadUrl(String input) {
        String url = input.trim();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            if (url.contains(".") && !url.contains(" ")) url = "https://" + url;
            else url = "https://www.google.com/search?q=" + Uri.encode(url);
        }
        webView.loadUrl(url);
        urlBar.setText(url);
    }

    private void showMenu() {
        String[] items = {"New Tab", "Extract iFrame URLs", "Desktop site"};
        new AlertDialog.Builder(this)
            .setTitle("Surf Fountain Menu")
            .setItems(items, (dialog, which) -> {
                if (which == 0) webView.loadUrl("about:blank");
                if (which == 1) extractIframeUrls();
                if (which == 2) toggleDesktopMode();
            }).show();
    }

    private void extractIframeUrls() {
        webView.evaluateJavascript(
            "(function(){let frames=document.querySelectorAll('iframe,video,source');let urls=[];frames.forEach(f=>{if(f.src)urls.push(f.src)});return urls.join('\\n');})()",
            value -> {
                String result = value == null ? "" : value.replaceAll("^\"|\"$", "").replace("\\n", "\n");
                if (result.isEmpty() || result.equals("null")) {
                    Toast.makeText(this, "No embedded URLs found", Toast.LENGTH_SHORT).show();
                } else {
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("text/plain");
                    share.putExtra(Intent.EXTRA_TEXT, result);
                    startActivity(Intent.createChooser(share, "Extracted URLs"));
                }
            }
        );
    }

    private void toggleDesktopMode() {
        WebSettings settings = webView.getSettings();
        String ua = settings.getUserAgentString();
        if (ua != null && ua.contains("Mobile")) {
            settings.setUserAgentString(ua.replace("Mobile", "Desktop"));
            Toast.makeText(this, "Desktop mode enabled", Toast.LENGTH_SHORT).show();
        } else if (ua != null) {
            settings.setUserAgentString(ua.replace("Desktop", "Mobile"));
            Toast.makeText(this, "Mobile mode enabled", Toast.LENGTH_SHORT).show();
        }
        webView.reload();
    }

    private class SurfClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            urlBar.setText(request.getUrl().toString());
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            progressBar.setVisibility(View.VISIBLE);
            urlBar.setText(url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private class SurfChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            progressBar.setProgress(newProgress);
        }

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            MainActivity.this.filePathCallback = filePathCallback;
            try {
                startActivityForResult(fileChooserParams.createIntent(), FILE_CHOOSER_REQUEST);
            } catch (Exception e) {
                filePathCallback.onReceiveValue(null);
                return false;
            }
            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CHOOSER_REQUEST) {
            if (filePathCallback == null) return;
            Uri[] result = WebChromeClient.FileChooserParams.parseResult(resultCode, data);
            filePathCallback.onReceiveValue(result);
            filePathCallback = null;
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public class JsBridge {
        @android.webkit.JavascriptInterface
        public void prompt(String message) {
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "PDL AI: " + message, Toast.LENGTH_LONG).show());
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
}
