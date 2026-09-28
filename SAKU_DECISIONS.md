# SAKU_DECISIONS.md

**Architecture & Product Decision Log — Saku Project**

Records all locked decisions, open questions, and future considerations. Each entry includes decision, reason, current implementation state, and phase when decided.

---

## Legend

| Label | Meaning |
|-------|---------|
| 🔒 **LOCKED** | Decision final, cannot change without explicit approval |
| 🔓 **OPEN** | Under discussion, not yet finalized |
| 🔮 **FUTURE** | Known future need, not yet decided |

---

## Domain Decisions

### 1. Pocket Has No Balance
- **Label**: 🔒 LOCKED
- **Decision**: Pocket is a planning target only. It does not hold money. `Asset.balance` is the only actual balance.
- **Reason**: Separates "where money lives" (Asset) from "what money is for" (Pocket). Enables multi-asset funding for one pocket.
- **Current Implementation**: `Pocket` model has no `balance` field (Models.kt:30-67). `PocketStats.realization` derived from `ExpenseAllocation`.
- **Phase**: Phase 3 (aligned in Phase 3.2)

### 2. Asset.balance Is Actual Money
- **Label**: 🔒 LOCKED
- **Decision**: `Asset.balance` represents real, spendable money. All INCOME/EXPENSE/TRANSFER mutate Asset.balance directly.
- **Reason**: Single source of truth for cash position. Simplifies reconciliation.
- **Current Implementation**: `SakuRepositoryImpl.insertTransaction()` updates Asset.balance atomically within `database.withTransaction` (lines 234-262).
- **Phase**: Phase 1 (foundational)

### 3. Pocket Realization from ExpenseAllocation
- **Label**: 🔒 LOCKED
- **Decision**: `PocketStats.realization = SUM(ExpenseAllocation.amount)` for that pocket. Not from Transaction.pocketId.
- **Reason**: Enables multi-pocket expense splits, audit trail, and correct realization even if transaction edited.
- **Current Implementation**: `SakuRepositoryImpl.getPocketStats()` line 376-390 computes realization from `expenseAllocationDao.getAllAllocations()`. Migration MIGRATION_2_3 populated `expense_allocations` from legacy `Transaction.pocketId`.
- **Phase**: Phase 3 (migration in MIGRATION_2_3, verified Phase 3.2)

### 4. Pocket-to-Pocket Transfer Removed
- **Label**: 🔒 LOCKED
- **Decision**: Direct Pocket→Pocket transfers are not supported. All transfers are Asset→Asset.
- **Reason**: Pocket has no balance → cannot transfer from it. Asset→Asset transfers correctly move actual money.
- **Current Implementation**: 
  - `TransactionType.TRANSFER` only uses `targetAssetId` (Models.kt:82-83)
  - `targetPocketId` / `targetPocketName` marked `@Deprecated` (Models.kt:90-93)
  - Migration MIGRATION_2_3 dropped pocket transfer columns from transactions table
- **Phase**: Phase 3.2

### 5. Asset-to-Asset Transfer Retained
- **Label**: 🔒 LOCKED
- **Decision**: Transfers between Assets (bank→e-wallet, cash→bank, etc.) are fully supported.
- **Reason**: Represents real money movement between accounts.
- **Current Implementation**: `insertTransaction()` handles TRANSFER type with dual asset balance update (lines 247-262).
- **Phase**: Phase 1

### 6. No Pocket Initial Balance (Saldo Awal)
- **Label**: 🔒 LOCKED
- **Decision**: Pockets cannot have an initial balance. Progress starts at 0 and grows only from ExpenseAllocations.
- **Reason**: Pocket is a target, not a container. "Saldo Awal" conflated Pocket with Asset.
- **Current Implementation**: `Pocket` model has no `allocatedAmount` or `balance` field. Migration MIGRATION_2_3 dropped `allocatedAmount` from pockets table.
- **Phase**: Phase 3.2

### 7. Pocket Realization Not Directly Editable
- **Label**: 🔒 LOCKED
- **Decision**: Users cannot manually edit `PocketStats.realization`. It is **always** derived from ExpenseAllocations.
- **Reason**: Prevents data inconsistency. Realization = actual spending, not aspirational.
- **Current Implementation**: `PocketStats.realization` is computed property in repository query (getPocketStats line 390). No UI input for realization.
- **Phase**: Phase 3

