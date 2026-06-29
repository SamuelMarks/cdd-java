#!/usr/bin/env python3
import os
import re
import subprocess
import glob


def get_color(pct):
    if pct >= 90:
        return "brightgreen"
    if pct >= 80:
        return "green"
    if pct >= 70:
        return "yellowgreen"
    if pct >= 60:
        return "yellow"
    if pct >= 50:
        return "orange"
    return "red"


def get_doc_coverage():
    java_files = glob.glob("src/main/java/**/*.java", recursive=True)
    total_public_elements = 0
    documented_public_elements = 0

    class_meth_pattern = re.compile(
        r"^\s*public\s+(?:static\s+|final\s+|abstract\s+|@[\w.]+\s+)*(?:class|interface|enum|record|[A-Za-z0-9<>\[\],\s]+\s+[A-Za-z0-9_]+)\s*(?:extends\s+[A-Za-z0-9<>\[\],\s]+)?(?:implements\s+[A-Za-z0-9<>\[\],\s]+)?(?:\{|\()",
        re.MULTILINE,
    )

    for file_path in java_files:
        with open(file_path, "r", encoding="utf-8") as f:
            content = f.read()

        # Simple approach: find all occurrences of public elements and check if they are preceded by /**
        # We split by 'public ' and look backwards.

        # A more robust regex for public classes and methods:
        matches = class_meth_pattern.finditer(content)
        for match in matches:
            total_public_elements += 1
            # Check if there is a docstring before this match
            # Get text before match
            before = content[: match.start()]
            # Strip trailing whitespace and annotations
            before = re.sub(r"@[\w.]+(?:\([^)]*\))?\s*", "", before).strip()
            if before.endswith("*/"):
                documented_public_elements += 1

    if total_public_elements == 0:
        return 100
    return int((documented_public_elements / total_public_elements) * 100)


def main():
    readme_path = os.path.join(os.path.dirname(__file__), "..", "README.md")
    if not os.path.exists(readme_path):
        return
    try:
        import shutil

        mvn_cmd = shutil.which("mvn") or "mvn"
        subprocess.run([mvn_cmd, "jacoco:report"], capture_output=True, text=True)
        jacoco_csv = os.path.join("target", "site", "jacoco", "jacoco.csv")
        test_cov = 0
        if os.path.exists(jacoco_csv):
            with open(jacoco_csv, "r") as f:
                lines = f.readlines()[1:]
                missed = sum(int(l.split(",")[3]) for l in lines)
                covered = sum(int(l.split(",")[4]) for l in lines)
                if missed + covered > 0:
                    test_cov = int((covered / (missed + covered)) * 100)
    except Exception as e:
        print(f"Coverage calculation failed: {e}")
        test_cov = 0

    doc_cov = get_doc_coverage()

    test_color = get_color(test_cov)
    doc_color = get_color(doc_cov)

    with open(readme_path, "r") as f:
        content = f.read()

    content = re.sub(
        r"\[\!\[Test Coverage\]\(https://img\.shields\.io/badge/test_coverage-[0-9.]+%25-[a-z]+\.svg\)\]\(#\)",
        f"[![Test Coverage](https://img.shields.io/badge/test_coverage-{test_cov}%25-{test_color}.svg)](#)",
        content,
    )

    content = re.sub(
        r"\[\!\[Doc Coverage\]\(https://img\.shields\.io/badge/doc_coverage-[0-9.]+%25-[a-z]+\.svg\)\]\(#\)",
        f"[![Doc Coverage](https://img.shields.io/badge/doc_coverage-{doc_cov}%25-{doc_color}.svg)](#)",
        content,
    )

    with open(readme_path, "w") as f:
        f.write(content)


if __name__ == "__main__":
    main()
