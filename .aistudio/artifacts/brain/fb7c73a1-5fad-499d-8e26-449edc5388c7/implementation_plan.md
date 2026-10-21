# Responsive Accounts, Functional Transfers & Account Logs Plan

## Overview
This plan addresses the layout clipping and responsiveness issues shown in the user screenshot, implements a fully functional Transfer experience with account selection (such as Cash to GCash), and adds comprehensive Account Logs so wallet balances and history accurately reflect real-world accounts.

---

## User Review Required

> [!IMPORTANT]
> The Transfer feature updates both sender and recipient account balances without counting as spending/expenses in budget totals, keeping your net worth accurate.
> Account Logs will be accessible both by tapping on any account card and in a "Recent Transfers" log on the Accounts screen.

---

## Proposed Changes

### 1. Screen Responsiveness & Layout Fixes (`AccountsScreen.kt`)
- **Fix Button & Text Clipping**: Replace the cramped `28.dp` height constraint on the `Reconcile` action with flexible, unclipped touch targets adhering to Material 3 48dp accessibility standards.
- **Adaptive Screen Sizing**: Apply `Modifier.widthIn(max = 600.dp)` with horizontal centering for foldables and tablets, preventing awkward stretching.
- **Scroll Padding**: Add `PaddingValues(bottom = 96.dp)` to the `LazyColumn` so the floating action button and bottom navigation bar never cover the bottom-most account card or actions.
- **Card Hierarchy**: Refine Account cards with consistent badge styling for account types (Cash, GCash, Maya, Bank) and clear amount hierarchy.

### 2. Functional & Intuitive Transfer Workflow (`TransferSheet.kt` & `AccountsScreen.kt`)
- **From / To Account Pickers**: Replace hardcoded defaults with visual account selectors showing each account's current balance and icon.
- **Quick Shortcuts**: 1-tap quick buttons for frequent transfers (e.g., "Cash → GCash", "Bank → Maya", "GCash → Bank").
- **Swap Button**: Allow reversing sender and recipient in one tap.
- **Live Balance Preview**: Dynamically show what both accounts' balances will become before confirming (e.g., "Cash: ₱1,750 → ₱1,250", "GCash: ₱1,286 → ₱1,786").
- **Validation**: Ensure non-negative amounts, prevent transferring to the same account, and warn if transferring more than available balance.
- **Atomic Execution**: Deduct from sender, credit recipient, and store the transfer record with timestamp and note.

### 3. Account Activity & Transfer Logs (`AccountDetailsSheet.kt` & `AccountsScreen.kt`)
- **Tap to Inspect Account**: Tapping any account card opens an `AccountDetailsSheet` showing:
  - Account verified balance, type, and total net-worth inclusion toggle.
  - Complete history log of that account's activity: incoming transfers, outgoing transfers, expenses, income, and reconciliation adjustments.
- **Recent Transfers Section**: On the Accounts screen, include an expandable "Recent Transfers & Activity" card listing past transfers with from/to badges, dates, and amounts.
- **Data Layer Enhancements**:
  - Add query in `TransferDao` and `BudgetRepository` for account-specific transfers and combined chronological account logs.
  - Expose `allTransfers` and `getAccountActivity(accountId)` flows in `MainViewModel`.

---

## Verification Plan

### Automated Tests
- Run `gradle :app:testDebugUnitTest` to verify repository and transfer math logic.
- Run `compile_applet` to ensure error-free compilation.

### Manual Verification
1. Open **Accounts** tab on small screen and standard emulator: verify all text ("Reconcile", balances, account types) is crisp and unclipped.
2. Tap **Transfer**: select "Cash (Wallet)" as From and "GCash" as To, enter `₱500`, confirm transfer.
3. Verify Cash decreases by ₱500 and GCash increases by ₱500; verify Net Worth remains unchanged.
4. Tap **GCash**: verify the transfer log shows `+₱500.00 from Cash (Wallet)`.
5. Verify bottom navigation and FAB do not overlap the last account card.
