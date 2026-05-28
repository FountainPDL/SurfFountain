package com.surffountain.browser.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.surffountain.browser.R;

public class PDLAIActivity extends AppCompatActivity {
    private LinearLayout chatLayout;
    private EditText inputField;
    private ScrollView scrollView;
    private String pageUrl;
    private String pageTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdlai);
        setTitle("PDL AI");
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pageUrl   = getIntent().getStringExtra("page_url");
        pageTitle = getIntent().getStringExtra("page_title");

        chatLayout = findViewById(R.id.chat_layout);
        inputField = findViewById(R.id.ai_input);
        scrollView = findViewById(R.id.scroll_view);
        Button sendBtn = findViewById(R.id.btn_send);

        addMessage("PDL AI", "Hello! I'm PDL AI, your Surf Fountain assistant.\n\nCurrent page: " + pageTitle + "\nURL: " + pageUrl + "\n\nHow can I help you?", false);

        sendBtn.setOnClickListener(v -> {
            String msg = inputField.getText().toString().trim();
            if (msg.isEmpty()) return;
            addMessage("You", msg, true);
            inputField.setText("");
            processQuery(msg);
        });
    }

    private void processQuery(String query) {
        String lower = query.toLowerCase();
        String response;
        if (lower.contains("summarize") || lower.contains("summary")) {
            response = "I can summarize pages! Currently viewing:\n\"" + pageTitle + "\"\nAt: " + pageUrl + "\n\nFor full AI summarization, connect your API key in Settings → PDL AI Settings.";
        } else if (lower.contains("translate")) {
            response = "Translation feature: I can help translate content. What language would you like?";
        } else if (lower.contains("download") || lower.contains("video")) {
            response = "To download video/media from this page, use Menu → Extract iFrame URLs to find embed sources, then use the download button on the media.";
        } else if (lower.contains("hello") || lower.contains("hi")) {
            response = "Hello! I'm PDL AI. I can help you with page summaries, search, translation, and browser tips!";
        } else if (lower.contains("search")) {
            response = "You can search using any engine. Go to Settings → Search Engine to change your default.";
        } else if (lower.contains("privacy") || lower.contains("shield")) {
            response = "Surf Shield blocks ads, trackers, and malicious scripts. Tap the 🛡 icon to manage per-site settings.";
        } else {
            response = "I'm PDL AI. I'm here to help with:\n• Page summaries\n• Translation\n• Search tips\n• Privacy advice\n• Download help\n\nConnect an API key in Settings for full AI capabilities.";
        }
        addMessage("PDL AI", response, false);
    }

    private void addMessage(String sender, String text, boolean isUser) {
        TextView tv = new TextView(this);
        tv.setText(sender + ": " + text);
        tv.setPadding(24, 16, 24, 16);
        tv.setTextSize(14f);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 8, 0, 8);
        tv.setLayoutParams(params);
        if (isUser) {
            tv.setBackgroundColor(0xFF7C3AED);
            tv.setTextColor(0xFFFFFFFF);
        } else {
            tv.setBackgroundColor(0xFFF3F0FF);
            tv.setTextColor(0xFF1F2937);
        }
        chatLayout.addView(tv);
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}
