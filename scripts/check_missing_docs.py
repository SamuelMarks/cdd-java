#!/usr/bin/env python3
import glob
import re
java_files = glob.glob('src/main/java/**/*.java', recursive=True)
class_meth_pattern = re.compile(r'^\s*public\s+(?:static\s+|final\s+|abstract\s+|@[\w.]+\s+)*(?:class|interface|enum|record|[A-Za-z0-9<>\[\],\s]+\s+[A-Za-z0-9_]+)\s*(?:extends\s+[A-Za-z0-9<>\[\],\s]+)?(?:implements\s+[A-Za-z0-9<>\[\],\s]+)?(?:\{|\()', re.MULTILINE)
for file_path in java_files:
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    matches = class_meth_pattern.finditer(content)
    for match in matches:
        before = content[:match.start()]
        before = re.sub(r'@[\w.]+(?:\([^)]*\))?\s*', '', before).strip()
        if not before.endswith('*/'):
            print(f"Missing doc in {file_path}: {match.group(0).strip()}")
