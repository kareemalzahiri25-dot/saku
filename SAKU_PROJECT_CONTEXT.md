# SAKU_PROJECT_CONTEXT.md

**Main context document for Saku project — enables any AI/engineer to understand the project immediately.**

---

## Project Identity

| Field | Value |
|-------|-------|
| **Name** | Saku |
| **Tagline** | "Catat. Atur. Tumbuh." |
| **Purpose** | Personal finance tracking app for Indonesian users — expense tracking, budget planning (Pockets), asset management, and financial reports |
| **Target Use** | Offline-first personal finance management on Android |
| **Core Principles** | 1. **Asset-centric** — Assets hold actual money balance<br>2. **Pocket as planning target** — Pockets track goals, not balances<br>3. **Realization from expenses** — Pocket progress = sum of ExpenseAllocations<br>4. **Offline-first** — Local database (Room) as source of truth<br>5. **Indonesian UX** — "Masuk" not "Login", "Kata Sandi" not "Password" |

---

## Technology Stack (Verified from `app/build.gradle.kts`)

| Technology | Status | Version/Notes |
|------------|--------|---------------|
| **Kotlin** | ✅ Implemented | KSP for code generation |
| **Jetpack Compose** | ✅ Implemented | Material 3, Compose BOM |
| **Material 3** | ✅ Implemented | `androidx.compose.material3` |
| **Navigation Compose** | ✅ Implemented | `androidx.navigation.compose` |
| **Room** | ✅ Implemented | `androidx.room.ktx`, `androidx.room.runtime`, KSP compiler |
| **DataStore** | ❌ Not Used | Dependency commented out in build.gradle.kts |
| **CameraX / OCR** | ❌ Not Used | Dependencies commented out |
| **Coil** | ✅ Implemented | Image loading (`libs.coil.compose`) |
| **Moshi** | ✅ Implemented | JSON serialization (`libs.moshi.kotlin`) |
| **Retrofit / OkHttp** | ✅ Implemented | Networking (`libs.retrofit`, `libs.okhttp`) |
| **Firebase AI / AppCheck** | ✅ Implemented | `libs.firebase.ai`, `libs.firebase.appcheck.*` |
| **Coroutines / Flow** | ✅ Implemented | `kotlinx.coroutines` |
| **Roborazzi** | ✅ Implemented | Screenshot testing (`libs.roborazzi`) |
| **Min SDK** | 24 | Android 7.0 |
| **Target SDK** | 36 | Android 14 |

---

## Architecture

**Structure (verified from actual folder layout):**

```
com.example
├── data
│   ├── local
│   │   ├── dao/           # DAO interfaces (Room)
│   │   ├── entity/        # Room entities (@Entity)
│   │   ├── SakuDatabase.kt      # Room database + migrations
│   │   └── SakuDatabaseCallback.kt
│   ├── repository
│   │   ├── SakuRepository.kt       # Repository interface
│   │   └── SakuRepositoryImpl.kt   # Implementation
│   └── remote/            # (empty / placeholder)
├── domain
│   └── model/
│       └── Models.kt      # Domain models (Asset, Pocket, Transaction, PocketAllocation, ExpenseAllocation, PocketStats, etc.)
├── ui
│   ├── components/        # Reusable Compose components
│   ├── navigation/
│   │   ├── SakuNavHost.kt       # Navigation graph (5 tabs + FAB)
│   │   └── Screen.kt            # Sealed class for routes
│   ├── screens/
│   │   ├── auth/                # Login, Register, ForgotPassword
│   │   ├── dashboard/           # DashboardScreen, DashboardViewModel
│   │   ├── kantong/             # KantongScreen, KantongViewModel
│   │   ├── profile/             # Profile, InformasiPribadi, Avatar
│   │   ├── report/              # ReportScreen, ReportViewModel
│   │   └── transaction/         # AddTransactionScreen, TransactionViewModel
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── SakuComponents.kt
└── di/                    # (if exists) Dependency injection
```

