package com.example.myapplication;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;

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
        SectionBgItem item = items.get(position);
        holder.lbl.setText(item.name);
        holder.chk.setChecked(item.isSelected);

        // Standard background controls - Always visible
        holder.colorPreview.setVisibility(View.VISIBLE);
        holder.seek.setVisibility(View.VISIBLE);
        holder.seekLineHeight.setVisibility(View.VISIBLE);
        holder.seekRadius.setVisibility(View.VISIBLE);
        
        holder.seek.setProgress(item.opacity);
        holder.seekLineHeight.setProgress((int)(item.lineHeight * 100));
        holder.seekRadius.setProgress(item.radius);
        holder.seekFontSize.setProgress(item.fontSize);
        holder.seekRotation.setProgress(item.rotation);
        holder.seekAlign.setProgress(item.alignment);

        // Header specific: Hide font size
        if (item.id.equals("mainHeader")) {
            holder.seekFontSize.setVisibility(View.GONE);
            if (holder.txtLabelFontSize != null) holder.txtLabelFontSize.setVisibility(View.GONE);
        } else {
            holder.seekFontSize.setVisibility(View.VISIBLE);
            if (holder.txtLabelFontSize != null) holder.txtLabelFontSize.setVisibility(View.VISIBLE);
        }

        // Global listeners for Rotation and Alignment (Available for ALL items)
        holder.seekAlign.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    item.alignment = progress;
                    activity.updateNameSettings(item.id, item.rotation, item.nameFontSize, item.titleFontSize, item.alignment);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        holder.seekRotation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    item.rotation = progress;
                    activity.updateNameSettings(item.id, item.rotation, item.nameFontSize, item.titleFontSize, item.alignment);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        if (item.isNameProfession) {
            holder.layoutName.setVisibility(View.VISIBLE);
            
            holder.seekNameSize.setProgress(item.nameFontSize);
            holder.seekTitleSize.setProgress(item.titleFontSize);

            holder.seekNameSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (progress < 50) progress = 50; 
                    if(fromUser) {
                        item.nameFontSize = progress;
                        activity.updateNameSettings(item.id, item.rotation, item.nameFontSize, item.titleFontSize, item.alignment);
                    }
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });

            holder.seekTitleSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (progress < 50) progress = 50; 
                    if(fromUser) {
                        item.titleFontSize = progress;
                        activity.updateNameSettings(item.id, item.rotation, item.nameFontSize, item.titleFontSize, item.alignment);
                    }
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });
            
        } else {
            holder.layoutName.setVisibility(View.GONE);
        }

        // Color and Logic Listeners (Apply to ALL items)
        try {
            holder.colorPreview.setBackgroundColor(Color.parseColor(item.color));
        } catch(Exception e) {
            holder.colorPreview.setBackgroundColor(Color.WHITE);
        }

        holder.seekFontSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    if (progress < 8) progress = 8;
                    item.fontSize = progress;
                    updateFontSize(item, progress);
                    if(item.isSelected || item.isMaster) {
                        for(SectionBgItem sub : items) {
                            if(sub.isSelected || (item.isMaster && sub != item)) {
                                sub.fontSize = progress;
                                updateFontSize(sub, progress);
                            }
                        }
                    }
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        holder.seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    item.opacity = progress;
                    updateOpacity(item, progress);
                    if(item.isSelected || item.isMaster) {
                        for(SectionBgItem sub : items) {
                            if(sub.isSelected || (item.isMaster && sub != item)) {
                                sub.opacity = progress;
                                updateOpacity(sub, progress);
                            }
                        }
                    }
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        holder.seekLineHeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    float val = progress / 100.0f;
                    item.lineHeight = val;
                    updateLineHeight(item, val);
                    if(item.isSelected || item.isMaster) {
                        for(SectionBgItem sub : items) {
                            if(sub.isSelected || (item.isMaster && sub != item)) {
                                sub.lineHeight = val;
                                updateLineHeight(sub, val);
                            }
                        }
                    }
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        holder.seekRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    item.radius = progress;
                    updateRadius(item, progress);
                    if(item.isSelected || item.isMaster) {
                        for(SectionBgItem sub : items) {
                            if(sub.isSelected || (item.isMaster && sub != item)) {
                                sub.radius = progress;
                                updateRadius(sub, progress);
                            }
                        }
                    }
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        holder.colorPreview.setOnClickListener(v -> {
            activity.pendingColorItem = item;
            activity.pendingColorAdapter = SectionBgAdapter.this;

            int col = Color.WHITE;
            try { col = Color.parseColor(item.color); } catch(Exception e){}

            ColorPickerDialog.newBuilder()
                    .setDialogId(MainActivity.SECTION_BG_COLOR_ID)
                    .setColor(col)
                    .setShowAlphaSlider(true)
                    .show(activity);
        });

        holder.chk.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.isSelected = isChecked;
        });
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
                    "document.getElementById('"+item.id+"').style.borderRadius = '" + val + "px'; " +
                    "}";
            activity.getWebView().evaluateJavascript(js, null);
        }
    }

    public void updateColor(SectionBgItem item, int colorInt) {
        String hex = String.format("#%06X", (0xFFFFFF & colorInt));
        item.color = hex;
        notifyDataSetChanged();
        if (activity.getWebView() != null) {
            if (item.isMaster) {
                activity.getWebView().evaluateJavascript("if(document.getElementById('"+item.id+"')) { document.getElementById('"+item.id+"').style.backgroundColor = '"+hex+"'; }", null);
                for(SectionBgItem sub : items) {
                    if(sub != item) {
                        sub.color = hex;
                        activity.getWebView().evaluateJavascript("if(document.getElementById('"+sub.id+"')) { document.getElementById('"+sub.id+"').style.setProperty('--item-bg', '"+hex+"'); }", null);
                    }
                }
                notifyDataSetChanged();
            } else {
                activity.getWebView().evaluateJavascript("if(document.getElementById('"+item.id+"')) { document.getElementById('"+item.id+"').style.setProperty('--item-bg', '"+hex+"'); }", null);
            }
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox chk;
        TextView lbl;
        SeekBar seek;
        SeekBar seekFontSize;
        SeekBar seekLineHeight;
        SeekBar seekRadius;
        
        SeekBar seekRotation;
        SeekBar seekNameSize;
        SeekBar seekTitleSize;
        SeekBar seekAlign;
        
        View layoutName;
        View colorPreview;
        TextView txtLabelFontSize;

        ViewHolder(View v) {
            super(v);
            chk = v.findViewById(R.id.chk_select);
            lbl = v.findViewById(R.id.txt_label);
            seek = v.findViewById(R.id.seek_opacity);
            seekFontSize = v.findViewById(R.id.seek_font_size);
            seekLineHeight = v.findViewById(R.id.seek_line_height);
            seekRadius = v.findViewById(R.id.seek_radius);
            colorPreview = v.findViewById(R.id.view_color_preview);
            txtLabelFontSize = v.findViewById(R.id.txt_label_font_size);
            
            seekRotation = v.findViewById(R.id.seek_row_rotation);
            seekNameSize = v.findViewById(R.id.seek_name_size);
            seekTitleSize = v.findViewById(R.id.seek_title_size);
            seekAlign = v.findViewById(R.id.seek_align);
            
            layoutName = v.findViewById(R.id.layout_name_controls);
        }
    }
}
