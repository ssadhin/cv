package com.example.myapplication;

import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.animation.OvershootInterpolator;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class HomeActivity extends AppCompatActivity implements ResumeAdapter.OnItemClickListener {

    private RecyclerView rvRecentResumes;
    private TextView tvEmptyState;
    private ResumeAdapter adapter;
    private File resumesDir;
    private ImageButton btnToggleView;
    private ImageButton btnSort;
    private android.widget.SeekBar gridSizeSlider;
    private TextView tvSortLabel;
    private View shareDropZone;
    private View brandingHeader;
    private boolean isGridView = false;
    private boolean isSortAlphabetical = false;
    private int gridSpanCount = 2; // Default 2 columns
    private static final String PREFS_NAME = "ResumeBuilderPrefs";
    private static final String KEY_IS_GRID_VIEW = "isGridView";
    private static final String KEY_IS_SORT_ALPHA = "isSortAlpha";
    private static final String KEY_GRID_SPAN = "gridSpanCount";

    private File currentDir;
    private View fabOptionsPanel;
    private View optionNewCV, optionExport, optionStepByStep, optionAI;
    private boolean isMenuOpen = false;
    
    private View folderRevealOverlay;
    private View advancedDragZones;
    private View zoneMoveToHome, zoneMoveToOtherGroup;
    private TextView tvRecentHeader;
    
    // Store coordinates for reverse animation
    private int lastFolderX, lastFolderY;
    
    private ActivityResultLauncher<String> importLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        rvRecentResumes = findViewById(R.id.rvRecentResumes);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        View btnHomeNewCV = findViewById(R.id.btnHomeNewCV);
        View btnHomeStepByStep = findViewById(R.id.btnHomeStepByStep);
        View btnHomeAI = findViewById(R.id.btnHomeAI);
        btnToggleView = findViewById(R.id.btnToggleView);
        btnSort = findViewById(R.id.btnSort);
        gridSizeSlider = findViewById(R.id.gridSizeSlider);
        tvSortLabel = findViewById(R.id.tvSortLabel);
        shareDropZone = findViewById(R.id.shareDropZone);

        brandingHeader = findViewById(R.id.brandingHeader);
        fabOptionsPanel = findViewById(R.id.fabOptionsPanel);
        optionNewCV = findViewById(R.id.optionNewCV);
        optionExport = findViewById(R.id.optionExport);
        optionStepByStep = findViewById(R.id.optionStepByStep);
        optionAI = findViewById(R.id.optionAI);

        tvRecentHeader = findViewById(R.id.tvRecentHeader);
        folderRevealOverlay = findViewById(R.id.folderRevealOverlay);
        advancedDragZones = findViewById(R.id.advancedDragZones);
        zoneMoveToHome = findViewById(R.id.zoneMoveToHome);
        zoneMoveToOtherGroup = findViewById(R.id.zoneMoveToOtherGroup);

        importLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    importVitaeFile(uri);
                }
            }
        );

        // Restore Preferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        isGridView = prefs.getBoolean(KEY_IS_GRID_VIEW, false);
        isSortAlphabetical = prefs.getBoolean(KEY_IS_SORT_ALPHA, false);
        gridSpanCount = prefs.getInt(KEY_GRID_SPAN, 2);
        
        updateToggleIcon();
        updateSortUI();
        updateSliderVisibility();
        
        gridSizeSlider.setProgress(5 - gridSpanCount); // 2 columns -> 3, 5 columns -> 0

        // Set initial branding padding to 6% of screen height
        int topPadding6Percent = (int) (getResources().getDisplayMetrics().heightPixels * 0.06);
        brandingHeader.setPadding(brandingHeader.getPaddingLeft(), topPadding6Percent, brandingHeader.getPaddingRight(), brandingHeader.getPaddingBottom());

        // Define directory for resumes
        resumesDir = new File(getFilesDir(), "resumes");
        if (!resumesDir.exists()) {
            resumesDir.mkdirs();
        }
        currentDir = resumesDir;

        // Initialize Delete Selected Button
        ImageButton btnDeleteSelected = findViewById(R.id.btnDeleteSelected);
        ImageButton btnCloseSelection = findViewById(R.id.btnCloseSelection);
        
        // Setup RecyclerView
        setupLayoutManager();
        adapter = new ResumeAdapter(this, new ArrayList<>(), this, isGridView, gridSpanCount, !currentDir.equals(resumesDir));
        
        // Set Selection Change Listener to update UI
        adapter.setOnSelectionChangeListener(count -> {
            if (count > 0) {
                btnDeleteSelected.setVisibility(View.VISIBLE);
                btnCloseSelection.setVisibility(View.VISIBLE);
                tvRecentHeader.setText(count + " Selected");
                // Hide sort/grid controls when selecting? Optional, but cleaner.
                btnSort.setVisibility(View.GONE);
                btnToggleView.setVisibility(View.GONE);
            } else {
                btnDeleteSelected.setVisibility(View.GONE);
                btnCloseSelection.setVisibility(View.GONE);
                tvRecentHeader.setText("Recent");
                btnSort.setVisibility(View.VISIBLE);
                btnToggleView.setVisibility(View.VISIBLE);
            }
        });
        
        btnDeleteSelected.setOnClickListener(v -> {
            confirmMultiDelete(new ArrayList<>(adapter.getSelectedPaths()));
        });
        
        btnCloseSelection.setOnClickListener(v -> {
            if (adapter != null) adapter.setSelectionMode(false);
        });

        rvRecentResumes.setAdapter(adapter);
        
        setupDragAndDrop();

        // Scroll Listener for Branding Header
        rvRecentResumes.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                View appLogo = findViewById(R.id.ivAppLogo);
                View appTitle = findViewById(R.id.tvAppTitle);
                View homeRoot = findViewById(R.id.homeRoot);
                if (appLogo == null || homeRoot == null || appTitle == null) return;

                // Create a robust transition
                android.transition.TransitionSet set = new android.transition.TransitionSet()
                    .addTransition(new android.transition.ChangeBounds())
                    .addTransition(new android.transition.Fade())
                    .setDuration(350);

                // Calculate 6% of screen height for consistent top padding
                int topPadding6Percent = (int) (getResources().getDisplayMetrics().heightPixels * 0.06);

                if (dy > 15 && appLogo.getVisibility() == View.VISIBLE) {
                     // Scrolling Down - Slide up
                     android.transition.TransitionManager.beginDelayedTransition((android.view.ViewGroup) homeRoot, set);
                     appLogo.setVisibility(View.GONE);
                     // Set top padding to exactly 6% so title moves into logo's place
                     brandingHeader.setPadding(brandingHeader.getPaddingLeft(), topPadding6Percent, brandingHeader.getPaddingRight(), 8);
                     
                     LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) appTitle.getLayoutParams();
                     params.topMargin = 0;
                     appTitle.setLayoutParams(params);
                     
                } else if (dy < -15 && appLogo.getVisibility() == View.GONE && !recyclerView.canScrollVertically(-1)) {
                     // Scrolling Up - Slide down
                     android.transition.TransitionManager.beginDelayedTransition((android.view.ViewGroup) homeRoot, set);
                     appLogo.setVisibility(View.VISIBLE);
                     // Restore same 6% padding for the logo
                     brandingHeader.setPadding(brandingHeader.getPaddingLeft(), topPadding6Percent, brandingHeader.getPaddingRight(), 16);
                     
                     LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) appTitle.getLayoutParams();
                     params.topMargin = (int) (8 * getResources().getDisplayMetrics().density);
                     appTitle.setLayoutParams(params);
                }
                
                // Force restoration at absolute top
                if (!recyclerView.canScrollVertically(-1) && appLogo.getVisibility() == View.GONE) {
                     android.transition.TransitionManager.beginDelayedTransition((android.view.ViewGroup) homeRoot, set);
                     appLogo.setVisibility(View.VISIBLE);
                     brandingHeader.setPadding(brandingHeader.getPaddingLeft(), topPadding6Percent, brandingHeader.getPaddingRight(), 16);
                     
                     LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) appTitle.getLayoutParams();
                     params.topMargin = (int) (8 * getResources().getDisplayMetrics().density);
                     appTitle.setLayoutParams(params);
                }
            }
        });

        btnHomeNewCV.setOnClickListener(v -> {
            if (isMenuOpen) {
                toggleMenu(false);
            } else {
                createNewResume();
            }
        });

        btnHomeStepByStep.setOnClickListener(v -> {
            if (isMenuOpen) {
                toggleMenu(false);
            } else {
                Intent intent = new Intent(HomeActivity.this, StepByStepActivity.class);
                startActivity(intent);
            }
        });

        btnHomeAI.setOnClickListener(v -> {
            if (isMenuOpen) {
                toggleMenu(false);
            } else {
                Intent intent = new Intent(HomeActivity.this, AIActivity.class);
                startActivity(intent);
            }
        });

        // Option Panel Click (to close)
        fabOptionsPanel.setOnClickListener(v -> toggleMenu(false));

        // Options Actions
        optionNewCV.setOnClickListener(v -> {
            toggleMenu(false);
            createNewResume();
        });

        optionExport.setOnClickListener(v -> {
            toggleMenu(false);
            importLauncher.launch("*/*");
        });

        optionStepByStep.setOnClickListener(v -> {
            toggleMenu(false);
            Intent intent = new Intent(HomeActivity.this, StepByStepActivity.class);
            startActivity(intent);
        });

        optionAI.setOnClickListener(v -> {
            toggleMenu(false);
            Intent intent = new Intent(HomeActivity.this, AIActivity.class);
            startActivity(intent);
        });

        // Toggle View
        btnToggleView.setOnClickListener(v -> {
            isGridView = !isGridView;
            savePreference(KEY_IS_GRID_VIEW, isGridView);
            updateToggleIcon();
            updateSliderVisibility();
            setupLayoutManager();
            if (adapter != null) {
                adapter.setViewType(isGridView);
            }
        });

        // Toggle Sort
        btnSort.setOnClickListener(v -> {
            isSortAlphabetical = !isSortAlphabetical;
            savePreference(KEY_IS_SORT_ALPHA, isSortAlphabetical);
            updateSortUI();
            loadResumes();
        });

        // Grid Size Slider
        gridSizeSlider.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    gridSpanCount = 5 - progress; // 5 columns (small) to 2 columns (large)
                    savePreference(KEY_GRID_SPAN, gridSpanCount);
                    if (isGridView) {
                        setupLayoutManager();
                    }
                    if (adapter != null) {
                        adapter.setSpanCount(gridSpanCount);
                    }
                }
            }
            @Override public void onStartTrackingTouch(android.widget.SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(android.widget.SeekBar seekBar) {}
        });
        
        // Handle Back Press for Folder Navigation
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (currentDir != null && !currentDir.equals(resumesDir)) {
                    // Go up one level
                    currentDir = currentDir.getParentFile();
                    loadResumes();
                } else {
                    // Default behavior (exit)
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        setupDragAndDrop();
    }

    private void setupLayoutManager() {
        if (isGridView) {
            rvRecentResumes.setLayoutManager(new GridLayoutManager(this, gridSpanCount));
        } else {
            rvRecentResumes.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    private void updateToggleIcon() {
        btnToggleView.setImageResource(isGridView ? R.drawable.ic_list : R.drawable.ic_grid);
    }

    private void updateSortUI() {
        btnSort.setImageResource(isSortAlphabetical ? android.R.drawable.ic_menu_today : android.R.drawable.ic_menu_sort_alphabetically);
        tvSortLabel.setText(isSortAlphabetical ? "Sorted A-Z" : "Sorted by time edited");
    }

    private void updateSliderVisibility() {
        gridSizeSlider.setVisibility(isGridView ? View.VISIBLE : View.GONE);
    }

    private void savePreference(String key, Object value) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        if (value instanceof Boolean) editor.putBoolean(key, (Boolean) value);
        else if (value instanceof Integer) editor.putInt(key, (Integer) value);
        else if (value instanceof String) editor.putString(key, (String) value);
        editor.apply();
    }

    private void createNewResume() {
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        intent.putExtra("EXTRA_IS_NEW", true);
        intent.putExtra("EXTRA_TARGET_DIR", currentDir.getAbsolutePath()); 
        startActivity(intent);
    }

    private void toggleMenu(boolean open) {
        if (isMenuOpen == open) return;
        isMenuOpen = open;

        fabOptionsPanel.setVisibility(View.VISIBLE);
        // Background Alpha
        ObjectAnimator bgAlpha = ObjectAnimator.ofFloat(fabOptionsPanel, "alpha", open ? 0f : 1f, open ? 1f : 0f);
        
        // Buttons Slide/Scale
        float startScale = open ? 0f : 1f;
        float endScale = open ? 1f : 0f;
        float startY = open ? 100f : 0f;
        float endY = open ? 0f : 100f;

        View[] options = {optionNewCV, optionExport, optionStepByStep, optionAI};
        AnimatorSet set = new AnimatorSet();
        List<android.animation.Animator> anims = new ArrayList<>();
        anims.add(bgAlpha);

        for (int i = 0; i < options.length; i++) {
            View opt = options[i];
            opt.setScaleX(startScale);
            opt.setScaleY(startScale);
            opt.setTranslationY(startY);
            
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(opt, "scaleX", startScale, endScale);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(opt, "scaleY", startScale, endScale);
            ObjectAnimator transY = ObjectAnimator.ofFloat(opt, "translationY", startY, endY);
            
            scaleX.setStartDelay(i * 50);
            scaleY.setStartDelay(i * 50);
            transY.setStartDelay(i * 50);
            
            anims.add(scaleX);
            anims.add(scaleY);
            anims.add(transY);
        }

        set.playTogether(anims);
        set.setInterpolator(new android.view.animation.OvershootInterpolator());
        set.setDuration(400);
        set.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (!open) {
                    fabOptionsPanel.setVisibility(View.GONE);
                }
            }
        });
        set.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadResumes();
    }

    private void loadResumes() {
        if (currentDir == null || !currentDir.exists()) currentDir = resumesDir;

        File[] files = currentDir.listFiles((dir, name) -> name.endsWith(".json") || new File(dir, name).isDirectory());
        List<File> fileList = new ArrayList<>();
        
        if (files != null) {
            fileList = new ArrayList<>(Arrays.asList(files));
            if (isSortAlphabetical) {
                Collections.sort(fileList, (f1, f2) -> {
                    if (f1.isDirectory() && !f2.isDirectory()) return -1;
                    if (!f1.isDirectory() && f2.isDirectory()) return 1;
                    return f1.getName().compareToIgnoreCase(f2.getName());
                });
            } else {
                Collections.sort(fileList, (f1, f2) -> {
                    if (f1.isDirectory() && !f2.isDirectory()) return -1;
                    if (!f1.isDirectory() && f2.isDirectory()) return 1;
                    return Long.compare(f2.lastModified(), f1.lastModified());
                });
            }
        }

        if (fileList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvRecentResumes.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvRecentResumes.setVisibility(View.VISIBLE);
            if (adapter != null) {
                boolean isSubfolder = !currentDir.equals(resumesDir);
                adapter.setIsInSubfolder(isSubfolder);
                adapter.updateData(fileList);
                
                if (tvRecentHeader != null) {
                    tvRecentHeader.setText(isSubfolder ? "Group: " + currentDir.getName() : "Recent");
                }
            }
        }
    }

    private void animateFolderTransition(View folderView, boolean entering, Runnable onAnimationEnd) {
        if (folderRevealOverlay == null) {
            if (onAnimationEnd != null) onAnimationEnd.run();
            return;
        }

        int cx, cy;
        if (entering && folderView != null) {
            int[] loc = new int[2];
            folderView.getLocationInWindow(loc);
            cx = loc[0] + folderView.getWidth() / 2;
            cy = loc[1] + folderView.getHeight() / 2;
            lastFolderX = cx;
            lastFolderY = cy;
        } else {
            cx = lastFolderX;
            cy = lastFolderY;
        }

        float startRadius = entering ? 0 : (float) Math.hypot(folderRevealOverlay.getWidth(), folderRevealOverlay.getHeight());
        float endRadius = entering ? (float) Math.hypot(folderRevealOverlay.getWidth(), folderRevealOverlay.getHeight()) : 0;

        folderRevealOverlay.setVisibility(View.VISIBLE);
        folderRevealOverlay.setAlpha(1.0f);
        
        android.animation.Animator anim = android.view.ViewAnimationUtils.createCircularReveal(folderRevealOverlay, cx, cy, startRadius, endRadius);
        anim.setDuration(500);
        anim.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (onAnimationEnd != null) onAnimationEnd.run();
                if (entering) {
                    folderRevealOverlay.animate().alpha(0).setDuration(400).withEndAction(() -> {
                        folderRevealOverlay.setVisibility(View.INVISIBLE);
                    }).start();
                } else {
                    folderRevealOverlay.setVisibility(View.INVISIBLE);
                }
            }
        });
        anim.start();
    }

    @Override
    public void onBackPressed() {
        if (isMenuOpen) {
            toggleMenu(false);
            return;
        }
        if (adapter != null && adapter.isSelectionMode()) {
            adapter.setSelectionMode(false);
            return;
        }
        if (!currentDir.equals(resumesDir)) {
            animateFolderTransition(null, false, () -> {
                currentDir = currentDir.getParentFile();
                loadResumes();
            });
            return;
        }
        super.onBackPressed();
    }

    // --- Adapter Callbacks ---

    @Override
    public void onItemClick(View view, File file) {
        if (file.isDirectory()) {
            animateFolderTransition(view, true, () -> {
                currentDir = file;
                loadResumes();
            });
        } else {
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            intent.putExtra("EXTRA_FILE_PATH", file.getAbsolutePath());
            startActivity(intent);
        }
    }

    @Override
    public void onDeleteClick(File file) {
        String type = file.isDirectory() ? "Folder" : "Resume";
        String displayName = file.getName();
        if (!file.isDirectory() && displayName.endsWith(".json")) {
            displayName = displayName.substring(0, displayName.lastIndexOf("."));
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Delete " + type)
            .setMessage("Are you sure you want to delete '" + displayName + "'?" + (file.isDirectory() ? "\nAll contents will be lost." : ""))
            .setPositiveButton("Delete", (dialog, which) -> {
                boolean deleted;
                if (file.isDirectory()) {
                    deleted = deleteRecursive(file);
                } else {
                    if (file.getName().endsWith(".json")) {
                        markPathForLocalStorageCleanup(file.getAbsolutePath());
                    }
                    deleted = file.delete();
                }
                
                if(deleted) {
                    loadResumes();
                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private boolean deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        } else if (fileOrDirectory.getName().endsWith(".json")) {
            markPathForLocalStorageCleanup(fileOrDirectory.getAbsolutePath());
        }
        return fileOrDirectory.delete();
    }

    private void markPathForLocalStorageCleanup(String path) {
        android.content.SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        java.util.Set<String> deletedPaths = prefs.getStringSet("deleted_cv_paths", new java.util.HashSet<>());
        java.util.Set<String> newPaths = new java.util.HashSet<>(deletedPaths);
        newPaths.add(path);
        prefs.edit().putStringSet("deleted_cv_paths", newPaths).apply();
    }

    @Override
    public void onRenameClick(File file) {
        final EditText input = new EditText(this);
        String originalName = file.getName();
        // Only strip extension if it's a file
        if (!file.isDirectory() && originalName.endsWith(".json")) {
             originalName = originalName.substring(0, originalName.lastIndexOf("."));
        }
        
        final String currentBaseName = originalName;
        input.setText(currentBaseName);
        
        new AlertDialog.Builder(this)
            .setTitle("Rename Resume")
            .setView(input)
            .setPositiveButton("Rename", (dialog, which) -> {
                String newName = input.getText().toString().trim();
                if (!newName.isEmpty() && !newName.equals(currentBaseName)) {
                    // Determine new file/dir path
                    File newFile;
                    if (file.isDirectory()) {
                        newFile = new File(resumesDir, newName); // Use currentDir? No, resumesDir might be wrong if inside folder.
                        // Actually, we should use file.getParentFile() to be safe!
                         newFile = new File(file.getParentFile(), newName);
                    } else {
                        newFile = new File(file.getParentFile(), newName + ".json");
                    }

                    if (file.renameTo(newFile)) {
                        // Also rename the thumbnail if it exists AND it is a file
                        if (!newFile.isDirectory()) {
                            File oldImage = new File(file.getParentFile(), currentBaseName + ".png");
                            if (oldImage.exists()) {
                                File newImage = new File(newFile.getParentFile(), newName + ".png");
                                oldImage.renameTo(newImage);
                            }
                        }
                        
                        loadResumes();
                    } else {
                        Toast.makeText(this, "Failed to rename (name might exist)", Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onMerge(File target, File source) {
        final EditText input = new EditText(this);
        input.setHint("Group name");
        
        new AlertDialog.Builder(this)
            .setTitle("Create Folder")
            .setView(input)
            .setPositiveButton("Create", (dialog, which) -> {
                String folderName = input.getText().toString().trim();
                if (folderName.isEmpty()) folderName = "New Folder";
                
                File newFolder = new File(currentDir, folderName);
                if (!newFolder.exists()) {
                    newFolder.mkdirs();
                }
                
                // Move source and target into new folder
                moveFileToFolder(source, newFolder);
                moveFileToFolder(target, newFolder);
                
                loadResumes();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onMoveToFolder(File folder, File source) {
        if (moveFileToFolder(source, folder)) {
            loadResumes();
            Toast.makeText(this, "Moved to " + folder.getName(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to move", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean moveFileToFolder(File source, File folder) {
        File dest = new File(folder, source.getName());
        boolean success = source.renameTo(dest);
        
        if (success) {
            // Also move thumbnail if it exists
            String sourceName = source.getName();
            if (sourceName.endsWith(".json")) {
                String baseName = sourceName.substring(0, sourceName.lastIndexOf("."));
                File oldThumb = new File(source.getParentFile(), baseName + ".png");
                if (oldThumb.exists()) {
                    File newThumb = new File(folder, baseName + ".png");
                    oldThumb.renameTo(newThumb);
                }
            }
        }
        return success;
    }

    @Override
    public void onDragStart(View view, File file) {
        // If in selection mode, drag ALL selected items
        ClipData data;
        if (adapter != null && adapter.isSelectionMode() && adapter.getSelectedPaths().size() > 1) {
            // Create ClipData with all selected paths
            java.util.Set<String> selectedPaths = adapter.getSelectedPaths();
            String[] pathsArray = selectedPaths.toArray(new String[0]);
            
            // First item
            data = ClipData.newPlainText("file_paths", pathsArray[0]);
            // Add remaining items
            for (int i = 1; i < pathsArray.length; i++) {
                data.addItem(new ClipData.Item(pathsArray[i]));
            }
        } else {
            // Single file drag
            data = ClipData.newPlainText("file_path", file.getAbsolutePath());
        }
        
        // Custom Shadow Builder to preserve roundness
        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view) {
            @Override
            public void onDrawShadow(android.graphics.Canvas canvas) {
                float density = view.getContext().getResources().getDisplayMetrics().density;
                float radius = file.isDirectory() ? 28 * density : 20 * density;
                
                android.graphics.Path path = new android.graphics.Path();
                android.graphics.RectF rect = new android.graphics.RectF(0, 0, view.getWidth(), view.getHeight());
                path.addRoundRect(rect, radius, radius, android.graphics.Path.Direction.CW);
                
                canvas.clipPath(path);
                view.draw(canvas);
                
                // Draw badge if multiple items
                if (adapter != null && adapter.isSelectionMode() && adapter.getSelectedPaths().size() > 1) {
                    android.graphics.Paint badgePaint = new android.graphics.Paint();
                    badgePaint.setColor(android.graphics.Color.parseColor("#1976D2"));
                    badgePaint.setAntiAlias(true);
                    
                    float badgeRadius = 20 * density;
                    float badgeX = view.getWidth() - badgeRadius - 8 * density;
                    float badgeY = badgeRadius + 8 * density;
                    canvas.drawCircle(badgeX, badgeY, badgeRadius, badgePaint);
                    
                    android.graphics.Paint textPaint = new android.graphics.Paint();
                    textPaint.setColor(android.graphics.Color.WHITE);
                    textPaint.setTextSize(12 * density);
                    textPaint.setAntiAlias(true);
                    textPaint.setTextAlign(android.graphics.Paint.Align.CENTER);
                    
                    String count = String.valueOf(adapter.getSelectedPaths().size());
                    canvas.drawText(count, badgeX, badgeY + 4 * density, textPaint);
                }
            }
        };
        view.startDragAndDrop(data, shadowBuilder, file, 0);
        
        boolean inSubfolder = !currentDir.equals(resumesDir);
        
        if (inSubfolder) {
            advancedDragZones.setVisibility(View.VISIBLE);
        }

        if (adapter != null && adapter.isSelectionMode()) {
            shareDropZone.setVisibility(View.VISIBLE);
        } else if (!inSubfolder) {
            shareDropZone.setVisibility(View.VISIBLE);
        }
    }

    private void setupDragAndDrop() {
        shareDropZone.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED: return true;
                case DragEvent.ACTION_DRAG_ENTERED: v.setAlpha(0.5f); return true;
                case DragEvent.ACTION_DRAG_EXITED: v.setAlpha(1.0f); return true;
                case DragEvent.ACTION_DROP:
                    if (adapter != null && adapter.isSelectionMode()) {
                        for (String path : adapter.getSelectedPaths()) {
                            shareFile(new File(path));
                        }
                    } else {
                        File file = (File) event.getLocalState();
                        shareFile(file);
                    }
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    shareDropZone.setVisibility(View.GONE);

                    v.setAlpha(1.0f);
                    return true;
                default: return false;
            }
        });



        zoneMoveToHome.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED: return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundColor(android.graphics.Color.parseColor("#BBDEFB"));
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setBackgroundColor(android.graphics.Color.parseColor("#E3F2FD"));
                    return true;
                case DragEvent.ACTION_DROP:
                    // Process ALL items from ClipData (batch support)
                    ClipData clipData = event.getClipData();
                    int itemCount = clipData.getItemCount();
                    int successCount = 0;
                    
                    for (int i = 0; i < itemCount; i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        String sourcePath = item.getText().toString();
                        File file = new File(sourcePath);
                        
                        if (moveFileToFolder(file, resumesDir)) {
                            successCount++;
                        }
                    }
                    
                    if (successCount > 0) {
                        loadResumes();
                        if (itemCount > 1) {
                            Toast.makeText(this, "Moved " + successCount + " items to Home", Toast.LENGTH_SHORT).show();
                            if (adapter != null) adapter.setSelectionMode(false);
                        } else {
                            Toast.makeText(this, "Moved to Home", Toast.LENGTH_SHORT).show();
                        }
                    }
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    advancedDragZones.setVisibility(View.GONE);
                    v.setBackgroundColor(android.graphics.Color.parseColor("#E3F2FD"));
                    return true;
                default: return false;
            }
        });



        zoneMoveToOtherGroup.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED: return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundColor(android.graphics.Color.parseColor("#C8E6C9"));
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setBackgroundColor(android.graphics.Color.parseColor("#E8F5E9"));
                    return true;
                case DragEvent.ACTION_DROP:
                    ClipData clipData = event.getClipData();
                    int itemCount = clipData.getItemCount();
                    if (itemCount == 0) return false;

                    List<File> dragFiles = new ArrayList<>();
                    for (int i = 0; i < itemCount; i++) {
                        dragFiles.add(new File(clipData.getItemAt(i).getText().toString()));
                    }

                    final EditText input = new EditText(this);
                    input.setHint("Group name...");
                    new AlertDialog.Builder(this)
                            .setTitle("Move to Group")
                            .setView(input)
                            .setPositiveButton("Move", (dialog, which) -> {
                                String folderName = input.getText().toString().trim();
                                if (!folderName.isEmpty()) {
                                    File folder = new File(resumesDir, folderName);
                                    if (!folder.exists()) folder.mkdirs();
                                    
                                    for (File f : dragFiles) {
                                        moveFileToFolder(f, folder);
                                    }
                                    loadResumes();
                                    if (adapter != null) adapter.setSelectionMode(false);
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    advancedDragZones.setVisibility(View.GONE);
                    v.setBackgroundColor(android.graphics.Color.parseColor("#E8F5E9"));
                    return true;
                default: return false;
            }
        });
    }
    
    private void showBatchMoveToOtherGroupDialog(ClipData clipData) {
        File[] folders = resumesDir.listFiles(File::isDirectory);
        
        if (folders == null || folders.length == 0) {
            Toast.makeText(this, "No groups available", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> folderNames = new ArrayList<>();
        List<File> folderList = new ArrayList<>();
        for (File f : folders) {
            if (!f.equals(currentDir)) {
                folderNames.add(f.getName());
                folderList.add(f);
            }
        }

        if (folderList.isEmpty()) {
            Toast.makeText(this, "No other groups available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        int itemCount = clipData.getItemCount();

        new AlertDialog.Builder(this)
            .setTitle("Move " + itemCount + " items to Group")
            .setItems(folderNames.toArray(new String[0]), (dialog, which) -> {
                File targetFolder = folderList.get(which);
                int successCount = 0;
                
                for (int i = 0; i < itemCount; i++) {
                    ClipData.Item item = clipData.getItemAt(i);
                    String sourcePath = item.getText().toString();
                    File sourceFile = new File(sourcePath);
                    
                    if (moveFileToFolder(sourceFile, targetFolder)) {
                        successCount++;
                    }
                }
                
                if (successCount > 0) {
                    Toast.makeText(this, "Moved " + successCount + " items to " + targetFolder.getName(), Toast.LENGTH_SHORT).show();
                    if (adapter != null) adapter.setSelectionMode(false);
                    loadResumes();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onUngroup(File file) {
        if (currentDir.equals(resumesDir)) return; // Already at root
        
        File parentDir = currentDir.getParentFile();
        if (moveFileToFolder(file, parentDir)) {
            loadResumes();
            Toast.makeText(this, "Moved to main list", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to ungroup", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareFile(File file) {
        try {
            // Package into .vitae (zip)
            File vitaeFile = packageAsVitae(file);
            if (vitaeFile == null) {
                Toast.makeText(this, "Error creating .vitae package", Toast.LENGTH_SHORT).show();
                return;
            }

            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", vitaeFile);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/zip"); 
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share .vitae Package"));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error sharing file", Toast.LENGTH_SHORT).show();
        }
    }

    private File packageAsVitae(File fileOrDir) {
        String baseName = fileOrDir.getName();
        if (baseName.endsWith(".json")) {
            baseName = baseName.substring(0, baseName.lastIndexOf("."));
        }
        File zipFile = new File(getCacheDir(), baseName + ".vitae");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            if (fileOrDir.isDirectory()) {
                zipFolder(fileOrDir, fileOrDir.getName(), zos);
            } else {
                // Zip JSON and its PNG
                zipFile(fileOrDir, zos);
                File thumb = new File(fileOrDir.getParentFile(), baseName + ".png");
                if (thumb.exists()) {
                    zipFile(thumb, zos);
                }
            }
            return zipFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    private void zipFolder(File folder, String parentPath, ZipOutputStream zos) throws IOException {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                zipFolder(file, parentPath + "/" + file.getName(), zos);
            } else if (file.getName().endsWith(".json") || file.getName().endsWith(".png")) {
                ZipEntry entry = new ZipEntry(parentPath + "/" + file.getName());
                zos.putNextEntry(entry);
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                }
                zos.closeEntry();
            }
        }
    }

    private void zipFile(File file, ZipOutputStream zos) throws IOException {
        ZipEntry entry = new ZipEntry(file.getName());
        zos.putNextEntry(entry);
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
        }
        zos.closeEntry();
    }

    private void importVitaeFile(Uri uri) {
        try (InputStream is = getContentResolver().openInputStream(uri);
             ZipInputStream zis = new ZipInputStream(is)) {
            
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File outFile = new File(resumesDir, entry.getName());
                if (entry.isDirectory()) {
                    outFile.mkdirs();
                } else {
                    outFile.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(outFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
            Toast.makeText(this, "Imported successfully!", Toast.LENGTH_SHORT).show();
            loadResumes();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Import failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmMultiDelete(java.util.List<String> paths) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete " + paths.size() + " items")
            .setMessage("Are you sure you want to delete all selected items?")
            .setPositiveButton("Delete All", (dialog, which) -> {
                int count = 0;
                for (String path : paths) {
                    File file = new File(path);
                    boolean deleted;
                    if (file.isDirectory()) {
                        deleted = deleteRecursive(file);
                    } else {
                        deleted = file.delete();
                        if (deleted) {
                            String baseName = file.getName();
                            if (baseName.endsWith(".json")) {
                                baseName = baseName.substring(0, baseName.lastIndexOf("."));
                                File thumb = new File(file.getParentFile(), baseName + ".png");
                                if (thumb.exists()) thumb.delete();
                            }
                        }
                    }
                    if (deleted) count++;
                }
                if (adapter != null) adapter.setSelectionMode(false);
                loadResumes();
                Toast.makeText(this, "Deleted " + count + " items", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
