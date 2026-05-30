const fs = require('fs');
let code = fs.readFileSync('app/src/main/assets/index.html', 'utf8');

const funcStart = code.indexOf('window.applySettingsToAllSections = function (sourceSectionId, options) {');
if (funcStart === -1) { console.log('ERROR: function not found'); process.exit(1); }
let braceCount = 0, funcEnd = -1, inString = false, stringChar = '';
for (let i = funcStart; i < code.length; i++) {
    const ch = code[i];
    if (inString) { if (ch === stringChar && code[i-1] !== '\\') inString = false; continue; }
    if (ch === '"' || ch === "'" || ch === '`') { inString = true; stringChar = ch; continue; }
    if (ch === '{') braceCount++;
    if (ch === '}') { braceCount--; if (braceCount === 0) { let j=i+1; while(j<code.length&&(code[j]===' '||code[j]==='\r'||code[j]==='\n'))j++; funcEnd=code[j]===';'?j+1:i+1; break; } }
}

const newFunc = `window.applySettingsToAllSections = function (sourceSectionId, options) {
                const sourceSec = document.getElementById(sourceSectionId);
                if (!sourceSec) return;
                var L = window.AndroidLayoutTracker ? function(msg) { window.AndroidLayoutTracker.log("Antigravity_Diva", msg); } : function() {};

                L("=== APPLY ALL START === Source: " + sourceSectionId);
                if (options.color !== undefined && options.secBg === undefined) options.secBg = options.color;

                var cSel = '.pd-row, .data-table-item, .simple-list-item, .skill-group, .contact-item, .item, .sub-item, .declaration-text, .objective-text';
                
                // ====================================================================
                // PHASE 1: DIRECT ITEM SCRAPE
                // Since the step-by-step editor applies styles directly to items,
                // the items themselves are the ONLY reliable source of truth.
                // ====================================================================
                var itemBg = '', rounding = '', spacing = '', lineHeight = '';
                var firstItem = sourceSec.querySelector(cSel);
                
                if (firstItem) {
                    var vcs = getComputedStyle(firstItem);
                    
                    // Background Color
                    itemBg = firstItem.style.getPropertyValue('--item-bg').trim() || 
                             vcs.getPropertyValue('--item-bg').trim() || 
                             firstItem.style.backgroundColor || 
                             vcs.backgroundColor;
                             
                    // Roundness
                    rounding = firstItem.style.getPropertyValue('--card-radius').trim() || 
                               vcs.getPropertyValue('--card-radius').trim() || 
                               firstItem.style.borderRadius || 
                               vcs.borderRadius || '0px';
                    
                    // Line Space (Margin Bottom)
                    spacing = firstItem.style.marginBottom || vcs.marginBottom || '15px';
                    
                    // Line Height
                    var lh = firstItem.style.lineHeight || vcs.lineHeight || '1.4';
                    // Convert line height px to relative if possible
                    lineHeight = lh;
                }

                // If background is transparent, try catching the web-based global tag/state as fallback
                if (!itemBg || itemBg === 'transparent' || itemBg === 'rgba(0, 0, 0, 0)') {
                   var sTag = window._SECTION_STYLE_TAGS[sourceSectionId];
                   if (sTag && sTag.textContent) {
                       var m2 = sTag.textContent.match(/background-color:\\s*([^;!]+)/);
                       if (m2) itemBg = m2[1].trim();
                   }
                }

                // Clean empty/transparent defaults
                if (itemBg === 'transparent' || itemBg === 'rgba(0, 0, 0, 0)') itemBg = '';

                L("[SCRAPED ITEMS] bg=" + (itemBg||'EMPTY') + " round=" + rounding + " spacing=" + spacing + " lh=" + lineHeight);

                // Section Properties
                var rs = getComputedStyle(sourceSec);
                var sectionBg = sourceSec.style.backgroundColor || rs.backgroundColor || 'transparent';
                var headerStyle = sourceSec.getAttribute('data-header-style') || 'Default';
                var hrs = sourceSec.querySelector('h2') ? getComputedStyle(sourceSec.querySelector('h2')) : rs;
                var gridCols = rs.gridTemplateColumns && rs.gridTemplateColumns !== 'none' ? rs.gridTemplateColumns.split(' ').length : 1;
                var gridGapRaw = hrs.gap || hrs.gridGap || '15px';
                var gridGap = parseInt(gridGapRaw) || 15;
                var alignment = hrs.justifyContent || 'left';

                var targets = Array.from(document.querySelectorAll('section')).filter(function(s) { return s.id && s.id !== 'mainHeader' && s.id !== sourceSectionId; });
                
                targets.forEach(function(targetSec) {
                    var oldSecId = window._currentSettingSectionId;
                    window._currentSettingSectionId = targetSec.id;

                    // 1. Apply general section settings
                    if (options.secBg && window.previewSectionBg) window.previewSectionBg(sectionBg);
                    if (options.grid && window.previewSectionGrid) window.previewSectionGrid(gridCols, gridGap);
                    if (options.align && window.updateSectionAlignment) window.updateSectionAlignment(targetSec.id, alignment, true, true);
                    if (options.style && window.updateSectionHeaderStyle) {
                        window.updateSectionHeaderStyle(targetSec.id, headerStyle);
                        if (headerStyle === 'Custom Style' && window._scrapeSectionCustomHeaderProps && window.updateSectionHeaderCustomStyle) {
                            var props = window._scrapeSectionCustomHeaderProps(sourceSectionId);
                            if (props) window.updateSectionHeaderCustomStyle(targetSec.id, props);
                        }
                    }

                    // 2. The global style block (Kept for fallback/newly added items)
                    if (options.subRound && window.previewSectionRounding) window.previewSectionRounding(rounding);
                    if (options.subLine && window.previewSectionSpacing) window.previewSectionSpacing(spacing);
                    if (options.subLH && window.previewSectionLineHeight) window.previewSectionLineHeight(lineHeight);
                    if (options.subBg && itemBg && window.previewSectionItemBg) window.previewSectionItemBg(itemBg);

                    // 3. 🎯 EXACT STEP BY STEP EMULATION (Force Inline Override)
                    var tItems = targetSec.querySelectorAll(cSel);
                    L("[STEP-BY-STEP] Forcing on " + targetSec.id + " -> " + tItems.length + " items");
                    for (var i = 0; i < tItems.length; i++) {
                        var el = tItems[i];
                        if (options.subRound) {
                            el.style.setProperty('--card-radius', rounding);
                            if (window.getComputedStyle(el).borderRadius !== rounding) {
                                el.style.borderRadius = rounding;
                            }
                        }
                        if (options.subLine) {
                            el.style.setProperty('margin-bottom', spacing, 'important');
                        }
                        if (options.subLH) {
                            // Convert back to number if needed or set as is
                            el.style.setProperty('line-height', lineHeight, 'important');
                        }
                        if (options.subBg && itemBg) {
                            el.style.setProperty('--item-bg', itemBg);
                            // Only force background color if it's currently transparent/empty 
                            // or explicitly set. Use CSS var to respect hierarchy.
                        }
                    }

                    window._currentSettingSectionId = oldSecId;
                });

                L("=== APPLY ALL COMPLETE ===");
                triggerAutoSave();
            }`;

code = code.substring(0, funcStart) + newFunc + code.substring(funcEnd);
fs.writeFileSync('app/src/main/assets/index.html', code, 'utf8');
console.log('SUCCESS: Direct scrape inline properties forced.');
