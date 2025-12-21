package tj;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;
import utils.TJDecorationPresetPanel.PresetIcon;
import tj.scenario.TJEmojiScenario;

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
        AffineTransform worldToScreenTransform = xform.getCurrentXformFromWorldToScreen();
        double screenToWorldScale = 1.0 / worldToScreenTransform.getScaleX();
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
