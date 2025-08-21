# PhotoSender

PhotoSender is an Android application designed to automatically monitor your device's gallery for new photos and send them to a specified Telegram bot. This project was built from scratch to provide a transparent, open-source tool for this purpose.

## Features

- **Automatic Photo Monitoring**: Runs a background service to detect newly saved images.
- **Telegram Integration**: Securely sends new photos to the Telegram bot of your choice.
- **Configurable**: Easily set your Telegram Bot Token and Chat ID within the app.
- **Modern UI**: A simple, clean user interface inspired by modern Android design patterns.
- **Built with Modern Android**: Uses Kotlin, Coroutines, and Retrofit for efficient and reliable performance.
- **Transparent Operation**: Runs as a standard Android Foreground Service, ensuring the user is always aware of its operation via a persistent notification, as required by modern Android security policies.

## Prerequisites

To compile and run this project, you will need:
- [Android Studio](https://developer.android.com/studio) (latest stable version recommended).
- A Java Development Kit (JDK), which is typically bundled with Android Studio.
- An Android device or emulator running Android 8.0 (Oreo, API 26) or higher.

## Setup & Compilation

Follow these steps to build the application from the source code.

1.  **Get the Source Code**
    You can either clone this repository using git or download the source code as a ZIP file.
    ```bash
    git clone <repository-url>
    ```

2.  **Open in Android Studio**
    - Launch Android Studio.
    - Select "Open an Existing Project" (or "Open" in newer versions).
    - Navigate to the cloned/downloaded `PhotoSender` directory and select it.
    - Android Studio will automatically sync the Gradle project. This may take a few minutes.

3.  **Build the APK**
    - Once the project has synced, go to the menu `Build` -> `Build Bundle(s) / APK(s)` -> `Build APK(s)`.
    - Android Studio will compile the code and generate an APK file.
    - When the build is complete, a notification will appear. Click "locate" to find the generated `app-debug.apk` file in the `PhotoSender/app/build/outputs/apk/debug/` directory.

4.  **Install the APK**
    - You can either run the app directly on a connected device/emulator from Android Studio by clicking the "Run" button (a green play icon).
    - Or, you can transfer the `app-debug.apk` file to your Android device and install it manually.

## Configuration

Before the app can work, you need to provide it with a Telegram Bot Token and your Chat ID.

#### Step 1: Create a Telegram Bot

1.  Open Telegram and search for `@BotFather`.
2.  Start a chat with BotFather and send the `/newbot` command.
3.  Follow the instructions to choose a name and username for your bot.
4.  BotFather will provide you with a **Bot Token**. It will look something like `1234567890:ABCdEfGhIjKlMnOpQrStUvWxYz`. **Copy this token.**
5.  Click the link to your new bot and press "Start".

#### Step 2: Get Your Chat ID

1.  In Telegram, search for the bot `@userinfobot`.
2.  Start a chat with it. It will immediately send you a message containing your **numeric User ID**. This is your Chat ID.
3.  **Copy your numeric Chat ID.**

#### Step 3: Configure the App

1.  Open the PhotoSender app on your phone for the first time.
2.  You will see a welcome screen. Tap "Go to Settings".
3.  Paste your **Bot Token** into the "Telegram Bot Token" field.
4.  Paste your **Chat ID** into the "Telegram Chat ID" field.
5.  Tap "Save".

## Usage

After saving the settings, the app will navigate to the main screen, request necessary permissions (for reading photos and showing notifications), and start the monitoring service.

A persistent notification will appear in your notification drawer, indicating that "PhotoSender Service" is running. This is a mandatory Android security feature and ensures you are aware of the app's background activity.

Now, whenever you save a new photo to your device (e.g., a screenshot or a camera picture), the service will detect it and upload it to your Telegram bot. You will see the photos appear in your private chat with the bot.
