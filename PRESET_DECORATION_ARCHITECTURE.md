# Preset Decoration Feature - Architecture Diagram

## System Interaction Flow

```
┌─────────────────────────────────────────────────────────────────────┐
│                         USER INTERFACE                              │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │                    TJCanvas2D (Main Canvas)                  │  │
│  │  ┌────────────────────────────────────────────────────────┐ │  │
│  │  │           Emoji Circle (World Coordinates)             │ │  │
│  │  │                                                         │ │  │
│  │  │   [Emoji Face]  ← (Yellow circle background)          │ │  │
│  │  │   [Features]    ← (Curves/PtCurves - base layer)     │ │  │
│  │  │   [Decorations] ← (TJImage objects - TOP LAYER) ✨   │ │  │
│  │  │   (All clipped to circle boundary)                    │ │  │
│  │  └────────────────────────────────────────────────────────┘ │  │
│  │         │                                           │         │  │
│  │         │ (Screen Coords)                   (Screen Coords) │  │
│  │         │                                           │         │  │
│  │  ┌──────▼─────────────────────────┐  ┌─────────────▼──────┐ │  │
│  │  │ Preset Decoration Panel (Right) │  │ Top/Bottom Panels  │ │  │
│  │  │ ┌──────────────────────────────┐│  │ (Navigation UI)    │ │  │
│  │  │ │ Hat 1      [Icon 1]         ││  │                    │ │  │
│  │  │ │ Glasses    [Icon 2]         ││  │                    │ │  │
│  │  │ │ Flower     [Icon 3]         ││  │                    │ │  │
│  │  │ │ Star       [Icon 4]         ││  │                    │ │  │
│  │  │ │ ...        [...] Draggable  ││  │                    │ │  │
│  │  │ └──────────────────────────────┘│  └────────────────────┘ │  │
│  │  └──────────────────────────────────┘                         │  │
│  └──────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘


                          DATA LAYER
        ┌─────────────────────────────────────────────┐
        │         TJEmojiPage (Data Model)            │
        │  ┌──────────────────────────────────────┐  │
        │  │ mPtCurves:           ArrayList<>     │  │
        │  │ mSelectedPtCurves:   ArrayList<>     │  │
        │  │ mDecorations:        ArrayList<>  ← │  │ NEW!
        │  │                      (TJImage objs)  │  │
        │  └──────────────────────────────────────┘  │
        └─────────────────────────────────────────────┘


                    INTERACTION LAYER
    ┌──────────────────────────────────────────────────────┐
    │     TJEmojiScenario → EmojiDrawingScene              │
    │  ┌────────────────────────────────────────────────┐ │
    │  │ Mouse Event Handlers:                          │ │
    │  │ ├─ handleMousePress()  → Detect drag source   │ │
    │  │ ├─ handleMouseDrag()   → Move decoration      │ │
    │  │ └─ handleMouseRelease()→ Finalize position    │ │
    │  └────────────────────────────────────────────────┘ │
    │                        │                             │
    │                        ▼                             │
    │  ┌────────────────────────────────────────────────┐ │
    │  │ TJDecorationDragMgr (State Management)        │ │
    │  │ ├─ startDragFromPreset()      [Phase 1]       │ │
    │  │ ├─ dragPresetOverCanvas()     [Phase 2]       │ │
    │  │ ├─ dropPresetOntoCanvas()     [Phase 3]       │ │
    │  │ ├─ startDragDecoration()      [Reposition]    │ │
    │  │ └─ dragDecorationOnCanvas()   [Reposition]    │ │
    │  └────────────────────────────────────────────────┘ │
    └──────────────────────────────────────────────────────┘
```

---

## Drag-and-Drop Sequence Diagram

### Initial Preset Drag (From Panel to Canvas)

```
User Action          Preset Panel           Drag Manager           Emoji Page
    │                    │                       │                      │
    │─ Click Icon ───────►│                       │                      │
    │                    │─ Detect Click ───────►│                       │
    │                    │◄─ Start Drag ────────│                       │
    │                    │                       │ (Save preset ref,     │
    │                    │                       │  save screen pt)      │
    │                    │                       │                       │
    │─ Drag Over Canvas  │─ Screen Updates ─────►│                       │
    │   (Visual Feedback)│                       │ (Update cursor pt)    │
    │                    │                       │                       │
    │─ Release Mouse ────┤                       │─ Load Image ─────────►│
    │   (On Canvas)      │                       │─ Create TJImage      │
    │                    │                       │─ Add to mDecorations │
    │                    │                       │                       │
    │◄─ Decoration ──────────────────────────────────────────────────────│
    │   Placed!          │                       │                       │
```

### Reposition Decoration (Already on Canvas)

