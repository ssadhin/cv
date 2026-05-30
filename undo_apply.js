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

const orig3Layer = `window.applySettingsToAllSections = function(sourceSectionId, options) {
                const sourceSec = document.getElementById(sourceSectionId);
                if (!sourceSec) return;

                var L = window.AndroidLayoutTracker ? function(msg) { window.AndroidLayoutTracker.log("Antigravity_Diva", msg); } : function() {};

                L("=== APPLY ALL START === Source: " + sourceSectionId);
                L("Checkboxes: " + Object.keys(options).filter(function(k) { return options[k]; }).join(', '));

                var state = window._SECTION_STYLE_STATE[sourceSectionId] || {};
                L("[READ Layer1 State] bg=" + (state.bg||'EMPTY') + " round=" + (state.round||'EMPTY') + " spacing=" + (state.spacing||'EMPTY') + " lh=" + (state.lh||'EMPTY'));

                var tagBg='', tagRound='', tagSpacing='', tagLH='';
                var styleTag = window._SECTION_STYLE_TAGS[sourceSectionId];
                if (styleTag && styleTag.textContent) {
                    var css = styleTag.textContent;
                    var m2;
                    if ((m2 = css.match(/background-color:\\s*([^;!]+)/))) tagBg = m2[1].trim();
                    if ((m2 = css.match(/border-radius:\\s*([^;!]+)/))) tagRound = m2[1].trim();
                    if ((m2 = css.match(/gap:\\s*([^;!]+)/))) tagSpacing = m2[1].trim();
                    if ((m2 = css.match(/line-height:\\s*([^;!]+)/))) tagLH = m2[1].trim();
                } else {
                    L("[READ Layer2 StyleTag] No injected style tag found for " + sourceSectionId);
                }

                var rs = getComputedStyle(sourceSec);
                var varBg = rs.getPropertyValue('--item-bg').trim();
                var varRound = rs.getPropertyValue('--card-radius').trim();
                var varSpacing = rs.getPropertyValue('--item-margin').trim();
                var varLH = rs.getPropertyValue('--line-height').trim();
                L("[READ Layer3 CSSVar] --item-bg=" + (varBg||'EMPTY') + " --card-radius=" + (varRound||'EMPTY') + " --item-margin=" + (varSpacing||'EMPTY') + " --line-height=" + (varLH||'EMPTY'));

                function pick(s, t, v) {
                    var result = '';
                    if (s && s !== 'transparent' && s !== 'rgba(0, 0, 0, 0)') result = s;
                    else if (t && t !== 'transparent' && t !== 'rgba(0, 0, 0, 0)') result = t;
                    else if (v && v !== 'transparent' && v !== 'rgba(0, 0, 0, 0)') result = v;
                    return result;
                }

                var itemBg     = pick(state.bg, tagBg, varBg);
                var rounding   = pick(state.round, tagRound, varRound) || '0px';
                var spacing    = pick(state.spacing, tagSpacing, varSpacing) || '15px';
                var lineHeight = pick(state.lh, tagLH, varLH) || '1.4';

                L("[RESOLVED ItemBg] = " + itemBg);
                L("[RESOLVED Round] = " + rounding);
                L("[RESOLVED Spacing] = " + spacing);
                L("[RESOLVED LH] = " + lineHeight);

                var sectionBg = sourceSec.style.backgroundColor || rs.backgroundColor || 'transparent';
                var headerStyle = sourceSec.getAttribute('data-header-style') || 'Default';
                var hrs = sourceSec.querySelector('h2') ? getComputedStyle(sourceSec.querySelector('h2')) : rs;
                var gridCols = rs.gridTemplateColumns && rs.gridTemplateColumns !== 'none' ? rs.gridTemplateColumns.split(' ').length : 1;
                var gridGapRaw = hrs.gap || hrs.gridGap || '15px';
                var gridGap = parseInt(gridGapRaw) || 15;
                var alignment = hrs.justifyContent || 'left';

                L("[SECTION PROPS] secBg=" + sectionBg + " header=" + headerStyle + " grid=" + gridCols + "x" + gridGap + " align=" + alignment);

                var targets = Array.from(document.querySelectorAll('section')).filter(function(s) { 
                    return s.id && s.id !== 'mainHeader' && s.id !== sourceSectionId;
                });
                
                L("[TARGETS] Found " + targets.length + " sections: " + targets.map(function(s){return s.id;}).join(', '));

                targets.forEach(function(targetSec) {
                    var oldSecId = window._currentSettingSectionId;
                    window._currentSettingSectionId = targetSec.id;
                    var applied = [];

                    if (options.secBg && sectionBg && sectionBg !== 'transparent' && sectionBg !== 'rgba(0, 0, 0, 0)') {
                        L("SectionBg: [" + targetSec.id + "] -> " + sectionBg);
                        if (window.previewSectionBg) { window.previewSectionBg(sectionBg); applied.push('secBg'); }
                    }
                    if (options.grid && window.previewSectionGrid) { window.previewSectionGrid(gridCols, gridGap); applied.push('grid'); }
                    if (options.align && window.updateSectionAlignment) { window.updateSectionAlignment(targetSec.id, alignment, true, true); applied.push('align'); }
                    if (options.style && window.updateSectionHeaderStyle) {
                        window.updateSectionHeaderStyle(targetSec.id, headerStyle);
                        applied.push('headerStyle');
                        if (headerStyle === 'Custom Style' && window._scrapeSectionCustomHeaderProps && window.updateSectionHeaderCustomStyle) {
                            var props = window._scrapeSectionCustomHeaderProps(sourceSectionId);
                            if (props) window.updateSectionHeaderCustomStyle(targetSec.id, props);
                        }
                    }
                    
                    if (options.subRound && window.previewSectionRounding) { window.previewSectionRounding(rounding); applied.push('round=' + rounding); }
                    if (options.subLine && window.previewSectionSpacing) { window.previewSectionSpacing(spacing); applied.push('spacing=' + spacing); }
                    if (options.subLH && window.previewSectionLineHeight) { window.previewSectionLineHeight(lineHeight); applied.push('lh=' + lineHeight); }
                    if (options.subBg && itemBg && window.previewSectionItemBg) { window.previewSectionItemBg(itemBg); applied.push('itemBg=' + itemBg); }

                    L("[APPLIED -> " + targetSec.id + "] " + applied.join(', '));
                    window._currentSettingSectionId = oldSecId;
                });

                L("=== APPLY ALL COMPLETE === " + targets.length + " sections updated.");
                triggerAutoSave();
            }`;

code = code.substring(0, funcStart) + orig3Layer + code.substring(funcEnd);
fs.writeFileSync('app/src/main/assets/index.html', code, 'utf8');
console.log('SUCCESS: Restored original 3-layer application loop.');
