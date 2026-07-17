# Transfer Tax, Icon Fixes, and Bidirectional Savings Sync

This plan introduces support for optional taxes on transfer transactions, fixes missing UI elements in the history page, and ensures that deleting savings-related transactions correctly updates the corresponding savings goal balance.

## User Review Required

> [!IMPORTANT]
> - **Database Migration**: Adding `taxAmount` to the `Transaction` entity will require a database migration (version 6 to 7).
> - **Savings Deletion**: Deleting a transaction with the category "Tabungan" will now automatically update the `currentAmount` and `sourceBalances` of the linked `SavingsGoal`. This assumes the transaction note exactly matches the `SavingsGoal` name (or we find another reliable link).

## Proposed Changes

### Data Layer

#### [Transaction.kt](file:///D:/finance-tracker/app/src/main/java/com/example/data/Transaction.kt)
- Add `taxAmount: Double = 0.0` field to the `Transaction` entity.

#### [AppDatabase.kt](file:///D:/finance-tracker/app/src/main/java/com/example/data/AppDatabase.kt)
- Increment database version to 7.

---

### ViewModel Layer

#### [BudgetViewModel.kt](file:///D:/finance-tracker/app/src/main/java/com/example/ui/BudgetViewModel.kt)
- **`addTransaction`**: Update signature to accept `taxAmount`.
- **`deleteTransaction`**: Implement bidirectional sync for savings:
    - If the transaction is of type `PENGELUARAN` and category is "Tabungan":
        - Extract goal name from notes or lookup.
        - Subtract the amount from the goal's `currentAmount` and its source (the transaction's `paymentMethod`).
    - If the transaction is of type `PEMASUKAN` and category is "Tabungan":
        - Add the amount back to the goal's `currentAmount` and its source.

---

### UI Layer

#### [DashboardScreen.kt](file:///D:/finance-tracker/app/src/main/java/com/example/ui/screens/DashboardScreen.kt)
- **`AddEditTransactionDialog`**:
    - Add a "Pajak / Biaya Admin" toggle or optional input field specifically for the `TRANSFER` type.
    - If tax is provided, include it in the `addTransaction` call.
    - For transfers, ensure the source balance is reduced by `amount + taxAmount`.

#### [HistoryScreen.kt](file:///D:/finance-tracker/app/src/main/java/com/example/ui/screens/HistoryScreen.kt)
- Fix missing icon for `TransactionType.TRANSFER` in the transaction list items.
- Update transaction item display to show `- Rp {taxAmount}` in white below the main amount if `taxAmount > 0`.

---

## Verification Plan

### Automated Tests
- N/A

### Manual Verification
1. **Transfer with Tax**:
    - Perform a transfer of 20k with 1k tax.
    - Verify history shows 20k (with teal icon) and "- 1.000" in white.
    - Verify source balance is reduced by 21k.
2. **Transfer Icon**:
    - Open the History page.
    - Verify transfer transactions now show a relevant icon (e.g., `SyncAlt` or `SwapHoriz`).
3. **Savings Deletion Sync**:
    - Add 50k to a "Beli Parfum" goal.
    - Go to history and delete that 50k "Alokasi ke tabungan" transaction.
    - Verify the "Beli Parfum" goal balance returns to its previous state.
    - Do the same for a "Menarik dana" (withdrawal) deletion.
