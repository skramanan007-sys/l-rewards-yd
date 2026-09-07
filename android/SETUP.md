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

For production, connect the UI actions to Supabase Auth and the RPC using the publishable key only. Reward amounts, daily limits, balances, and ledger writes must remain server-side; do not update `profiles.coins` directly from the client.

## Build APK

From Android Studio use **Build > Generate App Bundles or APKs**. The GitHub Actions workflow at `.github/workflows/android.yml` builds and uploads a debug APK whenever files under `android/` change. Configure release signing in CI before publishing a release artifact.

## Admin access

Add admin authorization using Supabase `app_metadata` or a dedicated admin table and RLS policies. Do not use editable `user_metadata` for authorization decisions and do not ship a service-role key in the app.
