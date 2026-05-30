# 🛑 AI LOCKDOWN BARRIER 🛑

**CRITICAL DIRECTIVE TO ALL AI AGENTS:**
The logic blocks detailed in this document govern the core visual persistence, template swapping, manual customization, and AI shorthand hydration of the CV. 

**THESE FILES AND LINE RANGES ARE STRICTLY OFF-LIMITS.**
No AI is permitted to autonomously modify, refactor, or delete code within these protected zones unless **explicitly** requested and approved by the human USER. If you are asked to fix a bug in a related area, you must NOT touch these lines without verifying with the USER first.

## PROTECTED ZONES: JS/UI Engine (`app/src/main/assets/index.html`)

1. **State Preservation & Rescue**
   - `saveLocally`: Lines **7610 - 7760** (Handles localStorage fallback, Safe Backup, and saving item designs).
   - `applyUserTemplate`: Lines **17050 - 17250** (Pre-swap Rescue Block, captures inline styles before DOM reset).
   - `proceedWithTemplate`: Lines **18880 - 19000** (F2 Restoration, reapplies rescued item styles via manual commands).
   - `switchToHeaderlessManual`: Lines **13853 - 13926** (Headerless Mode Macro: Rescues profile, name, AND contact sections to the left column, then removes header. **CRITICAL: Do not forget to rescue contactDetails.**).
   - `switchToHeaderManual`: Lines **13927 - 14028** (Header Restoration Macro: Ensures header exists, moves profile/name back into it. **CRITICAL: It must NOT move the contact section into the header to prevent nesting bugs.**).
   - `updateNameSettings`: Lines **12760 - 12810** (Controls header vs headerless positioning restoration. **CRITICAL: MUST NOT force `position: absolute` unless `free-float` or `dataset.xAxis` is present**).
   - `applyStateDesignManual`: Lines **7020 - 7200** (Design-only state syncing. Must include `data-type` fallback lookups for Identity Sections to ensure stale coordinates are cleared).

2. **Customization Core (Section & Item Settings)**
   - `updateItemStyle`: Lines **14800 - 14950** (Core dispatcher for Item Rounding, Item Background, Line Height).
   - `updateSectionHeaderStyle` / `applySavedCustomStyle`: Lines **15750 - 15950** (Custom Header Styles).
   - `applySettingsToAllSections`: Lines **16900 - 17050** (Grid background, alignment, and "Apply to All" replication logic).
   - `processAICommands`: Lines **7478 - 7720** (JS-Side Hydration and Live Update. Uses `androidAddSection` (Manual Command) to summon missing sections. Includes `data-type` element fallbacks and `aiLog` routing to `AI_Generation` tab. **CRITICAL: MUST NEVER be replaced with raw innerHTML or loadResumeData for AI generation — all section creation must flow through androidAddSection.**).

3. **Performance & Bridge Protections**
   - `triggerAutoSave`: Lines **6290 - 6350** (Debounce logic and deferred `refreshHeaderVisibility` to prevent layout thrashing).
   - `updateLeftFrameConfig`: Lines **8580 - 8650** (Sanitizes "transparent" poison to prevent Java crashes).

4. **Data Extraction & Native Wizard Sync (Existing CVs)**
   - `exportStructuredData` (Lines **6433 - 6612** in `index.html`): Universal data-key-driven extraction engine. Protect `isBlackColor`, `getComputedStyle(sec)` references, and the `ITEM_SELECTOR` constant to prevent invisible crashes and sync drift.
   - `getResumeData` (Lines **6390 - 6430** in `index.html`): Bridge method that catches crashes and sends extracted CV state to Java MSW.
   - `syncDataFromWebViewAndShowSections` (in `CVWizardManager.java`): Responsible for safely deserializing web data and syncing to the wizard without causing empty states. **Protect the "Targeted Refresh" logic (`pendingAddItemSectionId`)** that avoids nuclear ViewPager rebuilds and scroll resets when adding new items.
   - `addItemToSection` (Lines **9798 - 9870** in `index.html`): Protect the `initialBuild` logic that enforces uneditable labels for default items like Test Scores and Visa Status while keeping them editable for newly added items.

## PROTECTED ZONES: Java Native (`app/src/main/java/com/example/myapplication/ResumeDataManager.java` & `MainActivity.java`)

1. **AI Data Parser & Shorthand Logic**
   - `parseAndMergeJson`: Lines **279 - 354** (Merges AI command data).
   - `parseShorthandJson`: Lines **524 - 783** (Converts AI shorthand like `per`, `hdr`).
   - `SHORTHAND_MAP`: Lines **458 - 502** (AI key mapping).
   - `serializeSections`: Line **1050** (Public method. Converts `List<SectionModel>` → `JSONArray` for direct dispatch to `processAICommands`. **CRITICAL: This MUST remain public. It is the bridge between Java parsing and the JS Manual Command System.**).

