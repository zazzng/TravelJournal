# Preset Decoration Feature - Complete Reference Guide

## Document Index

You have 4 comprehensive documents:

| Document | Purpose | Length | Read First? |
|----------|---------|--------|------------|
| **PRESET_DECORATION_SUMMARY.md** | Overview & quick reference | 2 pages | ✓ YES |
| **PRESET_DECORATION_QUICK_START.md** | Copy-paste code by step | 5 pages | 2nd |
| **PRESET_DECORATION_IMPLEMENTATION.md** | Full architectural design | 12 pages | 3rd |
| **PRESET_DECORATION_ARCHITECTURE.md** | Visual diagrams & flows | 6 pages | Reference |

---

## Your Current Architecture (Before Implementation)

```
TJEmojiPage
  ├─ mPtCurves
  ├─ mSelectedPtCurves
  └─ mImages

TJEmojiScenario
  └─ EmojiDrawingScene
      ├─ Draw emoji circle
      ├─ Draw curves on top
      └─ Handle mouse events
```

---

## Your New Architecture (After Implementation)

```
TJEmojiPage
  ├─ mPtCurves
  ├─ mSelectedPtCurves
  ├─ mImages
  └─ mDecorations  ← NEW (ArrayList<TJImage>)

TJEmojiScenario
  ├─ mDecorationDragMgr ← NEW (TJDecorationDragMgr)
  └─ EmojiDrawingScene
      ├─ mDecorationPanel ← NEW (TJDecorationPresetPanel)
      ├─ Draw emoji circle
      ├─ Draw curves
      ├─ Draw decorations ← NEW (on top)
      └─ Handle mouse events (ENHANCED)

TJDecorationDragMgr ← NEW
  ├─ Drag from preset panel
  └─ Drag on canvas

TJDecorationPresetPanel ← NEW
  ├─ Load preset images
  └─ Display in grid
```

---

## The 3-Step Interaction Flow

### Step 1: Choose & Drag Preset
```
User clicks icon in TJDecorationPresetPanel
  ↓
handlePresetPanelPress() detects click
  ↓
TJDecorationDragMgr.startDragFromPreset(preset, point)
  ↓
Drag manager remembers:
  - Which preset was selected
  - Initial screen position
```

### Step 2: Visual Feedback While Dragging
```
User moves mouse over canvas
  ↓
handleMouseDrag() detects motion
  ↓
TJDecorationDragMgr.dragPresetOverCanvas(point)
  ↓
Updates last mouse point
  ↓
renderScreenObjects() shows drag preview
```

### Step 3: Drop & Create Decoration
```
User releases mouse on emoji canvas
  ↓
handleMouseRelease() detects release
  ↓
TJDecorationDragMgr.dropPresetOntoCanvas(tj, point)
  ↓
Converts screen point → world point using xform
  ↓
Creates new TJImage at world coordinates
  ↓
Adds to TJEmojiPage.mDecorations
  ↓
User can now drag it to reposition
```

---

## Code Organization

### 1. Data Storage (TJEmojiPage.java)
**What changes**: Add ONE field + 3 methods
```java
private ArrayList<TJImage> mDecorations;
public ArrayList<TJImage> getDecorations() { ... }
public void addDecoration(TJImage deco) { ... }
public void removeDecoration(TJImage deco) { ... }
```
**Why**: Stores all decorations for an emoji page, persists with save data

---

### 2. Drag Management (NEW: TJDecorationDragMgr.java)
**What it does**: Manages drag state and transformations
```java
// State tracking
mIsDecorationBeingDragged
mDraggedPreset (PresetIcon)
mDraggedDecoration (TJImage)
mLastMouseScreenPoint

// Methods for preset drag
startDragFromPreset()
dragPresetOverCanvas()
dropPresetOntoCanvas()

// Methods for decoration drag
startDragDecoration()
dragDecorationOnCanvas()
dropDecoration()
```
**Why**: Separates drag logic from scene rendering, easy to test

---

### 3. UI Panel (NEW: TJDecorationPresetPanel.java)
**What it does**: Shows preset decoration icons in a grid
```java
TJDecorationPresetPanel extends JPanel
  ├─ loadPresets() - reads from assets/decorations/
  ├─ layoutPresets() - arranges in vertical grid
  ├─ getPresetAtPoint(x, y) - hit detection
  └─ paintComponent() - renders icons
  
PresetIcon (inner class)
  ├─ name, image, x, y, width, height
  ├─ draw(Graphics2D)
  └─ contains(x, y)
```
**Why**: Provides intuitive UI for selecting decorations