```
User Action     Emoji Canvas           Drag Manager           Emoji Page
    │               │                       │                      │
    │─ Click on ────►│                       │                      │
    │   Decoration   │                       │                      │
    │                │─ Detect Hit ────────►│                       │
    │                │                       │─ Start Drag          │
    │                │                       │ (Save decoration ref, │
    │                │                       │  save screen pt)      │
    │                │                       │                       │
    │─ Drag on ──────►│─ Screen Updates ────►│                       │
    │   Canvas        │                       │─ Calculate Delta     │
    │                │                       │─ Transform to World  │
    │                │                       │                       │
    │                │◄────── Translate Decoration ────────────────│
    │                │        (Update x, y)                         │
    │                │                       │                       │
    │─ Release ──────►│─ End Drag ──────────►│                       │
    │   Mouse         │                       │ (Finalize position)   │
    │                │                       │                       │
    │◄─ Decoration ───────────────────────────────────────────────│
    │   at New Pos    │                       │                       │
```

---

## Class Relationships

```
TJEmojiPage
    ├─ mPtCurves: ArrayList<TJPtCurve>
    ├─ mSelectedPtCurves: ArrayList<TJPtCurve>
    └─ mDecorations: ArrayList<TJImage>  ◄── NEW
        │
        └─ TJImage  (inherited from existing codebase)
            ├─ mX, mY (position at center)
            ├─ mScale (0.0 - 1.0+)
            ├─ mRotation (in radians)
            ├─ mImage (BufferedImage - loaded from asset)
            ├─ draw(Graphics2D) - renders with transforms
            ├─ contains(Point) - hit detection
            └─ translate(dx, dy) - reposition

TJEmojiScenario
    └─ mDecorationDragMgr: TJDecorationDragMgr  ◄── NEW
        ├─ mIsDecorationBeingDragged: boolean
        ├─ mDraggedPreset: PresetIcon | null
        ├─ mDraggedDecoration: TJImage | null
        ├─ mLastMouseScreenPoint: Point
        ├─ startDragFromPreset(PresetIcon, Point)
        ├─ dragPresetOverCanvas(Point)
        ├─ dropPresetOntoCanvas(TJ, Point)
        ├─ startDragDecoration(TJImage, Point)
        └─ dragDecorationOnCanvas(TJ, Point)

EmojiDrawingScene (inner class of TJEmojiScenario)
    ├─ mDecorationPanel: TJDecorationPresetPanel  ◄── NEW
    ├─ handleMousePress(MouseEvent) - MODIFIED
    ├─ handleMouseDrag(MouseEvent) - MODIFIED
    └─ handleMouseRelease(MouseEvent) - MODIFIED

TJDecorationPresetPanel extends JPanel
    ├─ mPresets: Map<String, String> (name → image path)
    ├─ mPresetIcons: ArrayList<PresetIcon>
    ├─ mHoveredIcon: PresetIcon | null
    ├─ mSelectedIcon: PresetIcon | null
    ├─ loadPresets() - loads from assets/decorations/
    ├─ layoutPresets() - arranges icons in grid
    ├─ getPresetAtPoint(x, y) - hit detection
    └─ paintComponent(Graphics) - rendering

PresetIcon (inner class of TJDecorationPresetPanel)
    ├─ name: String
    ├─ image: BufferedImage
    ├─ x, y, width, height: int
    ├─ draw(Graphics2D)
    └─ contains(x, y): boolean
```

---

## Rendering Pipeline (Emoji Circle)

```
┌─────────────────────────────────────────────────────────────────┐
│ EmojiDrawingScene.renderWorldObjects(Graphics2D)                │
│  (Called every frame)                                           │
└─────────────────────────────────────────────────────────────────┘
                           │
                           ▼
            ┌──────────────────────────────┐
            │ Setup Clipping to Circle     │
            │ Shape originalClip = ...     │
            │ g2.setClip(circle)           │
            └──────────────────────────────┘
                           │
                           ▼
            ┌──────────────────────────────┐
            │ LAYER 1: Background          │
            │ g2.setColor(YELLOW)          │
            │ g2.fill(circle)              │
            │ g2.draw(circle border)       │
            └──────────────────────────────┘
                           │
                           ▼
            ┌──────────────────────────────┐
            │ LAYER 2: Base Features       │
            │ drawPtCurves()               │
            │ drawSelectedPtCurves()       │
            │ drawCurPtCurve()             │
            └──────────────────────────────┘
                           │
                           ▼
            ┌──────────────────────────────┐
            │ LAYER 3: DECORATIONS ✨      │
            │ for (TJImage deco :          │
            │      emojiPage.               │
            │      getDecorations()) {     │
            │   deco.draw(g2)              │
            │   [Optional: selection vis]  │
            │ }                            │
            └──────────────────────────────┘
                           │
                           ▼
            ┌──────────────────────────────┐
            │ Restore Original Clip        │
            │ g2.setClip(originalClip)     │
            └──────────────────────────────┘
```

---

## Coordinate System Transformations

