# Reorganizing UI and Implementing Budgeting System

This plan outlines the steps to reorganize the Dashboard and Report screens and add a new Budgeting system as requested.

## User Review Required

> [!IMPORTANT]
> - **Budgeting Logic**: Budget limits will be per "Payment Method" per month. Limits can be set as a fixed amount (IDR) or a percentage of the *total balance* across all methods.
> - **Visibility Toggle**: The "Money Distribution" section on the Homepage will be tied to the same balance visibility toggle as the main balance.

## Proposed Changes

### Data Layer
Add `BudgetLimit` entity and DAO to track monthly limits.

#### [BudgetLimit.kt](file:///D:/finance-tracker/app/src/main/java/com/example/data/BudgetLimit.kt) [NEW]
- Define `BudgetLimit` entity: `id`, `methodName`, `limitAmount` (nullable), `limitPercentage` (nullable).

#### [BudgetLimitDao.kt](file:///D:/finance-tracker/app/src/main/java/com/example/data/BudgetLimitDao.kt) [NEW]
- Standard CRUD operations for `BudgetLimit`.

#### [AppDatabase.kt](file:///D:/finance-tracker/app/src/main/java/com/example/data/AppDatabase.kt)
- Add `BudgetLimitDao` to the database.

---

### ViewModel Layer
Update `BudgetViewModel` to handle budget limits and the reorganized chart data.

#### [BudgetViewModel.kt](file:///D:/finance-tracker/app/src/main/java/com/example/ui/BudgetViewModel.kt)
- Expose `budgetLimits` Flow.
- Add methods to set/update budget limits.
- (Optional) Add a helper for calculating "All Time" distribution if not already convenient.

---

### UI Layer

#### [DashboardScreen.kt](file:///D:/finance-tracker/app/src/main/java/com/example/ui/screens/DashboardScreen.kt)
- **Remove**: "Arus Kas Bulanan" section (but keep `DoubleBarChart` component code if needed for other screens).
- **Add**: "Money Distribution" section below "Rp. xxx dipakai bulan ini".
    - Uses `DonutChart` and a list of method balances.
    - Title: "Money Distribution".
    - Visibility: Hidden by default (using `isBalanceVisible` state).
    - Animation: Expandable dropdown when unhidden.
    - Range: All Time.

#### [ReportScreen.kt](file:///D:/finance-tracker/app/src/main/java/com/example/ui/screens/ReportScreen.kt)
- **Remove**: "Metode" tab from `ReportTab`.
- **Add**: "Budgeting" tab to `ReportTab`.
- **Add**: `DoubleBarChart` between "Selisih" and the breakdown categories (Pemasukan/Pengeluaran).
- **Implement**: "Budgeting" screen content:
    - List of payment methods with current month spending vs limit.
    - UI to set/edit limits (fixed amount or percentage).
    - Progress bars showing usage.

## Verification Plan

### Automated Tests
- N/A (Manual verification is preferred for UI reorganizations).

### Manual Verification
1. **Homepage**:
    - Verify "Arus Kas Bulanan" is gone.
    - Verify "Money Distribution" appears below the "monthly spend" badge.
    - Verify it is hidden when balance is hidden.
    - Verify it shows "All Time" data.
2. **Report Page**:
    - Verify `DoubleBarChart` appears below "Selisih".
    - Verify the third tab is now "Budgeting".
    - Verify "Metode" tab is gone.
3. **Budgeting**:
    - Set a limit for a method (e.g., "Cash").
    - Add a transaction using that method.
    - Verify progress bar and spent amount update correctly.
