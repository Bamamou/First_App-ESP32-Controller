// ESP32 Arduino Code - Ultra-Fast UDP WiFi Version
#include <WiFi.h>
#include <WiFiUdp.h>

// WiFi credentials
const char* ssid = "YOUR_WIFI_NAME";
const char* password = "YOUR_WIFI_PASSWORD";

// UDP
WiFiUDP udp;
unsigned int localUdpPort = 4210;  // Local port to listen on
char incomingPacket[16];  // Buffer for incoming packets

// Relay pins for 4-channel relay module
const int BEDROOM_RELAY = 2;
const int LIVINGROOM_RELAY = 4;
const int BATHROOM_RELAY = 5;
const int KITCHEN_RELAY = 18;
const int STATUS_LED = 13;

void setup() {
  Serial.begin(115200);
  
  // Initialize relay pins
  pinMode(BEDROOM_RELAY, OUTPUT);
  pinMode(LIVINGROOM_RELAY, OUTPUT);
  pinMode(BATHROOM_RELAY, OUTPUT);
  pinMode(KITCHEN_RELAY, OUTPUT);
  pinMode(STATUS_LED, OUTPUT);
  
  // Set all relays OFF initially
  digitalWrite(BEDROOM_RELAY, HIGH);
  digitalWrite(LIVINGROOM_RELAY, HIGH);
  digitalWrite(BATHROOM_RELAY, HIGH);
  digitalWrite(KITCHEN_RELAY, HIGH);
  
  // Connect to WiFi
  WiFi.begin(ssid, password);
  Serial.print("Connecting to WiFi");
  
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
    digitalWrite(STATUS_LED, !digitalRead(STATUS_LED));
  }
  
  Serial.println();
  Serial.print("WiFi connected! IP address: ");
  Serial.println(WiFi.localIP());
  
  // Start UDP
  udp.begin(localUdpPort);
  Serial.printf("UDP Listening on IP: %s, Port: %d\n", WiFi.localIP().toString().c_str(), localUdpPort);
  
  // Success indication
  for(int i = 0; i < 5; i++) {
    digitalWrite(STATUS_LED, HIGH);
    delay(100);
    digitalWrite(STATUS_LED, LOW);
    delay(100);
  }
}

void loop() {
  int packetSize = udp.parsePacket();
  if (packetSize) {
    // Read the packet
    int len = udp.read(incomingPacket, 15);
    if (len > 0) {
      incomingPacket[len] = 0;
    }
    
    // Ultra-fast command processing - single character commands
    switch(incomingPacket[0]) {
      case '1': digitalWrite(BEDROOM_RELAY, LOW); break;      // Bedroom ON
      case '2': digitalWrite(BEDROOM_RELAY, HIGH); break;     // Bedroom OFF
      case '3': digitalWrite(LIVINGROOM_RELAY, LOW); break;   // Living Room ON
      case '4': digitalWrite(LIVINGROOM_RELAY, HIGH); break;  // Living Room OFF
      case '5': digitalWrite(BATHROOM_RELAY, LOW); break;     // Bathroom ON
      case '6': digitalWrite(BATHROOM_RELAY, HIGH); break;    // Bathroom OFF
      case '7': digitalWrite(KITCHEN_RELAY, LOW); break;      // Kitchen ON
      case '8': digitalWrite(KITCHEN_RELAY, HIGH); break;     // Kitchen OFF
      case 'A': // All ON
        digitalWrite(BEDROOM_RELAY, LOW);
        digitalWrite(LIVINGROOM_RELAY, LOW);
        digitalWrite(BATHROOM_RELAY, LOW);
        digitalWrite(KITCHEN_RELAY, LOW);
        break;
      case 'B': // All OFF
        digitalWrite(BEDROOM_RELAY, HIGH);
        digitalWrite(LIVINGROOM_RELAY, HIGH);
        digitalWrite(BATHROOM_RELAY, HIGH);
        digitalWrite(KITCHEN_RELAY, HIGH);
        break;
    }
    
    Serial.printf("Command: %c from %s:%d\n", incomingPacket[0], udp.remoteIP().toString().c_str(), udp.remotePort());
    
    // Brief LED flash to indicate command received
    digitalWrite(STATUS_LED, HIGH);
    delay(50);
    digitalWrite(STATUS_LED, LOW);
  }
  
  // Minimal delay for maximum responsiveness
  yield();
}
