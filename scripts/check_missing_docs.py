#!/usr/bin/env python3
"""Verifies 100% Javadoc coverage across all files, classes, methods, fields, and parameters."""

import glob
import os
import re
import sys


def strip_method_bodies(content: str) -> str:
    """Replaces method/block bodies at depth >= 2 with `{}` to expose top-level declarations."""
    out = []
    depth = 0
    i = 0
    n = len(content)
    in_str = False
    str_char = ""
    in_line_comment = False
    in_block_comment = False
    newline = chr(10)
    backslash = chr(92)

    while i < n:
        ch = content[i]
        nxt = content[i + 1] if i + 1 < n else ""

        if in_line_comment:
            out.append(ch)
            if ch == newline:
                in_line_comment = False
            i += 1
            continue

        if in_block_comment:
            out.append(ch)
            if ch == "*" and nxt == "/":
                out.append(nxt)
                in_block_comment = False
                i += 2
                continue
            i += 1
            continue

        if in_str:
            if depth < 2:
                out.append(ch)
            if ch == backslash and i + 1 < n:
                if depth < 2:
                    out.append(nxt)
                i += 2
                continue
            if ch == str_char:
                in_str = False
            i += 1
            continue

        if ch == "/" and nxt == "/":
            in_line_comment = True
            if depth < 2:
                out.append(ch)
                out.append(nxt)
            i += 2
            continue

        if ch == "/" and nxt == "*":
            in_block_comment = True
            if depth < 2:
                out.append(ch)
                out.append(nxt)
            i += 2
            continue

        if ch in ('"', "'"):
            in_str = True
            str_char = ch
            if depth < 2:
                out.append(ch)
            i += 1
            continue

        if ch == "{":
            depth += 1
            if depth <= 2:
                out.append(ch)
            i += 1
            continue

        if ch == "}":
            if depth <= 2:
                out.append(ch)
            depth = max(0, depth - 1)
            i += 1
            continue

        if depth < 2:
            out.append(ch)
        i += 1

    return "".join(out)


def split_params(params_str: str) -> list[str]:
    """Splits parameter list by top-level commas, ignoring commas inside `<...>` generics."""
    parts = []
    current = []
    angle_depth = 0
    for ch in params_str:
        if ch == "<":
            angle_depth += 1
            current.append(ch)
        elif ch == ">":
            angle_depth = max(0, angle_depth - 1)
            current.append(ch)
        elif ch == "," and angle_depth == 0:
            parts.append("".join(current).strip())
            current = []
        else:
            current.append(ch)
    if current:
        parts.append("".join(current).strip())
    return [p for p in parts if p]


def check_file(file_path: str) -> list[str]:
    """Checks a single Java file for missing or placeholder Javadocs."""
    errors = []
    with open(file_path, "r", encoding="utf-8") as f:
        raw_content = f.read()

    if os.path.basename(file_path) == "package-info.java":
        if "/**" not in raw_content:
            errors.append(f"Missing package Javadoc in {file_path}")
        return errors

    if re.search(r"\*/\s*/\*\*", raw_content):
        errors.append(f"Duplicate stacked Javadoc comments in {file_path}")

    for bad_phrase in ("param doc", "return doc", "exception doc", "Generated JavaDoc"):
        if re.search(rf"\b{re.escape(bad_phrase)}\b", raw_content):
            errors.append(f"Placeholder Javadoc '{bad_phrase}' found in {file_path}")

    skeleton = strip_method_bodies(raw_content)

    decl_pattern = re.compile(
        r"^[ 	]*(?:public|protected|private)\s+"
        r"(?:static\s+|final\s+|abstract\s+|synchronized\s+|volatile\s+|transient\s+)*"
        r"(?:class|interface|@interface|enum|record|[A-Za-z0-9_<>\[\].,\s?]+?\s+[A-Za-z0-9_]+)"
        r"\s*(?:extends\s+[A-Za-z0-9_<>\[\].,\s]+)?(?:implements\s+[A-Za-z0-9_<>\[\].,\s]+)?"
        r"\s*(?:\(|\{|;|=)",
        re.MULTILINE,
    )

    for match in decl_pattern.finditer(skeleton):
        decl_text = match.group(0).strip()
        before = skeleton[: match.start()]
        before_stripped = re.sub(
            r"(?:\s*@[A-Za-z0-9_.]+(?:\([^)]*\))?)+\s*$", "", before
        ).strip()
        if not before_stripped.endswith("*/"):
            errors.append(f"Missing Javadoc in {file_path}: {decl_text}")
            continue

        doc_start = before_stripped.rfind("/**")
        if doc_start == -1:
            errors.append(f"Malformed Javadoc in {file_path}: {decl_text}")
            continue

        doc_block = before_stripped[doc_start:]

        if decl_text.endswith("("):
            close_paren = skeleton.find(")", match.end() - 1)
            if close_paren != -1:
                params_str = skeleton[match.end() : close_paren].strip()
                if params_str:
                    param_parts = split_params(params_str)
                    for part in param_parts:
                        tokens = part.split()
                        if tokens:
                            param_name = tokens[-1].replace("...", "")
                            if f"@param {param_name}" not in doc_block:
                                errors.append(
                                    f"Missing @param {param_name} in {file_path}: {decl_text}"
                                )
                header_tokens = decl_text[:-1].strip().split()
                if len(header_tokens) >= 2:
                    method_name = header_tokens[-1]
                    return_type = header_tokens[-2]
                    if (
                        return_type
                        not in (
                            "void",
                            "public",
                            "protected",
                            "private",
                            "class",
                            "interface",
                            "enum",
                        )
                        and not method_name[0].isupper()
                    ):
                        if "@return" not in doc_block:
                            errors.append(
                                f"Missing @return in {file_path}: {decl_text}"
                            )

    return errors


def main() -> int:
    """Entrypoint for verifying Javadoc completeness across all packages."""
    errors = []
    packages = [d for d in glob.glob("src/main/java/*") if os.path.isdir(d)]
    for pkg_dir in sorted(packages):
        pkg_info = os.path.join(pkg_dir, "package-info.java")
        if not os.path.exists(pkg_info):
            errors.append(f"Missing package-info.java in {pkg_dir}")

    java_files = sorted(glob.glob("src/main/java/**/*.java", recursive=True)) + sorted(
        glob.glob("src/test/java/**/*.java", recursive=True)
    )
    for file_path in java_files:
        errors.extend(check_file(file_path))

    for err in errors:
        print(err)

    return 1 if errors else 0


if __name__ == "__main__":
    sys.exit(main())
