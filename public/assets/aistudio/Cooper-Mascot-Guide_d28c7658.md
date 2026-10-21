# Cooper — Mascot & Brand Icon Guide

> **Project:** Budget Tracker (iOS + Android)
> **Character:** Cooper, a husky puppy
> **Role of the mascot:** The only playful element in an otherwise calm, clean UI
> **Companion file:** `Skills Project-Content.md`

---

## 1. Character Identity

| Trait | Description |
|---|---|
| Species | Husky puppy, grey and cream fur, dark blue eyes |
| Personality | Cheerful, loyal, a little clumsy. Guards your money like a good dog |
| Signature props | Green coin pouch with paw print, gold paw-coin, brown harness, collar tag, green ball with bone |
| Tone | Friendly, never pushy. Celebrates quietly, never shames |
| Role | Encourages, reminds, celebrates. Never judges spending |

### Brand fit

- Cooper's **green props** match the app accent (`#1F5E4B`). Use that link on purpose.
- Fur stays warm grey/cream, which sits well on the warm off-white background (`#F7F5F0`).
- Cooper is a **guest** in the UI, not the layout. The data stays the hero.

---

## 2. Asset Inventory

| ID | File | Pose | Use |
|---|---|---|---|
| `sit` | `cooper-sit.png` | Front-facing, paws resting | Splash, onboarding, empty states |
| `head` | `cooper-head.png` | Head only | App icon, avatar, favicon |
| `run` | `cooper-run.png` | Running with coin pouch | Success, goal reached, under budget |
| `play` | `cooper-play.png` | Playing with green ball | Reminders, welcome back, light moments |

### Fix before production

- [ ] **Regenerate `head`** so its outline weight and shading match the other three (it currently has a thicker outline and flatter shading).
- [ ] **Create a simplified icon version of `head`:** fewer fur strokes, thicker outline, readable at 48px.
- [ ] **Create a monochrome silhouette** of `head` for notification icons (white on transparent).
- [ ] **Get a vector master** (SVG) of the logo version from a designer.
- [ ] Export every pose on a **transparent background**.

---

## 3. Usage Rules

### Do

- Show Cooper at **emotional moments**: onboarding, empty states, success, reminders.
- Use **one mascot per screen**, maximum.
- Keep Cooper **away from numbers**: no mascot beside amounts, budget bars, or charts.
- Keep animation **short and rare** (1 to 3 seconds, not on every action).
- Let the mascot be **optional**: add a Settings toggle "Show Cooper".
- Write microcopy as a friendly person, not as a brand.

### Don't

- Don't put Cooper in the transactions list, tab bar, or chart areas.
- Don't recolor the fur or change proportions.
- Don't add gradients, glows, or drop shadows behind him.
- Don't stretch, rotate, or crop awkwardly.
- Don't make him react negatively to overspending (no sad dog, no scolding).
- Don't loop animations endlessly on a visible screen.
- Don't combine Cooper with sparkles/confetti spam. One small sparkle is enough.

---

## 4. Placement Map

| Location | Asset | Size | Behavior |
|---|---|---|---|
| App icon (iOS) | `head` icon version | 1024×1024 | Solid background (green or warm white), no transparency |
| App icon (Android adaptive) | `head` | 432×432 foreground | Head inside the center 66% safe zone, solid color background layer |
| Splash screen | `sit` | ~240px tall | Centered, app name below, static or gentle idle |
| Onboarding slide 1 | `sit` | 600×600 | "Track spending in seconds" |
| Onboarding slide 2 | `play` | 600×600 | "Set simple monthly budgets" |
| Onboarding slide 3 | `run` | 600×600 | "Stay on track, stress-free" |
| Empty state: Transactions | `play` | 280px | One line of copy plus one button |
| Empty state: Budgets | `sit` | 280px | "Set your first budget." |
| Save success (Add Transaction) | `run` (small) | 160px | Appears about 1.2s, then fades |
| Goal reached / under budget | `run` | 200px | Once per month at most |
| Daily reminder notification | Monochrome `head` | 96×96 | White silhouette on transparent |
| Settings, About/Support | `head` | 64px | Static |
| Store listing | `head` icon, `run`, `play` | Per store spec | Max 1 to 2 screenshots feature Cooper |
| Website favicon | `head` icon | 512 / 180 / 32 | Static |

### Microcopy examples

| Moment | English | Filipino |
|---|---|---|
| Empty transactions | "Nothing logged yet. Add your first expense." | "Wala pang naka-log. Idagdag ang una mong gastos." |
| Save success | "Logged." | "Naka-log na." |
| Under budget | "You're on track this month." | "Maayos ang takbo ng budget mo ngayong buwan." |
| Reminder | "Anything to log today?" | "May gastos ka ba ngayon na hindi pa naka-log?" |
| Over budget (no mascot) | "Dining is $22 over. It happens." | "Lampas ng $22 ang Dining. Nangyayari 'yan." |

