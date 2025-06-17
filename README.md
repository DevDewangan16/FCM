# 🔔 Firebase Push Notification with CodeIgniter (PHP) and Kotlin (Android)

This project demonstrates how to integrate **Firebase Cloud Messaging (FCM)** push notifications in an **Android app using Kotlin + XML**, triggered by a **CodeIgniter (PHP) backend** when a delivery status is marked as complete.

---

## 📦 Features

- ✅ Save FCM Token from Android to CI Backend
- ✅ Send push notifications on order delivery
- ✅ No Composer needed (uses Firebase Legacy API)
- ✅ Clean and modular code for both Android and PHP

---

## refer this documentation
https://www.notion.so/Push-Notification-20ff4c3e1b448037a460f4d684d4e5b8?source=copy_link

## 🏗️ Project Structure

```bash
├── application/
│   └── controllers/
│       └── ApiController.php   # Handles token saving and push notification
├── Android/
│   └── app/
│       └── MainActivity.kt     # Sends token to backend
│       └── MyFirebaseMessagingService.kt # Receives notification
│       └── res/layout/activity_main.xml
├── database/
│   └── fcm_tokens.sql          # SQL to create token table