2. **Manual Command Pipeline (`MainActivity.java` — `handleApplyCode`)**

   > ⚠️ **WARNING:** `handleApplyCode` (Lines **10511 - 10670**) is the AI generation dispatcher. It MUST route ALL section creation through `processAICommands` → `androidAddSection` (the Manual Command System). **It must NEVER call `generateStateJson` or `loadResumeData` for AI generation.** Those functions build raw HTML that bypasses templates, listeners, drag handles, and design tokens — destroying the Manual Command System.

   - `handleApplyCode`: Lines **10511 - 10670** (Parses input, runs surgical checks, serializes via `serializeSections`, dispatches to `processAICommands`).
   - The pipeline is: `input → parseAndMergeJson → serializeSections → base64 → processAICommands → androidAddSection → hydrate fields`.

3. **CV Generation & HTML Rendering (LEGACY — NOT used by AI pipeline)**
   - `generateStateJson`: Lines **1081 - 1265** (Produces raw HTML string. **Used only by non-AI paths. Must NOT be wired into handleApplyCode.**).
   - `generateItemHTML`: Lines **1285 - 1560** (Rendering logic for every section type).

4. **Bridge Safety (`MainActivity.java`)**
   - `showFrameSettingsDialog`: Lines **9180 - 9250** (Uses `autoSetViewColor` to prevent `IllegalArgumentException` from invalid CSS strings).

## PROTECTED ZONES: Manual Command Section Creation (`index.html`)

> ⚠️ **WARNING:** These functions are the ONLY correct way to add sections to the CV. They create sections with proper HTML templates, drag handles, toolbar hooks, edit listeners, and auto-save bindings. Any code that bypasses these functions (e.g., raw `innerHTML` injection) will produce "dead" sections without interactivity.

1. `androidAddSection`: Lines **13388 - 13433** (Manual Command entry point. Finds section in `ALL_SECTIONS`, handles special/duplicate logic, delegates to `addSectionFromPanel`).
2. `addSectionFromPanel`: Lines **13102 - 13385** (Core section creator. Uses `ITEM_TEMPLATES`, handles column placement, enables drag, attaches toolbar click handlers, triggers auto-save).
3. `ALL_SECTIONS`: Lines **12959 - 13002** (Master registry of all section definitions with IDs, types, icons, default columns, and groups).

## PROTECTED ZONES: AI Communication
- `ManualAIActivity.java`: Entire file (Handles user-facing AI input dialog).
- `LocalCommandParser.java`: Entire file (Pre-processes commands).

## PROTECTED ZONES: MSW Focus Magnification & Highlight Engine

> ⚠️ **WARNING:** The functions below control the automatic zoom-in/zoom-out and field highlighting that fires when a user focuses on an EditText in the Manage Section Wizard. Modifying the `transform: scale()` logic, the adaptive width ratio calculation, or the section outline styles will break the editing experience. **CSS `zoom` must NEVER be used here** — it causes layout reflow and shrinks section widths. Only `transform: scale()` is safe (purely visual, no layout changes).

1. **JS/UI Engine (`app/src/main/assets/index.html`)**
   - `autoFocusSection`: Lines **13351 - 13366** (Scrolls WebView to section center + temporary blue outline pulse).
   - `clearWizardHighlights`: Lines **13369 - 13385** (Global reset: clears all section outlines, field highlights, and resets `transform` magnification back to 1x).
   - `highlightField`: Lines **13388 - 13494** (Core engine: locates the target field via `itemIndex + fieldKey` → `data-key` → `fieldIndex` fallback chain, applies orange section outline + blue field pulse, calculates adaptive magnification from element width ratio, and scrolls into view).

2. **Java Native Bridge (`app/src/main/java/com/example/myapplication/CVWizardManager.java`)**
   - `createStyledEdit` → `onFocusChangeListener`: Lines **1947 - 1993** (Triggers `autoFocusSection` and `highlightField` in the WebView when a user taps an EditText in the wizard panel).
   - `closeWizard` → `clearWizardHighlights` call: Line **895** (Resets all zoom/highlight state when the MSW panel is dismissed).

## PROTECTED ZONES: SECTION TOOLBAR & REORDERING LOGIC

> ⚠️ **WARNING:** The following blocks control the section management toolbar (Up, Down, Swap, Delete). Modifying the long-press reordering, the Native-to-JS jump bridge, or the column swapping logic will break CV structural management.

1. **JS/UI Engine (`app/src/main/assets/index.html`)**
   - `manualJumpToLimit`: Lines **9929 - 9969** (Native Bridge: Handles "Extreme Move" top/bottom jumps from Java toolbar).
   - `showSectionToolbar`: Lines **11751 - 11850** (HTML-side toolbar UI and section duplication).
   - `setupToolbarDrag`: Lines **11897 - 12050** (Unified drag/click handler with long-press "Extreme Move" for HTML toolbar).

