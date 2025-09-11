# Cross SDK Android - Developer Guide

[![](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![](https://img.shields.io/badge/Android-21%2B-green.svg)](https://developer.android.com/about/versions/android-5.0)
[![](https://img.shields.io/badge/Kotlin-1.8%2B-purple.svg)](https://kotlinlang.org/)

A comprehensive Android SDK for blockchain interactions, wallet connections, and decentralized application development.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Getting Started](#getting-started)
- [Core Concepts](#core-concepts)
- [API Reference](#api-reference)
- [Sample Applications](#sample-applications)
- [Best Practices](#best-practices)
- [Troubleshooting](#troubleshooting)

---

## 🌟 Overview

Cross SDK Android provides a robust framework for building decentralized applications (dApps) with seamless wallet integration, blockchain interactions, and comprehensive event tracking.

### Key Features

- 🔗 **Wallet Connection**: Easy integration with multiple wallets
- 🔐 **Blockchain Interactions**: Support for signing, transactions, and more
- 🎨 **UI Components**: Pre-built components for rapid development
- 📱 **Deep Links**: Seamless app-to-app communication
- 🌙 **Theme Support**: Dark/Light mode with customization

---

## 🚀 Getting Started

### Prerequisites

- Android API Level 21+
- Kotlin 1.7+
- Gradle 7.0+

### Installation

#### Repository Setup

Add the Cross Nexus repository to your `build.gradle.kts`:

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://package.cross-nexus.com/repository/cross-sdk-android/") }
}
```

#### Using BOM (Recommended)

Example `build.gradle.kts`:

```kotlin
dependencies {
    implementation(platform("io.crosstoken:android-bom:{BOM version}"))
    implementation("io.crosstoken:android-core")
    implementation("io.crosstoken:appkit")
}
```

#### Individual Modules

```kotlin
dependencies {
    implementation("io.crosstoken:foundation:{version}")
    implementation("io.crosstoken:android-core:{version}")
    implementation("io.crosstoken:sign:{version}")
    implementation("io.crosstoken:notify:{version}")
    implementation("io.crosstoken:appkit:{version}")
    implementation("io.crosstoken:modal-core:{version}")
}
```

### Quick Setup

#### 1. Initialize Core SDK

```kotlin
class YourApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        val appMetaData = Core.Model.AppMetaData(
            name = "Your App Name",
            description = "Your app description",
            url = "https://your-app.com",
            icons = listOf("https://your-app.com/icon.png"),
            redirect = "your-app://request",
            appLink = "https://your-app.com"
        )

        CoreClient.initialize(
            application = this,
            projectId = "YOUR_PROJECT_ID",
            metaData = appMetaData
        ) { error ->
            // Handle initialization error
        }
    }
}
```

#### 2. Initialize AppKit

```kotlin
AppKit.initialize(
    Modal.Params.Init(core = CoreClient)
) { error ->
    // Handle AppKit initialization error
}

// Set supported chains
AppKit.setChains(AppKitChainsPresets.ethChains.values.toList())
```

#### 3. Set up Delegate

```kotlin
object YourDelegate : AppKit.ModalDelegate {
    override fun onSessionApproved(approvedSession: Modal.Model.ApprovedSession) {
        // Handle session approval
    }
    
    override fun onSessionRejected(rejectedSession: Modal.Model.RejectedSession) {
        // Handle session rejection
    }
    
    // Implement other delegate methods...
}

AppKit.setDelegate(YourDelegate)
```

---

## 🧩 Core Concepts

### Props System

The `Props` class is the foundation of the SDK's event tracking system, providing structured event data throughout the application lifecycle.

#### Structure

```kotlin
data class Props(
    val event: String = EventType.ERROR,     // Event category
    val type: String = String.Empty,         // Specific event type
    val properties: Properties? = null       // Additional metadata
)
```

#### Usage Examples

```kotlin
// Error event
val errorProps = Props(
    event = EventType.ERROR,
    type = EventType.Error.SESSION_EXPIRED,
    properties = Properties(topic = sessionTopic, trace = debugTrace)
)

// Success event
val successProps = Props(
    event = EventType.SUCCESS,
    type = "SESSION_ESTABLISHED",
    properties = Properties(correlationId = requestId)
)

// User tracking event
val trackingProps = Props(
    event = EventType.TRACK,
    type = EventType.Track.CONNECT_SUCCESS
)
```

### Event Types

#### Base Event Categories

| Category | Description | Usage |
|----------|-------------|-------|
| `ERROR` | Error events | System failures, validation errors |
| `SUCCESS` | Success events | Successful operations |
| `INIT` | Initialization events | SDK setup, configuration |
| `TRACK` | User tracking events | User interactions, analytics |

#### Error Event Types

##### Connection Errors
```kotlin
EventType.Error.NO_WSS_CONNECTION          // WebSocket connection failed
EventType.Error.NO_INTERNET_CONNECTION     // Internet connection unavailable
```

##### Session Errors
```kotlin
EventType.Error.SESSION_EXPIRED            // Session has expired
EventType.Error.SESSION_APPROVE_FAILURE    // Session approval failed
EventType.Error.SESSION_SUBSCRIPTION_FAILURE // Session subscription failed
```

##### Validation Errors
```kotlin
EventType.Error.REQUIRED_NAMESPACE_VALIDATION_FAILURE  // Required namespace validation failed
EventType.Error.CHAINS_CAIP2_COMPLIANT_FAILURE        // CAIP-2 compliance check failed
EventType.Error.INVALID_CACAO                         // Invalid CACAO signature
```

#### Tracking Event Types

##### User Interface Events
```kotlin
EventType.Track.MODAL_LOADED               // Modal UI loaded
EventType.Track.CONNECT_SUCCESS            // Connection successful
EventType.Track.SELECT_WALLET              // Wallet selected
EventType.Track.SWITCH_NETWORK             // Network switched
```

### Properties Model

The `Properties` class provides detailed metadata for events:

```kotlin
data class Properties(
    // Communication
    val message: String? = null,
    val method: String? = null,
    val topic: String? = null,
    val correlationId: Long? = null,
    val clientId: String? = null,
    val direction: String? = null,
    
    // Environment
    val connected: Boolean? = null,
    val network: String? = null,
    val platform: String? = null,
    val userAgent: String? = null,
    
    // Debugging
    val trace: List<String>? = null,
    val name: String? = null
)
```

---

## 📚 API Reference

### Core Client API

#### CoreClient.initialize()

Initializes the Core SDK with project configuration.

```kotlin
CoreClient.initialize(
    application: Application,
    projectId: String,
    metaData: Core.Model.AppMetaData,
    connectionType: ConnectionType = ConnectionType.AUTOMATIC,
    onError: (Core.Model.Error) -> Unit = {}
)
```

**Parameters:**
- `application`: Android Application instance
- `projectId`: Your Cross project ID
- `metaData`: App metadata including name, description, icons
- `connectionType`: Connection management strategy
- `onError`: Error callback handler

### AppKit API

#### Connection Management

```kotlin
// Connect to wallet
AppKit.connect(
    params: Modal.Params.Connect,
    onSuccess: () -> Unit,
    onError: (Throwable) -> Unit
)

// Disconnect from wallet
AppKit.disconnect(
    onSuccess: () -> Unit,
    onError: (Throwable) -> Unit
)

// Get current session
val session: Modal.Model.Session? = AppKit.getSession()

// Get connected account
val account: Modal.Model.Account? = AppKit.getAccount()
```

#### Blockchain Requests

```kotlin
// Send blockchain request
AppKit.request(
    request: Request,
    onSuccess: (SentRequestResult) -> Unit,
    onError: (Throwable) -> Unit
)
```

**Supported Methods:**

| Method | Description | Parameters |
|--------|-------------|------------|
| `personal_sign` | Personal message signing | `[message, address]` |
| `eth_sign` | Ethereum message signing | `[address, data]` |
| `eth_sendTransaction` | Send transaction | Transaction object |
| `eth_signTypedData` | Typed data signing | Typed data object |
| `wallet_getAssets` | Get wallet assets | `[address]` |

#### Example Usage

```kotlin
// Personal sign request
val personalSignRequest = Request(
    method = "personal_sign",
    params = """["Hello World", "0x..."]"""
)

AppKit.request(
    request = personalSignRequest,
    onSuccess = { result ->
        println("Signature: ${result.signature}")
    },
    onError = { error ->
        println("Error: ${error.message}")
    }
)
```

#### Deep Link Handling

```kotlin
// Handle incoming deep links
AppKit.handleDeepLink(
    deepLink: String,
    onError: (Core.Model.Error) -> Unit
)

// Register activity for deep links
AppKit.register(activity: ComponentActivity)
```

### Event Use Cases

#### SendEventUseCase

Sends events to external analytics services.

```kotlin
class SendEventUseCase {
    fun send(
        props: Props,
        sdkType: SDKType = SDKType.APPKIT,
        timestamp: Long? = null,
        id: Long? = null
    )
}
```

#### InsertEventUseCase

Stores events in local database for offline tracking.

```kotlin
class InsertEventUseCase {
    suspend operator fun invoke(props: Props)
}
```

---

## 🎨 UI Components

### Pre-built Components

#### ConnectButton

```kotlin
@Composable
fun ConnectButton(
    state: AppKitState,
    buttonSize: ConnectButtonSize = ConnectButtonSize.NORMAL
)
```

#### AccountButton

```kotlin
@Composable
fun AccountButton(
    state: AppKitState,
    accountButtonType: AccountButtonType = AccountButtonType.NORMAL
)
```

#### Web3Button

```kotlin
@Composable
fun Web3Button(
    state: AppKitState,
    accountButtonType: AccountButtonType = AccountButtonType.MIXED
)
```

#### NetworkButton

```kotlin
@Composable
fun NetworkButton(state: AppKitState)
```

### Theme System

```kotlin
AppKitTheme(
    mode = AppKitTheme.Mode.AUTO, // AUTO, DARK, LIGHT
    lightColors = customLightColors,
    darkColors = customDarkColors
) {
    // Your UI content
}
```

---

## 📱 Sample Applications

The SDK includes two comprehensive sample applications demonstrating different use cases:

### Dapp Sample

**Purpose**: Demonstrates DApp developer integration patterns

**Key Features:**
- Session management and lifecycle
- Blockchain request handling
- Chain selection and switching
- Account management
- Error handling patterns

**Structure:**
```
sample/dapp/
├── DappSampleApp.kt          # Application setup
├── domain/DappDelegate.kt    # Event handling
├── ui/
│   ├── routes/
│   │   ├── account/          # Account management
│   │   ├── session/          # Session handling
│   │   └── chain_selection/  # Network selection
│   └── DappSampleEvents.kt   # Event definitions
```

**Usage Example:**
```kotlin
// Connect to wallet
viewModel.connect { result ->
    when (result) {
        is DappSampleEvents.SessionApproved -> {
            // Handle successful connection
        }
        is DappSampleEvents.SessionRejected -> {
            // Handle rejection
        }
    }
}

// Send transaction
viewModel.requestMethod("eth_sendTransaction") { result ->
    // Handle transaction result
}
```

### Modal Sample

**Purpose**: Demonstrates UI component integration and theming

**Key Features:**
- UI component showcase
- Theme customization
- Button variations
- State management

**Structure:**
```
sample/modal/
├── AppKitApp.kt              # Application setup
├── ModalSampleDelegate.kt    # Event handling
├── compose/HomeScreen.kt     # Main UI
└── ui/
    ├── LabScreen.kt          # Component testing
    └── theme/                # Custom theming
```

**Usage Example:**
```kotlin
@Composable
fun HomeScreen(navController: NavController) {
    val appKitState = rememberAppKitState(navController = navController)
    
    Column {
        ConnectButton(state = appKitState)
        AccountButton(state = appKitState)
        Web3Button(state = appKitState)
        NetworkButton(state = appKitState)
    }
}
```

---

## ✅ Best Practices

### 1. Event Tracking

**Do:**
```kotlin
// Use appropriate event types
sendEventUseCase.send(Props(
    event = EventType.TRACK,
    type = EventType.Track.CONNECT_SUCCESS,
    properties = Properties(network = "ethereum")
))
```

**Don't:**
```kotlin
// Avoid generic or unclear event types
sendEventUseCase.send(Props(
    event = "something_happened",
    type = "unknown"
))
```

### 2. Error Handling

**Do:**
```kotlin
AppKit.request(request,
    onSuccess = { result ->
        // Handle success
    },
    onError = { error ->
        // Log specific error for debugging
        logger.error("Request failed: ${error.message}")
        // Show user-friendly message
        showUserError("Transaction failed. Please try again.")
    }
)
```

**Don't:**
```kotlin
AppKit.request(request,
    onSuccess = { result -> /* ... */ },
    onError = { error ->
        // Don't ignore errors or show technical details to users
        print(error.stackTrace)
    }
)
```

### 3. Session Management

**Do:**
```kotlin
// Check session state before operations
AppKit.getSession()?.let { session ->
    // Perform authenticated operations
} ?: run {
    // Prompt user to connect
}
```

**Don't:**
```kotlin
// Don't assume session exists
val account = AppKit.getSession()!!.account // This can crash!
```

### 4. Deep Link Handling

**Do:**
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate()
        
        // Register for deep links
        AppKit.register(this)
        
        // Handle intent
        intent?.dataString?.let { deepLink ->
            AppKit.handleDeepLink(deepLink) { error ->
                // Handle deep link error
            }
        }
    }
}
```

### 5. Threading

**Do:**
```kotlin
// Use appropriate dispatchers
viewModelScope.launch(Dispatchers.IO) {
    insertEventUseCase(props)
}

// Switch to Main for UI updates
viewModelScope.launch {
    val result = withContext(Dispatchers.IO) {
        // Background work
    }
    // Update UI on Main thread
    updateUI(result)
}
```

---

## 🔧 Troubleshooting

### Common Issues

#### 1. SDK Initialization Fails

**Problem**: `CoreClient.initialize()` throws exception

**Solutions:**
- Verify project ID is correct
- Check network connectivity
- Ensure app metadata is properly configured
- Verify Android manifest permissions

```kotlin
// Add to AndroidManifest.xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

#### 2. Deep Links Not Working

**Problem**: App doesn't respond to deep links

**Solutions:**
- Verify intent filters in AndroidManifest.xml
- Ensure AppKit.register() is called
- Check deep link format matches configuration

```xml
<!-- AndroidManifest.xml -->
<activity android:name=".MainActivity">
    <intent-filter android:autoVerify="true">
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="your-app" />
    </intent-filter>
</activity>
```

#### 3. Session Connection Fails

**Problem**: Wallet connection timeouts or fails

**Solutions:**
- Check wallet app is installed and updated
- Verify network connectivity
- Ensure proper error handling
- Check chain configuration

```kotlin
// Debug session issues
AppKit.setDelegate(object : AppKit.ModalDelegate {
    override fun onSessionRejected(rejectedSession: Modal.Model.RejectedSession) {
        logger.debug("Session rejected: ${rejectedSession.reason}")
    }
    
    override fun onConnectionStateChange(state: Modal.Model.ConnectionState) {
        logger.debug("Connection state: $state")
    }
})
```

#### 4. UI Components Not Displaying

**Problem**: AppKit UI components don't appear

**Solutions:**
- Ensure AppKit is properly initialized
- Check Compose dependencies
- Verify theme configuration
- Ensure state management is correct

```kotlin
// Verify AppKit state
@Composable
fun MyScreen() {
    val appKitState = rememberAppKitState()
    val isConnected by appKitState.isConnected.collectAsState()
    
    if (isConnected) {
        // Show connected UI
    } else {
        ConnectButton(state = appKitState)
    }
}
```

### Debug Logging

Enable debug logging for troubleshooting:

```kotlin
// Add to Application onCreate
if (BuildConfig.DEBUG) {
    Timber.plant(Timber.DebugTree())
}
```

### Performance Optimization

#### Event Batching

```kotlin
// Batch multiple events
val events = listOf(
    Props(EventType.TRACK, EventType.Track.MODAL_OPEN),
    Props(EventType.TRACK, EventType.Track.WALLET_SELECTED)
)

// Send in batch to reduce overhead
events.forEach { sendEventUseCase.send(it) }
```

#### Memory Management

```kotlin
// Clean up resources in ViewModels
class MyViewModel : ViewModel() {
    override fun onCleared() {
        super.onCleared()
        // Cancel coroutines, clear references
    }
}
```

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🤝 Support

- **Documentation**: [SDK Documentation](https://docs.crosstoken.io)
- **Issues**: [GitHub Issues](https://github.com/crosstoken/cross-sdk-android/issues)
- **Community**: [Discord](https://discord.gg/crosstoken)

---
