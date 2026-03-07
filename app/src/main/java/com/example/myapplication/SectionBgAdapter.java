package com.example.myapplication;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;

import java.util.ArrayList;
import java.util.List;

public class SectionBgAdapter extends RecyclerView.Adapter<SectionBgAdapter.ViewHolder> {
    List<SectionBgItem> items;
    BottomSheetDialog dialog;
    MainActivity activity;

    public SectionBgAdapter(List<SectionBgItem> items, BottomSheetDialog dialog, MainActivity activity) {
        this.items = items;
        this.dialog = dialog;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_section_bg_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            SectionBgItem item = items.get(position);
            holder.lbl.setText(item.name);
            holder.chk.setChecked(item.isSelected);

            // Header Background Image Logic (Available for all sections now, or as per logic)
            // The requirement is to remove the "xml panel" (sliders), but keep the image button if applicable.
            // Adjusting visibility logic:
            
            // Default hide everything
            if (holder.layoutBgImage != null) holder.layoutBgImage.setVisibility(View.GONE);
            if (holder.layoutBlankSectionControls != null) holder.layoutBlankSectionControls.setVisibility(View.GONE);
            if (holder.layoutStickSectionControls != null) holder.layoutStickSectionControls.setVisibility(View.GONE);

            // Stick Section Logic Isolation
            if (item.isStick) {
                if (holder.layoutStickSectionControls != null) {
                    holder.layoutStickSectionControls.setVisibility(View.VISIBLE);
                    
                    if (holder.containerStick != null) {
                        holder.containerStick.setVisibility(View.VISIBLE);
                    }

                    // Tab Switching Logic
                    View.OnClickListener tabListener = v -> {
                        boolean isStyle = v.getId() == R.id.tab_stick_style;
                        
                        holder.tabStickStyle.setBackgroundColor(Color.parseColor(isStyle ? "#FFFFFF" : "#F5F5F5"));
                        holder.tabStickStyle.setTextColor(Color.parseColor(isStyle ? "#000000" : "#888888"));
                        
                        holder.tabStickGeneral.setBackgroundColor(Color.parseColor(!isStyle ? "#FFFFFF" : "#F5F5F5"));
                        holder.tabStickGeneral.setTextColor(Color.parseColor(!isStyle ? "#000000" : "#888888"));
                        
                        holder.layoutStickStyleTab.setVisibility(isStyle ? View.VISIBLE : View.GONE);
                        holder.layoutStickGeneralTab.setVisibility(!isStyle ? View.VISIBLE : View.GONE);
                    };
                    if (holder.tabStickStyle != null) {
                        holder.tabStickStyle.setOnClickListener(tabListener);
                        holder.tabStickGeneral.setOnClickListener(tabListener);
                        // Default to Style Tab
                        tabListener.onClick(holder.tabStickStyle);
                    }

                    // --- STYLE TAB ---
                    // Populate Shape Carousels
                    if (holder.listStickLeftShapes != null) populateStickShapes(holder.listStickLeftShapes, item, "left", this);
                    if (holder.listStickMidShapes != null) populateStickShapes(holder.listStickMidShapes, item, "mid", this);
                    if (holder.listStickRightShapes != null) populateStickShapes(holder.listStickRightShapes, item, "right", this);
                    if (holder.seekStickLeftH != null) {
                        holder.seekStickLeftH.setProgress(item.stickLeftH);
                        holder.seekStickLeftH.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (fromUser) {
                                    item.stickLeftH = progress;
                                    if (item.stickLeftProp && holder.seekStickLeftW != null) {
                                        item.stickLeftW = progress;
                                        holder.seekStickLeftW.setProgress(progress);
                                    }
                                    updateStickSize(item, "left", item.stickLeftW, item.stickLeftH, item.stickLeftProp);
                                }
                            }
                            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                        });
                    }
                    if (holder.seekStickLeftW != null) {
                        holder.seekStickLeftW.setProgress(item.stickLeftW);
                        holder.seekStickLeftW.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (fromUser) {
                                    item.stickLeftW = progress;
                                    if (item.stickLeftProp && holder.seekStickLeftH != null) {
                                        item.stickLeftH = progress;
                                        holder.seekStickLeftH.setProgress(progress);
                                    }
                                    updateStickSize(item, "left", item.stickLeftW, item.stickLeftH, item.stickLeftProp);
                                }
                            }
                            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                        });
                    }
                    if (holder.seekStickLeftRot != null) {
                        holder.seekStickLeftRot.setProgress(item.stickLeftRot);
                        holder.seekStickLeftRot.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if(fromUser) { item.stickLeftRot = progress; updateStickTargetRotation(item, "left", progress); }
                            }
                            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                        });
                    }
                    if (holder.chkStickLeftProp != null) {
                        holder.chkStickLeftProp.setChecked(item.stickLeftProp);
                        holder.chkStickLeftProp.setOnCheckedChangeListener((btn, isChecked) -> {
                            item.stickLeftProp = isChecked;
                            if (isChecked) {
                                item.stickLeftW = item.stickLeftH;
                                if (holder.seekStickLeftW != null) holder.seekStickLeftW.setProgress(item.stickLeftH);
                                updateStickSize(item, "left", item.stickLeftW, item.stickLeftH, true);
                            }
                        });
                    }
                    if (holder.btnStickLeftColor != null) {
                        try { holder.btnStickLeftColor.setBackgroundColor(Color.parseColor(item.stickLeftColor)); } catch(Exception e){}
                        holder.btnStickLeftColor.setOnClickListener(v -> {
                            activity.pendingColorItem = item;
                            activity.pendingColorAdapter = this;
                            activity.pendingColorTarget = "left";
                            int col = Color.BLACK; try { col = Color.parseColor(item.stickLeftColor); } catch(Exception e){}
                            ColorPickerDialog.newBuilder().setDialogId(MainActivity.SECTION_BG_COLOR_ID).setColor(col).setShowAlphaSlider(true).show(activity);
                        });
                    }

                    // Middle Column
                    if (holder.listStickMidShapes != null && holder.listStickMidShapes.getChildCount() == 0) {
                        populateStickShapes(holder.listStickMidShapes, item, "mid", this);
                    }
                    if (holder.seekStickMidH != null) {
                        holder.seekStickMidH.setProgress(item.stickMidH);
                        holder.seekStickMidH.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (fromUser) {
                                    item.stickMidH = progress;
                                    if (item.stickMidProp && holder.seekStickWidth != null) {
                                        item.stickMidW = progress;
                                        holder.seekStickWidth.setProgress(progress);
                                    }
                                    updateStickSize(item, "mid", item.stickMidW, item.stickMidH, item.stickMidProp);
                                }
                            }
                            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                        });
                    }
                    if (holder.seekStickWidth != null) { // This is Mid Width
                        holder.seekStickWidth.setProgress(item.stickMidW);
                        holder.seekStickWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (fromUser) {
                                    item.stickMidW = progress;
                                    if (item.stickMidProp && holder.seekStickMidH != null) {
                                        item.stickMidH = progress;
                                        holder.seekStickMidH.setProgress(progress);
                                    }
                                    updateStickSize(item, "mid", item.stickMidW, item.stickMidH, item.stickMidProp);
                                }
                            }
                            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                        });
                    }
                    if (holder.seekStickMidRot != null) {
                        holder.seekStickMidRot.setProgress(item.stickMidRot);
                        holder.seekStickMidRot.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if(fromUser) { item.stickMidRot = progress; updateStickTargetRotation(item, "mid", progress); }
                            }
                            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                        });
                    }
                    if (holder.chkStickMidProp != null) {
                        holder.chkStickMidProp.setChecked(item.stickMidProp);
                        holder.chkStickMidProp.setOnCheckedChangeListener((btn, isChecked) -> {
                            item.stickMidProp = isChecked;
                            if (isChecked) {
                                item.stickMidW = item.stickMidH;
                                if (holder.seekStickWidth != null) holder.seekStickWidth.setProgress(item.stickMidH);
                                updateStickSize(item, "mid", item.stickMidW, item.stickMidH, true);
                            }
                        });
                    }
                    if (holder.btnStickMidColor != null) {
                        try { holder.btnStickMidColor.setBackgroundColor(Color.parseColor(item.stickMidColor)); } catch(Exception e){}
                        holder.btnStickMidColor.setOnClickListener(v -> {
                            activity.pendingColorItem = item;
                            activity.pendingColorAdapter = this;
                            activity.pendingColorTarget = "mid";
                            int col = Color.BLACK; try { col = Color.parseColor(item.stickMidColor); } catch(Exception e){}
                            ColorPickerDialog.newBuilder().setDialogId(MainActivity.SECTION_BG_COLOR_ID).setColor(col).setShowAlphaSlider(true).show(activity);
                        });
                    }

                    // Right Column
                    if (holder.listStickRightShapes != null && holder.listStickRightShapes.getChildCount() == 0) {
                        populateStickShapes(holder.listStickRightShapes, item, "right", this);
                    }
                    if (holder.seekStickRightH != null) {
                        holder.seekStickRightH.setProgress(item.stickRightH);
                        holder.seekStickRightH.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (fromUser) {
                                    item.stickRightH = progress;
                                    if (item.stickRightProp && holder.seekStickRightW != null) {
                                        item.stickRightW = progress;
                                        holder.seekStickRightW.setProgress(progress);
                                    }
                                    updateStickSize(item, "right", item.stickRightW, item.stickRightH, item.stickRightProp);
                                }
                            }
                            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                        });
                    }
                    if (holder.seekStickRightW != null) {
                        holder.seekStickRightW.setProgress(item.stickRightW);
                        holder.seekStickRightW.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (fromUser) {
                                    item.stickRightW = progress;
                                    if (item.stickRightProp && holder.seekStickRightH != null) {
                                        item.stickRightH = progress;
                                        holder.seekStickRightH.setProgress(progress);
                                    }
                                    updateStickSize(item, "right", item.stickRightW, item.stickRightH, item.stickRightProp);
                                }
                            }
                            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                        });
                    }
                    if (holder.seekStickRightRot != null) {
                        holder.seekStickRightRot.setProgress(item.stickRightRot);
                        holder.seekStickRightRot.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if(fromUser) { item.stickRightRot = progress; updateStickTargetRotation(item, "right", progress); }
                            }
                            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                        });
                    }
                    if (holder.chkStickRightProp != null) {
                        holder.chkStickRightProp.setChecked(item.stickRightProp);
                        holder.chkStickRightProp.setOnCheckedChangeListener((btn, isChecked) -> {
                            item.stickRightProp = isChecked;
                            if (isChecked) {
                                item.stickRightW = item.stickRightH;
                                if (holder.seekStickRightW != null) holder.seekStickRightW.setProgress(item.stickRightH);
                                updateStickSize(item, "right", item.stickRightW, item.stickRightH, true);
                            }
                        });
                    }
                    if (holder.btnStickRightColor != null) {
                        try { holder.btnStickRightColor.setBackgroundColor(Color.parseColor(item.stickRightColor)); } catch(Exception e){}
                        holder.btnStickRightColor.setOnClickListener(v -> {
                            activity.pendingColorItem = item;
                            activity.pendingColorAdapter = this;
                            activity.pendingColorTarget = "right";
                            int col = Color.BLACK; try { col = Color.parseColor(item.stickRightColor); } catch(Exception e){}
                            ColorPickerDialog.newBuilder().setDialogId(MainActivity.SECTION_BG_COLOR_ID).setColor(col).setShowAlphaSlider(true).show(activity);
                        });
                    }

                    // --- GENERAL TAB ---
                    if (holder.seekStickZIndex != null) {
                        holder.seekStickZIndex.setProgress(item.zIndex);
                        holder.seekStickZIndex.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (fromUser) { item.zIndex = progress; updateStickZIndex(item, progress); }
                            }
                            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                        });
                    }
                    if (holder.seekStickMarginTop != null) {
                        holder.seekStickMarginTop.setProgress(item.marginTop);
                        holder.seekStickMarginTop.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (fromUser) { item.marginTop = progress; updateStickMarginTop(item, progress); }
                            }
                            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                        });
                    }
                    if (holder.seekStickMarginBottom != null) {
                        holder.seekStickMarginBottom.setProgress(item.marginBottom);
                        holder.seekStickMarginBottom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (fromUser) { item.marginBottom = progress; updateStickMarginBottom(item, progress); }
                            }
                            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                        });
                    }
                    if (holder.seekStickXAxis != null) {
                        holder.seekStickXAxis.setProgress(item.xAxis);
                        holder.seekStickXAxis.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (fromUser) { item.xAxis = progress; updateStickXAxis(item, progress); }
                            }
                            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                        });
                    }
                    if (holder.seekStickLineThickness != null) {
                        holder.seekStickLineThickness.setProgress(item.stickLineThickness);
                        holder.seekStickLineThickness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (fromUser) {
                                    item.stickLineThickness = progress;
                                    if (activity.getWebView() != null) {
                                        activity.getWebView().evaluateJavascript("if(window.updateStickLineThickness) { window.updateStickLineThickness('"+item.id+"', "+progress+"); }", null);
                                    }
                                }
                            }
                            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                        });
                    }
                    if (holder.btnStickLineColor != null) {
                        try { holder.btnStickLineColor.setBackgroundColor(Color.parseColor(item.stickLineColor)); } catch(Exception e){}
                        holder.btnStickLineColor.setOnClickListener(v -> {
                            activity.pendingColorItem = item;
                            activity.pendingColorAdapter = SectionBgAdapter.this;
                            activity.pendingColorTarget = "line";
                            int col = Color.BLACK;
                            try { col = Color.parseColor(item.stickLineColor); } catch(Exception e){}
                            ColorPickerDialog.newBuilder().setDialogId(MainActivity.SECTION_BG_COLOR_ID).setColor(col).setShowAlphaSlider(true).show(activity);
                        });
                    }
                    if (holder.seekStickRotation != null) {
                        holder.seekStickRotation.setProgress(item.rotation);
                        holder.seekStickRotation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (fromUser) { item.rotation = progress; updateStickGlobalRotation(item, progress); }
                            }
                            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                        });
                    }
                    if (holder.seekStickRadius != null) {
                        holder.seekStickRadius.setProgress(item.radius);
                        holder.seekStickRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (fromUser) { item.radius = progress; updateStickRadius(item, progress); }
                            }
                            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                        });
                    }

                }
            } 
            // Header & Blank Logic Combined Where Shared
            else if (item.id.equals("mainHeader") || item.isBlank) {
                
                // Background Image controls are shared for mainHeader and blank sections
                if (holder.layoutBgImage != null) {
                    holder.layoutBgImage.setVisibility(View.VISIBLE);
                    if (holder.btnBgImage != null) {
                        holder.btnBgImage.setOnClickListener(v -> {
                            if (activity != null) {
                                 if (item.id.equals("mainHeader")) {
                                     activity.pickHeaderImage();
                                 } else {
                                     activity.pickSectionBg(item.id);
                                 }
                            }
                        });
                        
                        if (item.isBlank) holder.btnBgImage.setText("Set Section Background Image");
                        else holder.btnBgImage.setText("Set Header Background Image");
                    }
                    if (holder.btnRemoveBgImage != null) {
                        holder.btnRemoveBgImage.setOnClickListener(v -> {
                            if (activity != null && activity.getWebView() != null) {
                                if (item.id.equals("mainHeader")) {
                                    // Assuming updateHeaderBgImage takes (dataUrl, stretch)
                                    activity.getWebView().evaluateJavascript("if(window.updateHeaderBgImage) window.updateHeaderBgImage('', false);", null);
                                } else {
                                    activity.getWebView().evaluateJavascript("if(window.updateSectionBgImage) window.updateSectionBgImage('"+item.id+"', '');", null);
                                }
                            }
                        });
                    }
                }
                
                // Image Mode Button Logic
                if (holder.btnImgFill != null) {
                    updateImageModeButtonUI(holder, item.imageMode);
                    holder.btnImgFill.setOnClickListener(v -> { item.imageMode = "cover"; updateImageMode(item, "cover"); updateImageModeButtonUI(holder, "cover"); });
                    holder.btnImgFit.setOnClickListener(v -> { item.imageMode = "contain"; updateImageMode(item, "contain"); updateImageModeButtonUI(holder, "contain"); });
                    holder.btnImgCenter.setOnClickListener(v -> { item.imageMode = "center"; updateImageMode(item, "center"); updateImageModeButtonUI(holder, "center"); });
                }

                // Blank section EXCLUSIVE controls
                if (item.isBlank && holder.layoutBlankSectionControls != null) {
                    holder.layoutBlankSectionControls.setVisibility(View.VISIBLE);

                    setupCategory(holder.headerTransform, holder.containerTransform, holder.txtStatusTransform);
                    setupCategory(holder.headerGeometry, holder.containerGeometry, holder.txtStatusGeometry);
                    setupCategory(holder.headerHole, holder.containerHole, holder.txtStatusHole);
                    setupCategory(holder.headerAppearance, holder.containerAppearance, holder.txtStatusAppearance);

                    // Force initial visibility states
                    holder.containerTransform.setVisibility(View.VISIBLE);
                    holder.txtStatusTransform.setText("▾");
                    holder.containerGeometry.setVisibility(View.GONE);
                    holder.txtStatusGeometry.setText("▸");
                    holder.containerHole.setVisibility(View.GONE);
                    holder.txtStatusHole.setText("▸");
                    holder.containerAppearance.setVisibility(View.GONE);
                    holder.txtStatusAppearance.setText("▸");
                    
                    if (holder.seekBlankSize != null) {
                        holder.seekBlankSize.setProgress(item.opacity > 0 ? item.opacity : 100); 
                        holder.seekBlankSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (fromUser) { item.opacity = progress; updateBlankSize(item, progress); }
                            }
                            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                        });
                    }
                    if (holder.seekBlankRadius != null) {
                        holder.seekBlankRadius.setProgress(item.radius);
                        holder.seekBlankRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (fromUser) { item.radius = progress; updateRadius(item, progress); }
                            }
                            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                        });
                    }
                    if (holder.seekBlankBlur != null) {
                        holder.seekBlankBlur.setProgress(item.blur);
                        holder.seekBlankBlur.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (fromUser) { item.blur = progress; updateBlur(item, progress); }
                            }
                            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                        });
                    }
                    if (holder.seekBlankZIndex != null) {
                        holder.seekBlankZIndex.setProgress(item.zIndex);
                        holder.seekBlankZIndex.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { if (fromUser) { item.zIndex = progress; updateZIndex(item, progress); } }
                            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                        });
                    }
                    if (holder.seekBlankX != null) {
                        holder.seekBlankX.setProgress(item.x);
                        holder.seekBlankX.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { if (fromUser) { item.x = progress; updateSectionX(item, progress); } }
                            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                        });
                    }
                    if (holder.seekBlankY != null) {
                        holder.seekBlankY.setProgress(item.y);
                        holder.seekBlankY.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { if (fromUser) { item.y = progress; updateSectionY(item, progress); } }
                            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                        });
                    }
                    if (holder.seekBlankRotation != null) {
                        holder.seekBlankRotation.setProgress(item.rotation);
                        holder.seekBlankRotation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { if (fromUser) { item.rotation = progress; updateSectionRotation(item, progress); } }
                            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                        });
                    }
                    if (holder.seekBlankHole != null) {
                        holder.seekBlankHole.setProgress(item.holeSize);
                        holder.seekBlankHole.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { if (fromUser) { item.holeSize = progress; updateSectionHole(item, progress); } }
                            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                        });
                    }
                    if (holder.seekBlankSmoothing != null) {
                        holder.seekBlankSmoothing.setProgress(item.smoothing);
                        holder.seekBlankSmoothing.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { if (fromUser) { item.smoothing = progress; updateSectionSmoothing(item, progress); } }
                            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                        });
                    }
                    if (holder.btnShapeRect != null) {
                        updateShapeButtonUI(holder, item.shapeType);
                        holder.btnShapeRect.setOnClickListener(v -> { item.shapeType = "rectangle"; updateSectionShape(item, "rectangle"); updateShapeButtonUI(holder, "rectangle"); });
                        holder.btnShapeCircle.setOnClickListener(v -> { item.shapeType = "circle"; updateSectionShape(item, "circle"); updateShapeButtonUI(holder, "circle"); });
                        holder.btnShapeTri.setOnClickListener(v -> { item.shapeType = "triangle"; updateSectionShape(item, "triangle"); updateShapeButtonUI(holder, "triangle"); });
                        holder.btnShapeHex.setOnClickListener(v -> { item.shapeType = "hexagon"; updateSectionShape(item, "hexagon"); updateShapeButtonUI(holder, "hexagon"); });
                    }
                    if (holder.btnHoleShapeRect != null) {
                        updateHoleShapeButtonUI(holder, item.holeShapeType);
                        holder.btnHoleShapeRect.setOnClickListener(v -> { item.holeShapeType = "rectangle"; updateSectionHoleShape(item, "rectangle"); updateHoleShapeButtonUI(holder, "rectangle"); });
                        holder.btnHoleShapeCircle.setOnClickListener(v -> { item.holeShapeType = "circle"; updateSectionHoleShape(item, "circle"); updateHoleShapeButtonUI(holder, "circle"); });
                        holder.btnHoleShapeTri.setOnClickListener(v -> { item.holeShapeType = "triangle"; updateSectionHoleShape(item, "triangle"); updateHoleShapeButtonUI(holder, "triangle"); });
                        holder.btnHoleShapeHex.setOnClickListener(v -> { item.holeShapeType = "hexagon"; updateSectionHoleShape(item, "hexagon"); updateHoleShapeButtonUI(holder, "hexagon"); });
                    }

                    // --- Blank Section Image Mode Buttons (Appearance Category) ---
                    if (holder.btnBlankImgFill != null) {
                        updateImageModeButtonUI(holder, item.imageMode);
                        holder.btnBlankImgFill.setOnClickListener(v -> { item.imageMode = "cover"; updateImageMode(item, "cover"); updateImageModeButtonUI(holder, "cover"); });
                        holder.btnBlankImgFit.setOnClickListener(v -> { item.imageMode = "contain"; updateImageMode(item, "contain"); updateImageModeButtonUI(holder, "contain"); });
                        holder.btnImgCenter.setOnClickListener(v -> { item.imageMode = "center"; updateImageMode(item, "center"); updateImageModeButtonUI(holder, "center"); });
                    }
                }
            }

            // Color and Logic Listeners (Apply to ALL items except special ones)
            if (item.id.equals("mainHeader") || item.isBlank || item.isStick) {
                holder.itemView.setOnClickListener(null);
                holder.chk.setVisibility(View.GONE);
            } else {
                holder.chk.setVisibility(View.VISIBLE);
                holder.itemView.setOnClickListener(v -> {
                    activity.pendingColorItem = item;
                    activity.pendingColorAdapter = SectionBgAdapter.this;
    
                    int col = Color.WHITE;
                    try { col = Color.parseColor(item.color); } catch(Exception e){}
    
                    ColorPickerDialog.newBuilder()
                            .setDialogId((activity != null && activity instanceof MainActivity) ? 200 : 200) // 200 is SECTION_BG_COLOR_ID
                            .setColor(col)
                            .setShowAlphaSlider(true)
                            .show(activity);
                });
            }

            holder.chk.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.isSelected = isChecked;
            });

        } catch (Exception e) {
            android.util.Log.e("SectionBgAdapter", "Error binding view", e);
        }
    }

    private void updateFontSize(SectionBgItem item, int val) {
        if (item.id.equals("mainHeader")) return;
        if (activity.getWebView() != null) {
            String js = "if(document.getElementById('"+item.id+"')) { " +
                    "document.getElementById('"+item.id+"').style.fontSize = '" + val + "px'; " +
                    "}";
            activity.getWebView().evaluateJavascript(js, null);
        }
    }

    private void updateOpacity(SectionBgItem item, int val) {
        if (activity.getWebView() != null) {
            double op = val / 100.0;
            String js = "if(document.getElementById('"+item.id+"')) { " +
                    "document.getElementById('"+item.id+"').style.setProperty('--item-opa', " + op + "); " +
                    "}";
            activity.getWebView().evaluateJavascript(js, null);
        }
    }

    private void updateLineHeight(SectionBgItem item, float val) {
        if (activity.getWebView() != null) {
            String js = "if(document.getElementById('"+item.id+"')) { " +
                    "document.getElementById('"+item.id+"').style.lineHeight = '" + val + "'; " +
                    "}";
            activity.getWebView().evaluateJavascript(js, null);
        }
    }

    private void updateRadius(SectionBgItem item, int val) {
        if (activity.getWebView() != null) {
            String js = "if(document.getElementById('"+item.id+"')) { " +
                    "if(window.updateSectionRadius) { window.updateSectionRadius('"+item.id+"', "+val+"); } else {" +
                    "document.getElementById('"+item.id+"').style.borderRadius = '" + val + "px'; " +
                    "}" +
                    "}";
            activity.getWebView().evaluateJavascript(js, null);
        }
    }

    private void updateBlur(SectionBgItem item, int val) {
        if (activity.getWebView() != null) {
            String js = "if(window.updateSectionBlur) { window.updateSectionBlur('"+item.id+"', "+val+"); }";
            activity.getWebView().evaluateJavascript(js, null);
        }
    }

    private void updateZIndex(SectionBgItem item, int val) {
        if (activity.getWebView() != null) {
            String js = "if(window.updateSectionZIndex) { window.updateSectionZIndex('"+item.id+"', "+val+"); }";
            activity.getWebView().evaluateJavascript(js, null);
        }
    }

    public void updateStickColor(SectionBgItem item, int colorInt, String target) {
        String hex = String.format("#%06X%02X", (0xFFFFFF & colorInt), (colorInt >>> 24));
        if (target.equals("left")) item.stickLeftColor = hex;
        else if (target.equals("mid")) item.stickMidColor = hex;
        else if (target.equals("right")) item.stickRightColor = hex;
        else if (target.equals("line")) {
            item.stickLineColor = hex;
            notifyDataSetChanged();
            if (activity.getWebView() != null) {
                activity.getWebView().evaluateJavascript("if(window.updateStickLineColor) { window.updateStickLineColor('"+item.id+"', '"+hex+"'); }", null);
            }
            return;
        }
        notifyDataSetChanged();
        if (activity.getWebView() != null) {
            String js = "if(window.updateStickColor) { window.updateStickColor('"+item.id+"', '"+target+"', '"+hex+"'); }";
            activity.getWebView().evaluateJavascript(js, null);
        }
    }

    public void updateColor(SectionBgItem item, int colorInt) {
        // CSS expects #RRGGBBAA, but Android uses #AARRGGBB. Swap them.
        String hex = String.format("#%06X%02X", (0xFFFFFF & colorInt), (colorInt >>> 24));
        item.color = hex;
        notifyDataSetChanged();
        if (activity.getWebView() != null) {
            if (item.isMaster) {
                activity.getWebView().evaluateJavascript("if(document.getElementById('"+item.id+"')) { document.getElementById('"+item.id+"').style.backgroundColor = '"+hex+"'; }", null);
                for(SectionBgItem sub : items) {
                    if(sub != item) {
                        sub.color = hex;
                        if (sub.isStick) {
                            // For stick sections, the background color changes the line color
                            sub.stickLineColor = hex;
                            activity.getWebView().evaluateJavascript("if(window.updateStickLineColor) { window.updateStickLineColor('"+sub.id+"', '"+hex+"'); }", null);
                        } else {
                            activity.getWebView().evaluateJavascript("if(document.getElementById('"+sub.id+"')) { document.getElementById('"+sub.id+"').style.setProperty('--item-bg', '"+hex+"'); }", null);
                        }
                    }
                }
                notifyDataSetChanged();
            } else if (item.isStick) {
                // For stick sections, the background color changes the line color
                item.stickLineColor = hex;
                activity.getWebView().evaluateJavascript("if(window.updateStickLineColor) { window.updateStickLineColor('"+item.id+"', '"+hex+"'); }", null);
            } else {
                activity.getWebView().evaluateJavascript("if(document.getElementById('"+item.id+"')) { document.getElementById('"+item.id+"').style.setProperty('--item-bg', '"+hex+"'); }", null);
            }
        }
    }

    private void updateBlankSize(SectionBgItem item, int val) {
        if (activity.getWebView() != null) {
            String js = "if(window.updateSectionSize) { window.updateSectionSize('"+item.id+"', "+val+"); }";
            activity.getWebView().evaluateJavascript(js, null);
        }
    }


    private void updateSectionX(SectionBgItem item, int val) {
        if (activity.getWebView() != null) {
            String js = "if(window.updateSectionX) { window.updateSectionX('"+item.id+"', "+val+"); }";
            activity.getWebView().evaluateJavascript(js, null);
        }
    }

    private void updateSectionY(SectionBgItem item, int val) {
        if (activity.getWebView() != null) {
            String js = "if(window.updateSectionY) { window.updateSectionY('"+item.id+"', "+val+"); }";
            activity.getWebView().evaluateJavascript(js, null);
        }
    }

    private void updateSectionRotation(SectionBgItem item, int val) {
        if (activity.getWebView() != null) {
            String js = "if(window.updateSectionRotation) { window.updateSectionRotation('"+item.id+"', "+val+"); }";
            activity.getWebView().evaluateJavascript(js, null);
        }
    }

    private void updateSectionShape(SectionBgItem item, String shape) {
        if (activity.getWebView() != null) {
            String js = "if(window.updateSectionShape) { window.updateSectionShape('"+item.id+"', '"+shape+"'); }";
            activity.getWebView().evaluateJavascript(js, null);
        }
    }

    private void updateSectionHole(SectionBgItem item, int val) {
        if (activity.getWebView() != null) {
            String js = "if(window.updateSectionHole) { window.updateSectionHole('"+item.id+"', "+val+"); }";
            activity.getWebView().evaluateJavascript(js, null);
        }
    }

    private void updateSectionHoleShape(SectionBgItem item, String shape) {
        if (activity.getWebView() != null) {
            String js = "if(window.updateSectionHoleShape) { window.updateSectionHoleShape('"+item.id+"', '"+shape+"'); }";
            activity.getWebView().evaluateJavascript(js, null);
        }
    }

    private void updateSectionSmoothing(SectionBgItem item, int val) {
        if (activity.getWebView() != null) {
            String js = "if(window.updateSectionSmoothing) { window.updateSectionSmoothing('"+item.id+"', "+val+"); }";
            activity.getWebView().evaluateJavascript(js, null);
        }
    }

    private void updateStickHeight(SectionBgItem item, int val) {
        if (activity.getWebView() != null) {
            String js = "if(window.updateStickHeight) { window.updateStickHeight('"+item.id+"', "+val+"); }";
            activity.getWebView().evaluateJavascript(js, null);
        }
    }

    private void updateStickWidth(SectionBgItem item, int val) {
        if (activity.getWebView() != null) {
            String js = "if(window.updateStickWidth) { window.updateStickWidth('"+item.id+"', "+val+"); }";
            activity.getWebView().evaluateJavascript(js, null);
        }
    }

    private void updateStickRotation(SectionBgItem item, int val) {
        if (activity.getWebView() != null) {
            String js = "if(window.updateStickRotation) { window.updateStickRotation('"+item.id+"', "+val+"); }";
            activity.getWebView().evaluateJavascript(js, null);
        }
    }

    private void updateStickZIndex(SectionBgItem item, int val) {
        if (activity.getWebView() != null) {
            String js = "if(window.updateStickZIndex) { window.updateStickZIndex('"+item.id+"', "+val+"); }";
            activity.getWebView().evaluateJavascript(js, null);
        }
    }

    private String getStickShapeName(int shapeId) {
        switch (shapeId) {
            case 1: return "Circle";
            case 2: return "Triangle";
            case 3: return "Square";
            default: return "None";
        }
    }

    private void updateStickRadius(SectionBgItem item, int val) {
        if (activity.getWebView() != null) {
            String js = "if(window.updateStickRadius) { window.updateStickRadius('"+item.id+"', "+val+"); }";
            activity.getWebView().evaluateJavascript(js, null);
        }
    }

    private void populateStickShapes(LinearLayout container, SectionBgItem item, String target, SectionBgAdapter adapter) {
        container.removeAllViews();
        String[] shapes = {"∅", "⬛", "⬤", "📐", "⬟", "▶", "▼", "➖"};
        int activeShape = target.equals("left") ? item.stickLeftShape : (target.equals("mid") ? item.stickMidShape : item.stickRightShape);

        // Add top spacer to allow centering first item
        int spacerHeight = (int) (60 * adapter.activity.getResources().getDisplayMetrics().density);
        View topSpacer = new View(adapter.activity);
        topSpacer.setLayoutParams(new LinearLayout.LayoutParams(1, spacerHeight));
        container.addView(topSpacer);

        final ArrayList<TextView> views = new ArrayList<>();
        for (int i = 0; i < shapes.length; i++) {
            TextView tv = new TextView(adapter.activity);
            tv.setText(shapes[i]);
            tv.setTextSize(24);
            tv.setPadding(0, 16, 0, 16);
            tv.setGravity(android.view.Gravity.CENTER);
            tv.setTextColor(Color.BLACK);
            
            int finalI = i;
            tv.setOnClickListener(v -> {
                if (target.equals("left")) item.stickLeftShape = finalI;
                else if (target.equals("mid")) item.stickMidShape = finalI;
                else if (target.equals("right")) item.stickRightShape = finalI;
                
                // Center the clicked item
                View parentView = (View) container.getParent();
                if (parentView instanceof ScrollView) {
                    ScrollView sv = (ScrollView) parentView;
                    sv.smoothScrollTo(0, tv.getTop() - sv.getHeight()/2 + tv.getHeight()/2);
                }
                
                adapter.updateStickShape(item, target, finalI);
            });

            container.addView(tv);
            views.add(tv);
        }

        // Add bottom spacer to allow centering last item
        View bottomSpacer = new View(adapter.activity);
        bottomSpacer.setLayoutParams(new LinearLayout.LayoutParams(1, spacerHeight));
        container.addView(bottomSpacer);

        // Scroll listener for focus/blur effect
        View parentView = (View) container.getParent();
        if (parentView instanceof ScrollView) {
            ScrollView sv = (ScrollView) parentView;

            sv.getViewTreeObserver().addOnScrollChangedListener(() -> {
                if (sv.getHeight() == 0) return;
                int centerY = sv.getScrollY() + sv.getHeight() / 2;
                for (int i = 0; i < views.size(); i++) {
                    TextView tv = views.get(i);
                    int viewCenterY = tv.getTop() + tv.getHeight() / 2;
                    float distance = Math.abs(centerY - viewCenterY);
                    float fraction = Math.max(0, 1 - (distance / (sv.getHeight() / 1.5f)));
                    
                    tv.setScaleX(0.6f + 0.4f * fraction);
                    tv.setScaleY(0.6f + 0.4f * fraction);
                    tv.setAlpha(0.3f + 0.7f * fraction);
                    
                    if (distance < tv.getHeight() / 2) {
                        tv.setTextColor(Color.parseColor("#2196F3")); // Highlight focused
                    } else {
                        tv.setTextColor(Color.BLACK);
                    }
                }
            });

            // Post scroll to active shape
            sv.post(() -> {
                if (activeShape >= 0 && activeShape < views.size()) {
                    TextView activeTv = views.get(activeShape);
                    sv.scrollTo(0, activeTv.getTop() - sv.getHeight()/2 + activeTv.getHeight()/2);
                }
            });

            // Snapping & Touch Logic
            sv.setOnTouchListener(new View.OnTouchListener() {
                private Runnable snapRunnable = () -> {
                    if (sv.getHeight() == 0) return;
                    int centerY = sv.getScrollY() + sv.getHeight() / 2;
                    TextView closest = null;
                    int minDistance = Integer.MAX_VALUE;
                    int closestIdx = -1;
                    
                    for (int i = 0; i < views.size(); i++) {
                        TextView tv = views.get(i);
                        int viewCenterY = tv.getTop() + tv.getHeight() / 2;
                        int dist = Math.abs(centerY - viewCenterY);
                        if (dist < minDistance) {
                            minDistance = dist;
                            closest = tv;
                            closestIdx = i;
                        }
                    }
                    
                    if (closest != null) {
                        sv.smoothScrollTo(0, closest.getTop() - sv.getHeight()/2 + closest.getHeight()/2);
                        if (target.equals("left")) item.stickLeftShape = closestIdx;
                        else if (target.equals("mid")) item.stickMidShape = closestIdx;
                        else if (target.equals("right")) item.stickRightShape = closestIdx;
                        adapter.updateStickShape(item, target, closestIdx);
                    }
                };

                @Override
                public boolean onTouch(View v, android.view.MotionEvent event) {
                    // Disable BottomSheet dragging while touching the carousel
                    if (adapter.dialog != null) {
                        View bottomSheet = adapter.dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                        if (bottomSheet != null) {
                            com.google.android.material.bottomsheet.BottomSheetBehavior<?> behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet);
                            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN || event.getAction() == android.view.MotionEvent.ACTION_MOVE) {
                                behavior.setDraggable(false);
                            } else if (event.getAction() == android.view.MotionEvent.ACTION_UP || event.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
                                behavior.setDraggable(true);
                            }
                        }
                    }
                    // Also disallow parent interception
                    v.getParent().requestDisallowInterceptTouchEvent(true);

                    if (event.getAction() == android.view.MotionEvent.ACTION_UP || event.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
                        sv.postDelayed(snapRunnable, 150);
                    }
                    return false;
                }
            });
        }
    }

    private void updateStickShape(SectionBgItem item, String target, int shapeId) {
        if (activity.getWebView() != null) {
            String js = "if(window.updateStickShape) { window.updateStickShape('"+item.id+"', '"+target+"', "+shapeId+"); }";
            activity.getWebView().evaluateJavascript(js, null);
        }
    }
    
    private void updateStickSize(SectionBgItem item, String target, int width, int height, boolean prop) {
        if (activity.getWebView() != null) {
            String js = "if(window.updateStickSize) { window.updateStickSize('"+item.id+"', '"+target+"', "+width+", "+height+", "+prop+"); }";
            activity.getWebView().evaluateJavascript(js, null);
        }
    }
    
    private void updateStickTargetRotation(SectionBgItem item, String target, int rot) {
        if (activity.getWebView() != null) {
            String js = "if(window.updateStickTargetRotation) { window.updateStickTargetRotation('"+item.id+"', '"+target+"', "+rot+"); }";
            activity.getWebView().evaluateJavascript(js, null);
        }
    }

    private void updateStickGlobalRotation(SectionBgItem item, int rot) {
        if (activity.getWebView() != null) {
            String js = "if(window.updateStickGlobalRotation) { window.updateStickGlobalRotation('"+item.id+"', "+rot+"); }";
            activity.getWebView().evaluateJavascript(js, null);
        }
    }

    private void updateStickMarginTop(SectionBgItem item, int val) {
        if (activity.getWebView() != null) {
            String js = "if(window.updateStickMarginTop) { window.updateStickMarginTop('"+item.id+"', "+val+"); }";
            activity.getWebView().evaluateJavascript(js, null);
        }
    }

    private void updateStickMarginBottom(SectionBgItem item, int val) {
        if (activity.getWebView() != null) {
            String js = "if(window.updateStickMarginBottom) { window.updateStickMarginBottom('"+item.id+"', "+val+"); }";
            activity.getWebView().evaluateJavascript(js, null);
        }
    }

    private void updateStickXAxis(SectionBgItem item, int val) {
        if (activity.getWebView() != null) {
            String js = "if(window.updateStickXAxis) { window.updateStickXAxis('"+item.id+"', "+val+"); }";
            activity.getWebView().evaluateJavascript(js, null);
        }
    }

    private void updateImageMode(SectionBgItem item, String mode) {
        if (activity.getWebView() != null) {
            String js;
            if (item.id.equals("mainHeader")) {
                js = "if(window.updateHeaderBgMode) { window.updateHeaderBgMode('"+mode+"'); }";
            } else {
                js = "if(window.updateSectionBgMode) { window.updateSectionBgMode('"+item.id+"', '"+mode+"'); }";
            }
            activity.getWebView().evaluateJavascript(js, null);
        }
    }

    private void updateImageModeButtonUI(ViewHolder holder, String mode) {
        int activeColor = Color.parseColor("#2196F3");
        int inactiveColor = Color.parseColor("#EEEEEE");
        int activeTextColor = Color.WHITE;
        int inactiveTextColor = Color.parseColor("#333333");

        holder.btnImgFill.setBackgroundTintList(android.content.res.ColorStateList.valueOf(mode.equals("cover") ? activeColor : inactiveColor));
        holder.btnImgFill.setTextColor(mode.equals("cover") ? activeTextColor : inactiveTextColor);

        holder.btnImgFit.setBackgroundTintList(android.content.res.ColorStateList.valueOf(mode.equals("contain") ? activeColor : inactiveColor));
        holder.btnImgFit.setTextColor(mode.equals("contain") ? activeTextColor : inactiveTextColor);

        holder.btnImgCenter.setBackgroundTintList(android.content.res.ColorStateList.valueOf(mode.equals("center") ? activeColor : inactiveColor));
        holder.btnImgCenter.setTextColor(mode.equals("center") ? activeTextColor : inactiveTextColor);

        // Also update the special blank image mode buttons if they exist
        if (holder.btnBlankImgFill != null) {
            holder.btnBlankImgFill.setBackgroundTintList(android.content.res.ColorStateList.valueOf(mode.equals("cover") ? activeColor : inactiveColor));
            holder.btnBlankImgFill.setTextColor(mode.equals("cover") ? activeTextColor : inactiveTextColor);
            holder.btnBlankImgFit.setBackgroundTintList(android.content.res.ColorStateList.valueOf(mode.equals("contain") ? activeColor : inactiveColor));
            holder.btnBlankImgFit.setTextColor(mode.equals("contain") ? activeTextColor : inactiveTextColor);
            holder.btnBlankImgCenter.setBackgroundTintList(android.content.res.ColorStateList.valueOf(mode.equals("center") ? activeColor : inactiveColor));
            holder.btnBlankImgCenter.setTextColor(mode.equals("center") ? activeTextColor : inactiveTextColor);
        }
    }

    private void updateShapeButtonUI(ViewHolder holder, String shape) {
        int activeColor = Color.parseColor("#4CAF50");
        int inactiveColor = Color.parseColor("#EEEEEE");
        int activeTextColor = Color.WHITE;
        int inactiveTextColor = Color.parseColor("#333333");

        holder.btnShapeRect.setBackgroundTintList(android.content.res.ColorStateList.valueOf(shape.equals("rectangle") ? activeColor : inactiveColor));
        holder.btnShapeRect.setTextColor(shape.equals("rectangle") ? activeTextColor : inactiveTextColor);

        holder.btnShapeCircle.setBackgroundTintList(android.content.res.ColorStateList.valueOf(shape.equals("circle") ? activeColor : inactiveColor));
        holder.btnShapeCircle.setTextColor(shape.equals("circle") ? activeTextColor : inactiveTextColor);

        holder.btnShapeTri.setBackgroundTintList(android.content.res.ColorStateList.valueOf(shape.equals("triangle") ? activeColor : inactiveColor));
        holder.btnShapeTri.setTextColor(shape.equals("triangle") ? activeTextColor : inactiveTextColor);

        holder.btnShapeHex.setBackgroundTintList(android.content.res.ColorStateList.valueOf(shape.equals("hexagon") ? activeColor : inactiveColor));
        holder.btnShapeHex.setTextColor(shape.equals("hexagon") ? activeTextColor : inactiveTextColor);
    }

    private void updateHoleShapeButtonUI(ViewHolder holder, String shape) {
        int activeColor = Color.parseColor("#FF9800");
        int inactiveColor = Color.parseColor("#EEEEEE");
        int activeTextColor = Color.WHITE;
        int inactiveTextColor = Color.parseColor("#333333");

        holder.btnHoleShapeRect.setBackgroundTintList(android.content.res.ColorStateList.valueOf(shape.equals("rectangle") ? activeColor : inactiveColor));
        holder.btnHoleShapeRect.setTextColor(shape.equals("rectangle") ? activeTextColor : inactiveTextColor);

        holder.btnHoleShapeCircle.setBackgroundTintList(android.content.res.ColorStateList.valueOf(shape.equals("circle") ? activeColor : inactiveColor));
        holder.btnHoleShapeCircle.setTextColor(shape.equals("circle") ? activeTextColor : inactiveTextColor);

        holder.btnHoleShapeTri.setBackgroundTintList(android.content.res.ColorStateList.valueOf(shape.equals("triangle") ? activeColor : inactiveColor));
        holder.btnHoleShapeTri.setTextColor(shape.equals("triangle") ? activeTextColor : inactiveTextColor);

        holder.btnHoleShapeHex.setBackgroundTintList(android.content.res.ColorStateList.valueOf(shape.equals("hexagon") ? activeColor : inactiveColor));
        holder.btnHoleShapeHex.setTextColor(shape.equals("hexagon") ? activeTextColor : inactiveTextColor);
    }

    private void setupCategory(View header, View container, TextView status) {
        header.setOnClickListener(v -> {
            if (container.getVisibility() == View.VISIBLE) {
                container.setVisibility(View.GONE);
                status.setText("▸");
            } else {
                container.setVisibility(View.VISIBLE);
                status.setText("▾");
            }
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox chk;
        TextView lbl;
        
        View layoutBgImage;
        Button btnBgImage;
        Button btnRemoveBgImage;

        View layoutBlankSectionControls;
        SeekBar seekBlankSize;
        SeekBar seekBlankRadius;
        SeekBar seekBlankBlur;
        SeekBar seekBlankZIndex;
        SeekBar seekBlankX;
        SeekBar seekBlankY;
        SeekBar seekBlankRotation;
        SeekBar seekBlankHole;
        SeekBar seekBlankSmoothing;

        View layoutStickSectionControls;
        View containerStick;
        
        TextView tabStickStyle, tabStickGeneral;
        View layoutStickStyleTab, layoutStickGeneralTab;

        ScrollView scrollStickLeft, scrollStickMid, scrollStickRight;
        LinearLayout listStickLeftShapes;
        SeekBar seekStickLeftH, seekStickLeftW, seekStickLeftRot;
        CheckBox chkStickLeftProp;
        ImageView btnStickLeftColor;

        LinearLayout listStickMidShapes;
        SeekBar seekStickMidH, seekStickWidth, seekStickMidRot;
        CheckBox chkStickMidProp;
        ImageView btnStickMidColor;

        LinearLayout listStickRightShapes;
        SeekBar seekStickRightH, seekStickRightW, seekStickRightRot;
        CheckBox chkStickRightProp;
        ImageView btnStickRightColor;

        SeekBar seekStickRotation, seekStickXAxis, seekStickMarginTop, seekStickMarginBottom, seekStickRadius, seekStickZIndex;
        SeekBar seekStickLineThickness;
        ImageView btnStickLineColor;

        Button btnImgFill, btnImgFit, btnImgCenter;
        Button btnBlankImgFill, btnBlankImgFit, btnBlankImgCenter;
        Button btnShapeRect, btnShapeCircle, btnShapeTri, btnShapeHex;
        Button btnHoleShapeRect, btnHoleShapeCircle, btnHoleShapeTri, btnHoleShapeHex;

        View headerTransform, headerGeometry, headerHole, headerAppearance;
        View containerTransform, containerGeometry, containerHole, containerAppearance;
        TextView txtStatusTransform, txtStatusGeometry, txtStatusHole, txtStatusAppearance;

        ViewHolder(View v) {
            super(v);
            chk = v.findViewById(R.id.chk_select);
            lbl = v.findViewById(R.id.txt_label);
            
            layoutBgImage = v.findViewById(R.id.layout_bg_image_control);
            btnBgImage = v.findViewById(R.id.btn_bg_image);
            btnRemoveBgImage = v.findViewById(R.id.btn_remove_bg_image);

            layoutBlankSectionControls = v.findViewById(R.id.layout_blank_section_controls);
            seekBlankSize = v.findViewById(R.id.seek_blank_size);
            seekBlankRadius = v.findViewById(R.id.seek_blank_radius);
            seekBlankBlur = v.findViewById(R.id.seek_blank_blur);
            seekBlankZIndex = v.findViewById(R.id.seek_blank_zindex);
            seekBlankX = v.findViewById(R.id.seek_blank_x);
            seekBlankY = v.findViewById(R.id.seek_blank_y);
            seekBlankRotation = v.findViewById(R.id.seek_blank_rotation);
            seekBlankHole = v.findViewById(R.id.seek_blank_hole);
            seekBlankSmoothing = v.findViewById(R.id.seek_blank_smoothing);

            layoutStickSectionControls = v.findViewById(R.id.layout_stick_section_controls);
            containerStick = v.findViewById(R.id.container_stick);
            
            tabStickStyle = v.findViewById(R.id.tab_stick_style);
            tabStickGeneral = v.findViewById(R.id.tab_stick_general);
            layoutStickStyleTab = v.findViewById(R.id.layout_stick_style_tab);
            layoutStickGeneralTab = v.findViewById(R.id.layout_stick_general_tab);

            scrollStickLeft = v.findViewById(R.id.scroll_stick_left);
            scrollStickMid = v.findViewById(R.id.scroll_stick_mid);
            scrollStickRight = v.findViewById(R.id.scroll_stick_right);

            listStickLeftShapes = v.findViewById(R.id.list_stick_left_shapes);
            seekStickLeftH = v.findViewById(R.id.seek_stick_left_h);
            seekStickLeftW = v.findViewById(R.id.seek_stick_left_w);
            seekStickLeftRot = v.findViewById(R.id.seek_stick_left_rot);
            chkStickLeftProp = v.findViewById(R.id.chk_stick_left_prop);
            btnStickLeftColor = v.findViewById(R.id.btn_stick_left_color);

            listStickMidShapes = v.findViewById(R.id.list_stick_mid_shapes);
            seekStickMidH = v.findViewById(R.id.seek_stick_mid_h);
            seekStickWidth = v.findViewById(R.id.seek_stick_width);
            seekStickMidRot = v.findViewById(R.id.seek_stick_mid_rot);
            chkStickMidProp = v.findViewById(R.id.chk_stick_mid_prop);
            btnStickMidColor = v.findViewById(R.id.btn_stick_mid_color);

            listStickRightShapes = v.findViewById(R.id.list_stick_right_shapes);
            seekStickRightH = v.findViewById(R.id.seek_stick_right_h);
            seekStickRightW = v.findViewById(R.id.seek_stick_right_w);
            seekStickRightRot = v.findViewById(R.id.seek_stick_right_rot);
            chkStickRightProp = v.findViewById(R.id.chk_stick_right_prop);
            btnStickRightColor = v.findViewById(R.id.btn_stick_right_color);

            seekStickRotation = v.findViewById(R.id.seek_stick_rotation);
            seekStickXAxis = v.findViewById(R.id.seek_stick_x_axis);
            seekStickMarginTop = v.findViewById(R.id.seek_stick_margin_top);
            seekStickMarginBottom = v.findViewById(R.id.seek_stick_margin_bottom);
            seekStickRadius = v.findViewById(R.id.seek_stick_radius);
            seekStickZIndex = v.findViewById(R.id.seek_stick_zindex);
            seekStickLineThickness = v.findViewById(R.id.seek_stick_line_thickness);
            btnStickLineColor = v.findViewById(R.id.btn_stick_line_color);

            btnImgFill = v.findViewById(R.id.btn_img_fill);
            btnImgFit = v.findViewById(R.id.btn_img_fit);
            btnImgCenter = v.findViewById(R.id.btn_img_center);

            btnShapeRect = v.findViewById(R.id.btn_shape_rect);
            btnShapeCircle = v.findViewById(R.id.btn_shape_circle);
            btnShapeTri = v.findViewById(R.id.btn_shape_tri);
            btnShapeHex = v.findViewById(R.id.btn_shape_hex);

            btnHoleShapeRect = v.findViewById(R.id.btn_hole_shape_rect);
            btnHoleShapeCircle = v.findViewById(R.id.btn_hole_shape_circle);
            btnHoleShapeTri = v.findViewById(R.id.btn_hole_shape_tri);
            btnHoleShapeHex = v.findViewById(R.id.btn_hole_shape_hex);

            headerTransform = v.findViewById(R.id.header_transform);
            headerGeometry = v.findViewById(R.id.header_geometry);
            headerHole = v.findViewById(R.id.header_hole);
            headerAppearance = v.findViewById(R.id.header_appearance);

            containerTransform = v.findViewById(R.id.container_transform);
            containerGeometry = v.findViewById(R.id.container_geometry);
            containerHole = v.findViewById(R.id.container_hole);
            containerAppearance = v.findViewById(R.id.container_appearance);

            txtStatusTransform = v.findViewById(R.id.txt_status_transform);
            txtStatusGeometry = v.findViewById(R.id.txt_status_geometry);
            txtStatusHole = v.findViewById(R.id.txt_status_hole);
            txtStatusAppearance = v.findViewById(R.id.txt_status_appearance);

            btnBlankImgFill = v.findViewById(R.id.btn_blank_img_fill);
            btnBlankImgFit = v.findViewById(R.id.btn_blank_img_fit);
            btnBlankImgCenter = v.findViewById(R.id.btn_blank_img_center);
        }
    }
}
