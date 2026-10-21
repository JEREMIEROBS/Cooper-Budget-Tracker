# Budget Tracker — Priority Features (P1 Only)

> Paste-ready section for the app's **Important Features**.
> Scope: P1 features only. Everything else (P2/P3) is parked in `Additional Features.md`.
> Principles: offline-first, integer money math, calm tone, no shame or pressure.

---

## Feature List at a Glance

| # | Feature | Release | Effort |
|---|---|---|---|
| 1 | Pay-cycle budgets | v1.1 | M |
| 2 | Safe-to-spend today | v1.1 | S |
| 3 | Favorites & templates | v1.1 | S |
| 4 | Search & saved filters | v1.1 | S |
| 5 | Hide balances & privacy screen | v1.1 | S |
| 6 | Filipino localization | v1.1 | S |
| 7 | Multiple accounts, e-wallets & transfers | v1.2 | M |
| 8 | Bills & subscriptions tracker | v1.2 | M |
| 9 | Savings goals | v1.2 | M |
| 10 | Utang tracker | v1.2 | M |
| 11 | Encrypted backup & restore | v1.3 | M |
| 12 | CSV import | v1.3 | M |
| 13 | Home screen widgets | v1.3 | M |
| 14 | Quick-add from notification / lock screen | v1.3 | M |

**Build order matters:** Pay-cycle budgets come first because Safe-to-spend, Bills, and Goals all depend on the cycle definition.

---

## 1. Pay-Cycle Budgets

**Problem:** Many users are paid on the 15th and 30th (kinsenas), weekly, or on custom dates. A calendar month doesn't match their real budget.

**What it does**
- User picks a cycle: **Monthly**, **Semi-monthly** (1st to 15th, 16th to end), **Weekly**, or **Custom** (any start day).
- Every "this month" figure becomes "this cycle".
- Budgets, Overview, Insights, and Safe-to-spend all follow the selected cycle.

**Rules**
- Cycle boundaries are computed in the user's timezone.
- Changing the cycle never deletes data. Past transactions are re-grouped.
- Handle short months and leap days.

**Data**
```ts
settings { cycleType: 'monthly'|'semimonthly'|'weekly'|'custom', cycleStartDay: number }
budgets  { ..., period: 'monthly'|'semimonthly'|'weekly'|'custom' }
```

**Acceptance criteria**
- [ ] Switching cycle type updates all screens without restart
- [ ] A transaction on the boundary day lands in the correct cycle
- [ ] Unit tests cover Feb 28/29, 31-day months, DST, and timezone change

---

## 2. Safe-to-Spend Today

**Problem:** Users want one number: "How much can I spend today?"

**What it does**
- Shows a single figure at the top of Overview.
- Formula: `(budget remaining − upcoming bills in this cycle) ÷ days left in cycle`
- Updates instantly after every transaction.

**Copy**
- Normal: *"You can spend ₱420 today."*
- Over: *"You're ₱350 over for this cycle."* (never a negative number, no scolding)

**Acceptance criteria**
- [ ] Excludes bills already paid, includes bills due before the cycle ends
- [ ] Last day of the cycle divides by 1, never by 0
- [ ] Respects Hide balances
- [ ] No mascot shown in the over-budget state

---

## 3. Favorites & Templates

**Problem:** People log the same few expenses daily (fare, coffee, lunch).

**What it does**
- Save any transaction as a favorite (amount optional, category, account, note).
- Top 4 favorites appear as one-tap chips on the Add screen.
- After typing a note, suggest category and the last amount used.

**Data**
```ts
favorites { id, amount?, note, categoryId, accountId?, sortOrder }
```

**Acceptance criteria**
- [ ] Repeat expense logged in **3 taps or fewer**
- [ ] Favorites can be reordered and deleted
- [ ] Suggestions work offline from local history

---

## 4. Search & Saved Filters

**What it does**
- Full-text search across notes.
- Filter by category, account, date range, and amount range.
- Save filter combinations (e.g., "Food, last 30 days, Cash").

**Acceptance criteria**
- [ ] Results appear in under 200ms with 10,000 transactions
- [ ] Saved filters persist across launches
- [ ] Empty result state has one sentence and a "Clear filters" action

