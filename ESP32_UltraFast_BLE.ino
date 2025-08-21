#include "BluetoothSerial.h"

BluetoothSerial SerialBT;

// Set to true to define Relay as Normally Open (NO)
#define RELAY_NO    false

// Define relay GPIO pins
#define BEDROOM_RELAY_PIN     23    // GPIO pin for bedroom relay (SWITCH1)
#define LIVINGROOM_RELAY_PIN  5     // GPIO pin for living room relay (SWITCH2)
#define BATHROOM_RELAY_PIN    4     // GPIO pin for bathroom relay (SWITCH3)
#define KITCHEN_RELAY_PIN     13    // GPIO pin for kitchen relay (SWITCH4)
#define LED_RED               15    // LED indicator pin

// Relay state tracking
bool bedroomState = false;
bool livingroomState = false;
bool bathroomState = false;
bool kitchenState = false;
bool bluetoothConnected = false;

// Function prototypes
void checkBluetoothConnection();

void setup() {
  SerialBT.begin("ESP32 Home Control"); // Bluetooth device name
  
  // Initialize all relay pins as outputs and set them to OFF (LOW)
  pinMode(BEDROOM_RELAY_PIN, OUTPUT);
  pinMode(LIVINGROOM_RELAY_PIN, OUTPUT);
  pinMode(BATHROOM_RELAY_PIN, OUTPUT);
  pinMode(KITCHEN_RELAY_PIN, OUTPUT);
  pinMode(LED_RED, OUTPUT);
  
  // Initialize all relays to OFF state (HIGH for NO relays, LOW for NC relays)
  digitalWrite(BEDROOM_RELAY_PIN, RELAY_NO ? LOW : HIGH);
  digitalWrite(LIVINGROOM_RELAY_PIN, RELAY_NO ? LOW : HIGH);
  digitalWrite(BATHROOM_RELAY_PIN, RELAY_NO ? LOW : HIGH);
  digitalWrite(KITCHEN_RELAY_PIN, RELAY_NO ? LOW : HIGH);
  digitalWrite(LED_RED, LOW);
}

void loop() {
  checkBluetoothConnection();
  
  // Ultra-fast single character command processing
  if (SerialBT.available()) {
    char command = SerialBT.read();
    
    // Immediate relay control - no string processing, no delays
    switch(command) {
      case '1': 
        digitalWrite(BEDROOM_RELAY_PIN, RELAY_NO ? HIGH : LOW);
        bedroomState = true;
        break;
      case '2': 
        digitalWrite(BEDROOM_RELAY_PIN, RELAY_NO ? LOW : HIGH);
        bedroomState = false;
        break;
      case '3': 
        digitalWrite(LIVINGROOM_RELAY_PIN, RELAY_NO ? HIGH : LOW);
        livingroomState = true;
        break;
      case '4': 
        digitalWrite(LIVINGROOM_RELAY_PIN, RELAY_NO ? LOW : HIGH);
        livingroomState = false;
        break;
      case '5': 
        digitalWrite(BATHROOM_RELAY_PIN, RELAY_NO ? HIGH : LOW);
        bathroomState = true;
        break;
      case '6': 
        digitalWrite(BATHROOM_RELAY_PIN, RELAY_NO ? LOW : HIGH);
        bathroomState = false;
        break;
      case '7': 
        digitalWrite(KITCHEN_RELAY_PIN, RELAY_NO ? HIGH : LOW);
        kitchenState = true;
        break;
      case '8': 
        digitalWrite(KITCHEN_RELAY_PIN, RELAY_NO ? LOW : HIGH);
        kitchenState = false;
        break;
      case 'A': // All ON
        digitalWrite(BEDROOM_RELAY_PIN, RELAY_NO ? HIGH : LOW);
        digitalWrite(LIVINGROOM_RELAY_PIN, RELAY_NO ? HIGH : LOW);
        digitalWrite(BATHROOM_RELAY_PIN, RELAY_NO ? HIGH : LOW);
        digitalWrite(KITCHEN_RELAY_PIN, RELAY_NO ? HIGH : LOW);
        bedroomState = livingroomState = bathroomState = kitchenState = true;
        break;
      case 'B': // All OFF
        digitalWrite(BEDROOM_RELAY_PIN, RELAY_NO ? LOW : HIGH);
        digitalWrite(LIVINGROOM_RELAY_PIN, RELAY_NO ? LOW : HIGH);
        digitalWrite(BATHROOM_RELAY_PIN, RELAY_NO ? LOW : HIGH);
        digitalWrite(KITCHEN_RELAY_PIN, RELAY_NO ? LOW : HIGH);
        bedroomState = livingroomState = bathroomState = kitchenState = false;
        break;
      default:
        // Ignore unknown commands - no response for maximum speed
        break;
    }
  }
}

void checkBluetoothConnection() {
  bool currentConnectionState = SerialBT.hasClient();
  
  if (currentConnectionState != bluetoothConnected) {
    bluetoothConnected = currentConnectionState;
    
    if (bluetoothConnected) {
      // Device connected - turn LED ON
      digitalWrite(LED_RED, HIGH);
    } else {
      // Device disconnected - turn LED OFF
      digitalWrite(LED_RED, LOW);
    }
  }
}