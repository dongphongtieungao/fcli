"""KMS/SDD audit gate for manifest, sources, links, contradictions, and evidence."""

from __future__ import annotations

import argparse
import re
import sys
import urllib.parse
from pathlib import Path
from typing import Any

import yaml


ROOT = Path(__file__).resolve().parents[1]
MANIFEST = ROOT / "docs/grounding/manifest.yaml"
SOURCES = ROOT / "docs/grounding/sources.md"
LIFECYCLE = ROOT / "docs/methodology/agent_request_lifecycle_kms_sdd_grounding.md"
CONTRADICTIONS = ROOT / "llm-wiki/contradiction-log.md"
RUN_CI = ROOT / "run_ci.cmd"

CRITICAL_LINK_FILES = [
    ROOT / "AGENTS.md",
    ROOT / "docs/AGENTS.md",
    ROOT / "docs/grounding/manifest.md",
    ROOT / "docs/grounding/sources.md",
    ROOT / "docs/grounding/prompts.md",
    LIFECYCLE,
    ROOT / "docs/04-spec-test/application-spec.md",
]

VERIFICATION_EVIDENCE_FILES = [
    ROOT / "docs/methodology/o_5.agent_verification_model_v2_reviewed.md",
    ROOT / "docs/06-quality-assurance/14.qa-matrix.md",
    ROOT / "docs/07-ci-cd-review/15.quality-matrix.md",
    ROOT / "docs/09-runbook/18.dod.md",
    ROOT / "tools/kb.ps1",
]


def rel(path: Path) -> str:
    try:
        return path.relative_to(ROOT).as_posix()
    except ValueError:
        return path.as_posix()


def read_text(path: Path) -> str:
    return path.read_text(encoding="utf-8", errors="ignore")


def load_manifest() -> dict[str, Any]:
    return yaml.safe_load(read_text(MANIFEST))


def local_path(value: str) -> Path | None:
    if not value:
        return None
    if value.startswith(("http://", "https://", "mailto:")):
        return None
    return ROOT / value


def markdown_links(path: Path) -> list[tuple[int, str]]:
    text = read_text(path)
    links: list[tuple[int, str]] = []
    for line_no, line in enumerate(text.splitlines(), 1):
        for match in re.finditer(r"(?<!!)\[[^\]]+\]\(([^)]+)\)", line):
            links.append((line_no, match.group(1).strip()))
    return links


def resolve_markdown_target(base: Path, target: str) -> Path | None:
    if not target or target.startswith(("#", "http://", "https://", "mailto:")):
        return None
    target = target.split("#", 1)[0].strip()
    if not target:
        return None
    if target.startswith("<") and target.endswith(">"):
        target = target[1:-1]
    target = urllib.parse.unquote(target)
    if any(ch in target for ch in "*?[]"):
        return None
    return (base.parent / target).resolve()


def check_manifest_paths(manifest: dict[str, Any]) -> list[str]:
    errors: list[str] = []
    for section, key in (("kms_registry", "path"), ("sources", "path_or_url")):
        for item in manifest.get(section, []):
            path_value = item.get(key)
            path = local_path(path_value)
            if path and not path.exists():
                source_id = item.get("source_id") or item.get("id") or "<unknown>"
                errors.append(f"{section}:{source_id} points to missing path {path_value}")
    return errors


def check_sources_catalog(manifest: dict[str, Any]) -> list[str]:
    errors: list[str] = []
    text = read_text(SOURCES)
    for item in manifest.get("sources", []):
        source_id = item.get("id")
        if source_id and f"`{source_id}`" not in text:
            errors.append(f"sources.md is missing source id `{source_id}`")
    for item in manifest.get("kms_registry", []):
        source_id = item.get("source_id")
        if item.get("truth_level") == "official" and source_id and f"`{source_id}`" not in text:
            errors.append(f"sources.md is missing official KMS source id `{source_id}`")
    return errors


