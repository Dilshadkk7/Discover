# Discover - Network Device Discovery App

This is an Android application built to satisfy the requirements of an interview assignment. The app discovers devices on a local Wi-Fi network, displays them in a list, and allows the user to view details about their public IP address.

## Features

*   **OAuth 2.0 Login:**
    *   Basic login screen with token caching for silent authentication.
    *   Handles network failures during silent authentication with a forced logout.
*   **Network Device Discovery (mDNS):**
    *   Discovers devices on the local Wi-Fi network using the mDNS protocol.
    *   Displays discovered devices in a RecyclerView, showing their name and local IP address.
    *   Persists discovered devices in a local SQLite database using Room.
    *   Loads devices from the database on launch and updates their online/offline status based on real-time discovery.
*   **Device Details:**
    *   Navigate to a detail screen by tapping on a device.
    *   Fetches the user's public IP address.
    *   Displays geological information (City, Region, Country) based on the public IP.
    *   Includes a functional back button for navigation.

## Technology Stack

*   **Language:** Kotlin
*   **UI:** XML
*   **Architecture:** MVVM (Model-View-ViewModel)
*   **Core Libraries:**
    *   **Coroutines & Flow:** For asynchronous operations.
    *   **ViewModel & LiveData:** For managing UI-related data in a lifecycle-conscious way.
    *   **Room:** For local database storage (SQLite).
    *   **NsdManager:** For mDNS (Network Service Discovery).
    *   **HttpURLConnection:** For making raw API calls without third-party libraries.
    *   **Material Components:** For modern UI elements.
    *   **View Binding:** To safely access views.

## APIs Used

*   **Public IP Address:** `https://api.ipify.org?format=json`
*   **Geological Information:** `https://ipinfo.io/<IP_ADDRESS>/geo`

## Setup & Testing

1.  Clone the repository.
2.  Open the project in Android Studio.
3.  Build and run the app on an Android device or emulator.

### Important Note on Testing Device Discovery

The app discovers services advertised via mDNS. By default, most devices are silent. To test this feature, you need a device on the same Wi-Fi network that is advertising a service.

*   **On a Mac:** The simplest way is to enable AirPlay Receiver.
    1.  Go to **System Settings** > **General** > **AirDrop & Handoff**.
    2.  Turn on **AirPlay Receiver**.
    3.  The app is currently configured to look for `_airplay._tcp.`, which will discover your Mac.
*   **On Windows:** The project requirements suggest using a tool like **ApowerMirror**, which advertises its presence on the network.
