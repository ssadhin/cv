const fs = require('fs');
let code = fs.readFileSync('app/src/main/assets/index.html', 'utf8');

const funcStart = code.indexOf('window.applySettingsToAllSections = function(sourceSectionId, options) {');
if (funcStart === -1) { console.log('ERROR: function not found'); process.exit(1); }
let braceCount = 0, funcEnd = -1, inString = false, stringChar = '';
for (let i = funcStart; i < code.length; i++) {
    const ch = code[i];
    if (inString) { if (ch === stringChar && code[i-1] !== '\\') inString = false; continue; }
    if (ch === '"' || ch === "'" || ch === '`') { inString = true; stringChar = ch; continue; }
    if (ch === '{') braceCount++;
    if (ch === '}') { braceCount--; if (braceCount === 0) { let j=i+1; while(j<code.length&&(code[j]===' '||code[j]==='\r'||code[j]==='\n'))j++; funcEnd=code[j]===';'?j+1:i+1; break; } }
}
if (funcEnd === -1) { console.log('ERROR: end not found'); process.exit(1); }

const newFunc = `window.applySettingsToAllSections = function(sourceSectionId, options) {
                const sourceSec = document.getElementById(sourceSectionId);
                if (!sourceSec) return;
                var L = window.AndroidLayoutTracker ? function(msg) { window.AndroidLayoutTracker.log("Antigravity_Diva", msg); } : function() {};

                L("=== APPLY ALL START === Source: " + sourceSectionId + " Mode: STRICT INDEX MAPPING");
                if (options.color !== undefined && options.secBg === undefined) options.secBg = options.color;

                var rs = getComputedStyle(sourceSec);
                
                // --- SECTION LEVEL PROPERTIES (Unified) ---
                var sectionBg = sourceSec.style.backgroundColor || rs.backgroundColor || 'transparent';
                var headerStyle = sourceSec.getAttribute('data-header-style') || 'Default';
                var hrs = sourceSec.querySelector('h2') ? getComputedStyle(sourceSec.querySelector('h2')) : rs;
                var gridCols = rs.gridTemplateColumns && rs.gridTemplateColumns !== 'none' ? rs.gridTemplateColumns.split(' ').length : 1;
                var gridGapRaw = hrs.gap || hrs.gridGap || '15px';
                var gridGap = parseInt(gridGapRaw) || 15;
                var alignment = hrs.justifyContent || 'left';

                // --- SUBSECTION / ITEM LEVEL PROPERTIES (Index Mapped) ---
                var cSel = '.pd-row, .data-table-item, .simple-list-item, .skill-group, .contact-item, .item, .sub-item, .declaration-text, .objective-text';
                var sourceItems = sourceSec.querySelectorAll(cSel);
                var sourceStyles = [];
                
                for (var i = 0; i < sourceItems.length; i++) {
                    var el = sourceItems[i];
                    var vcs = getComputedStyle(el);
                    
                    var bg = el.style.getPropertyValue('--item-bg').trim() || vcs.getPropertyValue('--item-bg').trim() || el.style.backgroundColor || vcs.backgroundColor;
                    if (bg === 'transparent' || bg === 'rgba(0, 0, 0, 0)') bg = '';
                    
                    var round = el.style.getPropertyValue('--card-radius').trim() || vcs.getPropertyValue('--card-radius').trim() || el.style.borderRadius || vcs.borderRadius || '0px';
                    var space = el.style.marginBottom || vcs.marginBottom || '15px';
                    var lh = el.style.lineHeight || vcs.lineHeight || '1.4';
                    
                    sourceStyles.push({ bg: bg, round: round, spacing: space, lh: lh });
                }
                
                L("[SCRAPE] Extracted exact styles for " + sourceStyles.length + " source subsections.");

                var targets = Array.from(document.querySelectorAll('section')).filter(function(s) { return s.id && s.id !== 'mainHeader' && s.id !== sourceSectionId; });
                
                targets.forEach(function(targetSec) {
                    var oldSecId = window._currentSettingSectionId;
                    window._currentSettingSectionId = targetSec.id;

                    // 1. Apply general section settings
                    if (options.secBg && window.previewSectionBg && sectionBg && sectionBg !== 'transparent') window.previewSectionBg(sectionBg);
                    if (options.grid && window.previewSectionGrid) window.previewSectionGrid(gridCols, gridGap);
                    if (options.align && window.updateSectionAlignment) window.updateSectionAlignment(targetSec.id, alignment, true, true);
                    if (options.style && window.updateSectionHeaderStyle) {
                        window.updateSectionHeaderStyle(targetSec.id, headerStyle);
                        if (headerStyle === 'Custom Style' && window._scrapeSectionCustomHeaderProps && window.updateSectionHeaderCustomStyle) {
                            var props = window._scrapeSectionCustomHeaderProps(sourceSectionId);
                            if (props) window.updateSectionHeaderCustomStyle(targetSec.id, props);
                        }
                    }

                    // 2. Strict Index Mapped Item Application
                    var tItems = targetSec.querySelectorAll(cSel);
                    L(" -> Applying to " + targetSec.id + ": mapping " + Math.min(tItems.length, sourceStyles.length) + " items strictly.");
                    
                    for (var j = 0; j < tItems.length; j++) {
                        // Strict mapping: Only apply styles if the source actually had an item at this index
                        if (j < sourceStyles.length) {
                            var sStyle = sourceStyles[j];
                            var tEl = tItems[j];
                            
                            if (options.subRound) {
                                tEl.style.setProperty('--card-radius', sStyle.round);
                                if (window.getComputedStyle(tEl).borderRadius !== sStyle.round) {
                                    tEl.style.borderRadius = sStyle.round;
                                }
                            }
                            if (options.subLine) {
                                tEl.style.setProperty('margin-bottom', sStyle.spacing, 'important');
                            }
                            if (options.subLH) {
                                tEl.style.setProperty('line-height', sStyle.lh, 'important');
                            }
                            if (options.subBg && sStyle.bg) {
                                tEl.style.setProperty('--item-bg', sStyle.bg);
                                tEl.style.backgroundColor = sStyle.bg;
                            }
                        }
                    }

                    window._currentSettingSectionId = oldSecId;
                });

                L("=== APPLY STRICT MAPPING COMPLETE ===");
                triggerAutoSave();
            }`;

code = code.substring(0, funcStart) + newFunc + code.substring(funcEnd);
fs.writeFileSync('app/src/main/assets/index.html', code, 'utf8');
console.log('SUCCESS: Strict array-mapped engine installed.');
