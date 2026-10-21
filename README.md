# Barakah Travel & Tours – Integrated Umrah Management System

## Project Overview

Barakah Travel & Tours is an Umrah travel management system developed to help organise customer information, travel applications, supporting documents, bookings and travel-related services.

The project consists of an Android staff application, an ASP.NET Core Web API, a customer-facing website and an Azure SQL relational database.

The system aims to reduce manual administration, improve access to customer records and provide a more organised approach to managing Umrah travel services.


## Task 2 – Video Demonstration

A demonstration of the Barakah Travel & Tours
Integrated Umrah Management System, including the
Android staff application, Firebase Google Sign-In,
customer management, application tracking,
document verification, settings, and system integration.

**Watch the demonstration on YouTube:**
https://youtu.be/G3n2f-ZlwRU


## Project Objectives

- Provide a centralised system for managing Umrah-related information.
- Allow authorised staff to securely access customer and application records.
- Connect the Android application to a backend API and relational database.
- Support the management and verification of customer documents.
- Improve the usability and efficiency of administrative processes.
- Apply version control and automated build testing during development.

## System Components

### 1. Android Staff Application

Developed using **Kotlin and Jetpack Compose** in Android Studio.

Implemented features include:

- Firebase Google Sign-In authentication.
- Staff dashboard displaying live database record counts.
- Customer records retrieved through the API.
- Travel application records and application management.
- Customer document listing and secure document viewing.
- Settings screen with user preferences, staff information and sign-out.
- Navigation between administrative sections.

Additional sections for packages, bookings, payments and travel schedules are included in the application structure but are not yet fully integrated.

The application has been tested on a physical Samsung Galaxy S26 device.

### 2. ASP.NET Core Web API

Developed using **C# and ASP.NET Core 8**.

The API provides the communication layer between the applications and the Azure SQL database.

Its responsibilities include retrieving and managing system information, supporting administrative operations and handling requests from the connected applications.

### 3. Customer Website

A customer-facing website is included in the repository.

The website provides a web-based interface for the customer side of the Umrah travel management system.

### 4. Azure SQL Database

The system uses **Microsoft Azure SQL Database** to store relational data.

The database contains 11 tables covering:

- Customers
- CustomerAccounts
- CustomerRegistrationCodes
- Users
- Applications
- Documents
- Packages
- Bookings
- Payments
- TravelDetails
- TravelSchedules

The database structure includes primary keys and foreign-key relationships to support data integrity.

## Technologies Used

| Component | Technology |
|---|---|
| Android application | Kotlin, Jetpack Compose |
| Android development | Android Studio |
| Authentication | Firebase Authentication, Google Sign-In |
| Backend API | C#, ASP.NET Core 8 |
| Customer website | ASP.NET Core, Razor, HTML, CSS, JavaScript |
| Database | Microsoft Azure SQL Database |
| Backend development | Visual Studio |
| Version control | Git, GitHub, Azure DevOps |
| Automated testing | GitHub Actions, Gradle |
| API connectivity | HTTPS and development tunnelling |

## Repository Structure

The repository includes:

- `app/` – Android application source code and resources.
- `gradlew` – Gradle wrapper script.
- `settings.gradle.kts` – Android Gradle project configuration.
- `BarakahTravel.API/` – ASP.NET Core backend API.
- `BarakahTravel.Website/` – Customer-facing website.
- `database/` – Database documentation and schema export.
- `.github/workflows/android-ci.yml` – GitHub Actions workflow.
- `README.md` – Project documentation.

Additional project files and supporting resources are also included.

## Installation and Setup

### Android Staff Application

1. Clone the GitHub repository.
2. Open the repository as a project in Android Studio.
3. Allow Gradle to synchronise the project dependencies.
4. Configure Firebase using the appropriate `google-services.json` file.
5. Ensure the backend API is accessible and configured correctly.
6. Connect an Android device or start a compatible emulator.
7. Build and run the Android application.

**Note:** Firebase configuration files and private credentials should be managed securely and must not be exposed publicly.

### ASP.NET Core API

1. Open the API project in Visual Studio.
2. Ensure the .NET 8 SDK is installed.
3. Configure the database connection using secure application configuration.
4. Ensure the Azure SQL database is accessible.
5. Build and run the API.
6. Verify that the Android application can communicate with the API.

### Customer Website

1. Open the website project in Visual Studio.
2. Restore the required .NET dependencies.
3. Configure the website's API and application settings.
4. Build and run the website.

Some functionality requires the API, database and authentication services to be available.

## Automated Testing and Continuous Integration

The project uses **GitHub Actions** for automated Android builds and testing.

The workflow is located at:

`.github/workflows/android-ci.yml`

The workflow runs when:

- Changes are pushed to the `main` branch.
- A pull request targets `main`.
- The workflow is started manually.

### Workflow Steps

1. Check out the repository.
2. Configure Java 17.
3. Restore Firebase configuration from a GitHub Actions secret.
4. Run the Android unit-test task.
5. Build the Android debug APK.

### Gradle Commands

Run Android unit tests:

```bash
./gradlew :app:testDebugUnitTest --no-daemon
```

Build the debug APK:

```bash
./gradlew :app:assembleDebug --no-daemon
```

### Continuous Integration Status

The Android build and test workflow has completed successfully on GitHub Actions.

The successful run confirms that the configured Android build and test tasks execute in the CI environment. It does not replace manual testing of application functionality.

## Testing and Validation

Testing activities include:

- Running the Android application on a physical Samsung Galaxy S26.
- Verifying Firebase Google Sign-In.
- Checking dashboard data retrieved from the API.
- Testing customer and application information screens.
- Checking document access and viewing.
- Testing settings and sign-out functionality.
- Verifying communication with the ASP.NET Core API.
- Checking relational database structure and data.
- Running automated Android build and test tasks through GitHub Actions.

## Security Considerations

The system incorporates authentication and controlled access to application functionality.

Sensitive configuration values, database passwords and private credentials should not be committed to public repositories.

Firebase configuration required for automated builds is supplied through GitHub Actions repository secrets.

Customer documents and personal information must be handled securely.

## Version Control and Collaboration

Git is used to track development changes.

**Azure DevOps** is used for project source control and collaboration, while **GitHub** contains the integrated project and GitHub Actions workflow.

Pull requests and commits provide a record of project development and integration.

## Future Improvements

Potential future enhancements include:

- Full Android integration of package management.
- Complete booking and payment management screens.
- Travel schedule management.
- Automated customer notifications.
- Online payment integration.
- Improved reporting and administrative analytics.
- Additional automated unit and integration tests.

## Conclusion

The Barakah Travel & Tours Integrated Umrah Management System combines Android, web, API and database technologies to support travel administration.

The project demonstrates mobile application development, backend API integration, relational database design, authentication, source control and automated build testing.

The implemented system provides a foundation for expanding and improving Umrah travel management services.
