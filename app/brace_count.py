import sys
import re

def count_braces(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        lines = f.readlines()
    
    depth = 0
    in_block_comment = False
    
    for i, line in enumerate(lines):
        line_num = i + 1
        
        # very basic comment stripping (doesn't handle strings well, but good enough for a rough gauge)
        if in_block_comment:
            if '*/' in line:
                in_block_comment = False
                line = line.split('*/', 1)[1]
            else:
                continue
                
        while '/*' in line:
            if '*/' in line:
                pre, post = line.split('/*', 1)
                _, post = post.split('*/', 1)
                line = pre + post
            else:
                line = line.split('/*', 1)[0]
                in_block_comment = True
                break
                
        # strip line comments
        line = line.split('//')[0]
        
        # strip strings roughly
        line = re.sub(r'"[^"\\]*(?:\\.[^"\\]*)*"', '""', line)
        
        for char in line:
            if char == '{':
                depth += 1
            elif char == '}':
                depth -= 1
                if depth == 0:
                    print(f"Depth hit 0 at line {line_num}!")
                elif depth < 0:
                    print(f"Depth went negative at line {line_num}!")
                    
    print(f"Final depth: {depth}")

count_braces(sys.argv[1])
