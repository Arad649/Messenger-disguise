# Settings Chat

Settings Chat is an Android peer-to-peer messenger prototype based on the Nuvia concept. It is clearly labeled as a chat app and uses an original gear-and-chat icon; it does not impersonate Samsung Settings or use Samsung artwork.

## What works

- Choose a universal `@UserID` (no country code or phone number)
- Add a person by ID and see their online state
- Send live text messages between two online devices
- Record and play voice notes
- Send images and original emoji stickers
- Make live audio and video calls
- Mute audio, disable video, and switch cameras during calls
- Keep contacts, chat history, and call history locally on the device
- Responsive dark Nuvia-style interface for Android 8.0+

Connections use [PeerJS 1.5.5](https://peerjs.com/) and WebRTC. WebRTC data and media are encrypted in transit with DTLS/SRTP and normally travel directly between devices.

## Important prototype limits

This is a peer-to-peer test build, not a production replacement for WhatsApp:

- Both users must have the app open and be online when connecting.
- There is no offline delivery, push notification, cloud backup, account recovery, contact syncing, moderation, or abuse reporting service.
- IDs are live PeerJS broker IDs, not password-protected accounts. An ID is not permanently reserved.
- Some mobile networks require a TURN relay for WebRTC. The free broker/STUN configuration cannot guarantee connectivity on every carrier or Wi-Fi network.
- Local history is limited, and image/voice attachments are deliberately capped to avoid filling WebView storage.

A production release needs an authenticated account backend, database, object storage, notification service, TURN infrastructure, safety tooling, and a security review.

## Download the APK

1. Open this repository's **Actions** tab.
2. Open the latest successful **Build Android APK** run.
3. Under **Artifacts**, download `settings-chat-debug-apk`.
4. Unzip it and install `app-debug.apk` on each Android phone.

Android may ask you to allow installs from your browser or file manager. The app also asks for microphone and camera access for voice notes and calls.

## Test between two phones

1. Install and open the APK on both phones.
2. Choose a different ID on each phone, for example `@arad_test` and `@friend_test`.
3. Keep both apps open and connected (green status dot).
4. On one phone, tap **+**, enter the other ID, and tap **Connect**.
5. Open the contact to exchange messages or start a call.

## Build locally

Requirements: JDK 17, Android SDK 35, and Gradle 8.11.1.

```bash
gradle :app:assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Privacy

Profiles, contacts, and message history are stored only in Android WebView local storage. Clearing app data or using **Delete local profile and messages** removes that local copy. The free PeerJS signalling service sees connection metadata needed to broker peers; message contents and media are sent over WebRTC.

## License

Project code is provided under the MIT License. PeerJS is separately available under its MIT license.
