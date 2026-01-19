# Car Settings AIDL Service

A multi-module Android project demonstrating AIDL-based IPC patterns for Android Automotive OS (AAOS).

## Modules

- **`:aidl-interfaces`**: Defines the AIDL interfaces and constants for vehicle property management.
- **`:common`**: Shared utility classes (Logger, Result, etc.).
- **`:car-settings-service`**: Background service that implements the AIDL interfaces and manages mock vehicle properties.
- **`:car-settings-client`**: Client application with a UI to interact with the service, demonstrating synchronous and asynchronous IPC calls.

## Key Features

- **IPC Communication**: Uses AIDL for communication between the client and service.
- **Property Management**: Implements `getProperty` and `setProperty` logic with area support (Global, Seat).
- **Callbacks**: Demonstrates `ICarSettingsCallback` for real-time property change notifications.
- **Profile Management**: Mock driver profile switching and notifications.
- **Hilt Dependency Injection**: Modern Android architecture with Hilt for DI and ViewModels in the client.

## How to Build

The project is structured as a standard Android Gradle project. Since the Gradle wrapper is missing in this repository, you should initialize it or use your local Gradle installation:

```bash
gradle assembleDebug
```

## Usage

1. Install both `car-settings-service` and `car-settings-client`.
2. Start the `Car Settings Client` app.
3. The client will automatically bind to the background service.
4. Interact with the UI to change brightness, HVAC temperature, and night mode.
5. Changes are sent to the service via IPC, and reflected back to the client via callbacks.
