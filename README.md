Project local development quickstart (macOS)

This project uses Spring Boot + PostgreSQL. The commands below assume you're working from the project root:

  /Users/pavanreddy/Downloads/app

1) Create a local env script (already provided)

  # Fill in secrets in scripts/env.sh (DO NOT commit)
  cd /Users/pavanreddy/Downloads/app
  nano scripts/env.sh
  # then in the same shell
  source scripts/env.sh

2) Start local Postgres (Docker)

  docker run --name allestate-postgres -e POSTGRES_PASSWORD=pass -e POSTGRES_USER=app -e POSTGRES_DB=appdb -p 5432:5432 -d postgres:15

Alternative: start both Postgres and the app with Docker Compose (recommended)

  # from project root
  docker compose up --build

This will build the app image, start Postgres and the app. The app will be available at http://localhost:8080 and Postgres will be exposed on 5432.

3) Run the app (from project root)

  ./mvnw -DskipTests package
  ./mvnw spring-boot:run -Dspring-boot.run.profiles=local

4) VS Code: passing environment variables to the debugger

  - Option A: source the env in the integrated terminal, then run the Debug configuration.
  - Option B: add an `env` section to `.vscode/launch.json` for the Java debug configuration. Example:

    {
      "version": "0.2.0",
      "configurations": [
        {
          "type": "java",
          "name": "Spring Boot",
          "request": "launch",
          "mainClass": "com.allestate.app.AppApplication",
          "projectName": "app",
          "env": {
            "SPRING_PROFILES_ACTIVE": "local",
            "SPRING_DATASOURCE_URL": "jdbc:postgresql://localhost:5432/appdb",
            "SPRING_DATASOURCE_USERNAME": "app",
            "SPRING_DATASOURCE_PASSWORD": "pass",
            "ANTHROPIC_API_KEY": "sk-REPLACE"
          }
        }
      ]
    }

5) GitHub / Codespaces

  - Add `ANTHROPIC_API_KEY` (and any other secrets) to your repository secrets in GitHub. Actions and Codespaces can read repository secrets.

Notes:
  - `scripts/env.sh` is already added to `.gitignore`. Keep secrets out of git.
  - On macOS your shell is zsh by default. `source scripts/env.sh` will load variables in the current terminal session.

