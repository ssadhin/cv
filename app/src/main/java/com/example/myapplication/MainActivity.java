package com.example.myapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.animation.OvershootInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import android.widget.Toast;
import android.widget.GridLayout;
import android.widget.FrameLayout;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.CompoundButton;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

import android.content.res.ColorStateList;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import android.content.SharedPreferences;

import android.net.Uri;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.util.Log;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.graphics.Typeface;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.HorizontalScrollView;
import android.widget.SeekBar;
import android.widget.ViewFlipper;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import android.view.LayoutInflater;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.os.Handler;
import android.os.Build;
import android.view.ViewAnimationUtils;


import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

public class MainActivity extends AppCompatActivity implements ColorPickerDialogListener {

    private View currentFrameSettingsView;
    private WebView myWebView;
    private boolean isNativeEditing = false;

    // Slide Mode State
    private final Handler nativeSlideHandler = new Handler(android.os.Looper.getMainLooper());

    // Original Buttons
    private View addFab, undoFab, redoFab, editFab, printFab;
    private View undoRedoContainer, addEditContainer;
    private ImageButton editFabIcon, undoFabIcon, redoFabIcon, addFabIcon;

    private ValueCallback<Uri[]> mUploadMessage;
    private ActivityResultLauncher<String> mGetContent;
    private ActivityResultLauncher<Intent> mSignatureResultLauncher;

    private SidePanelHelper leftPanel;
    private SidePanelHelper rightPanel;
    
    private int currentHeaderDesignIdx = 0; // Page index for Header
    private int currentSectionDesignIdx = 0; // Page index for Section
    private int currentProgressDesignIdx = 0; // Page index for Progress
    
    private int currentHeaderIdx = 0; // Selected choice index
    private int currentSectionIdx = 0;
    private int currentProgressIdx = 0;

    private final String[] sectionDesignIds = {"default", "timeline", "glass", "bento"};
    private final String[] headerDesignIds = {"d1", "d2", "d3"};
    private final String[] progressDesignIds = {"bar", "circle", "plain"};
    private boolean isPickingBgImage = false;
    private boolean isPickingSectionIcon = false;
    private String currentSectionIdForIcon = null;
    private boolean isPanelSwitching = false;
    private static final String TAG = "MainActivity";
    private static final int COLOR_PICKER_ID = 1;
    private static final int HIGHLIGHT_COLOR_PICKER_ID = 2;
    private static final int BACKGROUND_COLOR_ID = 3;
    private static final int ICON_COLOR_SINGLE_ID = 4;
    private static final int ICON_COLOR_SECTION_ID = 5;
    
    // Multi-Select
    private Set<String> selectedItemIds = new HashSet<>();
    private String currentSectionIdForMultiSelect = "";

    private static final int PROFILE_FRAME_COLOR_ID = 6;
    private static final int LEFT_FRAME_COLOR_ID = 1003;
    private static final int HEADER_FRAME_COLOR_ID = 1004;
    private static final int LEFT_FRAME_BG_COLOR_ID = 1005;
    private static final int CURRENT_SECTION_BG_COLOR_ID = 7;
    
    private String currentEditingSectionId;
    private BottomSheetDialog currentSectionSettingsDialog;
    private static final int HEADER_FRAME_BG_COLOR_ID = 1006;
    
    private static final int LEFT_FRAME_COLOR_END_ID = 1007;
    private static final int HEADER_FRAME_COLOR_END_ID = 1008;
    private static final int LEFT_FRAME_BG_COLOR_END_ID = 1009;
    private static final int HEADER_FRAME_BG_COLOR_END_ID = 1010;
    private static final int HEADER_TEXT_COLOR_ID = 1013;
    private static final int LEFT_COL_BG_ID = 1014;
    private static final int ITEM_BG_COLOR_ID = 1015;
    private static final int HEADER_ICON_BG_COLOR_ID = 1016;
    private static final int HEADER_TITLE_BG_COLOR_ID = 1017;
    private static final int HEADER_ICON_FRAME_COLOR_ID = 1018;
    private static final int HEADER_TITLE_FRAME_COLOR_ID = 1019;
    private static final int SPLIT_TOP_FRAME_COLOR_ID = 1026;
    private static final int SPLIT_BOTTOM_FRAME_COLOR_ID = 1027;
    private static final int HEADER_LEFT_SHAPE_COLOR_ID = 1020;
    private static final int HEADER_RIGHT_SHAPE_COLOR_ID = 1021;
    private static final int HEADER_CONNECTOR_COLOR_ID = 1022;
    private static final int HEADER_TITLE_FONT_COLOR_ID = 1023;
    private static final int LEFT_SPLIT_COLOR_ID = 1024;
    private static final int LEFT_SPLIT_TOP_COLOR_ID = 1025;
    private static final int DROP_SHAPE_COLOR_PICKER_ID = 1030;
    private static final int DROP_SHAPE_FRAME_START_COLOR_ID = 1031;
    private static final int DROP_SHAPE_FRAME_END_COLOR_ID = 1032;

    private String pendingShapeColorId = null;
    private String pendingShapeFrameStartColorId = null;
    private String pendingShapeFrameEndColorId = null;
    private View pendingShapeFrameStartColorView = null;
    private View pendingShapeFrameEndColorView = null;
    private String pendingShapeFrameStartHex = null;
    private String pendingShapeFrameEndHex = null;

    private String currentEditingItemId = null;
    private static final String PREFS_NAME = "ResumeBuilderPrefs";
    private static final String KEY_SELECTED_FONT = "SelectedFontName";

    // Unified Unified Content Panel
    private View unifiedContentPanel;
    private View groupFormatting;
    
    // Generic Actions
    private View btnAdd, btnCopy, btnEdit, btnSwap, btnUpload;

    // ATS Health Hub
    private JSONObject currentATSReport;
    private View atsHubOverlay;
    private View atsHubPanel;
    private CardView atsBadge;
    private TextView atsBadgeScore;
    private View btnUp, btnDown, btnDelete;
    
    // Formatting Buttons
    private View btnBold, btnItalic, btnUnderline, btnStrike, btnHighlight;

    // Active State
    private View activePanel = null;
    private CardView universalToolbarContainer;
    
    // Add Feature Panel
    private CardView addFeaturePanel;
    private View btnAddExperience, btnAddEducation, btnAddSkills, btnAddProjects, btnAddLanguages, btnAddCertificates, btnAddAwards, btnAddReferences, btnChangeTemplate, btnResetAll;
    private ViewPager2 viewPager; // To access from changeNativeFont
    private AddPanelAdapter addPanelAdapter;


    private SeekBar preciseColumnWidthSlider;
    private SeekBar precisePhotoRadiusSlider;
    private TextView precisePhotoRadiusValue;
    private SeekBar preciseZoomSlider;
    private SeekBar preciseFontSizeSlider;
    private SeekBar preciseLineSpacingSlider;
    private TextView preciseZoomValue;
    private TextView preciseFontSizeValue;
    private TextView preciseLineSpacingValue;

    private View currentSliderContainer = null;
    
    // Color Picker State
    private String currentColorRequestType = "";
    private String currentHighlightColor = "#FFF176"; // Default yellow highlight color
    
    private boolean isPickingIconImage = false;
    private boolean isPickingContactIcon = false;
    private String currentIconSectionId = "";
    private int currentIconItemIndex = -1;
    
    private String currentContactSectionId;
    private int currentContactIconIndex;
    private static final int CONTACT_COLOR_ID = 1001;
    private static final int ITEM_TINT_COLOR_ID = 1002;
    private boolean isPickingHeaderBg = false;
    private boolean isPickingLeftBg = false;
    private boolean isPickingSectionBg = false;
    private String currentSectionIdForBg = null;


    private TextView cvNameDisplay;
    private String currentFilePath;
    private boolean isNewFile;
    private String pendingJsonState; // Holds JSON data read from file until WebView is ready

    // Template Selection Mode UI
    private boolean isTemplateSelectionMode = false;
    private View templateInfoPanel;
    private View templateSelectionControls;
    private View btnTemplateCancel;
    private View btnTemplateConfirm;
    private View btnCollapseInfo;
    private View infoContent;
    private View btnTemplateInfoMinimized;
    


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        cvNameDisplay = findViewById(R.id.cv_name_display);

        Log.d(TAG, "=== MainActivity onCreate ===");
        if (getIntent() != null) {
            if (getIntent().getBooleanExtra("EXTRA_IS_NEW", false)) {
                isNewFile = true;
                Log.d(TAG, "This is a NEW CV");
            } else if (getIntent().hasExtra("EXTRA_FILE_PATH")) {
                currentFilePath = getIntent().getStringExtra("EXTRA_FILE_PATH");
                Log.d(TAG, "Opening EXISTING CV from: " + currentFilePath);
                if (currentFilePath != null) {
                    loadResumeFromFile(currentFilePath);
                }
            } else if (getIntent().hasExtra("EXTRA_FROM_AI")) {
                 // Logic to handle AI Data is in setupWebView -> onPageFinished
                 Log.d(TAG, "Opened from AI Activity");
            } else {
                Log.d(TAG, "No intent extras - default initialization");
            }
        }
        updateTitleDisplay(); // Initial update
        
        View rootView = findViewById(R.id.root_container);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            adjustButtonMargins();
            return insets;
        });
        
        setupResultLaunchers();
        setupWebView();
        setupSidePanels();
        setupOriginalButtons();
        setupWizardButton(); // New
        setupActionPanels();
        setupAddFeaturePanel();
        setupATSHub();
        setupTemplateSelectionUI();

        preciseColumnWidthSlider = findViewById(R.id.precise_column_width_slider);
        if (preciseColumnWidthSlider != null) {
            preciseColumnWidthSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        if (leftPanel != null && leftPanel.columnWidthSliderContainer.getVisibility() == View.VISIBLE) {
                            leftPanel.columnWidthSlider.setProgress(progress);
                        }
                        if (rightPanel != null && rightPanel.columnWidthSliderContainer.getVisibility() == View.VISIBLE) {
                            rightPanel.columnWidthSlider.setProgress(progress);
                        }
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
        }

        preciseZoomSlider = findViewById(R.id.precise_zoom_slider);
        if (preciseZoomSlider != null) {
            // Restore saved zoom level
            SharedPreferences prefs = getPreferences(MODE_PRIVATE);
            int savedZoom = prefs.getInt("zoom_level", 15); // Default 15 (45%)
            preciseZoomSlider.setProgress(savedZoom);
            if (preciseZoomValue != null) {
                preciseZoomValue.setText((30 + savedZoom) + "%");
            }

            preciseZoomSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        // Update the panel slider
                        if (leftPanel != null && leftPanel.zoomSliderContainer.getVisibility() == View.VISIBLE) {
                            leftPanel.zoomSlider.setProgress(progress);
                        }
                        if (rightPanel != null && rightPanel.zoomSliderContainer.getVisibility() == View.VISIBLE) {
                            rightPanel.zoomSlider.setProgress(progress);
                        }
                        // Also trigger the WebView functionality directly
                        if (myWebView != null) {
                            float zoomValue = 30 + progress;
                            myWebView.evaluateJavascript("document.getElementById('fontSizeSlider').value = " + zoomValue + "; document.getElementById('fontSizeSlider').dispatchEvent(new Event('input'));", null);
                        }

                        // Save persistence
                        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
                        preferences.edit().putInt("zoom_level", progress).apply();
                    }
                    // Update the value display
                    if (preciseZoomValue != null) {
                        preciseZoomValue.setText((30 + progress) + "%");
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
        }

        preciseFontSizeSlider = findViewById(R.id.precise_font_size_slider);
        if (preciseFontSizeSlider != null) {
            preciseFontSizeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        // Update the panel slider
                        if (leftPanel != null && leftPanel.fontSizeSliderContainer.getVisibility() == View.VISIBLE) {
                            leftPanel.fontSizeSlider.setProgress(progress);
                        }
                        if (rightPanel != null && rightPanel.fontSizeSliderContainer.getVisibility() == View.VISIBLE) {
                            rightPanel.fontSizeSlider.setProgress(progress);
                        }
                        // Also trigger the WebView functionality directly
                        if (myWebView != null) {
                            float fontSize = 10 + (progress * 0.5f);
                            myWebView.evaluateJavascript("document.getElementById('globalFontSizeSlider').value = " + fontSize + "; document.getElementById('globalFontSizeSlider').dispatchEvent(new Event('input'));", null);
                        }
                    }
                    // Update the value display
                    if (preciseFontSizeValue != null) {
                        float fontSize = 10 + (progress * 0.5f);
                        preciseFontSizeValue.setText(String.format("%.1fpt", fontSize));
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
        }

        preciseLineSpacingSlider = findViewById(R.id.precise_line_spacing_slider);
        if (preciseLineSpacingSlider != null) {
            preciseLineSpacingSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        // Update the panel slider
                        if (leftPanel != null && leftPanel.lineSpacingSliderContainer.getVisibility() == View.VISIBLE) {
                            leftPanel.lineSpacingSlider.setProgress(progress);
                        }
                        if (rightPanel != null && rightPanel.lineSpacingSliderContainer.getVisibility() == View.VISIBLE) {
                            rightPanel.lineSpacingSlider.setProgress(progress);
                        }
                        // Also trigger the WebView functionality directly
                        if (myWebView != null) {
                            float spacingValue = 1 + progress;
                            myWebView.evaluateJavascript("document.getElementById('spacingSlider').value = " + spacingValue + "; document.getElementById('spacingSlider').dispatchEvent(new Event('input'));", null);
                        }
                    }
                    // Update the value display
                    if (preciseLineSpacingValue != null) {
                        preciseLineSpacingValue.setText(String.valueOf(1 + progress));
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
        }

        // Initialize ghost slider value TextViews
        preciseZoomValue = findViewById(R.id.precise_zoom_value);
        preciseFontSizeValue = findViewById(R.id.precise_font_size_value);
        preciseLineSpacingValue = findViewById(R.id.precise_line_spacing_value);
        precisePhotoRadiusValue = findViewById(R.id.precise_photo_radius_value);

        precisePhotoRadiusSlider = findViewById(R.id.precise_photo_radius_slider);
        if (precisePhotoRadiusSlider != null) {
            precisePhotoRadiusSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        if (precisePhotoRadiusValue != null) {
                            precisePhotoRadiusValue.setText(String.valueOf(progress));
                        }
                        if (myWebView != null) {
                            myWebView.evaluateJavascript("if(window.updateProfileRadius) window.updateProfileRadius(" + progress + ");", null);
                        }
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    if (precisePhotoRadiusValue != null) precisePhotoRadiusValue.setVisibility(View.VISIBLE);
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if (precisePhotoRadiusValue != null) precisePhotoRadiusValue.setVisibility(View.GONE);
                    seekBar.setVisibility(View.GONE);
                }
            });
        }

        // ATS Badge
        atsBadge = findViewById(R.id.ats_badge);
        atsBadgeScore = findViewById(R.id.ats_badge_score);
    }

    private void setupActionPanels() {
        // Universal Container & Content
        universalToolbarContainer = findViewById(R.id.universal_toolbar_container);
        unifiedContentPanel = findViewById(R.id.unified_content_panel);
        
        // Formatting Group
        groupFormatting = findViewById(R.id.group_formatting);
        btnBold = findViewById(R.id.btn_bold);
        btnItalic = findViewById(R.id.btn_italic);
        btnUnderline = findViewById(R.id.btn_underline);
        btnStrike = findViewById(R.id.btn_strike);
        btnHighlight = findViewById(R.id.btn_highlight);
        
        // Setup Formatting Listeners (Static)
        setupPanelButton(btnBold, "Bold", android.R.drawable.ic_menu_edit, v -> myWebView.evaluateJavascript("document.execCommand('bold');", null));
        setupPanelButton(btnItalic, "Italic", android.R.drawable.ic_menu_edit, v -> myWebView.evaluateJavascript("document.execCommand('italic');", null));
        setupPanelButton(btnUnderline, "Underline", android.R.drawable.ic_menu_edit, v -> myWebView.evaluateJavascript("document.execCommand('underline');", null));
        setupPanelButton(btnStrike, "Strike", android.R.drawable.ic_menu_edit, v -> myWebView.evaluateJavascript("document.execCommand('strikethrough');", null));
        setupPanelButton(btnHighlight, "Highlight", android.R.drawable.ic_menu_edit, v -> myWebView.evaluateJavascript("document.execCommand('hiliteColor', false, 'yellow');", null));

        // Generic Buttons
        btnAdd = findViewById(R.id.btn_add);
        btnCopy = findViewById(R.id.btn_copy);
        btnEdit = findViewById(R.id.btn_edit);
        btnSwap = findViewById(R.id.btn_swap);
        btnUpload = findViewById(R.id.btn_upload);
        btnUp = findViewById(R.id.btn_up);
        btnDown = findViewById(R.id.btn_down);
        btnDelete = findViewById(R.id.btn_delete);
        
        // Listeners for Generic Buttons are set dynamically in configureToolbarButtons
    }

    private void configureToolbarButtons(String type) {
        // Reset Visibility
        groupFormatting.setVisibility(View.GONE);
        btnAdd.setVisibility(View.GONE);
        btnCopy.setVisibility(View.GONE);
        btnEdit.setVisibility(View.GONE);
        btnSwap.setVisibility(View.GONE);
        btnUpload.setVisibility(View.GONE);
        btnUp.setVisibility(View.GONE);
        btnDown.setVisibility(View.GONE);
        btnDelete.setVisibility(View.GONE);

        btnUp.setOnTouchListener(null);
        btnDown.setOnTouchListener(null);

        switch (type) {
            case "text_selection": // Text Formatting selection
            groupFormatting.setVisibility(View.VISIBLE);
            
            setupPanelButton(btnBold, "Bold", R.drawable.ic_format_bold, v -> myWebView.evaluateJavascript("document.execCommand('bold', false, null); triggerAutoSave();", null));
            setupPanelButton(btnItalic, "Italic", R.drawable.ic_format_italic, v -> myWebView.evaluateJavascript("document.execCommand('italic', false, null); triggerAutoSave();", null));
            setupPanelButton(btnUnderline, "Underline", R.drawable.ic_format_underlined, v -> myWebView.evaluateJavascript("document.execCommand('underline', false, null); triggerAutoSave();", null));
            setupPanelButton(btnStrike, "Strike", R.drawable.ic_format_strikethrough, v -> myWebView.evaluateJavascript("document.execCommand('strikeThrough', false, null); triggerAutoSave();", null));
            setupPanelButton(btnHighlight, "Highlight", R.drawable.ic_highlight, v -> myWebView.evaluateJavascript("toggleHighlight(); triggerAutoSave();", null));
            
            // Add long-press listener for custom highlight color selection
            btnHighlight.setOnLongClickListener(v -> {
                try {
                    int currentColor = Color.parseColor(currentHighlightColor);
                    ColorPickerDialog.newBuilder()
                        .setColor(currentColor)
                        .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                        .setAllowPresets(true)
                        .setDialogId(HIGHLIGHT_COLOR_PICKER_ID)
                        .setShowAlphaSlider(false)
                        .show(MainActivity.this);
                } catch (Exception e) {
                    // Fallback to default yellow if parsing fails
                    ColorPickerDialog.newBuilder()
                        .setColor(0xFFFFF176)
                        .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                        .setAllowPresets(true)
                        .setDialogId(HIGHLIGHT_COLOR_PICKER_ID)
                        .setShowAlphaSlider(false)
                        .show(MainActivity.this);
                }
                return true; // Consume the long press event
            });
            break;

        case "item": // Bullet Points: Add, Up, Down, Delete
                btnAdd.setVisibility(View.VISIBLE);
                btnUp.setVisibility(View.VISIBLE);
                btnDown.setVisibility(View.VISIBLE);
                btnDelete.setVisibility(View.VISIBLE);
                
                setupPanelButton(btnAdd, "Add", R.drawable.ic_add, v -> myWebView.evaluateJavascript("console.log('Add button clicked'); if(currentEditableElement) { console.log('currentEditableElement:', currentEditableElement.tagName, currentEditableElement.className); let ul = currentEditableElement.querySelector('ul.resp-list'); if(!ul) ul = currentEditableElement.closest('ul.resp-list'); console.log('Found ul.resp-list:', ul); if(ul) { const li = document.createElement('li'); li.contentEditable = true; li.textContent = 'New bullet point...'; li.onclick = (e) => { e.stopPropagation(); currentEditableElement = li; if(window.Android) Android.showToolbar('item'); }; ul.appendChild(li); console.log('New bullet added to ul'); li.scrollIntoView({ behavior: 'smooth', block: 'nearest' }); triggerAutoSave(); } else { console.log('No ul.resp-list found'); } } else { console.log('No currentEditableElement'); }", null));
                setupPanelButton(btnUp, "Up", R.drawable.up, v -> myWebView.evaluateJavascript("if(currentEditableElement) { const prev = currentEditableElement.previousElementSibling; if(prev && prev.tagName === currentEditableElement.tagName) { currentEditableElement.parentNode.insertBefore(currentEditableElement, prev); currentEditableElement.classList.add('item-slide-from-below'); prev.classList.add('item-slide-from-above'); setTimeout(() => { currentEditableElement.classList.remove('item-slide-from-below'); prev.classList.remove('item-slide-from-above'); }, 300); triggerAutoSave(); }}", null));
                setupPanelButton(btnDown, "Down", R.drawable.down, v -> myWebView.evaluateJavascript("if(currentEditableElement) { const next = currentEditableElement.nextElementSibling; if(next && next.tagName === currentEditableElement.tagName) { currentEditableElement.parentNode.insertBefore(next, currentEditableElement); currentEditableElement.classList.add('item-slide-from-above'); next.classList.add('item-slide-from-below'); setTimeout(() => { currentEditableElement.classList.remove('item-slide-from-above'); next.classList.remove('item-slide-from-below'); }, 300); triggerAutoSave(); }}", null));
                setupPanelButton(btnDelete, "Delete", R.drawable.delete, v -> myWebView.evaluateJavascript("if(currentEditableElement) { currentEditableElement.remove(); triggerAutoSave(); removeToolbar(); }", null));
                break;
                
            case "default": // Up, Down, Delete (for subsection items)
                btnUp.setVisibility(View.VISIBLE);
                btnDown.setVisibility(View.VISIBLE);
                btnDelete.setVisibility(View.VISIBLE);
                
                setupNativeSlideMode(btnUp, "up");
                setupNativeSlideMode(btnDown, "down");
                
                setupPanelButton(btnUp, "Up", R.drawable.up, v -> myWebView.evaluateJavascript("if(currentEditableElement) { const prev = currentEditableElement.previousElementSibling; if(prev && prev.classList && (prev.classList.contains('data-table-item') || prev.classList.contains('skill-group') || prev.classList.contains('simple-list-item') || prev.classList.contains('pd-row'))) { currentEditableElement.parentNode.insertBefore(currentEditableElement, prev); currentEditableElement.classList.add('item-slide-from-below'); prev.classList.add('item-slide-from-above'); setTimeout(() => { currentEditableElement.classList.remove('item-slide-from-below'); prev.classList.remove('item-slide-from-above'); }, 300); triggerAutoSave(); }}", null));
                setupPanelButton(btnDown, "Down", R.drawable.down, v -> myWebView.evaluateJavascript("if(currentEditableElement) { const next = currentEditableElement.nextElementSibling; if(next && next.classList && (next.classList.contains('data-table-item') || next.classList.contains('skill-group') || next.classList.contains('simple-list-item') || next.classList.contains('pd-row'))) { currentEditableElement.parentNode.insertBefore(next, currentEditableElement); currentEditableElement.classList.add('item-slide-from-above'); next.classList.add('item-slide-from-below'); setTimeout(() => { currentEditableElement.classList.remove('item-slide-from-above'); next.classList.remove('item-slide-from-below'); }, 300); triggerAutoSave(); }}", null));
                setupPanelButton(btnDelete, "Delete", R.drawable.delete, v -> myWebView.evaluateJavascript("if(currentEditableElement && confirm('Delete this item?')) { currentEditableElement.remove(); triggerAutoSave(); removeToolbar(); }", null));
                break;
                
            case "section": // Add, Copy, Edit, Swap, Up, Down, Delete
                btnAdd.setVisibility(View.VISIBLE);
                btnCopy.setVisibility(View.VISIBLE);
                btnEdit.setVisibility(View.VISIBLE);
                btnSwap.setVisibility(View.VISIBLE);
                btnUp.setVisibility(View.VISIBLE);
                btnDown.setVisibility(View.VISIBLE);
                btnDelete.setVisibility(View.VISIBLE);
                
                setupNativeSlideMode(btnUp, "up");
                setupNativeSlideMode(btnDown, "down");
                
                setupPanelButton(btnAdd, "Add", R.drawable.ic_add, v -> myWebView.evaluateJavascript("console.log('WEB_DEBUG: Native Add Button Clicked'); if(currentEditableElement) { console.log('WEB_DEBUG: Calling addItemToSection for ' + currentEditableElement.dataset.type); addItemToSection(currentEditableElement); removeToolbar(); triggerAutoSave(); } else { console.error('WEB_DEBUG: No currentEditableElement'); }", null));
                setupPanelButton(btnCopy, "Copy", R.drawable.copy, v -> myWebView.evaluateJavascript("if(currentEditableElement) { const section = currentEditableElement; let clone = section.cloneNode(true); let baseId = section.id.replace(/-\\d+$/, ''); let num = 1; while(document.getElementById(baseId + '-' + num)) num++; clone.id = baseId + '-' + num; section.parentNode.insertBefore(clone, section.nextSibling); triggerAutoSave(); const h2 = clone.querySelector('h2'); if(h2) { h2.onclick = (e) => { currentEditableElement = clone; if(window.Android) Android.showToolbar('section'); }; }; if(typeof enableFreeDrag === 'function') enableFreeDrag(clone); clone.querySelectorAll('.data-table-item, .simple-list-item, .skill-group, .pd-row').forEach(item => { item.onclick = (e) => { e.stopPropagation(); currentEditableElement = item; if(window.Android) Android.showToolbar('default'); }; }); clone.querySelectorAll('li').forEach(li => { li.onclick = (e) => { e.stopPropagation(); currentEditableElement = li; if(window.Android) Android.showToolbar('item'); }; }); clone.scrollIntoView({ behavior: 'smooth', block: 'center' }); setTimeout(() => { currentEditableElement = clone; if(window.Android) Android.showToolbar('section'); }, 200); }", null));
                setupPanelButton(btnEdit, "Edit", R.drawable.edit, v -> myWebView.evaluateJavascript("if(currentEditableElement) { const sec = currentEditableElement; if(sec.dataset.editing === 'true') { sec.removeAttribute('data-editing'); sec.querySelectorAll('[contenteditable]').forEach(el => el.contentEditable = false); const h2 = sec.querySelector('h2'); if(h2) { h2.querySelectorAll('.section-icon-upload-btn, .section-gear-btn').forEach(b => b.remove()); } sec.querySelectorAll('.section-icon-file-input').forEach(i => i.remove()); triggerAutoSave(); } else { sec.dataset.editing = 'true'; sec.querySelectorAll('.table-val, .table-label, .pd-val, .pd-label, .exp-role, .skill-header, .skill-sub, .proj-desc, span:not(.table-label):not(.pd-label), .simple-list-item').forEach(el => el.contentEditable = true); const h2 = sec.querySelector('h2'); if(h2) { h2.contentEditable = true; } } const isEditing = sec.dataset.editing === 'true'; if(window.Android) Android.updateEditButton(isEditing, sec.id); }", null));
                setupPanelButton(btnSwap, "Swap", R.drawable.swap, v -> myWebView.evaluateJavascript("if(currentEditableElement) { const section = currentEditableElement; const p = section.closest('.left-column') ? document.getElementById('leftCol') : document.getElementById('rightCol'); const t = p.id === 'leftCol' ? document.getElementById('rightCol') : document.getElementById('leftCol'); t.appendChild(section); section.style.transform = 'translateY(0)'; removeToolbar(); triggerAutoSave(); }", null));
                
                // LONG PRESS SWAP for full width (Sections)
                btnSwap.setOnLongClickListener(v -> {
                    myWebView.evaluateJavascript("if(currentEditableElement) { makeSectionFullWidth(currentEditableElement); }", null);
                    return true;
                });

                setupPanelButton(btnUp, "Up", R.drawable.up, v -> myWebView.evaluateJavascript("if(currentEditableElement) { let prev = currentEditableElement.previousElementSibling; while(prev && prev.tagName === 'SECTION' && prev.dataset.layer) { prev = prev.previousElementSibling; } if(prev && prev.tagName === 'SECTION') { currentEditableElement.parentNode.insertBefore(currentEditableElement, prev); currentEditableElement.style.marginTop = ''; currentEditableElement.classList.add('item-slide-from-below'); prev.classList.add('item-slide-from-above'); setTimeout(() => { currentEditableElement.classList.remove('item-slide-from-below'); prev.classList.remove('item-slide-from-above'); }, 300); triggerAutoSave(); }}", null));
                setupPanelButton(btnDown, "Down", R.drawable.down, v -> myWebView.evaluateJavascript("if(currentEditableElement) { let next = currentEditableElement.nextElementSibling; while(next && next.tagName === 'SECTION' && next.dataset.layer) { next = next.nextElementSibling; } if(next && next.tagName === 'SECTION') { currentEditableElement.parentNode.insertBefore(next, currentEditableElement); currentEditableElement.classList.add('item-slide-from-above'); next.classList.add('item-slide-from-below'); setTimeout(() => { currentEditableElement.classList.remove('item-slide-from-above'); next.classList.remove('item-slide-from-below'); }, 300); triggerAutoSave(); }}", null));
                setupPanelButton(btnDelete, "Delete", R.drawable.delete, v -> myWebView.evaluateJavascript("if(currentEditableElement && confirm('Delete this section?')) { const targetId = currentEditableElement.id; currentEditableElement.remove(); document.querySelectorAll('#global-ui-overlay [data-target-id=\"' + targetId + '\"]').forEach(el => el.remove()); triggerAutoSave(); removeToolbar(); }", null));
                break;
                
            case "header": // Swap, Up, Down, Copy, Delete
                btnSwap.setVisibility(View.VISIBLE);
                btnUp.setVisibility(View.VISIBLE);
                btnDown.setVisibility(View.VISIBLE);
                btnCopy.setVisibility(View.VISIBLE);
                btnDelete.setVisibility(View.VISIBLE);
                
                setupPanelButton(btnSwap, "Swap", R.drawable.swap, v -> myWebView.evaluateJavascript("if(currentEditableElement) { swapHeaderItemColumn(currentEditableElement); }", null));
                
                // LONG PRESS SWAP for full width (Header Items)
                btnSwap.setOnLongClickListener(v -> {
                    myWebView.evaluateJavascript("if(currentEditableElement) { makeHeaderItemFullWidth(currentEditableElement); }", null);
                    return true;
                });

                setupPanelButton(btnUp, "Up", R.drawable.up, v -> myWebView.evaluateJavascript("if(currentEditableElement) { const prev = currentEditableElement.previousElementSibling; if(prev && prev.classList.contains('contact-item')) { currentEditableElement.parentNode.insertBefore(currentEditableElement, prev); triggerAutoSave(); }}", null));
                setupPanelButton(btnDown, "Down", R.drawable.down, v -> myWebView.evaluateJavascript("if(currentEditableElement) { const next = currentEditableElement.nextElementSibling; if(next && next.classList.contains('contact-item')) { currentEditableElement.parentNode.insertBefore(next, currentEditableElement); triggerAutoSave(); }}", null));
                setupPanelButton(btnCopy, "Copy", R.drawable.copy, v -> myWebView.evaluateJavascript("duplicateHeaderItem();", null));
                setupPanelButton(btnDelete, "Delete", R.drawable.delete, v -> myWebView.evaluateJavascript("if(currentEditableElement && confirm('Delete this item?')) { currentEditableElement.remove(); triggerAutoSave(); removeToolbar(); }", null));
                break;
                

        }
    }

    private void setupNativeSlideMode(View btn, final String direction) {
        btn.setOnTouchListener(new View.OnTouchListener() {
            private boolean longPressTriggered = false;
            private float startRawY = 0;
            private boolean hasMovedSignificant = false;
            private Runnable longPressRunnable;
            private long lastUpdateTime = 0; // PERFORMANCE: Throttle bridge calls

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        longPressTriggered = false;
                        hasMovedSignificant = false;
                        startRawY = event.getRawY();
                        lastUpdateTime = 0;
                        
                        // Local Runnable for this specific hold
                        longPressRunnable = () -> {
                            if (myWebView != null) {
                                myWebView.evaluateJavascript("if(window.isSectionLayered) window.isSectionLayered(); else false;", value -> {
                                    if ("true".equals(value)) {
                                        longPressTriggered = true;
                                        hapticFeedback();
                                        playWaterDropAnimation();
                                        myWebView.evaluateJavascript("if(window.startNativeSlide) window.startNativeSlide();", null);
                                    }
                                });
                            }
                        };
                        nativeSlideHandler.postDelayed(longPressRunnable, 500);
                        // RETURN TRUE to ensure we get subsequent MOVE and UP events
                        return true; 

                    case MotionEvent.ACTION_MOVE:
                        float deltaY = event.getRawY() - startRawY;
                        if (!hasMovedSignificant && Math.abs(deltaY) > 10) {
                            hasMovedSignificant = true;
                            if (longPressRunnable != null) {
                                nativeSlideHandler.removeCallbacks(longPressRunnable);
                                longPressRunnable = null;
                            }
                        }

                        if (longPressTriggered) {
                            long currentTime = System.currentTimeMillis();
                            // ~60fps Throttling (16ms)
                            if (currentTime - lastUpdateTime > 16) {
                                if (myWebView != null) {
                                    myWebView.evaluateJavascript("if(window.updateNativeSlide) window.updateNativeSlide(" + deltaY + ");", null);
                                }
                                lastUpdateTime = currentTime;
                            }
                        }
                        return true; 

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (longPressRunnable != null) {
                            nativeSlideHandler.removeCallbacks(longPressRunnable);
                            longPressRunnable = null;
                        }
                        
                        if (longPressTriggered) {
                            if (myWebView != null) {
                                myWebView.evaluateJavascript("if(window.endNativeSlide) window.endNativeSlide();", null);
                            }
                            longPressTriggered = false;
                        } else {
                            // If it wasn't a long-press hold, manually trigger the click
                            if (!hasMovedSignificant && event.getAction() == MotionEvent.ACTION_UP) {
                                v.performClick();
                            }
                        }
                        return true; 
                }
                return false;
            }
        });
    }

    private void setupAddFeaturePanel() {
        addFeaturePanel = findViewById(R.id.add_feature_panel);
        
        // Setup ViewPager and TabLayout
        TabLayout tabLayout = findViewById(R.id.panel_tab_layout);
        viewPager = findViewById(R.id.panel_view_pager);

        if (tabLayout != null && viewPager != null) {
            addPanelAdapter = new AddPanelAdapter();
            viewPager.setAdapter(addPanelAdapter);

            new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                switch(position) {
                    case 0: tab.setText("Sections"); break;
                    case 1: tab.setText("Template"); break;
                    case 2: tab.setText("Fonts"); break;
                    case 3: tab.setText("Customization"); break;
                }
            }).attach();
        }
    }

    // Helper methods for Panel Actions
    private void addNativeSection(String type) {
        addNativeSection(type, null);
    }

    private void addNativeSection(String type, String columnOverride) {
        if (!type.isEmpty()) {
            String js = columnOverride != null 
                ? String.format("androidAddSection('%s', '%s');", type, columnOverride)
                : String.format("androidAddSection('%s');", type);
            myWebView.evaluateJavascript(js, null);

            // Usage Tracking
            SharedPreferences prefs = getSharedPreferences("section_usage", MODE_PRIVATE);
            int count = prefs.getInt("usage_" + type, 0);
            prefs.edit()
                .putInt("usage_" + type, count + 1)
                .putLong("recent_" + type, System.currentTimeMillis())
                .apply();
        }
    }

    private void changeNativeTemplate(String templateId) {
        if (templateId.equals("sidebar")) {
             myWebView.evaluateJavascript("localStorage.setItem('target_layout', 'sidebar'); location.reload();", null);
        } else {
             myWebView.evaluateJavascript("localStorage.setItem('target_layout', 'default'); location.reload();", null);
        }
        addFeaturePanel.setVisibility(View.GONE);
        if (addFabIcon != null) animateIconSwap(addFabIcon, R.drawable.ic_add);
    }

    // Helper method to generate CV-specific font key
    private String getFontKeyForCurrentCv() {
        if (currentFilePath != null && !currentFilePath.isEmpty()) {
            return KEY_SELECTED_FONT + "_" + new java.io.File(currentFilePath).getName();
        }
        return KEY_SELECTED_FONT; // Fallback for new unsaved CVs
    }

    private void changeNativeFont(String fontName, String fontFamily, String fontUrl) {
         // Persist selection using CV-specific key
         getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putString(getFontKeyForCurrentCv(), fontName).apply();
         
         String json = "{ \"id\": \"" + fontName + "\", \"name\": \"" + fontName + "\", \"family\": \"" + fontFamily + "\", \"url\": \"" + fontUrl + "\", \"isCustom\": false }";
         json = json.replace("\"", "\\\"");
         json = json.replace("'", "\\'");
         String cvId = currentFilePath != null ? new java.io.File(currentFilePath).getName() : "";
         String fontKey = cvId.isEmpty() ? "resume_font_config" : "resume_font_config_" + cvId;
         String js = "localStorage.setItem('" + fontKey + "', '" + json + "'); window.currentCvFontKey = '" + fontKey + "'; applyFontConfig();";
         myWebView.evaluateJavascript(js, null);
         
         // Refresh panel to update indicators
         if (addFeaturePanel.getVisibility() == View.VISIBLE && viewPager != null && viewPager.getAdapter() != null) {
             viewPager.getAdapter().notifyItemChanged(2); 
         }
    }
    
    private void importCustomFont() {
         myWebView.evaluateJavascript("var name = prompt('Enter font name'); if(name) { var url = prompt('Enter URL'); if(url) { localStorage.setItem('resume_font_config', JSON.stringify({id:'custom', name:name, family: name, url:url, isCustom:true})); applyFontConfig(); } }", null);
    }

    private boolean isRightPanelActive = false; // Default: Left panel is primary/active

    private void setupSidePanels() {
        leftPanel = new SidePanelHelper(true, this::swapPanels, this::toggleSliderVisibility);
        rightPanel = new SidePanelHelper(false, this::swapPanels, this::toggleSliderVisibility);

        if (rightPanel.getPanel() != null) {
            rightPanel.getPanel().setVisibility(View.GONE);
        }
        if (rightPanel.getShowButton() != null) {
            rightPanel.getShowButton().setVisibility(View.GONE);
        }
        if (leftPanel.getShowButton() != null) {
            leftPanel.getShowButton().setVisibility(View.GONE);
        }
    }

    private void swapPanels() {
        if (isPanelSwitching) {
            return;
        }
        isPanelSwitching = true;

        final SidePanelHelper panelToHide;
        final SidePanelHelper panelToShow;

        if (leftPanel.getPanel() != null && leftPanel.getPanel().getVisibility() == View.VISIBLE) {
            panelToHide = leftPanel;
            panelToShow = rightPanel;
        } else {
            panelToHide = rightPanel;
            panelToShow = leftPanel;
        }
        
        // Update active side state
        isRightPanelActive = (panelToShow == rightPanel);

        // Reposition active toolbar if visible
        if (universalToolbarContainer != null && universalToolbarContainer.getVisibility() == View.VISIBLE) {
            updateToolbarConstraints(universalToolbarContainer);
        }

        if (panelToHide != null) {
            panelToHide.hidePanel(() -> {
                if (panelToShow != null) {
                    panelToShow.showPanel(() -> isPanelSwitching = false);
                } else {
                    isPanelSwitching = false;
                }
            });
        } else {
            isPanelSwitching = false;
        }
    }


    
    private void onNativePanelOpened() {
        hideActiveNativeToolbar();
    }

    private void hideActiveNativeToolbar() {
         if (universalToolbarContainer != null && universalToolbarContainer.getVisibility() == View.VISIBLE) {
            runOnUiThread(() -> {
                 // STAGE 1: Shrink & Fade Content (Icons)
                 if (unifiedContentPanel != null) {
                     unifiedContentPanel.animate()
                        .scaleX(0f)
                        .scaleY(0f)
                        .alpha(0f)
                        .setDuration(200)
                        .setInterpolator(new AccelerateInterpolator())
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                // STAGE 2: Shrink & Slide OUT Panel
                                // Slide down by its height (plus margins)
                                float translationY = universalToolbarContainer.getHeight() * 1.5f;

                                universalToolbarContainer.animate()
                                    .scaleX(0f)
                                    .scaleY(0f) // Shrink to nothing
                                    .translationY(translationY) // Slide Down
                                    .alpha(0f)
                                    .setDuration(300)
                                    .setInterpolator(new AccelerateInterpolator())
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                             if (universalToolbarContainer != null) {
                                                 universalToolbarContainer.setVisibility(View.GONE);
                                                 // Reset properties for next show
                                                 universalToolbarContainer.setScaleX(1f);
                                                 universalToolbarContainer.setScaleY(1f);
                                                 universalToolbarContainer.setAlpha(1f);
                                                 universalToolbarContainer.setTranslationY(0f);
                                             }
                                             if (activePanel != null) { // unifiedContentPanel
                                                 activePanel.setVisibility(View.GONE);
                                                 activePanel.setScaleX(1f);
                                                 activePanel.setScaleY(1f);
                                                 activePanel.setAlpha(1f);
                                             }
                                             activePanel = null;
                                        }
                                    });
                            }
                        });
                 } else {
                     // Fallback if content panel is null
                     universalToolbarContainer.setVisibility(View.GONE);
                     activePanel = null;
                 }
            });
         } else {
             activePanel = null;
         }
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        // CSS expects #RRGGBBAA, but Android uses #AARRGGBB. Swap them.
        String formattedColor = String.format("#%06X%02X", (0xFFFFFF & color), (color >>> 24));
        
        // Security: Check if color is fully transparent (alpha == 0)
        // This often happens if the picker returns an invalid state or if 'no color' is selected.
        // We prevent this from defaulting to completely transparent for critical backgrounds if needed.
        boolean isTransparent = (color >>> 24) == 0;

        if (dialogId == COLOR_PICKER_ID) {
            // Regular color picker for UI elements (header, columns, text, etc.)
            if (myWebView != null) {
                myWebView.evaluateJavascript("if(window.updateColorFromNative) window.updateColorFromNative('" + currentColorRequestType + "', '" + formattedColor + "');", null);
            }
        } else if (dialogId == HIGHLIGHT_COLOR_PICKER_ID) {
            // Highlight color picker
            currentHighlightColor = formattedColor;
            if (myWebView != null) {
                myWebView.evaluateJavascript("if(window.setHighlightColor) window.setHighlightColor('" + formattedColor + "');", null);
            }
        } else if (dialogId == BACKGROUND_COLOR_ID) {
            // Background color picker
            if (isTransparent) {
                Log.w(TAG, "Transparent background selected - ignoring to prevent black screening.");
                return;
            }
            if (myWebView != null) {
                myWebView.evaluateJavascript("if(window.updateBgColor) window.updateBgColor('" + formattedColor + "');", null);
            }
        } else if (dialogId == SECTION_BG_COLOR_ID) {
             if (pendingColorItem != null && pendingColorAdapter != null) {
                if (pendingColorTarget != null && !pendingColorTarget.isEmpty()) {
                    pendingColorAdapter.updateStickColor(pendingColorItem, color, pendingColorTarget);
                } else {
                    pendingColorAdapter.updateColor(pendingColorItem, color);
                }
                pendingColorItem = null;
                pendingColorAdapter = null;
                pendingColorTarget = "";
            }
        } else if (dialogId == CURRENT_SECTION_BG_COLOR_ID) {
            if (myWebView != null && currentEditingSectionId != null) {
                myWebView.evaluateJavascript("if(window.updateSectionStyle) window.updateSectionStyle('" + currentEditingSectionId + "', 'background-color', '" + formattedColor + "');", null);
            }
        } else if (dialogId == ICON_COLOR_SINGLE_ID) {
            if (myWebView != null) {
                myWebView.evaluateJavascript("if(window.handleNativeIconColor) window.handleNativeIconColor('single', '" + formattedColor + "');", null);
            }
        } else if (dialogId == ICON_COLOR_SECTION_ID) {
            if (myWebView != null) {
                myWebView.evaluateJavascript("if(window.handleNativeIconColor) window.handleNativeIconColor('section', '" + formattedColor + "');", null);
            }
        } else if (dialogId == CONTACT_COLOR_ID) {
            if (myWebView != null) {
                myWebView.evaluateJavascript("if(window.applyContactSetting) window.applyContactSetting('" + currentContactSectionId + "', 'color', '" + formattedColor + "');", null);
            }
        } else if (dialogId == ITEM_TINT_COLOR_ID) {
            if (myWebView != null) {
                myWebView.evaluateJavascript("if(window.applyContactSetting) window.applyContactSetting('" + currentContactSectionId + "', 'itemTint', {index: " + currentContactIconIndex + ", color: '" + formattedColor + "'});", null);
            }
        } else if (dialogId == PROFILE_FRAME_COLOR_ID) {
            if (myWebView != null) {
                myWebView.evaluateJavascript("if(window.updateProfileConfig) window.updateProfileConfig({frameColor:'" + formattedColor + "'});", null);
            }
        } else if (dialogId == LEFT_FRAME_COLOR_ID) {
            if (myWebView != null) {
                myWebView.evaluateJavascript("if(window.updateLeftFrameColor) window.updateLeftFrameColor('" + formattedColor + "');", null);
            }
        } else if (dialogId == LEFT_FRAME_COLOR_END_ID) {
            if (myWebView != null) {
                myWebView.evaluateJavascript("if(window.updateLeftFrameConfig) window.updateLeftFrameConfig({colorEnd:'" + formattedColor + "'});", null);
            }
        } else if (dialogId == HEADER_FRAME_COLOR_ID) {
            if (myWebView != null) {
                myWebView.evaluateJavascript("if(window.updateHeaderFrameConfig) window.updateHeaderFrameConfig({colorStart:'" + formattedColor + "'});", null);
            }
        } else if (dialogId == HEADER_FRAME_COLOR_END_ID) {
            if (myWebView != null) {
                myWebView.evaluateJavascript("if(window.updateHeaderFrameConfig) window.updateHeaderFrameConfig({colorEnd:'" + formattedColor + "'});", null);
            }
        } else if (dialogId == LEFT_FRAME_BG_COLOR_ID) {
            if (myWebView != null) {
                myWebView.evaluateJavascript("if(window.updateLeftFrameConfig) window.updateLeftFrameConfig({bgStart:'" + formattedColor + "'});", null);
            }
        } else if (dialogId == LEFT_FRAME_BG_COLOR_END_ID) {
            if (myWebView != null) {
                myWebView.evaluateJavascript("if(window.updateLeftFrameConfig) window.updateLeftFrameConfig({bgEnd:'" + formattedColor + "'});", null);
            }
        } else if (dialogId == HEADER_FRAME_BG_COLOR_ID) {
            if (myWebView != null) {
                myWebView.evaluateJavascript("if(window.updateHeaderFrameConfig) window.updateHeaderFrameConfig({bgStart:'" + formattedColor + "'});", null);
            }
        } else if (dialogId == HEADER_FRAME_BG_COLOR_END_ID) {
            if (myWebView != null) {
                myWebView.evaluateJavascript("if(window.updateHeaderFrameConfig) window.updateHeaderFrameConfig({bgEnd:'" + formattedColor + "'});", null);
            }
        } else if (dialogId == HEADER_TEXT_COLOR_ID) {
            if (myWebView != null) {
                myWebView.evaluateJavascript("if(window.updateHeaderFrameConfig) window.updateHeaderFrameConfig({textColor:'" + formattedColor + "'});", null);
            }
        } else if (dialogId == LEFT_COL_BG_ID) {
            if (myWebView != null) {
                myWebView.evaluateJavascript("if(window.updateLeftColBg) window.updateLeftColBg('" + formattedColor + "');", null);
            }
        } else if (dialogId == ITEM_BG_COLOR_ID) {
            if (myWebView != null) {
                if ("MULTI_SELECT".equals(currentEditingItemId)) {
                    for (String id : selectedItemIds) {
                        myWebView.evaluateJavascript("if(window.updateItemStyle) window.updateItemStyle('" + id + "', 'bg_color', '" + formattedColor + "');", null);
                    }
                } else if (currentEditingItemId != null) {
                     myWebView.evaluateJavascript("if(window.updateItemStyle) window.updateItemStyle('" + currentEditingItemId + "', 'bg_color', '" + formattedColor + "');", null);
                }
            }
        } else if (dialogId == HEADER_ICON_BG_COLOR_ID) {
            if (myWebView != null && currentEditingSectionId != null) {
                myWebView.evaluateJavascript("if(window.updateSectionHeaderCustomStyle) window.updateSectionHeaderCustomStyle('" + currentEditingSectionId + "', {iconBg: '" + formattedColor + "'});", null);
            }
        } else if (dialogId == HEADER_TITLE_BG_COLOR_ID) {
            if (myWebView != null && currentEditingSectionId != null) {
                myWebView.evaluateJavascript("if(window.updateSectionHeaderCustomStyle) window.updateSectionHeaderCustomStyle('" + currentEditingSectionId + "', {textBg: '" + formattedColor + "'});", null);
            }
        } else if (dialogId == HEADER_ICON_FRAME_COLOR_ID) {
            if (myWebView != null && currentEditingSectionId != null) {
                myWebView.evaluateJavascript("if(window.updateSectionHeaderCustomStyle) window.updateSectionHeaderCustomStyle('" + currentEditingSectionId + "', {iconFrameColor: '" + formattedColor + "'});", null);
            }
        } else if (dialogId == HEADER_TITLE_FRAME_COLOR_ID) {
            if (myWebView != null && currentEditingSectionId != null) {
                myWebView.evaluateJavascript("if(window.updateSectionHeaderCustomStyle) window.updateSectionHeaderCustomStyle('" + currentEditingSectionId + "', {textFrameColor: '" + formattedColor + "'});", null);
            }
        } else if (dialogId == HEADER_LEFT_SHAPE_COLOR_ID) {
            if (myWebView != null && currentEditingSectionId != null) {
                myWebView.evaluateJavascript("if(window.updateSectionHeaderCustomStyle) window.updateSectionHeaderCustomStyle('" + currentEditingSectionId + "', {textLeftColor: '" + formattedColor + "'});", null);
            }
        } else if (dialogId == HEADER_RIGHT_SHAPE_COLOR_ID) {
            if (myWebView != null && currentEditingSectionId != null) {
                myWebView.evaluateJavascript("if(window.updateSectionHeaderCustomStyle) window.updateSectionHeaderCustomStyle('" + currentEditingSectionId + "', {textRightColor: '" + formattedColor + "'});", null);
            }
        } else if (dialogId == HEADER_CONNECTOR_COLOR_ID) {
            if (myWebView != null && currentEditingSectionId != null) {
                myWebView.evaluateJavascript("if(window.updateSectionHeaderCustomStyle) window.updateSectionHeaderCustomStyle('" + currentEditingSectionId + "', {connectorColor: '" + formattedColor + "'});", null);
            }
        } else if (dialogId == HEADER_TITLE_FONT_COLOR_ID) {
            if (myWebView != null && currentEditingSectionId != null) {
                myWebView.evaluateJavascript("if(window.updateSectionHeaderCustomStyle) window.updateSectionHeaderCustomStyle('" + currentEditingSectionId + "', {titleFontColor: '" + formattedColor + "'});", null);
            }
        } else if (dialogId == LEFT_SPLIT_TOP_COLOR_ID) {
            if (myWebView != null) {
                myWebView.evaluateJavascript("document.documentElement.style.setProperty('--left-col-bg', '" + formattedColor + "');", null);
                myWebView.evaluateJavascript("document.documentElement.style.setProperty('--left-col-bg-end', '" + formattedColor + "');", null);
                myWebView.evaluateJavascript("triggerAutoSave();", null);
            }
        } else if (dialogId == LEFT_SPLIT_COLOR_ID) {
            if (myWebView != null) {
                myWebView.evaluateJavascript("document.documentElement.style.setProperty('--left-split-color', '" + formattedColor + "');", null);
                myWebView.evaluateJavascript("var col = document.getElementById('leftCol'); if(col) col.classList.add('is-split');", null);
                myWebView.evaluateJavascript("document.documentElement.style.setProperty('--left-split-angle', '180deg');", null);
                myWebView.evaluateJavascript("triggerAutoSave();", null);
            }
        } else if (dialogId == SPLIT_TOP_FRAME_COLOR_ID) {
            if (myWebView != null) {
                myWebView.evaluateJavascript("window.updateSplitFrameConfig('top', { color: '" + formattedColor + "' });", null);
            }
        } else if (dialogId == SPLIT_BOTTOM_FRAME_COLOR_ID) {
            if (myWebView != null) {
                myWebView.evaluateJavascript("window.updateSplitFrameConfig('bottom', { color: '" + formattedColor + "' });", null);
            }
        } else if (dialogId == DROP_SHAPE_COLOR_PICKER_ID) {
            if (myWebView != null && pendingShapeColorId != null) {
                myWebView.evaluateJavascript("if(window.updateDropShapeColor) window.updateDropShapeColor('" + pendingShapeColorId + "', '" + formattedColor + "');", null);
                pendingShapeColorId = null;
            }
        } else if (dialogId == DROP_SHAPE_FRAME_START_COLOR_ID) {
            if (pendingShapeFrameStartColorView != null) {
                pendingShapeFrameStartColorView.setBackgroundColor(color);
            }
            pendingShapeFrameStartHex = formattedColor;
            if (myWebView != null && pendingShapeFrameStartColorId != null && pendingShapeFrameEndHex != null) {
                // Apply immediately to WebView if we know the shape and both colors
                myWebView.evaluateJavascript("if(window.updateDropShapeFrameColors) window.updateDropShapeFrameColors('" + pendingShapeFrameStartColorId + "', '" + pendingShapeFrameStartHex + "', '" + pendingShapeFrameEndHex + "');", null);
            }
        } else if (dialogId == DROP_SHAPE_FRAME_END_COLOR_ID) {
            if (pendingShapeFrameEndColorView != null) {
                pendingShapeFrameEndColorView.setBackgroundColor(color);
            }
            pendingShapeFrameEndHex = formattedColor;
            if (myWebView != null && pendingShapeFrameEndColorId != null && pendingShapeFrameStartHex != null) {
                // Apply immediately to WebView
                myWebView.evaluateJavascript("if(window.updateDropShapeFrameColors) window.updateDropShapeFrameColors('" + pendingShapeFrameEndColorId + "', '" + pendingShapeFrameStartHex + "', '" + pendingShapeFrameEndHex + "');", null);
            }
        }
    }

    @Override
    public void onDialogDismissed(int dialogId) {

    }

    // Define a simple functional interface for the callback
    public interface SliderToggler {
        void toggle(View view);
    }

    private void toggleSliderVisibility(View sliderContainer) {
        android.util.Log.d("SliderDebug", "toggleSliderVisibility called for: " + sliderContainer.getId());
        android.util.Log.d("SliderDebug", "Current container: " + (currentSliderContainer != null ? currentSliderContainer.getId() : "null"));
        
        // If we're clicking the button for the currently open slider, just close it.
        if (currentSliderContainer != null && currentSliderContainer == sliderContainer) {
            android.util.Log.d("SliderDebug", "CLOSING slider - same container clicked");
            sliderContainer.setVisibility(View.GONE);
            currentSliderContainer = null;
            // Hide all ghost sliders
            if (preciseColumnWidthSlider != null) {
                preciseColumnWidthSlider.setVisibility(View.GONE);
            }
            if (preciseZoomSlider != null) {
                preciseZoomSlider.setVisibility(View.GONE);
                if (preciseZoomValue != null) preciseZoomValue.setVisibility(View.GONE);
            }
            if (preciseFontSizeSlider != null) {
                preciseFontSizeSlider.setVisibility(View.GONE);
                if (preciseFontSizeValue != null) preciseFontSizeValue.setVisibility(View.GONE);
            }
            if (preciseLineSpacingSlider != null) {
                preciseLineSpacingSlider.setVisibility(View.GONE);
                if (preciseLineSpacingValue != null) preciseLineSpacingValue.setVisibility(View.GONE);
            }
        } else {
            // If another slider is open, close it first.
            if (currentSliderContainer != null) {
                currentSliderContainer.setVisibility(View.GONE);
            }
            // Now open the new one.
            sliderContainer.setVisibility(View.VISIBLE);
            currentSliderContainer = sliderContainer;

            // Hide all ghost sliders first
            if (preciseColumnWidthSlider != null) {
                preciseColumnWidthSlider.setVisibility(View.GONE);
            }
            if (preciseZoomSlider != null) {
                preciseZoomSlider.setVisibility(View.GONE);
                if (preciseZoomValue != null) preciseZoomValue.setVisibility(View.GONE);
            }
            if (preciseFontSizeSlider != null) {
                preciseFontSizeSlider.setVisibility(View.GONE);
                if (preciseFontSizeValue != null) preciseFontSizeValue.setVisibility(View.GONE);
            }
            if (preciseLineSpacingSlider != null) {
                preciseLineSpacingSlider.setVisibility(View.GONE);
                if (preciseLineSpacingValue != null) preciseLineSpacingValue.setVisibility(View.GONE);
            }

            // Show the appropriate ghost slider based on which panel slider is active
            int containerId = sliderContainer.getId();
            if (containerId == R.id.column_width_slider_container_left || containerId == R.id.column_width_slider_container_right) {
                if (preciseColumnWidthSlider != null) {
                    preciseColumnWidthSlider.setVisibility(View.VISIBLE);
                }
            } else if (containerId == R.id.zoom_slider_container_left || containerId == R.id.zoom_slider_container_right) {
                if (preciseZoomSlider != null) {
                    preciseZoomSlider.setVisibility(View.VISIBLE);
                    if (preciseZoomValue != null) preciseZoomValue.setVisibility(View.VISIBLE);
                }
            } else if (containerId == R.id.font_size_slider_container_left || containerId == R.id.font_size_slider_container_right) {
                if (preciseFontSizeSlider != null) {
                    preciseFontSizeSlider.setVisibility(View.VISIBLE);
                    if (preciseFontSizeValue != null) preciseFontSizeValue.setVisibility(View.VISIBLE);
                }
            } else if (containerId == R.id.line_spacing_slider_container_left || containerId == R.id.line_spacing_slider_container_right) {
                if (preciseLineSpacingSlider != null) {
                    preciseLineSpacingSlider.setVisibility(View.VISIBLE);
                    if (preciseLineSpacingValue != null) preciseLineSpacingValue.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private class SidePanelHelper {
        private final CardView panel;
        private final View showButton, hideButton, swapButton, columnWidthButton;
        private final FrameLayout zoomSliderContainer, fontSizeSliderContainer, lineSpacingSliderContainer, columnWidthSliderContainer;
        private final SeekBar zoomSlider, fontSizeSlider, lineSpacingSlider, columnWidthSlider;
        private final TextView zoomValue, fontSizeValue, lineSpacingValue;
        private final View zoomButton, fontSizeButton, lineSpacingButton, fontColorButton, toggleIconsButton;
        private final boolean isLeft;
        private final Runnable onSwap;
        private final SliderToggler sliderToggler;

        SidePanelHelper(boolean isLeft, Runnable onSwap, SliderToggler sliderToggler) {
            this.isLeft = isLeft;
            this.onSwap = onSwap;
            this.sliderToggler = sliderToggler;

            panel = findViewById(isLeft ? R.id.side_panel_left : R.id.side_panel_right);
            showButton = findViewById(isLeft ? R.id.show_button_left : R.id.show_button_right);
            hideButton = findViewById(isLeft ? R.id.hide_button_left : R.id.hide_button_right);
            swapButton = findViewById(isLeft ? R.id.swap_button_left : R.id.swap_button_right);
            columnWidthButton = findViewById(isLeft ? R.id.column_width_button_left : R.id.column_width_button_right);

            zoomSliderContainer = findViewById(isLeft ? R.id.zoom_slider_container_left : R.id.zoom_slider_container_right);
            fontSizeSliderContainer = findViewById(isLeft ? R.id.font_size_slider_container_left : R.id.font_size_slider_container_right);
            lineSpacingSliderContainer = findViewById(isLeft ? R.id.line_spacing_slider_container_left : R.id.line_spacing_slider_container_right);
            columnWidthSliderContainer = findViewById(isLeft ? R.id.column_width_slider_container_left : R.id.column_width_slider_container_right);
            zoomSlider = findViewById(isLeft ? R.id.zoom_slider_left : R.id.zoom_slider_right);
            fontSizeSlider = findViewById(isLeft ? R.id.font_size_slider_left : R.id.font_size_slider_right);
            lineSpacingSlider = findViewById(isLeft ? R.id.line_spacing_slider_left : R.id.line_spacing_slider_right);
            columnWidthSlider = findViewById(isLeft ? R.id.column_width_slider_left : R.id.column_width_slider_right);
            zoomButton = findViewById(isLeft ? R.id.zoom_button_left : R.id.zoom_button_right);
            fontSizeButton = findViewById(isLeft ? R.id.font_size_button_left : R.id.font_size_button_right);
            lineSpacingButton = findViewById(isLeft ? R.id.line_spacing_button_left : R.id.line_spacing_button_right);
            fontColorButton = findViewById(isLeft ? R.id.font_color_button_left : R.id.font_color_button_right);
            toggleIconsButton = findViewById(isLeft ? R.id.toggle_icons_button_left : R.id.toggle_icons_button_right);
            
            zoomValue = findViewById(isLeft ? R.id.zoom_value_left : R.id.zoom_value_right);
            fontSizeValue = findViewById(isLeft ? R.id.font_size_value_left : R.id.font_size_value_right);
            lineSpacingValue = findViewById(isLeft ? R.id.line_spacing_value_left : R.id.line_spacing_value_right);

            setupPanelListeners();
            setupSeekBarListeners();
        }

        public CardView getPanel() {
            return panel;
        }

        public View getShowButton() {
            return showButton;
        }

        private void setupPanelListeners() {
            if (hideButton != null) {
                hideButton.setOnClickListener(v -> hidePanel());
            }
            if (showButton != null) {
                showButton.setOnClickListener(v -> showPanel());
            }
            if (swapButton != null) {
                swapButton.setOnClickListener(v -> onSwap.run());
            }

            setupPanelButton(toggleIconsButton, "Icons", R.drawable.iconshow, v -> {
                if (myWebView != null) {
                    myWebView.evaluateJavascript("document.getElementById('toggleIconsBtn').click();", null);
                }
            });


            setupPanelButton(columnWidthButton, "Width", R.drawable.linebreak, v -> {
                android.util.Log.d("SliderDebug", "Width button clicked");
                sliderToggler.toggle(columnWidthSliderContainer);
            });

            ImageView columnWidthIcon = columnWidthButton.findViewById(R.id.panel_button_icon);
            if (columnWidthIcon != null) {
                columnWidthIcon.setRotation(90);
            }


            setupPanelButton(zoomButton, "Zoom", R.drawable.zoom, v -> {
                android.util.Log.d("SliderDebug", "Zoom button clicked");
                sliderToggler.toggle(zoomSliderContainer);
            });
            setupPanelButton(fontSizeButton, "Font Size", R.drawable.fontsize, v -> {
                android.util.Log.d("SliderDebug", "Font Size button clicked");
                sliderToggler.toggle(fontSizeSliderContainer);
            });
            setupPanelButton(lineSpacingButton, "Spacing", R.drawable.linebreak, v -> {
                android.util.Log.d("SliderDebug", "Spacing button clicked");
                sliderToggler.toggle(lineSpacingSliderContainer);
            });
            setupPanelButton(fontColorButton, "Color", R.drawable.color, v -> openColorPicker());
        }

        public void hidePanel() {
            hidePanel(null);
        }

        public void hidePanel(Runnable onHidden) {
            if (panel == null || panel.getVisibility() == View.GONE) {
                if (onHidden != null) onHidden.run();
                return;
            }

            // If a slider is open in this panel, close it.
            if (columnWidthSliderContainer.getVisibility() == View.VISIBLE) {
                sliderToggler.toggle(columnWidthSliderContainer);
            }
            if (zoomSliderContainer.getVisibility() == View.VISIBLE) {
                sliderToggler.toggle(zoomSliderContainer);
            }
            if (fontSizeSliderContainer.getVisibility() == View.VISIBLE) {
                sliderToggler.toggle(fontSizeSliderContainer);
            }
            if (lineSpacingSliderContainer.getVisibility() == View.VISIBLE) {
                sliderToggler.toggle(lineSpacingSliderContainer);
            }


            float translationX = isLeft ? -panel.getWidth() : panel.getWidth();
            ObjectAnimator animator = ObjectAnimator.ofFloat(panel, "translationX", 0, translationX);
            animator.setDuration(300);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    panel.setVisibility(View.GONE);
                    if (onHidden != null) {
                        onHidden.run();
                    }
                }
            });
            animator.start();

            if (onHidden == null && showButton != null) {
                showButton.setVisibility(View.VISIBLE);
                ObjectAnimator.ofFloat(showButton, "alpha", 0f, 1f).setDuration(300).start();
            }
        }

        public void showPanel() {
            showPanel(null);
        }

        public void showPanel(Runnable onShown) {
            MainActivity.this.onNativePanelOpened();
            if (panel == null || panel.getVisibility() == View.VISIBLE) {
                if (onShown != null) onShown.run();
                return;
            }
            panel.setVisibility(View.VISIBLE);

            float translationX = isLeft ? -panel.getWidth() : panel.getWidth();
            panel.setTranslationX(translationX);

            ObjectAnimator animator = ObjectAnimator.ofFloat(panel, "translationX", translationX, 0);
            animator.setDuration(300);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (onShown != null) {
                        onShown.run();
                    }
                }
            });
            animator.start();

            if (showButton != null) {
                showButton.setVisibility(View.GONE);
            }
        }


        private void openColorPicker() {
            BottomSheetDialog categoryDialog = new BottomSheetDialog(MainActivity.this);
            View view = getLayoutInflater().inflate(R.layout.dialog_color_type_selector, null);
            categoryDialog.setContentView(view);

            view.findViewById(R.id.btn_select_label).setOnClickListener(v -> {
                categoryDialog.dismiss();
                openNativeColorPicker("text_label", "#1a1a1a");
            });

            view.findViewById(R.id.btn_select_general).setOnClickListener(v -> {
                categoryDialog.dismiss();
                openNativeColorPicker("text", "#333333");
            });

            categoryDialog.show();
        }

        private void setupSeekBarListeners() {
            if (zoomSlider != null) {
                zoomSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser && myWebView != null) {
                            float zoomValue = 30 + progress;
                            myWebView.evaluateJavascript("document.getElementById('fontSizeSlider').value = " + zoomValue + "; document.getElementById('fontSizeSlider').dispatchEvent(new Event('input'));", null);
                            // Also update the ghost slider if it's visible
                            if (preciseZoomSlider != null && preciseZoomSlider.getVisibility() == View.VISIBLE) {
                                preciseZoomSlider.setProgress(progress);
                            }
                        }
                        // Update the panel value display
                        if (zoomValue != null) {
                            zoomValue.setText((30 + progress) + "%");
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });
            }

            if (fontSizeSlider != null) {
                fontSizeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser && myWebView != null) {
                            float fontSize = 10 + (progress * 0.5f);
                            myWebView.evaluateJavascript("document.getElementById('globalFontSizeSlider').value = " + fontSize + "; document.getElementById('globalFontSizeSlider').dispatchEvent(new Event('input'));", null);
                            // Also update the ghost slider if it's visible
                            if (preciseFontSizeSlider != null && preciseFontSizeSlider.getVisibility() == View.VISIBLE) {
                                preciseFontSizeSlider.setProgress(progress);
                            }
                        }
                        // Update the panel value display
                        if (fontSizeValue != null) {
                            float fontSize = 10 + (progress * 0.5f);
                            fontSizeValue.setText(String.format("%.1fpt", fontSize));
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });
            }

            if (lineSpacingSlider != null) {
                lineSpacingSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser && myWebView != null) {
                            float spacingValue = 1 + progress;
                            myWebView.evaluateJavascript("document.getElementById('spacingSlider').value = " + spacingValue + "; document.getElementById('spacingSlider').dispatchEvent(new Event('input'));", null);
                            // Also update the ghost slider if it's visible
                            if (preciseLineSpacingSlider != null && preciseLineSpacingSlider.getVisibility() == View.VISIBLE) {
                                preciseLineSpacingSlider.setProgress(progress);
                            }
                        }
                        // Update the panel value display
                        if (lineSpacingValue != null) {
                            lineSpacingValue.setText(String.valueOf(1 + progress));
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });
            }

            if (columnWidthSlider != null) {
                columnWidthSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (myWebView != null) {
                            myWebView.evaluateJavascript("document.documentElement.style.setProperty('--left-width', '" + progress + "%');", null);
                            if (preciseColumnWidthSlider != null && fromUser) {
                                preciseColumnWidthSlider.setProgress(progress);
                            }
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });
            }
        }
    }

    private void setupResultLaunchers() {
        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        if (isPickingIconImage) {
                            isPickingIconImage = false;
                            handleIconImageSelection(uri);
                        } else if (isPickingContactIcon) { // Added this block
                            isPickingContactIcon = false;
                            handleContactIconImageSelection(uri);
                        } else if (isPickingHeaderBg) {
                            isPickingHeaderBg = false;
                            handleHeaderBgSelection(uri);
                        } else if (isPickingLeftBg) {
                            isPickingLeftBg = false;
                            handleLeftBgSelection(uri);
                        } else if (isPickingSectionBg) {
                            isPickingSectionBg = false;
                            handleSectionBgSelection(uri);
                        } else if (isPickingBgImage) {
                            isPickingBgImage = false;
                            updateBgImage(uri);
                        } else if (isPickingSectionIcon) {
                            isPickingSectionIcon = false;
                            handleSectionIconSelection(uri);
                        }
 else if (mUploadMessage != null) {
                            mUploadMessage.onReceiveValue(new Uri[]{uri});
                            mUploadMessage = null;
                        } else {
                            // Profile Picture Flow (mUploadMessage is null when called via launchImagePicker)
                            handleProfileImageSelection(uri);
                        }
                    }
                });

        mSignatureResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String signaturePath = result.getData().getStringExtra("signature_path");
                        Log.d(TAG, "Signature path: " + signaturePath);
                        if (signaturePath != null && myWebView != null) {
                            myWebView.evaluateJavascript("setSignatureImage('" + signaturePath + "');", null);
                        }
                    }
                }
        );
    }

    private void setupWebView() {
        myWebView = findViewById(R.id.webview);
        if (myWebView == null) {
            Log.e(TAG, "WebView not found!");
            return;
        }
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        // Inject Java Interface
        myWebView.addJavascriptInterface(new WebAppInterface(this), "Android");
        
        // Load HTML
        // myWebView.loadUrl("file:///android_asset/index.html"); // Moved for custom handling

        // Check for AI Data passed via Intent
        String aiJson = getIntent().getStringExtra("EXTRA_STEP_BY_STEP_DATA");
        String aiTemplate = getIntent().getStringExtra("EXTRA_TARGET_LAYOUT");
        String aiPath = getIntent().getStringExtra("EXTRA_GENERATED_FILEPATH");
        
        // Custom Client to inject saved zoom on load
        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                
                // 0. Update currentFilePath if provided by AI
                if (aiPath != null) {
                    currentFilePath = aiPath;
                }

                // 1. Load AI Data (if exists)
                if (aiJson != null && !aiJson.isEmpty()) {
                    Log.d(TAG, "Injecting AI JSON data...");
                    String safeJson = aiJson.replace("'", "\\'").replace("\n", "\\n"); // Better escaping
                    myWebView.evaluateJavascript("window.loadResumeData('" + safeJson + "');", null);
                } else if (currentFilePath != null) {
                    loadResumeFromFile(currentFilePath);
                }

                // 2. Apply Template (if exists)
                if (aiTemplate != null && !aiTemplate.isEmpty()) {
                     Log.d(TAG, "Applying AI Template: " + aiTemplate);
                     // Map IDs just in case, or pass directly if matching JS
                     // sectionDesignIds = {"default", "timeline", "glass", "bento"};
                     myWebView.evaluateJavascript("if(window.updateSectionDesign) window.updateSectionDesign('" + aiTemplate + "');", null);
                     
                     // Update Native Index for UI consistency
                     for(int i=0; i<sectionDesignIds.length; i++) {
                         if(sectionDesignIds[i].equals(aiTemplate)) {
                             currentSectionIdx = i;
                             break;
                         }
                     }
                }
                
                // Apply saved zoom level
                SharedPreferences prefs = getPreferences(MODE_PRIVATE);
                int savedZoom = prefs.getInt("zoom_level", 15);
                float zoomValue = 30 + savedZoom;
                view.evaluateJavascript("document.getElementById('fontSizeSlider').value = " + zoomValue + "; document.getElementById('fontSizeSlider').dispatchEvent(new Event('input'));", null);
                // Clear localStorage for any CVs that were deleted from HomeActivity
                android.content.SharedPreferences builderPrefs = getSharedPreferences("ResumeBuilderPrefs", MODE_PRIVATE);
                java.util.Set<String> deletedPaths = builderPrefs.getStringSet("deleted_cv_paths", null);
                if (deletedPaths != null && !deletedPaths.isEmpty()) {
                    StringBuilder clearJs = new StringBuilder();
                    for (String dPath : deletedPaths) {
                        String sPath = dPath.replace("\\", "\\\\").replace("'", "\\'");
                        clearJs.append("(() => {")
                               .append("const pfx = 'cv_' + '").append(sPath).append("' + '_';")
                               .append("const keys = ['html', 'colors', 'metrics', 'flags', 'designs', 'background', 'sig', 'last_saved'];")
                               .append("keys.forEach(k => localStorage.removeItem(pfx + k));")
                               .append("})();");
                    }
                    view.evaluateJavascript(clearJs.toString() + "console.log('✓ Cleared localStorage for ' + " + deletedPaths.size() + " + ' deleted CVs');", null);
                    builderPrefs.edit().remove("deleted_cv_paths").apply();
                }
                
                // Inject current file path so JavaScript knows which CV file to save to
                if (currentFilePath != null) {
                    String safePath = currentFilePath.replace("\\", "\\\\").replace("'", "\\'");

                    // IMPORTANT: Clear stale localStorage BEFORE setting CURRENT_FILE_PATH
                    // because deferredReload polls for CURRENT_FILE_PATH and immediately
                    // reads localStorage once it finds it. Data must be gone first.

                    // If this is a NEW CV, clear any stale per-CV localStorage for this path
                    if (isNewFile) {
                        String clearJs = "const pfx = 'cv_' + '" + safePath + "' + '_';" +
                                       "const perCvKeys = ['html', 'colors', 'metrics', 'flags', 'designs', 'background', 'sig', 'last_saved'];" +
                                       "perCvKeys.forEach(k => localStorage.removeItem(pfx + k));" +
                                       "console.log('✓ Cleared stale localStorage for brand new CV path');";
                        view.evaluateJavascript(clearJs, null);
                    }

                    // NOW set the file path (this triggers deferredReload to find the path)
                    view.evaluateJavascript("window.CURRENT_FILE_PATH = '" + safePath + "';", null);
                    Log.d(TAG, "✓ Injected file path into WebView: " + currentFilePath);

                    // Inject CV ID for per-CV color data isolation
                    String cvId = new java.io.File(currentFilePath).getName();
                    view.evaluateJavascript("if(window.setCvId) { window.setCvId('" + cvId.replace("'", "\\'") + "'); }", null);
                    Log.d(TAG, "✓ Injected CV ID into WebView: " + cvId);
                } else {
                    view.evaluateJavascript("window.CURRENT_FILE_PATH = null;", null);
                    view.evaluateJavascript("if(window.setCvId) { window.setCvId(''); }", null);
                    Log.d(TAG, "✗ No file path - this is a new CV");
                }
                
                if (getIntent() != null && getIntent().getBooleanExtra("EXTRA_FROM_STEP_BY_STEP", false)) {
                    ArrayList<String> sections = getIntent().getStringArrayListExtra("EXTRA_STEP_BY_STEP_SECTIONS");
                    String layout = getIntent().getStringExtra("EXTRA_TARGET_LAYOUT");
                    String wizardData = getIntent().getStringExtra("EXTRA_STEP_BY_STEP_DATA");
                    String generatedPath = getIntent().getStringExtra("EXTRA_GENERATED_FILEPATH");
                    String templateJson = getIntent().getStringExtra("EXTRA_TEMPLATE_JSON");
                    
                    if (generatedPath != null) {
                        currentFilePath = generatedPath;
                        String safePath = currentFilePath.replace("\\", "\\\\").replace("'", "\\'");
                        view.evaluateJavascript("window.CURRENT_FILE_PATH = '" + safePath + "';", null);
                        Log.d(TAG, "✓ Set Auto-Generated File Path: " + currentFilePath);
                        updateTitleDisplay();
                    }

                    getIntent().removeExtra("EXTRA_FROM_STEP_BY_STEP"); // Only once

                    Log.d(TAG, "✓ Processing Step-by-Step Intent");

                    // Clear storage first to ensure a clean slate, then load the data
                    String clearStorageJs = "localStorage.removeItem('resume_app_v2_data'); " +
                            "localStorage.removeItem('resume_app_v2_colors'); " +
                            "localStorage.removeItem('resume_app_v2_metrics'); " +
                            "localStorage.removeItem('resume_app_v2_flags'); " +
                            "localStorage.removeItem('resume_app_v2_undo'); " +
                            "localStorage.removeItem('resume_app_v2_redo');";
                    
                    if (wizardData != null) {
                        if (templateJson != null) {
                            String safeTemplate = org.json.JSONObject.quote(templateJson);
                            String setupJs = "window.applyUserTemplate(" + safeTemplate + ", " + wizardData + "); ";
                            view.evaluateJavascript(clearStorageJs + setupJs, null);
                            Log.d(TAG, "✓ Applied Full Template + AI Data via applyUserTemplate");
                        } else {
                            String layoutSafe = (layout != null) ? "'" + layout + "'" : "null";
                            String setupJs = "window.loadResumeData(" + wizardData + ", " + layoutSafe + "); ";
                            view.evaluateJavascript(clearStorageJs + setupJs, null);
                            Log.d(TAG, "✓ Injected Full Wizard Data (via loadResumeData)");
                        }
                    }
 else if (sections != null) {
                        StringBuilder sb = new StringBuilder("[");
                        for (int i = 0; i < sections.size(); i++) {
                            sb.append("'").append(sections.get(i)).append("'");
                            if (i < sections.size() - 1) sb.append(",");
                        }
                        sb.append("]");
                        String setupJs = String.format("setupStepByStep(%s, '%s');", sb.toString(), layout);
                        view.evaluateJavascript(clearStorageJs + setupJs, null);
                        Log.d(TAG, "✓ Injected Section Setup JS (Fallback)");
                    } else {
                        view.evaluateJavascript(clearStorageJs + "location.reload();", null);
                    }
                    
                    isNewFile = false; // Reset if it was also a new file
                }
 else if (isNewFile) {
                    isNewFile = false; // Prevent infinite reload loop
                    Log.d(TAG, "Clearing localStorage for new CV");
                    view.evaluateJavascript("localStorage.removeItem('resume_app_v2_data'); localStorage.removeItem('resume_app_v2_colors'); localStorage.removeItem('resume_app_v2_metrics'); localStorage.removeItem('resume_app_v2_flags'); localStorage.removeItem('resume_app_v2_undo'); localStorage.removeItem('resume_app_v2_redo'); location.reload();", null);
                } else if (pendingJsonState != null) {
                    Log.d(TAG, "Loading CV data from file (" + pendingJsonState.length() + " bytes)");
                    String safeJson = pendingJsonState.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\r", "");
                    // Set CV-specific font key before loading data
                    if (currentFilePath != null) {
                        String fontKey = "resume_font_config_" + new java.io.File(currentFilePath).getName();
                        view.evaluateJavascript("window.currentCvFontKey = '" + fontKey + "';", null);
                    }
                    view.evaluateJavascript("window.loadResumeData('" + safeJson + "')", null);
                    pendingJsonState = null; // Clear after loading to prevent reuse
                    Log.d(TAG, "Cleared pendingJsonState after loading");
                } else {
                    Log.d(TAG, "No pending JSON state to load");
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("mailto:") || url.startsWith("tel:")) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                        return true;
                    } catch (Exception e) {
                        Log.e(TAG, "Error opening external URL: " + url, e);
                    }
                }
                return false;
            }
        });

        myWebView.setWebChromeClient(getChromeClient());
        myWebView.addJavascriptInterface(new WebAppInterface(this), "Android");
        // Build URL with query params so JS can read them immediately on page load
        // (evaluateJavascript from onPageFinished arrives too late - deferredReload has already run)
        StringBuilder urlBuilder = new StringBuilder("file:///android_asset/index.html");
        boolean hasParam = false;
        if (isNewFile) {
            urlBuilder.append("?isNew=true");
            hasParam = true;
        }
        if (currentFilePath != null) {
            urlBuilder.append(hasParam ? "&" : "?");
            urlBuilder.append("filePath=");
            urlBuilder.append(android.net.Uri.encode(currentFilePath));
        }
        myWebView.loadUrl(urlBuilder.toString());
    }

    private void injectJavaScript(WebView view) {
        // Intentionally left blank. The web content should explicitly call the WebAppInterface.
    }

    private void setupOriginalButtons() {
        printFab = findViewById(R.id.print_fab);
        if (printFab != null) {
            printFab.setOnClickListener(v -> {
                if (leftPanel.getPanel().getVisibility() == View.VISIBLE) {
                    leftPanel.hidePanel();
                } else if (rightPanel.getPanel().getVisibility() == View.VISIBLE) {
                    rightPanel.hidePanel();
                }
                createWebPrintJob(myWebView);
            });

            printFab.setOnLongClickListener(v -> {
                android.widget.PopupMenu popup = new android.widget.PopupMenu(this, v);
                popup.getMenu().add("Save as Template");
                popup.getMenu().add("Save as Docx");
                popup.setOnMenuItemClickListener(item -> {
                    if (item.getTitle().equals("Save as Template")) {
                        saveCurrentAsTemplate();
                    } else if (item.getTitle().equals("Save as Docx")) {
                        saveCurrentAsDocx();
                    }
                    return true;
                });
                popup.show();
                return true;
            });
        }




        undoFab = findViewById(R.id.fab_undo);
        undoRedoContainer = findViewById(R.id.undo_redo_container);
        undoRedoContainer = findViewById(R.id.undo_redo_container);
        View undoBtnView = findViewById(R.id.fab_undo_btn);
        if (undoBtnView != null) {
            undoFabIcon = (ImageButton) undoBtnView;
            undoBtnView.setOnClickListener(v -> {
                Log.d(TAG, "Native Undo Clicked");
                if (myWebView != null) {
                    myWebView.evaluateJavascript("if(window.undo) { console.log('JS Undo Calling'); window.undo(); } else { console.error('window.undo not found'); }", null);
                }
            });
        }

        View redoBtnView = findViewById(R.id.fab_redo_btn);
        if(redoBtnView != null) {
            redoFabIcon = (ImageButton) redoBtnView;
            redoBtnView.setOnClickListener(v -> {
                Log.d(TAG, "Native Redo Clicked");
                if (myWebView != null) {
                    myWebView.evaluateJavascript("if(window.redo) { console.log('JS Redo Calling'); window.redo(); } else { console.error('window.redo not found'); }", null);
                }
            });
        }



        editFab = findViewById(R.id.fab_edit);
        addEditContainer = findViewById(R.id.add_edit_container);
        if (editFab != null) {
            editFabIcon = (ImageButton) ((FrameLayout) editFab).getChildAt(1);
            editFab.setOnClickListener(v -> {
                isNativeEditing = !isNativeEditing;
                if (myWebView != null) {
                    myWebView.evaluateJavascript("toggleEditMode(" + isNativeEditing + ");", null);
                }
                updateNativeUI(isNativeEditing);
            });
        }

        addFab = findViewById(R.id.fab_add);
        addFabIcon = findViewById(R.id.fab_add_icon); // Initialize here
        if (addFab != null) {
            addFab.setVisibility(View.GONE); // Initially hidden (View Mode)
            addFab.setOnClickListener(v -> {
                if (addFeaturePanel != null) {
                    if (addFeaturePanel.getVisibility() == View.VISIBLE) {
                        // Animate close - slide down and fade out
                        addFeaturePanel.animate()
                            .alpha(0f)
                            .translationY(addFeaturePanel.getHeight())
                            .setDuration(250)
                            .setInterpolator(new AccelerateInterpolator())
                            .withEndAction(() -> {
                                addFeaturePanel.setVisibility(View.GONE);
                                addFeaturePanel.setTranslationY(0); // Reset position
                                if (addFabIcon != null) animateIconSwap(addFabIcon, R.drawable.ic_add); // Icon -> +
                            })
                            .start();
                    } else {
                        onNativePanelOpened();
                        // Animate open - slide up from bottom and fade in
                        addFeaturePanel.setVisibility(View.VISIBLE);
                        addFeaturePanel.setAlpha(0f);
                        addFeaturePanel.setTranslationY(addFeaturePanel.getHeight());
                        addFeaturePanel.bringToFront();
                        addFeaturePanel.animate()
                            .alpha(1f)
                            .translationY(0)
                            .setDuration(300)
                            .setInterpolator(new OvershootInterpolator())
                            .withEndAction(() -> {
                                if (addFabIcon != null) animateIconSwap(addFabIcon, R.drawable.ic_check_v); // Icon -> V
                                // MANDATORY REFRESH when opening to ensure UI is in sync
                                loadSectionsFromWebView();
                            })
                            .start();
                    }
                }
            });
        }
    }

    private void createWebPrintJob(WebView webView) {
        if (webView == null) {
            return;
        }
        PrintManager printManager = (PrintManager) this.getSystemService(Context.PRINT_SERVICE);
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter("MyDocument");
        String jobName = getString(R.string.app_name) + " Print Test";
        if (printManager != null) {
            PrintAttributes attributes = new PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .build();
            printManager.print(jobName, printAdapter, attributes);
        }
    }

    private void updateNativeUI(boolean isEditing) {
        if (editFabIcon != null) {
            editFabIcon.setImageResource(isEditing ? R.drawable.save : R.drawable.edit);
        }

        // Swap Buttons with Animation
        animateButtonVisibility(printFab, !isEditing);
        animateButtonVisibility(addFab, isEditing);

        // Hide Add Panel if leaving edit mode
        if (!isEditing && addFeaturePanel != null) {
            addFeaturePanel.setVisibility(View.GONE);
        }
        
        // Hide Temporary Toolbar if leaving edit mode (or entering - usually clear it)
        if (activePanel != null) {
            hideActiveNativeToolbar();
        }

        // Hide Side Panels if entering edit mode
        if (isEditing) {
            if (leftPanel != null && leftPanel.getPanel().getVisibility() == View.VISIBLE) {
                leftPanel.hidePanel();
            }
            if (rightPanel != null && rightPanel.getPanel().getVisibility() == View.VISIBLE) {
                rightPanel.hidePanel();
            }
        }
    }

    private void animateButtonVisibility(View view, boolean show) {
        if (view == null) return;
        
        // If already in desired state, do nothing (unless it's during initialization/rapid toggling, but GONE check helps)
        if (show && view.getVisibility() == View.VISIBLE && view.getAlpha() == 1f) return;
        if (!show && view.getVisibility() != View.VISIBLE) return;

        if (show) {
            view.setVisibility(View.VISIBLE);
            view.setAlpha(0f);
            view.setScaleX(1.5f);
            view.setScaleY(1.5f);
            view.setRotation(-180f);

            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.5f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.5f, 1f);
            ObjectAnimator rotate = ObjectAnimator.ofFloat(view, "rotation", -180f, 0f);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(fadeIn, scaleX, scaleY, rotate);
            animatorSet.setDuration(400);
            animatorSet.start();
        } else {
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0f);
            ObjectAnimator rotate = ObjectAnimator.ofFloat(view, "rotation", 0f, 180f);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(fadeOut, scaleX, scaleY, rotate);
            animatorSet.setDuration(300);
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.GONE);
                }
            });
            animatorSet.start();
        }
    }
    private void animateIconSwap(ImageView view, int iconRes) {
        if (view == null) return;
        
        // STAGE 1: Rotate 90deg and Shrink/Fade Out
        view.animate()
            .rotation(90f)
            .alpha(0f)
            .scaleX(0.5f)
            .scaleY(0.5f)
            .setDuration(150)
            .setInterpolator(new AccelerateInterpolator())
            .withEndAction(() -> {
                // Change Icon and RESET position for Phase 2
                view.setImageResource(iconRes);
                view.setRotation(-90f);
                view.setScaleX(1.5f);
                view.setScaleY(1.5f);
                
                // STAGE 2: Rotate back to 0deg and Grow/Fade In
                view.animate()
                    .rotation(0f)
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .setInterpolator(new OvershootInterpolator())
                    .start();
            })
            .start();
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (myWebView != null) {
            myWebView.saveState(outState);
        }
        // CRITICAL: Save currentFilePath so it persists across activity recreation
        if (currentFilePath != null) {
            outState.putString("CURRENT_FILE_PATH", currentFilePath);
            Log.d(TAG, "Saved currentFilePath to instance state: " + currentFilePath);
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (myWebView != null) {
            myWebView.restoreState(savedInstanceState);
        }
        // CRITICAL: Restore currentFilePath so subsequent saves update the correct file
        if (savedInstanceState.containsKey("CURRENT_FILE_PATH")) {
            currentFilePath = savedInstanceState.getString("CURRENT_FILE_PATH");
            Log.d(TAG, "Restored currentFilePath from instance state: " + currentFilePath);
        }
    }

    protected void openFileChooser(ValueCallback<Uri[]> uploadMsg) {
        mUploadMessage = uploadMsg;
        mGetContent.launch("image/*");
    }

    private void handleProfileImageSelection(Uri uri) {
        if (uri == null) return;
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                byte[] bytes = outputStream.toByteArray();
                String base64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                String dataUrl = "data:image/png;base64," + base64; 
                
                if (myWebView != null) {
                    myWebView.post(() -> myWebView.evaluateJavascript("if(window.setProfileImage) window.setProfileImage('" + dataUrl + "');", null));
                }
                inputStream.close();
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error handling profile image selection", e);
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleHeaderBgSelection(Uri uri) {
        if (uri == null) return;
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                byte[] bytes = outputStream.toByteArray();
                String base64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                String dataUrl = "data:image/png;base64," + base64; 
                
                if (myWebView != null) {
                    myWebView.post(() -> myWebView.evaluateJavascript("if(window.updateHeaderBgImage) window.updateHeaderBgImage('" + dataUrl + "');", null));
                }
                inputStream.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling header bg image selection", e);
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSectionIconSelection(Uri uri) {
        if (uri == null || currentSectionIdForIcon == null) return;
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                byte[] bytes = outputStream.toByteArray();
                String base64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                String dataUrl = "data:image/png;base64," + base64;
                
                if (myWebView != null) {
                    myWebView.post(() -> myWebView.evaluateJavascript("if(window.updateSectionIcon) window.updateSectionIcon('" + currentSectionIdForIcon + "', '" + dataUrl + "');", null));
                }
                inputStream.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling section icon selection", e);
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    public void pickHeaderImage() {
        isPickingHeaderBg = true;
        openFileChooser(null);
    }

    public void pickLeftImage() {
        isPickingLeftBg = true;
        openFileChooser(null);
    }

    public void pickSectionBg(String sectionId) {
        isPickingSectionBg = true;
        currentSectionIdForBg = sectionId;
        openFileChooser(null);
    }

    private void handleSectionBgSelection(Uri uri) {
        if (uri == null || currentSectionIdForBg == null) return;
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                byte[] bytes = outputStream.toByteArray();
                String base64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                String dataUrl = "data:image/png;base64," + base64;
                
                if (myWebView != null) {
                    myWebView.post(() -> myWebView.evaluateJavascript("if(window.updateSectionBgImage) window.updateSectionBgImage('" + currentSectionIdForBg + "', '" + dataUrl + "');", null));
                }
                inputStream.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling section bg image selection", e);
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleLeftBgSelection(Uri uri) {
        if (uri == null) return;
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                byte[] bytes = outputStream.toByteArray();
                String base64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                String dataUrl = "data:image/png;base64," + base64;
                
                if (myWebView != null) {
                     myWebView.post(() -> myWebView.evaluateJavascript("if(window.updateLeftBgImage) window.updateLeftBgImage('" + dataUrl + "');", null));
                }
                inputStream.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling left bg image selection", e);
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTitleDisplay() {
        if (cvNameDisplay == null) return;
        
        if (currentFilePath != null) {
            String name = new File(currentFilePath).getName();
            if (name.endsWith(".json")) name = name.substring(0, name.length() - 5);
            name = name.replace("_", " "); // Cleaner display
            cvNameDisplay.setText(name);
        } else {
            cvNameDisplay.setText("Untitled CV");
        }
    }

    private WebChromeClient getChromeClient() {
        return new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.e("WebViewConsole", consoleMessage.message() + " -- From line "
                        + consoleMessage.lineNumber() + " of "
                        + consoleMessage.sourceId());
                return true;
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                openFileChooser(filePathCallback);
                return true;
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                new AlertDialog.Builder(view.getContext())
                        .setTitle("Alert")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> result.confirm())
                        .setCancelable(false).create().show();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(view.getContext())
                        .setTitle("Confirm")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> result.confirm())
                        .setNegativeButton(android.R.string.cancel, (dialog, which) -> result.cancel())
                        .create().show();
                return true;
            }
        };
    }

    public void hapticFeedback() {
        runOnUiThread(() -> {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    v.vibrate(50);
                }
            }
        });
    }

    public void playWaterDropAnimation() {
        runOnUiThread(() -> {
            if (myWebView != null) {
                int cx = myWebView.getWidth() / 2;
                int cy = myWebView.getHeight() / 2;
                float finalRadius = (float) Math.hypot(cx, cy);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    Animator anim = ViewAnimationUtils.createCircularReveal(myWebView, cx, cy, 0, finalRadius);
                    anim.setDuration(600);
                    anim.start();
                }
            }
        });
    }

    public class WebAppInterface {
        Context mContext;

        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void saveCustomStyle(String styleName, String styleJson) {
            runOnUiThread(() -> {
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                String currentStyles = prefs.getString("SavedCustomStyles", "[]");
                try {
                    JSONArray stylesArray = new JSONArray(currentStyles);
                    JSONObject newStyle = new JSONObject();
                    newStyle.put("id", "CustomStyle_" + System.currentTimeMillis());
                    newStyle.put("name", styleName);
                    newStyle.put("props", new JSONObject(styleJson));
                    stylesArray.put(newStyle);
                    
                    prefs.edit().putString("SavedCustomStyles", stylesArray.toString()).apply();
                    Toast.makeText(MainActivity.this, "Style '" + styleName + "' saved!", Toast.LENGTH_SHORT).show();
                    
                    refreshStyleCarousel();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Error saving style", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void refreshStyleCarousel() {
            runOnUiThread(() -> {
                // If the section settings dialog is currently open for the current section, just reopen it to refresh the list
                if (currentEditingSectionId != null) {
                    if (currentSectionSettingsDialog != null && currentSectionSettingsDialog.isShowing()) {
                        currentSectionSettingsDialog.dismiss();
                    }
                    // This will recreate the dialog, thus fetching the new saved styles from SharedPreferences
                    showSectionSettingsDialog(currentEditingSectionId);
                }
            });
        }

        @JavascriptInterface
        public void onColorCorruption() {
            runOnUiThread(() -> {
                Toast.makeText(mContext, "⚠️ Color corruption detected! Auto-reverting to safe colors.", Toast.LENGTH_LONG).show();
            });
        }

        @JavascriptInterface
        public void openShapePicker(String sectionId) {
            runOnUiThread(() -> showShapePickerDialog());
        }

        @JavascriptInterface
        public void openColorPickerForShape(String shapeId, String currentColor) {
            runOnUiThread(() -> {
                pendingShapeColorId = shapeId;
                int initialColor;
                try {
                    initialColor = android.graphics.Color.parseColor(currentColor.startsWith("#") ? currentColor : "#1e3c72");
                } catch (Exception e) {
                    initialColor = android.graphics.Color.parseColor("#1e3c72");
                }
                ColorPickerDialog.newBuilder()
                        .setColor(initialColor)
                        .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                        .setAllowPresets(true)
                        .setDialogId(DROP_SHAPE_COLOR_PICKER_ID)
                        .setShowAlphaSlider(false)
                        .show(MainActivity.this);
            });
        }

        @JavascriptInterface
        public void openShapeTransformDialog(String shapeId, int currentWidth, int currentHeight, int currentRotation, int currentRadius) {
            runOnUiThread(() -> showShapeTransformDialog(shapeId, currentWidth, currentHeight, currentRotation, currentRadius));
        }

        @JavascriptInterface
        public void openShapeFrameDialog(String shapeId, int currentThickness, String startColor, String endColor, int sidesCount, String activeSidesJson) {
            runOnUiThread(() -> showShapeFrameDialog(shapeId, currentThickness, startColor, endColor, sidesCount, activeSidesJson));
        }

        @JavascriptInterface
        public void openSignatureActivity() {
            runOnUiThread(() -> {
                Log.d(TAG, "Launching SignatureActivity on main thread.");
                Intent intent = new Intent(MainActivity.this, SignatureActivity.class);
                mSignatureResultLauncher.launch(intent);
            });
        }

        @JavascriptInterface
        public void openPhotoRadiusSlider(int currentRadius) {
            // Deprecated - replaced by openProfileSettings
            openProfileSettings("smooth", currentRadius, 120, 120, 0, 0, 0, "#000000", false, true, true, true, true);
        }

        @JavascriptInterface
        public void openProfileSettings(String mode, int radius, int width, int height, int x, int y, int thickness, String frameColor, boolean noFrame, boolean roundTL, boolean roundTR, boolean roundBL, boolean roundBR) {
            MainActivity.this.openProfileSettings(mode, radius, width, height, x, y, thickness, frameColor, noFrame, roundTL, roundTR, roundBL, roundBR);
        }

        @JavascriptInterface
        public void openFrameSettings(String type, int thickness, String colorStart, String colorEnd, int radius, String sides, String bgStart, String bgEnd, String textColor, int width, int height, int blur, String scale, boolean isSplit, String splitColor, int splitPos, String splitDir, int zIndex) {
            runOnUiThread(() -> showFrameSettingsDialog(type, thickness, colorStart, colorEnd, radius, sides, bgStart, bgEnd, textColor, width, height, blur, scale, isSplit, splitColor, splitPos, splitDir, zIndex));
        }
        
        @JavascriptInterface
        public void toggleNativeFab(boolean visible) {
            runOnUiThread(() -> {
                if (isTemplateSelectionMode) return; // Ignore if in selection mode
                isNativeEditing = visible;
                updateNativeUI(visible);
            });
        }

        @JavascriptInterface
        public void onTemplateSectionsSelected(String jsonSections) {
            // Optional: called from JS if we wanted JS-initiated save
        }
        
        @JavascriptInterface
        public void updateNativeEditState(boolean isEditing) {
            runOnUiThread(() -> {
                isNativeEditing = isEditing;
                if (!isTemplateSelectionMode) {
                    updateNativeUI(isEditing);
                }
            });
        }

        @JavascriptInterface
        public void updateNativeLineSpacing(int value) {
            // This method exists for compatibility but doesn't need implementation
            // The spacing is handled directly in the WebView
        }

        @JavascriptInterface
        public void updateNativeUndoRedoState(boolean canUndo, boolean canRedo) {
            runOnUiThread(() -> {
                if (undoFabIcon != null) {
                    undoFabIcon.setEnabled(canUndo);
                    undoFabIcon.setAlpha(canUndo ? 1.0f : 0.3f);
                }
                if (redoFabIcon != null) {
                    redoFabIcon.setEnabled(canRedo);
                    redoFabIcon.setAlpha(canRedo ? 1.0f : 0.3f);
                }
                if (undoRedoContainer != null) {
                    undoRedoContainer.setVisibility(View.VISIBLE);
                }
            });
        }
        

        @JavascriptInterface
        public void saveResume(String jsonState) {
            runOnUiThread(() -> {
                if (currentFilePath == null) {
                    File dir;
                    String targetDir = getIntent().getStringExtra("EXTRA_TARGET_DIR");
                    if (targetDir != null) {
                        dir = new File(targetDir);
                    } else {
                        dir = new File(getFilesDir(), "resumes");
                    }
                    if (!dir.exists()) dir.mkdirs();
                    
                    String filename = "CV.json";
                    int genericCount = 1;
                    File genericFile;
                    do {
                        if (genericCount == 1) filename = "CV.json";
                        else filename = "CV " + genericCount + ".json";
                        genericFile = new File(dir, filename);
                        genericCount++;
                    } while (genericFile.exists());
                    try {
                        JSONObject json = new JSONObject(jsonState);
                        String name = "";
                        JSONObject hdr = json.optJSONObject("header");
                        if (hdr != null) name = hdr.optString("name", "");
                        
                        if (!name.isEmpty() && !name.equals("Your Name") && !name.equals("User")) {
                             String lastName = name.trim();
                             if (lastName.contains(" ")) {
                                 lastName = lastName.substring(lastName.lastIndexOf(" ") + 1);
                             }
                             lastName = lastName.replaceAll("[^a-zA-Z0-9]", "");
                             if (!lastName.isEmpty()) {
                                 int count = 1;
                                 File file;
                                 do {
                                     if (count == 1) {
                                         filename = String.format("%s CV.json", lastName);
                                     } else {
                                         filename = String.format("%s CV %d.json", lastName, count);
                                     }
                                     file = new File(dir, filename);
                                     count++;
                                 } while (file.exists());
                             }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Auto-name parse error", e);
                    }
                    
                    currentFilePath = new File(dir, filename).getAbsolutePath();
                    Log.d(TAG, "Creating NEW CV file: " + currentFilePath);
                    
                    // Notify JavaScript of the new file path
                    if (myWebView != null) {
                        String safePath = currentFilePath.replace("\\", "\\\\").replace("'", "\\'");
                        myWebView.evaluateJavascript("window.CURRENT_FILE_PATH = '" + safePath + "';", null);
                        // Set CV-specific font key
                        String fontKey = "resume_font_config_" + new java.io.File(currentFilePath).getName();
                        myWebView.evaluateJavascript("window.currentCvFontKey = '" + fontKey + "';", null);
                        Log.d(TAG, "Updated WebView with new file path and font key");
                    }
                } else {
                    Log.d(TAG, "Updating EXISTING CV file: " + currentFilePath);
                }

                // Check for Auto-Rename (if name changed or file is generic)
                try {
                    JSONObject json = new JSONObject(jsonState);
                    String name = "";
                    JSONObject hdr = json.optJSONObject("header");
                    if (hdr != null) name = hdr.optString("name", "");

                    if (!name.isEmpty() && !name.equals("Your Name") && !name.equals("User")) {
                         String lastName = name.trim();
                         if (lastName.contains(" ")) {
                             lastName = lastName.substring(lastName.lastIndexOf(" ") + 1);
                         }
                         lastName = lastName.replaceAll("[^a-zA-Z0-9]", "");
                         
                         if (!lastName.isEmpty()) {
                             File currentFile = new File(currentFilePath);
                             String currentName = currentFile.getName();
                             // Check if generic "Resume_" or name mismatch
                             boolean isGeneric = currentName.startsWith("Resume_") && currentName.matches("Resume_\\d+\\.json");
                             boolean nameMatch = currentName.startsWith(lastName + " CV");
                             
                             if (isGeneric || !nameMatch) {
                                 // Rename needed
                                 File dir = currentFile.getParentFile();
                                 int count = 1;
                                 File newFile;
                                 String newFilename;
                                 do {
                                     if (count == 1) {
                                         newFilename = String.format("%s CV.json", lastName);
                                     } else {
                                         newFilename = String.format("%s CV %d.json", lastName, count);
                                     }
                                     newFile = new File(dir, newFilename);
                                     count++;
                                 } while (newFile.exists() && !newFile.getAbsolutePath().equals(currentFilePath));
                                 
                                 if (!newFile.getAbsolutePath().equals(currentFilePath)) {
                                     currentFilePath = newFile.getAbsolutePath(); // Just update path for saving
                                     // If old file existed (it should), rename it to preserve linkage?
                                     // Actually FileOutputStream will create a new file if we change path.
                                     // But we want to DELETE the old generic file.
                                     
                                     if (currentFile.exists()) {
                                         currentFile.renameTo(newFile);
                                         // Rename thumb
                                         File oldThumb = new File(dir, currentName.replace(".json", ".png"));
                                         if (oldThumb.exists()) {
                                             oldThumb.renameTo(new File(dir, newFilename.replace(".json", ".png")));
                                         }
                                         Log.d(TAG, "Auto-Renamed file to: " + currentFilePath);
                                     }
                                     
                                     // Update JS
                                     if (myWebView != null) {
                                         String safePath = currentFilePath.replace("\\", "\\\\").replace("'", "\\'");
                                         myWebView.evaluateJavascript("window.CURRENT_FILE_PATH = '" + safePath + "';", null);
                                         // Update CV-specific font key after rename
                                         String fontKey = "resume_font_config_" + new java.io.File(currentFilePath).getName();
                                         myWebView.evaluateJavascript("window.currentCvFontKey = '" + fontKey + "';", null);
                                     }
                                 }
                             }
                         }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Auto-Rename check failed", e);
                }
                
                try (FileOutputStream fos = new FileOutputStream(currentFilePath)) {
                    fos.write(jsonState.getBytes());
                    Log.d(TAG, "Resume JSON saved to: " + currentFilePath);
                    
                    updateTitleDisplay();
                    
                    // Capture Screenshot
                    saveWebViewScreenshot(currentFilePath);
                    
                } catch (IOException e) {
                    Log.e(TAG, "Error saving resume", e);
                    Toast.makeText(MainActivity.this, "Error saving", Toast.LENGTH_SHORT).show();
                }
            });
        }
        



        @JavascriptInterface
        public void showToolbar(String toolbarType) {
            Log.d(TAG, "showToolbar called with type: " + toolbarType);
            runOnUiThread(() -> {
                if (!isNativeEditing) return;
                                // Active Panel is ALWAYS the Unified Content Panel
                 View panel = unifiedContentPanel;
 
                 if (panel == null || universalToolbarContainer == null) {
                     Log.e(TAG, "panel or universalToolbarContainer is NULL! Aborting showToolbar.");
                     return;
                 }
                 
                 activePanel = panel;
 
                 // Hide add feature panel when toolbar shows
                 if (addFeaturePanel != null) {
                     addFeaturePanel.setVisibility(View.GONE);
                     if (addFabIcon != null) animateIconSwap(addFabIcon, R.drawable.ic_add);
                 }
 
                 // Cancel any pending hide animations to prevent race conditions
                 universalToolbarContainer.animate().setListener(null).cancel();
 
                      // Check if Container is already visible (Switching vs Opening)
                      boolean isSwitching = universalToolbarContainer.getVisibility() == View.VISIBLE;
                      
                      if (isSwitching) {
                          // Reset properties in case we intercepted a Hide animation
                          universalToolbarContainer.setAlpha(1f);
                          universalToolbarContainer.setScaleX(1f);
                          universalToolbarContainer.setScaleY(1f);
                          universalToolbarContainer.setTranslationY(0f);
                          
                          panel.setAlpha(1f);
                          panel.setScaleX(1f);
                          panel.setScaleY(1f);
 
                          // Morph Transition (Seamless) - 500ms
                          AutoTransition transition = new AutoTransition();
                          transition.setDuration(300);
                          
                          // Animate on ROOT to capture Outer CardView resizing
                          ViewGroup root = findViewById(R.id.root_container);
                          TransitionManager.beginDelayedTransition(root, transition);
                          
                          // Apply Changes AFTER beginDelayedTransition
                          configureToolbarButtons(toolbarType);
                          updateToolbarConstraints(universalToolbarContainer);
                          
                          panel.setVisibility(View.VISIBLE);
                          universalToolbarContainer.setVisibility(View.VISIBLE);
                      } else {
                          // Opening Animation (Grow from Line) - Scale Y Only
                          configureToolbarButtons(toolbarType);
                          updateToolbarConstraints(universalToolbarContainer);
                          
                          panel.setVisibility(View.VISIBLE);
                          universalToolbarContainer.setVisibility(View.VISIBLE);
                         
                         // Start as a horizontal line
                         universalToolbarContainer.setAlpha(1f);
                         universalToolbarContainer.setScaleX(1f);
                         universalToolbarContainer.setScaleY(0f);
                         universalToolbarContainer.setTranslationY(0f);
                         if (panel != null) { // Reset content too
                             panel.setAlpha(1f);
                             panel.setScaleX(1f);
                             panel.setScaleY(1f);
                         }
                         
                         universalToolbarContainer.animate()
                            .scaleY(1f) // Grow Height
                            .setDuration(500)
                            .setInterpolator(new OvershootInterpolator())
                            .setListener(null);
                            
                         // Only animate children popping in if NOT switching
                         animatePanelChildren(panel);
                     }
                     universalToolbarContainer.bringToFront();
                 });
             }

        @JavascriptInterface
        public void updateEditButton(boolean isEditing, String sectionId) {
             runOnUiThread(() -> {
                 if (isEditing) {
                     showSectionSettingsDialog(sectionId);
                 }
             });
        }

        @JavascriptInterface
        public void onToolbarAction(String action) {
            Log.d(TAG, "onToolbarAction called: " + action);
            if ("section_delete".equals(action)) {
                // Refresh the panel state ONLY if a full section was removed
                onSectionAdded();
            }
        }

        @JavascriptInterface
        public void launchImagePicker() {
             runOnUiThread(() -> openFileChooser(null));
        }

        @JavascriptInterface
        public void openNativeColorPicker(String type, String currentColor) {
            MainActivity.this.openNativeColorPicker(type, currentColor);
        }

        @JavascriptInterface
        public void onSectionAdded() {
            // Refresh the Add Panel to update green frames and sorting
            // DELAYED to allow WebView deletion animation (400ms) to complete
            runOnUiThread(() -> {
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    if (addFeaturePanel != null && addFeaturePanel.getVisibility() == View.VISIBLE) {
                        loadSectionsFromWebView();
                    }
                }, 500); 
            });
        }

        @JavascriptInterface
        public void onATSReport(String jsonReport) {
            runOnUiThread(() -> {
                try {
                    currentATSReport = new JSONObject(jsonReport);
                    int score = currentATSReport.getInt("score");
                    updateATSBadge(score);
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing ATS report", e);
                }
            });
        }

        @JavascriptInterface
        public void onATSJobMatchResult(String jsonResult) {
            runOnUiThread(() -> {
                 if (atsHubOverlay != null && atsHubOverlay.getVisibility() == View.VISIBLE) {
                     updateATSHubJD(jsonResult);
                 }
            });
        }

        @JavascriptInterface
        public void openWizard(String structuredData) {
            runOnUiThread(() -> {
                Intent intent = new Intent(MainActivity.this, StepByStepActivity.class);
                intent.putExtra("EXTRA_START_STEP", 2); // Jump to 'Manage Sections'
                if (structuredData != null) {
                    intent.putExtra("EXTRA_INITIAL_STATE", structuredData);
                }
                startActivity(intent);
                finish();
            });
        }

        @JavascriptInterface
        public String loadUserTemplateJson(String fileName) {
            try {
                File dir = new File(getFilesDir(), "user_templates");
                File file = new File(dir, fileName);
                if (!file.exists()) return null;
                
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();
                return sb.toString();
            } catch (Exception e) {
                Log.e(TAG, "Error loading user template JSON", e);
                return null;
            }
        }

        @JavascriptInterface
        public void updateNativeDesignSelection(String category, String typeId) {
            runOnUiThread(() -> {
                Log.d(TAG, "updateNativeDesignSelection called for " + category + " with type: " + typeId);
                String[] ids;
                if (category.equals("header")) ids = headerDesignIds;
                else if (category.equals("section")) ids = sectionDesignIds;
                else if (category.equals("progress")) ids = progressDesignIds;
                else return;

                int index = -1;
                for (int i = 0; i < ids.length; i++) {
                    if (ids[i].equals(typeId)) {
                        index = i;
                        break;
                    }
                }

                if (index != -1) {
                    if (category.equals("header")) currentHeaderIdx = index;
                    else if (category.equals("section")) currentSectionIdx = index;
                    else if (category.equals("progress")) currentProgressIdx = index;

                    // Refresh the Customization tab if it's visible
                    if (addFeaturePanel != null && addFeaturePanel.getVisibility() == View.VISIBLE && viewPager != null) {
                        RecyclerView.Adapter adapter = viewPager.getAdapter();
                        if (adapter != null) {
                            adapter.notifyItemChanged(3); // Tab 3 is Customization
                        }
                    }
                }
            });
        }

        @JavascriptInterface
        public void openSectionBackgroundManager(String jsonItems) {
            runOnUiThread(() -> {
                showSectionBackgroundDialog(jsonItems);
            });
        }

        @JavascriptInterface
        public void openNameSettings(int currentRotation, int currentScale) {
            runOnUiThread(() -> {
                showNameSettingsDialog(currentRotation, currentScale);
            });
        }

        @JavascriptInterface
        public void showIconSettingsMenu(String sectionId, int itemIndex, String currentColor) {
            runOnUiThread(() -> {
                showIconSettingsDialog(sectionId, itemIndex, currentColor);
            });
        }

        @JavascriptInterface
        public void openContactSettings(String sectionId, String settingsJson) {
            runOnUiThread(() -> {
                showContactDetailsSettingsDialog(sectionId, settingsJson);
            });
        }

        @JavascriptInterface
        public void onHeaderMenuClicked() {
            runOnUiThread(() -> {
                android.widget.Toast.makeText(mContext, "Menu clicked!", android.widget.Toast.LENGTH_SHORT).show();
            });
        }

        @JavascriptInterface
        public void onHeaderResizeClicked() {
            runOnUiThread(() -> {
                android.widget.Toast.makeText(mContext, "Resize clicked!", android.widget.Toast.LENGTH_SHORT).show();
            });
        }

        @JavascriptInterface
        public void hapticFeedback() {
            MainActivity.this.hapticFeedback();
        }

        @JavascriptInterface
        public void playWaterDropAnimation() {
            MainActivity.this.playWaterDropAnimation();
        }
    }

    public void openNativeColorPicker(String type, String currentColor) {
        runOnUiThread(() -> {
            currentColorRequestType = type;
            int color = Color.BLACK;
            try {
                if (currentColor != null && !currentColor.isEmpty()) {
                    color = Color.parseColor(currentColor);
                }
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Invalid color format: " + currentColor);
            }
            
            ColorPickerDialog.newBuilder()
                    .setDialogId(COLOR_PICKER_ID)
                    .setColor(color)
                    .setShowAlphaSlider(true)
                    .show(MainActivity.this);
        });
    }

    public void openProfileSettings(String shapeMode, int radius, int width, int height, int x, int y, int thickness, String frameColor, boolean noFrame, boolean roundTL, boolean roundTR, boolean roundBL, boolean roundBR) {
        runOnUiThread(() -> {
            BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.TransparentBottomSheetDialog);
            View view = getLayoutInflater().inflate(R.layout.dialog_profile_settings, null);
            dialog.setContentView(view);

            // Limit height to 45% of screen (increased slightly for more controls)
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int maxHeight = (int) (displayMetrics.heightPixels * 0.45);
            view.getLayoutParams().height = maxHeight;

            com.google.android.material.button.MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.toggle_group_shape);
            SeekBar sliderRadius = view.findViewById(R.id.slider_radius);
            SeekBar sliderWidth = view.findViewById(R.id.slider_width);
            SeekBar sliderHeight = view.findViewById(R.id.slider_height);
            SeekBar sliderThickness = view.findViewById(R.id.slider_thickness);
            SeekBar sliderX = view.findViewById(R.id.slider_x);
            SeekBar sliderY = view.findViewById(R.id.slider_y);

            TextView txtSliderLabel = view.findViewById(R.id.txt_slider_label);
            TextView txtRadius = view.findViewById(R.id.txt_radius_val);
            
            TextView txtWidthVal = view.findViewById(R.id.txt_width_val);
            EditText inputWidthEdit = view.findViewById(R.id.input_width_edit);
            TextView txtHeightVal = view.findViewById(R.id.txt_height_val);
            EditText inputHeightEdit = view.findViewById(R.id.input_height_edit);
            
            TextView txtThickness = view.findViewById(R.id.txt_thickness_val);
            TextView txtX = view.findViewById(R.id.txt_x_val);
            TextView txtY = view.findViewById(R.id.txt_y_val);

            View colorIndicator = view.findViewById(R.id.view_frame_color_indicator);
            Button btnChangeColor = view.findViewById(R.id.btn_change_frame_color);

            // No Frame checkbox and related layouts
            CheckBox chkNoFrame = view.findViewById(R.id.chk_no_frame);
            View layoutThickness = view.findViewById(R.id.layout_thickness);
            View layoutFrameColor = view.findViewById(R.id.layout_frame_color);
            View layoutCorners = view.findViewById(R.id.layout_corners_selection);

            // Corner Checkboxes
            CheckBox chkRoundTL = view.findViewById(R.id.chk_round_tl);
            CheckBox chkRoundTR = view.findViewById(R.id.chk_round_tr);
            CheckBox chkRoundBL = view.findViewById(R.id.chk_round_bl);
            CheckBox chkRoundBR = view.findViewById(R.id.chk_round_br);

            final String[] currentFrameColor = {frameColor != null ? frameColor : ""};
            final String[] currentMode = {shapeMode != null ? shapeMode : "smooth"};
            final boolean[] currentNoFrame = {noFrame};
            final boolean[] currentRoundTL = {roundTL};
            final boolean[] currentRoundTR = {roundTR};
            final boolean[] currentRoundBL = {roundBL};
            final boolean[] currentRoundBR = {roundBR};

            // Set initial state for corners
            chkRoundTL.setChecked(roundTL);
            chkRoundTR.setChecked(roundTR);
            chkRoundBL.setChecked(roundBL);
            chkRoundBR.setChecked(roundBR);

            // Set initial state
            if ("polygon".equals(shapeMode)) {
                toggleGroup.check(R.id.btn_mode_polygon);
                txtSliderLabel.setText("Sides");
                sliderRadius.setMax(20); // Limit to 20 sides
                if (radius < 3) sliderRadius.setProgress(3);
                else sliderRadius.setProgress(Math.min(radius, 20));
            } else {
                toggleGroup.check(R.id.btn_mode_smooth);
                txtSliderLabel.setText("Radius");
                sliderRadius.setMax(100); // Back to radius max
                sliderRadius.setProgress(radius);
            }

            sliderWidth.setProgress(Math.min(width, 700));
            sliderHeight.setProgress(Math.min(height, 700));
            sliderThickness.setProgress(thickness);
            sliderX.setProgress(x + 100);
            sliderY.setProgress(y + 100);

            txtRadius.setText(String.valueOf(sliderRadius.getProgress()));
            txtWidthVal.setText(String.valueOf(width));
            inputWidthEdit.setText(String.valueOf(width));
            txtHeightVal.setText(String.valueOf(height));
            inputHeightEdit.setText(String.valueOf(height));
            txtThickness.setText(String.valueOf(thickness));
            txtX.setText(String.valueOf(x));
            txtY.setText(String.valueOf(y));

            if (currentFrameColor[0] != null && !currentFrameColor[0].isEmpty()) {
                try {
                    colorIndicator.setBackgroundColor(Color.parseColor(currentFrameColor[0]));
                } catch (Exception e) {
                    colorIndicator.setBackgroundColor(Color.BLACK);
                }
            } else {
                colorIndicator.setBackgroundColor(Color.GRAY); // Placeholder
            }

            // --- Clickable size value: tap to switch to editable mode ---
            txtWidthVal.setOnClickListener(v -> {
                txtWidthVal.setVisibility(View.GONE);
                inputWidthEdit.setVisibility(View.VISIBLE);
                inputWidthEdit.setText(txtWidthVal.getText());
                inputWidthEdit.requestFocus();
                inputWidthEdit.selectAll();
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.showSoftInput(inputWidthEdit, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            });

            inputWidthEdit.setOnFocusChangeListener((v2, hasFocus) -> {
                if (!hasFocus) {
                    int val = 120;
                    try { val = Integer.parseInt(inputWidthEdit.getText().toString().trim()); } catch (Exception e2) {}
                    if (val < 1) val = 1;
                    if (val > 700) val = 700;
                    txtWidthVal.setText(String.valueOf(val));
                    sliderWidth.setProgress(val);
                    inputWidthEdit.setVisibility(View.GONE);
                    txtWidthVal.setVisibility(View.VISIBLE);
                    
                    if (myWebView != null) {
                        int r = sliderRadius.getProgress();
                        int h = sliderHeight.getProgress();
                        int t = sliderThickness.getProgress();
                        int posX = sliderX.getProgress() - 100;
                        int posY = sliderY.getProgress() - 100;
                        String js = String.format("if(window.updateProfileConfig) window.updateProfileConfig({shapeMode:'%s', radius:%d, width:%d, height:%d, x:%d, y:%d, thickness:%d, frameColor:'%s', noFrame:%s, roundTL:%s, roundTR:%s, roundBL:%s, roundBR:%s});",
                            currentMode[0], r, val, h, posX, posY, t, currentFrameColor[0], currentNoFrame[0] ? "true" : "false", 
                            currentRoundTL[0] ? "true" : "false", currentRoundTR[0] ? "true" : "false", currentRoundBL[0] ? "true" : "false", currentRoundBR[0] ? "true" : "false");
                        myWebView.evaluateJavascript(js, null);
                    }
                }
            });

            txtHeightVal.setOnClickListener(v -> {
                txtHeightVal.setVisibility(View.GONE);
                inputHeightEdit.setVisibility(View.VISIBLE);
                inputHeightEdit.setText(txtHeightVal.getText());
                inputHeightEdit.requestFocus();
                inputHeightEdit.selectAll();
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.showSoftInput(inputHeightEdit, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            });

            inputHeightEdit.setOnFocusChangeListener((v2, hasFocus) -> {
                if (!hasFocus) {
                    int val = 120;
                    try { val = Integer.parseInt(inputHeightEdit.getText().toString().trim()); } catch (Exception e2) {}
                    if (val < 1) val = 1;
                    if (val > 700) val = 700;
                    txtHeightVal.setText(String.valueOf(val));
                    sliderHeight.setProgress(val);
                    inputHeightEdit.setVisibility(View.GONE);
                    txtHeightVal.setVisibility(View.VISIBLE);
                    
                    if (myWebView != null) {
                        int r = sliderRadius.getProgress();
                        int w = sliderWidth.getProgress();
                        int t = sliderThickness.getProgress();
                        int posX = sliderX.getProgress() - 100;
                        int posY = sliderY.getProgress() - 100;
                        String js = String.format("if(window.updateProfileConfig) window.updateProfileConfig({shapeMode:'%s', radius:%d, width:%d, height:%d, x:%d, y:%d, thickness:%d, frameColor:'%s', noFrame:%s, roundTL:%s, roundTR:%s, roundBL:%s, roundBR:%s});",
                            currentMode[0], r, w, val, posX, posY, t, currentFrameColor[0], currentNoFrame[0] ? "true" : "false", 
                            currentRoundTL[0] ? "true" : "false", currentRoundTR[0] ? "true" : "false", currentRoundBL[0] ? "true" : "false", currentRoundBR[0] ? "true" : "false");
                        myWebView.evaluateJavascript(js, null);
                    }
                }
            });

            inputWidthEdit.setOnEditorActionListener((tv, actionId, event) -> { inputWidthEdit.clearFocus(); return true; });
            inputHeightEdit.setOnEditorActionListener((tv, actionId, event) -> { inputHeightEdit.clearFocus(); return true; });

            chkNoFrame.setChecked(noFrame);
            float disabledAlpha = 0.4f;
            if (noFrame) {
                layoutThickness.setAlpha(disabledAlpha);
                layoutFrameColor.setAlpha(disabledAlpha);
                sliderThickness.setEnabled(false);
                btnChangeColor.setEnabled(false);
            }

            chkNoFrame.setOnCheckedChangeListener((buttonView, isChecked) -> {
                currentNoFrame[0] = isChecked;
                layoutThickness.setAlpha(isChecked ? disabledAlpha : 1f);
                layoutFrameColor.setAlpha(isChecked ? disabledAlpha : 1f);
                sliderThickness.setEnabled(!isChecked);
                btnChangeColor.setEnabled(!isChecked);

                if (myWebView != null) {
                    int r = sliderRadius.getProgress();
                    int w = sliderWidth.getProgress();
                    int h = sliderHeight.getProgress();
                    int t = isChecked ? 0 : sliderThickness.getProgress();
                    int posX = sliderX.getProgress() - 100;
                    int posY = sliderY.getProgress() - 100;
                    String fc = isChecked ? "" : currentFrameColor[0];
                    String js = String.format("if(window.updateProfileConfig) window.updateProfileConfig({shapeMode:'%s', radius:%d, width:%d, height:%d, x:%d, y:%d, thickness:%d, frameColor:'%s', noFrame:%s, roundTL:%s, roundTR:%s, roundBL:%s, roundBR:%s});",
                        currentMode[0], r, w, h, posX, posY, t, fc, isChecked ? "true" : "false",
                        currentRoundTL[0] ? "true" : "false", currentRoundTR[0] ? "true" : "false", currentRoundBL[0] ? "true" : "false", currentRoundBR[0] ? "true" : "false");
                    myWebView.evaluateJavascript(js, null);
                }
            });

            SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        int r = sliderRadius.getProgress();
                        if ("polygon".equals(currentMode[0]) && r < 3) {
                            r = 3;
                            sliderRadius.setProgress(3);
                        }
                        
                        int w = sliderWidth.getProgress();
                        int h = sliderHeight.getProgress();
                        int t = sliderThickness.getProgress();
                        int posX = sliderX.getProgress() - 100;
                        int posY = sliderY.getProgress() - 100;

                        txtRadius.setText(String.valueOf(r));
                        txtWidthVal.setText(String.valueOf(w));
                        txtHeightVal.setText(String.valueOf(h));
                        txtThickness.setText(String.valueOf(t));
                        txtX.setText(String.valueOf(posX));
                        txtY.setText(String.valueOf(posY));

                        if ("polygon".equals(currentMode[0])) {
                            TextView txtShape = view.findViewById(R.id.txt_shape_name);
                            txtShape.setVisibility(View.VISIBLE);
                            txtShape.setText(getShapeName(r));
                        }

                        if (myWebView != null) {
                            String js = String.format("if(window.updateProfileConfig) window.updateProfileConfig({shapeMode:'%s', radius:%d, width:%d, height:%d, x:%d, y:%d, thickness:%d, frameColor:'%s', noFrame:%s, roundTL:%s, roundTR:%s, roundBL:%s, roundBR:%s});", 
                                currentMode[0], r, w, h, posX, posY, t, currentFrameColor[0], currentNoFrame[0] ? "true" : "false",
                                currentRoundTL[0] ? "true" : "false", currentRoundTR[0] ? "true" : "false", currentRoundBL[0] ? "true" : "false", currentRoundBR[0] ? "true" : "false");
                            myWebView.evaluateJavascript(js, null);
                        }
                    }
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            };

            toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (isChecked) {
                    if (checkedId == R.id.btn_mode_smooth) {
                        currentMode[0] = "smooth";
                        txtSliderLabel.setText("Radius");
                        sliderRadius.setMax(100);
                    } else if (checkedId == R.id.btn_mode_polygon) {
                        currentMode[0] = "polygon";
                        txtSliderLabel.setText("Sides");
                        sliderRadius.setMax(20);
                        if (sliderRadius.getProgress() < 3) {
                            sliderRadius.setProgress(3);
                            txtRadius.setText("3");
                        } else if (sliderRadius.getProgress() > 20) {
                            sliderRadius.setProgress(20);
                            txtRadius.setText("20");
                        }
                    }
                    listener.onProgressChanged(sliderRadius, sliderRadius.getProgress(), true);
                }
            });

            CompoundButton.OnCheckedChangeListener cornerListener = (buttonView, isChecked) -> {
                if (buttonView.getId() == R.id.chk_round_tl) currentRoundTL[0] = isChecked;
                else if (buttonView.getId() == R.id.chk_round_tr) currentRoundTR[0] = isChecked;
                else if (buttonView.getId() == R.id.chk_round_bl) currentRoundBL[0] = isChecked;
                else if (buttonView.getId() == R.id.chk_round_br) currentRoundBR[0] = isChecked;

                if (myWebView != null) {
                    int r = sliderRadius.getProgress();
                    int w = sliderWidth.getProgress();
                    int h = sliderHeight.getProgress();
                    int t = sliderThickness.getProgress();
                    int posX = sliderX.getProgress() - 100;
                    int posY = sliderY.getProgress() - 100;
                    String js = String.format("if(window.updateProfileConfig) window.updateProfileConfig({shapeMode:'%s', radius:%d, width:%d, height:%d, x:%d, y:%d, thickness:%d, frameColor:'%s', noFrame:%s, roundTL:%s, roundTR:%s, roundBL:%s, roundBR:%s});",
                        currentMode[0], r, w, h, posX, posY, t, currentFrameColor[0], currentNoFrame[0] ? "true" : "false",
                        currentRoundTL[0] ? "true" : "false", currentRoundTR[0] ? "true" : "false", currentRoundBL[0] ? "true" : "false", currentRoundBR[0] ? "true" : "false");
                    myWebView.evaluateJavascript(js, null);
                }
            };

            chkRoundTL.setOnCheckedChangeListener(cornerListener);
            chkRoundTR.setOnCheckedChangeListener(cornerListener);
            chkRoundBL.setOnCheckedChangeListener(cornerListener);
            chkRoundBR.setOnCheckedChangeListener(cornerListener);

            btnChangeColor.setOnClickListener(v -> {
                MainActivity.this.openNativeColorPickerForProfileFrame(currentFrameColor[0]);
            });

            sliderRadius.setOnSeekBarChangeListener(listener);
            sliderWidth.setOnSeekBarChangeListener(listener);
            sliderHeight.setOnSeekBarChangeListener(listener);
            sliderThickness.setOnSeekBarChangeListener(listener);
            sliderX.setOnSeekBarChangeListener(listener);
            sliderY.setOnSeekBarChangeListener(listener);

            dialog.show();
        });
    }

    private String getShapeName(int sides) {
        if (sides <= 3) return "(Triangle)";
        if (sides == 4) return "(Diamond)";
        if (sides == 5) return "(Pentagon)";
        if (sides == 6) return "(Hexagon)";
        if (sides == 7) return "(Heptagon)";
        if (sides == 8) return "(Octagon)";
        if (sides < 12) return "(Polygon)";
        if (sides < 30) return "(Circle with lines)";
        return "(Circle)";
    }

    private void animatePanelChildren(View view) {
        if (view == null) return;
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                animatePanelChildren(group.getChildAt(i));
            }
        } else {
            // Leaf View (Icon/Text)
            view.setScaleX(0f);
            view.setScaleY(0f);
            view.setAlpha(0f);
            
            view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(200)
                .setStartDelay(100)
                .setInterpolator(new OvershootInterpolator())
                .start();
        }
    }



    private void updateToolbarConstraints(View panel) {
        if (panel == null) return;
        ConstraintLayout rootLayout = findViewById(R.id.root_container);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(rootLayout);

        // Dynamic Positioning
        if (isRightPanelActive) { 
            // Active Right -> Toolbar Left
            constraintSet.connect(panel.getId(), ConstraintSet.START, R.id.root_container, ConstraintSet.START, 16);
            constraintSet.clear(panel.getId(), ConstraintSet.END);
        } else { 
            // Active Left -> Toolbar Right
            constraintSet.connect(panel.getId(), ConstraintSet.END, R.id.root_container, ConstraintSet.END, 16);
            constraintSet.clear(panel.getId(), ConstraintSet.START);
        }
        constraintSet.applyTo(rootLayout);
    }

    private void setupPanelButton(View buttonView, String title, int iconRes, View.OnClickListener listener) {
        if (buttonView == null) {
            return;
        }
        ImageView icon = buttonView.findViewById(R.id.panel_button_icon);
        TextView text = buttonView.findViewById(R.id.panel_button_title);

        if (icon != null) {
            icon.setImageResource(iconRes);
        }
        if (text != null) {
            if (title != null && !title.isEmpty()) {
                text.setText(title);
                text.setVisibility(View.VISIBLE);
            } else {
                text.setVisibility(View.GONE);
            }
        }
        buttonView.setOnClickListener(listener);
    }

    private void adjustButtonMargins() {
        if (undoRedoContainer == null || addEditContainer == null) {
            return;
        }

        final int defaultMargin = (int) (2 * getResources().getDisplayMetrics().density);

        int navBarHeight = 0;
        WindowInsetsCompat windowInsets = ViewCompat.getRootWindowInsets(getWindow().getDecorView());
        if (windowInsets != null) {
            navBarHeight = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
        }

        int bottomMargin = defaultMargin + navBarHeight;

        setBottomMargin(undoRedoContainer, bottomMargin);
        setBottomMargin(addEditContainer, bottomMargin);
    }

    private void setBottomMargin(View view, int bottomMargin) {
        if (view != null && view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            params.bottomMargin = bottomMargin;
            view.requestLayout();
        }
    }

    private int currentAddPanelSort = 0; // 0: A-Z, 1: Usage, 2: Recent
    private boolean isAddPanelGrouped = true;
    private String addPanelSearchQuery = "";

    // --- Section Grid Logic ---
    private static class SectionItem {
        String id, name, desc, col, group;
        int iconRes;
        boolean isAdded;
        int usageCount = 0;
        long lastUsedTime = 0;
        SectionItem(String id, String name, String desc, int iconRes, boolean isAdded, String col, String group) {
            this.id = id; this.name = name; this.desc = desc; this.iconRes = iconRes; this.isAdded = isAdded;
            this.col = col; this.group = group;
        }
    }

    private String getCategoryForSection(String sectionId) {
        switch (sectionId) {
            case "nameProfessionSection":
            case "contactDetails":
            case "summarySection":
            case "personalDetails":
            case "passportDetails":
            case "visaStatus":
            case "languages":
            case "headerBoxSection":
            case "profileSection":
                return "Core Essentials";
            case "education":
            case "experience":
            case "projects":
            case "researchExp":
            case "teachingExp":
            case "internships":
            case "training":
                return "Professional Exp";
            case "skills":
            case "testScores":
            case "awards":
            case "certifications":
            case "publications":
            case "achievements":
            case "affiliations":
            case "grants":
                return "Skills & Honors";
            case "familyDetails":
            case "partnerExpectations":
            case "astrologySection":
            case "lifestyleHabits":
            case "physicalProfile":
            case "visualRegistry":
            case "activeLife":
                return "Personal & Photos";
            case "volunteer":
            case "hobbies":
            case "extra":
            case "interests":
            case "references":
            case "weblinks":
                return "Interests & More";
            case "declarationSection":
                return "Documentation";
            default:
                return "Others";
        }
    }

    private int getIconResForFontAwesome(String faIcon) {
        if (faIcon == null) return R.drawable.ic_add;
        switch (faIcon) {
            case "fa-briefcase": return R.drawable.ic_save; // Fallback
            case "fa-graduation-cap": return R.drawable.ic_save; // Fallback
            case "fa-tools": return R.drawable.ic_palette;
            case "fa-project-diagram": return R.drawable.ic_swap;
            case "fa-language": return R.drawable.ic_upload;
            case "fa-user-tie": return R.drawable.ic_edit;
            case "fa-palette": return R.drawable.ic_palette;
            case "fa-link": return R.drawable.ic_zoom;
            case "fa-id-badge": return R.drawable.ic_format_bold; // Name
            case "fa-address-book": return R.drawable.ic_format_underlined; // Contact
            case "fa-window-maximize": return R.drawable.ic_swap; // Header Box Togglebhn
            case "fa-chart-line": return R.drawable.ic_chart; // Progress
            default: return R.drawable.ic_add;
        }
    }

    private void loadSectionsFromWebView() {
        if (myWebView == null) return;
        
        SharedPreferences prefs = getSharedPreferences("section_usage", MODE_PRIVATE);

        // 1. Get ALL available sections first
        myWebView.evaluateJavascript("window.getSectionsJSON()", allSectionsJson -> {
            if (allSectionsJson == null || allSectionsJson.equals("null") || allSectionsJson.isEmpty()) return;

            // 2. Get CURRENTLY added section IDs
            myWebView.evaluateJavascript("window.getCurrentSectionIds()", currentIdsJson -> {
                try {
                    String jsonStr = allSectionsJson;
                    if (jsonStr.startsWith("\"") && jsonStr.endsWith("\"")) {
                        jsonStr = jsonStr.substring(1, jsonStr.length() - 1);
                        jsonStr = jsonStr.replace("\\\"", "\"");
                    }
                    JSONArray allArray = new JSONArray(jsonStr);
                    
                    Set<String> addedIds = new HashSet<>();
                    if (currentIdsJson != null && !currentIdsJson.equals("null") && !currentIdsJson.isEmpty()) {
                        String currentStr = currentIdsJson;
                        if (currentStr.startsWith("\"") && currentStr.endsWith("\"")) {
                            currentStr = currentStr.substring(1, currentStr.length() - 1);
                            currentStr = currentStr.replace("\\\"", "\"");
                        }
                        JSONArray currentArray = new JSONArray(currentStr);
                        for (int i = 0; i < currentArray.length(); i++) {
                            addedIds.add(currentArray.getString(i));
                        }
                    }

                    java.util.List<SectionItem> list = new java.util.ArrayList<>();
                    for (int i = 0; i < allArray.length(); i++) {
                        JSONObject obj = allArray.getJSONObject(i);
                        String id = obj.optString("id");
                        String type = obj.optString("type");
                        boolean isAdded = addedIds.contains(type) && !"blank_section".equals(type) && !"stick_section".equals(type);
                        
                        SectionItem item = new SectionItem(
                            id,
                            obj.optString("name"),
                            obj.optString("desc"),
                            getIconResForFontAwesome(obj.optString("icon")),
                            isAdded,
                            obj.optString("col", "Flexible"),
                            obj.optString("group", "Standard")
                        );
                        item.usageCount = prefs.getInt("usage_" + id, 0);
                        item.lastUsedTime = prefs.getLong("recent_" + id, 0);
                        list.add(item);
                    }
                    
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> populateSectionViews(list));
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing sections JSON: " + e.getMessage());
                }
            });
        });
    }

    private View expandedSectionView = null; // Track currently expanded view

    private void populateSectionViews(java.util.List<SectionItem> items) {
        if (addFeaturePanel == null) return;
        
        // --- 1. Filtering & Sorting ---
        java.util.List<SectionItem> filtered = new ArrayList<>();
        String query = addPanelSearchQuery.toLowerCase().trim();
        for (SectionItem item : items) {
            if (query.isEmpty() || item.name.toLowerCase().contains(query) || item.desc.toLowerCase().contains(query)) {
                filtered.add(item);
            }
        }
        
        Collections.sort(filtered, (o1, o2) -> {
            if (currentAddPanelSort == 1) { // Usage
                int cmp = Integer.compare(o2.usageCount, o1.usageCount);
                if (cmp != 0) return cmp;
            } else if (currentAddPanelSort == 2) { // Recent
                int cmp = Long.compare(o2.lastUsedTime, o1.lastUsedTime);
                if (cmp != 0) return cmp;
            }
            return o1.name.compareToIgnoreCase(o2.name);
        });

        LinearLayout catContainer = addFeaturePanel.findViewById(R.id.sections_category_container);
        if (catContainer == null) return;

        catContainer.removeAllViews();
        
        if (!isAddPanelGrouped) {
             // Show flat view (2 columns)
             View flatGrid = LayoutInflater.from(this).inflate(R.layout.container_section_grid, catContainer, false);
             catContainer.addView(flatGrid);
             LinearLayout leftCol = flatGrid.findViewById(R.id.column_left);
             LinearLayout rightCol = flatGrid.findViewById(R.id.column_right);
             for (int i = 0; i < filtered.size(); i++) {
                 View v = createSectionItemView(filtered.get(i));
                 if (i % 2 == 0) leftCol.addView(v);
                 else rightCol.addView(v);
             }
        } else {
            // Group by Category
            Map<String, List<SectionItem>> groups = new LinkedHashMap<>();
            String[] catOrder = {"Core Essentials", "Professional Exp", "Skills & Honors", "Personal & Photos", "Interests & More", "Documentation", "Others"};
            for (String cat : catOrder) groups.put(cat, new ArrayList<>());
            
            for (SectionItem item : filtered) {
                String cat = getCategoryForSection(item.id);
                groups.get(cat).add(item);
            }
            
            LayoutInflater inflater = LayoutInflater.from(this);
            java.util.List<View[]> allCats = new ArrayList<>();
            
            for (String catName : catOrder) {
                List<SectionItem> groupItems = groups.get(catName);
                if (groupItems.isEmpty()) continue;
                
                // Inflate Category Header
                View header = inflater.inflate(R.layout.item_accordion_header, catContainer, false);
                TextView txtTitle = header.findViewById(R.id.txt_header_title);
                ImageView imgChevron = header.findViewById(R.id.img_chevron);
                txtTitle.setText(catName);
                
                // Inflate Category Grid Container
                View gridContainer = inflater.inflate(R.layout.container_section_grid, catContainer, false);
                LinearLayout leftCol = gridContainer.findViewById(R.id.column_left);
                LinearLayout rightCol = gridContainer.findViewById(R.id.column_right);
                
                for (int i = 0; i < groupItems.size(); i++) {
                    View v = createSectionItemView(groupItems.get(i));
                    if (i % 2 == 0) leftCol.addView(v);
                    else rightCol.addView(v);
                }
                
                catContainer.addView(header);
                catContainer.addView(gridContainer);
                
                allCats.add(new View[]{header, gridContainer, imgChevron});
                
                gridContainer.setVisibility(View.VISIBLE);
                imgChevron.setRotation(180);
            }
        }
    }

    private View createSectionItemView(SectionItem item) {
        final String initialCol = item.col.toLowerCase().contains("left") ? "left" : "right";
        final String[] currentTargetCol = { initialCol }; 

        View v = LayoutInflater.from(this).inflate(R.layout.item_section_add, null);
        // Ensure it has layout params for vertical stacking in columns
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        v.setLayoutParams(params);

        View cardRoot = v.findViewById(R.id.card_root);
        TextView title = v.findViewById(R.id.section_title);
        TextView desc = v.findViewById(R.id.section_desc);
        ImageView icon = v.findViewById(R.id.section_icon);
        View iconContainer = v.findViewById(R.id.section_container); 
        ImageButton btnAdd = v.findViewById(R.id.btn_add_section);
        ImageView doneBadge = v.findViewById(R.id.done_badge);
        
        View placementContainer = v.findViewById(R.id.placement_container);
        TextView tvIndicator = v.findViewById(R.id.tv_placement_indicator);
        View btnToggle = v.findViewById(R.id.btn_toggle_placement);

        title.setText(item.name);
        desc.setText(item.desc);
        icon.setImageResource(item.iconRes);
        
        if (item.isAdded) {
            btnAdd.setImageResource(R.drawable.ic_check);
            btnAdd.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E0E0E0")));
            btnAdd.setImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#757575")));
            btnAdd.setEnabled(false);
            if (doneBadge != null) doneBadge.setVisibility(View.VISIBLE);
            v.setAlpha(0.6f);
        } else {
            btnAdd.setImageResource(R.drawable.ic_add);
            btnAdd.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50")));
            btnAdd.setImageTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
            btnAdd.setEnabled(true);
            if (doneBadge != null) doneBadge.setVisibility(View.GONE);
            v.setAlpha(1.0f);
        }

        if (tvIndicator != null) {
            String loc = item.col.toLowerCase();
            if (loc.contains("left")) currentTargetCol[0] = "left";
            else if (loc.contains("right")) currentTargetCol[0] = "right";
            else if (loc.contains("both")) currentTargetCol[0] = "both";
            else if (loc.contains("header")) currentTargetCol[0] = "header";
            
            tvIndicator.setText("Add to: " + (currentTargetCol[0].substring(0, 1).toUpperCase() + currentTargetCol[0].substring(1)));
        }
        
        if (btnToggle != null) {
            btnToggle.setOnClickListener(view -> {
                switch (currentTargetCol[0]) {
                    case "left": currentTargetCol[0] = "right"; break;
                    case "right": currentTargetCol[0] = "both"; break;
                    case "both": currentTargetCol[0] = "header"; break;
                    case "header": currentTargetCol[0] = "left"; break;
                    default: currentTargetCol[0] = "left";
                }
                tvIndicator.setText("Add to: " + (currentTargetCol[0].substring(0, 1).toUpperCase() + currentTargetCol[0].substring(1)));
            });
        }

        if (iconContainer != null) {
            iconContainer.setOnClickListener(view -> {
                if (item.isAdded) return;
                ViewGroup animRoot = (ViewGroup) addFeaturePanel.findViewById(R.id.panel_view_pager);
                if (animRoot == null) animRoot = addFeaturePanel; 
                TransitionManager.beginDelayedTransition(animRoot, new AutoTransition().setDuration(250));
                
                if (expandedSectionView != null && expandedSectionView != v) {
                    View otherPlacement = expandedSectionView.findViewById(R.id.placement_container);
                    MaterialCardView otherCard = expandedSectionView.findViewById(R.id.card_root);
                    if (otherPlacement != null) otherPlacement.setVisibility(View.GONE);
                    if (otherCard != null) animateCornerRadius(otherCard, 12, 28);
                }
                
                if (placementContainer.getVisibility() == View.VISIBLE) {
                    placementContainer.setVisibility(View.GONE);
                    animateCornerRadius((MaterialCardView)cardRoot, 12, 28);
                    expandedSectionView = null;
                } else {
                    placementContainer.setVisibility(View.VISIBLE);
                    animateCornerRadius((MaterialCardView)cardRoot, 28, 12);
                    expandedSectionView = v;
                }
            });
        }

        if (btnAdd != null) {
            btnAdd.setOnClickListener(view -> {
                if (!item.isAdded) {
                    addNativeSection(item.id, currentTargetCol[0]);
                    
                    // Blank and Stick sections can be added multiple times — don't disable
                    if (!"blank_section".equals(item.id) && !item.id.startsWith("blank_section") && 
                        !"stickSection".equals(item.id) && !item.id.startsWith("stickSection")) {
                        btnAdd.setImageResource(R.drawable.ic_check);
                        btnAdd.setEnabled(false);
                        v.animate().alpha(0.6f).setDuration(300).start();
                        if (doneBadge != null) doneBadge.setVisibility(View.VISIBLE);
                    }
                    
                    placementContainer.setVisibility(View.GONE);
                    animateCornerRadius((MaterialCardView)cardRoot, 12, 28);
                    expandedSectionView = null;
                }
            });
        }
        return v;
    }

    private void animateCornerRadius(MaterialCardView card, int fromDp, int toDp) {
        float density = getResources().getDisplayMetrics().density;
        ValueAnimator animator = ValueAnimator.ofFloat(fromDp * density, toDp * density);
        animator.setDuration(300);
        animator.addUpdateListener(animation -> card.setRadius((float) animation.getAnimatedValue()));
        animator.start();
    }



    // --- Inner Adapter Class ---
    private void updateSortUI(TextView az, TextView usage, TextView recent) {
        if (az == null || usage == null || recent == null) return;
        az.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor(currentAddPanelSort == 0 ? "#EEF2FF" : "#F5F5F5")));
        az.setTextColor(Color.parseColor(currentAddPanelSort == 0 ? "#1E3C72" : "#888888"));
        
        usage.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor(currentAddPanelSort == 1 ? "#EEF2FF" : "#F5F5F5")));
        usage.setTextColor(Color.parseColor(currentAddPanelSort == 1 ? "#1E3C72" : "#888888"));
        
        recent.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor(currentAddPanelSort == 2 ? "#EEF2FF" : "#F5F5F5")));
        recent.setTextColor(Color.parseColor(currentAddPanelSort == 2 ? "#1E3C72" : "#888888"));
    }

    private class AddPanelAdapter extends RecyclerView.Adapter<AddPanelAdapter.ViewHolder> {
        @Override
        public int getItemViewType(int position) { return position; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int layoutId = 0;
            if (viewType == 0) layoutId = R.layout.panel_page_sections;
            else if (viewType == 1) layoutId = R.layout.panel_page_templates;
            else if (viewType == 2) layoutId = R.layout.panel_page_fonts;
            else if (viewType == 3) layoutId = R.layout.panel_page_customization;
            
            View v = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            return new ViewHolder(v, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bindEvents();
        }

        @Override
        public int getItemCount() { return 4; }

        class ViewHolder extends RecyclerView.ViewHolder {
            int type;
            ViewHolder(View v, int type) { super(v); this.type = type; }

            void bindEvents() {
                if (type == 0) { // Sections Grid
                    loadSectionsFromWebView();
                    
                    // Search
                    EditText searchBox = itemView.findViewById(R.id.search_sections);
                    if (searchBox != null) {
                        searchBox.addTextChangedListener(new android.text.TextWatcher() {
                            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                                addPanelSearchQuery = s.toString();
                                loadSectionsFromWebView();
                            }
                            @Override public void afterTextChanged(android.text.Editable s) {}
                        });
                    }

                    // Sort Buttons
                    TextView btnAZ = itemView.findViewById(R.id.btn_sort_az);
                    TextView btnUsage = itemView.findViewById(R.id.btn_sort_usage);
                    TextView btnRecent = itemView.findViewById(R.id.btn_sort_recent);
                    CheckBox chkGroup = itemView.findViewById(R.id.chk_group_by);

                    if (btnAZ != null) btnAZ.setOnClickListener(v -> {
                        currentAddPanelSort = 0;
                        updateSortUI(btnAZ, btnUsage, btnRecent);
                        loadSectionsFromWebView();
                    });
                    if (btnUsage != null) btnUsage.setOnClickListener(v -> {
                        currentAddPanelSort = 1;
                        updateSortUI(btnAZ, btnUsage, btnRecent);
                        loadSectionsFromWebView();
                    });
                    if (btnRecent != null) btnRecent.setOnClickListener(v -> {
                        currentAddPanelSort = 2;
                        updateSortUI(btnAZ, btnUsage, btnRecent);
                        loadSectionsFromWebView();
                    });
                    if (chkGroup != null) {
                        chkGroup.setChecked(isAddPanelGrouped);
                        chkGroup.setOnCheckedChangeListener((cb, checked) -> {
                            isAddPanelGrouped = checked;
                            loadSectionsFromWebView();
                        });
                    }
                    
                    View btnReset = itemView.findViewById(R.id.btn_reset_all);
                    if (btnReset != null) btnReset.setOnClickListener(v -> {
                        new AlertDialog.Builder(itemView.getContext())
                            .setTitle("Reset Resume")
                            .setMessage("Reset everything?")
                            .setPositiveButton("Reset", (dialog, which) -> {
                                myWebView.evaluateJavascript("localStorage.clear(); location.reload();", null);
                                addFeaturePanel.setVisibility(View.GONE);
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                    });
                } else if (type == 1) { // Templates
                    refreshUserTemplates(itemView);
                } else if (type == 2) { // Fonts
                    bindFontBtn(R.id.btn_font_roboto, "Roboto", "'Roboto', sans-serif", "https://fonts.googleapis.com/css2?family=Roboto:wght@300;400;700&display=swap");
                    bindFontBtn(R.id.btn_font_poppins, "Poppins", "'Poppins', sans-serif", "https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap");
                    bindFontBtn(R.id.btn_font_opensans, "Open Sans", "'Open Sans', sans-serif", "https://fonts.googleapis.com/css2?family=Open+Sans:wght@300;400;600;700&display=swap");
                    bindFontBtn(R.id.btn_font_ubuntu, "Ubuntu", "'Ubuntu', sans-serif", "https://fonts.googleapis.com/css2?family=Ubuntu:wght@300;400;500;700&display=swap");
                    bindFontBtn(R.id.btn_font_merriweather, "Merriweather", "'Merriweather', serif", "https://fonts.googleapis.com/css2?family=Merriweather:ital,wght@0,300;0,400;0,700;1,400&display=swap");
                    bindFontBtn(R.id.btn_font_monospace, "Courier Prime", "'Courier Prime', monospace", "https://fonts.googleapis.com/css2?family=Courier+Prime:wght@400;700&display=swap");
                    
                    View btnCustom = itemView.findViewById(R.id.btn_font_custom);
                    if (btnCustom != null) btnCustom.setOnClickListener(v -> importCustomFont());
                } else if (type == 3) { // Customization
                    setupPagedExplorer(itemView, "header", headerDesignIds);
                    setupPagedExplorer(itemView, "section", sectionDesignIds);
                    setupPagedExplorer(itemView, "progress", progressDesignIds);
                    
                    bindBackgroundControls(itemView);
                    bindDesignerColumnControls(itemView);
                }
            }

            void bindDesignerColumnControls(View root) {
                 // Layout Mode (Default vs Mirror)
                 ViewFlipper flipper = root.findViewById(R.id.flipper_layout);
                 View btnPrev = root.findViewById(R.id.btn_layout_prev);
                 View btnNext = root.findViewById(R.id.btn_layout_next);
                 
                 if (flipper != null && btnPrev != null && btnNext != null) {
                     View.OnClickListener toggleLayout = v -> {
                         if (flipper.getDisplayedChild() == 0) {
                             flipper.showNext();
                             if (myWebView != null) myWebView.evaluateJavascript("if(window.updateDesignerColumnLayout) window.updateDesignerColumnLayout('mirror');", null);
                         } else {
                             flipper.showPrevious();
                             if (myWebView != null) myWebView.evaluateJavascript("if(window.updateDesignerColumnLayout) window.updateDesignerColumnLayout('default');", null);
                         }
                     };
                     btnPrev.setOnClickListener(toggleLayout);
                     btnNext.setOnClickListener(toggleLayout);
                 }

                 // Colors
                 View btnBg = root.findViewById(R.id.btn_left_col_bg);
                 View btnFrame = root.findViewById(R.id.btn_left_frame_color);
                 if (btnBg != null) btnBg.setOnClickListener(v -> openColorPicker(LEFT_COL_BG_ID));
                 if (btnFrame != null) btnFrame.setOnClickListener(v -> openColorPicker(LEFT_FRAME_COLOR_ID));

                 // SeekBars
                 SeekBar seekThick = root.findViewById(R.id.seek_left_frame_thickness);
                 SeekBar seekRadius = root.findViewById(R.id.seek_left_col_radius);
                 
                 if (seekThick != null) {
                     seekThick.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                         @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                             if (myWebView != null) myWebView.evaluateJavascript("if(window.updateLeftFrameThickness) window.updateLeftFrameThickness(" + progress + ");", null);
                         }
                         @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                         @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                     });
                 }

                 if (seekRadius != null) {
                     seekRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                         @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                             if (myWebView != null) myWebView.evaluateJavascript("if(window.updateLeftColRadius) window.updateLeftColRadius(" + progress + ");", null);
                         }
                         @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                         @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                     });
                 }
            }

            void bindBackgroundControls(View root) {
                View btnColor = root.findViewById(R.id.btn_bg_color);
                View btnImage = root.findViewById(R.id.btn_bg_image);
                SeekBar seekOpacity = root.findViewById(R.id.seek_bg_opacity);
                SeekBar seekGrayscale = root.findViewById(R.id.seek_bg_grayscale);

                if (btnColor != null) btnColor.setOnClickListener(v -> openColorPicker(BACKGROUND_COLOR_ID));
                if (btnImage != null) btnImage.setOnClickListener(v -> pickBackgroundImage());

                if (seekOpacity != null) {
                    seekOpacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if (myWebView != null) myWebView.evaluateJavascript("if(window.updateBgOpacity) window.updateBgOpacity(" + progress + ");", null);
                        }
                        @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                        @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                    });
                }

                if (seekGrayscale != null) {
                    seekGrayscale.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if (myWebView != null) myWebView.evaluateJavascript("if(window.updateBgGrayscale) window.updateBgGrayscale(" + progress + ");", null);
                        }
                        @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                        @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                    });
                }

                SeekBar seekColOpacity = root.findViewById(R.id.seek_col_opacity);
                if (seekColOpacity != null) {
                    seekColOpacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if (myWebView != null) myWebView.evaluateJavascript("if(window.updateColOpacity) window.updateColOpacity(" + progress + ");", null);
                        }
                        @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                        @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                    });
                }
            }
            
            void bindPlaceholder(int id, String toastMsg) {
                View btn = itemView.findViewById(id);
                if (btn != null) btn.setOnClickListener(v -> 
                    Toast.makeText(itemView.getContext(), toastMsg + " (Placeholder)", Toast.LENGTH_SHORT).show()
                );
            }
            
            void bindTemplateBtn(int id, String type) {
                View btn = itemView.findViewById(id);
                if (btn != null) btn.setOnClickListener(v -> changeNativeTemplate(type));
            }

            void bindProgressBtn(int id, String type) {
                 View btn = itemView.findViewById(id);
                 if (btn != null) btn.setOnClickListener(v -> {
                     if (myWebView != null) {
                         myWebView.evaluateJavascript("if(window.applyProgressDesign) window.applyProgressDesign('" + type + "');", null);
                     }
                 });
             }
            
            void bindFontBtn(int id, String name, String family, String url) {
                 android.widget.TextView btn = itemView.findViewById(id);
                 if (btn != null) {
                     btn.setOnClickListener(v -> changeNativeFont(name, family, url));
                     
                     String currentFont = itemView.getContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(getFontKeyForCurrentCv(), "Roboto");
                     if (currentFont.equals(name)) {
                         btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_active_dot, 0);
                     } else {
                         btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                     }
                 }
            }
            
            void bindSectionDesignBtn(int id, String type) {
                 View btn = itemView.findViewById(id);
                 if (btn != null) btn.setOnClickListener(v -> {
                     if (myWebView != null) {
                         myWebView.evaluateJavascript("if(window.applySectionDesign) window.applySectionDesign('" + type + "');", null);
                     }
                 });
             }

        }
    }

    private void setupPagedExplorer(View root, String category, String[] ids) {
        int flipperId = root.getResources().getIdentifier("flipper_" + category, "id", root.getContext().getPackageName());
        int prevId = root.getResources().getIdentifier("btn_" + category + "_prev", "id", root.getContext().getPackageName());
        int nextId = root.getResources().getIdentifier("btn_" + category + "_next", "id", root.getContext().getPackageName());

        ViewFlipper flipper = root.findViewById(flipperId);
        View btnPrev = root.findViewById(prevId);
        View btnNext = root.findViewById(nextId);

        if (flipper != null) {
            if (btnPrev != null) btnPrev.setOnClickListener(v -> { flipper.setInAnimation(root.getContext(), android.R.anim.slide_in_left); flipper.setOutAnimation(root.getContext(), android.R.anim.slide_out_right); flipper.showPrevious(); });
            if (btnNext != null) btnNext.setOnClickListener(v -> { flipper.setInAnimation(root.getContext(), R.anim.slide_in_right); flipper.setOutAnimation(root.getContext(), R.anim.slide_out_left); flipper.showNext(); });

            for (int i = 0; i < ids.length; i++) {
                int choiceId = root.getResources().getIdentifier("txt_" + category + "_choice_" + i, "id", root.getContext().getPackageName());
                TextView txtChoice = root.findViewById(choiceId);
                if (txtChoice != null) {
                    final int idx = i;
                    final String typeId = ids[i];
                    txtChoice.setOnClickListener(v -> {
                        if (myWebView != null) {
                            String jsFunc = "apply" + category.substring(0, 1).toUpperCase() + category.substring(1) + "Design";
                            myWebView.evaluateJavascript("if(window."+jsFunc+") window."+jsFunc+"('" + typeId + "');", null);
                        }
                        if (category.equals("header")) currentHeaderIdx = idx;
                        else if (category.equals("section")) currentSectionIdx = idx;
                        else if (category.equals("progress")) currentProgressIdx = idx;
                        updateChoiceHighlights(root, category, ids.length);
                    });
                }
            }

            // Auto-slide to the page containing the active choice
            int activeIdx = (category.equals("header")) ? currentHeaderIdx : (category.equals("section") ? currentSectionIdx : currentProgressIdx);
            flipper.setDisplayedChild(activeIdx / 2);

            updateChoiceHighlights(root, category, ids.length);
        }
    }

    private void updateChoiceHighlights(View root, String category, int count) {
        int activeIdx = (category.equals("header")) ? currentHeaderIdx : (category.equals("section") ? currentSectionIdx : currentProgressIdx);

        for (int i = 0; i < count; i++) {
            int choiceId = root.getResources().getIdentifier("txt_" + category + "_choice_" + i, "id", root.getContext().getPackageName());
            TextView txt = root.findViewById(choiceId);
            if (txt != null) {
                if (i == activeIdx) {
                    txt.setBackgroundResource(R.drawable.shape_active_choice);
                    txt.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_active_dot, 0);
                    txt.setCompoundDrawablePadding(4);
                } else {
                    txt.setBackgroundResource(0);
                    txt.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }
            }
        }
    }

    private void openColorPicker(int requestId) {
        onNativePanelOpened();
        ColorPickerDialog.newBuilder()
            .setDialogId(requestId)
            .setAllowPresets(true)
            .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
            .setShowAlphaSlider(true)
            .show(this);
    }

    private void pickBackgroundImage() {
        isPickingBgImage = true;
        mGetContent.launch("image/*");
    }

    private void updateBgImage(Uri uri) {
        if (uri == null) return;
        myWebView.evaluateJavascript("if(window.updateBgImage) window.updateBgImage('" + uri.toString() + "');", null);
    }





    private void updateATSBadge(int score) {
        if (atsBadge == null || atsBadgeScore == null) return;
        
        if (isNativeEditing) {
            atsBadge.setVisibility(View.GONE);
            return;
        }

        atsBadge.setVisibility(View.VISIBLE);
        atsBadgeScore.setText(String.valueOf(score));
        
        // Color logic
        int color;
        if (score >= 80) color = Color.parseColor("#00cc66");
        else if (score >= 60) color = Color.parseColor("#ffaa00");
        else color = Color.parseColor("#ff4444");
        
        android.graphics.drawable.GradientDrawable shape = (android.graphics.drawable.GradientDrawable) atsBadgeScore.getBackground();
        if (shape != null) {
            shape.setColor(color);
        }

        atsBadge.setOnClickListener(v -> showATSHub(score));
    }

    private void setupATSHub() {
        atsHubOverlay = findViewById(R.id.ats_hub_overlay);
        atsHubPanel = findViewById(R.id.ats_hub_panel);
        
        if (atsHubOverlay != null) {
            atsHubOverlay.setOnClickListener(v -> {
                if (v.getId() == R.id.ats_hub_overlay) {
                    hideATSHub();
                }
            });
        }
    }

    private void showATSHub(int score) {
        onNativePanelOpened();
        if (currentATSReport == null || atsHubOverlay == null || atsHubPanel == null) return;

        View view = atsHubPanel;
        TextView dialScore = view.findViewById(R.id.ats_dialog_score);
        TextView dialVerdict = view.findViewById(R.id.ats_dialog_verdict);
        ImageButton closeBtn = view.findViewById(R.id.ats_dialog_close);
        
        View headerRight = view.findViewById(R.id.section_right_header);
        LinearLayout contentRight = view.findViewById(R.id.section_right_content);
        ImageView chevronRight = view.findViewById(R.id.section_right_chevron);
        
        View headerWrong = view.findViewById(R.id.section_wrong_header);
        LinearLayout contentWrong = view.findViewById(R.id.section_wrong_content);
        ImageView chevronWrong = view.findViewById(R.id.section_right_chevron); // Wait, should be section_wrong_chevron
        // Re-view IDs in next step if necessary, but I checked dialog_ats_hub.xml before.
        
        // Correcting chevron ids based on dialog_ats_hub.xml:
        // section_right_chevron, section_wrong_chevron, section_improve_chevron
        
        chevronWrong = view.findViewById(R.id.section_wrong_chevron);
        
        View headerImprove = view.findViewById(R.id.section_improve_header);
        LinearLayout contentImprove = view.findViewById(R.id.section_improve_content);
        ImageView chevronImprove = view.findViewById(R.id.section_improve_chevron);

        android.widget.EditText jdInput = view.findViewById(R.id.ats_jd_input);
        android.widget.Button jdBtn = view.findViewById(R.id.ats_jd_btn);
        TextView jdResult = view.findViewById(R.id.ats_jd_result_text);

        dialScore.setText(String.valueOf(score));
        int color;
        String verdict;
        if (score >= 80) { color = Color.parseColor("#00cc66"); verdict = "Excellent! Setup for Success."; }
        else if (score >= 60) { color = Color.parseColor("#ffaa00"); verdict = "Good, but could be stronger."; }
        else { color = Color.parseColor("#ff4444"); verdict = "Needs Improvement."; }

        android.graphics.drawable.GradientDrawable bg = (android.graphics.drawable.GradientDrawable) dialScore.getBackground();
        if(bg != null) bg.setColor(color);
        dialVerdict.setText(verdict);

        try {
            JSONArray successes = currentATSReport.optJSONArray("successes");
            JSONArray errors = currentATSReport.optJSONArray("errors"); 
            JSONArray warnings = currentATSReport.optJSONArray("warnings");

            populateSectionItems(contentRight, successes, "#006633", "✓ ");
            populateSectionItems(contentWrong, errors, "#990000", "• ");
            populateSectionItems(contentImprove, warnings, "#664400", "→ ");

            setupExpansion(headerRight, contentRight, chevronRight);
            setupExpansion(headerWrong, contentWrong, chevronWrong);
            setupExpansion(headerImprove, contentImprove, chevronImprove);

        } catch (Exception e) { Log.e(TAG, "Error populating panel", e); }

        LinearLayout distContainer = view.findViewById(R.id.ats_score_distribution);
        dialScore.setOnClickListener(v -> {
            if (distContainer.getVisibility() == View.VISIBLE) {
                distContainer.setVisibility(View.GONE);
            } else {
                distContainer.setVisibility(View.VISIBLE);
                populateScoreBreakdown(distContainer);
            }
        });

        closeBtn.setOnClickListener(v -> hideATSHub());
        
        jdBtn.setOnClickListener(v -> {
            String text = jdInput.getText().toString();
            if (text.length() > 20) {
                String safeText = text.replace("'", "\\'").replace("\n", " ");
                myWebView.evaluateJavascript("window.atsAnalyzer.analyzeJD('" + safeText + "')", null);
                jdResult.setVisibility(View.VISIBLE);
                jdResult.setText("Analyzing...");
            }
        });

        // GROW ANIMATION
        atsHubOverlay.setVisibility(View.VISIBLE);
        atsHubOverlay.setAlpha(0f);
        atsHubOverlay.animate().alpha(1f).setDuration(300).start();

        int[] badgeLoc = new int[2];
        atsBadge.getLocationInWindow(badgeLoc);
        float pivotX = badgeLoc[0] + (atsBadge.getWidth() / 2f);
        float pivotY = badgeLoc[1] + (atsBadge.getHeight() / 2f);

        int[] panelLoc = new int[2];
        atsHubPanel.getLocationInWindow(panelLoc);
        atsHubPanel.setPivotX(pivotX - panelLoc[0]);
        atsHubPanel.setPivotY(pivotY - panelLoc[1]);

        atsHubPanel.setScaleX(0f);
        atsHubPanel.setScaleY(0f);
        atsHubPanel.setAlpha(0f);

        atsHubPanel.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(400)
                .setInterpolator(new android.view.animation.OvershootInterpolator(1.2f))
                .start();
    }

    private void hideATSHub() {
        if (atsHubOverlay == null || atsHubPanel == null || atsHubOverlay.getVisibility() != View.VISIBLE) return;

        atsHubOverlay.animate().alpha(0f).setDuration(300).setStartDelay(100).start();
        
        atsHubPanel.animate()
                .scaleX(0f)
                .scaleY(0f)
                .alpha(0f)
                .setDuration(350)
                .setInterpolator(new android.view.animation.AccelerateInterpolator())
                .setListener(new android.animation.AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(android.animation.Animator animation) {
                        atsHubOverlay.setVisibility(View.GONE);
                        atsHubPanel.animate().setListener(null);
                    }
                })
                .start();
    }

    private void populateScoreBreakdown(LinearLayout container) {
        if (currentATSReport == null) return;
        container.removeAllViews();
        
        // Add Title Header back
        TextView title = new TextView(this);
        title.setText("SCORE DISTRIBUTION");
        title.setTextSize(10);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setTextColor(Color.parseColor("#888888"));
        title.setLetterSpacing(0.1f);
        title.setPadding(0, 0, 0, 16);
        container.addView(title);

        try {
            JSONObject breakdown = currentATSReport.optJSONObject("breakdown");
            if (breakdown == null) return;

            final String[][] categories = {
                {"density", "Content Density", "15"},
                {"experience", "Career Experience", "20"},
                {"education", "Education Quality", "10"},
                {"skills", "Skill Matrix", "15"},
                {"impact", "Quantified Impact", "25"},
                {"verbs", "Strong Action Verbs", "15"}
            };

            for (String[] cat : categories) {
                View row = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, null);
                TextView t1 = row.findViewById(android.R.id.text1);
                TextView t2 = row.findViewById(android.R.id.text2);
                
                int pts = breakdown.optInt(cat[0], 0);
                t1.setText(cat[1]);
                t1.setTextSize(14);
                t1.setTextColor(Color.parseColor("#333333"));
                
                t2.setText(pts + " / " + cat[2] + " pts");
                t2.setTextSize(12);
                t2.setTextColor(pts > 0 ? Color.parseColor("#006633") : Color.parseColor("#999999"));
                
                container.addView(row);
            }
        } catch (Exception e) { Log.e(TAG, "Error populating breakdown", e); }
    }

    private void populateSectionItems(LinearLayout container, JSONArray items, String colorHex, String bullet) {
        container.removeAllViews();
        if (items == null || items.length() == 0) {
            TextView empty = new TextView(this);
            empty.setText("Nothing to show here.");
            empty.setPadding(8, 8, 8, 8);
            empty.setTextColor(Color.GRAY);
            container.addView(empty);
            return;
        }

        for (int i = 0; i < items.length(); i++) {
            try {
                JSONObject item = items.getJSONObject(i);
                TextView tv = new TextView(this);
                String title = item.optString("title", "");
                String msg = item.optString("msg", "");
                
                if (!msg.isEmpty()) {
                    tv.setText(bullet + title + ": " + msg);
                } else {
                    tv.setText(bullet + title);
                }
                
                tv.setTextColor(Color.parseColor(colorHex));
                tv.setPadding(0, 8, 0, 8);
                tv.setTextSize(14);
                container.addView(tv);
            } catch (JSONException e) {
                Log.e(TAG, "Error adding item to section", e);
            }
        }
    }

    private void setupExpansion(View header, LinearLayout content, ImageView chevron) {
        header.setOnClickListener(v -> {
            if (content.getVisibility() == View.VISIBLE) {
                content.setVisibility(View.GONE);
                chevron.setImageResource(R.drawable.ic_expand_more);
            } else {
                content.setVisibility(View.VISIBLE);
                chevron.setRotation(180); // Quick flip if ic_expand_less isn't used
                // Or better: chevron.setImageResource(R.drawable.ic_arrow_up); 
                // Since I have ic_expand_more, I'll just rotate it.
            }
        });
    }

    private void updateATSHubJD(String jsonResult) {
         if (atsHubOverlay == null || atsHubOverlay.getVisibility() != View.VISIBLE) return;
         try {
             JSONObject res = new JSONObject(jsonResult);
             int score = res.getInt("score");
             JSONArray missing = res.getJSONArray("missing");
             
             TextView resultText = atsHubPanel.findViewById(R.id.ats_jd_result_text);
             if(resultText != null) {
                 StringBuilder sb = new StringBuilder();
                 sb.append("Match Score: ").append(score).append("%\n\n");
                 sb.append("Missing Keywords:\n");
                 if (missing.length() == 0) sb.append("None! Great job.");
                 for(int i=0; i<missing.length(); i++) sb.append("• ").append(missing.getString(i)).append("\n");
                 
                 resultText.setText(sb.toString());
                 if(score > 70) resultText.setTextColor(Color.parseColor("#00cc66"));
                 else resultText.setTextColor(Color.parseColor("#ffaa00"));
             }
         } catch (Exception e) {}
    }

    
    /**
     * Reads a resume JSON file from disk and stores it in pendingJsonState
     * for injection into the WebView once it's ready.
     */
    private void loadResumeFromFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                Log.e(TAG, "File does not exist: " + filePath);
                Toast.makeText(this, "CV file not found", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Read the entire file into a string
            StringBuilder jsonBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBuilder.append(line);
                }
            }
            
            pendingJsonState = jsonBuilder.toString();
            Log.d(TAG, "Successfully loaded CV from file (" + pendingJsonState.length() + " bytes): " + filePath);
            
        } catch (IOException e) {
            Log.e(TAG, "Error reading resume file: " + filePath, e);
            Toast.makeText(this, "Error loading CV", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupWizardButton() {
        View wizardFab = findViewById(R.id.fab_wizard);
        if (wizardFab != null) {
            wizardFab.setOnClickListener(v -> {
                onNativePanelOpened();
                if (myWebView != null) {
                    myWebView.evaluateJavascript("window.exportStructuredData()", value -> {
                        // value is a JSON string (with surrounding quotes from JS)
                        String data = value;
                        if (data != null && data.startsWith("\"") && data.endsWith("\"")) {
                            // Unescape the string from JS
                            data = data.substring(1, data.length() - 1)
                                       .replace("\\\"", "\"")
                                       .replace("\\\\", "\\");
                        }
                        
                        Intent intent = new Intent(this, StepByStepActivity.class);
                        intent.putExtra("EXTRA_START_STEP", 2);
                        if (data != null && !data.equals("null")) {
                            intent.putExtra("EXTRA_INITIAL_STATE", data);
                        }
                        startActivity(intent);
                        finish();
                    });
                } else {
                    Intent intent = new Intent(this, StepByStepActivity.class);
                    intent.putExtra("EXTRA_START_STEP", 2);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }

    private void saveCurrentAsTemplate() {
        enterTemplateSelectionMode();
    }

    private void setupTemplateSelectionUI() {
        templateInfoPanel = findViewById(R.id.template_info_panel);
        templateSelectionControls = findViewById(R.id.template_selection_controls);
        btnTemplateCancel = findViewById(R.id.btn_template_cancel);
        btnTemplateConfirm = findViewById(R.id.btn_template_confirm);
        btnCollapseInfo = findViewById(R.id.btn_collapse_info);
        infoContent = findViewById(R.id.info_content);

        if (btnTemplateCancel != null) {
            btnTemplateCancel.setOnClickListener(v -> exitTemplateSelectionMode());
        }

        if (btnTemplateConfirm != null) {
            btnTemplateConfirm.setOnClickListener(v -> {
                // Trigger JS to get selected sections
                if (myWebView != null) {
                    // Fix: Use ternary expression instead of 'return' which is invalid at top-level
                    myWebView.evaluateJavascript("(window.getSelectedSections ? window.getSelectedSections() : '[]')", value -> {
                        // Value will be JSON array string e.g. "[]" or "[{...}]"
                        if (value == null || value.equals("null") || value.equals("\"[]\"") || value.equals("[]")) {
                            Toast.makeText(this, "Please select at least one section!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        onTemplateConfirmed(value);
                    });
                }
            });
        }

        if (btnCollapseInfo != null && templateInfoPanel != null) {
            // Find minimized button if not already found (it's in activity_main)
            btnTemplateInfoMinimized = findViewById(R.id.btn_template_info_minimized);
            
            // Set listener on the collapse button to MINIMIZE the whole panel
            btnCollapseInfo.setOnClickListener(v -> {
                // Hide Panel
                templateInfoPanel.animate()
                    .alpha(0f)
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(300)
                    .withEndAction(() -> {
                         templateInfoPanel.setVisibility(View.GONE);
                         // Show Minimized Button
                         if (btnTemplateInfoMinimized != null) {
                             btnTemplateInfoMinimized.setVisibility(View.VISIBLE);
                             btnTemplateInfoMinimized.setAlpha(0f);
                             btnTemplateInfoMinimized.setScaleX(0f);
                             btnTemplateInfoMinimized.setScaleY(0f);
                             btnTemplateInfoMinimized.animate()
                                 .alpha(1f)
                                 .scaleX(1f)
                                 .scaleY(1f)
                                 .setDuration(300)
                                 .start();
                         }
                    })
                    .start();
            });
            
            // Set listener on minimized button to RESTORE the panel
            if (btnTemplateInfoMinimized != null) {
                btnTemplateInfoMinimized.setOnClickListener(v -> {
                     // Hide Button
                     btnTemplateInfoMinimized.animate()
                         .alpha(0f)
                         .scaleX(0f)
                         .scaleY(0f)
                         .setDuration(200)
                         .withEndAction(() -> {
                             btnTemplateInfoMinimized.setVisibility(View.GONE);
                             // Show Panel
                             templateInfoPanel.setVisibility(View.VISIBLE);
                             templateInfoPanel.setScaleX(0.8f);
                             templateInfoPanel.setScaleY(0.8f);
                             templateInfoPanel.setAlpha(0f);
                             templateInfoPanel.animate()
                                 .alpha(1f)
                                 .scaleX(1f)
                                 .scaleY(1f)
                                 .setDuration(300)
                                 .start();
                         })
                         .start();
                });
            }
        }
    }

    private void enterTemplateSelectionMode() {
        if (isTemplateSelectionMode) return;
        isTemplateSelectionMode = true;
        
        // Hide Main UI
        if (addFab != null) addFab.setVisibility(View.GONE);
        if (printFab != null) printFab.setVisibility(View.GONE);
        if (editFab != null) editFab.setVisibility(View.GONE);
        View wizardFab = findViewById(R.id.fab_wizard);
        if (wizardFab != null) wizardFab.setVisibility(View.GONE);
        if (undoRedoContainer != null) undoRedoContainer.setVisibility(View.GONE);
        hideActiveNativeToolbar();
        if (leftPanel != null) leftPanel.hidePanel();
        if (rightPanel != null) rightPanel.hidePanel();
        if (cvNameDisplay != null) cvNameDisplay.setVisibility(View.GONE);

        // Show Template UI
        if (templateInfoPanel != null) {
            templateInfoPanel.setVisibility(View.VISIBLE);
            templateInfoPanel.setAlpha(0f);
            templateInfoPanel.animate().alpha(1f).setDuration(300).start();
        }
        if (templateSelectionControls != null) {
            templateSelectionControls.setVisibility(View.VISIBLE);
            templateSelectionControls.setTranslationY(200);
            templateSelectionControls.animate().translationY(0).setDuration(300).start();
        }

        // Notify JS
        if (myWebView != null) {
            myWebView.evaluateJavascript("if(window.enterTemplateSelectionMode) window.enterTemplateSelectionMode();", null);
        }
    }

    private void exitTemplateSelectionMode() {
        if (!isTemplateSelectionMode) return;
        isTemplateSelectionMode = false;

        // Hide Template UI
        if (templateInfoPanel != null) templateInfoPanel.setVisibility(View.GONE);
        if (templateSelectionControls != null) templateSelectionControls.setVisibility(View.GONE);
        if (btnTemplateInfoMinimized != null) btnTemplateInfoMinimized.setVisibility(View.GONE);

        // Restore Main UI
        if (editFab != null) editFab.setVisibility(View.VISIBLE);
        View wizardFab = findViewById(R.id.fab_wizard);
        if (wizardFab != null) wizardFab.setVisibility(View.VISIBLE);
        updateNativeUI(isNativeEditing); // Restores FABs based on edit mode
        if (undoRedoContainer != null) undoRedoContainer.setVisibility(View.VISIBLE);
        if (cvNameDisplay != null) cvNameDisplay.setVisibility(View.VISIBLE);

        // Notify JS
        if (myWebView != null) {
            myWebView.evaluateJavascript("if(window.exitTemplateSelectionMode) window.exitTemplateSelectionMode();", null);
        }
    }

    private void onTemplateConfirmed(String selectedSectionsJson) {
        Log.d(TAG, "Template Sections Selected: " + selectedSectionsJson);
        
        if (selectedSectionsJson == null || selectedSectionsJson.equals("null")) {
             Toast.makeText(this, "Error: No data returned", Toast.LENGTH_SHORT).show();
             return;
        }

        // Unquote if necessary (JS returns "\"[\"...]\"")
        String finalJson;
        if (selectedSectionsJson.startsWith("\"") && selectedSectionsJson.endsWith("\"")) {
             finalJson = selectedSectionsJson.substring(1, selectedSectionsJson.length() - 1)
                 .replace("\\\"", "\"")
                 .replace("\\\\", "\\");
        } else {
            finalJson = selectedSectionsJson;
        }

        // Show Enhanced Save Dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_save_template, null);
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.TransparentDialog)
            .setView(dialogView)
            .setCancelable(true)
            .create();

        EditText etName = dialogView.findViewById(R.id.et_template_name);
        EditText etEmail = dialogView.findViewById(R.id.et_template_email);
        EditText etTag = dialogView.findViewById(R.id.et_template_tag);
        Button btnSave = dialogView.findViewById(R.id.btn_confirm_save);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel_save);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String tag = etTag.getText().toString().trim();

            if (name.isEmpty()) {
                name = generateDefaultTemplateName();
            }

            performTemplateSave(finalJson, name, email, tag);
            dialog.dismiss();
        });

        dialog.show();
    }

    private String generateDefaultTemplateName() {
        File dir = new File(getFilesDir(), "user_templates");
        if (!dir.exists()) return "Template";
        
        String base = "Template";
        File firstFile = new File(dir, "Template_" + base + ".json");
        if (!firstFile.exists()) return base;

        int count = 2;
        while (new File(dir, "Template_" + base + " " + count + ".json").exists()) {
            count++;
        }
        return base + " " + count;
    }

    private void performTemplateSave(String json, String name, String email, String tag) {
        try {
            File dir = new File(getFilesDir(), "user_templates");
            if (!dir.exists()) dir.mkdirs();
            
            // Clean name for filename
            String safeName = name.replaceAll("[\\\\/:*?\"<>|]", "_");
            File file = new File(dir, "Template_" + safeName + ".json");
            
            try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
                JSONObject fullTemplate = new JSONObject(json);
                fullTemplate.put("timestamp", System.currentTimeMillis());
                fullTemplate.put("template_name", name);
                if (!email.isEmpty()) fullTemplate.put("user_email", email);
                if (!tag.isEmpty()) fullTemplate.put("template_tag", tag);
                writer.write(fullTemplate.toString());
            }
            
            saveWebViewScreenshot(file.getAbsolutePath());
            
            // Notify panel adapter that new template exists
            if (addPanelAdapter != null) {
                addPanelAdapter.notifyItemChanged(1); // Index 1 is Template tab
            }
            
            Toast.makeText(this, "Template '" + name + "' Saved!", Toast.LENGTH_SHORT).show();
            exitTemplateSelectionMode();
            
        } catch (Exception e) {
            Log.e(TAG, "Error performing template save", e);
            Toast.makeText(this, "Failed to save template", Toast.LENGTH_SHORT).show();
        }
    }


    private void saveCurrentAsDocx() {
        // Placeholder for future Docx export logic
        Toast.makeText(this, "Docx Export Placeholder: Saved!", Toast.LENGTH_SHORT).show();
    }

    private static class VitaeData {
        String json;
        Bitmap thumbnail;
    }

    private VitaeData loadVitaeAsset(String assetPath) {
        VitaeData data = new VitaeData();
        try (java.io.InputStream is = getAssets().open(assetPath);
             java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(is)) {
            
            java.util.zip.ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName().toLowerCase();
                Log.d(TAG, "Vitae Asset Entry: " + name + " in " + assetPath);
                
                if (name.endsWith(".json")) {
                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = zis.read(buffer)) > 0) baos.write(buffer, 0, len);
                    data.json = baos.toString("UTF-8");
                } else if (name.endsWith(".png")) {
                    // Robust way to decode Bitmap from ZipInputStream: read to byte array first
                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = zis.read(buffer)) > 0) baos.write(buffer, 0, len);
                    byte[] bytes = baos.toByteArray();
                    data.thumbnail = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                }
                zis.closeEntry();
            }
        } catch (java.io.IOException e) {
            Log.e(TAG, "Error reading .vitae asset: " + assetPath, e);
        }
        return data;
    }

    private void refreshUserTemplates(View root) {
        GridLayout container = root.findViewById(R.id.user_templates_container);
        TextView noTemplatesTv = root.findViewById(R.id.tv_no_templates);
        if (container == null) return;
        
        container.removeAllViews();

        int totalItems = 0;

        // 1. Load Default Templates from Assets (Non-deletable)
        try {
            String[] assetTemplates = getAssets().list("default_templates");
            if (assetTemplates != null) {
                for (String assetName : assetTemplates) {
                    if (assetName.endsWith(".vitae")) {
                        VitaeData vData = loadVitaeAsset("default_templates/" + assetName);
                        
                        View itemView = getLayoutInflater().inflate(R.layout.item_user_template, container, false);
                        
                        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                        params.width = 0;
                        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
                        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                        params.setMargins(8, 8, 8, 8);
                        itemView.setLayoutParams(params);

                        TextView tvName = itemView.findViewById(R.id.tvTemplateName);
                        ImageView ivThumb = itemView.findViewById(R.id.ivTemplateThumbnail);
                        ImageButton btnDelete = itemView.findViewById(R.id.btnDeleteTemplate);

                        tvName.setText(assetName.replace(".vitae", ""));
                        
                        if (vData.thumbnail != null) {
                            ivThumb.setImageBitmap(vData.thumbnail);
                            ivThumb.setImageTintList(null);
                        } else {
                            ivThumb.setImageResource(R.drawable.ic_description);
                            ivThumb.setImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                        }

                        btnDelete.setVisibility(View.GONE);

                        itemView.setOnClickListener(v -> {
                            if (vData.json != null && myWebView != null) {
                                String safeJson = org.json.JSONObject.quote(vData.json);
                                myWebView.evaluateJavascript("window.applyUserTemplate(" + safeJson + ")", null);
                                Toast.makeText(this, "Template '" + assetName.replace(".vitae", "") + "' Applied!", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e(TAG, "Template data missing for: " + assetName);
                                Toast.makeText(this, "Error: Template data missing", Toast.LENGTH_SHORT).show();
                            }
                        });

                        container.addView(itemView);
                        totalItems++;
                    }
                }
            }
        } catch (java.io.IOException e) {
            Log.e(TAG, "Error listing asset templates", e);
        }

        // 2. Load User-Saved Templates from Files (Deletable)
        File dir = new File(getFilesDir(), "user_templates");
        if (!dir.exists()) dir.mkdirs();
        File[] templates = dir.listFiles((d, name) -> name.endsWith(".json"));
        
        if (templates != null && templates.length > 0) {
            noTemplatesTv.setVisibility(View.GONE);
            for (File t : templates) {
                View itemView = getLayoutInflater().inflate(R.layout.item_user_template, container, false);
                
                // Set GridLayout params for 2 columns
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = GridLayout.LayoutParams.WRAP_CONTENT;
                params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                params.setMargins(8, 8, 8, 8);
                itemView.setLayoutParams(params);

                ImageView ivThumb = itemView.findViewById(R.id.ivTemplateThumbnail);
                TextView tvName = itemView.findViewById(R.id.tvTemplateName);
                ImageButton btnDelete = itemView.findViewById(R.id.btnDeleteTemplate);

                tvName.setText(t.getName().replace("Template_", "").replace(".json", ""));

                // Load thumbnail
                File thumbFile = new File(t.getAbsolutePath().replace(".json", ".png"));
                if (thumbFile.exists()) {
                    ivThumb.setImageBitmap(BitmapFactory.decodeFile(thumbFile.getAbsolutePath()));
                    ivThumb.setImageTintList(null);
                }

                itemView.setOnClickListener(v -> {
                    try {
                        StringBuilder sb = new StringBuilder();
                        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(t))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                sb.append(line);
                            }
                        }
                        String fullJson = sb.toString();

                        if (myWebView != null) {
                            String safeJson = org.json.JSONObject.quote(fullJson);
                            myWebView.evaluateJavascript("window.applyUserTemplate(" + safeJson + ")", null);
                            Toast.makeText(this, "Template Applied!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error applying user template", e);
                        Toast.makeText(this, "Failed to apply template", Toast.LENGTH_SHORT).show();
                    }
                });

                btnDelete.setOnClickListener(v -> deleteUserTemplate(t, root));
                
                container.addView(itemView);
                totalItems++;
            }
        }

        // Handle Spacers and Visibility
        if (totalItems > 0) {
            noTemplatesTv.setVisibility(View.GONE);
            if (totalItems % 2 != 0) {
                View spacer = new View(this);
                GridLayout.LayoutParams spacerParams = new GridLayout.LayoutParams();
                spacerParams.width = 0;
                spacerParams.height = 1;
                spacerParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                spacerParams.setMargins(8, 8, 8, 8);
                spacer.setLayoutParams(spacerParams);
                container.addView(spacer);
            }
        } else {
            noTemplatesTv.setVisibility(View.VISIBLE);
        }
    }

    private void deleteUserTemplate(File templateFile, View root) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Template")
            .setMessage("Are you sure you want to delete this template?")
            .setPositiveButton("Delete", (dialog, which) -> {
                if (templateFile.delete()) {
                    File thumbFile = new File(templateFile.getAbsolutePath().replace(".json", ".png"));
                    if (thumbFile.exists()) thumbFile.delete();
                    Toast.makeText(this, "Template deleted", Toast.LENGTH_SHORT).show();
                    refreshUserTemplates(root);
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void saveWebViewScreenshot(String jsonPath) {
         if (myWebView == null || myWebView.getWidth() <= 0 || myWebView.getHeight() <= 0) return;
         
         try {
             // Store current scroll position to restore later
             int scrollX = myWebView.getScrollX();
             int scrollY = myWebView.getScrollY();
             
             // Scroll to top to ensure we capture the resume header for the thumbnail
             myWebView.scrollTo(0, 0);
                              // Create a bitmap of the WebView visible content (now at the top)
              // CROP: Reduce width by 8 pixels to remove potential right-side dark lines/artifacts
              int captureWidth = Math.max(1, myWebView.getWidth() - 8);
              int captureHeight = myWebView.getHeight();
              
              android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(
                      captureWidth, 
                      captureHeight, 
                      android.graphics.Bitmap.Config.ARGB_8888);
              
              android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
              myWebView.draw(canvas);
             
             // Restore original scroll position so the user's view doesn't jump
             myWebView.scrollTo(scrollX, scrollY);
             
             if (bitmap != null) {
                 // Determine screenshot path (replace .json with .png)
                 String imagePath = jsonPath.replace(".json", ".png");
                 File imageFile = new File(imagePath);
                 
                 try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                     bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 80, fos);
                     Log.d(TAG, "Screenshot saved from top: " + imagePath);
                 } catch (IOException e) {
                     Log.e(TAG, "Error saving screenshot", e);
                 }
             }
         } catch (Exception e) {
             Log.e(TAG, "Error creating screenshot bitmap", e);
         }
    }

    public WebView getWebView() { return myWebView; }

    // --- Section Background Manager Logic ---

    public static final int SECTION_BG_COLOR_ID = 999;
    public SectionBgItem pendingColorItem;
    public SectionBgAdapter pendingColorAdapter;
    public String pendingColorTarget = ""; // "left", "mid", "right", or ""

    private void showSectionBackgroundDialog(String jsonItems) {
        onNativePanelOpened();
        try {
            JSONArray arr = new JSONArray(jsonItems);
            List<SectionBgItem> items = new ArrayList<>();
            for(int i=0; i<arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                boolean isNameProf = obj.optBoolean("isNameProfession", false);
                String itemName = obj.getString("name");
                
                // Debug logging
                Log.d("SectionBgDebug", "Item: " + itemName + ", isNameProfession: " + isNameProf);
                
                items.add(new SectionBgItem(
                    obj.getString("id"),
                    itemName,
                    obj.getString("color"),
                    obj.optInt("opacity", 100),
                    obj.optInt("radius", 0),
                    (float) obj.optDouble("lineHeight", 1.2),
                    obj.optBoolean("isMaster", false),
                    obj.optInt("rotation", 0),
                    obj.optInt("nameFontSize", 100),
                    obj.optInt("titleFontSize", 100),
                    obj.optInt("alignment", 1),
                    obj.optInt("fontSize", 14),
                    isNameProf
                ).setBlur(obj.optInt("blur", 0))
                 .setZIndex(obj.optInt("zIndex", 1))
                 .setX(obj.optInt("x", 50))
                 .setY(obj.optInt("y", 50))
                 .setIsBlank(obj.optBoolean("isBlank", false))
                 .setIsStick(obj.optBoolean("isStick", false))
                 .setImageMode(obj.optString("imageMode", "cover"))
                 .setLeftShape(obj.optInt("leftShape", 0))
                 .setRightShape(obj.optInt("rightShape", 0))
                 .setMarginTop(obj.optInt("marginTop", 20))
                 .setMarginBottom(obj.optInt("marginBottom", 20))
                 .setXAxis(obj.optInt("xAxis", 100))
                 .setStickLeftShape(obj.optInt("stickLeftShape", 0))
                 .setStickLeftW(obj.optInt("stickLeftW", 20))
                 .setStickLeftH(obj.optInt("stickLeftH", 20))
                 .setStickLeftProp(obj.optBoolean("stickLeftProp", true))
                 .setStickLeftRot(obj.optInt("stickLeftRot", 0))
                 .setStickLeftColor(obj.optString("stickLeftColor", "#000000"))
                 .setStickMidShape(obj.optInt("stickMidShape", 0))
                 .setStickMidW(obj.optInt("stickMidW", 20))
                 .setStickMidH(obj.optInt("stickMidH", 20))
                 .setStickMidProp(obj.optBoolean("stickMidProp", false))
                 .setStickMidRot(obj.optInt("stickMidRot", 0))
                 .setStickMidColor(obj.optString("stickMidColor", "#000000"))
                 .setStickRightShape(obj.optInt("stickRightShape", 0))
                 .setStickRightW(obj.optInt("stickRightW", 20))
                 .setStickRightH(obj.optInt("stickRightH", 20))
                 .setStickRightProp(obj.optBoolean("stickRightProp", true))
                 .setStickRightRot(obj.optInt("stickRightRot", 0))
                 .setStickRightColor(obj.optString("stickRightColor", "#000000"))
                 .setStickLineColor(obj.optString("stickLineColor", "#000000"))
                 .setStickLineThickness(obj.optInt("stickLineThickness", 2)));
            }

            BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.TransparentBottomSheetDialog);
            View view = getLayoutInflater().inflate(R.layout.dialog_section_backgrounds, null);
            dialog.setContentView(view);
            
            RecyclerView recycler = view.findViewById(R.id.recycler_backgrounds);
            recycler.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
            SectionBgAdapter adapter = new SectionBgAdapter(items, dialog, this);
            recycler.setAdapter(adapter);
            
            // Limit height to 55% of screen
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int maxHeight = (int) (displayMetrics.heightPixels * 0.55);
            view.getLayoutParams().height = maxHeight;
            
            view.findViewById(R.id.btn_close).setOnClickListener(v -> dialog.dismiss());
            
            // Hide toolbars/gear
            myWebView.evaluateJavascript("window.prepareForSettingsEdit();", null);
            dialog.setOnDismissListener(d -> {
                myWebView.evaluateJavascript("window.cleanupSettingsEdit();", null);
            });
            
            dialog.show();

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing section items", e);
        }
    }
    private void showNameSettingsDialog(int currentRotation, int currentScale) {
        onNativePanelOpened();
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.TransparentBottomSheetDialog);
        View view = getLayoutInflater().inflate(R.layout.dialog_name_settings, null);
        dialog.setContentView(view);
        
        // Limit height to 35% of screen
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int maxHeight = (int) (displayMetrics.heightPixels * 0.35);
        view.getLayoutParams().height = maxHeight;
        
        SeekBar seekRotation = dialog.findViewById(R.id.seek_rotation);
        SeekBar seekScale = dialog.findViewById(R.id.seek_scale);
        TextView textRotation = dialog.findViewById(R.id.text_rotation_val);
        EditText editScale = dialog.findViewById(R.id.edit_scale_val);
        Button btnReset = dialog.findViewById(R.id.btn_reset_name_settings);
        
        // Setup initial values
        if (seekRotation != null) {
            seekRotation.setProgress(currentRotation);
            if (textRotation != null) textRotation.setText(currentRotation + "°");
            
            seekRotation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (textRotation != null) textRotation.setText(progress + "°");
                    updateNameSettings("mainHeader", progress, seekScale != null ? seekScale.getProgress() : 100, 100, 1);
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }
        
        if (seekScale != null) {
            // Scale defaults to 100 if invalid
            if (currentScale < 50) currentScale = 100;
            seekScale.setProgress(currentScale);
            if (editScale != null) editScale.setText(String.valueOf(currentScale));
            
            seekScale.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (progress < 50) progress = 50; // enforce min here too just in case
                    if (fromUser && editScale != null) {
                        editScale.setText(String.valueOf(progress));
                    }
                    updateNameSettings("mainHeader", seekRotation != null ? seekRotation.getProgress() : 0, progress, 100, 1);
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }

        if (editScale != null) {
             editScale.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                    try {
                        int val = Integer.parseInt(editScale.getText().toString());
                        if (val < 50) val = 50;
                        if (val > 200) val = 200;
                        if (seekScale != null) seekScale.setProgress(val);
                        // Trigger update
                         updateNameSettings("mainHeader", seekRotation != null ? seekRotation.getProgress() : 0, val, 100, 1);
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                    android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editScale.getWindowToken(), 0);
                    return true;
                }
                return false;
            });
        }
        
        if (btnReset != null) {
            btnReset.setOnClickListener(v -> {
                if (seekRotation != null) seekRotation.setProgress(0);
                if (seekScale != null) seekScale.setProgress(100);
                if (editScale != null) editScale.setText("100");
                updateNameSettings("mainHeader", 0, 100, 100, 1);
            });
        }
        
        dialog.show();
    }
    
    public void updateNameSettings(String targetId, int rotation, int nameSize, int titleSize, int alignment) {
        if (myWebView != null) {
            myWebView.evaluateJavascript("if(window.updateNameSettings) window.updateNameSettings('" + targetId + "', " + rotation + ", " + nameSize + ", " + titleSize + ", " + alignment + ");", null);
        }
    }

    private void handleIconImageSelection(Uri uri) {
        if (uri == null) return;
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                byte[] bytes = outputStream.toByteArray();
                String base64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                String dataUrl = "data:image/png;base64," + base64; 
                
                if (myWebView != null) {
                    myWebView.post(() -> myWebView.evaluateJavascript("if(window.handleNativeIconImage) window.handleNativeIconImage('" + currentIconSectionId + "', " + currentIconItemIndex + ", '" + dataUrl + "');", null));
                }
                inputStream.close();
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error handling icon image selection", e);
            Toast.makeText(this, "Failed to load icon image", Toast.LENGTH_SHORT).show();
        }
    }

    private void showIconSettingsDialog(String sectionId, int itemIndex, String currentColor) {
        onNativePanelOpened();
        currentIconSectionId = sectionId;
        currentIconItemIndex = itemIndex;

        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.TransparentBottomSheetDialog);
        View view = getLayoutInflater().inflate(R.layout.dialog_icon_settings, null);
        dialog.setContentView(view);

        // Limit height to 35% of screen
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int maxHeight = (int) (displayMetrics.heightPixels * 0.35);
        view.getLayoutParams().height = maxHeight;

        View btnPickImage = view.findViewById(R.id.btn_change_icon_image);
        View btnRotate = view.findViewById(R.id.btn_rotate_icon_native);
        View colorPreviewSingle = view.findViewById(R.id.color_preview_single);
        View colorPreviewSection = view.findViewById(R.id.color_preview_section);
        SeekBar sliderSize = view.findViewById(R.id.slider_icon_size);
        TextView txtSizeVal = view.findViewById(R.id.txt_icon_size_val);

        if (currentColor != null && !currentColor.isEmpty()) {
            try {
                int c = Color.parseColor(currentColor);
                colorPreviewSingle.setBackgroundColor(c);
                colorPreviewSection.setBackgroundColor(c);
            } catch (Exception e) {
                Log.e(TAG, "Invalid color passed to icon settings: " + currentColor);
            }
        }

        btnPickImage.setOnClickListener(v -> {
            isPickingIconImage = true;
            mGetContent.launch("image/*");
            dialog.dismiss();
        });

        btnRotate.setOnClickListener(v -> {
            if (myWebView != null) {
                myWebView.evaluateJavascript("if(window.handleNativeIconRotate) window.handleNativeIconRotate('" + sectionId + "', " + itemIndex + ");", null);
            }
            dialog.dismiss();
        });

        colorPreviewSingle.setOnClickListener(v -> {
            currentColorRequestType = "icon_single";
            openNativeColorPickerForIcon(ICON_COLOR_SINGLE_ID, currentColor);
            dialog.dismiss();
        });

        colorPreviewSection.setOnClickListener(v -> {
            currentColorRequestType = "icon_section";
            openNativeColorPickerForIcon(ICON_COLOR_SECTION_ID, currentColor);
            dialog.dismiss();
        });

        sliderSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int size = 10 + (progress / 2); // 10px to 60px range approx
                txtSizeVal.setText(size + "px");
                if (fromUser && myWebView != null) {
                    myWebView.evaluateJavascript("if(window.handleNativeIconSize) window.handleNativeIconSize('" + sectionId + "', " + size + ");", null);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        dialog.show();
    }

    private void openNativeColorPickerForIcon(int dialogId, String currentColor) {
        onNativePanelOpened();
        int color = Color.BLACK;
        try {
            if (currentColor != null && !currentColor.isEmpty()) {
                color = Color.parseColor(currentColor);
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid color format: " + currentColor);
        }
        
        ColorPickerDialog.newBuilder()
                .setDialogId(dialogId)
                .setColor(color)
                .setShowAlphaSlider(true)
                .show(MainActivity.this);
    }

    private void handleContactIconImageSelection(Uri uri) {
        if (uri == null) return;
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                byte[] bytes = outputStream.toByteArray();
                String base64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                String dataUrl = "data:image/png;base64," + base64; 
                
                if (myWebView != null) {
                    myWebView.post(() -> myWebView.evaluateJavascript("if(window.handleNativeIconImage) window.handleNativeIconImage('" + currentContactSectionId + "', " + currentContactIconIndex + ", '" + dataUrl + "');", null));
                }
                inputStream.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling contact icon image selection", e);
            Toast.makeText(this, "Failed to load icon image", Toast.LENGTH_SHORT).show();
        }
    }

    private static final int CONTACT_COLOR_ID_LEGACY = 1001; // Keep for reference if needed

    private void showContactDetailsSettingsDialog(String sectionId, String settingsJson) {
        onNativePanelOpened();
        currentContactSectionId = sectionId;
        try {
            JSONObject settings = new JSONObject(settingsJson);
            BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.TransparentBottomSheetDialog);
            View view = getLayoutInflater().inflate(R.layout.dialog_contact_settings, null);
            dialog.setContentView(view);

            // Limit height to 60% of screen since we have a vertical list now
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int maxHeight = (int) (displayMetrics.heightPixels * 0.60);
            view.getLayoutParams().height = maxHeight;

            CheckBox checkIcons = view.findViewById(R.id.check_show_icons);
            CheckBox checkText = view.findViewById(R.id.check_show_text);
            SeekBar sliderSize = view.findViewById(R.id.slider_contact_icon_size);
            TextView txtSizeVal = view.findViewById(R.id.txt_contact_icon_size_val);
            View colorPreview = view.findViewById(R.id.color_preview_contact);
            SeekBar sliderSpacing = view.findViewById(R.id.slider_contact_spacing);
            TextView txtSpacingVal = view.findViewById(R.id.txt_contact_spacing_val);
            LinearLayout iconContainer = view.findViewById(R.id.container_contact_icons);
            Button btnDone = view.findViewById(R.id.btn_close_contact_settings);

            // Initial State
            boolean showIcons = settings.optBoolean("showIcons", true);
            boolean showText = settings.optBoolean("showText", false);
            int size = settings.optInt("size", 14);
            String color = settings.optString("color", "#333333");
            int spacing = settings.optInt("spacing", 8);
            JSONArray icons = settings.optJSONArray("icons");

            if (checkIcons != null) checkIcons.setChecked(showIcons);
            if (checkText != null) checkText.setChecked(showText);
            if (sliderSize != null) {
                sliderSize.setProgress(size);
                txtSizeVal.setText(size + "px");
            }
            if (sliderSpacing != null) {
                sliderSpacing.setProgress(spacing);
                txtSpacingVal.setText(spacing + "px");
            }
            try { if (colorPreview != null) colorPreview.setBackgroundColor(Color.parseColor(color)); } catch (Exception e) {}

            // Listeners
            if (checkIcons != null) {
                checkIcons.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    updateContactSetting(sectionId, "showIcons", isChecked);
                });
            }
            if (checkText != null) {
                checkText.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    updateContactSetting(sectionId, "showText", isChecked);
                });
            }

            if (sliderSize != null) {
                sliderSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        txtSizeVal.setText(progress + "px");
                        if (fromUser) updateContactSetting(sectionId, "size", progress);
                    }
                    @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                    @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                });
            }

            if (sliderSpacing != null) {
                sliderSpacing.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        txtSpacingVal.setText(progress + "px");
                        if (fromUser) updateContactSetting(sectionId, "spacing", progress);
                    }
                    @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                    @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                });
            }

            if (colorPreview != null) {
                colorPreview.setOnClickListener(v -> {
                    currentColorRequestType = "contact_icon_global";
                    openNativeColorPickerForContact(color, CONTACT_COLOR_ID);
                    dialog.dismiss();
                });
            }

            // Icons Manager (Vertical List)
            if (icons != null && iconContainer != null) {
                iconContainer.removeAllViews();
                for (int i = 0; i < icons.length(); i++) {
                    JSONObject iconObj = icons.getJSONObject(i);
                    int idx = iconObj.getInt("index");
                    String iconClass = iconObj.optString("class", "fa-link");
                    String iconUrl = iconObj.optString("url", "");
                    String itemText = iconObj.optString("text", "Item " + (i + 1));
                    String itemTint = iconObj.optString("tint", "");
                    int itemSize = iconObj.optInt("customSize", size);

                    View row = getLayoutInflater().inflate(R.layout.item_contact_icon_row, iconContainer, false);
                    ImageView ivPreview = row.findViewById(R.id.iv_icon_preview);
                    TextView tvText = row.findViewById(R.id.tv_contact_text);
                    View customIndicator = row.findViewById(R.id.custom_image_indicator);
                    ImageButton btnTint = row.findViewById(R.id.btn_item_tint);
                    ImageButton btnSize = row.findViewById(R.id.btn_item_size);

                    if (tvText != null) tvText.setText(itemText);

                    if (ivPreview != null) {
                        if (!iconUrl.isEmpty()) {
                            try {
                                String base64 = iconUrl.substring(iconUrl.indexOf(",") + 1);
                                byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
                                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                ivPreview.setImageBitmap(decodedByte);
                                if (customIndicator != null) customIndicator.setVisibility(View.VISIBLE);
                            } catch (Exception e) {
                                ivPreview.setImageResource(getIconResForFontAwesome(iconClass));
                            }
                        } else {
                            ivPreview.setImageResource(getIconResForFontAwesome(iconClass));
                        }
                        
                        // Apply tint to preview if set
                        if (!itemTint.isEmpty()) {
                            try { ivPreview.setColorFilter(Color.parseColor(itemTint)); } catch (Exception e) {}
                        }
                        
                        ivPreview.setOnClickListener(v -> {
                            currentContactIconIndex = idx;
                            isPickingContactIcon = true;
                            mGetContent.launch("image/*");
                            dialog.dismiss();
                        });
                    }

                    if (btnTint != null) {
                        btnTint.setOnClickListener(v -> {
                            currentContactIconIndex = idx;
                            currentColorRequestType = "contact_item_tint";
                            openNativeColorPickerForContact(itemTint.isEmpty() ? color : itemTint, ITEM_TINT_COLOR_ID);
                            dialog.dismiss();
                        });
                    }

                    if (btnSize != null) {
                        btnSize.setOnClickListener(v -> {
                            showItemSizeDialog(sectionId, idx, itemSize);
                        });
                    }

                    iconContainer.addView(row);
                }
            }

            if (btnDone != null) btnDone.setOnClickListener(v -> dialog.dismiss());
            dialog.show();

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing contact settings", e);
        }
    }

    private void showItemSizeDialog(String sectionId, int itemIndex, int currentSize) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 40);

        TextView title = new TextView(this);
        title.setText("Custom Icon Size");
        title.setTextSize(18f);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 0, 0, 20);
        layout.addView(title);

        SeekBar seekBar = new SeekBar(this);
        seekBar.setMax(100);
        seekBar.setProgress(currentSize);
        layout.addView(seekBar);

        TextView valText = new TextView(this);
        valText.setText(currentSize + "px");
        valText.setGravity(Gravity.CENTER);
        valText.setPadding(0, 20, 0, 0);
        layout.addView(valText);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                valText.setText(progress + "px");
                if (fromUser) {
                    try {
                        JSONObject sizeObj = new JSONObject();
                        sizeObj.put("index", itemIndex);
                        sizeObj.put("size", progress);
                        updateContactSetting(sectionId, "itemSize", sizeObj);
                    } catch (JSONException e) {}
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        builder.setView(layout);
        builder.setPositiveButton("DONE", null);
        builder.show();
    }

    private void openNativeColorPickerForProfileFrame(String currentColor) {
        onNativePanelOpened();
        int color = Color.BLACK;
        try {
            if (currentColor != null && !currentColor.isEmpty()) {
                color = Color.parseColor(currentColor);
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid color format: " + currentColor);
        }
        
        ColorPickerDialog.newBuilder()
                .setDialogId(PROFILE_FRAME_COLOR_ID)
                .setColor(color)
                .setShowAlphaSlider(true)
                .show(MainActivity.this);
    }

    private void openNativeColorPickerForContact(String currentColor, int dialogId) {
        onNativePanelOpened();
        int color = Color.BLACK;
        try {
            if (currentColor != null && !currentColor.isEmpty()) {
                color = Color.parseColor(currentColor);
            }
        } catch (IllegalArgumentException e) {
            color = Color.BLACK;
        }

        ColorPickerDialog.newBuilder()
                .setDialogId(dialogId)
                .setColor(color)
                .setShowAlphaSlider(true)
                .show(this);
    }

    // ===== DROP SHAPE PICKER DIALOG =====
    private void showShapePickerDialog() {
        com.google.android.material.bottomsheet.BottomSheetDialog shapeDialog = 
            new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        View shapeView = getLayoutInflater().inflate(R.layout.dialog_shape_picker, null);
        shapeDialog.setContentView(shapeView);

        String[][] shapes = {
            {"btnShapeCircle", "circle"},
            {"btnShapeSquare", "square"},
            {"btnShapeTriangle", "triangle"},
            {"btnShapeHexagon", "hexagon"},
            {"btnShapeHalfCircle", "halfcircle"},
            {"btnShapeStar", "star"},
            {"btnShapeDiamond", "diamond"},
            {"btnShapeSlantBottom", "slant-bottom"},
            {"btnShapeWaveBottom", "wave-bottom"},
            {"btnShapeWaveTop", "wave-top"},
            {"btnShapeSlantTop", "slant-top"},
            {"btnShapeVBottom", "v-bottom"},
            {"btnShapeCurvedSlant", "curved-slant"},
            {"btnShapeRibbonBanner", "ribbon-banner"},
            {"btnShapeUBottom", "u-bottom"}
        };

        for (String[] entry : shapes) {
            int resId = getResources().getIdentifier(entry[0], "id", getPackageName());
            View btn = shapeView.findViewById(resId);
            if (btn != null) {
                String shapeType = entry[1];
                btn.setOnClickListener(v -> {
                    if (myWebView != null) {
                        myWebView.evaluateJavascript("if(window.addDropShape) window.addDropShape('" + shapeType + "');", null);
                    }
                    shapeDialog.dismiss();
                });
            }
        }

        shapeDialog.show();
    }

    // ===== DROP SHAPE TRANSFORM DIALOG =====
    private void showShapeTransformDialog(String shapeId, int currentWidth, int currentHeight, int currentRotation, int currentRadius) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Transform Shape");

        ScrollView scroll = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);
        scroll.addView(layout);

        // State holder array so listeners can read/write the live values
        final int[] state = {currentWidth, currentHeight, currentRotation, currentRadius};

        // Helper function to update JS
        Runnable updateJS = () -> {
            if (myWebView != null) {
                myWebView.evaluateJavascript("if(window.updateDropShapeTransform) window.updateDropShapeTransform('" + shapeId + "', " + state[0] + ", " + state[1] + ", " + state[2] + ", " + state[3] + ");", null);
            }
        };

        // Width slider
        TextView widthLabel = new TextView(this);
        widthLabel.setText("Width: " + currentWidth + "px");
        layout.addView(widthLabel);
        SeekBar widthSeek = new SeekBar(this);
        widthSeek.setMax(1600);
        widthSeek.setProgress(currentWidth);
        widthSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                state[0] = Math.max(10, progress);
                widthLabel.setText("Width: " + state[0] + "px");
                if (fromUser) updateJS.run();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {
                if (myWebView != null) myWebView.evaluateJavascript("if(typeof triggerAutoSave==='function') triggerAutoSave();", null);
            }
        });
        layout.addView(widthSeek);

        // Height slider
        TextView heightLabel = new TextView(this);
        heightLabel.setText("Height: " + currentHeight + "px");
        layout.addView(heightLabel);
        SeekBar heightSeek = new SeekBar(this);
        heightSeek.setMax(1600);
        heightSeek.setProgress(currentHeight);
        heightSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                state[1] = Math.max(10, progress);
                heightLabel.setText("Height: " + state[1] + "px");
                if (fromUser) updateJS.run();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {
                if (myWebView != null) myWebView.evaluateJavascript("if(typeof triggerAutoSave==='function') triggerAutoSave();", null);
            }
        });
        layout.addView(heightSeek);

        // Rotation slider
        TextView rotateLabel = new TextView(this);
        rotateLabel.setText("Rotation: " + currentRotation + "°");
        layout.addView(rotateLabel);
        SeekBar rotateSeek = new SeekBar(this);
        rotateSeek.setMax(360);
        rotateSeek.setProgress(currentRotation);
        rotateSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                state[2] = progress;
                rotateLabel.setText("Rotation: " + state[2] + "°");
                if (fromUser) updateJS.run();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {
                if (myWebView != null) myWebView.evaluateJavascript("if(typeof triggerAutoSave==='function') triggerAutoSave();", null);
            }
        });
        layout.addView(rotateSeek);

        // Corner Radius slider
        TextView radiusLabel = new TextView(this);
        radiusLabel.setText("Corner Radius: " + currentRadius + "px");
        layout.addView(radiusLabel);
        SeekBar radiusSeek = new SeekBar(this);
        radiusSeek.setMax(500);
        radiusSeek.setProgress(currentRadius);
        radiusSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                state[3] = progress;
                radiusLabel.setText("Corner Radius: " + state[3] + "px");
                if (fromUser) updateJS.run();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {
                if (myWebView != null) myWebView.evaluateJavascript("if(typeof triggerAutoSave==='function') triggerAutoSave();", null);
            }
        });
        layout.addView(radiusSeek);

        builder.setView(scroll)
               .setPositiveButton("Done", null)
               .show();
    }

    private void showShapeFrameDialog(String shapeId, int currentThickness, String startColor, String endColor, int sidesCount, String activeSidesJson) {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(this, R.style.TransparentBottomSheetDialog);
        View view = getLayoutInflater().inflate(R.layout.dialog_shape_frame, null);
        dialog.setContentView(view);

        SeekBar seekThickness = view.findViewById(R.id.seek_frame_thickness);
        TextView txtThicknessVal = view.findViewById(R.id.txt_frame_thickness_val);
        View viewStartColor = view.findViewById(R.id.view_start_color);
        View viewEndColor = view.findViewById(R.id.view_end_color);
        Button btnStartColor = view.findViewById(R.id.btn_start_color);
        Button btnEndColor = view.findViewById(R.id.btn_end_color);

        // Store globals for color picker
        pendingShapeFrameStartColorId = shapeId;
        pendingShapeFrameEndColorId = shapeId;
        pendingShapeFrameStartColorView = viewStartColor;
        pendingShapeFrameEndColorView = viewEndColor;
        pendingShapeFrameStartHex = startColor;
        pendingShapeFrameEndHex = endColor;

        seekThickness.setProgress(currentThickness);
        txtThicknessVal.setText(String.valueOf(currentThickness));

        try { viewStartColor.setBackgroundColor(Color.parseColor(startColor)); } catch (Exception e) {}
        try { viewEndColor.setBackgroundColor(Color.parseColor(endColor)); } catch (Exception e) {}

        android.widget.GridLayout containerSides = view.findViewById(R.id.container_frame_sides);
        if (containerSides != null && sidesCount > 0) {
            containerSides.removeAllViews();
            try {
                org.json.JSONArray json = new org.json.JSONArray(activeSidesJson);
                for (int i = 0; i < sidesCount; i++) {
                    android.widget.CheckBox cb = new android.widget.CheckBox(this);
                    cb.setText("Side " + (i + 1));
                    cb.setChecked(json.optBoolean(i, true));
                    cb.setTextColor(Color.parseColor("#333333"));
                    int finalI = i;
                    cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if (myWebView != null) {
                            myWebView.evaluateJavascript("if(window.updateDropShapeFrameSide) window.updateDropShapeFrameSide('" + shapeId + "', " + finalI + ", " + isChecked + ");", null);
                        }
                    });
                    containerSides.addView(cb);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        seekThickness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtThicknessVal.setText(String.valueOf(progress));
                if (myWebView != null && fromUser) {
                    myWebView.evaluateJavascript("if(window.updateDropShapeFrameThickness) window.updateDropShapeFrameThickness('" + shapeId + "', " + progress + ");", null);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                if (myWebView != null) myWebView.evaluateJavascript("if(typeof triggerAutoSave==='function') triggerAutoSave();", null);
            }
        });

        btnStartColor.setOnClickListener(v -> {
            int initColor = Color.parseColor("#e53935");
            try { initColor = Color.parseColor(pendingShapeFrameStartHex); } catch (Exception e) {}
            ColorPickerDialog.newBuilder()
                .setColor(initColor)
                .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                .setAllowPresets(true)
                .setDialogId(DROP_SHAPE_FRAME_START_COLOR_ID)
                .setShowAlphaSlider(false)
                .show(MainActivity.this);
        });

        btnEndColor.setOnClickListener(v -> {
            int initColor = Color.parseColor("#1e3c72");
            try { initColor = Color.parseColor(pendingShapeFrameEndHex); } catch (Exception e) {}
            ColorPickerDialog.newBuilder()
                .setColor(initColor)
                .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                .setAllowPresets(true)
                .setDialogId(DROP_SHAPE_FRAME_END_COLOR_ID)
                .setShowAlphaSlider(false)
                .show(MainActivity.this);
        });

        dialog.show();
    }

    // --- Bottom Sheet Setup ---
    private void showSectionSettingsDialog(String sectionId) {
        onNativePanelOpened();
        currentEditingSectionId = sectionId;
        if (myWebView == null) return;
        
        myWebView.evaluateJavascript("if(window.getSectionStructure) window.getSectionStructure('" + sectionId + "');", value -> {
            if (value == null || value.equals("null")) return;
            
            // Unescape JSON string if doubly encoded
            String jsonStr = value;
            if (jsonStr.startsWith("\"") && jsonStr.endsWith("\"")) {
                jsonStr = jsonStr.substring(1, jsonStr.length() - 1).replace("\\\"", "\"");
            }
            
            final String finalJson = jsonStr;
            runOnUiThread(() -> {
                try {
                    JSONObject data = new JSONObject(finalJson);
                    String title = data.optString("title", "Section");
                    JSONArray items = data.optJSONArray("items");
                    
                    selectedItemIds.clear();
                    
                    BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.TransparentBottomSheetDialog);
                    currentSectionSettingsDialog = dialog;
                    dialog.setOnDismissListener(d -> {
                        if (currentSectionSettingsDialog == d) {
                            currentSectionSettingsDialog = null;
                        }
                    });
                    
                    View view = getLayoutInflater().inflate(R.layout.dialog_section_settings, null);
                    dialog.setContentView(view);

                    // Setup Tabs (REMOVED: Using Button Toggle Instead)
                    // ...

                    // References for Toggle
                    View layoutStyles = view.findViewById(R.id.layout_styles);
                    View layoutItems = view.findViewById(R.id.layout_items);
                    
                    // Setup Style Carousel
                    ViewPager2 pager = view.findViewById(R.id.pager_styles);
                    TabLayout indicator = view.findViewById(R.id.indicator_styles);
                    
                    String[] baseNames = {"Default", "Hidden Line", "No Headings", "Custom Style"};
                    String[] baseIds = {"Default", "Hidden Line", "no-headings", "Custom Style"}; // Matching JS logic
                    
                    // --- Load Saved Custom Styles ---
                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    String savedStylesStr = prefs.getString("SavedCustomStyles", "[]");
                    ArrayList<String> fullNames = new ArrayList<>(java.util.Arrays.asList(baseNames));
                    ArrayList<String> fullIds = new ArrayList<>(java.util.Arrays.asList(baseIds));
                    JSONArray parsedStyles = new JSONArray();
                    try {
                        parsedStyles = new JSONArray(savedStylesStr);
                        for (int i = 0; i < parsedStyles.length(); i++) {
                            JSONObject styleObj = parsedStyles.getJSONObject(i);
                            fullNames.add(styleObj.getString("name"));
                            fullIds.add(styleObj.getString("id"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    final JSONArray savedStyles = parsedStyles;

                    String[] finalNames = fullNames.toArray(new String[0]);
                    String[] finalIds = fullIds.toArray(new String[0]);
                    
                    StyleCarouselAdapter adapter = new StyleCarouselAdapter(finalNames, finalIds);
                    pager.setAdapter(adapter);
                    
                    new com.google.android.material.tabs.TabLayoutMediator(indicator, pager, (tab, position) -> {}).attach();
                    
                    View btnCustomHeaderSettings = view.findViewById(R.id.btnCustomHeaderSettings);
                    View customHeaderPanel = view.findViewById(R.id.layout_custom_header_panel);
                    View layoutSaveStyle = view.findViewById(R.id.layout_save_style);
                    TextView btnSaveNewStyle = view.findViewById(R.id.btnSaveNewStyle);
                    EditText inputNewStyleName = view.findViewById(R.id.inputNewStyleName);

                    // Handle Save custom style
                    if (btnSaveNewStyle != null && inputNewStyleName != null) {
                        btnSaveNewStyle.setOnClickListener(v -> {
                            String name = inputNewStyleName.getText().toString().trim();
                            if (name.isEmpty()) {
                                Toast.makeText(MainActivity.this, "Enter a style name", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (myWebView != null) {
                                myWebView.evaluateJavascript("if(window.saveCurrentCustomStyle) window.saveCurrentCustomStyle('" + sectionId + "', '" + name + "');", null);
                            }
                        });
                    }

                    pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                        @Override
                        public void onPageSelected(int position) {
                            String selectedStyle = finalIds[position];
                            // Show settings and save UI only for "Custom Style" specifically
                            boolean isBaseCustom = selectedStyle.equals("Custom Style");
                            
                            if (btnCustomHeaderSettings != null) {
                                btnCustomHeaderSettings.setVisibility(isBaseCustom ? View.VISIBLE : View.GONE);
                            }
                            if (layoutSaveStyle != null) {
                                layoutSaveStyle.setVisibility(isBaseCustom ? View.VISIBLE : View.GONE);
                            }
                            if (!isBaseCustom && customHeaderPanel != null) {
                                customHeaderPanel.setVisibility(View.GONE);
                            }
                            if (myWebView != null) {
                                if (selectedStyle.startsWith("CustomStyle_")) {
                                    // It's a saved custom style. Look up properties and pass them to JS.
                                    String propsJson = "{}";
                                    try {
                                        for (int i = 0; i < savedStyles.length(); i++) {
                                            if (savedStyles.getJSONObject(i).getString("id").equals(selectedStyle)) {
                                                propsJson = savedStyles.getJSONObject(i).getJSONObject("props").toString();
                                                break;
                                            }
                                        }
                                    } catch (Exception e) {}
                                    myWebView.evaluateJavascript("if(window.applySavedCustomStyle) window.applySavedCustomStyle('" + sectionId + "', '" + propsJson + "');", null);
                                } else {
                                    // Base styles (Default, Hidden Line, Custom Style)
                                    myWebView.evaluateJavascript("if(window.updateSectionHeaderStyle) window.updateSectionHeaderStyle('" + sectionId + "', '" + selectedStyle + "');", null);
                                }
                            }
                        }
                    });

                    // Setup Custom Header Panel Listeners
                    if (btnCustomHeaderSettings != null && customHeaderPanel != null) {
                        btnCustomHeaderSettings.setOnClickListener(v -> {
                            TransitionManager.beginDelayedTransition((ViewGroup) view);
                            customHeaderPanel.setVisibility(customHeaderPanel.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                        });

                        // Swap Icon Position
                        View btnSwap = view.findViewById(R.id.btnSwapIconPosition);
                        if (btnSwap != null) {
                            btnSwap.setOnClickListener(v -> {
                                if (myWebView != null) {
                                    myWebView.evaluateJavascript("if(window.updateSectionHeaderCustomStyle) window.updateSectionHeaderCustomStyle('" + sectionId + "', {swapIcon: true});", null);
                                }
                            });
                        }

                        // Connect Icon + Title
                        View btnConnect = view.findViewById(R.id.btnConnectIconTitle);
                        if (btnConnect != null) {
                            btnConnect.setOnClickListener(v -> {
                                if (myWebView != null) {
                                    myWebView.evaluateJavascript("if(window.updateSectionHeaderCustomStyle) window.updateSectionHeaderCustomStyle('" + sectionId + "', {connectToggle: true});", null);
                                }
                                // Toggle button appearance
                                boolean isActive = v.getTag() != null && (boolean) v.getTag();
                                isActive = !isActive;
                                v.setTag(isActive);
                                if (v instanceof TextView) {
                                    ((TextView) v).setBackgroundTintList(android.content.res.ColorStateList.valueOf(isActive ? 0xFFE8EAF6 : 0xFFF0F0F0));
                                    ((TextView) v).setTextColor(isActive ? 0xFF1E3C72 : 0xFF444444);
                                }
                            });
                        }

                        // Color Buttons
                        view.findViewById(R.id.btnHeaderIconBgColor).setOnClickListener(v -> {
                            openNativeColorPickerForContact("#FFFFFF", HEADER_ICON_BG_COLOR_ID);
                        });
                        view.findViewById(R.id.btnHeaderTextBgColor).setOnClickListener(v -> {
                            openNativeColorPickerForContact("#FFFFFF", HEADER_TITLE_BG_COLOR_ID);
                        });

                        // Sliders
                        SeekBar sliderIconRadius = view.findViewById(R.id.sliderHeaderIconRadius);
                        SeekBar sliderTextRadius = view.findViewById(R.id.sliderHeaderTextRadius);

                        sliderIconRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (fromUser && myWebView != null) {
                                    myWebView.evaluateJavascript("if(window.updateSectionHeaderCustomStyle) window.updateSectionHeaderCustomStyle('" + sectionId + "', {iconRadius: " + progress + "});", null);
                                }
                            }
                            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                        });

                        sliderTextRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (fromUser && myWebView != null) {
                                    myWebView.evaluateJavascript("if(window.updateSectionHeaderCustomStyle) window.updateSectionHeaderCustomStyle('" + sectionId + "', {textRadius: " + progress + "});", null);
                                }
                            }
                            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                        });

                        // Icon Size slider
                        SeekBar sliderIconSize = view.findViewById(R.id.sliderHeaderIconSize);
                        if (sliderIconSize != null) {
                            sliderIconSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                @Override
                                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                    if (fromUser && myWebView != null) {
                                        myWebView.evaluateJavascript("if(window.updateSectionHeaderCustomStyle) window.updateSectionHeaderCustomStyle('" + sectionId + "', {iconSize: " + progress + "});", null);
                                    }
                                }
                                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                            });
                        }

                        // Title Font Size slider
                        SeekBar sliderTitleFontSize = view.findViewById(R.id.sliderHeaderTitleFontSize);
                        if (sliderTitleFontSize != null) {
                            sliderTitleFontSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                @Override
                                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                    if (fromUser && myWebView != null) {
                                        int size = Math.max(8, progress);
                                        myWebView.evaluateJavascript("if(window.updateSectionHeaderCustomStyle) window.updateSectionHeaderCustomStyle('" + sectionId + "', {titleFontSize: " + size + "});", null);
                                    }
                                }
                                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                            });
                        }

                        // --- Shape Dimension & Color Controls ---
                        // Helper for simple seekbar->JS property binding
                        int[][] dimSliders = {
                            {R.id.sliderLeftShapeW}, {R.id.sliderLeftShapeH},
                            {R.id.sliderRightShapeW}, {R.id.sliderRightShapeH},
                            {R.id.sliderConnectorW}, {R.id.sliderConnectorH}
                        };
                        String[] dimProps = {"textLeftW", "textLeftH", "textRightW", "textRightH", "connectorW", "connectorH"};
                        for (int d = 0; d < dimSliders.length; d++) {
                            SeekBar sb = view.findViewById(dimSliders[d][0]);
                            if (sb == null) continue;
                            final String prop = dimProps[d];
                            sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                @Override
                                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                    if (fromUser && myWebView != null) {
                                        myWebView.evaluateJavascript("if(window.updateSectionHeaderCustomStyle) window.updateSectionHeaderCustomStyle('" + sectionId + "', {" + prop + ": " + progress + "});", null);
                                    }
                                }
                                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                            });
                        }

                        // Shape color pickers
                        View btnLeftColor = view.findViewById(R.id.btnLeftShapeColor);
                        if (btnLeftColor != null) btnLeftColor.setOnClickListener(v -> openNativeColorPickerForContact("#AAAAAA", HEADER_LEFT_SHAPE_COLOR_ID));

                        View btnRightColor = view.findViewById(R.id.btnRightShapeColor);
                        if (btnRightColor != null) btnRightColor.setOnClickListener(v -> openNativeColorPickerForContact("#AAAAAA", HEADER_RIGHT_SHAPE_COLOR_ID));

                        View btnConnColor = view.findViewById(R.id.btnConnectorColor);
                        if (btnConnColor != null) btnConnColor.setOnClickListener(v -> openNativeColorPickerForContact("#AAAAAA", HEADER_CONNECTOR_COLOR_ID));

                        View btnFontColor = view.findViewById(R.id.btnTitleFontColor);
                        if (btnFontColor != null) btnFontColor.setOnClickListener(v -> openNativeColorPickerForContact("#333333", HEADER_TITLE_FONT_COLOR_ID));

                        // --- Frame System ---
                        // Icon Frame
                        SeekBar sliderIconFrame = view.findViewById(R.id.sliderIconFrameWidth);
                        if (sliderIconFrame != null) {
                            sliderIconFrame.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                @Override
                                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                    if (fromUser && myWebView != null) {
                                        myWebView.evaluateJavascript("if(window.updateSectionHeaderCustomStyle) window.updateSectionHeaderCustomStyle('" + sectionId + "', {iconFrameWidth: " + progress + "});", null);
                                    }
                                }
                                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                            });
                        }
                        View btnIconFrameColor = view.findViewById(R.id.btnIconFrameColor);
                        if (btnIconFrameColor != null) {
                            btnIconFrameColor.setOnClickListener(v -> openNativeColorPickerForContact("#333333", HEADER_ICON_FRAME_COLOR_ID));
                        }

                        // Title Frame
                        SeekBar sliderTitleFrame = view.findViewById(R.id.sliderTitleFrameWidth);
                        if (sliderTitleFrame != null) {
                            sliderTitleFrame.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                @Override
                                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                    if (fromUser && myWebView != null) {
                                        myWebView.evaluateJavascript("if(window.updateSectionHeaderCustomStyle) window.updateSectionHeaderCustomStyle('" + sectionId + "', {textFrameWidth: " + progress + "});", null);
                                    }
                                }
                                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                            });
                        }
                        View btnTitleFrameColor = view.findViewById(R.id.btnTitleFrameColor);
                        if (btnTitleFrameColor != null) {
                            btnTitleFrameColor.setOnClickListener(v -> openNativeColorPickerForContact("#333333", HEADER_TITLE_FRAME_COLOR_ID));
                        }

                        // --- Shape Extension Buttons ---

                        // Inline helper: sets up a row of shape buttons
                        // Each button gets tag=shapeId, click resets row tints and calls JS
                        int[][] shapeGroups = {
                            {R.id.btnShapeTitleLeftNone, R.id.btnShapeTitleLeftTriangle, R.id.btnShapeTitleLeftHalfCircle, R.id.btnShapeTitleLeftArrow},
                            {R.id.btnShapeTitleRightNone, R.id.btnShapeTitleRightTriangle, R.id.btnShapeTitleRightHalfCircle, R.id.btnShapeTitleRightArrow},
                            {R.id.btnShapeIconNone, R.id.btnShapeIconTriangle, R.id.btnShapeIconCircle, R.id.btnShapeIconHexagon},
                            {R.id.btnConnectorNone, R.id.btnConnectorLine, R.id.btnConnectorDot, R.id.btnConnectorDiamond, R.id.btnConnectorHexagon}
                        };
                        String[] shapeProps = {"textLeftShape", "textRightShape", "iconShape", "connectorShape"};

                        for (int g = 0; g < shapeGroups.length; g++) {
                            final String propName = shapeProps[g];
                            final int[] btnIds = shapeGroups[g];
                            for (int si = 0; si < btnIds.length; si++) {
                                View btn = view.findViewById(btnIds[si]);
                                if (btn == null) continue;
                                final int shapeId = si;
                                final int[] allBtnIds = btnIds;
                                btn.setOnClickListener(v -> {
                                    // Reset all in row
                                    for (int id : allBtnIds) {
                                        View sibling = view.findViewById(id);
                                        if (sibling != null) {
                                            sibling.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFF5F5F5));
                                            if (sibling instanceof TextView) ((TextView) sibling).setTextColor(0xFF666666);
                                        }
                                    }
                                    // Highlight selected
                                    v.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFE8EAF6));
                                    if (v instanceof TextView) ((TextView) v).setTextColor(0xFF1E3C72);

                                    if (myWebView != null) {
                                        myWebView.evaluateJavascript("if(window.updateSectionHeaderCustomStyle) window.updateSectionHeaderCustomStyle('" + sectionId + "', {" + propName + ": " + shapeId + "});", null);
                                    }
                                });
                            }
                        }
                    }
                    
                    // Setup Header Title
                    EditText titleView = view.findViewById(R.id.headerTitle);
                    if (titleView != null) {
                        titleView.setText(title);
                        titleView.addTextChangedListener(new android.text.TextWatcher() {
                            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                            @Override public void afterTextChanged(android.text.Editable s) {
                                if (myWebView != null) {
                                    myWebView.evaluateJavascript("window.updateSectionTitle('" + sectionId + "', '" + s.toString().replace("'", "\\'") + "');", null);
                                }
                            }
                        });
                    }

                    // Setup Icon Picker
                    View iconContainer = view.findViewById(R.id.headerIconContainer);
                    if(iconContainer != null) {
                        iconContainer.setOnClickListener(v -> {
                            currentSectionIdForIcon = sectionId;
                            isPickingSectionIcon = true;
                            openFileChooser(null);
                        });
                    }

                    // Setup Buttons & Containers
                    LinearLayout btnHeaderStyle = view.findViewById(R.id.btnHeaderStyle);
                    LinearLayout btnSectionColor = view.findViewById(R.id.btnSectionColor);
                    LinearLayout btnAlignment = view.findViewById(R.id.btnAlignment);
                    LinearLayout btnGrid = view.findViewById(R.id.btnGrid);
                    LinearLayout btnApplyToAll = view.findViewById(R.id.btnApplyToAll);

                    if (btnHeaderStyle != null) {
                        btnHeaderStyle.setOnClickListener(v -> {
                             // Toggle Logic
                             TransitionManager.beginDelayedTransition((ViewGroup) view);
                             if (layoutStyles.getVisibility() == View.VISIBLE) {
                                 // Close Styles, Show Items
                                 layoutStyles.setVisibility(View.GONE);
                                 layoutItems.setVisibility(View.VISIBLE);
                                 // Optional: Update Button Tint to inactive?
                                 ((TextView)view.findViewById(R.id.txtStyleBtn)).setTextColor(0xFF666666);
                                 ((ImageView)view.findViewById(R.id.iconStyleBtn)).setColorFilter(0xFF666666);
                             } else {
                                 // Show Styles, Hide Items
                                 layoutItems.setVisibility(View.GONE);
                                 layoutStyles.setVisibility(View.VISIBLE);
                                 // Optional: Update Button Tint to active
                                 ((TextView)view.findViewById(R.id.txtStyleBtn)).setTextColor(0xFF1E3C72);
                                 ((ImageView)view.findViewById(R.id.iconStyleBtn)).setColorFilter(0xFF1E3C72);
                             }
                        });
                    }

                    LinearLayout containerAlignmentSlider = view.findViewById(R.id.containerAlignmentSlider);
                    SeekBar sliderAlignment = view.findViewById(R.id.sliderAlignment);
                    TextView txtAlignmentVal = view.findViewById(R.id.txtAlignmentVal);
                    LinearLayout containerGridSpacing = view.findViewById(R.id.containerGridSpacing);
                    
                    CheckBox chkAlignHeader = view.findViewById(R.id.chkAlignHeader);
                    CheckBox chkAlignItems = view.findViewById(R.id.chkAlignItems);

                    View.OnTouchListener sliderTouchListener = (v, event) -> {
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        if ((event.getAction() & android.view.MotionEvent.ACTION_MASK) == android.view.MotionEvent.ACTION_UP) {
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                        }
                        return false;
                    };

                    if (btnAlignment != null && containerAlignmentSlider != null) {
                        btnAlignment.setOnClickListener(v -> {
                            TransitionManager.beginDelayedTransition((ViewGroup) view.findViewById(R.id.container_row2));
                            if (containerAlignmentSlider.getVisibility() == View.VISIBLE) {
                                containerAlignmentSlider.setVisibility(View.GONE);
                            } else {
                                if (containerGridSpacing != null) containerGridSpacing.setVisibility(View.GONE);
                                containerAlignmentSlider.setVisibility(View.VISIBLE);
                            }
                        });
                    }

                    // --- Grid Layout Spinners ---
                    Spinner spinnerGridCols = view.findViewById(R.id.spinnerGridCols);
                    Spinner spinnerGridRows = view.findViewById(R.id.spinnerGridRows);

                    String[] gridOptions = {"1", "2", "3", "4", "5", "Unlimited"};
                    android.widget.ArrayAdapter<String> colsAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_item, gridOptions);
                    colsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    android.widget.ArrayAdapter<String> rowsAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_item, gridOptions);
                    rowsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    if (spinnerGridCols != null) spinnerGridCols.setAdapter(colsAdapter);
                    if (spinnerGridRows != null) {
                        rowsAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_item, gridOptions);
                        rowsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerGridRows.setAdapter(rowsAdapter);
                        spinnerGridRows.setSelection(5); // Default: Unlimited rows
                    }

                    // Read current grid state from section data
                    int currentCols = data.optInt("gridCols", 1);
                    int currentRows = data.optInt("gridRows", 0); // 0 = unlimited
                    if (spinnerGridCols != null) {
                        if (currentCols >= 1 && currentCols <= 5) spinnerGridCols.setSelection(currentCols - 1);
                        else spinnerGridCols.setSelection(0); // default 1
                    }
                    if (spinnerGridRows != null) {
                        if (currentRows >= 1 && currentRows <= 5) spinnerGridRows.setSelection(currentRows - 1);
                        else spinnerGridRows.setSelection(5); // Unlimited
                    }

                    // Spinner change listeners
                    final boolean[] spinnerInitialized = {false, false};
                    if (spinnerGridCols != null) {
                        spinnerGridCols.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(android.widget.AdapterView<?> parent, View v2, int position, long id) {
                                if (!spinnerInitialized[0]) { spinnerInitialized[0] = true; return; }
                                int cols = (position < 5) ? (position + 1) : 0; // 0 = unlimited
                                int rows = 0;
                                if (spinnerGridRows != null) {
                                    int rPos = spinnerGridRows.getSelectedItemPosition();
                                    rows = (rPos < 5) ? (rPos + 1) : 0;
                                }
                                if (myWebView != null) {
                                    myWebView.evaluateJavascript("window.updateSectionGridLayout('" + sectionId + "', " + cols + ", " + rows + ");", null);
                                }
                            }
                            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                        });
                    }
                    if (spinnerGridRows != null) {
                        spinnerGridRows.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(android.widget.AdapterView<?> parent, View v2, int position, long id) {
                                if (!spinnerInitialized[1]) { spinnerInitialized[1] = true; return; }
                                int rows = (position < 5) ? (position + 1) : 0;
                                int cols = 0;
                                if (spinnerGridCols != null) {
                                    int cPos = spinnerGridCols.getSelectedItemPosition();
                                    cols = (cPos < 5) ? (cPos + 1) : 0;
                                }
                                if (myWebView != null) {
                                    myWebView.evaluateJavascript("window.updateSectionGridLayout('" + sectionId + "', " + cols + ", " + rows + ");", null);
                                }
                            }
                            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                        });
                    }

                    if (btnGrid != null && containerGridSpacing != null) {
                        btnGrid.setOnClickListener(v -> {
                            TransitionManager.beginDelayedTransition((ViewGroup) view);
                            if (containerGridSpacing.getVisibility() == View.VISIBLE) {
                                containerGridSpacing.setVisibility(View.GONE);
                                ((TextView)view.findViewById(R.id.txtGridBtn)).setTextColor(0xFF666666);
                                ((ImageView)view.findViewById(R.id.iconGridBtn)).setColorFilter(0xFF666666);
                            } else {
                                if (containerAlignmentSlider != null) containerAlignmentSlider.setVisibility(View.GONE);
                                // Also hide styles and show items when opening grid
                                if (layoutStyles.getVisibility() == View.VISIBLE) {
                                    layoutStyles.setVisibility(View.GONE);
                                    layoutItems.setVisibility(View.VISIBLE);
                                    ((TextView)view.findViewById(R.id.txtStyleBtn)).setTextColor(0xFF666666);
                                    ((ImageView)view.findViewById(R.id.iconStyleBtn)).setColorFilter(0xFF666666);
                                }
                                containerGridSpacing.setVisibility(View.VISIBLE);
                                ((TextView)view.findViewById(R.id.txtGridBtn)).setTextColor(0xFF1E3C72);
                                ((ImageView)view.findViewById(R.id.iconGridBtn)).setColorFilter(0xFF1E3C72);
                            }
                        });
                    }

                    if (btnSectionColor != null) {
                        btnSectionColor.setOnClickListener(v -> {
                             currentEditingSectionId = sectionId;
                             String sectionBg = data.optString("bgColor", "#FFFFFF");
                             openNativeColorPickerForContact(sectionBg, CURRENT_SECTION_BG_COLOR_ID); 
                        });
                    }

                    if (btnApplyToAll != null) {
                        btnApplyToAll.setOnClickListener(v -> {
                             AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                             builder.setTitle("Apply to All Sections");
                             String[] options = {
                                 "Header Style", "Grid Spacing", "Grid Layout", "Item Width", "Alignment", "Background Color",
                                 "Subsection Background", "Subsection Line Height", "Subsection Roundness"
                             };
                             boolean[] checkedItems = {true, true, true, true, true, true, true, true, true};
                             builder.setMultiChoiceItems(options, checkedItems, (dialog1, which, isChecked) -> {
                                 checkedItems[which] = isChecked;
                             });
                             builder.setPositiveButton("Apply", (dialog1, which) -> {
                                 try {
                                     JSONObject opts = new JSONObject();
                                     opts.put("style", checkedItems[0]);
                                     opts.put("grid", checkedItems[1]);
                                     opts.put("gridLayout", checkedItems[2]);
                                     opts.put("itemWidth", checkedItems[3]);
                                     opts.put("align", checkedItems[4]);
                                     opts.put("color", checkedItems[5]);
                                     opts.put("subBg", checkedItems[6]);
                                     opts.put("subLine", checkedItems[7]);
                                     opts.put("subRound", checkedItems[8]);
                                     if(myWebView != null) {
                                         myWebView.evaluateJavascript("window.applySettingsToAllSections('" + sectionId + "', " + opts.toString() + ");", null);
                                     }
                                 } catch(JSONException e) { Log.e(TAG, "ApplyAll JSON error", e); }
                             });
                             builder.setNegativeButton("Cancel", null);
                             builder.show();
                        });
                    }

                    if (sliderAlignment != null) {
                        sliderAlignment.setOnTouchListener(sliderTouchListener);
                        
                        SeekBar.OnSeekBarChangeListener alignListener = new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                String alignStr = "Center";
                                if(progress == 0) alignStr = "Left";
                                if(progress == 2) alignStr = "Right";
                                if(txtAlignmentVal != null) txtAlignmentVal.setText(alignStr);
                                
                                // Only trigger if from user OR if explicitly called by checkbox
                                if (myWebView != null) {
                                    boolean applyHeader = chkAlignHeader != null && chkAlignHeader.isChecked();
                                    boolean applyItems = chkAlignItems != null && chkAlignItems.isChecked();
                                    myWebView.evaluateJavascript("window.updateSectionAlignment('" + sectionId + "', '" + alignStr.toLowerCase() + "', " + applyHeader + ", " + applyItems + ");", null);
                                }
                            }
                            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                        };
                        
                        sliderAlignment.setOnSeekBarChangeListener(alignListener);
                        
                        // Checkbox listeners to re-trigger alignment
                        if (chkAlignHeader != null) {
                            chkAlignHeader.setOnCheckedChangeListener((buttonView, isChecked) -> {
                                alignListener.onProgressChanged(sliderAlignment, sliderAlignment.getProgress(), true);
                            });
                        }
                        if (chkAlignItems != null) {
                            chkAlignItems.setOnCheckedChangeListener((buttonView, isChecked) -> {
                                alignListener.onProgressChanged(sliderAlignment, sliderAlignment.getProgress(), true);
                            });
                        }
                    }

                    // Setup Grid Spacing
                    SeekBar gridSlider = view.findViewById(R.id.sliderGridSpacing);
                    TextView gridVal = view.findViewById(R.id.txtGridSpacingVal);
                    if (gridSlider != null) {
                        gridSlider.setOnTouchListener(sliderTouchListener);
                        // Default check? Or maybe we can pass it in JSON later. defaulting to 10 for now.
                        gridSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (fromUser) {
                                    if(gridVal != null) gridVal.setText(progress + "px");
                                    if(myWebView != null) {
                                        myWebView.evaluateJavascript("window.updateSectionGridSpacing('" + sectionId + "', " + progress + ");", null);
                                    }
                                }
                            }
                            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                        });
                    }
                    
                    // Setup Item Width
                    SeekBar itemWidthSlider = view.findViewById(R.id.sliderItemWidth);
                    TextView itemWidthVal = view.findViewById(R.id.txtItemWidthVal);
                    if (itemWidthSlider != null) {
                        itemWidthSlider.setOnTouchListener(sliderTouchListener);
                        // Read current value from section data
                        int currentItemWidth = data.optInt("itemWidth", 0);
                        itemWidthSlider.setProgress(currentItemWidth);
                        if (itemWidthVal != null) itemWidthVal.setText(currentItemWidth == 0 ? "Auto" : currentItemWidth + "px");

                        itemWidthSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (fromUser) {
                                    if (itemWidthVal != null) itemWidthVal.setText(progress == 0 ? "Auto" : progress + "px");
                                    if (myWebView != null) {
                                        myWebView.evaluateJavascript("window.updateSectionItemWidth('" + sectionId + "', " + progress + ");", null);
                                    }
                                }
                            }
                            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                        });
                    }
                    // Setup Select All
                    TextView btnSelectAll = view.findViewById(R.id.btnSelectAll);
                    if (btnSelectAll != null) {
                        btnSelectAll.setOnClickListener(v -> {
                            boolean allSelected = selectedItemIds.size() == items.length();
                            if (allSelected) {
                                selectedItemIds.clear();
                                btnSelectAll.setText("Select All");
                            } else {
                                selectedItemIds.clear();
                                for (int i = 0; i < items.length(); i++) {
                                    try { selectedItemIds.add(items.getJSONObject(i).getString("id")); } catch (JSONException e) {}
                                }
                                btnSelectAll.setText("Deselect All");
                            }
                            
                            // Update all checkboxes and background states
                            LinearLayout subsectionContainer = view.findViewById(R.id.subsectionContainer);
                            if (subsectionContainer != null) {
                                for (int i = 0; i < subsectionContainer.getChildCount(); i++) {
                                    View itemView = subsectionContainer.getChildAt(i);
                                    CheckBox cb = itemView.findViewById(R.id.itemSelectCb);
                                    if (cb != null) cb.setChecked(!allSelected);
                                }
                            }
                            updateMultiSelectUIVisibility(view, items);
                        });
                    }

                    // Setup Items
                    LinearLayout container = view.findViewById(R.id.subsectionContainer);
                    if (container != null) {
                        container.removeAllViews();
                        
                        if (items != null) {
                            for (int i = 0; i < items.length(); i++) {
                                JSONObject item = items.getJSONObject(i);
                                String itemId = item.getString("id");
                                String itemText = item.getString("text");
                                String currentBg = item.optString("bgColor", "");
                                
                                View itemView = getLayoutInflater().inflate(R.layout.item_subsection, container, false);
                                TextView nameView = itemView.findViewById(R.id.itemName);
                                nameView.setText(itemText);
                                
                                // Selection Logic
                                CheckBox itemSelectCb = itemView.findViewById(R.id.itemSelectCb);
                                itemSelectCb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                                    if (isChecked) selectedItemIds.add(itemId);
                                    else selectedItemIds.remove(itemId);
                                    updateMultiSelectUIVisibility(view, items);
                                });
                                
                                // Color Logic
                                View colorView = itemView.findViewById(R.id.viewBgColor);
                                colorView.setOnClickListener(v -> {
                                    currentEditingItemId = itemId;
                                    openNativeColorPickerForFrame(currentBg, ITEM_BG_COLOR_ID);
                                });
                                
                                // Line Space Slider Logic
                                View btnLineSpace = itemView.findViewById(R.id.btnLineSpace);
                                SeekBar sliderLineSpace = itemView.findViewById(R.id.sliderLineSpace);
                                TextView lblLineSpace = itemView.findViewById(R.id.lblLineSpace);

                                // Roundness Slider Logic
                                View btnRoundness = itemView.findViewById(R.id.btnRoundness);
                                SeekBar sliderRoundness = itemView.findViewById(R.id.sliderRoundness);
                                TextView lblRoundness = itemView.findViewById(R.id.lblRoundness);
                                
                                ViewGroup actionsContainer = itemView.findViewById(R.id.actionsContainer);

                                // Helper to toggle single slider mode
                                View.OnClickListener toggleSlider = v -> {
                                    TransitionManager.beginDelayedTransition(actionsContainer);
                                    
                                    boolean showingLineSpace = (v == btnLineSpace && sliderLineSpace.getVisibility() != View.VISIBLE);
                                    boolean showingRoundness = (v == btnRoundness && sliderRoundness.getVisibility() != View.VISIBLE);

                                    // Reset all first
                                    btnLineSpace.setVisibility(View.VISIBLE);
                                    sliderLineSpace.setVisibility(View.GONE);
                                    if(lblLineSpace != null) lblLineSpace.setVisibility(View.GONE);

                                    if(colorView != null) colorView.setVisibility(View.VISIBLE);
                                    
                                    btnRoundness.setVisibility(View.VISIBLE);
                                    sliderRoundness.setVisibility(View.GONE);
                                    if(lblRoundness != null) lblRoundness.setVisibility(View.GONE);

                                    if (showingLineSpace) {
                                        // Show Line Space Slider, Hide others
                                        if(colorView != null) colorView.setVisibility(View.GONE);
                                        btnRoundness.setVisibility(View.GONE);
                                        sliderLineSpace.setVisibility(View.VISIBLE);
                                        if(lblLineSpace != null) lblLineSpace.setVisibility(View.VISIBLE);
                                    } else if (showingRoundness) {
                                        // Show Roundness Slider, Hide others
                                        btnLineSpace.setVisibility(View.GONE);
                                        if(colorView != null) colorView.setVisibility(View.GONE);
                                        sliderRoundness.setVisibility(View.VISIBLE);
                                        if(lblRoundness != null) lblRoundness.setVisibility(View.VISIBLE);
                                    }
                                };

                                btnLineSpace.setOnClickListener(toggleSlider);
                                btnRoundness.setOnClickListener(toggleSlider);
                                
                                // Slider Listeners
                                sliderLineSpace.setOnTouchListener(sliderTouchListener);
                                sliderLineSpace.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                    @Override
                                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                        if (fromUser && myWebView != null) {
                                            myWebView.evaluateJavascript("window.updateItemStyle('" + itemId + "', 'line_space', " + progress + ");", null);
                                        }
                                    }
                                    @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                                    @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                                });

                                sliderRoundness.setOnTouchListener(sliderTouchListener);
                                sliderRoundness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                    @Override
                                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                        if (fromUser && myWebView != null) {
                                             myWebView.evaluateJavascript("window.updateItemStyle('" + itemId + "', 'roundness', " + progress + ");", null);
                                        }
                                    }
                                    @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                                    @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                                });

                                container.addView(itemView);
                            }
                        }
                    } 
                    
                    // Setup Multi-Select Panel Controls
                    LinearLayout multiActionsContainer = view.findViewById(R.id.multiActionsContainer);
                    LinearLayout multiSlidersContainer = view.findViewById(R.id.multiSlidersContainer);
                    TextView lblMultiSlider = view.findViewById(R.id.lblMultiSlider);
                    SeekBar sliderMulti = view.findViewById(R.id.sliderMulti);
                    
                    ImageView btnMultiLineSpace = view.findViewById(R.id.btnMultiLineSpace);
                    ImageView btnMultiRoundness = view.findViewById(R.id.btnMultiRoundness);
                    View btnMultiColor = view.findViewById(R.id.btnMultiColor);

                    if (multiActionsContainer != null) {
                        View.OnClickListener toggleMultiSlider = v -> {
                            TransitionManager.beginDelayedTransition((ViewGroup) view); 
                            
                            String currentMode = (String) sliderMulti.getTag();
                            String targetMode = "";
                            int maxVal = 100;
                            
                            if (v == btnMultiLineSpace) {
                                targetMode = "line_space";
                                maxVal = 50;
                                lblMultiSlider.setText("Line Height");
                            } else if (v == btnMultiRoundness) {
                                targetMode = "roundness";
                                maxVal = 30;
                                lblMultiSlider.setText("Roundness");
                            }
                            
                            if (targetMode.equals(currentMode) && multiSlidersContainer.getVisibility() == View.VISIBLE) {
                                multiSlidersContainer.setVisibility(View.GONE);
                                sliderMulti.setTag(null);
                            } else {
                                multiSlidersContainer.setVisibility(View.VISIBLE);
                                sliderMulti.setTag(targetMode);
                                sliderMulti.setMax(maxVal);
                                sliderMulti.setProgress(10); 
                            }
                        };
                        
                        btnMultiLineSpace.setOnClickListener(toggleMultiSlider);
                        btnMultiRoundness.setOnClickListener(toggleMultiSlider);
                        
                        btnMultiColor.setOnClickListener(v -> {
                             currentEditingItemId = "MULTI_SELECT"; 
                             openNativeColorPickerForFrame("", ITEM_BG_COLOR_ID); 
                        });
                        
                        sliderMulti.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (fromUser && myWebView != null) {
                                    String mode = (String) seekBar.getTag();
                                    if (mode != null) {
                                        for (String id : selectedItemIds) {
                                            myWebView.evaluateJavascript("window.updateItemStyle('" + id + "', '" + mode + "', " + progress + ");", null);
                                        }
                                    }
                                }
                            }
                             @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                             @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                        });
                    }
                    
                    dialog.show();
                    
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing section data", e);
                }
            });
        });
    }

    interface SimpleSliderCallback {
        void onChanged(int value);
    }

    private void showSimpleSlider(View anchor, String title, int min, int max, int current, SimpleSliderCallback callback) {
         LinearLayout layout = new LinearLayout(this);
         layout.setOrientation(LinearLayout.VERTICAL);
         layout.setPadding(30, 30, 30, 30);
         layout.setBackgroundColor(Color.WHITE);
         
         TextView lbl = new TextView(this);
         lbl.setText(title);
         lbl.setTextColor(Color.BLACK);
         lbl.setTextSize(14);
         layout.addView(lbl);
         
         SeekBar sb = new SeekBar(this);
         sb.setMax(max - min);
         sb.setProgress(current - min);
         layout.addView(sb);
         
         TextView val = new TextView(this);
         val.setText(String.valueOf(current));
         val.setGravity(Gravity.CENTER);
         layout.addView(val);
         
         PopupWindow popup = new PopupWindow(layout, 600, ViewGroup.LayoutParams.WRAP_CONTENT, true);
         popup.setElevation(20);
         popup.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.WHITE)); 
         
         sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
             public void onProgressChanged(SeekBar s, int p, boolean u) {
                 int value = p + min;
                 val.setText(String.valueOf(value));
                 if(u) callback.onChanged(value);
             }
             public void onStartTrackingTouch(SeekBar s) {}
             public void onStopTrackingTouch(SeekBar s) {}
         });
         
         popup.showAsDropDown(anchor);
    }

    private void showFrameSettingsDialog(String type, int thickness, String colorStart, String colorEnd, int radius, String sides, String bgStart, String bgEnd, String textColor, int width, int height, int blur, String scale, boolean isSplit, String splitColor, int splitPos, String splitDir, int zIndex) {
        onNativePanelOpened();
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.TransparentBottomSheetDialog);
        View view = getLayoutInflater().inflate(R.layout.dialog_frame_settings, null);
        currentFrameSettingsView = view;
        dialog.setContentView(view);

        // Max Height 40%
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
            View bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
                int screenHeight = getResources().getDisplayMetrics().heightPixels;
                int maxHeight = (int) (screenHeight * 0.45);
                behavior.setMaxHeight(maxHeight);
                behavior.setPeekHeight(maxHeight);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        TabLayout tabLayout = view.findViewById(R.id.tab_layout_settings);
        View layoutStyles = view.findViewById(R.id.layout_styles);
        View layoutCustomize = view.findViewById(R.id.layout_customize);

        tabLayout.addTab(tabLayout.newTab().setText("Styles"));
        tabLayout.addTab(tabLayout.newTab().setText("Customize"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    layoutStyles.setVisibility(View.VISIBLE);
                    layoutCustomize.setVisibility(View.GONE);
                } else {
                    layoutStyles.setVisibility(View.GONE);
                    layoutCustomize.setVisibility(View.VISIBLE);
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Default to Customize tab
        tabLayout.getTabAt(1).select();
        layoutCustomize.setVisibility(View.VISIBLE);
        layoutStyles.setVisibility(View.GONE);
        if (type.equals("header")) {
            ViewPager2 pager = view.findViewById(R.id.pager_styles);
            TabLayout indicator = view.findViewById(R.id.indicator_styles);
            
            String[] styleNames = {"Minimal", "Drop Shape"};
            String[] styleIds = {"d1", "drop-shape"};
            
            StyleCarouselAdapter adapter = new StyleCarouselAdapter(styleNames, styleIds);
            pager.setAdapter(adapter);
            
            new com.google.android.material.tabs.TabLayoutMediator(indicator, pager, (tab, position) -> {}).attach();
            
            // Set initial page
            pager.setCurrentItem(currentHeaderIdx, false);
            
            pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    currentHeaderIdx = position;
                    String typeId = styleIds[position];
                    if (typeId.equals("drop-shape")) {
                        // Activate Drop Shape mode on the header box
                        if (myWebView != null) {
                            myWebView.evaluateJavascript("if(window.activateDropShapeMode) window.activateDropShapeMode();", null);
                        }
                    } else {
                        // Deactivate Drop Shape mode if switching away
                        if (myWebView != null) {
                            myWebView.evaluateJavascript("if(window.deactivateDropShapeMode) window.deactivateDropShapeMode();", null);
                            myWebView.evaluateJavascript("if(window.applyHeaderDesign) window.applyHeaderDesign('"+ typeId +"');", null);
                        }
                    }
                }
            });
        } else if (type.equals("left")) {
            ViewPager2 pager = view.findViewById(R.id.pager_styles);
            TabLayout indicator = view.findViewById(R.id.indicator_styles);
            
            String[] styleNames = {"Default", "Split"};
            String[] styleIds = {"default", "stack"};
            
            StyleCarouselAdapter adapter = new StyleCarouselAdapter(styleNames, styleIds);
            pager.setAdapter(adapter);
            
            new com.google.android.material.tabs.TabLayoutMediator(indicator, pager, (tab, position) -> {}).attach();
            
            // Set current selection
            // We need to determine the current layout mode from the JS state or variables
            // For now, let's just default or try to match 'stack' if it's active
            
            pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    String mode = styleIds[position];
                    if (myWebView != null) {
                        myWebView.evaluateJavascript("if(window.updateDesignerColumnLayout) window.updateDesignerColumnLayout('"+ mode +"');", null);
                    }
                }
            });
        }

        // Collapsible Sections
        View headerFrame = view.findViewById(R.id.header_frame);
        View containerFrame = view.findViewById(R.id.container_frame);
        ImageView imgFrameChevron = view.findViewById(R.id.img_frame_chevron);
        
        View headerMain = view.findViewById(R.id.header_main);
        View containerMain = view.findViewById(R.id.container_main);
        ImageView imgMainChevron = view.findViewById(R.id.img_main_chevron);
        
        View headerText = view.findViewById(R.id.header_text);
        View containerText = view.findViewById(R.id.container_text);
        ImageView imgTextChevron = view.findViewById(R.id.img_text_chevron);

        View headerImage = view.findViewById(R.id.header_image);
        View containerImage = view.findViewById(R.id.container_image);
        ImageView imgImageChevron = view.findViewById(R.id.img_image_chevron);

        // Create a list of all sections to manage accordion behavior
        List<View[]> allSectionsHeaders = new ArrayList<>();
        allSectionsHeaders.add(new View[]{headerFrame, containerFrame, imgFrameChevron});
        allSectionsHeaders.add(new View[]{headerMain, containerMain, imgMainChevron});
        allSectionsHeaders.add(new View[]{headerText, containerText, imgTextChevron});
        allSectionsHeaders.add(new View[]{headerImage, containerImage, imgImageChevron});

        setupFrameAccordion(headerFrame, containerFrame, imgFrameChevron, allSectionsHeaders);
        setupFrameAccordion(headerMain, containerMain, imgMainChevron, allSectionsHeaders);
        setupFrameAccordion(headerText, containerText, imgTextChevron, allSectionsHeaders);
        setupFrameAccordion(headerImage, containerImage, imgImageChevron, allSectionsHeaders);

        // Collapse all by default
        containerFrame.setVisibility(View.GONE);
        imgFrameChevron.setRotation(0);
        containerMain.setVisibility(View.GONE);
        imgMainChevron.setRotation(0);
        containerText.setVisibility(View.GONE);
        imgTextChevron.setRotation(0);
        containerImage.setVisibility(View.GONE);
        imgImageChevron.setRotation(0);

        if (type.equals("header")) {
            headerText.setVisibility(View.VISIBLE);
            headerImage.setVisibility(View.VISIBLE);
            // Show Header Background Image button
            View layoutHeaderBgImage = view.findViewById(R.id.layout_header_bg_image);
            if (layoutHeaderBgImage != null) {
                layoutHeaderBgImage.setVisibility(View.VISIBLE);
                View btnHeaderBgImage = view.findViewById(R.id.btn_header_bg_image);
                if (btnHeaderBgImage != null) {
                    btnHeaderBgImage.setOnClickListener(v -> pickHeaderImage());
                }
                View btnRemoveHeaderBg = view.findViewById(R.id.btn_remove_header_bg_image);
                if (btnRemoveHeaderBg != null) {
                    btnRemoveHeaderBg.setOnClickListener(v -> {
                        if (myWebView != null) {
                            myWebView.evaluateJavascript("if(window.updateHeaderBgImage) window.updateHeaderBgImage('', false);", null);
                        }
                    });
                }
                // Update Resolution Text
                if (layoutHeaderBgImage instanceof LinearLayout) {
                    TextView txtRes = (TextView) ((LinearLayout) layoutHeaderBgImage).getChildAt(1); // Assuming 2nd child is TextView
                    if (txtRes != null) {
                        txtRes.setText(getResString(width, height));
                    }
                }
            }
        } else if (type.equals("left")) {
             headerText.setVisibility(View.GONE);
             headerImage.setVisibility(View.VISIBLE);
             // Show Left Background Image button
             View layoutLeftBgImage = view.findViewById(R.id.layout_left_bg_image);
             if (layoutLeftBgImage != null) {
                 layoutLeftBgImage.setVisibility(View.VISIBLE);
                 View btnLeftBgImage = view.findViewById(R.id.btn_left_bg_image);
                 if (btnLeftBgImage != null) {
                     btnLeftBgImage.setOnClickListener(v -> pickLeftImage());
                 }
                 View btnRemoveLeftBg = view.findViewById(R.id.btn_remove_left_bg_image);
                 if (btnRemoveLeftBg != null) {
                     btnRemoveLeftBg.setOnClickListener(v -> {
                         if (myWebView != null) {
                             myWebView.evaluateJavascript("if(window.updateLeftBgImage) window.updateLeftBgImage('');", null);
                         }
                     });
                 }
                 // Update Resolution Text
                 if (layoutLeftBgImage instanceof LinearLayout) {
                     TextView txtRes = (TextView) ((LinearLayout) layoutLeftBgImage).getChildAt(1); // Assuming 2nd child is TextView
                     if (txtRes != null) {
                         txtRes.setText(getResString(width, height));
                     }
                 }
             }
        } else {
            headerText.setVisibility(View.GONE);
            headerImage.setVisibility(View.GONE);
        }

        // Swap Button (Only for Left)
        if (type.equals("left")) {
            View btnSwap = view.findViewById(R.id.btn_swap_columns);
            if (btnSwap != null) {
                btnSwap.setVisibility(View.VISIBLE);
                btnSwap.setOnClickListener(v -> {
                    if (myWebView != null) {
                         myWebView.evaluateJavascript("if(window.updateDesignerColumnLayout) window.updateDesignerColumnLayout();", null);
                    }
                });
            }
        }

        // Initial rotation for text if it's closed (it is GONE in XML)
        if (containerText.getVisibility() == View.GONE) {
            imgTextChevron.setRotation(0);
        } else {
            imgTextChevron.setRotation(180);
        }
        // Frame and Main are visible by default, so set them to 180
        imgFrameChevron.setRotation(180);
        imgMainChevron.setRotation(180);

        TextView txtTitle = view.findViewById(R.id.txt_dialog_title);
        SeekBar sliderThickness = view.findViewById(R.id.slider_thickness);
        SeekBar sliderRadius = view.findViewById(R.id.slider_radius);
        TextView txtThickness = view.findViewById(R.id.txt_thickness_val);
        TextView txtRadius = view.findViewById(R.id.txt_radius_val);

        // Frame Gradient Indicators
        View frameStartIndicator = view.findViewById(R.id.view_frame_color_start_indicator);
        View frameEndIndicator = view.findViewById(R.id.view_frame_color_end_indicator);
        Button btnFrameStart = view.findViewById(R.id.btn_change_frame_color_start);
        Button btnFrameEnd = view.findViewById(R.id.btn_change_frame_color_end);

        // Fill Gradient Indicators
        View fillStartIndicator = view.findViewById(R.id.view_column_bg_color_start_indicator);
        View fillEndIndicator = view.findViewById(R.id.view_column_bg_color_end_indicator);
        Button btnFillStart = view.findViewById(R.id.btn_change_column_bg_color_start);
        Button btnFillEnd = view.findViewById(R.id.btn_change_column_bg_color_end);

        CheckBox checkTop = view.findViewById(R.id.check_top);
        CheckBox checkBottom = view.findViewById(R.id.check_bottom);
        CheckBox checkLeft = view.findViewById(R.id.check_left);
        CheckBox checkRight = view.findViewById(R.id.check_right);

        // Header Text Color
        View layoutTextColor = view.findViewById(R.id.layout_text_color_container);
        View textColorIndicator = view.findViewById(R.id.view_text_color_indicator);
        Button btnTextColor = view.findViewById(R.id.btn_change_text_color);

        SeekBar sliderHeaderBlur = view.findViewById(R.id.slider_header_blur);
        SeekBar sliderLeftBlur = view.findViewById(R.id.slider_left_blur);

        RadioGroup rgHeaderScale = view.findViewById(R.id.rg_header_scale);
        RadioGroup rgLeftScale = view.findViewById(R.id.rg_left_scale);

        String title = type.substring(0, 1).toUpperCase() + type.substring(1) + " Settings";
        if (type.equals("left")) title = "Designer Column Settings";
        txtTitle.setText(title);

        sliderThickness.setProgress(thickness);
        sliderRadius.setProgress(radius);
        txtThickness.setText(String.valueOf(thickness));
        txtRadius.setText(String.valueOf(radius));

        if (sides != null && sides.length() == 4) {
            checkTop.setChecked(sides.charAt(0) == '1');
            checkBottom.setChecked(sides.charAt(1) == '1');
            checkLeft.setChecked(sides.charAt(2) == '1');
            checkRight.setChecked(sides.charAt(3) == '1');
        }

        final String[] fStart = {colorStart != null ? colorStart : "#ffffff"};
        final String[] fEnd = {colorEnd != null ? colorEnd : "#ffffff"};
        final String[] bStart = {bgStart != null ? bgStart : "#ffffff"};
        final String[] bEnd = {bgEnd != null ? bgEnd : "#ffffff"};
        final String[] tColor = {textColor != null ? textColor : "#333333"};

        if (type.equals("header")) {
            layoutTextColor.setVisibility(View.VISIBLE);
            if (sliderHeaderBlur != null) sliderHeaderBlur.setProgress(blur);
            if (rgHeaderScale != null && scale != null) {
                if (scale.equals("cover")) rgHeaderScale.check(R.id.rb_header_fill);
                else if (scale.equals("contain")) rgHeaderScale.check(R.id.rb_header_fit);
                else rgHeaderScale.check(R.id.rb_header_center);
            }
        } else if (type.equals("left")) {
            if (sliderLeftBlur != null) sliderLeftBlur.setProgress(blur);
            if (rgLeftScale != null && scale != null) {
                if (scale.equals("cover")) rgLeftScale.check(R.id.rb_left_fill);
                else if (scale.equals("contain")) rgLeftScale.check(R.id.rb_left_fit);
                else rgLeftScale.check(R.id.rb_left_center);
            }
        }

        autoSetViewColor(frameStartIndicator, fStart[0]);
        autoSetViewColor(frameEndIndicator, fEnd[0]);
        autoSetViewColor(fillStartIndicator, bStart[0]);
        autoSetViewColor(fillEndIndicator, bEnd[0]);
        autoSetViewColor(textColorIndicator, tColor[0]);

        if (type.equals("header")) {
            layoutTextColor.setVisibility(View.VISIBLE);
        }

        Runnable triggerUpdate = () -> {
            int t = sliderThickness.getProgress();
            int r = sliderRadius.getProgress();
            txtThickness.setText(String.valueOf(t));
            txtRadius.setText(String.valueOf(r));

            String s = (checkTop.isChecked() ? "1" : "0") +
                       (checkBottom.isChecked() ? "1" : "0") +
                       (checkLeft.isChecked() ? "1" : "0") +
                       (checkRight.isChecked() ? "1" : "0");

            if (myWebView != null) {
                int bVal = 0;
                String sVal = "cover";

                if (type.equals("left")) {
                     if (sliderLeftBlur != null) bVal = sliderLeftBlur.getProgress();
                     if (rgLeftScale != null) {
                         int id = rgLeftScale.getCheckedRadioButtonId();
                         if (id == R.id.rb_left_fit) sVal = "contain";
                         else if (id == R.id.rb_left_center) sVal = "auto";
                     }
                } else if (type.equals("header")) {
                    if (sliderHeaderBlur != null) bVal = sliderHeaderBlur.getProgress();
                     if (rgHeaderScale != null) {
                         int id = rgHeaderScale.getCheckedRadioButtonId();
                         if (id == R.id.rb_header_fit) sVal = "contain";
                         else if (id == R.id.rb_header_center) sVal = "auto";
                     }
                }

                String jsFunc = type.equals("left") ? "window.updateLeftFrameConfig" : "window.updateHeaderFrameConfig";
                String js = String.format("if(%s) %s({thickness:%d, radius:%d, colorStart:'%s', colorEnd:'%s', sides:'%s', bgStart:'%s', bgEnd:'%s', textColor:'%s', blur:%d, scale:'%s'});",
                    jsFunc, jsFunc, t, r, fStart[0], fEnd[0], s, bStart[0], bEnd[0], tColor[0], bVal, sVal);
                myWebView.evaluateJavascript(js, null);
            }
        };

        SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { if (fromUser) triggerUpdate.run(); }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        };
        sliderThickness.setOnSeekBarChangeListener(listener);
        sliderRadius.setOnSeekBarChangeListener(listener);
        if (sliderHeaderBlur != null) sliderHeaderBlur.setOnSeekBarChangeListener(listener);
        if (sliderLeftBlur != null) sliderLeftBlur.setOnSeekBarChangeListener(listener);

        // Header Height Logic
        View layoutHeaderHeight = view.findViewById(R.id.layout_header_height_settings);
        if (type.equals("left") && layoutHeaderHeight != null) {
            layoutHeaderHeight.setVisibility(View.GONE);
        }

        SeekBar sliderHeaderHeight = view.findViewById(R.id.sliderHeaderHeight);
        TextView txtHeaderHeightVal = view.findViewById(R.id.txtHeaderHeightVal);
        TextView btnHeaderHeightAuto = view.findViewById(R.id.btnHeaderHeightAuto);
        
        if (sliderHeaderHeight != null && txtHeaderHeightVal != null && btnHeaderHeightAuto != null) {
            // Default to 0? Or maybe we can't easily read current. Let's start at 0 (meaning small) or just leave it.
            // Better: if "Auto", set progress to 0?
            // Let's assume user starts interacting.

            sliderHeaderHeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                         if (progress < 20) progress = 20; // Min 20px
                         txtHeaderHeightVal.setText(progress + "px");
                         if (myWebView != null) {
                             myWebView.evaluateJavascript("if(window.updateHeaderHeight) window.updateHeaderHeight(" + progress + ");", null);
                         }
                    }
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });

            btnHeaderHeightAuto.setOnClickListener(v -> {
                if (myWebView != null) {
                    myWebView.evaluateJavascript("if(window.updateHeaderHeight) window.updateHeaderHeight(-1);", null);
                }
                txtHeaderHeightVal.setText("Auto");
                sliderHeaderHeight.setProgress(0);
            });
        }
        
        RadioGroup.OnCheckedChangeListener rgListener = (group, checkedId) -> triggerUpdate.run();
        if (rgHeaderScale != null) rgHeaderScale.setOnCheckedChangeListener(rgListener);
        if (rgLeftScale != null) rgLeftScale.setOnCheckedChangeListener(rgListener);

        // Z-Index Logic
        View layoutZIndex = view.findViewById(R.id.layout_z_index_settings);
        RadioGroup rgZIndex = view.findViewById(R.id.rg_z_index);
        
        // Log to both logcat and debug panel
        String initInfo = "layoutZIndex=" + (layoutZIndex != null ? "FOUND" : "NULL") + " rgZIndex=" + (rgZIndex != null ? "FOUND" : "NULL") + " type=" + type + " zIndex=" + zIndex;
        android.util.Log.d("ZINDEX_DEBUG", initInfo);
        if (myWebView != null) {
            myWebView.post(() -> myWebView.evaluateJavascript("if(window._zDbg) window._zDbg('JAVA_INIT', '" + initInfo + "');", null));
        }
        
        if (type.equals("header") && layoutZIndex != null && rgZIndex != null) {
            layoutZIndex.setVisibility(View.VISIBLE);
            
            String checkMsg = "Setting initial check for zIndex=" + zIndex;
            android.util.Log.d("ZINDEX_DEBUG", checkMsg);
            if (myWebView != null) {
                myWebView.post(() -> myWebView.evaluateJavascript("if(window._zDbg) window._zDbg('JAVA_CHECK', '" + checkMsg + "');", null));
            }
            
            if (zIndex == -1) rgZIndex.check(R.id.rb_z_below);
            else if (zIndex == 1) rgZIndex.check(R.id.rb_z_over);
            else rgZIndex.check(R.id.rb_z_collide);
            
            rgZIndex.setOnCheckedChangeListener((group, checkedId) -> {
                int zVal = 0;
                if (checkedId == R.id.rb_z_below) zVal = -1;
                else if (checkedId == R.id.rb_z_over) zVal = 1;
                
                String changeMsg = "RadioGroup changed! checkedId=" + checkedId + " zVal=" + zVal;
                android.util.Log.d("ZINDEX_DEBUG", changeMsg);
                
                if (myWebView != null) {
                    final int fzVal = zVal;
                    myWebView.post(() -> {
                        String js = "try { " +
                                    "  if(window._zDbg) window._zDbg('JAVA_RADIO', 'zVal=" + fzVal + "'); " +
                                    "  if(window.updateHeaderFrameConfig) window.updateHeaderFrameConfig({zIndex: " + fzVal + "}); " +
                                    "} catch(e) { " +
                                    "  if(window._zDbg) window._zDbg('JAVA_RADIO_ERROR', e.message); " +
                                    "}";
                        myWebView.evaluateJavascript(js, null);
                    });
                } else {
                    android.util.Log.d("ZINDEX_DEBUG", "myWebView is NULL!");
                }
            });
        }


        CompoundButton.OnCheckedChangeListener checkListener = (buttonView, isChecked) -> triggerUpdate.run();
        checkTop.setOnCheckedChangeListener(checkListener);
        checkBottom.setOnCheckedChangeListener(checkListener);
        checkLeft.setOnCheckedChangeListener(checkListener);
        checkRight.setOnCheckedChangeListener(checkListener);

        btnFrameStart.setOnClickListener(v -> openNativeColorPickerForFrame(fStart[0], type.equals("left") ? LEFT_FRAME_COLOR_ID : HEADER_FRAME_COLOR_ID));
        btnFrameEnd.setOnClickListener(v -> openNativeColorPickerForFrame(fEnd[0], type.equals("left") ? LEFT_FRAME_COLOR_END_ID : HEADER_FRAME_COLOR_END_ID));
        btnFillStart.setOnClickListener(v -> openNativeColorPickerForFrame(bStart[0], type.equals("left") ? LEFT_FRAME_BG_COLOR_ID : HEADER_FRAME_BG_COLOR_ID));
        btnFillEnd.setOnClickListener(v -> openNativeColorPickerForFrame(bEnd[0], type.equals("left") ? LEFT_FRAME_BG_COLOR_END_ID : HEADER_FRAME_BG_COLOR_END_ID));
        btnTextColor.setOnClickListener(v -> openNativeColorPickerForFrame(tColor[0], HEADER_TEXT_COLOR_ID));

        // Split background controls
        if (type.equals("left")) {
            View layoutSplitSettings = view.findViewById(R.id.layout_split_settings);
            CheckBox checkSplitMode = view.findViewById(R.id.check_split_mode);
            View containerSplitControls = view.findViewById(R.id.container_split_controls);
            View viewSplitTopIndicator = view.findViewById(R.id.view_split_top_color_indicator);
            Button btnChangeSplitTopColor = view.findViewById(R.id.btn_change_split_top_color);
            View viewSplitIndicator = view.findViewById(R.id.view_split_color_indicator);
            Button btnChangeSplitColor = view.findViewById(R.id.btn_change_split_color);
            SeekBar sliderSplitPos = view.findViewById(R.id.slider_split_pos);
            TextView txtSplitPos = view.findViewById(R.id.txt_split_pos_val);
            RadioGroup rgSplitDir = view.findViewById(R.id.rg_split_direction);

            if (layoutSplitSettings != null) {
                layoutSplitSettings.setVisibility(View.VISIBLE);
                checkSplitMode.setChecked(isSplit);
                containerSplitControls.setVisibility(isSplit ? View.VISIBLE : View.GONE);
                
                final String[] sColor = {splitColor != null ? splitColor : "#ffffff"};
                autoSetViewColor(viewSplitIndicator, sColor[0]);
                
                sliderSplitPos.setProgress(splitPos);
                txtSplitPos.setText(String.valueOf(splitPos));

                if ("horizontal".equals(splitDir)) {
                    rgSplitDir.check(R.id.rb_split_horizontal);
                } else {
                    rgSplitDir.check(R.id.rb_split_vertical);
                }

                checkSplitMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    containerSplitControls.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                    if (myWebView != null) {
                        myWebView.evaluateJavascript("if(window.updateLeftFrameConfig) window.updateLeftFrameConfig({isSplit:" + isChecked + "});", null);
                    }
                });

                if (bgStart != null) viewSplitTopIndicator.setBackgroundColor(Color.parseColor(bgStart));
                if (splitColor != null) viewSplitIndicator.setBackgroundColor(Color.parseColor(splitColor));

                btnChangeSplitTopColor.setOnClickListener(v -> openNativeColorPickerForFrame(bgStart != null ? bgStart : "#f7f9fc", LEFT_SPLIT_TOP_COLOR_ID));
                btnChangeSplitColor.setOnClickListener(v -> openNativeColorPickerForFrame(splitColor != null ? splitColor : "#ffffff", LEFT_SPLIT_COLOR_ID));

                sliderSplitPos.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        txtSplitPos.setText(String.valueOf(progress));
                        if (fromUser && myWebView != null) {
                            myWebView.evaluateJavascript("if(window.updateLeftFrameConfig) window.updateLeftFrameConfig({splitPos:" + progress + "});", null);
                        }
                    }
                    @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                    @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                });

                rgSplitDir.setOnCheckedChangeListener((group, checkedId) -> {
                    String dir = (checkedId == R.id.rb_split_horizontal) ? "horizontal" : "vertical";
                    if (myWebView != null) {
                        myWebView.evaluateJavascript("if(window.updateLeftFrameConfig) window.updateLeftFrameConfig({splitDir:'" + dir + "'});", null);
                    }
                });
            }
        }

        dialog.show();
    }

    private void autoSetViewColor(View v, String c) {
        try { v.setBackgroundColor(Color.parseColor(c)); } catch (Exception e) { v.setBackgroundColor(Color.WHITE); }
    }

    private void openNativeColorPickerForFrame(String currentColor, int dialogId) {
        onNativePanelOpened();
        int color = Color.WHITE;
        try {
            if (currentColor != null && !currentColor.isEmpty()) {
                color = Color.parseColor(currentColor);
            }
        } catch (IllegalArgumentException e) {
            color = Color.WHITE;
        }

        ColorPickerDialog.newBuilder()
                .setDialogId(dialogId)
                .setColor(color)
                .setShowAlphaSlider(true)
                .show(this);
    }

    private void updateContactSetting(String sectionId, String key, Object value) {
        if (myWebView != null) {
            String valStr = (value instanceof String) ? "'" + value + "'" : value.toString();
            myWebView.evaluateJavascript("if(window.applyContactSetting) window.applyContactSetting('" + sectionId + "', '" + key + "', " + valStr + ");", null);
        }
    }

    private String getResString(int width, int height) {
        if (width <= 0 || height <= 0) return "Resolution: N/A";
        int gcd = getGCD(width, height);
        return String.format("Format: JPG/PNG • Ratio: %d:%d • Res: %dx%dpx", width/gcd, height/gcd, width, height);
    }

    private void setupFrameAccordion(View header, View content, ImageView chevron, List<View[]> allSections) {
        header.setOnClickListener(v -> {
            boolean expanded = content.getVisibility() == View.VISIBLE;
            TransitionManager.beginDelayedTransition((ViewGroup) content.getParent(), new AutoTransition());
            
            if (expanded) {
                content.setVisibility(View.GONE);
                chevron.animate().rotation(0).setDuration(200).start();
            } else {
                // Close others first
                for (View[] section : allSections) {
                    View c = section[1];
                    ImageView i = (ImageView) section[2]; 
                    if (c != content && c.getVisibility() == View.VISIBLE) {
                        c.setVisibility(View.GONE);
                        i.animate().rotation(0).setDuration(200).start();
                    }
                }
                // Open this one
                content.setVisibility(View.VISIBLE);
                chevron.animate().rotation(180).setDuration(200).start();
            }
        });
    }

    private int getGCD(int a, int b) {
        return b == 0 ? a : getGCD(b, a % b);
    }


    private class StyleCarouselAdapter extends RecyclerView.Adapter<StyleCarouselAdapter.ViewHolder> {
        private String[] names;
        private String[] ids;
        private int[] colors = {0xFFEEEEEE, 0xFFE0E0E0, 0xFFF5F5F5}; // Light placeholders

        public StyleCarouselAdapter(String[] names, String[] ids) {
            this.names = names;
            this.ids = ids;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_style_carousel, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.txtName.setText(names[position]);
            // Use placeholder color for thumbnails
            holder.imgThumbnail.setBackgroundColor(colors[position % colors.length]);
            
            // If assets existed, we would use holder.imgThumbnail.setImageResource(...)
            // For now, let's add a subtle icon or text inside the placeholder to make it look "wow"
            if (names[position].equals("Minimal") || names[position].equals("Default")) {
                holder.imgThumbnail.setImageResource(android.R.drawable.ic_menu_edit);
            } else if (names[position].equals("Bold") || names[position].equals("Mirror")) {
                holder.imgThumbnail.setImageResource(android.R.drawable.ic_menu_add);
            } else if (names[position].equals("Custom Style") || names[position].equals("Split")) {
                holder.imgThumbnail.setImageResource(R.drawable.ic_star); // Use a star or similar icon
            } else { // Glass or Hidden Line
                holder.imgThumbnail.setImageResource(android.R.drawable.ic_menu_view);
            }
            holder.imgThumbnail.setImageAlpha(128); // Semi-transparent icon

            // Show delete button only for user-created custom styles
            if (ids[position].startsWith("CustomStyle_")) {
                holder.btnDelete.setVisibility(View.VISIBLE);
                holder.btnDelete.setOnClickListener(v -> {
                    new android.app.AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete Style")
                        .setMessage("Remove \"" + names[position] + "\"?")
                        .setPositiveButton("Delete", (d, w) -> {
                            deleteCustomStyle(ids[position]);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                });
            } else {
                holder.btnDelete.setVisibility(View.GONE);
            }

            // Show settings button for "Custom Style" or "Split"
            if (ids[position].equals("Custom Style") || ids[position].equals("stack")) {
                holder.btnSettings.setVisibility(View.VISIBLE);
                holder.btnSettings.setOnClickListener(v -> {
                    if (ids[position].equals("stack")) {
                        openLayoutSettings("stack");
                    } else {
                        Toast.makeText(MainActivity.this, "Settings for " + names[position] + " coming soon!", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                holder.btnSettings.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return names.length;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imgThumbnail;
            TextView txtName;
            ImageView btnDelete;
            ImageView btnSettings;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                imgThumbnail = itemView.findViewById(R.id.img_style_thumbnail);
                txtName = itemView.findViewById(R.id.txt_style_name);
                btnDelete = itemView.findViewById(R.id.btn_delete_style);
                btnSettings = itemView.findViewById(R.id.btn_style_settings);
            }
        }
    }

    private void openLayoutSettings(String mode) {
        if (!mode.equals("stack")) return;

        // Build a small dedicated dialog for Split style settings
        BottomSheetDialog splitDialog = new BottomSheetDialog(this, R.style.TransparentBottomSheetDialog);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(64, 48, 64, 48);
        root.setBackgroundColor(Color.parseColor("#F5F5F5"));

        // Title
        TextView title = new TextView(this);
        title.setText("Split Style Settings");
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setTextColor(Color.parseColor("#424242"));
        title.setPadding(0, 0, 0, 32);
        root.addView(title);

        // --- Main content: colors on left, slider on right ---
        LinearLayout mainRow = new LinearLayout(this);
        mainRow.setOrientation(LinearLayout.HORIZONTAL);
        mainRow.setGravity(android.view.Gravity.TOP);

        // == Left side: Color pickers ==
        LinearLayout colColors = new LinearLayout(this);
        colColors.setOrientation(LinearLayout.VERTICAL);
        colColors.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        // Top Color
        TextView labelTop = new TextView(this);
        labelTop.setText("Top Color");
        labelTop.setTextSize(14);
        labelTop.setTextColor(Color.parseColor("#757575"));
        labelTop.setPadding(0, 0, 0, 8);
        colColors.addView(labelTop);

        LinearLayout rowTop = new LinearLayout(this);
        rowTop.setOrientation(LinearLayout.HORIZONTAL);
        rowTop.setGravity(android.view.Gravity.CENTER_VERTICAL);
        rowTop.setPadding(0, 0, 0, 24);

        View indicatorTop = new View(this);
        indicatorTop.setLayoutParams(new LinearLayout.LayoutParams(56, 56));
        indicatorTop.setBackgroundColor(Color.parseColor("#f7f9fc"));
        if (myWebView != null) {
            myWebView.evaluateJavascript(
                "getComputedStyle(document.documentElement).getPropertyValue('--left-col-bg').trim()",
                value -> {
                    String c = value != null ? value.replace("\"", "").trim() : "";
                    if (!c.isEmpty() && c.startsWith("#")) {
                        runOnUiThread(() -> { try { indicatorTop.setBackgroundColor(Color.parseColor(c)); } catch(Exception e){} });
                    }
                });
        }
        rowTop.addView(indicatorTop);

        View spacer1 = new View(this);
        spacer1.setLayoutParams(new LinearLayout.LayoutParams(16, 1));
        rowTop.addView(spacer1);

        Button btnTop = new Button(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        btnTop.setText("Pick");
        btnTop.setTextSize(11);
        btnTop.setOnClickListener(v -> openNativeColorPickerForFrame("#f7f9fc", LEFT_SPLIT_TOP_COLOR_ID));
        rowTop.addView(btnTop);
        colColors.addView(rowTop);

        // Bottom Color
        TextView labelBottom = new TextView(this);
        labelBottom.setText("Bottom Color");
        labelBottom.setTextSize(14);
        labelBottom.setTextColor(Color.parseColor("#757575"));
        labelBottom.setPadding(0, 0, 0, 8);
        colColors.addView(labelBottom);

        LinearLayout rowBottom = new LinearLayout(this);
        rowBottom.setOrientation(LinearLayout.HORIZONTAL);
        rowBottom.setGravity(android.view.Gravity.CENTER_VERTICAL);

        View indicatorBottom = new View(this);
        indicatorBottom.setLayoutParams(new LinearLayout.LayoutParams(56, 56));
        indicatorBottom.setBackgroundColor(Color.parseColor("#ffffff"));
        if (myWebView != null) {
            myWebView.evaluateJavascript(
                "getComputedStyle(document.documentElement).getPropertyValue('--left-split-color').trim()",
                value -> {
                    String c = value != null ? value.replace("\"", "").trim() : "";
                    if (!c.isEmpty() && c.startsWith("#")) {
                        runOnUiThread(() -> { try { indicatorBottom.setBackgroundColor(Color.parseColor(c)); } catch(Exception e){} });
                    }
                });
        }
        rowBottom.addView(indicatorBottom);

        View spacer2 = new View(this);
        spacer2.setLayoutParams(new LinearLayout.LayoutParams(16, 1));
        rowBottom.addView(spacer2);

        Button btnBottom = new Button(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        btnBottom.setText("Pick");
        btnBottom.setTextSize(11);
        btnBottom.setOnClickListener(v -> openNativeColorPickerForFrame("#ffffff", LEFT_SPLIT_COLOR_ID));
        rowBottom.addView(btnBottom);
        colColors.addView(rowBottom);

        mainRow.addView(colColors);

        // == Right side: Vertical slider ==
        LinearLayout colSlider = new LinearLayout(this);
        colSlider.setOrientation(LinearLayout.VERTICAL);
        colSlider.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
        colSlider.setPadding(24, 0, 0, 0);

        TextView txtTop = new TextView(this);
        txtTop.setText("\u2191");
        txtTop.setTextSize(14);
        txtTop.setTextColor(Color.parseColor("#9E9E9E"));
        txtTop.setGravity(android.view.Gravity.CENTER);
        colSlider.addView(txtTop);

        SeekBar sliderPos = new SeekBar(this);
        int sliderSz = (int) (150 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams sliderParams = new LinearLayout.LayoutParams(sliderSz, sliderSz);
        sliderPos.setLayoutParams(sliderParams);
        sliderPos.setMax(100);
        sliderPos.setProgress(50);
        sliderPos.setRotation(-90f);
        colSlider.addView(sliderPos);

        TextView txtBottom2 = new TextView(this);
        txtBottom2.setText("\u2193");
        txtBottom2.setTextSize(14);
        txtBottom2.setTextColor(Color.parseColor("#9E9E9E"));
        txtBottom2.setGravity(android.view.Gravity.CENTER);
        colSlider.addView(txtBottom2);

        TextView txtPosVal = new TextView(this);
        txtPosVal.setText("50%");
        txtPosVal.setTextSize(14);
        txtPosVal.setTextColor(Color.parseColor("#424242"));
        txtPosVal.setTypeface(null, android.graphics.Typeface.BOLD);
        txtPosVal.setGravity(android.view.Gravity.CENTER);
        txtPosVal.setPadding(0, 8, 0, 0);
        colSlider.addView(txtPosVal);

        mainRow.addView(colSlider);
        root.addView(mainRow);

        // --- Shape Selectors ---
        String[] shapeTypes = {"none", "rect", "triangle", "semicircle", "hexagon", "arrow"};
        String[] shapeLabels = {"None", "Rect", "Tri", "Circ", "Hex", "Arr"};

        // Top Shape
        TextView labelTopShape = new TextView(this);
        labelTopShape.setText("Top Shape");
        labelTopShape.setTextSize(13);
        labelTopShape.setTextColor(Color.parseColor("#757575"));
        labelTopShape.setPadding(0, 16, 0, 8);
        root.addView(labelTopShape);

        HorizontalScrollView scrollTop = new HorizontalScrollView(this);
        LinearLayout rowTopShape = new LinearLayout(this);
        rowTopShape.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < shapeTypes.length; i++) {
            final String type = shapeTypes[i];
            Button btn = new Button(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            btn.setText(shapeLabels[i]);
            btn.setTextSize(10);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, (int)(40 * getResources().getDisplayMetrics().density));
            lp.setMargins(0, 0, 8, 0);
            btn.setLayoutParams(lp);
            btn.setPadding(4, 0, 4, 0);
            btn.setOnClickListener(v -> {
                if (myWebView != null) myWebView.evaluateJavascript("window.updateSplitShape('top', '" + type + "');", null);
            });
            rowTopShape.addView(btn);
        }
        scrollTop.addView(rowTopShape);
        root.addView(scrollTop);

        // Bottom Shape
        TextView labelBottomShape = new TextView(this);
        labelBottomShape.setText("Bottom Shape");
        labelBottomShape.setTextSize(13);
        labelBottomShape.setTextColor(Color.parseColor("#757575"));
        labelBottomShape.setPadding(0, 16, 0, 8);
        root.addView(labelBottomShape);

        HorizontalScrollView scrollBottom = new HorizontalScrollView(this);
        LinearLayout rowBottomShape = new LinearLayout(this);
        rowBottomShape.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < shapeTypes.length; i++) {
            final String type = shapeTypes[i];
            Button btn = new Button(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            btn.setText(shapeLabels[i]);
            btn.setTextSize(10);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, (int)(40 * getResources().getDisplayMetrics().density));
            lp.setMargins(0, 0, 8, 0);
            btn.setLayoutParams(lp);
            btn.setPadding(4, 0, 4, 0);
            btn.setOnClickListener(v -> {
                if (myWebView != null) myWebView.evaluateJavascript("window.updateSplitShape('bottom', '" + type + "');", null);
            });
            rowBottomShape.addView(btn);
        }
        scrollBottom.addView(rowBottomShape);
        root.addView(scrollBottom);

        // --- Shape Dimensions ---
        CheckBox checkLink = new CheckBox(this);
        checkLink.setText("Link width & height");
        checkLink.setTextSize(13);
        checkLink.setTextColor(Color.parseColor("#757575"));
        checkLink.setPadding(0, 16, 0, 8);
        root.addView(checkLink);

        // Width Slider
        TextView labelWidth = new TextView(this);
        labelWidth.setText("Shape Width");
        labelWidth.setTextSize(13);
        labelWidth.setTextColor(Color.parseColor("#757575"));
        labelWidth.setPadding(0, 8, 0, 8);
        root.addView(labelWidth);

        LinearLayout rowWidth = new LinearLayout(this);
        rowWidth.setOrientation(LinearLayout.HORIZONTAL);
        rowWidth.setGravity(android.view.Gravity.CENTER_VERTICAL);

        SeekBar sliderWidth = new SeekBar(this);
        sliderWidth.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        sliderWidth.setMax(300);
        sliderWidth.setMin(10);
        sliderWidth.setProgress(60);
        rowWidth.addView(sliderWidth);

        TextView txtWidthVal = new TextView(this);
        txtWidthVal.setText("60px");
        txtWidthVal.setTextSize(13);
        txtWidthVal.setTextColor(Color.parseColor("#424242"));
        txtWidthVal.setPadding(16, 0, 0, 0);
        rowWidth.addView(txtWidthVal);
        root.addView(rowWidth);

        // Height Slider
        TextView labelHeight2 = new TextView(this);
        labelHeight2.setText("Shape Height");
        labelHeight2.setTextSize(13);
        labelHeight2.setTextColor(Color.parseColor("#757575"));
        labelHeight2.setPadding(0, 16, 0, 8);
        root.addView(labelHeight2);

        LinearLayout rowHeight = new LinearLayout(this);
        rowHeight.setOrientation(LinearLayout.HORIZONTAL);
        rowHeight.setGravity(android.view.Gravity.CENTER_VERTICAL);

        SeekBar sliderHeight = new SeekBar(this);
        sliderHeight.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        sliderHeight.setMax(300);
        sliderHeight.setMin(10);
        sliderHeight.setProgress(60);
        rowHeight.addView(sliderHeight);

        TextView txtHeightVal = new TextView(this);
        txtHeightVal.setText("60px");
        txtHeightVal.setTextSize(13);
        txtHeightVal.setTextColor(Color.parseColor("#424242"));
        txtHeightVal.setPadding(16, 0, 0, 0);
        rowHeight.addView(txtHeightVal);
        root.addView(rowHeight);

        // --- WebView Interaction ---
        if (myWebView != null) {
            myWebView.evaluateJavascript(
                "getComputedStyle(document.documentElement).getPropertyValue('--left-split-pos').trim()",
                value -> {
                    String v = value != null ? value.replace("\"", "").replace("%", "").trim() : "50";
                    try {
                        int pos = Integer.parseInt(v);
                        runOnUiThread(() -> {
                            sliderPos.setProgress(pos);
                            txtPosVal.setText(pos + "%");
                        });
                    } catch (Exception e) {}
                });

            myWebView.evaluateJavascript(
                "getComputedStyle(document.documentElement).getPropertyValue('--split-shape-width').trim()",
                value -> {
                    String v = value != null ? value.replace("\"", "").replace("px", "").trim() : "60";
                    try {
                        int sz = Integer.parseInt(v);
                        runOnUiThread(() -> {
                            sliderWidth.setProgress(sz);
                            txtWidthVal.setText(sz + "px");
                        });
                    } catch (Exception e) {}
                });

            myWebView.evaluateJavascript(
                "getComputedStyle(document.documentElement).getPropertyValue('--split-shape-height').trim()",
                value -> {
                    String v = value != null ? value.replace("\"", "").replace("px", "").trim() : "60";
                    try {
                        int sz = Integer.parseInt(v);
                        runOnUiThread(() -> {
                            sliderHeight.setProgress(sz);
                            txtHeightVal.setText(sz + "px");
                        });
                    } catch (Exception e) {}
                });

            myWebView.evaluateJavascript(
                "document.documentElement.dataset.splitShapeLinked",
                value -> {
                    boolean linked = value != null && value.replace("\"", "").trim().equals("true");
                    runOnUiThread(() -> checkLink.setChecked(linked));
                });
        }

        checkLink.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (myWebView != null) myWebView.evaluateJavascript("window.setSplitShapeLinked(" + isChecked + ");", null);
        });

        SeekBar.OnSeekBarChangeListener dimensionListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;
                if (seekBar == sliderWidth) {
                    txtWidthVal.setText(progress + "px");
                    if (myWebView != null) myWebView.evaluateJavascript("window.updateSplitShapeWidth(" + progress + ");", null);
                    if (checkLink.isChecked()) {
                        sliderHeight.setProgress(progress);
                        txtHeightVal.setText(progress + "px");
                        if (myWebView != null) myWebView.evaluateJavascript("window.updateSplitShapeHeight(" + progress + ");", null);
                    }
                } else if (seekBar == sliderHeight) {
                    txtHeightVal.setText(progress + "px");
                    if (myWebView != null) myWebView.evaluateJavascript("window.updateSplitShapeHeight(" + progress + ");", null);
                    if (checkLink.isChecked()) {
                        sliderWidth.setProgress(progress);
                        txtWidthVal.setText(progress + "px");
                        if (myWebView != null) myWebView.evaluateJavascript("window.updateSplitShapeWidth(" + progress + ");", null);
                    }
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                if (myWebView != null) myWebView.evaluateJavascript("triggerAutoSave();", null);
            }
        };

        sliderWidth.setOnSeekBarChangeListener(dimensionListener);
        sliderHeight.setOnSeekBarChangeListener(dimensionListener);

        // --- Split Frames ---
        TextView labelSplitFrames = new TextView(this);
        labelSplitFrames.setText("Split Frames");
        labelSplitFrames.setTextSize(16);
        labelSplitFrames.setTextColor(Color.parseColor("#1e3c72"));
        labelSplitFrames.setTypeface(null, Typeface.BOLD);
        labelSplitFrames.setPadding(0, 32, 0, 16);
        root.addView(labelSplitFrames);

        // --- Top Frame ---
        addSplitFrameControls(root, "Top Frame", "top", SPLIT_TOP_FRAME_COLOR_ID, "--split-top-frame-");
        
        // --- Bottom Frame ---
        addSplitFrameControls(root, "Bottom Frame", "bottom", SPLIT_BOTTOM_FRAME_COLOR_ID, "--split-bottom-frame-");

        sliderPos.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtPosVal.setText(progress + "%");
                if (fromUser && myWebView != null) {
                    myWebView.evaluateJavascript("document.documentElement.style.setProperty('--left-split-pos', '" + progress + "%');", null);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (myWebView != null) {
                    myWebView.evaluateJavascript("triggerAutoSave();", null);
                }
            }
        });

        splitDialog.setContentView(root);
        splitDialog.show();
    }

    private void deleteCustomStyle(String styleId) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String currentStyles = prefs.getString("SavedCustomStyles", "[]");
        try {
            JSONArray stylesArray = new JSONArray(currentStyles);
            JSONArray newArray = new JSONArray();
            for (int i = 0; i < stylesArray.length(); i++) {
                JSONObject obj = stylesArray.getJSONObject(i);
                if (!obj.getString("id").equals(styleId)) {
                    newArray.put(obj);
                }
            }
            prefs.edit().putString("SavedCustomStyles", newArray.toString()).apply();
            Toast.makeText(this, "Style deleted", Toast.LENGTH_SHORT).show();

            // Refresh the dialog to update the carousel
            if (currentEditingSectionId != null && currentSectionSettingsDialog != null && currentSectionSettingsDialog.isShowing()) {
                currentSectionSettingsDialog.dismiss();
                showSectionSettingsDialog(currentEditingSectionId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error deleting style", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateMultiSelectUIVisibility(View rootView, JSONArray items) {
        View multiSelectPanel = rootView.findViewById(R.id.multiSelectPanel);
        LinearLayout subsectionContainer = rootView.findViewById(R.id.subsectionContainer);

        if (multiSelectPanel == null || subsectionContainer == null) return;

        int selectedCount = selectedItemIds.size();

        // 1. Calculate Positioning for Dynamic "Merge" Look
        if (selectedCount > 1) {
            float minY = Float.MAX_VALUE;
            float maxY = 0;
            boolean anyVisible = false;

            for (int i = 0; i < subsectionContainer.getChildCount(); i++) {
                View child = subsectionContainer.getChildAt(i);
                try {
                    String id = items.getJSONObject(i).getString("id");
                    if (selectedItemIds.contains(id)) {
                        float centerY = child.getY() + (child.getHeight() / 2f);
                        minY = Math.min(minY, centerY);
                        maxY = Math.max(maxY, centerY);
                        anyVisible = true;
                    }
                } catch (Exception e) {}
            }

            if (anyVisible) {
                float targetY = (minY + maxY) / 2f - (multiSelectPanel.getHeight() / 2f);
                
                if (multiSelectPanel.getVisibility() != View.VISIBLE) {
                    multiSelectPanel.setVisibility(View.VISIBLE);
                    multiSelectPanel.setAlpha(0f);
                    multiSelectPanel.setTranslationX(100f);
                    multiSelectPanel.setY(targetY);
                    multiSelectPanel.animate().alpha(1f).translationX(0f).setDuration(400).setInterpolator(new android.view.animation.OvershootInterpolator()).start();
                } else {
                    // Smoothly animate to new center
                    multiSelectPanel.animate().y(targetY).setDuration(300).setInterpolator(new android.view.animation.DecelerateInterpolator()).start();
                }
            }
        } else {
             if (multiSelectPanel.getVisibility() == View.VISIBLE) {
                multiSelectPanel.animate().alpha(0f).translationX(100f).setDuration(250).withEndAction(() -> multiSelectPanel.setVisibility(View.GONE)).start();
            }
        }

        // 2. Loop through Items for Visual Merge & inline control visibility
        if (items != null) {
            for (int i = 0; i < items.length(); i++) {
                try {
                     JSONObject item = items.getJSONObject(i);
                     String itemId = item.getString("id");
                     
                     // Find view by traversing children (assuming order matches)
                     View itemView = subsectionContainer.getChildAt(i); 
                     if (itemView == null) continue;

                     boolean isSelected = selectedItemIds.contains(itemId);
                     
                     // Inline Controls Visibility with "Gathering" Animation
                     View actionsContainer = itemView.findViewById(R.id.actionsContainer);
                     if (actionsContainer != null) {
                         if (selectedCount > 1 && isSelected) {
                             if (actionsContainer.getVisibility() == View.VISIBLE) {
                                 // Calculate relative center target
                                 float gatherX = multiSelectPanel.getX() - (itemView.getX() + actionsContainer.getX());
                                 float gatherY = multiSelectPanel.getY() - (itemView.getY() + actionsContainer.getY());
                                 
                                 actionsContainer.animate()
                                     .translationX(gatherX)
                                     .translationY(gatherY)
                                     .alpha(0f)
                                     .scaleX(0.5f)
                                     .scaleY(0.5f)
                                     .setDuration(400)
                                     .withEndAction(() -> {
                                         actionsContainer.setVisibility(View.GONE);
                                         actionsContainer.setTranslationX(0f);
                                         actionsContainer.setTranslationY(0f);
                                         actionsContainer.setAlpha(1f);
                                         actionsContainer.setScaleX(1f);
                                         actionsContainer.setScaleY(1f);
                                     }).start();
                             }
                         } else {
                             if (actionsContainer.getVisibility() != View.VISIBLE) {
                                 actionsContainer.setVisibility(View.VISIBLE);
                                 actionsContainer.setAlpha(0f);
                                 actionsContainer.setScaleX(0.5f);
                                 actionsContainer.setScaleY(0.5f);
                                 actionsContainer.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(300).start();
                             }
                         }
                     }
                     
                     // MERGE ANIMATION Logic
                     LinearLayout rootLayout = (LinearLayout) itemView;
                     
                     // Determine Merge State
                     boolean prevSelected = false;
                     boolean nextSelected = false;
                     
                     if (i > 0) {
                         String prevId = items.getJSONObject(i-1).getString("id");
                         prevSelected = selectedItemIds.contains(prevId);
                     }
                     if (i < items.length() - 1) {
                         String nextId = items.getJSONObject(i+1).getString("id");
                         nextSelected = selectedItemIds.contains(nextId);
                     }

                     boolean shouldMergeUp = isSelected && prevSelected;
                     boolean shouldMergeDown = isSelected && nextSelected;

                     // Animate Margins
                     LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) rootLayout.getLayoutParams();
                     int defaultMargin = (int) (8 * getResources().getDisplayMetrics().density);
                     int targetMarginBottom = shouldMergeDown ? 0 : defaultMargin;
                     
                     if (params.bottomMargin != targetMarginBottom) {
                        ValueAnimator anim = ValueAnimator.ofInt(params.bottomMargin, targetMarginBottom);
                        anim.addUpdateListener(val -> {
                            params.bottomMargin = (int) val.getAnimatedValue();
                            rootLayout.setLayoutParams(params);
                        });
                        anim.setDuration(300);
                        anim.start();
                     }

                     // Update Background (Corner Radius)
                     // Use a new Drawable to avoid sharing state (mutate)
                     // Replicating rounded_box_bg props
                     GradientDrawable bg = new GradientDrawable();
                     bg.setColor(Color.parseColor("#F5F5F5")); 
                     bg.setStroke((int)(1 * getResources().getDisplayMetrics().density), Color.parseColor("#E0E0E0"));
                     
                     float r = 8 * getResources().getDisplayMetrics().density; 
                     float[] radii = {r, r, r, r, r, r, r, r};
                     
                     if (shouldMergeUp) { radii[0]=0; radii[1]=0; radii[2]=0; radii[3]=0; } 
                     if (shouldMergeDown) { radii[4]=0; radii[5]=0; radii[6]=0; radii[7]=0; }
                     
                     bg.setCornerRadii(radii);
                     rootLayout.setBackground(bg);
                     
                     // WOBBLE EFFECT (Spring-like)
                     // Trigger if newly selected and count > 1? 
                     // Or just trigger when selectedCount > 1 and isSelected
                     if (isSelected && selectedCount > 1) {
                         if (rootLayout.getScaleX() == 1f) { 
                             ObjectAnimator scaleX = ObjectAnimator.ofFloat(rootLayout, "scaleX", 1f, 1.05f, 0.95f, 1.02f, 1f);
                             ObjectAnimator scaleY = ObjectAnimator.ofFloat(rootLayout, "scaleY", 1f, 0.95f, 1.05f, 0.98f, 1f);
                             AnimatorSet set = new AnimatorSet();
                             set.playTogether(scaleX, scaleY);
                             set.setDuration(400); 
                             set.start();
                         }
                     } else {
                         rootLayout.setScaleX(1f);
                         rootLayout.setScaleY(1f);
                     }
                     
                     
                } catch (Exception e) {
                    Log.e("MultiSelect", "Error updating item ui", e);
                }
            }
        }
    }

    private void addSplitFrameControls(LinearLayout root, String label, String target, int colorPickerId, String cssPrefix) {
        TextView txtLabel = new TextView(this);
        txtLabel.setText(label);
        txtLabel.setTextSize(14);
        txtLabel.setTextColor(Color.parseColor("#757575"));
        txtLabel.setPadding(0, 16, 0, 8);
        root.addView(txtLabel);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);

        Button btnColor = new Button(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        btnColor.setText("Color");
        btnColor.setTextSize(10);
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, (int)(40 * getResources().getDisplayMetrics().density));
        btnLp.setMargins(0,0,16,0);
        btnColor.setLayoutParams(btnLp);
        btnColor.setOnClickListener(v -> openNativeColorPickerForFrame("#ffffff", colorPickerId));
        row.addView(btnColor);

        SeekBar slider = new SeekBar(this);
        slider.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        slider.setMax(20);
        row.addView(slider);

        TextView txtVal = new TextView(this);
        txtVal.setText("0px");
        txtVal.setTextSize(13);
        txtVal.setTextColor(Color.parseColor("#424242"));
        txtVal.setPadding(16,0,0,0);
        row.addView(txtVal);
        root.addView(row);

        // Sides Toggles
        LinearLayout rowSides = new LinearLayout(this);
        rowSides.setOrientation(LinearLayout.HORIZONTAL);
        rowSides.setPadding(0, 8, 0, 16);
        String[] sides = {"Top", "Bottom", "Left", "Right"};
        for (int i = 0; i < 4; i++) {
            final int sideIdx = i;
            final String sideName = sides[i].toLowerCase();
            com.google.android.material.button.MaterialButton btnSide = new com.google.android.material.button.MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            btnSide.setText(sides[i]);
            btnSide.setTextSize(10);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, (int)(36 * getResources().getDisplayMetrics().density), 1f);
            if (i < 3) lp.setMargins(0,0,4,0);
            btnSide.setLayoutParams(lp);
            btnSide.setPadding(0,0,0,0);
            btnSide.setTag(0); // 0 = off, 1 = on
            
            btnSide.setOnClickListener(v -> {
                int state = (int) v.getTag() == 0 ? 1 : 0;
                v.setTag(state);
                ((com.google.android.material.button.MaterialButton)v).setStrokeColor(android.content.res.ColorStateList.valueOf(state == 1 ? Color.parseColor("#1e3c72") : Color.parseColor("#E0E0E0")));
                ((com.google.android.material.button.MaterialButton)v).setTextColor(state == 1 ? Color.parseColor("#1e3c72") : Color.parseColor("#757575"));
                
                if (myWebView != null) {
                    myWebView.evaluateJavascript("document.documentElement.style.getPropertyValue('" + cssPrefix + "top').trim()", s0 -> {
                    myWebView.evaluateJavascript("document.documentElement.style.getPropertyValue('" + cssPrefix + "bottom').trim()", s1 -> {
                    myWebView.evaluateJavascript("document.documentElement.style.getPropertyValue('" + cssPrefix + "left').trim()", s2 -> {
                    myWebView.evaluateJavascript("document.documentElement.style.getPropertyValue('" + cssPrefix + "right').trim()", s3 -> {
                        String[] currentSides = {
                            s0 != null ? s0.replace("\"","").trim() : "0", 
                            s1 != null ? s1.replace("\"","").trim() : "0", 
                            s2 != null ? s2.replace("\"","").trim() : "0", 
                            s3 != null ? s3.replace("\"","").trim() : "0"
                        };
                        currentSides[sideIdx] = String.valueOf(state);
                        String jsSides = "[" + currentSides[0] + "," + currentSides[1] + "," + currentSides[2] + "," + currentSides[3] + "]";
                        myWebView.evaluateJavascript("window.updateSplitFrameConfig('" + target + "', { sides: " + jsSides + " });", null);
                    });});});});
                }
            });
            rowSides.addView(btnSide);

            // Initial Side States from WebView
            if (myWebView != null) {
                myWebView.evaluateJavascript("getComputedStyle(document.documentElement).getPropertyValue('" + cssPrefix + sideName + "').trim()", val -> {
                    String vVal = val != null ? val.replace("\"", "").trim() : "0";
                    runOnUiThread(() -> {
                        int state = vVal.equals("1") ? 1 : 0;
                        btnSide.setTag(state);
                        btnSide.setStrokeColor(android.content.res.ColorStateList.valueOf(state == 1 ? Color.parseColor("#1e3c72") : Color.parseColor("#E0E0E0")));
                        btnSide.setTextColor(state == 1 ? Color.parseColor("#1e3c72") : Color.parseColor("#757575"));
                    });
                });
            }
        }
        root.addView(rowSides);

        // Initial Progress and Change Listener
        if (myWebView != null) {
            myWebView.evaluateJavascript("getComputedStyle(document.documentElement).getPropertyValue('" + cssPrefix + "thickness').trim()", val -> {
                String vVal = val != null ? val.replace("\"", "").replace("px", "").trim() : "0";
                try {
                    int p = (int)Float.parseFloat(vVal);
                    runOnUiThread(() -> {
                        slider.setProgress(p);
                        txtVal.setText(p + "px");
                    });
                } catch (Exception e) {}
            });
        }

        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtVal.setText(progress + "px");
                if (fromUser && myWebView != null) {
                    myWebView.evaluateJavascript("window.updateSplitFrameConfig('" + target + "', { thickness: " + progress + " });", null);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                if (myWebView != null) myWebView.evaluateJavascript("triggerAutoSave();", null);
            }
        });
    }
}

