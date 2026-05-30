package com.example.myapplication;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocalCommandParser {

    private static final Map<String, String> KEYWORD_TO_SHORTHAND = new HashMap<String, String>() {{
        put("header", "hdr"); put("name", "hdr"); put("profession", "hdr");
        put("summary", "sum"); put("objective", "sum"); put("obj", "sum");
        put("personal", "per"); put("details", "per");
        put("passport", "pass");
        put("experience", "exp"); put("work", "exp"); put("job", "exp");
        put("education", "edu"); put("degree", "edu"); put("school", "edu");
        put("skill", "skl"); put("skills", "skl");
        put("project", "pro"); put("projects", "pro");
        put("certification", "cert"); put("certifications", "cert"); put("certificate", "cert");
        put("award", "awd"); put("awards", "awd"); put("achievement", "ach");
        put("language", "lan"); put("languages", "lan");
        put("reference", "ref"); put("references", "ref");
        put("volunteer", "vol");
        put("publication", "pub"); put("publications", "pub");
        put("research", "res");
        put("teaching", "tea");
        put("grant", "gra"); put("grants", "gra");
        put("affiliation", "aff"); put("affiliations", "aff");
        put("training", "trn");
        put("internship", "intern"); put("internships", "intern");
        put("interest", "int"); put("interests", "int"); put("hobbies", "int"); put("hobby", "int");
        put("extra", "ext"); put("extracurricular", "ext");
        put("link", "web"); put("weblinks", "web"); put("website", "web");
        put("declaration", "dec");
        put("visa", "vis");
        put("test", "tst"); put("score", "tst"); put("scores", "tst");
        put("family", "fam");
        put("partner", "pex"); put("expectation", "pex");
        put("lifestyle", "lst"); put("habit", "lst");
        put("astrology", "ast"); put("astrological", "ast");
        put("physical", "phy");
        put("contact", "cnt");
        put("profile", "pic"); put("picture", "pic"); put("photo", "pic");
    }};

    /**
     * Parses a natural language command and converts it to Shorthand JSON.
     * Returns null if the command is too complex or not recognizable.
     */
    public static String parse(String input) {
        if (input == null) return null;
        String text = input.trim().toLowerCase();
        
        // Match simple remove/delete pattern: "remove <section>" or "delete <section>"
        Pattern removePattern = Pattern.compile("^(?:remove|delete|del|rm)\\s+(?:section\\s+)?([a-z\\s]+)$");
        Matcher removeMatcher = removePattern.matcher(text);
        if (removeMatcher.matches()) {
            String sectionWord = extractKeyWord(removeMatcher.group(1).trim());
            if (sectionWord != null) {
                return "{\"" + sectionWord + "\": [\"DELETE\"]}";
            }
        }

        // Match simple add/update pattern: "add <section> <info>" or "update <section> <info>"
        // This regex ensures we only run simple local regexes if it starts with a keyword
        Pattern addPattern = Pattern.compile("^(?:add|insert|put|update|change|set)\\s+(?:section\\s+)?([a-z]+)\\s+(.+)$");
        Matcher addMatcher = addPattern.matcher(text);
        if (addMatcher.matches()) {
            String sectionWordCand = addMatcher.group(1).trim();
            String info = addMatcher.group(2).trim();
            String shorthandKey = extractKeyWord(sectionWordCand);
            
            if (shorthandKey != null) {
                // Check if the "info" part contains other section keywords —
                // this means it's a multi-section command like "add education and experience and skills"
                // which should be deferred to the AI, not treated as content for one section.
                String[] infoWords = info.replaceAll("[^a-z\\s]", "").split("\\s+");
                for (String w : infoWords) {
                    if (KEYWORD_TO_SHORTHAND.containsKey(w) || 
                        (w.endsWith("s") && KEYWORD_TO_SHORTHAND.containsKey(w.substring(0, w.length() - 1)))) {
                        return null; // Too complex — let AI handle it
                    }
                }
                // Escape quotes and newlines for JSON
                String safeInfo = info.replace("\"", "\\\"").replace("\n", " ");
                return "{\"" + shorthandKey + "\": [\"" + safeInfo + "\"]}";
            }
        }
        
        // Complex structural queries like "move experience to top", "make this sound professional", 
        // "add a bullet point to my second job" will fail to match simple regexes and return null, 
        // deferring gracefully to the AI.
        return null; 
    }

    private static String extractKeyWord(String text) {
        String[] words = text.split("\\s+");
        for (String word : words) {
            String cleanWord = word.replaceAll("[^a-z]", "");
            if (KEYWORD_TO_SHORTHAND.containsKey(cleanWord)) {
                return KEYWORD_TO_SHORTHAND.get(cleanWord);
            }
            // Strip trailing 's' automatically for singular match
            if (cleanWord.endsWith("s") && KEYWORD_TO_SHORTHAND.containsKey(cleanWord.substring(0, cleanWord.length() - 1))) {
                 return KEYWORD_TO_SHORTHAND.get(cleanWord.substring(0, cleanWord.length() - 1));
            }
        }
        return null;
    }
}