---

### 4. Scene Integration (MODIFY: TJEmojiScenario.java)
**What changes**: 
- Add drag manager to main class
- Add decoration panel to EmojiDrawingScene
- Enhance mouse event handlers
- Add decoration rendering

**Methods modified in EmojiDrawingScene**:
- `getReady()` - initialize decoration panel
- `wrapUp()` - cleanup decoration panel
- `handleMousePress()` - detect decoration clicks
- `handleMouseDrag()` - drag decoration
- `handleMouseRelease()` - drop decoration
- `renderWorldObjects()` - draw decorations on top

**Why**: Integrates new components into existing scene system

---

### 5. Layout Support (MODIFY: TJ.java)
**What changes**: Add right panel support
```java
private JPanel mRightPanel;
public void setRightPanel(JPanel panel) { ... }
```
**Why**: Allows scenes to add UI components to the frame

---

## The Rendering Stack (Drawing Order)

```
Frame
  ├─ Background: Canvas background color (Light gray)
  │
  ├─ World Objects (after xform):
  │   ├─ Spread pages (white rectangles)
  │   │
  │   └─ Emoji Circle (clipped region):
  │       ├─ Circle fill (Yellow)
  │       ├─ Circle outline (Dark gray)
  │       ├─ Curves (Base features) ← User draws here
  │       ├─ Selected curves (Orange)
  │       ├─ Current curve
  │       └─ Decorations ← NEW (Hats, glasses, etc.)
  │
  └─ Screen Objects (no transform):
      ├─ Top nav panel
      ├─ Bottom nav panel
      ├─ Right panel (decoration presets) ← NEW
      ├─ Info text
      └─ Drag preview ← NEW
```

The key: **Decorations drawn AFTER base features, INSIDE clipped region**

---

## Coordinate Transformations

### The Transform Chain
```
User's physical position
  ↓
Screen pixels (what you see)
  ↓ (via TJXform.getCurrentXformFromScreenToWorld)
↓
World coordinates (where it's stored)
```

### For Decorations
```
1. User clicks preset (SCREEN coordinates)
   → EmojiDrawingScene.handleMousePress()
   
2. Drag over canvas (SCREEN coordinates)
   → EmojiDrawingScene.handleMouseDrag()
   
3. User releases (SCREEN coordinates)
   → TJDecorationDragMgr.dropPresetOntoCanvas()
   → Convert screen [sx, sy] to world [wx, wy]
   → Create TJImage(img, wx, wy, 0.3)
   
4. Render loop
   → Transform world [wx, wy] back to screen via xform
   → Draw at screen position with clipping
```

### Code Example
```java
// In dropPresetOntoCanvas():
Point2D.Double worldPoint = new Point2D.Double();
tj.getXform().getCurrentXformFromScreenToWorld()
    .transform(new Point2D.Double(screenPt.x, screenPt.y), worldPoint);

// Now use worldPoint.x, worldPoint.y for decoration position
TJImage deco = new TJImage(img, (int)worldPoint.x, (int)worldPoint.y, 0.3);
```

---

## Implementation Checklist by Phase

### ✓ Phase 1: Data Model (15 min)
- [ ] Add `mDecorations` field to TJEmojiPage
- [ ] Add getter method
- [ ] Add addDecoration() method
- [ ] Add removeDecoration() method
- [ ] Update constructor to initialize ArrayList
- [ ] Update isContentEmpty() to include decorations

### ✓ Phase 2: Drag Manager (45 min)
- [ ] Create TJDecorationDragMgr.java
- [ ] Implement state fields (isDragging, draggedPreset, draggedDecoration, etc.)
- [ ] Implement startDragFromPreset()
- [ ] Implement dragPresetOverCanvas()
- [ ] Implement dropPresetOntoCanvas()
- [ ] Implement startDragDecoration()
- [ ] Implement dragDecorationOnCanvas()
- [ ] Implement endDragSession()
- [ ] Test: Can create drag manager, call methods without errors

### ✓ Phase 3: Preset Panel UI (30 min)
- [ ] Create TJDecorationPresetPanel.java
- [ ] Create PresetIcon inner class
- [ ] Implement loadPresets() - auto-load from assets/decorations/
- [ ] Implement layoutPresets() - arrange in grid
- [ ] Implement getPresetAtPoint() - hit detection
- [ ] Implement paintComponent() - render icons with borders
- [ ] Implement setHoveredIcon(), setSelectedIcon() - visual feedback
- [ ] Test: Panel displays icons, hit detection works

