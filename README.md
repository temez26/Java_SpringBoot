# Java Spring Boot Learning Projects

This repository has **two separate Spring Boot projects** that I built while learning Spring Boot from tutorials:

| Folder | What it is | Tech |
|---|---|---|
| [`tasks/`](tasks/) | **The main app.** A full-stack task manager: a REST API in Spring Boot plus a React web UI. You make task lists, add tasks to them, and track progress. | Java 21, Spring Boot 3.5, Spring Data JPA, PostgreSQL, Maven, React + TypeScript + Vite |
| [`spring_demo/`](spring_demo/) | A small practice project for learning Spring basics: dependency injection, `@Service` beans, interfaces and a simple controller. It has no real features. | Java 21, Spring Boot 3.5, Gradle |

The projects don't depend on each other. Each one builds and runs on its own.

---

## 1. `tasks/` — Task Manager (main application)

### The main idea

You create **task lists**, for example "Groceries" or "School project". Each list holds **tasks**. A task has:

- a title and description
- a due date
- a priority (`HIGH`, `MEDIUM`, `LOW`)
- a status (`OPEN` or `CLOSED`)

Each list shows a **progress** value: the share of its tasks that are `CLOSED`.

The app has three parts that talk to each other like this:

```
 Browser (React UI, port 5173)
        │  HTTP requests to /api/...   (Vite forwards them to port 8080)
        ▼
 Spring Boot REST API (port 8080)
        │  JPA / Hibernate
        ▼
 PostgreSQL database (port 5432, runs in Docker)
```

### How the backend is organized (layers)

A request goes through the backend in this order:

```
Controller  →  Mapper (DTO → Entity)  →  Service  →  Repository  →  Database
Controller  ←  Mapper (Entity → DTO)  ←  Service  ←  Repository  ←
```

Here is each folder under `tasks/src/main/java/com/example/tasks/`:

| Folder / file | Role | What's inside |
|---|---|---|
| `TasksApplication.java` | Entry point | The `main` method that starts Spring Boot. |
| `controllers/` | **HTTP layer.** Receives web requests and returns JSON. | `TaskListController` handles `/api/task-lists`. `TaskController` handles `/api/task-lists/{id}/tasks`. `GlobalExceptionHandler` turns an `IllegalArgumentException` into a `400 Bad Request` JSON error. |
| `domain/entities/` | **Database model.** JPA classes mapped to tables. | `TaskList` maps to the `task_lists` table. `Task` maps to the `tasks` table. One list has many tasks. The enums are `TaskPriority` and `TaskStatus`. |
| `domain/dto/` | **API model.** The JSON shapes the API sends and receives (Java `record`s). | `TaskListDto` (includes `count` and `progress`), `TaskDto` and `ErrorResponse`. |
| `mappers/` | **Converts** between entities and DTOs. This keeps the database model separate from the JSON. | `TaskMapper` and `TaskListMapper` are interfaces. The `impl/` folder holds the code. `TaskListMapperImpl` also calculates `progress`. |
| `services/` | **Business logic and validation.** | The `TaskService` and `TaskListService` interfaces, with the code in `impl/`. Examples of the rules: a new item must not have an ID yet, and a title is required. New tasks start as `OPEN` and default to `MEDIUM` priority. The service also sets the `created` and `updated` timestamps. |
| `repositories/` | **Data access.** Spring Data JPA interfaces, so you don't write SQL. | `TaskListRepository` and `TaskRepository`. Spring builds queries from method names, such as `findByTaskListIdAndId`. |

Other files in `tasks/`:

| File | Purpose |
|---|---|
| `pom.xml` | Maven build file: dependencies (web, data-jpa, postgresql, h2 for tests) and the Java version. |
| `mvnw`, `mvnw.cmd`, `.mvn/` | Maven Wrapper. Runs Maven without installing it. |
| `compose.yml` | Starts a **PostgreSQL** container for local development (user `postgres`, password `password`). |
| `src/main/resources/application.properties` | Database connection settings. `ddl-auto=update` makes Hibernate create and update the tables itself. |
| `src/test/resources/application.properties` | Tests use an in-memory **H2** database instead, so they don't need Docker. |
| `src/test/.../TasksApplicationTests.java` | A basic test that checks the application starts. |

### REST API

Base URL: `http://localhost:8080`

**Task lists**

| Method | URL | What it does |
|---|---|---|
| `GET` | `/api/task-lists` | List all task lists |
| `POST` | `/api/task-lists` | Create a list. Body: `{ "title": "...", "description": "..." }` |
| `GET` | `/api/task-lists/{listId}` | Get one list (including its tasks, count and progress) |
| `PUT` | `/api/task-lists/{listId}` | Update a list. The body must include the same `id`. |
| `DELETE` | `/api/task-lists/{listId}` | Delete a list and all its tasks |

**Tasks in a list**

