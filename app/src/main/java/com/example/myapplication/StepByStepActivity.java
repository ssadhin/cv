package com.example.myapplication;

import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.Arrays;
import java.io.File;
import java.util.List;
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
import androidx.constraintlayout.widget.Guideline;

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

    private String selectedPurpose = "job";
    private String selectedTemplate = "default";
    private List<SectionModel> currentSections = new ArrayList<>();

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
        boolean isExpanded = false;
        List<ItemModel> items = new ArrayList<>();

        public SectionModel(String id, String name, String icon, String type, String group) {
            this.id = id;
            this.name = name;
            this.icon = icon;
            this.type = type;
            this.group = group;
            // Initialize with one empty item by default
            this.items.add(createDefaultItem(type));
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
                case "personal":
                    fields.add(new FieldModel("nationality", "Nationality", "American", "text"));
                    fields.add(new FieldModel("ethnicity", "Ethnicity", "Origin/Heritage", "text"));
                    fields.add(new FieldModel("dob", "Date of Birth", "15 January 1990", "text"));
                    fields.add(new FieldModel("gender", "Gender", "Male", "text"));
                    fields.add(new FieldModel("ms", "Marital Status", "Single", "text"));
                    fields.add(new FieldModel("height", "Height", "N/A", "text"));
                    break;
                case "passport":
                    fields.add(new FieldModel("pno", "Passport No", "P0000000", "text"));
                    fields.add(new FieldModel("idate", "Issue Date", "DD/MM/YYYY", "text"));
                    fields.add(new FieldModel("edate", "Expiry Date", "DD/MM/YYYY", "text"));
                    break;
                case "summary_paragraph":
                    fields.add(new FieldModel("summary", "Summary Statement", "Creative problem-solver with experience building user-focused digital products...", "textarea"));
                    break;
                case "education":
                    fields.add(new FieldModel("inst", "Institute", "Name", "text"));
                    fields.add(new FieldModel("year", "Year", "Year", "text"));
                    fields.add(new FieldModel("board", "Board Name", "Board Name", "text"));
                    fields.add(new FieldModel("deg", "Degree", "Degree", "text"));
                    fields.add(new FieldModel("gpa", "GPA", "", "text"));
                    break;
                case "experience":
                case "volunteer":
                case "internships":
                    fields.add(new FieldModel("comp", "Organization/Company", "Company", "text"));
                    fields.add(new FieldModel("dur", "Duration", "Date - Date", "text"));
                    fields.add(new FieldModel("role", "Role", "Role", "text"));
                    fields.add(new FieldModel("desc", "Responsibilities", "Responsibility...", "textarea"));
                    break;
                case "projects":
                    fields.add(new FieldModel("name", "Project Name", "Project", "text"));
                    fields.add(new FieldModel("year", "Year", "Year", "text"));
                    fields.add(new FieldModel("desc", "Description", "Description...", "textarea"));
                    fields.add(new FieldModel("link", "Link", "http://", "text"));
                    break;
                case "skills":
                case "hobbies":
                case "interests":
                    fields.add(new FieldModel("cat", "Category", "Skills", "text"));
                    fields.add(new FieldModel("vals", "Items", "Skill 1, Skill 2...", "textarea"));
                    break;
                case "languages":
                    fields.add(new FieldModel("lang", "Language", "English", "text"));
                    fields.add(new FieldModel("lvl", "Proficiency", "Proficient", "text"));
                    break;
                case "awards":
                case "achievements":
                    fields.add(new FieldModel("title", "Title", "Achievement Title", "text"));
                    fields.add(new FieldModel("year", "Year", "Year", "text"));
                    fields.add(new FieldModel("body", "Description", "Brief description...", "textarea"));
                    break;
                case "certifications":
                    fields.add(new FieldModel("name", "Certification Name", "Certification Name", "text"));
                    fields.add(new FieldModel("org", "Organization", "Organization", "text"));
                    fields.add(new FieldModel("date", "Date", "Date", "text"));
                    break;
                case "weblinks":
                    fields.add(new FieldModel("name", "Site Name", "Site Name", "text"));
                    fields.add(new FieldModel("url", "URL", "http://", "text"));
                    break;
                case "references":
                    fields.add(new FieldModel("name", "Name", "Name", "text"));
                    fields.add(new FieldModel("pos", "Position", "Position", "text"));
                    fields.add(new FieldModel("org", "Company", "Company", "text"));
                    fields.add(new FieldModel("email", "Email", "email@example.com", "text"));
                    fields.add(new FieldModel("phone", "Phone", "123-456-7890", "text"));
                    break;
                case "training":
                    fields.add(new FieldModel("title", "Training Title", "Training Name", "text"));
                    fields.add(new FieldModel("inst", "Institute", "Institute Name", "text"));
                    fields.add(new FieldModel("year", "Year", "Year", "text"));
                    fields.add(new FieldModel("desc", "Description", "Description...", "textarea"));
                    break;
                case "publications":
                    fields.add(new FieldModel("title", "Paper Title", "Title", "text"));
                    fields.add(new FieldModel("pub", "Publisher", "Publisher", "text"));
                    fields.add(new FieldModel("year", "Year", "Year", "text"));
                    fields.add(new FieldModel("link", "Link", "http://", "text"));
                    break;
                case "affiliations":
                    fields.add(new FieldModel("org", "Organization", "Organization", "text"));
                    fields.add(new FieldModel("role", "Role", "Member", "text"));
                    fields.add(new FieldModel("year", "Year", "Year", "text"));
                    break;
                case "extra":
                    fields.add(new FieldModel("act", "Activity", "Activity Name", "text"));
                    fields.add(new FieldModel("desc", "Description", "Description...", "textarea"));
                    break;
                case "visa":
                    fields.add(new FieldModel("status", "Visa / Work Status", "Eligible/Required/H1-B/etc", "text"));
                    fields.add(new FieldModel("country", "Target Country", "", "text"));
                    break;
                case "research":
                    fields.add(new FieldModel("topic", "Research Topic", "", "text"));
                    fields.add(new FieldModel("role", "Your Role", "Lead/Assistant/etc", "text"));
                    fields.add(new FieldModel("desc", "Method/Findings", "", "textarea"));
                    break;
                case "teaching":
                    fields.add(new FieldModel("course", "Course Name", "", "text"));
                    fields.add(new FieldModel("inst", "Institution", "", "text"));
                    fields.add(new FieldModel("desc", "Responsibilities", "", "textarea"));
                    break;
                case "grants":
                    fields.add(new FieldModel("title", "Grant/Funding Title", "", "text"));
                    fields.add(new FieldModel("amt", "Amount/Agency", "", "text"));
                    fields.add(new FieldModel("year", "Year", "", "text"));
                    break;
                case "test_scores":
                    fields.add(new FieldModel("test", "Test Name", "IELTS/SAT/GRE/etc", "text"));
                    fields.add(new FieldModel("score", "Score/Band", "", "text"));
                    fields.add(new FieldModel("date", "Test Date", "", "text"));
                    break;
                case "family":
                    fields.add(new FieldModel("father", "Father's Occ.", "", "text"));
                    fields.add(new FieldModel("mother", "Mother's Occ.", "", "text"));
                    fields.add(new FieldModel("siblings", "Siblings Info", "e.g., 2 Brothers, 1 Sister", "text"));
                    break;
                case "expectations":
                    fields.add(new FieldModel("pref", "Partner Preferences", "What you're looking for...", "textarea"));
                    break;
                case "lifestyle":
                    fields.add(new FieldModel("diet", "Diet", "Veg/Non-Veg/etc", "text"));
                    fields.add(new FieldModel("habits", "Social Habits", "Drinking/Smoking/etc", "text"));
                    break;
                case "physical":
                    fields.add(new FieldModel("weight", "Weight", "", "text"));
                    fields.add(new FieldModel("complexion", "Complexion", "Fair/Tanned/etc", "text"));
                    fields.add(new FieldModel("build", "Body Build", "Athletic/Slim/etc", "text"));
                    break;
                case "photography":
                    fields.add(new FieldModel("head", "Headshot Link/Desc", "Close-up portrait", "text"));
                    fields.add(new FieldModel("body", "Full Body Link/Desc", "Standing photo", "text"));
                    fields.add(new FieldModel("life", "Lifestyle Link/Desc", "General activity photo", "text"));
                    break;
                case "astrological":
                    fields.add(new FieldModel("rashi", "Rashi", "Zodiac Sign", "text"));
                    fields.add(new FieldModel("nakshatra", "Nakshatra", "Birth Star", "text"));
                    fields.add(new FieldModel("gotra", "Gotra", "Lineage", "text"));
                    break;
                default:
                    fields.add(new FieldModel("val", "Content", "New Item", "text"));
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

    // Template Model
    public static class TemplateModel {
        String id;
        String name;
        int previewRes;
        boolean isUserTemplate = false;
        String filePath = null;

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
    }

    private final List<SectionModel> ALL_SECTIONS = Arrays.asList(
        new SectionModel("headerSection", "Header Info", "fa-user-circle", "header", "gridEssentials"),
        new SectionModel("personalDetails", "Personal Details", "fa-id-card", "personal", "gridEssentials"),
        new SectionModel("passportDetails", "Passport Details", "fa-passport", "passport", "gridEssentials"),
        new SectionModel("summarySection", "Professional Summary", "fa-user-tie", "summary_paragraph", "gridEssentials"),
        new SectionModel("visaStatus", "Visa / Work Authorization", "fa-file-invoice", "visa", "gridEssentials"),
        new SectionModel("languages", "Languages", "fa-language", "languages", "gridEssentials"),
        new SectionModel("education", "Education", "fa-graduation-cap", "education", "gridExp"),
        new SectionModel("experience", "Work Experience", "fa-briefcase", "experience", "gridExp"),
        new SectionModel("projects", "Projects", "fa-project-diagram", "projects", "gridExp"),
        new SectionModel("skills", "Skills", "fa-tools", "skills", "gridExp"),
        new SectionModel("certificates", "Certificates (Simple)", "fa-certificate", "simple-list", "gridExp"),
        new SectionModel("awards", "Awards & Honors", "fa-trophy", "awards", "gridAdd"),
        new SectionModel("certifications", "Certifications (Adv)", "fa-certificate", "certifications", "gridAdd"),
        new SectionModel("volunteer", "Volunteer Exp", "fa-hands-helping", "volunteer", "gridAdd"),
        new SectionModel("publications", "Publications", "fa-book", "publications", "gridAdd"),
        new SectionModel("researchExp", "Research Experience", "fa-microscope", "research", "gridAdd"),
        new SectionModel("teachingExp", "Teaching Experience", "fa-chalkboard-teacher", "teaching", "gridAdd"),
        new SectionModel("grants", "Grants & Funding", "fa-hand-holding-usd", "grants", "gridAdd"),
        new SectionModel("affiliations", "Affiliations", "fa-users", "affiliations", "gridAdd"),
        new SectionModel("hobbies", "Hobbies", "fa-gamepad", "hobbies", "gridAdd"),
        new SectionModel("extra", "Extracurricular", "fa-futbol", "extra", "gridAdd"),
        new SectionModel("references", "References", "fa-user-check", "references", "gridAdd"),
        new SectionModel("training", "Training", "fa-chalkboard-teacher", "training", "gridAdd"),
        new SectionModel("internships", "Internships", "fa-laptop-code", "internships", "gridAdd"),
        new SectionModel("achievements", "Achievements", "fa-star", "achievements", "gridAdd"),
        new SectionModel("testScores", "Test Scores (Professional)", "fa-check-double", "test_scores", "gridAdd"),
        new SectionModel("weblinks", "Web Links", "fa-link", "weblinks", "gridAdd"),
        new SectionModel("familyDetails", "Family Details", "fa-users-cog", "family", "gridAdd"),
        new SectionModel("partnerExpectations", "Partner Expectations", "fa-heart", "expectations", "gridAdd"),
        new SectionModel("lifestyleHabits", "Lifestyle & Habits", "fa-apple-alt", "lifestyle", "gridAdd"),
        new SectionModel("astrologySection", "Astrological Details", "fa-sun", "astrological", "gridAdd"),
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

    private void applyPurposeDefaults() {
        currentSections.clear();
        // Baseline default list as seen in index.html
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

        for (int i = 0; i < defaultIds.size(); i++) {
            String id = defaultIds.get(i);
            for (SectionModel sec : ALL_SECTIONS) {
                if (sec.id.equals(id)) {
                    SectionModel instance = new SectionModel(sec.id, sec.name, sec.icon, sec.type, sec.group);
                    instance.isExpanded = (i == 0); // Expand first one
                    instance.addItem(); // Ensure at least one item exists
                    currentSections.add(instance);
                    break;
                }
            }
        }
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
                currentSections.clear();
                JSONArray sectionsArray = data.getJSONArray("sections");
                for (int i = 0; i < sectionsArray.length(); i++) {
                    JSONObject sLoopObj = sectionsArray.getJSONObject(i);
                    String sId = sLoopObj.getString("id");
                    String sType = sLoopObj.getString("type");
                    String sName = sLoopObj.getString("name");
                    
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
                        section = new SectionModel(sId, sName, sectionTemplate.icon, sType, sectionTemplate.group);
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
                            if (sectionTemplate != null && !sectionTemplate.items.isEmpty()) {
                                for (FieldModel f : sectionTemplate.items.get(0).fields) {
                                    FieldModel newF = new FieldModel(f.key, f.label, f.type);
                                    for (int k = 0; k < fieldsArray.length(); k++) {
                                        JSONObject fj = fieldsArray.getJSONObject(k);
                                        if (fj.getString("key").equals(f.key)) {
                                            newF.value = fj.optString("value", "");
                                            break;
                                        }
                                    }
                                    itemFields.add(newF);
                                }
                            } else {
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
        } catch (Exception e) {
            Log.e("StepByStep", "Error loading structured data", e);
        }
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
        });
        wizardWebView.loadUrl("file:///android_asset/index.html");
    }

    private void updateWebViewPreview() {
        if (wizardWebView == null) return;
        
        try {
            JSONObject state = generateStateJson();
            // Direct object passing to window.loadResumeData (now supports objects)
            String js = "if(window.loadResumeData) { window.loadResumeData(" + state.toString() + "); }";
            wizardWebView.evaluateJavascript(js, null);
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
            intent.putExtra("EXTRA_STEP_BY_STEP_DATA", generateStateJson().toString());
            
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
        TemplateViewHolder(View v) { super(v); rv = v.findViewById(R.id.rvTemplates); }
        void bind() {
            List<TemplateModel> templates = new ArrayList<>(Arrays.asList(
                new TemplateModel("default", "Standard Modern"),
                new TemplateModel("sidebar", "Sidebar Pro")
            ));

            // Load User Templates
            templates.addAll(loadUserTemplates());

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
            for (SectionModel current : currentSections) if (current.id.equals(sec.id)) { exists = true; break; }
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
            ImageView icon = v.findViewById(R.id.section_icon);
            View iconContainer = v.findViewById(R.id.section_container); 
            ImageButton btnAdd = v.findViewById(R.id.btn_add_section);

            title.setText(s.name);
            // Use group as description since we don't have explicit desc
            desc.setText(s.group.replace("grid", "").toUpperCase());
            icon.setImageResource(getIconResForFontAwesome(s.icon));
            
            // Hide placement buttons in Wizard as they don't apply here
            View placementContainer = v.findViewById(R.id.placement_container);
            if (placementContainer != null) placementContainer.setVisibility(View.GONE);

            View.OnClickListener listener = view -> {
                SectionModel instance = new SectionModel(s.id, s.name, s.icon, s.type, s.group);
                currentSections.add(instance);
                adapter.notifyItemInserted(currentSections.size() - 1);
                updateWebViewPreview();
                dialog.dismiss();
                Toast.makeText(this, s.name + " Added", Toast.LENGTH_SHORT).show();
            };

            if (iconContainer != null) iconContainer.setOnClickListener(listener);
            if (btnAdd != null) btnAdd.setOnClickListener(listener);

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
            
            if (m.isUserTemplate && m.filePath != null) {
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
                if (m.id.equals("sidebar")) {
                    h.ivPreview.setImageResource(R.drawable.ic_grid); // Replace with sidebar icon if available
                } else {
                    h.ivPreview.setImageResource(R.drawable.ic_grid);
                }
                h.ivPreview.setImageTintList(android.content.res.ColorStateList.valueOf(0xFF1A237E));
                h.ivPreview.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            }

            h.itemView.setOnClickListener(v -> {
                selectedTemplate = m.id;
                notifyDataSetChanged();
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
                currentSections.remove(p);
                notifyDataSetChanged();
                updateWebViewPreview();
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
                input.setText(field.value);
                if (field.type.equals("textarea")) {
                    input.setSingleLine(false);
                    input.setMinLines(3);
                }

                input.addTextChangedListener(new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                        field.value = s.toString();
                        // Delay sync to avoid UI lag? No, let's try direct first.
                        updateWebViewPreview();
                    }
                    @Override public void afterTextChanged(Editable s) {}
                });
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
                    if (!val.equals("...") && !val.isEmpty()) {
                        JSONObject c = new JSONObject();
                        c.put("text", val);
                        c.put("icon", icons[i]);
                        c.put("column", (i % 2 == 0) ? "left" : "right"); // simple split
                        cItems.put(c);
                    }
                }
                headerData.put("items", cItems);
            } catch (JSONException e) {
                Log.e("StepByStep", "Error syncing header data", e);
            }
        }
        
        StringBuilder html = new StringBuilder();
        html.append("<header id=\"mainHeader\">");
        
        if (headerData != null) {
            html.append("<div class=\"header-info\"><h1>").append(headerData.optString("name", "Your Name")).append("</h1>");
            html.append("<div class=\"contact-info\">");
            
            JSONArray items = headerData.optJSONArray("items");
            if (items != null) {
                StringBuilder left = new StringBuilder("<div class=\"header-column-left\" data-column=\"left\">");
                StringBuilder right = new StringBuilder("<div class=\"header-column-right\" data-column=\"right\">");
                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    String col = item.optString("column", "left");
                    StringBuilder target = col.equals("left") ? left : right;
                    
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
                left.append("</div>");
                right.append("</div>");
                html.append(left).append(right);
            }
            html.append("</div></div>");
        }
        html.append("</header>");
        
        // Split sections into Layout groups
        List<SectionModel> objectiveSecs = new ArrayList<>();
        List<SectionModel> leftSecs = new ArrayList<>();
        List<SectionModel> rightSecs = new ArrayList<>();
        List<SectionModel> footerSecs = new ArrayList<>();
        
        for (SectionModel s : currentSections) {
             if (s.id.equals("headerSection")) continue; // Don't render in body
             if (s.id.equals("summarySection")) objectiveSecs.add(s);
             else if (s.id.equals("declarationSection")) footerSecs.add(s);
             else if (Arrays.asList("personalDetails", "passportDetails", "languages", "skills", "certificates", "weblinks", "achievements", "hobbies").contains(s.id)) leftSecs.add(s);
             else rightSecs.add(s);
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

    private String generateSectionWrapper(SectionModel section) {
        StringBuilder sb = new StringBuilder();
        String extraClass = "";
        if (section.id.equals("summarySection")) extraClass = "objective";
        else if (section.id.equals("personalDetails")) extraClass = "personal-details";
        else if (section.id.equals("declarationSection")) extraClass = "declaration";

        // Add 'resume-section' class and data-section-type for scrolling
        sb.append("<section class=\"resume-section ").append(extraClass).append("\" id=\"").append(section.id).append("\" data-section-type=\"").append(section.type).append("\">");
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
            case "summary_paragraph":
                sb.append("<p style=\"line-height: 1.6; color: var(--text-main);\">").append(getFieldValue(item, "summary")).append("</p>");
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
                sb.append("<div class=\"table-row\"><span class=\"table-label\">Issue Date:</span> <span class=\"table-val\">").append(getFieldValue(item, "idate")).append("</span></div>");
                sb.append("<div class=\"table-row\"><span class=\"table-label\">Expiry Date:</span> <span class=\"table-val\">").append(getFieldValue(item, "edate")).append("</span></div></div>");
                break;
            case "weblinks":
                sb.append("<div class=\"data-table-item link-item\"><div class=\"table-row\" style=\"align-items:flex-start; gap:12px\"><div class=\"link-icon-container\"><i class=\"fas fa-globe fa-2x\"></i></div><div style=\"flex:1\"><div class=\"table-row\"><a href=\"#\" class=\"table-val\" style=\"font-weight:700; text-decoration:underline\">")
                  .append(getFieldValue(item, "name")).append("</a></div><div class=\"proj-desc\">").append(getFieldValue(item, "url")).append("</div></div></div></div>");
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
                for (FieldModel f : item.fields) {
                    if (f.value.isEmpty() || f.value.equals("...")) continue;
                    sb.append("<div class=\"pd-row\"><span class=\"pd-label\">").append(f.label).append(":</span> <span class=\"pd-val\">").append(f.value).append("</span></div>");
                }
                break;

            case "research":
            case "teaching":
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
            case "reference":
                sb.append("<div class=\"data-table-item\" style=\"background-color:#F8F9FA; padding:12px; border-radius:8px;\">");
                sb.append("<div class=\"table-row\"><span class=\"table-label\">Name:</span> <span class=\"table-val\" style=\"font-weight:600;\">").append(getFieldValue(item, "name")).append("</span></div>");
                sb.append("<div class=\"table-row\"><span class=\"table-label\">Position:</span> <span class=\"table-val\">").append(getFieldValue(item, "pos")).append("</span></div>");
                sb.append("<div class=\"table-row\"><span class=\"table-label\">Company:</span> <span class=\"table-val\">").append(getFieldValue(item, "org")).append("</span></div>");
                sb.append("<div class=\"table-row\"><span class=\"table-label\">Email:</span> <span class=\"table-val\">").append(getFieldValue(item, "email")).append("</span></div>");
                sb.append("<div class=\"table-row\"><span class=\"table-label\">Phone:</span> <span class=\"table-val\">").append(getFieldValue(item, "phone")).append("</span></div>");
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
                String bDesc = getFieldValue(item, "body");
                if (!bDesc.equals("...")) sb.append("<div class=\"proj-desc\">").append(bDesc).append("</div>");
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
                sb.append("<div class=\"table-row\"><span class=\"table-val\" style=\"font-weight:700;\">").append(getFieldValue(item, "act")).append("</span></div>");
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
            "  var sections = document.querySelectorAll('.resume-section');" +
            "  for (var i = 0; i < sections.length; i++) {" +
            "    var section = sections[i];" +
            "    section.style.transition = 'all 0.3s ease';" +
            "    section.style.backgroundColor = '';" +
            "    section.style.boxShadow = '';" +
            "    if (section.getAttribute('data-section-type') === '%s') {" +
            "      section.scrollIntoView({ behavior: 'smooth', block: 'center' });" +
            "      section.style.backgroundColor = '#E3F2FD';" +
            "      section.style.boxShadow = '0 0 0 2px #1976D2';" +
            "      setTimeout(function() {" +
            "        section.style.backgroundColor = '';" +
            "        section.style.boxShadow = '';" +
            "      }, 2000);" +
            "    }" +
            "  }" +
            "})();",
            sectionType
        );
        
        webView.post(() -> webView.evaluateJavascript(js, null));
    }
}
