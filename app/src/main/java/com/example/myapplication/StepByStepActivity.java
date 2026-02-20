package com.example.myapplication;

import android.content.Intent;
import android.text.Html;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.net.Uri;
import android.view.inputmethod.InputMethodManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.io.File;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector;
import androidx.constraintlayout.widget.Guideline;
import android.view.inputmethod.EditorInfo;
import android.text.InputType;

import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class StepByStepActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private LinearProgressIndicator progressBar;
    private Button btnBack, btnNext, btnSwitchToEditor;
    private TextView tvStepTitle;
    private ImageButton btnClose;
    private android.webkit.WebView wizardWebView;
    private View previewContainer;
    private View resizerHandle;
    private Guideline splitGuideline;
    private ImageButton btnZoomIn, btnZoomOut;
    private TextView tvZoomValue;
    private int currentZoom = 50;
    private ScaleGestureDetector scaleGestureDetector;
    private JSONObject headerData;
    private String signatureData = "";
    private JSONObject colorsData, metricsData;

    private ActivityResultLauncher<String> mGetContent;
    private FieldModel pendingImageField;
    private ImageView pendingImageView;

    private String selectedPurpose = "job";
    private String selectedTemplate = "default"; // ID or Filename
    private boolean isAssetTemplate = false;
    private boolean isUserTemplate = false;
    private String templateJsonData = null; // For loading file-based templates
    private List<SectionModel> currentSections = new ArrayList<>();
    private RecyclerView sectionsRecyclerView; // Reference to Step 3 sections list for keyboard navigation

    // Field Model
    public static class FieldModel {
        String key;
        String label;
        String value = "";
        String type = "text"; // text, textarea

        public FieldModel(String key, String label) {
            this.key = key;
            this.label = label;
        }

        public FieldModel(String key, String label, String value, String type) {
            this.key = key;
            this.label = label;
            this.value = value;
            this.type = type;
        }
        
        public FieldModel(String key, String label, String type) {
            this.key = key;
            this.label = label;
            this.type = type;
        }
    }

    // Item Model (e.g., one job in experience, one school in education)
    public static class ItemModel {
        List<FieldModel> fields = new ArrayList<>();
        public ItemModel(List<FieldModel> fields) {
            this.fields = fields;
        }
    }

    // Section Model
    public static class SectionModel {
        String id;
        String name;
        String type;
        String icon;
        String group;
        String column = "auto"; // Default to auto-balance
        boolean isExpanded = false;
        List<ItemModel> items = new ArrayList<>();

        public SectionModel(String id, String name, String icon, String type, String group) {
            this.id = id;
            this.name = name;
            this.icon = icon;
            this.type = type;
            this.group = group;
            // Initialize with default items
            if (type.equals("contact")) {
                this.items.add(createContactItem("Example Detail", "alex@gmail.com"));
                this.items.add(createContactItem("Call/WhatsApp", "+1 234 567 890"));
                this.items.add(createContactItem("Current Location", "Street 123, City, Country"));
            } else {
                this.items.add(createDefaultItem(type));
            }
        }

        public SectionModel(String id, String name, String icon, String type, String group, String column) {
            this(id, name, icon, type, group);
            this.column = column;
        }

        private static ItemModel createContactItem(String label, String value) {
            List<FieldModel> fields = new ArrayList<>();
            fields.add(new FieldModel("val", label, value, "text"));
            return new ItemModel(fields);
        }

        private static ItemModel createDefaultItem(String type) {
            List<FieldModel> fields = new ArrayList<>();
            switch (type) {
                case "header":
                    fields.add(new FieldModel("name", "Full Name", "Your Name", "text"));
                    fields.add(new FieldModel("addr", "Address", "City, Country", "text"));
                    fields.add(new FieldModel("email", "Email", "email@example.com", "text"));
                    fields.add(new FieldModel("phone", "Phone", "+1 234 567 890", "text"));
                    fields.add(new FieldModel("link", "LinkedIn / Web", "linkedin.com/in/...", "text"));
                    break;
                case "profile_pic":
                case "profileSection":
                    fields.add(new FieldModel("profile_pic", "Profile Picture", "", "image"));
                    break;
                case "name_profession":
                case "nameProfessionSection":
                    fields.add(new FieldModel("name", "Name", "Your Name"));
                    fields.add(new FieldModel("prof", "Profession", "Professional Title"));
                    break;
                case "personal":
                case "personalDetails":
                    fields.add(new FieldModel("nationality", "Nationality", ""));
                    fields.add(new FieldModel("dob", "Date of Birth", ""));
                    fields.add(new FieldModel("gender", "Gender", ""));
                    fields.add(new FieldModel("ms", "Marital Status", ""));
                    break;
                case "passport":
                case "passportDetails":
                    fields.add(new FieldModel("pno", "Passport No.", ""));
                    fields.add(new FieldModel("issued", "Issued By", ""));
                    fields.add(new FieldModel("idate", "Issue Date", ""));
                    fields.add(new FieldModel("edate", "Expiry Date", ""));
                    break;
                case "summary_paragraph":
                case "summarySection":
                    fields.add(new FieldModel("summary", "Summary", "textarea", "..."));
                    break;
                case "visaStatus":
                    fields.add(new FieldModel("country", "Country", ""));
                    fields.add(new FieldModel("type", "Visa Type", ""));
                    fields.add(new FieldModel("status", "Status", ""));
                    fields.add(new FieldModel("expiry", "Expiry Date", ""));
                    break;
                case "languages":
                    fields.add(new FieldModel("lang", "Language", ""));
                    fields.add(new FieldModel("lvl", "Level (e.g. Fluent)", ""));
                    break;
                case "education":
                    fields.add(new FieldModel("inst", "Institute", ""));
                    fields.add(new FieldModel("year", "Year", ""));
                    fields.add(new FieldModel("board", "Board", ""));
                    fields.add(new FieldModel("deg", "Degree", ""));
                    fields.add(new FieldModel("gpa", "GPA/Score", ""));
                    break;
                case "experience":
                    fields.add(new FieldModel("comp", "Company", ""));
                    fields.add(new FieldModel("dur", "Duration", ""));
                    fields.add(new FieldModel("role", "Role", ""));
                    fields.add(new FieldModel("desc", "Responsibilities", "textarea", ""));
                    break;
                case "projects":
                    fields.add(new FieldModel("name", "Project Name", ""));
                    fields.add(new FieldModel("year", "Year", ""));
                    fields.add(new FieldModel("desc", "Description", "textarea", ""));
                    fields.add(new FieldModel("link", "Project Link", ""));
                    break;
                case "researchExp":
                case "teachingExp":
                case "grants":
                case "training":
                    fields.add(new FieldModel("title", "Title", ""));
                    fields.add(new FieldModel("year", "Year / Date", ""));
                    fields.add(new FieldModel("inst", "Institution / Agency", ""));
                    fields.add(new FieldModel("desc", "Details", "textarea", ""));
                    break;
                case "skills":
                case "hobbies":
                case "interests":
                    fields.add(new FieldModel("cat", "Category", ""));
                    fields.add(new FieldModel("vals", "Items (comma separated)", "textarea", ""));
                    break;
                case "testScores":
                    fields.add(new FieldModel("test", "Test Name", ""));
                    fields.add(new FieldModel("score", "Score", ""));
                    fields.add(new FieldModel("date", "Date", ""));
                    break;
                case "simple-list":
                case "certificates":
                case "physicalFitness":
                    fields.add(new FieldModel("val", "Content", ""));
                    break;
                case "awards":
                case "achievements":
                    fields.add(new FieldModel("title", "Award/Achievement", ""));
                    break;
                case "contact":
                    fields.add(new FieldModel("val", "Contact Detail", "example@gmail.com"));
                    break;
                case "certifications":
                    fields.add(new FieldModel("name", "Certification Name", ""));
                    fields.add(new FieldModel("org", "Organization", ""));
                    fields.add(new FieldModel("date", "Date", ""));
                    break;
                case "volunteer":
                    fields.add(new FieldModel("role", "Role", ""));
                    fields.add(new FieldModel("dur", "Date", ""));
                    fields.add(new FieldModel("inst", "Organization", ""));
                    fields.add(new FieldModel("desc", "Description", "textarea", ""));
                    break;
                case "publications":
                    fields.add(new FieldModel("title", "Title", ""));
                    fields.add(new FieldModel("year", "Year", ""));
                    fields.add(new FieldModel("pub", "Publisher", ""));
                    fields.add(new FieldModel("link", "Link", ""));
                    break;
                case "affiliations":
                    fields.add(new FieldModel("org", "Organization", ""));
                    fields.add(new FieldModel("role", "Role", ""));
                    fields.add(new FieldModel("year", "Year", ""));
                    break;
                case "familyDetails":
                    fields.add(new FieldModel("father", "Father: Name / Occupation", ""));
                    fields.add(new FieldModel("mother", "Mother: Name / Occupation", ""));
                    fields.add(new FieldModel("siblings", "Siblings: Number / Details", ""));
                    break;
                case "partnerExpectations":
                    fields.add(new FieldModel("pref", "Edu / Prof Preference", ""));
                    fields.add(new FieldModel("expectations", "General Expectations", "textarea", ""));
                    break;
                case "lifestyleHabits":
                    fields.add(new FieldModel("diet", "Diet (Veg/Non-Veg)", ""));
                    fields.add(new FieldModel("smoking", "Smoking Habit", ""));
                    fields.add(new FieldModel("drinking", "Drinking Habit", ""));
                    break;
                case "astrologySection":
                case "astrological":
                    fields.add(new FieldModel("rashi", "Rashi / Zodiac", ""));
                    fields.add(new FieldModel("nakshatra", "Nakshatra / Star", ""));
                    fields.add(new FieldModel("gotra", "Gotra / Lineage", ""));
                    break;
                case "physical":
                case "physicalProfile":
                    fields.add(new FieldModel("height", "Height", ""));
                    fields.add(new FieldModel("weight", "Weight", ""));
                    fields.add(new FieldModel("complexion", "Complexion", ""));
                    fields.add(new FieldModel("build", "Build (e.g. Athletic)", ""));
                    fields.add(new FieldModel("eye", "Eye Color", ""));
                    break;
                case "visualRegistry":
                case "photography":
                    fields.add(new FieldModel("head", "Headshot Label", "Headshot"));
                    fields.add(new FieldModel("body", "Full-body Label", "Full-Body Shot"));
                    fields.add(new FieldModel("life", "Lifestyle Label", "Lifestyle Photo"));
                    break;
                case "active_lifestyle":
                case "activeLife":
                    fields.add(new FieldModel("activity", "Activity", ""));
                    fields.add(new FieldModel("achievements", "Achievements", ""));
                    fields.add(new FieldModel("health", "Health Status", ""));
                    break;
                case "extra":
                    fields.add(new FieldModel("act", "Activity", ""));
                    fields.add(new FieldModel("dur", "Duration", ""));
                    fields.add(new FieldModel("desc", "Description", "textarea", ""));
                    break;
                case "references":
                    fields.add(new FieldModel("name", "Name", ""));
                    fields.add(new FieldModel("pos", "Position", ""));
                    fields.add(new FieldModel("org", "Company", ""));
                    fields.add(new FieldModel("email", "Email", ""));
                    fields.add(new FieldModel("phone", "Phone", ""));
                    break;
                case "internships":
                    fields.add(new FieldModel("role", "Role", ""));
                    fields.add(new FieldModel("date", "Date", ""));
                    fields.add(new FieldModel("comp", "Company", ""));
                    fields.add(new FieldModel("desc", "Responsibilities", "textarea", ""));
                    break;
                case "weblinks":
                    fields.add(new FieldModel("name", "Site Name", ""));
                    fields.add(new FieldModel("link", "URL", ""));
                    fields.add(new FieldModel("desc", "Description", "textarea", ""));
                    break;
                case "declaration_block":
                case "declarationSection":
                    fields.add(new FieldModel("text", "Declaration Text", "textarea", "I hereby certify..."));
                    break;
                default:
                    fields.add(new FieldModel("val", "Content", ""));
            }
            return new ItemModel(fields);
        }
        
        public void addItem() {
            this.items.add(createDefaultItem(this.type));
        }
    }

    // Purpose Model
    public static class PurposeModel {
        String id;
        String name;
        String desc;
        int iconRes;

        public PurposeModel(String id, String name, String desc, int iconRes) {
            this.id = id;
            this.name = name;
            this.desc = desc;
            this.iconRes = iconRes;
        }
    }

    private static class VitaeData {
        String json;
        Bitmap thumbnail;
    }

    // Template Model
    public static class TemplateModel {
        String id;
        String name;
        int previewRes;
        boolean isUserTemplate = false;
        boolean isAssetTemplate = false;
        String filePath = null;
        String assetPath = null;
        Bitmap thumbnail = null;

        public TemplateModel(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public TemplateModel(String id, String name, boolean isUserTemplate, String filePath) {
            this.id = id;
            this.name = name;
            this.isUserTemplate = isUserTemplate;
            this.filePath = filePath;
        }

        public TemplateModel(String id, String name, boolean isAssetTemplate, String assetPath, Bitmap thumbnail) {
            this.id = id;
            this.name = name;
            this.isAssetTemplate = isAssetTemplate;
            this.assetPath = assetPath;
            this.thumbnail = thumbnail;
        }
    }

    private final List<SectionModel> ALL_SECTIONS = Arrays.asList(
        // SPECIAL: Header (Hidden from Step 3 list but used for templates)
        new SectionModel("headerSection", "Header Info", "fa-user-circle", "header", "gridEssentials"),

        // Essentials
        new SectionModel("profileSection", "Profile Picture", "fa-user-circle", "profile_pic", "gridEssentials"),
        new SectionModel("nameProfessionSection", "Name & Profession", "fa-id-badge", "name_profession", "gridEssentials"),
        new SectionModel("personalDetails", "Personal Details", "fa-id-card", "personal", "gridEssentials"),
        new SectionModel("passportDetails", "Passport Details", "fa-passport", "passport", "gridEssentials"),
        new SectionModel("visaStatus", "Visa Status", "fa-file-invoice", "visaStatus", "gridEssentials"),
        new SectionModel("summarySection", "Summary", "fa-user-tie", "summary_paragraph", "gridEssentials"),
        new SectionModel("languages", "Languages", "fa-language", "languages", "gridEssentials"),

        // Experience & Knowledge
        new SectionModel("education", "Education", "fa-graduation-cap", "education", "gridExp"),
        new SectionModel("experience", "Experience", "fa-briefcase", "experience", "gridExp"),
        new SectionModel("projects", "Projects", "fa-project-diagram", "projects", "gridExp"),
        new SectionModel("researchExp", "Research Experience", "fa-microscope", "researchExp", "gridExp"),
        new SectionModel("teachingExp", "Teaching Experience", "fa-chalkboard-teacher", "teachingExp", "gridExp"),
        new SectionModel("grants", "Grants & Funding", "fa-hand-holding-usd", "grants", "gridExp"),
        new SectionModel("skills", "Skills", "fa-tools", "skills", "gridExp"),
        new SectionModel("testScores", "Test Scores", "fa-check-double", "testScores", "gridExp"),
        new SectionModel("certificates", "Certificates (Simple)", "fa-certificate", "simple-list", "gridExp"),

        // Additional
        new SectionModel("awards", "Awards & Honors", "fa-trophy", "awards", "gridAdd"),
        new SectionModel("certifications", "Certifications (Adv)", "fa-certificate", "certifications", "gridAdd"),
        new SectionModel("volunteer", "Volunteer Exp", "fa-hands-helping", "volunteer", "gridAdd"),
        new SectionModel("publications", "Publications", "fa-book", "publications", "gridAdd"),
        new SectionModel("affiliations", "Affiliations", "fa-users", "affiliations", "gridAdd"),
        new SectionModel("familyDetails", "Family Details", "fa-users-cog", "familyDetails", "gridAdd"),
        new SectionModel("partnerExpectations", "Partner Expectations", "fa-heart", "partnerExpectations", "gridAdd"),
        new SectionModel("lifestyleHabits", "Lifestyle Habits", "fa-apple-alt", "lifestyleHabits", "gridAdd"),
        new SectionModel("astrologySection", "Astrological Details", "fa-sun", "astrologySection", "gridAdd"),
        new SectionModel("hobbies", "Hobbies", "fa-gamepad", "hobbies", "gridAdd"),
        new SectionModel("extra", "Extracurricular", "fa-futbol", "extra", "gridAdd"),
        new SectionModel("references", "References", "fa-user-check", "references", "gridAdd"),
        new SectionModel("training", "Training", "fa-chalkboard-teacher", "training", "gridAdd"),
        new SectionModel("internships", "Internships", "fa-laptop-code", "internships", "gridAdd"),
        new SectionModel("achievements", "Achievements", "fa-star", "achievements", "gridAdd"),
        new SectionModel("weblinks", "Web Links", "fa-link", "weblinks", "gridAdd"),
        new SectionModel("contactDetails", "Contact Details", "fa-address-book", "contact", "gridEssentials"),
        new SectionModel("physicalProfile", "Physical Profile", "fa-user-check", "physical", "gridAdd"),
        new SectionModel("visualRegistry", "Visual Representation", "fa-camera", "photography", "gridAdd"),
        new SectionModel("activeLife", "Active Lifestyle", "fa-running", "active_lifestyle", "gridAdd"),
        new SectionModel("physicalFitness", "Interests (Misc)", "fa-heartbeat", "simple-list", "gridAdd"),
        new SectionModel("interests", "Interests", "fa-star", "hobbies", "gridAdd"),
        new SectionModel("declarationSection", "Declaration", "fa-file-signature", "declaration_block", "gridAdd")
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_by_step);

        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null && pendingImageField != null) {
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(uri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 80, baos);
                            byte[] b = baos.toByteArray();
                            String encoded = "data:image/png;base64," + Base64.encodeToString(b, Base64.DEFAULT);
                            
                            pendingImageField.value = encoded;
                            if (pendingImageView != null) {
                                pendingImageView.setImageBitmap(bitmap);
                            }
                            updateWebViewPreview();
                        } catch (Exception e) {
                            Log.e("StepByStep", "Error picking image", e);
                            Toast.makeText(this, "Error picking image", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        viewPager = findViewById(R.id.viewPager);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);
        btnSwitchToEditor = findViewById(R.id.btnSwitchToEditor);
        tvStepTitle = findViewById(R.id.tvStepTitle);
        btnClose = findViewById(R.id.btnClose);
        wizardWebView = findViewById(R.id.wizardWebView);
        previewContainer = findViewById(R.id.previewContainer);
        resizerHandle = findViewById(R.id.resizerHandle);
        splitGuideline = findViewById(R.id.splitGuideline);
        btnZoomIn = findViewById(R.id.btnZoomIn);
        btnZoomOut = findViewById(R.id.btnZoomOut);
        tvZoomValue = findViewById(R.id.tvZoomValue);

        setupWizardWebView();
        setupResizer();
        setupZoomControls();

        viewPager.setAdapter(new StepsAdapter());
        viewPager.setUserInputEnabled(false); // Only buttons control it

        btnClose.setOnClickListener(v -> finish());
        
        btnBack.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() > 0) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
                updateUI();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() < 2) {
                if (viewPager.getCurrentItem() == 0) {
                    applyPurposeDefaults();
                } else if (viewPager.getCurrentItem() == 1) {
                    // Start of Step 2 (Sections)
                    updateSectionsBasedOnTemplate();
                }
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                updateUI();
            } else {
                launchEditor();
            }
        });

        btnSwitchToEditor.setOnClickListener(v -> launchEditor());

        if (getIntent() != null && getIntent().hasExtra("EXTRA_INITIAL_STATE")) {
            loadStructuredData(getIntent().getStringExtra("EXTRA_INITIAL_STATE"));
        }

        // Handle Jump to Specific Step
        if (getIntent() != null && getIntent().hasExtra("EXTRA_START_STEP")) {
            int startStep = getIntent().getIntExtra("EXTRA_START_STEP", 0);
            
            if (startStep == 2) {
                // If jumping to sections page, ensure defaults are applied if list is empty
                if (currentSections.isEmpty()) {
                    applyPurposeDefaults();
                }
                viewPager.setCurrentItem(2, false);
            } else {
                viewPager.setCurrentItem(startStep, false);
            }
        }

        updateUI();
    }

    private void setupResizer() {
        resizerHandle.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                float rawY = event.getRawY();
                int[] location = new int[2];
                View parent = (View) resizerHandle.getParent();
                parent.getLocationOnScreen(location);
                float relativeY = rawY - location[1];
                float totalHeight = parent.getHeight();
                
                float percentage = relativeY / totalHeight;
                if (percentage > 0.15f && percentage < 0.85f) {
                    splitGuideline.setGuidelinePercent(percentage);
                }
            }
            return true;
        });
    }

    private void setupZoomControls() {
        // Initialize Pinch-to-Zoom gesture detector
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                // Handle pinch zoom if needed, or rely on WebView's built-in handling
                return false; 
            }
        });

        // Zoom Buttons - Native Control
        btnZoomIn.setOnClickListener(v -> wizardWebView.zoomIn());
        btnZoomOut.setOnClickListener(v -> wizardWebView.zoomOut());
        
        tvZoomValue.setVisibility(View.GONE); // Hide the % label as it's less relevant for arbitrary scaling
    }

    private void updateUI() {
        int current = viewPager.getCurrentItem();
        progressBar.setProgress(current + 1);
        
        switch (current) {
            case 0:
                tvStepTitle.setText("Select Purpose");
                btnBack.setVisibility(View.INVISIBLE);
                btnNext.setText("Next");
                break;
            case 1:
                tvStepTitle.setText("Choose Template");
                btnBack.setVisibility(View.VISIBLE);
                btnNext.setText("Next");
                break;
            case 2:
                tvStepTitle.setText("Manage Sections");
                btnBack.setVisibility(View.VISIBLE);
                btnNext.setText("Finish");
                break;
        }
    }

    /**
     * Check if the selected template has a header element (mainHeader).
     * Templates without headers should use Identity + Contact sections instead.
     */
    private boolean templateHasHeader() {
        String templateJson = getSelectedTemplateJson();
        if (selectedTemplate.equals("default") || selectedTemplate.equals("sidebar")) {
            return true; 
        }
        
        if (templateJson == null) return true; 
        
        try {
            JSONObject template = new JSONObject(templateJson);
            
            // Priority 1: Explicit flag
            if (template.has("hasHeader")) {
                return template.getBoolean("hasHeader");
            }
            
            // Priority 2: Sections array check
            if (template.has("sections")) {
                JSONArray sections = template.getJSONArray("sections");
                for (int i = 0; i < sections.length(); i++) {
                    String id = sections.getJSONObject(i).optString("id", "");
                    if (id.equals("headerSection") || id.equals("mainHeader")) return true;
                }
                return false; 
            }
            
            // Priority 3: HTML indicator
            String html = template.optString("html", "");
            if (!html.isEmpty()) {
                return html.contains("id=\"mainHeader\"") || html.contains("id='mainHeader'");
            }
            
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    private void applyPurposeDefaults() {
        // Remove currentSections.clear(); so we preserve existing data loaded from Editor
        
        List<String> defaultIds = new ArrayList<>(Arrays.asList(
            "headerSection", "summarySection", "personalDetails", "passportDetails", 
            "languages", "certificates", "education", 
            "experience", "projects", "skills", "declarationSection"
        ));

        switch (selectedPurpose) {
            case "job":
                defaultIds.remove("passportDetails");
                defaultIds.remove("visaStatus");
                defaultIds.add("references");
                defaultIds.add("training");
                defaultIds.add("volunteer");
                defaultIds.add("certifications");
                defaultIds.add("awards");
                break;
            case "job_abroad":
                if (!defaultIds.contains("passportDetails")) defaultIds.add("passportDetails");
                defaultIds.add("visaStatus");
                defaultIds.add("testScores");
                defaultIds.add("references");
                defaultIds.add("volunteer");
                defaultIds.add("training");
                defaultIds.add("achievements");
                defaultIds.add("certifications");
                defaultIds.add("internships");
                break;
            case "academic":
                defaultIds.remove("passportDetails");
                defaultIds.remove("visaStatus");
                defaultIds.remove("experience");
                defaultIds.add("publications");
                defaultIds.add("researchExp");
                defaultIds.add("teachingExp");
                defaultIds.add("grants");
                defaultIds.add("weblinks");
                defaultIds.add("achievements");
                defaultIds.add("awards");
                defaultIds.add("affiliations");
                break;
            case "study_abroad":
                defaultIds.add("testScores");
                defaultIds.add("internships");
                defaultIds.add("weblinks");
                defaultIds.add("volunteer");
                defaultIds.add("extra");
                defaultIds.add("awards");
                defaultIds.add("achievements");
                break;
            case "marriage":
                defaultIds.remove("passportDetails");
                defaultIds.remove("experience");
                defaultIds.remove("projects");
                defaultIds.remove("visaStatus");
                defaultIds.remove("summarySection");
                defaultIds.add("extra");
                defaultIds.add("hobbies");
                defaultIds.add("achievements");
                defaultIds.add("references");
                defaultIds.add("affiliations");
                defaultIds.add("familyDetails");
                defaultIds.add("partnerExpectations");
                defaultIds.add("lifestyleHabits");
                defaultIds.add("astrologySection");
                defaultIds.add("physicalProfile");
                defaultIds.add("visualRegistry");
                defaultIds.add("activeLife");
                defaultIds.add("interests");
                break;
        }

        // Add sections from the purpose defaults ONLY if they don't already exist
        // This preserves the current order and data in existing sections
        for (String id : defaultIds) {
            boolean exists = false;
            for (SectionModel s : currentSections) {
                // ROBUST DEDUPLICATION: Check if ID matches OR if Type matches
                // This prevents adding "Passport Details" if "Passport" already exists
                if (s.id.equals(id)) {
                    exists = true;
                    break;
                }
                
                SectionModel def = findSectionById(id);
                if (def != null && s.type.equals(def.type)) {
                    exists = true;
                    break;
                }
                
                if (id.equals("headerSection") && (s.id.equals("nameProfessionSection"))) {
                    exists = true;
                    break;
                }
                if ((id.equals("nameProfessionSection")) && s.id.equals("headerSection")) {
                    exists = true;
                    break;
                }
            }
            
            if (!exists) {
                SectionModel secDef = findSectionById(id);
                if (secDef != null) {
                    SectionModel instance = new SectionModel(secDef.id, secDef.name, secDef.icon, secDef.type, secDef.group);
                    currentSections.add(instance);
                }
            }
        }
        
        // Ensure at least one section is expanded if none are
        boolean anyExpanded = false;
        for (SectionModel s : currentSections) if (s.isExpanded) { anyExpanded = true; break; }
        if (!anyExpanded && !currentSections.isEmpty()) currentSections.get(0).isExpanded = true;

        updateWebViewPreview();
    }

    private void loadStructuredData(String json) {
        try {
            JSONObject data = new JSONObject(json);
            if (data.has("layout")) selectedTemplate = data.getString("layout");
            if (data.has("header")) headerData = data.getJSONObject("header");
            if (data.has("sig")) signatureData = data.optString("sig", "");
            if (data.has("colors")) colorsData = data.getJSONObject("colors");
            if (data.has("metrics")) metricsData = data.getJSONObject("metrics");
            
            if (data.has("sections")) {
                loadSectionsFromJsonArray(data.getJSONArray("sections"));
            }
        } catch (Exception e) {
            Log.e("StepByStep", "Error loading structured data", e);
        }
    }

    private String normalizeSectionId(String id, String type) {
        // Map legacy/template IDs to canonical IDs
        if (id.equals("passport") || type.equals("passport")) return "passportDetails";
        if (id.equals("summary_paragraph") || type.equals("summary_paragraph") || id.equals("objective")) return "summarySection";
        if (id.equals("declaration") || id.equals("declaration_block") || type.equals("declaration") || type.equals("declaration_block")) return "declarationSection";
        if (id.equals("personal") || type.equals("personal")) return "personalDetails";
        
        // Default: try to find a match in ALL_SECTIONS by type if ID doesn't match
        for (SectionModel sec : ALL_SECTIONS) {
            if (sec.type.equals(type)) return sec.id;
        }
        
        return id;
    }

    private void loadSectionsFromJsonArray(JSONArray sectionsArray) throws JSONException {
        currentSections.clear();
        for (int i = 0; i < sectionsArray.length(); i++) {
            JSONObject sLoopObj = sectionsArray.getJSONObject(i);
            String rawId = sLoopObj.getString("id");
            String sType = sLoopObj.optString("type", "text"); // Default to text if missing
            String sName = sLoopObj.optString("name", rawId);
            
            // NORMALIZE ID: This prevents duplicates from legacy/AI data
            String sId = normalizeSectionId(rawId, sType);
            
            SectionModel sectionTemplate = null;
            for (SectionModel all : ALL_SECTIONS) {
                if (all.id.equals(sId)) { sectionTemplate = all; break; }
            }
            if (sectionTemplate == null) {
                for (SectionModel all : ALL_SECTIONS) {
                    if (all.type.equals(sType)) { sectionTemplate = all; break; }
                }
            }
            
            SectionModel section;
            if (sectionTemplate != null) {
                // Use template defaults if available, but override with JSON values
                // Use the CANONICAL ID from the template to ensure consistency
                section = new SectionModel(sectionTemplate.id, sName, sectionTemplate.icon, sType, sectionTemplate.group);
            } else {
                section = new SectionModel(sId, sName, "fa-star", sType, "gridAdd");
            }
            section.items.clear();

            if (sLoopObj.has("items")) {
                JSONArray itemsArray = sLoopObj.getJSONArray("items");
                for (int j = 0; j < itemsArray.length(); j++) {
                    JSONObject itemObj = itemsArray.getJSONObject(j);
                    JSONArray fieldsArray = itemObj.optJSONArray("fields");
                    if (fieldsArray == null) continue;

                    List<FieldModel> itemFields = new ArrayList<>();
                    // Try to map fields to known structure
                    if (sectionTemplate != null && !sectionTemplate.items.isEmpty()) {
                         // Intelligent mapping: Use the template's field definitions as a base
                         // But if the JSON has fields NOT in the template, we should add them too?
                         // For now, stick to the schema defined in SectionModel to ensure UI renders correctly
                        for (FieldModel f : sectionTemplate.items.get(0).fields) {
                            FieldModel newF = new FieldModel(f.key, f.label, f.type);
                            boolean found = false;
                            for (int k = 0; k < fieldsArray.length(); k++) {
                                JSONObject fj = fieldsArray.getJSONObject(k);
                                if (fj.getString("key").equals(f.key)) {
                                    newF.value = fj.optString("value", "");
                                    found = true;
                                    break;
                                }
                            }
                            itemFields.add(newF);
                        }
                    } else {
                        // Fallback: Just load what's there
                        for (int k = 0; k < fieldsArray.length(); k++) {
                            JSONObject fj = fieldsArray.getJSONObject(k);
                            FieldModel fModel = new FieldModel(fj.getString("key"), fj.getString("key"), "text");
                            fModel.value = fj.optString("value", "");
                            itemFields.add(fModel);
                        }
                    }
                    section.items.add(new ItemModel(itemFields));
                }
            }
            if (section.items.isEmpty()) section.addItem();
            currentSections.add(section);
        }
    }

    private void updateSectionsBasedOnTemplate() {
        String templateJson = getSelectedTemplateJson();
        
        // Check if this template has a header (works for both JSON and default templates)
        boolean hasHeader = templateHasHeader();
        
        // Process template JSON if available
        if (templateJson != null) {
            try {
                JSONObject template = new JSONObject(templateJson);
                
                if (template.has("sections")) {
                    // Template defines structure! Use it.
                    Log.d("StepByStep", "Applying template structure from JSON...");
                    
                    // Backup current data
                    List<SectionModel> oldSections = new ArrayList<>(currentSections);
                    
                    loadSectionsFromJsonArray(template.getJSONArray("sections"));
                    
                    // Restore data where IDs match
                    for (SectionModel newSec : currentSections) {
                        for (SectionModel oldSec : oldSections) {
                            if (oldSec.id.equals(newSec.id)) {
                                if (oldSec.items.size() > 1 || (oldSec.items.size() == 1 && !isItemEmpty(oldSec.items.get(0)))) {
                                    newSec.items = oldSec.items; 
                                }
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("StepByStep", "Error updating sections from template", e);
            }
        }
        
        // Header management: Substitute with granular if header-less, or revert if header-ful
        if (!hasHeader) {
            Log.d("StepByStep", "Template has no header -> granular");
            substituteHeaderWithGranularSections();
        } else {
            Log.d("StepByStep", "Template has header -> standard");
            revertGranularToHeaderIfNecessary();
        }
        
        // Force refresh the ViewPager adapter to ensure Step 3 (Sections) shows the changes
        RecyclerView.Adapter<?> adapter = viewPager.getAdapter();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        
        updateWebViewPreview();
    }
    
    /**
     * Substitute headerSection with nameProfessionSection when template has no header.
     * Also migrates data from headerSection fields to the new sections.
     */
    private void substituteHeaderWithGranularSections() {
        SectionModel headerSection = null;
        int headerIndex = -1;
        
        // Find headerSection
        for (int i = 0; i < currentSections.size(); i++) {
            if (currentSections.get(i).id.equals("headerSection")) {
                headerSection = currentSections.get(i);
                headerIndex = i;
                break;
            }
        }
        
        if (headerSection == null) {
            Log.d("StepByStep", "Substitution: No header found, checking for missing granular sections");
            // No headerSection to substitute, check if identity/contact already exist
            boolean hasIdentity = false, hasProfile = false;
            for (SectionModel s : currentSections) {
                if (s.id.equals("nameProfessionSection")) hasIdentity = true;
                if (s.id.equals("profileSection")) hasProfile = true;
            }
            
            // Add missing sections at the beginning
            if (!hasProfile) {
                SectionModel profileDef = findSectionById("profileSection");
                if (profileDef != null) currentSections.add(0, new SectionModel(profileDef.id, profileDef.name, profileDef.icon, profileDef.type, profileDef.group));
            }
            if (!hasIdentity) {
                SectionModel identitySec = findSectionById("nameProfessionSection");
                if (identitySec != null) {
                    SectionModel instance = new SectionModel(identitySec.id, identitySec.name, identitySec.icon, identitySec.type, identitySec.group);
                    instance.isExpanded = true;
                    currentSections.add(0, instance);
                }
            }
            return;
        }
        
        // Remove headerSection
        currentSections.remove(headerIndex);
        
        // Find identity and contact section definitions
        SectionModel identityDef = findSectionById("nameProfessionSection");

        // Create new Profile Picture section
        SectionModel profileDef = findSectionById("profileSection");
        if (profileDef != null) {
            SectionModel profileSec = new SectionModel(profileDef.id, profileDef.name, profileDef.icon, profileDef.type, profileDef.group);
            // Add after Contact (or Identity if Contact missing)
            int profilePos = headerIndex;
            if (identityDef != null) profilePos++;
            
            if (profilePos < currentSections.size()) {
                currentSections.add(profilePos, profileSec);
            } else {
                currentSections.add(profileSec);
            }
        }
    }

    private void revertGranularToHeaderIfNecessary() {
        boolean hasHeader = false;
        int identityIndex = -1;
        for (int i = 0; i < currentSections.size(); i++) {
            if (currentSections.get(i).id.equals("headerSection")) hasHeader = true;
            if (currentSections.get(i).id.equals("nameProfessionSection")) identityIndex = i;
        }

        if (hasHeader) {
            Log.d("StepByStep", "Revert: Header already present, skipping");
            return; 
        }
        
        if (identityIndex == -1) {
            Log.d("StepByStep", "Revert: No Identity found to migrate from, adding blank Header");
            // No identity but template wants header? Just add it at top and hope for the best
            SectionModel hdr = findSectionById("headerSection");
            if (hdr != null) currentSections.add(0, new SectionModel(hdr.id, hdr.name, hdr.icon, hdr.type, hdr.group));
            return;
        }

        // Migrate data back for Name and Contacts
        SectionModel identitySec = currentSections.get(identityIndex);
        SectionModel profileSec = null;
        
        for (SectionModel s : currentSections) {
            if (s.id.equals("profileSection")) profileSec = s;
        }

        SectionModel headerDef = findSectionById("headerSection");
        if (headerDef != null) {
            SectionModel headerInstance = new SectionModel(headerDef.id, headerDef.name, headerDef.icon, headerDef.type, headerDef.group);
            ItemModel headerItem = headerInstance.items.get(0);

            // Migrate Name
            if (!identitySec.items.isEmpty()) {
                String name = getFieldValue(identitySec.items.get(0), "name");
                if (!name.equals("...")) setFieldValue(headerItem, "name", name);
            }


            // Replace granular with Header
            currentSections.add(identityIndex, headerInstance);
            if (profileSec != null) currentSections.remove(profileSec);
            currentSections.remove(identitySec);
        }
    }

    private void setFieldValue(ItemModel item, String key, String value) {
        if (value.equals("...")) return;
        for (FieldModel f : item.fields) {
            if (f.key.equals(key)) {
                f.value = value;
                return;
            }
        }
    }
    
    private SectionModel findSectionById(String id) {
        for (SectionModel sec : ALL_SECTIONS) {
            if (sec.id.equals(id)) return sec;
        }
        return null;
    }
    
    private boolean isItemEmpty(ItemModel item) {
        for (FieldModel f : item.fields) {
            if (!f.value.isEmpty() && !f.value.equals("...")) return false;
        }
        return true;
    }

    private void setupWizardWebView() {
        android.webkit.WebSettings settings = wizardWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        // Enable Native Zoom
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false); // Hide the ugly buttons
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        wizardWebView.setWebViewClient(new android.webkit.WebViewClient() {
            @Override
            public void onPageFinished(android.webkit.WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d("StepByStep", "Wizard WebView loaded, syncing...");
                
                // Hide unnecessary editor UI elements in Wizard Preview
                String hideJs = "var style = document.createElement('style'); " +
                                "style.innerHTML = '.controls, .fab-container, #fabBtn, .floating-spacing-control, .floating-column-handle { display: none !important; }'; " +
                                "document.head.appendChild(style);";
                view.evaluateJavascript(hideJs, null);
                
                updateWebViewPreview();
                applyZoom();
            }

            @Override
            public boolean shouldOverrideUrlLoading(android.webkit.WebView view, String url) {
                if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("mailto:") || url.startsWith("tel:")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
        wizardWebView.loadUrl("file:///android_asset/index.html");
    }

    private void updateWebViewPreview() {
        if (wizardWebView == null) return;
        
        try {
            String templateJson = getSelectedTemplateJson();
            
            if (templateJson != null) {
                // Use applyUserTemplate for Asset/User templates
                JSONObject data = generateCleanDataJson();
                String safeTemplate = templateJson.replace("\\", "\\\\").replace("'", "\\'").replace("\n", " "); // Simple sanitization
                String js = "if(window.applyUserTemplate) { window.applyUserTemplate('" + safeTemplate + "', " + data.toString() + "); }";
                wizardWebView.evaluateJavascript(js, null);
            } else {
                // Fallback for default/sidebar (Legacy Mode)
                JSONObject state = generateStateJson();
                String layout = (selectedTemplate.equals("default")) ? "null" : "'" + selectedTemplate + "'";
                String js = "if(window.loadResumeData) { window.loadResumeData(" + state.toString() + ", " + layout + "); }";
                wizardWebView.evaluateJavascript(js, null);
            }
        } catch (Exception e) {
            Log.e("StepByStep", "Error updating preview", e);
        }
    }

    private void applyZoom() {
        if (wizardWebView != null) {
            wizardWebView.setInitialScale(currentZoom);
        }
    }

    private void launchEditor() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("EXTRA_FROM_STEP_BY_STEP", true);
        
        ArrayList<String> sectionIds = new ArrayList<>();
        for (SectionModel s : currentSections) sectionIds.add(s.id);
        intent.putStringArrayListExtra("EXTRA_STEP_BY_STEP_SECTIONS", sectionIds);
        intent.putExtra("EXTRA_TARGET_LAYOUT", selectedTemplate);
        
        // Pass full data
        try {
            String templateJson = getSelectedTemplateJson();
            if (templateJson != null) {
                intent.putExtra("EXTRA_TEMPLATE_JSON", templateJson);
                intent.putExtra("EXTRA_STEP_BY_STEP_DATA", generateCleanDataJson().toString());
            } else {
                intent.putExtra("EXTRA_STEP_BY_STEP_DATA", generateStateJson().toString());
            }
            
            // Auto-Generate Filename
            String autoPath = generateAutoFilename();
            if (autoPath != null) {
                intent.putExtra("EXTRA_GENERATED_FILEPATH", autoPath);
            }
        } catch (Exception e) {
            Log.e("StepByStep", "Error generating launch data", e);
        }
        
        startActivity(intent);
        finish();
    }

    private String generateAutoFilename() {
        try {
            String name = "User";
            // extract name from header section if available
            for (SectionModel s : currentSections) {
                if (s.id.equals("headerSection") && !s.items.isEmpty()) {
                    for (FieldModel f : s.items.get(0).fields) {
                        if (f.key.equals("name") && !f.value.isEmpty()) {
                            name = f.value;
                            break;
                        }
                    }
                }
            }
            
            // Get Last Name
            String lastName = name.trim();
            if (lastName.contains(" ")) {
                lastName = lastName.substring(lastName.lastIndexOf(" ") + 1);
            }
            // Sanitize filename
            lastName = lastName.replaceAll("[^a-zA-Z0-9]", "");
            if (lastName.isEmpty()) lastName = "User";
            
            String purpose = selectedPurpose.substring(0, 1).toUpperCase() + selectedPurpose.substring(1);
            
            File dir = new File(getFilesDir(), "resumes");
            if (!dir.exists()) dir.mkdirs();
            
            // Format: [LastName] [Purpose] CV [Count]
            // e.g. Doe Job CV 1.json
            int count = 1;
            File file;
            do {
                String fname;
                if (count == 1) {
                    fname = String.format("%s %s CV.json", lastName, purpose);
                } else {
                    fname = String.format("%s %s CV %d.json", lastName, purpose, count);
                }
                file = new File(dir, fname);
                count++;
            } while (file.exists());
            
            return file.getAbsolutePath();
            
        } catch (Exception e) {
            Log.e("StepByStep", "Error generating filename", e);
            return null;
        }
    }

    // ViewPager Adapter
    private class StepsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            if (viewType == 0) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.step_purpose, parent, false);
                return new PurposeViewHolder(view);
            } else if (viewType == 1) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.step_template, parent, false);
                return new TemplateViewHolder(view);
            } else {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.step_sections, parent, false);
                return new SectionsViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof PurposeViewHolder) {
                ((PurposeViewHolder) holder).bind();
            } else if (holder instanceof TemplateViewHolder) {
                ((TemplateViewHolder) holder).bind();
            } else if (holder instanceof SectionsViewHolder) {
                ((SectionsViewHolder) holder).bind();
            }
        }

        @Override
        public int getItemCount() { return 3; }

        @Override
        public int getItemViewType(int position) { return position; }
    }

    // ViewHolders for each step
    private class PurposeViewHolder extends RecyclerView.ViewHolder {
        RecyclerView rv;
        PurposeViewHolder(View v) { super(v); rv = v.findViewById(R.id.rvPurposes); }
        void bind() {
            List<PurposeModel> purposes = Arrays.asList(
                new PurposeModel("job", "Job", "Standard job application", R.drawable.avd_purpose_job),
                new PurposeModel("job_abroad", "Job Abroad", "International opportunities", R.drawable.avd_purpose_job_abroad),
                new PurposeModel("academic", "Academic", "University & research roles", R.drawable.avd_purpose_academic),
                new PurposeModel("study_abroad", "Study Abroad", "Applying for education abroad", R.drawable.avd_purpose_study_abroad),
                new PurposeModel("marriage", "Marriage", "Personal biodata for marriage", R.drawable.avd_purpose_marriage)
            );
            rv.setLayoutManager(new GridLayoutManager(StepByStepActivity.this, 2));
            rv.setAdapter(new PurposeAdapter(purposes));
        }
    }

    private class TemplateViewHolder extends RecyclerView.ViewHolder {
        RecyclerView rv;
        TextView tvNotice;
        TemplateViewHolder(View v) { 
            super(v); 
            rv = v.findViewById(R.id.rvTemplates);
            tvNotice = v.findViewById(R.id.tvTemplateNotice);
            
            String notice = "<b>Note:</b> You can choose from <font color='#1E3C72'><b>many more</b></font> templates online, download new layouts, and switch at <font color='#1E3C72'><b>any time</b></font>. This selection is just for creating the <font color='#1E3C72'><b>base structure</b></font>.";
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                tvNotice.setText(Html.fromHtml(notice, Html.FROM_HTML_MODE_LEGACY));
            } else {
                tvNotice.setText(Html.fromHtml(notice));
            }
        }
        void bind() {
            List<TemplateModel> templates = new ArrayList<>();
            
            // 1. Load Asset Templates (Default)
            templates.addAll(loadAssetTemplates());
            
            // Fallback if no assets
            if (templates.isEmpty()) {
                templates.add(new TemplateModel("default", "Standard Modern"));
                templates.add(new TemplateModel("sidebar", "Sidebar Pro"));
            }

            // 2. Load User Templates - REMOVED per request
            // templates.addAll(loadUserTemplates());

            rv.setLayoutManager(new GridLayoutManager(StepByStepActivity.this, 2));
            rv.setAdapter(new TemplateAdapter(templates));
        }
    }

    private List<TemplateModel> loadUserTemplates() {
        List<TemplateModel> userTemplates = new ArrayList<>();
        File dir = new File(getFilesDir(), "user_templates");
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
            if (files != null) {
                // Sort by date (newest first)
                Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
                
                for (File file : files) {
                    try {
                        String content = readFile(file);
                        JSONObject json = new JSONObject(content);
                        String name = json.optString("name", file.getName().replace(".json", ""));
                        userTemplates.add(new TemplateModel(file.getName(), name, true, file.getAbsolutePath()));
                    } catch (Exception e) {
                        Log.e("StepByStep", "Error loading user template: " + file.getName(), e);
                    }
                }
            }
        }
        return userTemplates;
    }

    private List<TemplateModel> loadAssetTemplates() {
        List<TemplateModel> list = new ArrayList<>();
        try {
            String[] files = getAssets().list("default_templates");
            if (files != null) {
                for (String file : files) {
                    if (file.endsWith(".vitae")) {
                        String assetPath = "default_templates/" + file;
                        VitaeData data = loadVitaeAsset(assetPath);
                        String name = file.replace(".vitae", "");
                        if (name.equalsIgnoreCase("Classic")) name = "With Header";
                        else if (name.equalsIgnoreCase("Timeline")) name = "Headerless";
                        // Use filename as ID
                        list.add(new TemplateModel(file, name, true, assetPath, data.thumbnail));
                    }
                }
            }
        } catch (IOException e) {
            Log.e("StepByStep", "Error listing asset templates", e);
        }
        return list;
    }

    private VitaeData loadVitaeAsset(String assetPath) {
        VitaeData data = new VitaeData();
        try (InputStream is = getAssets().open(assetPath);
             ZipInputStream zis = new ZipInputStream(is)) {
            
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName().toLowerCase();
                if (name.endsWith(".json")) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = zis.read(buffer)) > 0) baos.write(buffer, 0, len);
                    data.json = baos.toString("UTF-8");
                } else if (name.endsWith(".png")) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = zis.read(buffer)) > 0) baos.write(buffer, 0, len);
                    byte[] bytes = baos.toByteArray();
                    data.thumbnail = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
            Log.e("StepByStep", "Error reading .vitae asset: " + assetPath, e);
        }
        return data;
    }

    private String readFile(File file) throws java.io.IOException {
        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();
        return sb.toString();
    }

    private class SectionsViewHolder extends RecyclerView.ViewHolder {
        RecyclerView rv;
        View btnAdd, btnReset;
        SectionsViewHolder(View v) { 
            super(v); 
            rv = v.findViewById(R.id.rvSections); 
            btnAdd = v.findViewById(R.id.btnAddSection);
            btnReset = v.findViewById(R.id.btnReset);
        }
        void bind() {
            rv.setLayoutManager(new LinearLayoutManager(StepByStepActivity.this));
            SectionListAdapter adapter = new SectionListAdapter();
            rv.setAdapter(adapter);
            sectionsRecyclerView = rv; // Store reference for keyboard navigation


            btnAdd.setOnClickListener(v -> showAddSectionDialog(adapter));
            btnReset.setOnClickListener(v -> {
                applyPurposeDefaults();
                adapter.notifyDataSetChanged();
                updateWebViewPreview();
            });
        }
    }

    private void showAddSectionDialog(SectionListAdapter adapter) {
        // Inflate the custom panel layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.panel_page_sections, null);
        
        // Hide Reset button for this context
        View btnReset = dialogView.findViewById(R.id.btn_reset_all);
        if (btnReset != null) btnReset.setVisibility(View.GONE);
        
        TextView titleTv = (TextView) ((android.view.ViewGroup)dialogView).getChildAt(0);
        if (titleTv != null) titleTv.setText("Add Section");

        LinearLayout leftCol = dialogView.findViewById(R.id.column_left);
        LinearLayout rightCol = dialogView.findViewById(R.id.column_right);
        
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .create();

        // Populate available sections
        List<SectionModel> available = new ArrayList<>();
        for (SectionModel sec : ALL_SECTIONS) {
            boolean exists = false;
            for (SectionModel current : currentSections) {
                if (current.id.equals(sec.id) || current.type.equals(sec.type)) { 
                    exists = true; 
                    break; 
                }
            }
            if (!exists) available.add(sec);
        }

        if (available.isEmpty()) {
            Toast.makeText(this, "All sections added!", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < available.size(); i++) {
            SectionModel s = available.get(i);
            View v = LayoutInflater.from(this).inflate(R.layout.item_section_add, (i % 2 == 0) ? leftCol : rightCol, false);

            TextView title = v.findViewById(R.id.section_title);
            TextView desc = v.findViewById(R.id.section_desc);
            View iconFrame = v.findViewById(R.id.section_icon).getParent() instanceof FrameLayout ? (View)v.findViewById(R.id.section_icon).getParent() : null;
            View iconContainer = v.findViewById(R.id.section_container); 
            ImageButton btnAdd = v.findViewById(R.id.btn_add_section);
            
            // Placement logic
            LinearLayout placementContainer = v.findViewById(R.id.placement_container);
            TextView placementIndicator = v.findViewById(R.id.tv_placement_indicator);
            ImageButton btnSwap = v.findViewById(R.id.btn_toggle_placement);
            final String[] placement = {"auto"}; // mutable ref

            title.setText(s.name);
            // Description is now hidden by default in XML for "Pill" style
            if (desc != null) desc.setVisibility(View.GONE);
            
            // Function to update indicator text
            Runnable updateIndicatorText = () -> {
                String cap = placement[0].substring(0, 1).toUpperCase() + placement[0].substring(1);
                placementIndicator.setText("Add to: " + cap);
            };
            updateIndicatorText.run();

            // Toggle expansion on text click
            iconContainer.setOnClickListener(view -> {
                if (placementContainer.getVisibility() == View.VISIBLE) {
                    placementContainer.setVisibility(View.GONE);
                } else {
                    placementContainer.setVisibility(View.VISIBLE);
                }
            });

            // Cycle placement options
            if (btnSwap != null) {
                btnSwap.setOnClickListener(vSwap -> {
                    switch (placement[0]) {
                        case "auto":   placement[0] = "left"; break;
                        case "left":   placement[0] = "right"; break;
                        case "right":  placement[0] = "header"; break;
                        case "header": placement[0] = "auto"; break;
                    }
                    updateIndicatorText.run();
                });
            }

            btnAdd.setOnClickListener(view -> {
                SectionModel instance = new SectionModel(s.id, s.name, s.icon, s.type, s.group, placement[0]);
                currentSections.add(instance);
                adapter.notifyItemInserted(currentSections.size() - 1);
                updateWebViewPreview();
                dialog.dismiss();
                Toast.makeText(this, s.name + " Added", Toast.LENGTH_SHORT).show();
            });

            if (i % 2 == 0) leftCol.addView(v);
            else rightCol.addView(v);
        }
        
        dialog.show();
    }

    private int getIconResForFontAwesome(String faIcon) {
        if (faIcon == null) return R.drawable.ic_add;
        switch (faIcon) {
            case "fa-id-card": 
            case "fa-user-tie": 
            case "fa-user-circle":
                return R.drawable.ic_info; // Personal info
            case "fa-passport": 
            case "fa-book":
                return R.drawable.ic_description; // Docs
            case "fa-language": 
                return R.drawable.ic_format_size; // Text/Language
            case "fa-graduation-cap": 
            case "fa-chalkboard-teacher":
                return R.drawable.ic_info; // Education
            case "fa-briefcase": 
            case "fa-laptop-code":
                return R.drawable.ic_grid; // Work/Exp
            case "fa-project-diagram": 
                return R.drawable.ic_list; // Projects
            case "fa-tools": 
            case "fa-palette":
            case "fa-camera":
                return R.drawable.ic_palette; // Skills/Visuals
            case "fa-certificate": 
            case "fa-user-check":
                return R.drawable.ic_check_v; // Verified/Cert
            case "fa-trophy": 
            case "fa-star":
            case "fa-hands-helping":
            case "fa-gamepad":
            case "fa-futbol":
            case "fa-running":
                return R.drawable.ic_highlight; // Highlights/Achievements
            case "fa-users": 
                return R.drawable.ic_info; // References
            case "fa-link": 
                return R.drawable.ic_zoom; // Links
            case "fa-heartbeat": 
                return R.drawable.ic_info;
            case "fa-file-signature": 
                return R.drawable.ic_edit;
            default: return R.drawable.ic_add;
        }
    }

    // Adapters for sub-lists
    private class PurposeAdapter extends RecyclerView.Adapter<PurposeAdapter.ViewHolder> {
        List<PurposeModel> data;
        PurposeAdapter(List<PurposeModel> data) { this.data = data; }
        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new ViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_step_purpose, p, false));
        }
        @Override public void onBindViewHolder(@NonNull ViewHolder h, int p) {
            PurposeModel m = data.get(p);
            h.tvTitle.setText(m.name);
            h.tvDesc.setText(m.desc);
            h.ivIcon.setImageResource(m.iconRes);
            h.itemView.setOnClickListener(v -> {
                selectedPurpose = m.id;
                notifyDataSetChanged();
                
                Drawable drawable = h.ivIcon.getDrawable();
                if (drawable instanceof Animatable) {
                    ((Animatable) drawable).start();
                }
            });
            // Highlight selected
            ((com.google.android.material.card.MaterialCardView)h.itemView).setStrokeColor(
                selectedPurpose.equals(m.id) ? 0xFF1A237E : 0xFFEEEEEE);
            ((com.google.android.material.card.MaterialCardView)h.itemView).setStrokeWidth(
                selectedPurpose.equals(m.id) ? 6 : 2);

            // If already selected, start animation anyway (optional but nice)
            if (selectedPurpose.equals(m.id)) {
                Drawable drawable = h.ivIcon.getDrawable();
                if (drawable instanceof Animatable) {
                    ((Animatable) drawable).start();
                }
            }
        }
        @Override public int getItemCount() { return data.size(); }
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDesc;
            ImageView ivIcon;
            ViewHolder(View v) { super(v); tvTitle = v.findViewById(R.id.tvPurposeTitle); tvDesc = v.findViewById(R.id.tvPurposeDesc); ivIcon = v.findViewById(R.id.ivPurposeIcon); }
        }
    }

    private class TemplateAdapter extends RecyclerView.Adapter<TemplateAdapter.ViewHolder> {
        List<TemplateModel> data;
        TemplateAdapter(List<TemplateModel> data) { this.data = data; }
        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new ViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_step_template, p, false));
        }
        @Override public void onBindViewHolder(@NonNull ViewHolder h, int p) {
            TemplateModel m = data.get(p);
            h.tvTitle.setText(m.name);
            
            if (m.isAssetTemplate && m.thumbnail != null) {
                h.ivPreview.setImageBitmap(m.thumbnail);
                h.ivPreview.setImageTintList(null);
                h.ivPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else if (m.isUserTemplate && m.filePath != null) {
                // Load saved thumbnail
                String imagePath = m.filePath.replace(".json", ".png");
                File imgFile = new File(imagePath);
                if (imgFile.exists()) {
                    android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    h.ivPreview.setImageBitmap(bitmap);
                    h.ivPreview.setImageTintList(null); // Remove tint for real images
                    h.ivPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                } else {
                    h.ivPreview.setImageResource(R.drawable.ic_grid);
                    h.ivPreview.setImageTintList(android.content.res.ColorStateList.valueOf(0xFF1A237E));
                    h.ivPreview.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                }
            } else {
                // Built-in templates
                h.ivPreview.setImageResource(R.drawable.ic_grid);
                h.ivPreview.setImageTintList(android.content.res.ColorStateList.valueOf(0xFF1A237E));
                h.ivPreview.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            }

            h.itemView.setOnClickListener(v -> {
                selectedTemplate = m.id;
                notifyDataSetChanged();
                
                // Load and Apply Template Styles (Colors/Metrics)
                new Thread(() -> {
                    String jsonStr = null;
                    if (m.isAssetTemplate && m.assetPath != null) {
                        jsonStr = loadVitaeAsset(m.assetPath).json;
                    } else if (m.isUserTemplate && m.filePath != null) {
                        try { jsonStr = readFile(new File(m.filePath)); } catch(Exception e) {}
                    }
                    
                    if (jsonStr != null) {
                        try {
                            JSONObject obj = new JSONObject(jsonStr);
                            if (obj.has("colors")) colorsData = obj.getJSONObject("colors");
                            if (obj.has("metrics")) metricsData = obj.getJSONObject("metrics");
                            
                            runOnUiThread(() -> {
                                // Toast.makeText(StepByStepActivity.this, "Style Applied: " + m.name, Toast.LENGTH_SHORT).show();
                                updateWebViewPreview();
                            });
                        } catch (JSONException e) {
                            Log.e("StepByStep", "Error parsing template style", e);
                        }
                    }
                }).start();
            });
            
            // Highlight selected
            ((com.google.android.material.card.MaterialCardView)h.itemView).setStrokeColor(
                selectedTemplate.equals(m.id) ? 0xFF1A237E : 0xFFEEEEEE);
            ((com.google.android.material.card.MaterialCardView)h.itemView).setStrokeWidth(
                selectedTemplate.equals(m.id) ? 6 : 2);
        }
        @Override public int getItemCount() { return data.size(); }
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle;
            ImageView ivPreview;
            ViewHolder(View v) { 
                super(v); 
                tvTitle = v.findViewById(R.id.tvTemplateTitle); 
                ivPreview = v.findViewById(R.id.ivTemplatePreview);
            }
        }
    }

    private class SectionListAdapter extends RecyclerView.Adapter<SectionListAdapter.ViewHolder> {
        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new ViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_step_section, p, false));
        }
        @Override public void onBindViewHolder(@NonNull ViewHolder h, int p) {
            SectionModel m = currentSections.get(p);
            h.tvName.setText(m.name);
            
            // Handle Expansion - click expand button
            h.body.setVisibility(m.isExpanded ? View.VISIBLE : View.GONE);
            h.ivExpand.setRotation(m.isExpanded ? 180 : 0);
            
            h.ivExpand.setOnClickListener(v -> {
                boolean wasExpanded = m.isExpanded;
                for (SectionModel s : currentSections) s.isExpanded = false;
                m.isExpanded = !wasExpanded;
                notifyDataSetChanged();
                
                // Auto-scroll and highlight the expanded section in preview
                if (m.isExpanded) {
                    scrollToSectionInPreview(m.type);
                }
            });

            h.btnRemove.setOnClickListener(v -> {
                int currentPos = h.getAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    currentSections.remove(currentPos);
                    notifyItemRemoved(currentPos);
                    notifyItemRangeChanged(currentPos, currentSections.size());
                    updateWebViewPreview();
                }
            });
            
            // Setup Items RecyclerView
            h.rvItems.setLayoutManager(new LinearLayoutManager(StepByStepActivity.this));
            h.rvItems.setAdapter(new ItemListAdapter(m));
            
            // Only show Add Item if it's a multiple-item section
            boolean isMultiple = !Arrays.asList("header", "personal", "passport", "summary_paragraph", "declaration_block", "physical").contains(m.type);
            h.btnAddItem.setVisibility(isMultiple ? View.VISIBLE : View.GONE);
            h.btnAddItem.setOnClickListener(v -> {
                m.addItem();
                h.rvItems.getAdapter().notifyItemInserted(m.items.size() - 1);
                updateWebViewPreview();
            });
        }
        @Override public int getItemCount() { return currentSections.size(); }
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName;
            ImageButton btnRemove;
            View body, btnAddItem;
            ImageButton ivExpand;
            RecyclerView rvItems;
            ViewHolder(View v) { 
                super(v); 
                tvName = v.findViewById(R.id.tvSectionName); 
                btnRemove = v.findViewById(R.id.btnRemove);
                body = v.findViewById(R.id.expandableBody);
                btnAddItem = v.findViewById(R.id.btnAddItem);
                ivExpand = v.findViewById(R.id.ivExpand);
                rvItems = v.findViewById(R.id.rvItems);
            }
        }
    }

    /**
     * Advances to the next section in the list:
     * 1. Finds the currently expanded section
     * 2. Collapses it
     * 3. Expands the next section
     * 4. Scrolls the RecyclerView to show the newly expanded section
     */
    private void advanceToNextSection() {
        int currentIndex = -1;
        for (int i = 0; i < currentSections.size(); i++) {
            if (currentSections.get(i).isExpanded) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex >= 0 && currentIndex < currentSections.size() - 1) {
            final int nextIndex = currentIndex + 1;
            
            // Collapse current section with animation
            currentSections.get(currentIndex).isExpanded = false;
            
            // Notify adapter about the collapsed item for animation
            if (sectionsRecyclerView != null && sectionsRecyclerView.getAdapter() != null) {
                sectionsRecyclerView.getAdapter().notifyItemChanged(currentIndex);
                
                // Delay the expansion slightly for a smooth visual effect
                sectionsRecyclerView.postDelayed(() -> {
                    // Expand next section
                    currentSections.get(nextIndex).isExpanded = true;
                    sectionsRecyclerView.getAdapter().notifyItemChanged(nextIndex);
                    
                    // Smooth scroll to the newly expanded section
                    sectionsRecyclerView.smoothScrollToPosition(nextIndex);
                    
                    // Focus the first input field in the new section after it expands
                    sectionsRecyclerView.postDelayed(() -> {
                        RecyclerView.ViewHolder vh = sectionsRecyclerView.findViewHolderForAdapterPosition(nextIndex);
                        if (vh != null && vh.itemView != null) {
                            EditText firstInput = vh.itemView.findViewById(R.id.etFieldValue);
                            if (firstInput != null) {
                                firstInput.requestFocus();
                            }
                        }
                    }, 200);
                }, 150);
            }

            // Auto-scroll preview to the new section
            scrollToSectionInPreview(currentSections.get(nextIndex).type);
            
            // NOTE: Keyboard stays open - no hiding
        }
    }

    private class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ViewHolder> {
        SectionModel section;
        ItemListAdapter(SectionModel section) { this.section = section; }
        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new ViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_wizard_item, p, false));
        }
        @Override public void onBindViewHolder(@NonNull ViewHolder h, int p) {
            ItemModel item = section.items.get(p);
            h.fieldsContainer.removeAllViews();
            for (FieldModel field : item.fields) {
                View fieldView = LayoutInflater.from(h.itemView.getContext()).inflate(R.layout.item_wizard_field, h.fieldsContainer, false);
                TextView label = fieldView.findViewById(R.id.tvFieldLabel);
                EditText input = fieldView.findViewById(R.id.etFieldValue);
                
                label.setText(field.label);
                
                View layoutImagePicker = fieldView.findViewById(R.id.layoutImagePicker);
                Button btnPickImage = fieldView.findViewById(R.id.btnPickImage);
                ImageView ivImagePreview = fieldView.findViewById(R.id.ivImagePreview);

                if (field.type.equals("image")) {
                    input.setVisibility(View.GONE);
                    layoutImagePicker.setVisibility(View.VISIBLE);
                    
                    // Show current image preview if exists
                    if (field.value != null && !field.value.isEmpty()) {
                        try {
                            String base64Data = field.value;
                            if (base64Data.contains(",")) base64Data = base64Data.split(",")[1];
                            byte[] decodedString = Base64.decode(base64Data, Base64.DEFAULT);
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            ivImagePreview.setImageBitmap(decodedByte);
                        } catch (Exception e) {
                            ivImagePreview.setImageResource(android.R.drawable.ic_menu_gallery);
                        }
                    } else {
                        ivImagePreview.setImageResource(android.R.drawable.ic_menu_gallery);
                    }

                    btnPickImage.setOnClickListener(v -> {
                        pendingImageField = field;
                        pendingImageView = ivImagePreview;
                        mGetContent.launch("image/*");
                    });

                } else {
                    input.setVisibility(View.VISIBLE);
                    layoutImagePicker.setVisibility(View.GONE);
                    input.setText(field.value);
                    if (field.type.equals("textarea")) {
                        input.setSingleLine(false);
                        input.setMinLines(3);
                        input.setMaxLines(10);
                        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                    } else {
                        input.setSingleLine(true);
                        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                        input.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                    }

                    // Scroll to keep input visible when focused + position cursor at end
                    input.setOnFocusChangeListener((v, hasFocus) -> {
                        if (hasFocus) {
                            // Position cursor at end of text for easy deletion
                            input.post(() -> input.setSelection(input.getText().length()));
                            
                            if (sectionsRecyclerView != null) {
                                v.post(() -> {
                                    // Scroll the parent RecyclerView to ensure this field is visible
                                    int[] location = new int[2];
                                    v.getLocationInWindow(location);
                                    int screenHeight = getResources().getDisplayMetrics().heightPixels;
                                    // If field is in bottom half, scroll to make it more visible
                                    if (location[1] > screenHeight / 2) {
                                        sectionsRecyclerView.smoothScrollBy(0, 150);
                                    }
                                });
                            }
                        }
                    });

                    input.addTextChangedListener(new TextWatcher() {
                        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                            field.value = s.toString();
                            updateWebViewPreview();
                        }
                        @Override public void afterTextChanged(Editable s) {}
                    });

                    // Check if this is the last field of the last item in this section
                    int itemIndex = h.getAdapterPosition();
                    int fieldIndex = item.fields.indexOf(field);
                    boolean isLastItem = (itemIndex == section.items.size() - 1);
                    boolean isLastField = (fieldIndex == item.fields.size() - 1);

                    if (isLastItem && isLastField && !field.type.equals("textarea")) {
                        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        input.setOnEditorActionListener((v, actionId, event) -> {
                            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                                advanceToNextSection();
                                return true;
                            }
                            return false;
                        });
                    }
                }
                h.fieldsContainer.addView(fieldView);
            }

            // Remove Item button (only if multiple items exist)
            h.btnRemoveItem.setVisibility(section.items.size() > 1 ? View.VISIBLE : View.GONE);
            h.btnRemoveItem.setOnClickListener(v -> {
                section.items.remove(p);
                notifyDataSetChanged();
                updateWebViewPreview();
            });
        }
        @Override public int getItemCount() { return section.items.size(); }
        class ViewHolder extends RecyclerView.ViewHolder {
            LinearLayout fieldsContainer;
            ImageButton btnRemoveItem;
            ViewHolder(View v) { 
                super(v); 
                fieldsContainer = v.findViewById(R.id.fieldsContainer);
                btnRemoveItem = v.findViewById(R.id.btnRemoveItem);
            }
        }
    }

    private JSONObject generateStateJson() throws JSONException {
        JSONObject state = new JSONObject();
        
        if (headerData == null) headerData = new JSONObject();

        // 1. Sync Header Data from List if available
        SectionModel headerSec = null;
        for (SectionModel s : currentSections) {
            if (s.id.equals("headerSection")) {
                headerSec = s;
                break;
            }
        }

        if (headerSec != null && !headerSec.items.isEmpty()) {
            try {
                // Map list fields back to header JSON structure
                ItemModel hItem = headerSec.items.get(0);
                headerData.put("name", getFieldValue(hItem, "name"));
                
                JSONArray cItems = new JSONArray();
                String[] contacts = {"email", "phone", "addr", "link"};
                String[] icons = {"fa-envelope", "fa-phone", "fa-map-marker-alt", "fa-link"};
                
                for (int i=0; i<contacts.length; i++) {
                    String val = getFieldValue(hItem, contacts[i]);
                    // Don't sync placeholders or empty strings
                    if (!val.equals("...") && !val.isEmpty()) {
                        JSONObject c = new JSONObject();
                        c.put("text", val);
                        c.put("icon", icons[i]);
                        c.put("column", (i % 2 == 0) ? "left" : "right");
                        cItems.put(c);
                    }
                }
                headerData.put("items", cItems);
            } catch (JSONException e) {
                Log.e("StepByStep", "Error syncing header data", e);
            }
            // FALLBACK: If headerSection is missing (e.g., in header-less template),
            // sync from Identity section if it exists in currentSections
            SectionModel identitySec = null;
            for (SectionModel s : currentSections) {
                if (s.id.equals("nameProfessionSection")) identitySec = s;
            }

            try {
                if (identitySec != null && !identitySec.items.isEmpty()) {
                    headerData.put("name", getFieldValue(identitySec.items.get(0), "name"));
                }
            } catch (JSONException e) {
                Log.e("StepByStep", "Error syncing granular data to headerData", e);
            }
        }
        
        StringBuilder html = new StringBuilder();
        
        // CHECK FOR GRANULAR HEADER SECTIONS: If they exist, skip the hardcoded header block
        boolean hasGranularHeader = false;
        for (SectionModel s : currentSections) {
            if (s.id.equals("nameProfessionSection") || s.id.equals("profileSection")) {
                hasGranularHeader = true;
                break;
            }
        }

        // Split sections into Layout groups with Centralized Balancing Logic
        List<SectionModel> objectiveSecs = new ArrayList<>();
        List<SectionModel> footerSecs = new ArrayList<>();
        List<SectionModel> leftSecs = new ArrayList<>();
        List<SectionModel> rightSecs = new ArrayList<>();
        List<SectionModel> headerSecs = new ArrayList<>();

        Map<String, String> colMap = calculateColumnAssignments();

        for (SectionModel s : currentSections) {
            if (s.id.equals("headerSection")) continue;
            if (s.id.equals("summarySection")) {
                objectiveSecs.add(s);
                continue;
            }
            if (s.id.equals("declarationSection")) {
                footerSecs.add(s);
                continue;
            }

            String col = colMap.get(s.id);
            if ("left".equals(col)) {
                leftSecs.add(s);
            } else if ("header".equals(col)) {
                headerSecs.add(s);
            } else {
                rightSecs.add(s);
            }
        }

        if (!hasGranularHeader) {
            html.append("<header id=\"mainHeader\">");
            
            if (headerData != null) {
                html.append("<div class=\"header-info\"><h1>").append(headerData.optString("name", "Your Name")).append("</h1>");
                html.append("<div class=\"contact-info\">");
                
                JSONArray itemsArr = headerData.optJSONArray("items");
                if (itemsArr != null) {
                    StringBuilder leftStr = new StringBuilder("<div class=\"header-column-left\" data-column=\"left\">");
                    StringBuilder rightStr = new StringBuilder("<div class=\"header-column-right\" data-column=\"right\">");
                    for (int i = 0; i < itemsArr.length(); i++) {
                        JSONObject item = itemsArr.getJSONObject(i);
                        String col = item.optString("column", "left");
                        StringBuilder target = col.equals("left") ? leftStr : rightStr;
                        
                        target.append("<div class=\"contact-item\" data-column=\"").append(col).append("\">");
                        target.append("<i class=\"fas ").append(item.optString("icon", "").replace("fas ", "")).append("\"></i> ");
                        String link = item.optString("link", "");
                        if (!link.isEmpty() && !link.equals("javascript:void(0)")) {
                            target.append("<a href=\"").append(link).append("\" target=\"_blank\">").append(item.optString("text", "")).append("</a>");
                        } else {
                            target.append("<span>").append(item.optString("text", "")).append("</span>");
                        }
                        target.append("</div>");
                    }
                    leftStr.append("</div>");
                    rightStr.append("</div>");
                    html.append(leftStr).append(rightStr);
                }
                html.append("</div></div>");
            }
            // Add sections explicitly moved to header
            for (SectionModel s : headerSecs) html.append(generateSectionWrapper(s));
            html.append("</header>");
        } else {
            // If main header is skipped, still show these "header" sections at the top
            for (SectionModel s : headerSecs) html.append(generateSectionWrapper(s));
        }
        
        for (SectionModel s : objectiveSecs) html.append(generateSectionWrapper(s));

        html.append("<div class=\"main-content two-column\"><div class=\"column-resizer\" id=\"colResizer\"></div>");
        html.append("<div class=\"left-column\" id=\"leftCol\">");
        for (SectionModel s : leftSecs) html.append(generateSectionWrapper(s));
        html.append("</div><div class=\"right-column\" id=\"rightCol\">");
        for (SectionModel s : rightSecs) html.append(generateSectionWrapper(s));
        html.append("</div></div>");

        for (SectionModel s : footerSecs) html.append(generateSectionWrapper(s));
        
        state.put("html", html.toString());
        
        if (colorsData != null && colorsData.length() > 0) {
            state.put("colors", colorsData);
        } else {
            JSONObject colors = new JSONObject();
            colors.put("header", "#1e3c72"); colors.put("left", "#f7f9fc"); colors.put("right", "#ffffff"); 
            colors.put("text", "#333333"); colors.put("headerText", "#ffffff");
            state.put("colors", colors);
        }
        
        if (metricsData != null && metricsData.length() > 0) {
            state.put("metrics", metricsData);
        } else {
            JSONObject metrics = new JSONObject();
            metrics.put("width", "35%"); metrics.put("spacing", 50); metrics.put("header", 100); 
            metrics.put("radius", 0); metrics.put("cardRadius", 8); metrics.put("globalFontSize", 14);
            state.put("metrics", metrics);
        }
        
        state.put("sig", signatureData);
        
        JSONObject flags = new JSONObject();
        flags.put("iconsHidden", false); flags.put("breaksHidden", false); flags.put("isEditing", false);
        state.put("flags", flags);
        
        return state;
    }

    private String getSelectedTemplateJson() {
        // Handle built-in hardcoded types (no JSON)
        if (selectedTemplate.equals("default") || selectedTemplate.equals("sidebar")) {
            return null; 
        }

        // Check Assets
        List<TemplateModel> assets = loadAssetTemplates();
        for (TemplateModel m : assets) {
            if (m.id.equals(selectedTemplate) && m.assetPath != null) {
                return loadVitaeAsset(m.assetPath).json;
            }
        }

        // Check User Templates
        List<TemplateModel> userTemplates = loadUserTemplates();
        for (TemplateModel m : userTemplates) {
            if (m.id.equals(selectedTemplate) && m.filePath != null) {
                try {
                    return readFile(new File(m.filePath));
                } catch (IOException e) {
                    Log.e("StepByStep", "Error reading user template", e);
                }
            }
        }
        
        return null;
    }

    private JSONObject generateCleanDataJson() throws JSONException {
        JSONObject root = new JSONObject();
        JSONObject currentDataByDataType = new JSONObject();
        JSONObject sharedData = new JSONObject();

        // 1. Extract Shared Data (Header/Identity/Contact)
        SectionModel headerSec = null;
        SectionModel identitySec = null;
        SectionModel profileSec = null;
        SectionModel personalSec = null;
        for (SectionModel s : currentSections) {
            if (s.id.equals("headerSection")) headerSec = s;
            if (s.type.equals("identity")) identitySec = s;
            if (s.type.equals("profile_pic") || s.id.equals("profileSection")) profileSec = s;
            if (s.type.equals("personal")) personalSec = s;
        }
        
        String name = "Your Name";
        String title = "Professional Title";
        JSONArray contacts = new JSONArray();
        String profileImg = "";

        if (profileSec != null && !profileSec.items.isEmpty()) {
            profileImg = getFieldValue(profileSec.items.get(0), "profile_pic");
        }

        // Priority 1: Standard Header
        if (headerSec != null && !headerSec.items.isEmpty()) {
            ItemModel hItem = headerSec.items.get(0);
            name = getFieldValue(hItem, "name");
            
            // Check if headerSec has its own profile_pic field
            String hImg = getFieldValue(hItem, "profile_pic");
            if (!hImg.equals("...") && !hImg.isEmpty()) profileImg = hImg;
            
            // Collect standard contacts
            String[] contactKeys = {"email", "phone", "addr", "link"};
            String[] icons = {"fa-envelope", "fa-phone", "fa-map-marker-alt", "fa-link"};
            
            for (int i = 0; i < contactKeys.length; i++) {
                String val = getFieldValue(hItem, contactKeys[i]);
                if (!val.equals("...") && !val.isEmpty()) {
                    JSONObject c = new JSONObject();
                    c.put("text", val);
                    c.put("iconClass", "fas " + icons[i]);
                    contacts.put(c);
                }
            }
        } 
        // Priority 2: Granular Identity Sections
        else {
            if (identitySec != null && !identitySec.items.isEmpty()) {
                ItemModel iItem = identitySec.items.get(0);
                String n = getFieldValue(iItem, "name");
                String t = getFieldValue(iItem, "title");
                if (!n.equals("...") && !n.isEmpty()) name = n;
                if (!t.equals("...") && !t.isEmpty()) title = t;
                
                // Also check identity for image
                String iImg = getFieldValue(iItem, "profile_pic");
                if (!iImg.equals("...") && !iImg.isEmpty()) profileImg = iImg;
            }
        }
        
        sharedData.put("name", name);
        sharedData.put("title", title);
        sharedData.put("contacts", contacts);
        sharedData.put("image", profileImg);

        JSONArray personalArr = new JSONArray();
        if (personalSec != null && !personalSec.items.isEmpty()) {
            ItemModel pItem = personalSec.items.get(0);
            for (FieldModel f : pItem.fields) {
                if (f.value != null && !f.value.equals("...") && !f.value.isEmpty()) {
                    JSONObject pObj = new JSONObject();
                    pObj.put("label", f.label);
                    pObj.put("text", f.value);
                    personalArr.put(pObj);
                }
            }
        }
        sharedData.put("personal", personalArr);
        
        // 2. Generate HTML for all sections
        for (SectionModel s : currentSections) {
             if (s.id.equals("headerSection")) continue; // Header is handled via sharedData usually, but let's provide it just in case? 
             // complex templates might use data-type="headerSection" to inject HTML directly.
             // generateStateJson generates <header id="mainHeader">... 

             StringBuilder sb = new StringBuilder();
             // We don't need the <section> wrapper here because applyUserTemplate hydrates INNER HTML of existing sections
             // OR creates new sections if missing.
             // But generateSectionWrapper returns the FULL <section>.
             // applyUserTemplate's hydration logic (hydrateSection) replaces innerHTML.
             // correct.
             
             // However, for Missing sections, applyUserTemplate CREATES them using the content.
             // We should provide the full section HTML for safety, 
             // but currentDataByDataType expects content keyed by type.
             
             // Let's stick to what extractSection does in applyUserTemplate:
             // it stores sec.innerHTML.
             
             // So we should generate the INNER content.
             // Section Heading + Content Div?
             // In generateSectionWrapper: <h2>...</h2><div class="content-area">...</div>
             
             StringBuilder inner = new StringBuilder();
             inner.append("<h2><i class=\"fas ").append(s.icon).append("\"></i> ").append(s.name.toUpperCase()).append("</h2>");
             inner.append("<div class=\"content-area\">");
             for (ItemModel item : s.items) {
                 inner.append(generateItemHTML(s.type, item));
             }
             inner.append("</div>");
             
             // Key by ID or Type? applyUserTemplate checks data-type first, then ID.
             // We'll use ID As the key since that's unique.
             currentDataByDataType.put(s.id, inner.toString());
             // Also put by type for fallback
             currentDataByDataType.put(s.type, inner.toString());
        }
        
        root.put("currentDataByDataType", currentDataByDataType);
        root.put("sharedData", sharedData);
        
        // Pass centralized column assignments
        JSONObject colAssignments = new JSONObject();
        Map<String, String> colMap = calculateColumnAssignments();
        for (Map.Entry<String, String> entry : colMap.entrySet()) {
            colAssignments.put(entry.getKey(), entry.getValue());
        }
        root.put("columnAssignments", colAssignments);
        
        // Pass colors/metrics too if we want to preserve customizations made in Wizard (if any)
        // But Wizard currently doesn't allow editing colors/metrics, it only applies template defaults.
        // So we can skip them or pass what's in metricsData/colorsData if they were loaded from a saved state.
        if (colorsData != null) root.put("colors", colorsData);
        if (metricsData != null) root.put("metrics", metricsData);
        if (signatureData != null) root.put("sig", signatureData);
        
        return root;
    }
    private String generateSectionWrapper(SectionModel section) {
        StringBuilder sb = new StringBuilder();
        String extraClass = "";
        if (section.id.equals("summarySection")) extraClass = "objective";
        else if (section.id.equals("personalDetails")) extraClass = "personal-details";
        else if (section.id.equals("declarationSection")) extraClass = "declaration";

        // Add 'resume-section' class and data-type for scrolling and JS recognition
        sb.append("<section class=\"resume-section ").append(extraClass).append("\" id=\"").append(section.id).append("\" data-type=\"").append(section.type).append("\">");
        sb.append("<h2><i class=\"fas ").append(section.icon).append("\"></i> ").append(section.name.toUpperCase()).append("</h2>");
        sb.append("<div class=\"content-area\">");
        for (ItemModel item : section.items) {
            sb.append(generateItemHTML(section.type, item));
        }
        sb.append("</div></section>");
        return sb.toString();
    }

    private String generateItemHTML(String type, ItemModel item) {
        StringBuilder sb = new StringBuilder();
        switch (type) {
            case "personal":
                for(FieldModel f : item.fields) {
                    sb.append("<div class=\"pd-row\"><span class=\"pd-label\">").append(f.label).append(":</span> <span class=\"pd-val\">")
                      .append(f.value.isEmpty() ? "N/A" : f.value).append("</span></div>");
                }
                break;
            case "identity":
            case "name_profession":
                sb.append("<div class=\"data-table-item\" style=\"text-align:center;\">");
                sb.append("<h3>").append(getFieldValue(item, "name")).append("</h3>");
                sb.append("<div style=\"color:var(--primary-color);\">").append(getFieldValue(item, "prof")).append("</div>");
                sb.append("</div>");
                break;
            case "summary_paragraph":
                sb.append("<div class=\"summary-text\">").append(getFieldValue(item, "summary")).append("</div>");
                break;
            case "education":
                sb.append("<div class=\"data-table-item\"><div class=\"table-row\"><span class=\"table-label\">Institute:</span> <span class=\"table-val\" style=\"font-weight:600;\">")
                  .append(getFieldValue(item, "inst")).append("</span></div>");
                sb.append("<div class=\"table-row\"><span class=\"table-label\">Year:</span> <span class=\"table-val\">").append(getFieldValue(item, "year")).append("</span>");
                sb.append("<span class=\"table-label\" style=\"margin-left:14px;\">Board:</span> <span class=\"table-val\">").append(getFieldValue(item, "board")).append("</span></div>");
                sb.append("<div class=\"table-row\"><span class=\"table-label\">Degree:</span> <span class=\"table-val\">").append(getFieldValue(item, "deg")).append("</span>");
                String gpa = getFieldValue(item, "gpa");
                if (!gpa.equals("...") && !gpa.isEmpty()) {
                    sb.append("<span style=\"margin-left:auto; font-size:0.85em; opacity:0.8;\">GPA: ").append(gpa).append("</span>");
                }
                sb.append("</div></div>");
                break;
            case "experience":
            case "volunteer":
            case "internships":
                sb.append("<div class=\"data-table-item\"><div class=\"table-row\" style=\"justify-content: space-between;\"><span class=\"table-val\" style=\"font-weight:700;\">")
                  .append(getFieldValue(item, "comp")).append("</span><span class=\"table-val\" style=\"font-size:0.85em; color: var(--text-muted);\">")
                  .append(getFieldValue(item, "dur")).append("</span></div>");
                sb.append("<div class=\"table-row\"><span class=\"table-val exp-role\">").append(getFieldValue(item, "role")).append("</span></div>");
                sb.append("<ul class=\"resp-list\">");
                String desc = getFieldValue(item, "desc");
                if (desc.equals("...")) {
                    sb.append("<li>...</li>");
                } else {
                    String[] bullets = desc.split("\n");
                    for (String b : bullets) {
                        if (!b.trim().isEmpty()) {
                            sb.append("<li>").append(b.trim()).append("</li>");
                        }
                    }
                }
                sb.append("</ul></div>");
                break;
            case "projects":
                sb.append("<div class=\"data-table-item\"><div class=\"table-row\" style=\"justify-content: space-between;\"><span class=\"table-val\" style=\"font-weight:700;\">")
                  .append(getFieldValue(item, "name")).append("</span><span class=\"table-val\" style=\"font-size:0.85em; color: var(--text-muted);\">")
                  .append(getFieldValue(item, "year")).append("</span></div>");
                sb.append("<div class=\"proj-desc\">").append(getFieldValue(item, "desc")).append("</div>");
                sb.append("<div class=\"table-row\" style=\"margin-top:5px;\"><a href=\"#\" style=\"font-size:0.85em; color:var(--primary-color);\">")
                  .append(getFieldValue(item, "link")).append("</a></div></div>");
                break;
            case "skills":
            case "hobbies":
            case "interests":
                sb.append("<div class=\"skill-group\"><span class=\"skill-header\">").append(getFieldValue(item, "cat")).append("</span>");
                sb.append("<div class=\"skill-sub\">").append(getFieldValue(item, "vals")).append("</div></div>");
                break;
            case "languages":
                sb.append("<div class=\"data-table-item\"><div class=\"table-row\"><span class=\"table-label\">")
                  .append(getFieldValue(item, "lang")).append(":</span> <span class=\"table-val\">")
                  .append(getFieldValue(item, "lvl")).append("</span></div></div>");
                break;
            case "passport":
                sb.append("<div class=\"data-table-item\">");
                sb.append("<div class=\"table-row\"><span class=\"table-label\">Passport No:</span> <span class=\"table-val\">").append(getFieldValue(item, "pno")).append("</span></div>");
                sb.append("<div class=\"table-row\"><span class=\"table-label\">Issued By:</span> <span class=\"table-val\">").append(getFieldValue(item, "issued")).append("</span></div>");
                sb.append("<div class=\"table-row\"><span class=\"table-label\">Issue Date:</span> <span class=\"table-val\">").append(getFieldValue(item, "idate")).append("</span></div>");
                sb.append("<div class=\"table-row\"><span class=\"table-label\">Expiry Date:</span> <span class=\"table-val\">").append(getFieldValue(item, "edate")).append("</span></div>");
                sb.append("</div>");
                break;
            case "weblinks":
                sb.append("<div class=\"data-table-item link-item\"><div class=\"table-row\" style=\"align-items:flex-start; gap:12px\"><div class=\"link-icon-container\"><i class=\"fas fa-globe fa-2x\"></i></div><div style=\"flex:1\"><div class=\"table-row\"><a href=\"#\" class=\"table-val\" style=\"font-weight:700; text-decoration:underline\">")
                  .append(getFieldValue(item, "name")).append("</a></div><div class=\"proj-desc\">").append(getFieldValue(item, "link")).append("</div></div></div></div>");
                break;
            case "physical":
                sb.append("<div class=\"physics-grid\">");
                for (FieldModel f : item.fields) {
                    sb.append("<div class=\"pd-row\"><span class=\"pd-label\">").append(f.label).append(":</span> <span class=\"pd-val\">").append(f.value).append("</span></div>");
                }
                sb.append("</div>");
                break;
            case "visa":
            case "test_scores":
            case "family":
            case "lifestyle":
            case "astrological":
            case "expectations":
            case "familyDetails":     // Keep IDs for backward compatibility if any
            case "lifestyleHabits":
            case "astrologySection":
            case "partnerExpectations":
            case "testScores":
                for (FieldModel f : item.fields) {
                    if (f.value == null || f.value.isEmpty() || f.value.equals("...")) continue;
                    sb.append("<div class=\"pd-row\"><span class=\"pd-label\">").append(f.label).append(":</span> <span class=\"pd-val\">").append(f.value).append("</span></div>");
                }
                break;

            case "researchExp":
            case "teachingExp":
            case "grants":
                sb.append("<div class=\"data-table-item\" style=\"border-bottom:1px solid #E0E0E0; padding-bottom:12px; margin-bottom:12px;\">");
                sb.append("<div class=\"table-row\" style=\"justify-content:space-between;\"><span class=\"table-val\" style=\"font-weight:700;\">")
                  .append((getFieldValue(item, "topic") + getFieldValue(item, "course") + getFieldValue(item, "title")).replace("...", "")).append("</span>");
                sb.append("<span class=\"table-val\" style=\"font-size:0.85em; color: var(--text-muted);\">")
                  .append((getFieldValue(item, "year") + getFieldValue(item, "date")).replace("...", "")).append("</span></div>");
                sb.append("<div class=\"table-row\"><span class=\"table-val\" style=\"font-style:italic;\">")
                  .append((getFieldValue(item, "role") + getFieldValue(item, "inst") + getFieldValue(item, "amt")).replace("...", "")).append("</span></div>");
                String rDesc = getFieldValue(item, "desc");
                if (!rDesc.equals("...")) sb.append("<div class=\"proj-desc\" style=\"margin-top:4px;\">").append(rDesc).append("</div>");
                sb.append("</div>");
                break;

            case "photography":
                sb.append("<div class=\"photo-grid\"><div class=\"photo-card\"><i class=\"fas fa-user\"></i><div class=\"img-label\">")
                  .append(getFieldValue(item, "head")).append("</div></div><div class=\"photo-card\"><i class=\"fas fa-male\"></i><div class=\"img-label\">")
                  .append(getFieldValue(item, "body")).append("</div></div><div class=\"photo-card\"><i class=\"fas fa-camera-retro\"></i><div class=\"img-label\">")
                  .append(getFieldValue(item, "life")).append("</div></div></div>");
                break;
            case "profile_pic":
            case "profileSection":
            case "profile_picture":
                String imgVal = getFieldValue(item, "profile_pic");
                sb.append("<div class=\"profile-frame\" data-shape-mode=\"smooth\" data-radius=\"0\" data-scale=\"100\" data-x=\"0\" data-y=\"0\">");
                if (imgVal != null && !imgVal.equals("...") && !imgVal.isEmpty()) {
                    sb.append("<img id=\"profileImg\" src=\"").append(imgVal).append("\" alt=\"Profile\">");
                } else {
                    sb.append("<img id=\"profileImg\" src=\"\" alt=\"Profile\">");
                }
                sb.append("</div>");
                break;
            case "active_lifestyle":
                sb.append("<div class=\"data-table-item\">");
                for (FieldModel f : item.fields) {
                    sb.append("<div class=\"table-row\"><span class=\"table-label\">").append(f.label).append(":</span> <span class=\"table-val\">").append(f.value).append("</span></div>");
                }
                sb.append("</div>");
                break;
            case "declaration":
            case "declaration_block":
                sb.append("<p>").append(getFieldValue(item, "text")).append("</p>");
                if (signatureData != null && !signatureData.isEmpty()) {
                    sb.append("<div class=\"signature-wrapper\"><img src=\"").append(signatureData).append("\" class=\"signature-display\"></div>");
                }
                break;
            case "references":
                sb.append("<div class=\"data-table-item\">");
                sb.append("<div class=\"table-row\"><span class=\"table-val\" style=\"font-weight:700;\">").append(getFieldValue(item, "name")).append("</span></div>");
                sb.append("<div class=\"table-row\"><span class=\"table-val\">")
                  .append(getFieldValue(item, "pos")).append(", ").append(getFieldValue(item, "org")).append("</span></div>");
                sb.append("<div class=\"table-row\"><span class=\"table-val\" style=\"font-size:0.9em;\">")
                  .append(getFieldValue(item, "email")).append(" / ").append(getFieldValue(item, "phone")).append("</span></div>");
                sb.append("</div>");
                break;
            case "training":
                sb.append("<div class=\"data-table-item\" style=\"border-bottom:1px solid #E0E0E0; padding-bottom:12px; margin-bottom:12px;\">");
                sb.append("<div class=\"table-row\" style=\"justify-content:space-between;\"><span class=\"table-val\" style=\"font-weight:700; color:var(--header-color);\">").append(getFieldValue(item, "title")).append("</span>");
                sb.append("<span class=\"table-val\" style=\"color:#666;\">").append(getFieldValue(item, "year")).append("</span></div>");
                sb.append("<div class=\"table-row\"><span class=\"table-val\" style=\"font-style:italic;\">").append(getFieldValue(item, "inst")).append("</span></div>");
                String tDesc = getFieldValue(item, "desc");
                if (!tDesc.equals("...")) sb.append("<div class=\"proj-desc\" style=\"margin-top:4px;\">").append(tDesc).append("</div>");
                sb.append("</div>");
                break;
            case "awards":
            case "achievements":
                sb.append("<div class=\"data-table-item\">");
                sb.append("<div class=\"table-row\" style=\"justify-content:space-between;\"><span class=\"table-val\" style=\"font-weight:700;\">").append(getFieldValue(item, "title")).append("</span>");
                sb.append("<span class=\"table-val\" style=\"color:#666;\">").append(getFieldValue(item, "year")).append("</span></div>");
                String awardOrg = getFieldValue(item, "org");
                if (!awardOrg.isEmpty()) {
                    sb.append("<div class=\"table-row\"><span class=\"table-val\" style=\"font-size:0.9em;\">").append(awardOrg).append("</span></div>");
                }
                String bDesc = getFieldValue(item, "desc");
                if (!bDesc.equals("...") && !bDesc.isEmpty()) sb.append("<div class=\"proj-desc\">").append(bDesc).append("</div>");
                sb.append("</div>");
                break;
            case "certifications":
                sb.append("<div class=\"data-table-item\">");
                sb.append("<div class=\"table-row\" style=\"justify-content:space-between;\"><span class=\"table-val\" style=\"font-weight:700;\">").append(getFieldValue(item, "name")).append("</span>");
                sb.append("<span class=\"table-val\" style=\"color:#666;\">").append(getFieldValue(item, "date")).append("</span></div>");
                sb.append("<div class=\"table-row\"><span class=\"table-val\" style=\"font-style:italic;\">").append(getFieldValue(item, "org")).append("</span></div>");
                sb.append("</div>");
                break;
            case "publications":
                sb.append("<div class=\"data-table-item\">");
                sb.append("<div class=\"table-row\" style=\"justify-content:space-between;\"><span class=\"table-val\" style=\"font-weight:700;\">").append(getFieldValue(item, "title")).append("</span>");
                sb.append("<span class=\"table-val\" style=\"color:#666;\">").append(getFieldValue(item, "year")).append("</span></div>");
                sb.append("<div class=\"table-row\"><span class=\"table-val\" style=\"font-style:italic;\">").append(getFieldValue(item, "pub")).append("</span></div>");
                String pLink = getFieldValue(item, "link");
                if (!pLink.isEmpty() && !pLink.equals("...")) sb.append("<div class=\"table-row\"><a href=\"#\" style=\"font-size:0.85em;\">").append(pLink).append("</a></div>");
                sb.append("</div>");
                break;
            case "affiliations":
                sb.append("<div class=\"data-table-item\">");
                sb.append("<div class=\"table-row\" style=\"justify-content:space-between;\"><span class=\"table-val\" style=\"font-weight:700;\">").append(getFieldValue(item, "org")).append("</span>");
                sb.append("<span class=\"table-val\" style=\"color:#666;\">").append(getFieldValue(item, "year")).append("</span></div>");
                sb.append("<div class=\"table-row\"><span class=\"table-val\">").append(getFieldValue(item, "role")).append("</span></div>");
                sb.append("</div>");
                break;
            case "extra":
                sb.append("<div class=\"data-table-item\">");
                sb.append("<div class=\"table-row\" style=\"justify-content:space-between;\"><span class=\"table-val\" style=\"font-weight:700;\">").append(getFieldValue(item, "act")).append("</span>");
                sb.append("<span class=\"table-val\" style=\"font-size:0.85em; color: var(--text-muted);\">").append(getFieldValue(item, "dur")).append("</span></div>");
                String eDesc = getFieldValue(item, "desc");
                if (!eDesc.equals("...")) sb.append("<div class=\"proj-desc\">").append(eDesc).append("</div>");
                sb.append("</div>");
                break;
            default:
                // For simple-list items (like Certificates) and others
                String val = getFieldValue(item, "val");
                if (val.equals("...")) val = getFieldValue(item, "text"); // Fallback for old data
                sb.append("<div class=\"simple-list-item\">").append(val).append("</div>");
        }
        return sb.toString();
    }

    private String getFieldValue(ItemModel item, String key) {
        for(FieldModel f : item.fields) if(f.key.equals(key)) return f.value.isEmpty() ? "..." : f.value;
        return "...";
    }
    
    /**
     * Auto-scroll and highlight the expanded section in the WebView preview
     */
    private void scrollToSectionInPreview(String sectionType) {
        android.webkit.WebView webView = findViewById(R.id.wizardWebView);
        if (webView == null) return;
        
        // JavaScript to scroll to and highlight the section
        String js = String.format(
            "(function() {" +
            "  var targetType = '%s';" +
            "  var targets = document.querySelectorAll('section, header, [data-type], [data-section-type]');" +
            "  " +
            "  var matches = function(el, target) {" +
            "    var type = el.getAttribute('data-section-type') || el.getAttribute('data-type') || el.id || '';" +
            "    type = type.toLowerCase();" +
            "    target = target.toLowerCase();" +
            "    if (type === target) return true;" +
            "    if (target === 'header' && (type === 'mainheader' || type === 'headersection')) return true;" +
            "    if (target === 'identity' && (type === 'identitysection' || type === 'name_profession' || type === 'nameprofessionsection')) return true;" +
            "    if (target === 'profile_pic' && (type === 'profilesection' || type === 'profile_picture')) return true;" +
            "    if (target === 'summary_paragraph' && (type === 'summarysection' || type === 'objective')) return true;" +
            "    if (target === 'personal' && type === 'personaldetails') return true;" +
            "    if (target === 'passport' && type === 'passportdetails') return true;" +
            "    return false;" +
            "  };" +
            "  " +
            "  for (var i = 0; i < targets.length; i++) {" +
            "    var el = targets[i];" +
            "    el.style.transition = 'all 0.3s ease';" +
            "    el.style.backgroundColor = '';" +
            "    el.style.boxShadow = '';" +
            "    " +
            "    if (matches(el, targetType)) {" +
            "      el.scrollIntoView({ behavior: 'smooth', block: 'center' });" +
            "      var originalBg = el.style.backgroundColor;" +
            "      el.style.backgroundColor = 'rgba(25, 118, 210, 0.15)';" +
            "      el.style.boxShadow = '0 0 0 4px rgba(25, 118, 210, 0.3)';" +
            "      setTimeout(function() {" +
            "        el.style.backgroundColor = originalBg;" +
            "        el.style.boxShadow = '';" +
            "      }, 2500);" +
            "    }" +
            "  }" +
            "})();",
            sectionType
        );
        
        webView.post(() -> webView.evaluateJavascript(js, null));
    }

    private int estimateSectionHeight(SectionModel s) {
        int weight = 40; // h2 header height + section margins (~40px)
        for (ItemModel item : s.items) {
            weight += 20; // Item padding/card margins
            for (FieldModel f : item.fields) {
                if (f.value != null && !f.value.isEmpty()) {
                    if (f.type.equals("textarea")) {
                        String[] lines = f.value.split("\n");
                        weight += lines.length * 15; // 15px per line of text
                        for (String line : lines) {
                            if (line.length() > 60) weight += (line.length() / 60) * 15;
                        }
                    } else {
                        weight += 20; // 20px for label-value pair
                    }
                }
            }
        }
        return weight;
    }

    private Map<String, String> calculateColumnAssignments() {
        Map<String, String> assignments = new java.util.HashMap<>();
        
        List<String> wideIds = Arrays.asList(
            "experience", "projects", "publications", 
            "researchExp", "teachingExp", "grants", 
            "partnerExpectations", "expectations"
        );

        int rightHeight = 0;
        int leftHeight = 0;
        List<SectionModel> autoPool = new ArrayList<>();

        // First pass: Handle manual assignments and wide sections
        for (SectionModel s : currentSections) {
            if (s.id.equals("headerSection") || s.id.equals("summarySection") || s.id.equals("declarationSection")) {
                 continue; // These have fixed logic elsewhere or are intrinsic
            }

            if (s.column != null && !s.column.equals("auto")) {
                assignments.put(s.id, s.column);
                if (s.column.equals("left")) leftHeight += estimateSectionHeight(s);
                else if (s.column.equals("right")) rightHeight += estimateSectionHeight(s);
                // "header" doesn't affect column height balance
            } else if (wideIds.contains(s.type) || wideIds.contains(s.id)) {
                assignments.put(s.id, "right");
                rightHeight += estimateSectionHeight(s);
            } else {
                autoPool.add(s);
            }
        }

        // Second pass: Balance remaining auto sections
        for (SectionModel s : autoPool) {
            int h = estimateSectionHeight(s);
            if (leftHeight <= rightHeight + 100) {
                assignments.put(s.id, "left");
                leftHeight += h;
            } else {
                assignments.put(s.id, "right");
                rightHeight += h;
            }
        }

        return assignments;
    }
}
