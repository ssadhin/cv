package com.example.myapplication;

import android.content.ClipData;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ResumeAdapter extends RecyclerView.Adapter<ResumeAdapter.ViewHolder> {

    // Interface for interactions
    public interface OnItemClickListener {
        void onItemClick(View view, File file);
        void onDeleteClick(File file);
        void onRenameClick(File file);
        void onDragStart(View view, File file);
        void onMerge(File target, File source);
        void onMoveToFolder(File folder, File source);
        void onUngroup(File file);
    }

    private Context mContext;
    private List<File> mFiles;
    private OnItemClickListener mListener;

    private boolean isGridView;
    private boolean mIsInSubfolder;
    private int mSpanCount;
    private Set<String> mSelectedPaths = new HashSet<>();
    private boolean mIsSelectionMode = false;
    private OnSelectionChangeListener mSelectionListener;

    private static final int VIEW_TYPE_LIST = 0;
    private static final int VIEW_TYPE_GRID = 1;

    // Constructors
    public ResumeAdapter(Context context, List<File> files, OnItemClickListener listener, boolean isGridView, int spanCount, boolean isInSubfolder) {
        this.mContext = context;
        this.mFiles = files != null ? files : new ArrayList<>();
        this.mListener = listener;
        this.isGridView = isGridView;
        this.mSpanCount = spanCount;
        this.mIsInSubfolder = isInSubfolder;
    }

    // -- Data Update Methods --
    public void updateData(List<File> newFiles) {
        mFiles = newFiles != null ? newFiles : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setViewType(boolean isGridView) {
        this.isGridView = isGridView;
        notifyDataSetChanged();
    }

    public void setSpanCount(int spanCount) {
        this.mSpanCount = spanCount;
        notifyDataSetChanged();
    }

    public void setIsInSubfolder(boolean inSubfolder) {
        this.mIsInSubfolder = inSubfolder;
        notifyDataSetChanged();
    }

    // -- Selection Logic --
    public interface OnSelectionChangeListener {
        void onSelectionChanged(int count);
    }

    public void setOnSelectionChangeListener(OnSelectionChangeListener listener) {
        this.mSelectionListener = listener;
    }

    public boolean isSelectionMode() {
        return mIsSelectionMode;
    }

    public void setSelectionMode(boolean selectionMode) {
        if (this.mIsSelectionMode != selectionMode) {
            this.mIsSelectionMode = selectionMode;
            if (!selectionMode) {
                mSelectedPaths.clear();
                if (mSelectionListener != null) mSelectionListener.onSelectionChanged(0);
            }
            notifyDataSetChanged();
        }
    }

    public Set<String> getSelectedPaths() {
        return mSelectedPaths;
    }

    public void toggleSelection(File file) {
        String path = file.getAbsolutePath();
        if (mSelectedPaths.contains(path)) {
            mSelectedPaths.remove(path);
        } else {
            mSelectedPaths.add(path);
        }
        mIsSelectionMode = !mSelectedPaths.isEmpty();
        if (mSelectionListener != null) mSelectionListener.onSelectionChanged(mSelectedPaths.size());
        notifyDataSetChanged();
    }

    // -- RecyclerView Overrides --

    @Override
    public int getItemViewType(int position) {
        return isGridView ? VIEW_TYPE_GRID : VIEW_TYPE_LIST;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = (viewType == VIEW_TYPE_GRID) ? R.layout.item_recent_resume_grid : R.layout.item_recent_resume;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File file = mFiles.get(position);
        
        // CRITICAL: Reset all view properties to prevent recycling issues
        resetViewProperties(holder);
        float density = mContext.getResources().getDisplayMetrics().density;

        // 1. Text Info
        String name = file.getName();
        if (name.endsWith(".json")) {
            name = name.substring(0, name.lastIndexOf("."));
        }
        holder.tvName.setText(name);

        if (file.isDirectory()) {
            holder.tvDate.setText("Folder");
            if (holder.btnUngroup != null) holder.btnUngroup.setVisibility(View.GONE);
        } else {
            Date lastMod = new Date(file.lastModified());
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            holder.tvDate.setText("Last edited: " + sdf.format(lastMod));
            
            if (holder.btnUngroup != null) {
                holder.btnUngroup.setVisibility(mIsInSubfolder ? View.VISIBLE : View.GONE);
                holder.btnUngroup.setOnClickListener(v -> {
                    if (mListener != null) mListener.onUngroup(file);
                });
            }
        }

        // 2. Selection State
        boolean isSelected = mSelectedPaths.contains(file.getAbsolutePath());
        if (holder.ivSelectionIndicator != null) {
            holder.ivSelectionIndicator.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        }

        // 3. Card Styling & Dimensions
        if (holder.itemView instanceof CardView) {
            styleCardView((CardView) holder.itemView, file, isSelected, density);
        }
        
        // 3.5 Apply programmatic rounded background to infoContainer
        if (holder.infoContainer != null) {
            android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
            bg.setColor(Color.parseColor("#FFFFFF"));
            float radius = file.isDirectory() ? 28 * density : 20 * density;
            // Only round bottom corners
            bg.setCornerRadii(new float[]{0, 0, 0, 0, radius, radius, radius, radius});
            holder.infoContainer.setBackground(bg);
            // Force immediate redraw to prevent recycling artifacts
            holder.infoContainer.requestLayout();
            holder.infoContainer.invalidate();
        }

        // 4. Icon / Content Loading
        loadContent(holder, file, density);
        
        // 4.5 Defensive: Force rounded corners after content loading
        enforceRoundedCorners(holder, file, density);
        
        // 5. Scaling Text/Buttons based on Grid Size (if grid)
        if (isGridView) {
            adjustGridSizing(holder, density);
        } else {
             holder.tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
             holder.tvDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        }

        // 6. Listeners (Click, Long Click, Drag)
        setupListeners(holder, file);
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    // -- Helper Methods --

    private void styleCardView(CardView card, File file, boolean isSelected, float density) {
        card.setClipToOutline(true); // Crucial for rounded corners
        
        if (file.isDirectory()) {
            card.setCardElevation(0);
            card.setMaxCardElevation(0);
            card.setRadius(28 * density);
        } else {
            card.setCardElevation(isSelected ? 12 * density : 4 * density);
            card.setMaxCardElevation(isSelected ? 14 * density : 6 * density);
            card.setRadius(20 * density);
        }

        card.setCardBackgroundColor(isSelected ? 
            Color.parseColor("#E3F2FD") : 
            Color.parseColor("#FFFFFF"));
    }

    private void loadContent(ViewHolder holder, File file, float density) {
        String name = file.getName();
        if (name.endsWith(".json")) name = name.substring(0, name.lastIndexOf("."));
        File imageFile = new File(file.getParent(), name + ".png");

        // Determine required padding for "Generic Icons"
        // 5+ cols -> 25dp, ... 2 cols -> 50dp
        int basePaddingDp = 25;
        if (mSpanCount <= 2) basePaddingDp = 50;
        else if (mSpanCount == 3) basePaddingDp = 40;
        else if (mSpanCount == 4) basePaddingDp = 30;
        
        int dynamicPadding = (int) (basePaddingDp * density);

        if (file.isDirectory()) {
            // Folder Processing
            Bitmap preview = generateFolderPreview(file);
            if (preview != null) {
                holder.ivIcon.setImageBitmap(preview);
                holder.ivIcon.setScaleType(ImageView.ScaleType.FIT_XY);
                holder.ivIcon.setBackground(null);
                holder.ivIcon.setPadding(0, 0, 0, 0);
                
                // Force rounded clipping on the ImageView itself
                float radius = 28 * density;
                holder.ivIcon.setClipToOutline(true);
                holder.ivIcon.setOutlineProvider(new android.view.ViewOutlineProvider() {
                    @Override
                    public void getOutline(android.view.View view, android.graphics.Outline outline) {
                        outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
                    }
                });
            } else {
                // Empty Folder Generic Icon
                holder.ivIcon.setImageResource(R.drawable.cv); 
                holder.ivIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#FFC107")));
                holder.ivIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                // Apply padding so icon doesn't touch edges
                if (isGridView) {
                    holder.ivIcon.setPadding(dynamicPadding, dynamicPadding, dynamicPadding, dynamicPadding);
                } else {
                    int p = (int)(20 * density);
                    holder.ivIcon.setPadding(p, p, p, p);
                }
            }
        } else {
            // CV File Processing
            if (imageFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                holder.ivIcon.setImageBitmap(bitmap);
                holder.ivIcon.setBackground(null);
                holder.ivIcon.setImageTintList(null);
                holder.ivIcon.setPadding(0, 0, 0, 0); // Full bleed for screenshot
                applyTopCropScaling(holder.ivIcon, bitmap);
            } else {
                // Generic CV Icon
                holder.ivIcon.setImageResource(R.drawable.cv);
                holder.ivIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#BDBDBD")));
                holder.ivIcon.setBackground(null);
                if (isGridView) {
                    holder.ivIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    holder.ivIcon.setPadding(dynamicPadding, dynamicPadding, dynamicPadding, dynamicPadding);
                } else {
                    holder.ivIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    holder.ivIcon.setBackgroundResource(R.drawable.shape_icon_bg);
                    holder.ivIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#1e3c72")));
                    int p = (int)(10 * density);
                    holder.ivIcon.setPadding(p, p, p, p);
                }
            }
        }
    }

    private void adjustGridSizing(ViewHolder holder, float density) {
        if (holder.infoContainer == null) return;
        
        int titleSize, dateSize, padding, deleteSize;
        if (mSpanCount <= 2) {
            titleSize = 16; dateSize = 12; padding = 12; deleteSize = 32;
        } else if (mSpanCount == 3) {
            titleSize = 14; dateSize = 10; padding = 10; deleteSize = 28;
        } else if (mSpanCount == 4) {
            titleSize = 12; dateSize = 9; padding = 8; deleteSize = 24;
        } else { 
            titleSize = 11; dateSize = 8; padding = 6; deleteSize = 20;
        }

        holder.tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, titleSize);
        holder.tvDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, dateSize);
        int pPx = (int) (padding * density);
        holder.infoContainer.setPadding(pPx, pPx, pPx, pPx);

        ViewGroup.LayoutParams lp = holder.btnDelete.getLayoutParams();
        lp.width = (int) (deleteSize * density);
        lp.height = (int) (deleteSize * density);
        holder.btnDelete.setLayoutParams(lp);

        if (holder.btnUngroup != null && holder.btnUngroup.getVisibility() == View.VISIBLE) {
            ViewGroup.LayoutParams lpU = holder.btnUngroup.getLayoutParams();
            lpU.width = (int) (deleteSize * density);
            lpU.height = (int) (deleteSize * density);
            holder.btnUngroup.setLayoutParams(lpU);
        }
    }

    private void setupListeners(ViewHolder holder, File file) {
        holder.itemView.setOnClickListener(v -> {
            if (mIsSelectionMode) {
                toggleSelection(file);
            } else {
                if (mListener != null) mListener.onItemClick(v, file);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            // If in selection mode and item is already selected, just start drag (don't toggle)
            if (mIsSelectionMode && mSelectedPaths.contains(file.getAbsolutePath())) {
                // Already selected, just start drag with all selected items
                if (mListener != null) mListener.onDragStart(v, file);
            } else {
                // Not selected yet, or not in selection mode - toggle and start drag
                toggleSelection(file);
                if (mListener != null) mListener.onDragStart(v, file);
            }
            return true;
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (mListener != null) mListener.onDeleteClick(file);
        });
        
        holder.tvName.setOnClickListener(v -> {
            if (mListener != null) mListener.onRenameClick(file);
        });

        // Drag Listener for Merging
        holder.itemView.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED: return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setAlpha(0.5f);
                    v.setBackgroundColor(Color.parseColor("#E0E0E0"));
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setAlpha(1.0f);
                    v.setBackgroundColor(Color.TRANSPARENT);
                    return true;
                case DragEvent.ACTION_DROP:
                    v.setAlpha(1.0f);
                    v.setBackgroundColor(Color.TRANSPARENT);
                    
                    // Process ALL items in ClipData (supports batch drag)
                    ClipData clipData = event.getClipData();
                    int itemCount = clipData.getItemCount();
                    int successCount = 0;
                    
                    for (int i = 0; i < itemCount; i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        String sourcePath = item.getText().toString();
                        File sourceFile = new File(sourcePath);
                        
                        if (!sourceFile.equals(file)) {
                            if (file.isDirectory()) {
                                mListener.onMoveToFolder(file, sourceFile);
                                successCount++;
                            } else {
                                // Only merge if single item
                                if (itemCount == 1) {
                                    mListener.onMerge(file, sourceFile);
                                    successCount++;
                                }
                            }
                        }
                    }
                    
                    // Exit selection mode and show feedback if batch operation
                    if (itemCount > 1 && successCount > 0) {
                        if (mIsSelectionMode) {
                            setSelectionMode(false);
                        }
                        android.widget.Toast.makeText(mContext, 
                            "Moved " + successCount + " items", 
                            android.widget.Toast.LENGTH_SHORT).show();
                    }
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    v.setAlpha(1.0f);
                    v.setBackgroundColor(Color.TRANSPARENT);
                    return true;
            }
            return false;
        });
    }

    // -- Bitmap Generation --

    private Bitmap generateFolderPreview(File folder) {
        File[] pngs = folder.listFiles((dir, name) -> name.endsWith(".png"));
        if (pngs == null || pngs.length == 0) return null;

        int size = 400; 
        Bitmap result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        
        
        // 1. Draw Rounded Background (bake roundness into the bitmap itself)
        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.parseColor("#FFF8E1"));
        bgPaint.setAntiAlias(true);
        // 80px radius for 400px size ≈ 20dp on xxhdpi screens
        canvas.drawRoundRect(0, 0, size, size, 80, 80, bgPaint);

        // 2. Draw Items with Rounding
        int count = Math.min(pngs.length, 2);
        for (int i = 0; i < count; i++) {
            Bitmap bmp = BitmapFactory.decodeFile(pngs[i].getAbsolutePath());
            if (bmp != null) {
                // Layout: Side-by-side with padding
                int itemW = size / 2 - 20;
                int itemH = size - 40;
                int left = 10 + (i * (size / 2));
                int top = 20;
                
                RectF dst = new RectF(left, top, left + itemW, top + itemH);
                
                // Rounded Clip for the inner item
                int saveCount = canvas.save();
                Path path = new Path();
                // 60px radius enables nice rounding for the inner items
                path.addRoundRect(dst, 60, 60, Path.Direction.CW);
                canvas.clipPath(path);
                
                Rect src = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
                canvas.drawBitmap(bmp, src, dst, null);
                
                canvas.restoreToCount(saveCount);
                bmp.recycle();
            }
        }
        return result;
    }

    private void applyTopCropScaling(ImageView imageView, Bitmap bitmap) {
        imageView.setScaleType(ImageView.ScaleType.MATRIX);
        imageView.post(() -> {
            if (imageView == null || bitmap == null) return;
            int vWidth = imageView.getWidth();
            int vHeight = imageView.getHeight();
            int dWidth = bitmap.getWidth();
            int dHeight = bitmap.getHeight();

            if (vWidth <= 0 || vHeight <= 0 || dWidth <= 0 || dHeight <= 0) return;

            float scale;
            if (dWidth * vHeight > vWidth * dHeight) {
                scale = (float) vHeight / (float) dHeight;
            } else {
                scale = (float) vWidth / (float) dWidth;
            }

            Matrix matrix = new Matrix();
            matrix.setScale(scale, scale);
            imageView.setImageMatrix(matrix);
        });
    }

    /**
     * Defensive method to enforce rounded corners.
     * Called after binding to ensure CardView properties are maintained.
     */
    private void enforceRoundedCorners(ViewHolder holder, File file, float density) {
        if (!(holder.itemView instanceof CardView)) return;
        
        CardView card = (CardView) holder.itemView;
        
        // Force re-apply clipping
        card.setClipToOutline(true);
        card.setClipChildren(true);
        card.setClipToPadding(false);
        
        // Re-verify radius matches expected value
        float expectedRadius = file.isDirectory() ? 28 * density : 20 * density;
        if (Math.abs(card.getRadius() - expectedRadius) > 1f) {
            card.setRadius(expectedRadius);
        }
        
        // Ensure ImageView doesn't have conflicting properties
        if (holder.ivIcon != null) {
            // For folders and screenshots, background must be null
            String name = file.getName();
            if (name.endsWith(".json")) name = name.substring(0, name.lastIndexOf("."));
            File imageFile = new File(file.getParent(), name + ".png");
            
            if (file.isDirectory() || imageFile.exists()) {
                holder.ivIcon.setBackground(null);
                // Post-layout verification
                holder.ivIcon.post(() -> {
                    if (holder.ivIcon != null && holder.ivIcon.getBackground() != null) {
                        holder.ivIcon.setBackground(null);
                    }
                });
            }
        }
    }

    /**
     * Reset all view properties to prevent recycling issues.
     * This ensures each view starts from a clean state.
     */
    private void resetViewProperties(ViewHolder holder) {
        // Reset ImageView - AGGRESSIVE cleanup
        if (holder.ivIcon != null) {
            holder.ivIcon.setBackground(null);
            holder.ivIcon.setImageBitmap(null);
            holder.ivIcon.setImageDrawable(null);
            holder.ivIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            holder.ivIcon.setPadding(0, 0, 0, 0);
            holder.ivIcon.setClipToOutline(false);
            holder.ivIcon.setOutlineProvider(null);
            // Force clear any cached drawables
            holder.ivIcon.invalidate();
        }
        
        // Reset InfoContainer - AGGRESSIVE cleanup
        if (holder.infoContainer != null) {
            holder.infoContainer.setBackground(null); // Clear first
            holder.infoContainer.setClipToOutline(false);
            holder.infoContainer.setOutlineProvider(null);
            holder.infoContainer.invalidate();
        }
        
        // Reset CardView if it's the root
        if (holder.itemView instanceof CardView) {
            CardView card = (CardView) holder.itemView;
            card.setClipToOutline(true);
            card.setClipChildren(true);
            card.setClipToPadding(false);
        }
    }

    // -- View Holder --
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        ImageView ivSelectionIndicator;
        TextView tvName;
        TextView tvDate;
        View btnDelete;
        View btnUngroup;
        View infoContainer;

        ViewHolder(View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            ivSelectionIndicator = itemView.findViewById(R.id.ivSelectionIndicator);
            tvName = itemView.findViewById(R.id.tvResumeName);
            tvDate = itemView.findViewById(R.id.tvLastModified);
            btnDelete = itemView.findViewById(R.id.btnDeleteOption);
            btnUngroup = itemView.findViewById(R.id.btnUngroup);
            infoContainer = itemView.findViewById(R.id.infoContainer);
        }
    }
}