**Dependency Flow:**

```
UI (Compose Screens)
    ↓ collects StateFlow
ViewModel (MutableStateFlow → StateFlow)
    ↓ calls suspend functions
Repository (SakuRepositoryImpl)
    ↓ uses DAOs
Room DAO (Kotlin Flow / suspend)
    ↓
SQLite Database
```

- **No DataStore** — Preferences not yet implemented
- **No remote sync** — Fully offline
- **ViewModel pattern**: `private val _state = MutableStateFlow(...)` + `val state: StateFlow = _state`

---

## Screens (Status & Function)

| Screen | File | Status | Function |
|--------|------|--------|----------|
| **Login** | `LoginScreen.kt` + `AuthViewModel.kt` | ✅ Implemented | Email/password login, "Masuk" terminology |
| **Register** | `RegisterScreen.kt` + `AuthViewModel.kt` | ✅ Implemented | User registration |
| **Forgot Password** | `ForgotPasswordScreen.kt` + `AuthViewModel.kt` | ✅ Implemented | Password reset flow |
| **Dashboard** | `DashboardScreen.kt` + `DashboardViewModel.kt` | ✅ Implemented | FinancialSummary, asset balances, recent transactions, pocket progress cards |
| **Kantong (Pockets)** | `KantongScreen.kt` + `KantongViewModel.kt` | ✅ Implemented | Pocket list, detail, create/edit, progress bars from PocketStats.realization, allocations tab (UI only) |
| **Tambah Transaksi** | `AddTransactionScreen.kt` + `TransactionViewModel.kt` | ✅ Implemented | INCOME/EXPENSE/TRANSFER, single-pocket selection for EXPENSE, asset picker, category picker, date, note |
| **Laporan (Reports)** | `ReportScreen.kt` + `ReportViewModel.kt` | ✅ Implemented | Monthly cashflow, category breakdown |
| **Profil** | `ProfileScreen.kt` + `ProfileViewModel.kt` | ✅ Implemented | User info, avatar, settings |
| **Export/Backup** | `ExportBackupScreen.kt` + `ExportBackupViewModel.kt` | ✅ Implemented | Data export functionality |

**Navigation:** 5-tab bottom bar (Dashboard, Kantong, Add Transaction [FAB center], Laporan, Profil) — `SakuNavHost.kt`

---

## Core Domain

### Asset
- **Definition**: Pemilik saldo uang aktual (rekening bank, e-wallet, tunai, investasi, crypto)
- **Key Field**: `Asset.balance` = saldo aktual (Double)
- **Types**: `BANK`, `E_WALLET`, `CASH`, `INVESTMENT`, `CRYPTO`, `OTHER`
- **Transaction Impact**:
  - `INCOME` → `Asset.balance += amount`
  - `EXPENSE` → `Asset.balance -= amount` (validated: balance ≥ amount)
  - `TRANSFER` → Source `Asset.balance -= amount`, Target `Asset.balance += amount`
- **No balance field on Pocket** — Asset is the only balance holder

### Pocket
- **Definition**: Target pengeluaran/tabungan (bukan rekening/dompet)
- **No `balance` field** — Never has balance
- **Key Fields**:
  - `targetAmount: Double` — Target nominal
  - `realization` — **Derived** from `SUM(ExpenseAllocation.amount)` via `PocketStats`
  - `remaining = targetAmount - realization`
  - `progress = realization / targetAmount` (coerced 0.0–1.0 for UI, actual value shown numerically)
- **State Flags**: `isActive`, `completed`, `archived`
- **Pocket-to-Pocket Transfer**: ❌ Removed (deprecated in Phase 3)
- **Saldo Awal (Initial Balance)**: ❌ Removed (Phase 3)

