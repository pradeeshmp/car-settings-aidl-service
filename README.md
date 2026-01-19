# Car Settings AIDL Service

A comprehensive demonstration of AIDL-based IPC (Inter-Process Communication) patterns used in **Android Automotive OS (AAOS)**. This project showcases service-oriented architecture fundamental to automotive applications, with a background service exposing vehicle preferences through AIDL interfaces.

## 🎯 Project Overview

This multi-module Android project demonstrates real-world AAOS patterns by implementing a Car Settings Service that manages vehicle properties (display, audio, HVAC, vehicle controls, privacy) through a clean AIDL-based API. The architecture mirrors Android Automotive's `CarPropertyService` and related managers.

### Key Features

- ✅ Complete AIDL service implementation with client binding
- ✅ Thread-safe property management with persistence (DataStore)
- ✅ Area-based properties (GLOBAL, SEAT, DOOR, WINDOW, MIRROR)
- ✅ Driver profile switching with property isolation
- ✅ RemoteCallbackList for multi-client notification
- ✅ IBinder.DeathRecipient for crash handling
- ✅ HAL simulator for realistic property updates
- ✅ Permission-based access control
- ✅ Material Design 3 UI with Jetpack components
- ✅ Hilt dependency injection throughout
- ✅ Comprehensive testing setup

## 📁 Module Structure

```
car-settings-aidl-service/
├── aidl-interfaces/          # Shared AIDL definitions & constants
│   ├── ICarSettingsService.aidl
│   ├── ICarSettingsCallback.aidl
│   ├── IPropertyEventListener.aidl
│   ├── Parcelables (PropertyValue, CarPropertyInfo, etc.)
│   └── Constants (PropertyConstants, AreaConstants)
│
├── car-settings-service/     # Background service implementation
│   ├── CarSettingsService - Android Service entry point
│   ├── CarSettingsServiceImpl - AIDL Stub implementation
│   ├── property/ - PropertyStore, Registry, Validator, HAL Simulator
│   ├── binder/ - ClientManager, DeathRecipientManager
│   ├── permission/ - PermissionChecker
│   └── notification/ - Foreground service notification
│
├── car-settings-client/      # Client demo application
│   ├── manager/ - CarSettingsManager (high-level client API)
│   ├── ui/ - MainActivity, Fragments, ViewModels
│   └── views/ - Custom views (ConnectionStatusBar)
│
└── common/                   # Shared utilities
    ├── Result, Either types
    ├── Extensions
    └── Logger, CoroutineDispatchers
```

## 🏗️ Architecture Highlights

### Service-Client IPC Flow

```
┌─────────────────┐          AIDL Binding          ┌──────────────────┐
│                 │◄────────────────────────────────│                  │
│  Client App     │                                 │  Service App     │
│                 │    Property Operations          │                  │
│ CarSettings     │────────────────────────────────►│ CarSettings      │
│ Manager         │         (getProperty,           │ ServiceImpl      │
│                 │          setProperty)           │                  │
│                 │                                 │                  │
│                 │◄────Callbacks (oneway)──────────│                  │
│                 │    (property changes,           │                  │
│                 │     status updates)             │                  │
└─────────────────┘                                 └──────────────────┘
         │                                                   │
         │                                                   │
         ▼                                                   ▼
   ┌──────────┐                                      ┌──────────────┐
   │   UI     │                                      │ PropertyStore│
   │ Fragments│                                      │  + Registry  │
   │ViewModels│                                      │  + Validator │
   └──────────┘                                      └──────────────┘
```

### AAOS Patterns Demonstrated

| Pattern | Description | Location |
|---------|-------------|----------|
| **AIDL Service Binding** | Explicit intent binding with ComponentName | `CarSettingsManager.connect()` |
| **RemoteCallbackList** | Thread-safe callback management | `CarSettingsServiceImpl` |
| **IBinder.DeathRecipient** | Client crash detection and cleanup | `DeathRecipientManager` |
| **Property-based Config** | Similar to VehiclePropertyIds | `PropertyConstants.kt` |
| **Area-based Properties** | Zone-specific properties (SEAT, DOOR) | `AreaConstants.kt` |
| **Permission Enforcement** | Binder.getCallingUid() checks | `PermissionChecker.kt` |
| **Thread-safe Storage** | ReentrantReadWriteLock + ConcurrentHashMap | `PropertyStore.kt` |
| **HAL Simulation** | Mimics Vehicle HAL updates | `PropertyHalSimulator.kt` |
| **Foreground Service** | Reliable background service | `CarSettingsService` |
| **Flow-based Observation** | Reactive property observation | `CarSettingsManager.observeProperty()` |

## 🚀 Build and Run

### Prerequisites

- Android Studio Hedgehog or later
- JDK 17
- Android SDK API 34
- Gradle 8.0+

### Build Steps

```bash
# Clone the repository
git clone https://github.com/yourusername/car-settings-aidl-service.git
cd car-settings-aidl-service

# Build all modules
./gradlew build

# Install service first (required)
./gradlew :car-settings-service:installDebug

# Install client
./gradlew :car-settings-client:installDebug

# Run tests
./gradlew test
```

### Running the Demo

