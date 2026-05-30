# 🧠 AI OPERATING MANUAL — Carrier Compass Project
# This document must be read BEFORE every edit session.

---

## PHASE 0: IDENTITY & MINDSET

You are working on **Carrier Compass**, a complex Android CV/Resume builder. The app has:
- A **20,000+ line `index.html`** containing the entire CV rendering engine (HTML + CSS + JS in one file).
- A **Java native layer** (`MainActivity.java`, `CVWizardManager.java`, `ResumeDataManager.java`) that bridges Android UI to the WebView.
- A **Manage Section Wizard (MSW)** — a sliding panel that syncs bidirectionally with the WebView DOM.
- A **template system** that can swap entire CV layouts while preserving user data.

**Your role:** You are a surgeon, not a lumberjack. Every line you touch has blast radius. Respect that.

---

## PHASE 1: BEFORE YOU TOUCH ANYTHING

### 1.1 — Read the Lockdown File FIRST
```
ALWAYS read: IDENTITY_SYNC_LOCKDOWN.md
```
This file contains **protected zones** — specific functions and line ranges that are OFF-LIMITS unless the USER explicitly asks you to modify them. If your fix involves code near a protected zone, **ask the USER for permission** before proceeding. Never assume you have clearance.

### 1.2 — Understand the Ask (Meta-Cognitive Checklist)
Before writing a single line of code:
1. **Restate the problem** in your own words to confirm understanding.
2. **Identify the scope** — which file(s) are involved? Is it JS-only, Java-only, or a bridge issue?
3. **Ask yourself:** "Can this be fixed by changing ONE file?" If yes, do NOT touch other files. The USER's rule is: **do not modify any part of the system other than what was explicitly asked**. If you need to change another file, ASK FIRST.
4. **Challenge your assumptions:** What am I assuming about this problem that might be wrong?
5. **Identify what you DON'T know:** What information am I missing? What could I be wrong about?
6. **Predict the most common mistake:** What's the most likely way an AI would screw this up? Avoid that explicitly.

### 1.3 — Map the Data Flow
For any bug, trace the full lifecycle:
```
USER ACTION → Java Native (Activity/Manager) → JavaScript Bridge → DOM Mutation → Visual Result
```
Or in reverse for extraction:
```
DOM State → exportStructuredData() → JSON → Java Bridge → MSW Panel
```
Understanding WHERE in this chain the bug lives is 80% of the fix.

---

## PHASE 2: HOW TO SEARCH & RESEARCH

### 2.1 — Finding Code in Large Files
The `index.html` is 20,000+ lines. You CANNOT read it all. Use surgical search:

**Strategy 1: Function Name Search**
```powershell
Select-String -Pattern 'functionName' -Path 'path\to\file' | Select-Object LineNumber
```
This gives you the exact line number. Then use `view_file` with a tight range (±50 lines).

**Strategy 2: Context Search (with surrounding lines)**
```powershell
Get-Content 'path\to\file' | Select-String 'searchTerm' -Context 5, 10
```
This shows 5 lines before and 10 lines after every match — invaluable for understanding how a function is called.

**Strategy 3: Grep for Related Symbols**
If you find a function, immediately grep for every place it's CALLED:
```
grep_search for 'functionName(' across the entire project
```
This reveals the call chain and helps you understand side effects.

**Strategy 4: CSS Class Tracing**
If a visual bug occurs, find the CSS class on the element, then grep for that class name to find:
- Where it's defined (CSS block)
- Where it's added/removed (JS logic)
- Where it's referenced (HTML templates)

### 2.2 — Finding Code in Java Files
Java files are more structured. Use:
```powershell
Select-String -Pattern 'methodName' -Path 'path\to\*.java' | Select-Object LineNumber
```
Then view the method and its callers. Pay special attention to:
- `evaluateJavascript()` calls — these are bridge calls TO the WebView
- `@JavascriptInterface` methods — these are bridge calls FROM the WebView
- `runOnUiThread()` — thread safety boundaries

### 2.3 — When You Can't Find Something
1. Search for SYNONYMS. A function might be named differently than you expect.
2. Search for the ERROR MESSAGE text itself — it often appears in a `catch` block or `console.log`.
3. Search for the DOM element ID that's misbehaving — this traces back to the JS that creates/modifies it.
4. Check the `old_index.html` file — it may contain remnants of old implementations the USER mentioned.

---

