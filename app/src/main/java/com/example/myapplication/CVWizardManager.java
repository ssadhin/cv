package com.example.myapplication;
import androidx.appcompat.app.AlertDialog;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsAnimationCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CVWizardManager {

    private final MainActivity activity;
    
    // UI Elements
    private View wizardPanel;
    private View wizardHeader;
    private ViewPager2 viewPager;
    private LinearProgressIndicator progressBar;
    private Button btnBack, btnNext;
    private RecyclerView.Adapter<RecyclerView.ViewHolder> addSectionDialogAdapter;
    private TextView tvStepTitle;
    private ImageButton btnClose;
    private View footerSpacer;
    private View dragHandle;
    private View cardSteps;

    // State
    public String selectedPurpose = "job";
    public String selectedTemplate = null;
    public boolean isAssetTemplate = false;
    public boolean isUserTemplate = false;
    public String templateJsonData = null;
    public List<SectionModel> currentSections = new ArrayList<>();
    private boolean isSectionsOnlyMode = false;
    private SectionsAdapter sectionsAdapter;
    private List<PurposeModel> purposeList = new ArrayList<>();
    private List<TemplateModel> templateList = new ArrayList<>();
    private String lastAppliedPurpose = null;
    private String pendingAddItemSectionId = null;
    
    // Models (Moved from StepByStepActivity)
    public static class FieldModel {
        String key;
        String label;
        String value = "";
        String type = "text";
        String url = null;
        int index = 0;
        boolean isEditable = true;
        boolean isVisible = true;

        public FieldModel(String key, String label) {
            this.key = key;
            this.label = label;
        }

        public FieldModel(String key, String label, String value, String type) {
            this(key, label, value, type, 0);
        }

        public FieldModel(String key, String label, String value, String type, int index) {
            this.key = key;
            this.label = label;
            this.value = value;
            this.type = type;
            this.index = index;
        }
        
        public FieldModel(String key, String label, String type) {
            this.key = key;
            this.label = label;
            this.type = type;
        }

        public JSONObject toJson() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("key", key);
            json.put("label", label);
            json.put("value", value);
            json.put("type", type);
            json.put("url", url);
            json.put("index", index);
            json.put("editable", isEditable);
            json.put("visible", isVisible);
            return json;
        }
    }

    public static class ItemModel {
        List<FieldModel> fields = new ArrayList<>();
        public ItemModel(List<FieldModel> fields) {
            this.fields = fields;
        }

        public JSONObject toJson() throws JSONException {
            JSONObject json = new JSONObject();
            JSONArray fieldsArray = new JSONArray();
            for (FieldModel field : fields) {
                fieldsArray.put(field.toJson());
            }
            json.put("fields", fieldsArray);
            return json;
        }
    }

    public class SectionModel {
        String id;
        String name;
        String desc;
        String type;
        String icon;
        String group;
        String column = "auto";
        boolean isExpanded = false;
        List<ItemModel> items = new ArrayList<>();
        String spacerHeight = "0px";

        public SectionModel(String id, String name, String desc, String icon, String type, String group) {
            this.id = id;
            this.name = name;
            this.desc = desc;
            this.icon = icon;
            this.type = type;
            this.group = group;
            if (type.equals("contact")) {
                this.items.add(createContactItem("Example Detail", "alex@gmail.com"));
                this.items.add(createContactItem("Call/WhatsApp", "+1 234 567 890"));
                this.items.add(createContactItem("Current Location", "Street 123, City, Country"));
            } else if (type.equals("visaStatus") || type.equals("visastatus")) {
                List<FieldModel> f = new ArrayList<>();
                FieldModel f1 = new FieldModel("country_lbl", "Label", activity.getString(R.string.field_country), "text", 0); f1.isEditable = false;
                f.add(f1);
                f.add(new FieldModel("country", "Country", "United States", "text", 1));
                FieldModel f2 = new FieldModel("type_lbl", "Label", activity.getString(R.string.field_visa_type), "text", 2); f2.isEditable = false;
                f.add(f2);
                f.add(new FieldModel("type", "Type", "H1-B / Student", "text", 3));
                FieldModel f3 = new FieldModel("status_lbl", "Label", activity.getString(R.string.field_status), "text", 4); f3.isEditable = false;
                f.add(f3);
                f.add(new FieldModel("status", "Status", "Valid", "text", 5));
                FieldModel f4 = new FieldModel("expiry_lbl", "Label", activity.getString(R.string.field_edate), "text", 6); f4.isEditable = false;
                f.add(f4);
                f.add(new FieldModel("expiry", "Expiry", "DD/MM/YYYY", "text", 7));
                this.items.add(new ItemModel(f));
            } else if (type.equals("passport") || type.equals("passportDetails")) {
                this.items.add(createKVItem("Passport No", "P0000000"));
                this.items.add(createKVItem("Issued By", "Place of Issue"));
                this.items.add(createKVItem("Issue Date", "DD/MM/YYYY"));
                this.items.add(createKVItem("Expiry Date", "DD/MM/YYYY"));
            } else if (type.equals("testScores") || type.equals("testscores")) {
                List<FieldModel> f = new ArrayList<>();
                FieldModel f1 = new FieldModel("exam_lbl", "Label", "Exam", "text", 0); f1.isEditable = false;
                f.add(f1);
                f.add(new FieldModel("exam", "Exam", "IELTS", "text", 1));
                FieldModel f2 = new FieldModel("score_lbl", "Label", "Score", "text", 2); f2.isEditable = false;
                f.add(f2);
                f.add(new FieldModel("score", "Score", "8.0", "text", 3));
                FieldModel f3 = new FieldModel("date_lbl", "Label", "Date", "text", 4); f3.isEditable = false;
                f.add(f3);
                f.add(new FieldModel("date", "Date", "2023", "text", 5));
                this.items.add(new ItemModel(f));
            } else if (type.equals("familydetails")) {
                this.items.add(createKVItem("Father's Name", ""));
                this.items.add(createKVItem("Mother's Name", ""));
            } else {
                this.items.add(createDefaultItem(type));
            }
        }

        public SectionModel(String id, String name, String desc, String icon, String type, String group, String column) {
            this(id, name, desc, icon, type, group);
            this.column = column;
        }

        private ItemModel createKVItem(String label, String value, boolean isLabelEditable) {
            List<FieldModel> fields = new ArrayList<>();
            FieldModel keyField = new FieldModel("k", "Label", label, "text", 0);
            keyField.isEditable = isLabelEditable;
            fields.add(keyField);
            fields.add(new FieldModel("v", "Value", value, "text", 1));
            return new ItemModel(fields);
        }

        private ItemModel createKVItem(String label, String value) {
            return createKVItem(label, value, true);
        }

        private ItemModel createContactItem(String label, String value) {
            List<FieldModel> fields = new ArrayList<>();
            fields.add(new FieldModel("val", label, value, "text", 0));
            return new ItemModel(fields);
        }

        private ItemModel createDefaultItem(String type) {
            List<FieldModel> fields = new ArrayList<>();
            switch (type) {
                case "header":
                    fields.add(new FieldModel("name", activity.getString(R.string.field_name), activity.getString(R.string.placeholder_name), "text"));
                    fields.add(new FieldModel("addr", activity.getString(R.string.field_address), activity.getString(R.string.placeholder_address), "text"));
                    fields.add(new FieldModel("email", activity.getString(R.string.field_email), "email@example.com", "text"));
                    fields.add(new FieldModel("phone", activity.getString(R.string.field_phone), "+1 234 567 890", "text"));
                    fields.add(new FieldModel("link", activity.getString(R.string.field_linkedin), "linkedin.com/in/...", "text"));
                    break;
                case "profile_pic":
                case "profileSection":
                    fields.add(new FieldModel("profile_pic", activity.getString(R.string.field_profile_pic), "", "image"));
                    break;
                case "name_profession":
                case "nameProfessionSection":
                    fields.add(new FieldModel("name", activity.getString(R.string.field_name), activity.getString(R.string.placeholder_name)));
                    fields.add(new FieldModel("role", activity.getString(R.string.field_prof), activity.getString(R.string.placeholder_profession)));
                    break;
                case "personal":
                case "personalDetails":
                    FieldModel pK = new FieldModel("k", "Label", "Nationality", "text", 0);
                    pK.isEditable = true;
                    fields.add(pK);
                    fields.add(new FieldModel("v", "Value", "American", "text", 1));
                    break;
                case "summary_paragraph":
                case "summarySection":
                case "summary":
                    fields.add(new FieldModel("summary", activity.getString(R.string.field_summary), "...", "textarea"));
                    break;
                case "languages":
                    fields.add(new FieldModel("lang", activity.getString(R.string.field_lang), "e.g. English", "text", 0));
                    fields.add(new FieldModel("level", activity.getString(R.string.field_lvl_fluent), "e.g. Proficient", "text", 1));
                    break;
                case "education":
                    fields.add(new FieldModel("inst", activity.getString(R.string.field_inst), "University Name", "text", 0));
                    fields.add(new FieldModel("year", activity.getString(R.string.field_year), "2015-2019", "text", 1));
                    fields.add(new FieldModel("degree", activity.getString(R.string.field_deg), "Your Degree", "text", 2));
                    fields.add(new FieldModel("cgpa", activity.getString(R.string.field_gpa), "3.8/4", "text", 3));
                    break;
                case "experience":
                    fields.add(new FieldModel("company", activity.getString(R.string.field_comp), "New Company", "text", 0));
                    fields.add(new FieldModel("date", activity.getString(R.string.field_dur), "Jan 2020 - Present", "text", 1));
                    fields.add(new FieldModel("role", activity.getString(R.string.field_role), "Job Title", "text", 2));
                    fields.add(new FieldModel("bullet", activity.getString(R.string.field_desc), "Key achievement...", "text", 3));
                    break;
                case "projects":
                    fields.add(new FieldModel("title", activity.getString(R.string.field_title), "Project Name", "text", 0));
                    fields.add(new FieldModel("year", activity.getString(R.string.field_year), "2024", "text", 1));
                    fields.add(new FieldModel("desc", activity.getString(R.string.field_details), "Brief overview...", "text", 2));
                    break;
                case "researchExp":
                case "teachingExp":
                case "grants":
                case "training":
                    fields.add(new FieldModel("title", activity.getString(R.string.field_title), "", "text", 0));
                    fields.add(new FieldModel("year", activity.getString(R.string.field_year_date), "", "text", 1));
                    fields.add(new FieldModel("inst", activity.getString(R.string.field_inst_agency), "", "text", 2));
                    fields.add(new FieldModel("desc", activity.getString(R.string.field_details), "", "textarea", 3));
                    break;
                case "skills":
                    fields.add(new FieldModel("skill_header", activity.getString(R.string.field_cat), "e.g. Frontend", "text", 0));
                    fields.add(new FieldModel("skill_sub", activity.getString(R.string.field_items_comma), "e.g. React, CSS", "text", 1));
                    break;
                case "hobbies":
                case "interests":
                    fields.add(new FieldModel("cat", activity.getString(R.string.field_cat), ""));
                    fields.add(new FieldModel("vals", activity.getString(R.string.field_items_comma), "textarea", ""));
                    break;
                case "testScores":
                    fields.add(new FieldModel("test", activity.getString(R.string.field_test), ""));
                    fields.add(new FieldModel("score", activity.getString(R.string.field_score), ""));
                    fields.add(new FieldModel("date", activity.getString(R.string.field_date), ""));
                    break;
                case "simple-list":
                case "certificates":
                case "physicalFitness":
                    fields.add(new FieldModel("val", activity.getString(R.string.field_val_content), ""));
                    break;
                case "awards":
                case "achievements":
                    fields.add(new FieldModel("title", activity.getString(R.string.field_award_achievement), ""));
                    break;
                case "contact":
                    fields.add(new FieldModel("val", activity.getString(R.string.field_val_contact), "example@gmail.com"));
                    break;
                case "certifications":
                    fields.add(new FieldModel("name", activity.getString(R.string.field_cert_name), ""));
                    fields.add(new FieldModel("org", activity.getString(R.string.field_org), ""));
                    fields.add(new FieldModel("date", activity.getString(R.string.field_date), ""));
                    break;
                case "volunteer":
                    fields.add(new FieldModel("role", activity.getString(R.string.field_role), ""));
                    fields.add(new FieldModel("dur", activity.getString(R.string.field_dur), ""));
                    fields.add(new FieldModel("inst", activity.getString(R.string.field_org), ""));
                    fields.add(new FieldModel("desc", activity.getString(R.string.field_desc), "textarea", ""));
                    break;
                case "publications":
                    fields.add(new FieldModel("title", activity.getString(R.string.field_title), ""));
                    fields.add(new FieldModel("year", activity.getString(R.string.field_year), ""));
                    fields.add(new FieldModel("pub", activity.getString(R.string.field_pub), ""));
                    fields.add(new FieldModel("link", activity.getString(R.string.field_link), ""));
                    break;
                case "affiliations":
                    fields.add(new FieldModel("org", activity.getString(R.string.field_org), ""));
                    fields.add(new FieldModel("role", activity.getString(R.string.field_role), ""));
                    fields.add(new FieldModel("year", activity.getString(R.string.field_year), ""));
                    break;
                case "familyDetails":
                case "familydetails":
                    fields.add(new FieldModel("k", "Label", activity.getString(R.string.field_father_occup), "text", 0));
                    fields.add(new FieldModel("v", "Value", "", "text", 1));
                    break;
                case "partnerExpectations":
                case "partnerexpectations":
                    fields.add(new FieldModel("k", "Label", activity.getString(R.string.field_edu_prof_pref), "text", 0));
                    fields.add(new FieldModel("v", "Value", "", "text", 1));
                    break;
                case "lifestyleHabits":
                case "lifestylehabits":
                    fields.add(new FieldModel("k", "Label", activity.getString(R.string.field_diet_veg), "text", 0));
                    fields.add(new FieldModel("v", "Value", "", "text", 1));
                    break;
                case "astrologySection":
                case "astrological":
                case "astrologysection":
                    fields.add(new FieldModel("k", "Label", activity.getString(R.string.field_rashi), "text", 0));
                    fields.add(new FieldModel("v", "Value", "", "text", 1));
                    break;
                case "physical":
                case "physicalProfile":
                    fields.add(new FieldModel("height", activity.getString(R.string.field_height), ""));
                    fields.add(new FieldModel("weight", activity.getString(R.string.field_weight), ""));
                    fields.add(new FieldModel("complexion", activity.getString(R.string.field_complexion), ""));
                    fields.add(new FieldModel("build", activity.getString(R.string.field_build), ""));
                    fields.add(new FieldModel("eye", activity.getString(R.string.field_eye_color), ""));
                    fields.add(new FieldModel("hair", activity.getString(R.string.field_hair_color), ""));
                    break;
                case "visualRegistry":
                case "photography":
                    fields.add(new FieldModel("head", activity.getString(R.string.field_headshot_label), "Headshot"));
                    fields.add(new FieldModel("body", activity.getString(R.string.field_fullbody_label), "Full-Body Shot"));
                    fields.add(new FieldModel("life", activity.getString(R.string.field_lifestyle_photo_label), "Lifestyle Photo"));
                    break;
                case "active_lifestyle":
                case "activeLife":
                    fields.add(new FieldModel("activity", activity.getString(R.string.field_activity), ""));
                    fields.add(new FieldModel("achievements", activity.getString(R.string.field_achievements), ""));
                    fields.add(new FieldModel("health", activity.getString(R.string.field_health_status), ""));
                    break;
                case "extra":
                    fields.add(new FieldModel("act", activity.getString(R.string.field_activity), ""));
                    fields.add(new FieldModel("dur", activity.getString(R.string.field_dur), ""));
                    fields.add(new FieldModel("desc", activity.getString(R.string.field_desc), "textarea", ""));
                    break;
                case "references":
                    fields.add(new FieldModel("name", activity.getString(R.string.field_name), ""));
                    fields.add(new FieldModel("pos", activity.getString(R.string.field_pos), ""));
                    fields.add(new FieldModel("org", activity.getString(R.string.field_org), ""));
                    fields.add(new FieldModel("email", activity.getString(R.string.field_email), ""));
                    fields.add(new FieldModel("phone", activity.getString(R.string.field_phone), ""));
                    break;
                case "internships":
                    fields.add(new FieldModel("role", activity.getString(R.string.field_role), ""));
                    fields.add(new FieldModel("date", activity.getString(R.string.field_date), ""));
                    fields.add(new FieldModel("comp", activity.getString(R.string.field_comp), ""));
                    fields.add(new FieldModel("desc", activity.getString(R.string.field_desc), "textarea", ""));
                    break;
                case "weblinks":
                    fields.add(new FieldModel("name", activity.getString(R.string.field_site_name), ""));
                    fields.add(new FieldModel("link", activity.getString(R.string.field_url), ""));
                    fields.add(new FieldModel("desc", activity.getString(R.string.field_desc), "textarea", ""));
                    break;
                case "declaration_block":
                case "declarationSection":
                    fields.add(new FieldModel("text", activity.getString(R.string.field_declaration_text), "According to my knowledge, I confirm that every information in this CV is correct.", "textarea"));
                    break;
                default:
                    fields.add(new FieldModel("val", activity.getString(R.string.field_val_content), ""));
            }
            return new ItemModel(fields);
        }
        
        public void addItem() {
            this.items.add(createDefaultItem(this.type));
        }

        public JSONObject toJson() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("name", name);
            json.put("type", type);
            json.put("icon", icon);
            json.put("group", group);
            json.put("col", column);
            json.put("isExpanded", isExpanded);
            json.put("spacerHeight", spacerHeight);
            JSONArray itemsArray = new JSONArray();
            for (ItemModel item : items) {
                itemsArray.put(item.toJson());
            }
            json.put("items", itemsArray);
            return json;
        }
    }

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

        public TemplateModel(String id, String name, int previewRes) {
            this.id = id;
            this.name = name;
            this.previewRes = previewRes;
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

    public List<SectionModel> ALL_SECTIONS;

    public CVWizardManager(MainActivity activity) {
        this.activity = activity;
        initPurposes();
        initTemplates();
        initAllSections();
        initViews();
    }

    private void initPurposes() {
        purposeList = Arrays.asList(
            new PurposeModel("job", activity.getString(R.string.purpose_job), "Professional local career focus.", R.drawable.avd_purpose_job),
            new PurposeModel("job_abroad", activity.getString(R.string.purpose_job_abroad), "International standards & visa support.", R.drawable.avd_purpose_job_abroad),
            new PurposeModel("academic", activity.getString(R.string.purpose_academic), "Research, teaching & scholarship focus.", R.drawable.avd_purpose_academic),
            new PurposeModel("study_abroad", activity.getString(R.string.purpose_study_abroad), "University admissions & student visas.", R.drawable.avd_purpose_study_abroad),
            new PurposeModel("marriage", activity.getString(R.string.purpose_marriage), "Matrimonial biodata & personal profiles.", R.drawable.avd_purpose_marriage)
        );
    }

    private void initTemplates() {
        templateList = Arrays.asList(
            new TemplateModel("header_standard", "Standard Header", R.drawable.avd_template_header),
            new TemplateModel("headerless", "Headerless Mode", R.drawable.avd_template_headerless)
        );
    }

    private void initAllSections() {
        ALL_SECTIONS = Arrays.asList(
            new SectionModel("profileSection", activity.getString(R.string.section_profile), "Profile photo and basic identity.", "fa-user-circle", "profile_pic", "gridEssentials"),
            new SectionModel("nameProfessionSection", activity.getString(R.string.section_name_prof), "Your name and professional title.", "fa-id-badge", "name_profession", "gridEssentials"),
            new SectionModel("personalDetails", activity.getString(R.string.section_personal), "Detailed personal information.", "fa-id-card", "personal", "gridEssentials"),
            new SectionModel("passportDetails", activity.getString(R.string.section_passport), "Information for international jobs.", "fa-passport", "passport", "gridEssentials"),
            new SectionModel("visaStatus", activity.getString(R.string.section_visa), "Current work authorization status.", "fa-file-invoice", "visaStatus", "gridEssentials"),
            new SectionModel("summarySection", activity.getString(R.string.section_summary), "Professional summary or objective.", "fa-user-tie", "summary_paragraph", "gridEssentials"),
            new SectionModel("languages", activity.getString(R.string.section_languages), "Languages you speak and write.", "fa-language", "languages", "gridEssentials"),
            new SectionModel("education", activity.getString(R.string.section_education), "Your academic background.", "fa-graduation-cap", "education", "gridExp"),
            new SectionModel("experience", activity.getString(R.string.section_experience), "Professional work history.", "fa-briefcase", "experience", "gridExp"),
            new SectionModel("projects", activity.getString(R.string.section_projects), "Academic or professional projects.", "fa-project-diagram", "projects", "gridExp"),
            new SectionModel("researchExp", activity.getString(R.string.section_research), "Scientific or academic research.", "fa-microscope", "researchExp", "gridExp"),
            new SectionModel("teachingExp", activity.getString(R.string.section_teaching), "Educational and teaching roles.", "fa-chalkboard-teacher", "teachingExp", "gridExp"),
            new SectionModel("grants", activity.getString(R.string.section_grants), "Funding and financial awards.", "fa-hand-holding-usd", "grants", "gridExp"),
            new SectionModel("skills", activity.getString(R.string.section_skills), "Core technical and soft skills.", "fa-tools", "skills", "gridExp"),
            new SectionModel("testScores", activity.getString(R.string.section_test_scores), "Standardized test results.", "fa-check-double", "testScores", "gridExp"),
            new SectionModel("certificates", activity.getString(R.string.section_certificates_simple), "Single-line list of honors.", "fa-certificate", "simple-list", "gridExp"),
            new SectionModel("awards", activity.getString(R.string.section_awards), "Recognition and formal awards.", "fa-trophy", "awards", "gridAdd"),
            new SectionModel("certifications", activity.getString(R.string.section_certifications), "Professional certifications.", "fa-certificate", "certifications", "gridAdd"),
            new SectionModel("volunteer", activity.getString(R.string.section_volunteer), "Unpaid work and social causes.", "fa-hands-helping", "volunteer", "gridAdd"),
            new SectionModel("publications", activity.getString(R.string.section_publications), "Books, journals, and articles.", "fa-book", "publications", "gridAdd"),
            new SectionModel("affiliations", activity.getString(R.string.section_affiliations), "Professional memberships.", "fa-users", "affiliations", "gridAdd"),
            new SectionModel("familyDetails", activity.getString(R.string.section_family), "Information for matrimonial use.", "fa-users-cog", "familyDetails", "gridAdd"),
            new SectionModel("partnerExpectations", activity.getString(R.string.section_expectations), "Marriage partner preferences.", "fa-heart", "partnerExpectations", "gridAdd"),
            new SectionModel("lifestyleHabits", activity.getString(R.string.section_lifestyle), "Personal habits and lifestyle.", "fa-apple-alt", "lifestyleHabits", "gridAdd"),
            new SectionModel("astrologySection", activity.getString(R.string.section_astrology), "Horoscope and details.", "fa-sun", "astrologySection", "gridAdd"),
            new SectionModel("hobbies", activity.getString(R.string.section_hobbies), "Personal interests and pastimes.", "fa-gamepad", "hobbies", "gridAdd"),
            new SectionModel("extra", activity.getString(R.string.section_extra), "Extracurricular activities.", "fa-futbol", "extra", "gridAdd"),
            new SectionModel("references", activity.getString(R.string.section_references), "Contacts who vouch for you.", "fa-user-check", "references", "gridAdd"),
            new SectionModel("training", activity.getString(R.string.section_training), "Specialized training courses.", "fa-chalkboard-teacher", "training", "gridAdd"),
            new SectionModel("internships", activity.getString(R.string.section_internships), "Short-term trainee positions.", "fa-laptop-code", "internships", "gridAdd"),
            new SectionModel("achievements", activity.getString(R.string.section_achievements), "Notable milestones accomplished.", "fa-star", "achievements", "gridAdd"),
            new SectionModel("weblinks", activity.getString(R.string.section_weblinks), "Social media and website links.", "fa-link", "weblinks", "gridAdd"),
            new SectionModel("contactDetails", activity.getString(R.string.section_contact), "How managers can reach you.", "fa-address-book", "contact", "gridEssentials"),
            new SectionModel("physicalProfile", activity.getString(R.string.section_physical), "Fitness and physical stats.", "fa-user-check", "physical", "gridAdd"),
            new SectionModel("visualRegistry", activity.getString(R.string.section_visual), "Portfolio and visual entries.", "fa-camera", "photography", "gridAdd"),
            new SectionModel("activeLife", activity.getString(R.string.section_active), "Sports and active engagement.", "fa-running", "active_lifestyle", "gridAdd"),
            new SectionModel("physicalFitness", activity.getString(R.string.section_interests), "Fitness related details.", "fa-heartbeat", "simple-list", "gridAdd"),
            new SectionModel("interests", activity.getString(R.string.section_interests), "Your personal interests.", "fa-star", "hobbies", "gridAdd"),
            new SectionModel("declarationSection", activity.getString(R.string.section_declaration), "Formal declaration statement.", "fa-file-signature", "declaration_block", "gridAdd")
        );
    }

    private void initViews() {
        wizardPanel = activity.findViewById(R.id.wizard_panel);
        wizardHeader = activity.findViewById(R.id.wizard_header);
        viewPager = activity.findViewById(R.id.wizard_viewPager);
        progressBar = activity.findViewById(R.id.wizard_progressBar);
        btnBack = activity.findViewById(R.id.wizard_btnBack);
        btnNext = activity.findViewById(R.id.wizard_btnNext);
        tvStepTitle = activity.findViewById(R.id.wizard_tvStepTitle);
        btnClose = activity.findViewById(R.id.wizard_btnClose);
        footerSpacer = activity.findViewById(R.id.wizard_footerSpacer);
        dragHandle = activity.findViewById(R.id.wizard_drag_handle);
        cardSteps = activity.findViewById(R.id.wizard_cardSteps);
        setupResizer();

        viewPager.setAdapter(new StepsAdapter());
        viewPager.setUserInputEnabled(false);

        btnClose.setOnClickListener(v -> closeWizard());

        btnBack.setOnClickListener(v -> {
            if (isSectionsOnlyMode) return; // Cannot go back in sections-only mode
            if (viewPager.getCurrentItem() > 0) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
                updateUI();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() == 0 && (selectedPurpose == null || selectedPurpose.isEmpty())) {
                Toast.makeText(activity, activity.getString(R.string.select_purpose_first), Toast.LENGTH_SHORT).show();
                return;
            }
            if (viewPager.getCurrentItem() == 1 && (selectedTemplate == null || selectedTemplate.isEmpty())) {
                Toast.makeText(activity, activity.getString(R.string.select_template_first), Toast.LENGTH_SHORT).show();
                return;
            }
            if (viewPager.getCurrentItem() < 2) {
                if (viewPager.getCurrentItem() == 0) {
                    applyPurposeDefaults();
                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                    updateUI();
                } else if (viewPager.getCurrentItem() == 1) {
                    syncDataFromWebViewAndShowSections(); 
                }
            } else {
                finishWizard();
            }
        });
    }

    private void setupKeyboardSync(View root) {
        View panel = activity.findViewById(R.id.wizard_cardSteps);
        if (panel == null) return;

        ViewCompat.setWindowInsetsAnimationCallback(panel, new WindowInsetsAnimationCompat.Callback(
                WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_STOP) {
            private int initialHeight = 0;
            private int lastHeight = -1;
            private boolean lastKbState = false;

            @Override
            public void onPrepare(@NonNull WindowInsetsAnimationCompat animation) {
                initialHeight = panel.getHeight();
            }

            @Override
            public WindowInsetsCompat onProgress(@NonNull WindowInsetsCompat insets, @NonNull List<WindowInsetsAnimationCompat> runningAnimations) {
                int keyboardHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
                int screenHeight = activity.getResources().getDisplayMetrics().heightPixels;
                
                boolean kbOpen = keyboardHeight > 0;
                
                // Only notify adapter if the state actually changes
                if (kbOpen != lastKbState) {
                    lastKbState = kbOpen;
                    if (sectionsAdapter != null) {
                        sectionsAdapter.setKeyboardOpen(kbOpen);
                    }
                }
                
                int targetHeight = initialHeight;
                if (kbOpen) {
                    View header = activity.findViewById(R.id.wizard_header);
                    int headerBottom = (header != null) ? header.getBottom() : 0;
                    float density = activity.getResources().getDisplayMetrics().density;
                    int margin = (int) (24 * density);
                    targetHeight = screenHeight - keyboardHeight - headerBottom - margin - (int)(20 * density); 
                    if (targetHeight < initialHeight) targetHeight = initialHeight;
                }

                if (targetHeight != lastHeight) {
                    lastHeight = targetHeight;
                    panel.getLayoutParams().height = targetHeight;
                    panel.requestLayout();
                }
                
                panel.setTranslationY(0); 
                return insets;
            }

            @Override
            public void onEnd(@NonNull WindowInsetsAnimationCompat animation) {
                // Final precision nudge at the end of animation
                checkAndCorrectFocus();
            }
        });
    }

    private void checkAndCorrectFocus() {
        View focused = activity.getCurrentFocus();
        View dragHandle = activity.findViewById(R.id.wizard_drag_handle);
        RecyclerView rv = activity.findViewById(R.id.rvSections);
        View panel = activity.findViewById(R.id.wizard_cardSteps);
        
        if (focused instanceof android.widget.EditText && dragHandle != null && rv != null && panel != null && focused.getTag() != null) {
            // MAGNETIC ANCHOR: Push & Pull to 50% of the interactive zone
            doPrecisionCenter(focused, dragHandle, rv, panel);
            
            // SECOND PASS: Re-center after 500ms to catch any animation drift
            rv.postDelayed(() -> {
                View stillFocused = activity.getCurrentFocus();
                if (stillFocused == focused) {
                    doPrecisionCenter(focused, dragHandle, rv, panel);
                }
            }, 500);
        }
    }
    
    private void doPrecisionCenter(View focused, View dragHandle, RecyclerView rv, View panel) {
        rv.post(() -> {
            // The ANCHOR: 50% between drag handle bottom and keyboard top
            int[] dragLoc = new int[2];
            dragHandle.getLocationOnScreen(dragLoc);
            int topY = dragLoc[1] + dragHandle.getHeight(); // Bottom edge of drag handle
            
            int screenHeight = activity.getResources().getDisplayMetrics().heightPixels;
            WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(panel);
            int keyboardHeight = (insets != null) ? insets.getInsets(WindowInsetsCompat.Type.ime()).bottom : 0;
            int bottomY = screenHeight - keyboardHeight; // Top edge of keyboard
            
            // THE 50% MAGNETIC ANCHOR
            int anchorY = (topY + bottomY) / 2;

            // Find the Label + Field unit center
            android.view.ViewGroup container = (android.view.ViewGroup) focused.getParent();
            int fieldIndex = container.indexOfChild(focused);
            View labelView = (fieldIndex > 0) ? container.getChildAt(fieldIndex - 1) : null;
            
            int unitCenterY;
            if (labelView != null) {
                int[] labelLoc = new int[2];
                labelView.getLocationOnScreen(labelLoc);
                int topPoint = labelLoc[1];
                
                int[] fieldLoc = new int[2];
                focused.getLocationOnScreen(fieldLoc);
                int bottomPoint = fieldLoc[1] + focused.getHeight();
                
                unitCenterY = (topPoint + bottomPoint) / 2;
            } else {
                int[] fieldLoc = new int[2];
                focused.getLocationOnScreen(fieldLoc);
                unitCenterY = fieldLoc[1] + (focused.getHeight() / 2);
            }
            
            // PUSH or PULL until unit center == anchor
            int delta = unitCenterY - anchorY;
            if (Math.abs(delta) > 10) { // Only scroll if off by more than 10px
                rv.smoothScrollBy(0, delta);
            }
        });
    }

    public void openFullWizard() {
        activity.setMainUIForWizard(true);
        isSectionsOnlyMode = false;
        if (wizardPanel != null) {
            wizardPanel.setVisibility(View.VISIBLE);
            
            if (wizardHeader != null) {
                wizardHeader.setAlpha(0f);
                wizardHeader.setTranslationY(-200f);
                wizardHeader.animate().alpha(1f).translationY(0f).setDuration(400).start();
            }
            if (progressBar != null) {
                progressBar.setAlpha(0f);
                progressBar.setTranslationY(-200f);
                progressBar.animate().alpha(1f).translationY(0f).setDuration(400).start();
            }

            if (cardSteps != null) {
                cardSteps.setAlpha(0f);
                float height = cardSteps.getHeight() > 0 ? cardSteps.getHeight() : 1200f;
                cardSteps.setTranslationY(height);
                cardSteps.animate().alpha(1f).translationY(0f).setDuration(400)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator()).start();
            }

            viewPager.setCurrentItem(0, false);
            updateUI();
        }
    }

    public void openSectionsOnly() {
        isSectionsOnlyMode = true;
        syncDataFromWebViewAndShowSections();
    }

    public void syncDataFromWebViewAndShowSections() {
        if (activity == null || activity.myWebView == null) return;
        
        activity.runOnUiThread(() -> {
            activity.myWebView.evaluateJavascript("window.getResumeData();", value -> {
                if (value != null && !value.equals("null") && !value.equals("\"{}\"")) {
                    try {
                        String jsonStr = value;
                        if (jsonStr.startsWith("\"") && jsonStr.endsWith("\"")) {
                            jsonStr = jsonStr.substring(1, jsonStr.length() - 1)
                                            .replace("\\\"", "\"")
                                            .replace("\\\\", "\\");
                        }
                        
                        JSONObject resumeData = new JSONObject(jsonStr);
                        List<SectionModel> syncedSections = new ArrayList<>();
                        
                        java.util.Iterator<String> keys = resumeData.keys();
                        while (keys.hasNext()) {
                            String sectionId = keys.next();
                            JSONObject sectionJson = resumeData.getJSONObject(sectionId);
                            
                            SectionModel sm = null;
                            for (SectionModel predefined : ALL_SECTIONS) {
                                if (predefined.id.equals(sectionId)) {
                                    sm = predefined;
                                    break;
                                }
                            }
                            
                            String sectionName = sectionJson.optString("name", sectionId);
                            String sectionDesc = (sm != null) ? sm.desc : "A dynamic section in your CV.";
                            String sectionIcon = (sm != null) ? sm.icon : "fa-plus-circle";
                            String sectionType = (sm != null) ? sm.type : "dynamic";
                            String sectionGroup = (sm != null) ? sm.group : "dynamic";
                            
                            boolean wasExpanded = false;
                            for (SectionModel old : currentSections) {
                                if (old.id.equals(sectionId)) {
                                    wasExpanded = old.isExpanded;
                                    break;
                                }
                            }
                            
                            SectionModel syncedSm = new SectionModel(sectionId, sectionName, sectionDesc, sectionIcon, sectionType, sectionGroup);
                            syncedSm.isExpanded = wasExpanded;
                            syncedSm.items.clear();
                            // Removed pre-population of template fields to ensure MSW strictly mirrors WebView
                            
                            JSONArray itemsJson = sectionJson.optJSONArray("items");
                            if (itemsJson != null && itemsJson.length() > 0) {
                                for (int i = 0; i < itemsJson.length(); i++) {
                                    JSONObject itemObj = itemsJson.optJSONObject(i);
                                    if (itemObj == null) continue;

                                    JSONArray fieldsArray = itemObj.optJSONArray("fields");
                                    if (fieldsArray != null) {
                                        // Nested structure: { items: [ { fields: [...] } ] }
                                        ItemModel currentItem;
                                        if (i < syncedSm.items.size()) {
                                            currentItem = syncedSm.items.get(i);
                                        } else {
                                            currentItem = new ItemModel(new ArrayList<>());
                                            syncedSm.items.add(currentItem);
                                        }

                                        for (int j = 0; j < fieldsArray.length(); j++) {
                                            JSONObject fieldJson = fieldsArray.getJSONObject(j);
                                            String fKey = fieldJson.optString("key", "");
                                            String fVal = fieldJson.optString("value", "");
                                            String fUrl = fieldJson.optString("url", null);
                                            String fLabel = fieldJson.optString("label", "");
                                            boolean fEditable = fieldJson.optBoolean("editable", true);
                                            boolean fVisible = fieldJson.optBoolean("visible", true);

                                            boolean fieldFound = false;
                                            for (FieldModel fm : currentItem.fields) {
                                                if (fm.key.equals(fKey)) {
                                                    fm.value = fVal;
                                                    if (fUrl != null) fm.url = fUrl;
                                                    fm.isEditable = fEditable;
                                                    fm.isVisible = fVisible;
                                                    fieldFound = true;
                                                    break;
                                                }
                                            }
                                            if (!fieldFound && !fKey.isEmpty()) {
                                                if (fLabel.isEmpty()) {
                                                    fLabel = fKey.substring(0,1).toUpperCase() + fKey.substring(1);
                                                    if (sm != null && !sm.items.isEmpty()) {
                                                        for (FieldModel tfm : sm.items.get(0).fields) {
                                                            if (tfm.key.equals(fKey)) {
                                                                fLabel = tfm.label;
                                                                break;
                                                            }
                                                        }
                                                    }
                                                }
                                                FieldModel newFm = new FieldModel(fKey, fLabel, fVal, "text", currentItem.fields.size());
                                                newFm.isEditable = fEditable;
                                                newFm.isVisible = fVisible;
                                                currentItem.fields.add(newFm);
                                            }
                                        }
                                    } else {
                                        // Flat structure: { items: [ { key: '...', value: '...' } ] } (Fallback/Legacy)
                                        String fKey = itemObj.optString("key", "");
                                        String fVal = itemObj.optString("value", "");
                                        boolean fEditable = itemObj.optBoolean("editable", true);
                                        boolean fVisible = itemObj.optBoolean("visible", true);
                                        if (fKey.isEmpty()) continue;

                                        ItemModel firstItem = syncedSm.items.isEmpty() ? null : syncedSm.items.get(0);
                                        if (firstItem == null) {
                                            firstItem = new ItemModel(new ArrayList<>());
                                            syncedSm.items.add(firstItem);
                                        }

                                        boolean fieldFound = false;
                                        for (FieldModel fm : firstItem.fields) {
                                            if (fm.key.equals(fKey)) {
                                                fm.value = fVal;
                                                fm.isEditable = fEditable;
                                                fm.isVisible = fVisible;
                                                fieldFound = true;
                                                break;
                                            }
                                        }
                                        if (!fieldFound) {
                                            String fLabel = itemObj.optString("label", "");
                                            if (fLabel.isEmpty()) {
                                                fLabel = fKey.substring(0,1).toUpperCase() + fKey.substring(1);
                                                if (sm != null && !sm.items.isEmpty()) {
                                                    for (FieldModel tfm : sm.items.get(0).fields) {
                                                        if (tfm.key.equals(fKey)) {
                                                            fLabel = tfm.label;
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                            FieldModel newFm = new FieldModel(fKey, fLabel, fVal, "text", firstItem.fields.size());
                                            newFm.isEditable = fEditable;
                                            newFm.isVisible = fVisible;
                                            firstItem.fields.add(newFm);
                                        }
                                    }
                                }
                            }
                            syncedSections.add(syncedSm);
                        }
                        
                        if (!syncedSections.isEmpty()) {
                            currentSections.clear();
                            currentSections.addAll(syncedSections);
                        }
                        
                        final String addItemTarget = pendingAddItemSectionId;
                        pendingAddItemSectionId = null;
                        
                        activity.runOnUiThread(() -> {
                            if (addItemTarget != null && sectionsAdapter != null) {
                                // Targeted refresh: only update sections adapter, skip ViewPager rebuild
                                int targetIdx = -1;
                                for (int idx = 0; idx < currentSections.size(); idx++) {
                                    if (currentSections.get(idx).id.equals(addItemTarget)) {
                                        targetIdx = idx;
                                        break;
                                    }
                                }
                                if (targetIdx >= 0) {
                                    sectionsAdapter.pendingFocusSectionIndex = targetIdx;
                                    sectionsAdapter.pendingFocusLastItem = true;
                                }
                                sectionsAdapter.notifyDataSetChanged();
                                if (targetIdx >= 0) {
                                    RecyclerView rv = activity.findViewById(R.id.rvSections);
                                    if (rv != null) {
                                        final int scrollTo = targetIdx;
                                        rv.post(() -> rv.smoothScrollToPosition(scrollTo));
                                    }
                                }
                            } else {
                                if (viewPager != null && viewPager.getAdapter() != null) {
                                    viewPager.getAdapter().notifyDataSetChanged();
                                }
                                updateUI();
                            }
                        });
                        
                    } catch (Exception e) {
                        Log.e("CVWizardManager", "Error parsing resume data", e);
                    }
                }
            });
        });

        if (wizardPanel != null) {
            wizardPanel.setVisibility(View.VISIBLE);
            
            if (wizardHeader != null) {
                wizardHeader.setAlpha(0f);
                wizardHeader.setTranslationY(-200f);
                wizardHeader.animate().alpha(1f).translationY(0f).setDuration(400).start();
            }
            if (progressBar != null) {
                progressBar.setAlpha(0f);
                progressBar.setTranslationY(-200f);
                progressBar.animate().alpha(1f).translationY(0f).setDuration(400).start();
            }

            if (cardSteps != null) {
                cardSteps.setAlpha(0f);
                float height = cardSteps.getHeight() > 0 ? cardSteps.getHeight() : 1200f;
                cardSteps.setTranslationY(height);
                cardSteps.animate().alpha(1f).translationY(0f).setDuration(400)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator()).start();
            }

            progressBar.setVisibility(View.GONE);
            viewPager.setCurrentItem(2, false);
            updateUI();
        }
    }

    public void openWizard() {
        if (isWizardOpen()) return;
        openSectionsOnly();
    }

    public boolean isWizardOpen() {
        return wizardPanel != null && wizardPanel.getVisibility() == View.VISIBLE;
    }

    public void closeWizard() {
        activity.setMainUIForWizard(false);
        
        // Clear WebView highlights
        activity.myWebView.evaluateJavascript("if(window.clearWizardHighlights) window.clearWizardHighlights();", null);

        if (wizardPanel != null) {
            if (wizardHeader != null) {
                wizardHeader.animate().alpha(0f).translationY(-200f).setDuration(350).start();
            }
            if (progressBar != null) {
                progressBar.animate().alpha(0f).translationY(-200f).setDuration(350).start();
            }

            if (cardSteps != null) {
                float height = cardSteps.getHeight() > 0 ? cardSteps.getHeight() : 1200f;
                cardSteps.animate()
                    .alpha(0f)
                    .translationY(height)
                    .setDuration(350)
                    .setInterpolator(new android.view.animation.AccelerateInterpolator())
                    .withEndAction(() -> {
                        wizardPanel.setVisibility(View.GONE);
                    }).start();
            }
        }
    }

    private void finishWizard() {
        // [CRITICAL NOTE FOR FUTURE AI & DEVELOPERS]: 
        // This wizard is configured to ONLY HIDE ITSELF upon completion. 
        // DO NOT add or re-enable any 'sync' or 'Big Payload' logic (like setupStepByStep) here. 
        // The CV is already updated in real-time as the user selects their goal/purpose in Step 1 
        // using individual androidAddSection commands. Re-enabling sync here will cause 
        // duplication or overwrite the user's manual changes.
        closeWizard();
    }

    private void syncSectionsToWebView() {
        try {
            JSONArray sectionsArray = new JSONArray();
            for (SectionModel sm : currentSections) {
                sectionsArray.put(sm.toJson());
            }
            String payload = sectionsArray.toString();
            activity.logToDebug("Data", "Wizard: Syncing (" + payload.length() + " bytes) - Template: " + selectedTemplate);
            
            // Ultra-diverse logging: detail every section and its field values
            for (SectionModel sm : currentSections) {
                StringBuilder details = new StringBuilder();
                details.append("Wizard Section: ").append(sm.id).append(" - ");
                if (sm.items != null && !sm.items.isEmpty()) {
                    details.append(sm.items.size()).append(" items. | ");
                    for (int i = 0; i < sm.items.size(); i++) {
                        ItemModel item = sm.items.get(i);
                        details.append("Item ").append(i).append(": ");
                        if (item.fields != null) {
                            for (FieldModel fm : item.fields) {
                                if (fm.value != null && !fm.value.trim().isEmpty()) {
                                    details.append("[").append(fm.label).append(":").append(fm.value).append("] ");
                                }
                            }
                        }
                    }
                } else {
                    details.append("EMPTY! (0 items)");
                }
                activity.logToDebug("Data", details.toString());
            }

            activity.logToDebug("Data", "Wizard: Full Payload: " + (payload.length() > 500 ? payload.substring(0, 500) : payload));
            
            String js = String.format("if(window.setupStepByStep) window.setupStepByStep(%s, '%s');",
                    payload, selectedTemplate != null ? selectedTemplate : "default");
            
            activity.runOnUiThread(() -> {
                if (activity.myWebView != null) {
                    activity.myWebView.evaluateJavascript(js, null);
                }
            });
        } catch (Exception e) {
            Log.e("CVWizardManager", "Error syncing sections", e);
        }
    }

    private void stylePrimaryButton(Button btn, String text, int iconRes) {
        btn.setText(text.toUpperCase());
        btn.setAllCaps(true);
        btn.setTextColor(android.graphics.Color.WHITE);
        btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF545965));
        btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, iconRes, 0);
        btn.setCompoundDrawableTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE));
    }

    private void styleSecondaryButton(Button btn, String text, int iconRes) {
        btn.setVisibility(View.VISIBLE);
        btn.setText(text.toUpperCase());
        btn.setAllCaps(true);
        btn.setTextColor(0xFFA0A4B0);
        btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT));
        btn.setCompoundDrawablesWithIntrinsicBounds(iconRes, 0, 0, 0);
        btn.setCompoundDrawableTintList(android.content.res.ColorStateList.valueOf(0xFFA0A4B0));
    }

    private void updateUI() {
        int current = viewPager.getCurrentItem();
        progressBar.setProgress(current + 1);

        switch (current) {
            case 0:
                tvStepTitle.setText(activity.getString(R.string.select_purpose));
                btnBack.setVisibility(View.GONE);
                footerSpacer.setVisibility(View.GONE);
                stylePrimaryButton(btnNext, "CONTINUE", R.drawable.ic_next);
                
                if (selectedPurpose == null || selectedPurpose.isEmpty()) {
                    btnNext.setVisibility(View.INVISIBLE);
                } else {
                    btnNext.setVisibility(View.VISIBLE);
                    btnNext.setEnabled(true);
                    btnNext.setAlpha(1.0f);
                }
                break;
            case 1:
                tvStepTitle.setText("CHOOSE HEADER MODE");
                btnBack.setVisibility(View.VISIBLE);
                styleSecondaryButton(btnBack, "BACK", R.drawable.ic_back);
                footerSpacer.setVisibility(View.VISIBLE);
                stylePrimaryButton(btnNext, "NEXT", R.drawable.ic_next);
                
                if (selectedTemplate == null || selectedTemplate.isEmpty()) {
                    btnNext.setVisibility(View.INVISIBLE);
                } else {
                    btnNext.setVisibility(View.VISIBLE);
                    btnNext.setEnabled(true);
                    btnNext.setAlpha(1.0f);
                }
                break;
            case 2:
                tvStepTitle.setText(activity.getString(R.string.manage_sections));
                if (isSectionsOnlyMode) {
                    btnBack.setVisibility(View.GONE);
                    footerSpacer.setVisibility(View.GONE);
                    stylePrimaryButton(btnNext, "DONE", R.drawable.ic_finish);
                } else {
                    btnBack.setVisibility(View.VISIBLE);
                    styleSecondaryButton(btnBack, "BACK", R.drawable.ic_back);
                    footerSpacer.setVisibility(View.VISIBLE);
                    stylePrimaryButton(btnNext, "SWITCH", R.drawable.ic_finish);
                }
                btnNext.setEnabled(true);
                btnNext.setAlpha(1.0f);
                break;
        }
    }

    private void applySectionsIndividually(String purpose) {
        if (purpose == null) return; 

        // Step 1: Cleanup - Always clear everything first as requested
        activity.runOnUiThread(() -> {
            activity.clearAllNativeSections();
        });

        // Step 2: Define Section sets based on the approved implementation plan
        String[][] selectedSet;
        switch (purpose) {
            case "job_abroad":
                selectedSet = new String[][]{
                    {"headerBoxSection", "header"},
                    {"profileSection", "header"},
                    {"nameProfessionSection", "header"},
                    {"contactDetails", "left"},
                    {"personalDetails", "left"},
                    {"summarySection", "both"},
                    {"visaStatus", "left"},
                    {"passportDetails", "left"},
                    {"testScores", "left"},
                    {"languages", "left"},
                    {"education", "right"},
                    {"experience", "right"},
                    {"projects", "right"},
                    {"skills", "left"},
                    {"references", "right"},
                    {"volunteer", "right"},
                    {"training", "right"},
                    {"achievements", "right"},
                    {"certifications", "left"},
                    {"internships", "right"},
                    {"declarationSection", "both"}
                };
                break;
            case "academic":
                selectedSet = new String[][]{
                    {"headerBoxSection", "header"},
                    {"profileSection", "header"},
                    {"nameProfessionSection", "header"},
                    {"contactDetails", "left"},
                    {"personalDetails", "left"},
                    {"skills", "left"},
                    {"awards", "left"},
                    {"summarySection", "both"},
                    {"education", "right"},
                    {"researchExp", "right"},
                    {"teachingExp", "right"},
                    {"publications", "right"},
                    {"grants", "right"},
                    {"affiliations", "right"},
                    {"declarationSection", "both"}
                };
                break;
            case "study_abroad":
                selectedSet = new String[][]{
                    {"headerBoxSection", "header"},
                    {"profileSection", "header"},
                    {"nameProfessionSection", "header"},
                    {"contactDetails", "left"},
                    {"personalDetails", "left"},
                    {"visaStatus", "left"},
                    {"passportDetails", "left"},
                    {"testScores", "left"},
                    {"languages", "left"},
                    {"skills", "left"},
                    {"summarySection", "both"},
                    {"education", "right"},
                    {"experience", "right"},
                    {"projects", "right"},
                    {"researchExp", "right"},
                    {"internships", "right"},
                    {"declarationSection", "both"}
                };
                break;
            case "marriage":
                selectedSet = new String[][]{
                    {"headerBoxSection", "header"},
                    {"profileSection", "header"},
                    {"nameProfessionSection", "header"},
                    {"physicalProfile", "left"},
                    {"astrologySection", "left"},
                    {"familyDetails", "left"},
                    {"languages", "left"},
                    {"lifestyleHabits", "left"},
                    {"hobbies", "left"},
                    {"primaryEducation", "right"}, // Wait, id is just 'education' in JS
                    {"education", "right"},
                    {"experience", "right"},
                    {"partnerExpectations", "right"},
                    {"summarySection", "both"},
                    {"visualRegistry", "right"}
                };
                break;
            case "job":
            default:
                selectedSet = new String[][]{
                    {"headerBoxSection", "header"},
                    {"profileSection", "header"},
                    {"nameProfessionSection", "header"},
                    {"contactDetails", "left"},
                    {"personalDetails", "left"},
                    {"summarySection", "both"},
                    {"languages", "left"},
                    {"education", "right"},
                    {"experience", "right"},
                    {"projects", "right"},
                    {"skills", "left"},
                    {"references", "right"},
                    {"training", "right"},
                    {"volunteer", "right"},
                    {"certifications", "left"},
                    {"awards", "left"},
                    {"declarationSection", "both"}
                };
                break;
        }

        // Step 3: Add Sections one-by-one with specific column overrides
        for (String[] sec : selectedSet) {
            final String id = sec[0];
            final String col = sec[1];
            activity.runOnUiThread(() -> {
                activity.addNativeSection(id, col);
            });
        }
    }

    private void applyPurposeDefaults() {
        Log.d("DIVA-ContactTracker", "applyPurposeDefaults() triggered. Purpose: " + selectedPurpose);
        List<String> defaultIds = new ArrayList<>(Arrays.asList(
            "headerBoxSection", "profileSection", "nameProfessionSection", "contactDetails", "summarySection", "personalDetails", 
            "education", "experience", "projects", "skills", "declarationSection"
        ));

        switch (selectedPurpose) {
            case "job":
                defaultIds.remove("visaStatus");
                defaultIds.add("references");
                defaultIds.add("training");
                defaultIds.add("volunteer");
                defaultIds.add("certifications");
                defaultIds.add("awards");
                break;
            case "job_abroad":
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
                defaultIds.remove("visaStatus");
                defaultIds.add("researchExp");
                defaultIds.add("teachingExp");
                defaultIds.add("publications");
                defaultIds.add("grants");
                defaultIds.add("affiliations");
                defaultIds.add("awards");
                break;
            case "study_abroad":
                defaultIds.add("visaStatus");
                defaultIds.add("testScores");
                defaultIds.add("internships");
                defaultIds.add("volunteer");
                defaultIds.add("researchExp");
                break;
            case "marriage":
                defaultIds.clear();
                defaultIds.addAll(Arrays.asList(
                    "headerBoxSection", "profileSection", "summarySection", 
                    "physicalProfile", "astrologySection", "familyDetails",
                    "education", "experience", "languages",
                    "lifestyleHabits", "hobbies", "partnerExpectations", "visualRegistry"
                ));
                break;
            default:
                // Fallback to defaults
                break;
        }

        if (currentSections.isEmpty()) {
            Log.d("DIVA-ContactTracker", "Applying defaultIds: " + defaultIds.toString() + " (Checking if contactDetails is present)");
            for (String id : defaultIds) {
                for (SectionModel s : ALL_SECTIONS) {
                    if (s.id.equals(id)) {
                        currentSections.add(s);
                        break;
                    }
                }
            }
        }
    }

    private void updateSectionsBasedOnTemplate() {
        // Neutered: We no longer auto-inject sections when switching templates.
    }

    private boolean templateHasHeader() {
        if ("default".equals(selectedTemplate) || "sidebar".equals(selectedTemplate) || "header".equals(selectedTemplate)) {
            return true; 
        }
        if ("headerless".equals(selectedTemplate)) {
            return false;
        }
        if (templateJsonData == null) return true; 
        try {
            JSONObject template = new JSONObject(templateJsonData);
            if (template.has("hasHeader")) {
                return template.getBoolean("hasHeader");
            }
            if (template.has("sections")) {
                JSONArray sections = template.getJSONArray("sections");
                for (int i = 0; i < sections.length(); i++) {
                    String id = sections.getJSONObject(i).optString("id", "");
                    if (id.equals("headerSection") || id.equals("mainHeader")) return true;
                }
                return false; 
            }
            String html = template.optString("html", "");
            if (!html.isEmpty()) {
                return html.contains("id=\"mainHeader\"") || html.contains("id='mainHeader'");
            }
            return true;
        } catch (Exception e) {
            return true;
        }
    }


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
                setupKeyboardSync(view);
                return new SectionsViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof SectionsViewHolder) {
                SectionsViewHolder svh = (SectionsViewHolder) holder;
                sectionsAdapter = new SectionsAdapter(currentSections);
                svh.rv.setAdapter(sectionsAdapter);
                svh.rv.setLayoutManager(new LinearLayoutManager(activity));
                
                View btnAdd = holder.itemView.findViewById(R.id.btnAddSection);
                if (btnAdd != null) {
                    btnAdd.setOnClickListener(v -> {
                        showAddSectionDialog();
                    });
                }
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
        
        @Override
        public int getItemViewType(int position) {
            return position;
        }
    }

    private class PurposeAdapter extends RecyclerView.Adapter<PurposeAdapter.ViewHolder> {
        private final java.util.Set<Integer> animatedPositions = new java.util.HashSet<>();

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_step_purpose, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            PurposeModel pm = purposeList.get(position);
            holder.tvTitle.setText(pm.name);
            holder.tvDesc.setText(pm.desc);
            holder.ivIcon.setImageResource(pm.iconRes);

            // AVD Logic: Start animation with auto-restart for one-shot AVDs
            android.graphics.drawable.Drawable d = holder.ivIcon.getDrawable();
            if (d instanceof android.graphics.drawable.AnimatedVectorDrawable) {
                android.graphics.drawable.AnimatedVectorDrawable avd = (android.graphics.drawable.AnimatedVectorDrawable) d;
                avd.registerAnimationCallback(new android.graphics.drawable.Animatable2.AnimationCallback() {
                    @Override
                    public void onAnimationEnd(android.graphics.drawable.Drawable drawable) {
                        // Reset and replay after 1.5s pause so it loops gracefully
                        holder.ivIcon.postDelayed(() -> {
                            avd.reset();
                            avd.start();
                        }, 1500);
                    }
                });
                avd.start();
            }

            // STAGGERED ENTRANCE ANIMATION — only on first bind
            if (!animatedPositions.contains(position)) {
                animatedPositions.add(position);
                holder.itemView.setAlpha(0f);
                holder.itemView.setScaleX(0.85f);
                holder.itemView.setScaleY(0.85f);
                holder.itemView.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(450)
                        .setStartDelay(100 * position)
                        .setInterpolator(new android.view.animation.OvershootInterpolator(1.2f))
                        .start();
            } else {
                // Already animated — ensure fully visible
                holder.itemView.setAlpha(1f);
                holder.itemView.setScaleX(1f);
                holder.itemView.setScaleY(1f);
            }

            // Highlight selected
            boolean isSelected = pm.id.equals(selectedPurpose);
            holder.card.setStrokeWidth(isSelected ? (int)(3 * activity.getResources().getDisplayMetrics().density) : 0);
            holder.card.setStrokeColor(0xFF545965);

            holder.itemView.setOnClickListener(v -> {
                boolean wasAlreadySelected = pm.id.equals(selectedPurpose);
                selectedPurpose = pm.id;
                notifyDataSetChanged();
                updateUI();
                
                // Haptic feedback logic
                v.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS);
                
                // USER REQUEST: Add sections individually for the Job purpose
                // Only apply if it's NOT the purpose we just applied (prevents duplicates on double-click/back)
                if (!pm.id.equals(lastAppliedPurpose)) {
                    applySectionsIndividually(pm.id);
                    lastAppliedPurpose = pm.id;
                }

                // Auto-advance after short delay for feedback
                v.postDelayed(() -> {
                    if (viewPager.getCurrentItem() == 0) {
                        applyPurposeDefaults();
                        viewPager.setCurrentItem(1);
                        updateUI();
                    }
                }, 300);
            });
        }

        @Override
        public int getItemCount() {
            return purposeList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            com.google.android.material.card.MaterialCardView card;
            android.widget.ImageView ivIcon;
            android.widget.TextView tvTitle, tvDesc;

            public ViewHolder(View v) {
                super(v);
                card = v.findViewById(R.id.cardPurpose);
                ivIcon = v.findViewById(R.id.ivPurposeIcon);
                tvTitle = v.findViewById(R.id.tvPurposeTitle);
                tvDesc = v.findViewById(R.id.tvPurposeDesc);
            }
        }
    }

    private class TemplateAdapter extends RecyclerView.Adapter<TemplateAdapter.ViewHolder> {
        private final java.util.Set<Integer> animatedPositions = new java.util.HashSet<>();

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_step_template, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            TemplateModel tm = templateList.get(position);
            holder.tvTitle.setText(tm.name);
            holder.ivPreview.setImageResource(tm.previewRes);

            // AVD Logic: Start animation
            android.graphics.drawable.Drawable d = holder.ivPreview.getDrawable();
            if (d instanceof android.graphics.drawable.AnimatedVectorDrawable) {
                ((android.graphics.drawable.AnimatedVectorDrawable) d).start();
            }

            // STAGGERED ENTRANCE ANIMATION
            if (!animatedPositions.contains(position)) {
                animatedPositions.add(position);
                holder.itemView.setAlpha(0f);
                holder.itemView.setScaleX(0.85f);
                holder.itemView.setScaleY(0.85f);
                holder.itemView.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(450)
                        .setStartDelay(100 * position)
                        .setInterpolator(new android.view.animation.OvershootInterpolator(1.2f))
                        .start();
            } else {
                holder.itemView.setAlpha(1f);
                holder.itemView.setScaleX(1f);
                holder.itemView.setScaleY(1f);
            }

            // Highlight selected
            boolean isSelected = tm.id.equals(selectedTemplate);
            holder.card.setStrokeWidth(isSelected ? (int)(3 * activity.getResources().getDisplayMetrics().density) : 0);
            holder.card.setStrokeColor(0xFF545965);

            holder.itemView.setOnClickListener(v -> {
                selectedTemplate = tm.id;
                notifyDataSetChanged();
                updateUI();
                
                v.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS);
                
                // DIVA DIAGNOSTIC: Log all current sections to understand the state
                StringBuilder sectionIds = new StringBuilder();
                for (SectionModel currentSec : currentSections) {
                    sectionIds.append(currentSec.id).append(", ");
                }
                if (activity != null) activity.logToDebug("Wizard", "[DIVA-STEP2] Template '" + tm.id + "' clicked. currentSections=[" + sectionIds + "] count=" + currentSections.size());
                
                // Safely determine if the Header Box already exists in the DOM natively
                boolean hasHeader = false;
                for (SectionModel currentSec : currentSections) {
                    if ("headerBoxSection".equals(currentSec.id)) {
                        hasHeader = true;
                        break;
                    }
                }
                if (activity != null) activity.logToDebug("Wizard", "[DIVA-STEP2] hasHeader (from currentSections scan) = " + hasHeader);

                // Also check DOM directly via JS for a secondary confirmation
                if (activity != null && activity.myWebView != null) {
                    activity.myWebView.evaluateJavascript(
                        "(() => { " +
                        "  const h = document.getElementById('mainHeader'); " +
                        "  const resumePage = document.getElementById('resumePage'); " +
                        "  const profile = document.getElementById('profileSection'); " +
                        "  const nameSec = document.getElementById('nameProfessionSection'); " +
                        "  const contact = document.getElementById('contactDetails'); " +
                        "  const summary = document.getElementById('summarySection'); " +
                        "  const msg = '[DIVA-DOM] mainHeader=' + !!h + " +
                        "    ' | profileSection=' + !!profile + (profile ? '(parent=' + (profile.parentElement ? profile.parentElement.id || profile.parentElement.className : 'NONE') + ')' : '') + " +
                        "    ' | nameProfessionSection=' + !!nameSec + (nameSec ? '(parent=' + (nameSec.parentElement ? nameSec.parentElement.id || nameSec.parentElement.className : 'NONE') + ')' : '') + " +
                        "    ' | contactDetails=' + !!contact + (contact ? '(parent=' + (contact.parentElement ? contact.parentElement.id || contact.parentElement.className : 'NONE') + ')' : '') + " +
                        "    ' | summarySection=' + !!summary + (summary ? '(parent=' + (summary.parentElement ? summary.parentElement.id || summary.parentElement.className : 'NONE') + ')' : ''); " +
                        "  if (window.divaLog) window.divaLog(msg); " +
                        "  console.log(msg); " +
                        "  return msg; " +
                        "})()",
                        result -> { if (activity != null) activity.logToDebug("Wizard", "[DIVA-STEP2] DOM state: " + result); }
                    );
                }

                // Strictly enforce 'Headerless' Macro (Relocates Identity/Summary surgically)
                if ("headerless".equals(tm.id)) {
                    if (activity != null) activity.logToDebug("Wizard", "[DIVA-STEP2] EXECUTING: switchToHeaderlessManual()");
                    activity.myWebView.evaluateJavascript("window.switchToHeaderlessManual()", null);
                } 
                // Strictly enforce 'Standard' = Restore header and move identity sections into it
                else if ("header_standard".equals(tm.id) || "standard".equals(tm.id)) {
                    if (!hasHeader) {
                        if (activity != null) activity.logToDebug("Wizard", "[DIVA-STEP2] EXECUTING: switchToHeaderManual() (header NOT found in currentSections)");
                        activity.myWebView.evaluateJavascript("window.switchToHeaderManual()", null);
                    } else {
                        if (activity != null) activity.logToDebug("Wizard", "[DIVA-STEP2] SKIPPING: switchToHeaderManual() because hasHeader=true (headerBoxSection found in currentSections list)");
                    }
                } else {
                    if (activity != null) activity.logToDebug("Wizard", "[DIVA-STEP2] Template '" + tm.id + "' is neither 'headerless' nor 'header_standard'. No header macro executed.");
                }
                
                // Neutered: Total Sync Blackout per user request to prevent layout jumps.
                // syncSectionsWithWebView();

                v.postDelayed(() -> {
                    if (viewPager.getCurrentItem() == 1) {
                        viewPager.setCurrentItem(2);
                        updateUI();
                    }
                }, 300);
            });
        }

        @Override
        public int getItemCount() {
            return templateList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            com.google.android.material.card.MaterialCardView card;
            android.widget.ImageView ivPreview;
            android.widget.TextView tvTitle;

            public ViewHolder(View v) {
                super(v);
                card = v.findViewById(R.id.cardTemplate);
                ivPreview = v.findViewById(R.id.ivTemplatePreview);
                tvTitle = v.findViewById(R.id.tvTemplateTitle);
            }
        }
    }

    private class SectionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_SECTION = 0;
        private static final int TYPE_FOOTER = 1;
        
        private final List<SectionModel> sections;
        private int pendingFocusSectionIndex = -1;
        private boolean isKeyboardOpen = false;
        private boolean pendingFocusLastItem = false;
        private int activeSectionIndex = 0;

        public SectionsAdapter(List<SectionModel> sections) {
            this.sections = sections;
        }
        
        public void setKeyboardOpen(boolean open) {
            if (this.isKeyboardOpen != open) {
                this.isKeyboardOpen = open;
                notifyItemChanged(sections.size()); // Resize footer
            }
        }
        
        public void setActiveSectionIndex(int index) {
            if (this.activeSectionIndex != index) {
                this.activeSectionIndex = index;
                notifyItemChanged(sections.size()); // Resize footer
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == sections.size()) return TYPE_FOOTER;
            return TYPE_SECTION;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_FOOTER) {
                View space = new View(activity);
                space.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                return new RecyclerView.ViewHolder(space) {};
            }
            
            View view = LayoutInflater.from(activity).inflate(R.layout.item_wizard_section, parent, false);
            return new SectionItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder baseHolder, int position) {
            int viewType = getItemViewType(position);
            if (viewType == TYPE_FOOTER) {
                int screenHeight = activity.getResources().getDisplayMetrics().heightPixels;
                int height = 0; // Invisible by default
                if (isKeyboardOpen && activeSectionIndex >= sections.size() - 3) {
                    height = (int)(screenHeight * 0.7); // Only show for bottom 3 sections
                }
                ViewGroup.LayoutParams lp = baseHolder.itemView.getLayoutParams();
                if (lp.height != height) {
                    lp.height = height;
                    baseHolder.itemView.setLayoutParams(lp);
                }
                return;
            }
            
            SectionItemViewHolder holder = (SectionItemViewHolder) baseHolder;
            SectionModel sm = sections.get(position);
            holder.tvTitle.setText(sm.name);
            
            com.google.android.material.card.MaterialCardView card = (com.google.android.material.card.MaterialCardView) holder.itemView;

            if (sm.isExpanded) {
                holder.llFieldContainer.setVisibility(View.VISIBLE);
                holder.ivChevron.setRotation(180f);
                card.setStrokeColor(activity.getColor(R.color.text_primary));
                card.setStrokeWidth(2);
                populateFields(holder.llFieldContainer, sm);
                holder.ivChevron.animate().rotation(180f).setDuration(400).start();

                if (position == pendingFocusSectionIndex) {
                    final boolean focusLast = pendingFocusLastItem;
                    pendingFocusLastItem = false;
                    holder.llFieldContainer.postDelayed(() -> {
                        android.widget.EditText targetEt = null;
                        if (focusLast) {
                            // Focus the first field of the LAST entry (newly added item)
                            int lastEntryStart = -1;
                            for (int k = holder.llFieldContainer.getChildCount() - 1; k >= 0; k--) {
                                View child = holder.llFieldContainer.getChildAt(k);
                                if (child instanceof TextView && ((TextView) child).getText().toString().startsWith("Entry #")) {
                                    lastEntryStart = k;
                                    break;
                                }
                            }
                            int searchFrom = (lastEntryStart >= 0) ? lastEntryStart : 0;
                            for (int k = searchFrom; k < holder.llFieldContainer.getChildCount(); k++) {
                                View child = holder.llFieldContainer.getChildAt(k);
                                if (child instanceof android.widget.EditText) {
                                    targetEt = (android.widget.EditText) child;
                                    break;
                                } else if (child instanceof LinearLayout) {
                                    LinearLayout row = (LinearLayout) child;
                                    for (int r = 0; r < row.getChildCount(); r++) {
                                        if (row.getChildAt(r) instanceof android.widget.EditText) {
                                            targetEt = (android.widget.EditText) row.getChildAt(r);
                                            break;
                                        }
                                    }
                                    if (targetEt != null) break;
                                }
                            }
                        } else {
                            for (int k = 0; k < holder.llFieldContainer.getChildCount(); k++) {
                                View child = holder.llFieldContainer.getChildAt(k);
                                if (child instanceof android.widget.EditText) {
                                    targetEt = (android.widget.EditText) child;
                                    break;
                                }
                            }
                        }
                        if (targetEt != null) {
                            targetEt.requestFocus();
                            targetEt.setSelection(targetEt.getText().length());
                            final View fv = targetEt;
                            fv.post(() -> fv.getParent().requestChildFocus(fv, fv));
                        }
                    }, 450);
                    pendingFocusSectionIndex = -1;
                }
            } else {
                holder.llFieldContainer.setVisibility(View.GONE);
                holder.ivChevron.animate().rotation(0f).setDuration(400).start();
                card.setStrokeColor(activity.getColor(R.color.divider_color));
                card.setStrokeWidth(1);
            }
            
            holder.clHeader.setOnClickListener(v -> {
                RecyclerView rv = (RecyclerView) holder.itemView.getParent();
                if (rv != null) {
                    androidx.transition.TransitionManager.beginDelayedTransition(rv, 
                        new androidx.transition.AutoTransition().setDuration(400));
                }
                
                boolean wasExpanded = sm.isExpanded;
                for (SectionModel s : sections) s.isExpanded = false;
                sm.isExpanded = !wasExpanded;
                if (sm.isExpanded) {
                    activity.myWebView.evaluateJavascript("if(window.autoFocusSection) window.autoFocusSection('" + sm.id + "');", null);
                }
                notifyDataSetChanged();
                activity.logToDebug("Wizard", "Toggled section: " + sm.id + " (Expanded: " + sm.isExpanded + ")");
            });
            
            holder.ivRemove.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                String sectionId = sm.id;
                sections.remove(pos);
                notifyItemRemoved(pos);
                notifyItemRangeChanged(pos, getItemCount());

                String js = String.format("window.removeSection('%s')", sectionId);
                activity.myWebView.evaluateJavascript(js, null);
                
                activity.logToDebug("Wizard", "Deleted section in sync: " + sectionId);
            });
        }

        private void populateFields(LinearLayout container, SectionModel sm) {
            container.removeAllViews();
            boolean isImageOnly = sm.id.equals("profileSection") || sm.id.equals("visualRegistry") || sm.id.equals("physicalProfile");
            if (isImageOnly && (sm.items == null || sm.items.isEmpty())) {
                if (sm.items == null) sm.items = new ArrayList<>();
                List<FieldModel> mockFields = new ArrayList<>();
                mockFields.add(new FieldModel("profile_pic", "Photo", "", "image", 0));
                sm.items.add(new ItemModel(mockFields));
            }
            if (sm.items == null || sm.items.isEmpty()) {
                activity.logToDebug("MSW-UI", "❌ Rendering empty section for: " + sm.id + " (items list is empty/null)");
                return;
            }
            activity.logToDebug("MSW-UI", "✅ Rendering section " + sm.id + " with " + sm.items.size() + " items");

            for (int itemIdx = 0; itemIdx < sm.items.size(); itemIdx++) {
                final int finalItemIdx = itemIdx;
                ItemModel item = sm.items.get(itemIdx);
                List<FieldModel> fields = item.fields;
                activity.logToDebug("MSW-UI", "  -> Item #" + itemIdx + " has " + (fields != null ? fields.size() : 0) + " fields");

                // Add Subsection Header if more than one item
                if (sm.items.size() > 1) {
                    TextView tvHeader = new TextView(container.getContext());
                    tvHeader.setText("Entry #" + (itemIdx + 1));
                    tvHeader.setTextSize(12);
                    tvHeader.setAllCaps(true);
                    tvHeader.setLetterSpacing(0.1f);
                    tvHeader.setTypeface(null, android.graphics.Typeface.BOLD);
                    tvHeader.setTextColor(activity.getColor(R.color.brand_primary));
                    tvHeader.setPadding(32, 32, 0, 12);
                    container.addView(tvHeader);
                    
                    // Add a tiny divider line
                    View divider = new View(container.getContext());
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(100, 4);
                    lp.setMargins(32, 0, 0, 8);
                    divider.setLayoutParams(lp);
                    divider.setBackgroundColor(activity.getColor(R.color.brand_primary));
                    container.addView(divider);
                }

                for (int i = 0; i < fields.size(); i++) {
                    FieldModel fm = fields.get(i);
                    
                    // Mirror-First: Strictly respect visibility from WebView
                    if (!fm.isVisible) {
                        continue;
                    }
                    
                    boolean isKVPair = (fm.key.equals("k") && (i + 1 < fields.size()) && fields.get(i + 1).key.equals("v"));
                    // A label is "locked" if it's a 'k' in a k/v pair and is NOT editable
                    boolean lockLabel = isKVPair && !fm.isEditable;
                    
                    // Only show horizontal pair if BOTH are visible and editable (or if it's a special type like lang)
                    boolean isHorizontalPair = (isKVPair && !lockLabel && fields.get(i+1).isVisible) || 
                                              (fm.key.equals("lang") && (i + 1 < fields.size()) && fields.get(i + 1).key.equals("level") && fields.get(i+1).isVisible);

                    if (isHorizontalPair) {
                        final FieldModel f1 = fm;
                        final FieldModel f2 = fields.get(i + 1);
                        
                        LinearLayout rowLayout = new LinearLayout(container.getContext());
                        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
                        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        rowLp.setMargins(16, 8, 16, 8);
                        rowLayout.setLayoutParams(rowLp);

                        final android.widget.EditText et1 = createStyledEdit(container.getContext(), f1.value, f1.label, sm, f1, finalItemIdx);
                        final android.widget.EditText et2 = createStyledEdit(container.getContext(), f2.value, f2.label, sm, f2, finalItemIdx);

                        // FIX: Force lang/level to always be editable
                        if (f1.key.equals("lang") || f1.key.equals("level")) {
                            et1.setFocusable(true);
                            et1.setFocusableInTouchMode(true);
                            et1.setLongClickable(true);
                        }
                        if (f2.key.equals("lang") || f2.key.equals("level")) {
                            et2.setFocusable(true);
                            et2.setFocusableInTouchMode(true);
                            et2.setLongClickable(true);
                        }

                        final LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
                        lp1.setMargins(0, 0, 12, 0);
                        et1.setLayoutParams(lp1);
                        et1.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_NEXT);
                        et1.setOnEditorActionListener((v, actionId, event) -> {
                            et2.requestFocus();
                            return true;
                        });

                        final LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
                        et2.setLayoutParams(lp2);

                        // Dynamic focus-grow effect
                        final android.view.View.OnFocusChangeListener focus1 = et1.getOnFocusChangeListener();
                        et1.setOnFocusChangeListener((v, hasFocus) -> {
                            if (focus1 != null) focus1.onFocusChange(v, hasFocus);
                            if (hasFocus) {
                                lp1.weight = 2.0f;
                                lp2.weight = 1.0f;
                            } else {
                                lp1.weight = 1.0f;
                            }
                            rowLayout.requestLayout();
                        });

                        final android.view.View.OnFocusChangeListener focus2 = et2.getOnFocusChangeListener();
                        et2.setOnFocusChangeListener((v, hasFocus) -> {
                            if (focus2 != null) focus2.onFocusChange(v, hasFocus);
                            if (hasFocus) {
                                lp2.weight = 2.0f;
                                lp1.weight = 1.0f;
                            } else {
                                lp2.weight = 1.0f;
                            }
                            rowLayout.requestLayout();
                        });

                        rowLayout.addView(et1);
                        rowLayout.addView(et2);
                        container.addView(rowLayout);
                        
                        i++; // Skip the second field as it's handled in the row
                        continue;
                    }

                    String visualLabel = fm.label; 
                    String initialValue = fm.value;
                    FieldModel targetField = fm; 

                    if (lockLabel) {
                        visualLabel = fm.value; 
                        targetField = fields.get(i + 1);
                        initialValue = targetField.value;
                        i++; 
                    }

                    // FIX: _lbl fields (exam_lbl, score_lbl, etc.) rendered as proper labels
                    // instead of disabled EditTexts — matching Personal Details style
                    boolean isLabeledPair = !lockLabel && fm.key.endsWith("_lbl") && (i + 1 < fields.size()) && fields.get(i + 1).isVisible;
                    if (isLabeledPair) {
                        visualLabel = fm.value;
                        targetField = fields.get(i + 1);
                        initialValue = targetField.value;
                        i++;
                    }

                    TextView tvLabel = new TextView(container.getContext());
                    tvLabel.setText(visualLabel);
                    tvLabel.setTextSize(13);
                    tvLabel.setTypeface(null, android.graphics.Typeface.BOLD);
                    tvLabel.setTextColor(activity.getColor(R.color.text_secondary));
                    tvLabel.setPadding(8, 16, 0, 8);
                    container.addView(tvLabel);

                    boolean isImageField = false;
                    String lKey = targetField.key.toLowerCase();
                    if (targetField.type.equalsIgnoreCase("image")) isImageField = true;
                    if (lKey.contains("profile_pic") || lKey.contains("photography") || lKey.contains("picture") || lKey.contains("photo") || lKey.contains("avatar")) isImageField = true;
                    if ((sm.id.equals("profileSection") || sm.id.equals("visualRegistry") || sm.id.equals("physicalProfile")) && 
                        (lKey.equals("url") || lKey.equals("src") || lKey.equals("image") || lKey.equals("file") || lKey.equals("data") || lKey.equals("text"))) {
                        isImageField = true;
                    }
                    if (initialValue != null && initialValue.startsWith("data:image")) {
                        isImageField = true;
                    }

                    if (isImageField) {
                        tvLabel.setVisibility(View.GONE); // Hide the generic label since the button makes it obvious
                        
                        LinearLayout imgContainer = new LinearLayout(container.getContext());
                        imgContainer.setOrientation(LinearLayout.VERTICAL);
                        imgContainer.setGravity(android.view.Gravity.CENTER);
                        imgContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        imgContainer.setPadding(0, 32, 0, 48);

                        ImageView ivPreview = new ImageView(container.getContext());
                        int size = (int)(90 * activity.getResources().getDisplayMetrics().density);
                        LinearLayout.LayoutParams imgLp = new LinearLayout.LayoutParams(size, size);
                        imgLp.setMargins(0, 0, 0, 16);
                        ivPreview.setLayoutParams(imgLp);
                        ivPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        
                        boolean hasImage = false;
                        if (initialValue != null && initialValue.startsWith("data:image")) {
                            try {
                                String base64Image = initialValue.split(",")[1];
                                byte[] decodedString = android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT);
                                android.graphics.Bitmap decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                ivPreview.setImageBitmap(decodedByte);
                                hasImage = true;
                            } catch(Exception e) { 
                                // Ignore
                            }
                        }
                        
                        if (!hasImage) {
                            ivPreview.setImageResource(android.R.drawable.ic_menu_camera); 
                            ivPreview.setImageTintList(android.content.res.ColorStateList.valueOf(activity.getColor(R.color.brand_primary)));
                            int padding = (int)(24 * activity.getResources().getDisplayMetrics().density);
                            ivPreview.setPadding(padding, padding, padding, padding);
                        }

                        android.graphics.drawable.GradientDrawable bgImg = new android.graphics.drawable.GradientDrawable();
                        bgImg.setShape(android.graphics.drawable.GradientDrawable.OVAL);
                        bgImg.setStroke(4, activity.getColor(R.color.brand_primary));
                        bgImg.setColor(activity.getColor(R.color.bg_card));
                        if (!hasImage) {
                            bgImg.setColor(0xFFF1F5F9); // Light gray placeholder background
                        }
                        ivPreview.setBackground(bgImg);
                        ivPreview.setClipToOutline(true);

                        Button btnPick = new Button(container.getContext());
                        btnPick.setText(hasImage ? "Change Image" : "Select Image");
                        btnPick.setAllCaps(false);
                        btnPick.setTextSize(13);
                        btnPick.setTextColor(activity.getColor(R.color.white));
                        btnPick.setBackgroundTintList(android.content.res.ColorStateList.valueOf(activity.getColor(R.color.brand_primary)));
                        btnPick.setPadding(48, 0, 48, 0);
                        btnPick.setTag("btn_pick_" + sm.id);
                        
                        android.graphics.drawable.GradientDrawable btnBg = new android.graphics.drawable.GradientDrawable();
                        btnBg.setCornerRadius(30f);
                        btnPick.setBackground(btnBg);
                        
                        imgContainer.setTag("img_container_" + sm.id);
                        ivPreview.setTag("img_preview_" + sm.id);

                        final String finalFieldKey = targetField.key;
                        View.OnClickListener pickListener = v -> {
                            activity.pickSectionImage(sm.id, finalFieldKey, finalItemIdx);
                        };
                        ivPreview.setOnClickListener(pickListener);
                        btnPick.setOnClickListener(pickListener);

                        imgContainer.addView(ivPreview);
                        imgContainer.addView(btnPick);
                        container.addView(imgContainer);
                        
                        continue;
                    }

                    if (fm.type.equalsIgnoreCase("url")) {
                        LinearLayout linkContainer = new LinearLayout(container.getContext());
                        linkContainer.setOrientation(LinearLayout.VERTICAL);
                        linkContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        linkContainer.setPadding(0, 8, 0, 8);
                        
                        android.widget.EditText etText = createStyledEdit(container.getContext(), initialValue, "Display Text (e.g. Portfolio)", sm, targetField, finalItemIdx);
                        linkContainer.addView(etText);
                        
                        TextView tvExpand = new TextView(container.getContext());
                        tvExpand.setText("+ Set Link URL");
                        tvExpand.setTextSize(11);
                        tvExpand.setPadding(32, 0, 0, 16);
                        tvExpand.setTextColor(activity.getColor(R.color.brand_primary));
                        tvExpand.setPaintFlags(tvExpand.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
                        linkContainer.addView(tvExpand);
                        
                        android.widget.EditText etUrl = createStyledEdit(container.getContext(), fm.url != null ? fm.url : "", "Link URL (https://...)", sm, fm, finalItemIdx);
                        etUrl.setTag(i);
                        etUrl.setVisibility(View.GONE);
                        etUrl.setTextSize(13);
                        linkContainer.addView(etUrl);
                        
                        tvExpand.setOnClickListener(v -> {
                            android.transition.TransitionManager.beginDelayedTransition((ViewGroup) activity.findViewById(R.id.wizard_cardSteps));
                            etUrl.setVisibility(View.VISIBLE);
                            tvExpand.setVisibility(View.GONE);
                            etUrl.requestFocus();
                        });
                        
                        if (fm.url != null && !fm.url.isEmpty()) {
                            etUrl.setVisibility(View.VISIBLE);
                            tvExpand.setVisibility(View.GONE);
                        }
                        
                        container.addView(linkContainer);
                        continue;
                    }

                    android.widget.EditText et = createStyledEdit(container.getContext(), initialValue, "", sm, targetField, finalItemIdx);
                    et.setTag(i); // Tag for auto-scroll
                    container.addView(et);
                }
            }

            // Mirror-First: Add "ADD ITEM" button for dynamic sections
            boolean isSingleton = sm.id.equals("summarySection") || sm.id.equals("nameProfessionSection") || sm.id.equals("profileSection") || sm.id.equals("headerBoxSection") || sm.id.equals("declarationSection");
            if (!isSingleton) {
                android.widget.Button btnAddItem = new android.widget.Button(container.getContext());
                btnAddItem.setText("+ ADD ITEM");
                btnAddItem.setTextColor(activity.getColor(R.color.brand_primary));
                btnAddItem.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT));
                btnAddItem.setAllCaps(true);
                btnAddItem.setTextSize(14);
                btnAddItem.setTypeface(null, android.graphics.Typeface.BOLD);
                LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                btnLp.gravity = android.view.Gravity.CENTER_HORIZONTAL;
                btnLp.setMargins(0, 32, 0, 32);
                btnAddItem.setLayoutParams(btnLp);
                btnAddItem.setOnClickListener(v -> {
                    pendingAddItemSectionId = sm.id;
                    activity.myWebView.evaluateJavascript("if(window.androidAddItem) window.androidAddItem('" + sm.id + "');", null);
                    v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
                    // refreshWizard will be called from JS side via refreshWizard interface
                });
                container.addView(btnAddItem);
            }
        }

        private android.widget.EditText createStyledEdit(android.content.Context context, String value, String hint, SectionModel sm, FieldModel fm, int itemIndex) {
            android.widget.EditText et = new android.widget.EditText(context);
            et.setText(value);
            et.setHint(hint);
            et.setTextSize(15);
            et.setTextColor(activity.getColor(R.color.text_primary));
            et.setPadding(32, 28, 32, 28);
            et.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_NEXT);
            et.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

            et.setFocusable(fm.isEditable);
            et.setFocusableInTouchMode(fm.isEditable);
            et.setLongClickable(fm.isEditable);
            
            android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
            bg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            bg.setCornerRadius(16f);
            bg.setStroke(2, activity.getColor(R.color.divider_color));
            
            if (!fm.isEditable) {
                et.setTextColor(activity.getColor(R.color.text_secondary));
                bg.setColor(activity.getColor(R.color.bg_primary)); // Subtly different color for read-only
                et.setAlpha(0.85f);
            } else {
                bg.setColor(activity.getColor(R.color.bg_card));
            }
            et.setBackground(bg);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            lp.setMargins(0, 4, 0, 8);
            et.setLayoutParams(lp);

            // "Precision Center" Focus Engine
            et.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    activity.logToDebug("PD-TRACK", "🔦 FOCUS | sec: " + sm.id + " | key: " + fm.key + " | fmIdx: " + fm.index + " | itemIdx: " + itemIndex + " | editable: " + fm.isEditable + " | val: \"" + ((fm.value != null && fm.value.length() > 30) ? fm.value.substring(0, 30) : fm.value) + "\"");
                    if (sections != null) setActiveSectionIndex(sections.indexOf(sm));
                    
                    // 1. Center the section
                    activity.myWebView.evaluateJavascript("if(window.autoFocusSection) window.autoFocusSection('" + sm.id + "');", null);
                    
                    // 2. Highlighting: Tell WebView to highlight this specific field
                    String highlightJs = String.format("if(window.highlightField) window.highlightField('%s', %d, %d, '%s');", sm.id, fm.index, itemIndex, fm.key);
                    activity.logToDebug("PD-TRACK", "📤 highlightField JS: " + highlightJs);
                    activity.myWebView.evaluateJavascript(highlightJs, null);

                    v.postDelayed(() -> {
                        checkAndCorrectFocus();
                        if (v instanceof android.widget.EditText) {
                            android.widget.EditText cet = (android.widget.EditText) v;
                            cet.setSelection(cet.getText().length());
                        }
                    }, 500); // Wait for expansion to fully settle 
                }
            });

            // "Live Bridge" Sync Engine
            et.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    fm.value = s.toString();
                    if (fm.type != null && fm.type.equalsIgnoreCase("url")) {
                        // For links, we need to pass both text and url (handled by finding the sibling et in linkContainer if needed)
                        // Actually, it's safer to just trigger updateResumeField for the text and handle URL separately
                        // But wait! Link fields usually have a dedicated watcher. 
                        // To keep it simple, we use updateResumeField which handles text.
                    }
                    String js = String.format("window.updateResumeField('%s', '%s', '%s', %d, %d)",
                                sm.id, fm.key, fm.value.replace("'", "\\'").replace("\n", "\\n"), fm.index, itemIndex);
                    activity.logToDebug("PD-TRACK", "✏️ WRITE | sec: " + sm.id + " | key: " + fm.key + " | fmIdx: " + fm.index + " | itemIdx: " + itemIndex + " | val: \"" + (fm.value.length() > 30 ? fm.value.substring(0, 30) : fm.value) + "\"");
                    activity.myWebView.evaluateJavascript(js, null);
                }
            });

            // Action: Next behavior
            et.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_NEXT) {
                    View next = v.focusSearch(View.FOCUS_DOWN);
                    if (next != null && next instanceof android.widget.EditText) {
                        next.requestFocus();
                        return true;
                    } else {
                        // We reached the end of the section's fields.
                        // On the sections step (step 2), expand the NEXT section instead of closing.
                        if (viewPager != null && viewPager.getCurrentItem() == 2 && sections != null && sectionsAdapter != null) {
                            int currentIdx = sections.indexOf(sm);
                            if (currentIdx >= 0 && currentIdx + 1 < sections.size()) {
                                // Collapse current, expand next
                                RecyclerView rv = activity.findViewById(R.id.rvSections);
                                if (rv != null) {
                                    androidx.transition.TransitionManager.beginDelayedTransition(rv,
                                        new androidx.transition.AutoTransition().setDuration(400));
                                }
                                for (SectionModel s : sections) s.isExpanded = false;
                                SectionModel nextSec = sections.get(currentIdx + 1);
                                nextSec.isExpanded = true;
                                sectionsAdapter.pendingFocusSectionIndex = currentIdx + 1;
                                sectionsAdapter.notifyDataSetChanged();

                                // Scroll to the next section
                                if (rv != null) {
                                    rv.post(() -> rv.smoothScrollToPosition(currentIdx + 1));
                                }

                                activity.myWebView.evaluateJavascript("if(window.autoFocusSection) window.autoFocusSection('" + nextSec.id + "');", null);
                                activity.logToDebug("PD-TRACK", "⏭️ IME_NEXT: Jumped to next section: " + nextSec.id);
                                return true;
                            }
                        }
                        // Fallback: only trigger btnNext if not on sections step or no next section
                        if (btnNext != null && btnNext.getVisibility() == View.VISIBLE && btnNext.isEnabled()) {
                            btnNext.performClick();
                            return true;
                        }
                    }
                }
                return false;
            });

            if (fm.key.equalsIgnoreCase("summary") || 
                fm.key.equalsIgnoreCase("description") || 
                fm.key.equalsIgnoreCase("content") ||
                fm.key.equalsIgnoreCase("desc") ||
                fm.key.equalsIgnoreCase("body")) {
                et.setMinLines(3);
                et.setSingleLine(false);
                et.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_NONE); 
            } else {
                et.setSingleLine(true);
            }

            return et;
        }

        @Override
        public int getItemCount() {
            return sections.size() + 1; // + Footer
        }

        private int getIconResource(String faIcon) {
            if (faIcon == null) return R.drawable.ic_summary;
            String icon = faIcon.toLowerCase();
            if (icon.contains("user-tie") || icon.contains("user-circle")) return R.drawable.ic_summary;
            if (icon.contains("id-card") || icon.contains("id-badge")) return R.drawable.ic_personal;
            if (icon.contains("passport")) return R.drawable.ic_passport;
            if (icon.contains("language")) return R.drawable.ic_languages;
            if (icon.contains("certificate") || icon.contains("trophy") || icon.contains("star")) return R.drawable.ic_certificates;
            if (icon.contains("file-signature")) return R.drawable.ic_declaration;
            if (icon.contains("plus-circle")) return R.drawable.ic_summary; // Fallback to summary for generic "plus"
            return R.drawable.ic_summary; // Fallback
        }

        class SectionItemViewHolder extends RecyclerView.ViewHolder {
            ImageView ivRemove, ivChevron;
            TextView tvTitle;
            View clHeader;
            LinearLayout llFieldContainer;

            public SectionItemViewHolder(View v) {
                super(v);
                ivRemove = v.findViewById(R.id.ivRemove);
                ivChevron = v.findViewById(R.id.ivChevron);
                tvTitle = v.findViewById(R.id.tvSectionTitle);
                clHeader = v.findViewById(R.id.clSectionHeader);
                llFieldContainer = v.findViewById(R.id.llFieldContainer);
            }
        }
    }

    private class PurposeViewHolder extends RecyclerView.ViewHolder {
        RecyclerView rv;
        public PurposeViewHolder(View itemView) { 
            super(itemView);
            rv = itemView.findViewById(R.id.rvPurposes);
            if (rv != null) {
                rv.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(activity, 2));
                rv.setAdapter(new PurposeAdapter());
            }
        }
    }
    private class TemplateViewHolder extends RecyclerView.ViewHolder {
        RecyclerView rv;
        TextView tvNotice;

        public TemplateViewHolder(View itemView) { 
            super(itemView);
            rv = itemView.findViewById(R.id.rvTemplates);
            tvNotice = itemView.findViewById(R.id.tvTemplateNotice);
            
            if (tvNotice != null) {
                tvNotice.setText(androidx.core.text.HtmlCompat.fromHtml(activity.getString(R.string.template_notice), androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT));
            }
            if (rv != null) {
                rv.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(activity, 2));
                rv.setAdapter(new TemplateAdapter());
            }
        }
    }
    private class SectionsViewHolder extends RecyclerView.ViewHolder {
        RecyclerView rv;
        public SectionsViewHolder(View itemView) { 
            super(itemView);
            rv = itemView.findViewById(R.id.rvSections);
            if (rv != null) {
                rv.setLayoutManager(new LinearLayoutManager(activity));
                if (sectionsAdapter == null) {
                    sectionsAdapter = new SectionsAdapter(currentSections);
                }
                rv.setAdapter(sectionsAdapter);
            }
        }
    }

    private void setupResizer() {
        if (dragHandle == null || cardSteps == null) return;
        dragHandle.setOnTouchListener(new View.OnTouchListener() {
            private float initialY;
            private int initialHeight;

            @Override
            public boolean onTouch(View v, android.view.MotionEvent event) {
                switch (event.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        initialY = event.getRawY();
                        initialHeight = cardSteps.getHeight();
                        return true;
                    case android.view.MotionEvent.ACTION_MOVE:
                        float deltaY = initialY - event.getRawY();
                        int newHeight = (int) (initialHeight + deltaY);
                        
                        // Limits: Min 20% of screen, Max 90%
                        int screenHeight = activity.getResources().getDisplayMetrics().heightPixels;
                        if (newHeight > screenHeight * 0.2 && newHeight < screenHeight * 0.9) {
                            cardSteps.getLayoutParams().height = newHeight;
                            cardSteps.requestLayout();
                        }
                        return true;
                }
                return false;
            }
        });
    }

    public void notifyImageUpdated(String sectionId, String base64Image) {
        if (wizardPanel == null) return;
        String targetTag = "img_preview_" + sectionId;
        ImageView ivPreview = wizardPanel.findViewWithTag(targetTag);
        if (ivPreview != null && base64Image != null) {
            try {
                String base64Data = base64Image;
                if (base64Image.startsWith("data:image")) {
                    base64Data = base64Image.split(",")[1];
                }
                byte[] decodedString = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT);
                android.graphics.Bitmap decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                
                // Update preview and UI
                ivPreview.setImageBitmap(decodedByte);
                ivPreview.setPadding(0, 0, 0, 0);
                
                // Force clipping for polygon/oval shapes
                ivPreview.setClipToOutline(true);
                
                if (ivPreview.getBackground() instanceof android.graphics.drawable.GradientDrawable) {
                    android.graphics.drawable.GradientDrawable bgImg = (android.graphics.drawable.GradientDrawable) ivPreview.getBackground();
                    if (bgImg != null) bgImg.setColor(activity.getColor(R.color.bg_card));
                }
                
                Button btnPick = wizardPanel.findViewWithTag("btn_pick_" + sectionId);
                if (btnPick != null) {
                    btnPick.setText("Change Image");
                }
                
            } catch(Exception e) {
                Log.e("CVWizardManager", "Failed to update internal preview", e);
            }
        }
    }

    /**
     * Synchronizes the internal 'currentSections' list with the current state of the WebView DOM.
     * This ensures bi-directional updates between the editor's side panel and the Wizard.
     */
    /**
     * Shows a dialog with all available sections so the user can add more from Step 3.
     */
    private void showAddSectionDialog() {
        if (activity == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.CustomAlertDialog);
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_add_sections_wizard, null);
        builder.setView(dialogView);

        RecyclerView rv = dialogView.findViewById(R.id.rvAddSectionsWizard);
        rv.setLayoutManager(new LinearLayoutManager(activity));

        AlertDialog dialog = builder.create();

        // Simple adapter for the dialog
        addSectionDialogAdapter = new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_add_section_dialog, parent, false);
                return new RecyclerView.ViewHolder(v) {};
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                SectionModel sm = ALL_SECTIONS.get(position);
                TextView tvName = holder.itemView.findViewById(R.id.tvSectionName);
                TextView tvDesc = holder.itemView.findViewById(R.id.tvSectionDesc);
                android.widget.ImageView ivIcon = holder.itemView.findViewById(R.id.ivSectionIcon);
                View btnAdd = holder.itemView.findViewById(R.id.btnDialogAdd);
                
                tvName.setText(sm.name);
                tvDesc.setText(sm.desc != null ? sm.desc : "Add this section to your CV");
                ivIcon.setImageResource(activity.getIconResForFontAwesome(sm.icon));

                boolean exists = false;
                for (SectionModel cur : currentSections) {
                    if (cur.id.equals(sm.id)) {
                        exists = true;
                        break;
                    }
                }

                if ("headerBoxSection".equals(sm.id)) {
                    btnAdd.setEnabled(true);
                    btnAdd.setAlpha(1.0f);
                    if (exists) {
                        ((Button)btnAdd).setText("Remove -");
                        ((Button)btnAdd).setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E74C3C"))); // Reddish
                    } else {
                        ((Button)btnAdd).setText("Add +");
                        ((Button)btnAdd).setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#27AE60"))); // Greenish
                    }
                    
                    final boolean finalExists = exists;
                    btnAdd.setOnClickListener(v -> {
                        if (activity != null) activity.logToDebug("HeaderToggle", "Wizard: Button clicked! Section ID: '" + sm.id + "', Name: '" + sm.name + "', exists: " + finalExists);
                        // Header Box needs SPECIAL handling via addSectionFromPanel to maintain structure
                        String js = "addSectionFromPanel({ id: 'headerBoxSection', name: 'Header Box', icon: 'fa-heading', type: 'header_box', col: 'header', group: 'gridEssentials', desc: 'Toggle the main header on/off' });";
                        activity.myWebView.evaluateJavascript(js, null);
                        v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
                    });
                } else if (exists && !sm.id.equals("blankSection") && !sm.id.equals("stickSection")) {
                    btnAdd.setEnabled(true);
                    btnAdd.setAlpha(1.0f);
                    ((Button)btnAdd).setText("Remove -");
                    ((Button)btnAdd).setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E74C3C"))); // Reddish
                    btnAdd.setOnClickListener(v -> {
                        if (activity != null) activity.logToDebug("HeaderToggle", "Wizard: Remove clicked! Section ID: '" + sm.id + "'");
                        // Pass to JS to REMOVE it from the DOM
                        String js = String.format("window.removeSection('%s')", sm.id);
                        activity.myWebView.evaluateJavascript(js, null);
                        
                        v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
                        // The dialog adapter will refresh automatically via syncSectionsWithWebView/onSectionAdded
                    });
                } else {
                    btnAdd.setEnabled(true);
                    btnAdd.setAlpha(1.0f);
                    ((Button)btnAdd).setText("Add +");
                    ((Button)btnAdd).setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#27AE60"))); // Greenish
                    btnAdd.setOnClickListener(v -> {
                        if (activity != null) activity.logToDebug("HeaderToggle", "Wizard: Add clicked! Section ID: '" + sm.id + "'");
                        // Pass to JS to ADD it to the DOM
                        String col = sm.column != null ? sm.column : "auto";
                        String js = String.format("window.androidAddSection('%s', '%s')", sm.id, col);
                        activity.myWebView.evaluateJavascript(js, null);
                        
                        v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
                        // The dialog adapter will refresh automatically via syncSectionsWithWebView/onSectionAdded
                    });
                }
            }

            @Override
            public int getItemCount() {
                return ALL_SECTIONS.size();
            }
        };
        rv.setAdapter(addSectionDialogAdapter);

        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setOnDismissListener(d -> addSectionDialogAdapter = null);
        dialog.show();
    }

    public void syncSectionsWithWebView() {
        if (activity == null || activity.myWebView == null) return;

        activity.myWebView.evaluateJavascript("window.getCurrentSectionIds()", jsonIds -> {
            if (jsonIds == null || jsonIds.equals("null") || jsonIds.isEmpty()) return;

            try {
                String cleanJson = jsonIds;
                if (cleanJson.startsWith("\"") && cleanJson.endsWith("\"")) {
                    cleanJson = cleanJson.substring(1, cleanJson.length() - 1);
                    cleanJson = cleanJson.replace("\\\"", "\"");
                }
                org.json.JSONArray idArray = new org.json.JSONArray(cleanJson);
                java.util.List<String> activeIds = new java.util.ArrayList<>();
                for (int i = 0; i < idArray.length(); i++) {
                    String id = idArray.getString(i);
                    if (!activeIds.contains(id)) activeIds.add(id);
                }

                // Match them back to our master ALL_SECTIONS to update currentSections
                List<SectionModel> updatedList = new ArrayList<>();
                for (String id : activeIds) {
                    for (SectionModel sm : ALL_SECTIONS) {
                        if (sm.id.equals(id)) {
                            // Reuse existing model if present to keep data/expansion state
                            SectionModel existingRec = null;
                            for (SectionModel cur : currentSections) {
                                if (cur.id.equals(id)) {
                                    existingRec = cur;
                                    break;
                                }
                            }
                            updatedList.add(existingRec != null ? existingRec : sm);
                            break;
                        }
                    }
                }

                activity.runOnUiThread(() -> {
                    currentSections.clear();
                    currentSections.addAll(updatedList);
                    if (sectionsAdapter != null) {
                        sectionsAdapter.notifyDataSetChanged();
                    }
                    
                    // REFRESH the dialog list if it's currently showing
                    if (addSectionDialogAdapter != null) {
                        addSectionDialogAdapter.notifyDataSetChanged();
                    }
                    activity.logToDebug("WizardSync", "Synced " + currentSections.size() + " sections from WebView");
                });

            } catch (org.json.JSONException e) {
                activity.logToDebug("WizardSync", "JSON Error: " + e.getMessage());
            }
        });
    }
    public void updateFieldFromWebView(String sectionId, String fieldKey, String value, int fieldIndex) {
        if (activity == null || activity.myWebView == null || currentSections == null) return;

        // 1. Find the SectionModel in MSW
        SectionModel targetSection = null;
        for (SectionModel sm : currentSections) {
            if (sm.id.equals(sectionId)) {
                targetSection = sm;
                break;
            }
        }
        if (targetSection == null) return;

        // 2. Find and Update the FieldModel value in MSW state
        int globalIdx = 0;
        FieldModel targetField = null;
        if (targetSection.items != null) {
            for (ItemModel item : targetSection.items) {
                if (item.fields != null) {
                    for (FieldModel fm : item.fields) {
                        if (globalIdx == fieldIndex) {
                            targetField = fm;
                            fm.value = value;
                            activity.logToDebug("MSW_Sync", "Updated MSW State: " + sectionId + " [" + fieldKey + "] = " + value);
                            break;
                        }
                        globalIdx++;
                    }
                }
                if (targetField != null) break;
            }
        }

        // 3. Find the EditText in the MSW UI and update it
        if (targetSection.isExpanded && sectionsAdapter != null) {
            RecyclerView rv = activity.findViewById(R.id.rvSections);
            if (rv != null) {
                for (int i = 0; i < rv.getChildCount(); i++) {
                    View sectionView = rv.getChildAt(i);
                    RecyclerView.ViewHolder holder = rv.getChildViewHolder(sectionView);
                    if (holder instanceof SectionsAdapter.SectionItemViewHolder) {
                        SectionsAdapter.SectionItemViewHolder sHolder = (SectionsAdapter.SectionItemViewHolder) holder;
                        int pos = sHolder.getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION && pos < currentSections.size() && currentSections.get(pos).id.equals(sectionId)) {
                            LinearLayout container = sHolder.llFieldContainer;
                            int etIdx = 0;
                            for (int k = 0; k < container.getChildCount(); k++) {
                                View child = container.getChildAt(k);
                                if (child instanceof android.widget.EditText) {
                                    if (etIdx == fieldIndex) {
                                        android.widget.EditText et = (android.widget.EditText) child;
                                        if (!et.getText().toString().equals(value) && !et.hasFocus()) {
                                            et.setText(value);
                                        }
                                        return;
                                    }
                                    etIdx++;
                                } else if (child instanceof ViewGroup) {
                                    ViewGroup vg = (ViewGroup) child;
                                    for (int m = 0; m < vg.getChildCount(); m++) {
                                        View innerChild = vg.getChildAt(m);
                                        if (innerChild instanceof android.widget.EditText) {
                                            if (etIdx == fieldIndex) {
                                                android.widget.EditText et = (android.widget.EditText) innerChild;
                                                if (!et.getText().toString().equals(value) && !et.hasFocus()) {
                                                    et.setText(value);
                                                }
                                                return;
                                            }
                                            etIdx++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
