const fs = require('fs');

const html = fs.readFileSync('app/src/main/assets/index.html', 'utf8');
const match = html.match(/<script[^>]*>([\s\S]*?)<\/script>/gi);
const js = match[1].replace(/<script[^>]*>/i, '').replace(/<\/script>/i, '');

let depth = 0;
let lineNum = 5429; // approximate start line

let i = 0;
let lineStart = 0;

while (i < js.length) {
    let c = js[i];
    
    if (c === '\n') {
        const lineText = js.substring(lineStart, i).trim();
        // Print lines where functions end or if depth is high
        if (lineText.startsWith('}') && lineText.length <= 5) {
            // console.log(`L${lineNum}: depth=${depth} | ${lineText}`);
        }
        
        lineNum++;
        lineStart = i + 1;
    }
    
    // Comments
    if (c === '/' && js[i+1] === '/') {
        while (i < js.length && js[i] !== '\n') i++;
        continue;
    }
    if (c === '/' && js[i+1] === '*') {
        i += 2;
        while (i < js.length && !(js[i] === '*' && js[i+1] === '/')) {
            if (js[i] === '\n') {
                lineNum++;
                lineStart = i + 1;
            }
            i++;
        }
        i += 2;
        continue;
    }
    
    // Strings and Regex
    if (c === '"' || c === "'" || c === '`') {
        let quote = c;
        i++;
        while (i < js.length) {
            if (js[i] === '\\') i += 2;
            else if (js[i] === quote) { i++; break; }
            else {
                if (js[i] === '\n') {
                    lineNum++;
                    lineStart = i + 1;
                }
                i++;
            }
        }
        continue;
    }
    
    // Regex literal heuristics
    if (c === '/') {
        let isRegex = false;
        let backtrack = i - 1;
        while (backtrack >= 0 && /[ \t]/.test(js[backtrack])) backtrack--;
        if (backtrack >= 0) {
            let prevChar = js[backtrack];
            if (/[=,\(\[\!\:\?\&\|\{\;]/.test(prevChar)) {
                isRegex = true;
            }
        }
        if (isRegex) {
            i++;
            while (i < js.length) {
                if (js[i] === '\\') i += 2;
                else if (js[i] === '/') { i++; break; }
                else if (js[i] === '\n') { break; } // invalid regex, abort
                else i++;
            }
            continue;
        }
    }
    
    if (c === '{') {
        depth++;
        // console.log(`L${lineNum} (+): depth=${depth} | ${js.substring(lineStart, i+20).trim().split('\n')[0]}`);
    }
    if (c === '}') {
        depth--;
        // console.log(`L${lineNum} (-): depth=${depth} | ${js.substring(lineStart, i+20).trim().split('\n')[0]}`);
    }
    
    i++;
}

console.log(`Final depth: ${depth}`);
