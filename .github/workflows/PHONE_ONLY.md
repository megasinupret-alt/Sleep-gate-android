# SleepGate — работа только с телефона

## Что нужно
- Samsung/Android телефон
- браузер
- GitHub аккаунт
- Supabase аккаунт

## 1. GitHub
1. Открой github.com в браузере.
2. Создай репозиторий `sleepgate-android`.
3. Загрузи содержимое этой папки в репозиторий (кнопка Add file → Upload files).
4. После загрузки открой Actions → Build SleepGate APK → Run workflow.
5. После сборки открой последний запуск → Artifacts → sleepgate-debug-apk.
6. Скачай APK на телефон и установи его.

## 2. Supabase
1. Создай проект на supabase.com.
2. Открой SQL Editor.
3. Выполни весь файл `backend/supabase_schema.sql`.
4. Открой Project Settings → API.
5. Скопируй Project URL и публичный anon/publishable key.
6. В файле `app/src/main/java/com/sleepgate/app/SupabaseConfig.kt` замени две строки:
   - URL
   - ANON_KEY
7. Загрузи изменённый файл в GitHub.
8. Снова запусти Actions → Build SleepGate APK.

## 3. Что заработает после Supabase
- регистрация и вход по email/password;
- облачный список мест;
- добавление новых мест авторизованным пользователем;
- облачные отзывы;
- загрузка фотографий в Storage bucket `place-photos`;
- профиль пользователя.

## Важно
Никогда не вставляй `service_role` key в Android-приложение. Используется только публичный anon/publishable key.