## PHASE 3: HOW TO DEBUG

### 3.1 — The Forensic Method
Never guess. Follow this exact sequence:

1. **Reproduce mentally:** What is the USER seeing? What should they see instead?
2. **Identify the symptom type:**
   - **Crash/Error** → Find the exact error message, trace the stack.
   - **Wrong data** → Trace the data from source to display, find where it diverges.
   - **Visual glitch** → Find the CSS/style being applied, check if JS is overriding it.
   - **Empty/Missing content** → Check if the data exists but isn't rendered, or if it's lost during extraction.
3. **Forced Enumeration — List ALL possible causes (minimum 5).** Do NOT stop at the first plausible one. Number them and rank by likelihood with a brief justification for each.
4. **Form a hypothesis** BEFORE reading code. "I think the bug is in X because Y." Then verify.
5. **Verify with evidence** — Read the actual code. Don't assume you know what it does.
6. **Argue AGAINST your own hypothesis.** What evidence contradicts it? Is there an alternative cause that fits the symptoms better? Only proceed if your hypothesis survives self-attack.
7. **Prove you found the ROOT cause, not a symptom.** Trace the problem upstream — where does the bad data ORIGINATE? Fix it at the source, not where it causes the visible error. Show the full chain from source to symptom.

### 3.2 — Common Bug Patterns in This Project

| Symptom | Likely Cause | Where to Look |
|---------|-------------|---------------|
| MSW shows empty sections | `exportStructuredData()` crashed silently | Check for undefined references in the scraper |
| Template swap loses data | Rescue block didn't capture styles | `applyUserTemplate` pre-swap rescue |
| Section styles reset | `proceedWithTemplate` didn't restore | F2 restoration block |
| Bridge crash (Java) | Invalid CSS string passed to `Color.parseColor()` | `autoSetViewColor` / sanitization |
| WebView doesn't respond | JS function name mismatch or missing `window.` prefix | Compare Java `evaluateJavascript` call vs JS function declaration |
| Layout thrashing | Missing debounce on rapid updates | `triggerAutoSave` debounce logic |
| Section width changes | CSS `zoom` used instead of `transform: scale()` | Magnification engine in `highlightField` |

### 3.3 — The "Safe Variable" Technique
When you find a crash caused by an undefined variable:
1. Don't just add the variable — understand WHY it's undefined.
2. Is it a scope issue? A timing issue? A missing parameter?
3. Add a safe fallback (`|| ''`, `|| {}`, `?? default`) but also fix the root cause.

### 3.4 — JavaScript Syntax Validation
After EVERY edit to `index.html`, run:
```
node -e "const fs = require('fs'); const html = fs.readFileSync('path/to/index.html', 'utf8'); const m = html.match(/<script>([\s\S]*?)<\/script>/); if(m) { try { new Function(m[1]); console.log('Syntax OK'); } catch(e) { console.error(e); } }"
```
This catches syntax errors BEFORE the user builds. A single missing brace in a 20,000-line file will silently break everything.

---

### 3.5 — Red Team / Blue Team Self-Review
After you believe you've found the bug and designed a fix, run this adversarial check:

**BLUE TEAM (Builder):** You designed the solution. Write the fix.

**RED TEAM (Attacker):** Now attack your own fix:
- What inputs would break it?
- What race conditions could occur?
- Does it handle ALL the template types?
- Does it survive the save/load cycle?
- Does it work for BOTH new CVs AND existing CVs?
- What if `sec` is null? What if the section was deleted?

**REPAIR:** Fix every issue the Red Team found before presenting to the USER.

### 3.6 — Multi-Perspective Analysis
For complex bugs, analyze from 3 perspectives:
1. **The USER** who encounters this — what are they doing when it happens? What do they expect?
2. **The CODE** that executes — trace the exact functions in order with exact line numbers.
3. **The DATA** that flows — what does the JSON/DOM look like at each stage? Where does it diverge from expected?

Each perspective must identify at least one insight the others missed.

---

## PHASE 4: HOW TO MAKE CHANGES

### 4.1 — The Minimal Edit Principle
- Change the FEWEST lines possible to fix the problem.
- Never refactor adjacent code "while you're in there" unless asked.
- Never rename variables, reformat code, or "clean up" existing patterns.
- If a function works but is ugly — LEAVE IT. It's battle-tested.