def check_lifecycle_manifest_sync(manifest: dict[str, Any]) -> list[str]:
    text = read_text(LIFECYCLE)
    errors: list[str] = []
    version = str(manifest.get("version", ""))
    updated = str(manifest.get("last_updated") or manifest.get("updated", ""))
    if f"version `{version}`" not in text:
        errors.append(f"lifecycle doc does not mention manifest version `{version}`")
    if f"`updated: {updated}`" not in text and f"`last_updated: {updated}`" not in text:
        errors.append(f"lifecycle doc does not mention manifest updated date `{updated}`")
    return errors


def check_critical_links() -> list[str]:
    errors: list[str] = []
    for path in CRITICAL_LINK_FILES:
        if not path.exists():
            errors.append(f"critical link file missing: {rel(path)}")
            continue
        for line_no, target in markdown_links(path):
            resolved = resolve_markdown_target(path, target)
            if resolved and not resolved.exists():
                errors.append(f"{rel(path)}:{line_no} has missing link target {target}")
    return errors


def count_all_missing_doc_links() -> int:
    count = 0
    for path in (ROOT / "docs").rglob("*.md"):
        for _, target in markdown_links(path):
            resolved = resolve_markdown_target(path, target)
            if resolved and not resolved.exists():
                count += 1
    return count


def check_contradictions() -> list[str]:
    if not CONTRADICTIONS.exists():
        return []
    errors: list[str] = []
    lines = read_text(CONTRADICTIONS).splitlines()
    allowed = ("resolved", "non-blocking", "accepted", "documented", "closed")
    for line_no, line in enumerate(lines, 1):
        if not line.startswith("|") or "---" in line or "Status" in line:
            continue
        cells = [cell.strip().lower() for cell in line.strip("|").split("|")]
        if len(cells) < 5:
            continue
        status = cells[-1]
        if not status.startswith(allowed):
            errors.append(f"{rel(CONTRADICTIONS)}:{line_no} has unresolved contradiction status `{cells[-1]}`")
    return errors


def check_verification_evidence() -> list[str]:
    errors: list[str] = []
    for path in VERIFICATION_EVIDENCE_FILES:
        if not path.exists():
            errors.append(f"verification evidence file missing: {rel(path)}")
    lifecycle = read_text(LIFECYCLE)
    for needle in ("Verification Gate", "VERIFIED", "PARTIALLY_VERIFIED", "NOT_VERIFIED", "BLOCKED"):
        if needle not in lifecycle:
            errors.append(f"lifecycle doc is missing verification marker `{needle}`")
    if RUN_CI.exists():
        run_ci_text = read_text(RUN_CI)
        if "tools/kms_ci_audit.py" not in run_ci_text and "tools\\kms_ci_audit.py" not in run_ci_text:
            errors.append("run_ci.cmd exists but does not include the KMS CI audit gate")
    return errors


def main() -> int:
    parser = argparse.ArgumentParser(description="Run KMS/SDD audit gate.")
    parser.add_argument("--strict-links", action="store_true", help="Fail on any missing Markdown link under docs/.")
    args = parser.parse_args()

    manifest = load_manifest()
    checks = {
        "manifest paths": check_manifest_paths(manifest),
        "sources catalog": check_sources_catalog(manifest),
        "lifecycle manifest sync": check_lifecycle_manifest_sync(manifest),
        "critical links": check_critical_links(),
        "contradictions": check_contradictions(),
        "verification evidence": check_verification_evidence(),
    }

    all_missing_links = count_all_missing_doc_links()
    if args.strict_links and all_missing_links:
        checks["all docs links"] = [f"{all_missing_links} missing Markdown link target(s) under docs/"]

    failures = 0
    for name, errors in checks.items():
        if errors:
            failures += len(errors)
            print(f"FAIL {name}")
            for error in errors:
                print(f"  - {error}")
        else:
            print(f"PASS {name}")

    if all_missing_links:
        print(f"WARN all docs links: {all_missing_links} missing target(s); use --strict-links to fail on all.")
    else:
        print("PASS all docs links")

    if failures:
        print(f"RESULT FAIL: {failures} blocking KMS audit issue(s)")
        return 1
    print("RESULT PASS: KMS audit gate passed")
    return 0


if __name__ == "__main__":
    sys.exit(main())
