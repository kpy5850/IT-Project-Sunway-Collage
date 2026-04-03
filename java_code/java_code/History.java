package com.example.app_design;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class History extends AppCompatActivity {

    private LinearLayout historyListContainer;
    private DatabaseManager dbManager;
    private TextView btnClearAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);

        historyListContainer = findViewById(R.id.history_list_container);
        btnClearAll = findViewById(R.id.btn_clear_all);
        dbManager = new DatabaseManager(this);

        loadHistoryData();

        btnClearAll.setOnClickListener(v -> showClearConfirmationDialog());

        setupBottomNav();
    }

    private void showClearConfirmationDialog(){
        new AlertDialog.Builder(this)
                .setTitle("Clear History")
                .setMessage("Are you sure you want to delete all history records? This cannot be undone.")
                .setPositiveButton("Yes, Clear", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        performClearHistory();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performClearHistory(){
        dbManager.open();
        dbManager.clearAllHistory();
        dbManager.close();

        loadHistoryData();

        Toast.makeText(this, "All history cleared!", Toast.LENGTH_SHORT).show();
    }

    private void loadHistoryData(){
        dbManager.open();
        Cursor cursor = dbManager.getAllQuestionsWithStatus();

        if(cursor != null && cursor.moveToFirst()){
            historyListContainer.removeAllViews();
            do{
                String topicName = cursor.getString(cursor.getColumnIndexOrThrow("topic_name"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.R_DATE));
                int qNumber = cursor.getInt(cursor.getColumnIndexOrThrow("q_number"));

                int qId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.Q_ID));

                if (date == null) date = "No record";

                int status;
                int statusIndex = cursor.getColumnIndex(DatabaseHelper.R_STATUS);
                if(cursor.isNull(statusIndex)){
                    status = -1;
                }else {
                    status = cursor.getInt(statusIndex);
                }
                addHistoryCard(topicName, qNumber, date, status, qId);
            }while(cursor.moveToNext());
            cursor.close();
        }
        dbManager.close();
    }

    private void addHistoryCard(String topicName, int qNumber, String date, int status, int qId){
        View cardView = LayoutInflater.from(this).inflate(R.layout.item_history_card, historyListContainer, false);
        LinearLayout cardBg = cardView.findViewById(R.id.history_card_bg);
        TextView txtTitle = cardView.findViewById(R.id.txt_history_title);
        TextView txtStatus = cardView.findViewById(R.id.txt_history_status);

        String fullTitle = topicName + " Q" + qNumber + " (" + date + ")";
        txtTitle.setText(fullTitle);

        if (status == 1) {
            cardBg.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.done));
            txtStatus.setText("Status: Done");
        } else if (status == 0) {
            cardBg.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.in_process));
            txtStatus.setText("Status: In Process");
        } else {
            cardBg.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.none));
            txtStatus.setText("Status: None");
        }

        cardView.setOnClickListener(v -> {
            if(status == 1){
                Intent intent = new Intent(History.this, AI_Tutor.class);
                intent.putExtra("LOAD_HISTORY_ID", qId);
                intent.putExtra("TOPIC_TITLE", topicName);
                startActivity(intent);
            }else{
                Intent intent = new Intent(History.this, Practice.class);
                intent.putExtra("TOPIC_TITLE", topicName);
                intent.putExtra("START_INDEX", qNumber - 1);
                startActivity(intent);
            }
        });

        historyListContainer.addView(cardView);
    }

    private void updateBottomNavState(int selectedItemId){
        int activeColor = android.graphics.Color.parseColor("#1800ad");
        int inactiveColor = android.graphics.Color.parseColor("#888888");

        updateSingleNavDesign(R.id.btn_home, R.id.bottom_img_home, R.id.bottom_txt_home,  selectedItemId == R.id.btn_home ? activeColor : inactiveColor);

        updateSingleNavDesign(R.id.btn_practice, R.id.bottom_img_practice, R.id.bottom_txt_practice,  selectedItemId == R.id.btn_practice ? activeColor : inactiveColor);

        updateSingleNavDesign(R.id.btn_history, R.id.bottom_img_history, R.id.bottom_txt_history,  selectedItemId == R.id.btn_history ? activeColor : inactiveColor);

        updateSingleNavDesign(R.id.btn_ai, R.id.bottom_img_ai, R.id.bottom_txt_ai,  selectedItemId == R.id.btn_ai ? activeColor : inactiveColor);
    }

    private void updateSingleNavDesign(int containerId, int imageId, int textId, int color) {
        View container = findViewById(containerId);
        if (container != null) {
            ImageView img = container.findViewById(imageId);
            TextView txt = container.findViewById(textId);

            if(img != null) img.setColorFilter(color);
            if(txt != null) txt.setTextColor(color);
        }
    }

    private void setupBottomNav() {
        setNavAction(findViewById(R.id.btn_home), Home.class);
        setNavAction(findViewById(R.id.btn_practice), Practice.class);
        setNavAction(findViewById(R.id.btn_scan), Scan.class);
        setNavAction(findViewById(R.id.btn_history), History.class);
        setNavAction(findViewById(R.id.btn_ai), AI_Tutor.class);
        updateBottomNavState(R.id.btn_history);
    }

    private void setNavAction(View view, final Class<?> targetActivity) {
        if (view == null) return;
        view.setOnClickListener(v -> {
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction(() -> {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();

                if (!this.getClass().equals(targetActivity)) {
                    startActivity(new Intent(History.this, targetActivity));
                }
            }).start();
        });
    }
}
