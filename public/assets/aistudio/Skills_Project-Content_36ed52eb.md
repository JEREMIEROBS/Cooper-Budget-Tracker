# Budget Tracker — Project Skills & Content

> **Role:** Senior Mobile Developer
> **Product:** Personal budget tracker (iOS + Android)
> **Approach:** Local-first, fast, calm, and honest with numbers
> **Design stance:** Modern, clean, human. No "generated" look.

---

## 1. Product Summary

A mobile app that helps one person answer three questions in under five seconds:

1. **How much have I spent this month?**
2. **How much is left in each category?**
3. **Am I on track?**

It is not a finance super-app. It does fewer things, and does them with precision.

### Core principles

- **Logging an expense takes under 5 seconds** (amount → category → done).
- **Works fully offline.** The network is optional, never required.
- **Numbers are always exact.** No float math on money.
- **The user owns their data.** Export anytime, delete anytime.
- **Quiet UI.** The data is the hero, not the chrome.

### Target users

| Persona | Need |
|---|---|
| Young professional | Stop wondering where the salary went |
| Freelancer | Irregular income, per-month planning |
| Student | Tight allowance, simple category limits |

---

## 2. Skills Required

### Engineering skills

| Area | Skill | Level |
|---|---|---|
| Mobile | React Native + Expo, TypeScript (strict) | Advanced |
| Navigation | Expo Router (file-based) | Intermediate |
| Local data | SQLite, migrations, indexed queries | Advanced |
| State | Zustand for UI state, TanStack Query for DB reads | Intermediate |
| Money handling | Integer minor units, currency formatting (`Intl`) | Advanced |
| Charts | Victory Native / react-native-skia | Intermediate |
| Animation | Reanimated 3, Gesture Handler | Intermediate |
| Notifications | Local scheduled notifications | Intermediate |
| Security | Biometric lock, secure storage | Intermediate |
| Testing | Jest, React Native Testing Library, Maestro (E2E) | Intermediate |
| Delivery | EAS Build, EAS Submit, OTA updates | Intermediate |

### Product & design skills

- Information hierarchy for numeric dashboards
- Typography with tabular figures
- Accessibility (WCAG AA contrast, dynamic type, screen readers)
- Empty-state and error-state writing
- Microcopy that sounds like a person, not a brand

---

## 3. Tech Stack

| Layer | Choice | Why |
|---|---|---|
| Framework | **Expo (React Native) + TypeScript** | One codebase, fast iteration, solid native APIs |
| Navigation | Expo Router | Typed routes, deep links for free |
| Database | `expo-sqlite` + Drizzle ORM | Typed schema, real migrations, no server |
| State | Zustand + TanStack Query | Small, predictable, no boilerplate |
| Forms | React Hook Form + Zod | Validation shared with types |
| Charts | Victory Native XL (Skia) | Smooth, customizable, native feel |
| Dates | date-fns | Tree-shakable, immutable |
| Lint/format | ESLint + Prettier + `tsc --noEmit` | Enforced in CI |
| CI/CD | GitHub Actions + EAS | Automated builds and tests |

**Optional later:** encrypted cloud sync (Supabase or own backend). Do **not** build it in v1.

---

## 4. Architecture

Feature-first structure with a thin shared layer.

```
budget-tracker/
├── app/                      # Expo Router screens
│   ├── (tabs)/
│   │   ├── index.tsx         # Overview
│   │   ├── transactions.tsx
│   │   ├── budgets.tsx
│   │   └── insights.tsx
│   ├── add-transaction.tsx   # Modal
│   └── settings/
├── src/
│   ├── features/
│   │   ├── transactions/     # components, hooks, queries, schema
│   │   ├── budgets/
│   │   ├── categories/
│   │   └── insights/
│   ├── db/
│   │   ├── schema.ts
│   │   ├── migrations/
│   │   └── client.ts
│   ├── lib/
│   │   ├── money.ts          # integer-cents helpers
│   │   ├── dates.ts
│   │   └── format.ts
│   ├── ui/                   # design-system primitives
│   │   ├── Text.tsx
│   │   ├── Button.tsx
│   │   ├── Amount.tsx
│   │   └── tokens.ts
│   └── store/
├── assets/fonts/
└── e2e/                      # Maestro flows
```

### Layering rules

- Screens compose features. Features never import from other features.
- `ui/` knows nothing about the domain.
- All DB access goes through `features/*/queries.ts`.
- No business logic inside components.

---

## 5. Data Model

All money is stored as **integer minor units** (cents). Never floats.

