# STATE.md

**Current project state snapshot**

## Deterministic Evidence
- **Git HEAD**: d7d23c1b83a1a1547ede07f2e5f2d0c43b295ef2
- **Build System**: Gradle 8.x with Kotlin DSL
- **Root Directory Contents**:
  - build.gradle.kts (top-level)
  - settings.gradle.kts
  - gradle/ (wrapper, libs.versions.toml)
  - gradlew
  - metadata.json
  - design-references/ (16 markdown files)
  - app/ (main source module)

## App Configuration
- **Package**: com.example
- **Theme**: Theme.MyApplication (Material 3)
- **Entry Point**: MainActivity with SakuAppContainer
- **Permissions**: INTERNET, CAMERA
- **Min SDK**: Not specified (assumed)

## Current Modifications
- **Modified**: SAKU_DECISIONS.md, SAKU_PROGRESS.md, SAKU_PROJECT_CONTEXT.md
- **Status**: All changes staged/unstaged (git status shows modifications)

## Core Dependencies
- Compose, AndroidX, Material 3
- Kotlin Coroutines (Flow)
- Hilt/KSP (Google DevTools KSP plugin present)
- Secrets Gradle plugin (API key management)

## User Preferences
- **Language**: Indonesian (Masuk, Kata Sandi)
- **UI Requirements**: Material 3 + existing theme components only
- **Style**: No new dependencies, no over-engineering
- **Structure**: Modular code, split composables (not monoliths)
- **Process**: No code before design approval

## Key Components Present
- **Auth Screens**: LoginScreen, RegisterScreen, ForgotPasswordScreen
- **Core Features**: Dashboard, Kantong (pockets), Transactions, Profile, Reports, Export
- **Database**: SakuDatabase with DAOs
- **Services**: CurrencyConversionService, ReceiptScannerService, ApiKeyConfigService
- **UI Components**: SakuComponents, ProfileComponents
- **Testing**: Comprehensive test suite including Robolectric and unit tests

## Navigation System
- Bottom navbar with 5 items
- Curved notch center design
- Elevated FAB
- Screen sealed class route definitions

## Component Architecture
- **ViewModels**: TransactionViewModel, ProfileViewModel, etc.
- **Composable Structure**: Modularized with clear separation
- **Theme Integration**: Custom colors and Material 3 components

## Security
- Camera permission for receipt scanning
- Secrets plugin for API key management
- FileProvider for sharing functionality

## Build Configuration
- Debug build available (assembleDebug)
- Proguard rules present
- Data extraction and backup rules configured
- RoRobazzi for screenshot testing