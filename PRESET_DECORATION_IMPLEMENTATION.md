# Preset Decoration Feature Implementation Guide

## Overview
This guide explains how to implement a **right-side decoration panel** that allows users to drag-and-drop preset decorative assets (hats, glasses, accessories) onto an emoji canvas. The decorations will appear layered on top of the emoji, remain movable, and won't affect the emoji itself.

---

## Architecture Overview

### Current System Context
- **TJEmojiScenario**: Manages emoji drawing scenes (EmojiDrawScene and EmojiDrawingScene)
- **TJEmojiPage**: Data model for emoji content (curves, selected curves, and images)
- **TJImage**: Existing image class with position, scale, rotation, and serialization support
- **TJCanvas2D**: Rendering engine with world-to-screen transform support
- **Command Pattern**: Uses `XCmdToChangeScene` and command classes for state management

### Key Integration Points
1. **TJEmojiPage** stores decorations as `TJImage` objects (already has image support!)
2. **TJEmojiScenario.EmojiDrawingScene** renders the emoji circle
3. **Rendering pipeline** uses clipping to keep content inside the circle
4. **Mouse events** handled through XScenario framework

---

## Implementation Strategy

### Phase 1: Data Model & Storage

#### Add Decoration Support to TJEmojiPage
In [TJEmojiPage.java](src/tj/TJEmojiPage.java), add a new field for decorations:

```java
// In TJEmojiPage class
private ArrayList<TJImage> mDecorations;

public ArrayList<TJImage> getDecorations() {
    return this.mDecorations;
}

public void addDecoration(TJImage decoration) {
    if (decoration == null) return;
    this.mDecorations.add(decoration);
}

public void removeDecoration(TJImage decoration) {
    this.mDecorations.remove(decoration);
}
```

Update the constructor:
```java
private TJEmojiPage(boolean isEditable) {
    this.mPtCurves = new ArrayList<>();
    this.mSelectedPtCurves = new ArrayList<>();
    this.mDecorations = new ArrayList<>();  // NEW
    this.mIsEditable = isEditable;
}
```

Update `isContentEmpty()`:
```java
public boolean isContentEmpty() {
    if (!this.mPtCurves.isEmpty()) return false;
    if (!this.mImages.isEmpty()) return false;
    if (!this.mDecorations.isEmpty()) return false;  // NEW
    return true;
}
```

---

### Phase 2: UI Panel Creation

#### Create Decoration Preset Panel Component
Create a new file: **TJDecorationPresetPanel.java** in `src/utils/`

