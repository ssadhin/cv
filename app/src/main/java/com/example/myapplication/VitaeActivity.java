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

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class VitaeActivity extends AppCompatActivity {

    private DrawingView drawingView;
    private boolean isErase = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vitae);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        drawingView = new DrawingView(this, null);
        FrameLayout drawingContainer = findViewById(R.id.drawing_container);
        drawingContainer.addView(drawingView);

        FloatingActionButton clearFab = findViewById(R.id.fab_clear);
        clearFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawingView.clearCanvas();
            }
        });

        FloatingActionButton saveFab = findViewById(R.id.fab_save);
        saveFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        final FloatingActionButton eraseFab = findViewById(R.id.fab_erase);
        eraseFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isErase = !isErase;
                drawingView.setErase(isErase);
                if (isErase) {
                    eraseFab.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                } else {
                    eraseFab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#123B5D")));
                }
            }
        });
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

    @Override
    public void onBackPressed() {
        Bitmap signature = drawingView.getCroppedBitmap();
        Uri signatureUri = saveBitmapToFile(signature);

        Intent resultIntent = new Intent();
        resultIntent.putExtra("signatureUri", signatureUri);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}