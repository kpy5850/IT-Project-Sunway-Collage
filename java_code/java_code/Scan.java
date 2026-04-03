package com.example.app_design;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Scan extends AppCompatActivity {

    private ImageCapture imageCapture;
    private ImageView imgCapturePreview;
    private LinearLayout confirmLayout;
    private Uri currentSavedUri;
    private static final int CAMERA_PERMISSION_CORE = 100;
    private ActivityResultLauncher<String> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan);
        setupBottomNav();

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        currentSavedUri = uri;
                        imgCapturePreview.setImageURI(uri);
                        imgCapturePreview.setVisibility(View.VISIBLE);
                        confirmLayout.setVisibility(View.VISIBLE);
                        findViewById(R.id.Btn_scan).setVisibility(View.GONE);
                    }
                }
        );

        findViewById(R.id.btn_gallery).setOnClickListener(v -> {
            galleryLauncher.launch("image/*");
        });

        imgCapturePreview = findViewById(R.id.img_capture_preview);
        confirmLayout = findViewById(R.id.confirm_layout);

        if(allPermissionsGranted()){
            startCamera();
        }else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CORE);
        }
        findViewById(R.id.Btn_scan).setOnClickListener(v -> takePhoto());

        findViewById(R.id.btn_retake).setOnClickListener(v -> {
            imgCapturePreview.setVisibility(View.GONE);
            confirmLayout.setVisibility(View.GONE);
            findViewById(R.id.Btn_scan).setVisibility(View.VISIBLE);
        });

        findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            Intent intent = new Intent(Scan.this, AI_Tutor.class);
            intent.putExtra("USER_IMAGE_URI", currentSavedUri.toString());
            intent.putExtra("CURRENT_Q_ID", getIntent().getIntExtra("CURRENT_Q_ID", -1));
            intent.putExtra("TOPIC_TITLE", getIntent().getStringExtra("TOPIC_TITLE"));
            startActivity(intent);
        });
    }

    private void startCamera(){
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try{
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                androidx.camera.view.PreviewView viewFinder = findViewById(R.id.viewFinder);
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
            } catch (Exception e){
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto(){
        if(imageCapture == null)
            return;
        File photoFile;
        try{
            photoFile = createImageFile();
        } catch (IOException e){
            Toast.makeText(this, "Failed to create file", Toast.LENGTH_SHORT).show();
            return;
        }

        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                currentSavedUri = Uri.fromFile(photoFile);
                runOnUiThread(() -> {
                    imgCapturePreview.setImageURI(currentSavedUri);
                    imgCapturePreview.setVisibility(View.VISIBLE);

                    confirmLayout.setVisibility(View.VISIBLE);
                    findViewById(R.id.Btn_scan).setVisibility(View.GONE);
                });
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Toast.makeText(Scan.this, "Capture failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        return image;
    }

    private boolean allPermissionsGranted(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CORE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                    showPermissionSettingsDialog();
                } else {
                    Toast.makeText(this, "Camera permission is required to scan questions", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void showPermissionSettingsDialog() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("Camera access is needed to scan math questions. Please enable it in App Settings.")
                .setPositiveButton("Go to Settings", (dialog, which) -> {
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void setupBottomNav() {
        setNavAction(findViewById(R.id.btn_home), Home.class);
        setNavAction(findViewById(R.id.btn_practice), Practice.class);
        setNavAction(findViewById(R.id.btn_history), History.class);
        setNavAction(findViewById(R.id.btn_ai), AI_Tutor.class);

    }

    private void setNavAction(View view, final Class<?> targetActivity) {
        if (view == null) return;
        view.setOnClickListener(v -> {
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction(() -> {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();

                if (!this.getClass().equals(targetActivity)) {
                    startActivity(new Intent(Scan.this, targetActivity));
                }
            }).start();
        });
    }
}
