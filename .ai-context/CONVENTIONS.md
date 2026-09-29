# CONVENTIONS.md

**Project coding conventions derived from codebase**

## Kotlin/Java Style
- Package naming: com.example
- Imports: Standard AndroidX aliases
- Code organization: Modules (ui/, data/, domain/, core/)

## ViewModel Pattern
**Required pattern** (non-negotiable):
```kotlin
// ViewModel must use MutableStateFlow for state management
private val _state = MutableStateFlow<UiState>(UiState.Idle)
val state: StateFlow<UiState> = _state.asStateFlow()

// NEVER use mutableStateOf in ViewModels for domain state
// mutableStateOf only for local UI state within @Composable functions
```

## State Management
- UI State: Use StateFlow in ViewModels for domain data
- Local UI State: Use mutableStateOf in @Composable functions only
- No mutable state outside ViewModels for domain logic

## Navigation
- Uses Screen sealed class for route definitions
- SakuNavHost for host with container parameter

## Theme System
- Material 3 as foundation
- Custom color constants (SakuDarkGreen, SakuCreamBackground, etc.)
- Component theme integration via MyApplicationTheme

## File Structure
- Consistent two-level package depth (com.example.*)
- Domain layer isolated from UI layer
- Data layer behind repositories

## Testing
- Includes unit tests (ExampleUnitTest.kt) and instrumentation tests (ExampleInstrumentedTest.kt)
- Includes screenshot tests (GreetingScreenshotTest.kt)
- Includes integration tests (SakuDataIntegrityTest.kt, SakuFinancialCalculationTest.kt, etc.)

## Material Design
- Material 3 components throughout
- Elevated surfaces, themed typography
- Custom spacing and density tokens

## State Flow Best Practices
- Always expose as read-only StateFlow
- Private MutableStateFlow with _ prefix
- No direct external state mutation