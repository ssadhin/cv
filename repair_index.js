const fs = require('fs');
const content = fs.readFileSync('app/src/main/assets/index.html', 'utf8');

const startTag = 'function setupAutoSave() {';
const endTag = 'window.getResumeData = function() {';

const startIdx = content.indexOf(startTag);
const endIdx = content.indexOf(endTag);

if (startIdx === -1 || endIdx === -1) {
    console.error(`Could not find tags: start=${startIdx}, end=${endIdx}`);
    process.exit(1);
}

const newCode = `function setupAutoSave() {
                const observer = new MutationObserver(() => {
                    triggerAutoSave();
                });
                observer.observe(resumePage, { subtree: true, childList: true, characterData: true, attributes: true });

                document.querySelectorAll('input[type="range"], input[type="color"]').forEach(el => {
                    el.addEventListener('input', triggerAutoSave);
                });

                // Initialize DNA Stabilization Mode
                window.isStabilizingDNA = true;
                setTimeout(() => {
                    window.isStabilizingDNA = false;
                    if (window.divaLog) window.divaLog('[Lifecycle] ✅ DNA Stabilization Mode OFF. Auto-save active.');
                }, 3500);

                // Force save on backgrounding to prevent state loss
                document.addEventListener("visibilitychange", function() {
                    if (document.hidden) {
                        if (window.divaLog) window.divaLog('[Lifecycle] 🔒 App Backgrounded: Forcing INSTANT save.');
                        saveLocally(); 
                    }
                });
            }

            `;

const result = content.substring(0, startIdx) + newCode + content.substring(endIdx);
fs.writeFileSync('app/src/main/assets/index.html', result);
console.log('index.html repaired successfully.');
