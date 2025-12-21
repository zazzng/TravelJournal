package utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
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
    
    // Static reference for tracking dragged preset across all panels
    public static PresetIcon sCurrentDraggedPreset = null;
    
    // Decoration presets
    private Map<String, String> mPresets;
    private ArrayList<PresetIcon> mPresetIcons;
    private PresetIcon mHoveredIcon = null;
    private PresetIcon mSelectedIcon = null;
    private PresetIcon mDraggedIcon = null;
    private Point mLastMousePoint = null;
    
    // Callback for decoration creation
    public interface DecorationCreationListener {
        void onDecorationDragStart(PresetIcon preset);
        void onDecorationDragMove(Point screenPoint);
        void onDecorationDragEnd();
    }
    
    private DecorationCreationListener mDecorationListener = null;
    
    public void setDecorationListener(DecorationCreationListener listener) {
        System.out.println("[DECORATION-PANEL] setDecorationListener called! listener=" + listener + ", this panel=" + this);
        this.mDecorationListener = listener;
        System.out.println("[DECORATION-PANEL] Listener set! mDecorationListener is now: " + this.mDecorationListener);
    }
    
    public TJDecorationPresetPanel(int width, int height) {
        System.out.println("[DECORATION-PANEL] Constructor called for: " + this);
        setPreferredSize(new Dimension(width, height));
        setBackground(PANEL_BG);
        setBorder(new LineBorder(Color.GRAY, 1));
        
        mPresets = new HashMap<>();
        mPresetIcons = new ArrayList<>();
        
        loadPresets();
        layoutPresets();
        
        // Add mouse listeners for dragging
        addMouseListener(new MouseListener() {
            @Override
            public void mousePressed(MouseEvent e) {
                PresetIcon icon = getPresetAtPoint(e.getX(), e.getY());
                if (icon != null) {
                    mDraggedIcon = icon;
                    mLastMousePoint = e.getPoint();
                    setSelectedIcon(icon);
                    // Update static reference for scenario to access
                    sCurrentDraggedPreset = icon;
                    
                    // Notify listener about drag start
                    if (mDecorationListener != null) {
                        mDecorationListener.onDecorationDragStart(icon);
                    }
                    System.out.println("[DECORATION-PANEL] Started dragging: " + icon.name + " (static set to " + sCurrentDraggedPreset.name + ")");
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (mDraggedIcon != null) {
                    System.out.println("[DECORATION-PANEL] Released: " + mDraggedIcon.name + " at (" + e.getX() + "," + e.getY() + ")");
                    
                    // Notify listener about drag end
                    if (mDecorationListener != null) {
                        mDecorationListener.onDecorationDragEnd();
                    }
                    
                    // Only clear the static field if release is within panel bounds
                    // If release is outside panel (on canvas), leave it for canvas to process
                    if (e.getX() >= 0 && e.getX() < getWidth() && e.getY() >= 0 && e.getY() < getHeight()) {
                        System.out.println("[DECORATION-PANEL] Release was within panel bounds - clearing static field");
                        sCurrentDraggedPreset = null;
                    } else {
                        System.out.println("[DECORATION-PANEL] Release was OUTSIDE panel bounds - leaving static field for canvas to process");
                    }
                    mDraggedIcon = null;
                }
                mLastMousePoint = null;
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {}
            
            @Override
            public void mouseEntered(MouseEvent e) {}
            
            @Override
            public void mouseExited(MouseEvent e) {}
        });
        
        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (mDraggedIcon != null) {
                    mLastMousePoint = e.getPoint();
                    System.out.println("[DECORATION-PANEL] mouseDragged: e.getPoint()=" + e.getPoint() + ", listener=" + mDecorationListener);
                    
                    // Notify listener about the drag movement
                    if (mDecorationListener != null) {
                        try {
                            java.awt.Point panelLocation = getLocationOnScreen();
                            java.awt.Point screenPoint = new java.awt.Point(
                                panelLocation.x + e.getX(),
                                panelLocation.y + e.getY()
                            );
                            System.out.println("[DECORATION-PANEL] Calling onDecorationDragMove with screen point: " + screenPoint + " (panel location=" + panelLocation + ", e.getPoint()=" + e.getPoint() + ")");
                            mDecorationListener.onDecorationDragMove(screenPoint);
                        } catch (Exception ex) {
                            System.out.println("[DECORATION-PANEL] Error in mouseDragged: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                    } else {
                        System.out.println("[DECORATION-PANEL] mDecorationListener is NULL!");
                    }
                }
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {
                PresetIcon icon = getPresetAtPoint(e.getX(), e.getY());
                setHoveredIcon(icon);
            }
        });
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
    
    public PresetIcon getDraggedIcon() {
        return mDraggedIcon;
    }
    
    public Point getLastMousePoint() {
        return mLastMousePoint;
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
