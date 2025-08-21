# 🛠️ ESP32 Setup & Code Examples

## 📋 Hardware Requirements

### ESP32 Development Board
- **ESP32-WROOM-32** (recommended)
- **ESP32-DevKitC** (alternative)
- Built-in Bluetooth Classic support
- GPIO pins for relay control

### Relay Module
- **4-Channel Relay Module** (5V/3.3V compatible)
- **Single Relay Module** (for testing)
- Opto-isolated for safety
- Compatible with ESP32 GPIO voltage levels

### Wiring Diagram
```
ESP32 Pin    →    4-Channel Relay Module
─────────────────────────────────────────
GPIO 2       →    IN1 (Bedroom)
GPIO 4       →    IN2 (Living Room)  
GPIO 5       →    IN3 (Bathroom)
GPIO 18      →    IN4 (Kitchen)
3.3V/5V      →    VCC
GND          →    GND
```

## 💻 ESP32 Arduino Code

### Complete Smart Home Controller Code
```cpp
#include "BluetoothSerial.h"

// Check if Bluetooth is available
#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to enable it
#endif

// Initialize Bluetooth Serial
BluetoothSerial SerialBT;

// Define GPIO pins for relays (4 rooms)
const int BEDROOM_PIN = 2;
const int LIVING_ROOM_PIN = 4;
const int BATHROOM_PIN = 5;
const int KITCHEN_PIN = 18;

// Device name for Bluetooth discovery
const String DEVICE_NAME = "ESP32-SmartHome";

// Current status of each room
bool bedroomStatus = false;
bool livingRoomStatus = false;
bool bathroomStatus = false;
bool kitchenStatus = false;

void setup() {
  Serial.begin(115200);
  
  // Initialize relay pins as outputs
  pinMode(BEDROOM_PIN, OUTPUT);
  pinMode(LIVING_ROOM_PIN, OUTPUT);
  pinMode(BATHROOM_PIN, OUTPUT);
  pinMode(KITCHEN_PIN, OUTPUT);
  
  // Turn off all relays initially (HIGH = OFF for most relay modules)
  digitalWrite(BEDROOM_PIN, HIGH);
  digitalWrite(LIVING_ROOM_PIN, HIGH);
  digitalWrite(BATHROOM_PIN, HIGH);
  digitalWrite(KITCHEN_PIN, HIGH);
  
  // Start Bluetooth with device name
  SerialBT.begin(DEVICE_NAME);
  Serial.println("The device started, now you can pair it with bluetooth!");
  Serial.println("Device Name: " + DEVICE_NAME);
  
  // Print status
  Serial.println("Smart Home Controller Ready!");
  Serial.println("Commands: BEDROOM_ON, BEDROOM_OFF, LIVING_ON, LIVING_OFF");
  Serial.println("         BATHROOM_ON, BATHROOM_OFF, KITCHEN_ON, KITCHEN_OFF");
}

void loop() {
  // Check for Bluetooth data
  if (SerialBT.available()) {
    String command = SerialBT.readString();
    command.trim(); // Remove whitespace
    command.toUpperCase(); // Convert to uppercase
    
    Serial.println("Received command: " + command);
    
    // Process room commands
    if (command == "BEDROOM_ON") {
      controlRelay(BEDROOM_PIN, true);
      bedroomStatus = true;
      SerialBT.println("Bedroom lights ON");
      Serial.println("Bedroom lights turned ON");
      
    } else if (command == "BEDROOM_OFF") {
      controlRelay(BEDROOM_PIN, false);
      bedroomStatus = false;
      SerialBT.println("Bedroom lights OFF");
      Serial.println("Bedroom lights turned OFF");
      
    } else if (command == "LIVING_ON") {
      controlRelay(LIVING_ROOM_PIN, true);
      livingRoomStatus = true;
      SerialBT.println("Living room lights ON");
      Serial.println("Living room lights turned ON");
      
    } else if (command == "LIVING_OFF") {
      controlRelay(LIVING_ROOM_PIN, false);
      livingRoomStatus = false;
      SerialBT.println("Living room lights OFF");
      Serial.println("Living room lights turned OFF");
      
    } else if (command == "BATHROOM_ON") {
      controlRelay(BATHROOM_PIN, true);
      bathroomStatus = true;
      SerialBT.println("Bathroom lights ON");
      Serial.println("Bathroom lights turned ON");
      
    } else if (command == "BATHROOM_OFF") {
      controlRelay(BATHROOM_PIN, false);
      bathroomStatus = false;
      SerialBT.println("Bathroom lights OFF");
      Serial.println("Bathroom lights turned OFF");
      
    } else if (command == "KITCHEN_ON") {
      controlRelay(KITCHEN_PIN, true);
      kitchenStatus = true;
      SerialBT.println("Kitchen lights ON");
      Serial.println("Kitchen lights turned ON");
      
    } else if (command == "KITCHEN_OFF") {
      controlRelay(KITCHEN_PIN, false);
      kitchenStatus = false;
      SerialBT.println("Kitchen lights OFF");
      Serial.println("Kitchen lights turned OFF");
      
    } else if (command == "STATUS") {
      // Send status of all rooms
      String status = "STATUS:";
      status += "BEDROOM:" + String(bedroomStatus ? "ON" : "OFF") + ",";
      status += "LIVING:" + String(livingRoomStatus ? "ON" : "OFF") + ",";
      status += "BATHROOM:" + String(bathroomStatus ? "ON" : "OFF") + ",";
      status += "KITCHEN:" + String(kitchenStatus ? "ON" : "OFF");
      
      SerialBT.println(status);
      Serial.println("Status sent: " + status);
      
    } else {
      SerialBT.println("Unknown command: " + command);
      Serial.println("Unknown command received: " + command);
    }
  }
  
  delay(20); // Small delay to prevent overwhelming the processor
}

// Function to control relay safely
void controlRelay(int pin, bool state) {
  if (state) {
    digitalWrite(pin, LOW);  // LOW = ON for most relay modules
  } else {
    digitalWrite(pin, HIGH); // HIGH = OFF for most relay modules
  }
}
```