```java
package utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class TJDecorationPresetPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    
    // Panel dimensions
    private static final int PRESET_ICON_SIZE = 64;
    private static final int PRESET_PADDING = 8;
    private static final int PRESET_GRID_COLS = 1;  // Single column for right panel
    private static final Color PANEL_BG = new Color(220, 220, 220);
    private static final Color HIGHLIGHT_COLOR = new Color(100, 150, 255, 100);
    
    // Decoration presets: name -> image path
    private Map<String, String> mPresets;
    private ArrayList<PresetIcon> mPresetIcons;
    private PresetIcon mHoveredIcon = null;
    private PresetIcon mSelectedIcon = null;
    
    public TJDecorationPresetPanel(int width, int height) {
        setPreferredSize(new Dimension(width, height));
        setBackground(PANEL_BG);
        setBorder(new LineBorder(Color.GRAY, 1));
        
        mPresets = new HashMap<>();
        mPresetIcons = new ArrayList<>();
        
        // Load preset decorations from assets folder
        loadPresets();
        layoutPresets();
    }
    
    private void loadPresets() {
        // You can configure these presets - adjust path based on your assets structure
        // Format: name -> relative path to decoration asset
        File assetsDir = new File("assets/decorations");
        
        if (assetsDir.exists() && assetsDir.isDirectory()) {
            // Auto-load all images from assets/decorations folder
            for (File file : assetsDir.listFiles((dir, name) -> 
                name.endsWith(".png") || name.endsWith(".jpg"))) {
                String name = file.getName().replaceFirst("[.][^.]+$", "");
                mPresets.put(name, "assets/decorations/" + file.getName());
            }
        } else {
            // Fallback: load hardcoded presets
            mPresets.put("Hat 1", "assets/decorations/hat_1.png");
            mPresets.put("Glasses 1", "assets/decorations/glasses_1.png");
            mPresets.put("Flower", "assets/decorations/flower.png");
            mPresets.put("Star", "assets/decorations/star.png");
        }
    }
    
    private void layoutPresets() {
        int x = PRESET_PADDING;
        int y = PRESET_PADDING;
        
        for (String presetName : mPresets.keySet()) {
            String imagePath = mPresets.get(presetName);
            BufferedImage img = ImageLoader.loadImage(imagePath);
            
            if (img != null) {
                PresetIcon icon = new PresetIcon(presetName, img, x, y, 
                    PRESET_ICON_SIZE, PRESET_ICON_SIZE);
                mPresetIcons.add(icon);
                y += PRESET_ICON_SIZE + PRESET_PADDING;
            }
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        
        // Draw all preset icons
        for (PresetIcon icon : mPresetIcons) {
            icon.draw(g2);
            
            // Highlight hovered icon
            if (icon == mHoveredIcon) {
                g2.setColor(HIGHLIGHT_COLOR);
                g2.fillRect(icon.x - 2, icon.y - 2, icon.width + 4, icon.height + 4);
            }
            
            // Highlight selected icon
            if (icon == mSelectedIcon) {
                g2.setColor(Color.ORANGE);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRect(icon.x - 2, icon.y - 2, icon.width + 4, icon.height + 4);
            }
        }
    }
    
    public PresetIcon getPresetAtPoint(int x, int y) {
        for (PresetIcon icon : mPresetIcons) {
            if (icon.contains(x, y)) {
                return icon;
            }
        }
        return null;
    }
    
    public void setHoveredIcon(PresetIcon icon) {
        mHoveredIcon = icon;
        repaint();
    }
    
    public void setSelectedIcon(PresetIcon icon) {
        mSelectedIcon = icon;
        repaint();
    }
    
    public PresetIcon getSelectedIcon() {
        return mSelectedIcon;
    }
    
    // ==================== INNER CLASS ====================
    
    public static class PresetIcon {
        public String name;
        public BufferedImage image;
        public int x, y, width, height;
        
        public PresetIcon(String name, BufferedImage img, int x, int y, int w, int h) {
            this.name = name;
            this.image = img;
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
        }
        
        public void draw(Graphics2D g2) {
            if (image != null) {
                // Scale image to fit the icon size
                g2.drawImage(image, x, y, width, height, null);
                
                // Border
                g2.setColor(Color.GRAY);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRect(x, y, width, height);
            }
        }
        
        public boolean contains(int px, int py) {
            return px >= x && px < x + width && py >= y && py < y + height;
        }
    }
}
```

---

### Phase 3: Drag-and-Drop Logic

#### Create Decoration Drag Manager
Create a new file: **TJDecorationDragMgr.java** in `src/tj/`

