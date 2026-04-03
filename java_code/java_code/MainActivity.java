package com.example.app_design;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Bundle;
import android.content.Intent;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean("isFirstRun", true);

        if(isFirstRun){
            DataInitializer initializer = new DataInitializer(this);
            initializer.importEduType();
            initializer.importChapters();
            initializer.importTopics();
            initializer.importSampleQuestions();

            prefs.edit().putBoolean("isFirstRun", false).apply();
            Toast.makeText(this, "Data Initialized!", Toast.LENGTH_SHORT).show();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, Home.class);
                startActivity(intent);
                finish();
            }
        }, 2000);
    }
}
