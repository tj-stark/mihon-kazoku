<div align="center">

<a href="https://github.com/tj-stark/mihon-kazoku">
    <img src="./.github/assets/logo.png" alt="Mihon Kazoku logo" title="Mihon Kazoku" width="100"/>
</a>

# Mihon Kazoku (家族)

### Safe, Family-Focused Manga & Comic Reader for Android
*A kid-safe fork of Mihon with PIN-gated parental controls, built-in adult content filters, and tamper-proof backups.*

---

### 📥 Download Latest APK

[![Download Latest Release](https://img.shields.io/badge/Download%20APK-v0.21.2--kazoku-2ea44f?style=for-the-badge&logo=android&logoColor=white)](https://github.com/tj-stark/mihon-kazoku/releases/latest/download/Mihon-Kazoku-v0.21.2-arm64-v8a.apk)
[![All Releases](https://img.shields.io/badge/All%20Releases-GitHub-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/tj-stark/mihon-kazoku/releases)

**[👉 Click here to directly download Mihon-Kazoku-v0.21.2-arm64-v8a.apk](https://github.com/tj-stark/mihon-kazoku/releases/latest/download/Mihon-Kazoku-v0.21.2-arm64-v8a.apk)**

*Requires Android 8.0 (Oreo) or higher. Supports `arm64-v8a` (standard for almost all modern Android phones and tablets).*

---

</div>

## 🛡️ Why Mihon Kazoku?

Standard Mihon is a wonderful reader, but it is designed exclusively for adult users. Its built-in "18+" toggle can easily be switched off by any child, unvetted extension catalogs expose explicit covers and titles, and restoring a backup can wipe parental restrictions entirely.

**Mihon Kazoku** solves this by establishing a strict cryptographic security boundary between **Parent Mode** and **Child Mode**:

### Key Features

* 🔒 **PIN-Gated Parent Mode:**
  * The entire **Browse** tab (Sources, Extensions, and Migration) is locked behind a secure SHA-256 salted PIN.
  * Sensitive settings (**Data and Storage**, **Security**, **Advanced**, and **Browse Settings**) are gated behind the same PIN.
  * Configurable session timeouts automatically lock the app back into Child Mode.

* 👶 **Worry-Free Child Mode:**
  * Kids have full access to their reading **Library**, chapter downloads, reader settings, and viewing customization.
  * In-app WebViews and "Open in Browser" shortcuts are completely suppressed in Child Mode so external unvetted websites cannot be reached.
  * Search is restricted to the local approved library—external source search queries are disabled.

* 📦 **Pre-Seeded Keiyoushi Extension Repository:**
  * No need to track down, copy, or paste repository URLs. Keiyoushi's official repository is configured out-of-the-box in the database.
  * Parents can still add additional third-party repositories or delete Keiyoushi at any time (and it stays deleted).

* 🚫 **Explicit-Named Source & Extension Filter:**
  * Over 130 dedicated adult scrapers (*Coomer*, *Kemono*, *HentaiFox*, *Pornwha*, *AllPornComic*, *Manga18fx*, *AsmHentai*, *Fakku*, etc.) are filtered out of the extension catalog and sources list by default.
  * Unlike Mihon's native blunt 18+ filter, mainstream aggregators (*MangaDex*, *MangaKakalot*, *ComicK*, *Bato*, etc.) remain accessible so parents can curate standard Shonen, Shojo, and all-ages titles.

* 💾 **Tamper-Proof, Multi-Format Backups:**
  * Backups seamlessly ingest `.mihonbk`, `.tachibk`, and `.kazokubk` formats.
  * When parental controls are active, an immutable cryptographic security block is permanently stamped into the backup metadata.
  * A locked backup **cannot be restored** without entering the PIN—preventing the *"uninstall / clear data / restore without settings"* bypass trick.

---

## 📲 Installation Guide

1. **Download:** Tap the **[Download APK](https://github.com/tj-stark/mihon-kazoku/releases/latest/download/Mihon-Kazoku-v0.21.2-arm64-v8a.apk)** button above on your Android device.
2. **Install:** Open the downloaded `.apk` file from your browser's download notification or your Files app.
   * *If prompted, allow "Install unknown apps" for your browser.*
3. **Setup Parental PIN:**
   * Open **Mihon Kazoku**.
   * Go to **More → Settings → Security → Parental Controls**.
   * Set your Parental PIN.
4. **Curate Content:**
   * Unlock the **Browse** tab using your PIN.
   * Install the extensions you trust, search for your kid-friendly series, and add them to the **Library**.
   * Tap the lock icon in the top toolbar to re-lock the app into Child Mode.

---

## 🤝 Upstream & Credits

Mihon Kazoku is an independent, family-oriented fork built upon the incredible work of the [Mihon](https://github.com/mihonapp/mihon) and [Tachiyomi](https://github.com/tachiyomiorg) open-source communities.

Licensed under the [Apache 2.0 License](/LICENSE).
