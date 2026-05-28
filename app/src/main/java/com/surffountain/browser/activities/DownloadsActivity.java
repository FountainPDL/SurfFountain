package com.surffountain.browser.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.surffountain.browser.R;
import com.surffountain.browser.managers.BrowserDatabase;
import com.surffountain.browser.models.DownloadItem;
import java.util.List;

public class DownloadsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        setTitle("Downloads");
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        BrowserDatabase db = BrowserDatabase.getInstance(this);
        ListView listView = findViewById(R.id.list_view);
        List<DownloadItem> items = db.downloadDao().getAll();

        String[] titles = new String[items.size()];
        for (int i = 0; i < items.size(); i++) {
            titles[i] = items.get(i).fileName + "\n" + items.get(i).url;
        }
        listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, titles));
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}
