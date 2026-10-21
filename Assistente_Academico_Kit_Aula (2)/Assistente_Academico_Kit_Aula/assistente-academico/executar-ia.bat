@echo off
cd /d "%~dp0"
java -jar bin\assistente-academico.jar --app.modo=ia --ollama.modelo=qwen2.5:latest
pause