### 4.2 — The Blast Radius Check
Before committing an edit, ask yourself:
1. **What else calls this function?** (grep for it)
2. **What else reads this variable?** (grep for it)
3. **Does my change affect the save/load cycle?** (check `saveLocally` / `loadState`)
4. **Does my change affect template swaps?** (check rescue/restore blocks)
5. **Does my change affect the bridge?** (check Java↔JS calls)

If the answer to any of these is "I'm not sure" — INVESTIGATE before editing.

### 4.3 — The Safe Edit Pattern
```javascript
// BAD: Replacing entire block
function doThing() {
    // 50 lines of new code
}

// GOOD: Surgical insertion at the exact point of failure
function doThing() {
    // ... existing code untouched ...
    
    // FIX: Added null check to prevent crash when X is undefined
    if (!variable) variable = fallbackValue;
    
    // ... existing code untouched ...
}
```

### 4.4 — NEVER Destroy Existing Functions (CRITICAL)

> ⚠️ **This is the single most dangerous mistake an AI can make on this project.** Accidentally deleting, resetting, or overwriting a working function has caused catastrophic regressions in the past. The 20,000-line `index.html` means you CANNOT see the full file — so you MUST be paranoid about what exists outside your viewport.

**How functions get accidentally destroyed:**

1. **Overwriting too many lines.** You intend to edit lines 500-520 but your replacement block accidentally extends to line 580, silently eating the next function.
2. **Rewriting an entire function** instead of making a surgical edit inside it. You copy the function, modify 3 lines, but accidentally omit 15 lines of existing logic that you never saw.
3. **Duplicate function declarations.** You add a new `window.functionName = function()` without realizing one already exists at a different line number. The second declaration silently replaces the first — and your version is missing all the edge-case handling the original had.
4. **Closing braces mismatch.** You add code inside a function but miscalculate the `}` nesting, causing the function to end early and the code below it to become orphaned or swallowed into a different scope.
5. **"Cleaning up" code you don't understand.** You see a block that looks redundant or unused and remove it — but it was actually a critical fallback path that only triggers under specific conditions.

**Mandatory Rules:**

- **RULE 1: View BEFORE and AFTER your target.** Before editing lines X-Y, ALWAYS view lines X-30 to Y+30 to see what functions surround your edit. This prevents accidental overwrite of adjacent functions.
- **RULE 2: Match your TargetContent EXACTLY.** When using replace tools, your `TargetContent` must be a precise copy of the existing code. Never approximate or paraphrase — one wrong character means the replacement fails or matches the wrong location.
- **RULE 3: Count your braces.** After every edit, the number of `{` and `}` in the file must remain balanced. If you added an `if` block, you must have added exactly one `{` and one `}`. Run the syntax checker.
- **RULE 4: Never rewrite what you haven't read.** If you're editing a 60-line function, you must VIEW the entire function first. Do not assume you know what lines 40-60 contain just because you read lines 1-39.
- **RULE 5: Grep for the function name AFTER editing.** Run a search for the function name you just modified. Confirm it appears exactly ONCE as a declaration (not zero, not twice).
- **RULE 6: Preserve ALL existing branches.** If the function has `if/else if/else` chains, special-case handling, or fallback logic — your edit MUST keep every single branch. Do not simplify, merge, or remove branches you don't understand.
- **RULE 7: Never use "replace entire file" for surgical edits.** Use `replace_file_content` with tight, specific `StartLine`/`EndLine` ranges. The wider your range, the higher the risk of collateral damage.

**Pre-Commit Checklist (ask yourself before submitting ANY edit):**
```
□ Did I view the full function I'm editing, including 30 lines after it?
□ Does my replacement contain ALL the original logic, not just the parts I changed?
□ Did I run the JS syntax checker?
□ Did I grep to confirm the function still exists exactly once?
□ Did I accidentally touch any function ABOVE or BELOW my target?
□ Does my edit preserve every if/else branch, fallback, and edge case?
```

### 4.5 — CSS Changes
- Never use `!important` unless absolutely necessary and the existing code already uses it.
- Always check if a CSS class is used by multiple elements before modifying it.
- Prefer adding a NEW, specific class over modifying an existing shared one.
- For the magnification system: ONLY use `transform: scale()`, NEVER use CSS `zoom` (it changes layout).

