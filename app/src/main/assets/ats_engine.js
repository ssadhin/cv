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

        const baseVerbs = ['achieve', 'manage', 'create', 'lead', 'develop', 'increase', 'decrease', 'save', 'negotiate', 'launch', 'improve', 'generate', 'spearhead', 'orchestrate', 'implement', 'engineer', 'design', 'optimize', 'resolve', 'oversee', 'execute', 'mentor', 'accelerate', 'expand', 'pioneer', 'transform', 'deliver', 'produce', 'initiate', 'coordinate', 'facilitate', 'maximize', 'minimize', 'streamline', 'strengthen', 'direct', 'supervise', 'administer', 'control', 'found', 'establish', 'achieved', 'managed', 'created', 'led', 'developed', 'increased', 'decreased', 'saved', 'negotiated', 'launched', 'improved', 'generated', 'spearheaded', 'orchestrated', 'implemented', 'engineered', 'designed', 'optimized', 'resolved', 'oversaw', 'executed', 'mentored', 'accelerated', 'expanded', 'pioneered', 'transformed', 'delivered', 'produced', 'initiated', 'coordinated', 'facilitated', 'maximized', 'minimized', 'streamlined', 'strengthened', 'directed', 'supervised', 'administered', 'controlled', 'founded', 'established', 'achieving', 'managing', 'creating', 'leading', 'developing', 'increasing', 'decreasing', 'saving', 'negotiating', 'launching', 'improving', 'generating', 'spearheading', 'orchestrating', 'implementing', 'engineering', 'designing', 'optimizing', 'resolving', 'overseeing', 'executing', 'mentoring', 'accelerating', 'expanding', 'pioneering', 'transforming', 'delivering', 'producing', 'initiating', 'coordinating', 'facilitating', 'maximizing', 'minimizing', 'streamlining', 'strengthening', 'directing', 'supervising', 'administering', 'controlling', 'founding', 'establishing'];
        this.strongVerbs = new Set(baseVerbs);
        
        this.synonyms = {
            'ui': 'user interface',
            'ux': 'user experience',
            'seo': 'search engine optimization',
            'js': 'javascript',
            'aws': 'amazon web services',
            'api': 'application programming interface',
            'qa': 'quality assurance',
            'kpi': 'key performance indicator',
            'roi': 'return on investment',
            'pr': 'public relations'
        };

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

    stem(word) {
        let w = word.toLowerCase();
        if (w.endsWith('ing') && w.length > 4) return w.slice(0, -3);
        if (w.endsWith('ed') && w.length > 3) return w.slice(0, -2);
        if (w.endsWith('ment') && w.length > 5) return w.slice(0, -4);
        if (w.endsWith('ies') && w.length > 4) return w.slice(0, -3) + 'y';
        if (w.endsWith('s') && !w.endsWith('ss') && w.length > 3) return w.slice(0, -1);
        return w;
    }

    checkHazards() {
        const tables = document.querySelectorAll('table');
        if (tables.length > 0) {
            this.warnings.push({ title: "Table Layout Detected", msg: "Warning: HTML Tables can sometimes break older ATS parsers. Consider standard text formatting." });
        }
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

        this.checkHazards();

        this.metrics = { wordCount: 0, quantifiedBullets: 0, actionVerbs: 0, totalBullets: 0, yearsOfExperience: 0 };
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
        const hasExp = sectionTypes.some(h => h.includes('experience') || h.includes('work') || h.includes('employment') || h.includes('history') || h.includes('background') || h.includes('career'));
        const hasEdu = sectionTypes.some(h => h.includes('education') || h.includes('academic') || h.includes('degree'));
        const hasSkills = sectionTypes.some(h => h.includes('skill') || h.includes('technolog') || h.includes('competenc') || h.includes('tool'));

        if (hasExp) { this.breakdown.experience = 20; this.successes.push({ title: "Core: Work Experience Found" }); }
        else this.issues.push({ title: "Missing Career History", msg: "ATS cannot find your 'Work Experience'." });

        if (hasEdu) { this.breakdown.education = 10; this.successes.push({ title: "Core: Education Section Found" }); }
        else this.issues.push({ title: "Missing Education", msg: "Always include professional or academic credentials." });

        if (hasSkills) { this.breakdown.skills = 15; this.successes.push({ title: "Core: Skills/Tools Section Found" }); }
        else this.warnings.push({ title: "Missing Skills Matrix", msg: "A dedicated skills section is vital." });

        // 3. Impact & Action Verb Intelligence (Impact: 25, Verbs: 15)
        const bullets = document.querySelectorAll('li, .resp-list li');
        this.metrics.totalBullets = bullets.length;

        const firstWordsUsed = {};

        bullets.forEach(li => {
            const text = li.innerText.trim();
            if (text.length < 5) return;

            const impactRegex = /\d+%|[\$\£\€]\d+|\d+[\$\£\€]|\d+[x\+]|\b([1-9]|[1-9]\d|[1-9]\d\d)\b|\b(one|two|three|four|five|six|seven|eight|nine|ten|twenty|thirty|forty|fifty|sixty|seventy|eighty|ninety|hundred|thousand)\b/i;
            let cleanText = text.replace(/\b(19|20)\d{2}\b/g, '');
            cleanText = cleanText.replace(/\b\d{3}[-.\s]?\d{3}[-.\s]?\d{4}\b/g, '');

            if (impactRegex.test(cleanText)) {
                this.metrics.quantifiedBullets++;
            }

            const firstWords = text.split(/\s+/).slice(0, 3).map(w => w.toLowerCase().replace(/[^a-z]/g, ''));
            let foundVerb = false;
            let actualVerb = "";
            for (let w of firstWords) {
                if (this.strongVerbs.has(w)) {
                    foundVerb = true;
                    actualVerb = w;
                    break;
                }
            }

            if (foundVerb) {
                this.metrics.actionVerbs++;
                firstWordsUsed[actualVerb] = (firstWordsUsed[actualVerb] || 0) + 1;
            }

            if (text.split(' ').length > 40) {
                this.warnings.push({ title: "Run-on Bullet Point", msg: "Keep bullets under 30 words." });
            }
        });

        // Repetition check
        for (const [word, count] of Object.entries(firstWordsUsed)) {
            if (count > 2) {
                this.warnings.push({ title: "Repetitive Action Verbs", msg: `You started ${count} bullet points with "${word}". Try diversifying your vocabulary.` });
            }
        }

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
        this.calculateTenure();

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

        let evidenceText = "";
        const expSections = document.querySelectorAll('[data-type="experience"], .experience, .work');
        if (expSections.length > 0) {
            expSections.forEach(sec => evidenceText += " " + sec.innerText.toLowerCase());
        } else {
            let bodyClone = document.body.cloneNode(true);
            const skillNodes = bodyClone.querySelectorAll('.skill-sub, [data-type="skills"], .skill-group');
            skillNodes.forEach(n => n.remove());
            evidenceText = bodyClone.innerText.toLowerCase();
        }

        let unproven = [];

        skills.forEach(skill => {
            // Escape special regex characters like in C++ or C#
            const escapedSkill = skill.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
            // Use word boundary, but account for non-word trailing characters like '++' or '#'
            const regex = new RegExp(`(?:^|\\W)${escapedSkill}(?:$|\\W)`, 'i');
            
            if (!regex.test(evidenceText)) unproven.push(skill);
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

    calculateTenure() {
        let expText = "";
        const expSections = document.querySelectorAll('[data-type="experience"], .experience, .work');
        if (expSections.length > 0) {
            expSections.forEach(sec => expText += " " + sec.innerText);
        } else {
            expText = document.body.innerText; // Fallback
        }

        const dateRegex = /(?:(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\s+)?(?:0?[1-9]|1[0-2])?(?:\/)?\s*(19\d{2}|20\d{2})\s*(?:-|–|to)\s*(?:(?:(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\s+)?(?:0?[1-9]|1[0-2])?(?:\/)?\s*(19\d{2}|20\d{2})|Present|Current|Now)/gi;
        
        let match;
        let intervals = [];

        while ((match = dateRegex.exec(expText)) !== null) {
            let str = match[0].toLowerCase();
            let parts = str.split(/-|–|to/).map(s => s.trim());
            if (parts.length < 2) continue;

            let startStr = parts[0];
            let endStr = parts[1];

            let startYearMatch = startStr.match(/(19|20)\d{2}/);
            let endYearMatch = endStr.match(/(19|20)\d{2}/);

            if (!startYearMatch) continue;

            let startYear = parseInt(startYearMatch[0]);
            let startMonth = 0; 
            const months = ['jan','feb','mar','apr','may','jun','jul','aug','sep','oct','nov','dec'];
            for(let i=0; i<12; i++) {
                if(startStr.includes(months[i])) { startMonth = i; break; }
            }
            let startNum = startYear + (startMonth / 12);

            let endNum = 0;
            if (endStr.includes('present') || endStr.includes('current') || endStr.includes('now')) {
                let now = new Date();
                endNum = now.getFullYear() + (now.getMonth() / 12);
            } else if (endYearMatch) {
                let endYear = parseInt(endYearMatch[0]);
                let endMonth = 11; // Default to Dec
                for(let i=0; i<12; i++) {
                    if(endStr.includes(months[i])) { endMonth = i; break; }
                }
                endNum = endYear + (endMonth / 12);
            } else {
                continue;
            }

            if (startNum <= endNum) {
                intervals.push([startNum, endNum]);
            }
        }

        if (intervals.length === 0) {
            this.metrics.yearsOfExperience = 0;
            this.issues.push({ title: "No Dates Found", msg: "ATS could not parse any career dates. Ensure you use standard formats like 'Jan 2018 - Present' or '2018 - 2020'."});
            return;
        }

        intervals.sort((a, b) => a[0] - b[0]);

        let merged = [intervals[0]];
        for (let i = 1; i < intervals.length; i++) {
            let last = merged[merged.length - 1];
            let curr = intervals[i];
            if (curr[0] <= last[1]) {
                last[1] = Math.max(last[1], curr[1]);
            } else {
                merged.push(curr);
            }
        }

        let totalYears = 0;
        merged.forEach(interval => {
            totalYears += (interval[1] - interval[0]);
        });

        this.metrics.yearsOfExperience = Math.round(totalYears * 10) / 10;

        if (this.metrics.yearsOfExperience > 0) {
            this.successes.push({ title: "Timeline Parsed", msg: `Successfully calculated ~${this.metrics.yearsOfExperience} Total Years of Experience.` });
        }
    }

    analyzeJD(jdText) {
        if (!jdText || jdText.length < 20) return;

        const stopWords = new Set(['and', 'the', 'to', 'of', 'a', 'in', 'for', 'with', 'on', 'at', 'from', 'by', 'an', 'be', 'as', 'or', 'are', 'this', 'that', 'it', 'is', 'was', 'will', 'can', 'have', 'has', 'had', 'but', 'not', 'which', 'what', 'when', 'where', 'who', 'whom', 'why', 'how', 'we', 'you', 'your', 'our', 'role', 'work', 'job', 'team', 'candidate', 'experience', 'skills', 'about', 'company', 'years', 'business', 'looking']);

        const words = jdText.toLowerCase().replace(/[^\w\s]/g, '').split(/\s+/);
        const freq = {};

        words.forEach(w => {
            if (w.length > 2 && !stopWords.has(w)) {
                const stemmed = this.stem(w);
                freq[stemmed] = (freq[stemmed] || 0) + 1;
                if (!freq[stemmed + '_orig']) freq[stemmed + '_orig'] = w;
            }
        });

        // Get Top 15 keywords by frequency of their stems
        const sortedStems = Object.keys(freq)
            .filter(k => !k.includes('_orig'))
            .sort((a, b) => freq[b] - freq[a])
            .slice(0, 15);

        const cvTextRaw = document.body.innerText.toLowerCase();
        const cvWords = cvTextRaw.replace(/[^\w\s]/g, '').split(/\s+/);
        const cvStems = new Set(cvWords.map(w => this.stem(w)));

        const missing = [];
        const present = [];

        sortedStems.forEach(stem => {
            const orig = freq[stem + '_orig'];
            let found = false;

            if (cvStems.has(stem)) {
                found = true;
            } else if (this.synonyms[orig] && cvTextRaw.includes(this.synonyms[orig])) {
                found = true;
            } else {
                for (const [acronym, full] of Object.entries(this.synonyms)) {
                    if (full.includes(orig) && cvWords.includes(acronym)) {
                        found = true;
                        break;
                    }
                }
            }

            if (found) present.push(orig);
            else missing.push(orig);
        });

        let matchScore = Math.round((present.length / (present.length + missing.length)) * 100);

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