```java
package tj;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.geom.Point2D;
import utils.TJDecorationPresetPanel.PresetIcon;

public class TJDecorationDragMgr {
    // Drag state
    private boolean mIsDecorationBeingDragged = false;
    private PresetIcon mDraggedPreset = null;
    private TJImage mDraggedDecoration = null;  // Decoration being moved after placement
    private Point mLastMouseScreenPoint = null;
    
    public boolean isDecorationBeingDragged() {
        return mIsDecorationBeingDragged;
    }
    
    public PresetIcon getDraggedPreset() {
        return mDraggedPreset;
    }
    
    public TJImage getDraggedDecoration() {
        return mDraggedDecoration;
    }
    
    // ==================== DRAG FROM PRESET PANEL ====================
    
    public void startDragFromPreset(PresetIcon preset, Point screenPoint) {
        mIsDecorationBeingDragged = true;
        mDraggedPreset = preset;
        mDraggedDecoration = null;
        mLastMouseScreenPoint = new Point(screenPoint);
    }
    
    public void dragPresetOverCanvas(Point screenPoint) {
        if (mIsDecorationBeingDragged && mDraggedPreset != null) {
            mLastMouseScreenPoint = new Point(screenPoint);
        }
    }
    
    public void dropPresetOntoCanvas(TJ tj, Point screenPoint) {
        if (!mIsDecorationBeingDragged || mDraggedPreset == null) {
            return;
        }
        
        // Get emoji page
        TJEmojiScenario scenario = TJEmojiScenario.getSingle();
        TJEmojiPage emojiPage = scenario.getTargetEmojiPage();
        
        if (emojiPage == null) {
            System.out.println("[DECO-DRAG] No active emoji page");
            endDragSession();
            return;
        }
        
        // Load the preset image
        BufferedImage presetImg = mDraggedPreset.image;
        
        if (presetImg == null) {
            System.out.println("[DECO-DRAG] Could not load preset image");
            endDragSession();
            return;
        }
        
        // Convert screen point to world point
        Point2D.Double worldPoint = new Point2D.Double();
        tj.getXform().getCurrentXformFromScreenToWorld()
            .transform(new Point2D.Double(screenPoint.x, screenPoint.y), worldPoint);
        
        // Create decoration image with initial properties
        double scale = 0.3;  // Start with 30% scale relative to image size
        TJImage decoration = new TJImage(presetImg, (int)worldPoint.x, (int)worldPoint.y, scale);
        
        // Add decoration to emoji page
        emojiPage.addDecoration(decoration);
        
        System.out.println("[DECO-DRAG] Added decoration at (" + worldPoint.x + ", " + worldPoint.y + ")");
        
        endDragSession();
    }
    
    // ==================== DRAG DECORATION ON CANVAS ====================
    
    public void startDragDecoration(TJImage decoration, Point screenPoint) {
        mIsDecorationBeingDragged = true;
        mDraggedDecoration = decoration;
        mDraggedPreset = null;
        mLastMouseScreenPoint = new Point(screenPoint);
    }
    
    public void dragDecorationOnCanvas(TJ tj, Point screenPoint) {
        if (!mIsDecorationBeingDragged || mDraggedDecoration == null) {
            return;
        }
        
        // Calculate screen delta
        int dx = screenPoint.x - mLastMouseScreenPoint.x;
        int dy = screenPoint.y - mLastMouseScreenPoint.y;
        
        // Transform screen delta to world delta using the scale factor
        TJXform xform = tj.getXform();
        double screenToWorldScale = 1.0 / xform.getCurrentScale();
        double worldDx = dx * screenToWorldScale;
        double worldDy = dy * screenToWorldScale;
        
        // Update decoration position
        mDraggedDecoration.translate(worldDx, worldDy);
        
        mLastMouseScreenPoint = new Point(screenPoint);
    }
    
    public void dropDecoration() {
        endDragSession();
    }
    
    // ==================== UTILITY ====================
    
    public void endDragSession() {
        mIsDecorationBeingDragged = false;
        mDraggedPreset = null;
        mDraggedDecoration = null;
        mLastMouseScreenPoint = null;
    }
    
    public Point getLastMouseScreenPoint() {
        return mLastMouseScreenPoint;
    }
}
```

---

### Phase 4: Scene Integration

#### Modify TJEmojiScenario
Update [TJEmojiScenario.java](src/tj/scenario/TJEmojiScenario.java):

```java
// Add to TJEmojiScenario class
private TJDecorationDragMgr mDecorationDragMgr = new TJDecorationDragMgr();

public TJDecorationDragMgr getDecorationDragMgr() {
    return mDecorationDragMgr;
}
```

#### Modify EmojiDrawingScene (Inner Class)
In the `EmojiDrawingScene` inner class within TJEmojiScenario:

**Update `handleMousePress` to detect decoration clicks:**

```java
@Override
public void handleMousePress(MouseEvent e) {
    TJ tj = (TJ)this.mScenario.getApp();
    TJEmojiScenario scenario = (TJEmojiScenario)this.mScenario;
    Point screenPt = e.getPoint();

    // Check if clicking on a decoration (movable after placement)
    TJEmojiPage emojiPage = scenario.getTargetEmojiPage();
    if (emojiPage != null) {
        for (TJImage decoration : emojiPage.getDecorations()) {
            if (decoration.contains(screenPt)) {
                // Start dragging existing decoration
                scenario.getDecorationDragMgr().startDragDecoration(decoration, screenPt);
                System.out.println("[EMOJI-DRAW] Started dragging existing decoration");
                return;
            }
        }
    }

    // Otherwise, check if clicking on preset panel or drawing on emoji
    // (existing draw logic continues here)
    // ... rest of original handleMousePress
}
```

