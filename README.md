# LAN-Drop 🚀

A zero-configuration, pure Java desktop application for high-speed peer-to-peer file sharing over local networks. LAN-Drop eliminates the need for cloud servers, USB drives, or manual IP typing.

## ✨ Features
- **Zero-Setup Peer Discovery:** Automatically finds nearby devices in seconds using UDP Multicast.
- **High-Speed Transfers:** Utilizes multi-threaded direct TCP sockets for full-bandwidth utilization.
- **Intuitive GUI:** Drag-and-drop support with real-time transfer progress tracking.
- **Transfer History:** Built-in SQLite database (via JDBC) to log all sent and received files.
- **Cross-Platform:** Runs seamlessly on any OS with a Java Runtime Environment.

## 🛠️ Tech Stack
- **Language:** Java (JDK 17+)
- **GUI Framework:** Java Swing
- **Networking:** `java.net` (UDP Datagrams & TCP Sockets)
- **Database:** SQLite with JDBC
- **Concurrency:** `java.util.concurrent` (Thread Pools & Synchronization)

## 📋 Prerequisites
- Java Development Kit (JDK) 17 or higher.
- SQLite JDBC Driver (included in `lib/`).

## 🚀 Setup & Installation
1. **Clone the repository:**
   ```bash
   git clone [https://github.com/yourusername/lan-drop.git](https://github.com/yourusername/lan-drop.git)
   cd lan-drop