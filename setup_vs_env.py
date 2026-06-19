#!/usr/bin/env python3
"""
Helper script to setup Visual Studio Build Tools environment
This script finds and activates VS Build Tools environment before running commands
"""

import os
import sys
import subprocess
from pathlib import Path


def find_vs_build_tools():
    """Find Visual Studio Build Tools installation"""
    vs_paths = [
        r"C:\Program Files\Microsoft Visual Studio\2022\BuildTools",
        r"C:\Program Files\Microsoft Visual Studio\2022\Community",
        r"C:\Program Files\Microsoft Visual Studio\2022\Professional",
        r"C:\Program Files\Microsoft Visual Studio\2022\Enterprise",
        r"C:\Program Files (x86)\Microsoft Visual Studio\2022\BuildTools",
        r"C:\Program Files (x86)\Microsoft Visual Studio\2022\Community",
    ]

    for vs_path in vs_paths:
        vcvars_path = Path(vs_path) / "VC" / "Auxiliary" / "Build" / "vcvars64.bat"
        if vcvars_path.exists():
            return str(vcvars_path)

    return None


def setup_vs_environment():
    """Setup Visual Studio environment variables"""
    vcvars_path = find_vs_build_tools()

    if not vcvars_path:
        print("Visual Studio Build Tools not found")
        print("Please install from: https://visualstudio.microsoft.com/downloads/")
        return False

    print("Found Visual Studio Build Tools")
    print(f"Setting up environment from: {vcvars_path}")

    # Run vcvars64.bat and capture environment variables
    # Note: This is tricky in Python, we'll use a workaround
    cmd = f'"{vcvars_path}" && set'
    try:
        result = subprocess.run(cmd, shell=True, capture_output=True, text=True, executable="cmd.exe")

        if result.returncode == 0:
            # Parse environment variables from output
            for line in result.stdout.split("\n"):
                if "=" in line:
                    key, value = line.split("=", 1)
                    key = key.strip()
                    value = value.strip()
                    if key and value:
                        os.environ[key] = value

            # Verify cl.exe is now available
            cl_path = os.environ.get("VCINSTALLDIR", "")
            if cl_path:
                cl_exe = Path(cl_path) / "Tools" / "MSVC" / "*" / "bin" / "Hostx64" / "x64" / "cl.exe"
                # Try to find cl.exe
                import glob

                cl_files = glob.glob(str(cl_exe))
                if cl_files:
                    print("✓ C compiler (cl.exe) is now available")
                    return True

            print("WARNING: Could not verify cl.exe, but VS environment is set")
            return True
        else:
            print(f"Failed to setup VS environment: {result.stderr}")
            return False

    except Exception as e:
        print(f"Error setting up VS environment: {e}")
        return False


if __name__ == "__main__":
    if sys.platform != "win32":
        print("This script is for Windows only")
        sys.exit(1)

    success = setup_vs_environment()
    if success:
        print("\nVisual Studio environment is ready!")
        print("You can now run: python download_packages_all.py windows")
    else:
        print("\nFailed to setup Visual Studio environment")
        sys.exit(1)
