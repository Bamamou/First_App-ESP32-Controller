# Smart Home Controller

A professional Android app for controlling ESP32-based smart home devices via Bluetooth.

## Features
- 4-Room Control (Bedroom, Living Room, Bathroom, Kitchen)
- Real-time device status monitoring
- Bluetooth device scanning and connection
- Professional modern UI design
- Individual relay control for each room

## Requirements
- Android 7.0 (API level 24) or higher
- Bluetooth-enabled device
- ESP32 microcontroller with Bluetooth Classic support

## Permissions
- **Bluetooth**: Required to communicate with ESP32 devices
- **Location**: Required for Bluetooth device discovery on Android 6.0+

## Setup
1. Install the app on your Android device
2. Enable Bluetooth on your device
3. Scan for available ESP32 devices
4. Select and connect to your ESP32
5. Control your smart home devices using the room buttons

## ESP32 Commands
The app sends these commands to your ESP32:
- `BEDROOM_ON` / `BEDROOM_OFF`
- `LIVINGROOM_ON` / `LIVINGROOM_OFF` 
- `BATHROOM_ON` / `BATHROOM_OFF`
- `KITCHEN_ON` / `KITCHEN_OFF`

## Privacy
This app does not collect, store, or transmit any personal data. All communication happens locally between your phone and ESP32 device.

## Support
For issues or questions, please contact [your-email@domain.com]

## License
© 2025 Smart Home Controller. All rights reserved.
