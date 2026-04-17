# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 응답 언어

모든 답변은 **한국어**로 작성한다.

## Project Overview

A multi-module Spring Boot + Vue.js bulletin board system with threaded replies, file attachments, organization hierarchy management, and password reset via email.

## Commands

### Frontend (inside `frontend/`)
```bash
npm run serve   # Dev server on port 8081 (proxies /api → localhost:8080)
npm run build   # Production build to frontend/build/public
npm run lint    # ESLint
```

### Backend (Gradle, from project root)
```bash
./gradlew :board-app:bootRun    # Run API server (port 8080)
./gradlew :board-web:bootRun    # Run web server (port 8080, serves compiled frontend)
./gradlew build                 # Full build — runs npm build as part of compileJava
```

### Active Spring profile
Pass `-Dspring.profiles.active=local` (H2 file-based, uploads to `D:/jsjeon/study/upload`) or `prod` when running.

## Architecture

### Module layout
```
root
├── module-board/     # Shared domain, MyBatis mappers, service interfaces
├── board-app/        # Spring Boot REST API server (port 8080)
├── board-web/        # Spring Boot web server — serves compiled Vue assets (port 8080)
└── frontend/         # Vue 2 SPA
```

`module-board` is a shared library depended on by both `board-app` and `board-web`. The Gradle build compiles the Vue SPA into `frontend/build/public`, which is bundled into the `board-web` JAR as static resources, so a single artifact serves the entire app in production.

### Frontend → Backend communication
- Dev: Vue CLI dev server (8081) proxies `/api/**` to `http://127.0.0.1:8080`
- Prod: frontend is embedded in `board-web`; all requests are in-process
- HTTP client: `frontend/src/utils/api.js` (Axios instance, base URL `/api`)

### Backend layers (in `module-board`)
```
Controller (board-app) → Service interface → ServiceImpl → Mapper interface → MyBatis XML mapper → H2
```
- MyBatis mapper XMLs live in `src/main/resources/com/board/mapper/`
- Domain objects in `com.board.domain` (also the MyBatis type alias package)
- `underscore_to_camel_case` auto-mapping is enabled

### Database
H2 in-memory (schema at `board-app/src/main/resources/h2/schema.sql`):
- `board` — posts; threaded via `group_id / group_order / depth`
- `board_file` — attachments
- `organization` + `organization_closure` — org hierarchy (closure table pattern)
- `password_reset` — email-based token reset

### Frontend structure (`frontend/src/`)
- `router/index.js` — 8 routes; default redirects `/` → `/board/list`
- `store/index.js` — Vuex store (minimal)
- `views/board/` — BoardList, BoardView, BoardWrite, BoardReply
- `views/organization/` — OrganizationTree (jsTree)
- `views/password/` — PasswordResetRequest, PasswordReset
- `components/` — Layout, Header, Footer, Sidebar, shared BoardList component

### Key REST endpoints
| Method | Path | Purpose |
|--------|------|---------|
| GET | `/api/board/list` | Paginated post list |
| GET | `/api/board/{seq}` | Post detail |
| POST | `/api/board` | Create post |
| PUT | `/api/board/{seq}` | Update post |
| DELETE | `/api/board/{seq}` | Delete post |
| POST | `/api/board/reply` | Create threaded reply |
| GET | `/api/board/file/download` | File download |
| GET | `/api/organization/tree` | Org hierarchy |
| POST | `/api/organization/add` | Add org node |
| POST | `/api/organization/move` | Move org node |
| POST | `/api/password/reset-request` | Send reset email |
| GET | `/api/password/verify` | Verify reset token |
| POST | `/api/password/reset` | Apply new password |

### Tech stack versions
- Spring Boot 2.7.13, Java 11, MyBatis 2.2.0
- Vue 2.7.4, Vue Router 3, Vuex 3, Bootstrap-Vue 2.22, Axios 0.27
- UI: AdminLTE template + Bootstrap 5
- Org tree: jsTree 3.3
