# Preset Decoration Feature - Quick Start Code Snippets

## Step 1: Extend TJEmojiPage Data Model

**File: `src/tj/TJEmojiPage.java`**

Add these fields and methods to the class:

```java
// Add to field declarations section
private ArrayList<TJImage> mDecorations;

// Add getter
public ArrayList<TJImage> getDecorations() {
    return this.mDecorations;
}

// Add method to add decoration
public void addDecoration(TJImage decoration) {
    if (decoration == null) return;
    this.mDecorations.add(decoration);
}

// Add method to remove decoration
public void removeDecoration(TJImage decoration) {
    this.mDecorations.remove(decoration);
}
```

Update the private constructor:

```java
private TJEmojiPage(boolean isEditable) {
    this.mPtCurves = new ArrayList<>();
    this.mSelectedPtCurves = new ArrayList<>();
    this.mDecorations = new ArrayList<>();  // ADD THIS LINE
    this.mIsEditable = isEditable;
}
```

Update the `isContentEmpty()` method:

```java
public boolean isContentEmpty() {
    if (!this.mPtCurves.isEmpty()) return false;
    if (!this.mImages.isEmpty()) return false;
    if (!this.mDecorations.isEmpty()) return false;  // ADD THIS LINE
    return true;
}
```

---

## Step 2: Create TJDecorationDragMgr

**File: `src/tj/TJDecorationDragMgr.java`** (NEW)

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
    private TJImage mDraggedDecoration = null;
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
        System.out.println("[DECO-DRAG] Started dragging preset: " + preset.name);
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
        double scale = 0.3;  // Start with 30% scale
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
        System.out.println("[DECO-DRAG] Started dragging decoration on canvas");
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

## Step 3: Create TJDecorationPresetPanel

