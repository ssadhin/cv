package com.example.myapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
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
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

import android.content.SharedPreferences;
import android.net.Uri;
import org.json.JSONObject;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

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
import org.json.JSONException;


import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

public class MainActivity extends AppCompatActivity implements ColorPickerDialogListener {

    private WebView myWebView;
    private boolean isNativeEditing = false;

    // Original Buttons
    private View addFab, undoFab, redoFab, editFab, printFab;
    private View undoRedoContainer, addEditContainer;
    private ImageButton editFabIcon, undoFabIcon, redoFabIcon, addFabIcon;

    private ValueCallback<Uri[]> mUploadMessage;
    private ActivityResultLauncher<String> mGetContent;
    private ActivityResultLauncher<Intent> mSignatureResultLauncher;

    private SidePanelHelper leftPanel;
    private SidePanelHelper rightPanel;
    private boolean isPanelSwitching = false;
    private static final String TAG = "MainActivity";
    private static final int COLOR_PICKER_ID = 1;

    private static final int HIGHLIGHT_COLOR_PICKER_ID = 2;
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


    private SeekBar preciseColumnWidthSlider;
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
    
    



    private TextView cvNameDisplay;
    private String currentFilePath;
    private boolean isNewFile;
    private String pendingJsonState; // Holds JSON data read from file until WebView is ready
    
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
                