## 🔧 Advanced Features Code

### Wi-Fi Status Monitoring (Optional Enhancement)
```cpp
#include <WiFi.h>

// Wi-Fi credentials (optional for status monitoring)
const char* ssid = "YOUR_WIFI_SSID";
const char* password = "YOUR_WIFI_PASSWORD";

void setupWiFi() {
  WiFi.begin(ssid, password);
  Serial.print("Connecting to WiFi");
  
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  
  Serial.println();
  Serial.print("WiFi connected! IP address: ");
  Serial.println(WiFi.localIP());
}

void sendWiFiStatus() {
  if (WiFi.status() == WL_CONNECTED) {
    SerialBT.println("WIFI:CONNECTED:" + WiFi.localIP().toString());
  } else {
    SerialBT.println("WIFI:DISCONNECTED");
  }
}
```

### Auto-Discovery Enhancement
```cpp
void setup() {
  // ... existing setup code ...
  
  // Enhanced Bluetooth setup with custom UUID
  SerialBT.begin(DEVICE_NAME, true); // Enable master mode
  
  // Optional: Set custom service UUID for better identification
  // SerialBT.setPin("1234"); // Set pairing PIN if needed
  
  Serial.println("Enhanced Smart Home Controller Ready!");
  Serial.println("Device discoverable as: " + DEVICE_NAME);
}
```

## 🎯 Protocol Commands

### Supported Commands
| Command | Description | Response |
|---------|-------------|----------|
| `BEDROOM_ON` | Turn on bedroom lights | `Bedroom lights ON` |
| `BEDROOM_OFF` | Turn off bedroom lights | `Bedroom lights OFF` |
| `LIVING_ON` | Turn on living room lights | `Living room lights ON` |
| `LIVING_OFF` | Turn off living room lights | `Living room lights OFF` |
| `BATHROOM_ON` | Turn on bathroom lights | `Bathroom lights ON` |
| `BATHROOM_OFF` | Turn off bathroom lights | `Bathroom lights OFF` |
| `KITCHEN_ON` | Turn on kitchen lights | `Kitchen lights ON` |
| `KITCHEN_OFF` | Turn off kitchen lights | `Kitchen lights OFF` |
| `STATUS` | Get status of all rooms | `STATUS:BEDROOM:ON,LIVING:OFF,...` |

### Command Format
- **Case Insensitive**: Commands work in any case
- **Trim Whitespace**: Leading/trailing spaces are ignored
- **Response Format**: Human-readable confirmations
- **Error Handling**: Unknown commands return error message

## 🔌 Hardware Setup Steps

### 1. ESP32 Programming
```bash
# Install ESP32 board in Arduino IDE
# File → Preferences → Additional Board Manager URLs:
# https://dl.espressif.com/dl/package_esp32_index.json

# Tools → Board → ESP32 Arduino → ESP32 Dev Module
# Tools → Upload Speed → 921600
# Tools → Flash Frequency → 80MHz
```

### 2. Relay Module Connection
```
⚠️  SAFETY FIRST: Always disconnect power when wiring!

ESP32 → Relay Module → Load (Lights/Appliances)
GPIO → IN pins → Control relay switching
VCC → 3.3V/5V → Power relay logic
GND → GND → Common ground
```

### 3. Power Supply Considerations
- **ESP32**: Can be powered via USB or external 5V/3.3V
- **Relay Module**: May need separate 5V supply for relay coils
- **Load**: Connect mains power through relay contacts (NO/NC)

## 🔍 Troubleshooting

### Common Issues
1. **Bluetooth not discoverable**
   - Check ESP32 Bluetooth is enabled in code
   - Verify device name matches exactly
   - Try restarting both devices

2. **Relays not switching**
   - Check wiring connections
   - Verify GPIO pin numbers match code
   - Test relay module with simple LED

3. **Commands not working**
   - Check Serial Monitor for received commands
   - Verify command spelling and case
   - Test with simple Serial input first

### Debugging Commands
```cpp
// Add to loop() for debugging
Serial.println("Bluetooth connected: " + String(SerialBT.hasClient()));
Serial.println("Command received: " + command);
Serial.println("Room statuses - B:" + String(bedroomStatus) + 
               " L:" + String(livingRoomStatus) + 
               " BT:" + String(bathroomStatus) + 
               " K:" + String(kitchenStatus));
```

This ESP32 code provides a **robust, professional foundation** for your smart home automation project!
