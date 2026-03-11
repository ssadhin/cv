package com.example.myapplication;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ResumeDataManager {

    public static final String[] TEMPLATE_IDS = {"default", "timeline", "classic", "bento"};
    public static final String[] TEMPLATE_NAMES = {"Standard Modern", "Timeline", "Classic", "Bento Grid"};

    public static class FieldModel {
        String key;
        String label;
        String value = "";
        String type = "text";

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

    public static class ItemModel {
        List<FieldModel> fields = new ArrayList<>();
        public ItemModel(List<FieldModel> fields) {
            this.fields = fields;
        }
    }

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
            this.items.add(createDefaultItem(type));
        }

        public void addItem() {
            this.items.add(createDefaultItem(this.type));
        }
    }

    public static ItemModel createDefaultItem(String type) {
        List<FieldModel> fields = new ArrayList<>();
        switch (type) {
            case "header":
                fields.add(new FieldModel("name", "Full Name", "Your Name", "text"));
                fields.add(new FieldModel("email", "Email", "email@example.com", "text"));
                fields.add(new FieldModel("phone", "Phone", "+1 234 567 890", "text"));
                fields.add(new FieldModel("addr", "Address", "City, Country", "text"));
                fields.add(new FieldModel("linkedin", "LinkedIn", "linkedin.com/in/...", "text"));
                fields.add(new FieldModel("github", "GitHub", "github.com/...", "text"));
                fields.add(new FieldModel("portfolio", "Portfolio", "...", "text"));
                fields.add(new FieldModel("facebook", "Facebook", "facebook.com/...", "text"));
                fields.add(new FieldModel("web", "Website/Link", "http://...", "text"));
                break;
            case "personal":
                fields.add(new FieldModel("nationality", "Nationality", "American", "text"));
                fields.add(new FieldModel("dob", "Date of Birth", "15 January 1990", "text"));
                fields.add(new FieldModel("gender", "Gender", "Male", "text"));
                fields.add(new FieldModel("ms", "Marital Status", "Single", "text"));
                fields.add(new FieldModel("height", "Height", "N/A", "text"));
                break;
            case "passport":
                fields.add(new FieldModel("pno", "Passport No", "P0000000", "text"));
                fields.add(new FieldModel("issued", "Issued By", "Place of Issue", "text"));
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
                fields.add(new FieldModel("headshot", "Headshot Link/Desc", "Close-up portrait", "text"));
                fields.add(new FieldModel("fullbody", "Full Body Link/Desc", "Standing photo", "text"));
                fields.add(new FieldModel("family", "Family Photo Link/Desc", "With parents/siblings", "text"));
                break;
            default:
                fields.add(new FieldModel("val", "Content", "New Item", "text"));
        }
        return new ItemModel(fields);
    }

    public static final List<SectionModel> ALL_SECTIONS_TEMPLATE = Arrays.asList(
        new SectionModel("nameProfessionSection", "Name & Profession", "fa-id-badge", "name_profession", "gridEssentials"),
        new SectionModel("personalDetails", "Personal Details", "fa-id-card", "personal", "gridEssentials"),
        new SectionModel("passportDetails", "Passport Details", "fa-passport", "passport", "gridEssentials"),
        new SectionModel("summarySection", "Summary", "fa-user-tie", "summary_paragraph", "gridEssentials"),
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

    public static List<SectionModel> parseStructuredJson(String json) {
        List<SectionModel> sections = new ArrayList<>();
        try {
            String sanitized = sanitizeJson(json);
            JSONObject data = new JSONObject(sanitized);
            if (data.has("sections")) {
                JSONArray sectionsArray = data.getJSONArray("sections");
                for (int i = 0; i < sectionsArray.length(); i++) {
                    JSONObject sLoopObj = sectionsArray.getJSONObject(i);
                    String sId = sLoopObj.getString("id");
                    String sType = sLoopObj.getString("type");
                    String sName = sLoopObj.getString("name");
                    
                    SectionModel sectionTemplate = null;
                    for (SectionModel all : ALL_SECTIONS_TEMPLATE) {
                        if (all.id.equals(sId)) { sectionTemplate = all; break; }
                    }
                    if (sectionTemplate == null) {
                        for (SectionModel all : ALL_SECTIONS_TEMPLATE) {
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
                    sections.add(section);
                }
            } else {
                // Parse Shorthand AI Prompt Format
                sections = parseShorthandJson(data);
            }
        } catch (Exception e) {
            Log.e("ResumeDataManager", "Error parsing structured JSON", e);
        }
        return sections;
    }

    private static final java.util.Map<String, String[]> SHORTHAND_MAP = new java.util.HashMap<String, String[]>() {{
        // key -> [sectionId, sectionName, type]
        put("hdr", new String[]{"nameProfessionSection", "Name & Profession", "name_profession"});
        put("sum", new String[]{"summarySection", "Summary", "summary_paragraph"});
        put("per", new String[]{"personalDetails", "Personal Details", "personal"});
        put("pass", new String[]{"passportDetails", "Passport Details", "passport"});
        put("exp", new String[]{"experience", "Work Experience", "experience"});
        put("edu", new String[]{"education", "Education", "education"});
        put("skl", new String[]{"skills", "Skills", "skills"});
        put("pro", new String[]{"projects", "Projects", "projects"});
        put("cert", new String[]{"certifications", "Certifications", "certifications"});
        put("crt", new String[]{"certificates", "Certificates", "simple-list"});
        put("awd", new String[]{"awards", "Awards", "awards"});
        put("ach", new String[]{"awards", "Achievements", "awards"});
        put("lan", new String[]{"languages", "Languages", "languages"});
        put("ref", new String[]{"references", "References", "references"});
        put("vol", new String[]{"volunteer", "Volunteer", "volunteer"});
        put("pub", new String[]{"publications", "Publications", "publications"});
        put("res", new String[]{"researchExp", "Research Experience", "research"});
        put("tea", new String[]{"teachingExp", "Teaching Experience", "teaching"});
        put("gra", new String[]{"grants", "Grants & Funding", "grants"});
        put("aff", new String[]{"affiliations", "Affiliations", "affiliations"});
        put("trn", new String[]{"training", "Training", "training"});
        put("int", new String[]{"internships", "Internships", "internships"});
        put("hob", new String[]{"hobbies", "Hobbies", "hobbies"});
        put("ext", new String[]{"extra", "Extracurricular", "extra"});
        put("web", new String[]{"weblinks", "Web Links", "weblinks"});
        put("dec", new String[]{"declarationSection", "Declaration", "declaration_block"});
        put("vis", new String[]{"visaStatus", "Visa Status", "visa"});
        put("tst", new String[]{"testScores", "Test Scores", "test_scores"});
        put("fam", new String[]{"familyDetails", "Family Details", "family"});
        put("pex", new String[]{"partnerExpectations", "Partner Expectations", "expectations"});
        put("lst", new String[]{"lifestyleHabits", "Lifestyle", "lifestyle"});
        put("ast", new String[]{"astrologySection", "Astrological Details", "astrological"});
        put("phy", new String[]{"physicalProfile", "Physical Profile", "physical"});
    }};

    // Contact fields that should appear in header, not personal details
    private static final List<String> CONTACT_FIELDS = Arrays.asList(
        "email", "phone", "addr", "address", "link", "linkedin", "website", "url", "mobile", "tel",
        "portfolio", "github", "medium", "web",
        "facebook", "twitter", "instagram", "discord", "reddit", "quora"
    );

    private static List<SectionModel> parseShorthandJson(JSONObject data) {
        java.util.Map<String, SectionModel> sectionMap = new java.util.LinkedHashMap<>();
        
        // First pass: recovery and contact filtering
        try {
            if (data.has("per")) {
                JSONObject per = data.optJSONObject("per");
                if (per != null) {
                    JSONObject hdr = data.optJSONObject("hdr");
                    if (hdr == null) {
                        hdr = new JSONObject();
                        data.put("hdr", hdr);
                    }
                    
                    java.util.Iterator<String> perKeys = per.keys();
                    List<String> keysToMove = new ArrayList<>();
                    while (perKeys.hasNext()) {
                        String key = perKeys.next();
                        String valStr = per.optString(key, "");
                        
                        boolean isContactVal = isEmail(valStr) || isPhone(valStr);
                        boolean isContactKey = CONTACT_FIELDS.contains(key.toLowerCase());
                        
                        if (isContactKey || isContactVal) {
                            if (!hdr.has(key)) {
                                hdr.put(key, per.get(key));
                            }
                            keysToMove.add(key);
                        }
                    }
                    for (String k : keysToMove) per.remove(k);
                }
            }
        } catch (Exception e) {
            Log.e("ResumeDataManager", "Error filtering contact fields", e);
        }

        // Second pass: Filter contact fields (Ensure we don't have redundant logic if needed)
        try {
            // Header sync - No longer syncing back to a 'cnt' shorthand since we removed it
        } catch (Exception e) {
             Log.e("ResumeDataManager", "Error syncing header", e);
        }
        
        try {
            java.util.Iterator<String> keys = data.keys();
            while (keys.hasNext()) {
                String shortKey = keys.next();
                String[] mapping = SHORTHAND_MAP.get(shortKey);
                if (mapping == null) continue;

                String sId = mapping[0];
                String sName = mapping[1];
                String sType = mapping[2];
                
                SectionModel template = null;
                for (SectionModel t : ALL_SECTIONS_TEMPLATE) {
                    if (t.id.equals(sId)) { template = t; break; }
                }
                
                // Get or Create Section
                SectionModel section = sectionMap.get(sId);
                if (section == null) {
                    section = new SectionModel(sId, sName, 
                        template != null ? template.icon : "fa-star", sType, 
                        template != null ? template.group : "gridAdd");
                    section.items.clear();
                    sectionMap.put(sId, section);
                }
                
                Object val = data.get(shortKey);
                if (val instanceof JSONArray) {
                    JSONArray arr = (JSONArray) val;
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject itemObj = arr.getJSONObject(i);
                        List<FieldModel> fields = new ArrayList<>();
                        java.util.Iterator<String> itemKeys = itemObj.keys();
                        while (itemKeys.hasNext()) {
                            String fKey = itemKeys.next();
                            if (shortKey.equals("per") && CONTACT_FIELDS.contains(fKey.toLowerCase())) continue;
                            
                            // 1. Alias Mapping (Fix for missing Company/Institute)
                            String lowerKey = fKey.toLowerCase();
                            String mappedKey = fKey;
                            
                            if (lowerKey.equals("com") || lowerKey.equals("company") || lowerKey.equals("org")) mappedKey = "comp";
                            else if (lowerKey.equals("inst") || lowerKey.equals("school") || lowerKey.equals("college")) mappedKey = "inst";
                            else if (lowerKey.equals("role") || lowerKey.equals("position") || lowerKey.equals("pos") || lowerKey.equals("title")) mappedKey = "role";
                            else if (lowerKey.equals("desc") || lowerKey.equals("description") || lowerKey.equals("summary") || lowerKey.equals("resp")) mappedKey = "desc";
                            else if (lowerKey.equals("year") || lowerKey.equals("date") || lowerKey.equals("dur")) mappedKey = "dur";
                            
                            // Contextual fix for 'dur' vs 'year' depending on section
                            if (shortKey.equals("edu") && mappedKey.equals("dur")) mappedKey = "year";
                            else if (shortKey.equals("exp") && mappedKey.equals("year")) mappedKey = "dur";

                            String label = fKey;
                            if (template != null && !template.items.isEmpty()) {
                                for (FieldModel tf : template.items.get(0).fields) {
                                    if (tf.key.equalsIgnoreCase(mappedKey) || tf.key.equalsIgnoreCase(fKey)) {
                                        label = tf.label;
                                        mappedKey = tf.key; // Align to template strict key
                                        break;
                                    }
                                }
                            }
                            
                            FieldModel fm = new FieldModel(mappedKey, label, "text");
                            fm.value = getJsonVal(itemObj, fKey);
                            fields.add(fm);
                        }
                        if (!fields.isEmpty()) section.items.add(new ItemModel(fields));
                    }
                } else if (val instanceof JSONObject) {
                    JSONObject itemObj = (JSONObject) val;
                    List<FieldModel> fields = new ArrayList<>();
                    java.util.Iterator<String> itemKeys = itemObj.keys();
                    while (itemKeys.hasNext()) {
                        String fKey = itemKeys.next();
                        if (shortKey.equals("per") && CONTACT_FIELDS.contains(fKey.toLowerCase())) continue;
                        
                        // Alias Mapping for JSONObject case (hdr, cnt, sum, per)
                        String lowerKey = fKey.toLowerCase();
                        String mappedKey = fKey;
                        
                        if (shortKey.equals("sum") && (lowerKey.equals("desc") || lowerKey.equals("summary") || lowerKey.equals("body"))) {
                            mappedKey = "summary";
                        } else if (lowerKey.contains("linkedin")) {
                            mappedKey = "linkedin";
                        } else if (lowerKey.contains("github")) {
                            mappedKey = "github";
                        } else if (lowerKey.contains("facebook") || lowerKey.equals("fb")) {
                            mappedKey = "facebook";
                        } else if (lowerKey.equals("link") || lowerKey.equals("website") || lowerKey.equals("url") || lowerKey.equals("web")) {
                            mappedKey = "link";
                        }
                        
                        String label = fKey;
                        if (template != null && !template.items.isEmpty()) {
                            for (FieldModel tf : template.items.get(0).fields) {
                                if (tf.key.equalsIgnoreCase(mappedKey) || tf.key.equalsIgnoreCase(fKey)) {
                                    label = tf.label;
                                    mappedKey = tf.key;
                                    break;
                                }
                            }
                        }
                        
                        FieldModel fm = new FieldModel(mappedKey, label, "text");
                        fm.value = getJsonVal(itemObj, fKey);
                        fields.add(fm);
                    }
                    if (!fields.isEmpty()) section.items.add(new ItemModel(fields));
                }
                Log.d("ResumeDataManager", "Processed shorthand section: " + shortKey + " -> " + sId + " (Total items: " + section.items.size() + ")");
            }

            // Ensure every section has at least one item if empty (to avoid crashes)
            for(SectionModel s : sectionMap.values()) {
                if(s.items.isEmpty()) s.addItem();
            }

        } catch (Exception e) {
            Log.e("ResumeDataManager", "Error parsing shorthand JSON", e);
        }
        
        return new ArrayList<>(sectionMap.values());
    }

    private static String sanitizeJson(String json) {
        if (json == null) return "{}";
        String s = json.trim();
        // Remove markdown wrappers
        if (s.startsWith("```json")) s = s.substring(7);
        if (s.startsWith("```")) s = s.substring(3);
        if (s.endsWith("```")) s = s.substring(0, s.length() - 3);
        s = s.trim();
        
        // Remove trailing commas before closing braces/brackets
        s = s.replaceAll(",\\s*\\}", "}");
        s = s.replaceAll(",\\s*\\]", "]");
        
        return s;
    }

    public static List<SectionModel> parseSmartText(String text) {
        List<SectionModel> sections = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) return sections;

        String[] lines = text.split("\n");
        SectionModel currentSection = null;
        ItemModel currentItem = null;
        
        // Shorthand Mappings (Shorthand -> Template ID)
        java.util.Map<String, String> sectionMap = new java.util.HashMap<>();
        sectionMap.put("hdr", "headerSection"); sectionMap.put("header", "headerSection");
        sectionMap.put("sum", "summarySection"); sectionMap.put("summary", "summarySection");
        sectionMap.put("objective", "summarySection");
        sectionMap.put("exp", "experience"); sectionMap.put("experience", "experience");
        sectionMap.put("edu", "education"); sectionMap.put("education", "education");
        sectionMap.put("pro", "projects"); sectionMap.put("projects", "projects");
        sectionMap.put("skl", "skills"); sectionMap.put("skills", "skills");
        sectionMap.put("lang", "languages"); sectionMap.put("lan", "languages");
        sectionMap.put("cert", "certifications"); sectionMap.put("crt", "certificates");
        sectionMap.put("vol", "volunteer"); sectionMap.put("ach", "achievements");
        sectionMap.put("web", "weblinks"); sectionMap.put("hob", "hobbies");
        sectionMap.put("pub", "publications"); sectionMap.put("ref", "references");
        sectionMap.put("trn", "training"); sectionMap.put("int", "internships");
        sectionMap.put("per", "personalDetails"); sectionMap.put("pass", "passportDetails");
        sectionMap.put("awd", "awards"); sectionMap.put("dec", "declarationSection");

        // Key Mappings (Shorthand -> Field Key)
        java.util.Map<String, String> keyMap = new java.util.HashMap<>();
        keyMap.put("com", "comp"); keyMap.put("company", "comp"); keyMap.put("org", "comp");
        keyMap.put("role", "role"); keyMap.put("title", "role"); keyMap.put("pos", "role");
        keyMap.put("dur", "dur"); keyMap.put("date", "dur"); keyMap.put("year", "year");
        keyMap.put("inst", "inst"); keyMap.put("school", "inst"); keyMap.put("college", "inst");
        keyMap.put("deg", "deg"); keyMap.put("degree", "deg");
        keyMap.put("desc", "desc"); keyMap.put("resp", "desc"); keyMap.put("summary", "summary");
        keyMap.put("vals", "vals"); keyMap.put("items", "vals");
        keyMap.put("lang", "lang"); keyMap.put("lvl", "lvl");
        keyMap.put("name", "name"); keyMap.put("email", "email"); keyMap.put("phone", "phone");
        keyMap.put("addr", "addr"); keyMap.put("link", "link");

        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty()) continue;

            // 1. Detect Section Header
            String cleanLine = trimmedLine.toLowerCase().replace("---", "").replace(":", "").trim();
            if (sectionMap.containsKey(cleanLine)) {
                String targetId = sectionMap.get(cleanLine);
                SectionModel template = null;
                for (SectionModel s : ALL_SECTIONS_TEMPLATE) {
                    if (s.id.equals(targetId)) { template = s; break; }
                }
                if (template != null) {
                    currentSection = new SectionModel(template.id, template.name, template.icon, template.type, template.group);
                    currentSection.items.clear();
                    sections.add(currentSection);
                    currentItem = null;
                    continue;
                }
            }

            // 2. Parse Key-Value Pair
            if (trimmedLine.contains(":") && currentSection != null) {
                int splitIdx = trimmedLine.indexOf(":");
                String rawKey = trimmedLine.substring(0, splitIdx).trim();
                String val = trimmedLine.substring(splitIdx + 1).trim();
                
                // --- INVERSION CHECK ---
                // If key looks like a value (e.g. contains @ or is very long) AND value looks like a key
                if ((rawKey.contains("@") || rawKey.length() > 25) && val.length() < 15 && !val.contains(" ")) {
                     // Swap them
                     String temp = rawKey;
                     rawKey = val;
                     val = temp;
                }
                
                String cleanKey = rawKey.toLowerCase();
                String mappedKey = keyMap.getOrDefault(cleanKey, cleanKey);
                
                // --- AUTO-HEADER RECOVERY ---
                // If we see an email/phone but no section is active, or if we see it in Personal
                boolean isContactVal = isEmail(val) || isPhone(val);
                if (currentSection == null && isContactVal) {
                    // Create a synthetic header
                    SectionModel template = null;
                    for (SectionModel s : ALL_SECTIONS_TEMPLATE) {
                        if (s.id.equals("headerSection")) { template = s; break; }
                    }
                    if (template != null) {
                        currentSection = new SectionModel(template.id, template.name, template.icon, template.type, template.group);
                        currentSection.items.clear();
                        sections.add(currentSection);
                        currentItem = createDefaultItem(currentSection.type);
                        for (FieldModel f : currentItem.fields) f.value = ""; 
                        currentSection.items.add(currentItem);
                    }
                }

                if (currentSection != null && currentSection.id.equals("personalDetails")) {
                    if (isContactVal) {
                        // This belongs in Header, skip adding to Personal
                        Log.d("ResumeDataManager", "SmartText: Skipping contact value in Personal: " + val);
                        continue; 
                    }
                }

                // Contextual mapping fixes
                if (currentSection.id.equals("summarySection") && (mappedKey.equals("desc") || mappedKey.equals("summary"))) {
                    mappedKey = "summary";
                }
                if (currentSection.id.equals("projects") && mappedKey.equals("title")) {
                    mappedKey = "name";
                }

                // Check for new item start
                boolean isPrimaryToken = (currentSection.type.equals("experience") && mappedKey.equals("comp")) ||
                                         (currentSection.type.equals("education") && mappedKey.equals("inst")) ||
                                         (currentSection.type.equals("projects") && mappedKey.equals("name")) ||
                                         (currentSection.type.equals("languages") && mappedKey.equals("lang")) ||
                                         (currentSection.type.equals("skills") && mappedKey.equals("cat")) ||
                                         (currentSection.type.equals("header") && mappedKey.equals("name")); // Prevent header overwrite

                if (currentItem == null || isPrimaryToken) {
                    currentItem = createDefaultItem(currentSection.type);
                    for (FieldModel f : currentItem.fields) f.value = ""; // Start blank
                    currentSection.items.add(currentItem);
                }

                // Set value - match by key or approximate mapping
                boolean found = false;
                for (FieldModel f : currentItem.fields) {
                    if (f.key.equals(mappedKey)) {
                        f.value = val;
                        found = true;
                        break;
                    }
                }
                
                if (!found) {
                    String fallbackKey = null;
                    if (mappedKey.equals("comp")) fallbackKey = "org"; 
                    else if (mappedKey.equals("org")) fallbackKey = "comp";
                    else if (mappedKey.equals("role")) fallbackKey = "title";
                    else if (mappedKey.equals("title")) fallbackKey = "role";
                    else if (mappedKey.equals("desc")) fallbackKey = "body";
                    
                    if (fallbackKey != null) {
                         for (FieldModel fm : currentItem.fields) {
                            if (fm.key.equals(fallbackKey)) {
                                fm.value = val;
                                found = true;
                                break;
                            }
                        }
                    }
                }
            } else if (currentSection != null && currentItem != null) {
                 for (FieldModel fm : currentItem.fields) {
                    if ((fm.key.equals("desc") || fm.key.equals("summary") || fm.key.equals("vals")) && !fm.value.isEmpty()) {
                        fm.value += "\n" + trimmedLine;
                        break;
                    }
                }
            }
        }
        
        // Ensure every section has at least one item if empty (to avoid crashes)
        for(SectionModel s : sections) {
             if(s.items.isEmpty()) s.addItem();
        }
        
        return sections;
    }

    public static JSONObject generateStateJson(List<SectionModel> sections, JSONObject originalData) throws JSONException {
        JSONObject state = new JSONObject();
        JSONObject headerData = new JSONObject();
        
        if (originalData != null && originalData.has("header")) {
            headerData = originalData.getJSONObject("header");
        }

        // 1. Sync Header Data from List if available
        SectionModel headerSec = null;
        for (SectionModel s : sections) {
            if (s.id.equals("nameProfessionSection")) {
                headerSec = s;
                break;
            }
        }

        if (headerSec != null && !headerSec.items.isEmpty()) {
            ItemModel hItem = headerSec.items.get(0);
            headerData.put("name", getFieldValue(hItem, "name"));
            
            JSONArray cItems = new JSONArray();
            int count = 0;
            for (FieldModel f : hItem.fields) {
                if (f.key.equals("name") || f.key.equals("role") || f.key.equals("title")) continue;
                if (f.value.isEmpty() || f.value.equals("...")) continue;

                JSONObject c = new JSONObject();
                c.put("text", f.value);
                
                String icon = "fa-info-circle";
                String prefix = "fas";
                String lowKey = f.key.toLowerCase();

                if (lowKey.contains("email")) icon = "fa-envelope";
                else if (lowKey.contains("phone") || lowKey.contains("tel") || lowKey.contains("mob")) icon = "fa-phone";
                else if (lowKey.contains("addr") || lowKey.contains("city") || lowKey.contains("loc")) icon = "fa-map-marker-alt";
                else if (lowKey.contains("link") || lowKey.contains("web") || lowKey.contains("url") || lowKey.contains("port") || lowKey.contains("git") || lowKey.contains("site")) icon = "fa-link";
                
                if (lowKey.contains("git")) { icon = "fa-github"; prefix = "fab"; }
                else if (lowKey.contains("linkedin")) { icon = "fa-linkedin"; prefix = "fab"; }
                else if (lowKey.contains("facebook") || lowKey.contains("fb")) { icon = "fa-facebook"; prefix = "fab"; }
                else if (lowKey.contains("twitter") || lowKey.contains("x")) { icon = "fa-twitter"; prefix = "fab"; }
                else if (lowKey.contains("insta")) { icon = "fa-instagram"; prefix = "fab"; }
                
                c.put("icon", prefix + " " + icon);
                c.put("column", (count % 2 == 0) ? "left" : "right");
                cItems.put(c);
                count++;
            }
            
            headerData.put("items", cItems);
        }
        
        StringBuilder html = new StringBuilder();
        html.append("<header id=\"mainHeader\">");
        
        if (headerData.length() > 0) {
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
                    target.append("<span>").append(item.optString("text", "")).append("</span>");
                    target.append("</div>");
                }
                left.append("</div>");
                right.append("</div>");
                html.append(left).append(right);
            }
            html.append("</div></div>");
        }
        html.append("</header>");
        
        // 2. Split sections into Layout groups
        List<SectionModel> objectiveSecs = new ArrayList<>();
        List<SectionModel> leftSecs = new ArrayList<>();
        List<SectionModel> rightSecs = new ArrayList<>();
        List<SectionModel> footerSecs = new ArrayList<>();
        
        // List of IDs that belong to the LEFT column
        List<String> leftIds = Arrays.asList(
            "personalDetails", "passportDetails", "languages", "skills", 
            "certificates", "weblinks", "achievements", "hobbies", "interests",
            "visualRegistry", "physicalProfile", "visaStatus", "lifestyleHabits", 
            "astrologySection", "familyDetails", "partnerExpectations", "testScores"
        );

        for (SectionModel s : sections) {
             if (s.id.equals("nameProfessionSection") || s.id.equals("profileSection")) continue;
             
             
             if (s.id.equals("summarySection")) {
                 objectiveSecs.add(s);
             } else if (s.id.equals("declarationSection")) {
                 footerSecs.add(s);
             } else if (leftIds.contains(s.id)) {
                 leftSecs.add(s);
             } else {
                 // Default to RIGHT column (Education, Experience, Projects, etc.)
                 rightSecs.add(s);
             }
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
        
        JSONObject colors = new JSONObject();
        colors.put("header", "#1e3c72"); colors.put("left", "#f7f9fc"); colors.put("right", "#ffffff");
        colors.put("text", "#333333"); colors.put("headerText", "#ffffff");

        if (originalData != null && originalData.has("colors")) {
            JSONObject existing = originalData.getJSONObject("colors");
            java.util.Iterator<String> cKeys = existing.keys();
            while (cKeys.hasNext()) {
                String k = cKeys.next();
                String val = existing.optString(k, "");
                
                // VALIDATION: Prevent black column corruption
                // Check both 6-char (#000000) and 8-char ARGB (#ff000000) formats
                if (k.equals("left") || k.equals("right")) {
                    String lowerVal = val.toLowerCase();
                    if (lowerVal.equals("#000000") || lowerVal.equals("#ff000000") || 
                        lowerVal.equals("#333333") || lowerVal.equals("#ff333333") ||
                        lowerVal.equals("black")) {
                        Log.w("ResumeDataManager", "Discarding suspicious black column color: " + val + " for " + k);
                        continue; // Use default
                    }
                }
                colors.put(k, existing.get(k));
            }
        }
        state.put("colors", colors);
        
        if (originalData != null && originalData.has("metrics")) {
             state.put("metrics", originalData.getJSONObject("metrics"));
        } else {
            JSONObject metrics = new JSONObject();
            metrics.put("width", "35%"); metrics.put("spacing", 50); metrics.put("header", 100); 
            metrics.put("radius", 0); metrics.put("cardRadius", 8); metrics.put("globalFontSize", 14);
            state.put("metrics", metrics);
        }
        
        if (originalData != null && originalData.has("sig")) state.put("sig", originalData.getString("sig"));
        else state.put("sig", "");
        
        JSONObject flags = new JSONObject();
        flags.put("iconsHidden", false); flags.put("breaksHidden", false); flags.put("isEditing", true);
        state.put("flags", flags);
        
        return state;
    }

    private static String generateSectionWrapper(SectionModel section) {
        StringBuilder sb = new StringBuilder();
        String extraClass = "";
        if (section.id.equals("summarySection")) extraClass = "objective";
        else if (section.id.equals("personalDetails")) extraClass = "personal-details";
        else if (section.id.equals("declarationSection")) extraClass = "declaration";

        sb.append("<section class=\"resume-section ").append(extraClass).append("\" id=\"").append(section.id).append("\" data-section-type=\"").append(section.type).append("\">");
        sb.append("<h2><i class=\"fas ").append(section.icon).append("\"></i> ").append(section.name.toUpperCase()).append("</h2>");
        sb.append("<div class=\"content-area\">");
        for (ItemModel item : section.items) {
            sb.append(generateItemHTML(section.type, item));
        }
        sb.append("</div></section>");
        return sb.toString();
    }

    private static String generateItemHTML(String type, ItemModel item) {
        StringBuilder sb = new StringBuilder();
        switch (type) {
            case "header":
                // 1. Name & Title
                String name = getFieldValue(item, "name");
                String title = getFieldValue(item, "role"); // Some templates might map title/role
                if (title.isEmpty() || title.equals("...")) title = getFieldValue(item, "title");

                if (!name.isEmpty() && !name.equals("...")) {
                    sb.append("<h1>").append(name).append("</h1>");
                }
                if (!title.isEmpty() && !title.equals("...")) {
                    sb.append("<div class=\"professional-title\">").append(title).append("</div>");
                }

                // 2. Header Info (Email, Phone, Addr, Links)
                sb.append("<div class=\"contact-container\">"); // Optional wrapper
                for(FieldModel f : item.fields) {
                    if (f.key.equals("name") || f.key.equals("role") || f.key.equals("title")) continue;
                    if (f.value.isEmpty() || f.value.equals("...")) continue;

                    String icon = "fa-info-circle";
                    String prefix = "fas";
                    String lowKey = f.key.toLowerCase();
                    String val = f.value;

                    if (lowKey.contains("email")) icon = "fa-envelope";
                    else if (lowKey.contains("phone") || lowKey.contains("tel") || lowKey.contains("mob")) icon = "fa-phone";
                    else if (lowKey.contains("addr") || lowKey.contains("city") || lowKey.contains("loc")) icon = "fa-map-marker-alt";
                    else if (lowKey.contains("link") || lowKey.contains("web") || lowKey.contains("url") || lowKey.contains("port") || lowKey.contains("git") || lowKey.contains("site")) icon = "fa-link";
                    
                    if (lowKey.contains("git")) { icon = "fa-github"; prefix = "fab"; }
                    else if (lowKey.contains("linkedin")) { icon = "fa-linkedin"; prefix = "fab"; }
                    else if (lowKey.contains("face") || lowKey.contains("fb")) { icon = "fa-facebook"; prefix = "fab"; }
                    else if (lowKey.contains("twit") || lowKey.contains("x")) { icon = "fa-twitter"; prefix = "fab"; }
                    else if (lowKey.contains("insta")) { icon = "fa-instagram"; prefix = "fab"; }
                    else if (lowKey.contains("disc")) { icon = "fa-discord"; prefix = "fab"; }
                    else if (lowKey.contains("red")) { icon = "fa-reddit"; prefix = "fab"; }
                    else if (lowKey.contains("quo")) { icon = "fa-quora"; prefix = "fab"; }

                    String displayVal = val;
                    if (val.startsWith("http") || val.contains(".com") || val.contains(".org") || val.contains(".me") || val.contains(".io")) {
                        String href = val.startsWith("http") || val.startsWith("www") ? val : "https://" + val;
                        if (!val.startsWith("http") && val.startsWith("www")) href = "https://" + val;
                        displayVal = "<a href=\"" + href + "\" target=\"_blank\">" + val + "</a>";
                    }

                    sb.append("<div class=\"contact-item\"><i class=\"").append(prefix).append(" ").append(icon).append("\"></i> ")
                      .append("<span>").append(displayVal).append("</span></div>");
                }
                sb.append("</div>");
                break;
            case "personal":
                for(FieldModel f : item.fields) {
                    sb.append("<div class=\"pd-row\"><span class=\"pd-label\">").append(f.label).append(":</span> <span class=\"pd-val\">")
                      .append(f.value.isEmpty() ? "N/A" : f.value).append("</span></div>");
                }
                break;
            case "weblinks":
                sb.append("<div class=\"data-table-item\">");
                String wName = getFieldValue(item, "name");
                String wUrl = getFieldValue(item, "url");
                sb.append("<div class=\"table-row\"><span class=\"table-label\">").append(wName).append(":</span> ")
                  .append("<span class=\"table-val\"><a href=\"").append(wUrl.startsWith("http") ? wUrl : "https://" + wUrl)
                  .append("\" target=\"_blank\">").append(wUrl).append("</a></span></div>");
                sb.append("</div>");
                break;
            case "summary_paragraph":
                sb.append("<p style=\"line-height: 1.6; color: var(--text-main);\">").append(getFieldValue(item, "summary")).append("</p>");
                break;
            case "education":
                sb.append("<div class=\"data-table-item\"><div class=\"table-row\" style=\"justify-content: space-between;\"><div><span class=\"table-label\">Institute:</span> <span class=\"table-val\" style=\"font-weight:600;\">")
                  .append(getFieldValue(item, "inst")).append("</span></div>");
                String gpa = getFieldValue(item, "gpa");
                if (!gpa.equals("...") && !gpa.isEmpty()) {
                    sb.append("<span class=\"table-val\" style=\"font-size:0.85em; color: var(--text-muted);\">CGPA: ").append(gpa).append("</span>");
                }
                sb.append("</div>");
                sb.append("<div class=\"table-row\"><span class=\"table-label\">Year:</span> <span class=\"table-val\">").append(getFieldValue(item, "year")).append("</span>");
                sb.append("<span class=\"table-label\" style=\"margin-left:14px;\">Board:</span> <span class=\"table-val\">").append(getFieldValue(item, "board")).append("</span></div>");
                sb.append("<div class=\"table-row\"><span class=\"table-label\">Degree:</span> <span class=\"table-val\">").append(getFieldValue(item, "deg")).append("</span></div></div>");
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
                String[] bullets = desc.equals("...") ? new String[]{"..."} : desc.split("\n");
                for (String b : bullets) {
                    if (!b.trim().isEmpty()) {
                        sb.append("<li>").append(b.trim()).append("</li>");
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
                sb.append("<div class=\"data-table-item\">");
                sb.append("<div class=\"table-row\" style=\"justify-content:space-between;\"><span class=\"table-val\" style=\"font-weight:700;\">")
                  .append((getFieldValue(item, "topic") + getFieldValue(item, "course") + getFieldValue(item, "title")).replace("...", "")).append("</span>");
                sb.append("<span class=\"table-val\" style=\"font-size:0.85em; color: var(--text-muted);\">")
                  .append((getFieldValue(item, "year") + getFieldValue(item, "date")).replace("...", "")).append("</span></div>");
                sb.append("<div class=\"table-row\"><span class=\"table-val\" style=\"font-style:italic;\">")
                  .append((getFieldValue(item, "role") + getFieldValue(item, "inst") + getFieldValue(item, "amt")).replace("...", "")).append("</span></div>");
                String rDesc = getFieldValue(item, "desc");
                if (!rDesc.equals("...")) sb.append("<div class=\"proj-desc\">").append(rDesc).append("</div>");
                sb.append("</div>");
                break;

            case "passport":
                sb.append("<div class=\"data-table-item\">");
                sb.append("<div class=\"table-row\"><span class=\"table-label\">Passport No:</span> <span class=\"table-val\">").append(getFieldValue(item, "pno")).append("</span></div>");
                sb.append("<div class=\"table-row\"><span class=\"table-label\">Issue Date:</span> <span class=\"table-val\">").append(getFieldValue(item, "idate")).append("</span></div>");
                sb.append("<div class=\"table-row\"><span class=\"table-label\">Expiry Date:</span> <span class=\"table-val\">").append(getFieldValue(item, "edate")).append("</span></div></div>");
                break;

            case "physical":
                sb.append("<div class=\"physics-grid\">");
                for (FieldModel f : item.fields) {
                    sb.append("<div class=\"pd-row\"><span class=\"pd-label\">").append(f.label).append(":</span> <span class=\"pd-val\">").append(f.value).append("</span></div>");
                }
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
                sb.append("<div class=\"signature-wrapper\">")
                  .append("<div class=\"signature-controls\" id=\"sigControls\">")
                  .append("<button class=\"signature-btn\" id=\"uploadSigBtn\"><i class=\"fas fa-upload\"></i> Upload Image</button>")
                  .append("<button class=\"signature-btn\" id=\"drawSigBtn\"><i class=\"fas fa-pen-fancy\"></i> Draw Signature</button>")
                  .append("<button class=\"signature-btn\" id=\"clearSigBtn\" style=\"color:red;\"><i class=\"fas fa-eraser\"></i> Clear</button>")
                  .append("</div>")
                  .append("<input type=\"file\" id=\"sigFileInput\" accept=\"image/*\" style=\"display:none;\">")
                  .append("<canvas id=\"signatureCanvas\" width=\"300\" height=\"100\"></canvas>")
                  .append("<img src=\"\" alt=\"\" id=\"signatureImg\" class=\"signature-display\" style=\"display:none;\">")
                  .append("<div class=\"signature-line\"><span id=\"textSignature\" contenteditable=\"true\">Alex Johnson</span></div>")
                  .append("</div>");
                break;
            case "references":
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
            default:
                String val = getFieldValue(item, "val");
                if (val.equals("...")) val = getFieldValue(item, "text");
                sb.append("<div class=\"simple-list-item\">").append(val).append("</div>");
        }
        return sb.toString();
    }

    private static String getJsonVal(JSONObject obj, String key) {
        Object val = obj.opt(key);
        if (val == null) return "";
        if (val instanceof JSONArray) {
            JSONArray arr = (JSONArray) val;
            StringBuilder sb = new StringBuilder();
            for (int i=0; i<arr.length(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(arr.optString(i, ""));
            }
            return sb.toString();
        }
        return obj.optString(key, "");
    }

    private static boolean isEmail(String text) {
        if (text == null) return false;
        String t = text.trim();
        return t.contains("@") && t.contains(".") && !t.contains(" ") && t.length() > 5;
    }

    private static boolean isPhone(String text) {
        if (text == null) return false;
        String t = text.trim();
        String digits = t.replaceAll("[^0-9]", "");
        // More robust: reject if it looks like a year or small count
        if (digits.length() < 7 || digits.length() > 15) return false;
        return t.startsWith("+") || t.matches(".*[0-9]{3,}-.*") || t.matches(".*[0-9]{7,}.*");
    }

    private static String getFieldValue(ItemModel item, String key) {
        for(FieldModel f : item.fields) if(f.key.equals(key)) return f.value.isEmpty() ? "..." : f.value;
        return "...";
    }
}