**Update `handleMouseDrag`:**

```java
@Override
public void handleMouseDrag(MouseEvent e) {
    TJ tj = (TJ)this.mScenario.getApp();
    TJEmojiScenario scenario = (TJEmojiScenario)this.mScenario;
    Point screenPt = e.getPoint();
    
    // If dragging a decoration on canvas, update its position
    if (scenario.getDecorationDragMgr().isDecorationBeingDragged() &&
        scenario.getDecorationDragMgr().getDraggedDecoration() != null) {
        scenario.getDecorationDragMgr().dragDecorationOnCanvas(tj, screenPt);
        tj.getCanvas2D().repaint();
        return;
    }
    
    // If dragging from preset panel, show visual feedback
    if (scenario.getDecorationDragMgr().getDraggedPreset() != null) {
        scenario.getDecorationDragMgr().dragPresetOverCanvas(screenPt);
        tj.getCanvas2D().repaint();
        return;
    }
    
    // Otherwise, existing draw logic
    // ... rest of original handleMouseDrag
}
```

**Update `handleMouseRelease`:**

```java
@Override
public void handleMouseRelease(MouseEvent e) {
    TJ tj = (TJ)this.mScenario.getApp();
    TJEmojiScenario scenario = (TJEmojiScenario)this.mScenario;
    Point screenPt = e.getPoint();
    
    TJDecorationDragMgr dragMgr = scenario.getDecorationDragMgr();
    
    // If dragging a decoration from preset panel, drop it on canvas
    if (dragMgr.isDecorationBeingDragged() && dragMgr.getDraggedPreset() != null) {
        dragMgr.dropPresetOntoCanvas(tj, screenPt);
        tj.getCanvas2D().repaint();
        return;
    }
    
    // If dragging an existing decoration, finalize its new position
    if (dragMgr.isDecorationBeingDragged() && dragMgr.getDraggedDecoration() != null) {
        dragMgr.dropDecoration();
        tj.getCanvas2D().repaint();
        return;
    }
    
    // Otherwise, existing release logic
    // ... rest of original handleMouseRelease
}
```

---

### Phase 5: Rendering & Layering

#### Update EmojiDrawingScene Rendering
Modify the `renderWorldObjects` method in `EmojiDrawingScene`:

```java
@Override
public void renderWorldObjects(Graphics2D g2) {
    TJ tj = (TJ)this.mScenario.getApp();
    TJCanvas2D canvas = tj.getCanvas2D();
    TJEmojiScenario scenario = (TJEmojiScenario)this.mScenario;

    Ellipse2D.Double circle = scenario.getTargetBounds();
    if(circle == null) return;

    TJEmojiPage emojiPage = scenario.getTargetEmojiPage();
    if(emojiPage == null) return;

    // Setup clipping to emoji circle
    Shape originalClip = g2.getClip();
    g2.setClip(circle);
    
    // 1. Draw emoji face background and border
    g2.setColor(scenario.getEmojiFaceColor());
    g2.fill(circle);
    g2.setColor(Color.DARK_GRAY);
    g2.setStroke(new BasicStroke(2));
    g2.draw(circle);
    
    // 2. Draw emoji features (curves) - base layer
    canvas.drawPtCurves(g2, emojiPage.getPtCurves());
    canvas.drawSelectedPtCurves(g2, emojiPage.getSelectedPtCurves());
    canvas.drawCurPtCurve(g2);
    
    // 3. Draw decorations on TOP (top layer)
    for (TJImage decoration : emojiPage.getDecorations()) {
        decoration.draw(g2);
        
        // Optional: Draw selection indicator for dragged decoration
        if (decoration == scenario.getDecorationDragMgr().getDraggedDecoration()) {
            g2.setColor(new Color(100, 150, 255, 100));
            g2.fillRect((int)decoration.getX() - 40, (int)decoration.getY() - 40, 80, 80);
        }
    }
    
    g2.setClip(originalClip);
}
```

#### Update Preset Panel Rendering
In `EmojiDrawingScene.renderScreenObjects`:

