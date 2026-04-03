package com.example.app_design;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AI_Tutor extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private ImageView userQuestionDisplay, imgStandardAnswerLarge, imgDisplaySystemQ, imgDisplayUserQ;
    private TextView aiReplyText;
    private DatabaseManager dbManager;
    private int currentQId;
    private TextView txtAiSimilarity, txtAiStatus;
    private TextView txtStudentSimilarity, txtStudentStatus;
    private View layoutSystemQ, layoutUserQ;
    private View comparisonCard;
    private android.widget.Button btnNextQuestion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ai_tutor);

        drawerLayout = findViewById(R.id.drawer_layout);
        userQuestionDisplay = findViewById(R.id.img_display_user_q);
        aiReplyText = findViewById(R.id.txt_ai_reply);

        imgStandardAnswerLarge = findViewById(R.id.img_standard_answer_large);
        txtAiSimilarity = findViewById(R.id.txt_ai_similarity_result);
        txtAiStatus = findViewById(R.id.txt_ai_status_result);
        txtStudentSimilarity = findViewById(R.id.txt_student_similarity_result);
        txtStudentStatus = findViewById(R.id.txt_student_status_result);
        dbManager = new DatabaseManager(this);
        currentQId = getIntent().getIntExtra("CURRENT_Q_ID", -1);
        Log.d("DEBUG_AI", "Current Question ID received: " + currentQId);
        btnNextQuestion = findViewById(R.id.btn_next_question);

        //Invisible Logic
        imgDisplaySystemQ = findViewById(R.id.img_display_system_q);
        imgDisplayUserQ = findViewById(R.id.img_display_user_q);
        layoutSystemQ = findViewById(R.id.layout_system_q);
        layoutUserQ = findViewById(R.id.layout_user_q);
        comparisonCard = findViewById(R.id.comparison_card);

        //Sidebar Logic
        setupSidebarLogic();
        findViewById(R.id.btn_selectionbar).setOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.START);
        });

        //Image Zoom Logic
        String imageUriStr = getIntent().getStringExtra("USER_IMAGE_URI");
        int historyQId = getIntent().getIntExtra("LOAD_HISTORY_ID", -1);

        if (imageUriStr != null) {
            Uri imageUri = Uri.parse(imageUriStr);
            imgDisplayUserQ.setImageURI(imageUri);

            layoutUserQ.setVisibility(View.VISIBLE);
            aiReplyText.setVisibility(View.VISIBLE);

            boolean isPracticeMode = (currentQId != -1);

            if (isPracticeMode) {
                loadSystemQuestionImage(currentQId);
            }

            analyzeQuestion(imageUri, isPracticeMode);

        } else if (historyQId != -1) {
            loadHistoryDataToUI(historyQId);
        } else {
            performNewChatReset();
            addChatBubble("Hi! I am your AI Math Tutor.\nYou can ask me anything!", false);
        }

        //History Logic
        if(historyQId != -1){
            loadHistoryDataToUI(historyQId);
        }

        //Chatbox Logic
        EditText txtChatbox = findViewById(R.id.txt_chatbox);

        findViewById(R.id.sidebar_new_chat).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);

            performNewChatReset();

            addChatBubble("How can I help you with a new question?", false);
        });

        findViewById(R.id.btn_send).setOnClickListener(v -> {
            String msg = txtChatbox.getText().toString().trim();
            if (!msg.isEmpty()) {
                txtChatbox.setText("");
                processSimpleChat(msg);
            }
        });

        findViewById(R.id.img_camera).setOnClickListener(v -> {
            Intent intent = new Intent(this, Scan.class);
            intent.putExtra("CURRENT_Q_ID", currentQId);
            startActivity(intent);
        });

        findViewById(R.id.btn_next_question).setOnClickListener(v -> {
            Intent intent = new Intent(this, Practice.class);
            intent.putExtra("ACTION", "NEXT");
            startActivity(intent);
        });

        setupBottomNav();
    }


    private void analyzeQuestion(Uri userImageUri, boolean isPracticeMode) {
        aiReplyText.setText("AI is analyzing your math problem...");
        aiReplyText.setVisibility(View.VISIBLE);

        try {
            Bitmap userBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), userImageUri);
            Bitmap finalBitmapToAI;

            if (isPracticeMode) {
                layoutSystemQ.setVisibility(View.VISIBLE);
                comparisonCard.setVisibility(View.VISIBLE);
                btnNextQuestion.setVisibility(View.VISIBLE);

                loadSystemQuestionImage(currentQId);

                dbManager.open();
                byte[] imgData = dbManager.getQuestionImageById(currentQId);
                dbManager.close();

                Bitmap systemBitmap = null;
                if (imgData != null) {
                    systemBitmap = BitmapFactory.decodeByteArray(imgData, 0, imgData.length);
                }

                finalBitmapToAI = (systemBitmap != null) ? combineBitmaps(systemBitmap, userBitmap) : userBitmap;

            } else {
                layoutSystemQ.setVisibility(View.GONE);
                comparisonCard.setVisibility(View.GONE);
                btnNextQuestion.setVisibility(View.GONE);

                finalBitmapToAI = userBitmap;
            }

            processImageWithAI(finalBitmapToAI, userBitmap, isPracticeMode);

        } catch (Exception e) {
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            Log.e("ANALYZE_ERROR", "Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadSystemQuestionImage(int qId){
        if(qId != -1){
            dbManager.open();
            byte[] imgData = dbManager.getQuestionImageById(qId);
            dbManager.close();

            if(imgData != null){
                Bitmap bitmap = BitmapFactory.decodeByteArray(imgData, 0, imgData.length);
                imgDisplaySystemQ.setImageBitmap(bitmap);
            } else {
                Log.e("DEBUG_IMAGE", "No image data found for QID: " + qId);
                imgDisplaySystemQ.setImageResource(R.drawable.logo);
            }
        } else {
            Log.e("DEBUG_IMAGE", "Invalid QID: -1, skipping image load.");
        }
    }

    private void processImageWithAI(Bitmap bitmap, Bitmap originalUserBitmap, boolean isPracticeMode) {
        aiReplyText.setVisibility(View.VISIBLE);
        TextView thinkingBubble = addChatBubble("...", false);

        String mathPrompt = "You are a professional Malaysian mathematics teacher. Analyze the image." +
                "\n\n**TASK 1: IDENTIFY CHAPTER**" +
                "\nAnalyze the math problem in the image and determine the Chapter Number based on this syllabus:" +
                "\n- Quadratic Functions/Equations -> 1" +
                "\n- Number Bases -> 2" +
                "\n- Logical Reasoning -> 3" +
                "\n- Operations on Sets -> 4" +
                "\n- Network in Graph Theory -> 5" +
                "\n- Linear Inequalities -> 6" +
                "\n- Motion Graphs -> 7" +
                "\n- Dispersion -> 8" +
                "\n- Probability -> 9" +
                "\n- Financial Management -> 10" +
                "\n" +
                "\n**TASK 2: TEACH THE SOLUTION**" +
                "\nProvide a comprehensive, step-by-step tutorial on how to solve the question found in the image (The TOP part of the image if it's a stitched image)." +
                "\n- **Explain concepts:** Briefly mention the formula or concept used (e.g., 'Use the factorization method' or 'Apply the formula x = -b ± ...')." +
                "\n- **Show steps:** Break down the calculation line by line." +
                "\n- **Be encouraging:** Use a helpful tone." +
                "\n" +
                "\n**OUTPUT FORMAT:**" +
                "\nProvide response STRICTLY in the following JSON format (no markdown, no extra text):" +
                "\n{" +
                "\n  \"chapter_num\": \"The integer number ONLY (e.g. '1', NOT 'Chapter 1')\"," +
                "\n  \"solution\": \"Your detailed step-by-step teaching explanation here. Use \\n for new lines to make it readable.\"," +
                "\n  \"final_answer\": \"The final concise result\"" +
                "\n}";

        AIHelper.askAIWithImage(bitmap, mathPrompt, new AIHelper.AIResponseListener() {
            @Override
            public void onResponse(String answer) {
                runOnUiThread(() -> {
                    try {
                        thinkingBubble.setVisibility(View.GONE);
                        String cleanJson = answer.replaceAll("```json", "").replaceAll("```", "").trim();
                        if(cleanJson.contains("{") && cleanJson.contains("}")){
                            cleanJson = cleanJson.substring(cleanJson.indexOf("{"), cleanJson.lastIndexOf("}") + 1);
                        }

                        JSONObject json = new JSONObject(cleanJson);
                        String solution = json.getString("solution");

                        aiReplyText.setText("");

                        String title = isPracticeMode ? "【AI Tutor Explanation】\n" : "【AI Step-by-Step Solution】\n";

                        typeWriteEffect(aiReplyText, title + solution, () -> {
                            if (isPracticeMode) {
                                compareWithStandard(solution, originalUserBitmap);
                            }
                        });
                    } catch (Exception e) {
                        aiReplyText.setText(answer);
                        Log.e("JSON_ERROR", "Error: " + e.getMessage());
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> aiReplyText.setText("Error: " + error));
            }
        });
    }

    private void compareWithStandard(String aiSolution, Bitmap userBitmap){

        Log.d("DEBUG_AI", "Comparing using Question ID: " + currentQId);

        if (currentQId != -1) {
            dbManager.open();
            byte[] teacherAnsBlob = null;
            try {
                teacherAnsBlob = dbManager.getAnswerImageById(currentQId);
            } catch (Exception e) {
                Log.e("DB_ERROR", "Error retrieving image: " + e.getMessage());
            }
            if (teacherAnsBlob != null) {
                Log.d("DEBUG_AI", "Found standard answer image in DB");

                Bitmap teacherAnsBitmap = BitmapFactory.decodeByteArray(teacherAnsBlob, 0, teacherAnsBlob.length);

                runOnUiThread(() -> imgStandardAnswerLarge.setImageBitmap(teacherAnsBitmap));
                try {
                    saveToHistory(aiSolution, teacherAnsBlob, currentQId);
                } catch (Exception e) {
                    Log.e("DB_ERROR", "Error saving history: " + e.getMessage());
                }

                dbManager.close();

                Bitmap comparisonBitmap = combineBitmapsForComparison(userBitmap, teacherAnsBitmap);

                String gradingPrompt = "You are a STRICT Mathematics Examiner and Content Moderator." +
                        "\n" +
                        "\n**INPUTS PROVIDED**:" +
                        "\n1. **IMAGE**: Top part = Student's Handwritten Answer. Bottom part = Official Answer Key." +
                        "\n2. **AI TEXT SOLUTION**: \"" + aiSolution + "\"" +
                        "\n" +
                        "\n**CRITICAL VALIDATION (READ FIRST)**:" +
                        "\n- **Non-Math Check**: If the TOP image contains NO mathematical content (e.g., photos of people, landscapes, random objects, or non-math text), you MUST set 'student_similarity' to 0 and 'student_status' to 'Incorrect'." +
                        "\n- **Irrelevant Topic**: If the TOP image is a math problem but belongs to a completely different topic or question than the BOTTOM image (Answer Key), you MUST set 'student_similarity' to 0 and 'student_status' to 'Incorrect'." +
                        "\n" +
                        "\n**YOUR TASKS**:" +
                        "\n**TASK 1 (AI vs Key)**: Compare the 'AI TEXT SOLUTION' logic against the BOTTOM image (Answer Key). Are they mathematically equivalent?" +
                        "\n**TASK 2 (Student vs Key)**: If the content is valid math, compare the TOP visual part against the BOTTOM visual part." +
                        "\n- **Logic/Value Mismatch**: If final numbers or core methods differ, student_status is 'Incorrect'." +
                        "\n" +
                        "\nReturn strictly JSON format:" +
                        "\n{" +
                        "\n  \"ai_similarity\": (0-100, integer for TASK 1)," +
                        "\n  \"ai_status\": \"Correct\" or \"Incorrect\"," +
                        "\n  \"student_similarity\": (0-100, integer for TASK 2)," +
                        "\n  \"student_status\": \"Correct\" or \"Incorrect\"," +
                        "\n  \"explanation\": \"Briefly explain the results. If rejected for being irrelevant, state why.\"" +
                        "\n}";

                AIHelper.askAIWithImage(comparisonBitmap, gradingPrompt, new AIHelper.AIResponseListener() {
                    @Override
                    public void onResponse(String verifiedAnswer) {
                        Log.d("DEBUG_AI", "Step 4: AI Comparison Raw Response: " + verifiedAnswer);
                        runOnUiThread(() -> {
                            try {
                                String cleanJson = verifiedAnswer;
                                cleanJson = cleanJson.replace("```json", "").replace("```", "");
                                if(cleanJson.contains("{") && cleanJson.contains("}")){
                                    cleanJson = cleanJson.substring(cleanJson.indexOf("{"), cleanJson.lastIndexOf("}") + 1);
                                }

                                JSONObject json = new JSONObject(cleanJson);

                                int aiSimilarity = json.getInt("ai_similarity");
                                String aiStatus = json.getString("ai_status");
                                int studentSimilarity = json.getInt("student_similarity");
                                String studentStatus = json.getString("student_status");
                                String explanation = json.getString("explanation");

                                updateStatusUI(aiSimilarity, aiStatus, studentSimilarity, studentStatus);

                                String gradingResult = "\n\n----------------------------\n" +
                                        "【Grading Analysis】\n" + explanation;

                                aiReplyText.append(gradingResult);

                                findViewById(R.id.practice_scroll_view).post(() ->
                                        ((ScrollView) findViewById(R.id.practice_scroll_view)).fullScroll(View.FOCUS_DOWN));

                            } catch (Exception e) {
                                Log.e("JSON_ERROR", "Parsing failed: " + e.getMessage());
                                aiReplyText.append("\n\n[Error parsing score]\n" + verifiedAnswer);
                            }
                        });
                    }
                    @Override
                    public void onError(String error) {
                        Log.e("DEBUG_AI", "AI Error: " + error);
                    }
                });
            } else {
                Log.e("DEBUG_AI", "Error: No answer image found for QID: " + currentQId);
                dbManager.close();
                runOnUiThread(() -> Toast.makeText(AI_Tutor.this, "Standard answer not found in DB", Toast.LENGTH_SHORT).show());
            }
        } else {
            Log.e("DEBUG_AI", "Error: Invalid Current Question ID (-1)");
        }
    }

    //ai answer img + local answer img
    private Bitmap combineBitmaps(Bitmap topImage, Bitmap bottomImage){
        if(topImage == null) return bottomImage;
        if(bottomImage == null) return topImage;

        int width = Math.max(topImage.getWidth(), bottomImage.getWidth());
        int height = topImage.getHeight() + bottomImage.getHeight() + 20;

        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(result);

        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(topImage, 0, 0, null);

        android.graphics.Paint paint = new android.graphics.Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5);
        canvas.drawBitmap(bottomImage, 0, topImage.getHeight() + 20, null);

        return result;
    }

    //user answer img + local answer img
    private Bitmap combineBitmapsForComparison(Bitmap userImg, Bitmap standardImg){
        if(userImg == null) return standardImg;
        if(standardImg == null) return  userImg;

        int targetWidth = 1080;

        int userScaledHeight = (int) (userImg.getHeight() * ((float) targetWidth / userImg.getWidth()));
        int standardScaledHeight = (int) (standardImg.getHeight() * ((float) targetWidth / standardImg.getWidth()));

        Bitmap scaledUser = Bitmap.createScaledBitmap(userImg, targetWidth, userScaledHeight, true);
        Bitmap scaledStandard = Bitmap.createScaledBitmap(standardImg, targetWidth, standardScaledHeight, true);

        int spacing = 50;
        int finalHeight = userScaledHeight + standardScaledHeight + spacing;

        Bitmap result = Bitmap.createBitmap(targetWidth, finalHeight, Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(result);
        canvas.drawColor(Color.WHITE);

        canvas.drawBitmap(scaledUser, 0, 0, null);

        android.graphics.Paint paint = new android.graphics.Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(10);
        canvas.drawLine(0, userScaledHeight + (spacing / 2), targetWidth, userScaledHeight + (spacing / 2), paint);
        canvas.drawBitmap(scaledStandard, 0, userScaledHeight + spacing, null);

        return result;
    }

    private void updateStatusUI(int aiSimilarity, String aiStatus, int studentSimilarity, String studentStatus){
        txtAiSimilarity.setText(aiSimilarity + "%");
        if("Correct".equalsIgnoreCase(aiStatus)){
            txtAiStatus.setText("✅ Correct");
            txtAiStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
            txtAiSimilarity.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
        }else{
            txtAiStatus.setText("❌ Incorrect");
            txtAiStatus.setTextColor(android.graphics.Color.parseColor("#F44336"));
            txtAiSimilarity.setTextColor(android.graphics.Color.parseColor("#F44336"));
        }

        txtStudentSimilarity.setText(studentSimilarity + "%");
        if("Correct".equalsIgnoreCase(studentStatus)){
            txtStudentStatus.setText("✅ Correct");
            txtStudentStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
            txtStudentSimilarity.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
        }else{
            txtStudentStatus.setText("❌ Incorrect");
            txtStudentStatus.setTextColor(android.graphics.Color.parseColor("#F44336"));
            txtStudentSimilarity.setTextColor(android.graphics.Color.parseColor("#F44336"));
        }
    }

    private void saveToHistory(String solution, byte[] standardImg, int qId){
        String currentData = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        dbManager.saveOrUpdateHistory(currentData, true, solution.getBytes(), standardImg, qId);
    }

    private void setupSidebarLogic(){
        View drawerView = findViewById(R.id.nav_view);

        View newChatBtn = drawerView.findViewById(R.id.sidebar_new_chat);
        if (newChatBtn != null) {
            newChatBtn.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                performNewChatReset();
                addChatBubble("How can I help you with a new question?", false);
            });
        }
    }

    private void performNewChatReset(){
        layoutUserQ.setVisibility(View.GONE);
        layoutSystemQ.setVisibility(View.GONE);
        comparisonCard.setVisibility(View.GONE);
        btnNextQuestion.setVisibility(View.GONE);

        aiReplyText.setVisibility(View.GONE);
        aiReplyText.setText("");

        LinearLayout chatContainer = findViewById(R.id.chat_content_layout);
        if (chatContainer != null) {
            chatContainer.removeAllViews();
        }

        currentQId = -1;
    }

    private void startNewQuestionChat(int qId) {
        performNewChatReset();

        this.currentQId = qId;

        layoutSystemQ.setVisibility(View.VISIBLE);
        loadSystemQuestionImage(qId);

        addChatBubble("I've loaded Question " + qId + ". Please scan your answer whenever you're ready!", false);
    }

    private void toggleSubmenu(LinearLayout layout, ImageView arrow) {
        if(layout.getVisibility() == View.GONE){
            layout.setVisibility(View.VISIBLE);
            arrow.setRotation(180f);
        } else {
            layout.setVisibility(View.GONE);
            arrow.setRotation(0f);
        }
    }

    private void loadHistoryDataToUI(int qId) {
        this.currentQId = qId;

        dbManager.open();
        Cursor cursor = dbManager.getHistoryByQId(qId);

        if (cursor != null && cursor.moveToFirst()) {

            byte[] aiAnsBytes = cursor.getBlob(cursor.getColumnIndexOrThrow(DatabaseHelper.AI_ANS));
            String aiSolutionText = "";
            if (aiAnsBytes != null) {
                aiSolutionText = new String(aiAnsBytes);
            }

            byte[] standardAnsBlob = cursor.getBlob(cursor.getColumnIndexOrThrow(DatabaseHelper.S_ANS));
            Bitmap standardAnsBitmap = null;
            if (standardAnsBlob != null) {
                standardAnsBitmap = BitmapFactory.decodeByteArray(standardAnsBlob, 0, standardAnsBlob.length);
            }

            cursor.close();
            dbManager.close();

            layoutSystemQ.setVisibility(View.VISIBLE);
            comparisonCard.setVisibility(View.VISIBLE);
            btnNextQuestion.setVisibility(View.VISIBLE);
            layoutUserQ.setVisibility(View.GONE);

            loadSystemQuestionImage(qId);

            aiReplyText.setText("【History Record】\n" + aiSolutionText);

            if (standardAnsBitmap != null) {
                imgStandardAnswerLarge.setImageBitmap(standardAnsBitmap);
            }

            View similarityContainer = findViewById(R.id.layout_similarity_info);
            if (similarityContainer != null) {
                similarityContainer.setVisibility(View.GONE);
            }
            Toast.makeText(this, "History loaded for Q" + qId, Toast.LENGTH_SHORT).show();

        } else {
            dbManager.close();

            layoutSystemQ.setVisibility(View.GONE);
            layoutUserQ.setVisibility(View.GONE);
            comparisonCard.setVisibility(View.GONE);
            btnNextQuestion.setVisibility(View.GONE);

            aiReplyText.setText("You haven't done Question " + qId + " yet.\nPlease scan a picture to start.");
            Toast.makeText(this, "No history found for Q" + qId, Toast.LENGTH_SHORT).show();
        }
    }

    private void showZoomableImageDialog(Bitmap highResBitmap){
        if (highResBitmap == null) {
            Toast.makeText(this, "Image data not available", Toast.LENGTH_SHORT).show();
            return;
        }

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

    private void setupImageZoom(ImageView imageView){
        imageView.setOnClickListener(v -> {
            android.graphics.drawable.Drawable drawable = imageView.getDrawable();
            if(drawable != null){
                Bitmap bitmap;
                if(drawable instanceof android.graphics.drawable.BitmapDrawable){
                    bitmap = ((android.graphics.drawable.BitmapDrawable) drawable).getBitmap();
                }else{
                    bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                    android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
                    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                    drawable.draw(canvas);
                }
                showZoomableImageDialog(bitmap);
            }else{
                Toast.makeText(this, "Image is loading...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processSimpleChat(String userQuestion) {
        addChatBubble(userQuestion, true);
        TextView aiBubble = addChatBubble("...", false);

        String simplePrompt = "You are a helpful and friendly AI Math Tutor. " +
                "If the user asks a math question, provide a clear explanation. " +
                "If the user just wants to chat (e.g., says Hi, or how are you), respond naturally and friendly. " +
                "Keep your response concise and helpful.\n\n" +
                "User says: " + userQuestion;

        AIHelper.askAI(simplePrompt, new AIHelper.AIResponseListener() {
            @Override
            public void onResponse(String answer) {
                runOnUiThread(() -> {
                    typeWriteEffect(aiBubble, answer, () -> {
                    });
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    aiBubble.setText("Text Invalid.");
                });
            }
        });
    }

    private void typeWriteEffect(TextView targetTv, String text, Runnable onComplete){
        final int delay = 30;
        final android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());

        new Thread(() -> {
            for(int i = 0; i <= text.length(); i++){
                final String currentText = text.substring(0, i);
                handler.post(() -> targetTv.setText(currentText));
                try{
                    Thread.sleep(delay);
                } catch (Exception e) {
                    break;
                }
            }
            if (onComplete != null) {
                handler.post(onComplete);
            }
        }).start();
    }

    private TextView addChatBubble(String initialText, boolean isUser){
        LinearLayout chatContainer = findViewById(R.id.chat_content_layout);
        int layoutId = isUser ? R.layout.item_user_bubble : R.layout.item_ai_bubble;

        View bubbleView = LayoutInflater.from(this).inflate(layoutId, chatContainer, false);

        TextView tv = bubbleView.findViewById(R.id.txt_interactive_chat);

        if (tv != null) {
            tv.setText(initialText);
        }

        chatContainer.addView(bubbleView);

        ScrollView scrollView = findViewById(R.id.practice_scroll_view);
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));

        return tv;
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
        updateBottomNavState(R.id.btn_ai);
    }

    private void setNavAction(View view, final Class<?> targetActivity) {
        if (view == null) return;
        view.setOnClickListener(v -> {
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction(() -> {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();

                if (!this.getClass().equals(targetActivity)) {
                    Intent intent = new Intent(v.getContext(), targetActivity);
                    if (targetActivity.equals(Scan.class)) {
                        intent.putExtra("CURRENT_Q_ID", currentQId);
                    }
                    startActivity(intent);
                }
            }).start();
        });
    }
}
