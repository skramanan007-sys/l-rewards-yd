# L Rewards Android

Native Kotlin + Jetpack Compose project for L Rewards. The app uses a premium dark Material 3 interface and the connected Supabase project contains the core schema, RLS policies, signup profile trigger, and server-side `claim_reward` RPC.

## Open and run

1. Open the `android` directory in Android Studio Ladybug or newer.
2. Use JDK 17 and install Android SDK 35.
3. Add the client-safe Supabase values to `android/gradle.properties`:

```properties
supabaseUrl=https://xgwprwljmrutkrtmcdff.supabase.co
supabasePublishableKey=YOUR_PUBLISHABLE_KEY
```

Never put the Supabase secret/service-role key in the Android project or APK.

4. Sync Gradle and run the `app` configuration on an emulator or device.

## Supabase setup

The connected Supabase migration `l_rewards_core_schema` creates `profiles`, `transactions`, `daily_limits`, `redemptions`, and `game_settings`, enables RLS, creates the signup profile trigger, and exposes `claim_reward(game_type, amount)` to authenticated users. Email confirmation remains controlled by the Supabase Auth project settings.

The current native UI is wired to Supabase Auth, profile reads/updates, transaction history, reward RPCs, redemption RPCs, and admin metrics/actions. Reward amounts, daily limits, balances, and ledger writes remain server-side; the Android client never updates `profiles.coins` directly.

The Android client must contain only the publishable key. The secret/service-role key must never be placed in `gradle.properties`, source control, CI artifacts, or the APK. Supabase Auth persists its session using the Kotlin client; signing out clears the local session.

Before release, use a real Supabase account with confirmed email, designate administrators in the database, and verify every redemption/admin action against the RLS policies. The included release build is minified but still requires a project-owned signing key before distribution.

## Build APK

From Android Studio use **Build > Generate App Bundles or APKs**. The GitHub Actions workflow at `.github/workflows/android.yml` builds and uploads a debug APK whenever files under `android/` change. Configure release signing in CI before publishing a release artifact.

## Admin access

Add admin authorization using Supabase `app_metadata` or a dedicated admin table and RLS policies. Do not use editable `user_metadata` for authorization decisions and do not ship a service-role key in the app.