### 8. PocketAllocation Is Planning Only
- **Label**: 🔒 LOCKED
- **Decision**: `PocketAllocation` (Asset → Pocket) represents **intent to fund**, not actual funding. Does not reduce Asset.balance.
- **Reason**: Separates planning from execution. Allows "I plan to put 5M into Savings" without moving money.
- **Current Implementation**: 
  - Model: `PocketAllocation(id, assetId, pocketId, allocatedAmount)` (Models.kt:133-138)
  - Repository: `insertAllocation`, `updateAllocation`, queries exist (SakuRepositoryImpl)
  - **UI: NOT IMPLEMENTED** — No screen to create/view allocations
- **Phase**: Phase 3 (domain), Phase 4.2 (UI planned)

### 9. PocketAllocation UI Not Implemented
- **Label**: 🔒 LOCKED
- **Decision**: PocketAllocation domain + repository exist, but NO UI screens connect to them.
- **Reason**: Phase 3 focused on domain alignment. UI deferred to Phase 4.2.
- **Current Implementation**: Repository methods exist but unused. No AssetDetailScreen, no allocation tab in PocketDetail.
- **Phase**: Phase 3 (known gap)

### 10. Multi-Pocket Expense Not in Phase 4.1
- **Label**: 🔒 LOCKED
- **Decision**: Phase 4.1 only hardens **single-pocket** expense flow. Multi-pocket expense split is Phase 4.3.
- **Reason**: Infrastructure (ExpenseAllocation table, DAO batch insert) already supports multi-pocket. UI/VM changes needed for 4.3.
- **Current Implementation**: 
  - DAO has `insertAllocations(List<ExpenseAllocationEntity>)`
  - AddTransactionScreen only has single `selectedPocketId`
  - TransactionViewModel creates Transaction with single pocketId
- **Phase**: Phase 4 Planning (this audit)

---

## Technical Architecture Decisions

### 11. No UI Redesign Without Explicit Instruction
- **Label**: 🔒 LOCKED
- **Decision**: AI/agent must not redesign screens, change layouts, or modify UX flow without user explicitly asking.
- **Reason**: User owns product design. Agent implements approved specs only.
- **Current Implementation**: Enforced in all agent instructions.
- **Phase**: Ongoing (from project start)

### 12. Hermes as Planning/Review Agent
- **Label**: 🔒 LOCKED
- **Decision**: Hermes Agent performs planning, audit, review, specification. Does NOT write production code.
- **Reason**: Separation of concerns. Planning requires different mindset than implementation.
- **Current Implementation**: This project uses Hermes for Phase 0-4 planning.
- **Phase**: Project start

### 13. Coding Agent as Implementation Agent
- **Label**: 🔒 LOCKED
- **Decision**: Separate coding agent (Claude Code, Codex, etc.) writes production code based on Hermes specifications.
- **Reason**: Implementation requires different tooling and focus.
- **Current Implementation**: Not yet invoked — Phase 4.1 will be first handoff.
- **Phase**: Phase 4 Planning

### 14. Phase Gates: Plan → Review → Implement → Verify → Checkpoint
- **Label**: 🔒 LOCKED
- **Decision**: Every phase follows: Planning doc → User review → Implementation → Build/test verification → Git checkpoint.
- **Reason**: Prevents drift, ensures quality gates, enables rollback.
- **Current Implementation**: Phase 0-3 completed with checkpoints. Phase 4.1 audit = planning step.
- **Phase**: Project start

### 15. No Domain Decision Changes Without Approval
- **Label**: 🔒 LOCKED
- **Decision**: Decisions 1-10 above are locked. Changing them requires explicit user approval with rationale.
- **Reason**: Domain model stability. Downstream code (UI, repo, tests) depends on these invariants.
- **Current Implementation**: Enforced in agent instructions.
- **Phase**: Project start

---

## Open Decisions (Needing Resolution)

### 16. Transaction.pocketId Deprecation Timeline
- **Label**: 🔒 LOCKED
- **Decision**: Keep `Transaction.pocketId` / `pocketName` as `@Deprecated` through Phase 4.3, then remove.
- **Reason**: Legacy field needed for migration safety. UI still writes to it. Repository reads it to create ExpenseAllocation. Removing now would break existing flow.
- **Current State**: 
  - Domain model: `@Deprecated` annotations (Models.kt:85-88, Phase 4.1 verified)
  - Entity: `@Deprecated` annotations added to TransactionEntity (Entities.kt:201-205, Phase 4.1)
  - Repository: Reads `transaction.pocketId` to create ExpenseAllocation
