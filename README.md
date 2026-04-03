# ReViSE-backend

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ReViSE-EuroSpaceCenter_ReViSE-backend&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=ReViSE-EuroSpaceCenter_ReViSE-backend)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=ReViSE-EuroSpaceCenter_ReViSE-backend&metric=coverage)](https://sonarcloud.io/summary/new_code?id=ReViSE-EuroSpaceCenter_ReViSE-backend)

## Welcome aboard!

Set course for **Europa**, Jupiter’s icy moon. More than 600 million kilometers from Earth, the adventure begins: exploration, cooperation, and discovery will be your driving forces.

**ReViSE** is a board game designed by the **Euro Space Center (ESC),** in collaboration with the **University of Namur** and **B12 Consulting**.

An online version will now accompany the physical board game, allowing a more immersive experience. 
This repository contains the backend of the online version of ReViSE, which is built using **Spring Boot** and **Java**. It provides the necessary APIs to manage game sessions, player interactions, and game state.

## Getting Started
To run the backend locally, follow these steps:
1. **Clone the project repository:**
   ```bash
   git clone https://github.com/ReViSE-EuroSpaceCenter/ReViSE-backend
    ```
2. **Navigate to the project directory:**
   ```bash
   cd ReViSE-backend
   ```
4. **Build the project using Maven:**
   ```bash
   mvn clean install
   ```
5. **Run the application:**
   ```bash
   mvn spring-boot:run  
   ```

## Running Tests

To run the tests, use the following command:
```bash
mvn test
```

## Dev Profile
The `dev` profile is used for development purposes and includes additional configurations such as Swagger documentation. To activate the `dev` profile, you can set the following environment variable before running the application:
```bash
export SPRING_PROFILES_ACTIVE=dev
```

## Documentation
Detailed documentation and wiki for the project can be found in the [ReViSE Wiki](https://github.com/ReViSE-EuroSpaceCenter/ReViSE/wiki).


## API documentation

The API documentation is available as a Swagger specification, which can be accessed at: http://localhost:8080/swagger-ui/index.html.

The `dev` profile must be active.