```java
@Override
public void renderScreenObjects(Graphics2D g2) {
    TJ tj = (TJ)this.mScenario.getApp();
    TJCanvas2D canvas = tj.getCanvas2D();
    
    // Draw preset panel on right side of screen
    if (mDecorationPanel != null) {
        // The panel is a JPanel, so it's already rendered via Swing
        // But you can add custom overlay rendering here if needed
    }
    
    // Optional: Draw visual feedback while dragging from preset
    TJEmojiScenario scenario = (TJEmojiScenario)this.mScenario;
    if (scenario.getDecorationDragMgr().getDraggedPreset() != null) {
        Point screenPt = scenario.getDecorationDragMgr().getLastMouseScreenPoint();
        if (screenPt != null) {
            BufferedImage dragImg = scenario.getDecorationDragMgr().getDraggedPreset().image;
            if (dragImg != null) {
                g2.drawImage(dragImg, screenPt.x - 32, screenPt.y - 32, 64, 64, null);
                // Semi-transparent overlay
                g2.setColor(new Color(0, 0, 0, 100));
                g2.fillRect(screenPt.x - 32, screenPt.y - 32, 64, 64);
            }
        }
    }
}
```

---

### Phase 6: Panel Integration into Main Layout

#### Modify EmojiDrawingScene to Add Decoration Panel
In the `getReady()` method of `EmojiDrawingScene`:

```java
private TJDecorationPresetPanel mDecorationPanel = null;

@Override
public void getReady() {
    TJ tj = (TJ)this.mScenario.getApp();
    TJEmojiScenario scenario = (TJEmojiScenario)this.mScenario;
    
    if (this.mTopNavPanel == null) {
        initializeTopNav();
    }
    if (this.mBottomNavPanel == null) {
        initializeBottomNav();
    }
    
    // Initialize decoration preset panel
    if (this.mDecorationPanel == null) {
        int panelWidth = 120;  // Right side panel width
        int panelHeight = tj.getCanvas2D().getHeight();
        this.mDecorationPanel = new TJDecorationPresetPanel(panelWidth, panelHeight);
        
        // Add event listeners for preset interaction
        this.mDecorationPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handlePresetPanelPress(e);
            }
        });
    }
    
    tj.setTopPanel(this.mTopNavPanel);
    tj.setBottomPanel(this.mBottomNavPanel);
    tj.setRightPanel(this.mDecorationPanel);  // Add right panel
    
    initializeEmojiCircle(tj);
}

@Override
public void wrapUp() {
    TJ tj = (TJ)this.mScenario.getApp();
    tj.setTopPanel(null);
    tj.setBottomPanel(null);
    tj.setRightPanel(null);  // Clear right panel
}

private void handlePresetPanelPress(MouseEvent e) {
    TJDecorationPresetPanel panel = (TJDecorationPresetPanel) e.getSource();
    PresetIcon preset = panel.getPresetAtPoint(e.getX(), e.getY());
    
    if (preset != null) {
        TJEmojiScenario scenario = (TJEmojiScenario)this.mScenario;
        scenario.getDecorationDragMgr().startDragFromPreset(preset, e.getPoint());
        System.out.println("[EMOJI-DRAW] Started dragging preset: " + preset.name);
    }
}
```

#### Update TJ.java to Support Right Panel
Add to [TJ.java](src/tj/TJ.java):

```java
private JPanel mRightPanel = null;

public JPanel getRightPanel() {
    return this.mRightPanel;
}

public void setRightPanel(JPanel panel) {
    this.mRightPanel = panel;
    // Update layout in your main frame
    // This depends on your current layout architecture
    this.revalidate();
    this.repaint();
}
```

---

## File Assets Structure

Create this directory structure for decorations:

```
assets/
├── decorations/
│   ├── hat_1.png           (64x64 - 128x128 recommended)
│   ├── hat_2.png
│   ├── glasses_1.png
│   ├── glasses_2.png
│   ├── flower.png
│   ├── star.png
│   ├── bow.png
│   ├── crown.png
│   ├── sunglasses.png
│   └── ... (add more as needed)
```

**Important**: All decoration images should:
- Have transparent backgrounds (PNG with alpha channel)
- Be roughly 64x64 to 128x128 pixels
- Use the same scale for visual consistency
- Have clear subject (decoration) centered

---

## Implementation Checklist

### Data Model
- [ ] Add `mDecorations` field to `TJEmojiPage`
- [ ] Add getter/setter methods for decorations
- [ ] Update `isContentEmpty()` to include decorations
- [ ] Ensure decorations serialize with the emoji page