### PocketAllocation
- **Definition**: Perencanaan/intent alokasi dana dari **Asset → Pocket**
- **Model**: `PocketAllocation(id, assetId, pocketId, allocatedAmount)`
- **Does NOT reduce Asset.balance** — Pure planning intent
- **Source of**: `PocketStats.plannedAllocation = SUM(PocketAllocation.allocatedAmount)` per pocket
- **Repository Operations**: `insertAllocation`, `updateAllocation`, `getAllocationsByAsset`, `getAllocationsByPocket` — **Implemented but NOT connected to UI**
- **UI Status**: ❌ Not implemented (no AssetDetailScreen, no allocation management in PocketDetail)

### ExpenseAllocation
- **Definition**: Hubungan **EXPENSE Transaction → Pocket** (realisasi)
- **Model**: `ExpenseAllocation(id, transactionId, pocketId, amount)`
- **Source of Truth for**: `PocketStats.realization = SUM(ExpenseAllocation.amount)` per pocket
- **Infrastructure Status**: ✅ Fully implemented
  - Entity: `ExpenseAllocationEntity` (Room, table `expense_allocations`)
  - DAO: `ExpenseAllocationDao` (insert, getByTransaction, getByPocket, getTotalRealization, delete)
  - Migration: `MIGRATION_2_3` created table + migrated existing `Transaction.pocketId` data
|- **Creation Logic**: In `SakuRepositoryImpl.insertTransaction()` line 278–286:
  ```kotlin
  if (transaction.type == TransactionType.EXPENSE && !transaction.pocketId.isNullOrBlank()) {
      expenseAllocationDao.insertAllocation(ExpenseAllocationEntity(...))
  }
  ```
|- **ID Generation**: UUID (since Phase 4.1). Previously `ea_${System.currentTimeMillis()}` had collision risk for multi-pocket splits.
|- **Multi-Pocket Support**: Infrastructure supports multiple allocations per transaction (DAO has `insertAllocations(list)`), **but UI only supports single-pocket selection** (AddTransactionScreen uses single `selectedPocketId`)

### Transaction
| Field | Status | Notes |
|-------|--------|-------|
| `id` | ✅ Active | Primary key |
| `title` | ✅ Active | User-entered |
| `amount` | ✅ Active | > 0 validated |
| `type` | ✅ Active | `INCOME` \| `EXPENSE` \| `TRANSFER` |
| `categoryId` / `categoryName` / `categoryIcon` | ✅ Active | Denormalized |
| `assetId` / `assetName` | ✅ Active | Source asset (required) |
| `targetAssetId` / `targetAssetName` | ✅ Active | **Only for TRANSFER** (Asset→Asset) |
| `dateMillis` | ✅ Active | Transaction date |
| `note` | ✅ Active | Optional |
| `receiptImageUrl` | ✅ Active | Optional |
|| **`pocketId`** | 🟡 **DEPRECATED** | Legacy single-pocket ref. Still written by UI → Repository creates ExpenseAllocation from it. Marked `@Deprecated` in domain model (Phase 4.1). Remove in Phase 4.3. |
|| **`pocketName`** | 🟡 **DEPRECATED** | Denormalized legacy field. Marked `@Deprecated` in domain model (Phase 4.1). Remove in Phase 4.3. |
|| **`targetPocketId`** | 🟡 **DEPRECATED** | Pocket→Pocket transfer (removed). Marked `@Deprecated` in domain model (Phase 4.1). |
|| **`targetPocketName`** | 🟡 **DEPRECATED** | Pocket→Pocket transfer (removed). Marked `@Deprecated` in domain model (Phase 4.1). |

**Source of Truth:**
- Pocket assignment for EXPENSE → **ExpenseAllocation table** (not Transaction.pocketId)
- Pocket assignment for TRANSFER → **Asset-to-Asset only** (targetAssetId)
- Transaction.pocketId is **legacy compatibility field**, retained for migration safety

---

## Data Flow

