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
if (funcEnd === -1) { console.log('ERROR: end not found'); process.exit(1); }
console.log('Found: ' + (funcEnd - funcStart) + ' chars');

const newFunc = `window.applySettingsToAllSections = function (sourceSectionId, options) {
                const sourceSec = document.getElementById(sourceSectionId);
                if (!sourceSec) return;

                var L = window.AndroidLayoutTracker ? function(msg) { window.AndroidLayoutTracker.log("Antigravity_Diva", msg); } : function() {};

                L("=== APPLY ALL START === Source: " + sourceSectionId);
                if (options.color !== undefined && options.secBg === undefined) options.secBg = options.color;
                L("Checkboxes: " + Object.keys(options).filter(function(k) { return options[k]; }).join(', '));

                // ====================================================================
                // PHASE 1: READ SOURCE DESIGN (4-layer)
                // ====================================================================
                var state = window._SECTION_STYLE_STATE[sourceSectionId] || {};
                L("[L1 State] bg=" + (state.bg||'EMPTY') + " round=" + (state.round||'EMPTY') + " spacing=" + (state.spacing||'EMPTY') + " lh=" + (state.lh||'EMPTY'));
                
                var tagBg='', tagRound='', tagSpacing='', tagLH='';
                var styleTag = window._SECTION_STYLE_TAGS[sourceSectionId];
                if (styleTag && styleTag.textContent) {
                    var css = styleTag.textContent;
                    var m2;
                    if ((m2 = css.match(/background-color:\\s*([^;!]+)/))) tagBg = m2[1].trim();
                    if ((m2 = css.match(/border-radius:\\s*([^;!]+)/))) tagRound = m2[1].trim();
                    if ((m2 = css.match(/gap:\\s*([^;!]+)/))) tagSpacing = m2[1].trim();
                    if ((m2 = css.match(/line-height:\\s*([^;!]+)/))) tagLH = m2[1].trim();
                    L("[L2 Tag] bg=" + (tagBg||'EMPTY') + " round=" + (tagRound||'EMPTY'));
                } else { L("[L2 Tag] None"); }

                var rs = getComputedStyle(sourceSec);
                var varBg = rs.getPropertyValue('--item-bg').trim();
                var varRound = rs.getPropertyValue('--card-radius').trim();
                var varSpacing = rs.getPropertyValue('--item-margin').trim();
                var varLH = rs.getPropertyValue('--line-height').trim();
                L("[L3 Var] bg=" + (varBg||'EMPTY') + " round=" + (varRound||'EMPTY'));

                // Layer 4: Visual DOM Scrape
                var visualBg = '', visualRound = '';
                var cSel = '.pd-row, .data-table-item, .simple-list-item, .skill-group, .contact-item, .item, .sub-item, .declaration-text, .objective-text';
                var cItems = sourceSec.querySelectorAll(cSel);
                L("[L4 Visual] Found " + cItems.length + " content items in source");
                for (var vi = 0; vi < cItems.length; vi++) {
                    var vcs = getComputedStyle(cItems[vi]);
                    var vbg = vcs.backgroundColor;
                    var vrd = vcs.borderRadius;
                    L("[L4 Item " + vi + "] " + cItems[vi].className + " | bg=" + vbg + " | round=" + vrd);
                    if (!visualBg && vbg && vbg !== 'transparent' && vbg !== 'rgba(0, 0, 0, 0)') visualBg = vbg;
                    if (!visualRound && vrd && vrd !== '0px' && vrd !== '0') visualRound = vrd;
                }

                function pick(s, t, v, dom, fallback, label) {
                    var result = fallback;
                    if (s && s !== 'transparent' && s !== 'rgba(0, 0, 0, 0)') result = s;
                    else if (t && t !== 'transparent' && t !== 'rgba(0, 0, 0, 0)') result = t;
                    else if (v && v !== 'transparent' && v !== 'rgba(0, 0, 0, 0)') result = v;
                    else if (dom && dom !== 'transparent' && dom !== 'rgba(0, 0, 0, 0)') result = dom;
                    L("[RESOLVED " + label + "] = " + (result || 'NONE'));
                    return result;
                }

                var itemBg     = pick(state.bg, tagBg, varBg, visualBg, '', 'ItemBg');
                var rounding   = pick(state.round, tagRound, varRound, visualRound, '0px', 'Round');
                var spacing    = pick(state.spacing, tagSpacing, varSpacing, '', '15px', 'Spacing');
                var lineHeight = pick(state.lh, tagLH, varLH, '', '1.4', 'LH');

                var sectionBg = sourceSec.style.backgroundColor || rs.backgroundColor || 'transparent';
                var headerStyle = sourceSec.getAttribute('data-header-style') || 'Default';
                var hrs = sourceSec.querySelector('h2') ? getComputedStyle(sourceSec.querySelector('h2')) : rs;
                var gridCols = rs.gridTemplateColumns && rs.gridTemplateColumns !== 'none' ? rs.gridTemplateColumns.split(' ').length : 1;
                var gridGapRaw = hrs.gap || hrs.gridGap || '15px';
                var gridGap = parseInt(gridGapRaw) || 15;
                var alignment = hrs.justifyContent || 'left';

                // ====================================================================
                // PHASE 2: APPLY + VERIFY
                // ====================================================================
                var targets = Array.from(document.querySelectorAll('section')).filter(function(s) { return s.id && s.id !== 'mainHeader' && s.id !== sourceSectionId; });
                L("[TARGETS] " + targets.length + " sections");

                targets.forEach(function(targetSec) {
                    var oldSecId = window._currentSettingSectionId;
                    window._currentSettingSectionId = targetSec.id;
                    L("--- Processing: " + targetSec.id + " (setting _currentSettingSectionId=" + targetSec.id + ") ---");

                    if (options.secBg && sectionBg && sectionBg !== 'transparent' && sectionBg !== 'rgba(0, 0, 0, 0)') {
                        L("  Calling previewSectionBg(" + sectionBg + ")");
                        if (window.previewSectionBg) window.previewSectionBg(sectionBg);
                    }
                    if (options.subRound) {
                        L("  Calling previewSectionRounding(" + rounding + ") | fn exists=" + !!window.previewSectionRounding);
                        if (window.previewSectionRounding) window.previewSectionRounding(rounding);
                    }
                    if (options.subLine) {
                        L("  Calling previewSectionSpacing(" + spacing + ") | fn exists=" + !!window.previewSectionSpacing);
                        if (window.previewSectionSpacing) window.previewSectionSpacing(spacing);
                    }
                    if (options.subLH) {
                        L("  Calling previewSectionLineHeight(" + lineHeight + ") | fn exists=" + !!window.previewSectionLineHeight);
                        if (window.previewSectionLineHeight) window.previewSectionLineHeight(lineHeight);
                    }
                    if (options.subBg && itemBg) {
                        L("  Calling previewSectionItemBg(" + itemBg + ") | fn exists=" + !!window.previewSectionItemBg);
                        if (window.previewSectionItemBg) window.previewSectionItemBg(itemBg);
                    } else if (options.subBg) {
                        L("  SKIPPED previewSectionItemBg: itemBg is empty");
                    }
                    if (options.grid && window.previewSectionGrid) window.previewSectionGrid(gridCols, gridGap);
                    if (options.align && window.updateSectionAlignment) window.updateSectionAlignment(targetSec.id, alignment, true, true);
                    if (options.style && window.updateSectionHeaderStyle) {
                        window.updateSectionHeaderStyle(targetSec.id, headerStyle);
                        if (headerStyle === 'Custom Style' && window._scrapeSectionCustomHeaderProps && window.updateSectionHeaderCustomStyle) {
                            var props = window._scrapeSectionCustomHeaderProps(sourceSectionId);
                            if (props) window.updateSectionHeaderCustomStyle(targetSec.id, props);
                        }
                    }

                    // VERIFICATION: Check what ACTUALLY happened
                    var verifyState = window._SECTION_STYLE_STATE[targetSec.id] || {};
                    var verifyTag = window._SECTION_STYLE_TAGS[targetSec.id];
                    L("  [VERIFY " + targetSec.id + "] State.bg=" + (verifyState.bg||'EMPTY') + " State.round=" + (verifyState.round||'EMPTY') + " TagExists=" + !!verifyTag);

                    window._currentSettingSectionId = oldSecId;
                });

                L("=== APPLY ALL COMPLETE ===");
                triggerAutoSave();
            }`;

code = code.substring(0, funcStart) + newFunc + code.substring(funcEnd);
fs.writeFileSync('app/src/main/assets/index.html', code, 'utf8');
console.log('SUCCESS: Function replaced.');

