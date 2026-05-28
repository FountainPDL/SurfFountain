package com.surffountain.browser.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.surffountain.browser.R;
import com.surffountain.browser.managers.AdBlockManager;
import com.surffountain.browser.managers.BrowserDatabase;

public class SettingsActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    private AdBlockManager adBlock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle("Settings");
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        prefs = getSharedPreferences("surf_prefs", MODE_PRIVATE);
        adBlock = AdBlockManager.getInstance(this);

        Switch adBlockSwitch = findViewById(R.id.switch_adblock);
        Switch httpsSwitch = findViewById(R.id.switch_https);
        Switch darkModeSwitch = findViewById(R.id.switch_dark);
        Switch cookiesSwitch = findViewById(R.id.switch_cookies);
        Switch jsSwitch = findViewById(R.id.switch_js);
        EditText homepageInput = findViewById(R.id.input_homepage);
        Spinner searchSpinner = findViewById(R.id.spinner_search);
        Spinner themeSpinner = findViewById(R.id.spinner_theme);

        adBlockSwitch.setChecked(adBlock.isEnabled());
        httpsSwitch.setChecked(prefs.getBoolean("https_upgrade", true));
        darkModeSwitch.setChecked(prefs.getInt("theme_mode", 0) == 1);
        cookiesSwitch.setChecked(prefs.getBoolean("accept_cookies", true));
        jsSwitch.setChecked(prefs.getBoolean("javascript", true));
        homepageInput.setText(prefs.getString("homepage", "https://www.google.com"));

        String[] engines = {"Google", "DuckDuckGo", "Brave Search", "Bing", "Ecosia", "StartPage"};
        searchSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, engines));
        searchSpinner.setSelection(prefs.getInt("search_engine", 0));

        String[] themes = {"System Default", "Dark", "Light"};
        themeSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, themes));
        themeSpinner.setSelection(prefs.getInt("theme_mode", 0));

        adBlockSwitch.setOnCheckedChangeListener((b, checked) -> adBlock.setEnabled(checked));
        httpsSwitch.setOnCheckedChangeListener((b, checked) -> prefs.edit().putBoolean("https_upgrade", checked).apply());
        darkModeSwitch.setOnCheckedChangeListener((b, checked) -> prefs.edit().putInt("theme_mode", checked ? 1 : 0).apply());
        cookiesSwitch.setOnCheckedChangeListener((b, checked) -> prefs.edit().putBoolean("accept_cookies", checked).apply());
        jsSwitch.setOnCheckedChangeListener((b, checked) -> prefs.edit().putBoolean("javascript", checked).apply());

        searchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                prefs.edit().putInt("search_engine", pos).apply();
            }
            public void onNothingSelected(AdapterView<?> p) {}
        });

        themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                prefs.edit().putInt("theme_mode", pos).apply();
                Toast.makeText(SettingsActivity.this, "Restart app to apply theme", Toast.LENGTH_SHORT).show();
            }
            public void onNothingSelected(AdapterView<?> p) {}
        });

        findViewById(R.id.btn_save_homepage).setOnClickListener(v -> {
            prefs.edit().putString("homepage", homepageInput.getText().toString().trim()).apply();
            Toast.makeText(this, "Homepage saved", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btn_clear_history).setOnClickListener(v -> {
            BrowserDatabase.getInstance(this).historyDao().clearAll();
            Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}
