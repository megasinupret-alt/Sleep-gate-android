# SleepGate 1.40 — cloud build

This package starts from the uploaded `SleepGate-1.39.apk` and applies a minimal OAuth/WebView fix.

## Fix

The 1.39 Google OAuth WebView receives the Supabase `sleepgate://auth` redirect, but WebView can try to navigate to that custom scheme and show `ERR_UNKNOWN_URL_SCHEME`.

The build patch inserts `WebView.stopLoading()` immediately before the existing OAuth callback in `onPageStarted`, so the existing 1.39 token-processing logic can run without the WebView continuing into the custom scheme.

## Build from a phone

1. Extract this ZIP.
2. Upload the extracted files to the existing GitHub repository.
3. Open **Actions → Build SleepGate 1.40 → Run workflow**.
4. After it finishes, open the workflow run and download the **SleepGate-1.40** artifact.

The workflow creates a new signing key for this test build. Therefore Android may require uninstalling 1.39 before installing 1.40 if the signatures differ. This is a test build, not the final production signing key.
