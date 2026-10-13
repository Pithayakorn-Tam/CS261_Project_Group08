"# CS261_Project_Group08" 

# TUMeet (CS261 Project Group 08)

TUMeet is a real-time chat application designed with a decoupled architecture, separating the client-side frontend from the server-side backend. The system supports real-time communication using Spring WebSockets, utilizes Microsoft SQL Server as the primary relational database, and is fully containerized using Docker.

## Features
* **Real-time Messaging:** Instant bidirectional communication powered by Spring WebSocket.
* **REST API Integration:** Clean API interfaces configured with CORS to facilitate secure communication between the frontend and backend.
* **Containerized Deployment:** Backend packaged as a WAR file running on Apache Tomcat 10.1 alongside MS SQL Server 2022 within isolated Docker containers.
* **Decoupled Architecture:** Strict separation of UI client assets and server logic for independent development and deployment workflows.

## Project Structure
The root workspace `CS261_Project_Group08` is structured as follows:

```text
CS261_Project_Group08/
├── README.md
├── .gitignore
├── frontend/
│   ├── index.html
│   ├── css/
│   │   └── style.css
│   └── js/
│       └── api.js
└── backend/
    ├── Dockerfile
    ├── pom.xml
    ├── mvnw
    ├── mvnw.cmd
    ├── .gitattributes
    └── src/
        ├── main/
        │   ├── java/com/tumeet/backend/
        │   │   ├── BackendApplication.java
        │   │   ├── ServletInitializer.java
        │   │   ├── config/
        │   │   │   └── WebConfig.java
        │   │   ├── controller/
        │   │   ├── model/
        │   │   └── repository/
        │   └── resources/
        │       └── application.properties
        └── test/