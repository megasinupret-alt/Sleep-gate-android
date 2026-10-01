# SleepGate v1.0 — phone-only setup

This package is a normal Android Gradle project. The root must contain `app/`, `build.gradle.kts`, and `settings.gradle.kts`.

## Easiest phone workflow
1. Create a new empty GitHub repository.
2. Upload the **contents** of this folder so `app/` is at repository root. Do not upload the zip itself as the only file.
3. The workflow is already at `.github/workflows/build.yml`.
4. Open GitHub → Actions → Build SleepGate APK → Run workflow.
5. Open the completed run → Artifacts → sleepgate-debug-apk → download the ZIP and extract the APK.

If GitHub's mobile upload cannot upload a folder, use GitHub Codespaces only once to extract the ZIP, or use the GitHub web upload interface from a browser that supports directory upload.
