const fs = require('fs');
const content = fs.readFileSync('app/src/main/assets/index.html', 'utf8');

const scriptRegex = /<script\b[^>]*>([\s\S]*?)<\/script>/g;
let match;
let blockCount = 0;

while ((match = scriptRegex.exec(content)) !== null) {
    blockCount++;
    if (blockCount !== 2) continue; // Only check block 2

    const scriptBody = match[1];
    const lines = scriptBody.split('\n');
    let stack = [];
    
    for (let i = 0; i < lines.length; i++) {
        const line = lines[i];
        for (let j = 0; j < line.length; j++) {
            const char = line[j];
            if (char === '{') {
                stack.push({ line: i + 1, col: j + 1 });
            } else if (char === '}') {
                if (stack.length === 0) {
                    console.log(`EXTRA CLOSING BRACE at line ${i + 1}, column ${j + 1}`);
                    console.log(`Line content: ${line}`);
                } else {
                    stack.pop();
                }
            }
        }
    }
    
    if (stack.length > 0) {
        console.log(`${stack.length} UNCLOSED OPENING BRACES:`);
        stack.forEach(b => console.log(`  Line ${b.line}, Col ${b.col}`));
    }
}