```ts
// schema.ts (Drizzle, simplified)

accounts       { id, name, type, currency, openingBalance, archivedAt }
categories     { id, name, kind: 'expense' | 'income', color, icon, sortOrder, archivedAt }
transactions   { id, accountId, categoryId, amount, kind, note, occurredAt, createdAt, updatedAt }
budgets        { id, categoryId, period: 'month', amount, startsOn }
recurring      { id, templateJson, rule, nextRunAt, active }
settings       { key, value }
```

### Indexes

- `transactions(occurredAt DESC)`
- `transactions(categoryId, occurredAt)`
- `budgets(categoryId, startsOn)`

### Money helper (required behavior)

```ts
toMinor("12.50", "USD")   // 1250
formatMoney(1250, "USD")  // "$12.50" (via Intl.NumberFormat)
```

Rules: parse strings, not floats. Round only at display. Store the currency code alongside the amount.

---

## 6. Design System

### 6.1 Direction

Think **paper ledger meets modern banking app**: warm neutrals, strong numerals, generous whitespace, one confident accent. It should feel designed by a person with opinions.

### 6.2 What to avoid (the "AI look")

| Avoid | Do instead |
|---|---|
| Purple/blue neon gradients | Flat surfaces, one accent color |
| Glassmorphism and heavy blur | Solid cards with a hairline border |
| Emoji as icons | One consistent line icon set (Lucide, 1.75 stroke) |
| Giant drop shadows | 1px borders or a very subtle single shadow |
| Rounded everything (24px+) | A restrained radius scale: 8 / 12 / 16 |
| Centered hero text with sparkles | Left-aligned, information-dense layouts |
| Generic copy ("Unlock your financial potential!") | Plain language ("You have $214 left for groceries") |
| Random decorative illustrations | Real data, real empty states |
| Rainbow category colors | A muted, curated 8-color palette |

### 6.3 Color tokens

**Light**

| Token | Value | Use |
|---|---|---|
| `bg` | `#F7F5F0` | App background (warm off-white) |
| `surface` | `#FFFFFF` | Cards |
| `border` | `#E7E3DA` | Hairlines |
| `text` | `#1C1B18` | Primary text |
| `textMuted` | `#6F6B62` | Secondary text |
| `accent` | `#1F5E4B` | Primary actions (deep green) |
| `accentSoft` | `#E3EEE9` | Accent backgrounds |
| `expense` | `#B4472F` | Negative amounts (brick) |
| `income` | `#2F7D5B` | Positive amounts |
| `warning` | `#B8832A` | Nearing limit |

**Dark**

| Token | Value |
|---|---|
| `bg` | `#121211` |
| `surface` | `#1B1B19` |
| `border` | `#2A2926` |
| `text` | `#F2F0EA` |
| `textMuted` | `#9A968B` |
| `accent` | `#5FB596` |

Minimum contrast: **4.5:1** for body text, **3:1** for large text and icons.

### 6.4 Typography

| Role | Font | Notes |
|---|---|---|
| Numbers / amounts | **Geist Mono** or **IBM Plex Mono** (tabular) | Digits align in lists |
| Headings | **Fraunces** or **Instrument Serif** (sparingly) | Adds character to totals |
| UI text | **Inter** or **Geist Sans** | Neutral, readable |

| Style | Size / Line | Weight |
|---|---|---|
| Display (balance) | 40 / 44 | 500 |
| Title | 22 / 28 | 600 |
| Body | 16 / 24 | 400 |
| Caption | 13 / 18 | 400 |

Support OS dynamic type up to 130% without clipping.

### 6.5 Spacing, radius, elevation

- Spacing scale: `4, 8, 12, 16, 24, 32, 48`
- Screen padding: `20`
- Radius: `8` inputs/chips, `12` cards, `16` sheets
- Elevation: none by default; one subtle shadow for floating action elements only

### 6.6 Components

- **Amount** — mono font, sign-aware color, tabular alignment
- **CategoryChip** — small dot + label, no filled rainbow pills
- **TransactionRow** — 56px height, category dot, note, amount right-aligned, date as group header
- **BudgetBar** — thin 6px track, accent fill, switches to warning at 80%, expense color at 100%
- **NumberPad** — custom large keypad for fast entry
- **Sheet** — bottom sheet for add/edit with drag handle
- **EmptyState** — one sentence, one action, no illustration

### 6.7 Motion

- 150–220ms ease-out for state changes
- Spring only for sheet drag and number tick-up
- Respect "Reduce Motion"
- No looping or decorative animation

---

## 7. Screens & Content

### 7.1 Overview

- Month selector (left/right chevrons)
- Large "Spent this month" figure with "of $X budgeted"
- Remaining-per-day hint
- Top 3 categories with BudgetBars
- Last 5 transactions

### 7.2 Add Transaction (modal)

