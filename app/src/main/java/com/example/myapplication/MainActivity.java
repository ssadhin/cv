package com.example.myapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.util.Log;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

public class MainActivity extends AppCompatActivity implements ColorPickerDialogListener {

    private WebView myWebView;
    private boolean isNativeEditing = false;

    // Original Buttons
    private View addFab, undoFab, redoFab, editFab, printFab;
    private View undoRedoContainer, addEditContainer;
    private ImageButton editFabIcon;

    private ValueCallback<Uri[]> mUploadMessage;
    private ActivityResultLauncher<String> mGetContent;
    private ActivityResultLauncher<Intent> mSignatureResultLauncher;

    private SidePanelHelper leftPanel;
    private SidePanelHelper rightPanel;
    private boolean isPanelSwitching = false;
    private static final String TAG = "MainActivity";
    private static final int COLOR_PICKER_ID = 1;
    private CardView temporaryPanel;

    private View tempAddButton, tempCopyButton, tempEditButton, tempSwapButton, tempListButton, tempDeleteButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View rootView = findViewById(R.id.root_container);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            adjustButtonMargins();
            return insets;
        });

        setupResultLaunchers();
        setupWebView();
        setupSidePanels();
        setupOriginalButtons();
        setupTemporaryPanel();
    }

    private void setupTemporaryPanel() {
        temporaryPanel = findViewById(R.id.temporary_panel);
        tempAddButton = findViewById(R.id.temp_add_button);
        tempCopyButton = findViewById(R.id.temp_copy_button);
        tempEditButton = findViewById(R.id.temp_edit_button);
        tempSwapButton = findViewById(R.id.temp_swap_button);
        tempListButton = findViewById(R.id.temp_list_button);
        tempDeleteButton = findViewById(R.id.temp_delete_button);

        setupPanelButton(tempAddButton, "Add", R.drawable.iconshow, v -> {
            if (myWebView != null) {
                myWebView.evaluateJavascript("document.querySelector('.edit-toolbar .fa-plus').closest('button').click();", null);
            }
        });
        setupPanelButton(tempCopyButton, "Copy", android.R.drawable.ic_menu_save, v -> {
            if (myWebView != null) {
                myWebView.evaluateJavascript("document.querySelector('.edit-toolbar .fa-copy').closest('button').click();", null);
            }
        });
        setupPanelButton(tempEditButton, "Edit", R.drawable.edit, v -> {
            if (myWebView != null) {
                myWebView.evaluateJavascript("document.querySelector('.edit-toolbar .fa-edit').closest('button').click();", null);
            }
        });
        setupPanelButton(tempSwapButton, "Swap", R.drawable.swap, v -> {
            if (myWebView != null) {
                myWebView.evaluateJavascript("document.querySelector('.edit-toolbar .fa-exchange-alt').closest('button').click();", null);
            }
        });
        setupPanelButton(tempListButton, "List", R.drawable.pagebreak, v -> {
            if (myWebView != null) {
                myWebView.evaluateJavascript("document.querySelector('.edit-toolbar .fa-list-ul').closest('button').click();", null);
            }
        });
        setupPanelButton(tempDeleteButton, "Delete", R.drawable.delete, v -> {
            if (myWebView != null) {
                myWebView.evaluateJavascript("deleteCurrentSelection();", null);
            }
        });
    }


    private void setupSidePanels() {
        leftPanel = new SidePanelHelper(true, this::swapPanels);
        rightPanel = new SidePanelHelper(false, this::swapPanels);

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

    @Override
    public void onColorSelected(int dialogId, int color) {
        if (dialogId == COLOR_PICKER_ID) {
            String formattedColor = String.format("#%06X", (0xFFFFFF & color));
            if (myWebView != null) {
                myWebView.evaluateJavascript("document.documentElement.style.setProperty('--text-main', '" + formattedColor + "');", null);
            }
        }
    }

    @Override
    public void onDialogDismissed(int dialogId) {

    }

    private class SidePanelHelper {
        private final CardView panel;
        private final View showButton, hideButton, swapButton, deleteButton;
        private final FrameLayout zoomSliderContainer, fontSizeSliderContainer, lineSpacingSliderContainer;
        private final SeekBar zoomSlider, fontSizeSlider, lineSpacingSlider;
        private final View zoomButton, fontSizeButton, lineSpacingButton, fontColorButton, toggleIconsButton;
        private boolean areIconsHidden = false;
        private View currentSliderContainer = null;
        private final boolean isLeft;
        private final Runnable onSwap;

        SidePanelHelper(boolean isLeft, Runnable onSwap) {
            this.isLeft = isLeft;
            this.onSwap = onSwap;

            panel = findViewById(isLeft ? R.id.side_panel_left : R.id.side_panel_right);
            showButton = findViewById(isLeft ? R.id.show_button_left : R.id.show_button_right);
            hideButton = findViewById(isLeft ? R.id.hide_button_left : R.id.hide_button_right);
            swapButton = findViewById(isLeft ? R.id.swap_button_left : R.id.swap_button_right);
            deleteButton = findViewById(isLeft ? R.id.delete_button_left : R.id.delete_button_right);
            zoomSliderContainer = findViewById(isLeft ? R.id.zoom_slider_container_left : R.id.zoom_slider_container_right);
            fontSizeSliderContainer = findViewById(isLeft ? R.id.font_size_slider_container_left : R.id.font_size_slider_container_right);
            lineSpacingSliderContainer = findViewById(isLeft ? R.id.line_spacing_slider_container_left : R.id.line_spacing_slider_container_right);
            zoomSlider = findViewById(isLeft ? R.id.zoom_slider_left : R.id.zoom_slider_right);
            fontSizeSlider = findViewById(isLeft ? R.id.font_size_slider_left : R.id.font_size_slider_right);
            lineSpacingSlider = findViewById(isLeft ? R.id.line_spacing_slider_left : R.id.line_spacing_slider_right);
            zoomButton = findViewById(isLeft ? R.id.zoom_button_left : R.id.zoom_button_right);
            fontSizeButton = findViewById(isLeft ? R.id.font_size_button_left : R.id.font_size_button_right);
            lineSpacingButton = findViewById(isLeft ? R.id.line_spacing_button_left : R.id.line_spacing_button_right);
            fontColorButton = findViewById(isLeft ? R.id.font_color_button_left : R.id.font_color_button_right);
            toggleIconsButton = findViewById(isLeft ? R.id.toggle_icons_button_left : R.id.toggle_icons_button_right);

            setupPanelListeners();
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
                areIconsHidden = !areIconsHidden;
                if (myWebView != null) {
                    myWebView.evaluateJavascript("document.getElementById('toggleIconsBtn').click();", null);
                }
            });

            setupPanelButton(deleteButton, "Delete", R.drawable.delete, v -> {
                if (myWebView != null) {
                    myWebView.evaluateJavascript("document.getElementById('resetBtn').click();", null);
                }
            });

            setupPanelButton(zoomButton, "Zoom", R.drawable.zoom, v -> toggleSliderVisibility(zoomSliderContainer));
            setupPanelButton(fontSizeButton, "Font Size", R.drawable.fontsize, v -> toggleSliderVisibility(fontSizeSliderContainer));
            setupPanelButton(lineSpacingButton, "Spacing", R.drawable.linebreak, v -> toggleSliderVisibility(lineSpacingSliderContainer));
            setupPanelButton(fontColorButton, "Color", R.drawable.color, v -> openColorPicker());


            setupSeekBarListeners();
        }

        public void hidePanel() {
            hidePanel(null);
        }

        public void hidePanel(Runnable onHidden) {
            if (panel == null || panel.getVisibility() == View.GONE) {
                if (onHidden != null) onHidden.run();
                return;
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
            ColorPickerDialog.newBuilder()
                    .setDialogId(COLOR_PICKER_ID)
                    .setColor(Color.BLACK)
                    .setShowAlphaSlider(true)
                    .show(MainActivity.this);
        }

        private void setupSeekBarListeners() {
            if (zoomSlider != null) {
                zoomSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser && myWebView != null) {
                            float zoomValue = 30 + progress;
                            myWebView.evaluateJavascript("document.getElementById('fontSizeSlider').value = " + zoomValue + "; document.getElementById('fontSizeSlider').dispatchEvent(new Event('input'));", null);
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

        private void toggleSliderVisibility(View sliderContainer) {
            if (sliderContainer == null) {
                return;
            }
            if (currentSliderContainer != null && currentSliderContainer != sliderContainer) {
                currentSliderContainer.setVisibility(View.GONE);
            }

            if (sliderContainer.getVisibility() == View.VISIBLE) {
                sliderContainer.setVisibility(View.GONE);
                currentSliderContainer = null;
            } else {
                sliderContainer.setVisibility(View.VISIBLE);
                currentSliderContainer = sliderContainer;
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
        myWebView.setWebViewClient(new WebViewClient());
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
        if (undoFab != null) {
            undoFab.setOnClickListener(v -> {
                if (myWebView != null) {
                    myWebView.evaluateJavascript("document.getElementById('undoBtn').click();", null);
                }
            });
        }

        redoFab = findViewById(R.id.fab_redo);
        if(redoFab != null) {
            redoFab.setOnClickListener(v -> {
                if (myWebView != null) {
                    myWebView.evaluateJavascript("document.getElementById('redoBtn').click();", null);
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
                if (editFabIcon != null) {
                    if (isNativeEditing) {
                        editFabIcon.setImageResource(R.drawable.save);
                        if (printFab != null) hidePrintButton(printFab);
                        if (leftPanel.getPanel().getVisibility() == View.VISIBLE) {
                            leftPanel.hidePanel();
                        }
                        if (rightPanel.getPanel().getVisibility() == View.VISIBLE) {
                            rightPanel.hidePanel();
                        }
                    } else {
                        editFabIcon.setImageResource(R.drawable.edit);
                        if (printFab != null) showPrintButton(printFab);
                    }
                }
            });
        }


        addFab = findViewById(R.id.fab_add);
        if (addFab != null) {
            addFab.setOnClickListener(v -> {
                if (myWebView != null) {
                    myWebView.loadUrl("file:///android_asset/more-features.html");
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
            printManager.print(jobName, printAdapter, new PrintAttributes.Builder().build());
        }
    }

    private void hidePrintButton(View view) {
        if (view == null || view.getVisibility() != View.VISIBLE) return;

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(fadeOut, scaleX, scaleY);
        animatorSet.setDuration(300);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
            }
        });
        animatorSet.start();
    }

    private void showPrintButton(View view) {
        if (view == null) return;

        view.setVisibility(View.VISIBLE);
        view.setAlpha(0f);
        view.setScaleX(0f);
        view.setScaleY(0f);
        view.setRotation(-360f);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0f, 1f);
        ObjectAnimator rotate = ObjectAnimator.ofFloat(view, "rotation", -360f, 0f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(fadeIn, scaleX, scaleY, rotate);
        animatorSet.setDuration(500);
        animatorSet.start();
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (myWebView != null) {
            myWebView.saveState(outState);
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (myWebView != null) {
            myWebView.restoreState(savedInstanceState);
        }
    }

    protected void openFileChooser(ValueCallback<Uri[]> uploadMsg) {
        mUploadMessage = uploadMsg;
        mGetContent.launch("image/*");
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
        public void showToolbar(String toolbarType) {
            runOnUiThread(() -> {
                if (temporaryPanel == null) return;

                ConstraintLayout rootLayout = findViewById(R.id.root_container);
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(rootLayout);

                boolean isRightPanelActive = rightPanel.getPanel().getVisibility() == View.VISIBLE || rightPanel.getShowButton().getVisibility() == View.VISIBLE;

                if (isRightPanelActive) {
                    constraintSet.connect(R.id.temporary_panel, ConstraintSet.START, R.id.root_container, ConstraintSet.START, 16);
                    constraintSet.clear(R.id.temporary_panel, ConstraintSet.END);
                } else {
                    constraintSet.connect(R.id.temporary_panel, ConstraintSet.END, R.id.root_container, ConstraintSet.END, 16);
                    constraintSet.clear(R.id.temporary_panel, ConstraintSet.START);
                }
                constraintSet.applyTo(rootLayout);

                setToolbarButtons(toolbarType);
                if (temporaryPanel.getVisibility() != View.VISIBLE) {
                    temporaryPanel.setVisibility(View.VISIBLE);
                    temporaryPanel.setAlpha(0.0f);
                    temporaryPanel.animate().alpha(1.0f).setDuration(200).setListener(null);
                }
            });
        }

        @JavascriptInterface
        public void hideToolbar() {
            runOnUiThread(() -> {
                if (temporaryPanel == null || temporaryPanel.getVisibility() != View.VISIBLE) return;
                temporaryPanel.animate().alpha(0.0f).setDuration(200).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        temporaryPanel.setVisibility(View.GONE);
                    }
                });
            });
        }

        private void setToolbarButtons(String toolbarType) {
            if (tempAddButton == null) return; // All buttons are loaded together

            tempAddButton.setVisibility(View.GONE);
            tempCopyButton.setVisibility(View.GONE);
            tempEditButton.setVisibility(View.GONE);
            tempSwapButton.setVisibility(View.GONE);
            tempListButton.setVisibility(View.GONE);
            tempDeleteButton.setVisibility(View.GONE);

            switch (toolbarType) {
                case "default":
                    tempDeleteButton.setVisibility(View.VISIBLE);
                    break;
                case "item":
                    tempDeleteButton.setVisibility(View.VISIBLE);
                    tempListButton.setVisibility(View.VISIBLE);
                    break;
                case "section":
                    tempAddButton.setVisibility(View.VISIBLE);
                    tempCopyButton.setVisibility(View.VISIBLE);
                    tempEditButton.setVisibility(View.VISIBLE);
                    tempSwapButton.setVisibility(View.VISIBLE);
                    tempDeleteButton.setVisibility(View.VISIBLE);
                    break;
            }
        }
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
            text.setText(title);
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
}
