# ChatApp

ChatApp is a Java desktop chat application built as a first-year major project. It demonstrates Java GUI development, socket programming, multithreading, database connectivity, and message persistence in a simple private one-to-one messaging system.

## Overview

This project allows registered users to:

- log in using credentials stored in MySQL
- connect to a Java socket server
- send private messages to other users
- reload old conversations from the database

The application is designed as a small but complete client-server system, with a desktop interface on the client side and MySQL used for authentication and chat history.

## Features

- User login connected to MySQL
- Private chat between users
- Chat history loaded from the database
- Socket-based live messaging
- Persistent message storage
- External configuration using `chatapp.properties`
- Sample demo users included through the SQL setup script

## Tech Stack

| Layer | Technology |
| --- | --- |
| Language | Java |
| GUI | AWT |
| Networking | Java Sockets |
| Database | MySQL |
| JDBC Driver | MySQL Connector/J |

## How It Works

1. The client opens the login window.
2. User credentials are checked against the `users` table in MySQL.
3. After login, the client connects to the socket server.
4. Messages are sent through the server and stored in the `messages` table.
5. Previous chats are loaded from MySQL whenever a user opens a conversation.

## Project Structure

```text
ChatApp/
|-- bin/
|-- config/
|   `-- chatapp.properties
|-- database/
|   `-- schema.sql
|-- lib/
|   `-- mysql-connector-j-9.7.0.jar
|-- src/
|   |-- client/
|   |-- config/
|   |-- database/
|   |-- gui/
|   |-- main/
|   `-- server/
`-- README.md
```

## Requirements

- Java JDK 8 or later
- MySQL Server
- MySQL Connector/J jar

The JDBC driver is already included in:

```text
lib/mysql-connector-j-9.7.0.jar
```

## Configuration

Update the database settings in `config/chatapp.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/chatapp
db.user=root
db.password=your_mysql_password

server.host=localhost
server.port=5000
```

## Database Setup

Run `database/schema.sql` in MySQL Workbench or with the MySQL command line.

Important:

- this script drops and recreates the `chatapp` database
- it creates the required `users` and `messages` tables
- it inserts sample users for testing

### Sample Demo Accounts

- `alice / alice123`
- `bob / bob123`
- `charlie / charlie123`

## Compile

Run this from the project root:

```powershell
javac -cp "lib\mysql-connector-j-9.7.0.jar;bin" -d bin src\config\AppConfig.java src\database\PasswordUtil.java src\database\DBConnection.java src\gui\LoginFrame.java src\gui\ChatFrame.java src\server\ChatServer.java src\main\Main.java src\main\TestDB.java src\client\ChatClient.java
```

## Run the Project

### 1. Start the server

Easiest way on Windows:

```powershell
.\run-server.bat
```

Or run it manually:

```powershell
java -cp "bin;lib\mysql-connector-j-9.7.0.jar" server.ChatServer
```

### 2. Start the client

Open a new terminal and run:

```powershell
.\run-client.bat
```

Or run it manually:

```powershell
java -cp "bin;lib\mysql-connector-j-9.7.0.jar" main.Main
```

To test chatting, run the client twice and log in with two different users.

## Build Shortcut

If you want to rebuild the project quickly on Windows:

```powershell
.\build.bat
```

## Demo Flow

You can demonstrate the project in this order:

1. Start the server
2. Open two client windows
3. Log in as `alice` in one client
4. Log in as `bob` in the second client
5. Send messages between them
6. Close one client and reopen it
7. Show that previous messages are still loaded from MySQL

## Key Learning Areas

This project demonstrates:

- object-oriented programming in Java
- event-driven GUI development
- client-server architecture
- socket communication
- JDBC database access
- basic multithreading

## Known Notes

- The project expects MySQL to be running locally.
- If port `5000` is already in use, stop the old server process before starting a new one.
- Run the application from the project root so the config file can be found correctly.

## Future Improvements

- group chat
- timestamps in the chat UI
- user registration from the client
- online/offline presence
- message search

## Academic Purpose

This project was created as a university major project for learning and demonstration purposes.