---

## 5. Hide Balances & Privacy Screen

**What it does**
- Eye icon masks all amounts as "••••".
- App preview is blurred in the app switcher.
- Optional: block screenshots on sensitive screens (Android `FLAG_SECURE`).
- Remembers the user's choice between launches.

**Acceptance criteria**
- [ ] Masking applies everywhere: Overview, lists, widgets, notifications
- [ ] Biometric/device passcode required to unhide if app lock is enabled
- [ ] Verified on both iOS and Android

---

## 6. Filipino Localization

**What it does**
- Full Tagalog (`fil`) translation alongside English.
- Peso (₱) as the default currency with correct number formatting.
- Language switch in Settings, defaulting to device language.

**Rules**
- All strings live in translation files, with no hardcoded text.
- Plan for text expansion (Tagalog is often longer than English).
- Money and dates formatted with `Intl`.

**Acceptance criteria**
- [ ] Zero untranslated strings in `fil`
- [ ] No clipped text at 130% dynamic type in Tagalog
- [ ] Preset category names are localized

---

## 7. Multiple Accounts, E-Wallets & Transfers

**What it does**
- Account types: **Cash, Bank, E-wallet (GCash, Maya), Credit card, Savings**.
- Each transaction belongs to one account.
- **Transfers** between accounts are not expenses (no double counting).
- Per-account filter in Transactions.
- **Reconcile:** user enters the real balance, app offers an adjustment entry.

**Data**
```ts
accounts  { ..., type, includeInNetWorth, creditLimit?, dueDay? }
transfers { id, fromAccountId, toAccountId, amount, occurredAt, note }
```

**Acceptance criteria**
- [ ] Balance = opening balance + income − expenses ± transfers
- [ ] Transfers never appear in spending totals or budgets
- [ ] Archiving an account keeps its history
- [ ] Migration from the single-account schema keeps all existing data

---

## 8. Bills & Subscriptions Tracker

**What it does**
- Add bills: name, amount, due day, repeat rule, category, account.
- Reminders: **3 days before** and **on the due date**.
- **Mark as paid** creates the transaction automatically.
- Subscriptions view shows total monthly cost and next charge date.
- Suggest possible subscriptions from repeated merchant + amount patterns (suggestion only, never auto-created).

**Data**
```ts
bills { id, name, amount, dueDay, rule, categoryId, accountId, active }
```

**Acceptance criteria**
- [ ] Marking paid twice never creates duplicate transactions (idempotency key)
- [ ] Bills due on the 31st fall back to the last day of shorter months
- [ ] Unpaid upcoming bills feed into Safe-to-spend
- [ ] Reminders can be turned off per bill

---

## 9. Savings Goals

**What it does**
- Create a goal: name, target amount, optional target date, optional linked account.
- Progress bar and projected completion date.
- Add money manually or through a recurring rule.
- Cooper (`run` pose) appears at **25 / 50 / 75 / 100%** milestones, once each.

**Data**
```ts
goals       { id, name, targetAmount, targetDate?, linkedAccountId?, createdAt }
goalEntries { id, goalId, amount, occurredAt }
```

**Rules**
- No streaks, no badges, no pressure copy.
- Goal money is tracked separately from spending budgets.

**Acceptance criteria**
- [ ] Projected date updates after each contribution
- [ ] Withdrawing from a goal is supported and logged
- [ ] Milestone celebration shows once per milestone and respects "Show Cooper"

---

## 10. Utang Tracker (Lend / Borrow)

**What it does**
- Record **who owes you** and **who you owe**.
- Partial payments, notes, optional due date.
- Kept separate from budget totals unless the user converts an entry to an expense.
- Optional reminder to the user only. The app never messages the other person.

**Data**
```ts
debts { id, kind: 'owed_to_me'|'owed_by_me', counterparty, balance, dueDate?, note }
debtPayments { id, debtId, amount, occurredAt }
```

**Copy**
- *"Mark's balance: ₱1,200."* Neutral and respectful. No shaming language.

