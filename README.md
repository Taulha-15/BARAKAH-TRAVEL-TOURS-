# Barakah Staff Android App

Kotlin + Jetpack Compose starter project for the Barakah Travel staff administration app.

## Included
- Staff portal demo login
- Dashboard
- Customer search, edit form and simulated registration-code generation
- Application status demo controls
- Documents, packages, bookings, payments and travel schedule sample screens
- Settings screen
- Clear demo-mode notices

## Important
This build uses in-memory sample data. The login accepts any non-empty username and password and is **not authentication**. Registration codes are generated locally and are **not persisted or valid for the website**. Do not use this build to manage real customer data. Connect the existing .NET API, staff authentication, authorization and database before production use.

## Open in Android Studio
1. Extract the ZIP.
2. In Android Studio select **Open** and choose the `BarakahStaffApp` folder.
3. Allow Gradle sync. Use JDK 17 and install Android SDK 35 if Android Studio requests it.
4. Run on an emulator or connected Android phone.

If Android Studio reports that the Android Gradle Plugin version is unavailable in your setup, use the IDE's suggested compatible upgrade, keeping Kotlin and Compose plugin versions aligned.