1. Amount via NumberPad (autofocus)
2. Category row (recent first)
3. Optional note and date (defaults: today)
4. Primary button: **Save**

Target: **3 taps** for a repeat expense.

### 7.3 Transactions

- Grouped by day, sticky headers
- Search and filter (category, account, date range)
- Swipe left to delete (with undo), swipe right to edit

### 7.4 Budgets

- Per-category monthly limits
- "Copy last month" action
- Rollover is optional and off by default

### 7.5 Insights

- Month-over-month bar chart
- Category breakdown (horizontal bars, not pie)
- Biggest changes vs last month
- Plain-language sentence summaries

### 7.6 Settings

- Currency and first day of month
- Categories management
- Biometric lock
- Reminders
- Export CSV / JSON
- Delete all data

### Sample microcopy

| Moment | Copy |
|---|---|
| Empty transactions | "Nothing logged yet. Add your first expense." |
| Near limit | "Groceries: $38 left, 9 days to go." |
| Over limit | "Dining is $22 over. It happens." |
| Delete | "Deleted. Undo" |
| Export done | "Saved to Files." |

---

## 8. Phase-by-Phase Plan

Estimated for **one senior developer**. Adjust to your team.

---

### Phase 0 — Discovery & Foundations (Week 1)

**Goals:** Lock scope, avoid rework.

**Tasks**
- Write a one-page PRD: problem, users, v1 scope, non-goals
- Define the success metric (e.g., median time-to-log < 5s)
- Collect 5–8 reference apps; note what to borrow and what to avoid
- Sketch the 4 core flows on paper
- Decide currency, locale, and platform targets

**Deliverables:** PRD, flow sketches, backlog in a tracker

**Exit criteria:** v1 scope fits in the remaining phases with buffer

---

### Phase 1 — Design System & Prototype (Weeks 1–2)

**Goals:** A coherent look before any feature code.

**Tasks**
- Build tokens (color, type, spacing, radius) in Figma and in `tokens.ts`
- Design the 6 key screens in light and dark
- Design all empty, loading, and error states
- Build a clickable prototype for the Add Transaction flow
- Test it with 3–5 people; time how long logging takes

**Deliverables:** Figma file, tokens file, tested prototype

**Exit criteria:** Add-flow testers log an expense in under 5 seconds

---

### Phase 2 — Project Setup & Core Infrastructure (Week 3)

**Goals:** A clean base that scales.

**Tasks**
- Initialize Expo + TypeScript (strict), Expo Router
- Configure ESLint, Prettier, path aliases, Husky pre-commit
- Set up GitHub Actions: lint → typecheck → test
- Set up SQLite + Drizzle with the first migration
- Implement `money.ts` with unit tests (edge cases first)
- Load fonts, build `Text`, `Button`, `Amount`, theme provider (light/dark)
- Set up error boundary and crash reporting (Sentry)

**Deliverables:** Running app shell, CI green, design primitives

**Exit criteria:** Fresh clone → `npm i && npx expo start` works; CI passes

---

### Phase 3 — Core Features (Weeks 4–6)

**Goals:** A usable daily-driver.

**Week 4 — Transactions**
- Categories CRUD with seeded defaults
- Add / edit / delete transaction with NumberPad
- Transactions list (grouped, sticky headers, virtualized)

**Week 5 — Budgets & Overview**
- Monthly budget per category
- BudgetBar and remaining calculations
- Overview screen with live queries

**Week 6 — Polish the core**
- Search and filters
- Undo on delete
- Haptics on save, number entry
- Keyboard and safe-area edge cases

**Deliverables:** Feature-complete core loop

**Exit criteria:** You use it yourself for a full week without frustration

---

### Phase 4 — Insights, Recurring & Quality-of-Life (Weeks 7–8)

**Tasks**
- Insights charts and sentence summaries
- Recurring transactions (rent, subscriptions) with a scheduler on app open
- Local notifications: daily log reminder, budget threshold alerts
- CSV and JSON export; JSON import for restore
- Biometric lock with secure storage

**Deliverables:** Retention and trust features

**Exit criteria:** Recurring items generate correctly across month boundaries and DST changes

---

### Phase 5 — Accessibility, Performance, Hardening (Week 9)

**Tasks**
- Screen reader pass (VoiceOver and TalkBack) on all flows
- Dynamic type at 130%, contrast checks in both themes
- Profile list performance with 10,000 transactions
- Cold start target: **< 1.5s** on a mid-range Android
- Add DB indexes where profiling shows slow queries
- Handle edge cases: timezone change, leap days, negative balances, huge amounts
- Add migration tests (upgrade from every prior schema version)

**Deliverables:** Accessibility report, performance report

**Exit criteria:** No critical a11y issues; scrolling stays at 60fps with seeded large data