**File: `src/utils/TJDecorationPresetPanel.java`** (NEW)

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
    private static final Color PANEL_BG = new Color(220, 220, 220);
    private static final Color HIGHLIGHT_COLOR = new Color(100, 150, 255, 100);
    
    // Decoration presets
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
        
        loadPresets();
        layoutPresets();
    }
    
    private void loadPresets() {
        // Try to load from assets/decorations folder
        File assetsDir = new File("assets/decorations");
        
        if (assetsDir.exists() && assetsDir.isDirectory()) {
            File[] files = assetsDir.listFiles((dir, name) -> 
                name.endsWith(".png") || name.endsWith(".jpg"));
            
            if (files != null) {
                for (File file : files) {
                    String name = file.getName().replaceFirst("[.][^.]+$", "");
                    mPresets.put(name, "assets/decorations/" + file.getName());
                }
            }
        }
        
        // Fallback presets if directory doesn't exist
        if (mPresets.isEmpty()) {
            mPresets.put("Hat 1", "assets/decorations/hat_1.png");
            mPresets.put("Glasses", "assets/decorations/glasses_1.png");
            mPresets.put("Flower", "assets/decorations/flower.png");
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
            
            // Highlight hovered
            if (icon == mHoveredIcon) {
                g2.setColor(HIGHLIGHT_COLOR);
                g2.fillRect(icon.x - 2, icon.y - 2, icon.width + 4, icon.height + 4);
            }
            
            // Highlight selected
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
                g2.drawImage(image, x, y, width, height, null);
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

## Step 4: Update TJEmojiScenario

**File: `src/tj/scenario/TJEmojiScenario.java`**

Add field to the main class (not inner class):

```java
public class TJEmojiScenario extends XScenario {
    // ... existing fields ...
    
    // ADD THIS:
    private TJDecorationDragMgr mDecorationDragMgr = new TJDecorationDragMgr();
    
    public TJDecorationDragMgr getDecorationDragMgr() {
        return mDecorationDragMgr;
    }
    
    // ... rest of existing code ...
}
```

---

## Step 5: Update EmojiDrawingScene (Inner Class)

**File: `src/tj/scenario/TJEmojiScenario.java` - EmojiDrawingScene inner class**

Add field:

```java
public static class EmojiDrawingScene extends TJScene {
    private JPanel mTopNavPanel;
    private JPanel mBottomNavPanel;
    private TJDecorationPresetPanel mDecorationPanel = null;  // ADD THIS
    
    // ... rest of existing code ...
}
```

Update `getReady()` method (add decoration panel init):

```java
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
    
    // ADD THIS BLOCK:
    if (this.mDecorationPanel == null) {
        int panelWidth = 120;
        int panelHeight = tj.getCanvas2D().getHeight();
        this.mDecorationPanel = new TJDecorationPresetPanel(panelWidth, panelHeight);
        
        this.mDecorationPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handlePresetPanelPress(e);
            }
        });
    }
    
    tj.setTopPanel(this.mTopNavPanel);
    tj.setBottomPanel(this.mBottomNavPanel);
    // ADD THIS: tj.setRightPanel(this.mDecorationPanel);
    
    initializeEmojiCircle(tj);
}
```

Update `wrapUp()` method:

```java
@Override
public void wrapUp() {
    TJ tj = (TJ)this.mScenario.getApp();
    tj.setTopPanel(null);
    tj.setBottomPanel(null);
    // ADD THIS: tj.setRightPanel(null);
}
```

Add new method (helper for preset panel):

```java
private void handlePresetPanelPress(MouseEvent e) {
    TJDecorationPresetPanel panel = (TJDecorationPresetPanel) e.getSource();
    TJDecorationPresetPanel.PresetIcon preset = panel.getPresetAtPoint(e.getX(), e.getY());
    
    if (preset != null) {
        TJEmojiScenario scenario = (TJEmojiScenario)this.mScenario;
        scenario.getDecorationDragMgr().startDragFromPreset(preset, e.getPoint());
    }
}
```

Update `handleMousePress()` (add decoration detection):

```java
@Override
public void handleMousePress(MouseEvent e) {
    TJ tj = (TJ)this.mScenario.getApp();
    TJEmojiScenario scenario = (TJEmojiScenario)this.mScenario;
    Point screenPt = e.getPoint();
    
    // ADD THIS BLOCK:
    TJEmojiPage emojiPage = scenario.getTargetEmojiPage();
    if (emojiPage != null) {
        for (TJImage decoration : emojiPage.getDecorations()) {
            if (decoration.contains(screenPt)) {
                scenario.getDecorationDragMgr().startDragDecoration(decoration, screenPt);
                System.out.println("[EMOJI-DRAW] Started dragging existing decoration");
                return;
            }
        }
    }
    
    // ... rest of original handleMousePress code ...
}
```

Update `handleMouseDrag()` (add decoration dragging):

```java
@Override
public void handleMouseDrag(MouseEvent e) {
    TJ tj = (TJ)this.mScenario.getApp();
    TJEmojiScenario scenario = (TJEmojiScenario)this.mScenario;
    Point screenPt = e.getPoint();
    
    // ADD THIS BLOCK:
    if (scenario.getDecorationDragMgr().isDecorationBeingDragged() &&
        scenario.getDecorationDragMgr().getDraggedDecoration() != null) {
        scenario.getDecorationDragMgr().dragDecorationOnCanvas(tj, screenPt);
        tj.getCanvas2D().repaint();
        return;
    }
    
    if (scenario.getDecorationDragMgr().getDraggedPreset() != null) {
        scenario.getDecorationDragMgr().dragPresetOverCanvas(screenPt);
        tj.getCanvas2D().repaint();
        return;
    }
    
    // ... rest of original handleMouseDrag code ...
}
```

Update `handleMouseRelease()` (add decoration drop logic):

```java
@Override
public void handleMouseRelease(MouseEvent e) {
    TJ tj = (TJ)this.mScenario.getApp();
    TJEmojiScenario scenario = (TJEmojiScenario)this.mScenario;
    Point screenPt = e.getPoint();
    
    TJDecorationDragMgr dragMgr = scenario.getDecorationDragMgr();
    
    // ADD THIS BLOCK:
    if (dragMgr.isDecorationBeingDragged() && dragMgr.getDraggedPreset() != null) {
        dragMgr.dropPresetOntoCanvas(tj, screenPt);
        tj.getCanvas2D().repaint();
        return;
    }
    
    if (dragMgr.isDecorationBeingDragged() && dragMgr.getDraggedDecoration() != null) {
        dragMgr.dropDecoration();
        tj.getCanvas2D().repaint();
        return;
    }
    
    // ... rest of original handleMouseRelease code ...
}
```

Update `renderWorldObjects()` (add decoration rendering):

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

    // Setup clipping
    Shape originalClip = g2.getClip();
    g2.setClip(circle);
    
    // Draw circle background
    g2.setColor(scenario.getEmojiFaceColor());
    g2.fill(circle);
    g2.setColor(Color.DARK_GRAY);
    g2.setStroke(new BasicStroke(2));
    g2.draw(circle);
    
    // Draw base features
    canvas.drawPtCurves(g2, emojiPage.getPtCurves());
    canvas.drawSelectedPtCurves(g2, emojiPage.getSelectedPtCurves());
    canvas.drawCurPtCurve(g2);
    
    // ADD THIS BLOCK: Draw decorations on top
    for (TJImage decoration : emojiPage.getDecorations()) {
        decoration.draw(g2);
        
        if (decoration == scenario.getDecorationDragMgr().getDraggedDecoration()) {
            g2.setColor(new Color(100, 150, 255, 100));
            g2.fillRect((int)decoration.getX() - 40, (int)decoration.getY() - 40, 80, 80);
        }
    }
    
    g2.setClip(originalClip);
}
```

---

## Step 6: Update TJ.java (Add Right Panel Support)

**File: `src/tj/TJ.java`**

Add field:

```java
public class TJ extends XApp {
    // ... existing fields ...
    
    private JPanel mRightPanel = null;  // ADD THIS
    
    // Add getter and setter:
    public JPanel getRightPanel() {
        return this.mRightPanel;
    }
    
    public void setRightPanel(JPanel panel) {
        this.mRightPanel = panel;
        // Update your layout manager here based on your current UI structure
        // Example for BorderLayout:
        // getContentPane().add(panel, BorderLayout.EAST);
        // this.revalidate();
        // this.repaint();
    }
}
```

---

## Step 7: Create Asset Folder Structure

Create this folder in your project root:

```
assets/
└── decorations/
    ├── hat_1.png           (transparent PNG)
    ├── hat_2.png
    ├── glasses_1.png
    ├── glasses_2.png
    ├── flower.png
    ├── star.png
    ├── bow.png
    └── crown.png
```

**Important**: Use transparent PNG images (with alpha channel) for best results. Images should be roughly 64x64 to 128x128 pixels.

---

## Testing Checklist

After implementing, test these scenarios:

- [ ] Preset panel appears on right side of emoji drawing screen
- [ ] Can click and drag preset icon from panel
- [ ] Dragged preset shows visual feedback while dragging
- [ ] Decoration appears on canvas when released over emoji circle
- [ ] Decoration stays inside emoji circle boundary
- [ ] Can click and drag decoration after placement to reposition it
- [ ] Multiple decorations can be added to same emoji
- [ ] Decorations are saved/loaded with emoji page
- [ ] No errors in console during drag operations
- [ ] Emoji features (curves) render correctly under decorations

---

## Common Issues & Solutions

### Issue: Preset panel not appearing
**Solution**: Check that `setRightPanel()` is implemented correctly in TJ.java and your layout manager includes the right panel.

### Issue: Decorations disappearing outside circle
**Solution**: Ensure `g2.setClip(circle)` is called before drawing decorations and `g2.setClip(originalClip)` is called after.

### Issue: Drag position is offset
**Solution**: Verify coordinate transformation in `dragDecorationOnCanvas()` - check that `screenToWorldScale` is correctly calculated.

### Issue: Decoration not responding to clicks
**Solution**: Ensure `decoration.contains(screenPt)` is called in `handleMousePress()` before trying to drag.

### Issue: Assets not loading
**Solution**: Verify path is correct - should be `assets/decorations/filename.png` relative to project root. Check ImageLoader.loadImage() works with that path.