### UI Components
- [ ] Create `TJDecorationPresetPanel.java`
- [ ] Create `PresetIcon` inner class
- [ ] Implement `loadPresets()` from assets folder
- [ ] Implement preset icon rendering and layout

### Drag-and-Drop Logic
- [ ] Create `TJDecorationDragMgr.java`
- [ ] Implement `startDragFromPreset()` and `dropPresetOntoCanvas()`
- [ ] Implement `startDragDecoration()` and `dragDecorationOnCanvas()`
- [ ] Handle world-to-screen coordinate transforms

### Scene Integration
- [ ] Add `TJDecorationDragMgr` instance to `TJEmojiScenario`
- [ ] Update `EmojiDrawingScene.handleMousePress()` for decoration detection
- [ ] Update `EmojiDrawingScene.handleMouseDrag()` for drag feedback
- [ ] Update `EmojiDrawingScene.handleMouseRelease()` for drop logic
- [ ] Add preset panel to `EmojiDrawingScene.getReady()`

### Rendering
- [ ] Update `renderWorldObjects()` to draw decorations on top with clipping
- [ ] Update `renderScreenObjects()` to show drag preview
- [ ] Add selection visual feedback during drag

### Layout
- [ ] Modify `TJ.java` to support right panel
- [ ] Integrate `TJDecorationPresetPanel` into main layout
- [ ] Adjust canvas size to accommodate panel

### Assets
- [ ] Create `assets/decorations/` folder
- [ ] Add preset decoration images (PNG with transparency)

---

## Key Design Decisions

### Why `TJImage` for Decorations?
`TJImage` already has everything you need:
- Position (x, y) at center
- Scale factor
- Rotation support
- Serialization support (PNG bytes)
- Hit detection (`contains()` method)
- Drawing with transforms

### Why Separate List from Images?
Keeping `mDecorations` separate from `mImages` provides:
- Clear intent (these are layered on emoji, not separate page elements)
- Easy filtering for save/export
- Logical grouping for state management

### Clipping to Emoji Circle
By clipping to the `Ellipse2D.Double` circle:
- Decorations are visually contained within emoji boundary
- No need to modify individual decoration position logic
- Prevents visual overflow outside intended area

### Drag-and-Drop Phases
1. **Start drag** from preset panel: Record preset and screen position
2. **Drag on canvas**: Update mouse position, show feedback
3. **Drop**: Convert screen point to world point, create decoration
4. **Reposition**: Click decoration to drag it; uses same mechanisms

---

## Future Enhancements

1. **Decoration Properties Panel**: Show decoration list, allow deletion, scaling, rotation
2. **Undo/Redo**: Implement commands like `TJCmdToAddDecoration`, `TJCmdToRemoveDecoration`
3. **Preset Categories**: Organize decorations (hats, accessories, etc.)
4. **Preset Editor**: Create/save custom decorations
5. **Flip/Mirror**: Add horizontal flip support for decorations
6. **Z-Order Control**: Allow users to change decoration stacking order
7. **Decoration Snap**: Snap to common positions (top, sides, etc.)
8. **Export with Decorations**: Include decorations when exporting emoji

---

## Testing Checklist

- [ ] Load decoration presets from assets folder
- [ ] Preset icons display correctly in right panel
- [ ] Can drag preset from panel to emoji canvas
- [ ] Decoration appears at drop location
- [ ] Decoration stays within emoji circle boundary
- [ ] Can drag decoration after placement
- [ ] Multiple decorations can be added
- [ ] Decorations appear on top of emoji features
- [ ] Emoji saves and loads with decorations
- [ ] Emoji exports with decorations included
- [ ] Visual feedback during drag (cursor change, highlight)

---

## Summary

This implementation leverages your existing architecture:
- **Data**: Uses `TJImage` which already handles transform, scale, rotation, and serialization
- **Rendering**: Extends existing `TJEmojiPage` to store decorations; they're rendered in `EmojiDrawingScene`
- **Interaction**: Extends mouse event handling in `EmojiDrawingScene`; reuses command pattern via `TJEmojiScenario`
- **UI**: Adds a simple right panel with preset icons; no major layout changes needed

The key insight is that **decorations are just `TJImage` objects layered on top of the emoji**, managed by the emoji page, and rendered last (on top).
