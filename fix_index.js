const fs = require('fs');
let code = fs.readFileSync('app/src/main/assets/index.html', 'utf8');

const targetStr = `                            if (options.subLH && window.updateItemStyle) {
                                // updateItemStyle doesn't currently support line height directly for items
                                tEl.style.setProperty('line-height', sStyle.lh, 'important');
                            } else if (options.subLH) {
                                tEl.style.setProperty('line-height', sStyle.lh, 'important');
                            }
return 'none';
            };`;

const targetStr2 = targetStr.replace(/\n/g, '\r\n');

const replaceStr = `                            if (options.subLH && window.updateItemStyle) {
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

if (code.includes(targetStr)) {
    code = code.replace(targetStr, replaceStr);
    fs.writeFileSync('app/src/main/assets/index.html', code, 'utf8');
    console.log('Fixed LF');
} else if (code.includes(targetStr2)) {
    code = code.replace(targetStr2, replaceStr.replace(/\n/g, '\r\n'));
    fs.writeFileSync('app/src/main/assets/index.html', code, 'utf8');
    console.log('Fixed CRLF');
} else {
    console.log('Could not find target block');
}