**Acceptance criteria**
- [ ] Partial payments reduce the balance exactly (integer math)
- [ ] Settled entries move to a "Settled" list, not deleted
- [ ] No mascot shown on this screen

---

## 11. Encrypted Backup & Restore

**Problem:** Local-first apps lose all data when a phone is lost or broken. **This is the top trust feature.**

**What it does**
- Export an **encrypted file** (AES-256) protected by a user-chosen password.
- Save via the share sheet to Files, iCloud Drive, or Google Drive.
- **Restore** validates the file and shows a summary first (e.g., "1,284 transactions, 8 accounts") before applying.
- Reminder to back up every 2 weeks (can be disabled).

**Rules**
- Password is never stored or recoverable. Warn the user clearly.
- Include `schemaVersion` in the file; restore must migrate older backups.
- Automatic local backup runs before every database migration.

**Acceptance criteria**
- [ ] Backup → wipe → restore returns identical data (verified by test)
- [ ] Wrong password shows a clear error and changes nothing
- [ ] Corrupted file is rejected safely
- [ ] Works fully offline

---

## 12. CSV Import

**What it does**
- Pick a CSV, then a **column mapping** step (date, amount, description, account).
- Preview the first 20 rows before importing.
- **Duplicate detection** (same date + amount + description).
- Saved mapping presets for GCash, Maya, and common banks.
- Auto-categorize from keywords (e.g., "JOLLIBEE" → Food), always editable.

**Acceptance criteria**
- [ ] Handles different date formats, thousand separators, and negative-amount conventions
- [ ] Amounts parsed as strings into integer minor units, never floats
- [ ] Import is atomic: all rows or none
- [ ] Undo last import is available
- [ ] Malformed rows are listed with reasons, not silently dropped

---

## 13. Home Screen Widgets

**What it does**

| Widget | Size | Content |
|---|---|---|
| Safe-to-spend | Small | Today's number |
| Quick add | Small | "+ Add expense" opens the keypad |
| Categories | Medium | Top 3 categories with budget bars |

**Rules**
- Read-only data from the local DB through an app group (iOS) / shared storage (Android).
- Respect Hide balances (shows "••••").
- Match the design tokens: warm background, green accent, mono numerals.
- No mascot in widgets.

**Acceptance criteria**
- [ ] Widget updates within a minute of a new transaction
- [ ] Tapping "+ Add" opens directly to the keypad
- [ ] Light and dark mode both verified

---

## 14. Quick-Add from Notification / Lock Screen

**Goal:** From anywhere to a saved expense in **under 3 seconds**.

**What it does**
- Optional persistent notification with an "Add expense" action.
- Android Quick Settings tile.
- iOS Lock Screen / Control Center control (iOS 18+).

**Acceptance criteria**
- [ ] Opens the keypad with the amount field focused
- [ ] Works when the app is closed
- [ ] Can be disabled in Settings
- [ ] Respects app lock

---

## Release Plan

| Release | Theme | Features | Est. time |
|---|---|---|---|
| **v1.1** | Faster | 1, 2, 3, 4, 5, 6 | 3 to 4 weeks |
| **v1.2** | Fits my life | 7, 8, 9, 10 | 4 to 6 weeks |
| **v1.3** | Never lose my data | 11, 12, 13, 14 | 3 to 4 weeks |

---

## Shared Rules for Every Feature Above

- [ ] Works **offline**
- [ ] Money is **integer minor units**, never floats
- [ ] Light and dark themes verified
- [ ] Empty, loading, and error states designed
- [ ] Accessible labels, focus order, dynamic type up to 130%
- [ ] Unit tests for logic, E2E test for the happy path
- [ ] Database migration tested with an automatic pre-migration backup
- [ ] Analytics (if any) never include amounts, notes, or categories
- [ ] Mascot rules followed (`Cooper-Mascot-Guide.md`): never beside numbers, never in error or over-budget states

---

## Parked for Later (Not in This Scope)

Envelope rollover, irregular-income planner, receipt OCR, voice entry, Siri Shortcuts, monthly recap, spending forecast, multi-currency, PDF reports, debt payoff planner, net worth, cloud sync, smartwatch, shared budgets, and bank auto-import. All are detailed in `Additional Features.md`.
