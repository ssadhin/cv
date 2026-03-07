package com.example.myapplication;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.ContentValues;
import android.provider.MediaStore;
import android.os.Environment;
import java.io.OutputStream;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SignatureActivity extends AppCompatActivity {

    private DrawingView drawingView;
    private boolean isErase = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        drawingView = new DrawingView(this, null);
        FrameLayout drawingContainer = findViewById(R.id.drawing_container);
        drawingContainer.addView(drawingView);

        final FloatingActionButton penFab = findViewById(R.id.fab_pen);
        final FloatingActionButton eraseFab = findViewById(R.id.fab_erase);
        final FloatingActionButton clearFab = findViewById(R.id.fab_clear);
        final FloatingActionButton saveFab = findViewById(R.id.fab_save);
        final FloatingActionButton exportFab = findViewById(R.id.fab_export);
        final LinearLayout widthControlLayout = findViewById(R.id.width_control_layout);
        final SeekBar widthSeekBar = findViewById(R.id.sb_width);
        final TextView widthValueText = findViewById(R.id.tv_width_value);

        // Initial Tool State: Pen
        isErase = false;
        drawingView.setErase(false);
        penFab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF4081"))); // Active Pink/Accent
        eraseFab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#123B5D")));

        // Initial SeekBar setup
        widthSeekBar.setProgress((int) drawingView.getStrokeWidth());
        widthValueText.setText(String.valueOf(widthSeekBar.getProgress()));

        widthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float width = (float) progress;
                    if (width < 2) width = 2; // Minimum visible width
                    drawingView.setStrokeWidth(width);
                    widthValueText.setText(String.valueOf((int)width));
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        View.OnClickListener toolClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean clickedSame = (view.getId() == R.id.fab_pen && !isErase) || (view.getId() == R.id.fab_erase && isErase);
                
                if (view.getId() == R.id.fab_pen) {
                    isErase = false;
                } else {
                    isErase = true;
                }
                
                drawingView.setErase(isErase);
                
                // Highlight active tool
                penFab.setBackgroundTintList(ColorStateList.valueOf(!isErase ? Color.parseColor("#FF4081") : Color.parseColor("#123B5D")));
                eraseFab.setBackgroundTintList(ColorStateList.valueOf(isErase ? Color.parseColor("#FF4081") : Color.parseColor("#123B5D")));

                // Sync SeekBar
                widthSeekBar.setProgress((int) drawingView.getStrokeWidth());
                widthValueText.setText(String.valueOf(widthSeekBar.getProgress()));

                // Toggle visibility with animation
                if (clickedSame) {
                    if (widthControlLayout.getVisibility() == View.VISIBLE) {
                        widthControlLayout.animate().alpha(0f).translationY(20f).withEndAction(() -> widthControlLayout.setVisibility(View.GONE)).start();
                    } else {
                        widthControlLayout.setVisibility(View.VISIBLE);
                        widthControlLayout.setAlpha(0f);
                        widthControlLayout.setTranslationY(20f);
                        widthControlLayout.animate().alpha(1f).translationY(0f).start();
                    }
                } else {
                    // Just show if switching tools
                    if (widthControlLayout.getVisibility() != View.VISIBLE) {
                        widthControlLayout.setVisibility(View.VISIBLE);
                        widthControlLayout.setAlpha(0f);
                        widthControlLayout.setTranslationY(20f);
                        widthControlLayout.animate().alpha(1f).translationY(0f).start();
                    }
                }
            }
        };

        penFab.setOnClickListener(toolClickListener);
        eraseFab.setOnClickListener(toolClickListener);

        clearFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawingView.clearCanvas();
            }
        });

        saveFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSave();
            }
        });

        exportFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportToGallery();
            }
        });
    }

    private void exportToGallery() {
        Bitmap signature = drawingView.getCroppedBitmap();
        if (signature == null) return;

        String fileName = "Signature_" + System.currentTimeMillis() + ".png";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Signatures");

        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                signature.compress(Bitmap.CompressFormat.PNG, 100, out);
                Toast.makeText(this, "Signature exported to Gallery", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Uri saveBitmapToFile(Bitmap bitmap) {
        File file = new File(getCacheDir(), "signature.png");
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            return Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void performSave() {
        Bitmap signature = drawingView.getCroppedBitmap();
        Uri signatureUri = saveBitmapToFile(signature);

        Intent resultIntent = new Intent();
        if (signatureUri != null) {
            resultIntent.putExtra("signature_path", signatureUri.toString());
        }
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        performSave();
    }
}
