## Bitchat - Backend. What is this?
  
This backend service for chat application which covers below capabilities,
* Authentication
  * Using basic authentication
  * JWT token based authentication
  * Using cookies (Remember me functionality - Still in progress)
* Self-signed HTTPS connection (TLSv1.2)
* Web socket
* JPA persistence
* Redirecting to login page for logging in, then redirecting to desired page after that
* Clearing expired session after 1-hour idle
* Keeping sessions in memory for better performance
* swagger UI for API documentation
  
Technologies
* Spring boot 2.1.x
* Java 1.8
* Embedded Tomcat 9.0.x
* Swagger 2.7.x
* Hibernate 5.3.x
* PostgreSQL
* WebSocket

# How to setup
1. Build application with "mvn package"
2. Run application with "java -jar target/BitChat-0.0.1-SNAPSHOT.jar"
3. https://localhost:8888/swagger-ui.html to play with API endpoints
