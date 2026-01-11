# Emoji Circle Drawing - Coordinate Space Fix

## Problem

When drawing in the emoji circle that spans across the two-page journal spread, curves could **not be drawn continuously from right to left**. There was an **invisible vertical boundary at the center** (the page divider).

### Root Cause

**Coordinate space mismatch:**

1. **Mouse input** comes in **screen coordinates** (from `MouseEvent.getPoint()`)
2. **Curves should be stored** in **world coordinates** (the canonical drawing space)
3. **The emoji circle** was in **screen coordinates** but the rendering system expected **world coordinates**

This caused a semantic mismatch:
- Points were created in screen space
- They were stored directly without transformation
- When drawn, the system tried to render them as if they were already in world space
- This caused clipping at the page divider (x = WORLD_PAGE_WIDTH) because the coordinate system assumed two separate pages

## Solution

### Changed Files
- `src/tj/scenario/TJEmojiScenario.java` - `EmojiDrawingScene` class

### Key Changes

#### 1. `handleMousePress()` - Initialize Curve with World Coordinates
**Before:**
```java
TJCmdToCreateCurPtCurve.execute(tj, pt);  // pt was screen coordinates
```

**After:**
```java
// Convert screen coordinates to world coordinates
Point2D.Double worldPt = tj.getXform().calcPtFromScreenToWorld(screenPt);

// Create curve directly with world coordinates
TJPtCurve ptCurve = new TJPtCurve(worldPt,
    tj.getCanvas2D().getCurColorForPtCurve(),
    tj.getCanvas2D().getCurStrokeForPtCurve()
);
tj.getPtCurveMgr().setCurPtCurve(ptCurve);
```

**Why:** Bypasses `TJCmdToCreateCurPtCurve` which was doing a screen→world conversion (intended for normal draw scenario). For emoji, we do the conversion here directly, ensuring the curve starts with properly-transformed world coordinates.

#### 2. `handleMouseDrag()` - Add Points in World Coordinates
**Before:**
```java
if (mEmojiCircle != null && mEmojiCircle.contains(screenPt)) {
    TJCmdToUpdateCurPtCurve.execute(tj, screenPt);  // screen coords + page bounds checking
}
```

**After:**
```java
// Only draw inside the circle boundary (screen space check)
if (mEmojiCircle == null || !mEmojiCircle.contains(screenPt)) {
    return;
}

// Convert screen coordinates to world coordinates
Point2D.Double worldPt = tj.getXform().calcPtFromScreenToWorld(screenPt);

TJPtCurve curPtCurve = tj.getPtCurveMgr().getCurPtCurve();
if (curPtCurve == null) return;

// Check minimum distance between points (in world space)
int size = curPtCurve.getPts().size();
if (size > 0) {
    Point2D.Double lastWorldPt = curPtCurve.getPts().get(size - 1);
    if (worldPt.distance(lastWorldPt) < TJPtCurve.MIN_DIST_BTWN_PTS) {
        return;
    }
}

// Add point in world coordinates
curPtCurve.addPt(worldPt);
```

**Why:**
- Circle boundary check stays in **screen space** (where the circle is defined)
- Point distance checking is now **in world space** (more accurate for the actual curve)
- Points are added to the curve **in world coordinates** (matching how normal drawing works)
- No page bounds clipping (emoji circle spans both pages - it doesn't care about the divider)

#### 3. `getReady()` - Convert Initial Point to World Coordinates
**Before:**
```java
TJCmdToCreateCurPtCurve.execute(tj, scenario.mLastMousePoint);
```

**After:**
```java
Point2D.Double worldPt = tj.getXform().calcPtFromScreenToWorld(scenario.mLastMousePoint);
TJPtCurve ptCurve = new TJPtCurve(worldPt,
    tj.getCanvas2D().getCurColorForPtCurve(),
    tj.getCanvas2D().getCurStrokeForPtCurve()
);
tj.getPtCurveMgr().setCurPtCurve(ptCurve);
```

**Why:** Ensures the transition curve is also created with world coordinates.

## Technical Details

### Coordinate Systems

**Screen Coordinates:**
- Origin at top-left of canvas
- Range: [0, canvasWidth] × [0, canvasHeight]
- Used for mouse input, UI boundaries, the emoji circle visual boundary

**World Coordinates:**
- Logical drawing space with zoom/pan transforms
- The transform chain: `World → Screen` and `Screen → World`
- Applied via `TJ.getXform().getCurrentXformFromScreenToWorld()` and friends

### Why This Works

1. **Mouse input** (screen) → **convert to world** → **store in curve**
2. **Rendering pipeline** applies world→screen transform to draw the curve
3. **Circle spanning both pages** now works because:
   - Points are stored in world space (cross-page divider naturally)
   - The page divider boundary is ignored (emoji page doesn't split left/right)
   - No clipping occurs at x = WORLD_PAGE_WIDTH

### Comparison with Normal Drawing

**Normal Draw Scenario:**
```
MouseEvent (screen) 
  → TJCmdToUpdateCurPtCurve (converts screen→world)
  → TJDrawScenario.getTargetBounds() (checks page boundaries)
  → Page-relative coordinate space (left or right)
  → Stored with page clipping
```

**Emoji Drawing (Fixed):**
```
MouseEvent (screen)
  → Convert to world directly (screen→world transform)
  → Check circle boundary (screen space - visual constraint)
  → Store in world coordinates (unified space, no page split)
  → Rendered in world space (curves span both pages)
```

## Testing

After this fix:
- ✅ Drawing should work continuously from left to right inside the emoji circle
- ✅ No invisible boundary at the center
- ✅ Curves saved to the emoji page in world coordinates
- ✅ Curves render correctly at any zoom/pan level
- ✅ Circle boundary remains enforced (can't draw outside visually)

## Related Code

- `TJXform.calcPtFromScreenToWorld()` - Screen to world coordinate transformation
- `TJPtCurve` - Stores points in world coordinates
- `TJEmojiPage` - Container for curves (compatible with world-space points)
- `TJCanvas2D.paintComponent()` - Applies world↔screen transforms during rendering
