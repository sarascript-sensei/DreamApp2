package com.unicef.dreamapp2.ui.language;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.unicef.dreamapp2.R;
import com.unicef.dreamapp2.application.MyPreferenceManager;
import com.yariksoffice.lingver.Lingver;

public class LanguageActivity extends AppCompatActivity {

    RadioGroup.OnCheckedChangeListener onLanguagedCheckedListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                // Set to Kyrgyz
                case R.id.kyrgyz:
                    setToKyrgyz();
                    break;
                    // Set to Russian
                case R.id.russian:
                    setToRussian();
                    break;
                    // Se to english
                case R.id.english:
                    setToEnglish();
                    break;
            }
            editor.putBoolean("locale_changed", true);
            editor.commit();
            finish();
        }
    };

    private RadioGroup languageOption;
    private RadioButton languageRadioButton;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);

        sharedPreferences = MyPreferenceManager.getMySharedPreferences(this); // Shared preferences
        editor = sharedPreferences.edit(); // Edit shared preferences

        initViews(); // Initialize views
    }

    // Initialize views
    private void initViews() {
        languageOption = findViewById(R.id.languageRadioGroup);
        String currentLocale = sharedPreferences.getString("locale", "ru"); // Current language
        assert currentLocale != null;
        // Russian
        if (currentLocale.equals("ru")) {
            languageRadioButton = findViewById(R.id.russian);
            languageRadioButton.setChecked(true);
        }
        // Kyrgyz
        if (currentLocale.equals("ky")) {
            languageRadioButton = findViewById(R.id.kyrgyz);
            languageRadioButton.setChecked(true);
        }
        // English
        if (currentLocale.equals("en")) {
            languageRadioButton = findViewById(R.id.english);
            languageRadioButton.setChecked(true);
        }
        languageOption.setOnCheckedChangeListener(onLanguagedCheckedListener);
    }

    /*
     * Sets Russian as a default language
     * */
    private void setToRussian() {
        setSelectedLanguage("ru");
    }
    /*
     * Sets Kyrgyz as a default language
     * */
    private void setToKyrgyz() {
        setSelectedLanguage("ky");
    }
    /*
     * Sets English as a default language
     * */
    private void setToEnglish() {
        setSelectedLanguage("en");
    }

    private void setSelectedLanguage(String language) {
        Lingver.getInstance().setLocale(this, language);
        editor.remove("locale").putString("locale", language).commit();
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}