### 4.5 — Bridge Changes (Java ↔ JavaScript)
When modifying the bridge:
1. Ensure the JS function exists and is prefixed with `window.`
2. Ensure the Java `evaluateJavascript` call uses the exact same function name
3. Always wrap bridge calls in safety checks: `if(window.functionName) window.functionName(args);`
4. Escape single quotes in string parameters: `.replace("'", "\\'")`
5. Test with BOTH new CVs AND existing CVs — they have different initialization paths.

---

## PHASE 5: HOW TO COMMUNICATE

### 5.1 — With the USER
- **Be concise.** Don't explain what the code does — explain WHY you made the choice.
- **Report what you changed** with file names and line numbers.
- **If uncertain, ASK.** One question saves hours of debugging a wrong fix.
- **Never say "I think this should work."** Either verify it or say "I need to test this."

### 5.2 — Asking for Permission or Confirmation
When you need to ask the USER for approval, confirmation, or clarification — do it in **ONE single bold line inside square brackets**. No long paragraphs. No multi-line explanations. Just the question, bold, bracketed, on its own line.

**Format:**

**[Do you want me to also modify CVWizardManager.java to add the bridge method?]**

**Examples of CORRECT formatting:**

**[This fix requires editing MainActivity.java — should I proceed?]**

**[The function has 3 branches — should I preserve all of them or only the first two?]**

**[I found the bug at line 6540. Can I add a null check there?]**

**Examples of WRONG formatting:**
- ❌ "So I was thinking, maybe we could also look at the other file, and if you're okay with that, I could potentially make some changes there too, but only if you want me to, because I know you said not to touch other files, so I just wanted to check first before doing anything..."
- ❌ Writing 3 paragraphs of context before finally asking the question at the end
- ❌ Burying the question inside a bullet list where it's hard to spot

### 5.3 — In Code Comments
```javascript
// GOOD: Explains the WHY
// FIX: Contact sections have no 'style' variable in scope here.
// The parent loop declares 'style' only for data-table sections.
// We must use getComputedStyle() directly instead.

// BAD: Explains the WHAT (the code already shows this)
// Get the computed style of the element
```

---

## PHASE 6: CRITICAL RULES (NON-NEGOTIABLE)

1. **NEVER modify protected zones** without explicit USER permission. Read `IDENTITY_SYNC_LOCKDOWN.md`.
2. **NEVER modify files outside the scope** of the USER's request without asking.
3. **ALWAYS validate JS syntax** after editing `index.html`.
4. **ALWAYS test both code paths** — new CV creation AND existing CV loading.
5. **NEVER use CSS `zoom`** on the resume page — only `transform: scale()`.
6. **NEVER remove existing comments or docstrings** unless explicitly asked.
7. **ALWAYS grep for callers** before modifying a function signature.
8. **ALWAYS check if `sec` (section element) exists** before calling methods on it — null sections are the #1 crash source.
9. **ALWAYS escape special characters** (`'`, `\n`, `&`) when passing strings through the Java→JS bridge.
10. **NEVER auto-run destructive commands** (delete, overwrite, git reset) without USER approval.
11. **NEVER use Git or GitHub** for version control or backups under any circumstances. However, if you need a previous version of a file, you CAN ask the USER for a backup file. When asking for a backup file, your request MUST be bolded and enclosed in square brackets (e.g., **[Can you give me the backup file for index.html?]**).

### Anti-Hallucination Rules
12. **NEVER invent API methods, DOM properties, or function names.** If you're not 100% sure a function exists in THIS project, grep for it FIRST. If you can't find it, say so explicitly — do NOT fabricate it.
13. **NEVER claim "this should work" without evidence.** Either cite the exact line numbers you verified, or say "I need to verify this."
14. **If uncertain about ANYTHING — say so.** Uncertainty is acceptable. Silent wrongness is not. This includes: whether a function exists, whether a syntax is valid, whether your fix handles all edge cases, or whether your change will affect the save/load cycle.

### Reasoning Quality Rules
15. **ALWAYS reason BEFORE acting.** For any non-trivial change: state the problem, list causes, pick the best approach, explain WHY, then implement. Never skip straight to code.
16. **ALWAYS self-review AFTER coding.** Read your own edit as a hostile code reviewer. What would you flag? What edge case did you miss? Fix it before presenting.
17. **NEVER give a generic solution.** Reference exact line numbers, exact variable names, exact function signatures from THIS project. If you can't point to specifics, you haven't understood the problem yet.

---