                setupPanelButton(btnAdd, "Add", R.drawable.ic_add, v -> myWebView.evaluateJavascript("if(currentEditableElement) { const section = currentEditableElement; const contentArea = section.querySelector('.content-area'); if(contentArea) { const lastItem = contentArea.querySelector('.data-table-item:last-child, .skill-group:last-child, .simple-list-item:last-child, .pd-row:last-child'); if(lastItem) { const newItem = lastItem.cloneNode(true); newItem.querySelectorAll('.table-val, .table-label, .skill-sub, .skill-header, .pd-val, .pd-label, .exp-role, .proj-desc, li, .simple-list-item').forEach(el => { el.contentEditable = true; el.textContent = el.classList.contains('table-label') || el.classList.contains('pd-label') ? 'Label:' : 'New entry...'; }); contentArea.appendChild(newItem); newItem.onclick = (e) => { e.stopPropagation(); currentEditableElement = newItem; if(window.Android) Android.showToolbar('default'); }; newItem.querySelectorAll('li').forEach(li => { li.onclick = (e) => { e.stopPropagation(); currentEditableElement = li; if(window.Android) Android.showToolbar('item'); }; }); triggerAutoSave(); } } removeToolbar(); }", null));
                setupPanelButton(btnCopy, "Copy", R.drawable.copy, v -> myWebView.evaluateJavascript("if(currentEditableElement) { const section = currentEditableElement; let clone = section.cloneNode(true); let baseId = section.id.replace(/-\\d+$/, ''); let num = 1; while(document.getElementById(baseId + '-' + num)) num++; clone.id = baseId + '-' + num; section.parentNode.insertBefore(clone, section.nextSibling); triggerAutoSave(); const h2 = clone.querySelector('h2'); if(h2) h2.onclick = (e) => { currentEditableElement = clone; if(window.Android) Android.showToolbar('section'); }; if(typeof enableFreeDrag === 'function') enableFreeDrag(clone); clone.querySelectorAll('.data-table-item, .simple-list-item, .skill-group, .pd-row').forEach(item => { item.onclick = (e) => { e.stopPropagation(); currentEditableElement = item; if(window.Android) Android.showToolbar('default'); }; }); clone.querySelectorAll('li').forEach(li => { li.onclick = (e) => { e.stopPropagation(); currentEditableElement = li; if(window.Android) Android.showToolbar('item'); }; }); clone.scrollIntoView({ behavior: 'smooth', block: 'center' }); setTimeout(() => { currentEditableElement = clone; if(window.Android) Android.showToolbar('section'); }, 200); }", null));
                setupPanelButton(btnEdit, "Edit", R.drawable.edit, v -> myWebView.evaluateJavascript("if(currentEditableElement) { const sec = currentEditableElement; if(sec.dataset.editing === 'true') { sec.removeAttribute('data-editing'); sec.querySelectorAll('[contenteditable]').forEach(el => el.contentEditable = false); sec.querySelectorAll('.section-icon-upload-btn').forEach(b => b.remove()); sec.querySelectorAll('.section-icon-file-input').forEach(i => i.remove()); triggerAutoSave(); } else { sec.dataset.editing = 'true'; sec.querySelectorAll('.table-val, .table-label, .pd-val, .pd-label, .exp-role, .skill-header, .skill-sub, .proj-desc, span:not(.table-label):not(.pd-label), .simple-list-item').forEach(el => el.contentEditable = true); const h2 = sec.querySelector('h2'); if(h2) { h2.contentEditable = true; const icon = h2.querySelector('i'); if(icon && !h2.querySelector('.section-icon-upload-btn')) { const uploadBtn = document.createElement('span'); uploadBtn.className = 'section-icon-upload-btn'; uploadBtn.innerHTML = '<i class=\"fas fa-cloud-upload-alt\"></i>'; uploadBtn.contentEditable = 'false'; uploadBtn.onclick = (e) => { e.preventDefault(); e.stopPropagation(); let input = h2.querySelector('.section-icon-file-input'); if(!input) { input = document.createElement('input'); input.type = 'file'; input.accept = 'image/*'; input.className = 'section-icon-file-input'; input.style.display = 'none'; input.onchange = (ev) => { const file = ev.target.files[0]; if(file) { const reader = new FileReader(); reader.onload = (re) => { icon.style.backgroundImage = 'url(' + re.target.result + ')'; icon.style.backgroundSize = 'contain'; icon.style.backgroundRepeat = 'no-repeat'; icon.style.backgroundPosition = 'center'; icon.style.color = 'transparent'; icon.dataset.customIcon = 'true'; triggerAutoSave(); }; reader.readAsDataURL(file); } }; h2.appendChild(input); } input.click(); }; h2.insertBefore(uploadBtn, icon.nextSibling); } } } const isEditing = sec.dataset.editing === 'true'; if(window.Android) Android.updateEditButton(isEditing); }", null));
                setupPanelButton(btnSwap, "Swap", R.drawable.swap, v -> myWebView.evaluateJavascript("if(currentEditableElement) { const section = currentEditableElement; const p = section.closest('.left-column') ? document.getElementById('leftCol') : document.getElementById('rightCol'); const t = p.id === 'leftCol' ? document.getElementById('rightCol') : document.getElementById('leftCol'); t.appendChild(section); section.style.transform = 'translateY(0)'; removeToolbar(); triggerAutoSave(); }", null));
                setupPanelButton(btnUp, "Up", R.drawable.up, v -> myWebView.evaluateJavascript("if(currentEditableElement) { const prev = currentEditableElement.previousElementSibling; if(prev && prev.tagName === 'SECTION') { currentEditableElement.parentNode.insertBefore(currentEditableElement, prev); currentEditableElement.style.marginTop = ''; currentEditableElement.classList.add('item-slide-from-below'); prev.classList.add('item-slide-from-above'); setTimeout(() => { currentEditableElement.classList.remove('item-slide-from-below'); prev.classList.remove('item-slide-from-above'); }, 300); triggerAutoSave(); }}", null));
                setupPanelButton(btnDown, "Down", R.drawable.down, v -> myWebView.evaluateJavascript("if(currentEditableElement) { const next = currentEditableElement.nextElementSibling; if(next && next.tagName === 'SECTION') { currentEditableElement.parentNode.insertBefore(next, currentEditableElement); currentEditableElement.classList.add('item-slide-from-above'); next.classList.add('item-slide-from-below'); setTimeout(() => { currentEditableElement.classList.remove('item-slide-from-above'); next.classList.remove('item-slide-from-below'); }, 300); triggerAutoSave(); }}", null));
                setupPanelButton(btnDelete, "Delete", R.drawable.delete, v -> myWebView.evaluateJavascript("if(currentEditableElement && confirm('Delete this section?')) { currentEditableElement.remove(); triggerAutoSave(); removeToolbar(); }", null));
                break;
                
            case "header": // Swap, Up, Down, Copy, Upload, Delete
                btnSwap.setVisibility(View.VISIBLE);
                btnUp.setVisibility(View.VISIBLE);
                btnDown.setVisibility(View.VISIBLE);
                btnCopy.setVisibility(View.VISIBLE);
                btnUpload.setVisibility(View.VISIBLE);
                btnDelete.setVisibility(View.VISIBLE);
                
