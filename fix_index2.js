const fs = require('fs');
let code = fs.readFileSync('app/src/main/assets/index.html', 'utf8');

const startIdx = code.indexOf('if (options.subLH && window.updateItemStyle) {');
if (startIdx === -1) {
    console.log('Not found');
    process.exit(1);
}

const endStr = 'return \'none\';\r\n            };';
const endStr2 = 'return \'none\';\n            };';

let endIdx = code.indexOf(endStr, startIdx);
let matchStr = endStr;
if (endIdx === -1) {
    endIdx = code.indexOf(endStr2, startIdx);
    matchStr = endStr2;
}

if (endIdx === -1) {
    console.log('End not found');
    process.exit(1);
}

const fullTarget = code.substring(startIdx, endIdx + matchStr.length);
console.log('TARGET:');
console.log(fullTarget);

const replacement = `if (options.subLH && window.updateItemStyle) {
                                // updateItemStyle doesn't currently support line height directly for items
                                tEl.style.setProperty('line-height', sStyle.lh, 'important');
                            } else if (options.subLH) {
                                tEl.style.setProperty('line-height', sStyle.lh, 'important');
                            }
                        }
                    }

                    window._currentSettingSectionId = oldSecId;
                });

                L("=== APPLY ALL STRICT COMPLETE ===");
                if (typeof triggerAutoSave === 'function') triggerAutoSave();
            };`;

const isCRLF = code.includes('\r\n');
const newBlock = isCRLF ? replacement.replace(/\n/g, '\r\n') : replacement;

code = code.replace(fullTarget, newBlock);
fs.writeFileSync('app/src/main/assets/index.html', code, 'utf8');
console.log('Replaced successfully!');
