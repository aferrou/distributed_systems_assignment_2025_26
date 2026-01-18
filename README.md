**<h1>FitTrack Fitness Platform</h1>**
<h2>Overview</h2>
FitTrack is a web-based application developed as part of the course "Distributed Systems" at Harokopio University of Athens (HUA) during the academic year 2025-2026. The purpose of FitTrack is to implement a comprehensive fitness appointment and training management system built with Spring Boot that connects users with personal trainers. 
The platform allows users to book training sessions, track their fitness progress, and receive personalized reports. The system consists of two main components:

- Main Application - Core fitness platform with user/trainer management and appointment booking

- Integration Service - External service for weather forecasts

<h2>Prerequisites</h2>

- Java 21 or higher

- Maven 3.8+

<h3>Setup & Run</h3>

---

**<h3>Run Locally</h3>**

**Important**: The [integration service](https://github.com/aferrou/distributed_systems_assignment_integration_service) must be running for weather forecasts to work in the main application.

1. Clone the repository
```
git clone https://github.com/aferrou/distributed_systems_assignment_2025_26.git
```

2. Build the project
```
mvn clean install
```

3. Start the application
```
mvn spring-boot:run
```

4. Access the application
```
http://localhost:8080
```