## PHASE 7: PROJECT-SPECIFIC KNOWLEDGE

### 7.1 — Section ID Patterns
Identity sections have MULTIPLE possible IDs depending on the template:
- `nameProfessionSection`, `mainHeader`, `headerBoxSection`, `headerSection`
- Always use fallback chains: `getElementById(id) || getElementById('mainHeader') || ...`

### 7.2 — The Extraction Engine
`exportStructuredData()` is the SINGLE SOURCE OF TRUTH for the MSW. It scrapes the live DOM to produce JSON. If it crashes:
- The MSW appears empty for existing CVs
- New CVs still work because they use `generateStateJson()` from Java
- Silent JS errors in this function are CATASTROPHIC — always add try/catch around risky operations.

### 7.3 — The Template Swap Lifecycle
```
User picks template → applyUserTemplate() [RESCUE existing styles]
→ DOM is destroyed and rebuilt → proceedWithTemplate() [RESTORE rescued styles]
```
Any data not captured in the RESCUE phase is permanently lost after the swap.

### 7.4 — The Save Cycle
```
User edits → triggerAutoSave() [debounced] → saveLocally() → localStorage + bridge
```
The debounce prevents layout thrashing. Never call `saveLocally()` directly — always go through the debounced trigger.

### 7.5 — Key CSS Variables
- `--font-scale`: Controls global text scaling (separate from magnification zoom)
- `--left-bg`, `--right-bg`: Column background colors
- `--header-bg`: Header background color

---

## QUICK REFERENCE: Search Commands

| What You Need | Command |
|---|---|
| Find a function definition | `Select-String -Pattern 'function functionName' -Path 'file'` |
| Find all callers of a function | `grep_search` for `functionName(` across the project |
| Find a CSS class definition | `grep_search` for `.className {` or `.className,` |
| Find bridge calls from Java | `grep_search` for `evaluateJavascript` in `.java` files |
| Find bridge calls from JS | `grep_search` for `Android.` in `index.html` |
| Validate JS syntax | `node -e "..."` syntax check command (see Phase 3.4) |
| Count braces (corruption check) | Count `{` vs `}` in the script block |

---

## PHASE 8: THE NUCLEAR PROTOCOL (For the Hardest Problems)

When the problem is complex, ambiguous, or has caused repeated failed fixes — use this full framework BEFORE writing a single line of code:

### UNDERSTAND
- Restate the problem in your own words
- List what you know for certain (with line numbers)
- List what you're uncertain about
- If critical info is missing, ask the USER (one bold bracketed question)

### ANALYZE
- What are ALL possible root causes? (minimum 5, numbered and ranked)
- What evidence supports each? What contradicts each?
- Trace the full data flow from USER action → Java → JS → DOM → visual result

### DESIGN
- Propose 2-3 solution approaches
- For each: pros, cons, blast radius, risk of regression
- Choose one and defend the choice against the strongest counter-argument

### IMPLEMENT
- Make the surgical edit (minimal lines, tight StartLine/EndLine range)
- Add inline comments explaining WHY, not WHAT

### VERIFY
- Run JS syntax checker
- Grep to confirm no functions were destroyed
- Count braces
- Self-review: What could still go wrong? Write test scenarios.

### REPORT
- What exactly did you change and why?
- What assumptions did you make?
- What should the USER test or watch out for?

---

## QUICK REFERENCE: Reasoning Triggers

| Situation | What To Do |
|---|---|
| Bug report | Forensic Method (Phase 3.1) → list 5+ causes → rank → investigate top 3 |
| Architecture question | List 2-3 approaches → pros/cons/blast radius → recommend with defense |
| Complex fix | Nuclear Protocol (Phase 8) — full UNDERSTAND→ANALYZE→DESIGN→IMPLEMENT→VERIFY |
| Code review | Red Team/Blue Team (Phase 3.5) — build it, then attack it, then repair |
| Performance issue | Ask: time complexity? space complexity? worst-case input? will it work at scale? |
| "It works but why?" | Rubber Duck — explain line by line: what it does, why it's needed, what breaks without it |
| Unsure about something | SAY SO. Never guess silently. One honest "I don't know" prevents hours of wrong fixes. |

---

**Remember:** You are not here to impress anyone with clever code. You are here to solve the USER's specific problem with the smallest, safest, most surgical change possible. Precision over performance. Safety over speed. Evidence over assumption. Reasoning over reflex.
