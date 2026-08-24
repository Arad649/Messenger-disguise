# Nuvia

Nuvia is a cross-platform peer-to-peer messenger prototype for Android, Windows, and the web. The three versions use the same universal `@UserID` system and can communicate with each other while both clients are online.

## What works

- Create a local account or choose **Log in** to save an existing `@UserID`, display name, and password on a new device
- Stay signed in automatically by default, with an optional password-on-launch setting
- Display the active `@UserID`, online state, and device type in every main header
- Share your User ID and add link through the device share sheet, copy the ID, or show a scannable QR code
- Show an explicit **Online** or **Offline** presence label in contact rows, chat headers, and profile sheets
- Add private nicknames and mute, block, archive, unarchive, or delete contacts from their profile
- Keep archived conversations in a separate chat section and suppress alerts from muted contacts
- Add a person by universal ID without a phone number or country code
- Connect Android phones and Windows PCs under the same visible ID system
- Send live text messages, voice notes, images, and original emoji stickers
- Transfer compressed voice notes and photos in smaller paced chunks, with compatibility fallback for older clients
- Queue rapid consecutive messages while a phone or PC connection is still opening
- Post photo or text stories, browse them in a dedicated Stories section, and expire them after 24 hours
- Receive system notifications for messages, stories, and incoming calls while the client is running
- Delete your messages for everyone, keep a visible `Deleted message` tombstone, and delete received messages locally
- Show `Not sent`, `Sent`, or `Seen` delivery state, mark a packet sent after transport succeeds, and offer Retry when it fails
- Make peer-to-peer audio and video calls
- Mute audio, disable video, and switch cameras during calls
- Keep contacts, message history, and call history locally on each device
- Run on Android 8.0+ and Windows 10/11 x64

Connections use [PeerJS 1.5.5](https://peerjs.com/) and WebRTC. WebRTC data and media are encrypted in transit with DTLS/SRTP and normally travel directly between devices.

## Account and device behavior

The password is processed with PBKDF2-SHA-256 and a random salt, then stored as a derived hash in that device's local storage. The original password is not stored.

This version has no central account server. To use the same visible ID on a phone and PC, create or log in with the same `@UserID` on each device. Each platform uses its own endpoint (`mobile` or `desktop`) so both can be online at once. A new-device login saves credentials locally; it cannot validate an account against a server, restore messages, or synchronize passwords and history.

## Important prototype limits

- Both people must have at least one phone or PC client open and online.
- There is no offline server delivery or push delivery after every client has been fully closed. Running/minimized clients can show device notifications.
- Password sign-in protects the local copy on that device, but an ID is not permanently reserved globally without a server.
- Some networks require a TURN relay. The free broker/STUN configuration cannot guarantee calls on every carrier or Wi-Fi network.
- Chat history has no app-defined message count or time limit. Device storage remains finite, and large media is stored separately in the browser/WebView media database.
- The Windows executable is an unsigned development build, so Windows SmartScreen may display a warning.
- Desktop version 2.6.0 uses software rendering for wider GPU compatibility and writes startup diagnostics to `%APPDATA%\\Nuvia\\settings-connect.log`.

A production release needs an authenticated backend, database, storage service, push notifications, TURN infrastructure, account recovery, safety tooling, code signing, and a security review.

## Download Android

1. Open the repository's **Actions** tab.
2. Open the latest successful **Build Android APK** run.
3. Download the `nuvia-android-apk` artifact.
4. Unzip it and install `app-debug.apk`.

## Download Windows PC

1. Open the repository's **Actions** tab.
2. Open the latest successful **Build Windows PC App** run.
3. Download the `nuvia-windows-pc` artifact.
4. Unzip and run `Nuvia-PC-2.6.0.exe`.

## Open in a browser

Use [Nuvia Web](https://settings-connect-web.arad649.chatgpt.site) on a Windows, macOS, Linux, Android, or iOS browser. Allow microphone/camera access when prompted for voice notes and calls. The browser version uses the same visible `@UserID` and peer-to-peer connection format as Android and Windows.

## Test phone to PC

1. Install Android on one device and Windows on the PC.
2. Create different test IDs, such as `@arad_phone` and `@friend_pc`.
3. Keep both clients open until the header says **online**.
4. Tap or click **+**, enter the other person's ID, and select **Connect**.
5. Open the contact to exchange messages or start a call.

## Build Android locally

Requirements: JDK 17, Android SDK 35, and Gradle 8.11.1.

```bash
gradle :app:assembleDebug
```

## Build Windows locally

Requirements: Node.js 24 and npm.

```bash
cd desktop
npm install
npm run dist:win
```

## Privacy

Account hashes, contacts, and message history remain in local app storage. The free PeerJS signalling service receives connection metadata needed to broker peers; message content and media use WebRTC.

## License

Project code is provided under the MIT License. PeerJS and Electron are separately available under their respective licenses.
