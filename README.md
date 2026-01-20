# 🚗 Car Settings AIDL Service

[![Android CI](https://github.com/username/car-settings-aidl-service/workflows/Android%20CI/badge.svg)](https://github.com/username/car-settings-aidl-service/actions)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![API](https://img.shields.io/badge/API-29%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=29)

A **production-ready**, multi-module Android project demonstrating **AIDL-based IPC patterns** for Android Automotive OS (AAOS). This project showcases a complete service-client architecture where a background service exposes vehicle preferences through AIDL interfaces, mirroring the design patterns used in real AAOS services like `CarPropertyService` and `CarHvacManager`.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Module Structure](#module-structure)
- [AAOS Patterns Demonstrated](#aaos-patterns-demonstrated)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Build & Installation](#build--installation)
- [Usage Guide](#usage-guide)
- [Key Features](#key-features)
- [Project Structure](#project-structure)
- [Documentation](#documentation)
- [Testing](#testing)
- [Contributing](#contributing)
- [License](#license)

---

## 🎯 Overview

This project demonstrates **production-quality** implementation of:

- ✅ **AIDL-based IPC** between service and client processes
- ✅ **Property-based vehicle configuration** management (47 properties across 6 categories)
- ✅ **Thread-safe operations** with proper synchronization
- ✅ **Real-time callbacks** for property change notifications
- ✅ **Driver profile management** with persistent storage
- ✅ **Client death handling** and automatic reconnection
- ✅ **Modern Android architecture** (Hilt, Coroutines, Flow, DataStore)

**Target Audience:** Android developers learning AAOS, and teams building automotive applications.

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Client Process                          │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  UI Layer (Fragments + ViewModels)                     │ │
│  │  • Display Settings  • Dashboard                       │ │
│  └───────────────────┬────────────────────────────────────┘ │
│                      │                                       │
│  ┌───────────────────▼────────────────────────────────────┐ │
│  │  CarSettingsManager                                    │ │
│  │  • ServiceConnection + Auto-reconnect                  │ │
│  │  • StateFlow<ConnectionState>                          │ │
│  │  • Flow-based property observation                     │ │
│  └───────────────────┬────────────────────────────────────┘ │
└────────────────────────────────────────────────────────────┘
                       │
                 ══════╪══════  AIDL/Binder IPC
                       │
┌────────────────────────────────────────────────────────────┐
│                     Service Process                         │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  CarSettingsService (Foreground Service)              │ │
│  │  └─► CarSettingsServiceImpl (ICarSettingsService.Stub)│ │
│  └───────────────────┬────────────────────────────────────┘ │
│                      │                                       │
│     ┌────────────────┼────────────────┐                     │
│     │                │                │                     │
│  ┌──▼────┐   ┌──────▼─────┐   ┌─────▼──────┐              │
│  │Property│   │Property    │   │Client      │              │
│  │Store   │   │Registry    │   │Manager     │              │
│  │(Values)│   │(Metadata)  │   │(Tracking)  │              │
│  └────────┘   └────────────┘   └────────────┘              │
│                                                             │
│  ┌──────────────┐      ┌────────────────────┐             │
│  │HAL Simulator │      │DeathRecipient Mgr  │             │
│  │(Mock updates)│      │(Crash handling)    │             │
│  └──────────────┘      └────────────────────┘             │
└────────────────────────────────────────────────────────────┘
```

**See [ARCHITECTURE.md](ARCHITECTURE.md) for detailed sequence diagrams, threading model, and design decisions.**

---

## 📦 Module Structure

### 1. **`:aidl-interfaces`** - Shared AIDL Definitions

Defines the contract between service and client:

- **AIDL Interfaces:**
  - `ICarSettingsService.aidl` - Main service interface (18 methods)
  - `ICarSettingsCallback.aidl` - Callback for property changes (oneway methods)
  - `IPropertyEventListener.aidl` - Lightweight single-property listener

- **Parcelable Types:**
  - `PropertyValue` - Vehicle property value with metadata
  - `CarPropertyInfo` - Property metadata (type, access mode, permissions)
  - `CarPropertyConfig` - Property configuration with area support
  - `AreaConfig` - Area-specific constraints (min/max values)
  - `CarSettingsError` - Structured error information

- **Constants (Kotlin):**
  - `PropertyConstants.kt` - **47 property IDs** across 6 groups:
    - 🖥️ Display (8): brightness, theme, language, night mode, etc.
    - 🔊 Audio (13): volume controls, balance, fade, EQ presets
    - 🌡️ HVAC (12): temperature, fan speed, AC, seat heating, defrost
    - 🚙 Vehicle (12): units, door locks, headlights, ambient lighting
    - 👤 Profile (6): driver profiles, seat/mirror positions
    - 🔒 Privacy (6): location, analytics, voice data permissions
  - `AreaConstants.kt` - Seat, door, window, mirror, wheel area IDs
  - `ErrorCodes.kt`, `ServiceStatus.kt`, `AccessMode.kt`, etc.

### 2. **`:car-settings-service`** - Background Service

The service implementation that manages vehicle properties:

**Core Components:**
- `CarSettingsService.kt` - Android Service entry point (foreground service)
- `CarSettingsServiceImpl.kt` - AIDL Stub implementation (382 lines)
  - RemoteCallbackList for thread-safe callbacks
  - ReentrantReadWriteLock for property synchronization
  - Binder caller tracking (UID/PID)

**Property Management:**
- `PropertyStore.kt` - Thread-safe storage with DataStore persistence
- `PropertyRegistry.kt` - Property metadata and configuration
- `PropertyValidator.kt` - Value validation (range, type, bitmask)
- `PropertyHalSimulator.kt` - Simulates HAL property updates

**Client Management:**
- `ClientManager.kt` - Track connected clients (UID/PID)
- `DeathRecipientManager.kt` - Handle client crashes
- `PermissionChecker.kt` - Property permission enforcement

**Other:**
- `ServiceNotificationManager.kt` - Foreground service notification
- Hilt DI modules (`ServiceModule.kt`)

### 3. **`:car-settings-client`** - Client Application

The client app that binds to the service and provides a UI:

**Manager Layer:**
- `CarSettingsManager.kt` - High-level client API (357 lines)
  - ServiceConnection with auto-reconnect (exponential backoff)
  - `StateFlow<ConnectionState>` for connection status
  - `SharedFlow<PropertyChangeEvent>` for property changes
  - Suspend functions: `getIntProperty()`, `setFloatProperty()`, etc.
  - Flow-based observation: `observeProperty(propertyId)`

**UI Layer:**
- `MainActivity.kt` - Single activity with bottom navigation
- `DashboardFragment` + `DashboardViewModel` - Connection status, quick settings
- `DisplaySettingsFragment` + `DisplaySettingsViewModel` - Brightness, theme, language

**Custom Views:**
- `ConnectionStatusBar` - Shows connection state with reconnect button

### 4. **`:common`** - Shared Utilities

- `Result.kt` - Result wrapper with Success/Error/Loading states
- `Either.kt` - Either<Left, Right> type for error handling
- `Logger.kt` - Timber wrapper for centralized logging
- `Extensions.kt` - Kotlin extensions for PropertyValue conversion
- `CoroutineDispatchers.kt` - Dependency injection for dispatchers

---

## 🎓 AAOS Patterns Demonstrated

This project demonstrates **critical patterns** used in production AAOS services:

| Pattern | Usage in Project | Real AAOS Example |
|---------|------------------|-------------------|
| **AIDL IPC** | Service-client communication | `CarPropertyService`, `CarAudioManager` |
| **RemoteCallbackList** | Thread-safe callback management | Used throughout AAOS for multi-client callbacks |
| **Oneway Callbacks** | Non-blocking property notifications | `ICarPropertyEventCallback.aidl` |
| **IBinder.DeathRecipient** | Client crash detection & cleanup | `CarPropertyService` client tracking |
| **Binder.getCallingUid()** | Caller identification for permissions | `CarPackageManagerService` |
| **ReentrantReadWriteLock** | Property store synchronization | `CarPropertyService` HAL property cache |
| **Property-based Config** | Area-based properties (SEAT, DOOR) | `VehiclePropertyIds`, `VehicleAreaType` |
| **Driver Profiles** | Profile switching with persistence | `UserHalService`, profile management |
| **Foreground Service** | Reliable background service | Core AAOS services run as system services |
| **Coroutines + Flow** | Async operations, reactive streams | Modern Android pattern (replacing LiveData) |
| **Hilt DI** | Dependency injection | Standard in modern Android/AAOS apps |
| **DataStore** | Persistent property storage | Modern alternative to SharedPreferences |

---

## 🛠️ Technology Stack

- **Language:** Kotlin 1.9+
- **Build System:** Gradle 8.0+ with Kotlin DSL
- **Minimum SDK:** 29 (Android 10)
- **Target SDK:** 34 (Android 14)

**Core Libraries:**
- **Hilt** 2.48+ - Dependency injection
- **Coroutines** 1.7+ - Asynchronous programming
- **Flow** - Reactive streams
- **DataStore** - Persistent storage
- **Timber** - Logging
- **Material Design 3** - UI components

**Development:**
- **JUnit 5** - Unit testing framework
- **MockK** - Mocking library
- **Turbine** - Flow testing
- **GitHub Actions** - CI/CD

---

## ✅ Prerequisites

- **Android Studio Hedgehog (2023.1.1)** or later
- **JDK 17** or later
- **Android SDK** with API 34
- **Gradle 8.0+** (wrapper included)
- **Git** for version control

**Optional:**
- Android Automotive OS emulator for realistic testing
- Physical automotive hardware (if available)

---

## 🔨 Build & Installation

### 1. Clone the Repository

```bash
git clone https://github.com/username/car-settings-aidl-service.git
cd car-settings-aidl-service
```

### 2. Build the Project

```bash
# Build all modules
./gradlew build

# Or build specific modules
./gradlew :car-settings-service:assembleDebug
./gradlew :car-settings-client:assembleDebug
```

### 3. Install on Device/Emulator

**Option A: Using Gradle**
```bash
# Install service APK (must be installed first)
./gradlew :car-settings-service:installDebug

# Install client APK
./gradlew :car-settings-client:installDebug
```

**Option B: Using ADB**
```bash
# Install service
adb install car-settings-service/build/outputs/apk/debug/car-settings-service-debug.apk

# Install client
adb install car-settings-client/build/outputs/apk/debug/car-settings-client-debug.apk
```

**⚠️ Important:** The **service must be installed before the client** for binding to work.

---

## 📱 Usage Guide

### Starting the Application

1. **Launch Client App:**
   ```bash
   adb shell am start -n com.automotive.carsettings.client/.ui.MainActivity
   ```

2. **Verify Service Binding:**
   - The client automatically binds to the service on startup
   - Check the connection status indicator at the top of the screen
   - Status: 🔴 Disconnected → 🟡 Connecting → 🟢 Connected

### Interacting with Properties

**Display Settings:**
- Adjust brightness slider (0-255)
- Toggle night mode
- Change theme (Light/Dark/Auto)
- Select display language

**Dashboard:**
- View connection status
- See real-time property changes
- Monitor connected client count

### Testing Service Robustness

**Test Client Death Handling:**
```bash
# Kill client process while service is running
adb shell am force-stop com.automotive.carsettings.client

# Service should cleanup resources automatically
# Restart client - it should reconnect automatically
```

**Test Service Restart:**
```bash
# Kill service
adb shell am force-stop com.automotive.carsettings.service

# Client should detect disconnection and attempt reconnection
```

**View Service State:**
```bash
# Logcat filtering
adb logcat | grep "CarSettings"
```

---

## ✨ Key Features

### 1. Property Management
- **47 vehicle properties** organized into 6 logical groups
- **Area-based properties** (GLOBAL, SEAT, DOOR, WINDOW, MIRROR, WHEEL)
- **Type-safe access** for Int32, Float, Boolean, String properties
- **Batch operations** for multiple properties
- **Value validation** with range and type checking

### 2. Real-time Callbacks
- **Global callbacks** for all property changes
- **Property-specific listeners** for targeted observation
- **Oneway methods** prevent service blocking
- **Automatic cleanup** on client death

### 3. Driver Profiles
- **5 driver profiles** supported
- **Profile switching** with automatic property save/load
- **Persistent storage** using DataStore
- **Profile change notifications** to all clients

### 4. Robust Connection Management
- **Automatic reconnection** with exponential backoff
- **Connection state tracking** via StateFlow
- **Graceful degradation** on service unavailability
- **Client death detection** with IBinder.DeathRecipient

### 5. Permission System
- **Three permission levels:** Normal, Privileged, System
- **Per-property permissions** enforcement
- **Binder caller identification** (UID/PID)
- **Runtime permission checks**

### 6. HAL Simulation
- **Mock HAL updates** for continuous properties (e.g., temperature)
- **Configurable update rates**
- **Realistic value fluctuations**
- **Demonstrates service-to-client push updates**

---

## 📂 Project Structure

```
car-settings-aidl-service/
├── aidl-interfaces/
│   ├── src/main/
│   │   ├── aidl/com/automotive/carsettings/aidl/
│   │   │   ├── ICarSettingsService.aidl       # Main service interface
│   │   │   ├── ICarSettingsCallback.aidl      # Property change callbacks
│   │   │   ├── IPropertyEventListener.aidl    # Single-property listener
│   │   │   ├── PropertyValue.aidl             # Parcelable declarations
│   │   │   ├── CarPropertyInfo.aidl
│   │   │   ├── CarPropertyConfig.aidl
│   │   │   ├── AreaConfig.aidl
│   │   │   └── CarSettingsError.aidl
│   │   └── java/com/automotive/carsettings/aidl/
│   │       ├── PropertyConstants.kt           # 47 property IDs
│   │       ├── AreaConstants.kt               # Vehicle area IDs
│   │       ├── PropertyValue.kt               # @Parcelize data class
│   │       ├── CarPropertyInfo.kt
│   │       ├── CarPropertyConfig.kt
│   │       ├── AreaConfig.kt
│   │       ├── CarSettingsError.kt
│   │       └── ... (other constants)
│   └── build.gradle.kts
│
├── car-settings-service/
│   ├── src/main/
│   │   ├── AndroidManifest.xml                # Service declaration
│   │   └── java/com/automotive/carsettings/service/
│   │       ├── CarSettingsService.kt          # Android Service entry
│   │       ├── CarSettingsServiceImpl.kt      # AIDL Stub (382 lines)
│   │       ├── CarSettingsApplication.kt      # Hilt app class
│   │       ├── property/
│   │       │   ├── PropertyStore.kt           # Thread-safe storage
│   │       │   ├── PropertyRegistry.kt        # Metadata registry
│   │       │   ├── PropertyValidator.kt       # Value validation
│   │       │   └── PropertyHalSimulator.kt    # Mock HAL
│   │       ├── binder/
│   │       │   ├── ClientManager.kt           # Client tracking
│   │       │   └── DeathRecipientManager.kt   # Death handling
│   │       ├── permission/
│   │       │   └── PermissionChecker.kt       # Permission enforcement
│   │       ├── notification/
│   │       │   └── ServiceNotificationManager.kt
│   │       └── di/
│   │           └── ServiceModule.kt           # Hilt module
│   └── build.gradle.kts
│
├── car-settings-client/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── res/                               # UI resources
│   │   └── java/com/automotive/carsettings/client/
│   │       ├── CarSettingsClientApplication.kt
│   │       ├── manager/
│   │       │   └── CarSettingsManager.kt      # Client API (357 lines)
│   │       └── ui/
│   │           ├── MainActivity.kt
│   │           ├── dashboard/
│   │           │   ├── DashboardFragment.kt
│   │           │   └── DashboardViewModel.kt
│   │           ├── display/
│   │           │   ├── DisplaySettingsFragment.kt
│   │           │   └── DisplaySettingsViewModel.kt
│   │           └── views/
│   │               └── ConnectionStatusBar.kt
│   └── build.gradle.kts
│
├── common/
│   ├── src/main/java/com/automotive/carsettings/common/
│   │   ├── Result.kt                          # Result wrapper
│   │   ├── Either.kt                          # Either type
│   │   ├── Logger.kt                          # Timber wrapper
│   │   ├── Extensions.kt                      # Kotlin extensions
│   │   └── CoroutineDispatchers.kt            # DI for dispatchers
│   └── build.gradle.kts
│
├── .github/workflows/
│   └── build.yml                              # CI/CD pipeline
├── settings.gradle.kts
├── build.gradle.kts
├── README.md                                  # This file
├── ARCHITECTURE.md                            # Detailed architecture docs
└── LICENSE
```

---

## 📚 Documentation

- **[ARCHITECTURE.md](ARCHITECTURE.md)** - Comprehensive architecture documentation with:
  - High-level component diagrams
  - 4 detailed sequence diagrams (Mermaid)
  - Threading model explanation
  - Data flow diagrams
  - 7 key design decisions with rationale
  - Performance considerations

- **Code Documentation:**
  - All public APIs include KDoc comments
  - AIDL interfaces have detailed method documentation
  - Complex logic includes inline comments

---

## 🧪 Testing

### Running Unit Tests

```bash
# Run all tests
./gradlew test

# Run tests for specific module
./gradlew :car-settings-service:test
./gradlew :car-settings-client:test

# Generate test report
./gradlew test --rerun-tasks
# Reports: */build/reports/tests/test/index.html
```

### Test Coverage

**Current Implementation:**
- ✅ PropertyValidator unit tests
- ✅ PropertyStore unit tests
- ✅ PermissionChecker unit tests
- ✅ CarSettingsManager integration tests
- ⚠️ Mock service for client testing (planned)

### Manual Testing

**Service Binding:**
```bash
# Check if service is running
adb shell dumpsys activity services | grep CarSettingsService

# Check bound clients
adb logcat | grep "Client registered"
```

**Property Operations:**
```bash
# Monitor property changes
adb logcat | grep "Property set"
adb logcat | grep "onPropertyChanged"
```

---

## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:

1. **Fork** the repository
2. **Create a feature branch:** `git checkout -b feature/your-feature`
3. **Write tests** for new functionality
4. **Ensure code quality:** Run lint and tests
5. **Commit with clear messages:** Follow conventional commits
6. **Push to your fork:** `git push origin feature/your-feature`
7. **Submit a Pull Request**

**Code Style:**
- Follow Kotlin coding conventions
- Use KtLint for formatting
- Add KDoc for public APIs
- Keep methods focused and concise

---

## 📄 License

```
Copyright 2024 Car Settings AIDL Service Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

---

## 🎯 Learning Objectives

This project helps you understand:

1. ✅ **AIDL Fundamentals** - Writing AIDL interfaces, parcelables, and callbacks
2. ✅ **Binder IPC** - Inter-process communication mechanisms
3. ✅ **Thread Safety** - ReentrantReadWriteLock, ConcurrentHashMap, RemoteCallbackList
4. ✅ **Service Architecture** - Foreground services, binding, lifecycle
5. ✅ **Client Management** - Connection state, reconnection, death handling
6. ✅ **AAOS Patterns** - Property-based configuration, area support, profiles
7. ✅ **Modern Android** - Hilt, Coroutines, Flow, StateFlow, DataStore
8. ✅ **Production Patterns** - Error handling, logging, validation, testing

---

## 🔗 Related Projects & Resources

**Official AAOS Documentation:**
- [Android Automotive OS Overview](https://source.android.com/docs/automotive)
- [Vehicle HAL](https://source.android.com/docs/automotive/vhal)
- [Car API Reference](https://developer.android.com/reference/android/car/package-summary)

**Sample Projects:**
- [Google's AAOS Samples](https://github.com/android/car-samples)
- [AOSP Car Service](https://android.googlesource.com/platform/packages/services/Car/)

---

## 📞 Support

- **Issues:** [GitHub Issues](https://github.com/username/car-settings-aidl-service/issues)
- **Discussions:** [GitHub Discussions](https://github.com/username/car-settings-aidl-service/discussions)
- **Email:** your.email@example.com

---

## 🙏 Acknowledgments

This project was inspired by real Android Automotive OS services and demonstrates patterns used in production automotive applications. Special thanks to the Android and AAOS open-source communities.

---

<div align="center">

**⭐ If this project helps you learn AAOS, please consider giving it a star!**

Made with ❤️ for the Android Automotive community

</div>
