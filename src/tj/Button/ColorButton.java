package tj.Button;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import utils.ImageLoader;

public class ColorButton implements Button {
    protected int marginX;
    protected int marginY;
    protected double sca;
    protected BufferedImage img;

    // Anchor type: e.g. "bottom-right", "top-left", etc.
    public enum Anchor { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }
    private Anchor anchor;

    public ColorButton(int marginX, int marginY, Anchor anchor) {
        img = ImageLoader.loadImage("assets/ColorButton.png");
        this.marginX = marginX;
        this.marginY = marginY;
        this.anchor = anchor;
        this.sca = 0.1;
    }

    private int panelW = 800;
    private int panelH = 572;
    
    public void setPanelDimensions(int w, int h) {
        this.panelW = w;
        this.panelH = h;
    }

    @Override
    public void draw(Graphics2D g2) {
        // save old transform
        AffineTransform old = g2.getTransform();
        // g2.setTransform(new AffineTransform()); // reset for screen-space drawing

        if (img == null) {
            g2.setTransform(old);
            return;
        }

        int w = (int)(img.getWidth() * sca);
        int h = (int)(img.getHeight() * sca);

        // Compute anchored screenX/screenY - this is where we translate to
        int screenX = switch (anchor) {
            case TOP_LEFT     -> marginX + w;
            case TOP_RIGHT    -> panelW - marginX;
            case BOTTOM_LEFT  -> marginX + w;
            case BOTTOM_RIGHT -> panelW - marginX;
        };

        int screenY = switch (anchor) {
            case TOP_LEFT     -> marginY + h;
            case TOP_RIGHT    -> marginY + h;
            case BOTTOM_LEFT  -> panelH - marginY;
            case BOTTOM_RIGHT -> panelH - marginY;
        };

        // Store for click detection
        this.lastScreenX = screenX;
        this.lastScreenY = screenY;
        this.lastWidth = w;
        this.lastHeight = h;

        // Draw image (your original anchor style: bottom-right relative)
        // Translate to screen position, then offset by half the width/height to center it there
        g2.translate(screenX, screenY);
        g2.scale(sca, sca);
        g2.drawImage(img, -img.getWidth(), -img.getHeight(), null);

        g2.setTransform(old); // restore
    }
    
    private int lastScreenX = 0;
    private int lastScreenY = 0;
    private int lastWidth = 0;
    private int lastHeight = 0;

    // Correct click detection including dynamic screenX/screenY
    public boolean clicked(int x, int y, int panelW, int panelH) {
        // Use stored values from last draw call
        boolean isClicked = x >= (lastScreenX - lastWidth) &&
               x <= lastScreenX &&
               y >= (lastScreenY - lastHeight) &&
               y <= lastScreenY;
        
        System.out.println("ColorButton.clicked() - x=" + x + ", y=" + y + 
            ", lastScreenX=" + lastScreenX + ", lastScreenY=" + lastScreenY +
            ", lastWidth=" + lastWidth + ", lastHeight=" + lastHeight +
            ", panelW=" + panelW + ", panelH=" + panelH +
            ", isClicked=" + isClicked);
        
        return isClicked;
    }
}

