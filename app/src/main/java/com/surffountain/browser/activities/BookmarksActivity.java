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
import com.surffountain.browser.models.Bookmark;
import java.util.List;

public class BookmarksActivity extends AppCompatActivity {
    private BrowserDatabase db;
    private List<Bookmark> bookmarks;
    private ArrayAdapter<String> adapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        setTitle("Bookmarks");
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = BrowserDatabase.getInstance(this);
        listView = findViewById(R.id.list_view);
        loadBookmarks();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(android.net.Uri.parse(bookmarks.get(position).url));
            startActivity(intent);
            finish();
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            new AlertDialog.Builder(this)
                .setTitle("Delete bookmark?")
                .setPositiveButton("Delete", (d, w) -> {
                    db.bookmarkDao().delete(bookmarks.get(position));
                    loadBookmarks();
                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
            return true;
        });
    }

    private void loadBookmarks() {
        bookmarks = db.bookmarkDao().getAll();
        String[] titles = new String[bookmarks.size()];
        for (int i = 0; i < bookmarks.size(); i++) {
            titles[i] = bookmarks.get(i).title + "\n" + bookmarks.get(i).url;
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, titles);
        listView.setAdapter(adapter);
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}
