const fs = require('fs');
const lines = fs.readFileSync('app/src/main/assets/index.html', 'utf8').split('\n');

let depth = 0;
let inStr = false, strCh = '';
let inComment = false, inBlockComment = false;

for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    let prevDepth = depth;
    
    for (let j = 0; j < line.length; j++) {
        const c = line[j];
        const nc = line[j + 1];

        if (inBlockComment) {
            if (c === '*' && nc === '/') { inBlockComment = false; j++; }
            continue;
        }
        if (inComment) { continue; }
        if (inStr) {
            if (c === '\\') { j++; continue; }
            if (c === strCh) inStr = false;
            continue;
        }
        if (c === '/' && nc === '/') { inComment = true; continue; }
        if (c === '/' && nc === '*') { inBlockComment = true; j++; continue; }
        if (c === '"' || c === "'" || c === '`') { inStr = true; strCh = c; continue; }

        if (c === '{') depth++;
        if (c === '}') depth--;
    }
    inComment = false;
    
    const htmlLine = i + 1;
    
    if (htmlLine >= 18850 && htmlLine <= 19025 && depth !== prevDepth) {
        const change = depth - prevDepth;
        const arrow = change > 0 ? '+' + change : '' + change;
        console.log(`L${htmlLine}: ${prevDepth}->${depth} (${arrow}) | ${line.trim().substring(0, 80)}`);
    }
}
