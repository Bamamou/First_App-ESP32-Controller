# 🏠 Smart Home Controller

**Professional Android app for controlling ESP32-based smart home devices via Bluetooth**

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com/)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)
[![Kotlin](https://img.shields.io/badge/Kotlin-100%25-blue.svg)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

Transform your smartphone into a powerful smart home remote control! Control multiple ESP32-based devices with an intuitive, modern interface.

## 📱 App Preview

### Main Interface
```
┌─────────────────────────────────┐
│      Smart Home Controller     │
│ ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ │
│                                 │
│ 🔴 Status: Connected to ESP32   │
│                                 │
│ Device Discovery                │
│ ┌─────────────────────────────┐ │
│ │ 📟 ESP32-SmartHome          │ │
│ │    AA:BB:CC:DD:EE:FF        │ │
│ │    [Compatible] ESP32 Ctrl  │ │
│ └─────────────────────────────┘ │
│                                 │
│ 🏠 Smart Home Control           │
│ ┌─────────────┬─────────────────┐│
│ │🛏️ Bedroom   │🛋️ Living Room   ││
│ │[ON] [OFF]   │[ON] [OFF]       ││
│ │ Status: ON  │ Status: OFF     ││
│ ├─────────────┼─────────────────┤│
│ │🚿 Bathroom  │🍳 Kitchen       ││
│ │[ON] [OFF]   │[ON] [OFF]       ││
│ │ Status: OFF │ Status: ON      ││
│ └─────────────┴─────────────────┘│
└─────────────────────────────────┘
```

## ✨ Key Features

- 🏠 **4-Room Control** - Independently control Bedroom, Living Room, Bathroom, and Kitchen
- 📱 **Professional UI** - Modern Material Design with card-based layout
- 🔍 **Smart Device Discovery** - Advanced scanning with device type recognition
- 📊 **Real-Time Status** - Visual indicators show current state of each room
- 🔐 **Secure Connection** - Direct Bluetooth communication, no internet required
- 🎯 **ESP32 Optimized** - Specifically designed for ESP32 microcontrollers
- 📋 **Scrollable Device List** - Handle multiple devices with smooth scrolling
- 🎨 **Custom Icons** - Professional app icon with Bluetooth and control symbols

## 🚀 Installation

### Method 1: Install APK on Android Device
```bash
# Download the latest release APK
# Copy to your Android device
# Enable "Install from Unknown Sources" in Settings
# Tap the APK file to install
```

### Method 2: Build from Source
```bash
# Clone the repository
git clone https://github.com/Bamamou/First_App-ESP32-Controller.git
cd First_App-ESP32-Controller

# Build debug APK
.\build.bat

# Install on connected device
.\install.bat

# Create release APK
.\release.bat
```

### Method 3: Android Studio
```bash
# Open project in Android Studio
# Connect Android device or start emulator
# Click Run button or press Ctrl+F5
```

## 🔧 ESP32 Setup

### Arduino Code Example
```cpp
#include "BluetoothSerial.h"

BluetoothSerial SerialBT;

// Define relay pins for each room
#define BEDROOM_RELAY    2
#define LIVINGROOM_RELAY 4
#define BATHROOM_RELAY   5
#define KITCHEN_RELAY    18

void setup() {
  Serial.begin(115200);
  SerialBT.begin("ESP32-SmartHome"); // Bluetooth device name
  
  // Initialize relay pins as outputs
  pinMode(BEDROOM_RELAY, OUTPUT);
  pinMode(LIVINGROOM_RELAY, OUTPUT);
  pinMode(BATHROOM_RELAY, OUTPUT);
  pinMode(KITCHEN_RELAY, OUTPUT);
  
  // Turn off all relays initially
  digitalWrite(BEDROOM_RELAY, LOW);
  digitalWrite(LIVINGROOM_RELAY, LOW);
  digitalWrite(BATHROOM_RELAY, LOW);
  digitalWrite(KITCHEN_RELAY, LOW);
  
  Serial.println("Smart Home Controller Ready!");
  Serial.println("Waiting for Bluetooth connection...");
}

void loop() {
  if (SerialBT.available()) {
    String command = SerialBT.readString();
    command.trim();
    
    // Bedroom Controls
    if (command == "BEDROOM_ON") {
      digitalWrite(BEDROOM_RELAY, HIGH);
      SerialBT.println("Bedroom lights ON");
      Serial.println("Bedroom ON");
    }
    else if (command == "BEDROOM_OFF") {
      digitalWrite(BEDROOM_RELAY, LOW);
      SerialBT.println("Bedroom lights OFF");
      Serial.println("Bedroom OFF");
    }
    
    // Living Room Controls
    else if (command == "LIVINGROOM_ON") {
      digitalWrite(LIVINGROOM_RELAY, HIGH);
      SerialBT.println("Living room lights ON");
      Serial.println("Living Room ON");
    }
    else if (command == "LIVINGROOM_OFF") {
      digitalWrite(LIVINGROOM_RELAY, LOW);
      SerialBT.println("Living room lights OFF");
      Serial.println("Living Room OFF");
    }
    
    // Bathroom Controls
    else if (command == "BATHROOM_ON") {
      digitalWrite(BATHROOM_RELAY, HIGH);
      SerialBT.println("Bathroom lights ON");
      Serial.println("Bathroom ON");
    }
    else if (command == "BATHROOM_OFF") {
      digitalWrite(BATHROOM_RELAY, LOW);
      SerialBT.println("Bathroom lights OFF");
      Serial.println("Bathroom OFF");
    }
    
    // Kitchen Controls
    else if (command == "KITCHEN_ON") {
      digitalWrite(KITCHEN_RELAY, HIGH);
      SerialBT.println("Kitchen lights ON");
      Serial.println("Kitchen ON");
    }
    else if (command == "KITCHEN_OFF") {
      digitalWrite(KITCHEN_RELAY, LOW);
      SerialBT.println("Kitchen lights OFF");
      Serial.println("Kitchen OFF");
    }
    
    // Unknown command
    else {
      SerialBT.println("Unknown command: " + command);
      Serial.println("Unknown command: " + command);
    }
  }
  
  delay(20); // Small delay for stability
}
```

### Hardware Connections
```
ESP32 Pin  →  Relay Module  →  Device
GPIO 2     →  Relay 1       →  Bedroom Lights
GPIO 4     →  Relay 2       →  Living Room Lights  
GPIO 5     →  Relay 3       →  Bathroom Fan
GPIO 18    →  Relay 4       →  Kitchen Appliances

VCC        →  3.3V or 5V (depending on relay module)
GND        →  GND
```

## 📋 Command Protocol

| Room | ON Command | OFF Command | Description |
|------|------------|--------------|-------------|
| 🛏️ Bedroom | `BEDROOM_ON` | `BEDROOM_OFF` | Controls bedroom lights/devices |
| 🛋️ Living Room | `LIVINGROOM_ON` | `LIVINGROOM_OFF` | Controls living room lights/devices |
| 🚿 Bathroom | `BATHROOM_ON` | `BATHROOM_OFF` | Controls bathroom fan/lights |
| 🍳 Kitchen | `KITCHEN_ON` | `KITCHEN_OFF` | Controls kitchen appliances |

## 🛠️ Development

### Project Structure
```
app/
├── src/main/
│   ├── java/com/smarthome/esp32controller/
│   │   ├── MainActivity.kt                 # Main app logic
│   │   └── ModernBluetoothDeviceAdapter.kt # Device list adapter
│   ├── res/
│   │   ├── layout/
│   │   │   ├── activity_main.xml          # Main UI layout
│   │   │   └── item_bluetooth_device.xml  # Device card layout
│   │   ├── drawable/                      # Custom UI elements
│   │   ├── values/
│   │   │   ├── colors.xml                 # App color scheme
│   │   │   └── strings.xml                # Text resources
│   │   └── mipmap/                        # App icons
│   └── AndroidManifest.xml               # App configuration
└── build.gradle.kts                      # Build configuration
```

### Key Components

#### Bluetooth Management
```kotlin
class MainActivity : AppCompatActivity() {
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    
    private fun sendCommand(command: String) {
        try {
            outputStream?.write(command.toByteArray())
            showMessage("Command sent: $command")
            // Update room status based on command
            updateRoomStatusFromCommand(command)
        } catch (e: IOException) {
            showMessage("Failed to send command")
        }
    }
}
```

#### Modern Device Discovery
```kotlin
class ModernBluetoothDeviceAdapter(
    private val onDeviceClick: (BluetoothDevice) -> Unit
) : RecyclerView.Adapter<ModernBluetoothDeviceAdapter.DeviceViewHolder>() {
    
    fun addDevice(device: BluetoothDevice) {
        if (!devices.any { it.address == device.address }) {
            devices.add(device)
            notifyItemInserted(devices.size - 1)
        }
    }
}
```

### Build Commands
```bash
# Build debug APK
.\build.bat

# Build release APK  
.\release.bat

# Install on connected device
.\install.bat

# Clean build files
.\clean.bat
```

## 📱 Requirements

- **Android Version**: 7.0 (API level 24) or higher
- **Hardware**: Bluetooth-enabled Android device
- **Permissions**: Bluetooth and Location (for device discovery)
- **ESP32**: Microcontroller with Bluetooth Classic support

## 🔐 Privacy & Security

- ✅ **100% Local Operation** - No internet connection required
- ✅ **No Data Collection** - Zero personal information stored or transmitted
- ✅ **Direct Communication** - Secure Bluetooth connection only
- ✅ **Open Source** - Full source code available for review

## 🎯 Use Cases

- **Smart Lighting** - Control lights in multiple rooms
- **Home Automation** - Fans, heaters, air conditioning
- **IoT Projects** - Custom ESP32-based devices
- **Security Systems** - Remote control access
- **Garden Automation** - Irrigation and lighting systems

## 🚀 Getting Started

1. **Setup ESP32**:
   - Flash the provided Arduino code to your ESP32
   - Connect relays to GPIO pins 2, 4, 5, 18
   - Power on your ESP32

2. **Install App**:
   - Download and install the APK on your Android device
   - Grant Bluetooth and Location permissions

3. **Connect & Control**:
   - Open the app and tap "Scan for Devices"
   - Select your ESP32 from the device list
   - Tap "Connect" and start controlling your smart home!

## 🔧 Troubleshooting

### Common Issues

**App won't find ESP32:**
- Ensure ESP32 Bluetooth is enabled and discoverable
- Check if ESP32 is already paired with another device
- Try restarting both devices

**Connection failed:**
- Verify ESP32 is running the correct code
- Check if device is within Bluetooth range (typically 10 meters)
- Ensure no other apps are using the ESP32 connection

**Emulator issues:**
- Some emulators don't support Bluetooth
- Use a physical Android device for full functionality
- Grant all required permissions in Android settings

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📞 Support

For issues or questions:
- Create an issue on GitHub
- Email: [your-email@domain.com]

---

**Made with ❤️ for the smart home community**