### ✓ Phase 4: Scene Integration - Part A (30 min)
- [ ] Add mDecorationDragMgr to TJEmojiScenario main class
- [ ] Add getter for drag manager
- [ ] Add mDecorationPanel field to EmojiDrawingScene
- [ ] Add preset panel initialization in getReady()
- [ ] Add preset panel mouse listener
- [ ] Implement handlePresetPanelPress()
- [ ] Test: Panel appears when entering emoji drawing scene

### ✓ Phase 5: Scene Integration - Part B (45 min)
- [ ] Update handleMousePress() to detect decoration clicks
- [ ] Update handleMouseDrag() to handle decoration dragging
- [ ] Update handleMouseRelease() to drop decorations
- [ ] Add decoration rendering to renderWorldObjects()
- [ ] Test: Can drag preset from panel and see it appear

### ✓ Phase 6: Layout & Polish (20 min)
- [ ] Add mRightPanel field to TJ.java
- [ ] Add setRightPanel() method to TJ.java
- [ ] Update EmojiDrawingScene.getReady() to call setRightPanel()
- [ ] Update EmojiDrawingScene.wrapUp() to clear right panel
- [ ] Create assets/decorations/ folder
- [ ] Add test decoration images
- [ ] Test: Panel appears on right side, survives scene transitions

---

## Testing Scenarios

### Scenario 1: Single Decoration
```
1. Open emoji page
2. Click/drag "Hat" preset from right panel
3. Drop on emoji face
4. Verify: Hat appears at drop point, inside circle
5. Drag hat to new position
6. Verify: Hat moves smoothly
```

### Scenario 2: Multiple Decorations
```
1. Add Hat (step 1-4 from Scenario 1)
2. Add Glasses - drag from panel
3. Verify: Both visible, no overlap issues
4. Reposition each independently
5. Verify: Each moves independently
```

### Scenario 3: Boundary Clipping
```
1. Add Hat to emoji
2. Drag hat toward edge of circle
3. Drag past boundary (release outside circle)
4. Verify: Hat is still clipped at boundary, not cut off mid-image
5. Reposition inside boundary
```

### Scenario 4: Persistence
```
1. Add decorations to emoji
2. Save journal/emoji page
3. Exit scene, come back
4. Verify: Decorations are still there
5. Can move them again
```

### Scenario 5: Export
```
1. Add decorations to emoji
2. Export emoji circle drawing
3. Verify: Exported image includes decorations
```

---

## Common Gotchas & How to Avoid Them

### Gotcha 1: Decorations appear outside circle
**Cause**: Forgot `g2.setClip(circle)` before drawing  
**Fix**: Always call `g2.setClip(circle)` before rendering, then `g2.setClip(originalClip)` after

### Gotcha 2: Decoration doesn't respond to clicks
**Cause**: Only checking new decorations, not existing ones  
**Fix**: In handleMousePress(), check all decorations:
```java
for (TJImage decoration : emojiPage.getDecorations()) {
    if (decoration.contains(screenPt)) { ... }
}
```

### Gotcha 3: Drag position is offset
**Cause**: Not converting screen→world coordinates  
**Fix**: Always use xform to convert:
```java
tj.getXform().getCurrentXformFromScreenToWorld()
    .transform(screenPoint, worldPoint);
```

### Gotcha 4: Preset images aren't loading
**Cause**: Wrong path in loadPresets()  
**Fix**: Verify `assets/decorations/filename.png` exists relative to project root, test ImageLoader.loadImage()

### Gotcha 5: Decoration drag is jerky
**Cause**: Not calculating screen-to-world scale correctly  
**Fix**: Use `screenToWorldScale = 1.0 / xform.getCurrentScale()`

### Gotcha 6: Can't drag from panel to canvas
**Cause**: handleMousePress() on canvas doesn't check for drag manager state  
**Fix**: Check `dragMgr.getDraggedPreset() != null` to distinguish drag from preset vs. draw

---

## Key Classes & Methods Reference

### TJEmojiPage (MODIFIED)
```java
getDecorations(): ArrayList<TJImage>        // NEW
addDecoration(TJImage): void                // NEW
removeDecoration(TJImage): void             // NEW
```