2. **Java Native (`app/src/main/java/com/example/myapplication/MainActivity.java`)**
   - `showToolbar`: Lines **3527 - 3610** (Displays the native reordering panel).
   - `configureToolbarButtons`: Lines **833 - 972** (Sets up Up/Down/Swap/Copy listeners).
   - `setupNativeSlideMode`: Lines **974 - 1050** (Long-press detection that triggers `manualJumpToLimit`).

## PROTECTED ZONES: PERSONAL & CONTACT DETAILS (MSW & WebView)

> ⚠️ **WARNING:** The following blocks govern the "Elastic Row" / Horizontal Pair rendering of Personal and Contact Details in both the editor and the Wizard. Modifying the `isHorizontalPair` logic or the `pd-row` styles will break the alignment and focus-grow behavior of custom details.

1. **JS/UI Engine (`app/src/main/assets/index.html`)**
   - `ITEM_TEMPLATES['personal']` & `ITEM_TEMPLATES['contact']`: Lines **5627 - 5680** (Core HTML structure for contact/personal items).
   - `.pd-row`, `.pd-label`, `.pd-val` CSS: Lines **1740 - 1769** (Base layout for personal details).
   - `processAICommands` Personal Special Case: Lines **7590 - 7630** (Hydrates multiple 'k' and 'v' item models into distinct side-by-side rows. **CRITICAL: Must loop over `secData.items` and extract `k` and `v` together to avoid collapsing the entire section into one label.**)

2. **Java Native Bridge (`app/src/main/java/com/example/myapplication/CVWizardManager.java` & `ResumeDataManager.java`)**
   - `ResumeDataManager.parseStructuredJson`: Lines **710 - 810** (Parses incoming shorthand JSON arrays/objects. **CRITICAL: For 'personal' and 'contact', it must bundle `k` and `v` fields into a single `ItemModel` instead of mapping them sequentially, otherwise rows will visually split.**)
   - `CVWizardManager.isHorizontalPair` Logic: Lines **1776 - 1840** (Handles horizontal k/v pairs, dynamic focus-grow weights, and keyboard navigation between fields in the MSW).

## PROTECTED ZONES: AI BUTTON (FAB) — Layout & Java Wiring

> ⚠️ **WARNING:** The AI button (`fab_ai`) was previously commented out in both the XML layout and Java code. It has been restored. Do NOT re-comment or hide it without explicit USER approval.

1. **XML Layout (`app/src/main/res/layout/activity_main.xml`)**
   - `fab_ai` FrameLayout: Lines **72 - 75** (The AI button in the `add_edit_container` stack. Uses `@drawable/ic_ai_custom` as icon and `@string/ai_assistant` as content description. Has wobbly_slime animation and haptic feedback.)
   - `add_edit_container` dimension ratio: Line **70** (`app:layout_constraintDimensionRatio="1:4.5"`). **CRITICAL: This was changed from `1:3.5` to `1:4.5` to accommodate 4 buttons (AI, Wizard, Edit, Print/Add). Reverting to `1:3.5` will squish all buttons.**

2. **Java Native (`app/src/main/java/com/example/myapplication/MainActivity.java`)**
   - Field declarations: Line **175** (`fabAi` in the View declaration) and Line **177** (`btnAi` in the ImageButton declaration). **CRITICAL: These were previously commented out with `/* */`. They MUST remain uncommented.**
   - `setupOriginalButtons` wiring: Lines **2591 - 2596** (Finds `fab_ai` by ID, extracts `btn_ai` ImageButton, sets click listener to `showAiBox()`, attaches wobbly touch animation). **CRITICAL: This block was previously commented out with `/* */`. It MUST remain uncommented.**
   - `showAiBox`: Line **10377** (The method invoked when the AI button is tapped. Opens the AI input interface.)
## PROTECTED ZONES: SERVER-SIDE SECURITY & MONETIZATION PIPELINE

> 🛑 **CRITICAL SECURITY ARCHITECTURE:** The subscription and tier system uses a **Server-Authoritative Tier Model**. The server (Cloudflare Worker) is the single source of truth for user tiers. The client NEVER sets its own tier — it only receives tier from the server after cryptographic verification. **Any code that allows the client to self-assign a tier bypasses purchase verification and constitutes a security vulnerability.**

