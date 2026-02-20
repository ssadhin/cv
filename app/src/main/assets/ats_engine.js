/**
 * ATS Health Hub Engine (Headless)
 * - Heuristic Scoring (Impact, Skills, Brevity)
 * - Keyword Matching (Job Description)
 * - Sends data to Android via window.Android.onATSReport()
 */

class ATSAnalyzer {
    constructor() {
        this.score = 0;
        this.issues = [];
        this.metrics = {
            wordCount: 0,
            quantifiedBullets: 0,
            actionVerbs: 0,
            totalBullets: 0
        };
        this.successes = []; // "What you did right"
        this.jobKeywords = [];

        // Action verbs list (Top 50 common strong verbs)
        this.strongVerbs = new Set([
            'achieved', 'managed', 'created', 'led', 'developed', 'increased', 'decreased', 'saved',
            'negotiated', 'launched', 'improved', 'generated', 'spearheaded', 'orchestrated', 'implemented',
            'engineered', 'designed', 'optimized', 'resolved', 'oversaw', 'executed', 'mentored',
            'accelerated', 'expanded', 'pioneered', 'transformed', 'delivered', 'produced', 'initiated',
            'coordinated', 'facilitated', 'maximized', 'minimized', 'streamlined', 'strengthened',
            'directed', 'supervised', 'administered', 'controlled', 'founded', 'established'
        ]);

        this.init();
    }

