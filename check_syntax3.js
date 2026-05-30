const fs = require('fs');
const html = fs.readFileSync('app/src/main/assets/index.html', 'utf8');
const match = html.match(/<script[^>]*>([\s\S]*?)<\/script>/gi);
const js = match[1].replace(/<script[^>]*>/i, '').replace(/<\/script>/i, '');

let depth = 0;
let inStr = false, strCh = '';
let inComment = false, inBlockComment = false;

const lines = js.split('\n');

for (let lineIdx = 0; lineIdx < lines.length; lineIdx++) {
    const line = lines[lineIdx];
    let prevDepth = depth;
    
    for (let i = 0; i < line.length; i++) {
        const c = line[i];
        const nc = line[i + 1];

        if (inBlockComment) {
            if (c === '*' && nc === '/') { inBlockComment = false; i++; }
            continue;
        }
        if (inComment) { continue; }
        if (inStr) {
            if (c === '\\') { i++; continue; }
            if (c === strCh) inStr = false;
            continue;
        }
        if (c === '/' && nc === '/') { inComment = true; continue; }
        if (c === '/' && nc === '*') { inBlockComment = true; i++; continue; }
        if (c === '"' || c === "'" || c === '`') { inStr = true; strCh = c; continue; }

        if (c === '{') depth++;
        if (c === '}') depth--;
    }
    inComment = false;
    
    const htmlLine = lineIdx + 5429;
    
    // Check missing braces within metrics block
    if (htmlLine >= 19020 && htmlLine <= 19160 && depth !== prevDepth) {
        const change = depth - prevDepth;
        const arrow = change > 0 ? '+' + change : '' + change;
        console.log(`L${htmlLine}: ${prevDepth}->${depth} (${arrow}) | ${line.trim().substring(0, 80)}`);
    }
}
