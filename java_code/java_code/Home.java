package com.example.app_design;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class Home extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        setupBottomNav();
        setupExpandableList();
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
        updateBottomNavState(R.id.btn_home);
    }

    private void setNavAction(View view, final Class<?> targetActivity) {
        if (view == null) return;
        view.setOnClickListener(v -> {
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction(() -> {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();

                if (!this.getClass().equals(targetActivity)) {
                    startActivity(new Intent(Home.this, targetActivity));
                }
            }).start();
        });
    }

    private void setupExpandableList() {
        //Form 4 setup
        View form4Arrow = findViewById(R.id.arrow_button_container1);
        ImageView form4ArrowIcon = findViewById(R.id.arrow_icon1);
        LinearLayout chapterList = findViewById(R.id.chapter_list_container);

        if(form4Arrow != null) {
            form4Arrow.setOnClickListener(v -> {
                boolean isVisible = chapterList.getVisibility() == View.VISIBLE;
                chapterList.setVisibility(isVisible ? View.GONE : View.VISIBLE);
                v.animate().rotation(isVisible ? 0 : 180).setDuration(200).start();
                form4ArrowIcon.animate().rotation(isVisible ? 0 : 180).setDuration(200).start();
            });
        }

        setupChapter(R.id.chapter1_arrow, R.id.topic1_list_container, new String[]{
                "1.1 - Quadratic Functions and Equations"
        });

        setupChapter(R.id.chapter2_arrow, R.id.topic2_list_container, new String[]{
                "2.1 - Number Bases"
        });

        setupChapter(R.id.chapter3_arrow, R.id.topic3_list_container, new String[]{
                "3.1 - Statements", "3.2 - Arguments"
        });

        setupChapter(R.id.chapter4_arrow, R.id.topic4_list_container, new String[]{
                "4.1 - Intersection of Sets", "4.2 - Union of Sets", "4.3 - Combined Operations of Sets"
        });

        setupChapter(R.id.chapter5_arrow, R.id.topic5_list_container, new String[]{
                "5.1 - Network"
        });

        setupChapter(R.id.chapter6_arrow, R.id.topic6_list_container, new String[]{
                "6.1 - Linear Inequalities in Two Variables", "6.2 - Systems of Linear Inequalities in Two Variables"
        });

        setupChapter(R.id.chapter7_arrow, R.id.topic7_list_container, new String[]{
                "7.1 - Distance-Time Graph", "7.2 - Speed-Time Graph"
        });

        setupChapter(R.id.chapter8_arrow, R.id.topic8_list_container, new String[]{
                "8.1 - Dispersion", "8.2 - Measures of Dispersion"
        });

        setupChapter(R.id.chapter9_arrow, R.id.topic9_list_container, new String[]{
                "9.1 - Combined Events", "9.2 - Dependent Events and Independent Events",
                "9.3 - Mutually Exclusive Events and Non-Mutually Exclusive Events", "9.4 - Application of Probability"
        });

        setupChapter(R.id.chapter10_arrow, R.id.topic10_list_container, new String[]{
                "10.1 - Financial Planning and Management"
        });

        //Form 5 setup
        View form5Arrow = findViewById(R.id.arrow_button_container_f5);
        ImageView form5ArrowIcon = findViewById(R.id.arrow_icon_f5);
        LinearLayout chapterListF5 = findViewById(R.id.chapter_list_container_f5);

        if(form5Arrow != null) {
            form5Arrow.setOnClickListener(v -> {
                boolean isVisible = chapterListF5.getVisibility() == View.VISIBLE;
                chapterListF5.setVisibility(isVisible ? View.GONE : View.VISIBLE);
                form5ArrowIcon.animate().rotation(isVisible ? 0 : 180).setDuration(200).start();
            });
        }

        setupChapter(R.id.f5_chapter1_arrow, R.id.f5_topic1_list_container, new String[]{
                "1.1 - Direct Variation", "1.2 - Inverse Variation", "1.3 - Combined Variation"
        });

        setupChapter(R.id.f5_chapter2_arrow, R.id.f5_topic2_list_container, new String[]{
                "2.1 - Matrices", "2.2 - Basic Operations on Matrices"
        });

        setupChapter(R.id.f5_chapter3_arrow, R.id.f5_topic3_list_container, new String[]{
                "3.1 - Risk and Insurance Coverage"
        });

        setupChapter(R.id.f5_chapter4_arrow, R.id.f5_topic4_list_container, new String[]{
                "4.1 - Taxation"
        });

        setupChapter(R.id.f5_chapter5_arrow, R.id.f5_topic5_list_container, new String[]{
                "5.1 - Congruency", "5.2 - Enlargement", "5.3 - Combined Transformations", "5.4 - Tessellation"
        });

        setupChapter(R.id.f5_chapter6_arrow, R.id.f5_topic6_list_container, new String[]{
                "6.1 - Sine, Cosine and Tangent Functions", "6.2 - Graphs of Sine, Cosine and Tangent Functions"
        });

        setupChapter(R.id.f5_chapter7_arrow, R.id.f5_topic7_list_container, new String[]{
                "7.1 - Dispersion", "7.2 - Measures of Dispersion"
        });

        setupChapter(R.id.f5_chapter8_arrow, R.id.f5_topic8_list_container, new String[]{
                "8.1 - Mathematical Modeling"
        });
    }

    private void setupChapter(int arrowId, int containerId, String[] topics) {
        View arrow = findViewById(arrowId);
        LinearLayout container = findViewById(containerId);

        if (arrow == null || container == null) return;
        container.removeAllViews();

        for (String topicTitle : topics) {
            TextView topicView = new TextView(this);
            topicView.setText(topicTitle);
            topicView.setPadding(100, 30, 20, 30);
            topicView.setTextSize(18);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            topicView.setLayoutParams(params);

            android.util.TypedValue outValue = new android.util.TypedValue();
            getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            topicView.setBackgroundResource(outValue.resourceId);
            topicView.setClickable(true);

            topicView.setOnClickListener(tv -> {
                Intent intent = new Intent(Home.this, Practice.class);
                intent.putExtra("TOPIC_TITLE", topicTitle);
                startActivity(intent);
            });

            container.addView(topicView);
        }

        arrow.setOnClickListener(v -> {
            if (container.getVisibility() == View.GONE) {
                container.setVisibility(View.VISIBLE);
                v.animate().rotation(180).setDuration(200).start();
            } else {
                container.setVisibility(View.GONE);
                v.animate().rotation(0).setDuration(200).start();
            }
        });
    }
}
