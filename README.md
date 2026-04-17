# Excelsior

Excelsior is a specialized, offline-first desktop application designed to help you organize, filter, and track your video game collection. Built with a sleek, modern interface, it gives you complete control over your backlog without relying on bloated game launchers.

<img width="1920" height="1080" alt="excelsior_gui" src="https://github.com/user-attachments/assets/24bee024-b51a-4f36-b404-470ded7ad07e" />

## Features
* **Custom Library Management:** Add, edit, and categorize your games by playtime, release date, and completion status.
* **Dynamic Cover Art:** Automatic integration with SteamGridDB to instantly fetch and apply high-resolution cover art for your entire library.
* **Advanced Filtering:** Quickly sort through your backlog with robust filtering and a dedicated spreadsheet view.
* **Fully Portable Data:** Your library is saved locally as a simple, human-readable file, meaning you completely own your data.
* **Different Views:** You can switch between two different views: the primary high-quality cover art view, and the secondary spreadsheet-like table view.

<img width="1920" height="1080" alt="excelsior_gui_spreadsheet" src="https://github.com/user-attachments/assets/95524792-0ca6-499f-abd5-8621bfbf9a1e" />

## Installation
1. Navigate to the [Releases page](https://github.com/AbdulrahmanAlaamry/Excelsior/releases/latest).
2. Download the latest **`Excelsior_Setup.exe`**.
3. Run the installer to unpack the application and create a desktop shortcut. 
   * *Note: As an indie application, Windows SmartScreen may display an "Unknown Publisher" warning. Click **More info** -> **Run anyway**.*

## First-Time Setup (API Key)
To keep the application lightweight and free, Excelsior uses BYOK for automatic cover art downloads. You will need a free API key from SteamGridDB.

1. Create a free account at [SteamGridDB.com](https://www.steamgriddb.com/).
2. Generate a personal API key in your account profile.
3. Open Excelsior and you will be prompted to enter the API key.

> **Advanced: Manual Configuration**
> If you prefer to set up your key manually or need to edit it outside of the app:
> 1. Open Excelsior at least once to generate the local `~/.excelsior/` folder (typically located at `C:\Users\YourUsername\.excelsior\`).
> 2. Open the `config.properties` file inside that folder.
> 3. Paste your API key into the file (e.g., `STEAMGRID_API_KEY=your_key_here`), save it, and restart the application.

## Privacy & Data
Excelsior is an offline-first application. Your library data and API key never leave your computer, and no telemetry or tracking data is collected.

## Feedback & Suggestions
Excelsior is an actively maintained project. If you have an idea for a new feature, a suggestion for the UI, or if you run into a bug, [I would love to hear about it!](https://github.com/AbdulrahmanAlaamry/Excelsior/issues)