### 1. Backend — RSA Signature Verification (`vitae-backend/src/index.js`)

   - `PLAY_RSA_PUBLIC_KEY`: Line **4** (Google Play Console RSA public key for purchase signature verification. **CRITICAL: This is a public key — safe to embed — but MUST NOT be replaced or removed.**)
   - `verifyPlaySignature`: Lines **6 - 27** (Core RSA verification using Web Crypto API. Verifies Google's RSASSA-PKCS1-v1_5 SHA-1 signature on the purchase JSON. **CRITICAL: MUST NOT be weakened, bypassed, or stubbed out.**)
   - `PRODUCT_TIER_MAP`: Lines **30 - 35** (Maps Google Play product IDs to tier names. **CRITICAL: Only these product IDs are valid. Adding arbitrary mappings bypasses billing.**)

### 2. Backend — Server-Authoritative Tier Endpoints (`vitae-backend/src/index.js`)

   - `POST /api/subscriptions/verify`: Lines **238 - 277** (Accepts `uid`, `originalJson`, `signature` from client → calls `verifyPlaySignature` → if valid, updates tier in D1 DB. **CRITICAL: This is the ONLY path to grant a paid tier from a purchase. It MUST verify the signature before granting.**)
   - `POST /api/users/sync`: Lines **207 - 232** (Syncs user metadata. **CRITICAL: Does NOT accept `tier` from client. Tier column is NOT in the ON CONFLICT UPDATE clause. Returns the server-authoritative tier so the client can sync locally.**)
   - `GET /api/users/:uid/tier`: Lines **283 - 293** (Returns server-authoritative tier for a user. Read-only.)
   - `POST /api/coupons/verify`: Lines **165 - 201** (Server-side coupon verification. Codes stored in `env.VALID_COUPONS` secret — **CRITICAL: Coupons were previously hardcoded in source code. They are now in Wrangler secrets. MUST NOT be moved back to source.**)

### 3. Backend — Admin Auth & Safety (`vitae-backend/src/index.js`)

   - Admin Auth Gate: Lines **62 - 76** (All `/api/admin/*` routes require `Authorization: Bearer ${ADMIN_SECRET}` header. Secret loaded from `env.ADMIN_SECRET`.)
   - CORS Restriction: Lines **43 - 56** (Only allows `"null"` origin = Android WebView `file://`. Native HTTP clients don't send Origin.)
   - Storage Safeguard: Lines **126 - 139** (950MB template storage cap to prevent D1 overflow.)
   - Error Sanitization: Lines **586 - 590** (Global catch block redacts internal error details from client responses. **CRITICAL: MUST NOT expose `e.message` to clients.**)

### 4. Backend — AI Proxy & Quota (`vitae-backend/src/index.js`)

   - `POST /api/ai/chat`: Lines **532 - 582** (Proxies to Google Gemini API with 1,000-request daily global quota tracked in D1 `AiUsage` table. **CRITICAL: `GEMINI_API_KEY` is in env secrets. MUST NOT be exposed to client.**)

### 5. Client — Server-Side Purchase Verification (`SubscriptionActivity.java`)

   - `applyPurchaseEffect`: Lines **421 - 478** (Sends `purchase.getOriginalJson()` + `purchase.getSignature()` to `/api/subscriptions/verify`. Only applies tier via `tierManager.setTierFromServer()` if server returns `success: true`. **CRITICAL: MUST NOT call `tierManager.setTier()` directly — that would bypass server verification.**)
   - `checkSubscriptionStatus`: Lines **313 - 353** (On billing connect, queries active Play Store purchases and re-verifies the best one server-side. Prevents stale local tiers.)
   - `handlePurchase`: Lines **399 - 419** (Acknowledges purchase then routes to `applyPurchaseEffect` for server verification.)

### 6. Client — Server-Authoritative Tier Sync (`UserTierManager.java`)

   - `syncToCloudflare`: Lines **200 - 252** (Sends user metadata to `/api/users/sync`. **CRITICAL: Does NOT send `tier` in the request body (line 216 comment). Reads authoritative tier from server response and updates local prefs only if different.**)
   - `setTierFromServer`: Lines **126 - 128** (Sets tier locally WITHOUT triggering cloud sync — prevents infinite sync loop. **CRITICAL: This is the only safe way to apply a server-verified tier locally.**)
   - `setTier`: Lines **117 - 120** (Sets tier AND triggers `syncUserToCloud()`. Used only for coupon flow and legacy paths.)

### 7. Client — Server-Side Coupon Verification (`SubscriptionActivity.java`)

   - `applyCoupon`: Lines **586 - 661** (Sends coupon code + uid to `/api/coupons/verify`. Server validates against `env.VALID_COUPONS` secret, updates DB tier, returns new tier. **CRITICAL: Coupon validation is 100% server-side. MUST NOT add client-side coupon maps.**)

---
**Status:** STABLE (AI Button Restored, Container Ratio Adjusted, Crash & Summary Sync Resolved)
**Last Verified:** May 22, 2026 (Fixed ClassCastException in showAiBox; resolved MSW summary/declaration data reset bug by introducing specific 'p' element targeting in index.html to protect DOM hierarchy during real-time sync)
