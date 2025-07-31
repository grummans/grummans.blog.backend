# NoyBlog

A personal blog application built with Spring Boot and PostgreSQL.

## Description

NoyBlog is a modern blog platform that allows users to create and manage blog posts with categories and comments. The application provides a clean and efficient way to share thoughts and engage with readers through a comment system.

## Technologies Used

- **Java 21** - Programming language
- **Spring Boot 3.5.4** - Application framework
- **Spring Data JPA** - Data persistence
- **PostgreSQL** - Database
- **Lombok** - Code generation for reducing boilerplate
- **MapStruct 1.6.3** - Bean mapping framework
- **Maven** - Build tool

## Features

- Blog post management
- Category organization
- Comment system with approval workflow
- Hierarchical comments (replies to comments)
- Author information tracking

## Database Schema

The application uses the following main entities:

### Categories
- Category management with unique slugs
- Color coding for visual organization
- Description support

### Comments
- Comment approval system
- Nested comments (parent-child relationships)
- Author information (name and email)
- Timestamps for creation tracking

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- PostgreSQL database
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

## Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd noyblog
   ```

2. **Configure the database**
   
   Update the database connection in `src/main/resources/application.yml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://your-host:5432/your-database
       username: your-username
       password: your-password
   ```

3. **Build the project**
   ```bash
   mvn clean compile
   ```

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

   Or run the JAR file:
   ```bash
   mvn clean package
   java -jar target/noyblog-0.0.1-SNAPSHOT.jar
   ```

## Configuration

The application is configured via `application.yml`:

- **Server Port**: 8085 (configurable)
- **Database**: PostgreSQL connection settings
- **Application Name**: noyblog
- **Custom Banner**: Located at `classpath:banner.txt`

## Project Structure

```
src/
├── main/
│   ├── java/com/grummans/noyblog/
│   │   ├── NoyblogApplication.java     # Main application class
│   │   ├── controller/                 # REST controllers
│   │   ├── model/                      # JPA entities
│   │   │   ├── Categories.java         # Category entity
│   │   │   └── Comments.java           # Comment entity
│   │   ├── repository/                 # Data repositories
│   │   └── service/                    # Business logic
│   └── resources/
│       ├── application.yml             # Application configuration
│       ├── banner.txt                  # Custom startup banner
│       ├── static/                     # Static web resources
│       └── templates/                  # View templates
└── test/                               # Test classes
```

## Development

### Running in Development Mode

1. Ensure PostgreSQL is running and accessible
2. Configure your database connection in `application.yml`
3. Run the application using your IDE or Maven:
   ```bash
   mvn spring-boot:run
   ```

### Building for Production

```bash
mvn clean package
```

This creates an executable JAR file in the `target/` directory.

## API Endpoints

The application exposes RESTful endpoints for:
- Blog post management
- Category operations
- Comment handling

*(Detailed API documentation to be added)*

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the license information in `pom.xml` for details.

## Author

**Grummans** - *Initial work*

## Troubleshooting

### Common Issues

1. **Port Already in Use**
   - Change the server port in `application.yml`
   - Or stop the process using the port

2. **Database Connection Issues**
   - Verify PostgreSQL is running
   - Check connection details in `application.yml`
   - Ensure the database exists

3. **Build Issues**
   - Run `mvn clean` to clear cached files
   - Verify Java 21 is installed and configured

## Future Enhancements

- [ ] User authentication and authorization
- [ ] Rich text editor for blog posts
- [ ] Image upload functionality
- [ ] Email notifications for comments
- [ ] RSS feed support
- [ ] Search functionality
- [ ] Admin dashboard

---

For questions or support, please create an issue in the repository.