                setupPanelButton(btnSwap, "Swap", R.drawable.swap, v -> myWebView.evaluateJavascript("if(currentEditableElement) { swapHeaderItemColumn(currentEditableElement); }", null));
                setupPanelButton(btnUp, "Up", R.drawable.up, v -> myWebView.evaluateJavascript("if(currentEditableElement) { const prev = currentEditableElement.previousElementSibling; if(prev && prev.classList.contains('contact-item')) { currentEditableElement.parentNode.insertBefore(currentEditableElement, prev); triggerAutoSave(); }}", null));
                setupPanelButton(btnDown, "Down", R.drawable.down, v -> myWebView.evaluateJavascript("if(currentEditableElement) { const next = currentEditableElement.nextElementSibling; if(next && next.classList.contains('contact-item')) { currentEditableElement.parentNode.insertBefore(next, currentEditableElement); triggerAutoSave(); }}", null));
                setupPanelButton(btnCopy, "Copy", R.drawable.copy, v -> myWebView.evaluateJavascript("duplicateHeaderItem();", null));
                setupPanelButton(btnUpload, "Upload", R.drawable.upload, v -> myWebView.evaluateJavascript("triggerHeaderItemUpload();", null));
                setupPanelButton(btnDelete, "Delete", R.drawable.delete, v -> myWebView.evaluateJavascript("if(currentEditableElement && confirm('Delete this item?')) { currentEditableElement.remove(); triggerAutoSave(); removeToolbar(); }", null));
                break;
                

        }
    }

    private void setupAddFeaturePanel() {
        addFeaturePanel = findViewById(R.id.add_feature_panel);
        
        // Setup ViewPager and TabLayout
        TabLayout tabLayout = findViewById(R.id.panel_tab_layout);
        viewPager = findViewById(R.id.panel_view_pager);

        if (tabLayout != null && viewPager != null) {
            AddPanelAdapter adapter = new AddPanelAdapter();
            viewPager.setAdapter(adapter);

            new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                switch(position) {
                    case 0: tab.setText("Sections"); break;
                    case 1: tab.setText("Template"); break;
                    case 2: tab.setText("Fonts"); break;
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

    private void changeNativeFont(String fontName, String fontFamily, String fontUrl) {
         // Persist selection
         getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putString(KEY_SELECTED_FONT, fontName).apply();
         
         String json = "{ \"id\": \"" + fontName + "\", \"name\": \"" + fontName + "\", \"family\": \"" + fontFamily + "\", \"url\": \"" + fontUrl + "\", \"isCustom\": false }";
         json = json.replace("\"", "\\\"");
         json = json.replace("'", "\\'");
         String js = "localStorage.setItem('resume_font_config', '" + json + "'); applyFontConfig();";
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
        String formattedColor = String.format("#%06X", (0xFFFFFF & color));
        
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
            // Get current text color from variable if possible
            String currentColor = "#333333";
            // For sidebar, we might want to get the actual computed style, but 'text' is safe for now
            openNativeColorPicker("text", currentColor);
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
                    if (mUploadMessage != null && uri != null) {
                        mUploadMessage.onReceiveValue(new Uri[]{uri});
                        mUploadMessage = null;
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
        
        // Custom Client to inject saved zoom on load
        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Apply saved zoom level
                SharedPreferences prefs = getPreferences(MODE_PRIVATE);
                int savedZoom = prefs.getInt("zoom_level", 15);
                float zoomValue = 30 + savedZoom;
                view.evaluateJavascript("document.getElementById('fontSizeSlider').value = " + zoomValue + "; document.getElementById('fontSizeSlider').dispatchEvent(new Event('input'));", null);
                
                // Inject current file path so JavaScript knows which CV file to save to
                if (currentFilePath != null) {
                    String safePath = currentFilePath.replace("\\", "\\\\").replace("'", "\\'");
                    view.evaluateJavascript("window.CURRENT_FILE_PATH = '" + safePath + "';", null);
                    Log.d(TAG, "✓ Injected file path into WebView: " + currentFilePath);
                } else {
                    view.evaluateJavascript("window.CURRENT_FILE_PATH = null;", null);
                    Log.d(TAG, "✗ No file path - this is a new CV");
                }
                
                if (getIntent() != null && getIntent().getBooleanExtra("EXTRA_FROM_STEP_BY_STEP", false)) {
                    ArrayList<String> sections = getIntent().getStringArrayListExtra("EXTRA_STEP_BY_STEP_SECTIONS");
                    String layout = getIntent().getStringExtra("EXTRA_TARGET_LAYOUT");
                    String wizardData = getIntent().getStringExtra("EXTRA_STEP_BY_STEP_DATA");
                    String generatedPath = getIntent().getStringExtra("EXTRA_GENERATED_FILEPATH");
                    
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
                        String layoutSafe = (layout != null) ? "'" + layout + "'" : "null";
                        String setupJs = "window.loadResumeData(" + wizardData + ", " + layoutSafe + "); ";
                        view.evaluateJavascript(clearStorageJs + setupJs, null);
                        Log.d(TAG, "✓ Injected Full Wizard Data (via loadResumeData)");
                    } else if (sections != null) {
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
                    view.evaluateJavascript("window.loadResumeData('" + safeJson + "')", null);
                    pendingJsonState = null; // Clear after loading to prevent reuse
                    Log.d(TAG, "Cleared pendingJsonState after loading");
                } else {
                    Log.d(TAG, "No pending JSON state to load");
                }
            }
        });

        myWebView.setWebChromeClient(getChromeClient());
        myWebView.addJavascriptInterface(new WebAppInterface(this), "Android");
        myWebView.loadUrl("file:///android_asset/index.html");
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

    public class WebAppInterface {
        Context mContext;

        WebAppInterface(Context c) {
            mContext = c;
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
        public void toggleNativeFab(boolean visible) {
            runOnUiThread(() -> {
                isNativeEditing = visible;
                updateNativeUI(visible);
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
                        Log.d(TAG, "Updated WebView with new file path");
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
                    Toast.makeText(MainActivity.this, "Saved", Toast.LENGTH_SHORT).show();
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
        public void updateEditButton(boolean isEditing) {
             runOnUiThread(() -> {
                 if(atsBadge != null) {
                     atsBadge.setVisibility(isEditing ? View.GONE : View.VISIBLE);
                     if (!isEditing && atsBadgeScore != null) {
                         // Instant Loading State
                         atsBadgeScore.setText("...");
                         android.graphics.drawable.GradientDrawable shape = (android.graphics.drawable.GradientDrawable) atsBadgeScore.getBackground();
                         if (shape != null) shape.setColor(Color.parseColor("#999999"));
                     }
                 }
                 
                 if (isEditing) {
                     editFabIcon.setImageResource(R.drawable.ic_check); // Change to Checkmark
                     isNativeEditing = true;
                 } else {
                     editFabIcon.setImageResource(R.drawable.edit); // Change back to Pencil
                     isNativeEditing = false;
                     
                     // Trigger fresh analysis immediately
                     myWebView.evaluateJavascript("if(window.atsAnalyzer) window.atsAnalyzer.analyze();", null);
                 }
             });
        }

        @JavascriptInterface
        public void onToolbarAction(String action) {
            Log.d(TAG, "onToolbarAction called: " + action);
            // This is a stub to prevent JS crashes.
            // Future implementation can handle specific actions here if needed.
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
            runOnUiThread(() -> {
                if (addFeaturePanel != null && addFeaturePanel.getVisibility() == View.VISIBLE) {
                    loadSectionsFromWebView();
                }
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
        public void onMobilePanelActive() {
            // This can be used to show a native menu or just bridge to the wizard
            Log.d(TAG, "onMobilePanelActive triggered");
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

    // --- Section Grid Logic ---
    private static class SectionItem {
        String id, name, desc, col, group;
        int iconRes;
        boolean isAdded;
        SectionItem(String id, String name, String desc, int iconRes, boolean isAdded, String col, String group) {
            this.id = id; this.name = name; this.desc = desc; this.iconRes = iconRes; this.isAdded = isAdded;
            this.col = col; this.group = group;
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
            default: return R.drawable.ic_add;
        }
    }

    private void loadSectionsFromWebView() {
        if (myWebView == null) return;
        
        // 1. Get ALL available sections first
        myWebView.evaluateJavascript("window.getSectionsJSON()", allSectionsJson -> {
            if (allSectionsJson == null || allSectionsJson.equals("null") || allSectionsJson.isEmpty()) return;

            // 2. Get CURRENTLY added section IDs
            myWebView.evaluateJavascript("window.getCurrentSectionIds()", currentIdsJson -> {
                try {
                    // (Same parsing logic as before...)
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
                        boolean isAdded = addedIds.contains(type);
                        
                        list.add(new SectionItem(
                            id,
                            obj.optString("name"),
                            obj.optString("desc"),
                            getIconResForFontAwesome(obj.optString("icon")),
                            isAdded,
                            obj.optString("col", "Flexible"),
                            obj.optString("group", "Standard")
                        ));
                    }
                    
                    Collections.sort(list, (o1, o2) -> Boolean.compare(o1.isAdded, o2.isAdded));

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
        LinearLayout leftCol = addFeaturePanel.findViewById(R.id.column_left);
        LinearLayout rightCol = addFeaturePanel.findViewById(R.id.column_right);
        
        if (leftCol == null || rightCol == null) return;
        
        leftCol.removeAllViews();
        rightCol.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < items.size(); i++) {
            SectionItem item = items.get(i);
            View v = inflater.inflate(R.layout.item_section_add, (i % 2 == 0) ? leftCol : rightCol, false);

            TextView title = v.findViewById(R.id.section_title);
            TextView desc = v.findViewById(R.id.section_desc);
            ImageView icon = v.findViewById(R.id.section_icon);
            View iconContainer = v.findViewById(R.id.section_container); 
            ImageButton btnAdd = v.findViewById(R.id.btn_add_section);

            title.setText(item.name);
            desc.setText(item.desc);
            icon.setImageResource(item.iconRes);
            
            // Handle Added/Not Added state
            if (item.isAdded) {
                // If added, change button to checkmark/gray or hide?
                btnAdd.setImageResource(R.drawable.ic_check);
                btnAdd.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E0E0E0")));
                btnAdd.setImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#757575")));
                btnAdd.setEnabled(false);
                v.setAlpha(0.6f);
            } else {
                btnAdd.setImageResource(R.drawable.ic_add);
                btnAdd.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                btnAdd.setImageTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
                btnAdd.setEnabled(true);
                v.setAlpha(1.0f);
            }

            // Click listener on the whole container or button
            View.OnClickListener addListener = view -> {
                if (!item.isAdded) {
                    addNativeSection(item.id, item.col);
                }
            };
            
            if (iconContainer != null) iconContainer.setOnClickListener(addListener);
            if (btnAdd != null) btnAdd.setOnClickListener(addListener);

            // Add to appropriate column
            if (i % 2 == 0) {
                leftCol.addView(v);
            } else {
                rightCol.addView(v);
            }
        }
    }



    // --- Inner Adapter Class ---
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
            
            View v = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            return new ViewHolder(v, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bindEvents();
        }

        @Override
        public int getItemCount() { return 3; }

        class ViewHolder extends RecyclerView.ViewHolder {
            int type;
            ViewHolder(View v, int type) { super(v); this.type = type; }

            void bindEvents() {
                if (type == 0) { // Sections Grid
                    loadSectionsFromWebView();
                    
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
                    bindTemplateBtn(R.id.btn_template_classic, "default");
                    bindTemplateBtn(R.id.btn_template_sidebar, "sidebar");
                } else if (type == 2) { // Fonts
                    bindFontBtn(R.id.btn_font_roboto, "Roboto", "'Roboto', sans-serif", "https://fonts.googleapis.com/css2?family=Roboto:wght@300;400;700&display=swap");
                    bindFontBtn(R.id.btn_font_poppins, "Poppins", "'Poppins', sans-serif", "https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap");
                    bindFontBtn(R.id.btn_font_opensans, "Open Sans", "'Open Sans', sans-serif", "https://fonts.googleapis.com/css2?family=Open+Sans:wght@300;400;600;700&display=swap");
                    bindFontBtn(R.id.btn_font_ubuntu, "Ubuntu", "'Ubuntu', sans-serif", "https://fonts.googleapis.com/css2?family=Ubuntu:wght@300;400;500;700&display=swap");
                    bindFontBtn(R.id.btn_font_merriweather, "Merriweather", "'Merriweather', serif", "https://fonts.googleapis.com/css2?family=Merriweather:ital,wght@0,300;0,400;0,700;1,400&display=swap");
                    bindFontBtn(R.id.btn_font_monospace, "Courier Prime", "'Courier Prime', monospace", "https://fonts.googleapis.com/css2?family=Courier+Prime:wght@400;700&display=swap");
                    
                    View btnCustom = itemView.findViewById(R.id.btn_font_custom);
                    if (btnCustom != null) btnCustom.setOnClickListener(v -> importCustomFont());
                }
            }
            
            void bindTemplateBtn(int id, String type) {
                View btn = itemView.findViewById(id);
                if (btn != null) btn.setOnClickListener(v -> changeNativeTemplate(type));
            }
            
            void bindFontBtn(int id, String name, String family, String url) {
                 android.widget.TextView btn = itemView.findViewById(id);
                 if (btn != null) {
                     btn.setOnClickListener(v -> changeNativeFont(name, family, url));
                     
                     String currentFont = itemView.getContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(KEY_SELECTED_FONT, "Roboto");
                     if (currentFont.equals(name)) {
                         btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_active_dot, 0);
                     } else {
                         btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                     }
                 }
            }
        }
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
}