> Over-budget and error states **do not show Cooper**. Keep those screens neutral.

---

## 5. Animation Plan

### 5.1 States

| State | Pose | Length | Trigger |
|---|---|---|---|
| `idle` | `sit` or `head` | 3s loop | Splash, onboarding |
| `celebrate` | `run` | 2s, play once | Save success, goal reached |
| `play` | `play` | 3s loop | Empty states, reminders screen |

### 5.2 Generation prompts (image-to-video)

Tools: Runway, Kling, Luma Dream Machine, Pika, or Hailuo. Upload the pose image as the **first frame**.

**Idle**
```
Cute husky puppy mascot, keep the exact same character design, colors and
outline style as the reference image. Subtle idle loop: gentle breathing,
slow blink once, ears twitch slightly, tongue moves a little, tiny head tilt.
Static camera, no zoom, no camera movement. Plain solid white background.
2D illustrated sticker style, clean outlines. Seamless 3 second loop.
```

**Celebrate**
```
Same puppy mascot as reference, keep character design identical. The puppy
bounces happily in place, tail wagging fast, the gold coins in the green
pouch jiggle and shine, one small sparkle appears. Static camera, plain
white background, 2D illustrated style, clean outlines, no new objects.
Loop of 2 seconds.
```

**Play**
```
Same puppy mascot as reference, identical design. The puppy pats the green
ball with its paws playfully, tail wagging, ball rolls slightly forward and
back. Static camera, plain white background, 2D illustrated style,
consistent outline thickness. 3 second loop.
```

**Negative prompt (if supported)**
```
extra limbs, changing fur pattern, different colors, morphing face, camera
zoom, text, watermark, realistic 3D, blurry, background scenery
```

### 5.3 Prompt tips

- One motion per prompt. Simpler motion gives a more stable character.
- Keep clips 2 to 4 seconds so they loop cleanly.
- Use a **plain white or green-screen background** to make removal easy.
- If the face, fur pattern, or colors drift, regenerate. Don't fix it later.
- Save every prompt and original generation in `source/` as evidence of authorship.

### 5.4 Pipeline: video to app-ready asset

```
Generate video  →  Remove background  →  Trim & loop  →  Export  →  Optimize  →  Add to app
 (Runway etc.)     (Runway / CapCut /     (check first    (WebP)     (< 500 KB     (expo-image)
                    rembg)                 = last frame)              each)
```

| Format | Pros | Cons | Verdict |
|---|---|---|---|
| **Animated WebP (transparent)** | Easy, works with `expo-image` | Larger files, no interactivity | **Use for v1** |
| **APNG** | Transparent, simple | Heavy | Fallback |
| **Transparent WebM/MOV** | Light | Patchy React Native support | Avoid |
| **Rive** | Tiny, interactive states, code-triggered | Needs a rigged, layered character | **Target for v2** |
| **Lottie** | Small | Needs vector art, poor fit for detailed fur | Not recommended |

### 5.5 Implementation (v1)

```tsx
import { Image } from 'expo-image';
import { useReducedMotion } from 'react-native-reanimated';

export function Cooper({ state = 'sit', size = 160 }) {
  const reduce = useReducedMotion();
  const staticSrc = {
    sit: require('@/assets/mascot/static/cooper-sit.png'),
    run: require('@/assets/mascot/static/cooper-run.png'),
    play: require('@/assets/mascot/static/cooper-play.png'),
  }[state];
  const animSrc = {
    sit: require('@/assets/mascot/anim/cooper-idle.webp'),
    run: require('@/assets/mascot/anim/cooper-celebrate.webp'),
    play: require('@/assets/mascot/anim/cooper-play.webp'),
  }[state];

  return (
    <Image
      source={reduce ? staticSrc : animSrc}
      style={{ width: size, height: size }}
      contentFit="contain"
      accessibilityLabel="Cooper the husky puppy"
      accessible={false /* decorative */}
    />
  );
}
```

Rules in code:
- Respect **Reduce Motion**: show the static PNG.
- Mark Cooper as **decorative** (hidden from screen readers) unless he carries meaning.
- Preload the animations used on first launch.
- Respect the Settings toggle "Show Cooper".

---

## 6. File & Folder Structure

