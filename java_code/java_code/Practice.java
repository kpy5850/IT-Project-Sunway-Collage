package com.example.app_design;

import com.google.android.material.button.MaterialButton;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class Practice extends AppCompatActivity {

    private ImageView questionImageDisplay;
    private TextView txtTitle;
    private DatabaseManager dbManager;
    private List<String> allTopics;
    private int currentTopicIndex = 0;
    private int internalQuestionIndex = 0;
    private MaterialButton btnPlayVideo;
    private byte[] currentQuestionImgData;
    private MaterialButton btnNext, btnBack;

    private static final String PREFS_NAME = "LearningPrefs";
    private static final String KEY_TOPIC_INDEX = "last_topic_index";
    private static final String KEY_QUESTION_INDEX = "last_question_index";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.practice);
        setupBottomNav();

        txtTitle = findViewById(R.id.txt_title);
        questionImageDisplay = findViewById(R.id.Img_Question);
        btnNext = findViewById(R.id.btn_next);
        btnBack = findViewById(R.id.btn_back);

        btnPlayVideo = findViewById(R.id.btn_play_video);

        dbManager = new DatabaseManager(this);
        dbManager.open();
        allTopics = dbManager.getAllTopicNames();

        String action = getIntent().getStringExtra("ACTION");
        String selectedTopic = getIntent().getStringExtra("TOPIC_TITLE");
        if (selectedTopic != null) {
            txtTitle.setText(selectedTopic);
            currentTopicIndex = allTopics.indexOf(selectedTopic.trim());
            if (currentTopicIndex == -1) currentTopicIndex = 0;
            internalQuestionIndex = getIntent().getIntExtra("START_INDEX", 0);
        } else {
            loadProgress();
            if (currentTopicIndex >= allTopics.size()) currentTopicIndex = 0;
            selectedTopic = allTopics.get(currentTopicIndex);
            txtTitle.setText(selectedTopic);
        }

        loadQuestion(selectedTopic);
        questionImageDisplay.setOnClickListener(v -> {
            if(questionImageDisplay.getDrawable() != null){
                showZoomableImageDialog();
            }
        });
        loadVideoForTopic(selectedTopic);
        ImageButton btnUpload = findViewById(R.id.Img_Upload);
        btnUpload.setOnClickListener(v -> showUploadDialog());

        btnBack.setOnClickListener(v -> triggerPreviousQuestion());
        btnNext.setOnClickListener(v -> triggerNextQuestion());

        if("NEXT".equals(action)){
            triggerNextQuestion();
        }
    }

    private void loadVideoForTopic(String topicName){
        String videoId = null;


        android.util.Log.d("VIDEO_DEBUG", "Checking topic: " + topicName);
        if (topicName != null) {
            //Form 4
            if (topicName.contains("1.1") || topicName.contains("Quadratic")) {
                videoId = "vJIOC4bwqQM"; // F4 Chp 1
            } else if (topicName.startsWith("2.") && topicName.contains("Number Bases")) {
                videoId = "y9touGOJoDY"; // F4 Chp 2
            } else if (topicName.startsWith("3.") && (topicName.contains("Statement") || topicName.contains("Argument"))) {
                videoId = "s-UvXryqBjU"; // F4 Chp 3
            } else if (topicName.startsWith("4.") && topicName.contains("Set")) {
                videoId = "_b-s3mYdudA"; // F4 Chp 4
            } else if (topicName.startsWith("5.") && topicName.contains("Network")) {
                videoId = "WRX7RJFdakw"; // F4 Chp 5
            } else if (topicName.startsWith("6.") && topicName.contains("Inequalities")) {
                videoId = "MgYDdvyD5So"; // F4 Chp 6
            } else if (topicName.startsWith("7.") && topicName.contains("Graph") && topicName.contains("Time")) {
                videoId = "sl-S8JrByLk"; // F4 Chp 7
            } else if (topicName.startsWith("8.") && topicName.contains("Dispersion")) {
                videoId = "igUVWyDyi-Y"; // F4 Chp 8
            } else if (topicName.startsWith("9.") && (topicName.contains("Event") || topicName.contains("Probability"))) {
                videoId = "6neoMiUfPAI"; // F4 Chp 9
            } else if (topicName.startsWith("10.") && topicName.contains("Financial")) {
                videoId = "wfp4k9EVmEY"; // F4 Chp 10
            }
            //FORM 5
            else if (topicName.startsWith("1.") && topicName.contains("Variation")) {
                videoId = "WaQN0cvQyzA"; // F5 Chp 1
            } else if (topicName.startsWith("2.") && topicName.contains("Matri")) {
                videoId = "3eqNXYLmC3c"; // F5 Chp 2
            } else if (topicName.startsWith("3.") && topicName.contains("Insurance")) {
                videoId = "WqlaYjTzBAM"; // F5 Chp 3
            } else if (topicName.startsWith("4.") && topicName.contains("Taxation")) {
                videoId = "0lmo1ZoJPcQ"; // F5 Chp 4
            } else if (topicName.startsWith("5.") && (topicName.contains("Congruency") || topicName.contains("Enlargement") || topicName.contains("Transformation") || topicName.contains("Tessellation"))) {
                videoId = "piQO854sjss"; // F5 Chp 5
            } else if (topicName.startsWith("6.") && (topicName.contains("Sine") || topicName.contains("Cosine") || topicName.contains("Tangent"))) {
                videoId = "U3EQYGz5rFY"; // F5 Chp 6
            } else if (topicName.startsWith("7.") && topicName.contains("Dispersion")) {
                videoId = "zqZ13x0NPh8"; // F5 Chp 7
            }
        }

        android.util.Log.d("VIDEO_DEBUG", "Topic: " + topicName + " | Video ID: " + videoId);

        if (videoId != null) {
            btnPlayVideo.setVisibility(View.VISIBLE);
            final String finalVideoId = videoId;
            btnPlayVideo.setOnClickListener(null);
            btnPlayVideo.setOnClickListener(v -> openYouTube(finalVideoId));
        } else {
            btnPlayVideo.setVisibility(View.GONE);
        }
    }

    private void openYouTube(String videoId) {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));

        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + videoId));

        try {
            startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            startActivity(webIntent);
        }
    }

    private void updatePage(String topicName){
        txtTitle.setText(topicName);
        loadQuestion(topicName);
        loadVideoForTopic(topicName);
    }

    private void showFinishDialog() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Exercise Complete！")
                .setMessage("Congratulation, you done this chapter question！Do you want to continue the next chapter？")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, which) -> {
                    int nextIdx = currentTopicIndex + 1;
                    boolean foundNext = false;

                    while (nextIdx < allTopics.size()) {
                        if (dbManager.getQuestionCount(allTopics.get(nextIdx)) > 0) {
                            currentTopicIndex = nextIdx;
                            internalQuestionIndex = 0;
                            updatePage(allTopics.get(currentTopicIndex));
                            saveProgress();
                            foundNext = true;
                            break;
                        }
                        nextIdx++;
                    }

                    if (!foundNext) {
                        Toast.makeText(this, "You have finished all topics!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Practice.this, Home.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                    Toast.makeText(this, "You still can check the previous question", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void loadQuestion(String topicName) {
        int totalCount = dbManager.getQuestionCount(topicName);
        android.util.Log.d("DB_CHECK", "Topic: " + topicName + " | Total in DB: " + totalCount);

        byte[] imgData = dbManager.getQuestionImage(topicName, internalQuestionIndex);
        this.currentQuestionImgData = imgData;

        TextView subtitle = findViewById(R.id.practice_subtitle1);

        if (imgData != null) {
            try{
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 1;
                Bitmap bitmap = BitmapFactory.decodeByteArray(imgData, 0, imgData.length, options);
                questionImageDisplay.setImageBitmap(bitmap);

                if (subtitle != null) {
                    subtitle.setText("Question " + (internalQuestionIndex + 1) + ":");
                }

                View scrollView = findViewById(R.id.practice_scroll_view);
                if (scrollView != null) {
                    scrollView.scrollTo(0, 0);
                }

                int qId = dbManager.getQuestionId(topicName, internalQuestionIndex);
                if(qId != -1){
                    dbManager.updateStatus(qId, 0);
                }
            }catch (OutOfMemoryError e) {
                questionImageDisplay.setImageResource(android.R.color.transparent);
                Toast.makeText(this, "The image for Question " + (internalQuestionIndex + 1) + " is too large to display.", Toast.LENGTH_LONG).show();
            }
        }else {
            questionImageDisplay.setImageResource(android.R.color.transparent);
            if (subtitle != null) {
                subtitle.setText("Question " + (internalQuestionIndex + 1) + " (Error/No Data)");
            }
            Toast.makeText(this, "Image too large or No data for Question " + (internalQuestionIndex + 1), Toast.LENGTH_SHORT).show();
        }
    }

    private void showZoomableImageDialog(){
        if(currentQuestionImgData == null) return;

        Bitmap highResBitmap = BitmapFactory.decodeByteArray(currentQuestionImgData, 0, currentQuestionImgData.length);

        android.widget.RelativeLayout container = new android.widget.RelativeLayout(this);
        android.widget.RelativeLayout.LayoutParams containerParams = new android.widget.RelativeLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
        );
        container.setLayoutParams(containerParams);

        com.github.chrisbanes.photoview.PhotoView photoView = new com.github.chrisbanes.photoview.PhotoView(this);
        photoView.setImageBitmap(highResBitmap);

        android.widget.RelativeLayout.LayoutParams photoParams = new android.widget.RelativeLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );
        photoParams.addRule(android.widget.RelativeLayout.CENTER_IN_PARENT);
        photoView.setLayoutParams(photoParams);

        container.addView(photoView);

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        androidx.appcompat.app.AlertDialog dialog = builder.setView(container).create();
        if(dialog.getWindow() != null){
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.parseColor("#88333333")));
        }

        photoView.setOnClickListener(v -> dialog.dismiss());
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
            );
        }
    }

    private void triggerPreviousQuestion(){
        if(internalQuestionIndex > 0){
            internalQuestionIndex--;
            updatePage(allTopics.get(currentTopicIndex));
            saveProgress();
        } else if (currentTopicIndex > 0) {
            int prevIdx = currentTopicIndex - 1;
            while (prevIdx >= 0) {
                int count = dbManager.getQuestionCount(allTopics.get(prevIdx));
                if (count > 0) {
                    currentTopicIndex = prevIdx;
                    internalQuestionIndex = count - 1;
                    updatePage(allTopics.get(currentTopicIndex));
                    saveProgress();
                    return;
                }
                prevIdx--;
            }
            Toast.makeText(this, "No more previous questions!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Already at the first question!", Toast.LENGTH_SHORT).show();
        }
    }


    private void triggerNextQuestion() {
        int totalInTopic = dbManager.getQuestionCount(allTopics.get(currentTopicIndex));

        if (internalQuestionIndex < totalInTopic - 1) {
            internalQuestionIndex++;
            updatePage(allTopics.get(currentTopicIndex));
            saveProgress();
        } else {
            showFinishDialog();
        }
    }

    private void saveProgress(){
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_TOPIC_INDEX, currentTopicIndex);
        editor.putInt(KEY_QUESTION_INDEX, internalQuestionIndex);
        editor.apply();
    }

    private void loadProgress(){
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentTopicIndex = prefs.getInt(KEY_TOPIC_INDEX, 0);
        internalQuestionIndex = prefs.getInt(KEY_QUESTION_INDEX, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) {
            dbManager.close();
        }
    }

    private void showUploadDialog(){
        String[] options = {"Photo (Camera)", "Album (Gallery)", "Cancel"};

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Upload Method");
        builder.setItems(options, (dialog, which) ->{
            if(which == 0){
                int qId = dbManager.getQuestionId(txtTitle.getText().toString(), internalQuestionIndex);
                Intent intentCamera = new Intent(this, Scan.class);
                intentCamera.putExtra("TOPIC_TITLE", txtTitle.getText().toString());
                intentCamera.putExtra("CURRENT_Q_ID", qId);
                startActivity(intentCamera);
            } else if (which == 1) {
                Intent intentGallery = new Intent(Intent.ACTION_PICK);
                intentGallery.setType("image/*");
                startActivityForResult(intentGallery, 2);
            } else {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && data != null){
            Uri selectedImage = data.getData();
            if(selectedImage != null) {
                Intent intentAi = new Intent(this, AI_Tutor.class);
                intentAi.putExtra("USER_IMAGE_URI", selectedImage.toString());
                intentAi.putExtra("TOPIC_TITLE", txtTitle.getText().toString());

                int qId = dbManager.getQuestionId(txtTitle.getText().toString(), internalQuestionIndex);
                intentAi.putExtra("CURRENT_Q_ID" , qId);

                startActivity(intentAi);
            }
        }
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
        updateBottomNavState(R.id.btn_practice);
    }

    private void setNavAction(View view, final Class<?> targetActivity) {
        if (view == null) return;
        view.setOnClickListener(v -> {
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction(() -> {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();

                if (!this.getClass().equals(targetActivity)) {
                    startActivity(new Intent(Practice.this, targetActivity));
                }
            }).start();
        });
    }
}
