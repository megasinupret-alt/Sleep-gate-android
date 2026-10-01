# SleepGate Android v1.0

Phone-first Android MVP for airport rest/sleep discovery.

## v1.0
- Native Kotlin + Jetpack Compose
- AUH Terminal A pilot data
- Day/night Sleep Score
- Local demo mode without backend
- Supabase email/password authentication
- Supabase REST API for places and ratings
- Supabase Storage photo upload
- RLS + public photo bucket setup SQL
- GitHub Actions cloud APK build — no PC required
- Debug build fixed: Compose enabled, Java 17, Android SDK installed in Actions

## Demo data
The two AUH Terminal A locations are based on user-reported observations and are not independently verified. Demo images are illustrative and not real airport photographs.

## Build on phone
See `PHONE_ONLY.md`.

## Supabase
Run `backend/supabase_schema.sql`, then put your project's URL and public anon/publishable key into `SupabaseConfig.kt`.

## Security
Never put a Supabase `service_role` key into the Android app.
