#!/usr/bin/env python3
"""Package the PyInstaller executable as a release-friendly zip artifact."""

from __future__ import annotations

import argparse
from pathlib import Path
from zipfile import ZIP_DEFLATED, ZipFile


def find_executable(dist_dir: Path) -> Path:
    for candidate in ("portfolium.exe", "portfolium"):
        path = dist_dir / candidate
        if path.is_file():
            return path
    raise FileNotFoundError(f"No PyInstaller executable found in {dist_dir}")


def build_zip(binary_path: Path, artifact_name: str) -> Path:
    output_path = binary_path.parent / f"{artifact_name}.zip"
    with ZipFile(output_path, mode="w", compression=ZIP_DEFLATED) as archive:
        archive.write(binary_path, arcname=binary_path.name)
    return output_path


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Package the generated PyInstaller binary into a zip file."
    )
    parser.add_argument("artifact_name", help="Base name for the output zip artifact")
    parser.add_argument(
        "--dist-dir",
        default="dist",
        help="Directory where PyInstaller places build outputs (default: dist)",
    )
    args = parser.parse_args()

    dist_dir = Path(args.dist_dir)
    executable = find_executable(dist_dir)
    archive = build_zip(executable, args.artifact_name)
    print(f"Created artifact: {archive}")


if __name__ == "__main__":
    main()

