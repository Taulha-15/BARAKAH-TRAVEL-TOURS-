# Barakah Travel & Tours – Integrated Umrah Management System

**Module:** XISD6329 – Work Integrated Learning 3B  

**Team Members:**
- Aneeqah Fisher (ST10454674)
- Taulha Basardien (ST10457106)
- Ingvar Thor Hultkrantz (ST10434704)

---

## Demonstration Video

youtube link:https://youtu.be/SPgfXXcivIg?si=JehzrTYxFq_a7WyZ

The demonstration video shows the Android staff application running on a physical Android phone including all feautures.

---

## 1. Project Overview

Barakah Travel & Tours is an Umrah travel service provider. Before this system was introduced, staff relied on manual processes to collect customer information, manage supporting documents and organise travel arrangements.

The **Barakah Travel & Tours Integrated Umrah Management System** was developed to improve these processes by providing a centralised digital platform for customers and staff.

The system consists of:

- **Customer Website:** Allows registered customers to complete their personal information, select travel packages, upload required documents and track their applications.
- **Android Staff Application:** Allows authorised staff to manage customers, applications, documents, travel packages, bookings, payments and travel schedules.
- **ASP.NET Core Web API:** Handles communication between the website, Android application and database.
- **Azure SQL Database:** Stores the information shared by the website and staff application.

Both platforms communicate with the same API and database, reducing duplicate data entry and helping maintain consistent information.

**Target users:** Barakah Travel & Tours customers (pilgrims) and authorised staff/administrators.

---

## 2. System Design and Considerations

### Single Source of Truth
The customer website and Android staff application use the same ASP.NET Core API and Azure SQL database. Information submitted through the website can therefore be accessed by authorised staff through the staff application.

### Authentication and Access Control
The system uses two authentication approaches:

- **Staff:** Google Sign-In through Firebase Authentication. The API verifies Firebase ID tokens and checks that the staff member is active and has an authorised Admin or Staff role in the Users table.
- **Customers:** Registration-code onboarding followed by an email/password account. Customer passwords are stored using ASP.NET Core Identity password hashing.

### Privacy and Security
Customer information and uploaded documents are restricted to authorised users. Passwords are not stored as plain text. The database schema export does not contain actual customer records or passwords.

### Usability
The system includes clear navigation, input validation, helpful error messages and separate interfaces for customers and staff.

### Maintainability
The project is organised into separate folders for the Android application, API, website, database and documentation. GitHub Actions is used to automate Android build and unit-test checks.

---

## 3. Main System Features

### 3.1 Customer Website

Customers can:

- Access the website and view available travel packages.
- Register using a registration code issued by staff.
- Create an account using their email address and password.
- Complete their personal information.
- Select an available Umrah travel package.
- Upload required supporting documents.
- Track their application progress.

The website upload process includes additional document requirements:
- An unabridged birth certificate for children under 18.
- Two passport-sized colour photographs with a white background for visa purposes.

### 3.2 Android Staff Application

The Android staff application provides the following functionality:

| Feature | Description |
|---|---|
| Staff Login | Secure Google Sign-In through Firebase Authentication. |
| Dashboard | Displays customer, application, document and booking counts retrieved from the API. |
| Customers | Allows staff to view and manage customer information. |
| Applications | Allows staff to review applications and update their statuses. |
| Documents | Allows staff to view, review, accept or reject supporting documents. |
| Packages | Allows staff to add, edit and deactivate travel packages. |
| Bookings | Allows staff to view bookings, create bookings from eligible approved applications and update booking statuses. |
| Payments | Allows staff to record payments linked to bookings and update payment statuses. |
| Travel Schedules | Allows staff to create and edit travel schedules, including dates, times, airports and flight information. |
| Settings | Provides access to staff profile information and application preferences. |

### 3.3 Payment Management

Staff can record payment information, including:

- Booking reference.
- Payment amount.
- Payment date.
- Payment method.
- Payment reference number.
- Additional notes.