```
                    PRESET PANEL
                  (Screen Coords)
                        │
              Click at [x, y] screen
                        │
                        ▼
            TJDecorationDragMgr
            .startDragFromPreset()
            (Save preset reference)
                        │
                        ▼
            User drags over CANVAS
            .dragPresetOverCanvas()
            (Update screen position)
                        │
                        ▼
            User releases MOUSE
            .dropPresetOntoCanvas()
                        │
                        ├─ Read screen point [sx, sy]
                        │
                        ├─ Transform to world:
                        │  Point2D.Double worldPt = new Point2D.Double(sx, sy)
                        │  xform.getCurrentXformFromScreenToWorld()
                        │    .transform(screenPt, worldPt)
                        │
                        └─ Create decoration at world coordinates
                           TJImage deco = new TJImage(
                               presetImg,
                               (int)worldPt.x,     ◄── World X
                               (int)worldPt.y,     ◄── World Y
                               0.3)                ◄── Initial scale


                    DECORATION DRAG
              (Initially at world coords)
                        │
              User clicks decoration
                        │
                        ▼
            TJEmojiScenario checks:
            if (decoration.contains(screenPt)) {
                startDragDecoration(deco, screenPt)
            }
                        │
                        ▼
            User drags mouse on CANVAS
            .dragDecorationOnCanvas()
                        │
                        ├─ Calculate screen delta:
                        │  dx = currentScreenPt.x - lastScreenPt.x
                        │  dy = currentScreenPt.y - lastScreenPt.y
                        │
                        ├─ Transform to world delta:
                        │  scale = 1.0 / xform.getCurrentScale()
                        │  worldDx = dx * scale
                        │  worldDy = dy * scale
                        │
                        └─ Update decoration:
                           decoration.translate(worldDx, worldDy)
```

---

## File Structure After Implementation

```
TravelJournal - BeforeFinalFinal/
├── src/
│   ├── tj/
│   │   ├── TJEmojiPage.java (MODIFIED: add mDecorations field)
│   │   ├── TJDecorationDragMgr.java (NEW)
│   │   ├── TJ.java (MODIFIED: add setRightPanel())
│   │   └── scenario/
│   │       └── TJEmojiScenario.java (MODIFIED: add decoration support)
│   └── utils/
│       └── TJDecorationPresetPanel.java (NEW)
│
├── assets/
│   ├── decorations/ (NEW DIRECTORY)
│   │   ├── hat_1.png
│   │   ├── hat_2.png
│   │   ├── glasses_1.png
│   │   ├── glasses_2.png
│   │   ├── flower.png
│   │   ├── star.png
│   │   ├── bow.png
│   │   ├── crown.png
│   │   └── ... (more presets)
│   └── (existing assets)
│
└── PRESET_DECORATION_IMPLEMENTATION.md (THIS GUIDE)
```

---

## State Machine: Emoji Drawing Scene

```
                    ┌─────────────────────┐
                    │   Ready State       │
                    │ No interaction      │
                    │ rendering emoji     │
                    └────────┬────────────┘
                             │
              ┌──────────────┴──────────────┐
              │                             │
              ▼                             ▼
    ┌──────────────────────┐     ┌─────────────────────┐
    │ Dragging Preset from │     │  Dragging Decoration │
    │ Decoration Panel     │     │  on Canvas          │
    │ ✓ Show cursor near   │     │ ✓ Move decoration   │
    │   mouse              │     │   with mouse        │
    │ ✓ Highlight icon     │     │ ✓ Visual feedback   │
    └──────┬───────────────┘     └────────┬────────────┘
           │                              │
           │ Release on canvas           │ Release
           │                              │
           └──────────────┬───────────────┘
                          │
                          ▼
                ┌──────────────────────┐
                │  Decoration Added    │
                │ or Repositioned      │
                │ Back to Ready State  │
                └──────────────────────┘
```

---

## Key Insights

### Why This Architecture Works:

1. **Leverage Existing TJImage**
   - Already handles: position, scale, rotation, serialization
   - Just add them to emoji page as separate collection

2. **Clipping Ensures Boundary**
   - Decorations are rendered inside `g2.setClip(circle)`
   - No overflow outside emoji boundary needed
   - Simple and efficient

3. **World Coordinates for Decorations**
   - Store decoration position in world space (like other page objects)
   - Reuse transform system for screen-to-world conversions
   - Easy to pan/zoom with rest of page

4. **Separation of Concerns**
   - Drag logic in `TJDecorationDragMgr` (clean, testable)
   - UI rendering in `TJDecorationPresetPanel` (encapsulated)
   - Scene integration in `EmojiDrawingScene` (minimal changes)
   - Data in `TJEmojiPage` (persistent)

5. **Minimal Modifications**
   - Don't redesign emoji itself (render curves as before)
   - Don't change existing command system
   - Just add: 1 new class, 1 new panel class, 1 modified scene class