### TJDecorationDragMgr (NEW)
```java
isDecorationBeingDragged(): boolean
getDraggedPreset(): PresetIcon
getDraggedDecoration(): TJImage
startDragFromPreset(PresetIcon, Point): void
dragPresetOverCanvas(Point): void
dropPresetOntoCanvas(TJ, Point): void
startDragDecoration(TJImage, Point): void
dragDecorationOnCanvas(TJ, Point): void
dropDecoration(): void
endDragSession(): void
getLastMouseScreenPoint(): Point
```

### TJDecorationPresetPanel (NEW)
```java
getPresetAtPoint(int, int): PresetIcon
setHoveredIcon(PresetIcon): void
setSelectedIcon(PresetIcon): void
getSelectedIcon(): PresetIcon
```

### EmojiDrawingScene (MODIFIED)
```java
getReady(): void                            // Enhanced
wrapUp(): void                              // Enhanced
handleMousePress(MouseEvent): void          // Enhanced
handleMouseDrag(MouseEvent): void           // Enhanced
handleMouseRelease(MouseEvent): void        // Enhanced
renderWorldObjects(Graphics2D): void        // Enhanced
handlePresetPanelPress(MouseEvent): void    // NEW
```

### TJEmojiScenario (MODIFIED)
```java
getDecorationDragMgr(): TJDecorationDragMgr // NEW
```

---

## Performance Considerations

### Rendering Impact
- Decorations rendered as images (very fast)
- Clipping to circle is efficient (GPU-accelerated)
- No performance hit for typical use (5-10 decorations)

### Memory Impact
- Each decoration = one TJImage object
- TJImage stores BufferedImage + position/transform
- PNG bytes stored for serialization
- Typical: ~50-100 KB per decoration

### Optimization Ideas (Future)
- Cache scaled/rotated versions of images
- Use sprite atlas for multiple small icons
- Batch render decorations
- Implement undo/redo for decoration changes

---

## Debugging Tips

### Enable Debug Output
Already included in code:
```java
System.out.println("[DECO-DRAG] Started dragging preset: " + preset.name);
System.out.println("[DECO-DRAG] Added decoration at (" + worldPoint.x + ", " + worldPoint.y + ")");
```

### Check State
```java
TJEmojiScenario scenario = TJEmojiScenario.getSingle();
TJDecorationDragMgr dragMgr = scenario.getDecorationDragMgr();
System.out.println("Is dragging: " + dragMgr.isDecorationBeingDragged());
System.out.println("Dragged preset: " + dragMgr.getDraggedPreset());
System.out.println("Dragged decoration: " + dragMgr.getDraggedDecoration());
```

### Verify Data
```java
TJEmojiPage page = scenario.getTargetEmojiPage();
System.out.println("Decorations count: " + page.getDecorations().size());
for (TJImage deco : page.getDecorations()) {
    System.out.println("  - Decoration at (" + deco.getX() + ", " + deco.getY() + ")");
}
```

---

## Next: Where to Go From Here

### Once Basic Feature Works:
1. ✓ Save/load decorations with emoji page
2. ✓ Verify persistence works
3. ✓ Test with multiple decorations
4. ✓ Fine-tune initial scale/position

### Then Enhance With:
1. **Decoration Panel**: Show list, allow deletion, scaling
2. **Commands**: TJCmdToAddDecoration, TJCmdToRemoveDecoration (undo/redo)
3. **Properties**: Rotation, flip, opacity controls
4. **Categories**: Organize presets (hats, glasses, etc.)
5. **Snap-to**: Snap to common positions (top, sides)
6. **Z-order**: Change stacking order of decorations
7. **Custom Decorations**: User can add their own presets

---

## Summary Table

| Aspect | Details |
|--------|---------|
| **New Classes** | TJDecorationDragMgr, TJDecorationPresetPanel (+ PresetIcon inner) |
| **Modified Classes** | TJEmojiPage, TJEmojiScenario, TJ |
| **New Files** | 2 classes + assets/decorations/ folder |
| **Total New Code** | ~300 lines |
| **Reused Classes** | TJImage (existing), TJXform, TJCanvas2D |
| **Key Concept** | Decorations = ArrayList<TJImage> stored in TJEmojiPage |
| **Drawing Order** | Circle background → Curves → Decorations (on top) |
| **Clipping** | All drawn inside g2.setClip(circle) |
| **Persistence** | Automatic (TJImage already serializes) |
| **User Flow** | Click preset → Drag → Drop → Can reposition |

---

**You now have everything needed to implement the preset decoration feature. Start with QUICK_START.md and code it step-by-step!**
