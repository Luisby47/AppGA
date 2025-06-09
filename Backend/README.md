# Lab2 Kotlin Application

## Deployment to Render

This project is configured for deployment to Render using Docker.

### Deployment Steps

1. Sign up for a Render account at [render.com](https://render.com) if you don't have one.

2. Connect your GitHub, GitLab, or Bitbucket repository to Render.

3. Create a new Web Service on Render:
   - Select your repository
   - Render will automatically detect the `render.yaml` configuration
   - Choose the Free plan (or appropriate plan for your needs)
   - Click "Create Web Service"

4. Render will automatically build and deploy your application based on the Dockerfile.

### Configuration Files

- **Dockerfile**: Defines the Docker image for the application
- **render.yaml**: Configures the Render deployment settings
- **.dockerignore**: Specifies files to exclude from the Docker build

### Environment Variables

The application is configured to use port 8080 by default. The `render.yaml` file sets this up automatically.

### Database

The application uses an SQLite database file (`lab2.db`) which is included in the Docker image. For a production environment, consider using Render's PostgreSQL service for a more robust solution.

### Local Development

To run the application locally:

```bash
mvn clean package
java -jar target/Lab2-1.0-SNAPSHOT.jar
```

The application will be available at http://localhost:8080
