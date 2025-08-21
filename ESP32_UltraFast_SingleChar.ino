#include "BluetoothSerial.h"

BluetoothSerial SerialBT;

// Relay pins for 4-channel relay module
const int BEDROOM_RELAY = 2;
const int LIVINGROOM_RELAY = 4;
const int BATHROOM_RELAY = 5;
const int KITCHEN_RELAY = 18;
const int STATUS_LED = 13;

void setup() {
  Serial.begin(115200);
  
  // Initialize relay pins as outputs
  pinMode(BEDROOM_RELAY, OUTPUT);
  pinMode(LIVINGROOM_RELAY, OUTPUT);
  pinMode(BATHROOM_RELAY, OUTPUT);
  pinMode(KITCHEN_RELAY, OUTPUT);
  pinMode(STATUS_LED, OUTPUT);
  
  // Set all relays to OFF initially (HIGH = OFF for most relay modules)
  digitalWrite(BEDROOM_RELAY, HIGH);
  digitalWrite(LIVINGROOM_RELAY, HIGH);
  digitalWrite(BATHROOM_RELAY, HIGH);
  digitalWrite(KITCHEN_RELAY, HIGH);
  digitalWrite(STATUS_LED, LOW);
  
  // Initialize Bluetooth with optimized settings
  SerialBT.begin("ESP32_SmartHome"); 
  Serial.println("ESP32 Smart Home Controller - Ultra-Fast Mode");
  
  // Success indication
  for(int i = 0; i < 3; i++) {
    digitalWrite(STATUS_LED, HIGH);
    delay(100);
    digitalWrite(STATUS_LED, LOW);
    delay(100);
  }
}

void loop() {
  // Ultra-fast single character command processing
  if (SerialBT.available()) {
    char command = SerialBT.read();
    
    // Immediate relay control - no string processing, no delays
    switch(command) {
      case '1': 
        digitalWrite(BEDROOM_RELAY, LOW);
        Serial.println("Bedroom ON");
        break;
      case '2': 
        digitalWrite(BEDROOM_RELAY, HIGH);
        Serial.println("Bedroom OFF");
        break;
      case '3': 
        digitalWrite(LIVINGROOM_RELAY, LOW);
        Serial.println("Living Room ON");
        break;
      case '4': 
        digitalWrite(LIVINGROOM_RELAY, HIGH);
        Serial.println("Living Room OFF");
        break;
      case '5': 
        digitalWrite(BATHROOM_RELAY, LOW);
        Serial.println("Bathroom ON");
        break;
      case '6': 
        digitalWrite(BATHROOM_RELAY, HIGH);
        Serial.println("Bathroom OFF");
        break;
      case '7': 
        digitalWrite(KITCHEN_RELAY, LOW);
        Serial.println("Kitchen ON");
        break;
      case '8': 
        digitalWrite(KITCHEN_RELAY, HIGH);
        Serial.println("Kitchen OFF");
        break;
      case 'A': // All ON
        digitalWrite(BEDROOM_RELAY, LOW);
        digitalWrite(LIVINGROOM_RELAY, LOW);
        digitalWrite(BATHROOM_RELAY, LOW);
        digitalWrite(KITCHEN_RELAY, LOW);
        Serial.println("All ON");
        break;
      case 'B': // All OFF
        digitalWrite(BEDROOM_RELAY, HIGH);
        digitalWrite(LIVINGROOM_RELAY, HIGH);
        digitalWrite(BATHROOM_RELAY, HIGH);
        digitalWrite(KITCHEN_RELAY, HIGH);
        Serial.println("All OFF");
        break;
      default:
        // Ignore unknown commands - no response for maximum speed
        break;
    }
    
    // Brief status LED flash
    digitalWrite(STATUS_LED, HIGH);
    delayMicroseconds(500); // Microsecond delay for ultra-fast feedback
    digitalWrite(STATUS_LED, LOW);
  }
  
  // Minimal delay - maximum responsiveness
  delayMicroseconds(100);
}
