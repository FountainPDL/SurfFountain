package com.surffountain.browser.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.surffountain.browser.MainActivity;
import com.surffountain.browser.R;
import com.surffountain.browser.managers.BrowserDatabase;
import com.surffountain.browser.models.HistoryItem;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    private BrowserDatabase db;
    private List<HistoryItem> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        setTitle("History");
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = BrowserDatabase.getInstance(this);
        ListView listView = findViewById(R.id.list_view);
        items = db.historyDao().getAll();

        String[] titles = new String[items.size()];
        for (int i = 0; i < items.size(); i++) {
            titles[i] = items.get(i).title + "\n" + items.get(i).url;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, titles);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(android.net.Uri.parse(items.get(position).url));
            startActivity(intent);
            finish();
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            new AlertDialog.Builder(this)
                .setTitle("Clear all history?")
                .setPositiveButton("Clear All", (d, w) -> {
                    db.historyDao().clearAll();
                    items.clear();
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null).show();
            return true;
        });
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}