| Method | URL | What it does |
|---|---|---|
| `GET` | `/api/task-lists/{listId}/tasks` | List the tasks in a list |
| `POST` | `/api/task-lists/{listId}/tasks` | Create a task. Body: `{ "title", "description", "dueDate", "priority" }` |
| `GET` | `/api/task-lists/{listId}/tasks/{taskId}` | Get one task |
| `PUT` | `/api/task-lists/{listId}/tasks/{taskId}` | Update a task. `id`, `priority` and `status` are required. |
| `DELETE` | `/api/task-lists/{listId}/tasks/{taskId}` | Delete a task |

Example of a task in JSON:

```json
{
  "id": "3f1c...",
  "title": "Buy milk",
  "description": "2 liters",
  "dueDate": "2026-11-12T10:00:00",
  "priority": "HIGH",
  "status": "OPEN"
}
```

If validation fails, the API returns `400` with a body like this:

```json
{ "status": 400, "message": "Task must have a title!", "details": "uri=/api/task-lists/.../tasks" }
```

### Frontend: `tasks/tasks-ui/`

A React single-page app (TypeScript, Vite, Tailwind CSS and the NextUI component library, dark theme). The UI comes from the tutorial (see `tasks-ui/LICENSE`).

| Folder / file | Role |
|---|---|
| `src/main.tsx` | Entry point. Wraps the app in `AppProvider` (data) and `NextUIProvider` (UI theme). |
| `src/App.tsx` | **Routes (pages):** see the table below. |
| `src/AppProvider.tsx` | **All API calls and app state.** It uses React Context and `useReducer`, and calls the backend with `axios`. Components get data through `useAppContext()`. |
| `src/components/` | The four screens (pages) of the app. |
| `src/domain/` | TypeScript types that match the backend DTOs: `Task`, `TaskList`, `TaskPriority`, `TaskStatus`. |
| `vite.config.ts` | Dev server config. It **proxies `/api` to `http://localhost:8080`**, so the browser only talks to port 5173. |
| `Dockerfile`, `docker-compose.yml` | Optional: run the frontend dev server in Docker. |
| `tailwind.config.js`, `postcss.config.js`, `eslint.config.js`, `tsconfig*.json` | Styling, linting and TypeScript settings. |

Pages:

| URL | Component | What you see |
|---|---|---|
| `/` | `TaskListsScreen` | All task lists with their progress. A button creates a new list. |
| `/new-task-list` | `CreateUpdateTaskListScreen` | A form to create a list |
| `/edit-task-list/:listId` | `CreateUpdateTaskListScreen` | The same form, used to edit a list |
| `/task-lists/:listId` | `TasksScreen` | The tasks in one list: tick a task to open or close it, edit or delete it, or delete the whole list |
| `/task-lists/:listId/new-task` | `CreateUpdateTaskScreen` | A form to create a task |
| `/task-lists/:listId/edit-task/:taskId` | `CreateUpdateTaskScreen` | The same form, used to edit a task |

### How to run it

Requirements: **Java 21**, **Docker** (for PostgreSQL) and **Node.js 18+**.

**Step 1: start the database** (from the `tasks/` folder)

```bash
docker compose up -d
```

**Step 2: start the backend** (from the `tasks/` folder; on Windows use `mvnw.cmd` instead of `./mvnw`)

```bash
./mvnw spring-boot:run
```

The API runs at http://localhost:8080. The tables are created on the first start.

**Step 3: start the frontend** (from the `tasks/tasks-ui/` folder)

```bash
npm install
```

```bash
npm run dev
```

Open http://localhost:5173.

**Run the backend tests** (H2 in-memory, no Docker needed; from the `tasks/` folder)

```bash
./mvnw test
```

---

## 2. `spring_demo/` — Spring basics practice

A small project for learning how **Spring manages objects (beans)** and **dependency injection**. It is not part of the task app.

| File | What it shows |
|---|---|
| `DemoApplication.java` | Starts Spring and gets the `OrderService` bean from the `ApplicationContext`. It sets a payment service and calls `placeOrder()`, which prints to the console. |
| `PaymentService.java` | An **interface** with one method, `processPayment(amount)`. |
| `StripePaymentService.java`, `PayPalPaymentService.java` | Two **implementations** of that interface. Each one only prints text to the console. This shows how you can swap one implementation for another. |
| `OrderService.java` | A `@Service` that uses a `PaymentService`. It is used to practice setter injection compared with creating objects with `new`. |
| `HomeController.java` + `resources/static/index.html` | Returns a simple "Hello world!" page at `/`. |
| `build.gradle`, `gradlew`, `gradlew.bat` | Gradle build file and Gradle Wrapper. This project uses **Gradle**; `tasks/` uses Maven. |

Run it from the `spring_demo/` folder (on Windows use `gradlew.bat`):

```bash
./gradlew bootRun
```

Then open http://localhost:8080. The payment output shows in the console.

> Both projects use port **8080**, so run only one at a time, or change `server.port` in `application.properties`.

---

