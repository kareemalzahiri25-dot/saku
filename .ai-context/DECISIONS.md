# DECISIONS.md

**Project decisions and rationale**

## Architecture Decisions

### Navigation Architecture
- **Decision**: Screen sealed class + SakuNavHost
- **Rationale**: Type-safe navigation with compile-time safety, clear route definitions

### State Management Pattern
- **Decision**: MutableStateFlow + StateFlow in ViewModels
- **Rationale**: Flow-based reactive state management consistent with Kotlin ecosystem, eliminates lifecycle issues, preserves state across configuration changes

### Theme System
- **Decision**: Material 3 + custom colors (SakuDarkGreen, SakuCreamBackground, etc.)
- **Rationale**: Modern Material Design foundation with brand-specific colors, consistent with existing codebase

### Component Architecture
- **Decision**: Split composables (modularization)
- **Rationale**: Avoids monolithic UI components, improves maintainability and testability

## Technology Decisions

### Compose Multiplatform
- **Decision**: Used for UI (Android target)
- **Rationale**: Modern declarative UI, integrates well with Android ecosystems

### Kotlin Coroutines with Flow
- **Decision**: State management and data flows
- **Rationale**: Native Android async support, reactive programming paradigm

### Hilt/KSP
- **Decision**: Dependency injection with Kotlin Symbol Processing
- **Rationale**: Compile-time safe DI, eliminates runtime reflection overhead

### Material 3
- **Decision**: Primary design system
- **Rationale**: Latest Material Design spec, Android-first implementation

## Security Decisions

### Camera Permission
- **Decision**: Required for receipt scanning feature
- **Rationale**: Core functionality for expense entry via receipt capture

### Secrets Plugin
- **Decision**: API key management via Gradle secrets plugin
- **Rationale**: Secure credential management, no hardcoded keys in codebase

## Testing Strategy

### Multiple Test Types
- **Decision**: Unit tests + Robolectric + integration tests
- **Rationale**: Comprehensive coverage from unit to end-to-end

### Screenshot Testing
- **Decision**: RoRobazzi for visual regression
- **Rationale**: Ensures UI consistency across changes

## UI/UX Decisions

### Bottom Navigation
- **Decision**: 5-item bottom navbar with curved notch center
- **Rationale**: Material 3 guidelines, modern mobile app pattern

### Elevated FAB
- **Decision**: Floating action button with elevation
- **Rationale**: Material Design emphasis on depth and hierarchy

### Indonesian Language
- **Decision**: UI text in Indonesian (Masuk, Kata Sandi)
- **Rationale**: Target user base primary language

## Data Layer Decisions

### Repository Pattern
- **Decision**: SakuRepository interface with implementations
- **Rationale**: Abstraction layer for data sources, testability

### Room Database
- **Decision**: Local persistence with SakuDatabase
- **Rationale**: Android native solution, compile-time SQL safety

### Architecture Layers
- **Decision**: Domain (models/interfaces) -> Data (repositories/services) -> UI (presentation)
- **Rationale**: Clean separation of concerns, testability

## Build System Decisions

### Gradle Kotlin DSL
- **Decision**: Build scripts in .gradle.kts
- **Rationale**: Kotlin syntax for builds, better IDE integration

### Plugin Configuration
- **Decision**: Standard Android plugins + third-party plugins
- **Rationale**: Modern Android development best practices

## Code Quality Decisions

### No Redundant Code
- **Decision**: Minimal Ponytail solutions (YAGNI principle)
- **Rationale**: Avoid over-engineering, focused on requirements

### Type Safety
- **Decision**: Sealed classes for navigation, Flow types
- **Rationale**: Compile-time safety, developer experience

## Componentization Decision

### Split Composables
- **Decision**: Modular UI components (not monolithic)
- **Rationale**: Maintainability, reusability, testability

## Performance Considerations

### Lazy Loading
- **Decision**: Efficient data loading patterns
- **Rationale**: Better app responsiveness, reduced memory usage

## Localization Decision

### Indonesian Focus
- **Decision**: Indonesian UI strings and terminology
- **Rationale**: Target market user experience

## Error Handling Decisions

### Platform-Specific
- **Decision**: Native Android error handling patterns
- **Rationale**: Consistent with platform conventions

## Security Decision Summary
- **Secrets Plugin**: API key management
- **FileProvider**: Safe file sharing
- **Permissions**: Minimal required (Internet, Camera)

## Architecture Philosophy
- **Separation**: Clear separation of concerns (Domain/Data/UI)
- **Testability**: Modular components enable unit testing
- **Maintainability**: Type safety and clear abstractions
- **User Experience**: Material Design with brand customization