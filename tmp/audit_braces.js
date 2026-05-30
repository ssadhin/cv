const fs = require('fs');
const path = 'c:/Users/hasad/StudioProjects/MyApplicatio/app/src/main/assets/index.html';
const logPath = 'c:/Users/hasad/StudioProjects/MyApplicatio/tmp/audit_log.txt';

const content = fs.readFileSync(path, 'utf8');
const lines = content.split('\n');

let depth = 0;
let scriptOpen = false;
let inString = null; // ' or " or `
let inComment = false; // /* ... */
let inLineComment = false; // // ...

let log = [];
function record(msg) {
    log.push(msg);
}

for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    const lineNum = i + 1;
    
    if (line.includes('<script')) {
        scriptOpen = true;
        depth = 0;
        inString = null;
        record(`--- Script Started at Line ${lineNum} ---`);
    }
    
    if (scriptOpen) {
        for (let j = 0; j < line.length; j++) {
            const char = line[j];
            const nextChar = line[j+1];

            // Handle Strings
            if (!inComment && !inLineComment) {
                if (inString) {
                    if (char === inString && line[j-1] !== '\\') {
                        inString = null;
                    }
                    continue;
                } else {
                    if (char === "'" || char === '"' || char === '`') {
                        inString = char;
                        continue;
                    }
                }
            }

            // Handle Comments
            if (!inString && !inComment && !inLineComment && char === '/' && nextChar === '/') {
                inLineComment = true;
                break;
            }
            if (!inString && !inComment && !inLineComment && char === '/' && nextChar === '*') {
                inComment = true;
                j++;
                continue;
            }
            if (inComment && char === '*' && nextChar === '/') {
                inComment = false;
                j++;
                continue;
            }

            if (inComment || inLineComment) continue;

            // Count Braces
            if (char === '{') {
                depth++;
                record(`${lineNum}: OPEN { (depth ${depth}): ${line.trim()}`);
            }
            if (char === '}') {
                depth--;
                record(`${lineNum}: CLOSE } (depth ${depth}): ${line.trim()}`);
            }
        }
        
        inLineComment = false;

        if (line.includes('</script')) {
            scriptOpen = false;
            record(`--- Script Ended at Line ${lineNum} (Final depth: ${depth}) ---`);
        }
    }
}
record('--- AUDIT COMPLETE ---');
fs.writeFileSync(logPath, log.join('\n'));
