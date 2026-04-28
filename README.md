# CampusServiceApp (CampusConnect)

CampusServiceApp is a comprehensive, feature-rich Android application designed specifically for university students. It serves as a unified platform for social networking, peer-to-peer commerce, academic productivity, and real-time communication within a campus community.

## 📱 Features

The application is modularized into several core domains to provide a super-app experience:

* **Social Feed:** A dynamic timeline where students can share text and image posts, interact via upvotes and comments, and stay updated with campus events.
* **Marketplace:** A dedicated space for students to buy, sell, or exchange items within the campus. Includes a "Lost & Found" section.
* **Real-time Chat & Social:** Connect with peers, send friend requests, and communicate instantly through real-time messaging.
* **Opportunities:** A hub for discovering campus jobs, internships, research positions, and other career-building opportunities.
* **Productivity Tools:** Built-in utilities such as a Pomodoro timer and task management features to help students stay focused on academics.
* **User Profiles & Notifications:** Customizable user profiles with an integrated notification system to keep track of social interactions and updates.

## 🛠️ Technology Stack

* **Language:** Java (Android SDK)
* **Architecture:** MVVM (Model-View-ViewModel) with Android Architecture Components (LiveData, ViewModel).
* **UI & Navigation:** 
  * ViewBinding for safe UI element interactions.
  * Jetpack Navigation Component for managing fragment transitions.
  * Material Design Components.
* **Networking & API:** 
  * Retrofit2 & OkHttp3 for robust network calls.
  * Gson for JSON serialization/deserialization.
* **Image Loading:** Glide
* **Backend Integration:** **Supabase** (PostgreSQL Database, Authentication, and Cloud Storage for multi-part image uploads).

## 🚀 Getting Started

### Prerequisites
* Android Studio (Koala or newer recommended)
* JDK 11 or higher
* A Supabase project

### Setup Instructions

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   ```

2. **Open the project in Android Studio:**
   Navigate to the cloned directory and open it as an Android Studio project.

3. **Configure Environment Variables:**
   To protect sensitive credentials, this project uses an `.env` file that is excluded from version control via `.gitignore`.
   * Create a file named `.env` in the root directory of the project.
   * Add your Supabase credentials to the `.env` file:
     ```properties
     SUPABASE_URL=https://your-project-id.supabase.co
     SUPABASE_ANON_KEY=your-supabase-anon-key
     ```
   * The `app/build.gradle.kts` is already configured to read these properties and inject them into the `BuildConfig` class.

4. **Database Setup:**
   Run the SQL statements provided in the `supabase_setup.sql` file within your Supabase project's SQL Editor to set up the necessary tables, policies, and functions.

5. **Build and Run:**
   Sync the project with Gradle files and click the "Run" button in Android Studio to launch the app on an emulator or physical device.

## 🔒 Security Note
Never commit your `.env` or `secrets.properties` files to version control. The `.gitignore` has been configured to ignore these files automatically.

## 📄 License
This project is licensed under the terms provided in the `LICENSE` file.
