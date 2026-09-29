# MAP.md

**Deterministic project map generated from current state**

## Project Overview
- **Project**: Saku (Personal Finance App)
- **Language**: Kotlin
- **Platform**: Android (Composable UI)
- **Build System**: Gradle (Kotlin DSL)
- **Primary Language**: Indonesian (UI text: "Masuk", "Kata Sandi")

## Core Components

### app/src/main/
- `MainActivity.kt` - Entry point with SakuAppContainer
- `AndroidManifest.xml` - Standard permissions (Internet, Camera), Material theme
- `ui/` - Complete UI layer with navigation

### Key Directories
- `ui/screens/` - All app screens (auth, dashboard, kantong, profile, report, transaction, export)
- `ui/navigation/` - NavHost and Screen definitions
- `ui/theme/` - Material 3 theming
- `data/` - Repository, database, services
- `domain/` - Domain models and interfaces

## Architecture
- **ViewModel Pattern**: MutableStateFlow + StateFlow for domain state (ViewModels only)
- **Separation**: UI (UI layer) vs Domain (business logic) vs Data (repositories, database)
- **Navigation**: Screen sealed class routes, SakuNavHost

## Theme System
- Colors: SakuDarkGreen (primary), SakuCreamBackground, SakuCreamSurface, SakuExpenseRed, SakuGoldAccent
- Components: Bottom navbar, elevated FAB, curved notch center
- Text styles: SakuTextPrimary, SakuTextMuted

## Dependencies
- Build: android-application, kotlin-compose, google-services, roborazzi, secrets
- Core: Compose, AndroidX, Material 3

## Git State
- Current HEAD: d7d23c1b83a1a1547ede07f2e5f2d0c43b295ef2
- Modified: SAKU_DECISIONS.md, SAKU_PROGRESS.md, SAKU_PROJECT_CONTEXT.md
- Context mode: init