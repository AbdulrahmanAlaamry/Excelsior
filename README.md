# Excelsior

Excelsior is a specialized, offline-first desktop application designed to help you organize, filter, and track your video game collection. Built with a sleek, modern interface, it gives you complete control over your backlog without relying on bloated game launchers.

<img width="3834" height="2033" alt="excelsior_gui" src="https://github.com/user-attachments/assets/24bee024-b51a-4f36-b404-470ded7ad07e" />

## Features
* **Custom Library Management:** Add, edit, and categorize your games by playtime, release date, and completion status.
* **Dynamic Cover Art:** Automatic integration with SteamGridDB to instantly fetch and apply high-resolution cover art for your entire library.
* **Advanced Filtering:** Quickly sort through your backlog with robust filtering and a dedicated spreadsheet view.
* **Fully Portable Data:** Your library is saved locally as a simple, human-readable file, meaning you completely own your data.

## Installation
1. Navigate to the [Releases page](https://github.com/AbdulrahmanAlaamry/Excelsior/releases/latest).
2. Download the latest **`Excelsior_Setup.exe`**.
3. Run the installer to unpack the application and create a desktop shortcut. 
   * *Note: As an indie application, Windows SmartScreen may display an "Unknown Publisher" warning. Click **More info** -> **Run anyway**.*

## First-Time Setup (API Key)
To keep the application lightweight and free, Excelsior uses BYOK for automatic cover art downloads. You will need a free API key from SteamGridDB.

1. Create a free account at [SteamGridDB.com](https://www.steamgriddb.com/).
2. Generate a personal API key in your account profile.
3. Open Excelsior for the first time. The app will generate a `config.properties` file in your `~/.excelsior/` folder (typically `C:\Users\YourUsername\.excelsior\`).
4. Paste your API key into that file, save it, and restart the app. Cover art downloads will now be fully unlocked!

## Privacy & Data
Excelsior is an offline-first application. Your library data and API key never leave your computer, and no telemetry or tracking data is collected.
