import sys

file_path = r'c:\Users\hasad\StudioProjects\MyApplicatio\app\src\main\java\com\example\myapplication\MainActivity.java'

with open(file_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

# Find the start of the class or where things went wrong
# We want to restore the block after imports.

correct_header = [
    "public class MainActivity extends AppCompatActivity implements ColorPickerDialogListener {\n",
    "\n",
    "    private final OkHttpClient httpClient = new OkHttpClient();\n",
    "    private static final String API_BASE_URL = \"https://vitae-backend.asanistudiobangladesh.workers.dev\";\n",
    "    \n",
    "    private View currentFrameSettingsView;\n",
    "    private WebView myWebView;\n",
    "    private boolean isNativeEditing = false;\n",
    "    private boolean hasEnteredEditOnce = false;\n",
    "\n",
    "    // Slide Mode State\n",
    "    private final Handler nativeSlideHandler = new Handler(android.os.Looper.getMainLooper());\n",
    "    private boolean isOnlineMode = false;\n",
    "\n",
    "    // Original Buttons\n",
    "    private View addFab, undoFab, redoFab, editFab, printFab /*, fabAi*/;\n",
    "    private View undoRedoContainer, addEditContainer;\n",
    "    private ImageButton editFabIcon, undoFabIcon, redoFabIcon, addFabIcon /*, btnAi*/;\n",
    "    private EditText etAiBoxInputPointer;\n",
    "    private boolean isPickingAiBoxDocument = false;\n",
    "\n",
    "    private ValueCallback<Uri[]> mUploadMessage;\n",
    "    private ActivityResultLauncher<String> mGetContent;\n",
    "    private ActivityResultLauncher<Intent> mSignatureResultLauncher;\n",
    "    private ActivityResultLauncher<String> mReviewImagePicker;\n",
    "    private ActivityResultLauncher<Intent> mVoiceResultLauncher;\n"
]

# Find where the imports end and the class should start.
# Usually after "import java.io.IOException;" or similar.
insert_idx = -1
for i, line in enumerate(lines):
    if "import java.io.IOException;" in line:
        insert_idx = i + 1
        break

if insert_idx != -1:
    # Check if there's any messy line to remove before inserting
    # The current mess starts right after the imports.
    # From previous view_file, line 133 is currentSelectedBase64Images.
    # So we need to remove everything between insert_idx and the line containing currentSelectedBase64Images.
    
    end_remove_idx = -1
    for i in range(insert_idx, len(lines)):
        if "private final List<String> currentSelectedBase64Images" in lines[i]:
            end_remove_idx = i
            break
            
    if end_remove_idx != -1:
        new_lines = lines[:insert_idx] + ["\n"] + correct_header + lines[end_remove_idx:]
        with open(file_path, 'w', encoding='utf-8') as f:
            f.writelines(new_lines)
        print("Successfully fixed MainActivity.java header.")
    else:
        print("Error: Could not find the point to resume.")
else:
    print("Error: Could not find end of imports.")
