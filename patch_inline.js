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

                // LAYER 1: State
                var state = window._SECTION_STYLE_STATE[sourceSectionId] || {};
                
                // LAYER 2: Tag
                var tagBg='', tagRound='', tagSpacing='', tagLH='';
                var styleTag = window._SECTION_STYLE_TAGS[sourceSectionId];
                if (styleTag && styleTag.textContent) {
                    var css = styleTag.textContent;
                    var m2;
                    if ((m2 = css.match(/background-color:\\s*([^;!]+)/))) tagBg = m2[1].trim();
                    if ((m2 = css.match(/border-radius:\\s*([^;!]+)/))) tagRound = m2[1].trim();
                    if ((m2 = css.match(/gap:\\s*([^;!]+)/))) tagSpacing = m2[1].trim();
                    if ((m2 = css.match(/line-height:\\s*([^;!]+)/))) tagLH = m2[1].trim();
                }

                // LAYER 3: CSS Var
                var rs = getComputedStyle(sourceSec);
                var varBg = rs.getPropertyValue('--item-bg').trim();
                var varRound = rs.getPropertyValue('--card-radius').trim();
                var varSpacing = rs.getPropertyValue('--item-margin').trim();
                var varLH = rs.getPropertyValue('--line-height').trim();

                // LAYER 4: Visual (The source of truth when sliders were used step-by-step)
                var visualBg = '', visualRound = '';
                var cSel = '.pd-row, .data-table-item, .simple-list-item, .skill-group, .contact-item, .item, .sub-item, .declaration-text, .objective-text';
                var cItems = sourceSec.querySelectorAll(cSel);
                for (var vi = 0; vi < cItems.length; vi++) {
                    var vcs = getComputedStyle(cItems[vi]);
                    var vbg = vcs.backgroundColor;
                    var vrd = vcs.borderRadius;
                    if (!visualBg && vbg && vbg !== 'transparent' && vbg !== 'rgba(0, 0, 0, 0)') visualBg = vbg;
                    if (!visualRound && vrd && vrd !== '0px' && vrd !== '0') visualRound = vrd;
                }

                function pick(s, t, v, dom, fallback) {
                    if (s && s !== 'transparent' && s !== 'rgba(0, 0, 0, 0)') return s;
                    if (t && t !== 'transparent' && t !== 'rgba(0, 0, 0, 0)') return t;
                    if (v && v !== 'transparent' && v !== 'rgba(0, 0, 0, 0)') return v;
                    if (dom && dom !== 'transparent' && dom !== 'rgba(0, 0, 0, 0)') return dom;
                    return fallback;
                }

                var itemBg     = pick(state.bg, tagBg, varBg, visualBg, '');
                var rounding   = pick(state.round, tagRound, varRound, visualRound, '0px');
                var spacing    = pick(state.spacing, tagSpacing, varSpacing, '', '15px');
                var lineHeight = pick(state.lh, tagLH, varLH, '', '1.4');

                // Section Properties
                var sectionBg = sourceSec.style.backgroundColor || rs.backgroundColor || 'transparent';
                var headerStyle = sourceSec.getAttribute('data-header-style') || 'Default';
                var hrs = sourceSec.querySelector('h2') ? getComputedStyle(sourceSec.querySelector('h2')) : rs;
                var gridCols = rs.gridTemplateColumns && rs.gridTemplateColumns !== 'none' ? rs.gridTemplateColumns.split(' ').length : 1;
                var gridGapRaw = hrs.gap || hrs.gridGap || '15px';
                var gridGap = parseInt(gridGapRaw) || 15;
                var alignment = hrs.justifyContent || 'left';

                // ====================================================================
                // PHASE 2: APPLY USING STEP-BY-STEP EMULATION
                // ====================================================================
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
                    L("[STEP-BY-STEP] Forcing inline styles on " + tItems.length + " items in " + targetSec.id);
                    for (var i = 0; i < tItems.length; i++) {
                        var el = tItems[i];
                        if (options.subRound) {
                            el.style.setProperty('--card-radius', rounding);
                            el.style.borderRadius = rounding;
                        }
                        if (options.subLine) {
                            el.style.setProperty('margin-bottom', spacing, 'important');
                        }
                        if (options.subLH) {
                            el.style.lineHeight = lineHeight;
                        }
                        if (options.subBg && itemBg) {
                            el.style.setProperty('--item-bg', itemBg);
                            el.style.backgroundColor = itemBg;
                        }
                    }

                    window._currentSettingSectionId = oldSecId;
                });

                L("=== APPLY ALL COMPLETE ===");
                triggerAutoSave();
            }`;

code = code.substring(0, funcStart) + newFunc + code.substring(funcEnd);
fs.writeFileSync('app/src/main/assets/index.html', code, 'utf8');
console.log('SUCCESS: Inline forcing implemented.');