1. **Start the Service**: Launch "Car Settings Service" app (it will show a foreground notification)
2. **Start the Client**: Launch "Car Settings" app
3. **Observe Connection**: The status bar will show "Connected" when binding succeeds
4. **Explore Features**:
   - **Dashboard**: View service status and quick settings
   - **Display**: Adjust brightness, theme, night mode
   - More screens demonstrate different property types

## 🔑 Key Files Explained

### Service Side

- **`ICarSettingsService.aidl`** (aidl-interfaces:57): Main service interface with 25+ methods for property CRUD, batch operations, metadata, callbacks, permissions, and diagnostics

- **`CarSettingsServiceImpl.kt`** (car-settings-service:371): Core service implementation with RemoteCallbackList management, property-specific listeners, and thread-safe property operations

- **`PropertyStore.kt`** (car-settings-service:244): Thread-safe property storage with DataStore persistence, profile management, and default value initialization

- **`PropertyRegistry.kt`** (car-settings-service:165): Metadata registry mapping property IDs to CarPropertyInfo and CarPropertyConfig with area support

- **`PropertyHalSimulator.kt`** (car-settings-service:127): Simulates HAL behavior with periodic temperature updates and random property changes

### Client Side

- **`CarSettingsManager.kt`** (car-settings-client:356): High-level client API with ServiceConnection, auto-reconnect (exponential backoff), Flow-based observation, and suspend functions for all property types

- **`DashboardViewModel.kt`** (car-settings-client:86): Demonstrates property observation and state management with StateFlow

- **`DisplaySettingsViewModel.kt`** (car-settings-client:102): Shows reactive property updates and user-initiated changes

### Shared

- **`PropertyConstants.kt`** (aidl-interfaces:376): Defines 50+ property IDs following AAOS VehiclePropertyIds pattern with type/area/group encoding

- **`AreaConstants.kt`** (aidl-interfaces:135): Seat, door, window, mirror, wheel area definitions with helper functions

## 📊 Property Examples

```kotlin
// Display brightness (GLOBAL, INT32, 0-255)
val brightness = PropertyConstants.DISPLAY_BRIGHTNESS

// HVAC temperature (SEAT-specific, FLOAT, 16-30°C)
val driverTemp = PropertyConstants.HVAC_TEMPERATURE
val areaId = AreaConstants.SEAT_DRIVER

// Door lock (DOOR-specific, BOOLEAN)
val doorLock = PropertyConstants.VEHICLE_DOOR_LOCK
val doorArea = AreaConstants.DOOR_ROW_1_LEFT
```

## 🔒 Permission Model

Properties are classified into three permission levels:

- **NORMAL**: Accessible by all clients (display, audio, privacy settings)
- **PRIVILEGED**: Requires `signature` permission (door locks, headlights)
- **SYSTEM**: Requires `signature|privileged` permission (safety-critical properties)

Defined in `AndroidManifest.xml`:
```xml
<permission
    android:name="com.automotive.carsettings.permission.PRIVILEGED"
    android:protectionLevel="signature" />
```

## 🧪 Testing

```bash
# Run unit tests
./gradlew test

# Run instrumentation tests
./gradlew connectedAndroidTest

# Check code coverage
./gradlew jacocoTestReport
```

Key test files:
- `PropertyValidatorTest.kt` - Property value validation
- `PropertyStoreTest.kt` - Storage and persistence
- `CarSettingsManagerTest.kt` - Client API with mock service

## 📈 Comparison to Real AAOS Services

| This Project | Real AAOS Service | Purpose |
|--------------|-------------------|---------|
| `ICarSettingsService` | `ICarProperty` | Property access interface |
| `PropertyConstants` | `VehiclePropertyIds` | Property ID definitions |
| `AreaConstants` | `VehicleAreaSeat/Door/etc` | Area masks |
| `PropertyStore` | `PropertyHalService` | HAL interaction layer |
| `CarSettingsManager` | `CarPropertyManager` | Client API |
| `PropertyHalSimulator` | Vehicle HAL | Hardware abstraction |

## 🛠️ Technologies Used

- **Kotlin** 1.9.20
- **Android Gradle Plugin** 8.2.0
- **Hilt** 2.48 (Dependency Injection)
- **Jetpack**: ViewModel, LiveData, Navigation, DataStore
- **Coroutines** 1.7.3
- **Material Design 3**
- **Timber** (Logging)
- **JUnit 5**, **MockK**, **Turbine** (Testing)

## 📚 Learning Resources

- [Android Automotive OS Documentation](https://source.android.com/devices/automotive)
- [AIDL Overview](https://developer.android.com/develop/background-work/services/aidl)
- [Car API Reference](https://developer.android.com/reference/android/car/packages)

## 🤝 Contributing

This is a demonstration project for learning AAOS patterns. Feel free to fork and extend for your own educational purposes.

## 📄 License

MIT License - See LICENSE file for details

## 🙏 Acknowledgments

Inspired by Android Automotive OS's CarPropertyService and related components. This project is for educational purposes and demonstrates best practices for AIDL-based IPC in automotive contexts.

---

**Note**: This is a demonstration project and should not be used in production vehicles without proper safety validation and certification.
