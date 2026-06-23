@echo off
setlocal
cd /d "%~dp0"

if not exist ".venv\Scripts\python.exe" (
  echo Missing .venv. Run: uv sync
  exit /b 1
)

".venv\Scripts\python.exe" -m uvicorn src.main:app --host 127.0.0.1 --port 4100