Payment statuses include Recorded, Pending, Verified and Rejected.

### 3.4 Travel Schedule Management

Staff can maintain travel information linked to customer bookings, including:

- Departure and return dates.
- Departure and return times.
- Departure and arrival airports.
- Airline and flight number.
- Travel schedule status.
- Additional notes.

Schedule statuses include Scheduled, Confirmed, Completed and Cancelled.

---

## 4. Screenshots and Wireframes

Screenshots of the Android staff application and customer website are stored in the `docs/images/` directory

## 5. Repository Structure

```text
BARAKAH-TRAVEL-TOURS/
│
├── app/
│   ├── src/main/              Android application source and resources
│   └── src/test/              Unit tests
│
├── BarakahTravel.API/         ASP.NET Core 8 Web API
│
├── BarakahTravel.Website/     ASP.NET Core Razor Pages website
│
├── database/
│   ├── Schema export (.dacpac)
│   ├── README
│   └── seed_customer_accounts.sql
│
├── prototype/                 Task 1 HTML/CSS/JavaScript prototype
│
├── docs/
│   ├── images/                 Application and website screenshots
│   ├── images/wireframes/      Website wireframes
│   └── Btt staff application guide.pdf
│
└── .github/
    └── workflows/
        └── android-ci.yml      GitHub Actions workflow
---

## 6. Technologies Used

| Component | Technology |
|---|---|
| Android Staff Application | Kotlin, Jetpack Compose |
| Android Networking | Retrofit |
| Local Preferences | DataStore |
| Staff Authentication | Firebase Authentication, Google Sign-In |
| Customer Authentication | ASP.NET Core Identity |
| Backend API | C#, ASP.NET Core 8 |
| Data Access | Entity Framework Core |
| Customer Website | ASP.NET Core Razor Pages, HTML, CSS, JavaScript, Bootstrap |
| Database | Microsoft Azure SQL Database |
| Database Management | SQL Server / Azure SQL |
| Version Control | Git and GitHub |
| Project Management | Azure DevOps / Azure Boards |
| Continuous Integration | GitHub Actions |
| Automated Testing | JUnit |

---

## 7. Installation and Setup

### 7.1 Android Staff Application

1. Clone the GitHub repository.
2. Open the project in Android Studio.
3. Allow Gradle to sync and download the required dependencies.
4. Add your Firebase `google-services.json` file to the `app/` directory.
5. Configure the API address in `app/build.gradle.kts` using `API_BASE_URL` for debug builds.
6. Configure `API_BASE_URL_RELEASE` in `gradle.properties` for release builds.
7. Connect a physical Android device or use an emulator.
8. Run the application and sign in using an authorised staff Google account.

**Important:** Do not commit `google-services.json`, private keys or other sensitive configuration files to the public repository.

### 7.2 ASP.NET Core API

1. Open `BarakahTravel.API.sln` in Visual Studio.
2. Ensure that the .NET 8 SDK is installed.
3. Configure the `DefaultConnection` connection string using user secrets or environment variables.
4. Run the API.
5. In Development mode, open Swagger UI to view and test the available API endpoints.

### 7.3 Customer Website

1. Open the `BarakahTravel.Website` project in Visual Studio.
2. Configure the API base address in the application configuration.
3. Ensure that the API and database are accessible.
4. Run the website and test the customer registration and application process.

### 7.4 Database

The database schema can be created using:

`database/BarakahTravelD_Schema.dacpac`

Optional simulated customer accounts can be added using:

`database/seed_customer_accounts.sql`

The seed script provides simulated customer accounts for testing purposes.

---

## 8. GitHub and Continuous Integration

The project uses GitHub for source control and GitHub Actions for automated Android build and testing checks.

The workflow is located at:

`.github/workflows/android-ci.yml`

The workflow runs when changes are pushed or pull requests are made to the `main` branch. It can also be started manually.

The workflow performs the following steps:

1. Checks out the repository.
2. Sets up Java 17.
3. Restores Firebase configuration using the `GOOGLE_SERVICES_JSON` GitHub secret.
4. Runs Android unit tests.
5. Builds the Android debug application.

Commands used by the workflow can also be run locally:

**Run unit tests:**

```bash
./gradlew :app:testDebugUnitTest --no-daemon
```

**Build the debug APK:**

```bash
./gradlew :app:assembleDebug --no-daemon
```

---

## 9. Automated Testing

The Android application includes unit tests located at:

`app/src/test/java/com/barakahtravel/staff/ValidationTest.kt`

The test class contains 25 JUnit test cases covering:

- Email validation.
- Password validation.
- Phone number validation.
- Name validation.
- Registration-code validation.

The tests include valid, invalid and boundary-case inputs.

Additional API and repository-level automated tests are identified as future improvements.

---

## 10. Release Notes

## Final Submission

**New features and improvements:**

- Added additional customer document requirements to the website upload form and API validation, including an unabridged birth certificate for children under 18 and two passport-sized colour photographs.
- Added package management functionality to the Android staff application, allowing staff to add, edit and deactivate packages.
- Added booking management functionality, including viewing bookings, creating bookings from eligible approved applications and updating booking statuses.
- Added payment management functionality, allowing staff to record payments and update payment statuses.
- Added travel schedule management functionality, allowing staff to create and edit travel schedules linked to bookings.
- Connected the dashboard booking summary to the bookings API so that the total booking count can be displayed.
- Added release signing configuration and a separate release API address for Play Store builds.
- Updated the application version to 1.1 (versionCode 2).
- Added automated Android unit tests to the GitHub Actions workflow.
- Added `seed_customer_accounts.sql` for simulated customer account testing.

**Fixes and maintenance:**

- Fixed the coroutine scope warning that appeared when navigating between screens by correctly rethrowing cancellation exceptions.
- Removed duplicate Kotlin files from the repository.
- Moved the Task 1 staff portal prototype into the `prototype/` directory.
- Improved the project README with updated system functionality, setup instructions, testing and publishing guidance.

### Task 2 Prototype

The initial integrated system included:

- Android staff application with sign-in, dashboard, customers, applications, documents and settings.
- ASP.NET Core Web API.
- Razor Pages customer website.
- Azure SQL database schema.
- Initial GitHub Actions workflow.

---

## 11. Known Limitations and Future Improvements

Although the main system features have been implemented, several improvements can be considered for future development:

- Complete automatic push notification delivery using Firebase Cloud Messaging (FCM), including device-token registration and backend notification sending.
- Introduce Google Sign-In as an additional authentication option for customers.
- Expand API and repository-level automated test coverage.
- Move development data-protection keys and development tunnel URLs into secure hosting configuration.
- Improve offline access and synchronisation where required.
- Continue improving application usability based on staff and customer feedback.

**Notification note:** The current Android application includes a notification preference in Settings, but automatic push notification delivery has not been fully implemented.

---

## 13. User Documentation

The Staff App User Guide provides instructions for using the Android staff application.

It covers:

- Staff sign-in.
- Dashboard navigation.
- Customer management.
- Application review.
- Document verification.
- Travel package management.
- Booking management.
- Payment management.
- Travel schedule management.
- Settings and troubleshooting.

---

## 14. Conclusion

The Barakah Travel & Tours Integrated Umrah Management System provides a centralised solution for managing customer information and important travel operations.

By connecting the customer website and Android staff application to a shared ASP.NET Core API and Azure SQL database, the system reduces duplicate data entry and improves the accessibility and organisation of business information.

The project demonstrates the practical application of mobile development, web development, database management, authentication, API integration, testing and version control within a real-world business context.

**Module:** XISD6329 – Work Integrated Learning 3B  
**Project:** Barakah Travel & Tours – Integrated Umrah Management System  

