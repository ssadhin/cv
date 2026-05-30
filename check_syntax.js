const fs = require('fs');
const content = fs.readFileSync('app/src/main/assets/index.html', 'utf8');

const scriptRegex = /<script\b[^>]*>([\s\S]*?)<\/script>/g;
let match;
let blockCount = 0;

while ((match = scriptRegex.exec(content)) !== null) {
    blockCount++;
    const scriptBody = match[1];
    const offset = content.substring(0, match.index).split('\n').length;
    
    try {
        new Function(scriptBody);
    } catch (e) {
        console.log(`Error in script block ${blockCount} (starting near line ${offset}):`);
        console.log(e.message);
        
        // Try to find the exact line
        const lines = scriptBody.split('\n');
        // We can't easily get the line number from new Function error in Node without more work,
        // but we can look for obvious issues like unclosed braces.
        
        // Heuristic: check brace balance
        let openBraces = (scriptBody.match(/{/g) || []).length;
        let closeBraces = (scriptBody.match(/}/g) || []).length;
        console.log(`Brace balance: { (${openBraces}) vs } (${closeBraces})`);
        
        if (openBraces !== closeBraces) {
            console.log("!!! BRACE MISMATCH DETECTED !!!");
        }
        
        // Show context
        const errorLineMatch = e.stack.match(/<anonymous>:(\d+):(\d+)/);
        if (errorLineMatch) {
            const relLine = parseInt(errorLineMatch[1]);
            const absLine = offset + relLine - 1;
            console.log(`Approx absolute line: ${absLine}`);
            console.log(`Code: ${lines[relLine-1]}`);
        }
    }
}
console.log(`Checked ${blockCount} script blocks.`);
