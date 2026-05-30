import os

file_path = r'c:\Users\hasad\StudioProjects\MyApplicatio\app\src\main\res\layout\activity_main.xml'

with open(file_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

# Goal: Restore the structure between lines 45 and 65 (roughly).
# We want: 
# ...
#         </FrameLayout>
#     </LinearLayout>
# 
#     <LinearLayout android:id="@+id/add_edit_container" 
#                   android:layout_width="0dp" 
#                   android:layout_height="0dp" 
#                   android:layout_marginEnd="16dp" 
#                   android:layout_marginBottom="16dp" 
#                   android:gravity="end" 
#                   android:orientation="vertical" 
#                   app:layout_constraintWidth_percent="0.135" 
#                   app:layout_constraintDimensionRatio="1:4.0" 
#                   app:layout_constraintBottom_toBottomOf="parent" 
#                   app:layout_constraintEnd_toEndOf="parent" 
#                   android:clipChildren="false" 
#                   android:clipToPadding="false">
# 
#         <!-- <FrameLayout android:id="@+id/fab_ai" ... -->

# Let's find the fab_redo_btn to identify the spot.
undo_redo_end_idx = -1
for i, line in enumerate(lines):
    if 'android:id="@+id/fab_redo_btn"' in line:
        # Move down to find the end of its LinearLayout
        for j in range(i, len(lines)):
            if '</LinearLayout>' in lines[j]:
                undo_redo_end_idx = j
                break
        if undo_redo_end_idx != -1:
            break

# Also find where the AI button comment starts
fab_ai_idx = -1
for i, line in enumerate(lines):
    if 'android:id="@+id/fab_ai"' in line:
        fab_ai_idx = i
        break

if undo_redo_end_idx != -1 and fab_ai_idx != -1:
    # Everything between undo_redo_end_idx and fab_ai_idx might be messy.
    # We want to replace it.
    
    # Check if lines[fab_ai_idx - 1] is the start of the comment "<!--"
    comment_start_idx = fab_ai_idx
    while comment_start_idx > 0 and '<!--' not in lines[comment_start_idx]:
        comment_start_idx -= 1
        
    new_middle = [
        "    </LinearLayout>\n",
        "\n",
        "    <LinearLayout android:id=\"@+id/add_edit_container\" android:layout_width=\"0dp\" android:layout_height=\"0dp\" android:layout_marginEnd=\"16dp\" android:layout_marginBottom=\"16dp\" android:gravity=\"end\" android:orientation=\"vertical\" app:layout_constraintWidth_percent=\"0.135\" app:layout_constraintDimensionRatio=\"1:4.0\" app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintEnd_toEndOf=\"parent\" android:clipChildren=\"false\" android:clipToPadding=\"false\">\n",
        " \n"
    ]
    
    final_lines = lines[:undo_redo_end_idx] + new_middle + lines[comment_start_idx:]
    
    with open(file_path, 'w', encoding='utf-8') as f:
        f.writelines(final_lines)
    print("Successfully fixed activity_main.xml.")
else:
    print(f"Error: Could not find indices. undo_redo_end_idx={undo_redo_end_idx}, fab_ai_idx={fab_ai_idx}")