### INCOME Flow
```
User: AddTransactionScreen (type=INCOME, assetId, amount)
    ↓
TransactionViewModel.saveTransaction()
    ↓
SakuRepositoryImpl.insertTransaction()
    → database.withTransaction {
        1. assetDao.getAssetById(assetId)
        2. Asset.balance += amount
        3. assetDao.updateAsset(updatedAsset)
        4. transactionDao.insertTransaction(entity)
    }
    ↓
Dashboard/Kantong: FinancialSummary.totalIncome updated via Flow
```

### EXPENSE Flow
```
User: AddTransactionScreen (type=EXPENSE, assetId, pocketId, amount)
    ↓
TransactionViewModel.saveTransaction()
    ↓
SakuRepositoryImpl.insertTransaction()
    → database.withTransaction {
        1. assetDao.getAssetById(assetId)
        2. Asset.balance -= amount (validated: balance ≥ amount)
        3. assetDao.updateAsset(updatedAsset)
        4. transactionDao.insertTransaction(entity)
        5. IF pocketId not blank → expenseAllocationDao.insertAllocation(
               id=UUID.randomUUID().toString(), transactionId, pocketId, amount)
    }
    ↓
ExpenseAllocation created → PocketStats.realization = SUM(ExpenseAllocation.amount)
    ↓
Dashboard/Kantong/Report: PocketStats.realization updated via Flow (getPocketStats())
```

### TRANSFER Flow (Asset → Asset only)
```
User: AddTransactionScreen (type=TRANSFER, sourceAssetId, targetAssetId, amount)
    ↓
TransactionViewModel.saveTransaction()
    ↓
SakuRepositoryImpl.insertTransaction()
    → database.withTransaction {
        1. sourceAsset.balance -= amount (validated)
        2. targetAsset.balance += amount
        3. Both assets updated
        4. transactionDao.insertTransaction(entity with targetAssetId)
    }
    ↓
Dashboard: Both asset balances updated via Flow
```

### Planning Flow (Asset → PocketAllocation → PocketStats)
```
User: (Future) AssetDetailScreen → allocate to Pocket
    ↓
Repository.insertAllocation(PocketAllocation)
    ↓
PocketAllocation table updated
    ↓
PocketStats.plannedAllocation = SUM(PocketAllocation.allocatedAmount)
    ↓
Kantong/Dashboard: Planned allocation shown (progress bar target)
```

---

## Current Constraints (Locked Rules)

1. **No UI Redesign** — Cannot redesign any screen without explicit instruction
2. **No New Dependencies** — Use only existing libraries in build.gradle.kts
3. **Material 3 + Existing Theme Only** — Colors: `SakuDarkGreen`, `SakuCreamBackground`, `SakuCreamSurface`, `SakuCreamBorder`, `SakuTextPrimary`, `SakuTextMuted`, `SakuExpenseRed`, `SakuGoldAccent`
4. **Modular Composables** — Split components, no monolith screens
5. **Indonesian UI Text** — "Masuk", "Kata Sandi", "Kantong", "Laporan", "Profil"
6. **No Pocket Balance** — Pocket never has balance field
7. **No Pocket-to-Pocket Transfer** — Removed in Phase 3
8. **No Saldo Awal Pocket** — Removed in Phase 3
9. **ExpenseAllocation is Source of Truth** — For Pocket realization
10. **Single-Pocket EXPENSE UI** — Current UI only supports one pocket per expense (Phase 4.1 scope)
11. **Phase Gates** — Plan → Review → Implement → Verify → Checkpoint
12. **No Domain Decision Changes** — Without explicit approval
13. **ViewModel Pattern** — `MutableStateFlow` for private, `StateFlow` exposed
14. **Offline-First** — Room as source of truth, no network sync
15. **Git Checkpoint Per Phase** — Clean working tree before next phase

---

*Document generated from actual source code audit. Last verified against commit `d7d23c1`.*