```
assets/mascot/
├── static/
│   ├── cooper-sit.png
│   ├── cooper-head.png
│   ├── cooper-run.png
│   └── cooper-play.png
├── anim/
│   ├── cooper-idle.webp
│   ├── cooper-celebrate.webp
│   └── cooper-play.webp
├── icon/
│   ├── app-icon-1024.png
│   ├── adaptive-foreground-432.png
│   ├── notification-mono-96.png
│   └── favicon-512.png
└── source/
    ├── vector/              # SVG master (from designer)
    ├── originals/           # raw AI generations, untouched
    └── prompts.txt          # every prompt used, with date and tool
```

**Naming:** `cooper-<pose>[-<variant>].<ext>`, lowercase, hyphens only.

---

## 7. Icon Export Checklist

- [ ] iOS: 1024×1024, no transparency, no rounded corners (the OS masks them)
- [ ] Android adaptive: 432×432 foreground and background layers, head within the center 66%
- [ ] Android monochrome (themed icon): single-color silhouette
- [ ] Notification icon: white silhouette on transparent, 96×96
- [ ] Favicon: 512, 180, 32
- [ ] Test the icon at **48px, 60px, 120px**: eyes, nose, and tongue must stay readable
- [ ] Test on light, dark, and colored wallpapers

---

## 8. Quality Checklist (before shipping any Cooper asset)

- [ ] Same outline weight and shading as the other poses
- [ ] Transparent background, no white halo around fur edges
- [ ] Colors match the approved palette
- [ ] File size under 500 KB per animation
- [ ] Loop is seamless (first frame equals last frame)
- [ ] Works in light and dark mode (check edge halos on dark)
- [ ] Reduce Motion fallback verified
- [ ] Appears on no more than one element per screen

---

## 9. Trademark & Legal Checklist

> This is a checklist, not legal advice. Consult an IP lawyer or **IPOPHL** (Philippines) before filing.

### Name

- [ ] "Cooper" is a very common name. Search for conflicts in software (**Class 9**) and financial or budgeting services (**Class 36**).
- [ ] Search **IPOPHL**, **USPTO**, **EUIPO**, and **WIPO Global Brand Database**.
- [ ] Consider a more distinctive name (for example, "Cooper" plus a unique word).
- [ ] Check app store names and domain / social handles for availability.

### Logo

- [ ] Do a reverse-image search to confirm the design doesn't resemble an existing mascot or brand.
- [ ] File the **wordmark** and **logo mark** separately for the strongest protection.
- [ ] Prepare clean logo files (SVG + PNG) for the application.

### Authorship (important for AI-generated art)

- [ ] Many jurisdictions don't grant copyright to purely AI-generated output. Add **real human authorship**: have a designer redraw or vectorize the final logo.
- [ ] Keep all originals, prompts, dates, and revision history as evidence.
- [ ] Read the **commercial-use terms** of every AI tool used.
- [ ] Get a **written agreement** (work-for-hire or assignment) from any designer.

### Classes to consider

| Class | Covers |
|---|---|
| 9 | Downloadable mobile apps |
| 36 | Budgeting and financial management services |
| 42 | Software as a service |

---

## 10. Phase Plan for the Mascot

Maps to the phases in `Skills Project-Content.md`.

| Phase | Mascot tasks | Output |
|---|---|---|
| **0, Discovery** | Name search, trademark pre-check | Short-list of names |
| **1, Design system** | Fix `head` style, simplify icon, silhouette, vector master, define placement | Final static assets, icon set |
| **2, Setup** | Add `assets/mascot/`, build `<Cooper />` component, Reduce Motion handling | Working component |
| **3, Core features** | Use Cooper in onboarding and empty states (static first) | Screens integrated |
| **4, Extras** | Generate animations, remove backgrounds, export WebP, add success and reminder moments | Animated states |
| **5, Hardening** | Check performance, file sizes, a11y, Reduce Motion, "Show Cooper" toggle | Optimized assets |
| **6, Beta** | Ask testers: cute or distracting? Adjust frequency | Feedback report |
| **7, Release** | Final icons, store screenshots, file trademark | Store-ready assets |
| **8, Post-launch** | Optional: rig in Rive, seasonal variants | v2 mascot |

---

## 11. Future Ideas (v2+)

- Rive-rigged Cooper with state machine (idle, celebrate, play, sleep)
- Seasonal variants (scarf, Santa hat) shown rarely
- Home screen widget with a small static Cooper
- Sticker pack for sharing
- Unlockable accessories (only if they stay calm and non-gamified)

---

## 12. Quick Reference

**Cooper appears:** onboarding, empty states, save success, goal reached, reminders, icon.
**Cooper never appears:** next to numbers, in lists, in charts, in error or over-budget states.

**One mascot per screen. Short animations. Always optional.**
