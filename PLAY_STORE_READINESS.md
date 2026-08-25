# Nuvia Play Store readiness

This audit was refreshed for Nuvia 2.9.0 using official product and Android quality sources.

## Competitive feature benchmark

| Product strength | Official reference | Nuvia 2.9.0 response |
|---|---|---|
| WhatsApp: simple private messaging, calls, Status, Channels, privacy controls | [WhatsApp end-to-end encryption](https://faq.whatsapp.com/820124435853543), [Status and Channels](https://faq.whatsapp.com/1170535238421230) | Universal IDs without phone numbers, calls, stories, per-contact controls, notification privacy, and a privacy checkup |
| Signal: usernames, stories, private calls, disappearing and view-once media, Note to Self | [Signal feature index](https://support.signal.org/hc/en-us/sections/360001602792-Signal-Messenger-Features), [Note to Self](https://support.signal.org/hc/en-us/articles/360043272451-Note-to-Self) | Universal `@UserID`, Saved messages, disappearing timers, view-once photos, read-receipt and presence controls |
| Telegram: reactions, polls, QR sharing, scheduling, rich automation | [Reactions and QR codes](https://telegram.org/blog/reactions-spoilers-translations), [Scheduled messages](https://telegram.org/blog/scheduled-reminders-themes), [Polls](https://telegram.org/blog/polls-2-0-vmq) | Reactions, polls, QR sharing, scheduled and silent messages, plus a simpler privacy-first interface |
| Discord: persistent presence, text/voice/video, communities | [Discord overview](https://discord.com/), [Discord communication features](https://docs.discord.com/developers/discord-social-sdk/core-concepts/communication-features) | Live presence controls and cross-device text, voice, and video without requiring a community/server setup |

## Added in 2.9.0

- Original synthesized sound palette for navigation, sending, receiving, reactions, recording, calls, success, and errors
- Separate toggles for interface sounds, incoming-message sounds, and haptic feedback
- Replies, editing, reactions, copying, pinning, deletion, retry, and conversation search
- Polls, scheduled text, silent messages, view-once photos, typing indicators, and disappearing timers
- Pinned and unread chat filters, Saved messages, notification preview privacy, data saver, themes, and large text
- Android conversation-style notifications, silent notification channel, stable conversation grouping, and 48 dp primary controls

## Required before a real public launch

Nuvia 2.9.0 remains a peer-to-peer prototype. A dependable Play Store messenger still needs:

1. A server-backed identity system that permanently reserves IDs, verifies Google sign-in, supports account recovery, and prevents impersonation.
2. Offline message queues and Firebase Cloud Messaging so messages and calls arrive when the app is closed.
3. Audited application-layer end-to-end encryption, key verification, secure backups, and documented threat modeling.
4. Production TURN infrastructure for calls and media on restrictive mobile and Wi-Fi networks.
5. Abuse reporting, spam limits, message requests, moderation workflows, child-safety controls, and an appeal process.
6. A privacy policy, terms, data-safety declaration, support contact, account-deletion flow, and age/content rating.
7. A signed Android App Bundle, release keystore protection, Play Integrity strategy, crash/ANR monitoring, staged rollout, and device-lab testing.
8. Direct reply, conversation shortcuts, and bubbles for full alignment with [Android's messaging-app quality guidance](https://developer.android.com/docs/quality-guidelines/archive/core/core-app-quality-2026-03-20).

Google's current quality guidance also emphasizes stability, accessible touch targets, coherent transitions, and monitoring Android vitals: [user experience](https://developer.android.com/quality/user-experience) and [technical quality](https://developer.android.com/quality/technical).