---

### Phase 6 — Testing & Beta (Week 10)

**Tasks**
- Unit tests: money, dates, budget math (aim ≥ 90% on `lib/`)
- Component tests for key UI
- Maestro E2E: onboarding, add expense, edit, delete, budget limit, export
- TestFlight + Play Internal Testing with 10–20 testers
- Collect feedback with an in-app "Send feedback" link
- Triage: fix crashes and data bugs first, polish second

**Deliverables:** Beta build, bug list, test report

**Exit criteria:** Crash-free sessions > 99.5%; zero data-loss bugs open

---

### Phase 7 — Release (Week 11)

**Tasks**
- App icon, splash, store screenshots (real data, no mock-up gimmicks)
- Store listing copy written in plain language
- Privacy policy and data-safety form (accurate: data stays on device)
- Production builds through EAS; staged rollout (10% → 50% → 100%)
- Set up OTA update channel for JS-only fixes

**Deliverables:** Live app, monitoring dashboards

**Exit criteria:** Approved on both stores; monitoring shows healthy first 48 hours

---

### Phase 8 — Post-Launch (Ongoing)

- Weekly review of crashes and feedback
- Ship small improvements every 2 weeks
- Candidate v1.x features: widgets, shared budgets, receipt photos, encrypted sync, bank import
- Re-measure time-to-log and 30-day retention

---

## 9. Timeline Overview

| Phase | Focus | Duration |
|---|---|---|
| 0 | Discovery | 1 wk |
| 1 | Design system & prototype | 1–2 wks |
| 2 | Setup & infrastructure | 1 wk |
| 3 | Core features | 3 wks |
| 4 | Insights & extras | 2 wks |
| 5 | A11y, performance, hardening | 1 wk |
| 6 | Testing & beta | 1 wk |
| 7 | Release | 1 wk |
| **Total** | | **≈ 11 weeks** |

---

## 10. Engineering Standards

### Code

- TypeScript `strict: true`, no `any` without a comment
- Small components, logic in hooks
- Conventional Commits (`feat:`, `fix:`, `chore:`)
- PRs under ~400 lines, always reviewed (or self-reviewed with a checklist if solo)

### Money & dates (non-negotiable)

- Integer minor units only
- Store timestamps in UTC, display in local time
- Month boundaries computed in the user's timezone
- Every money function has tests for: zero, negative, rounding, large values, non-2-decimal currencies (JPY, KWD)

### Security & privacy

- No analytics that capture amounts, notes, or categories
- Biometric lock uses OS APIs; fall back to device passcode
- Exports are user-initiated only
- Clear "Delete all data" with confirmation

### Performance budget

| Metric | Target |
|---|---|
| Cold start | < 1.5s |
| Add-transaction open | < 150ms |
| List scroll | 60fps with 10k rows |
| JS bundle | Keep lean; audit every new dependency |

---

## 11. Definition of Done (per feature)

- [ ] Works offline
- [ ] Light and dark themes verified
- [ ] Empty, loading, and error states designed
- [ ] Accessible labels and focus order
- [ ] Unit tests for logic, E2E for the happy path
- [ ] No new lint or type errors
- [ ] Tested on a small phone and a large phone
- [ ] Tested on one low-end Android device
- [ ] Copy reviewed for tone

---

## 12. Risk Register

| Risk | Impact | Mitigation |
|---|---|---|
| Scope creep (bank sync, AI insights) | High | Hard non-goals list in the PRD |
| Money rounding bugs | High | Integer math + exhaustive tests |
| Data loss on migration | High | Migration tests, automatic pre-migration backup |
| Recurring item duplicates | Medium | Idempotency key per scheduled run |
| Store rejection | Medium | Accurate privacy forms, early TestFlight |
| Design drifting to "generic" | Medium | Token discipline, the avoid-list in 6.2 |

---

## 13. Quick-Start Checklist

```bash
npx create-expo-app@latest budget-tracker --template tabs
cd budget-tracker
npx expo install expo-sqlite expo-font expo-haptics expo-local-authentication expo-notifications
npm i drizzle-orm zustand @tanstack/react-query react-hook-form zod date-fns
npm i -D drizzle-kit typescript eslint prettier jest
```

1. Create `tokens.ts` from section 6
2. Write and test `money.ts` before touching UI
3. Define schema and run the first migration
4. Build `Amount` and `TransactionRow`
5. Ship the Add Transaction flow end-to-end, then iterate

---

## 14. Non-Goals for v1

- Bank account linking
- Multi-user or shared budgets
- Investment or crypto tracking
- AI-generated advice
- Gamification, streaks, or badges

Keeping these out is what keeps the product calm and the build on schedule.