- **Phase**: Phase 4.1 Implementation

### 17. ExpenseAllocation ID Generation
- **Label**: 🔒 LOCKED
- **Decision**: Use UUID for ExpenseAllocation IDs.
- **Reason**: 
  - One Transaction can have multiple ExpenseAllocations (multi-pocket Phase 4.3)
  - Using `ea_${transaction.id}` would cause collision when multiple allocations exist
  - UUID is collision-resistant, future-compatible, no schema change needed
- **Current State**: UUID implemented in SakuRepositoryImpl.kt line 280 (Phase 4.1)
- **Phase**: Phase 4.1 Implementation

### 18. Pocket Deletion Cascade
- **Label**: 🔒 LOCKED
- **Decision**: When deleting a Pocket, ExpenseAllocations are automatically deleted via `expenseAllocationDao.deleteAllocationsByPocket(pocketId)`.
- **Reason**: Prevents orphaned allocations that would corrupt PocketStats.realization for deleted pocket.
- **Current State**: 
  - DAO method exists: `deleteAllocationsByPocket(pocketId)` (Daos.kt:157)
  - Repository calls it: `SakuRepositoryImpl.deletePocket()` line 133 (verified Phase 4.1 audit)
  - No code change needed — cleanup already correct
- **Phase**: Phase 4.1 Audit (verified existing implementation)

### 19. DataStore for User Preferences
- **Label**: 🔮 FUTURE
- **Question**: Should user preferences (theme, currency, biometric) use DataStore instead of current approach?
- **Current State**: DataStore dependency commented out. Preferences likely in memory/User entity.
- **Phase**: Post-Phase 4

### 20. CameraX / OCR for Receipt Scanning
- **Label**: 🔮 FUTURE
- **Question**: Implement receipt OCR for transaction entry?
- **Current State**: CameraX dependencies commented out in build.gradle.kts. `Transaction.receiptImageUrl` field exists but unused.
- **Phase**: Post-Phase 4

### 21. Remote Sync / Backup
- **Label**: 🔮 FUTURE
- **Question**: Cloud backup, multi-device sync, or export-only?
- **Current State**: ExportBackupScreen exists. Firebase configured but Auth/Firestore commented out.
- **Phase**: Post-Phase 4

---

## Future Considerations (Not Yet Decided)

### 22. Recurring Transactions
- **Label**: 🔮 FUTURE
- **Scope**: Scheduled INCOME/EXPENSE (monthly salary, subscriptions)
- **Dependencies**: AlarmManager/WorkManager, new domain models

### 23. Budget vs Pocket Distinction
- **Label**: 🔮 FUTURE
- **Question**: Are Pockets sufficient for all budgeting needs, or need separate "Budget" concept (time-bound, category-based)?

### 24. Debt/Liability Tracking
- **Label**: 🔮 FUTURE
- **Scope**: Credit cards, loans, payables — currently only Assets (positive balance)

### 25. Multi-Currency
- **Label**: 🔮 FUTURE
- **Current State**: `Asset.currency` field exists (default IDR). No conversion logic.

### 26. Shared/Family Pockets
- **Label**: 🔮 FUTURE
- **Scope**: Collaborative pockets between users

---

## Decision History by Phase

| Phase | Decisions Locked |
|-------|------------------|
| Phase 0 | Project setup, tech stack, offline-first |
| Phase 1 | Asset/Pocket/Transaction core models, Asset balance mutations, Room schema v1 |
| Phase 2 | Auth screens, Navigation (5 tabs), Dashboard, Theme system |
| Phase 3 | Pocket domain alignment: removed Pocket balance, removed Pocket transfer, removed Saldo Awal, PocketStats from ExpenseAllocation, Migration v2→v3 |
| Phase 3.2 | KantongScreen refactor (425 lines removed), progress handling, progress > 100% display |
| Phase 4.1 | Transaction→ExpenseAllocation hardening: UUID allocation IDs, Transaction.pocketId deprecation (Entity+Domain), Pocket deletion cascade verified |

---

*Last updated: Phase 4.1 Implementation complete. Decisions 1-18 are LOCKED.*