    init() {
        // Initial check
        setTimeout(() => this.analyze(), 500);

        // Listen for changes (debounced)
        let debounceTimer;
        const observer = new MutationObserver(() => {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => this.analyze(), 500);
        });
        observer.observe(document.body, { childList: true, subtree: true, characterData: true });
    }

    // --- LOGIC ---

    analyze() {
        this.issues = [];    // Errors ("Wrong")
        this.successes = []; // "Right"
        this.warnings = [];  // "Improve"
        this.breakdown = {
            density: 0,
            experience: 0,
            education: 0,
            skills: 0,
            impact: 0,
            verbs: 0
        };

        this.metrics = { wordCount: 0, quantifiedBullets: 0, actionVerbs: 0, totalBullets: 0 };
        let rawScore = 0;

        // 1. Content & Density Analysis (Max 15)
        const textContent = document.body.innerText;
        this.metrics.wordCount = textContent.split(/\s+/).length;

        if (this.metrics.wordCount >= 150 && this.metrics.wordCount <= 1000) {
            this.breakdown.density = 15;
            this.successes.push({ title: "Optimal Word Count", msg: `${this.metrics.wordCount} words is a great balance.` });
        } else if (this.metrics.wordCount < 150) {
            this.breakdown.density = 5;
            this.issues.push({ title: "Content matches 'Stub'", msg: "Your resume is dangerously short (<150 words)." });
        } else {
            this.breakdown.density = 10;
            this.warnings.push({ title: "Overwhelming Length", msg: "Consider trimming content. 400-800 words is best." });
        }

        // 2. Structural Intelligence (Exp: 20, Edu: 10, Skills: 15)
        const sections = Array.from(document.querySelectorAll('.section-title, h2, [data-type] h2')).map(h => ({
            text: h.innerText.toLowerCase(),
            parent: h.closest('[data-type]') || h.parentElement
        }));

        const sectionTypes = sections.map(s => s.text);
        const hasExp = sectionTypes.some(h => h.includes('experience') || h.includes('work'));
        const hasEdu = sectionTypes.some(h => h.includes('education'));
        const hasSkills = sectionTypes.some(h => h.includes('skill'));

        if (hasExp) { this.breakdown.experience = 20; this.successes.push({ title: "Core: Work Experience Found" }); }
        else this.issues.push({ title: "Missing Career History", msg: "ATS cannot find your 'Work Experience'." });

        if (hasEdu) { this.breakdown.education = 10; this.successes.push({ title: "Core: Education Section Found" }); }
        else this.issues.push({ title: "Missing Education", msg: "Always include professional or academic credentials." });

        if (hasSkills) { this.breakdown.skills = 15; this.successes.push({ title: "Core: Skills/Tools Section Found" }); }
        else this.warnings.push({ title: "Missing Skills Matrix", msg: "A dedicated skills section is vital." });

        // 3. Impact & Action Verb Intelligence (Impact: 25, Verbs: 15)
        const bullets = document.querySelectorAll('li, .resp-list li');
        this.metrics.totalBullets = bullets.length;

        bullets.forEach(li => {
            const text = li.innerText.trim();
            if (text.length < 5) return;

            if (/\d+%|\$\d+|\d+\+|\b\d{2,}\b/.test(text) && !text.match(/\b(19|20)\d{2}\b/)) {
                this.metrics.quantifiedBullets++;
            }

            const firstWord = text.split(' ')[0].toLowerCase().replace(/[^a-z]/g, '');
            if (this.strongVerbs.has(firstWord)) {
                this.metrics.actionVerbs++;
            }

            if (text.split(' ').length > 40) {
                this.warnings.push({ title: "Run-on Bullet Point", msg: "Keep bullets under 30 words." });
            }
        });

        const impactRatio = this.metrics.totalBullets > 0 ? (this.metrics.quantifiedBullets / this.metrics.totalBullets) : 0;
        const verbRatio = this.metrics.totalBullets > 0 ? (this.metrics.actionVerbs / this.metrics.totalBullets) : 0;

        this.breakdown.impact = Math.min(25, Math.round(impactRatio * 100));
        this.breakdown.verbs = Math.min(15, Math.round(verbRatio * 100));

        if (impactRatio < 0.25 && this.metrics.totalBullets > 3) {
            this.warnings.push({ title: "Vague Accomplishments", msg: "Add %, $, or numbers to prove results." });
        } else if (impactRatio >= 0.3) {
            this.successes.push({ title: "Strong Quantified Impact", msg: "Plenty of data to prove your results." });
        }



        this.checkSkillEvidence();

        this.score = Object.values(this.breakdown).reduce((a, b) => a + b, 0);
        this.score = Math.min(100, Math.round(this.score));
        this.sendReportToAndroid();
    }

    checkSkillEvidence() {
        let skills = new Set();
        const skillTypes = document.querySelectorAll('.skill-sub, [data-type="skills"] li, .skill-group span');
        skillTypes.forEach(el => {
            const txt = el.innerText.toLowerCase();
            txt.split(/[,\/]/).forEach(s => {
                const clean = s.trim();
                if (clean.length > 1) skills.add(clean);
            });
        });

        if (skills.size === 0) return;

        const evidenceText = document.body.innerText.toLowerCase();
        let unproven = [];

        skills.forEach(skill => {
            if (!evidenceText.includes(skill)) unproven.push(skill);
        });

        if (unproven.length > 3) {
            this.warnings.push({
                title: "Unverified Expertise",
                msg: `Keywords like ${unproven.slice(0, 2).join(', ')} appear in skills but not in experience.`
            });
        } else if (skills.size > 5) {
            this.successes.push({ title: "Skills Backed by Evidence", msg: "Your experience confirms your listed technical skills." });
        }
    }

    analyzeJD(jdText) {
        if (!jdText || jdText.length < 20) return;

        const stopWords = new Set(['and', 'the', 'to', 'of', 'a', 'in', 'for', 'with', 'on', 'at', 'from', 'by', 'an', 'be', 'as', 'or', 'are', 'this', 'that', 'it', 'is', 'was', 'will', 'can', 'have', 'has', 'had', 'but', 'not', 'which', 'what', 'when', 'where', 'who', 'whom', 'why', 'how', 'we', 'you', 'your', 'our', 'role', 'work', 'job', 'team', 'candidate', 'experience', 'skills']);

        const words = jdText.toLowerCase().replace(/[^\w\s]/g, '').split(/\s+/);
        const freq = {};

        words.forEach(w => {
            if (w.length > 2 && !stopWords.has(w)) {
                freq[w] = (freq[w] || 0) + 1;
            }
        });

        // Get Top 15 keywords
        const sorted = Object.keys(freq).sort((a, b) => freq[b] - freq[a]).slice(0, 15);

        // Match against entire CV text
        const cvText = document.body.innerText.toLowerCase();
        const missing = [];
        const present = [];

        sorted.forEach(word => {
            if (cvText.includes(word)) present.push(word);
            else missing.push(word);
        });

        // Smart Verdict
        let matchScore = Math.round((present.length / sorted.length) * 100);

        // Send Result to Android
        if (window.Android && window.Android.onATSJobMatchResult) {
            const result = {
                score: matchScore,
                present: present,
                missing: missing
            };
            window.Android.onATSJobMatchResult(JSON.stringify(result));
        }
    }

    sendReportToAndroid() {
        if (window.Android && window.Android.onATSReport) {
            const report = {
                score: this.score,
                metrics: this.metrics,
                breakdown: this.breakdown, // Added this
                errors: this.issues,       // "Wrong"
                successes: this.successes, // "Right"
                warnings: this.warnings    // "Improve"
            };
            window.Android.onATSReport(JSON.stringify(report));
        }
    }
}

// Initialize
if (!window.atsAnalyzer) {
    window.atsAnalyzer = new ATSAnalyzer();
}
